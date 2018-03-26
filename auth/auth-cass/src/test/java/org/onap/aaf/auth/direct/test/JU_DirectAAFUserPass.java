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
package org.onap.aaf.auth.direct.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.direct.DirectAAFUserPass;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.CredVal.Type;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class JU_DirectAAFUserPass {

	// TODO: Ian - This test is in shambles. fix it

	//public static AuthzEnv env;
	//public static Question question;
	public DirectAAFUserPass directAAFUserPass;

	@Mock
	AuthzEnv env;

	@Mock
	Question question;

	String user;

	Type type; 

	byte[] pass;

	@Before
	public void setUp() {
		directAAFUserPass = new DirectAAFUserPass(env, question);
	}

	@Test
	public void testvalidate(){

		//	Boolean bolVal =  directAAFUserPass.validate(user, type, pass);
		//	assertEquals((bolVal==null),true);

		assertTrue(true);

	}

	@Test
	public void notYetTested() {
		fail("Tests not yet implemented");
	}

}
