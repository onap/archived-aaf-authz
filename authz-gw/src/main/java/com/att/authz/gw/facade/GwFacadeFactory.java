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
package com.att.authz.gw.facade;

import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import com.att.authz.gw.mapper.Mapper_1_0;
import com.att.authz.gw.service.GwServiceImpl;
import com.att.inno.env.APIException;
import com.att.inno.env.Data;

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
