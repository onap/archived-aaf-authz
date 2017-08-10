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
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package com.osaaf.defOrg;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import com.att.authz.env.AuthzTrans;
import com.att.authz.org.OrganizationException;
import com.att.authz.org.Organization.Identity;
import com.osaaf.defOrg.Identities.Data;

@RunWith(PowerMockRunner.class)
public class JU_DefaultOrgIdentity {

	private DefaultOrgIdentity defaultOrgIdentity;
	private DefaultOrgIdentity defaultOrgIdentityMock;
	
	@Mock
	AuthzTrans authzTransMock;
	
	String key="key";
	
	@Mock
	private DefaultOrg defaultOrgMock;
	@Mock
	private Data dataMock;
	@Mock
	private Identity identityMock;
	
	@Before
	public void setUp() throws OrganizationException{
		MockitoAnnotations.initMocks(this);
		defaultOrgIdentityMock = PowerMockito.mock(DefaultOrgIdentity.class);
	}
	
	@Test
	public void testEquals(){
		Object b = null;
		Boolean res = defaultOrgIdentityMock.equals(b);
		System.out.println("value of res " +res);
	}
	
}
