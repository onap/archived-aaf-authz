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
package org.onap.aaf.auth.cmd.test.perm;

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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.perm.List;
import org.onap.aaf.auth.cmd.perm.ListActivity;
import org.onap.aaf.auth.cmd.perm.Perm;
import org.onap.aaf.auth.cmd.role.Role;
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

import org.onap.aaf.auth.cmd.test.HMangrStub;

@RunWith(MockitoJUnitRunner.class)
public class JU_ListActivity {
	
	private static ListActivity listActivity;

	@Mock private SecuritySetter<HttpURLConnection> ssMock;
	@Mock private Locator<URI> locMock;
	@Mock private Writer wrtMock;
	@Mock private Rcli<HttpURLConnection> clientMock;
	@Mock private Future<History> futureMock;
	@Mock private History histMock;
	
	private PropAccess access;
	private HMangrStub hman;	
	private AuthzEnv aEnv;
	private AAFcli aafcli;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp () throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

		when(clientMock.read(any(String.class), any(RosettaDF.class))).thenReturn(futureMock);
		futureMock.value = histMock;

		hman = new HMangrStub(access, locMock, clientMock);
		access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
		aEnv = new AuthzEnv();
		aafcli = new AAFcli(access, aEnv, wrtMock, hman, null, ssMock);

		listActivity = new ListActivity(new List(new Perm(new Role(aafcli))));
	}
	
	@Test
	public void testExecError() throws APIException, LocatorException, CadiException, URISyntaxException {
		listActivity._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
	}
	
	@Test
	public void testExecSuccess() throws APIException, LocatorException, CadiException, URISyntaxException {
		when(futureMock.get(any(Integer.class))).thenReturn(true);
		listActivity._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
	}
	
	@Test
	public void testDetailedHelp() {
		StringBuilder sb = new StringBuilder();
		listActivity.detailedHelp(0, sb);
	}
}
