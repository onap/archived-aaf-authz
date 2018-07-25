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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.rosetta.env.RosettaDF;

import aaf.v2_0.Users;

import org.onap.aaf.auth.cmd.user.List;
import org.onap.aaf.auth.cmd.user.ListForPermission;
import org.onap.aaf.auth.cmd.test.HMangrStub;
import org.onap.aaf.auth.cmd.user.User;

public class JU_ListForPermission {
	
	private ListForPermission listPerms;

	@Mock private SecuritySetter<HttpURLConnection> ssMock;
	@Mock private Locator<URI> locMock;
	@Mock private Writer wrtMock;
	@Mock private Rcli<HttpURLConnection> clientMock;
	@Mock private Future<Users> usersFutureMock;
	@Mock private Users usersMock;
	@Mock private Users.User userMock1;
	@Mock private Users.User userMock2;

	private PropAccess access;
	private HMangrStub hman;	
	private AuthzEnv aEnv;
	private AAFcliStub aafcliStub;
	
	private ArrayList<Users.User> users;
	
	private boolean isTest;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

		when(clientMock.read(any(String.class), any(RosettaDF.class))).thenReturn(usersFutureMock);
		
		
		when(userMock1.getId()).thenReturn("id1");
		when(userMock2.getId()).thenReturn("id2");

		users = new ArrayList<>();
		users.add(userMock1);
		users.add(userMock2);

		when(usersMock.getUser()).thenReturn(users);

		usersFutureMock.value = usersMock;

		hman = new HMangrStub(access, locMock, clientMock);
		access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
		aEnv = new AuthzEnv();
		aafcliStub = new AAFcliStub(access, aEnv, wrtMock, hman, null, ssMock);
		isTest = true;

		List list = new List(new User(aafcliStub));
		listPerms = new ListForPermission(list);
	}

	@Test
	public void test() throws APIException, LocatorException, CadiException, URISyntaxException {
		when(usersFutureMock.get(any(Integer.class))).thenReturn(false);
		listPerms.exec(0, new String[] {"type", "instance", "action"});
		
		when(usersFutureMock.get(any(Integer.class))).thenReturn(true);
		listPerms.exec(0, new String[] {"type", "instance", "action"});

		when(usersFutureMock.code()).thenReturn(404);
		listPerms.exec(0, new String[] {"type", "instance", "action"});
		
		isTest = false;
		listPerms.exec(0, new String[] {"type", "instance", "action"}); 

		listPerms.exec(0, new String[] {"type", "\\*", "\\*"});
	}

	@Test
	public void testDetailedHelp() {
		StringBuilder sb = new StringBuilder();
		listPerms.detailedHelp(0, sb);
	}

	private class AAFcliStub extends AAFcli {
		public AAFcliStub(Access access, AuthzEnv env, Writer wtr, HMangr hman, SecurityInfoC<HttpURLConnection> si,
			SecuritySetter<HttpURLConnection> ss) throws APIException, CadiException {
			super(access, env, wtr, hman, si, ss);
		}

		@Override
		public boolean isTest() {
			return isTest;
		}
	}

}
