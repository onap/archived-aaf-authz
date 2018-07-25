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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.auth.cmd.AAFcli;
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

import aaf.v2_0.CredRequest;

import org.onap.aaf.auth.cmd.user.Role;
import org.onap.aaf.auth.cmd.user.User;
import org.onap.aaf.auth.cmd.test.HMangrStub;

public class JU_Role {
	
	private Role role;

	@Mock private SecuritySetter<HttpURLConnection> ssMock;
	@Mock private Locator<URI> locMock;
	@Mock private Writer wrtMock;
	@Mock private Rcli<HttpURLConnection> clientMock;
	@Mock private Future<CredRequest> credRequestFutureMock;
	@Mock private Future<Void> voidFutureMock;

	private PropAccess access;
	private HMangrStub hman;	
	private AuthzEnv aEnv;
	private AAFcli aafcli;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

		when(clientMock.create(any(String.class), any(RosettaDF.class), any(CredRequest.class))).thenReturn(credRequestFutureMock);
		when(clientMock.delete(any(String.class), any(Class.class))).thenReturn(credRequestFutureMock);
		when(clientMock.update(any(String.class))).thenReturn(voidFutureMock);
		when(clientMock.update(any(String.class), any(RosettaDF.class), any(CredRequest.class))).thenReturn(credRequestFutureMock);

		hman = new HMangrStub(access, locMock, clientMock);
		access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
		aEnv = new AuthzEnv();
		aafcli = new AAFcli(access, aEnv, wrtMock, hman, null, ssMock);

		role = new Role(new User(aafcli));
	}

	@Test
	public void testAdd() throws APIException, LocatorException, CadiException, URISyntaxException {
		role.exec(0, new String[] {"add", "user", "role"});

		when(credRequestFutureMock.code()).thenReturn(202);
		role.exec(0, new String[] {"add", "user", "role"});

		when(credRequestFutureMock.code()).thenReturn(404);
		role.exec(0, new String[] {"add", "user", "role"});

		when(credRequestFutureMock.get(any(Integer.class))).thenReturn(true);
		role.exec(0, new String[] {"add", "user", "role"});
		
		try {
			role.exec(0, new String[] {"add", "user"});
			fail("Should have thrown an exception");
		} catch (CadiException e) {
			assertThat(e.getMessage(), is("Too few args: role <add|del|setTo|extend> <user> [role[,role]* (!REQ S)] "));
		}
	}
	
	@Test public void testDel() throws APIException, LocatorException, CadiException, URISyntaxException {
		role.exec(0, new String[] {"del", "user", "role"});

		when(credRequestFutureMock.code()).thenReturn(202);
		role.exec(0, new String[] {"del", "user", "role"});

		when(credRequestFutureMock.code()).thenReturn(404);
		role.exec(0, new String[] {"del", "user", "role"});

		when(credRequestFutureMock.get(any(Integer.class))).thenReturn(true);
		role.exec(0, new String[] {"del", "user", "role"});
		
		try {
			role.exec(0, new String[] {"del", "user"});
			fail("Should have thrown an exception");
		} catch (CadiException e) {
			assertThat(e.getMessage(), is("Too few args: role <add|del|setTo|extend> <user> [role[,role]* (!REQ S)] "));
		}
	}
	
	@Test
	public void testSetTo() throws APIException, LocatorException, CadiException, URISyntaxException {
		role.exec(0, new String[] {"setTo", "user"});

		role.exec(0, new String[] {"setTo", "user", "role"});

		when(credRequestFutureMock.get(any(Integer.class))).thenReturn(true);
		role.exec(0, new String[] {"setTo", "user"});
	}
	
	@Test
	public void testExtend() throws APIException, LocatorException, CadiException, URISyntaxException {
		role.exec(0, new String[] {"extend", "user", "role"});

		when(voidFutureMock.code()).thenReturn(202);
		role.exec(0, new String[] {"extend", "user", "role"});

		when(voidFutureMock.code()).thenReturn(404);
		role.exec(0, new String[] {"extend", "user", "role"});

		when(voidFutureMock.get(any(Integer.class))).thenReturn(true);
		role.exec(0, new String[] {"extend", "user", "role"});
		
		try {
			role.exec(0, new String[] {"extend", "user"});
			fail("Should have thrown an exception");
		} catch (CadiException e) {
			assertThat(e.getMessage(), is("Too few args: role <add|del|setTo|extend> <user> [role[,role]* (!REQ S)] "));
		}
	}
	
	@Test
	public void testDetailedHelp() {
		StringBuilder sb = new StringBuilder();
		role.detailedHelp(0, sb);
	}
}
