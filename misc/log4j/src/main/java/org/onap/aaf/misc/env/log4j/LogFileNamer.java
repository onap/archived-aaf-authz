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

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class LogFileNamer {
    private final String root;
    private final String dir;

    public LogFileNamer(final String dir, final String root) {
        this.dir = dir;
        if (root == null || "".equals(root) || root.endsWith("/")) {
            this.root = root;
        } else {
            this.root = root + "-";
        }
    }

    public LogFileNamer noPID() {
        return this;
    }

    private static final String FIRST_FILE_FORMAT_STR = "%s/%s%s.log";
    private static final String FILE_FORMAT_STR = "%s/%s%s.%d.log";

    /**
     * Accepts a String. If Separated by "|" then first part is the Appender name,
     * and the second is used in the FileNaming (This is to allow for shortened
     * Logger names, and more verbose file names) ONAP: jna code has license issues.
     * Just do Date + Unique Number
     * 
     * @param appender
     * 
     *            returns the String Appender
     * @throws IOException
     */
    public String setAppender(String appender) throws IOException {
    	File f = new File(String.format(FIRST_FILE_FORMAT_STR, dir, root, appender));
    	File lock = new File(f.getAbsoluteFile()+".lock");
    	if(f.exists()) {
    		if(lock.exists()) {
		        int i = 0;
		        while ((f = new File(String.format(FILE_FORMAT_STR, dir, root, appender, i))).exists() &&
		        	   (lock = new File(f.getAbsoluteFile()+".lock")).exists()) {
		            ++i;
		        }
    		}
    	}
        
        try {
        	lock.createNewFile();
        	lock.deleteOnExit();
        	f.createNewFile();
        } catch (IOException e) {
        	throw new IOException("Cannot create file '" + f.getCanonicalPath() + '\'', e);
        }
        System.setProperty("LOG4J_FILENAME_" + appender, f.getCanonicalPath());
        return appender;
    }

    public void configure(final String path, final String fname, final String log_level) throws IOException {
        final String fullPath = path + '/' + fname;
        if (new File(fullPath).exists()) {
            org.apache.log4j.PropertyConfigurator.configureAndWatch(fullPath, 60 * 1000L);
        } else {
            URL rsrc = ClassLoader.getSystemResource(fname);
            if (rsrc == null) {
                String msg = "Neither File: " + path + '/' + fname + " nor resource on Classpath " + fname + " exist";
                throw new IOException(msg);
            }
            org.apache.log4j.PropertyConfigurator.configure(rsrc);
        }

    }
}
