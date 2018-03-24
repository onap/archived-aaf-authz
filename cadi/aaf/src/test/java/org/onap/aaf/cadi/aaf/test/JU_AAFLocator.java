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

package org.onap.aaf.cadi.aaf.test;

import java.net.HttpURLConnection;
import java.net.URI;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.aaf.v2_0.AAFLocator;
import org.onap.aaf.cadi.aaf.v2_0.AbsAAFLocator;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.misc.env.impl.BasicTrans;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

public class JU_AAFLocator {

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
		try {
			// TODO: Ian [JUnit] This fails because these files don't exist
			PropAccess propAccess = new PropAccess("cadi_prop_files=/opt/app/aaf/common/com.att.aaf.common.props:/opt/app/aaf/common/com.att.aaf.props");
			SecurityInfoC<HttpURLConnection> si = SecurityInfoC.instance(propAccess, HttpURLConnection.class);
			String alu = propAccess.getProperty(Config.AAF_LOCATE_URL,"https://mithrilcsp.sbc.com:8095/locate");
			URI locatorURI = new URI(alu+"/com.att.aaf.service/2.0");
			AbsAAFLocator<BasicTrans> al = new AAFLocator(si, locatorURI);
			Assert.assertTrue(al.refresh());
			Item i = al.first();
			i = al.next(i);
			i = al.best();
			System.out.println("hi");
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

}
