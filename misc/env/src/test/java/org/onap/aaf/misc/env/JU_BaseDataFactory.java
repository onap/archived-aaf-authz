/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.misc.env;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aaf.misc.env.impl.EnvFactory;

public class JU_BaseDataFactory {

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void testGenSchemaException() {
        Store env = Mockito.mock(Store.class);
        Mockito.doReturn("testdir").when(env).get(null, EnvFactory.DEFAULT_SCHEMA_DIR);
        try {
            BaseDataFactory.genSchema(env, new String[] {});
        } catch (APIException e) {
            assertTrue(e.getLocalizedMessage().contains("does not exist.  You can set this with"));
        }
    }

    @Test
    public void testGenSchemaXsdException() {
        Store env = Mockito.mock(Store.class);
        Mockito.doReturn(System.getProperty("user.dir")).when(env).get(null, EnvFactory.DEFAULT_SCHEMA_DIR);
        String[] schemaFIles = new String[] {"../auth-client/src/main/xsd/aaf_2_0.xsd"};
        try {
            BaseDataFactory.genSchema(env, schemaFIles);
        } catch (APIException e) {
            assertTrue(e.getLocalizedMessage().contains("for schema validation"));
        }
    }

    @Test
    public void testGenSchemaNoException() {
        Store env = Mockito.mock(Store.class);
        Mockito.doReturn(System.getProperty("user.dir")).when(env).get(null, EnvFactory.DEFAULT_SCHEMA_DIR);
        String[] schemaFIles = new String[] {"../../auth-client/src/main/xsd/aaf_2_0.xsd"};
        try {
            BaseDataFactory.genSchema(env, schemaFIles);
        } catch (APIException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetQName() {
        String[] schemaFIles = new String[] {"../../auth-client/src/main/xsd/aaf_2_0.xsd"};
        try {
            BaseDataFactory.getQName(Api.class);
        } catch (APIException e) {
            assertTrue(e.getLocalizedMessage().contains("package-info does not have an XmlSchema annotation"));
        }
    }
}
