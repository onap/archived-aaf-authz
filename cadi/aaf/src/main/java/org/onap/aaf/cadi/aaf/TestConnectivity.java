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
import java.util.Map;

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
import org.onap.aaf.cadi.configure.Agent;
import org.onap.aaf.cadi.http.HBasicAuthSS;
import org.onap.aaf.cadi.http.HClient;
import org.onap.aaf.cadi.http.HX509SS;
import org.onap.aaf.cadi.locator.SingleEndpointLocator;
import org.onap.aaf.cadi.oauth.HRenewingTokenSS;
import org.onap.aaf.cadi.util.FixURIinfo;
import org.onap.aaf.misc.env.APIException;

public class TestConnectivity {

    private static Map<String, String> aaf_urls;


    public static void main(String[] args) {
        if (args.length<1) {
            System.out.println("Usage: ConnectivityTester <cadi_prop_files> [<AAF FQDN (i.e. aaf.dev.att.com)>]");
        } else {
            print(true,"START OF CONNECTIVITY TESTS",new Date().toString(),System.getProperty("user.name"),
                    "Note: All API Calls are /authz/perms/user/<AppID/Alias of the caller>");

            if (!args[0].contains(Config.CADI_PROP_FILES+'=')) {
                args[0]=Config.CADI_PROP_FILES+'='+args[0];
            }

            PropAccess access = new PropAccess(args);
            try {
                SecurityInfoC<HttpURLConnection> si = SecurityInfoC.instance(access, HttpURLConnection.class);
                aaf_urls = Agent.loadURLs(access);

                List<SecuritySetter<HttpURLConnection>> lss = loadSetters(access,si);
                /////////
                String directAAFURL = aaf_urls.get(Config.AAF_URL);
                if(directAAFURL!=null && !(directAAFURL.contains("/locate/") || directAAFURL.contains("AAF_LOCATE_URL"))) {
                    print(true,"Test Connections by non-located aaf_url");
                    Locator<URI> locator = new SingleEndpointLocator(directAAFURL);
                    connectTest(locator,new URI(directAAFURL));

                    SecuritySetter<HttpURLConnection> ss = si.defSS;
                    permTest(locator,ss);
                } else {
                    /////////
                    print(true,"Test Connections driven by AAFLocator");
                    String serviceURI = aaf_urls.get(Config.AAF_URL);

                    for (String url : new String[] {
                            serviceURI,
                            aaf_urls.get(Config.AAF_OAUTH2_TOKEN_URL),
                            aaf_urls.get(Config.AAF_OAUTH2_INTROSPECT_URL),
                            aaf_urls.get(Config.AAF_URL_CM),
                            aaf_urls.get(Config.AAF_URL_GUI),
                            aaf_urls.get(Config.AAF_URL_FS),
                            aaf_urls.get(Config.AAF_URL_HELLO)
                    }) {
                        URI uri = new URI(url);
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
                    Locator<URI> locator = new AAFLocator(si,new URI(serviceURI));
                    for (SecuritySetter<HttpURLConnection> ss : lss) {
                        permTest(locator,ss);
                    }

                    //////////
                    print(true,"Test essential BasicAuth Service call, driven by AAFLocator");
                    boolean hasBath=false;
                    for (SecuritySetter<HttpURLConnection> ss : lss) {
                        if (ss instanceof HBasicAuthSS) {
                            hasBath=true;
                            basicAuthTest(new AAFLocator(si, new URI(serviceURI)),ss);
                        }
                    }
                    if(!hasBath) {
                        System.out.println("No User/Password to test");
                    }
                }

            } catch (Exception e) {
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
            if (hbass==null || hbass.getID()==null) {
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
            if (hxss==null || hxss.getID()==null) {
                access.log(Level.INFO, "X509 (Client certificate) Information is not available in configuration, X509 tests will not be conducted... Continuing");
            } else {
                access.log(Level.INFO, "X509 (Client certificate) Information found with ID",hxss.getID(),".  X509 tests will be performed.");
                lss.add(hxss);
            }
        } catch (Exception e) {
            access.log(Level.INFO, "X509 (Client certificate) Security Setter constructor threw exception: \"",e.getMessage(),"\". X509 tests will not be performed");
        }

        String tokenURL = aaf_urls.get(Config.AAF_OAUTH2_TOKEN_URL);

        try {
            HRenewingTokenSS hrtss = new HRenewingTokenSS(access, tokenURL);
            access.log(Level.INFO, "AAF OAUTH2 Information found with ID",hrtss.getID(),".  AAF OAUTH2 tests will be performed.");
            lss.add(hrtss);
        } catch (Exception e) {
            access.log(Level.INFO, "AAF OAUTH2 Security Setter constructor threw exception: \"",e.getMessage(),"\". AAF OAUTH2 tests will not be conducted... Continuing");
        }

        tokenURL = access.getProperty(Config.AAF_ALT_OAUTH2_TOKEN_URL);
        if (tokenURL==null) {
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
        if (strong) {
            for (int i=0;i<70;++i) {
                out.print('=');
            }
            out.println();
        }
        for (String s : args) {
            out.print(strong?"==  ":"------ ");
            out.print(s);
            if (!strong) {
                out.print("  ------");
            }
            out.println();
        }
        if (strong) {
            for (int i=0;i<70;++i) {
                out.print('=');
            }
        }
        out.println();
    }

    private static void connectTest(Locator<URI> dl, URI locatorURI) throws LocatorException {
        URI uri;
        Socket socket;
        print(false,"TCP/IP Connect test to all Located Services for "  + locatorURI.toString() );
        for (Item li = dl.first();li!=null;li=dl.next(li)) {
            if ((uri = dl.get(li)) == null) {
                System.out.println("Locator Item empty");
            } else {
                System.out.printf("Located %s using %s\n",uri.toString(), locatorURI.toString());
                socket = new Socket();
                try {
                    FixURIinfo fui = new FixURIinfo(uri);
                    try {
                        socket.connect(new InetSocketAddress(fui.getHost(),  fui.getPort()),3000);
                        System.out.printf("Can Connect a Socket to %s %d\n",fui.getHost(),fui.getPort());
                    } catch (IOException e) {
                        System.out.printf("Cannot Connect a Socket to  %s %d: %s\n",fui.getHost(),fui.getPort(),e.getMessage());
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
            if (uri==null) {
                System.out.print("No URI available using " + ss.getClass().getSimpleName());
                System.out.println();
                return;
            } else {
                System.out.print("Resolved to: " + uri + " using " + ss.getClass().getSimpleName());
            }
            if (ss instanceof HRenewingTokenSS) {
                System.out.println(" " + ((HRenewingTokenSS)ss).tokenURL());
            } else {
                System.out.println();
            }
            HClient client = new HClient(ss, uri, 3000);
            client.setMethod("GET");
            String user = ss.getID();

            String pathInfo = "/authz/perms/user/"+user;
            client.setPathInfo(pathInfo);
            System.out.println(pathInfo);

            client.send();
            Future<String> future = client.futureReadString();
            if (future.get(7000)) {
                System.out.println(future.body());
            } else {
                if (future.code()==401 && ss instanceof HX509SS) {
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
            if (future.get(7000)) {
                System.out.println("BasicAuth Validated");
            } else {
                System.out.println("Failure " + future.code() + ":" + future.body());
            }
        } catch (CadiException | LocatorException | APIException e) {
            e.printStackTrace();
        }
    }
}
