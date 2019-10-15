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
package org.onap.aaf.auth.dao.cached;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.auth.dao.CIDAO;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.CredDAO.Data;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;

public class JU_CachedCredDAOTest {

    @Mock
    private CIDAO<AuthzTrans> info;
    @Mock
    private CredDAO dao;
    private AuthzTrans trans;
    @Mock
    private Result<List<Data>> value;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        when(dao.readID(trans, "id")).thenReturn(value);
    }

    @Test
    public void testOk() {
        when(value.isOK()).thenReturn(false);
        CachedCredDAO ccDao = new CachedCredDAO(dao, info, 100l);

        ccDao.readNS(trans, "ns");
        Result<List<Data>> result = ccDao.readID(trans, "id");

        assertEquals(result.status, Status.OK);
        verify(dao).readNS(trans, "ns");
        verify(dao).readID(trans, "id");
    }

}
