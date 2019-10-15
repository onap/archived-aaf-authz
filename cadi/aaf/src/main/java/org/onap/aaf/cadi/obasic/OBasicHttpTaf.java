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

package org.onap.aaf.cadi.obasic;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.cadi.BasicCred;
import org.onap.aaf.cadi.CachedPrincipal;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.CredVal;
import org.onap.aaf.cadi.Hash;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.Taf;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CachedPrincipal.Resp;
import org.onap.aaf.cadi.CredVal.Type;
import org.onap.aaf.cadi.client.Result;
import org.onap.aaf.cadi.oauth.AbsOTafLur;
import org.onap.aaf.cadi.oauth.OAuth2Principal;
import org.onap.aaf.cadi.oauth.TimedToken;
import org.onap.aaf.cadi.oauth.TokenClient;
import org.onap.aaf.cadi.taf.HttpTaf;
import org.onap.aaf.cadi.taf.TafResp;
import org.onap.aaf.cadi.taf.TafResp.RESP;
import org.onap.aaf.cadi.taf.basic.BasicHttpTafResp;
import org.onap.aaf.cadi.util.FQI;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Pool.Pooled;

/**
 * BasicHttpTaf
 * <p>
 * This TAF implements the "Basic Auth" protocol.  
 * <p>
 * WARNING! It is true for any implementation of "Basic Auth" that the password is passed unencrypted.  
 * This is because the expectation, when designed years ago, was that it would only be used in 
 * conjunction with SSL (https).  It is common, however, for users to ignore this on the assumption that
 * their internal network is secure, or just ignorance.  Therefore, a WARNING will be printed
 * when the HTTP Channel is not encrypted (unless explicitly turned off).
 * <p>
 * @author Jonathan
 *
 */
public class OBasicHttpTaf extends AbsOTafLur implements HttpTaf {
    private final String realm;
    private final CredVal rbac;


    public OBasicHttpTaf(final PropAccess access, final CredVal rbac, final String realm, final String token_url, final String introspect_url) throws CadiException {
        super(access, token_url,introspect_url);
        this.rbac = rbac;
        this.realm = realm;
    }

    /**
     * Note: BasicHttp works for either Carbon Based (Humans) or Silicon Based (machine) Lifeforms.  
     * @see Taf
     */
    public TafResp validate(Taf.LifeForm reading, HttpServletRequest req, HttpServletResponse resp) {
        // See if Request implements BasicCred (aka CadiWrap or other), and if User/Pass has already been set separately
        String user = "invalid";
        String password=null;
        byte[] cred=null;
        if (req instanceof BasicCred) {
            BasicCred bc = (BasicCred)req;
            user = bc.getUser();
            cred = bc.getCred();
        } else {
            String authz = req.getHeader("Authorization");
            if (authz != null && authz.startsWith("Basic ")) {
                if (!req.isSecure()) {
                    access.log(Level.WARN,"WARNING! BasicAuth has been used over an insecure channel");
                }
                try {
                    String temp = Symm.base64noSplit.decode(authz.substring(6));
                    int colon = temp.lastIndexOf(':');
                    if (colon>0) {
                        user = temp.substring(0,colon);
                        password = temp.substring(colon+1);
                    } else {
                        access.printf(Level.AUDIT,"Malformed BasicAuth entry ip=%s, entry=%s",req.getRemoteAddr(),
                                access.encrypt(temp));
                        return new BasicHttpTafResp(access,user,"Malformed BasicAuth entry",RESP.FAIL,resp,realm,false);
                    }
                    if (!rbac.validate(user,Type.PASSWORD,password.getBytes(),req)) {
                        return new BasicHttpTafResp(access,user,buildMsg(null,req,"user/pass combo invalid for ",user,"from",req.getRemoteAddr()), 
                                RESP.TRY_AUTHENTICATING,resp,realm,true);
                    }
                } catch (IOException e) {
                    access.log(e, ERROR_GETTING_TOKEN_CLIENT);
                    return new BasicHttpTafResp(access,user,ERROR_GETTING_TOKEN_CLIENT,RESP.FAIL,resp,realm,false);
                }
            } else {
                return new BasicHttpTafResp(access,user,"Not a Basic Auth",RESP.TRY_ANOTHER_TAF,resp,realm,false);
            }
        }

        try {
            if (password==null && cred!=null) {
                password = new String(cred);
                cred = Hash.hashSHA256(cred);
            } else if (password!=null && cred==null) {
                cred = Hash.hashSHA256(password.getBytes());
            }
            Pooled<TokenClient> pclient = tokenClientPool.get();
            try {
                pclient.content.password(user, password);
                String scope=FQI.reverseDomain(client_id);
                Result<TimedToken> rtt = pclient.content.getToken('B',scope);
                if (rtt.isOK()) {
                    if (rtt.value.expired()) {
                        return new BasicHttpTafResp(access,user,"BasicAuth/OAuth Token: Token Expired",RESP.FAIL,resp,realm,true);
                    } else {
                        TimedToken tt = rtt.value;
                        Result<OAuth2Principal> prin = tkMgr.toPrincipal(tt.getAccessToken(), cred);
                        if (prin.isOK()) {
                            return new BasicHttpTafResp(access,prin.value,"BasicAuth/OAuth Token Authentication",RESP.IS_AUTHENTICATED,resp,realm,true);
                        } else {
                            return new BasicHttpTafResp(access,user,"BasicAuth/OAuth Token: " + prin.code + ' ' + prin.error,RESP.FAIL,resp,realm,true);
                        }
                    }
                } else {
                    return new BasicHttpTafResp(access,user,"BasicAuth/OAuth Token: " + rtt.code + ' ' + rtt.error,RESP.FAIL,resp,realm,true);
                }
            } finally {
                pclient.done();
            }            
        } catch (APIException | CadiException | LocatorException | NoSuchAlgorithmException e) {
            access.log(e, ERROR_GETTING_TOKEN_CLIENT);
            return new BasicHttpTafResp(access,user,ERROR_GETTING_TOKEN_CLIENT,RESP.TRY_ANOTHER_TAF,resp,realm,false);
        }
    }

    protected String buildMsg(Principal pr, HttpServletRequest req, Object ... msg) {
        StringBuilder sb = new StringBuilder();
        if (pr!=null) {
            sb.append("user=");
            sb.append(pr.getName());
            sb.append(',');
        }
        sb.append("ip=");
        sb.append(req.getRemoteAddr());
        sb.append(",port=");
        sb.append(req.getRemotePort());
        if (msg.length>0) {
            sb.append(",msg=\"");
            for (Object s : msg) {
                sb.append(s.toString());
            }
            sb.append('"');
        }
        return sb.toString();
    }

    @Override
    public Resp revalidate(CachedPrincipal prin, Object state) {
//        if (prin instanceof BasicPrincipal) {
//            BasicPrincipal ba = (BasicPrincipal)prin;
//            if (DenialOfServiceTaf.isDeniedID(ba.getName())!=null) {
//                return Resp.UNVALIDATED;
//            }
//            return rbac.validate(ba.getName(), Type.PASSWORD, ba.getCred(), state)?Resp.REVALIDATED:Resp.UNVALIDATED;
//        }
        return Resp.NOT_MINE;
    }

    public String toString() {
        return "Basic Auth enabled on realm: " + realm;
    }
}
