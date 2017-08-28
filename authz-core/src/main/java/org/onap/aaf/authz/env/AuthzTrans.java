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

import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.inno.env.LogTarget;
import org.onap.aaf.inno.env.TransStore;

public interface AuthzTrans extends TransStore {
	public abstract AuthzTrans set(HttpServletRequest req);

	public abstract void setUser(Principal p);
	
	public abstract String user();

	public abstract Principal getUserPrincipal();

	public abstract String ip();

	public abstract int port();

	public abstract String meth();

	public abstract String path();

	public abstract String agent();
	
	public abstract AuthzEnv env();

	public abstract void setLur(Lur lur);

	public abstract boolean fish(Permission p);
	
	public abstract boolean forceRequested();
	
	public abstract Organization org();

	public abstract boolean moveRequested();

	public abstract boolean futureRequested();
	
	public abstract void logAuditTrail(LogTarget lt);

}
