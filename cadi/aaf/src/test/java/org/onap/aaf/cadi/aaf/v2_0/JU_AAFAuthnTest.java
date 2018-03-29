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
package org.onap.aaf.cadi.aaf.v2_0;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import javax.net.ssl.HttpsURLConnection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.AbsUserCache;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;

public class JU_AAFAuthnTest {
	
	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private AAFCon<HttpsURLConnection> con;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private Future<String> fp;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private AbsUserCache<AAFPermission> cache;

	
	@Before
	public void setup() throws APIException, CadiException {
		MockitoAnnotations.initMocks(this);
		when(con.client(Config.AAF_DEFAULT_VERSION).read("/authn/basicAuth", "text/plain")).thenReturn(fp);
	}
	/*TODO broken JUNIT with MOCKITO
	 * 
	 * reduce size of this test file by looping thru conOfClient instead of copy pasta?
	 * 
	 * These appear to receive data but cannot validate them.  Maybe due to @before
	 * Possible fixes for tomorrow: look into auth,validate, @before mockito, @mock issues?
	 *
	 */
	@Test
	public void testAAFAuthnAAFConOfCLIENT() throws Exception {
		when(fp.get(anyInt())).thenReturn(false);
		when(fp.code()).thenReturn(401);
		when(fp.header("WWW-Authenticate")).thenReturn("Basic realm=\"Value\"");
		AAFAuthn<HttpsURLConnection> auth = con.newAuthn();
		assertNotNull(auth.validate("NewUser", "New Password"));;
	}

	@Test
	public void testAAFAuthnAAFConOfCLIENTAbsUserCacheOfAAFPermission() throws Exception {
		AAFAuthn<HttpsURLConnection> auth = con.newAuthn(cache);
		assertNotNull(auth);
	}
	//TODO broken JUNIT with MOCKITO
	@Test
	public void testAAFAuthnAAFConOfCLIENT1() throws Exception {
		when(fp.get(anyInt())).thenReturn(false);
		when(fp.code()).thenReturn(401);
		when(fp.header("WWW-Authenticate")).thenReturn("Basic realm=\"Value\"");
		AAFAuthn<HttpsURLConnection> auth = con.newAuthn();
		
		assertNotNull(auth.validate("NewUser1", "New Password1"));;
	}

	@Test
	public void testAAFAuthnAAFConOfCLIENTAbsUserCacheOfAAFPermission1() throws Exception {
		AAFAuthn<HttpsURLConnection> auth = con.newAuthn(cache);
		assertNotNull(auth);
	}
	//TODO broken JUNIT with MOCKITO
	@Test
	public void testAAFAuthnAAFConOfCLIENT2() throws Exception {
		when(fp.get(anyInt())).thenReturn(false);
		when(fp.code()).thenReturn(401);
		when(fp.header("WWW-Authenticate")).thenReturn("Basic realm=\"Value\"");
		AAFAuthn<HttpsURLConnection> auth = con.newAuthn();
		
		assertNotNull(auth.validate("NewUser2", "New Password2"));;
	}

	@Test
	public void testAAFAuthnAAFConOfCLIENTAbsUserCacheOfAAFPermission2() throws Exception {
		AAFAuthn<HttpsURLConnection> auth = con.newAuthn(cache);
		assertNotNull(auth);

	}
	//TODO broken JUNIT with MOCKITO
	@Test
	public void testAAFAuthnAAFConOfCLIENT3() throws Exception {
		when(fp.get(anyInt())).thenReturn(false);
		when(fp.code()).thenReturn(401);
		when(fp.header("WWW-Authenticate")).thenReturn("Basic realm=\"Value\"");
		AAFAuthn<HttpsURLConnection> auth = con.newAuthn();
		
		assertNotNull(auth.validate("NewUser3", "New Password3"));;
	}

	@Test
	public void testAAFAuthnAAFConOfCLIENTAbsUserCacheOfAAFPermission3() throws Exception {
		AAFAuthn<HttpsURLConnection> auth = con.newAuthn(cache);
		assertNotNull(auth);

	}
	//TODO broken JUNIT with MOCKITO
	@Test
	public void testAAFAuthnAAFConOfCLIENT4() throws Exception {
		when(fp.get(anyInt())).thenReturn(false);
		when(fp.code()).thenReturn(401);
		when(fp.header("WWW-Authenticate")).thenReturn("Basic realm=\"Value\"");
		AAFAuthn<HttpsURLConnection> auth = con.newAuthn();
		
		assertNotNull(auth.validate("NewUser4", "New Password4"));;
	}

	@Test
	public void testAAFAuthnAAFConOfCLIENTAbsUserCacheOfAAFPermission4() throws Exception {
		AAFAuthn<HttpsURLConnection> auth = con.newAuthn(cache);
		assertNotNull(auth);

	}
	//TODO broken JUNIT with MOCKITO
	@Test
	public void testAAFAuthnAAFConOfCLIENT5() throws Exception {
		when(fp.get(anyInt())).thenReturn(false);
		when(fp.code()).thenReturn(401);
		when(fp.header("WWW-Authenticate")).thenReturn("Basic realm=\"Value\"");
		AAFAuthn<HttpsURLConnection> auth = con.newAuthn();
		
		assertNotNull(auth.validate("NewUser5", "New Password5"));;
	}

	@Test
	public void testAAFAuthnAAFConOfCLIENTAbsUserCacheOfAAFPermission5() throws Exception {
		AAFAuthn<HttpsURLConnection> auth = con.newAuthn(cache);
		assertNotNull(auth);

	}
}
