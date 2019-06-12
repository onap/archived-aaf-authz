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

package org.onap.aaf.auth.env;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.OrganizationFactory;
import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.impl.BasicTrans;

public class AuthzTransImpl extends BasicTrans implements AuthzTrans {
	private static final String N_A = "n/a";
	private static final String BLANK = "";
	private HttpServletRequest hreq;
    private TaggedPrincipal user;
    private Lur lur;
    private Organization org;
    private int mask;
    private Date now;
    private String tag;
    public AuthzTransImpl(AuthzEnv env) {
        super(env);
        org=null;
        mask=0;
        tag=null;
    }

    /**
     * @see org.onap.aaf.auth.env.test.AuthTrans#set(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public AuthzTrans set(HttpServletRequest req) {
    	hreq = req;
        user = (TaggedPrincipal)req.getUserPrincipal();
        
        for (REQD_TYPE rt : REQD_TYPE.values()) {
            requested(rt,req);
        }
        // Handle alternate "request" for "future"
        String request = req.getParameter("request");
        if (request!=null) {
            requested(REQD_TYPE.future,(request.length()==0 || "true".equalsIgnoreCase(request)));
        }

        org=null;
        return this;
    }
    @Override
    public HttpServletRequest hreq() {
    	return hreq;
    }
    
    @Override
    public void setUser(TaggedPrincipal p) {
        user = p;
    }

    /**
     * @see org.onap.aaf.auth.env.test.AuthTrans#user()
     */
    @Override
    public String user() {
        return user==null?N_A:user.getName();
    }
    
    /**
     * @see org.onap.aaf.auth.env.test.AuthTrans#getUserPrincipal()
     */
    @Override
    public TaggedPrincipal getUserPrincipal() {
        return user;
    }

    /**
     * @see org.onap.aaf.auth.env.test.AuthTrans#ip()
     */
    @Override
    public String ip() {
        return hreq==null?N_A:hreq.getRemoteAddr();
    }

    /**
     * @see org.onap.aaf.auth.env.test.AuthTrans#port()
     */
    @Override
    public int port() {
        return hreq==null?0:hreq.getRemotePort();
    }


    /* (non-Javadoc)
     * @see org.onap.aaf.auth.env.test.AuthzTrans#meth()
     */
    @Override
    public String meth() {
        return hreq==null?"":hreq.getMethod();
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.env.test.AuthzTrans#path()
     */
    @Override
    public String path() {
    	return hreq==null?"":hreq.getPathInfo();
    }

    /**
     * @see org.onap.aaf.auth.env.test.AuthTrans#agent()
     */
    @Override
    public String agent() {
        return hreq==null?BLANK:hreq.getHeader("User-Agent");
    }

    @Override
    public AuthzEnv env() {
        return (AuthzEnv)delegate;
    }
    
    @Override
    public boolean requested(REQD_TYPE requested) {
        return (mask&requested.bit)==requested.bit;
    }
    
    public void requested(REQD_TYPE requested, boolean b) {
        if (b) {
            mask|=requested.bit;
        } else {
            mask&=~requested.bit;
        }
    }
    
    private void requested(REQD_TYPE reqtype, HttpServletRequest req) {
        String p = req.getParameter(reqtype.name());
        if (p!=null) {
            requested(reqtype,p.length()==0 || "true".equalsIgnoreCase(p));
        }
    }

    @Override
    public void setLur(Lur lur) {
        this.lur = lur;
    }
    
    @Override
    public boolean fish(Permission ... pond) {
        if (lur!=null) {
            return lur.fish(user, pond);
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.env.test.AuthzTrans#org()
     */
    @Override
    public Organization org() {
        if (org==null) {
            try {
                if ((org = OrganizationFactory.obtain(env(), user()))==null) {
                    org = Organization.NULL;
                }
            } catch (Exception e) {
                
                org = Organization.NULL;
            }
        } 
        return org;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.env.test.AuthzTrans#logAuditTrailOnly(com.att.inno.env.LogTarget)
     */
    @Override
    public void logAuditTrail(LogTarget lt) {
        if (lt.isLoggable()) {
            StringBuilder sb = new StringBuilder();
            auditTrail(1, sb);
            lt.log(sb);
        }
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.env.test.AuthzTrans#now()
     */
    @Override
    public Date now() {
        if (now==null) {
            now = new Date();
        }
        return now;
    }
    
    /*
     * (non-Javadoc)
     * @see org.onap.aaf.auth.env.AuthzTrans#setTag(java.lang.String)
     */
    @Override
    public void setTag(String tag) {
    	this.tag = tag;
    }

    @Override
    public String getTag() {
    	return tag;
    }
}
