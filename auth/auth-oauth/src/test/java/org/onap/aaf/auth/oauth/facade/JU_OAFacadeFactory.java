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

package org.onap.aaf.auth.oauth.facade;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.auth.dao.cass.OAuthTokenDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.oauth.service.OAuthService;
import org.onap.aaf.misc.env.APIException;

import aafoauth.v2_0.Introspect;

public class JU_OAFacadeFactory {

    @Mock
    private OAuthService service;

    private String token;

    private AuthzTrans trans;
    @Mock
    private Result<Data> rs;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void testStatusNotOk() throws APIException {
        when(service.introspect(trans, token)).thenReturn(rs);
        when(rs.notOK()).thenReturn(true);

        DirectIntrospect<Introspect> direct = OAFacadeFactory.directV1_0(service);
        Result<Introspect> rti = direct.mappedIntrospect(trans, token);

        assertEquals(rti.status, 0);
    }

    @Test
    public void testStatusOk() throws APIException {
        when(service.introspect(trans, token)).thenReturn(rs);
        when(rs.notOK()).thenReturn(false);

        DirectIntrospect<Introspect> directV1_0 = OAFacadeFactory.directV1_0(service);
        Result<Introspect> rti = directV1_0.mappedIntrospect(trans, token);

        assertEquals(rti.status, 0);
    }

    @Test
    public void testStatusOkWithResultSetEmpty() throws APIException {
        when(service.introspect(trans, token)).thenReturn(rs);
        when(rs.isEmpty()).thenReturn(true);
        when(rs.notOK()).thenReturn(false);

        DirectIntrospect<Introspect> directV1_0 = OAFacadeFactory.directV1_0(service);
        Result<Introspect> rti = directV1_0.mappedIntrospect(trans, token);

        assertEquals(rti.status, Result.ERR_NotFound);
    }
}
