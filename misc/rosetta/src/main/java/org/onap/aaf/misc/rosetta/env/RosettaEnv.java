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

package org.onap.aaf.misc.rosetta.env;

import java.applet.Applet;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.validation.Schema;

import org.onap.aaf.misc.env.APIException;

/**
 * An essential Implementation of Env, which will fully function, without any sort
 * of configuration.
 *
 * Use as a basis for Group level Env, just overriding where needed.
 * @author Jonathan
 *
 */
public class RosettaEnv extends org.onap.aaf.misc.env.impl.BasicEnv {

    public RosettaEnv() {
        super();
    }

    public RosettaEnv(Applet applet, String... tags) {
        super(applet, tags);
    }

    public RosettaEnv(String[] args) {
        super(args);
    }

    public RosettaEnv(String tag, String[] args) {
        super(tag, args);
    }

    public RosettaEnv(String tag, Properties props) {
        super(tag, props);
    }

    public RosettaEnv(Properties props) {
        super(props);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> RosettaDF<T> newDataFactory(Class<?>... classes) throws APIException {
        return new RosettaDF<T>(this, null, null, (Class<T>)classes[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> RosettaDF<T> newDataFactory(Schema schema, Class<?>... classes) throws APIException {
            return new RosettaDF<T>(this, schema, null, (Class<T>)classes[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public<T> RosettaDF<T> newDataFactory(QName qName, Class<?> ... classes) throws APIException {
        return new RosettaDF<T>(this, null, qName.getNamespaceURI(),(Class<T>)classes[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public<T> RosettaDF<T> newDataFactory(Schema schema, QName qName, Class<?> ... classes) throws APIException {
        return new RosettaDF<T>(this, schema,qName.getNamespaceURI(),(Class<T>)classes[0]);
    }
}
