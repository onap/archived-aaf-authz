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
package com.att.authz.org;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import com.att.authz.env.AuthzEnv;
import com.att.inno.env.APIException;

@RunWith(PowerMockRunner.class)
public class JU_OrganizationFactory {
	private static final String ORG_SLOT = null;
	OrganizationFactory organizationFactory;
	@Mock
	AuthzEnv authzEnvMock;
	String orgClass="orgclass";
	String orgNS="orgns";
	@Before
	public void setUp(){
		organizationFactory = new OrganizationFactory();	
	}

	@SuppressWarnings("static-access")
	@Test(expected = APIException.class)
	public void testSetDefaultOrg() throws APIException {
		//PowerMockito.when(authzEnvMock.slot(ORG_SLOT)).thenReturn("ORG_SLOT");
		organizationFactory.setDefaultOrg(authzEnvMock, orgClass);
	}
	
	@SuppressWarnings("static-access")
	@Test(expected = OrganizationException.class)
	public void testObtain() throws OrganizationException{
		PowerMockito.when(authzEnvMock.getProperty("Organization."+orgNS)).thenReturn("notnull");
		organizationFactory.obtain(authzEnvMock, orgNS);
	}
}
