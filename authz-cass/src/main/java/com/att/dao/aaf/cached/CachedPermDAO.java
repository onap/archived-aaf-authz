/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aai
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * Copyright © 2017 Amdocs
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
package com.att.dao.aaf.cached;

import java.util.List;

import com.att.authz.env.AuthzTrans;
import com.att.authz.layer.Result;
import com.att.dao.CIDAO;
import com.att.dao.CachedDAO;
import com.att.dao.aaf.cass.PermDAO;
import com.att.dao.aaf.cass.PermDAO.Data;
import com.att.dao.aaf.cass.RoleDAO;
import com.att.dao.aaf.cass.Status;

public class CachedPermDAO extends CachedDAO<AuthzTrans,PermDAO, PermDAO.Data> {

	public CachedPermDAO(PermDAO dao, CIDAO<AuthzTrans> info) {
		super(dao, info, PermDAO.CACHE_SEG);
	}

	public Result<List<Data>> readNS(AuthzTrans trans, final String ns) {
		DAOGetter getter = new DAOGetter(trans,dao()) {
			public Result<List<Data>> call() {
				return dao.readNS(trans, ns);
			}
		};
		
		Result<List<Data>> lurd = get(trans, ns, getter);
		if(lurd.isOKhasData()) {
			return lurd;
		} else {
			
		}
//		if(getter.result==null) {
//			if(lurd==null) {
				return Result.err(Status.ERR_PermissionNotFound,"No Permission found - " + lurd.details);
//			} else {
//				return Result.ok(lurd);
//			}
//		}
//		return getter.result;
	}

	public Result<List<Data>> readChildren(AuthzTrans trans, final String ns, final String type) {
		return dao().readChildren(trans,ns,type);
	}

	/**
	 * 
	 * @param trans
	 * @param ns
	 * @param type
	 * @return
	 */
	public Result<List<Data>> readByType(AuthzTrans trans, final String ns, final String type) {
		DAOGetter getter = new DAOGetter(trans,dao()) {
			public Result<List<Data>> call() {
				return dao.readByType(trans, ns, type);
			}
		};
		
		// Note: Can reuse index1 here, because there is no name collision versus response
		Result<List<Data>> lurd = get(trans, ns+'|'+type, getter);
		if(lurd.isOK() && lurd.isEmpty()) {
			return Result.err(Status.ERR_PermissionNotFound,"No Permission found");
		}
		return lurd;
	}
	
	/**
	 * Add desciption to this permission
	 * 
	 * @param trans
	 * @param ns
	 * @param type
	 * @param instance
	 * @param action
	 * @param description
	 * @return
	 */
	public Result<Void> addDescription(AuthzTrans trans, String ns, String type, 
			String instance, String action, String description) {
		//TODO Invalidate?
		return dao().addDescription(trans, ns, type, instance, action, description);
	}
	
	public Result<Void> addRole(AuthzTrans trans, PermDAO.Data perm, RoleDAO.Data role) {
		Result<Void> rv = dao().addRole(trans,perm,role.encode());
		if(trans.debug().isLoggable())
			trans.debug().log("Adding",role.encode(),"to", perm, "with CachedPermDAO.addRole");
		invalidate(trans,perm);
		return rv;
	}

	public Result<Void> delRole(AuthzTrans trans, Data perm, RoleDAO.Data role) {
		Result<Void> rv = dao().delRole(trans,perm,role.encode());
		if(trans.debug().isLoggable())
			trans.debug().log("Removing",role.encode(),"from", perm, "with CachedPermDAO.delRole");
		invalidate(trans,perm);
		return rv;
	}


}
