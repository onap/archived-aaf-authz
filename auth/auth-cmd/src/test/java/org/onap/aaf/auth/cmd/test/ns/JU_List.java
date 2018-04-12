/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.auth.cmd.test.ns;

import static org.junit.Assert.*;

import java.io.Writer;
import java.net.URI;

import org.onap.aaf.auth.cmd.ns.List;
import org.onap.aaf.auth.cmd.ns.NS;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.misc.env.APIException;

import aaf.v2_0.Nss;

import org.onap.aaf.auth.cmd.AAFcli;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class JU_List {
	
	List list;

	@Before
	public void setUp() throws APIException, LocatorException {
		PropAccess prop = new PropAccess();
		AuthzEnv aEnv = new AuthzEnv();
		Writer wtr = mock(Writer.class);
		Locator loc = mock(Locator.class);
		HMangr hman = new HMangr(aEnv, loc);		
		AAFcli aafcli = new AAFcli(prop, aEnv, wtr, hman, null, null);
		NS ns = new NS(aafcli);
		
		list = new List(ns);
	}
	
	@Test
	public void testReport() {
		Future<Nss> fu = mock(Future.class);
		Nss.Ns nss = new Nss.Ns();
		Nss ns = new Nss();
		fu.value = ns;
		fu.value.getNs();
		System.out.print(fu.value.getNs());
		
		list.report(null, "test");
		list.report(fu, "test");
	}

}
