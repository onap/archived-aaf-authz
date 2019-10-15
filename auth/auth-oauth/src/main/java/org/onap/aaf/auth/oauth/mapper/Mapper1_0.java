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

package org.onap.aaf.auth.oauth.mapper;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.auth.dao.cass.OAuthTokenDAO;
import org.onap.aaf.auth.dao.cass.OAuthTokenDAO.Data;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.oauth.service.OAuthService;
import org.onap.aaf.auth.oauth.service.OCreds;
import org.onap.aaf.auth.oauth.service.OAuthService.CLIENT_TYPE;
import org.onap.aaf.auth.oauth.service.OAuthService.GRANT_TYPE;
import org.onap.aaf.cadi.client.Holder;
import org.onap.aaf.cadi.oauth.OAuth2Principal;
import org.onap.aaf.cadi.util.Vars;
import org.onap.aaf.misc.env.util.Split;

import aaf.v2_0.Error;
import aafoauth.v2_0.Introspect;
import aafoauth.v2_0.Token;
import aafoauth.v2_0.TokenRequest;


public class Mapper1_0 extends MapperIntrospect1_0 implements Mapper<TokenRequest,Token,Introspect,Error> {
    @Override
    public Class<?> getClass(API api) {
        switch(api) {
            case TOKEN_REQ:        return TokenRequest.class; 
            case TOKEN:         return Token.class;
            case INTROSPECT:     return Introspect.class;
            case ERROR:         return Error.class;
            case VOID:             return Void.class;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A> A newInstance(API api) {
        switch(api) {
            case TOKEN_REQ:        return (A)new TokenRequest();
            case TOKEN:         return (A)new Token();
            case INTROSPECT:     return (A)new Introspect();
            case ERROR:         return (A)new Error();
            case VOID:             return null;
        }
        return null;
    }

    //////////////  Mapping Functions /////////////
    @Override
    public Error errorFromMessage(StringBuilder holder, String msgID, String text, Object ... var) {
        Error err = new Error();
        err.setMessageId(msgID);
        // AT&T Restful Error Format requires numbers "%" placements
        err.setText(Vars.convert(holder, text, var));
        for (Object s : var) {
            err.getVariables().add(s.toString());
        }
        return err;
    }

    @Override
    public TokenRequest tokenReqFromParams(HttpServletRequest req) {
        TokenRequest tr = new TokenRequest();
        boolean data = false;
        Map<String, String[]> map = req.getParameterMap();
        for (Entry<String, String[]> es : map.entrySet()) {
            switch(es.getKey()) {
                case "client_id":
                    if (es.getValue().length==1) {
                        tr.setClientId(es.getValue()[0]);
                        data = true;
                    }
                    break;
                case "client_secret":
                    if (es.getValue().length==1) {
                        tr.setClientSecret(es.getValue()[0]);
                        data = true;
                    }
                    break;
                case "username":
                    if (es.getValue().length==1) {
                        tr.setUsername(es.getValue()[0]);
                        data = true;
                    }
                    break;
                case "password":
                    if (es.getValue().length==1) {
                        tr.setPassword(es.getValue()[0]);
                        data = true;
                    }
                    break;
                case "scope":
                    if (es.getValue().length==1) {
                        tr.setScope(es.getValue()[0]);
                        data = true;
                    }
                    break;
                case "grant_type":
                    if (es.getValue().length==1) {
                        tr.setGrantType(es.getValue()[0]);
                        data = true;
                    }
                    break;
                case "refresh_token":
                    if (es.getValue().length==1) {
                        tr.setRefreshToken(es.getValue()[0]);
                        data = true;
                    }
                    break;

            }
        }
        return data?tr:null;
    }



    /* (non-Javadoc)
     * @see org.onap.aaf.auth.oauth.mapper.Mapper#credsFromReq(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public OCreds credsFromReq(TokenRequest tokReq) {
        return new OCreds(tokReq.getClientId(),tokReq.getClientSecret(),
                         tokReq.getUsername(),tokReq.getPassword());
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.oauth.mapper.Mapper#tokenReq(java.lang.Object)
     */
    @Override
    public Data clientTokenReq(TokenRequest tokReq, Holder<GRANT_TYPE> hgt) {
        OAuthTokenDAO.Data tdd = new OAuthTokenDAO.Data();
        tdd.client_id = tokReq.getClientId(); 
        tdd.user = tokReq.getUsername();
        if (tokReq.getRefreshToken()!=null) {
            tdd.refresh=tokReq.getRefreshToken();
        }
    
        for (GRANT_TYPE ttt : GRANT_TYPE.values()) {
            if (ttt.name().equals(tokReq.getGrantType())) {
                hgt.set(ttt);
                break;
            }
        }
    
        switch(hgt.get()) {
            case client_credentials:
            case password:
            case refresh_token:
                tdd.type = CLIENT_TYPE.confidential.ordinal();
                break;
            default:
                tdd.type = CLIENT_TYPE.unknown.ordinal();
                break;
        }
        String scopes=tokReq.getScope(); 
        if (scopes!=null) {
            Set<String> ss = tdd.scopes(true);
            for (String s: Split.split(' ', tokReq.getScope())) {
                ss.add(s);
            }
        }
    
        tdd.state = tokReq.getState();
        return tdd;
    }

    @Override
    public Result<Token> tokenFromData(Result<Data> rd) {
        if (rd.notOK()) {
            return Result.err(rd);
        }
        Data d = rd.value;
        Token token = new Token();
        if (OAuthService.TOKEN_TYPE.values().length>d.type) {
            token.setTokenType(OAuthService.TOKEN_TYPE.values()[d.type].name());
        } else {
            token.setTokenType("Invalid");
        }
        token.setAccessToken(d.id);
        token.setRefreshToken(d.refresh);
        token.setExpiresIn((int)(d.exp_sec-(System.currentTimeMillis())/1000));
        token.setScope(getScopes(d.scopes(false)));
        token.setState(d.state);
        return Result.ok(token);
    }



    /* (non-Javadoc)
     * @see org.onap.aaf.auth.oauth.mapper.Mapper#fromPrincipal(org.onap.aaf.cadi.oauth.OAuth2Principal)
     */
    @Override
    public Introspect fromPrincipal(OAuth2Principal p) {
        return p.tokenPerm().getIntrospect();
    }

}