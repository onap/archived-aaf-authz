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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.auth.batch.helpers.Creator;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;

import junit.framework.Assert;

public class JU_UserRole {

    UserRole userRole;
    UserRole userRole1;
    Date date;
    PrintStream ds;

    @Before
    public void setUp() {
        date = new Date();
        userRole = new UserRole("user", "ns", "rname", date);
        userRole = new UserRole("user", "role", "ns", "rname", date);
    }

    @Test
    public void testTotalLoaded() {
        Assert.assertEquals(0, userRole.totalLoaded());
    }

    @Test
    public void testDeleted() {
        Assert.assertEquals(0, userRole.deleted());
    }

    @Test
    public void testExpunge() {
        userRole.expunge();
    }

    @Test
    public void testSetDeleteStream() {
        userRole.setDeleteStream(ds);
    }

    @Test
    public void testSetRecoverStream() {
        userRole.setRecoverStream(ds);
    }

    @Test
    public void testUrdd() {
        Assert.assertTrue(userRole.urdd() instanceof UserRoleDAO.Data);
    }

    @Test
    public void testUser() {
        Assert.assertEquals("user", userRole.user());
    }

    @Test
    public void testRole() {
        Assert.assertEquals("role", userRole.role());
    }

    @Test
    public void testNs() {
        Assert.assertEquals("ns", userRole.ns());
    }

    @Test
    public void testRName() {
        Assert.assertEquals("rname", userRole.rname());
    }

    @Test
    public void testExpires() {
        Assert.assertEquals(date, userRole.expires());
        userRole.expires(date);
    }

    @Test
    public void testToString() {
        Assert.assertTrue(userRole.toString() instanceof String);
    }

    @Test
    public void testGet() {
        userRole.get("u", "r");
    }

    @Test
    public void testResetLocalData() {
        userRole.resetLocalData();
    }

    @Test
    public void testLoad() {
        Creator<UserRole> creator = mock(Creator.class);
        Trans trans = mock(Trans.class);
        Session session = mock(Session.class);
        LogTarget target = mock(LogTarget.class);
        TimeTaken tt = mock(TimeTaken.class);
        ResultSet results = mock(ResultSet.class);

        List<Row> rows = new ArrayList<Row>();

        when(trans.info()).thenReturn(target);
        when(trans.start("Read UserRoles", Env.REMOTE)).thenReturn(tt);
        when(trans.start("Load UserRole", Env.SUB)).thenReturn(tt);
        when(session.execute(any(SimpleStatement.class))).thenReturn(results);
        when(results.iterator()).thenReturn(rows.iterator());

    }

}
