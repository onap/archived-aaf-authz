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

package org.onap.aaf.cadi.client.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.aaf.cadi.routing.GreatCircle;

public class JU_GreatCircle {

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
	public void test() {
		Assert.assertEquals(7.803062505568182,GreatCircle.calc(38.627345,-90.193774, 35.252234,-81.384929),0.000000001);
		Assert.assertEquals(0.0,GreatCircle.calc(38.627345,-90.193774, 38.627345,-90.193774),0.000000001);
		Assert.assertEquals(7.803062505568182,GreatCircle.calc(35.252234,-81.384929,38.627345,-90.193774),0.000000001);
		Assert.assertEquals(7.803062505568182,GreatCircle.calc(38.627345,-90.193774, 35.252234,-81.384929),0.000000001);
		Assert.assertEquals(7.803062505568182,GreatCircle.calc(-38.627345,90.193774, -35.252234,81.384929),0.000000001);
		Assert.assertEquals(105.71060033936052,GreatCircle.calc(-38.627345,90.193774, -35.252234,-81.384929),0.000000001);
		Assert.assertEquals(105.71060033936052,GreatCircle.calc(38.627345,-90.193774, 35.252234,81.384929),0.000000001);
		Assert.assertEquals(74.32786874922931,GreatCircle.calc(-38.627345,90.193774, 35.252234,81.384929),0.000000001);
	}

}
