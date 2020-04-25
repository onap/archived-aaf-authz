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
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.OrganizationFactory;
import org.onap.aaf.auth.rserv.RServlet;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.register.Registrant;
import org.onap.aaf.cadi.register.Registrar;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

public abstract class AbsServiceStarter<ENV extends RosettaEnv, TRANS extends Trans> implements ServiceStarter {
    private Registrar<ENV> registrar;
    private boolean doRegister;
    protected AbsService<ENV,TRANS> service;
    protected String hostname;
    protected final boolean secure;


    public AbsServiceStarter(final AbsService<ENV,TRANS> service, boolean secure) {
        this.secure = secure;
        this.service = service;
        try {
            OrganizationFactory.init(service.env);
        } catch (OrganizationException e) {
            service.access.log(e, "Missing defined Organization Plugins");
               System.exit(3);
        }
        // do_register - this is used for specialty Debug Situations.  Developer can create an Instance for a remote system
        // for Debugging purposes without fear that real clients will start to call your debug instance
        doRegister = !"TRUE".equalsIgnoreCase(access().getProperty("aaf_locate_no_register",null));
        hostname = access().getProperty(Config.HOSTNAME, null);
        if (hostname==null) {
            try {
                hostname = Inet4Address.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                hostname= "cannotBeDetermined";
            }
        }
        _propertyAdjustment();
    }


    protected abstract void _start(RServlet<TRANS> rserv) throws Exception;
    protected abstract void _propertyAdjustment();

    public ENV env() {
        return service.env;
    }

    public Access access() {
        return service.access;
    }

    @Override
    public final void start() throws Exception {
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<?> app = es.submit(this);
        final AbsServiceStarter<?,?> absSS = this;
        // Docker/K8 may separately create startup Status in this dir for startup
        // sequencing.  If so, delete ON EXIT
        Runtime.getRuntime().addShutdownHook(new Thread() {
          @Override
          public void run() {
              absSS.access().printf(Level.INIT, "Shutting down %s:%s\n",absSS.service.appName, absSS.service.appVersion);
              absSS.shutdown();
              app.cancel(true);
          }
        });
        if(System.getProperty("ECLIPSE", null)!=null) {
            Thread.sleep(2000);
            if(!app.isCancelled()) {
                System.out.println("Service Started in Eclipse: ");
                System.out.print("  Hit <enter> to end:\n");
                try {
                    System.in.read();
                    System.exit(0);
                } catch (IOException e) {
                }
            }
        }
    }

    @SafeVarargs
    public final synchronized void register(final Registrant<ENV> ... registrants) {
        if (doRegister) {
            if (registrar==null) {
                registrar = new Registrar<ENV>(env(),false);
            }
            for (Registrant<ENV> r : registrants) {
                registrar.register(r);
            }
        }
    }

    @Override
    public void run() {
        try {
            _start(service);
        } catch (Exception e) {
            e.printStackTrace();
            shutdown();
        }
    }

    @Override
    public void shutdown() {
        if (registrar!=null) {
            registrar.close(env());
            registrar=null;
        }
        if (service!=null) {
            File status = new File("/opt/app/aaf/status/");
            boolean deleted = false;
            if(status.exists()) {
                int lastdot = service.appName.lastIndexOf("aaf.");
                String fname;
                if(lastdot<0) {
                    fname = service.appName + '-' + hostname;
                } else {
                    fname = service.appName.substring(lastdot).replace('.', '-')
                            + '-' + hostname;
                }
                status = new File(status, fname);
                if(status.exists()) {
                    deleted=status.delete();
                }
            }
            if(deleted) {
                service.access.log(Level.INIT, "Deleted Status",status.getAbsolutePath());
            } else if(status.exists()) {
                service.access.log(Level.INIT, "Status not deleted: ",status.getAbsolutePath());
            }
            service.destroy();
        }
    }
}
