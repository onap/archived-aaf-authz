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
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.cmd.test.role;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.role.List;
import org.onap.aaf.auth.cmd.role.Role;
import org.onap.aaf.auth.cmd.test.JU_AAFCli;
import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.misc.env.APIException;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.security.Principal;

import org.junit.Test;

public class JU_List {
	
	AAFcli cli;

	private class ListRolesStub extends List {

		public ListRolesStub(Role parent) {
			super(parent);
			// TODO Auto-generated constructor stub
		}


	}
	
	@Before
	public void setUp() throws APIException, LocatorException, GeneralSecurityException, IOException{
		cli = JU_AAFCli.getAAfCli();
	}
	
	@Test
	public void testRoles() throws APIException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Role role = new Role(cli);
		ListRolesStub listStub = new ListRolesStub(role);
		Future future = mock(Future.class);
		Rcli rcli = mock(Rcli.class);
		
		Class c = listStub.getClass();
		Class[] cArg = new Class[3];
		cArg[0] = Future.class;
		cArg[1] = Rcli.class;
		cArg[2] = String.class;//Steps to test a protected method
		//Method listMethod = c.getDeclaredMethod("list", cArg);
		//listMethod.setAccessible(true);
		//listMethod.invoke(listStub, future, rcli, "test");
		
	}

}
