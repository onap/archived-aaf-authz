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

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.util.Pool;
import org.onap.aaf.misc.env.util.Pool.Pooled;
import org.w3c.dom.Node;

/**
 * JAXBumar classes are inexpensive for going in and out of scope
 * and have been made thread safe via Pooling
 * 
 * @author Jonathan
 *
 */
public class JAXBumar {
    // Need to store off possible JAXBContexts based on Class, which will be stored in Creator
    private static Map<Class<?>[],Pool<SUnmarshaller>> pools = new HashMap<>();

    private Class<?> cls;
    private Schema schema;
    private Pool<SUnmarshaller> mpool;;

    // Handle Marshaller class setting of properties only when needed
    private class SUnmarshaller {
        private Unmarshaller u;
        private Schema s;
        
        public SUnmarshaller(Unmarshaller unmarshaller) throws JAXBException {
            u = unmarshaller;
            s = null;
        }
        
        public Unmarshaller get(Schema schema) throws JAXBException {
            if (s != schema) {
                u.setSchema(s = schema);
            }
            return u;
        }
    }
    
    private class Creator implements Pool.Creator<SUnmarshaller> {
        private JAXBContext jc;
        private String name;
        
        public Creator(Class<?>[] classes) throws JAXBException {
            jc = JAXBContext.newInstance(classes);
            name = "JAXBumar: " + classes[0].getName();
        }
        
        // @Override
        public SUnmarshaller create() throws APIException {
            try {
                return new SUnmarshaller(jc.createUnmarshaller());
            } catch (JAXBException e) {
                throw new APIException(e);
            }
        }
        
        public String toString() {
            return name;
        }

        // @Override
        public void destroy(SUnmarshaller sui) {
            // Nothing to do
        }
        
        // @Override
        public boolean isValid(SUnmarshaller t) {
            return true; 
        }

        // @Override
        public void reuse(SUnmarshaller t) {
            // Nothing to do here
        }

    }

    private Pool<SUnmarshaller> getPool(Class<?> ... classes) throws JAXBException {
        Pool<SUnmarshaller> mp;
        synchronized(pools) {
            mp = pools.get(classes);
            if (mp==null) {
                pools.put(classes,mp = new Pool<SUnmarshaller>(new Creator(classes)));
            }
        }        
        return mp;
    }

    public JAXBumar(Class<?> ... classes) throws JAXBException {
        cls = classes[0];
        mpool = getPool(classes);
        schema = null;
    }
    
    /**
     * Constructs a new JAXBumar with schema validation enabled.
     * 
     * @param schema
     * @param theClass
     * @throws JAXBException
     */
    public JAXBumar(Schema schema, Class<?> ... classes) throws JAXBException {
        cls = classes[0];
        mpool = getPool(classes);
        this.schema = schema;
    }
    
    @SuppressWarnings("unchecked")
    public<O> O unmarshal(LogTarget env, Node node) throws JAXBException, APIException {
        Pooled<SUnmarshaller> s = mpool.get(env);
        try {
        	s.content.get(schema).setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        	s.content.get(schema).setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return s.content.get(schema).unmarshal(node,(Class<O>)cls).getValue();
        } finally {
            s.done();
        }

    }
    
    @SuppressWarnings("unchecked")
    public<O> O unmarshal(LogTarget env, String xml) throws JAXBException, APIException {
        if (xml==null) throw new JAXBException("Null Input for String unmarshal");
        Pooled<SUnmarshaller> s = mpool.get(env);
        try {
        	s.content.get(schema).setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        	s.content.get(schema).setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                return (O)s.content.get(schema).unmarshal(
                    new StreamSource(new StringReader(xml))
                    ,(Class<O>)cls).getValue();
        } finally {
            s.done();
        }
    }
    
    @SuppressWarnings("unchecked")
    public<O> O unmarshal(LogTarget env, File xmlFile) throws JAXBException, APIException {
        Pooled<SUnmarshaller> s = mpool.get(env);
        try {
        	s.content.get(schema).setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        	s.content.get(schema).setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return (O)s.content.get(schema).unmarshal(xmlFile);
        } finally {
            s.done();
        }

    }
    
    @SuppressWarnings("unchecked")
    public<O> O unmarshal(LogTarget env,InputStream is) throws JAXBException, APIException {
        Pooled<SUnmarshaller> s = mpool.get(env);
        try {
        	s.content.get(schema).setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        	s.content.get(schema).setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return (O)s.content.get(schema).unmarshal(is);
        } finally {
            s.done();
        }
    }

    @SuppressWarnings("unchecked")
    public<O> O unmarshal(LogTarget env, Reader rdr) throws JAXBException, APIException {
        Pooled<SUnmarshaller> s = mpool.get(env);
        try {
        	s.content.get(schema).setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        	s.content.get(schema).setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return (O)s.content.get(schema).unmarshal(rdr);
        } finally {
            s.done();
        }
    }

    @SuppressWarnings("unchecked")
    public<O> O unmarshal(LogTarget env, XMLStreamReader xsr) throws JAXBException, APIException {
        Pooled<SUnmarshaller> s = mpool.get(env);
        try {
        	s.content.get(schema).setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        	s.content.get(schema).setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return (O)s.content.get(schema).unmarshal(xsr,(Class<O>)cls).getValue();
        } finally {
            s.done();
        }
    }

    @SuppressWarnings("unchecked")
    public<O> O unmarshal(LogTarget env, XMLEventReader xer) throws JAXBException, APIException {
        Pooled<SUnmarshaller> s = mpool.get(env);
        try {
        	s.content.get(schema).setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        	s.content.get(schema).setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return (O)s.content.get(schema).unmarshal(xer,(Class<O>)cls).getValue();
        } finally {
            s.done();
        }
    }

    @SuppressWarnings("unchecked")
    public<O> O newInstance() throws InstantiationException, IllegalAccessException{
        return ((Class<O>)cls).newInstance();
    }
}
