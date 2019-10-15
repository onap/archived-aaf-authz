/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.auth.server;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.PropAccess.LogIt;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.log4j.LogFileNamer;
import org.onap.aaf.misc.env.util.Chrono;

public class Log4JLogIt implements LogIt {
    protected static final String AAF_LOG4J_PREFIX = "aaf_log4j_prefix";
    // Log4j does it's own date.  Can't apparently turn it off.

    private final String service;
    private final String audit;
    private final String init;
    private final String trace;

    private final Logger lservice;
    private final Logger laudit;
    private final Logger linit;
    private final Logger ltrace;


    public Log4JLogIt(final String[] args, final String root) throws APIException {
        String propsFile = getArgOrVM(AAF_LOG4J_PREFIX, args, "org.osaaf.aaf")+".log4j.props";
        String log_dir = getArgOrVM(Config.CADI_LOGDIR,args,"/opt/app/osaaf/logs");
        String etc_dir = getArgOrVM(Config.CADI_ETCDIR,args,"/opt/app/osaaf/etc");
        String log_level = getArgOrVM(Config.CADI_LOGLEVEL,args,"INFO");
        File logs = new File(log_dir);
        if (!logs.isDirectory()) {
            logs.delete();
        }
        if (!logs.exists()) {
            logs.mkdirs();
        }

        if (System.getProperty("log4j.configuration")==null) {
            System.setProperty("log4j.configuration", etc_dir+'/'+propsFile);
        }
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

            lfn.configure(etc_dir,propsFile, log_level);
        } catch (IOException e) {
            throw new APIException(e);
        }
    }

    private static final String getArgOrVM(final String tag, final String args[], final String def) {
        String tagEQ = tag + '=';
        String value;
        for (String arg : args) {
            if (arg.startsWith(tagEQ)) {
                return arg.substring(tagEQ.length());
            }
        }
        // check System.properties
        value = System.getProperty(tag);
        if (value!=null) { 
            return value;
        }
    
        return def;
    }

    @Override
    public void push(Level level, Object... elements) {
        switch(level) {
            case AUDIT:
                laudit.warn(PropAccess.buildMsg(audit, Chrono.utcFmt, level, elements));
                break;
            case INIT:
                linit.warn(PropAccess.buildMsg(init, Chrono.utcFmt, level, elements));
                break;
            case ERROR:
                lservice.error(PropAccess.buildMsg(service, Chrono.utcFmt, level, elements));
                break;
            case WARN:
                lservice.warn(PropAccess.buildMsg(service, Chrono.utcFmt, level, elements));
                break;
            case INFO:
                lservice.info(PropAccess.buildMsg(service, Chrono.utcFmt, level, elements));
                break;
            case DEBUG:
                lservice.debug(PropAccess.buildMsg(service, Chrono.utcFmt, level, elements));
                break;
            case TRACE:
                ltrace.trace(PropAccess.buildMsg(service, Chrono.utcFmt, level, elements));
                break;
            case NONE:
                break;
            default:
                lservice.info(PropAccess.buildMsg(service, Chrono.utcFmt, level, elements));
                break;
    
        }

    }
}
