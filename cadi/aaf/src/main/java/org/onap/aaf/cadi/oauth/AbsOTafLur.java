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
import java.security.GeneralSecurityException;
import java.security.Principal;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Pool;
import org.onap.aaf.misc.env.util.Pool.Creator;

public abstract class AbsOTafLur {
    protected static final String ERROR_GETTING_TOKEN_CLIENT = "Error getting TokenClient";
    protected static final String REQUIRED_FOR_OAUTH2 = " is required for OAuth Access";

    protected final TokenMgr tkMgr;
    protected final PropAccess access;
    protected final String client_id;
    protected static Pool<TokenClient> tokenClientPool;

    protected AbsOTafLur(final PropAccess access, final String token_url, final String introspect_url) throws CadiException {
        this.access = access;
        String ci;
        if ((ci = access.getProperty(Config.AAF_APPID,null))==null) {
            if ((ci = access.getProperty(Config.CADI_ALIAS,null))==null) {
                throw new CadiException(Config.AAF_APPID + REQUIRED_FOR_OAUTH2);
            }
        }
        client_id = ci;

        synchronized(access) {
            if (tokenClientPool==null) {
                tokenClientPool = new Pool<TokenClient>(new TCCreator(access));
            }
            try {
                tkMgr = TokenMgr.getInstance(access, token_url, introspect_url);
            } catch (APIException e) {
                throw new CadiException("Unable to create TokenManager",e);
            }
        }
    }

    private class TCCreator implements Creator<TokenClient> {
        private TokenClientFactory tcf;
        private final int timeout;
        private final String url,enc_secret;
    
        public TCCreator(PropAccess access) throws CadiException { 
            try {
                tcf = TokenClientFactory.instance(access);
            } catch (APIException | GeneralSecurityException | IOException e1) {
                throw new CadiException(e1);
            }
        
            if ((url = access.getProperty(Config.AAF_OAUTH2_TOKEN_URL,null))==null) {
                throw new CadiException(Config.AAF_OAUTH2_TOKEN_URL + REQUIRED_FOR_OAUTH2);
            }
        
            try {
                timeout = Integer.parseInt(access.getProperty(Config.AAF_CONN_TIMEOUT, Config.AAF_CONN_TIMEOUT_DEF));
            } catch (NumberFormatException e) {
                throw new CadiException("Bad format for " + Config.AAF_CONN_TIMEOUT, e);
            }
            if ((enc_secret= access.getProperty(Config.AAF_APPPASS,null))==null) {
                throw new CadiException(Config.AAF_APPPASS + REQUIRED_FOR_OAUTH2);
            }
        }
    
        @Override
        public TokenClient create() throws APIException {
            try {
                TokenClient tc = tcf.newClient(url, timeout);
                tc.client_creds(client_id, access.decrypt(enc_secret, true));
                return tc;
            } catch (CadiException | LocatorException | IOException e) {
                throw new APIException(e);
            }
        }

        @Override
        public void destroy(TokenClient t) {
        }

        @Override
        public boolean isValid(TokenClient t) {
            return t!=null && t.client_id()!=null;
        }

        @Override
        public void reuse(TokenClient t) {
        }
    };

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.Lur#destroy()
     */
    public void destroy() {
        tkMgr.close();
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.Lur#clear(java.security.Principal, java.lang.StringBuilder)
     */
    public void clear(Principal p, StringBuilder report) {
        tkMgr.clear(p, report);
    }



}
