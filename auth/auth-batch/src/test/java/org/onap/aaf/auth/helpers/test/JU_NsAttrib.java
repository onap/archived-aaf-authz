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

package org.onap.aaf.auth.helpers.test;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.auth.helpers.NsAttrib;
import org.onap.aaf.auth.helpers.creators.RowCreator;

import junit.framework.Assert;

public class JU_NsAttrib {

	NsAttrib nsAttrib;

	@Before
	public void setUp() {
		nsAttrib = new NsAttrib("ns", "key", "value");
	}

	@Test
	public void testToString() {
		Assert.assertEquals("\"ns\",\"key\",\"value\"", nsAttrib.toString());
	}

	@Test
	public void testV2() {
		NsAttrib.v2_0_11.create(RowCreator.getRow());
		Assert.assertEquals("select ns,key,value from authz.ns_attrib", NsAttrib.v2_0_11.select());
	}

}
