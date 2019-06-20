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

package org.onap.aaf.auth.rserv;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.TransStore;

/**
 * Create a new Transaction Object for each and every incoming Transaction
 * 
 * Attach to Request.  User "FilterHolder" mechanism to retain single instance.
 * 
 * TransFilter includes CADIFilter as part of the package, so that it can
 * set User Data, etc, as necessary.
 * 
 * @author Jonathan
 *
 */
public abstract class TransOnlyFilter<TRANS extends TransStore> implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    


    protected abstract TRANS newTrans(HttpServletRequest req, HttpServletResponse resp);
    protected abstract TimeTaken start(TRANS trans);
    protected abstract void authenticated(TRANS trans, TaggedPrincipal p);
    protected abstract void tallyHo(TRANS trans);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        TRANS trans = newTrans((HttpServletRequest)request,(HttpServletResponse)response);
        TimeTaken overall = start(trans);
        try {
            request.setAttribute(TransFilter.TRANS_TAG, trans);
            chain.doFilter(request, response);
        } finally {
            overall.done();
        }
        tallyHo(trans);
    }

    @Override
    public void destroy() {
    };
}
