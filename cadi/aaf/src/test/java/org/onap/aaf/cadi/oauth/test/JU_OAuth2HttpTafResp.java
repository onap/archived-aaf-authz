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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.oauth.OAuth2HttpTafResp;
import org.onap.aaf.cadi.oauth.OAuth2Principal;
import org.onap.aaf.cadi.principal.TrustPrincipal;
import org.onap.aaf.cadi.taf.TafResp.RESP;

public class JU_OAuth2HttpTafResp {

    private static final String description = "description";

    @Mock private TrustPrincipal princMock;
    @Mock private OAuth2Principal oauthMock;
    @Mock private HttpServletResponse respMock;

    private PropAccess access;

    private RESP status;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        status = RESP.NO_FURTHER_PROCESSING;
    }

    @Test
    public void test() throws IOException {
        OAuth2HttpTafResp resp = new OAuth2HttpTafResp(access, princMock,  description, status, respMock);
        resp = new OAuth2HttpTafResp(access, oauthMock,  description, status, respMock, true);
        assertThat(resp.isFailedAttempt(), is(true));
        assertThat(resp.isAuthenticated(), is(status));
        assertThat(resp.authenticate(), is(RESP.HTTP_REDIRECT_INVOKED));
    }

}
