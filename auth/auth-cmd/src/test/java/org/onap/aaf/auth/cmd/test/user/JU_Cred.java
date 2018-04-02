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

import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.user.Cred;
import org.onap.aaf.auth.cmd.user.User;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.misc.env.APIException;

@RunWith(MockitoJUnitRunner.class)
public class JU_Cred {

	private static Cred testCred;
	private static User testUser;
	private static AuthzEnv env;


	@BeforeClass
	public static void setUp() throws FileNotFoundException, APIException {
		
		testCred = mock(Cred.class);
		testUser = mock(User.class);
		env = mock(AuthzEnv.class);
		Mockito.when(env.getProperty(Cmd.STARTDATE,null)).thenReturn(null);
		Mockito.when(env.getProperty(Cmd.ENDDATE,null)).thenReturn(null);
		
	}

	@Test
	public void exec() throws CadiException, APIException, LocatorException, FileNotFoundException {
		boolean isNullpointer=false;
		AAFcli aaFcli=	new AAFcli(env, new PrintWriter("temp"), null, null, null);
	User user= new User(aaFcli);
	 Cred testCred= new Cred(user);
	try {
		testCred._exec(0, "add", "del", "reset", "extend");
	} catch (Exception e) {
		isNullpointer=true;
	} 
	assertEquals(isNullpointer, true);
	}


	@Test
	public void exec_add() {		
		try {
			assertNotNull(testCred._exec(0, "zeroed","add","del","reset","extend"));
		} catch (CadiException | APIException | LocatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void exec_del() {		
		try {
			assertNotNull(testCred._exec(1, "zeroed","add","del","reset","extend"));
		} catch (CadiException | APIException | LocatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void exec_reset() {		
		try {
			assertNotNull(testCred._exec(2, "zeroed","add","del","reset","extend"));
		} catch (CadiException | APIException | LocatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void exec_extend() {		
		try {
			assertNotNull(testCred._exec(3, "zeroed","add","del","reset","extend"));
		} catch (CadiException | APIException | LocatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
