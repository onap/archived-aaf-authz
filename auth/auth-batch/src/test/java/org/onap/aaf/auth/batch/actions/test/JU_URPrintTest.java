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

package org.onap.aaf.auth.batch.actions.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.auth.batch.actions.URPrint;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.LogTarget;

public class JU_URPrintTest {
	@Mock
	private AuthzTrans trans;
	@Mock
	LogTarget target;

	@Before
	public void setUp() throws Exception {
		initMocks(this);
		when(trans.info()).thenReturn(target);
	}

	@Test
	public void testURPrint() {
		URPrint print = new URPrint("Info Text");
		UserRole ur = new UserRole("user", "ns", "rname", Calendar.getInstance().getTime());
		assertEquals(Result.ok().status, print.exec(trans, ur, "text").status);
	}

}
