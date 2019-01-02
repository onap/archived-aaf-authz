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
import java.io.Reader;

import javax.xml.bind.JAXBException;
import javax.xml.validation.Schema;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.old.IOObjectifier;

/**
 * Allow Extended IO interface usage without muddying up the Stringifier Interface
 */
public class JAXBObjectifier<T> implements IOObjectifier<T> {
    private JAXBumar jumar;

    public JAXBObjectifier(Schema schema, Class<?>... classes) throws APIException {
        try {
            jumar = new JAXBumar(schema, classes);
        } catch (JAXBException e) {
            throw new APIException(e);
        }
    }

    public JAXBObjectifier(Class<?>... classes) throws APIException {
        try {
            jumar = new JAXBumar(classes);
        } catch (JAXBException e) {
            throw new APIException(e);
        }
    }
    
    // package on purpose
    JAXBObjectifier(JAXBumar jumar) {
        this.jumar = jumar;
    }

    @SuppressWarnings("unchecked")
    // @Override
    public T objectify(Env env, String input) throws APIException {
        TimeTaken tt = env.start("JAXB Unmarshal", Env.XML);
        try {
            tt.size(input.length());
            return (T)jumar.unmarshal(env.debug(), input);
        } catch (JAXBException e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }

    @SuppressWarnings("unchecked")
    // @Override
    public T objectify(Env env, Reader rdr) throws APIException {
        //TODO create a Reader that Counts?
        TimeTaken tt = env.start("JAXB Unmarshal", Env.XML);
        try {
            return (T)jumar.unmarshal(env.debug(), rdr);
        } catch (JAXBException e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }


    @SuppressWarnings("unchecked")
    // @Override
    public T objectify(Env env, InputStream is) throws APIException {
        //TODO create a Reader that Counts?
        TimeTaken tt = env.start("JAXB Unmarshal", Env.XML);
        try {
            return (T)jumar.unmarshal(env.debug(), is);
        } catch (JAXBException e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }


    public void servicePrestart(Env env) throws APIException {
    }

    public void threadPrestart(Env env) throws APIException {
    }

    // // @Override
    public void refresh(Env env) throws APIException {
    }

    // // @Override
    public void threadDestroy(Env env) throws APIException {
    }

    // // @Override
    public void serviceDestroy(Env env) throws APIException {
    }


    @SuppressWarnings("unchecked")
    public T newInstance() throws APIException {
        try {
            return (T)jumar.newInstance();
        } catch (Exception e) {
            throw new APIException(e);
        }
    }

}

