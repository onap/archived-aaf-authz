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

package org.onap.aaf.cadi.lur.aaf.test1;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;
import org.onap.aaf.cadi.aaf.PermEval;

public class JU_PermEval {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		// TRUE
		assertTrue(PermEval.evalAction("fred","fred"));
		assertTrue(PermEval.evalAction("fred,wilma","fred"));
		assertTrue(PermEval.evalAction("barney,betty,fred,wilma","fred"));
		assertTrue(PermEval.evalAction("*","fred"));
		
		assertTrue(PermEval.evalInstance("fred","fred"));
		assertTrue(PermEval.evalInstance("fred,wilma","fred"));
		assertTrue(PermEval.evalInstance("barney,betty,fred,wilma","fred"));
		assertTrue(PermEval.evalInstance("*","fred"));
		
		assertTrue(PermEval.evalInstance(":fred:fred",":fred:fred"));
		assertTrue(PermEval.evalInstance(":fred:fred,wilma",":fred:fred"));
		assertTrue(PermEval.evalInstance(":fred:barney,betty,fred,wilma",":fred:fred"));
		assertTrue(PermEval.evalInstance("*","fred"));
		assertTrue(PermEval.evalInstance(":*:fred",":fred:fred"));
		assertTrue(PermEval.evalInstance(":fred:*",":fred:fred"));
		assertTrue(PermEval.evalInstance(":!f.*:fred",":fred:fred"));
		assertTrue(PermEval.evalInstance(":fred:!f.*",":fred:fred"));
		
		/// FALSE
		assertFalse(PermEval.evalInstance("fred","wilma"));
		assertFalse(PermEval.evalInstance("fred,barney,betty","wilma"));
		assertFalse(PermEval.evalInstance(":fred:fred",":fred:wilma"));
		assertFalse(PermEval.evalInstance(":fred:fred",":wilma:fred"));
		assertFalse(PermEval.evalInstance(":wilma:!f.*",":fred:fred"));
		assertFalse(PermEval.evalInstance(":!f.*:wilma",":fred:fred"));
		assertFalse(PermEval.evalInstance(":!w.*:!f.*",":fred:fred"));
		assertFalse(PermEval.evalInstance(":!f.*:!w.*",":fred:fred"));

		assertFalse(PermEval.evalInstance(":fred:!x.*",":fred:fred"));

		// MSO Tests 12/3/2015
		assertFalse(PermEval.evalInstance("/v1/services/features/*","/v1/services/features"));
		assertFalse(PermEval.evalInstance(":v1:services:features:*",":v1:services:features"));
		assertTrue(PermEval.evalInstance("/v1/services/features/*","/v1/services/features/api1"));
		assertTrue(PermEval.evalInstance(":v1:services:features:*",":v1:services:features:api2"));
		// MSO - Xue Gao
		assertTrue(PermEval.evalInstance(":v1:requests:*",":v1:requests:test0-service"));   


		
		// Same tests, with Slashes
		assertTrue(PermEval.evalInstance("/fred/fred","/fred/fred"));
		assertTrue(PermEval.evalInstance("/fred/fred,wilma","/fred/fred"));
		assertTrue(PermEval.evalInstance("/fred/barney,betty,fred,wilma","/fred/fred"));
		assertTrue(PermEval.evalInstance("*","fred"));
		assertTrue(PermEval.evalInstance("/*/fred","/fred/fred"));
		assertTrue(PermEval.evalInstance("/fred/*","/fred/fred"));
		assertTrue(PermEval.evalInstance("/!f.*/fred","/fred/fred"));
		assertTrue(PermEval.evalInstance("/fred/!f.*","/fred/fred"));
		
		/// FALSE
		assertFalse(PermEval.evalInstance("fred","wilma"));
		assertFalse(PermEval.evalInstance("fred,barney,betty","wilma"));
		assertFalse(PermEval.evalInstance("/fred/fred","/fred/wilma"));
		assertFalse(PermEval.evalInstance("/fred/fred","/wilma/fred"));
		assertFalse(PermEval.evalInstance("/wilma/!f.*","/fred/fred"));
		assertFalse(PermEval.evalInstance("/!f.*/wilma","/fred/fred"));
		assertFalse(PermEval.evalInstance("/!w.*/!f.*","/fred/fred"));
		assertFalse(PermEval.evalInstance("/!f.*/!w.*","/fred/fred"));

		assertFalse(PermEval.evalInstance("/fred/!x.*","/fred/fred"));
		
		assertTrue(PermEval.evalInstance(":!com.att.*:role:write",":com.att.temp:role:write"));

		// CPFSF-431 Group needed help with Wild Card
		// They tried
		assertTrue(PermEval.evalInstance(
				":topic.com.att.ecomp_test.crm.pre*",
				":topic.com.att.ecomp_test.crm.predemo100"
				));

		// Also can be
		assertTrue(PermEval.evalInstance(
				":!topic.com.att.ecomp_test.crm.pre.*",
				":topic.com.att.ecomp_test.crm.predemo100"
				));

		


		
	}

}
