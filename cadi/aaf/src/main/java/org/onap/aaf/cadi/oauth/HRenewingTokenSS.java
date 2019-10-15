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

package org.onap.aaf.cadi.oauth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.client.Result;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HAuthorizationHeader;
import org.onap.aaf.cadi.principal.Kind;
import org.onap.aaf.cadi.util.FQI;
import org.onap.aaf.misc.env.APIException;

public class HRenewingTokenSS extends HAuthorizationHeader {
    private TokenClientFactory tcf;
    private final TokenClient tc;
    private final String[] scopes;
    private final String tokenURL;

    public HRenewingTokenSS(final PropAccess access, final String tokenURL, final String ... nss) throws CadiException, IOException, GeneralSecurityException {
        this(access,SecurityInfoC.instance(access, HttpURLConnection.class),tokenURL,nss);
    }

    public HRenewingTokenSS(final PropAccess access, final SecurityInfoC<HttpURLConnection> si, final String tokenURL, final String ... nss) throws CadiException, IOException, GeneralSecurityException {
        super(si,null,null/*Note: HeadValue overloaded */);
        this.tokenURL = tokenURL;
        try {
            tcf = TokenClientFactory.instance(access);
            tc = tcf.newClient(tokenURL);
            tc.client_creds(access);
            setUser(tc.client_id());
            String defaultNS = FQI.reverseDomain(tc.client_id());
            if (nss.length>0) {
                boolean hasDefault = false;
                for (String ns : nss) {
                    if (ns.equals(defaultNS)) {
                        hasDefault = true;
                    }
                }
                if (hasDefault) {
                    scopes=nss;
                } else {
                    String[] nssPlus = new String[nss.length+1];
                    nssPlus[0]=defaultNS;
                    System.arraycopy(nss, 0, nssPlus, 1, nss.length);
                    scopes = nssPlus;
                }
            } else {
                scopes = new String[] {defaultNS};
            }

        } catch (GeneralSecurityException | IOException | LocatorException | APIException e) {
            throw new CadiException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.client.AbsAuthentication#headValue()
     */
    @Override
    protected String headValue() throws IOException {
        Result<TimedToken> token;
        try {
            token = tc.getToken(Kind.OAUTH,scopes);
            if (token.isOK()) {
                return "Bearer " + token.value.getAccessToken();
            } else {
                throw new IOException("Token cannot be obtained: " + token.code + '-' + token.error);
            }
        } catch (IOException e) {
            throw e;
        } catch (LocatorException | CadiException | APIException e) {
            throw new IOException(e);
        }
    }

    public String tokenURL() {
        return tokenURL;
    }
}
