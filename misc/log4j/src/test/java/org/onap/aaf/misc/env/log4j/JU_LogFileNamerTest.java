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

package org.onap.aaf.misc.env.log4j;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class JU_LogFileNamerTest {
    private File dir = new File(".");

    @Before
    public void setUp() throws Exception {
    }

    private void cleanup(String name) {
//    	System.out.println("XXXX" + dir.getAbsolutePath());
    	for(File f : dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.contains(name) && name.endsWith(".log");
			}
		})) {
//    		System.out.println("Deleting " + f.getAbsolutePath());
    		f.delete();
    	};
    }


    @Test
    public void test() throws IOException {
    	String name = "Append";
    	try {
	        LogFileNamer logFileNamer = new LogFileNamer(dir.getCanonicalPath(), "log");
	        assertEquals(logFileNamer, logFileNamer.noPID());
	
	        logFileNamer.setAppender(name);
	        assertEquals(System.getProperty("LOG4J_FILENAME_Append"),
	            dir.getCanonicalFile() + File.separator + "log-" + name + ".log");
	
	        logFileNamer.setAppender(name);
	        assertEquals(System.getProperty("LOG4J_FILENAME_Append"),
	            dir.getCanonicalFile() + File.separator + "log-" + name + ".0.log");
    	} finally {
    		cleanup("log-" + name);
    	}
    }

    @Test
    public void testBlankRoot() throws IOException {
    	String name = "Different";
    	try {
	        LogFileNamer logFileNamer = new LogFileNamer(dir.getCanonicalPath(), "");
	        assertEquals(logFileNamer, logFileNamer.noPID());
	
	        logFileNamer.setAppender(name);
	        assertEquals(System.getProperty("LOG4J_FILENAME_Different"),
	            dir.getCanonicalPath() + File.separator + name + ".log");
	
	        logFileNamer.setAppender(name);
	        assertEquals(System.getProperty("LOG4J_FILENAME_Different"),
	            dir.getCanonicalPath() + File.separator + name + ".0.log");
    	} finally {
    		cleanup(name);
    	}
    }

}
