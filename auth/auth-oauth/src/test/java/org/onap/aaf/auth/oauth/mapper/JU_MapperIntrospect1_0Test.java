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

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.handler.MessageContext.Scope;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.auth.dao.cass.OAuthTokenDAO.Data;
import org.onap.aaf.auth.layer.Result;

import aafoauth.v2_0.Introspect;

public class JU_MapperIntrospect1_0Test {
    @Mock
    private HttpServletRequest req;

    Data data;

    @Before
    public void setup() {
        initMocks(this);
        data = new Data();
    }

    @Test
    public void testIntrospect() {
        data.type = 1;

        Result<Data> dataResult = Result.create(data, 0, "detail", "var");

        MapperIntrospect<Introspect> mapper = new MapperIntrospect1_0();

        Result<Introspect> intro = mapper.introspect(dataResult);

        assertEquals(intro.value.getClientType(), "confidential");
    }

    @Test
    public void testIntrospectWithUnknowType() {
        data.type = 5;
        data.scopes = new HashSet<String>();

        data.scopes.add(Scope.APPLICATION.toString());
        data.scopes.add(Scope.HANDLER.toString());

        Result<Data> dataResult = Result.create(data, 0, "detail", "var");

        MapperIntrospect<Introspect> mapper = new MapperIntrospect1_0();

        Result<Introspect> intro = mapper.introspect(dataResult);

        assertEquals(intro.value.getClientType(), "unknown");
    }

    @Test
    public void testIntrospectWithNotOk() {
        data.type = 5;

        Result<Data> dataResult = Result.create(data, 1, "detail", "var");

        MapperIntrospect<Introspect> mapper = new MapperIntrospect1_0();

        Result<Introspect> intro = mapper.introspect(dataResult);

        assertEquals(intro.value, null);
    }

}
