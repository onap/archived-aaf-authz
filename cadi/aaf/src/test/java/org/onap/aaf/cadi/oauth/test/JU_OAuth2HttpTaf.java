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

package org.onap.aaf.cadi.oauth.test;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.any;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.oauth.OAuth2HttpTaf;
import org.onap.aaf.cadi.oauth.OAuth2Principal;
import org.onap.aaf.cadi.oauth.TokenMgr;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.cadi.Taf.LifeForm;
import org.onap.aaf.cadi.client.Result;

public class JU_OAuth2HttpTaf {

    private static final String authz = "Bearer John Doe";

    @Mock private TokenMgr tmgrMock;
    @Mock private HttpServletResponse respMock;
    @Mock private HttpServletRequest reqMock;
    @Mock private OAuth2Principal princMock;

    private PropAccess access;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
    }

    @Test
    public void test() throws APIException, CadiException, LocatorException {
        OAuth2HttpTaf taf = new OAuth2HttpTaf(access, tmgrMock);

        taf.validate(LifeForm.CBLF, reqMock, respMock);
        when(reqMock.getHeader("Authorization")).thenReturn(authz);

        doReturn(Result.ok(200, princMock)).when(tmgrMock).toPrincipal(anyString(), (byte[])any());
        taf.validate(LifeForm.CBLF, reqMock, respMock);

        when(reqMock.isSecure()).thenReturn(true);

        doReturn(Result.err(404, "not found")).when(tmgrMock).toPrincipal(anyString(), (byte[])any());
        taf.validate(LifeForm.CBLF, reqMock, respMock);

        taf.revalidate(null, null);
    }

}
