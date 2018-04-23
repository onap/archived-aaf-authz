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

import org.junit.Test;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;

// This class exists purely to instantiate (and therefore attain coverage of) the Future class

public class JU_Future {

	@Test
	public void test() {
		@SuppressWarnings("unused")
		Future<Integer> f = new FutureStub();
	}
	
	private class FutureStub extends Future<Integer> {
		@Override public boolean get(int timeout) throws CadiException { return false; } 
		@Override public int code() { return 0; } 
		@Override public String body() { return null; } 
		@Override public String header(String tag) { return null; }
	}

}
