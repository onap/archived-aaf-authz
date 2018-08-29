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

package org.onap.aaf.cadi.aaf;

import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.aaf.v2_0.AAFLocator;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HBasicAuthSS;
import org.onap.aaf.cadi.http.HClient;
import org.onap.aaf.cadi.http.HX509SS;
import org.onap.aaf.cadi.oauth.HRenewingTokenSS;
import org.onap.aaf.misc.env.APIException;

public class TestConnectivity {
	
	public static void main(String[] args) {
		if(args.length<1) {
			System.out.println("Usage: ConnectivityTester <cadi_prop_files> [<AAF FQDN (i.e. aaf.dev.att.com)>]");
		} else {
			print(true,"START OF CONNECTIVITY TESTS",new Date().toString(),System.getProperty("user.name"),
					"Note: All API Calls are /authz/perms/user/<AppID/Alias of the caller>");

			if(!args[0].contains(Config.CADI_PROP_FILES+'=')) {
				args[0]=Config.CADI_PROP_FILES+'='+args[0];
			}

			PropAccess access = new PropAccess(args);
			String aaflocate;
			if(args.length>1) {
				aaflocate = "https://" + args[1];
				access.setProperty(Config.AAF_LOCATE_URL, "https://" + args[1]);
			} else {
				aaflocate = access.getProperty(Config.AAF_LOCATE_URL);
				if(aaflocate==null) {
					print(true,"Properties must contain ",Config.AAF_LOCATE_URL);
				}
			}
			
			try {
				SecurityInfoC<HttpURLConnection> si = SecurityInfoC.instance(access, HttpURLConnection.class);
				
				List<SecuritySetter<HttpURLConnection>> lss = loadSetters(access,si);
				/////////
				print(true,"Test Connections driven by AAFLocator");
				URI serviceURI = new URI(Defaults.AAF_URL);

				for(URI uri : new URI[] {
						serviceURI,
						new URI(Defaults.OAUTH2_TOKEN_URL),
						new URI(Defaults.OAUTH2_INTROSPECT_URL),
						new URI(Defaults.CM_URL),
						new URI(Defaults.GUI_URL),
						new URI(Defaults.FS_URL),
						new URI(Defaults.HELLO_URL)
				}) {
					Locator<URI> locator = new AAFLocator(si, uri);
					try {
						connectTest(locator, uri);
					} catch (Exception e) {
						e.printStackTrace();
						System.err.flush();
					}
				}

				/////////
				print(true,"Test Service for Perms driven by AAFLocator");
				Locator<URI> locator = new AAFLocator(si,serviceURI);
				for(SecuritySetter<HttpURLConnection> ss : lss) {
					permTest(locator,ss);
				}

				//////////
				print(true,"Test essential BasicAuth Service call, driven by AAFLocator");
				for(SecuritySetter<HttpURLConnection> ss : lss) {
					if(ss instanceof HBasicAuthSS) {
						basicAuthTest(new AAFLocator(si, serviceURI),ss);
					}
				}
				
			} catch(Exception e) {
				e.printStackTrace(System.err);
			} finally {
				print(true,"END OF TESTS");
			}
		}
	}
	
	private static List<SecuritySetter<HttpURLConnection>> loadSetters(PropAccess access, SecurityInfoC<HttpURLConnection> si)  {
		print(true,"Load Security Setters from Configuration Information");
		String user = access.getProperty(Config.AAF_APPID);

		ArrayList<SecuritySetter<HttpURLConnection>> lss = new ArrayList<>();
		

		try {
			HBasicAuthSS hbass = new HBasicAuthSS(si,true);
			if(hbass==null || hbass.getID()==null) {
				access.log(Level.INFO, "BasicAuth Information is not available in configuration, BasicAuth tests will not be conducted... Continuing");
			} else {
				access.log(Level.INFO, "BasicAuth Information found with ID",hbass.getID(),".  BasicAuth tests will be performed.");
				lss.add(hbass);
			}
		} catch (Exception e) {
			access.log(Level.INFO, "BasicAuth Security Setter constructor threw exception: \"",e.getMessage(),"\". BasicAuth tests will not be performed");
		}

		try {
			HX509SS hxss = new HX509SS(user,si);
			if(hxss==null || hxss.getID()==null) {
				access.log(Level.INFO, "X509 (Client certificate) Information is not available in configuration, X509 tests will not be conducted... Continuing");
			} else {
				access.log(Level.INFO, "X509 (Client certificate) Information found with ID",hxss.getID(),".  X509 tests will be performed.");
				lss.add(hxss);
			}
		} catch (Exception e) {
			access.log(Level.INFO, "X509 (Client certificate) Security Setter constructor threw exception: \"",e.getMessage(),"\". X509 tests will not be performed");
		}

		String tokenURL = access.getProperty(Config.AAF_OAUTH2_TOKEN_URL);
		String locateURL=access.getProperty(Config.AAF_LOCATE_URL);
		if(tokenURL==null || (tokenURL.contains("/locate/") && locateURL!=null)) {
			tokenURL=Defaults.OAUTH2_TOKEN_URL+"/token";
		}

		try {
			HRenewingTokenSS hrtss = new HRenewingTokenSS(access, tokenURL);
			access.log(Level.INFO, "AAF OAUTH2 Information found with ID",hrtss.getID(),".  AAF OAUTH2 tests will be performed.");
			lss.add(hrtss);
		} catch (Exception e) {
			access.log(Level.INFO, "AAF OAUTH2 Security Setter constructor threw exception: \"",e.getMessage(),"\". AAF OAUTH2 tests will not be conducted... Continuing");
		}
		
		tokenURL = access.getProperty(Config.AAF_ALT_OAUTH2_TOKEN_URL);
		if(tokenURL==null) {
			access.log(Level.INFO, "AAF Alternative OAUTH2 requires",Config.AAF_ALT_OAUTH2_TOKEN_URL, "OAuth2 tests to", tokenURL, "will not be conducted... Continuing");
		} else {
			try {
				HRenewingTokenSS hrtss = new HRenewingTokenSS(access, tokenURL);
				access.log(Level.INFO, "ALT OAUTH2 Information found with ID",hrtss.getID(),".  ALT OAUTH2 tests will be performed.");
				lss.add(hrtss);
			} catch (Exception e) {
				access.log(Level.INFO, "ALT OAUTH2 Security Setter constructor threw exception: \"",e.getMessage(),"\". ALT OAuth2 tests to", tokenURL, " will not be conducted... Continuing");
			}
		}
		
		return lss;
	}

	private static void print(Boolean strong, String ... args) {
		PrintStream out = System.out;
		out.println();
		if(strong) {
			for(int i=0;i<70;++i) {
				out.print('=');
			}
			out.println();
		}
		for(String s : args) {
			out.print(strong?"==  ":"------ ");
			out.print(s);
			if(!strong) {
				out.print("  ------");
			}
			out.println();
		}
		if(strong) {
			for(int i=0;i<70;++i) {
				out.print('=');
			}
		}
		out.println();
	}

	private static void connectTest(Locator<URI> dl, URI locatorURI) throws LocatorException {
		URI uri;
		Socket socket;
		print(false,"TCP/IP Connect test to all Located Services for "  + locatorURI.toString() );
		for(Item li = dl.first();li!=null;li=dl.next(li)) {
			if((uri = dl.get(li)) == null) {
				System.out.println("Locator Item empty");
			} else {
				socket = new Socket();
				try {
					try {
						socket.connect(new InetSocketAddress(uri.getHost(),  uri.getPort()),3000);
						System.out.printf("Can Connect a Socket to %s %d\n",uri.getHost(),uri.getPort());
					} catch (IOException e) {
						System.out.printf("Cannot Connect a Socket to  %s %d: %s\n",uri.getHost(),uri.getPort(),e.getMessage());
					}
				} finally {
					try {
						socket.close();
					} catch (IOException e1) {
						System.out.printf("Could not close Socket Connection: %s\n",e1.getMessage());
					}
				}
			}
		}
	}

	private static void permTest(Locator<URI> dl, SecuritySetter<HttpURLConnection> ss)  {
		try {
			URI uri = dl.get(dl.best());
			if(uri==null) {
				System.out.print("No URI available using " + ss.getClass().getSimpleName());
				System.out.println();
				return;
			} else {
				System.out.print("Resolved to: " + uri + " using " + ss.getClass().getSimpleName());
			}
			if(ss instanceof HRenewingTokenSS) {
				System.out.println(" " + ((HRenewingTokenSS)ss).tokenURL());
			} else {
				System.out.println();
			}
			HClient client = new HClient(ss, uri, 3000);
			client.setMethod("GET");
			String user = ss.getID();
			if(user.indexOf('@')<0) {
				user+="@isam.att.com";
			}
			client.setPathInfo("/authz/perms/user/"+user);
			client.send();
			Future<String> future = client.futureReadString();
			if(future.get(7000)) {
				System.out.println(future.body());	
			} else {
				if(future.code()==401 && ss instanceof HX509SS) {
					System.out.println("  Authentication denied with 401 for Certificate.\n\t"
							+ "This means Certificate isn't valid for this environment, and has attempted another method of Authentication");
				} else {
					System.out.println(future.code() + ":" + future.body());
				}
			}
		} catch (CadiException | LocatorException | APIException e) {
			e.printStackTrace();
		}
	}


	private static void basicAuthTest(Locator<URI> dl, SecuritySetter<HttpURLConnection> ss) {
		try {
			URI uri = dl.get(dl.best());
			System.out.println("Resolved to: " + uri);
			HClient client = new HClient(ss, uri, 3000);
			client.setMethod("GET");
			client.setPathInfo("/authn/basicAuth");
			client.addHeader("Accept", "text/plain");
			client.send();
	
		
			Future<String> future = client.futureReadString();
			if(future.get(7000)) {
				System.out.println("BasicAuth Validated");	
			} else {
				System.out.println("Failure " + future.code() + ":" + future.body());
			}
		} catch (CadiException | LocatorException | APIException e) {
			e.printStackTrace();
		}
	}
}
