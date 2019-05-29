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

package org.onap.aaf.misc.env.impl;

import java.applet.Applet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.validation.Schema;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.DataFactory;
import org.onap.aaf.misc.env.Decryptor;
import org.onap.aaf.misc.env.Encryptor;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.EnvJAXB;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.StaticSlot;
import org.onap.aaf.misc.env.StoreImpl;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.TransCreate;
import org.onap.aaf.misc.env.TransJAXB;
import org.onap.aaf.misc.env.jaxb.JAXBDF;
import org.onap.aaf.misc.env.util.Split;

/**
 * An essential Implementation of Env, which will fully function, without any sort
 * of configuration.
 * 
 * Use as a basis for Group level Env, just overriding where needed.
 * @author Jonathan
 *
 */
public class BasicEnv extends StoreImpl implements EnvJAXB, TransCreate<TransJAXB>{
    protected LogTarget fatal=LogTarget.SYSERR;
    protected LogTarget error=LogTarget.SYSERR;
    protected LogTarget audit=LogTarget.SYSOUT;
    protected LogTarget init=LogTarget.SYSOUT;
    protected LogTarget warn=LogTarget.SYSERR;
    protected LogTarget info=LogTarget.SYSOUT;
    protected LogTarget debug=LogTarget.NULL;
    protected LogTarget trace=LogTarget.NULL;
//    protected Map<String, String> props;
    
//    private boolean sysprops;

    public BasicEnv(String ... args) {
        super(null,args);
    }

    public BasicEnv(String tag, String[] args) {
        super(tag, args);
    }
    

    /**
     * Suitable for use in Applets... obtain all the values 
     * listed for the variable String arg "tags"
     */
    public BasicEnv(Applet applet, String ... tags) {
        super(null, tags);
//        props = new HashMap<>();
//        String value;
//        for (int i=0;i<tags.length;++i) {
//            value = applet.getParameter(tags[i]);
//            if (value!=null) {
//                props.put(tags[i], value);
//            }
//        }
    }

    public BasicEnv(Properties props) {
        super(null, props);
    }

    public BasicEnv(String tag, Properties props) {
        super(tag, props);
    }



    // @Override
    public LogTarget fatal() {
        return fatal;
    }

    // @Override
    public LogTarget error() {
        return error;
    }

    
    // @Override
    public LogTarget audit() {
        return audit;
    }

    // @Override
    public LogTarget init() {
        return init;
    }

    // @Override
    public LogTarget warn() {
        return warn;
    }

    // @Override
    public LogTarget info() {
        return info;
    }

    // @Override
    public LogTarget debug() {
        return debug;
    }

    public void debug(LogTarget lt) {
        debug = lt;
    }

    // @Override
    public LogTarget trace() {
        return trace;
    }

    @Override
    public TimeTaken start(String name, int flag, Object ... values) {
        return new TimeTaken(name, flag, values) {
            /**
             * Format to be printed when called upon
             */
            // @Override
            public void output(StringBuilder sb) {
    
                switch(flag) {
                    case Env.XML: sb.append("XML "); break;
                    case Env.JSON: sb.append("JSON "); break;
                    case Env.REMOTE: sb.append("REMOTE "); break;
                }
                sb.append(String.format(name, values));
                if ((flag & Env.CHECKPOINT) != Env.CHECKPOINT) {
                    sb.append(' ');
                    sb.append((end-start)/1000000f);
                    sb.append("ms");
                    if (size>=0) {
                        sb.append(" size: ");
                        sb.append(Long.toString(size));
                    }
                }
            }
        };
    }

    // @Override
    public String getProperty(String key) {
        return get(staticSlot(key),null);
    }
    
    public Properties getProperties(String ... filter) {
        Properties props = new Properties();
        boolean yes;
        for (String key : existingStaticSlotNames()) {
            if (filter.length>0) {
                yes = false;
                for (String f : filter) {
                    if (key.startsWith(f)) {
                        yes = true;
                        break;
                    }
                }
            } else {
                yes = true;
            }
            if (yes) {
                String value = getProperty(key);
                if (value!=null) {
                    props.put(key, value);
                }
            }
        }
        return props;
    }
    
    // @Override
    public String getProperty(String key, String defaultValue) {
        return get(staticSlot(key),defaultValue);
    }

    // @Override
    public String setProperty(String key, String value) {
        put(staticSlot(key),value==null?null:value.trim());
        return value;
    }
    
    protected Decryptor decryptor = Decryptor.NULL;
    protected Encryptor encryptor = Encryptor.NULL;

    
    public Decryptor decryptor() {
        return decryptor; 
    }
    
    public void set(Decryptor newDecryptor) {
        decryptor = newDecryptor;
    }
    
    public Encryptor encryptor() {
        return encryptor; 
    }
    
    public void set(Encryptor newEncryptor) {
        encryptor = newEncryptor;
    }

    
//    @SuppressWarnings("unchecked")
    // @Override
    public <T> DataFactory<T> newDataFactory(Class<?>... classes) throws APIException {
//        if (String.class.isAssignableFrom(classes[0])) 
//            return (DataFactory<T>) new StringDF(this);
        return new JAXBDF<T>(this,classes);
    }

//    @SuppressWarnings("unchecked")
    // @Override
    public <T> DataFactory<T> newDataFactory(Schema schema, Class<?>... classes) throws APIException {
//        if (String.class.isAssignableFrom(classes[0])) 
//            return (DataFactory<T>) new StringDF(this);
        return new JAXBDF<T>(this, schema, classes);
    }

//    @SuppressWarnings("unchecked")
    // @Override
    public<T> DataFactory<T> newDataFactory(QName qName, Class<?> ... classes) throws APIException {
//        if (String.class.isAssignableFrom(classes[0])) 
//            return (DataFactory<T>) new StringDF(this);
        return new JAXBDF<T>(this, qName, classes);
    }

    // @Override
    public<T> DataFactory<T> newDataFactory(Schema schema, QName qName, Class<?> ... classes) throws APIException {
        return new JAXBDF<T>(this, schema, qName, classes);
    }

    // @Override
    public BasicTrans newTrans() {
        return new BasicTrans(this);
    }

    public void loadFromSystemPropsStartsWith(String ... str) {
         for (String name : System.getProperties().stringPropertyNames()) {
            for (String s : str) {
                if (name.startsWith(s)) {
                    setProperty(name, System.getProperty(name));
                }
            }
        }
    }

    /**
     * 
     * 
     */
    public void loadToSystemPropsStartsWith(String ... str) {
        String value;
        for (String name : existingStaticSlotNames()) {
            for (String s : str) {
                if (name.startsWith(s)) {
                    if ((value = getProperty(name))!=null)
                        System.setProperty(name,value);
                }
            }
         }
    }
    
    public void loadPropFiles(String tag, ClassLoader classloader) throws IOException {
        String propfiles = getProperty(tag);
        if (propfiles!=null) {
            for (String pf : Split.splitTrim(File.pathSeparatorChar, propfiles)) {
                InputStream is = classloader==null?null:classloader.getResourceAsStream(pf);
                if (is==null) {
                    File f = new File(pf);
                    if (f.exists()) {
                        is = new FileInputStream(f);
                    }
                }
                if (is!=null) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    try {
                        String line;
                        while ((line=br.readLine())!=null) {
                            line = line.trim();
                            if (!line.startsWith("#")) {
                                String[] tv = Split.splitTrim('=', line);
                                if (tv.length==2) {
                                    setProperty(tv[0],tv[1]);
                                }
                            }
                        }
                    } finally {
                        try {
                            br.close();
                        } catch (IOException e) {
                            error().log(e);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Create a StaticSlot, and load it from existing Properties
     * 
     * @param name
     * @param propName
     * @return
     */
    public synchronized StaticSlot staticSlot(String name, final String propName) {
        StaticSlot ss = staticSlot(name);
        put(ss,getProperty(propName));
        return ss;
    }


}
