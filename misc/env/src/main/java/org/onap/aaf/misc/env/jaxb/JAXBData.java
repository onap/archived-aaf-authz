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

package org.onap.aaf.misc.env.jaxb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.bind.JAXBException;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.EnvJAXB;
import org.onap.aaf.misc.env.old.IOStringifier;
import org.onap.aaf.misc.env.old.Objectifier;
import org.onap.aaf.misc.env.old.Stringifier;
/**
 * <H1>Data</H1>
 * <i>Data</i> facilitates lazy marshaling of data with a pre-determined
 * marshaling mechanism.<p>
 * 
 * It stores either Object (defined by Generic {@literal <T>}) or String.<p>  
 * 
 * On asking for Object of type {@literal <T>}, it will respond with the object
 * if it exists, or unmarshal the string and pass the result back.<p>
 * 
 * On asking for String, it will respond with the String
 * if it exists, or marshal the String and pass the result back.<p>
 * 
 * @author Jonathan
 *
 * @param <T>
 */
public final class JAXBData<T> implements Data<T>{
    private Stringifier<T> stringifier;
    private Objectifier<T> objectifier;
    private String dataAsString;
    private T dataAsObject;
    private Class<T> tclass;
    private JAXBDF<T> df;
    private Env creatingEnv;
    private boolean options[] = new boolean[] {false, false};
    
    /**
     * Construct a Data Object with an appropriate Stringifier, Objectifier and Class to support
     * 
     * @param env
     * @param strfr
     * @param objfr
     * @param text
     * @param typeClass
     */
    JAXBData(Env env, JAXBDF<T> df, Stringifier<T> strfr, Objectifier<T> objfr, String text, Class<T> typeClass) {
        dataAsString = text;
        dataAsObject = null;
        stringifier = strfr;
        objectifier = objfr;
        tclass = typeClass;
        creatingEnv = env;
        this.df = df;
    }
    
    
    /**
     * Construct a Data Object with an appropriate Stringifier, Objectifier and Object (which will
     * yield it's class)
     * 
     * @param env
     * @param strfr
     * @param objfr
     * @param object
     */
    @SuppressWarnings("unchecked")
    JAXBData(Env env, JAXBDF<T> df, Stringifier<T> strfr, Objectifier<T> objfr, T object) {
        dataAsString = null;
        dataAsObject = object;
        stringifier = strfr;
        objectifier = objfr;
        tclass = (Class<T>) object.getClass();
        creatingEnv = env;
        this.df = df;
    }

    /**
     * Respond with the String if it exists, or marshal the String and pass the result back.<p>
     * 
     * Explicitly use a specific Env for logging purposes
     * 
     * @param env
     * @return String
     * @throws APIException
     */
    public String asString(EnvJAXB env) throws APIException {
        if (dataAsString!=null) {
            return dataAsString;
        } else {
        	dataAsString = stringifier.stringify(env, dataAsObject);
            return dataAsString;
        }
    }

    /**
     * Respond with the String if it exists, or marshal the String and pass the result back.
     * 
     * However, use the Env the Data Object was created with.
     * 
     * @return String
     * @throws APIException
     */
    // @Override
    public String asString() throws APIException {
        if (dataAsString!=null) {
            return dataAsString;
        } else {
        	dataAsString = stringifier.stringify(creatingEnv, dataAsObject,options);
            return dataAsString;
        }
    }
    
    public Data<T> to(OutputStream os) throws APIException, IOException {
        if (dataAsString!=null) {
            os.write(dataAsString.getBytes());
        } else if (stringifier instanceof IOStringifier){
            ((IOStringifier<T>)stringifier).stringify(creatingEnv, dataAsObject, os, options);
        } else {
            dataAsString = stringifier.stringify(creatingEnv, dataAsObject, options);
            os.write(dataAsString.getBytes());
        }
        return this;
    }


    // @Override
    public JAXBData<T> to(Writer writer) throws APIException, IOException {
        if (dataAsString!=null) {
            writer.write(dataAsString);
        } else if (stringifier instanceof IOStringifier){
            ((IOStringifier<T>)stringifier).stringify(creatingEnv, dataAsObject, writer, options);
        } else {
            dataAsString = stringifier.stringify(creatingEnv, dataAsObject, options);
            writer.write(dataAsString);
        }
        return this;
    }


    public InputStream getInputStream() throws APIException {
        if (dataAsString==null) {
            dataAsString = stringifier.stringify(creatingEnv,dataAsObject,options);
        }
        return new ByteArrayInputStream(dataAsString.getBytes());
    }
    
    /**
     * Respond with the Object of type {@literal <T>} if it exists, or unmarshal from String 
     * and pass the result back.<p>
     * 
      * Explicitly use a specific Env for logging purposes
     * 
     * @param env
     * @return T
     * @throws APIException
     */

    public T asObject(EnvJAXB env) throws APIException {
        if (dataAsObject !=null) {
            return dataAsObject;
        } else {
            // Some Java compilers need two statements here
            dataAsObject = objectifier.objectify(env, dataAsString);
            return dataAsObject;
        }
    }

    /**
     * Respond with the Object of type {@literal <T>} if it exists, or unmarshal from String 
     * and pass the result back.<p>
     *
     * However, use the Env the Data Object was created with.
     * 
     * @return T
     * @throws APIException
     */
    // @Override
    public T asObject() throws APIException {
        if (dataAsObject !=null) {
            return dataAsObject;
        } else {
            // Some Java compilers need two statements here
            dataAsObject = objectifier.objectify(creatingEnv, dataAsString);
            return dataAsObject;
        }
    }
    

    /**
     * Return the Class Type supported by this DataObject
     * 
     * @return {@literal Class<T>}
     */
    // @Override
    public Class<T> getTypeClass() {
        return tclass;
    }
    
    
    /**
     * For Debugging Convenience, we marshal to String if possible.
     * 
     * Behavior is essentially the same as asString(), except asString() throws
     * an APIException.  <p>
     * Since toString() must not throw exceptions, the function just catches and prints an
     * error, which is probably not the behavior desired.<p>
     *  
     * Therefore, use "asString()" where possible in actual Transactional code. 
     * 
     * @see java.lang.Object#toString()
     */
    // @Override
    public String toString() {
        if (dataAsString!=null) {
            return dataAsString;
        } else {
            try {
            	dataAsString = stringifier.stringify(creatingEnv, dataAsObject);
                return dataAsString;
            } catch (APIException e) {
                return "ERROR - Can't Stringify from Object " + e.getLocalizedMessage();
            }
        }
    }

    public Data<T> load(T t) throws APIException {
        dataAsObject = t;
        dataAsString = null;
        return this;
    }


    public Data<T> load(String str) throws APIException {
        dataAsObject = null;
        dataAsString = str;
        return this;
    }


    public Data<T> load(InputStream is) throws APIException {
        try {
            dataAsObject = df.jumar.unmarshal(creatingEnv.debug(),is);
            dataAsString = null;
        } catch (JAXBException e) {
            throw new APIException(e);
        }
        return this;
    }


    public Data<T> load(Reader rdr) throws APIException {
        try {
            dataAsObject = df.jumar.unmarshal(creatingEnv.debug(),rdr);
            dataAsString = null;
        } catch (JAXBException e) {
            throw new APIException(e);
        }
        return this;
    }


    // @Override
    public void direct(InputStream input, OutputStream output) throws APIException, IOException {
        byte b[] = new byte[128];
        int count;
        do {
            count = input.read(b);
            if (count>0)output.write(b, 0, count);
        } while (count>=0);
    }


    // @Override
    public Data<T> out(TYPE type) {
        // it's going to be XML regardless...
        return this;
    }


    // @Override
    public Data<T> in(TYPE type) {
        // Not Supported... will still be XML
        return this;
    }


    // @Override
    public Data<T> option(int option) {
        options[0] = (option&Data.PRETTY)==Data.PRETTY;
        options[1] = (option&Data.FRAGMENT)==Data.FRAGMENT;
        return this;
    }
    
}