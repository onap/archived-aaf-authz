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
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.OrganizationFactory;
import org.onap.aaf.auth.rserv.RServlet;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.register.Registrant;
import org.onap.aaf.cadi.register.Registrar;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

public abstract class AbsServiceStarter<ENV extends RosettaEnv, TRANS extends Trans> implements ServiceStarter {
    private Registrar<ENV> registrar;
    private boolean do_register;
    protected AbsService<ENV,TRANS> service;


    public AbsServiceStarter(final AbsService<ENV,TRANS> service) {
        this.service = service;
        try {
            OrganizationFactory.init(service.env);
        } catch (OrganizationException e) {
            service.access.log(e, "Missing defined Organzation Plugins");
            System.exit(3);
        }
        // do_register - this is used for specialty Debug Situations.  Developer can create an Instance for a remote system
        // for Debugging purposes without fear that real clients will start to call your debug instance
        do_register = !"TRUE".equalsIgnoreCase(access().getProperty("aaf_locate_no_register",null));
        _propertyAdjustment();
    }
    
    public abstract void _start(RServlet<TRANS> rserv) throws Exception;
    public abstract void _propertyAdjustment();
    
    public ENV env() {
        return service.env;
    }
    
    public Access access() {
        return service.access;
    }

    @Override
    public final void start() throws Exception {
        _start(service);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdown();
            }
        });
    }

    @SafeVarargs
    public final synchronized void register(final Registrant<ENV> ... registrants) {
        if (do_register) {
            if (registrar==null) {
                registrar = new Registrar<ENV>(env(),false);
            }
            for (Registrant<ENV> r : registrants) {
                registrar.register(r);
            }
        }
    }

    @Override
    public void shutdown() {
        if (registrar!=null) {
            registrar.close(env());
            registrar=null;
        } 
        if (service!=null) {
            service.destroy();
        }
    }
}
