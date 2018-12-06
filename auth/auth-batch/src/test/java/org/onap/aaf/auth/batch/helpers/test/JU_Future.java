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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.auth.batch.helpers.Creator;
import org.onap.aaf.auth.batch.helpers.Future;
import org.onap.aaf.auth.batch.helpers.creators.RowCreator;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;

public class JU_Future {

	Future future;
	Date start;
	Date expires;
	ByteBuffer bBuff;

	@Before
	public void setUp() {
		UUID id = new UUID(0, 0);
		start = new Date();
		expires = new Date();
		future = new Future(id, "Re-Validate Ownership for AAF Namespace '\'test\'test", "target", start, expires,
				bBuff);
	}

	@Test
	public void testId() {
		Assert.assertTrue(future.id() instanceof UUID);
	}

	@Test
	public void testMemo() {
		Assert.assertEquals("Re-Validate Ownership for AAF Namespace '\'test\'test", future.memo());
	}

	@Test
	public void testStart() {
		Assert.assertTrue(future.start() instanceof Date);
	}

	@Test
	public void testExpires() {
		Assert.assertTrue(future.expires() instanceof Date);
	}

	@Test
	public void testTarget() {
		Assert.assertEquals("target", future.target());
	}

	@Test
	public void testExpunge() {
		future.expunge();
	}

	@Test
	public void testCompareTo() {
		future.compareTo(null);
		future.compareTo(future);
	}

	@Test
	public void testResetLocalData() {
		Future.resetLocalData();
	}

	@Test
	public void testSizeForDeletion() {
		Assert.assertEquals(0, Future.sizeForDeletion());
	}

	@Test
	public void testPendingDelete() {
		Assert.assertEquals(false, Future.pendingDelete(future));
	}

	@Test
	public void testLoad() {
		Session session = mock(Session.class);
		Trans trans = mock(Trans.class);
		@SuppressWarnings("unchecked")
		Creator<Future> creator = (Creator<Future>)mock(Creator.class);
		LogTarget target = mock(LogTarget.class);
		TimeTaken tt = mock(TimeTaken.class);
		ResultSet results = mock(ResultSet.class);
		ArrayList<Row> rows = new ArrayList<Row>();
		Row row = RowCreator.getRow();
		rows.add(row);

		when(results.all()).thenReturn(rows);
		when(trans.info()).thenReturn(target);
		when(trans.start("Load Futures", Env.REMOTE)).thenReturn(tt);
		when(trans.start("Process Futures", Env.SUB)).thenReturn(tt);
		when(session.execute(any(SimpleStatement.class))).thenReturn(results);
		when(creator.create(row)).thenReturn(future);

		Future.load(trans, session, creator);
	}

	@Test
	public void testV2() {
		Future.v2_0_17.create(RowCreator.getRow());
		assertEquals(Future.v2_0_17.select(), "select id,memo,target,start,expires from authz.future");
	}

	@Test
	public void testWithConstruct() {
		Future.withConstruct.create(RowCreator.getRow());
		assertEquals(Future.withConstruct.select(), "select id,memo,target,start,expires,construct from authz.future");
	}

	@Test
	public void testDelayedDeleteWithDryRun() {
		AuthzTrans trans = mock(AuthzTrans.class);
		LogTarget target = mock(LogTarget.class);

		when(trans.info()).thenReturn(target);

		assertEquals(Result.ok().status, future.delayedDelete(trans, null, true, "text").status);
	}

	@Test
	public void testDelayedDeleteNonDryRun() {
		AuthzTrans trans = mock(AuthzTrans.class);
		LogTarget target = mock(LogTarget.class);
		FutureDAO fd = mock(FutureDAO.class);

		when(trans.info()).thenReturn(target);
		when(fd.delete(any(AuthzTrans.class), any(FutureDAO.Data.class), any(Boolean.class))).thenReturn(Result.ok());

		assertEquals(Result.ok().status, future.delayedDelete(trans, fd, false, "text").status);
	}

}
