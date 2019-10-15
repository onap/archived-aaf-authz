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

package org.onap.aaf.auth.batch.helpers.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.auth.batch.helpers.Approval;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.LogTarget;

import junit.framework.Assert;

public class JU_Approval {

    Approval approval;
    UUID id;
    UUID ticket;


    @Before
    public void setUp() {
        id = new UUID(0, 0);
        ticket = new UUID(0, 0);

        approval = new Approval(id, ticket, "approver","user", "memo", "operation", "status", "type", 100l);
    }

    @Test
    public void testRoleFromMemo() {
        Assert.assertNull(Approval.roleFromMemo(null));
        Assert.assertEquals("org.onap.ns.admin",
                Approval.roleFromMemo(Approval.RE_VALIDATE_ADMIN + "org.onap.ns]"));
        Assert.assertEquals("org.onap.ns.owner", Approval.roleFromMemo(Approval.RE_VALIDATE_OWNER + "org.onap.ns]"));
        Assert.assertEquals("org.onap.ns.member", Approval.roleFromMemo(Approval.RE_APPROVAL_IN_ROLE
                + "bob] + [org.onap.ns.member] - Expires 2018-12-25"));
    }

    @Test
    public void testExpunge() {
        approval.expunge();
    }

//    @Test
//    public void testGetLast_notified() {
//        Assert.assertTrue(approval.getLast_notified() instanceof Date);
//    }
//
//    @Test
//    public void testSetLastNotified() {
//        approval.setLastNotified(date);
//    }

    @Test
    public void testGetStatus() {
        Assert.assertEquals("status", approval.getStatus());
    }

    @Test
    public void testSetStatus() {
        approval.setStatus("status");
    }

    @Test
    public void testGetId() {
        Assert.assertTrue(approval.getId() instanceof UUID);
    }

    @Test
    public void testGetTicket() {
        Assert.assertTrue(approval.getTicket() instanceof UUID);
    }

    @Test
    public void testGetMemo() {
        Assert.assertEquals("memo", approval.getMemo());
    }

    @Test
    public void testGetOperation() {
        Assert.assertEquals("operation", approval.getOperation());
    }

    @Test
    public void testGetType() {
        Assert.assertEquals("type", approval.getType());
    }

    @Test
    public void testLapsed() {
        approval.lapsed();
    }

    @Test
    public void testGetRole() {
        Assert.assertNull(approval.getRole());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("user memo", approval.toString());
    }

    @Test
    public void testResetLocalData() {
        approval.resetLocalData();
    }

    @Test
    public void testPendingDelete() {
        Assert.assertFalse(approval.pendingDelete(approval));
    }

    @Test
    public void testUpdateNonDryRun() {
        approval = new Approval(id, ticket, "approver", "user", "memo", "operation", "status", "type", 100l);
        AuthzTrans trans = mock(AuthzTrans.class);
        ApprovalDAO dao = mock(ApprovalDAO.class);
        LogTarget target = mock(LogTarget.class);

        when(trans.info()).thenReturn(target);

//        approval.update(trans, dao, false);
    }

    @Test
    public void testUpdateDryRun() {
        approval = new Approval(id, ticket, "approver", "user", "memo", "operation", "status", "type", 100l);
        AuthzTrans trans = mock(AuthzTrans.class);
        ApprovalDAO dao = mock(ApprovalDAO.class);
        LogTarget target = mock(LogTarget.class);

        when(trans.info()).thenReturn(target);

//        approval.update(trans, dao, true);
    }

    @Test
    public void testDelayDeleteDryRun() {
        approval = new Approval(id, ticket, "approver", "user", "memo", "operation", "status", "type", 100l);
        AuthzTrans trans = mock(AuthzTrans.class);
        ApprovalDAO dao = mock(ApprovalDAO.class);
        LogTarget target = mock(LogTarget.class);

        when(trans.info()).thenReturn(target);

        List<Approval> list = new ArrayList<Approval>();
        list.add(approval);
        Approval.delayDelete(trans, dao, true, list, "text");
    }

    @Test
    public void testDelayDeleteNonDryRun() {
        approval = new Approval(id, ticket, "approver", "user", "memo", "operation", "status", "type", 100l);
        AuthzTrans trans = mock(AuthzTrans.class);
        ApprovalDAO dao = mock(ApprovalDAO.class);
        LogTarget target = mock(LogTarget.class);

        when(trans.info()).thenReturn(target);
        Result<Void> rv = Result.ok();
        when(dao.delete(any(AuthzTrans.class), any(ApprovalDAO.Data.class), any(Boolean.class))).thenReturn(rv);

        List<Approval> list = new ArrayList<Approval>();
        list.add(approval);
        Approval.delayDelete(trans, dao, false, list, "text");
    }

    @Test
    public void testDelayDeleteResultNotOk() {
        approval = new Approval(id, ticket, "approver",  "user", "memo", "operation", "status", "type", 100l);
        AuthzTrans trans = mock(AuthzTrans.class);
        ApprovalDAO dao = mock(ApprovalDAO.class);
        LogTarget target = mock(LogTarget.class);

        when(trans.info()).thenReturn(target);
        Result<Void> rv = Result.err(new Exception());
        when(dao.delete(any(AuthzTrans.class), any(ApprovalDAO.Data.class), any(Boolean.class))).thenReturn(rv);

        List<Approval> list = new ArrayList<Approval>();
        list.add(approval);
        Approval.delayDelete(trans, dao, false, list, "text");
    }


}
