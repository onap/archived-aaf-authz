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

package org.onap.aaf.auth.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.onap.aaf.cadi.Access;

public class Log4JAccessAppender extends AppenderSkeleton{
    private Access access;

    public Log4JAccessAppender(Access access) {
        this.access = access;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    protected void append(LoggingEvent event) {
        Access.Level al = null;
        switch(event.getLevel().toInt()) {
            case Level.FATAL_INT:
            case Level.ERROR_INT:
                if(access.willLog(Access.Level.ERROR)) {
                    al=Access.Level.ERROR;
                }
                break;
            case Level.WARN_INT:
                if(!access.willLog(Access.Level.WARN)) {
                    al=Access.Level.WARN;
                }
                break;
            case Level.ALL_INT:
            case Level.INFO_INT:
                if(!access.willLog(Access.Level.INFO)) {
                    al=Access.Level.INFO;
                }
                break;
            case Level.TRACE_INT:
                if(!access.willLog(Access.Level.TRACE)) {
                    al=Access.Level.TRACE;
                }
                break;
        }
        if(al!=null) {
            access.log(al,"Log4J["+event.getLoggerName()+"]["+event.getLevel()+']',event.getMessage());
        }
    }

}

