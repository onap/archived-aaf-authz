/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package com.att.authz.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import com.att.aft.dme2.api.DME2Exception;
//import com.att.aft.dme2.api.DME2FilterHolder;
//import com.att.aft.dme2.api.DME2FilterHolder.RequestDispatcherType;
import com.att.aft.dme2.api.DME2Manager;
import com.att.aft.dme2.api.DME2Server;
import com.att.aft.dme2.api.DME2ServerProperties;
import com.att.aft.dme2.api.DME2ServiceHolder;
import com.att.aft.dme2.api.util.DME2FilterHolder;
import com.att.aft.dme2.api.util.DME2FilterHolder.RequestDispatcherType;
import com.att.aft.dme2.api.util.DME2ServletHolder;
//import com.att.aft.dme2.api.DME2ServletHolder;
import com.att.authz.cadi.DirectAAFLur;
import com.att.authz.cadi.DirectAAFUserPass;
import com.att.authz.cadi.DirectCertIdentity;
import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import com.att.authz.env.AuthzTransFilter;
import com.att.authz.facade.AuthzFacadeFactory;
import com.att.authz.facade.AuthzFacade_2_0;
import com.att.authz.org.OrganizationFactory;
import com.att.authz.server.AbsServer;
import com.att.authz.service.api.API_Api;
import com.att.authz.service.api.API_Approval;
import com.att.authz.service.api.API_Creds;
import com.att.authz.service.api.API_Delegate;
import com.att.authz.service.api.API_History;
import com.att.authz.service.api.API_Mgmt;
import com.att.authz.service.api.API_NS;
import com.att.authz.service.api.API_Perms;
import com.att.authz.service.api.API_Roles;
import com.att.authz.service.api.API_User;
import com.att.authz.service.api.API_UserRole;
import com.att.authz.service.mapper.Mapper.API;
import com.att.cadi.CadiException;
import com.att.cadi.LocatorException;
import com.att.cadi.SecuritySetter;
import com.att.cadi.aaf.v2_0.AAFTrustChecker;
import com.att.cadi.config.Config;
import com.att.cadi.config.SecurityInfoC;
import com.att.cadi.http.HBasicAuthSS;
import com.att.cadi.http.HMangr;
import com.att.cadi.http.HX509SS;
import com.att.cadi.locator.DME2Locator;
import com.att.cadi.taf.basic.BasicHttpTaf;
import com.att.cssa.rserv.HttpMethods;
import com.att.dao.CassAccess;
import com.att.dao.aaf.cass.CacheInfoDAO;
import com.att.dao.aaf.hl.Question;
import com.att.inno.env.APIException;
import com.att.inno.env.Data;
import com.att.inno.env.Env;
import com.datastax.driver.core.Cluster;

public class AuthAPI extends AbsServer {

	private static final String ORGANIZATION = "Organization.";
	private static final String DOMAIN = "openecomp.org";

// TODO Add Service Metrics
//	private Metric serviceMetric;
	public final Question question;
//	private final SessionFilter sessionFilter;
	private AuthzFacade_2_0 facade;
	private AuthzFacade_2_0 facade_XML;
	private DirectAAFUserPass directAAFUserPass;
	
	/**
	 * Construct AuthzAPI with all the Context Supporting Routes that Authz needs
	 * 
	 * @param env
	 * @param decryptor 
	 * @throws APIException 
	 */
	public AuthAPI(AuthzEnv env) throws Exception {
		super(env,"AAF");
	
		// Set "aaf_url" for peer communication based on Service DME2 URL
		env.setProperty(Config.AAF_URL, "https://DME2RESOLVE/"+env.getProperty("DMEServiceName"));
		
		// Setup Log Names
		env.setLog4JNames("log4j.properties","authz","authz|service","audit","init","trace");

		final Cluster cluster = com.att.dao.CassAccess.cluster(env,null);

		// jg 4/2015 SessionFilter unneeded... DataStax already deals with Multithreading well
		
		// Setup Shutdown Hooks for Cluster and Pooled Sessions
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
//				sessionFilter.destroy();
				cluster.close();
			}
		}); 
		
		// Initialize Facade for all uses
		AuthzTrans trans = env.newTrans();

		// Initialize Organizations... otherwise, first pass may miss
		int org_size = ORGANIZATION.length();
		for(String n : env.existingStaticSlotNames()) {
			if(n.startsWith(ORGANIZATION)) {
				OrganizationFactory.obtain(env, n.substring(org_size));
			}
		}
		
		// Need Question for Security purposes (direct User/Authz Query in Filter)
		// Start Background Processing
		question = new Question(trans, cluster, CassAccess.KEYSPACE, true);
		
		DirectCertIdentity.set(question.certDAO);
		
		facade = AuthzFacadeFactory.v2_0(env,trans,Data.TYPE.JSON,question);
		facade_XML = AuthzFacadeFactory.v2_0(env,trans,Data.TYPE.XML,question);

		directAAFUserPass = new DirectAAFUserPass(
    			trans.env(),question,trans.getProperty("Unknown"));

		
		// Print results and cleanup
		StringBuilder sb = new StringBuilder();
		trans.auditTrail(0, sb);
		if(sb.length()>0)env.init().log(sb);
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
	
	/**
	 * Setup XML and JSON implementations for each supported Version type
	 * 
	 * We do this by taking the Code passed in and creating clones of these with the appropriate Facades and properties
	 * to do Versions and Content switches
	 * 
	 */
	public void route(HttpMethods meth, String path, API api, Code code) throws Exception {
		String version = "2.0";
		Class<?> respCls = facade.mapper().getClass(api); 
		if(respCls==null) throw new Exception("Unknown class associated with " + api.getClass().getName() + ' ' + api.name());
		String application = applicationJSON(respCls, version);

		route(env,meth,path,code,application,"application/json;version=2.0","*/*");
		application = applicationXML(respCls, version);
		route(env,meth,path,code.clone(facade_XML,false),application,"text/xml;version=2.0");
	}

	/**
	 * Start up AuthzAPI as DME2 Service
	 * @param env
	 * @param props
	 * @throws Exception 
	 * @throws LocatorException 
	 * @throws CadiException 
	 * @throws NumberFormatException 
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 * @throws APIException 
	 */
	public void startDME2(Properties props) throws Exception {
        DME2Manager dme2 = new DME2Manager("AuthzServiceDME2Manager",props);
       	String s = dme2.getStringProp(Config.AFT_DME2_SSL_INCLUDE_PROTOCOLS,null);
       	env.init().log("DME2 Service TLS Protocols are set to",(s==null?"DME2 Default":s));
        
        DME2ServiceHolder svcHolder;
        List<DME2ServletHolder> slist = new ArrayList<DME2ServletHolder>();
        svcHolder = new DME2ServiceHolder();
        String serviceName = env.getProperty("DMEServiceName",null);
    	if(serviceName!=null) {
	    	svcHolder.setServiceURI(serviceName);
	        svcHolder.setManager(dme2);
	        svcHolder.setContext("/");
	        DME2ServletHolder srvHolder = new DME2ServletHolder(this, new String[]{"/authz","/authn","/mgmt"});
	        srvHolder.setContextPath("/*");
	        slist.add(srvHolder);
	        
	        EnumSet<RequestDispatcherType> edlist = EnumSet.of(
	        		RequestDispatcherType.REQUEST,
	        		RequestDispatcherType.FORWARD,
	        		RequestDispatcherType.ASYNC
	        		);
	        
	        List<DME2FilterHolder> flist = new ArrayList<DME2FilterHolder>();

	        // Add DME2 Metrics
	        // DME2 removed the Metrics Filter in 2.8.8.5
        	// flist.add(new DME2FilterHolder(new DME2MetricsFilter(serviceName),"/*",edlist));
	        
	        // Note: Need CADI to fill out User for AuthTransFilter... so it's first
    		// Make sure there is no AAF TAF configured for Filters
    		env.setProperty(Config.AAF_URL,null);

	        flist.add(
		        new DME2FilterHolder(
		        	new AuthzTransFilter(env, null /* no connection to AAF... it is AAF */,
	        			new AAFTrustChecker((Env)env),
	        	        new DirectAAFLur(env,question), // Note, this will be assigned by AuthzTransFilter to TrustChecker
	        	        new BasicHttpTaf(env, directAAFUserPass,
		        			DOMAIN,Long.parseLong(env.getProperty(Config.AAF_CLEAN_INTERVAL, Config.AAF_CLEAN_INTERVAL_DEF)),
		        			false
		        			) // Add specialty Direct TAF
		        		),
		        	"/*", edlist));

	        svcHolder.setFilters(flist);
	        svcHolder.setServletHolders(slist);
	        
	        DME2Server dme2svr = dme2.getServer();
	        
	        String hostname = env.getProperty("HOSTNAME",null);
	        if(hostname!=null) {
	        	//dme2svr.setHostname(hostname);
	        	hostname=null;
	        }
	       // dme2svr.setGracefulShutdownTimeMs(5000);
	
	        env.init().log("Starting AAF Jetty/DME2 server...");
	        dme2svr.start();
	        try {
//	        	if(env.getProperty("NO_REGISTER",null)!=null)
	        	dme2.bindService(svcHolder);
	        	//env.init().log("DME2 is available as HTTPS on port:",dme2svr.getPort());
	        	
	        	// Start CacheInfo Listener
	        	HMangr hman = new HMangr(env, new DME2Locator(env, dme2,"https://DME2RESOLVE/"+serviceName,true /*remove self from cache*/));
				SecuritySetter<HttpURLConnection> ss;
				
//				InetAddress ip = InetAddress.getByName(dme2svr.getHostname());
				SecurityInfoC<HttpURLConnection> si = new SecurityInfoC<HttpURLConnection>(env);
				String mechID;
				if((mechID=env.getProperty(Config.AAF_MECHID))==null) {
					String alias = env.getProperty(Config.CADI_ALIAS);
					if(alias==null) {
						env.init().log(Config.CADI_ALIAS, "is required for AAF Authentication by Certificate.  Alternately, set",Config.AAF_MECHID,"and",Config.AAF_MECHPASS);
						System.exit(1);
					}
					ss = new HX509SS(alias,si,true);
					env.init().log("X509 Certificate Client configured:", alias);
				} else {
					String pass = env.getProperty(Config.AAF_MECHPASS);
					if(pass==null) {
						env.init().log(Config.AAF_MECHPASS, "is required for AAF Authentication by ID/Pass");
						System.exit(1);
					}
					ss = new HBasicAuthSS(mechID,env.decrypt(pass, true),si,true);
					env.init().log("BasicAuth (ID/Pass) Client configured.");
				}
				
				//TODO Reenable Cache Update
	    		//CacheInfoDAO.startUpdate(env, hman, ss, dme2svr.getHostname(), dme2svr.getPort());
	        	
	            while(true) { // Per DME2 Examples...
	            	Thread.sleep(5000);
	            }
	        } catch(DME2Exception e) { // Error binding service doesn't seem to stop DME2 or Process
	            env.init().log(e,"DME2 Initialization Error");
	        	dme2svr.stop();
	        	System.exit(1);
	        } catch(InterruptedException e) {
	            env.init().log("AAF Jetty Server interrupted!");
	        }
    	} else {
    		env.init().log("Properties must contain 'DMEServiceName'");
    	}
	}

	public static void main(String[] args) {
		setup(AuthAPI.class,"authAPI.props");
	}
}
