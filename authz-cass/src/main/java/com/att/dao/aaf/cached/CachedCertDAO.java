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
package com.att.dao.aaf.cached;

import java.util.List;

import com.att.authz.env.AuthzTrans;
import com.att.authz.layer.Result;
import com.att.dao.CIDAO;
import com.att.dao.CachedDAO;
import com.att.dao.aaf.cass.CertDAO;

public class CachedCertDAO extends CachedDAO<AuthzTrans, CertDAO, CertDAO.Data> {
	public CachedCertDAO(CertDAO dao, CIDAO<AuthzTrans> info) {
		super(dao, info, CertDAO.CACHE_SEG);
	}
	
	/**
	 * Pass through Cert ID Lookup
	 * 
	 * @param trans
	 * @param ns
	 * @return
	 */
	
	public Result<List<CertDAO.Data>> readID(AuthzTrans trans, final String id) {
		return dao().readID(trans, id);
	}
	
	public Result<List<CertDAO.Data>> readX500(AuthzTrans trans, final String x500) {
		return dao().readX500(trans, x500);
	}


}
