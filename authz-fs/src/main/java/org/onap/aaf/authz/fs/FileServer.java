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
package org.onap.aaf.authz.fs;

import static org.onap.aaf.cssa.rserv.HttpMethods.GET;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import org.onap.aaf.authz.env.AuthzEnv;
import org.onap.aaf.authz.env.AuthzTrans;
import org.onap.aaf.authz.env.AuthzTransOnlyFilter;
import org.onap.aaf.cssa.rserv.CachingFileAccess;
import org.onap.aaf.cssa.rserv.RServlet;

import com.att.aft.dme2.api.DME2Manager;
import com.att.aft.dme2.api.DME2Server;
import com.att.aft.dme2.api.DME2ServerProperties;
import com.att.aft.dme2.api.DME2ServiceHolder;
import com.att.aft.dme2.api.util.DME2FilterHolder;
import com.att.aft.dme2.api.util.DME2FilterHolder.RequestDispatcherType;
import com.att.aft.dme2.api.util.DME2ServletHolder;
import org.onap.aaf.inno.env.APIException;


public class FileServer extends RServlet<AuthzTrans>  {
	public FileServer(final AuthzEnv env) throws APIException, IOException {
		try {
			///////////////////////  
			// File Server 
			///////////////////////
			
			CachingFileAccess<AuthzTrans> cfa = new CachingFileAccess<AuthzTrans>(env);
			route(env,GET,"/:key", cfa); 
			route(env,GET,"/:key/:cmd", cfa); 
			///////////////////////
	
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			// Load Properties from authFramework.properties.  Needed for DME2 and AuthzEnv
			Properties props = new Properties();
			URL rsrc = ClassLoader.getSystemResource("FileServer.props");
			if(rsrc==null) {
				System.err.println("Folder containing FileServer.props must be on Classpath");
				System.exit(1);
			}
			InputStream is = rsrc.openStream();
			try {
				props.load(is);
			} finally {
				is.close();
			}
			
			// Load Properties into AuthzEnv
			AuthzEnv env = new AuthzEnv(props); 
			env.setLog4JNames("log4j.properties","authz","fs","audit","init",null);
			
			// AFT Discovery Libraries only read System Props
			env.loadToSystemPropsStartsWith("AFT_","DME2_");
			env.init().log("DME2 using " + env.getProperty("DMEServiceName","unknown") + " URI");
			
			// Start DME2 (DME2 needs Properties form of props)
		    DME2Manager dme2 = new DME2Manager("RServDME2Manager",props);
		    
		    DME2ServiceHolder svcHolder;
		    List<DME2ServletHolder> slist = new ArrayList<DME2ServletHolder>();
		    svcHolder = new DME2ServiceHolder();
		    String serviceName = env.getProperty("DMEServiceName",null);
			if(serviceName!=null) {
		    	svcHolder.setServiceURI(serviceName);
		        svcHolder.setManager(dme2);
		        svcHolder.setContext("/");
		        
		        FileServer fs = new FileServer(env);
		        DME2ServletHolder srvHolder = new DME2ServletHolder(fs);
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
		        
		    	// Need TransFilter
		    	flist.add(new DME2FilterHolder(new AuthzTransOnlyFilter(env),"/*",edlist));
		        svcHolder.setFilters(flist);
		        svcHolder.setServletHolders(slist);
		        
		        DME2Server dme2svr = dme2.getServer();
		        DME2ServerProperties dsprops = dme2svr.getServerProperties();
		        dsprops.setGracefulShutdownTimeMs(1000);

		        env.init().log("Starting AAF FileServer with Jetty/DME2 server...");
		        dme2svr.start();
		        try {
//		        	if(env.getProperty("NO_REGISTER",null)!=null)
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

		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
}
