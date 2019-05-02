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

import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.dao.cass.LocateDAO.Data;
import org.onap.aaf.auth.direct.DirectRegistrar;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.config.Config;


@RunWith(MockitoJUnitRunner.class) 
public class JU_DirectRegistrar {

	@Mock
	LocateDAO ldao;
	
//	@Mock
//	Data locate;
	
	@Mock
	Access access;
	
	@Mock
	AuthzEnv env;
	
//	@Mock
	AuthzTrans trans;
	
	@Before
	public void setUp() throws Exception {
		initMocks(this);
	}
	
	@Test
	public void testUpdate() {
		DirectRegistrar registrarObj=null;
		try {
			Mockito.doReturn("20").when(access).getProperty(Config.CADI_LATITUDE,null);
			Mockito.doReturn("20").when(access).getProperty(Config.CADI_LONGITUDE,null);
			Mockito.doReturn("20").when(access).getProperty(Config.AAF_LOCATOR_CONTAINER,"");
			registrarObj = new DirectRegistrar( access, ldao, 9080);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		org.onap.aaf.auth.layer.Result<Void> retVal1 = new Result<Void>(null,0,"test",new String[0]);
		Mockito.doReturn(trans).when(env).newTransNoAvg();
//		Mockito.doReturn(retVal1).when(ldao).update(trans,locate);
		registrarObj.update(env);
//		System.out.println(retVal1);
	}
	
	
	
}