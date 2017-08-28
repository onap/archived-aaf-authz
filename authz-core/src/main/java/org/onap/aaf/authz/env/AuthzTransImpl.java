/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package org.onap.aaf.authz.env;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.authz.org.Organization;
import org.onap.aaf.authz.org.OrganizationFactory;

import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.inno.env.LogTarget;
import org.onap.aaf.inno.env.impl.BasicTrans;

public class AuthzTransImpl extends BasicTrans implements AuthzTrans {
	private static final String TRUE = "true";
	private Principal user;
	private String ip,agent,meth,path;
	private int port;
	private Lur lur;
	private Organization org;
	private String force;
	private boolean futureRequested;

	public AuthzTransImpl(AuthzEnv env) {
		super(env);
		ip="n/a";
		org=null;
	}

	/**
	 * @see org.onap.aaf.authz.env.AuthTrans#set(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public AuthzTrans set(HttpServletRequest req) {
		user = req.getUserPrincipal();
		ip = req.getRemoteAddr();
		port = req.getRemotePort();
		agent = req.getHeader("User-Agent");
		meth = req.getMethod();
		path = req.getPathInfo();
		force = req.getParameter("force");
		futureRequested = TRUE.equalsIgnoreCase(req.getParameter("request"));
		org=null;
		return this;
	}
	
	@Override
	public void setUser(Principal p) {
		user = p;
	}

	/**
	 * @see org.onap.aaf.authz.env.AuthTrans#user()
	 */
	@Override
	public String user() {
		return user==null?"n/a":user.getName();
	}
	
	/**
	 * @see org.onap.aaf.authz.env.AuthTrans#getUserPrincipal()
	 */
	@Override
	public Principal getUserPrincipal() {
		return user;
	}

	/**
	 * @see org.onap.aaf.authz.env.AuthTrans#ip()
	 */
	@Override
	public String ip() {
		return ip;
	}

	/**
	 * @see org.onap.aaf.authz.env.AuthTrans#port()
	 */
	@Override
	public int port() {
		return port;
	}


	/* (non-Javadoc)
	 * @see org.onap.aaf.authz.env.AuthzTrans#meth()
	 */
	@Override
	public String meth() {
		return meth;
	}

	/* (non-Javadoc)
	 * @see org.onap.aaf.authz.env.AuthzTrans#path()
	 */
	@Override
	public String path() {
		return path;
	}

	/**
	 * @see org.onap.aaf.authz.env.AuthTrans#agent()
	 */
	@Override
	public String agent() {
		return agent;
	}

	@Override
	public AuthzEnv env() {
		return (AuthzEnv)delegate;
	}
	
	@Override
	public boolean forceRequested() {
		return TRUE.equalsIgnoreCase(force);
	}
	
	public void forceRequested(boolean force) {
		this.force = force?TRUE:"false";
	}
	
	@Override
	public boolean moveRequested() {
		return "move".equalsIgnoreCase(force);
	}

	@Override
	public boolean futureRequested() {
		return futureRequested;
	}
	

	@Override
	public void setLur(Lur lur) {
		this.lur = lur;
	}
	
	@Override
	public boolean fish(Permission p) {
		if(lur!=null) {
			return lur.fish(user, p);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.onap.aaf.authz.env.AuthzTrans#org()
	 */
	@Override
	public Organization org() {
		if(org==null) {
			try {
				if((org = OrganizationFactory.obtain(env(), user()))==null) {
					org = Organization.NULL;
				}
			} catch (Exception e) {
				org = Organization.NULL;
			}
		} 
		return org;
	}

	/* (non-Javadoc)
	 * @see org.onap.aaf.authz.env.AuthzTrans#logAuditTrailOnly(org.onap.aaf.inno.env.LogTarget)
	 */
	@Override
	public void logAuditTrail(LogTarget lt) {
		if(lt.isLoggable()) {
			StringBuilder sb = new StringBuilder();
			auditTrail(1, sb);
			lt.log(sb);
		}
	}
}
