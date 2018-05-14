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

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.helpers.MonthData;

import static org.mockito.Mockito.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

public class JU_MonthData {
	
	File f;
	MonthData mData;
	BufferedWriter bw = null;
	FileWriter fw = null;
	
	@Before
	public void setUp() throws IOException {
		mData = new MonthData("env");
		f = new File("Monthlyenv.dat");
		f.createNewFile();
		bw = new BufferedWriter(new FileWriter(f));
		bw.write("#test"+ "\n");
		bw.write("long,tester"+ "\n");
		bw.write("1,2,3,4,5"+ "\n");
		bw.close();
		
		mData = new MonthData("env");
	}

	@Test
	public void testAdd() {
		mData.add(2, "target", 10, 1, 1);
	}
	
	@Test
	public void testNotExists() {
		mData.notExists(2);
	}
	
	@After
	public void cleanUp() {
		if(f.exists()) {
			f.delete();
		}
	}

}
