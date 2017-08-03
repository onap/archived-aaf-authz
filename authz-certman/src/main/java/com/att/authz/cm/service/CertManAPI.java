/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aai
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * Copyright © 2017 Amdocs
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
package com.att.authz.cm.service;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

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
import com.att.authz.cm.api.API_Artifact;
import com.att.authz.cm.api.API_Cert;
import com.att.authz.cm.ca.CA;
import com.att.authz.cm.facade.Facade1_0;
import com.att.authz.cm.facade.FacadeFactory;
import com.att.authz.cm.mapper.Mapper.API;
import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import com.att.authz.env.AuthzTransFilter;
import com.att.authz.server.AbsServer;
import com.att.cache.Cache;
import com.att.cache.Cache.Dated;
import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.CadiException;
import com.att.cadi.TrustChecker;
import com.att.cadi.aaf.v2_0.AAFAuthn;
import com.att.cadi.aaf.v2_0.AAFCon;
import com.att.cadi.aaf.v2_0.AAFConHttp;
import com.att.cadi.aaf.v2_0.AAFLurPerm;
import com.att.cadi.aaf.v2_0.AAFTrustChecker;
import com.att.cadi.config.Config;
import com.att.cssa.rserv.HttpMethods;
import com.att.inno.env.APIException;
import com.att.inno.env.Data;
import com.att.inno.env.Env;
import com.att.inno.env.Trans;
import com.att.inno.env.util.Split;

public class CertManAPI extends AbsServer {

	private static final String USER_PERMS = "userPerms";
	private static final Map<String,CA> certAuths = new TreeMap<String,CA>();
	private static final String AAF_CERTMAN_CA_PREFIX = null;
	public Facade1_0 facade1_0; // this is the default Facade
	public Facade1_0 facade1_0_XML; // this is the XML Facade
	public Map<String, Dated> cacheUser;
	public AAFAuthn<?> aafAuthn;
	public AAFLurPerm aafLurPerm;

	private String[] EMPTY;
	private AAFCon<?> aafcon;
	
	/**
	 * Construct AuthzAPI with all the Context Supporting Routes that Authz needs
	 * 
	 * @param env
	 * @param si 
	 * @param dm 
	 * @param decryptor 
	 * @throws APIException 
	 */
	public CertManAPI(AuthzEnv env) throws Exception {
		super(env,"CertMan");
		env.setLog4JNames("log4j.properties","authz","cm","audit","init","trace");
		
		//aafcon = new AAFConHttp(env);
		
		aafLurPerm = aafcon.newLur();
		// Note: If you need both Authn and Authz construct the following:
		aafAuthn = aafcon.newAuthn(aafLurPerm);

		String aaf_env = env.getProperty(Config.AAF_ENV);
		if(aaf_env==null) {
			throw new APIException("aaf_env needs to be set");
		}
		
		// Initialize Facade for all uses
		AuthzTrans trans = env.newTrans();
		
		// Load Supported Certificate Authorities by property 
		for(String key : env.existingStaticSlotNames()) {
			if(key.startsWith(AAF_CERTMAN_CA_PREFIX)) {
				int idx = key.indexOf('.');
				String[] params = Split.split(';', env.getProperty(key));
				if(params.length>1) {
					@SuppressWarnings("unchecked")
					Class<CA> cac = (Class<CA>)Class.forName((String)params[0]);
					Class<?> ptype[] = new Class<?>[params.length+1];
					ptype[0]=Trans.class;
					ptype[1]=String.class;
					Object pinst[] = new Object[params.length+1];
					pinst[0]=trans;
					pinst[1]= key.substring(idx+1);
					for(int i=1;i<params.length;++i) {
						idx = i+1;
						ptype[idx]=String.class;
						pinst[idx]=params[i];
					}
					Constructor<CA> cons = cac.getConstructor(ptype);
					CA ca = cons.newInstance(pinst);
					certAuths.put(ca.getName(),ca);
				}
			}
		}
		if(certAuths.size()==0) {
			throw new APIException("No Certificate Authorities have been configured in CertMan");
		}
		
		CMService service = new CMService(trans, this);
		// note: Service knows how to shutdown Cluster on Shutdown, etc.  See Constructor
		facade1_0 = FacadeFactory.v1_0(this,trans, service,Data.TYPE.JSON);   // Default Facade
		facade1_0_XML = FacadeFactory.v1_0(this,trans,service,Data.TYPE.XML); 
		

		synchronized(env) {
			if(cacheUser == null) {
				cacheUser = Cache.obtain(USER_PERMS);
				Cache.startCleansing(env, USER_PERMS);
				Cache.addShutdownHook(); // Setup Shutdown Hook to close cache
			}
		}
		
		////////////////////////////////////////////////////////////////////////////
		// APIs
		////////////////////////////////////////////////////////////////////////
		API_Cert.init(this);
		API_Artifact.init(this);
		
		StringBuilder sb = new StringBuilder();
		trans.auditTrail(2, sb);
		trans.init().log(sb);
	}
	
	public CA getCA(String key) {
		return certAuths.get(key);
	}

	public String[] getTrustChain(String key) {
		CA ca = certAuths.get(key);
		if(ca==null) {
			return EMPTY;
		} else {
			return ca.getTrustChain();
		}
	}

	/**
	 * Setup XML and JSON implementations for each supported Version type
	 * 
	 * We do this by taking the Code passed in and creating clones of these with the appropriate Facades and properties
	 * to do Versions and Content switches
	 * 
	 */
	public void route(HttpMethods meth, String path, API api, Code code) throws Exception {
		String version = "1.0";
		// Get Correct API Class from Mapper
		Class<?> respCls = facade1_0.mapper().getClass(api); 
		if(respCls==null) throw new Exception("Unknown class associated with " + api.getClass().getName() + ' ' + api.name());
		// setup Application API HTML ContentTypes for JSON and Route
		String application = applicationJSON(respCls, version);
		route(env,meth,path,code,application,"application/json;version="+version,"*/*");

		// setup Application API HTML ContentTypes for XML and Route
		application = applicationXML(respCls, version);
		route(env,meth,path,code.clone(facade1_0_XML),application,"application/xml;version="+version);
		
		// Add other Supported APIs here as created
	}
	
	public void routeAll(HttpMethods meth, String path, API api, Code code) throws Exception {
		route(env,meth,path,code,""); // this will always match
	}


	/**
	 * Start up AuthzAPI as DME2 Service
	 * @param env
	 * @param props
	 * @throws DME2Exception
	 * @throws CadiException 
	 */
	public void startDME2(Properties props) throws DME2Exception, CadiException {
        DME2Manager dme2 = new DME2Manager("AAF Certman DME2Manager", props);


        DME2ServiceHolder svcHolder;
        List<DME2ServletHolder> slist = new ArrayList<DME2ServletHolder>();
        svcHolder = new DME2ServiceHolder();
        String serviceName = env.getProperty("DMEServiceName",null);
    	if(serviceName!=null) {
	    	svcHolder.setServiceURI(serviceName);
	        svcHolder.setManager(dme2);
	        svcHolder.setContext("/");
	        
	        
	        
	        DME2ServletHolder srvHolder = new DME2ServletHolder(this, new String[]{"/cert"});
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
	        
	        // Secure all GUI interactions with AuthzTransFilter
	        flist.add(new DME2FilterHolder(
	        		new AuthzTransFilter(env,aafcon,TrustChecker.NOTRUST),
	        		"/*", edlist));
	        

	        svcHolder.setFilters(flist);
	        svcHolder.setServletHolders(slist);
	        
	        DME2Server dme2svr = dme2.getServer();
	        DME2ServerProperties dsprops = dme2svr.getServerProperties();
	        dsprops.setGracefulShutdownTimeMs(1000);
	
	        env.init().log("Starting AAF Certman Jetty/DME2 server...");
	        dme2svr.start();
	        try {
//	        	if(env.getProperty("NO_REGISTER",null)!=null)
	        	dme2.bindService(svcHolder);
	        	env.init().log("DME2 is available as HTTP"+(dsprops.isSslEnable()?"/S":""),"on port:",dsprops.getPort());
	            while(true) { // Per DME2 Examples...
	            	Thread.sleep(5000);
	            }
	        } catch(InterruptedException e) {
	            env.init().log("AAF Jetty Server interrupted!");
	        } catch(Exception e) { // Error binding service doesn't seem to stop DME2 or Process
	            env.init().log(e,"DME2 Initialization Error");
	        	dme2svr.stop();
	        	System.exit(1);
	        }
    	} else {
    		env.init().log("Properties must contain DMEServiceName");
    	}
	}

	public static void main(String[] args) {
		setup(CertManAPI.class, "certman.props");

	}

}
