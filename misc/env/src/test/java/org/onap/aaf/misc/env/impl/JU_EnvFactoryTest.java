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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.misc.env.EnvJAXB;
import org.onap.aaf.misc.env.TransCreate;
import org.onap.aaf.misc.env.TransJAXB;

public class JU_EnvFactoryTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSingleton() {
        BasicEnv singleton = EnvFactory.singleton();

        assertEquals(EnvFactory.singleton, singleton);
    }

    @Test
    public void testSetSingleton() {
        String[] str = { "argument1" };
        BasicEnv env = new BasicEnv("tag", str);
        EnvFactory.setSingleton(env);

        assertEquals(EnvFactory.singleton(), env);
    }

    @Test
    public void testNewTrans() {
        TransJAXB newTrans = EnvFactory.newTrans();

        assertTrue(newTrans instanceof BasicTrans);
    }

    @Test
    public void testNewTransEnvJAXB() {
        EnvJAXB env = new BasicEnv("");

        TransJAXB trans = EnvFactory.newTrans(env);

        assertTrue(trans instanceof BasicTrans);
    }

    @Test
    public void testTransCreator() {
        TransCreate<TransJAXB> transCreator = EnvFactory.transCreator();

        TransJAXB newTrans = transCreator.newTrans();

        assertTrue(newTrans instanceof BasicTrans);
    }

}
