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

package org.onap.aaf.auth.oauth;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.cadi.principal.BearerPrincipal;
import org.onap.aaf.cadi.util.Split;

public class OAuth2Filter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest hreq = (HttpServletRequest)request;
        Principal p = hreq.getUserPrincipal();
        if (request.getContentType().equals("application/x-www-form-urlencoded")) {

        } else if (p instanceof BearerPrincipal) {
            for (String authz : Split.splitTrim(';', hreq.getHeader("Authorization"))) {
                if (authz.startsWith("Bearer ")) {
                    ((BearerPrincipal)p).setBearer(authz.substring(7));
                }
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
