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

package org.onap.aaf.cadi.http;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.AbsAuthentication;
import org.onap.aaf.cadi.config.SecurityInfoC;

public class HAuthorizationHeader extends AbsAuthentication<HttpURLConnection> {

    public HAuthorizationHeader(SecurityInfoC<HttpURLConnection> si, String user, String headValue) throws IOException {
        super(si,user,headValue==null?null:headValue.getBytes());
    }

    @Override
    public void setSecurity(HttpURLConnection huc) throws CadiException {
        if (isDenied()) {
            throw new CadiException(REPEAT_OFFENDER);
        }
        try {
            huc.addRequestProperty(AUTHORIZATION , headValue());
        } catch (IOException e) {
            throw new CadiException(e);
        }
        if (securityInfo!=null && huc instanceof HttpsURLConnection) {
            securityInfo.setSocketFactoryOn((HttpsURLConnection)huc);
        }
    }

}
