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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.principal.OAuth2FormPrincipal;
import org.onap.aaf.cadi.principal.TrustPrincipal;
import org.onap.aaf.cadi.taf.AbsTafResp;
import org.onap.aaf.cadi.taf.TafResp;

public class OAuth2FormHttpTafResp extends AbsTafResp implements TafResp {
    private static final String tafName = DirectOAuthTAF.class.getSimpleName();
    private HttpServletResponse httpResp;
    private RESP status;
    private final boolean wasFailed;

    public OAuth2FormHttpTafResp(Access access, OAuth2FormPrincipal principal, String desc, RESP status, HttpServletResponse resp, boolean wasFailed) {
        super(access,tafName,principal, desc);
        httpResp = resp;
        this.status = status; 
        this.wasFailed = wasFailed;
    }

    public OAuth2FormHttpTafResp(Access access, TrustPrincipal principal, String desc, RESP status,HttpServletResponse resp) {
        super(access,tafName,principal, desc);
        httpResp = resp;
        this.status = status; 
        wasFailed = true; // if Trust Principal added, must be good
    }

    public RESP authenticate() throws IOException {
        httpResp.setStatus(401); // Unauthorized
        return RESP.HTTP_REDIRECT_INVOKED;
    }

    public RESP isAuthenticated() {
        return status;
    }

    public boolean isFailedAttempt() {
        return wasFailed;
    }

}
