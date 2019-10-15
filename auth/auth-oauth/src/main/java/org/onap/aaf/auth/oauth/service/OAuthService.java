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

package org.onap.aaf.auth.oauth.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.auth.dao.DAO;
import org.onap.aaf.auth.dao.cass.OAuthTokenDAO;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.cass.OAuthTokenDAO.Data;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.direct.DirectAAFUserPass;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.NullTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.CredVal.Type;
import org.onap.aaf.cadi.client.Holder;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.oauth.AAFToken;
import org.onap.aaf.cadi.oauth.TokenClient;
import org.onap.aaf.cadi.oauth.TokenClientFactory;
import org.onap.aaf.cadi.util.Split;
import org.onap.aaf.misc.env.APIException;

import aafoauth.v2_0.Introspect;

public class OAuthService {

    private static final int TOK_EXP = 60*60*1000; // 1 hour, millis.

    public enum TOKEN_TYPE {unknown,bearer,refresh}
    public enum GRANT_TYPE {unknown,password,client_credentials,refresh_token};
    public enum CLIENT_TYPE {unknown,confidential};

    // Additional Expires
    private final DAO<AuthzTrans, ?>[] daos;
    public final OAuthTokenDAO tokenDAO;
    private final DirectAAFUserPass directUserPass;
    private final TokenClientFactory tcf;
    private TokenClient altIntrospectClient;
    private String altDomain;
    private final JSONPermLoader permLoader;


    // If we add more CAs, may want to parameterize

    @SuppressWarnings("unchecked")
    public OAuthService(final Access access, final AuthzTrans trans, final Question q) throws APIException, IOException {
        permLoader = JSONPermLoaderFactory.direct(q);
        tokenDAO = new OAuthTokenDAO(trans, q.historyDAO());
        daos =(DAO<AuthzTrans, ?>[]) new DAO<?,?>[] {
            tokenDAO
        };
        try {
            String alt_url = access.getProperty(Config.AAF_ALT_OAUTH2_INTROSPECT_URL,null);
            if (alt_url!=null) {
                tcf = TokenClientFactory.instance(access);
                String[] split = Split.split(',', alt_url);
                int timeout = split.length>1?Integer.parseInt(split[1]):3000;
                altIntrospectClient = tcf.newClient(split[0], timeout);
                altIntrospectClient.client_creds(access.getProperty(Config.AAF_ALT_CLIENT_ID,null), 
                                           access.getProperty(Config.AAF_ALT_CLIENT_SECRET,null));
                altDomain = '@'+access.getProperty(Config.AAF_ALT_OAUTH2_DOMAIN,null);
            } else {
                tcf = null;
            }
            directUserPass = new DirectAAFUserPass(trans.env(), q);
        } catch (GeneralSecurityException | CadiException | LocatorException e) {
            throw new APIException("Could not construct TokenClientFactory",e);
        }

    }

    public Result<Void> validate(AuthzTrans trans, OCreds creds) {
        if (directUserPass.validate(creds.username, Type.PASSWORD, creds.password, trans)) {
            return Result.ok();
        } else {
            return Result.err(Result.ERR_Security, "Invalid Credential for ",creds.username);
        }
    }

    public Result<Data> createToken(AuthzTrans trans, HttpServletRequest req, OAuthTokenDAO.Data odd, Holder<GRANT_TYPE> hgt) {
        switch(hgt.get()) {
            case client_credentials:
            case password:
                return createBearerToken(trans, odd);
            case refresh_token:
                return refreshBearerToken(trans, odd);
            default:
                return Result.err(Result.ERR_BadData, "Unknown Grant Type");
        }
    }

    private Result<Data> createBearerToken(AuthzTrans trans, OAuthTokenDAO.Data odd) {
        if (odd.user==null) {
            odd.user = trans.user();
        }
        odd.id = AAFToken.toToken(UUID.randomUUID());
        odd.refresh = AAFToken.toToken(UUID.randomUUID());
        odd.active = true;
        long exp;
        odd.expires = new Date(exp=(System.currentTimeMillis()+TOK_EXP));
        odd.exp_sec = exp/1000;
        odd.req_ip = trans.ip();

        try {
            Result<Data> rd = loadToken(trans, odd);
            if (rd.notOK()) {
                return rd;
            }
        } catch (APIException | CadiException e) {
            return Result.err(e);
        }
        return tokenDAO.create(trans, odd);
    }

    private Result<Data> loadToken(AuthzTrans trans, Data odd) throws APIException, CadiException {
        Result<String> rs = permLoader.loadJSONPerms(trans,odd.user,odd.scopes(false));
        if (rs.isOK()) {
            odd.content = rs.value;
            odd.type = TOKEN_TYPE.bearer.ordinal();
            return Result.ok(odd);
        } else if (rs.status == Result.ERR_NotFound || rs.status==Status.ERR_UserRoleNotFound) {
            odd.type = TOKEN_TYPE.bearer.ordinal();
            return Result.ok(odd);
        } else {
            return Result.err(Result.ERR_Backend,"Error accessing AAF Info: %s",rs.errorString());
        }
    }



    private Result<Data> refreshBearerToken(AuthzTrans trans, Data odd) {
        Result<List<Data>> rld = tokenDAO.readByUser(trans, trans.user());
        if (rld.notOK()) {
            return Result.err(rld);
        }
        if (rld.isEmpty()) {
            return Result.err(Result.ERR_NotFound,"Data not Found for %1 %2",trans.user(),odd.refresh==null?"":odd.refresh.toString());
        }
        Data token = null;
        for (Data d : rld.value) {
            if (d.refresh.equals(odd.refresh)) {
                token = d;
                boolean scopesNE = false;
                Set<String> scopes = odd.scopes(false);
                if (scopes.size()>0) { // only check if Scopes listed, RFC 6749, Section 6
                    if (scopesNE=!(scopes.size() == d.scopes(false).size())) {
                        for (String s : odd.scopes(false)) {
                            if (!d.scopes(false).contains(s)) {
                                scopesNE=true;
                                break;
                            }
                        }
                    }
                    if (scopesNE) {
                        return Result.err(Result.ERR_BadData,"Requested Scopes do not match existing Token");
                    }
                }
                break;
            }
        }
    
        if (token==null) {
            trans.audit().printf("Duplicate Refresh Token (%s) attempted for %s. Possible Replay Attack",odd.refresh.toString(),trans.user());
            return Result.err(Result.ERR_Security,"Invalid Refresh Token");
        } else {
            // Got the Result
            Data deleteMe = new Data();
            deleteMe.id = token.id;
            token.id = AAFToken.toToken(UUID.randomUUID());
            token.client_id = trans.user();
            token.refresh = AAFToken.toToken(UUID.randomUUID());
            long exp;
            token.expires = new Date(exp=(System.currentTimeMillis()+TOK_EXP));
            token.exp_sec = exp/1000;
            token.req_ip = trans.ip();
            Result<Data> rd = tokenDAO.create(trans, token);
            if (rd.notOK()) {
                return Result.err(rd);
            }
            Result<Void> rv = tokenDAO.delete(trans, deleteMe,false);
            if (rv.notOK()) {
                trans.error().log("Unable to delete token", token);
            }
        }
        return Result.ok(token);
    }

    public Result<OAuthTokenDAO.Data> introspect(AuthzTrans trans, String token) {
        Result<List<Data>> rld;
        try {
            UUID uuid = AAFToken.fromToken(token);
            if (uuid==null) { // not an AAF Token
                // Attempt to get Alternative Token
                if (altIntrospectClient!=null) {
                     org.onap.aaf.cadi.client.Result<Introspect> rai = altIntrospectClient.introspect(token);
                     if (rai.isOK()) {
                         Introspect in = rai.value;
                         if (in.getExp()==null) {
                            trans.audit().printf("Alt OAuth sent back inactive, empty token: requesting_id,%s,access_token=%s,ip=%s\n",trans.user(),token,trans.ip());
                         }
                         long expires = in.getExp()*1000;
                         if (in.isActive() && expires>System.currentTimeMillis()) {
                            // We have a good Token, modify to be Fully Qualified
                            String fqid = in.getUsername()+altDomain;
                            // read contents
                            rld = tokenDAO.read(trans, token);
                            if (rld.isOKhasData()) {
                                Data td = rld.value.get(0);
                                in.setContent(td.content);
                            } else {
                                Data td = new Data();
                                td.id = token;
                                td.client_id = in.getClientId();
                                td.user = fqid;
                                td.active=true;
                                td.type = TOKEN_TYPE.bearer.ordinal();
                                td.expires = new Date(expires);
                                td.exp_sec = in.getExp();
                                Set<String> scopes = td.scopes(true);
                                if (in.getScope()!=null) {
                                    for (String s : Split.split(' ', in.getScope())) {
                                        scopes.add(s);
                                    }
                                }
                                // td.state = nothing to add at this point
                                td.req_ip = trans.ip();
                                trans.checkpoint(td.user + ':' + td.client_id + ", " + td.id);
                                return loadToken(trans, td);
                            }
                         }
//                         System.out.println(rai.value.getClientId());
                     } else {
                        trans.audit().printf("Alt OAuth rejects: requesting_id,%s,access_token=%s,ip=%s,code=%d,error=%s\n",trans.user(),token,trans.ip(),rai.code,rai.error);
                     }
                } else {
                    trans.audit().printf("Bad Token: requesting_id,%s,access_token=%s,ip=%s\n",trans.user(),token,trans.ip());
                }
                return Result.err(Result.ERR_Denied,"Bad Token");
            } else {
                return dbIntrospect(trans,token);
            }
        } catch (CadiException | APIException | LocatorException e) {
            return Result.err(e);
        }
    }

    public Result<Data> dbIntrospect(final AuthzTrans trans, final String token) {
        Result<List<Data>> rld = tokenDAO.read(trans, token);
        if (rld.notOKorIsEmpty()) {
            return Result.err(rld);
        }
        OAuthTokenDAO.Data odd = rld.value.get(0);
        trans.checkpoint(odd.user + ':' + odd.client_id + ", " + odd.id);
        if (odd.active) {
            if (odd.expires.before(trans.now())) {
                return Result.err(Result.ERR_Policy,"Token %1 has expired",token);
            }
            return Result.ok(rld.value.get(0)); // ok keyed on id/token.
        } else {
            return Result.err(Result.ERR_Denied,"Token %1 is inactive",token);
        }
    }

    public void close() {
        for (DAO<AuthzTrans,?> dao : daos) {
            dao.close(NullTrans.singleton());
        }
    }

}
