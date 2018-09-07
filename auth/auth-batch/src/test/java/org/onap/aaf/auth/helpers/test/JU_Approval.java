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

package org.onap.aaf.auth.helpers.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.helpers.Approval;

import junit.framework.Assert;

import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class JU_Approval {
    
    Approval approval;
    UUID id;
    UUID ticket;
    Date date;
    
    @Before
    public void setUp() {
        id = new UUID(0, 0);
        ticket = new UUID(0, 0);
        date = new Date();
        
        approval = new Approval(id, ticket, "approver", date, 
                 "user", "memo", "operation", "status", "type", 100l);
    }

    @Test
    public void testRoleFromMemo() {
        Assert.assertNull(approval.roleFromMemo(null));
        Assert.assertEquals(".admin", approval.roleFromMemo("Re-Validate as Administrator for AAF Namespace '\'test\'test"));
        Assert.assertEquals(".owner", approval.roleFromMemo("Re-Validate Ownership for AAF Namespace '\'test\'test"));
        Assert.assertEquals("", approval.roleFromMemo("Re-Approval in Role '\'test\'test"));
    }
    
    @Test
    public void testExpunge() {
        approval.expunge();
    }
    
    @Test
    public void testGetLast_notified() {
        Assert.assertTrue(approval.getLast_notified()instanceof Date);
    }
    
    @Test
    public void testSetLastNotified() {
        approval.setLastNotified(date);
    }
    
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
    public void testSizeForDeletion() {
        Assert.assertEquals(0, approval.sizeForDeletion());
    }
    
    @Test
    public void testPendingDelete() {
        Assert.assertFalse(approval.pendingDelete(approval));
    }
    
    @Test
    public void testDelayDelete() {
        AuthzTrans trans = mock(AuthzTrans.class);
        ApprovalDAO dao = mock(ApprovalDAO.class);
        List<Approval> list = null;
        approval.delayDelete(trans, dao, true, list, "text");
    }

}
