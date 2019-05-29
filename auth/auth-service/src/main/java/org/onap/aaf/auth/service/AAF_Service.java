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

package org.onap.aaf.auth.service;

import javax.servlet.Filter;

import org.onap.aaf.auth.cache.Cache;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.cass.CacheInfoDAO;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.direct.DirectAAFLur;
import org.onap.aaf.auth.direct.DirectAAFUserPass;
import org.onap.aaf.auth.direct.DirectCertIdentity;
import org.onap.aaf.auth.direct.DirectLocatorCreator;
import org.onap.aaf.auth.direct.DirectRegistrar;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransFilter;
import org.onap.aaf.auth.org.OrganizationFactory;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.auth.server.AbsService;
import org.onap.aaf.auth.server.JettyServiceStarter;
import org.onap.aaf.auth.server.Log4JLogIt;
import org.onap.aaf.auth.service.api.API_Api;
import org.onap.aaf.auth.service.api.API_Approval;
import org.onap.aaf.auth.service.api.API_Creds;
import org.onap.aaf.auth.service.api.API_Delegate;
import org.onap.aaf.auth.service.api.API_History;
import org.onap.aaf.auth.service.api.API_Mgmt;
import org.onap.aaf.auth.service.api.API_NS;
import org.onap.aaf.auth.service.api.API_Perms;
import org.onap.aaf.auth.service.api.API_Roles;
import org.onap.aaf.auth.service.api.API_User;
import org.onap.aaf.auth.service.api.API_UserRole;
import org.onap.aaf.auth.service.facade.AuthzFacadeFactory;
import org.onap.aaf.auth.service.facade.AuthzFacade_2_0;
import org.onap.aaf.auth.service.mapper.Mapper.API;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.v2_0.AAFTrustChecker;
import org.onap.aaf.cadi.aaf.v2_0.AbsAAFLocator;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.register.Registrant;
import org.onap.aaf.cadi.taf.basic.BasicHttpTaf;
import org.onap.aaf.cadi.util.FQI;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Env;

import com.datastax.driver.core.Cluster;

public class AAF_Service extends AbsService<AuthzEnv,AuthzTrans> {

    private static final String ORGANIZATION = "Organization.";

    public final Question question;
    private AuthzFacade_2_0 facade;
    private AuthzFacade_2_0 facade_XML;
    private DirectAAFUserPass directAAFUserPass;
    private final Cluster cluster;
    //private final OAuthService oauthService;
    
    /**
     * Construct AuthzAPI with all the Context Supporting Routes that Authz needs
     * 
     * @param env
     * @param decryptor 
     * @throws APIException 
     */
    public AAF_Service( final AuthzEnv env) throws Exception {
        super(env.access(), env);

        // Initialize Facade for all uses
        AuthzTrans trans = env.newTrans();

        cluster = org.onap.aaf.auth.dao.CassAccess.cluster(env,null);

        // Need Question for Security purposes (direct User/Authz Query in Filter)
        // Start Background Processing
        question = new Question(trans, cluster, CassAccess.KEYSPACE);
        question.startTimers(env);
        
        DirectCertIdentity.set(question.certDAO());

        // Have AAFLocator object Create DirectLocators for Location needs
        AbsAAFLocator.setCreator(new DirectLocatorCreator(env, question.locateDAO));
        
        // Initialize Organizations... otherwise, first pass may miss
        int org_size = ORGANIZATION.length();
        for (String n : env.existingStaticSlotNames()) {
            if (n.startsWith(ORGANIZATION)) {
                OrganizationFactory.obtain(env, n.substring(org_size));
            }
        }
        

        // For direct Introspection needs.
        //oauthService = new OAuthService(trans, question);
        
        facade = AuthzFacadeFactory.v2_0(env,trans,Data.TYPE.JSON,question);
        facade_XML = AuthzFacadeFactory.v2_0(env,trans,Data.TYPE.XML,question);

        directAAFUserPass = new DirectAAFUserPass(trans.env(),question);
    
        // Print results and cleanup
        StringBuilder sb = new StringBuilder();
        trans.auditTrail(0, sb);
        if (sb.length()>0)env.init().log(sb);
        trans = null;
        sb = null;

        ////////////////////////////////////////////////////////////////////////////
        // Time Critical
        //  These will always be evaluated first
        ////////////////////////////////////////////////////////////////////////
        API_Creds.timeSensitiveInit(env, this, facade,directAAFUserPass);
        API_Perms.timeSensitiveInit(this, facade);
        ////////////////////////////////////////////////////////////////////////
        // Service APIs
        ////////////////////////////////////////////////////////////////////////
        API_Creds.init(this, facade);
        API_UserRole.init(this, facade);
        API_Roles.init(this, facade);
        API_Perms.init(this, facade);
        API_NS.init(this, facade);
        API_User.init(this, facade);
        API_Delegate.init(this,facade);
        API_Approval.init(this, facade);
        API_History.init(this, facade);

        ////////////////////////////////////////////////////////////////////////
        // Management APIs
        ////////////////////////////////////////////////////////////////////////
        // There are several APIs around each concept, and it gets a bit too
        // long in this class to create.  The initialization of these Management
        // APIs have therefore been pushed to StandAlone Classes with static
        // init functions
        API_Mgmt.init(this, facade);
        API_Api.init(this, facade);
        
    }
    
    @Override
    public Filter[] _filters(Object ... additionalTafLurs) throws CadiException, LocatorException {
        final String domain = FQI.reverseDomain(access.getProperty(Config.AAF_ROOT_NS,Config.AAF_ROOT_NS_DEF));
        try {
            Object[] atl=new Object[additionalTafLurs.length+2];
            atl[0]=new DirectAAFLur(env,question); // Note, this will be assigned by AuthzTransFilter to TrustChecker
            atl[1]= new BasicHttpTaf(env, directAAFUserPass,
                    domain,Long.parseLong(env.getProperty(Config.AAF_CLEAN_INTERVAL, Config.AAF_CLEAN_INTERVAL_DEF)),
                    false);

            if (additionalTafLurs.length>0) {
                System.arraycopy(additionalTafLurs, 0, atl, 2, additionalTafLurs.length);
            }
            
            return new Filter[] {
                new AuthzTransFilter(env,aafCon(),
                    new AAFTrustChecker((Env)env),
                    atl
            )};
        } catch (NumberFormatException e) {
            throw new CadiException("Invalid Property information", e);
        }
    }



    @SuppressWarnings("unchecked")
    @Override
    public Registrant<AuthzEnv>[] registrants(final int actualPort) throws CadiException {
        return new Registrant[] {
            new DirectRegistrar(access,question.locateDAO, actualPort)
        };
    }
    
    @Override 
    public void postStartup(final String hostname, final int port) throws APIException {
    	try {
			CacheInfoDAO.startUpdate(env, aafCon().hman(), aafCon().securityInfo().defSS,hostname,port);
		} catch (CadiException | LocatorException e) {
			throw new APIException(e);
		}
    }

    @Override
    public void destroy() {
        Cache.stopTimer();
        CacheInfoDAO.stopUpdate();
        if (cluster!=null) {
            cluster.close();
        }
        super.destroy();
    }

    
    /**
     * Setup XML and JSON implementations for each supported Version type
     * 
     * We do this by taking the Code passed in and creating clones of these with the appropriate Facades and properties
     * to do Versions and Content switches
     * 
     */
    public void route(HttpMethods meth, String path, API api, Code code) throws Exception {
        Class<?> respCls = facade.mapper().getClass(api); 
        if (respCls==null) throw new Exception("Unknown class associated with " + api.getClass().getName() + ' ' + api.name());
        String application = applicationJSON(respCls, Config.AAF_DEFAULT_API_VERSION);

        route(env,meth,path,code,application,"application/json;version="+Config.AAF_DEFAULT_API_VERSION,"*/*");
        application = applicationXML(respCls, Config.AAF_DEFAULT_API_VERSION);
        route(env,meth,path,code.clone(facade_XML,false),application,"text/xml;version="+Config.AAF_DEFAULT_API_VERSION);
    }

    /**
     * Start up AAF_Service as Jetty Service
     */
    public static void main(final String[] args) {
        try {
            Log4JLogIt logIt = new Log4JLogIt(args, "authz");
            PropAccess propAccess = new PropAccess(logIt,args);
            
            AbsService<AuthzEnv, AuthzTrans> service = new AAF_Service(new AuthzEnv(propAccess));
            JettyServiceStarter<AuthzEnv,AuthzTrans> jss = new JettyServiceStarter<AuthzEnv,AuthzTrans>(service);
            jss.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
