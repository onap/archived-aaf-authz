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

import aaf.v2_0.History;

import org.onap.aaf.auth.cmd.user.List;
import org.onap.aaf.auth.cmd.user.ListActivity;
import org.onap.aaf.auth.cmd.user.User;
import org.onap.aaf.auth.cmd.test.HMangrStub;

public class JU_ListActivity {
	
	private ListActivity listActivity;

	@Mock private SecuritySetter<HttpURLConnection> ssMock;
	@Mock private Locator<URI> locMock;
	@Mock private Writer wrtMock;
	@Mock private Rcli<HttpURLConnection> clientMock;
	@Mock private Future<History> historyFutureMock;
	@Mock private History historyMock;

	private PropAccess access;
	private HMangrStub hman;	
	private AuthzEnv aEnv;
	private AAFcli aafcli;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

		when(clientMock.read(any(String.class), any(RosettaDF.class))).thenReturn(historyFutureMock);
		
		historyFutureMock.value = historyMock;

		hman = new HMangrStub(access, locMock, clientMock);
		access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
		aEnv = new AuthzEnv();
		aafcli = new AAFcli(access, aEnv, wrtMock, hman, null, ssMock);

		List list = new List(new User(aafcli));
		listActivity = new ListActivity(list);
	}

	@Test
	public void testError() throws APIException, LocatorException, CadiException, URISyntaxException {
		when(historyFutureMock.get(any(Integer.class))).thenReturn(false);
		listActivity.exec(0, new String[] {"user"});
	}
	
	@Test
	public void testSuccess() throws APIException, LocatorException, CadiException, URISyntaxException {
		when(historyFutureMock.get(any(Integer.class))).thenReturn(true);
		listActivity.exec(0, new String[] {"user"});
	}
	
	@Test
	public void testDetailedHelp() {
		StringBuilder sb = new StringBuilder();
		listActivity.detailedHelp(0, sb);
	}
}
