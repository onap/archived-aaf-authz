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
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.cadi.taf.TafResp.RESP;

public class JU_OAuth2FormHttpTafRespTest {

    @Mock
    private HttpServletResponse resp;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void testAuthenticated() throws IOException {
        OAuth2FormHttpTafResp oAuth2 = new OAuth2FormHttpTafResp(null, null, null, null, resp);

        assertEquals(oAuth2.authenticate(), RESP.HTTP_REDIRECT_INVOKED);

        verify(resp, only()).setStatus(401);
    }

    @Test
    public void testIsAuthenticated() throws IOException {
        OAuth2FormHttpTafResp oAuth2 = new OAuth2FormHttpTafResp(null, null, null, RESP.HAS_PROCESSED, null, false);

        assertEquals(oAuth2.isAuthenticated(), RESP.HAS_PROCESSED);
        assertFalse(oAuth2.isFailedAttempt());
    }
}
