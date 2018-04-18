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
package org.onap.aaf.org.test;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.org.DefaultOrg;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class JU_DefaultOrg {

	DefaultOrg defaultOrg;
	//private DefaultOrg defaultOrgMock;
	@Mock
	AuthzEnv authzEnvMock;

	@Mock
	AuthzTrans authzTransMock;

	@Mock
	File fIdentitiesMock;

	private static final String PROPERTY_IS_REQUIRED = " property is Required";
	private static final String DOMAIN = "osaaf.com";
	private static final String REALM = "com.osaaf";
	private static final String NAME = "Default Organization";
	private static final String NO_PASS = NAME + " does not support Passwords.  Use AAF";
	String mailHost,mailFromUserId,supportAddress;
	private String SUFFIX;
	String s;
	String defFile;

	//@Before
	public void setUp() throws OrganizationException{
		MockitoAnnotations.initMocks(this);
		PowerMockito.when(authzEnvMock.getProperty(s=(REALM + ".mailHost"), null)).thenReturn("hello");
		PowerMockito.when(authzEnvMock.getProperty(s=(REALM + ".supportEmail"), null)).thenReturn("notnull");
		PowerMockito.when(authzEnvMock.getProperty(Matchers.anyString())).thenReturn("src" + File.separator + "test" + File.separator + "resources" + File.separator + "test.txt");
		PowerMockito.when(fIdentitiesMock.exists()).thenReturn(true);
		//PowerMockito.when((fIdentitiesMock!=null && fIdentitiesMock.exists())).thenReturn(true);
		defaultOrg = new DefaultOrg(authzEnvMock, REALM);
	}

	//@Test    //(expected=OrganizationException.class)
	public void test() throws OrganizationException{
		//PowerMockito.when(authzEnvMock.getProperty(Matchers.anyString())).thenReturn(" ");
		//defaultOrg = new DefaultOrg(authzEnvMock);
		assertTrue(defaultOrg != null);
	}


	//@Test    //(expected=OrganizationException.class)
	public void testMultipleCreds() throws OrganizationException{
		String id = "test";
		//PowerMockito.when(authzEnvMock.getProperty(Matchers.anyString())).thenReturn(" ");
		//defaultOrg = new DefaultOrg(authzEnvMock);
		boolean canHaveMultipleCreds;
		canHaveMultipleCreds = defaultOrg.canHaveMultipleCreds(id );
		System.out.println("value of canHaveMultipleCreds:  " + canHaveMultipleCreds);
		assertTrue(canHaveMultipleCreds);
	}


	//@Test   
	public void testGetIdentityTypes() throws OrganizationException{
		Set<String> identityTypes = defaultOrg.getIdentityTypes();
		System.out.println("value of IdentityTypes:  " + identityTypes);
		assertTrue(identityTypes.size() == 4);
	}


	//@Test   
	public void testGetRealm() throws OrganizationException{
		String realmTest = defaultOrg.getRealm();
		System.out.println("value of realm:  " + realmTest);
		assertTrue(realmTest == REALM);
	}

	public void supportsRealm() {
		String otherRealm = "org.ossaf.something";
		defaultOrg.addSupportedRealm(otherRealm);
		assertTrue(defaultOrg.supportsRealm(otherRealm));
	}
	//@Test   
	public void testGetName() throws OrganizationException{
		String testName = defaultOrg.getName();
		System.out.println("value of name:  " + testName);
		assertTrue(testName == NAME);
	}


	//@Test   
	public void testGetDomain() throws OrganizationException{
		String testDomain = defaultOrg.getDomain();
		System.out.println("value of domain:  " + testDomain);
		assertTrue(testDomain == DOMAIN);
	}

	// @Test
	// public void testIsValidID(){	
	// 	String Result = defaultOrg.isValidID(Matchers.anyString());
	// 	System.out.println("value of res " +Result);
	// 	assertNotNull(Result);	
	// }

	//@Test
	public void notYetImplemented() {
		fail("Tests in this file should not be trusted");
	}

}
