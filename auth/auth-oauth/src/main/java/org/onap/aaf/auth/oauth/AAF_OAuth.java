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


package org.onap.aaf.auth.oauth;

import java.util.Map;

import javax.servlet.Filter;

import org.onap.aaf.auth.cache.Cache;
import org.onap.aaf.auth.cache.Cache.Dated;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.direct.DirectLocatorCreator;
import org.onap.aaf.auth.direct.DirectRegistrar;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransFilter;
import org.onap.aaf.auth.oauth.api.API_Token;
import org.onap.aaf.auth.oauth.facade.OAFacade;
import org.onap.aaf.auth.oauth.facade.OAFacade1_0;
import org.onap.aaf.auth.oauth.facade.OAFacadeFactory;
import org.onap.aaf.auth.oauth.mapper.Mapper.API;
import org.onap.aaf.auth.oauth.service.OAuthService;
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
import org.onap.aaf.cadi.aaf.v2_0.AbsAAFLocator;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.oauth.TokenMgr;
import org.onap.aaf.cadi.oauth.TokenMgr.TokenPermLoader;
import org.onap.aaf.cadi.register.Registrant;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.env.Env;

import com.datastax.driver.core.Cluster;

import aafoauth.v2_0.Introspect;

public class AAF_OAuth extends AbsService<AuthzEnv,AuthzTrans> {
    public Map<String, Dated> cacheUser;
    public AAFAuthn<?> aafAuthn;
    public AAFLurPerm aafLurPerm;
    private final OAuthService service;
    private OAFacade1_0 facade1_0;
    private final Question question;
    private TokenPermLoader tpLoader; 
    private final Cluster cluster;
    
    /**
     * Construct AuthzAPI with all the Context Supporting Routes that Authz needs
     * 
     * @param env
     * @param si 
     * @param dm 
     * @param decryptor 
     * @throws APIException 
     */
    public AAF_OAuth(final AuthzEnv env) throws Exception {
        super(env.access(),env);
        
        String aaf_env = env.getProperty(Config.AAF_ENV);
        if (aaf_env==null) {
            throw new APIException("aaf_env needs to be set");
        }
        
        // Initialize Facade for all uses
        AuthzTrans trans = env.newTrans();
        cluster = org.onap.aaf.auth.dao.CassAccess.cluster(env,null);
        
        aafLurPerm = aafCon().newLur();
        // Note: If you need both Authn and Authz construct the following:
        aafAuthn = aafCon().newAuthn(aafLurPerm);

        // Start Background Processing
        //    Question question = 
        question = new Question(trans, cluster, CassAccess.KEYSPACE);
        question.startTimers(env);

        // Have AAFLocator object Create DirectLocators for Location needs
        AbsAAFLocator.setCreator(new DirectLocatorCreator(env, question.locateDAO));


        service = new OAuthService(env.access(),trans,question);
        facade1_0 = OAFacadeFactory.v1_0(this, trans, service, TYPE.JSON);
        StringBuilder sb = new StringBuilder();
        trans.auditTrail(2, sb);
        trans.init().log(sb);
        
        API_Token.init(this, facade1_0);
    }
    
    /**
     * Setup XML and JSON implementations for each supported Version type
     * 
     * We do this by taking the Code passed in and creating clones of these with the appropriate Facades and properties
     * to do Versions and Content switches
     * 
     */
    public void route(HttpMethods meth, String path, API api, HttpCode<AuthzTrans, OAFacade<Introspect>> code) throws Exception {
        String version = "1.0";
        // Get Correct API Class from Mapper
        Class<?> respCls = facade1_0.mapper().getClass(api); 
        if (respCls==null) throw new Exception("Unknown class associated with " + api.getClass().getName() + ' ' + api.name());
        // setup Application API HTML ContentTypes for JSON and Route
        String application = applicationJSON(respCls, version);
        if (meth.equals(HttpMethods.POST)) {
            route(env,meth,path,code,application,"application/json;version="+version,"application/x-www-form-urlencoded","*/*");
        } else {
            route(env,meth,path,code,application,"application/json;version="+version,"*/*");
        }
    }
    
    @Override
    public Filter[] _filters(Object ... additionalTafLurs) throws CadiException, LocatorException {
        try {
            DirectOAuthTAF doat = new DirectOAuthTAF(env,question,facade1_0);
            Object[] atl=new Object[additionalTafLurs.length+2];
            atl[0] = doat;
            atl[1] = doat.directUserPass();

            if (additionalTafLurs.length>0) {
                System.arraycopy(additionalTafLurs, 0, atl, 2, additionalTafLurs.length);
            }
            
            return new Filter[] {
                new AuthzTransFilter(env,aafCon(),
                    new AAFTrustChecker((Env)env),
                    atl
            )};
        } catch (NumberFormatException | APIException e) {
            throw new CadiException("Invalid Property information", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Registrant<AuthzEnv>[] registrants(final int port) throws CadiException {
        return new Registrant[] {
            new DirectRegistrar(access,question.locateDAO,port)
        };
    }


    @Override
    public void destroy() {
        Cache.stopTimer();
        if (service!=null) {
            service.close();
        }
        if (cluster!=null) {
            cluster.close();
        }
        super.destroy();
    }
    
    // For use in CADI ONLY
    public TokenMgr.TokenPermLoader tpLoader() {
        return tpLoader;
    }

    public static void main(final String[] args) {
        try {
            Log4JLogIt logIt = new Log4JLogIt(args, "oauth");
            PropAccess propAccess = new PropAccess(logIt,args);

            try {
                new JettyServiceStarter<AuthzEnv,AuthzTrans>(
                    new AAF_OAuth(new AuthzEnv(propAccess)),true)
                        .start();
            } catch (Exception e) {
                propAccess.log(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
