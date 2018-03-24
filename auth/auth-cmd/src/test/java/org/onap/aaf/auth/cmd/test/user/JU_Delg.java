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
 * *
 ******************************************************************************/
package org.onap.aaf.auth.cmd.test.user;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cmd.user.Delg;
import org.onap.aaf.auth.cmd.user.User;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.misc.env.APIException;

@RunWith(MockitoJUnitRunner.class)
public class JU_Delg {
	
	private static User testUser;
	private static Delg delg;
	
	@BeforeClass
	public static void setUp() throws APIException {
		testUser = mock(User.class);
		delg = mock(Delg.class);
	}
	
	@Test
	public void exec_add() {
		try {
			assertEquals(delg._exec(0, "zero","add","upd","del"), 0);
		} catch (CadiException | APIException | LocatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void exec_upd() {
		try {
			assertEquals(delg._exec(1, "zero","add","upd","del"), 0);
		} catch (CadiException | APIException | LocatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void exec_del() {
		try {
			assertEquals(delg._exec(2, "zero","add","upd","del"), 0);
		} catch (CadiException | APIException | LocatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
