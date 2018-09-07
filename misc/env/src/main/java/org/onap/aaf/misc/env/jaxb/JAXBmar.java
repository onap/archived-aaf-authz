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

/**
 * JAXBumar.java
 *
 * Created on: Apr 10, 2009
 * Created by: Jonathan
 *
 * Revamped to do away with ThreadLocal 5/27/2011, JonathanGathman
 *
 * (c) 2009 SBC Knowledge Ventures, L.P. All rights reserved.
 ******************************************************************* 
 * RESTRICTED - PROPRIETARY INFORMATION The Information contained 
 * herein is for use only by authorized employees of AT&T Services, 
 * Inc., and authorized Affiliates of AT&T Services, Inc., and is 
 * not for general distribution within or outside the respective 
 * companies. 
 *******************************************************************
 */
package org.onap.aaf.misc.env.jaxb;

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.util.Pool;
import org.onap.aaf.misc.env.util.Pool.Pooled;

/**
 * JAXBmar classes are inexpensive for going in and out of scope
 * and have been made thread safe via Pooling

 * @author Jonathan
 *
 */
public class JAXBmar {
    // Need to store off possible JAXBContexts based on Class, which will be stored in Creator
    private static Map<Class<?>[],Pool<PMarshaller>> pools = new HashMap<>();

    // Handle Marshaller class setting of properties only when needed
    private class PMarshaller {
        private Marshaller m;
        private boolean p;
        private boolean f;
        
        public PMarshaller(Marshaller marshaller) throws JAXBException {
            m = marshaller;
            m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, p = false);
            m.setProperty(Marshaller.JAXB_FRAGMENT, f = false);
        }
        
        public Marshaller get(boolean pretty, boolean fragment) throws JAXBException {
            if(pretty != p) {
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, p = pretty);
            }
            if(fragment != f) {
                m.setProperty(Marshaller.JAXB_FRAGMENT, f = fragment);
            }
            return m;
        }
    }
    
    private class Creator implements Pool.Creator<PMarshaller> {
        private JAXBContext jc;
        private String name;
        public Creator(Class<?>[] classes) throws JAXBException {
            jc = JAXBContext.newInstance(classes);
            name = "JAXBmar: " + classes[0].getName();
        }
        
        // @Override
        public PMarshaller create() throws APIException {
            try {
                return new PMarshaller(jc.createMarshaller());
            } catch (JAXBException e) {
                throw new APIException(e);
            }
        }

        public String toString() {
            return name;
        }

        // @Override
        public void reuse(PMarshaller pm) {
            // Nothing to do
        }
        
        // @Override
        public void destroy(PMarshaller pm) {
            // Nothing to do
        }

        // @Override
        public boolean isValid(PMarshaller t) {
            return true; 
        }
    }

    //TODO isn't UTF-8 a standard string somewhere for encoding?
    private boolean fragment= false;
    private boolean pretty=false;
    private QName qname;
    
    private Pool<PMarshaller> mpool; // specific Pool associated with constructed Classes
    private Class<?> cls;
    
    private Pool<PMarshaller> getPool(Class<?> ... classes) throws JAXBException {
        Pool<PMarshaller> mp;
        synchronized(pools) {
            mp = pools.get(classes);
            if(mp==null) {
                pools.put(classes,mp = new Pool<PMarshaller>(new Creator(classes)));
            }
        }        
        return mp;
    }
    
    public JAXBmar(Class<?>... classes) throws JAXBException {
        cls = classes[0];
        mpool = getPool(classes);
        qname = null;
    }

    public JAXBmar(QName theQname, Class<?>... classes) throws JAXBException {
        cls = classes[0];
        mpool = getPool(classes);
        qname = theQname;
    }

    @SuppressWarnings("unchecked")
    public<O> O marshal(LogTarget lt,O o, Writer writer, boolean ... options) throws JAXBException, APIException {
        boolean pretty, fragment;
        pretty = options.length>0?options[0]:this.pretty;
        fragment = options.length>1?options[1]:this.fragment;
        Pooled<PMarshaller> m = mpool.get(lt);
        try {
            if(qname==null) {
                m.content.get(pretty,fragment).marshal(o, writer);
            } else {
                m.content.get(pretty,fragment).marshal(
                    new JAXBElement<O>(qname, (Class<O>)cls, o ),
                    writer);
            }
            return o;
        } finally {
            m.done();
        }
    }

    @SuppressWarnings("unchecked")
    public<O> O marshal(LogTarget lt, O o, OutputStream os, boolean ... options) throws JAXBException, APIException {
        boolean pretty, fragment;
        pretty = options.length>0?options[0]:this.pretty;
        fragment = options.length>1?options[1]:this.fragment;
        Pooled<PMarshaller> m = mpool.get(lt);
        try {
            if(qname==null) {
                m.content.get(pretty,fragment).marshal(o, os);
            } else {
                m.content.get(pretty,fragment).marshal(
                    new JAXBElement<O>(qname, (Class<O>)cls, o ),os);
            }
            return o;
        } finally {
            m.done();
        }
    }
    
    public<O> O marshal(LogTarget lt, O o, Writer writer, Class<O> clss) throws JAXBException, APIException {
        Pooled<PMarshaller> m = mpool.get(lt);
        try {
            if(qname==null) {
                m.content.get(pretty,fragment).marshal(o, writer);
            } else {
                m.content.get(pretty,fragment).marshal(
                    new JAXBElement<O>(qname, clss, o),writer);
            }
            return o;
        } finally {
            m.done();
        }
            
    }

    public<O> O marshal(LogTarget lt, O o, OutputStream os, Class<O> clss) throws JAXBException, APIException {
        Pooled<PMarshaller> m = mpool.get(lt);
        try {
            if(qname==null) { 
                m.content.get(pretty,fragment).marshal(o, os);
            } else {
                m.content.get(pretty,fragment).marshal(
                    new JAXBElement<O>(qname, clss, o ),os);
            }
            return o;
        } finally {
            m.done();
        }
    }

    /**
     * @return
     */
    public Class<?> getMarshalClass() {
        return cls;
    }

    public<O> String stringify(LogTarget lt, O o) throws JAXBException, APIException {
        StringWriter sw = new StringWriter();
        marshal(lt,o,sw);
        return sw.toString();
    }

    public JAXBmar pretty(boolean pretty) {
        this.pretty = pretty;
        return this;
    }
    
    public JAXBmar asFragment(boolean fragment) {
        this.fragment = fragment;
        return this;
    }
}
