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

package org.onap.aaf.cadi.register;

import java.util.Deque;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.onap.aaf.cadi.client.Result;
import org.onap.aaf.misc.env.impl.BasicEnv;

public class Registrar<ENV extends BasicEnv> {
    private static final String REGISTRAR = "Registrar";
    private static final long INTERVAL = 15*60*1000L; // 15 mins
    private static final long START = 3000; // Start in 3 seconds
    private static final Object LOCK = new Object();
    private Deque<Registrant<ENV>> registrants;
    private Timer timer, erroringTimer;

    public Registrar(final ENV env, boolean shutdownHook) {
        registrants = new ConcurrentLinkedDeque<Registrant<ENV>>();

        erroringTimer = null;
        timer = new Timer(REGISTRAR,true);
        timer.schedule(new RegistrationTimerTask(env), START, INTERVAL); 
    
        if (shutdownHook) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    close(env);
                }
            });
        }
    }

    private class RegistrationTimerTask extends TimerTask {
        private final ENV env;
        public RegistrationTimerTask(ENV env) {
            this.env = env;
        }
        @Override
        public void run() {
            for (Iterator<Registrant<ENV>> iter = registrants.iterator(); iter.hasNext();) {
                Registrant<ENV> reg = iter.next();
                Result<Void> rv = reg.update(env);
                synchronized(LOCK) {
                    if (rv.isOK()) {
                        if (erroringTimer!=null) {
                            erroringTimer.cancel();
                            erroringTimer = null;
                        }
                    } else {
                        env.error().log(rv.toString());
                        // Account for different Registrations not being to same place
                        if (erroringTimer==null) {
                            erroringTimer =  new Timer(REGISTRAR + " error re-check ",true);
                            erroringTimer.schedule(new RegistrationTimerTask(env),20000,20000);
                        }
                    }
                }
            }
        }
    }

    public void register(Registrant<ENV> r) {
        registrants.addLast(r);
    }

    public void deregister(Registrant<ENV> r) {
        registrants.remove(r);
    }

    public void close(ENV env) {
        timer.cancel();

        Registrant<ENV> r;
        while (registrants.peek()!=null) {
            r = registrants.pop();
            r.cancel(env);
        }
    }
}
