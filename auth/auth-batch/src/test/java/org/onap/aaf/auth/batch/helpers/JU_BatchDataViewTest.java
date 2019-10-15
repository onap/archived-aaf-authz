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

package org.onap.aaf.auth.batch.helpers;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.Session;

public class JU_BatchDataViewTest {

    @Mock
    AuthzTrans trans;

    @Mock
    Session session;

    @Mock
    PropAccess access;

    BatchDataView batchDataViewObj;

    @Before
    public void setUp() throws APIException, IOException {
        initMocks(this);
        batchDataViewObj = new BatchDataView(trans, session, true);
        Mockito.doReturn(Mockito.mock(TimeTaken.class)).when(trans).start(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void testNs() {
        Result<NsDAO.Data> retVal = batchDataViewObj.ns(trans, "test");
        assertTrue(retVal.status == 9);

        NS n = new NS("test1", "test2", "test3", 1, 2);
        NS.data.put("test", n);
        retVal = batchDataViewObj.ns(trans, "test");
        assertTrue(retVal.status == 0);
    }

    @Test
    public void testRoleByName() {
        Result<RoleDAO.Data> retVal = batchDataViewObj.roleByName(trans,
                "test");
        assertTrue(retVal.status == 9);

        Role n = new Role("test1");
        n.rdd = new RoleDAO.Data();
        Role.byName.put("test", n);
        retVal = batchDataViewObj.roleByName(trans, "test");
        assertTrue(retVal.status == 0);

        n.rdd = null;
        Role.byName.put("test", n);
        retVal = batchDataViewObj.roleByName(trans, "test");
        assertTrue(retVal.status == 9);
    }
    @Test
    public void testUrsByRole() {
        Result<List<UserRoleDAO.Data>> retVal = batchDataViewObj
                .ursByRole(trans, "test");
        assertTrue(retVal.status == 9);

        Role n = new Role("test1");
        n.rdd = new RoleDAO.Data();
        UserRole ur = new UserRole("user", "role", "ns", "rname", new Date());
        (new UserRole.DataLoadVisitor()).visit(ur);
        retVal = batchDataViewObj.ursByRole(trans, "role");
        assertTrue(retVal.status == 0);

    }
    @Test
    public void testUrsByUser() {
        Result<List<UserRoleDAO.Data>> retVal = batchDataViewObj
                .ursByUser(trans, "test");
        assertTrue(retVal.status == 9);

        Role n = new Role("test1");
        n.rdd = new RoleDAO.Data();
        UserRole ur = new UserRole("user", "role", "ns", "rname", new Date());
        (new UserRole.DataLoadVisitor()).visit(ur);
        retVal = batchDataViewObj.ursByUser(trans, "user");
        assertTrue(retVal.status == 0);

    }
    @Test
    public void testDeleteFuture() {
        FutureDAO.Data dataObj = new FutureDAO.Data();
        dataObj.id = new UUID(1000L, 1000L);
        Result<FutureDAO.Data> retVal = batchDataViewObj.delete(trans, dataObj);
        assertTrue(retVal.status == 0);

    }
    @Test
    public void testDeleteApproval() {
        ApprovalDAO.Data dataObj = new ApprovalDAO.Data();
        dataObj.id = new UUID(1000L, 1000L);
        Result<ApprovalDAO.Data> retVal = batchDataViewObj.delete(trans,
                dataObj);
        assertTrue(retVal.status == 0);

    }

    @Test
    public void testInsertApproval() {
        ApprovalDAO.Data dataObj = new ApprovalDAO.Data();
        dataObj.id = new UUID(1000L, 1000L);
        dataObj.memo = "memo";
        dataObj.ticket = new UUID(1000L, 1000L);
        Result<ApprovalDAO.Data> retVal = batchDataViewObj.insert(trans,
                dataObj);
        assertTrue(retVal.status == 0);

    }
    @Test
    public void testInsertFuture() {
        FutureDAO.Data dataObj = new FutureDAO.Data();
        dataObj.id = new UUID(1000L, 1000L);
        dataObj.memo = "memo";
        dataObj.construct = ByteBuffer.allocate(1000);
        Result<FutureDAO.Data> retVal = batchDataViewObj.insert(trans, dataObj);
        assertTrue(retVal.status == 0);

        dataObj.target_key = "memo";
        retVal = batchDataViewObj.insert(trans, dataObj);
        assertTrue(retVal.status == 0);
    }
    @Test
    public void testFlush() {
        batchDataViewObj.flush();

    }
}
