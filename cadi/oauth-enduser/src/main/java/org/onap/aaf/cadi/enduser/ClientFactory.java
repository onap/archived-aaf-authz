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
package org.onap.aaf.cadi.enduser;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.oauth.TokenClientFactory;
import org.onap.aaf.misc.env.APIException;

public class ClientFactory {
    private final TokenClientFactory tcf;
    public ClientFactory(final PropAccess access) throws APIException, CadiException {
        try {
            tcf = TokenClientFactory.instance(access);
        } catch (GeneralSecurityException | IOException e) {
            throw new CadiException(e);
        }
    }

    public ClientFactory(String[] args) throws APIException, CadiException {
        this(new PropAccess(args));
    }

    public SimpleRESTClient simpleRESTClient(final String endpoint, final String ... scopes) throws URISyntaxException, LocatorException, CadiException, APIException {
        return new SimpleRESTClient(tcf, Config.AAF_OAUTH2_TOKEN_URL, endpoint, scopes);
    }

    public Access getAccess() {
        return tcf.access;
    }
}
