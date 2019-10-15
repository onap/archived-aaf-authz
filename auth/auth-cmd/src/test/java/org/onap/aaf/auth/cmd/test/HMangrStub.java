/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * <p>
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/

package org.onap.aaf.auth.cmd.test;

import java.net.HttpURLConnection;
import java.net.URI;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.http.HMangr;

public class HMangrStub extends HMangr {

    private Rcli<HttpURLConnection> clientMock;

    public HMangrStub(Access access, Locator<URI> loc, Rcli<HttpURLConnection> clientMock) throws LocatorException {
        super(access, loc);
        this.clientMock = clientMock;
    }

    @Override public<RET> RET same(SecuritySetter<HttpURLConnection> ss, Retryable<RET> retryable) {
        try {
            return retryable.code(clientMock);
        } catch (Exception e) {
        }
        return null;
    }
    @Override public<RET> RET oneOf(SecuritySetter<HttpURLConnection> ss, Retryable<RET> retryable, boolean notify, String host) {
        try {
            return retryable.code(clientMock);
        } catch (Exception e) {
        }
        return null;
    }
}
