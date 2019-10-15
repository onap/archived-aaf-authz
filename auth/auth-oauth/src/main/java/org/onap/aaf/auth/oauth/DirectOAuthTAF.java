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
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.direct.DirectAAFUserPass;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.oauth.facade.DirectIntrospect;
import org.onap.aaf.auth.rserv.TransFilter;
import org.onap.aaf.cadi.CachedPrincipal;
import org.onap.aaf.cadi.CachedPrincipal.Resp;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.CredVal.Type;
import org.onap.aaf.cadi.Hash;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Taf.LifeForm;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.oauth.OAuth2HttpTafResp;
import org.onap.aaf.cadi.oauth.OAuth2Principal;
import org.onap.aaf.cadi.oauth.TokenClient;
import org.onap.aaf.cadi.oauth.TokenClientFactory;
import org.onap.aaf.cadi.oauth.TokenMgr;
import org.onap.aaf.cadi.oauth.TokenMgr.TokenPermLoader;
import org.onap.aaf.cadi.oauth.TokenPerm;
import org.onap.aaf.cadi.principal.OAuth2FormPrincipal;
import org.onap.aaf.cadi.taf.HttpTaf;
import org.onap.aaf.cadi.taf.TafResp;
import org.onap.aaf.cadi.taf.TafResp.RESP;
import org.onap.aaf.cadi.util.Split;
import org.onap.aaf.misc.env.APIException;

import aafoauth.v2_0.Introspect;

public class DirectOAuthTAF implements HttpTaf {
    private PropAccess access;
    private DirectIntrospect<Introspect> oaFacade;
    private TokenMgr tkMgr;
    private final DirectAAFUserPass directUserPass;
    private TokenClient altIntrospectClient;

    public DirectOAuthTAF(AuthzEnv env, Question q,  DirectIntrospect<Introspect> facade) throws APIException, CadiException {
        access = env.access();
        oaFacade = facade;
        tkMgr = TokenMgr.getInstance(access,"dbToken","dbIntrospect");
        String alt_url = access.getProperty(Config.AAF_ALT_OAUTH2_INTROSPECT_URL,null);
        TokenClientFactory tcf;
        if (alt_url!=null) {
            try {
                tcf = TokenClientFactory.instance(access);
                String[] split = Split.split(',', alt_url);
                int timeout = split.length>1?Integer.parseInt(split[1]):3000;
                altIntrospectClient = tcf.newClient(split[0], timeout);
                altIntrospectClient.client_creds(access.getProperty(Config.AAF_ALT_CLIENT_ID,null),
                                           access.getProperty(Config.AAF_ALT_CLIENT_SECRET,null));
            } catch (GeneralSecurityException | IOException | LocatorException e) {
                throw new CadiException(e);
            }
        }

        directUserPass = new DirectAAFUserPass(env,q);
    }

    @Override
    public TafResp validate(LifeForm reading, HttpServletRequest req, HttpServletResponse resp) {
        String value;
        String token;
        if ((value=req.getHeader("Authorization"))!=null && value.startsWith("Bearer ")) {
            token = value.substring(7);
        } else {
            token = null;
        }

        if ("application/x-www-form-urlencoded".equals(req.getContentType())) {
            Map<String, String[]> map = req.getParameterMap();
            String client_id=null,client_secret=null,username=null,password=null;
            for (Map.Entry<String, String[]> es : map.entrySet()) {
                switch(es.getKey()) {
                    case "client_id":
                        for (String s : es.getValue()) {
                            client_id=s;
                        }
                        break;
                    case "client_secret":
                        for (String s : es.getValue()) {
                            client_secret=s;
                        }
                        break;
                    case "username":
                        for (String s : es.getValue()) {
                            username=s;
                        }
                        break;
                    case "password":
                        for (String s : es.getValue()) {
                            password=s;
                        }
                        break;
                    case "token":
                        if (token!=null) { // Defined as both Bearer and Form Encoded - Error
                            return new OAuth2HttpTafResp(access, null, "Token Info found as both Bearer Token and Form Info", RESP.FAIL, resp, true);
                        }
                        for (String s : es.getValue()) {
                            token=s;
                        }
                        break;
                    // Ignore others
                }
            }

            if (client_id==null || client_secret==null) {
                return new OAuth2HttpTafResp(access, null, "client_id and client_secret required", RESP.TRY_ANOTHER_TAF, resp, false);
            }

            if (token==null) { // No Token to work with, use only Client_ID and Client_Secret
                AuthzTrans trans = (AuthzTrans)req.getAttribute(TransFilter.TRANS_TAG);

                if (directUserPass.validate(client_id, Type.PASSWORD, client_secret.getBytes(), trans)) {
                    // Client_ID is valid
                    if (username==null) { // Validating just the Client_ID
                        return new OAuth2FormHttpTafResp(access,new OAuth2FormPrincipal(client_id,client_id),"OAuth client_id authenticated",RESP.IS_AUTHENTICATED,resp,false);
                    } else {
                        //TODO - Does a clientID need specific Authorization to pair authentication with user name?  At the moment, no.
                        // username is ok.
                        if (password!=null) {
                            if (directUserPass.validate(username, Type.PASSWORD, password.getBytes(), trans)) {
                                return new OAuth2FormHttpTafResp(access,new OAuth2FormPrincipal(client_id, username),"OAuth username authenticated",RESP.IS_AUTHENTICATED,resp,false);
                            } else {
                                return new OAuth2HttpTafResp(access,null,"OAuth username " + username + " not authenticated ",RESP.FAIL,resp,true);
                            }
                        } else { // no Password
                            //TODO Check for Trust Permission, which requires looking up Perms?
                            return new OAuth2HttpTafResp(access,null,"OAuth username " + username + " not authenticated ",RESP.FAIL,resp,true);
                        }
                    }
                } else {
                    return new OAuth2HttpTafResp(access,null,"OAuth client_id " + client_id + " not authenticated ",RESP.FAIL,resp,true);
                }
            }
        }

        // OK, have only a Token to validate
        if (token!=null) {
            AuthzTrans trans = (AuthzTrans)req.getAttribute(TransFilter.TRANS_TAG);

            try {
                Result<Introspect> ri = oaFacade.mappedIntrospect(trans, token);
                if (ri.isOK()) {
                    TokenPerm tp = tkMgr.putIntrospect(ri.value, Hash.hashSHA256(token.getBytes()));
                    if (tp==null) {
                        return new OAuth2HttpTafResp(access, null, "TokenPerm persistence failure", RESP.FAIL, resp, false);
                    } else {
                        return new OAuth2HttpTafResp(access,new OAuth2Principal(tp,Hash.hashSHA256(token.getBytes())),"Token Authenticated",RESP.IS_AUTHENTICATED,resp,false);
                    }
                } else {
                    return new OAuth2HttpTafResp(access, null, ri.errorString(), RESP.FAIL, resp, false);
                }
            } catch (APIException e) {
                trans.error().log(e,"Error getting token");
                return new OAuth2HttpTafResp(access, null, "Error getting token: " + e.getMessage(), RESP.TRY_ANOTHER_TAF, resp, false);
            } catch (NoSuchAlgorithmException e) {
                return new OAuth2HttpTafResp(access, null, "Error in security algorithm: " + e.getMessage(), RESP.TRY_ANOTHER_TAF, resp, false);
            }
        }
        return new OAuth2HttpTafResp(access, null, "No OAuth2 Credentials in OAuthForm", RESP.TRY_ANOTHER_TAF, resp, false);
    }

    @Override
    public Resp revalidate(CachedPrincipal prin, Object state) {
        // TODO Auto-generated method stub
        return null;
    }

    class ServiceTPL implements TokenPermLoader {
        private final AuthzTrans trans;
        public ServiceTPL(AuthzTrans atrans) {
            trans = atrans;
        }

        @Override
        public org.onap.aaf.cadi.client.Result<TokenPerm> load(String accessToken, byte[] cred) throws APIException, CadiException, LocatorException {
            Result<Introspect> ri = oaFacade.mappedIntrospect(trans, accessToken);
            if (ri.notOK()) {
                //TODO what should the status mapping be?
                return org.onap.aaf.cadi.client.Result.err(ri.status,ri.errorString());
            }
            return org.onap.aaf.cadi.client.Result.ok(200,tkMgr.putIntrospect(ri.value, cred));
        }
    }

    public DirectAAFUserPass directUserPass() {
        return directUserPass;
    }
}

