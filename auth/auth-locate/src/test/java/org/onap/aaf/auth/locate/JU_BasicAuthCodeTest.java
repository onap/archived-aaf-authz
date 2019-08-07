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
package org.onap.aaf.auth.locate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.locate.facade.LocateFacade;
import org.onap.aaf.cadi.aaf.v2_0.AAFAuthn;
import org.onap.aaf.cadi.principal.BasicPrincipal;
import org.onap.aaf.cadi.principal.X509Principal;
import org.onap.aaf.misc.env.LogTarget;

public class JU_BasicAuthCodeTest {
    @Mock
    AAFAuthn authn;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    AuthzTrans trans;

    @Mock
    HttpServletRequest req;

    @Mock
    HttpServletResponse resp;

    @Mock
    LogTarget error;

    @Mock
    LocateFacade facade;

    @Mock
    BasicPrincipal basicPrincipal;
    @Mock
    X509Principal x509Principal;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void testWithNullUserPrincipal() throws Exception {
        BasicAuthCode basicAuthCode = new BasicAuthCode(authn, facade);
        LocateCode locateCode = basicAuthCode.clone(facade, false);

        assertEquals(locateCode.desc(), basicAuthCode.desc());

        when(trans.getUserPrincipal()).thenReturn(null);
        when(trans.error()).thenReturn(error);

        basicAuthCode.handle(trans, req, resp);
    }

    @Test
    public void testWithBasicUserPrincipal() throws Exception {
        BasicAuthCode basicAuthCode = new BasicAuthCode(authn, facade);
        LocateCode locateCode = basicAuthCode.clone(facade, false);

        assertEquals(locateCode.desc(), basicAuthCode.desc());

        when(trans.getUserPrincipal()).thenReturn(basicPrincipal);

        basicAuthCode.handle(trans, req, resp);

        verify(resp).setStatus(HttpStatus.OK_200);
    }

    @Test
    public void testWithX509UserPrincipal() throws Exception {
        BasicAuthCode basicAuthCode = new BasicAuthCode(authn, facade);
        LocateCode locateCode = basicAuthCode.clone(facade, false);

        assertEquals(locateCode.desc(), basicAuthCode.desc());

        when(trans.getUserPrincipal()).thenReturn(x509Principal);
        when(req.getHeader("Authorization")).thenReturn("Basic 76//76");

        basicAuthCode.handle(trans, req, resp);

        verify(resp).setStatus(HttpStatus.FORBIDDEN_403);
    }

}
