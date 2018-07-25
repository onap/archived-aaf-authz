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

import aaf.v2_0.Approvals;

import org.onap.aaf.auth.cmd.user.List;
import org.onap.aaf.auth.cmd.user.ListApprovals;
import org.onap.aaf.auth.cmd.user.User;
import org.onap.aaf.auth.cmd.test.HMangrStub;

public class JU_ListApprovals {
	
	private ListApprovals listApprovals;

	@Mock private SecuritySetter<HttpURLConnection> ssMock;
	@Mock private Locator<URI> locMock;
	@Mock private Writer wrtMock;
	@Mock private Rcli<HttpURLConnection> clientMock;
	@Mock private Future<Approvals> approvalsFutureMock;
	@Mock private Approvals approvalsMock;

	private PropAccess access;
	private HMangrStub hman;	
	private AuthzEnv aEnv;
	private AAFcli aafcli;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

		when(clientMock.read(any(String.class), any(RosettaDF.class))).thenReturn(approvalsFutureMock);
		
		approvalsFutureMock.value = approvalsMock;

		hman = new HMangrStub(access, locMock, clientMock);
		access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
		aEnv = new AuthzEnv();
		aafcli = new AAFcli(access, aEnv, wrtMock, hman, null, ssMock);

		List list = new List(new User(aafcli));
		listApprovals = new ListApprovals(list);
	}

	@Test
	public void testUser() throws APIException, LocatorException, CadiException, URISyntaxException {
		when(approvalsFutureMock.get(any(Integer.class))).thenReturn(false);
		listApprovals.exec(0, new String[] {"user", "id"});
		
		when(approvalsFutureMock.get(any(Integer.class))).thenReturn(true);
		listApprovals.exec(0, new String[] {"user", "id"});

		when(approvalsFutureMock.code()).thenReturn(404);
		listApprovals.exec(0, new String[] {"user", "id"});
	}
	
	@Test
	public void testApprover() throws APIException, LocatorException, CadiException, URISyntaxException {
		when(approvalsFutureMock.get(any(Integer.class))).thenReturn(false);
		listApprovals.exec(0, new String[] {"approver", "id"});
		
		when(approvalsFutureMock.get(any(Integer.class))).thenReturn(true);
		listApprovals.exec(0, new String[] {"approver", "id"});

		when(approvalsFutureMock.code()).thenReturn(404);
		listApprovals.exec(0, new String[] {"approver", "id"});
	}
	
	@Test
	public void testTicket() throws CadiException, APIException, LocatorException {
		when(approvalsFutureMock.get(any(Integer.class))).thenReturn(false);
		listApprovals.exec(0, new String[] {"ticket", "id"});
		
		when(approvalsFutureMock.get(any(Integer.class))).thenReturn(true);
		listApprovals.exec(0, new String[] {"ticket", "id"});

		when(approvalsFutureMock.code()).thenReturn(404);
		listApprovals.exec(0, new String[] {"ticket", "id"});
	}
	
	@Test
	public void testDetailedHelp() {
		StringBuilder sb = new StringBuilder();
		listApprovals.detailedHelp(0, sb);
	}
}
