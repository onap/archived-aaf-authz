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
package com.att.authz.fs;

import static com.att.cssa.rserv.HttpMethods.GET;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import com.att.cssa.rserv.CachingFileAccess;
import com.att.inno.env.APIException;
import com.att.authz.fs.*;

@RunWith(MockitoJUnitRunner.class)
public class JU_FileServer {	
	@Mock
	AuthzEnv authzEnvMock;
	AuthzEnv authzEnv = new AuthzEnv();
	
	@Before
	public void setUp() throws APIException, IOException{

	}
	
	@SuppressWarnings("static-access")
	@Test
	public void testMain() throws Exception{
		
		String[] args = null;
		Properties props = new Properties();
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("FileServer.props").getFile());

//PowerMockito.whenNew(Something.class).withArguments(argument).thenReturn(mockSomething);
		//			env.setLog4JNames("log4j.properties","authz","fs","audit","init",null);
    // PowerMockito.whenNew(AuthzEnv.class).withArguments(props).thenReturn(authzEnvMock);
   //  PowerMockito.doNothing().when(authzEnvMock.setLog4JNames(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString()));
  // PowerMockito.when(new AuthzEnv(props)).thenReturn(authzEnvMock);
		//PowerMockito.doNothing().when(authzEnv).setLog4JNames(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString());
	//PowerMockito.doNothing().when(authzEnvMock).setLog4JNames(" "," "," "," "," "," ");

		FileServer.main(args);
		//assertTrue(true);
		
	}
	
}
