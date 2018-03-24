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

package org.onap.aaf.cadi.oauth.test;

import static org.junit.Assert.*;

import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.oauth.AAFToken;

import junit.framework.Assert;

public class JU_OAuthToken {

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
	public void testMax() throws CadiException {
		UUID uuid = new UUID(Long.MAX_VALUE,Long.MAX_VALUE);
		String token = AAFToken.toToken(uuid);
		System.out.println(token);
		UUID uuid2 = AAFToken.fromToken(token);
		Assert.assertEquals(uuid, uuid2);
	}
	
	@Test
	public void testMin() throws CadiException {
		UUID uuid = new UUID(Long.MIN_VALUE,Long.MIN_VALUE);
		String token = AAFToken.toToken(uuid);
		System.out.println(token);
		UUID uuid2 = AAFToken.fromToken(token);
		Assert.assertEquals(uuid, uuid2);
	}

	@Test
	public void testRandom() throws CadiException {
		for(int i=0;i<100;++i) {
			UUID uuid = UUID.randomUUID();
			String token = AAFToken.toToken(uuid);
			System.out.println(token);
			UUID uuid2 = AAFToken.fromToken(token);
			Assert.assertEquals(uuid, uuid2);
		}
	}


}
