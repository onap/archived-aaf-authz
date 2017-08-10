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
package com.att.authz.env;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import com.att.cadi.CadiException;
import com.att.cadi.Connector;
import com.att.cadi.TrustChecker;

@RunWith(PowerMockRunner.class)  
public class JU_AuthzTransFilter {
AuthzTransFilter authzTransFilter;
@Mock
AuthzEnv authzEnvMock;
@Mock
Connector connectorMock;
@Mock
TrustChecker trustCheckerMock;
@Mock
AuthzTrans authzTransMock;
Object additionalTafLurs;
	
	@Before
	public void setUp(){
		try {
			authzTransFilter = new AuthzTransFilter(authzEnvMock, connectorMock, trustCheckerMock, additionalTafLurs);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void test()
	{
		//authzTransFilter.newTrans();
		assertTrue(true);
	}
	
	@Test
	public void testTallyHo(){
		PowerMockito.when(authzTransMock.info().isLoggable()).thenReturn(true);
		//if(trans.info().isLoggable())
		authzTransFilter.tallyHo(authzTransMock);
		
	}
	
	
//	AuthzTrans at = env.newTrans();
//	at.setLur(getLur());
//	return at
}
