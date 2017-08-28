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
package org.onap.aaf.authz.gw.facade;

import org.onap.aaf.authz.env.AuthzEnv;
import org.onap.aaf.authz.env.AuthzTrans;
import org.onap.aaf.authz.gw.mapper.Mapper_1_0;
import org.onap.aaf.authz.gw.service.GwServiceImpl;

import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Data;

import gw.v1_0.Error;
import gw.v1_0.InRequest;
import gw.v1_0.Out;


public class GwFacadeFactory {
	public static GwFacade_1_0 v1_0(AuthzEnv env, AuthzTrans trans, Data.TYPE type) throws APIException {
		return new GwFacade_1_0(env,
				new GwServiceImpl<
					InRequest,
					Out,
					Error>(trans,new Mapper_1_0()),
				type);  
	}

}
