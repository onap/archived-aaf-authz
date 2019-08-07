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

package org.onap.aaf.misc.env;

import java.io.PrintStream;
import java.util.Date;

import org.onap.aaf.misc.env.util.Chrono;

/**
 * LogTarget is the interface with which to assign any kind of Logging Implementations.
 * 
 * Implement for any Logging Library of your choice, and for any logging string Format desired.
 * 
 * Included are several Static Implementations for various uses:
 *      NULL: Does nothing with Logging Messages
 *   SYSOUT: Writes messages in general form to System Out
 *   SYSERR: Writes messages in general form to System Err
 *   
 * @author Jonathan
 *
 */
public interface LogTarget {
    public abstract void log(Object... msgs);
    public abstract void log(Throwable e, Object ... msgs);
    public abstract boolean isLoggable();
    public abstract void printf(String fmt, Object ... vars);

    // A Convenient LogTarget to insert when a NO-OP is desired.
    public static final LogTarget NULL = new LogTarget() {
        public void log(Object ... msgs) {
        }

        public void log(Throwable t, Object ... msgs) {
        }

        public boolean isLoggable() {
            return false;
        }

        @Override
        public void printf(String fmt, Object ... vars) {
        }
    };

    // A Convenient LogTarget to write to the Console
    public static final LogTarget SYSOUT = new StreamLogTarget(System.out,false);
    
    // A Convenient LogTarget to write to the Console
    public static final LogTarget SYSERR = new StreamLogTarget(System.err,false);
    
    public static class StreamLogTarget implements LogTarget {
        private final PrintStream out;
        private final boolean closeMe;

        public StreamLogTarget(PrintStream ps) {
            this(ps,true);
        }

        /* Do NOT close SYSTEM ERR or OUT*/
        protected StreamLogTarget(PrintStream ps, boolean shouldClose) {
            out = ps;
            closeMe = shouldClose;
        }
        public void log(Object ... msgs) {
            out.print(Chrono.dateFmt.format(new Date()));
            out.print(": ");
            for (Object str : msgs) {
                if (str!=null) {
                    out.print(str.toString());
                    out.print(' ');
                } else {
                    out.print("null ");
                }
            }
            out.println();
        }

        public void log(Throwable t, Object ... msgs) {
            out.print(Chrono.dateFmt.format(new Date()));
            out.print(": ");
            for (Object str : msgs) {
                out.print(str.toString());
                out.print(' ');
            }
            out.println();
            t.printStackTrace(out);
            out.println();
        }

        public boolean isLoggable() {
            return true;
        }

        @Override
        public void printf(String fmt, Object ... vars) {
            log(String.format(fmt,vars));
        }
        
        public void close() {
            if(closeMe) {
                out.close();
            }
        }

    }

};