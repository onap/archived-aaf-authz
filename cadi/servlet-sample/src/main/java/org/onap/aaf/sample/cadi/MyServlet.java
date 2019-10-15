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

package org.onap.aaf.sample.cadi;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.cadi.aaf.v2_0.AAFCon;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.principal.TaggedPrincipal;

// Uncomment if you utilized the "MiniJASPIWrap" in the Servlet setup in "main()", and want to protect your service via Permission or mapped role
//    @RolesAllowed({"com.att.aaf.myPerm|myInstance|myAction"})
    public class MyServlet implements Servlet {
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
        
            res.getOutputStream().println("<html><header><title>CSP Servlet Test</title></header><body><h1>You're good to go!</h1><pre>" +
                    request.getUserPrincipal());
        
            String perm = request.getParameter("PERM");
            if (perm!=null) {
                if (request.isUserInRole(perm)) {
                    if (perm.indexOf('|')<0) { 
                        res.getOutputStream().println("\nCongrats!, You are in Role " + perm);
                    } else { 
                        res.getOutputStream().println("\nCongrats!, You have Permission " + perm);
                    }
                } else {
                    if (perm.indexOf('|')<0) { 
                        res.getOutputStream().println("\nSorry, you are NOT in Role " + perm);
                    } else {
                        res.getOutputStream().println("\nSorry, you do NOT have Permission " + perm);
                    }
                }
            }
        
            // You can get the working AAFCon from Trans
            AAFCon<?> aafcon = AAFCon.obtain(req);
            if (aafcon!=null) {
                try {
                    res.getOutputStream().println("----- Perms JSON from direct call -----");
                    final Principal up = request.getUserPrincipal();
                    TaggedPrincipal tp;
                    if (up instanceof TaggedPrincipal) {
                        tp = (TaggedPrincipal)up;
                    } else {
                        tp = new TaggedPrincipal() {
                            @Override
                            public String getName() {
                                return up.getName();
                            }

                            @Override
                            public String tag() {
                                return "Unknown";
                            }
                        };
                    }
                    // This call will be "as the user calling", but only if permission is set to trust.
//                    Future<String> future = aafcon.clientAs(Config.AAF_DEFAULT_API_VERSION,tp).read("/authz/perms/user/"+request.getUserPrincipal().getName(),"application/Perms+json");
                    Future<String> future = aafcon.client(Config.AAF_DEFAULT_API_VERSION).read("/authz/perms/user/"+request.getUserPrincipal().getName(),"application/Perms+json");
                    if (future.get(4000 /* timeout */)) {
                        res.getOutputStream().print(future.value);
                    } else {
                        System.err.println(future.code() + ", " + future.body());
                        res.getOutputStream().print(future.code() + ", " + future.body());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                res.getOutputStream().println("No AAFCon instantiated");
            }
            res.getOutputStream().print("</pre></body></html>");
        
        }

        public String getServletInfo() {
            return "MyServlet";
        }

        public void destroy() {
        }
    }