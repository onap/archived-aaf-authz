/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aai
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * Copyright © 2017 Amdocs
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
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package com.osaaf.defOrg;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import com.att.authz.org.Executor;
import com.att.authz.org.OrganizationException;
import com.att.authz.org.Organization.Identity;
import com.att.authz.org.Organization.Policy;
import com.osaaf.defOrg.Identities.Data;

@RunWith(PowerMockRunner.class)
public class JU_DefaultOrg {

DefaultOrg defaultOrg;
//private DefaultOrg defaultOrgMock;
@Mock
AuthzEnv authzEnvMock;

private static final String PROPERTY_IS_REQUIRED = " property is Required";
private static final String DOMAIN = "osaaf.com";
private static final String REALM = "com.osaaf";
private static final String NAME = "Default Organization";
private static final String NO_PASS = NAME + " does not support Passwords.  Use AAF";
String mailHost,mailFromUserId,supportAddress;
private String SUFFIX;
String s;
String defFile;
@Mock
File fIdentitiesMock;

@Before
public void setUp() throws OrganizationException{
	MockitoAnnotations.initMocks(this);
	PowerMockito.when(authzEnvMock.getProperty(s=(REALM + ".mailHost"), null)).thenReturn("hello");
	PowerMockito.when(authzEnvMock.getProperty(s=(REALM + ".supportEmail"), null)).thenReturn("notnull");
	PowerMockito.when(authzEnvMock.getProperty(Matchers.anyString())).thenReturn("C:/Users/sv8675/Desktop/AAF-Code-Sai/AAF-master/authz/authz-defOrg/src/main/java/test.txt");
	PowerMockito.when(fIdentitiesMock.exists()).thenReturn(true);
	//PowerMockito.when((fIdentitiesMock!=null && fIdentitiesMock.exists())).thenReturn(true);
	defaultOrg = new DefaultOrg(authzEnvMock);
}

@Test    //(expected=OrganizationException.class)
public void test() throws OrganizationException{
	//PowerMockito.when(authzEnvMock.getProperty(Matchers.anyString())).thenReturn(" ");
	//defaultOrg = new DefaultOrg(authzEnvMock);
	assertTrue(true);
}

@Test
public void testIsValidID(){	
	String Result = defaultOrg.isValidID(Matchers.anyString());
	System.out.println("value of res " +Result);
	assertNotNull(Result);	
}

@Mock
AuthzTrans authzTransMock;


}
