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
package org.onap.aaf.auth.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.auth.actions.Message;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.helpers.Notification.TYPE;
import org.onap.aaf.auth.helpers.creators.RowCreator;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

public class JU_NotificationTest {

	@Mock
	private AuthzTrans trans;
	@Mock
	private Creator<Notification> creator;
	@Mock
	private TimeTaken tt;

	@Mock
	private LogTarget logTarget;
	private Message msg;

	@Before
	public void setUp() throws Exception {
		initMocks(this);

		msg = new Message();
		msg.line("%n", "Message");

		when(trans.info()).thenReturn(logTarget);
		when(trans.start("Load Notify", Env.REMOTE)).thenReturn(tt);
	}

	@Test
	public void test() {
		Notification notification = Notification.create("user", TYPE.CN);
		assertEquals(notification.checksum(), 0);
		notification.set(msg);
		assertEquals(notification.checksum(), 10);
		assertNull(Notification.get("user", TYPE.CN));
		assertTrue(notification.update(trans, null, true));
		assertTrue(notification.toString().contains("\"user\",\"CN\","));

		Notification.v2_0_18.create(RowCreator.getRow());
		assertEquals(Notification.v2_0_18.select(), "SELECT user,type,last,checksum FROM authz.notify LIMIT 100000");

	}
}