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

package org.onap.aaf.cadi.oauth.test;

import java.io.StringReader;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.oauth.TokenPerm.LoadPermissions;
import org.onap.aaf.misc.rosetta.ParseException;

public class JU_FastPerms {

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
		String json = "{\"perm\":[" +
				"  {\"type\":\"com.access\",\"instance\":\"*\",\"action\":\"read,approve\"}," +
				"  {\"type\":\"org.osaaf.aaf.access\",\"instance\":\"*\",\"action\":\"*\"}," +
				"  {\"type\":\"org.osaaf.aaf.access\",\"instance\":\"*\",\"action\":\"read\"}," +
				"  {\"type\":\"org.osaaf.aaf.attrib\",\"instance\":\":com.att.*:swm\",\"action\":\"*\"}," +
				"  {\"type\":\"org.osaaf.aaf.bogus\",\"instance\":\"sample\",\"action\":\"read\"}," +
				"  {\"type\":\"org.osaaf.aaf.ca\",\"instance\":\"aaf\",\"action\":\"ip\"}," +
				"  {\"type\":\"org.osaaf.aaf.ca\",\"instance\":\"local\",\"action\":\"domain\"}," +
				"  {\"type\":\"org.osaaf.aaf.cache\",\"instance\":\"*\",\"action\":\"clear\"}," +
				"  {\"type\":\"org.osaaf.aaf.cass\",\"instance\":\":mithril\",\"action\":\"*\"}," +
				"  {\"type\":\"org.osaaf.aaf.certman\",\"instance\":\"local\",\"action\":\"read,request,showpass\"}," +
				"  {\"type\":\"org.osaaf.aaf.db\",\"instance\":\"pool\",\"action\":\"clear\"}," +
				"  {\"type\":\"org.osaaf.aaf.deny\",\"instance\":\"com.att\",\"action\":\"*\"}," +
				"  {\"type\":\"org.osaaf.aaf.jenkins\",\"instance\":\"mithrilcsp.sbc.com\",\"action\":\"admin\"}," +
				"  {\"type\":\"org.osaaf.aaf.log\",\"instance\":\"com.att\",\"action\":\"id\"}," +
				"  {\"type\":\"org.osaaf.aaf.myPerm\",\"instance\":\"myInstance\",\"action\":\"myAction\"}," +
				"  {\"type\":\"org.osaaf.aaf.ns\",\"instance\":\":com.att.*:ns\",\"action\":\"write\"}," +
				"  {\"type\":\"org.osaaf.aaf.ns\",\"instance\":\":com.att:ns\",\"action\":\"write\"}," +
				"  {\"type\":\"org.osaaf.aaf.password\",\"instance\":\"com.att\",\"action\":\"extend\"}," +
				"  {\"type\":\"org.osaaf.access\",\"instance\":\"*\",\"action\":\"read\"}," +
				"  {\"type\":\"org.osaaf.authz.access\",\"instance\":\"*\",\"action\":\"read\"}," +
				"  {\"type\":\"org.osaaf.authz.dev.access\",\"instance\":\"*\",\"action\":\"*\"}," +
				"  {\"type\":\"org.osaaf.authz.swm.star\",\"instance\":\"*\",\"action\":\"*\"}," +
				"  {\"type\":\"org.osaaf.cadi.access\",\"instance\":\"*\",\"action\":\"*\"}," +
				"  {\"type\":\"org.osaaf.chris.access\",\"instance\":\"*\",\"action\":\"*\"}," +
				"  {\"type\":\"org.osaaf.csid.lab.swm.node\",\"instance\":\"*\",\"action\":\"*\"}," +
				"  {\"type\":\"org.osaaf.myapp.access\",\"instance\":\"*\",\"action\":\"*\"}," +
				"  {\"type\":\"org.osaaf.myapp.access\",\"instance\":\"*\",\"action\":\"read\"}," +
				"  {\"type\":\"org.osaaf.sample.access\",\"instance\":\"*\",\"action\":\"read\"}," +
				"  {\"type\":\"org.osaaf.sample.swm.myPerm\",\"instance\":\"*\",\"action\":\"read\"}," +
				"  {\"type\":\"org.osaaf.temp.access\",\"instance\":\"*\",\"action\":\"read\"}," +
				"  {\"type\":\"org.osaaf.test.access\",\"instance\":\"*\",\"action\":\"*\"}," +
				"  {\"type\":\"org.osaaf.test.access\",\"instance\":\"*\",\"action\":\"read\"}," +
				"  {\"type\":\"com.test.access\",\"instance\":\"*\",\"action\":\"read\"}," +
				"  {\"type\":\"com.test.access\",\"instance\":\"*\",\"action\":\"read\"}" +
				"]}";
		
		try {
			LoadPermissions lp = new LoadPermissions(new StringReader(json));
			for(Permission p : lp.perms) {
				System.out.println(p.toString());
			}
			System.out.println("done");
		} catch (ParseException e) {
			System.err.println(e);
		}
		
		
	}
	

}
