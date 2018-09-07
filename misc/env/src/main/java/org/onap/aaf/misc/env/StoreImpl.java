/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.misc.env;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.onap.aaf.misc.env.util.Split;

import java.util.Properties;


public class StoreImpl implements Store {
    /*
     * The re-adjustment factor for growing the Static State array. 
     */
    private static final int growSize = 10;
    
    /*
     * The index reference for Slot assignment.
     */
    private int local;
    
    /*
     * The index reference for StaticSlot assignment. 
     */
    private int stat;
    
    /*
     * The name/slot map for local (transaction specific) State.
     */
    private HashMap<String, Slot> localMap;
    
    /*
     * The name/slot map for Static State.
     */
    private HashMap<String, StaticSlot> staticMap;

    private Object[] staticState;
    
    public StoreImpl() {
         staticState = new Object[growSize];
         staticMap = new HashMap<>();
         localMap = new HashMap<>();
    }
    
    public StoreImpl(String tag) {
         staticState = new Object[growSize];
         staticMap = new HashMap<>();
         localMap = new HashMap<>();
    }

    
    public StoreImpl(String tag, String[] args) {
         staticState = new Object[growSize];
         staticMap = new HashMap<>();
         localMap = new HashMap<>();

         if(tag!=null) {
            String tequals = tag + '=';
            for(String arg : args) {
                if(arg.startsWith(tequals) && !arg.equals(tequals)) { // needs to have something after =
                    Properties props = new Properties();
                    for(String f : Split.split(File.pathSeparatorChar,arg.substring(tequals.length()))) {
                        moreProps(new File(f),props);
                    }
                    for(Entry<Object, Object> es : props.entrySet()) {
                        put(staticSlot(es.getKey().toString()),es.getValue());
                    }
                }
            }
         }

        // Make sure properties on command line override those in Props
        propsFromArgs(tag,args);
    }
    
    public StoreImpl(String tag, Properties props) {
         staticState = new Object[growSize];
         staticMap = new HashMap<>();
         localMap = new HashMap<>();
         
         if(tag!=null) {
             String fname = props.getProperty(tag);
             if(fname!=null) {
                 for(String f : Split.split(File.pathSeparatorChar,fname)) {
                     if(!moreProps(new File(f),props)) {
                        System.err.println("Unable to load Properties from " + f); 
                     }
                 }
             }
         }

         for(Entry<Object, Object> es : props.entrySet()) {
             put(staticSlot(es.getKey().toString()),es.getValue());
         }
    }

    public void propsFromArgs(String tag, String[] args) {
        if(tag!=null) {
            for(String arg : args) {
                String sarg[] = Split.split('=',arg);
                if(sarg.length==2) {
                    if(tag.equals(sarg[0])) {
                        for(String fname : Split.split(File.pathSeparatorChar,sarg[1])) {
                            moreProps(new File(fname),null /* no target */);
                        }
                    }
                    put(staticSlot(sarg[0]),sarg[1]);
                }
            }
        }
    }

    private boolean moreProps(File f, Properties target) {
         if(f.exists()) {
             Properties props = new Properties();
             try {
                 FileInputStream fis = new FileInputStream(f);
                 try {
                     props.load(fis);
                     if(target!=null) {
                         target.load(fis);
                     }
                 } finally {
                     fis.close();
                 }
             } catch(IOException e) {
                 System.err.println(e);
             }
             for(Entry<Object, Object> es : props.entrySet()) {
                 put(staticSlot(es.getKey().toString()),es.getValue());
             }
             return true;
         } else {
             return false;
         }
    }

    public Object[] newTransState() {
        return new Object[local];
    }

    /* (non-Javadoc)
     * @see com.att.env.Store#slot(java.lang.String)
     */
    public synchronized Slot slot(String name) {
        name = name == null ? "" : name.trim();
        Slot slot = localMap.get(name);
        if (slot == null)  {
            slot = new Slot(local++, name);
            localMap.put(name, slot);
        }
        return slot;
    }
    
    
    /* (non-Javadoc)
     * @see com.att.env.Store#existingSlot(java.lang.String)
     */
    public Slot existingSlot(String name) {
        return localMap.get(name);
    }
    
    /* (non-Javadoc)
     * @see com.att.env.Store#existingSlotNames()
     */
    public List<String> existingSlotNames() {
        return new ArrayList<>(localMap.keySet());
    }

    /* (non-Javadoc)
     * @see com.att.env.Store#staticSlot(java.lang.String)
     */
    public synchronized StaticSlot staticSlot(String name) {
        name = name == null ? "" : name.trim();
        StaticSlot slot = staticMap.get(name);
        if (slot == null)  {
            if (stat%growSize == 0) {
                Object[] temp = staticState;
                staticState = new Object[temp.length+growSize];
                System.arraycopy(temp, 0, staticState, 0, temp.length);
            }
            slot = new StaticSlot(stat++, name);
            staticMap.put(name, slot);
        }
        return slot;
    }
    
    /* (non-Javadoc)
     * @see com.att.env.Store#put(com.att.env.StaticSlot, java.lang.Object)
     */
    public void put(StaticSlot slot, Object value) {
        staticState[slot.slot] = value;
    }
    
    /* (non-Javadoc)
     * @see com.att.env.Store#get(com.att.env.StaticSlot T defaultObject)
     */
    @SuppressWarnings("unchecked")
    public<T> T get(StaticSlot sslot,T dflt) {
        T t = (T)staticState[sslot.slot];
        return t==null?dflt:t;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(StaticSlot sslot) {
        return (T)staticState[sslot.slot];
    }

    public List<String> existingStaticSlotNames() {
        return new ArrayList<>(staticMap.keySet());
    }
}

