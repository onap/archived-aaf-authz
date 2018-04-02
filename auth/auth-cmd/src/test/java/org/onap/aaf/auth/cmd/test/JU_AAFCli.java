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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfo;
import org.onap.aaf.cadi.http.HBasicAuthSS;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.cadi.locator.PropertyLocator;
import org.onap.aaf.misc.env.APIException;
@RunWith(MockitoJUnitRunner.class)
public class JU_AAFCli {

	private static AAFcli cli;
	private static int TIMEOUT = Integer.parseInt(Config.AAF_CONN_TIMEOUT_DEF);

//	@BeforeClass
//	public static void setUp() throws Exception, Exception {
//		cli = getAAfCli();
//	}
//
//	@Test
//	public void eval() throws Exception {
//		assertTrue(cli.eval("#startswith"));
//	}
//
//	@Test
//	public void eval_empty() throws Exception {
//		assertTrue(cli.eval(""));
//	}
//
//	@Test
//	public void eval1() throws Exception {
//		assertTrue(cli.eval("@[123"));
//	}
//
//	@Test
//	public void eval2() throws Exception {
//		assertFalse(cli.eval("as @[ 123"));
//	}
//
//	@Test
//	public void eval3() throws Exception {
//		try {
//			cli.eval("expect @[ 123");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			assertTrue(e instanceof CadiException);
//		}
//	}
//
//	public void eval31() throws Exception {
//		try {
//			cli.eval("expect 1 @[ 123");
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			assertTrue(e instanceof CadiException);
//		}
//	}
//
//	@Test
//	public void eval4() throws Exception {
//		try {
//			cli.eval("sleep @[ 123");
//		} catch (Exception e) {
//			assertTrue(e instanceof NumberFormatException);
//		}
//	}
//
//	@Test
//	public void eval41() throws Exception {
//		assertTrue(cli.eval("sleep 1 @[ 123"));
//	}
//
//	@Test
//	public void eval5() throws Exception {
//		try {
//			cli.eval("delay @[ 123");
//		} catch (Exception e) {
//			assertTrue(e instanceof NumberFormatException);
//		}
//	}
//
//	@Test
//	public void eval51() throws Exception {
//		assertTrue(cli.eval("delay 1 @[ 123"));
//	}
//
//	@Test
//	public void eval7() throws Exception {
//		assertFalse(cli.eval("exit @[ 123"));
//	}
//
//	@Test
//	public void eval8() throws Exception {
//		assertTrue(cli.eval("REQUEST @[ 123"));
//	}
//
//	@Test
//	public void eval9() throws Exception {
//		assertTrue(cli.eval("FORCE @[ 123"));
//	}
//
//	@Test
//	public void eval10() throws Exception {
//		assertTrue(cli.eval("set @[ 123"));
//	}
//
//	@Test
//	public void keyboardHelp() throws Exception {
//		boolean noError=true;
//		try {
//			cli.keyboardHelp();
//		} catch (Exception e) {
//			noError=false;
//		}
//		assertTrue(noError);
//	}
//	
//
//	
//	@Test
//	public void setProp() throws Exception {
//		boolean noError=true;
//		try {
//			cli.keyboardHelp();
//		} catch (Exception e) {
//			noError=false;
//		}
//		assertTrue(noError);
//	}
//	
//	@Test
//	public void eval_randomString() throws Exception {
//		assertTrue(cli.eval("Some random string @#&*& to check complete 100 coverage"));
//	}

//	public static AAFcli getAAfCli() throws APIException, LocatorException, GeneralSecurityException, IOException {
//		final AuthzEnv env = new AuthzEnv(System.getProperties());
//		String aafUrl = "https://DME2RESOLVE";
//		SecurityInfo si = new SecurityInfo(env);
//		env.loadToSystemPropsStartsWith("AAF", "DME2");
//		Locator loc;
//		loc = new PropertyLocator(aafUrl);
//		TIMEOUT = Integer.parseInt(env.getProperty(Config.AAF_CONN_TIMEOUT, Config.AAF_CONN_TIMEOUT_DEF));
//		HMangr hman = new HMangr(env, loc).readTimeout(TIMEOUT).apiVersion("2.0");
//
//		// TODO: Consider requiring a default in properties
//		env.setProperty(Config.AAF_DEFAULT_REALM,
//				System.getProperty(Config.AAF_DEFAULT_REALM, Config.getDefaultRealm()));
//		HBasicAuthSS ss = mock(HBasicAuthSS.class);
//		//TODO: Gabe[JUnit] constructor issue
//		return new AAFcli(env, new OutputStreamWriter(System.out), hman, si, ss);
//	}
	
	@Test						//TODO: Temporary fix AAF-111
	public void netYetTested() {
		assertTrue(true);
	}
}
