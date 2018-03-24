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
package org.onap.aaf.cadi.cass;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Access.Level;
import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.exceptions.AuthenticationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.att.aaf.cadi.cass.AAFAuthenticatedUser;
import com.att.aaf.cadi.cass.AAFAuthenticator;
import com.att.aaf.cadi.cass.AAFBase;

import junit.framework.Assert;

public class JU_AAFAuthenticator extends AAFBase
{
	

	@Before
	public void setUp()
	{
		
	}

	@After
	public void tearDown()
	{
		
	}
	
	@Test
	public void checkRequiredAuth() {
		AAFAuthenticator test = new AAFAuthenticator();
		Assert.assertTrue(test.requireAuthentication());
	}
	//TODO: Call may be broken due to missing ATT specific code
	@Test
	public void checkAuthenticate()  throws AuthenticationException {
		AuthenticatedUser user = new AuthenticatedUser("testUser");
		AAFAuthenticator test = new AAFAuthenticator();	
		Map<String, String> cred = new HashMap<String,String>();
		cred.put("username", "testUser");
		cred.put("password", "testPass");
		String username = (String)cred.get("username");
		Access access = new PropAccess();
		AAFAuthenticatedUser aau = new AAFAuthenticatedUser(access,username);
		String fullName=aau.getFullName();
		access.log(Level.DEBUG, "Authenticating", aau.getName(),"(", fullName,")");
		//test.authenticate(cred);
		//Assert.assert
		
	}
	
	@Test(expected = AuthenticationException.class)
	public void checkThrowsUser() throws AuthenticationException {
		AAFAuthenticator test = new AAFAuthenticator();
		Map<String, String> cred = new HashMap<String,String>();
		cred.put("username", null);
		Assert.assertNull(cred.get("username"));
		test.authenticate(cred);
	}
	
	@Test(expected = AuthenticationException.class)
	public void checkThrowsPass() throws AuthenticationException {
		AAFAuthenticator test = new AAFAuthenticator();
		Map<String, String> cred = new HashMap<String,String>();
		cred.put("username", "testUser");
		cred.put("password", "bsf:");
		Assert.assertNotNull(cred.get("password"));
		test.authenticate(cred);
		
		cred.put("password", null);
		Assert.assertNull(cred.get("password"));
		test.authenticate(cred);
	}



}
