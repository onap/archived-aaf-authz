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
package org.onap.aaf.auth.cmd.test.ns;

import org.junit.Assert;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.ns.List;
import org.onap.aaf.auth.cmd.ns.ListUsers;
import org.onap.aaf.auth.cmd.ns.ListUsersInRole;
import org.onap.aaf.auth.cmd.ns.NS;
import org.onap.aaf.auth.cmd.test.HMangrStub;
import org.onap.aaf.auth.cmd.test.JU_AAFCli;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.rosetta.env.RosettaDF;

import aaf.v2_0.Nss;
import aaf.v2_0.Role;
import aaf.v2_0.Roles;
import aaf.v2_0.Users;

@RunWith(MockitoJUnitRunner.class)
public class JU_ListUsersInRole {

	private ListUsersInRole lsUserinRole;
	
	@Mock private SecuritySetter<HttpURLConnection> ssMock;
	@Mock private Locator<URI> locMock;
	@Mock private Writer wrtMock;
	@Mock private Rcli<HttpURLConnection> clientMock;
	@Mock private Nss nssMock;
	@Mock private Nss.Ns nsMock;
	@Mock private Future<String> futureMock;
	@Mock private Future<Nss> futureNssMock;
	@Mock private Future<Roles> futureRolesMock;
	@Mock private Roles rolesMock;
	@Mock private Role roleMock;
	@Mock private Future<Users> futureUsersMock;
	@Mock private Users usersMock;
	@Mock private Users.User userMock;
	@Mock private XMLGregorianCalendar xmlGreg;

	private PropAccess access;
	private HMangrStub hman;	
	private AuthzEnv aEnv;
	private AAFcli aafcli;

	@Before
	public void setUp() throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

		when(clientMock.create(any(), any(), any(String.class))).thenReturn(futureMock);
		when(clientMock.delete(any(), any(), any(String.class))).thenReturn(futureMock);
		when(clientMock.read(any(String.class), any(RosettaDF.class))).thenReturn(futureNssMock);
		when(clientMock.update(any(), any(), any(String.class))).thenReturn(futureMock);

		hman = new HMangrStub(access, locMock, clientMock);
		access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
		aEnv = new AuthzEnv();
		aafcli = new AAFcli(access, aEnv, wrtMock, hman, null, ssMock);
		NS ns = new NS(aafcli);
		List ls = new List(ns);//possible wrong import, remove import org.onap.aaf.auth.cmd.ns to see other options
		ListUsers lsU = new ListUsers(ls);
		lsUserinRole = new ListUsersInRole(lsU);
	}
	
	@Test
	public void testExecError() throws APIException, LocatorException, CadiException, URISyntaxException {
		lsUserinRole._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
	}
	
	@Test
	public void testExecSuccess1() throws APIException, LocatorException, CadiException, URISyntaxException {
		when(futureNssMock.code()).thenReturn(404);
		lsUserinRole._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
		lsUserinRole._exec(1, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExecSuccess2() throws Exception {
		lsUserinRole._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
		when(futureNssMock.get(any(Integer.class))).thenReturn(true);
		when(futureRolesMock.get(any(Integer.class))).thenReturn(true);
		when(futureUsersMock.get(any(Integer.class))).thenReturn(true);
		when(clientMock.read(any(String.class), any(RosettaDF.class))).thenReturn(futureNssMock).thenReturn(futureRolesMock).thenReturn(futureUsersMock);		
		futureNssMock.value = nssMock;
		ArrayList<Nss.Ns> nsArray = new ArrayList<>();
		nsArray.add(nsMock);
		when(nssMock.getNs()).thenReturn(nsArray);
		futureRolesMock.value = rolesMock;
		ArrayList<Role> roleArray = new ArrayList();
		roleArray.add(roleMock);
		when(rolesMock.getRole()).thenReturn(roleArray);
		when(roleMock.getName()).thenReturn("name");
		futureUsersMock.value = usersMock;
		ArrayList<Users.User> userArray = new ArrayList();
		userArray.add(userMock);
		when(usersMock.getUser()).thenReturn(userArray);
		when(userMock.getId()).thenReturn("test");
		when(userMock.getExpires()).thenReturn(xmlGreg);
		lsUserinRole._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
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
