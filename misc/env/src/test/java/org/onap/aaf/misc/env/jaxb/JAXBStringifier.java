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

import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.old.IOStringifier;

public class JAXBStringifier<T> implements IOStringifier<T> {
    private JAXBmar jmar;

    public JAXBStringifier(Class<?>... classes) throws APIException {
        try {
            jmar = new JAXBmar(classes);
        } catch (JAXBException e) {
            throw new APIException(e);
        }
    }

    public JAXBStringifier(QName qname, Class<?>... classes)
            throws APIException {
        try {
            jmar = new JAXBmar(qname, classes);
        } catch (JAXBException e) {
            throw new APIException(e);
        }
    }
    
    // package on purpose
    JAXBStringifier(JAXBmar jmar) {
        this.jmar = jmar;
    }

    // // @Override
    public void stringify(Env env, T input, Writer writer, boolean ... options)
            throws APIException {
        TimeTaken tt = env.start("JAXB Marshal", Env.XML);
        try {
            jmar.marshal(env.debug(), input, writer, options);
        } catch (JAXBException e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }

    // @Override
    public void stringify(Env env, T input, OutputStream os, boolean ... options)
            throws APIException {
        // TODO create an OutputStream that Counts?
        TimeTaken tt = env.start("JAXB Marshal", Env.XML);
        try {
            jmar.marshal(env.debug(), input, os, options);
        } catch (JAXBException e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }

    // @Override
    public String stringify(Env env, T input, boolean ... options) throws APIException {
        TimeTaken tt = env.start("JAXB Marshal", Env.XML);
        StringWriter sw = new StringWriter();
        try {
            jmar.marshal(env.debug(), input, sw, options);
            String rv = sw.toString();
            tt.size(rv.length());
            return rv;
        } catch (JAXBException e) {
            tt.size(0);
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }

    // // @Override
    public void servicePrestart(Env env) throws APIException {
    }

    // // @Override
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

    // @Override
    public JAXBStringifier<T> pretty(boolean pretty) {
        jmar.pretty(pretty);
        return this;
    }

    // @Override
    public JAXBStringifier<T> asFragment(boolean fragment) {
        jmar.asFragment(fragment);
        return this;
    }

}
