/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package org.onap.aaf.cmd.ns;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.cmd.AAFcli;
import org.onap.aaf.cmd.JU_AAFCli;

@RunWith(MockitoJUnitRunner.class)
public class JU_ListUsersInRole {

	private static ListUsersInRole lsUserinRole;

	@BeforeClass
	public static void setUp() throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
		AAFcli cli = JU_AAFCli.getAAfCli();
		NS ns = new NS(cli);
		List ls = new List(ns);
		ListUsers lsU = new ListUsers(ls);
		lsUserinRole = new ListUsersInRole(lsU);
	}

	@Test
	public void exec() {
		try {
			assertEquals(lsUserinRole._exec(0, "add", "del", "reset", "extend"), 500);
		} catch (Exception e) {
			assertEquals(e.getMessage(), "java.net.UnknownHostException: DME2RESOLVE");
		}
	}

	@Test
	public void detailedHelp() {
		boolean hasNoError = true;
		try {
			lsUserinRole.detailedHelp(1, new StringBuilder("test"));
		} catch (Exception e) {
			hasNoError = false;
		}
		assertEquals(hasNoError, true);
	}
}
