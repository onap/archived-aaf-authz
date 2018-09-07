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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.BaseDataFactory;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.EnvJAXB;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.old.IOObjectifier;
import org.onap.aaf.misc.env.old.IOStringifier;
import org.onap.aaf.misc.env.old.OldDataFactory;

public class JAXBDF<T> extends BaseDataFactory implements OldDataFactory<T>,IOObjectifier<T>, IOStringifier<T> {
    // Package on purpose
    EnvJAXB primaryEnv;
    JAXBumar jumar;
    JAXBmar jmar;

    public JAXBDF(EnvJAXB env, Class<?> ... classes) throws APIException {
        try {
            primaryEnv = env;
            jumar = new JAXBumar(classes);
            jmar = new JAXBmar(classes) ;
        } catch (JAXBException e) {
            throw new APIException(e);
        }
    }
    
    public JAXBDF(EnvJAXB env, Schema schema, Class<?> ... classes) throws APIException {
        try {
            primaryEnv = env;
            jumar = new JAXBumar(schema, classes);
            jmar = new JAXBmar(classes);
        } catch (JAXBException e) {
            throw new APIException(e);
        }
    }
    
    public JAXBDF(EnvJAXB env, QName qname, Class<?> ... classes) throws APIException {
        try {
            primaryEnv = env;
            jumar = new JAXBumar(classes);
            jmar = new JAXBmar(qname, classes);
        } catch (JAXBException e) {
            throw new APIException(e);
        }
    }

    public JAXBDF(EnvJAXB env, Schema schema, QName qname, Class<?> ... classes) throws APIException {
        try {
            primaryEnv = env;
            jumar = new JAXBumar(schema, classes);
            jmar = new JAXBmar(qname, classes);
        } catch (JAXBException e) {
            throw new APIException(e);
        }
    }
    
    // @Override
    public T newInstance() throws APIException {
        try {
            return jumar.newInstance();
        } catch (Exception e) {
            throw new APIException(e);
        }
    }

    // @Override
    public IOStringifier<T> pretty(boolean pretty) {
        jmar.pretty(pretty);
        return this;
    }

    // @Override
    public IOStringifier<T> asFragment(boolean fragment) {
        jmar.asFragment(fragment);
        return this;
    }

    // @Override
    public void servicePrestart(Env env) throws APIException {
    }

    // @Override
    public void threadPrestart(Env env) throws APIException {
    }

    // @Override
    public void refresh(Env env) throws APIException {
    }

    // @Override
    public void threadDestroy(Env env) throws APIException {
    }

    // @Override
    public void serviceDestroy(Env env) throws APIException {
    }

    @SuppressWarnings("unchecked")
    // @Override
    public Data<T> newData() {
        return new JAXBData<T>(primaryEnv, this, new JAXBStringifier<T>(jmar), new JAXBObjectifier<T>(jumar),"",(Class<T>)jmar.getMarshalClass());
    }

    @SuppressWarnings("unchecked")
    // @Override
    public Data<T> newData(Env env) {
        return new JAXBData<T>(env, this,new JAXBStringifier<T>(jmar), new JAXBObjectifier<T>(jumar),"",(Class<T>)jmar.getMarshalClass());
    }

    // @Override
    public Data<T> newData(T type) {
        return new JAXBData<T>(primaryEnv, this, new JAXBStringifier<T>(jmar), new JAXBObjectifier<T>(jumar), type);
    }

    // @Override
    public Data<T> newDataFromStream(Env env, InputStream input) throws APIException {
        //TODO Write an unvalidated String using STAX checking for end of Doc?
        // perhaps key evaluation as well.
        try {
            T t = jumar.unmarshal(env.debug(), input);
            return new JAXBData<T>(primaryEnv, this, new JAXBStringifier<T>(jmar), new JAXBObjectifier<T>(jumar),t);
        } catch(JAXBException e) {
            throw new APIException(e);
        }
    }

    @SuppressWarnings("unchecked")
    // @Override
    public Data<T> newDataFromString(String string) {
        return new JAXBData<T>(primaryEnv, this,new JAXBStringifier<T>(jmar), new JAXBObjectifier<T>(jumar), string,(Class<T>)jmar.getMarshalClass());
    }

    /////////// Old DataFactory Interface 
    // @Override
    public String stringify(T type) throws APIException {
        try {
            StringWriter sw = new StringWriter();
            jmar.marshal(primaryEnv.debug(), type, sw);
            return sw.toString();
        } catch (JAXBException e) {
            throw new APIException(e);
        }    
    }

    // @Override
    public void stringify(T type, Writer writer) throws APIException {
        try {
            jmar.marshal(primaryEnv.debug(), type, writer);
        } catch (JAXBException e) {
            throw new APIException(e);
        }    
    }

    // @Override
    public void stringify(T type, OutputStream os) throws APIException {
        try {
            jmar.marshal(primaryEnv.debug(), type, os);
        } catch (JAXBException e) {
            throw new APIException(e);
        }    
    }

    /////////// New DataFactory Interface 
    // @Override
    public String stringify(Env env, T input, boolean ... options) throws APIException {
        try {
            StringWriter sw = new StringWriter();
            TimeTaken tt = env.start("JAXB Stringify", Env.XML);
            try {
                jmar.marshal(env.debug(), input, sw, options);
            } finally {
                tt.done();
            }
            String str = sw.toString();
            tt.size(str.getBytes().length);
            return str;
        } catch (JAXBException e) {
            throw new APIException(e);
        }
    }

    // @Override
    public void stringify(Env env, T input, Writer writer, boolean ... options) throws APIException {
        TimeTaken tt = env.start("JAXB Stringify", Env.XML);
        try {
            jmar.marshal(env.debug(), input, writer, options);
        } catch (JAXBException e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }

    // @Override
    public void stringify(Env env, T input, OutputStream os, boolean ... options) throws APIException {
        TimeTaken tt = env.start("JAXB Stringify", Env.XML);
        try {
            jmar.marshal(env.debug(), input, os, options);
        } catch (JAXBException e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }

    // @Override
    public T objectify(Env env, Reader rdr) throws APIException {
        TimeTaken tt = env.start("JAXB Objectify", Env.XML);
        try {
            return jumar.unmarshal(env.debug(), rdr);
        } catch (JAXBException e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }

    // @Override
    public T objectify(Reader rdr) throws APIException {
        try {
            return jumar.unmarshal(primaryEnv.debug(), rdr);
        } catch (JAXBException e) {
            throw new APIException(e);
        }    
    }

    // @Override
    public T objectify(Env env, InputStream is) throws APIException {
        TimeTaken tt = env.start("JAXB Objectify", Env.XML);
        try {
            return jumar.unmarshal(env.debug(), is);
        } catch (JAXBException e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }

    // @Override
    public T objectify(InputStream is) throws APIException {
        try {
            return jumar.unmarshal(primaryEnv.debug(), is);
        } catch (JAXBException e) {
            throw new APIException(e);
        }    
    }

    // @Override
    public T objectify(Env env, String input) throws APIException {
        TimeTaken tt = env.start("JAXB Objectify", Env.XML);
        tt.size(input.getBytes().length);
        try {
            return jumar.unmarshal(env.debug(), input);
        } catch (JAXBException e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }

    // @Override
    public T objectify(String text) throws APIException {
        try {
            return jumar.unmarshal(primaryEnv.debug(), text);
        } catch (JAXBException e) {
            throw new APIException(e);
        }    
    }

    @SuppressWarnings("unchecked")
    // @Override
    public Class<T> getTypeClass() {
        return (Class<T>)jmar.getMarshalClass();
    }

}
