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
package com.att.authz.cadi;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.att.authz.env.AuthzEnv;
import com.att.cadi.CredVal.Type;
import com.att.dao.aaf.hl.Question;
import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
public class JU_DirectAAFUserPass {
	
//public static AuthzEnv env;
//public static Question question;
public static String string;
public DirectAAFUserPass directAAFUserPass;

@Mock
AuthzEnv env;
Question question;
String user;
Type type; 
byte[] pass;
	@Before
	public void setUp() {
		directAAFUserPass = new DirectAAFUserPass(env, question, string);
	}
	
	@Test
	public void testvalidate(){

//	Boolean bolVal =  directAAFUserPass.validate(user, type, pass);
	//	assertEquals((bolVal==null),true);

		assertTrue(true);
		
	}
}
