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
import org.onap.aaf.auth.cmd.perm.ListByName;
import org.onap.aaf.auth.cmd.perm.Perm;
import org.onap.aaf.auth.cmd.role.Role;
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

import aaf.v2_0.History;

import org.onap.aaf.auth.cmd.test.HMangrStub;

@RunWith(MockitoJUnitRunner.class)
public class JU_ListByName {
	
	private ListByName listByName;

	@Mock private SecuritySetter<HttpURLConnection> ssMock;
	@Mock private Locator<URI> locMock;
	@Mock private Writer wrtMock;
	@Mock private Rcli<HttpURLConnection> clientMock;
	@Mock private Future<History> futureMock;
	@Mock private History histMock;

	private PropAccess access;
	private HMangrStub hman;	
	private AuthzEnv aEnv;
	private AAFcliStub aafcli;
	
	private boolean detailed = false;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp () throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

		when(clientMock.read(any(String.class), any(RosettaDF.class))).thenReturn(futureMock);
		futureMock.value = histMock;

		hman = new HMangrStub(access, locMock, clientMock);
		access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
		aEnv = new AuthzEnv();
		aafcli = new AAFcliStub(access, aEnv, wrtMock, hman, null, ssMock);

		listByName = new ListByName(new List(new Perm(new Role(aafcli))));
	}
	
	@Test
	public void testExec() throws APIException, LocatorException, CadiException, URISyntaxException {
		listByName._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});

		detailed = true;
		listByName._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
	}
	
	@Test
	public void testDetailedHelp() {
		StringBuilder sb = new StringBuilder();
		listByName.detailedHelp(0, sb);
	}

	private class AAFcliStub extends AAFcli {
		public AAFcliStub(Access access, AuthzEnv env, Writer wtr, HMangr hman, SecurityInfoC<HttpURLConnection> si,
			SecuritySetter<HttpURLConnection> ss) throws APIException, CadiException {
			super(access, env, wtr, hman, si, ss);
		}

		@Override
		public boolean isDetailed() {
			return detailed;
		}
	}

}
