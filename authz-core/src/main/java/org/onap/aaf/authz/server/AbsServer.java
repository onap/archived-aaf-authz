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
package org.onap.aaf.authz.server;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.onap.aaf.authz.common.Define;
import org.onap.aaf.authz.env.AuthzEnv;
import org.onap.aaf.authz.env.AuthzTrans;
import org.onap.aaf.cssa.rserv.RServlet;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
//import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.v2_0.AAFConHttp;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.http.HTransferSS;
import org.onap.aaf.inno.env.APIException;

public abstract class AbsServer extends RServlet<AuthzTrans> {
	private static final String AAF_API_VERSION = "2.0";
	public final String app;
	public final AuthzEnv env;
	public AAFConHttp aafCon;

    public AbsServer(final AuthzEnv env, final String app) throws CadiException, GeneralSecurityException, IOException {
    	this.env = env;
    	this.app = app;
    	if(env.getProperty(Config.AAF_URL)!=null) {
    		//aafCon = new AAFConHttp(env);
    	}
    }
    
    // This is a method, so we can overload for AAFAPI
    public String aaf_url() {
    	return env.getProperty(Config.AAF_URL);
    }
    
	public abstract void startDME2(Properties props) throws Exception;
	public static void setup(Class<?> abss, String propFile) {

		try {
			// Load Properties from authFramework.properties.  Needed for DME2 and AuthzEnv
			Properties props = new Properties();
			URL rsrc = ClassLoader.getSystemResource(propFile);
			if(rsrc==null) {
				System.err.println("Folder containing " + propFile + " must be on Classpath");
				System.exit(1);
			}

			InputStream is = rsrc.openStream();
			try {
				props.load(is);
			} finally {
				is.close();
				is=null;
			}

			// Load Properties into AuthzEnv
			AuthzEnv env = new AuthzEnv(props);
			// Log where Config found
			env.init().log("Configuring from",rsrc.getPath());
			rsrc = null;
			
			// Print Cipher Suites Available
			if(env.debug().isLoggable()) {
				SSLContext context = SSLContext.getDefault();
				SSLSocketFactory sf = context.getSocketFactory();
				StringBuilder sb = new StringBuilder("Available Cipher Suites: ");
				boolean first = true;
				int count=0;
				for( String cs : sf.getSupportedCipherSuites()) {
					if(first)first = false;
					else sb.append(',');
					sb.append(cs);
					if(++count%4==0){sb.append('\n');}
				}
				env.debug().log(sb);
			}

			// Set ROOT NS, etc
			Define.set(env);

			// Convert CADI properties and Encrypted Passwords for these two properties (if exist) 
			// to DME2 Readable.  Further, Discovery Props are loaded to System if missing.
			// May be causing client errors
			//Config.cadiToDME2(env,props);
			env.init().log("DME2 ServiceName: " + env.getProperty("DMEServiceName","unknown"));

			// Construct with Env
			Constructor<?> cons = abss.getConstructor(new Class<?>[] {AuthzEnv.class});
			// Start DME2 (DME2 needs Properties form of props)
			AbsServer s = (AbsServer)cons.newInstance(env);
			
			// Schedule removal of Clear Text Passwords from System Props (DME2 Requirement) 
//			new Timer("PassRemove").schedule(tt, 120000);
//			tt=null;
			
			s.startDME2(props);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}
	
	public Rcli<?> client() throws CadiException {
		return aafCon.client(AAF_API_VERSION);
	}

	public Rcli<?> clientAsUser(Principal p) throws CadiException {
		return aafCon.client(AAF_API_VERSION).forUser(
				new HTransferSS(p,app, aafCon.securityInfo()));
	}

	public<RET> RET clientAsUser(Principal p,Retryable<RET> retryable) throws APIException, LocatorException, CadiException  {
			return aafCon.hman().best(new HTransferSS(p,app, aafCon.securityInfo()), retryable);
	}

}
