/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * <p>
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/

package org.onap.aaf.auth.cm.api;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cm.AAF_CM;
import org.onap.aaf.auth.cm.api.API_Cert;
import org.onap.aaf.auth.env.AuthzTrans;
;

@RunWith(MockitoJUnitRunner.class)
public class JU_API_Cert {

    @Mock
    private static API_Cert api;

    @Mock
    private static AAF_CM certManApi;

    private static AAF_CM noMockAPI;
    private static API_Cert api_1;

    private static HttpServletRequest req;
    private static HttpServletResponse res;

    @BeforeClass
    public static void setUp() {
        AuthzTrans trans = mock(AuthzTrans.class);
        req = mock(HttpServletRequest.class);
        res = mock(HttpServletResponse.class);
        trans.setProperty("testTag", "UserValue");
        trans.set(req,res);
    }

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Test
    public void init_bothValued() {
        try {
            api.init(certManApi);
        } catch (Exception e) {
            //thrown.expect(NullPointerException.class);
            e.printStackTrace();
        }
    }

    @Test
    public void init_Null_() {
        try {
            api.init(null);
        } catch (Exception e) {
            //thrown.expect(Exception.class);
            e.printStackTrace();
        }
    }

    @Test
    public void init_NMC_Null() {
        try {
            api_1.init(null);
        } catch (Exception e) {
            //thrown.expect(NullPointerException.class);
            e.printStackTrace();
        }
    }

    @Test
    public void init_NMC() {
        try {
            api_1.init(noMockAPI);
        } catch (Exception e) {
            //thrown.expect(NullPointerException.class);
            e.printStackTrace();
        }
    }
}
