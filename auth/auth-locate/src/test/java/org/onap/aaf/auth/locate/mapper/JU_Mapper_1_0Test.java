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
package org.onap.aaf.auth.locate.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.auth.locate.mapper.Mapper.API;

import locate.v1_0.Endpoints;
import locate.v1_0.MgmtEndpoints;
import locate_local.v1_0.Error;
import locate_local.v1_0.InRequest;
import locate_local.v1_0.Out;

public class JU_Mapper_1_0Test {

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGetClasses() {
        Mapper_1_1 mapper = new Mapper_1_1();
        assertEquals(InRequest.class, mapper.getClass(API.IN_REQ));
        assertEquals(Out.class, mapper.getClass(API.OUT));
        assertEquals(Error.class, mapper.getClass(API.ERROR));
        assertEquals(Void.class, mapper.getClass(API.VOID));
        assertEquals(Endpoints.class, mapper.getClass(API.ENDPOINTS));
        assertEquals(MgmtEndpoints.class, mapper.getClass(API.MGMT_ENDPOINTS));
    }

    @Test
    public void testNewInstance() {
        Mapper_1_1 mapper = new Mapper_1_1();
        assertTrue(mapper.newInstance(API.IN_REQ) instanceof InRequest);
        assertTrue(mapper.newInstance(API.OUT) instanceof Out);
        assertTrue(mapper.newInstance(API.ERROR) instanceof Error);
        assertTrue(mapper.newInstance(API.ENDPOINTS) instanceof Endpoints);
        assertTrue(mapper.newInstance(API.MGMT_ENDPOINTS) instanceof MgmtEndpoints);
        assertEquals(null, mapper.newInstance(API.VOID));
    }

}
