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

import javax.net.ssl.HttpsURLConnection;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.AbsTransferSS;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.principal.TaggedPrincipal;


public class HTransferSS extends AbsTransferSS<HttpURLConnection> {
    public HTransferSS(TaggedPrincipal principal, String app) throws IOException {
        super(principal, app);
    }

    public HTransferSS(TaggedPrincipal principal, String app, SecurityInfoC<HttpURLConnection> si) {
        super(principal, app, si);
    }

    @Override
    public void setSecurity(HttpURLConnection huc) throws CadiException {
        if (defSS==null) {
            throw new CadiException("Need App Credentials to send message");
        }
        defSS.setSecurity(huc);
        if (value!=null) {
                huc.addRequestProperty(Config.CADI_USER_CHAIN, value);
        }
        if (securityInfo!=null) {
            securityInfo.setSocketFactoryOn((HttpsURLConnection)huc);
        }
    }

    @Override
    public int setLastResponse(int respCode) {
        return 0;
    }

}
