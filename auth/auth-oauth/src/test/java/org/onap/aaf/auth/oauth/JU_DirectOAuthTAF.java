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

package org.onap.aaf.auth.oauth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.oauth.facade.DirectIntrospect;
import org.onap.aaf.auth.rserv.TransFilter;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.taf.TafResp;
import org.onap.aaf.misc.env.APIException;

import aafoauth.v2_0.Introspect;

public class JU_DirectOAuthTAF {

    @Mock
    private AuthzEnv env;

    @Mock
    private PropAccess access;

    private Properties props = new Properties();

    @Mock
    private HttpServletRequest req;

    private Map<String, String[]> parameterMap;
    @Mock
    private DirectIntrospect<Introspect> facade;
    @Mock
    private AuthzTrans trans;
    @Mock
    private Result<Introspect> ri;

    @Before
    public void setup() {
        initMocks(this);
        parameterMap = new TreeMap<String, String[]>();

    }

    @Test
    public void testValidateWithoutSecret() throws APIException, CadiException {
        parameterMap.put("client_id", new String[] { "Client1" });
        // parameterMap.put("client_secret", new String[] { "Secret1" });
        parameterMap.put("username", new String[] { "User1" });
        parameterMap.put("password", new String[] { "Pass1" });
        parameterMap.put("token", new String[] { "token1" });
        when(env.access()).thenReturn(access);
        when(access.getProperties()).thenReturn(props);
        when(req.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(req.getParameterMap()).thenReturn(parameterMap);

        DirectOAuthTAF oAuthTaf = new DirectOAuthTAF(env, null, null);

        TafResp validate = oAuthTaf.validate(null, req, null);

        assertNotNull(validate);
        assertEquals(validate.getAccess(), access);
        assertEquals(validate.desc(), "client_id and client_secret required");
    }

    @Test
    public void testValidateWithSecret() throws APIException, CadiException {
        parameterMap.put("client_id", new String[] { "Client1" });
        parameterMap.put("client_secret", new String[] { "Secret1" });
        parameterMap.put("username", new String[] { "User1" });
        parameterMap.put("password", new String[] { "Pass1" });
        parameterMap.put("token", new String[] { "token1" });

        when(env.access()).thenReturn(access);
        when(access.getProperties()).thenReturn(props);
        when(req.getContentType()).thenReturn("application/x-www-form-urlencoded");
        when(req.getParameterMap()).thenReturn(parameterMap);
        when(req.getAttribute(TransFilter.TRANS_TAG)).thenReturn(trans);
        when(facade.mappedIntrospect(trans, "token1")).thenReturn(ri);

        DirectOAuthTAF oAuthTaf = new DirectOAuthTAF(env, null, facade);

        TafResp validate = oAuthTaf.validate(null, req, null);

        assertNotNull(validate);
        assertEquals(validate.getAccess(), access);
        assertEquals(validate.desc(), ri.errorString());

        assertNull(oAuthTaf.revalidate(null, null));
        assertNotNull(oAuthTaf.directUserPass());
    }

}
