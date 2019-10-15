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

package org.onap.aaf.sample.cadi.jetty;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ServletHolder;
import org.onap.aaf.cadi.filter.RolesAllowed;



/**
 * MiniJASPIWrap
 *
 * Support the ability to check JASPI Annotation Style Authorizations.
 *
 * This can be a clean way to enforce API Authorization without mistakes in code.
 *
 * @author JonathanGathman
 *
 */
public class MiniJASPIWrap extends ServletHolder {
    private RolesAllowed rolesAllowed;
    //private String roles;
    public MiniJASPIWrap(Class<? extends Servlet> servlet) {
        super(servlet);
        this.rolesAllowed = servlet.getAnnotation(RolesAllowed.class);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if (rolesAllowed!=null) {
            for (String str : rolesAllowed.value()) {
                if (first)first=false;
                else sb.append(',');
                sb.append(str);
            }
        }
        //roles = sb.toString();
    }

    /**
     * handle
     *
     * When utilized, this class authorizes the transaction by first calling the standard J2EE API call
     * "isUserInRole" with the role(s) found in the class Annotations (JASPI Style) 
     */
    @Override
    public void handle(Request baseRequest, ServletRequest request,    ServletResponse response) throws ServletException,    UnavailableException, IOException {
        if (rolesAllowed==null) {
            super.handle(baseRequest, request, response);
        } else { // Validate
            try {
            
                HttpServletRequest hreq = (HttpServletRequest)request;
                boolean proceed = false;
                for (String role : rolesAllowed.value()) {
                    if (hreq.isUserInRole(role)) {
                        proceed = true;
                        break;
                    }
                }
                if (proceed) {
                    super.handle(baseRequest, request, response);
                } else {
                    //baseRequest.getServletContext().log(hreq.getUserPrincipal().getName()+" Refused " + roles);
                    ((HttpServletResponse)response).sendError(403); // forbidden
                }
            } catch (ClassCastException e) {
                throw new ServletException("JASPIWrap only supports HTTPServletRequest/HttpServletResponse");
            }
        }    
    }

}
