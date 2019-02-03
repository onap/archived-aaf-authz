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

import org.onap.aaf.misc.env.Decryptor;
import org.onap.aaf.misc.env.Encryptor;
import org.onap.aaf.misc.env.EnvJAXB;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.StaticSlot;
import org.onap.aaf.misc.env.TimeTaken;


public class BasicTrans extends AbsTransJAXB {
    
    public BasicTrans(EnvJAXB env) {
        super(env);
    }

    @Override
    protected TimeTaken newTimeTaken(String name, int flag, Object ... values) {
        /**
         * Note: could have created a different format for Time Taken, but using BasicEnv's instead
         */
        return delegate.start(name, flag, values);
    }
    
    public Slot slot(String name) {
        return delegate.slot(name);
    }

    public <T> T get(StaticSlot slot) {
        return delegate.get(slot);
    }

    public <T> T get(StaticSlot slot, T dflt) {
        return delegate.get(slot,dflt);
    }

    public String setProperty(String tag, String value) {
        delegate.setProperty(tag, value);
        return value;
    }

    public String getProperty(String tag) {
        return delegate.getProperty(tag);
    }

    public String getProperty(String tag, String deflt) {
        return delegate.getProperty(tag, deflt);
    }

    @Override
    public Decryptor decryptor() {
        return delegate.decryptor();
    }

    @Override
    public Encryptor encryptor() {
        return delegate.encryptor();
    }

}
