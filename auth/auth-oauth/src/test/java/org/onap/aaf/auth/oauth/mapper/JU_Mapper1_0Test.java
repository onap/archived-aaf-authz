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

package org.onap.aaf.auth.oauth.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.onap.aaf.auth.dao.cass.OAuthTokenDAO.Data;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.oauth.mapper.Mapper.API;
import org.onap.aaf.auth.oauth.service.OAuthService.GRANT_TYPE;
import org.onap.aaf.auth.oauth.service.OCreds;
import org.onap.aaf.cadi.client.Holder;
import org.onap.aaf.cadi.oauth.OAuth2Principal;

import aaf.v2_0.Error;
import aafoauth.v2_0.Introspect;
import aafoauth.v2_0.Token;
import aafoauth.v2_0.TokenRequest;

public class JU_Mapper1_0Test {
    @Mock
    private HttpServletRequest req;

    @Mock
    private TokenRequest tokenRequest;

    @Mock
    private Holder<GRANT_TYPE> hgt;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OAuth2Principal p;

    private Data data;

    @Before
    public void setup() {
        initMocks(this);
        data = new Data();
        data.id = "id";
    }

    @Test
    public void testMapper() {
        Mapper<TokenRequest, Token, Introspect, Error> mapper = new Mapper1_0();
        assertEquals(TokenRequest.class, mapper.getClass(API.TOKEN_REQ));
        assertEquals(Token.class, mapper.getClass(API.TOKEN));
        assertEquals(Introspect.class, mapper.getClass(API.INTROSPECT));
        assertEquals(Error.class, mapper.getClass(API.ERROR));
        assertEquals(Void.class, mapper.getClass(API.VOID));

        assertTrue(mapper.newInstance(API.TOKEN_REQ) instanceof TokenRequest);
        assertTrue(mapper.newInstance(API.TOKEN) instanceof Token);
        assertTrue(mapper.newInstance(API.INTROSPECT) instanceof Introspect);
        assertTrue(mapper.newInstance(API.ERROR) instanceof Error);
        assertEquals(null, mapper.newInstance(API.VOID));

        Error error = mapper.errorFromMessage(null, null, "text", "var1", "var2");
        assertEquals("text", error.getText());

        Object tokenReqFromParams = mapper.tokenReqFromParams(req);
        assertNull(tokenReqFromParams);
    }

    @Test
    public void testTokeReqFromParams() {
        Map<String, String[]> parameterMap = new TreeMap<String, String[]>();
        parameterMap.put("client_id", new String[] { "ClientId1" });
        parameterMap.put("client_secret", new String[] { "client_secret" });
        parameterMap.put("username", new String[] { "username" });
        parameterMap.put("password", new String[] { "password" });
        parameterMap.put("scope", new String[] { "scope" });
        parameterMap.put("grant_type", new String[] { "grant_type" });
        parameterMap.put("refresh_token", new String[] { "refresh_token" });
        parameterMap.put("etc", new String[] { "etc" });
        when(req.getParameterMap()).thenReturn(parameterMap);

        Mapper<TokenRequest, Token, Introspect, Error> mapper = new Mapper1_0();

        TokenRequest param = mapper.tokenReqFromParams(req);

        assertEquals("ClientId1", param.getClientId());
        assertEquals("client_secret", param.getClientSecret());
        assertEquals("username", param.getUsername());
        assertEquals("password", param.getPassword());
        assertEquals("scope", param.getScope());
        assertEquals("grant_type", param.getGrantType());
        assertEquals("refresh_token", param.getRefreshToken());

        OCreds credsFromReq = mapper.credsFromReq(param);
        assertEquals("ClientId1", credsFromReq.client_id);
        assertEquals("username", credsFromReq.username);

    }

    @Test
    public void testTokeReqFromParamsWithNoValues() {
        Map<String, String[]> parameterMap = new TreeMap<String, String[]>();
        parameterMap.put("client_id", new String[] {});
        parameterMap.put("client_secret", new String[] {});
        parameterMap.put("username", new String[] {});
        parameterMap.put("password", new String[] {});
        parameterMap.put("scope", new String[] {});
        parameterMap.put("grant_type", new String[] {});
        parameterMap.put("refresh_token", new String[] {});
        parameterMap.put("etc", new String[] {});
        when(req.getParameterMap()).thenReturn(parameterMap);

        Mapper<TokenRequest, Token, Introspect, Error> mapper = new Mapper1_0();

        Object param = mapper.tokenReqFromParams(req);

        assertNull(param);

    }

    @Test
    public void testClientTokenReqWithClientCred() {
        when(hgt.get()).thenReturn(GRANT_TYPE.client_credentials);
        when(tokenRequest.getState()).thenReturn("State");
        when(tokenRequest.getGrantType()).thenReturn("client_credentials");
        when(tokenRequest.getScope()).thenReturn("Scope");

        Mapper<TokenRequest, Token, Introspect, Error> mapper = new Mapper1_0();

        Data clientTokenReq = mapper.clientTokenReq(tokenRequest, hgt);

        assertEquals("State", clientTokenReq.state);
        assertTrue(clientTokenReq.scopes.contains("Scope"));

    }

    @Test
    public void testClientTokenReqWithPassword() {
        when(hgt.get()).thenReturn(GRANT_TYPE.unknown);
        when(tokenRequest.getState()).thenReturn("State");
        when(tokenRequest.getRefreshToken()).thenReturn("UnKnown");

        Mapper<TokenRequest, Token, Introspect, Error> mapper = new Mapper1_0();

        Data clientTokenReq = mapper.clientTokenReq(tokenRequest, hgt);

        assertEquals("State", clientTokenReq.state);
        assertEquals(clientTokenReq.type, 0);
    }

    @Test
    public void testTokenFromDataWithNotOk() {
        Result<Data> dataResult = Result.create(null, 1, "detail", "var");

        Mapper<TokenRequest, Token, Introspect, Error> mapper = new Mapper1_0();

        Result<Token> clientTokenReq = mapper.tokenFromData(dataResult);

        assertEquals(null, clientTokenReq.value);
    }

    @Test
    public void testTokenFromData() {

        Result<Data> dataResult = Result.create(data, 0, "detail", "var");

        Mapper<TokenRequest, Token, Introspect, Error> mapper = new Mapper1_0();

        Result<Token> clientTokenReq = mapper.tokenFromData(dataResult);

        assertEquals(clientTokenReq.value.getAccessToken(), data.id);
    }

    @Test
    public void testTokenFromDataWithNoTokenType() {
        data.type = 20;

        Result<Data> dataResult = Result.create(data, 0, "detail", "var");

        Mapper<TokenRequest, Token, Introspect, Error> mapper = new Mapper1_0();

        Result<Token> clientTokenReq = mapper.tokenFromData(dataResult);

        assertEquals(clientTokenReq.value.getAccessToken(), data.id);
        assertEquals(clientTokenReq.value.getTokenType(), "Invalid");
    }

    @Test
    public void testFromPrincipal() {

        Introspect introspect = new Introspect();
        when(p.tokenPerm().getIntrospect()).thenReturn(introspect);

        Mapper<TokenRequest, Token, Introspect, Error> mapper = new Mapper1_0();

        Introspect intro = mapper.fromPrincipal(p);

        assertEquals(introspect, intro);
    }
}
