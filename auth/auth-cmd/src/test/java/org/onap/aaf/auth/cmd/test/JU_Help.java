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
package org.onap.aaf.auth.cmd.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.Help;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.misc.env.APIException;

//import com.att.aft.dme2.internal.jetty.http.HttpStatus;
//TODO: Gabe [JUnit] Import missing
@RunWith(MockitoJUnitRunner.class)
public class JU_Help {
	
//	private static AAFcli cli;
//	private static Help help;
//	
//	@Mock
//	private static List<Cmd> cmds;
//	
//	@BeforeClass
//	public static void setUp() throws APIException, LocatorException, GeneralSecurityException, IOException {
//		cli = JU_AAFCli.getAAfCli();
//		cmds = new ArrayList<>();
//		help = new Help(cli, cmds);
//	}
//	
//	@Test
//	public void exec_HTTP_200() {
//		try {
//			assertEquals(help._exec(1, "helps"), HttpStatus.OK_200);
//		} catch (CadiException | APIException | LocatorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	@Test
//	public void exec_HTTP_200_1() {
//		try {
//			assertEquals(help._exec(1, "helps","help"), HttpStatus.OK_200);
//		} catch (CadiException | APIException | LocatorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	@Test
//	public void detailhelp() {
//		boolean hasError=false;
//		try {
//			help.detailedHelp(2, new StringBuilder("detail help test"));
//		} catch (Exception e) {
//			hasError=true;
//		}
//		assertEquals(hasError,false);
//	}
	
	@Test						//TODO: Temporary fix AAF-111
	public void netYetTested() {
		fail("Tests not yet implemented");
	}
}
