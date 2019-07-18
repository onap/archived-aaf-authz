/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * 
 *  Modifications Copyright (C) 2018 IBM.
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

package org.onap.aaf.misc.rosetta;



import org.junit.Test;
import org.onap.aaf.misc.rosetta.ParseException;

public class JU_ParseException {

	@Test
	public void test4() {
		ParseException pe=new ParseException();
		}
	
	@Test
	public void test() {
		ParseException pe=new ParseException("exception");
		}
	
	@Test
	public void test1() {
		ParseException pe=new ParseException(new NullPointerException("demo"));
		}
	
	@Test
	public void test2() {
		ParseException pe=new ParseException("exception1",new NullPointerException("demo"));
		}
	
	

}

