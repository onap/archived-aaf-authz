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
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.CadiWrap;
import org.onap.aaf.cadi.Connector;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.TrustChecker;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.filter.CadiHTTPManip;
import org.onap.aaf.cadi.taf.TafResp;
import org.onap.aaf.cadi.taf.TafResp.RESP;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.TransStore;
import org.onap.aaf.misc.env.util.Split;

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
public abstract class TransFilter<TRANS extends TransStore> implements Filter {
    public static final String TRANS_TAG = "__TRANS__";
    
    private CadiHTTPManip cadi;

    private final String[] no_authn;
    
    public TransFilter(Access access, Connector con, TrustChecker tc, Object ... additionalTafLurs) throws CadiException, LocatorException {
        cadi = new CadiHTTPManip(access, con, tc, additionalTafLurs);
        String no = access.getProperty(Config.CADI_NOAUTHN, null);
        if (no!=null) {
            no_authn = Split.split(':', no);
        } else {
            no_authn=null;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    
    protected Lur getLur() {
        return cadi.getLur();
    }

    protected abstract TRANS newTrans(HttpServletRequest request);
    protected abstract TimeTaken start(TRANS trans, ServletRequest request);
    protected abstract void authenticated(TRANS trans, Principal p);
    protected abstract void tallyHo(TRANS trans);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;
        
        TRANS trans = newTrans(req);
        
        TimeTaken overall = start(trans,request);
        try {
            request.setAttribute(TRANS_TAG, trans);
            
            if (no_authn!=null) {
                for (String prefix : no_authn) {
                    if (req.getPathInfo().startsWith(prefix)) {
                        chain.doFilter(request, response);
                        return;
                    }
                }
            }

            TimeTaken security = trans.start("CADI Security", Env.SUB);
            TafResp resp;
            RESP r;
            CadiWrap cw = null;
            try {
                resp = cadi.validate(req,res,trans);
                switch(r=resp.isAuthenticated()) {
                    case IS_AUTHENTICATED:
                        cw = new CadiWrap(req,resp,cadi.getLur());
                        authenticated(trans, cw.getUserPrincipal());
                        break;
                    default:
                        break;
                }
            } finally {
                security.done();
            }
            
            if (r==RESP.IS_AUTHENTICATED) {
                trans.checkpoint(resp.desc());
                if (cadi.notCadi(cw, res)) {
                    chain.doFilter(cw, response);
                }
            } else {
                //TODO this is a good place to check if too many checks recently
                // Would need Cached Counter objects that are cleaned up on 
                // use
                trans.checkpoint(resp.desc(),Env.ALWAYS);
                if (resp.isFailedAttempt()) {
                        trans.audit().log(resp.desc());
                }
            }
        } catch (Exception e) {
            trans.error().log(e);
            trans.checkpoint("Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw new ServletException(e);
        } finally {
            overall.done();
            tallyHo(trans);
        }
    }

    @Override
    public void destroy() {
    };
}
