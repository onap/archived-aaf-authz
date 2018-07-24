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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.ns.List;
import org.onap.aaf.auth.cmd.ns.ListNsKeysByAttrib;
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

import aaf.v2_0.Keys;
import aaf.v2_0.Nss;
import aaf.v2_0.Roles;

@RunWith(MockitoJUnitRunner.class)
public class JU_ListNsKeysByAttrib {
	
	private ListNsKeysByAttrib lsNsKeys;
	
	@Mock private SecuritySetter<HttpURLConnection> ssMock;
	@Mock private Locator<URI> locMock;
	@Mock private Writer wrtMock;
	@Mock private Rcli<HttpURLConnection> clientMock;
	@Mock private Keys keysMock;
	@Mock private Future<String> futureMock;
	@Mock private Future<Keys> futureKeysMock;


	private PropAccess access;
	private HMangrStub hman;	
	private AuthzEnv aEnv;
	private AAFcli aafcli;
	
	@Before
	public void setUp () throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
		MockitoAnnotations.initMocks(this);

		when(clientMock.create(any(), any(), any(String.class))).thenReturn(futureMock);
		when(clientMock.delete(any(), any(), any(String.class))).thenReturn(futureMock);
		when(clientMock.read(any(String.class), any(RosettaDF.class))).thenReturn(futureKeysMock);
		when(clientMock.update(any(), any(), any(String.class))).thenReturn(futureMock);

		hman = new HMangrStub(access, locMock, clientMock);
		access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
		aEnv = new AuthzEnv();
		aafcli = new AAFcli(access, aEnv, wrtMock, hman, null, ssMock);
		NS ns = new NS(aafcli);
		List ls = new List(ns);
		
		lsNsKeys = new ListNsKeysByAttrib(ls);
	}
	
	@Test
	public void testExecError() throws APIException, LocatorException, CadiException, URISyntaxException {
		lsNsKeys._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
	}
	
	@Test
	public void testExecSuccess1() throws APIException, LocatorException, CadiException, URISyntaxException {
		when(futureKeysMock.code()).thenReturn(404);
		lsNsKeys._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
		lsNsKeys._exec(1, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testExecSuccess2() throws APIException, LocatorException, CadiException, URISyntaxException {
		lsNsKeys._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
		when(futureKeysMock.get(any(Integer.class))).thenReturn(true);
		when(clientMock.read(any(String.class), any(RosettaDF.class))).thenReturn(futureKeysMock).thenReturn(futureKeysMock);		
		futureKeysMock.value = keysMock;
		ArrayList keyList = new ArrayList();
		keyList.add("test");
		when(keysMock.getKey()).thenReturn(keyList);
		lsNsKeys._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
	}
	
	@Test
	public void testDetailedHelp() {
		StringBuilder sb = new StringBuilder();
		lsNsKeys.detailedHelp(0, sb );
	}
}

