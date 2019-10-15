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

package org.onap.aaf.cadi.http;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.client.BasicAuth;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.principal.BasicPrincipal;

public class HBasicAuthSS extends HAuthorizationHeader implements BasicAuth {
    public HBasicAuthSS(SecurityInfoC<HttpURLConnection> si, String user, String password) throws IOException {
        super(si, user, "Basic " + Symm.base64noSplit.encode(user + ':' + password));
        if(password==null) {
            throw new IOException("No password passed for " + user);
        }
    }

    public HBasicAuthSS(SecurityInfoC<HttpURLConnection> si) throws IOException {
        this(si,si.access.getProperty(Config.AAF_APPID, null),
                si.access.decrypt(si.access.getProperty(Config.AAF_APPPASS, null), false));
    }

    public HBasicAuthSS(SecurityInfoC<HttpURLConnection> si, boolean setDefault) throws IOException {
        this(si,si.access.getProperty(Config.AAF_APPID, null),
                si.access.decrypt(si.access.getProperty(Config.AAF_APPPASS, null), false),setDefault);
    }


    public HBasicAuthSS(SecurityInfoC<HttpURLConnection> si, String user, String pass, boolean asDefault) throws IOException {
        this(si, user,pass);
        if (asDefault) {
            si.set(this);
        }
    }

    public HBasicAuthSS(BasicPrincipal bp, SecurityInfoC<HttpURLConnection> si) throws IOException {
        this(si, bp.getName(),new String(bp.getCred()));
    }

    public HBasicAuthSS(BasicPrincipal bp, SecurityInfoC<HttpURLConnection> si, boolean asDefault) throws IOException {
        this(si, bp.getName(),new String(bp.getCred()));
        if (asDefault) {
            si.set(this);
        }
    }


}
