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

package org.onap.aaf.auth.oauth;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.oauth.facade.OAFacade;

import aafoauth.v2_0.Introspect;

public class JU_OACodeTest {

    @Mock
    private OAFacade<Introspect> facade;

    @Mock
    private OAFacade<Introspect> facade1;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void testOACodeDefaultMethod() throws Exception {
        OACode code = new OACode(facade, "Original Description", true, "role1") {

            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                // Blank implementation to test abstract OACode class.
            }
        };

        OACode clone = code.clone(facade1, false);

        assertNotSame(code, clone);

        assertTrue(code.useJSON);
        assertFalse(clone.useJSON);

    }
}
