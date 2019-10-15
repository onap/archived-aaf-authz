/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.cadi.enduser.test;

import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.security.Principal;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.enduser.ClientFactory;
import org.onap.aaf.cadi.enduser.RESTException;
import org.onap.aaf.cadi.enduser.SimpleRESTClient;
import org.onap.aaf.cadi.enduser.SimpleRESTClient.Input;
import org.onap.aaf.misc.env.APIException;


public class SimpleRestClientExample {
    public final static void main(final String args[]) throws URISyntaxException, LocatorException {
        try {
            // Note: Expect ClientFactory to be long-lived... do NOT create more than once.
            ClientFactory cf = new ClientFactory(args);
        

            String urlString = cf.getAccess().getProperty("myurl", null);
            if (urlString==null) {
                System.out.println("Note: In your startup, add \"myurl=https://<aaf hello machine>:8130\" to command line\n\t"
                        + "OR\n\t" 
                        + " add -Dmyurl=https://<aaf hello machine>:8130 to VM Args\n\t"
                        + "where \"aaf hello machine\" is an aaf Installation you know about.");
            } else {
            
                SimpleRESTClient restClient = cf.simpleRESTClient(urlString,"org.osaaf.aaf");

                /////////////////////////////////////////////////////////////
                //  
                // Creating Content for CREATE/UPDATE
                //
                /////////////////////////////////////////////////////////////
                // Create an object that can be reusable IN THIS THREAD ONLY... Not Thread-safe on purpose
                Input input = new SimpleRESTClient.Input();
            
                // Note: alternate use is to set the input object to an already created String
                // Input input = new SimpleRESTClient.Input(aString);
            
                PrintWriter pw = input.writer();
                pw.print("{\"something\": [");
                for (int i=0;i<4;++i) {
                    if (i>0) {
                        pw.print(',');
                    }
                    pw.print("{\"myint\":");
                    pw.print(i);
                    pw.print('}');
                }
                pw.println("]}");
            
                // You can check or log the content
                String content = input.toString();
                System.out.println(content);
            
                // Good form for Writers is that you should close it... 
                pw.close();

                /////////////////////////////////////////////////////////////
                //  
                // CREATE/POST
                //
                /////////////////////////////////////////////////////////////
                System.out.println("-------- START REST CREATE/UPDATE --------");
                try {
                    restClient.create("resthello/rest_id", input);
                    // No Error code, it worked.
                    System.out.println("No Error Code, Create worked...");
                } catch (RESTException e) {
                    System.out.println(e.getCode());
                    System.out.println(e.getMsg());
                } finally {
                    System.out.println("-------- END REST CREATE/UPDATE --------");
                }


                /////////////////////////////////////////////////////////////
                //  
                // READ/GET
                //
                /////////////////////////////////////////////////////////////

                // Make some calls.  Note that RESTException is thrown if Call does not complete.
                // RESTException has HTTP Code and any Message sent from Server
                System.out.println("-------- START REST READ/GET --------");
                boolean expectException = false;
                try {
                
                    // Call with no Queries
                    String rv = restClient.get("resthello/rest_id");
                    System.out.println(rv);
                
                    // Same call with "read" style
                    rv = restClient.read("resthello/rest_id");
                    System.out.println(rv);
                
                
                    // Call with Queries
                    rv = restClient.get("resthello/rest_id?perm=org.osaaf.people|*|read");
                    System.out.println(rv);
                
                    // Call setting ID from principal coming from Trans
                    // Pretend Transaction
                    HRequest req = new HRequest("demo@people.osaaf.org"); // Pretend Trans has Jonathan as Identity
                
                    // Call with RESTException, which allows obtaining HTTPCode and any Error message sent
                    rv = restClient.endUser(req.userPrincipal()).get("resthello/rest_id?perm=org.osaaf.people|*|read");
                    System.out.println(rv);

                    // Expect Exception here.
                    System.out.println("-------- START Expecting Exception starting here --------");
                    expectException = true;
                    restClient.get("notAnAPI");
                } catch (RESTException e) {
                    System.out.println(e.getCode());
                    System.out.println(e.getMsg());
                    System.out.println(e.getMessage());
                    System.out.println(e.getLocalizedMessage());
                    System.out.println(e);
                } finally {
                    if (expectException) {
                        System.out.println("-------- END Expecting Exception starting here --------");
                    }
                    System.out.println("-------- END REST READ/GET --------");
                }

                /////////////////////////////////////////////////////////////
                //  
                // UPDATE/PUT
                //
                /////////////////////////////////////////////////////////////

            
                // If you use "input" object again as a writer, you can clear it on the same thread, and go again
                input.clear();
                // Here we just set to a String, instead of Writing
                input.set("{\"something\" : []}");
            
                System.out.println("-------- END REST UPDATE/PUT --------");
                try {
                    String rv = restClient.update("resthello/rest_id", input);
                    // No Error code, it worked.  REST Update will return the updated Data
                    System.out.println("Update worked");
                    System.out.println(rv);
                } catch (RESTException e) {
                    System.out.println(e.getCode());
                    System.out.println(e.getMsg());
                } finally {
                    System.out.println("-------- END REST UPDATE/PUT --------");
                }

                /////////////////////////////////////////////////////////////
                //  
                // DELETE
                //
                /////////////////////////////////////////////////////////////

                System.out.println("-------- START REST DELETE --------");
                try {
                    restClient.delete("resthello/rest_id");
                    // No Error code, it worked.  REST Update will return the updated Data
                    System.out.println("Delete worked");
                } catch (RESTException e) {
                    System.out.println(e.getCode());
                    System.out.println(e.getMsg());
                } finally {
                    System.out.println("-------- END REST DELETE --------");
                }
            }        
        } catch (CadiException | APIException e) {
            e.printStackTrace();
        }
    }

    private static class HRequest { 
    
        public HRequest(String fqi) {
            name = fqi;
        }
        protected final String name;

    // fake out HttpServletRequest, only for get Principal
        public Principal userPrincipal() {
            return new Principal() {

                @Override
                public String getName() {
                    return name;
                }
            
            };
        }
    }
}
