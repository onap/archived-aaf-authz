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
package org.onap.aaf.authz.gw;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.onap.aaf.authz.env.AuthzEnv;
import org.onap.aaf.authz.gw.api.API_AAFAccess;
import org.onap.aaf.authz.gw.api.API_Api;
import org.onap.aaf.authz.gw.api.API_Find;
import org.onap.aaf.authz.gw.api.API_Proxy;
import org.onap.aaf.authz.gw.api.API_TGuard;
import org.onap.aaf.authz.gw.facade.GwFacade_1_0;
import org.onap.aaf.authz.gw.mapper.Mapper.API;
import org.onap.aaf.authz.server.AbsServer;
import org.onap.aaf.cache.Cache;
import org.onap.aaf.cache.Cache.Dated;
import org.onap.aaf.cssa.rserv.HttpMethods;

import com.att.aft.dme2.api.DME2Exception;

import com.att.aft.dme2.api.DME2Manager;
import com.att.aft.dme2.api.DME2Server;
import com.att.aft.dme2.api.DME2ServerProperties;
import com.att.aft.dme2.api.DME2ServiceHolder;
import com.att.aft.dme2.api.util.DME2FilterHolder;
import com.att.aft.dme2.api.util.DME2FilterHolder.RequestDispatcherType;
import com.att.aft.dme2.api.util.DME2ServletHolder;
import org.onap.aaf.cadi.CadiException;
//import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.v2_0.AAFAuthn;
import org.onap.aaf.cadi.aaf.v2_0.AAFLurPerm;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.inno.env.APIException;

public class GwAPI extends AbsServer {
	private static final String USER_PERMS = "userPerms";
	private GwFacade_1_0 facade; // this is the default Facade
	private GwFacade_1_0 facade_1_0_XML;
	public Map<String, Dated> cacheUser;
	public final String aafurl;
	public final AAFAuthn<HttpURLConnection> aafAuthn;
	public final AAFLurPerm aafLurPerm;
	public DME2Manager dme2Man;

	
	/**
	 * Construct AuthzAPI with all the Context Supporting Routes that Authz needs
	 * 
	 * @param env
	 * @param si 
	 * @param dm 
	 * @param decryptor 
	 * @throws APIException 
	 */
	public GwAPI(AuthzEnv env) throws Exception {
		super(env,"AAF GW");
		aafurl = env.getProperty(Config.AAF_URL); 

		// Setup Logging
		//env.setLog4JNames("log4j.properties","authz","gw","audit","init","trace");

		aafLurPerm = aafCon.newLur();
		// Note: If you need both Authn and Authz construct the following:
		aafAuthn = aafCon.newAuthn(aafLurPerm);

		// Initialize Facade for all uses
		//AuthzTrans trans = env.newTrans();

	//	facade = GwFacadeFactory.v1_0(env,trans,Data.TYPE.JSON);   // Default Facade
	//	facade_1_0_XML = GwFacadeFactory.v1_0(env,trans,Data.TYPE.XML);

		synchronized(env) {
			if(cacheUser == null) {
				cacheUser = Cache.obtain(USER_PERMS);
				//Cache.startCleansing(env, USER_PERMS);
				Cache.addShutdownHook(); // Setup Shutdown Hook to close cache
			}
		}
		
		////////////////////////////////////////////////////////////////////////////
		// Time Critical
		//  These will always be evaluated first
		////////////////////////////////////////////////////////////////////////
		API_AAFAccess.init(this,facade);
		API_Find.init(this, facade);
		API_TGuard.init(this, facade);
		API_Proxy.init(this, facade);
		
		////////////////////////////////////////////////////////////////////////
		// Management APIs
		////////////////////////////////////////////////////////////////////////
		// There are several APIs around each concept, and it gets a bit too
		// long in this class to create.  The initialization of these Management
		// APIs have therefore been pushed to StandAlone Classes with static
		// init functions
		API_Api.init(this, facade);

		////////////////////////////////////////////////////////////////////////
		// Default Function
		////////////////////////////////////////////////////////////////////////
		API_AAFAccess.initDefault(this,facade);

	}
	
	/**
	 * Setup XML and JSON implementations for each supported Version type
	 * 
	 * We do this by taking the Code passed in and creating clones of these with the appropriate Facades and properties
	 * to do Versions and Content switches
	 * 
	 */
	public void route(HttpMethods meth, String path, API api, GwCode code) throws Exception {
		String version = "1.0";
		// Get Correct API Class from Mapper
		Class<?> respCls = facade.mapper().getClass(api); 
		if(respCls==null) throw new Exception("Unknown class associated with " + api.getClass().getName() + ' ' + api.name());
		// setup Application API HTML ContentTypes for JSON and Route
		String application = applicationJSON(respCls, version);
		//route(env,meth,path,code,application,"application/json;version="+version,"*/*");

		// setup Application API HTML ContentTypes for XML and Route
		application = applicationXML(respCls, version);
		//route(env,meth,path,code.clone(facade_1_0_XML,false),application,"text/xml;version="+version);
		
		// Add other Supported APIs here as created
	}
	
	public void routeAll(HttpMethods meth, String path, API api, GwCode code) throws Exception {
		//route(env,meth,path,code,""); // this will always match
	}


	/**
	 * Start up AuthzAPI as DME2 Service
	 * @param env
	 * @param props
	 * @throws DME2Exception
	 * @throws CadiException 
	 */
	public void startDME2(Properties props) throws DME2Exception, CadiException {
		
		dme2Man = new DME2Manager("GatewayDME2Manager",props);

        DME2ServiceHolder svcHolder;
        List<DME2ServletHolder> slist = new ArrayList<DME2ServletHolder>();
        svcHolder = new DME2ServiceHolder();
        String serviceName = env.getProperty("DMEServiceName",null);
    	if(serviceName!=null) {
	    	svcHolder.setServiceURI(serviceName);
	        svcHolder.setManager(dme2Man);
	        svcHolder.setContext("/");
	        
	        
	        
	        DME2ServletHolder srvHolder = new DME2ServletHolder(this, new String[] {"/dme2","/api"});
	        srvHolder.setContextPath("/*");
	        slist.add(srvHolder);
	        
	        EnumSet<RequestDispatcherType> edlist = EnumSet.of(
	        		RequestDispatcherType.REQUEST,
	        		RequestDispatcherType.FORWARD,
	        		RequestDispatcherType.ASYNC
	        		);

	        ///////////////////////
	        // Apply Filters
	        ///////////////////////
	        List<DME2FilterHolder> flist = new ArrayList<DME2FilterHolder>();
	        
	        // Leave Login page un secured
	       // AuthzTransOnlyFilter atof = new AuthzTransOnlyFilter(env);
	      //  flist.add(new DME2FilterHolder(atof,"/login", edlist));

	        // Secure all other interactions with AuthzTransFilter
//	        flist.add(new DME2FilterHolder(
//	        		new AuthzTransFilter(env, aafCon, new AAFTrustChecker(
//	    	        		env.getProperty(Config.CADI_TRUST_PROP, Config.CADI_USER_CHAIN),
//	    	        		Define.ROOT_NS + ".mechid|"+Define.ROOT_COMPANY+"|trust"
//	        			)),
//	        		"/*", edlist));
//	        

	        svcHolder.setFilters(flist);
	        svcHolder.setServletHolders(slist);
	        
	        DME2Server dme2svr = dme2Man.getServer();
//	        dme2svr.setGracefulShutdownTimeMs(1000);
	
	       // env.init().log("Starting GW Jetty/DME2 server...");
	        dme2svr.start();
	        DME2ServerProperties dsprops = dme2svr.getServerProperties();
	        try {
//	        	if(env.getProperty("NO_REGISTER",null)!=null)
	        	dme2Man.bindService(svcHolder);
//	        	env.init().log("DME2 is available as HTTP"+(dsprops.isSslEnable()?"/S":""),"on port:",dsprops.getPort());

	            while(true) { // Per DME2 Examples...
	            	Thread.sleep(5000);
	            }
	        } catch(InterruptedException e) {
	           // env.init().log("AAF Jetty Server interrupted!");
	        } catch(Exception e) { // Error binding service doesn't seem to stop DME2 or Process
	         //   env.init().log(e,"DME2 Initialization Error");
	        	dme2svr.stop();
	        	System.exit(1);
	        }
    	} else {
    		//env.init().log("Properties must contain DMEServiceName");
    	}
	}

	public static void main(String[] args) {
		setup(GwAPI.class,"authGW.props");
	}

//	public void route(PropAccess env, HttpMethods get, String string, GwCode gwCode, String string2, String string3,
//			String string4) {
//		// TODO Auto-generated method stub
//		
//	}

}
