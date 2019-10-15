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


package org.onap.aaf.auth.hello;

import java.util.Map;

import javax.servlet.Filter;

import org.onap.aaf.auth.cache.Cache.Dated;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransFilter;
import org.onap.aaf.auth.rserv.HttpCode;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.auth.server.AbsService;
import org.onap.aaf.auth.server.JettyServiceStarter;
import org.onap.aaf.auth.server.Log4JLogIt;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.v2_0.AAFAuthn;
import org.onap.aaf.cadi.aaf.v2_0.AAFLurPerm;
import org.onap.aaf.cadi.aaf.v2_0.AAFTrustChecker;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.register.Registrant;
import org.onap.aaf.cadi.register.RemoteRegistrant;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;

public class AAF_Hello extends AbsService<AuthzEnv,AuthzTrans> {
    public enum API{TOKEN_REQ, TOKEN,INTROSPECT, ERROR,VOID};
    public Map<String, Dated> cacheUser;
    public AAFAuthn<?> aafAuthn;
    public AAFLurPerm aafLurPerm;

    /**
     * Construct AuthzAPI with all the Context Supporting Routes that Authz needs
     *
     * @param env
     * @param si 
     * @param dm 
     * @param decryptor 
     * @throws APIException 
     */
    public AAF_Hello(final AuthzEnv env) throws Exception {
        super(env.access(), env);
    
        aafLurPerm = aafCon().newLur();
        // Note: If you need both Authn and Authz construct the following:
        aafAuthn = aafCon().newAuthn(aafLurPerm);

        String aaf_env = env.getProperty(Config.AAF_ENV);
        if (aaf_env==null) {
            throw new APIException("aaf_env needs to be set");
        }
    
        // Initialize Facade for all uses
        AuthzTrans trans = env.newTrans();
        StringBuilder sb = new StringBuilder();
        trans.auditTrail(2, sb);
        trans.init().log(sb);
    
        API_Hello.init(this);
}

    /**
     * Setup XML and JSON implementations for each supported Version type
     *
     * We do this by taking the Code passed in and creating clones of these with the appropriate Facades and properties
     * to do Versions and Content switches
     *
     */
    public void route(HttpMethods meth, String path, API api, HttpCode<AuthzTrans, AAF_Hello> code){
        String version = "1.0";
        // Get Correct API Class from Mapper
        route(env,meth,path,code,"text/plain;version="+version,"*/*");
    }

    @Override
    public Filter[] _filters(Object ... additionalTafLurs) throws CadiException, LocatorException {
        try {
            return new Filter[] {
                    new AuthzTransFilter(env,aafCon(),
                        new AAFTrustChecker((Env)env),
                        additionalTafLurs)
                };
        } catch (NumberFormatException e) {
            throw new CadiException("Invalid Property information", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Registrant<AuthzEnv>[] registrants(final int port) throws CadiException, LocatorException {
        return new Registrant[] {
            new RemoteRegistrant<AuthzEnv>(aafCon(),port)
        };
    }

    public static void main(final String[] args) {
        try {
            Log4JLogIt logIt = new Log4JLogIt(args, "hello");
            PropAccess propAccess = new PropAccess(logIt,args);

            try {
                new JettyServiceStarter<AuthzEnv,AuthzTrans>(
                    new AAF_Hello(new AuthzEnv(propAccess)),true)
                        .start();
            } catch (Exception e) {
                propAccess.log(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
