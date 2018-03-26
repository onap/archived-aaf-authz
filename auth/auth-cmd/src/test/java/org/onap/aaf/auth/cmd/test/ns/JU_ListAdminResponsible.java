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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.ns.List;
import org.onap.aaf.auth.cmd.ns.ListAdminResponsible;
import org.onap.aaf.auth.cmd.ns.NS;
import org.onap.aaf.auth.cmd.test.JU_AAFCli;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.misc.env.APIException;

@RunWith(MockitoJUnitRunner.class)
public class JU_ListAdminResponsible {
	
//	private static ListAdminResponsible lsAdminRes;
//	
//	@BeforeClass
//	public static void setUp () throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
//		AAFcli cli = JU_AAFCli.getAAfCli();
//		NS ns = new NS(cli);
//		List ls = new List(ns);
//		lsAdminRes = new ListAdminResponsible(ls);
//	}
//	
//	@Test
//	public void exec() {
//		try {
//			//TODO: Gabe [JUnit] Not visible for junit
//			assertEquals(lsAdminRes._exec(0, "add","del","reset","extend"),500);
//		} catch (CadiException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (APIException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (LocatorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	@Test						//TODO: Temporary fix AAF-111
	public void netYetTested() {
		fail("Tests not yet implemented");
	}
}

