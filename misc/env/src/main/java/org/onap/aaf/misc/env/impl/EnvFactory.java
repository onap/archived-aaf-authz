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

import org.onap.aaf.misc.env.EnvJAXB;
import org.onap.aaf.misc.env.TransCreate;
import org.onap.aaf.misc.env.TransJAXB;

/**
 * EnvFactory
 * 
 * @author Jonathan
 * 
 */
public class EnvFactory {

    public static final String SCHEMA_DIR = "env-schema_dir";
    public static final String DEFAULT_SCHEMA_DIR = "src/main/xsd";
    static BasicEnv singleton;

    static {
        singleton = new BasicEnv();
    }
    public static BasicEnv singleton() {
        return singleton;
    }
    
    public static void setSingleton(BasicEnv be) {
        singleton = be;
    }
    
    public static TransJAXB newTrans() {
        return new BasicTrans(singleton);
    }

    public static TransJAXB newTrans(EnvJAXB env) {
        return new BasicTrans(env);
    }
    
    public static TransCreate<TransJAXB> transCreator() {
        return new TransCreate<TransJAXB>() {
            // @Override
            public BasicTrans newTrans() {
                return singleton.newTrans();
            }
        };
    }
}

