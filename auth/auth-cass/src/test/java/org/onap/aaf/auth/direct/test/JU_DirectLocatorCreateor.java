/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.auth.direct.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.direct.DirectLocatorCreator;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;


@RunWith(MockitoJUnitRunner.class) 
public class JU_DirectLocatorCreateor {

	@Mock
	LocateDAO ldao;
	
	@Mock
	AuthzEnv env;
	
	@Before
	public void setUp() throws Exception {
		initMocks(this);
	}
	
	@Test
	public void testCreate() {
		PropAccess access = Mockito.mock(PropAccess.class);
		Mockito.doReturn(access).when(env).access();
		Mockito.doReturn("20").when(access).getProperty(Config.CADI_LATITUDE,null);
		Mockito.doReturn("20").when(access).getProperty(Config.CADI_LONGITUDE,null);
		DirectLocatorCreator aafLocatorObj=new DirectLocatorCreator(env, ldao);
		try {
			aafLocatorObj.setSelf("test", 9080);
			aafLocatorObj.create("test","30.20.30.30");
		} catch (LocatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCreateHostnameNull() {
		PropAccess access = Mockito.mock(PropAccess.class);
		Mockito.doReturn(access).when(env).access();
		Mockito.doReturn("20").when(access).getProperty(Config.CADI_LATITUDE,null);
		Mockito.doReturn("20").when(access).getProperty(Config.CADI_LONGITUDE,null);
		DirectLocatorCreator aafLocatorObj=new DirectLocatorCreator(env, ldao);
		try {
			aafLocatorObj.create("test","30.20.30.30");
		} catch (LocatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}