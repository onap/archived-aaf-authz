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
package org.onap.aaf.auth.cmd.test.mgmt;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;

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

import org.onap.aaf.auth.cmd.mgmt.Deny;
import org.onap.aaf.auth.cmd.mgmt.Mgmt;
import org.onap.aaf.auth.cmd.test.HMangrStub;

public class JU_Deny {
	
	private Deny deny;

	@Mock private SecuritySetter<HttpURLConnection> ssMock;
	@Mock private Locator<URI> locMock;
	@Mock private Writer wrtMock;
	@Mock private Rcli<HttpURLConnection> clientMock;
	@Mock private Future<String> stringFutureMock;
	@Mock private Future<Void> voidFutureMock;

	private PropAccess access;
	private HMangrStub hman;	
	private AuthzEnv aEnv;
	private AAFcli aafcli;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

		when(clientMock.create(any(), any(), any(String.class))).thenReturn(stringFutureMock);
		when(clientMock.delete(any(), any(), any(String.class))).thenReturn(stringFutureMock);
		when(clientMock.update(any(), any(), any(String.class))).thenReturn(stringFutureMock);

		when(clientMock.create(any(String.class), any(Class.class))).thenReturn(voidFutureMock);
		when(clientMock.delete(any(String.class), any(Class.class))).thenReturn(voidFutureMock);

		hman = new HMangrStub(access, locMock, clientMock);
		access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
		aEnv = new AuthzEnv();
		aafcli = new AAFcli(access, aEnv, wrtMock, hman, null, ssMock);

		deny = new Deny(new Mgmt(aafcli));
	}
	
	@Test
	public void testDenySomethingError() throws CadiException, APIException, LocatorException {
		Deny.DenySomething denySomething = deny.new DenySomething(deny, "id", "test");
		denySomething.exec(0, new String[]{"add", "test_name"});
		denySomething.exec(0, new String[]{"del", "test_name"});

		denySomething.exec(0, new String[]{"add", "test@somedomain.com"});
		denySomething.exec(0, new String[]{"del", "test@somedomain.com"});

		denySomething = deny.new DenySomething(deny, "not_an_id", "test");
		denySomething.exec(0, new String[]{"add", "test_name"});
		denySomething.exec(0, new String[]{"del", "test_name"});
	}

	@Test
	public void testDenySomethingSuccess() throws CadiException, APIException, LocatorException {
		when(voidFutureMock.get(any(Integer.class))).thenReturn(true);

		Deny.DenySomething denySomething = deny.new DenySomething(deny, "id", "test");
		denySomething.exec(0, new String[]{"add", "test_name"});
		denySomething.exec(0, new String[]{"del", "test_name"});

		denySomething.exec(0, new String[]{"add", "test@somedomain.com"});
		denySomething.exec(0, new String[]{"del", "test@somedomain.com"});

		denySomething = deny.new DenySomething(deny, "not_an_id", "test");
		denySomething.exec(0, new String[]{"add", "test_name"});
		denySomething.exec(0, new String[]{"del", "test_name"});
	}
	

}
