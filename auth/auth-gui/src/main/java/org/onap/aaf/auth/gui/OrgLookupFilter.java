/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * <p>
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/
package org.onap.aaf.auth.gui;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.rserv.TransFilter;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.principal.TaggedPrincipal;

public class OrgLookupFilter implements Filter {

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain fc) throws IOException, ServletException {
        final AuthzTrans trans = (AuthzTrans) req.getAttribute(TransFilter.TRANS_TAG);
        if (req instanceof HttpServletRequest) {
            Principal p = ((HttpServletRequest)req).getUserPrincipal();
            if (p instanceof TaggedPrincipal) {
                ((TaggedPrincipal)p).setTagLookup(new TaggedPrincipal.TagLookup() {
                    @Override
                    public String lookup() throws CadiException {
                        Identity id;
                        try {
                            id = trans.org().getIdentity(trans, p.getName());
                            if (id!=null && id.isFound()) {
                                return id.firstName();
                            }
                        } catch (OrganizationException e) {
                            throw new CadiException(e);
                        }
                        return p.getName();
                    }
                });
            }
            fc.doFilter(req, resp);
        }
    
    }


    @Override
    public void destroy() {
    }
}
