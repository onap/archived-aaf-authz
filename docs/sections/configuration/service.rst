.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

Service Configuration  - Connecting to AAF
==========================================



Methods to Connect
==================

•	If you are a Servlet in a Container, use CADI Framework with AAF Plugin.  It's very easy, and includes BasicAuth for Services.  
•	Java Technologies
•	Technologies using Servlet Filters
•	DME2 (and other Servlet Containers) can use Servlet Filters
•	Any WebApp can plug in CADI as a Servlet Filter
•	Jetty can attach a Servlet Filter with Code, or as WebApp
•	Tomcat 7 has a "Valve" plugin, which is similar and supported
•	Use the AAFLur Code directly (shown)
•	All Java Technologies utilize Configuration to set what Security elements are required
•	example: Global Login can be turned on/off, AAF Client needs information to connect to AAF Service
•	There are several specialty cases, which AAF can work with, including embedding all properties in a Web.xml, but the essentials needed are:
•	CADI Jars
•	cadi.properties file (configured the same for all technologies)
•	Encrypt passwords with included CADI technology, so that there are no Clear Text Passwords in Config Files (ASPR)
•	See CADI Deployment on how to perform this with several different technologies.
•	AAF Restfully (see RESTFul APIS)

IMPORTANT: If Direct RESTFul API is used, then it is the Client's responsibility to Cache and avoid making an AAF Service Calls too often
Example: A Tool like Cassandra will ask for Authentication hundreds of times a second for the same identity during a transaction.  Calling the AAF Service for each would be slow for the client, and wasteful of Network and AAF Service Capacities.  
Rogue Clients can and will be denied access to AAF.


J2EE (Servlet Filter) Method
============================

1.	Per J2EE design, the Filter will deny any unauthenticated HTTP/S call; the Servlet will not even be invoked.
a.	Therefore, the Servlet can depend on any transaction making it to their code set is Authenticated.
b.	Identity can be viewed based on the HttpServletRequest Object (request.getUserPrincipal() )
2.	Per J2EE design, AAF Filter overloads the HttpServletRequest for a String related to "Role".  (request.isUserInRole("...") )
a.	For AAF, do not put in "Role", but the three parts of requested "Permission", separated by "|", i.e.  "org.onap.aaf.myapp.myperm|myInstance|myAction".
3.	NOT REQUIRED: An added benefit, but not required, is a JASPI like interface, where you can add an Annotation to your Servlet. 
a.	When used, no transaction will come into your code if the listed Permissions are not Granted to the Incoming Transaction.  
b.	This might be helpful for covering separate Management Servlet implementations.



Servlet Code Snippet
=========================

.. code-block:: java

  public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
      HttpServletRequest request;
      try {
          request = (HttpServletRequest)req;
      } catch (ClassCastException e) {
         throw new ServletException("Only serving HTTP today",e);
      }
     
      // Note: CADI is OVERLOADING the concept of "isUserInRole".. You need to think "doesUserHavePermssion()"
      // Assume that you have CREATED and GRANTED An AAF Permission in YOUR Namespace
      // Example Permission:   "org.onap.aaf.myapp.myPerm * write"
 
      // Think in your head, "Does user have write permission on any instance of org.onap.aaf.myapp.myPerm
      if(request.isUserInRole("org.onap.aaf.myapp.myPerm|*|write")) { 
          // *** Do something here that someone with "myPerm write" permissions is allowed to do
      } else {
          // *** Do something reasonable if user is denied, like an Error Message
      }
 
    }

Here is a working TestServlet, where you can play with different Permissions that you own on the URL, i.e.:
https://<your machine:port>/caditest/testme?PERM=org.onap.aaf.myapp.myPerm|*|write

Sample Servlet (Working example)
================================

.. code-block:: java

  package org.onap.aaf.cadi.debug;
  import java.io.FileInputStream;
  import java.io.IOException;
  import java.net.InetAddress;
  import java.net.UnknownHostException;
  import java.util.HashMap;
  import java.util.Map;
  import java.util.Map.Entry;
  import java.util.Properties;
  import javax.servlet.Servlet;
  import javax.servlet.ServletConfig;
  import javax.servlet.ServletException;
  import javax.servlet.ServletRequest;
  import javax.servlet.ServletResponse;
  import javax.servlet.http.HttpServletRequest;
  import org.eclipse.jetty.server.Server;
  import org.eclipse.jetty.server.ServerConnector;
  import org.eclipse.jetty.server.handler.ContextHandler;
  import org.eclipse.jetty.servlet.FilterHolder;
  import org.eclipse.jetty.servlet.FilterMapping;
  import org.eclipse.jetty.servlet.ServletContextHandler;
  import org.eclipse.jetty.servlet.ServletHandler;
  import org.onap.aaf.cadi.filter.CadiFilter;
  import org.onap.aaf.cadi.filter.RolesAllowed;
  import org.onap.aaf.cadi.jetty.MiniJASPIWrap;
 
  public class CSPServletTest {
    public static void main(String[] args) {
        // Go ahead and print Test reports in cadi-core first
        Test.main(args);
        String hostname=null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
        Properties props = new Properties();
        Map<String,String> map = new HashMap<String,String>();
        try {
            FileInputStream fis = new FileInputStream("run/cadi.properties");
            try {
                props.load(fis);
                String key,value;
                for( Entry<Object, Object> es  : props.entrySet()) {
                    key = es.getKey().toString();
                    value = es.getValue().toString();
                    map.put(key,value);
                    if(key.startsWith("AFT_") || key.startsWith("DME2")) {
                        System.setProperty(key,value);
                    }
                }
            } finally {
                fis.close();
            }
        } catch(IOException e) {
            System.err.println("Cannot load run/cadi.properties");
            System.exit(1);
        }
        String portStr = System.getProperty("port");
        int port = portStr==null?8080:Integer.parseInt(portStr);
        try {
            // Add ServletHolder(s) and Filter(s) to a ServletHandler
            ServletHandler shand = new ServletHandler();
             
            FilterHolder cfh = new FilterHolder(CadiFilter.class);
            cfh.setInitParameters(map);
             
            shand.addFilterWithMapping(cfh, "/*", FilterMapping.ALL);
            shand.addServletWithMapping(new MiniJASPIWrap(MyServlet.class),"/*");
            // call initialize after start
             
            ContextHandler ch = new ServletContextHandler();
            ch.setContextPath("/caditest");
            ch.setHandler(shand);
            for( Entry<Object,Object> es : props.entrySet()) {
                ch.getInitParams().put(es.getKey().toString(), es.getValue().toString());
            }
            //ch.setErrorHandler(new MyErrorHandler());
             
            // Create Server and Add Context Handler
            final Server server = new Server();
            ServerConnector http = new ServerConnector(server);
            http.setPort(port);
            server.addConnector(http);
            server.setHandler(ch);
         
            // Start
            server.start();
            shand.initialize();
             
            System.out.println("To test, put http://"+ hostname + ':' + port + "/caditest/testme in a browser or 'curl'");
            // if we were really a server, we'd block the main thread with this join...
            // server.join();
            // But... since we're a test service, we'll block on StdIn
            System.out.println("Press <Return> to end service...");
            System.in.read();
            server.stop();
            System.out.println("All done, have a good day!");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    @RolesAllowed({"org.onap.aaf.myapp.myPerm|myInstance|myAction"})
    public static class MyServlet implements Servlet {
        private ServletConfig servletConfig;
     
        public void init(ServletConfig config) throws ServletException {
            servletConfig = config;
        }
     
        public ServletConfig getServletConfig() {
            return servletConfig;
        }
     
        public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
            HttpServletRequest request;
            try {
                request = (HttpServletRequest)req;
            } catch (ClassCastException e) {
                throw new ServletException("Only serving HTTP today",e);
            }
             
            res.getOutputStream().print("<html><header><title>CSP Servlet Test</title></header><body><h1>You're good to go!</h1><pre>" +
                    request.getUserPrincipal());
             
            String perm = request.getParameter("PERM");
            if(perm!=null)
                if(request.isUserInRole(perm)) {
                    if(perm.indexOf('|')<0) 
                        res.getOutputStream().print("\nCongrats!, You are in Role " + perm);
                      else
                        res.getOutputStream().print("\nCongrats!, You have Permission " + perm);
                } else {
                    if(perm.indexOf('|')<0) 
                        res.getOutputStream().print("\nSorry, you are NOT in Role " + perm);
                      else
                        res.getOutputStream().print("\nSorry, you do NOT have Permission " + perm);
                }
             
            res.getOutputStream().print("</pre></body></html>");
             
        }
     
        public String getServletInfo() {
            return "MyServlet";
        }
     
        public void destroy() {
        }
    }
   }
 
Java Direct (AAFLur) Method
===========================
The AAFLur is the exact component used within all the Plugins mentioned above.  It is written so that it can be called standalone as well, see the Example as follows

.. code-block:: java

  package org.onap.aaf.example;

  import java.util.ArrayList;
  import java.util.List;
  import java.util.Properties;

  import org.onap.aaf.cadi.Access;
  import org.onap.aaf.cadi.Permission;
  import org.onap.aaf.cadi.aaf.v2_0.AAFAuthn;
  import org.onap.aaf.cadi.aaf.v2_0.AAFCon;
  import org.onap.aaf.cadi.aaf.v2_0.AAFLurPerm;
  import org.onap.aaf.cadi.config.Config;
  import org.onap.aaf.cadi.lur.aaf.AAFPermission;
  import org.onap.aaf.cadi.lur.aaf.test.TestAccess;

  public class ExamplePerm2_0 {
	public static void main(String args[]) {
		// Normally, these should be set in environment.  Setting here for clarity
		Properties props = System.getProperties();
		props.setProperty("AFT_LATITUDE", "32.780140");
		props.setProperty("AFT_LONGITUDE", "-96.800451");
		props.setProperty("AFT_ENVIRONMENT", "AFTUAT");
		props.setProperty(Config.AAF_URL,
		"https://DME2RESOLVE/service=org.onap.aaf.authz.AuthorizationService/version=2.0/envContext=TEST/routeOffer=BAU_SE"
				);
		props.setProperty(Config.AAF_USER_EXPIRES,Integer.toString(5*60000));	// 5 minutes for found items to live in cache
		props.setProperty(Config.AAF_HIGH_COUNT,Integer.toString(400));		// Maximum number of items in Cache);
		props.setProperty(Config.CADI_KEYFILE,"keyfile"); //Note: Be sure to generate with java -jar <cadi_path>/lib/cadi-core*.jar keygen keyfile
  //		props.setProperty("DME2_EP_REGISTRY_CLASS","DME2FS");
  //		props.setProperty("AFT_DME2_EP_REGISTRY_FS_DIR","../../authz/dme2reg");

		
		// Link or reuse to your Logging mechanism
		Access myAccess = new TestAccess(); // 
		
		// 
		try {
			AAFCon<?> con = new AAFConDME2(myAccess);
			
			// AAFLur has pool of DME clients as needed, and Caches Client lookups
			AAFLurPerm aafLur = con.newLur();
			// Note: If you need both Authn and Authz construct the following:
			AAFAuthn<?> aafAuthn = con.newAuthn(aafLur);

			// Do not set Mech ID until after you construct AAFAuthn,
			// because we initiate  "401" info to determine the Realm of 
			// of the service we're after.
			con.basicAuth("xxxx@aaf.abc.com", "XXXXXX");

			try {
				
				// Normally, you obtain Principal from Authentication System.
				// For J2EE, you can ask the HttpServletRequest for getUserPrincipal()
				// If you use CADI as Authenticator, it will get you these Principals from
				// CSP or BasicAuth mechanisms.
				String id = "xxxx@aaf.abc.com"; //"cluster_admin@gridcore.abc.com";

				// If Validate succeeds, you will get a Null, otherwise, you will a String for the reason.
				String ok = aafAuthn.validate(id, "XXXXXX");
				if(ok!=null)System.out.println(ok);
				
				ok = aafAuthn.validate(id, "wrongPass");
				if(ok!=null)System.out.println(ok);


				// AAF Style permissions are in the form
				// Type, Instance, Action 
				AAFPermission perm = new AAFPermission("org.onap.aaf.grid.core.coh",":dev_cluster", "WRITE");
				
				// Now you can ask the LUR (Local Representative of the User Repository about Authorization
				// With CADI, in J2EE, you can call isUserInRole("org.onap.aaf.mygroup|mytype|write") on the Request Object 
				// instead of creating your own LUR
				System.out.println("Does " + id + " have " + perm);
				if(aafLur.fish(id, perm)) {
					System.out.println("Yes, you have permission");
				} else {
					System.out.println("No, you don't have permission");
				}

				System.out.println("Does Bogus have " + perm);
				if(aafLur.fish("Bogus", perm)) {
					System.out.println("Yes, you have permission");
				} else {
					System.out.println("No, you don't have permission");
				}

				// Or you can all for all the Permissions available
				List<Permission> perms = new ArrayList<Permission>();
				
				aafLur.fishAll(id,perms);
				for(Permission prm : perms) {
					System.out.println(prm.getKey());
				}
				
				// It might be helpful in some cases to clear the User's identity from the Cache
				aafLur.remove(id);
			} finally {
				aafLur.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
  }

  
There are two current AAF Lurs which you can utilize:
•	Org.onap.aaf.cadi.aaf.v2_0.AAFLurPerm is the default, and will fish based on the Three-fold "Permission" standard in AAF
To run this code, you will need from a SWM deployment (org.onap.aaf.cadi:cadi, then soft link to jars needed):
•	cadi-core-<version>.jar
•	cadi-aaf-<version>-full.jar
   or by Maven
<dependency>
<groupId>org.onap.aaf.cadi</groupId>
<artifactId>aaf-cadi-aaf</artifactId>
<version>THE_LATEST_VERSION</version>
<classifier>full</classifier> 
</dependency>


