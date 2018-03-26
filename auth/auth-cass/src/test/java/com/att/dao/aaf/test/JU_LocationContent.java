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

package com.att.dao.aaf.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

import locate.v1_0.MgmtEndpoint;
import locate.v1_0.MgmtEndpoint.SpecialPorts;
import locate.v1_0.MgmtEndpoints;

public class JU_LocationContent {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		PropAccess access = new PropAccess();
		RosettaEnv env = new RosettaEnv(access.getProperties());
		try {
			RosettaDF<MgmtEndpoints> medf = env.newDataFactory(MgmtEndpoints.class);
			medf.out(TYPE.JSON);
			medf.option(Data.PRETTY);
			MgmtEndpoint me = new MgmtEndpoint();
			me.setHostname("mithrilcsp.sbc.com");
			me.setLatitude(32);
			me.setLongitude(-90);
			me.setMajor(2);
			me.setMinor(0);
			me.setPatch(19);
			me.setPort(3312);
			me.setProtocol("http");
			me.getSubprotocol().add("TLS1.1");

			SpecialPorts sp = new SpecialPorts();
			sp.setName("debug");
			sp.setPort(9000);
			sp.setProtocol("java");
			me.getSpecialPorts().add(sp);
			
			MgmtEndpoints mes = new MgmtEndpoints();
			mes.getMgmtEndpoint().add(me);
			System.out.println(medf.newData().load(mes).asString());
			
		} catch (APIException e) {
			e.printStackTrace();
		}
		
	}

}
