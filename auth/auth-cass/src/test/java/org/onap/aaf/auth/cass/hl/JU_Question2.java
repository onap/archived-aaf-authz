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

package org.onap.aaf.auth.cass.hl;

import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.aaf.auth.dao.hl.Question;

public class JU_Question2 {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws IOException {
		String s,u;
		System.out.println((s="com") + '=' + (u=Question.toUnique(s)));
		System.out.println(u+'='+(Question.fromUnique(u)));
		System.out.println((s="org.osaaf.cdp.Tenant32_what-a-joy") + '=' + (u=Question.toUnique(s)));
		System.out.println(u+'='+(Question.fromUnique(u)));
		System.out.println((s="org.osaaf.cdp") + '=' + (u=Question.toUnique(s)));
		System.out.println(u+'='+(Question.fromUnique(u)));

//		Assert.assertSame(s="com", Question.toUnique(s));
//		Assert.assertSame(s="", Question.toUnique(s));
//		Assert.assertSame(s="com.aa", Question.toUnique(s));
//		Assert.assertNotSame(s="com.Aa", Question.toUnique(s));
//		Assert.assertEquals("com.aa", Question.toUnique(s));
//		Assert.assertNotSame(s="com.Aa.1", Question.toUnique(s));
//		Assert.assertEquals("com.aa.1", Question.toUnique(s));
//		Assert.assertNotSame(s="com.Aa.1-3", Question.toUnique(s));
//		Assert.assertEquals("com.aa.13", Question.toUnique(s));
//		Assert.assertEquals("com.aa.13", Question.toUnique("com.aA.1_3"));
	}

}
