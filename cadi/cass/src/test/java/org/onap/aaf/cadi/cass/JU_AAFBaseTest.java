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
package org.onap.aaf.cadi.cass;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Access.Level;

import junit.framework.Assert;

import com.att.aaf.cadi.cass.AAFBase;

import static org.junit.Assert.*;

import org.apache.cassandra.exceptions.ConfigurationException;

public class JU_AAFBaseTest
{
	
	//TODO: REmove this file, no need for junit for abstract class
	@Before
	public void setUp()
	{
		
	}

	@After
	public void tearDown()
	{
		
	}

	
	@Test
	public void test_method_setAccess_0_branch_0()
	{
		System.out.println("Now Testing Method:setAccess Branch:0");
		
		//Call Method
		AAFBase.setAccess(null);
		
	}
	
	
	

}
