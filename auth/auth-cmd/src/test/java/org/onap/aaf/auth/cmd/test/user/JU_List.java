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

import static org.mockito.Mockito.when;
import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Rcli;

import aaf.v2_0.Approval;
import aaf.v2_0.Approvals;
import aaf.v2_0.Delg;
import aaf.v2_0.Delgs;

import org.onap.aaf.auth.cmd.user.List;
import org.onap.aaf.auth.cmd.user.User;
import org.onap.aaf.auth.cmd.test.HMangrStub;

public class JU_List {
	
	private List list;

	@Mock private SecuritySetter<HttpURLConnection> ssMock;
	@Mock private Locator<URI> locMock;
	@Mock private Writer wrtMock;
	@Mock private Rcli<HttpURLConnection> clientMock;

	@Mock private Approvals approvalsMock;
	@Mock private Approval approvalMock1;
	@Mock private Approval approvalMock2;
	@Mock private Approval approvalMock3;

	@Mock private Delgs delgsMock;
	@Mock private Delg delgMock1;
	@Mock private Delg delgMock2;
	@Mock private Delg delgMock3;

	private PropAccess access;
	private HMangrStub hman;	
	private AuthzEnv aEnv;
	private AAFcli aafcli;
	
	// These should probably be generic Lists, but there's already a classname clash, so...
	private ArrayList<Approval> approvals;
	private ArrayList<Delg> delgs;

	@Before
	public void setUp() throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
		MockitoAnnotations.initMocks(this);
		
		when(approvalMock1.getTicket()).thenReturn("id1");
		when(approvalMock2.getTicket()).thenReturn("id2");
		when(approvalMock3.getTicket()).thenReturn("id2");

		approvals = new ArrayList<>();
		approvals.add(approvalMock1);
		approvals.add(approvalMock2);
		approvals.add(approvalMock3);

		when(approvalsMock.getApprovals()).thenReturn(approvals);

		delgs = new ArrayList<>();
		delgs.add(delgMock1);
		delgs.add(delgMock2);
		delgs.add(delgMock3);

		when(delgsMock.getDelgs()).thenReturn(delgs);

		hman = new HMangrStub(access, locMock, clientMock);
		access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
		aEnv = new AuthzEnv();
		aafcli = new AAFcli(access, aEnv, wrtMock, hman, null, ssMock);
		
		list = new List(new User(aafcli));
	}
	
	// NOTE: the first report method is package private - it will need to be tested in a packaged class

	@Test
	public void testReport2() {
		list.report(approvalsMock, "title", "id");
	}
	
	@Test
	public void testReport3() {
		list.report(delgsMock, "title", "id");
	}

}
