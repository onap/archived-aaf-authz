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
package com.att.authz.env;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import com.att.cadi.Access.Level;

@RunWith(PowerMockRunner.class)
public class JU_AuthzEnv {
	private static final com.att.cadi.Access.Level DEBUG = null;
	AuthzEnv authzEnv;
	enum Level {DEBUG, INFO, AUDIT, INIT, WARN, ERROR};
	
	@Before
	public void setUp(){
		authzEnv = new AuthzEnv();
	}

	@Test
	public void testTransRate() {
	Long Result =	authzEnv.transRate();
	System.out.println("value of result " +Result); //Expected 300000
	assertNotNull(Result);		
	}
	
	@Test(expected = IOException.class)
	public void testDecryptException() throws IOException{
		String encrypted = null;
		authzEnv.decrypt(encrypted, true);
	}
	
	@Test
	public void testDecrypt() throws IOException{
		String encrypted = "encrypted";
		String Result = authzEnv.decrypt(encrypted, true);
		System.out.println("value of res " +Result);
		assertEquals("encrypted",Result);
	}

}
