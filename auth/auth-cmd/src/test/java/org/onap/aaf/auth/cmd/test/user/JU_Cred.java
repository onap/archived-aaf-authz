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

import org.onap.aaf.auth.cmd.user.Cred;
import org.onap.aaf.auth.cmd.user.User;
import org.onap.aaf.auth.cmd.test.HMangrStub;

public class JU_Cred {
	
	private Cred cred;

	@Mock private SecuritySetter<HttpURLConnection> ssMock;
	@Mock private Locator<URI> locMock;
	@Mock private Writer wrtMock;
	@Mock private Rcli<HttpURLConnection> clientMock;
	@Mock private Future<CredRequest> credRequestFutureMock;

	private PropAccess access;
	private HMangrStub hman;	
	private AuthzEnv aEnv;
	private AAFcli aafcli;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

		when(clientMock.create(any(String.class), any(RosettaDF.class), any(CredRequest.class))).thenReturn(credRequestFutureMock);
		when(clientMock.delete(any(String.class), any(RosettaDF.class), any(CredRequest.class))).thenReturn(credRequestFutureMock);
		when(clientMock.update(any(String.class), any(RosettaDF.class), any(CredRequest.class))).thenReturn(credRequestFutureMock);

		hman = new HMangrStub(access, locMock, clientMock);
		access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
		aEnv = new AuthzEnv();
		aafcli = new AAFcli(access, aEnv, wrtMock, hman, null, ssMock);

		cred = new Cred(new User(aafcli));
	}

	@Test
	public void testAdd() throws APIException, LocatorException, CadiException, URISyntaxException {
		cred.exec(0, new String[] {"add", "id", "password"});

		when(credRequestFutureMock.code()).thenReturn(406);
		cred.exec(0, new String[] {"add", "id", "password"});

		when(credRequestFutureMock.code()).thenReturn(202);
		cred.exec(0, new String[] {"add", "id", "password"});

		when(credRequestFutureMock.get(any(Integer.class))).thenReturn(true);
		cred.exec(0, new String[] {"add", "id", "password"});
		
		try {
			cred.exec(0, new String[] {"add", "id"});
			fail("Should have thrown an exception");
		} catch (CadiException e) {
			assertThat(e.getMessage(), is("Password Required"));
		}
	}
	
	@Test
	public void testDel() throws APIException, LocatorException, CadiException, URISyntaxException {
		cred.exec(0, new String[] {"del", "id", "entry"});

		when(credRequestFutureMock.code()).thenReturn(406);
		cred.exec(0, new String[] {"del", "id", "entry"});

		when(credRequestFutureMock.code()).thenReturn(202);
		cred.exec(0, new String[] {"del", "id", "entry"});

		when(credRequestFutureMock.get(any(Integer.class))).thenReturn(true);
		cred.exec(0, new String[] {"del", "id", "entry"});
	}
	
	@Test
	public void testReset() throws APIException, LocatorException, CadiException, URISyntaxException {
		cred.exec(0, new String[] {"reset", "id", "password"});

		when(credRequestFutureMock.code()).thenReturn(406);
		cred.exec(0, new String[] {"reset", "id", "password"});

		when(credRequestFutureMock.code()).thenReturn(202);
		cred.exec(0, new String[] {"reset", "id", "password"});

		when(credRequestFutureMock.get(any(Integer.class))).thenReturn(true);
		cred.exec(0, new String[] {"reset", "id", "password"});
	}
	
	@Test
	public void testExtend() throws APIException, LocatorException, CadiException, URISyntaxException {
		cred.exec(0, new String[] {"extend", "id"});

		when(credRequestFutureMock.code()).thenReturn(406);
		cred.exec(0, new String[] {"extend", "id"});

		when(credRequestFutureMock.code()).thenReturn(202);
		cred.exec(0, new String[] {"extend", "id"});

		when(credRequestFutureMock.get(any(Integer.class))).thenReturn(true);
		cred.exec(0, new String[] {"extend", "id"});
	}
	
	@Test
	public void testDetailedHelp() {
		StringBuilder sb = new StringBuilder();
		cred.detailedHelp(0, sb);
	}
}
