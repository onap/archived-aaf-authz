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
package org.onap.aaf.auth.server;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.PropAccess.LogIt;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.log4j.LogFileNamer;

public class Log4JLogIt implements LogIt {
	// Sonar says cannot be static... it's ok.  not too many PropAccesses created.
	private final SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	private final String service;
	private final String audit;
	private final String init;
	private final String trace;

	private final Logger lservice;
	private final Logger laudit;
	private final Logger linit;
	private final Logger ltrace;


	public Log4JLogIt(final String log_dir, final String log_level, final String propsFile, final String root) throws APIException {
		LogFileNamer lfn = new LogFileNamer(log_dir,root);
		try {
			service=lfn.setAppender("service"); // when name is split, i.e. authz|service, the Appender is "authz", and "service"
			audit=lfn.setAppender("audit");     // is part of the log-file name
			init=lfn.setAppender("init");
			trace=lfn.setAppender("trace");

			lservice = Logger.getLogger(service);
			laudit = Logger.getLogger(audit);
			linit = Logger.getLogger(init);
			ltrace = Logger.getLogger(trace);
	
			lfn.configure(propsFile, log_level);
		} catch (IOException e) {
			throw new APIException(e);
		}
	}
	
	@Override
	public void push(Level level, Object... elements) {
		switch(level) {
			case AUDIT:
				laudit.warn(PropAccess.buildMsg(audit, iso8601, level, elements));
				break;
			case INIT:
				linit.warn(PropAccess.buildMsg(init, iso8601, level, elements));
				break;
			case ERROR:
				lservice.error(PropAccess.buildMsg(service, iso8601, level, elements));
				break;
			case WARN:
				lservice.warn(PropAccess.buildMsg(service, iso8601, level, elements));
				break;
			case INFO:
				lservice.info(PropAccess.buildMsg(service, iso8601, level, elements));
				break;
			case DEBUG:
				lservice.debug(PropAccess.buildMsg(service, iso8601, level, elements));
				break;
			case TRACE:
				ltrace.trace(PropAccess.buildMsg(service, iso8601, level, elements));
				break;
			case NONE:
				break;
			default:
				lservice.info(PropAccess.buildMsg(service, iso8601, level, elements));
				break;
		
		}

	}
}
