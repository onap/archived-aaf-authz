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

package org.onap.aaf.misc.env.impl;

import java.io.PrintWriter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.util.StringBuilderWriter;

/**
 * Many services have chosen to use Log4J for their lower level Logging Implementation.  This LogTarget will allow
 * any of the messages sent to be set to the appropriate Log4J level.
 * 
 * @author Jonathan 
 *
 */
public class Log4JLogTarget implements LogTarget {
    private Level level;
    private Logger log;

    public Log4JLogTarget(String loggerName, Level level) throws APIException {
        this.level = level;
        if (loggerName != null && loggerName.length() > 0) {
            log = Logger.getLogger(loggerName);
        } else {
            log = Logger.getRootLogger();
        }
    }

    // @Override
    public boolean isLoggable() {
        return log.isEnabledFor(level);
    }

    // @Override
    public void log(Object... msgs) {
        log(null, msgs);
    }

    // @Override
    public void log(Throwable e, Object... msgs) {
        if (log.isEnabledFor(level)) {
            StringBuilder sb = new StringBuilder();
            
            String msg;
            if (e != null) {
                e.printStackTrace(new PrintWriter(new StringBuilderWriter(sb)));
            }
            for (int i = 0; i < msgs.length; ++i) {
                if(msgs[i]!=null) {
                    msg = msgs[i].toString();
                    if (msg != null && msg.length() > 0) {
                        int sbl = sb.length();
                        if (sbl > 0) {
                            char last = sb.charAt(sbl - 1);
                            if (" (.".indexOf(last) < 0
                                    && "().".indexOf(msg.charAt(0)) < 0)
                                sb.append(' ');
                        }
                        sb.append(msg);
                    }
                }
            }
            log.log(level, sb.toString());
        }
    }

    /* (non-Javadoc)
     * @see com.att.inno.env.LogTarget#printf(java.lang.String, java.lang.String[])
     */
    @Override
    public void printf(String fmt, Object ... vars) {
        if(log.isEnabledFor(level)) {
            log.log(level,String.format(fmt,vars));
        }
    }

    public static void setLog4JEnv(String loggerName, BasicEnv env) throws APIException {
            env.fatal = new Log4JLogTarget(loggerName,Level.FATAL);
            env.error = new Log4JLogTarget(loggerName,Level.ERROR);
            env.warn = env.audit = env.init = new Log4JLogTarget(loggerName,Level.WARN);
            env.info = new Log4JLogTarget(loggerName,Level.INFO);
            env.debug = new Log4JLogTarget(loggerName,Level.DEBUG);
            env.trace = new Log4JLogTarget(loggerName,Level.TRACE);
    }
    
}