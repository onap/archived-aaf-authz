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

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.auth.batch.helpers.Creator;
import org.onap.aaf.auth.batch.helpers.NS;
import org.onap.aaf.auth.batch.helpers.NS.NSSplit;
import org.onap.aaf.auth.batch.helpers.creators.RowCreator;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;

import junit.framework.Assert;

public class JU_NS {

    NS ns;
    NSSplit nSSplit;

    @Before
    public void setUp() {
        ns = new NS("name", "description", "parent", 1, 1);
        nSSplit = new NSSplit("string", 1);
    }

    @Test
    public void testToString() {
        Assert.assertEquals("name", ns.toString());
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals(3373707, ns.hashCode());
    }

    @Test
    public void testEquals() {
        Assert.assertEquals(true, ns.equals("name"));
        Assert.assertEquals(false, ns.equals("name1"));
    }

    @Test
    public void testCompareTo() {
        NS nsValid = new NS("name", "description", "parent", 1, 1);
        Assert.assertEquals(0, ns.compareTo(nsValid));

        NS nsInvalid = new NS("name1", "description", "parent", 1, 1);
        Assert.assertEquals(-1, ns.compareTo(nsInvalid));
    }

    @Test
    public void testDeriveParent() {
        ns.deriveParent("d.ot.te.d");
    }

    @Test
    public void testLoadWithoutNS() {
        Trans trans = mock(Trans.class);
        Session session = mock(Session.class);
        Creator<NS> creator = mock(Creator.class);
        LogTarget target = mock(LogTarget.class);
        TimeTaken tt = mock(TimeTaken.class);
        ResultSet results = mock(ResultSet.class);
        ArrayList<Row> rows = new ArrayList<Row>();
        Row row = RowCreator.getRow();
        rows.add(row);

        when(trans.info()).thenReturn(target);
        when(trans.start("Read Namespaces", Env.REMOTE)).thenReturn(tt);
        when(trans.start("Load Namespaces", Env.SUB)).thenReturn(tt);
        when(session.execute(any(SimpleStatement.class))).thenReturn(results);
        when(results.iterator()).thenReturn(rows.iterator());
        when(creator.create(row)).thenReturn(ns);

        NS.load(trans, session, creator);
    }

    @Test
    public void testLoadOne() {
        Trans trans = mock(Trans.class);
        Session session = mock(Session.class);
        Creator<NS> creator = mock(Creator.class);
        LogTarget target = mock(LogTarget.class);
        TimeTaken tt = mock(TimeTaken.class);
        ResultSet results = mock(ResultSet.class);
        ArrayList<Row> rows = new ArrayList<Row>();
        Row row = RowCreator.getRow();
        rows.add(row);

        when(trans.info()).thenReturn(target);
        when(trans.start("Read Namespaces", Env.REMOTE)).thenReturn(tt);
        when(trans.start("Load Namespaces", Env.SUB)).thenReturn(tt);
        when(session.execute(any(SimpleStatement.class))).thenReturn(results);
        when(results.iterator()).thenReturn(rows.iterator());
        when(creator.create(row)).thenReturn(ns);

        NS.loadOne(trans, session, creator, "text");
    }

    @Test
    public void testCount() {
        Trans trans = mock(Trans.class);
        Session session = mock(Session.class);
        LogTarget target = mock(LogTarget.class);
        TimeTaken tt = mock(TimeTaken.class);
        ResultSet results = mock(ResultSet.class);
        ArrayList<Row> rows = new ArrayList<Row>();
        Row row = RowCreator.getRow();
        rows.add(row);

        when(trans.info()).thenReturn(target);
        when(trans.start("Count Namespaces", Env.REMOTE)).thenReturn(tt);
        when(session.execute(any(SimpleStatement.class))).thenReturn(results);
        when(results.one()).thenReturn(row);

        assertEquals(0, NS.count(trans, session));
    }

    @Test
    public void testV2() {
        NS.v2_0_11.create(RowCreator.getRow());
        assertEquals(NS.v2_0_11.select(), "SELECT name, description, parent, type, scope FROM authz.ns ");
    }

}
