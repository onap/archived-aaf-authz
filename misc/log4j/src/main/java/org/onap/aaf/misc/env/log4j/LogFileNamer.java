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
import java.net.URL;

public class LogFileNamer {
	public static final int pid = PIDAccess.INSTANCE.getpid();
	public final String root;
	private boolean printPID;
	
	public LogFileNamer(String root) {
		if(root==null || "".equals(root) || root.endsWith("/")) {
			this.root = root;
		} else {
			this.root = root + "-";
		}
		printPID=true;
	}
	
	public LogFileNamer noPID() {
		printPID = false;
		return this;
	}
	/**
	 * Accepts a String.
	 * If Separated by "|" then first part is the Appender name, and the second is used in the FileNaming
	 * (This is to allow for shortened Logger names, and more verbose file names)
	 * 
	 * @param appender
	 * 
	 * returns the String Appender
	 */
	public String setAppender(String appender) {
		int pipe = appender.indexOf('|');
		if(pipe>=0) {
			String rv;
			System.setProperty(
				"LOG4J_FILENAME_"+(rv=appender.substring(0,pipe)),
				root + appender.substring(pipe+1) + (printPID?('-' + pid):"") + ".log");
			return rv;
		} else {
			System.setProperty(
				"LOG4J_FILENAME_"+appender,
				root + appender + (printPID?('-' + pid):"") + ".log");
			return appender;
		}
		
	}

	public void configure(String props) {
		String fname;
		if(new File(fname="etc/"+props).exists()) {
			org.apache.log4j.PropertyConfigurator.configureAndWatch(fname,60*1000);
		} else {
			URL rsrc = ClassLoader.getSystemResource(props);
			if(rsrc==null) System.err.println("Neither File: " + fname + " or resource on Classpath " + props + " exist" );
			org.apache.log4j.PropertyConfigurator.configure(rsrc);
		}
	}
}
