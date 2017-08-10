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
package com.att.cssa.rserv;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import com.att.inno.env.Trans;

@RunWith(PowerMockRunner.class)
public class JU_CodeSetter {
	CodeSetter codeSetter;
	@Mock
	Trans transMock;
	@Mock
	HttpServletRequest reqMock;
	@Mock
	HttpServletResponse respMock;
	
	@Before
	public void setUp(){
		codeSetter = new CodeSetter(transMock, reqMock, respMock);
	}
	
	@SuppressWarnings("rawtypes")
	@Mock
	Route routeMock;
	
	@Test
	public void testMatches() throws IOException, ServletException{
		boolean result = codeSetter.matches(routeMock);
		System.out.println("value of res " + codeSetter.matches(routeMock));
		assertFalse(result);
	}

}
