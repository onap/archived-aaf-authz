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
package com.att.authz.facade;

import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import com.att.authz.service.AuthzCassServiceImpl;
import com.att.authz.service.mapper.Mapper_2_0;
import com.att.dao.aaf.hl.Question;
import com.att.inno.env.APIException;
import com.att.inno.env.Data;


public class AuthzFacadeFactory {
	public static AuthzFacade_2_0 v2_0(AuthzEnv env, AuthzTrans trans, Data.TYPE type, Question question) throws APIException {
		return new AuthzFacade_2_0(env,
				new AuthzCassServiceImpl<
					aaf.v2_0.Nss,
					aaf.v2_0.Perms,
					aaf.v2_0.Pkey,
					aaf.v2_0.Roles,
					aaf.v2_0.Users,
					aaf.v2_0.UserRoles,
					aaf.v2_0.Delgs,
					aaf.v2_0.Certs,
					aaf.v2_0.Keys,
					aaf.v2_0.Request,
					aaf.v2_0.History,
					aaf.v2_0.Error,
					aaf.v2_0.Approvals>
					(trans,new Mapper_2_0(question),question),
				type);
	}
	

}
