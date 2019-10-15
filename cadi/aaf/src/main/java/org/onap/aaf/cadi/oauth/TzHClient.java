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

package org.onap.aaf.cadi.oauth;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.aaf.v2_0.AAFLocator;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.cadi.http.HTokenSS;
import org.onap.aaf.misc.env.APIException;

/**
 * Tokenized HClient
 * <p>
 * @author Jonathan
 *
 */
public class TzHClient extends TzClient {
    private HMangr hman;
    public SecurityInfoC<HttpURLConnection> si;
    private TimedToken token;
    private SecuritySetter<HttpURLConnection> tokenSS;

    public TzHClient(Access access, String tagOrURL) throws CadiException, LocatorException {
        try {
            si = SecurityInfoC.instance(access, HttpURLConnection.class);
            hman = new HMangr(access, new AAFLocator(si,new URI(access.getProperty(tagOrURL, tagOrURL))));
        } catch (URISyntaxException e) {
            throw new CadiException(e);
        }
    }
    public TzHClient(Access access, SecurityInfoC<HttpURLConnection> hsi, Locator<URI> loc) throws LocatorException {
        si = hsi;
        hman = new HMangr(access, loc);
    }

    public void setToken(final String client_id, TimedToken token) throws IOException {
        this.token = token;
        tokenSS = new HTokenSS(si, client_id, token.getAccessToken());
    }

    public <RET> RET best (Retryable<RET> retryable) throws CadiException, LocatorException, APIException {
        if (token == null || tokenSS==null) {
            throw new CadiException("OAuth2 Token has not been set");
        }
        if (token.expired()) {
            //TODO Refresh?
            throw new CadiException("Expired Token");
        } else {
            return hman.best(tokenSS, retryable);
        }
    }
}
