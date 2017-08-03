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

import javax.servlet.http.HttpServletResponse;

import com.att.authz.env.AuthzTrans;
import com.att.authz.layer.Result;
import com.att.cssa.rserv.RServlet;


/**
 *   
 *
 */
public interface GwFacade {

/////////////////////  STANDARD ELEMENTS //////////////////
	/** 
	 * @param trans
	 * @param response
	 * @param result
	 */
	void error(AuthzTrans trans, HttpServletResponse response, Result<?> result);

	/**
	 * 
	 * @param trans
	 * @param response
	 * @param status
	 */
	void error(AuthzTrans trans, HttpServletResponse response, int status,	String msg, String ... detail);


	/**
	 * 
	 * @param trans
	 * @param resp
	 * @param rservlet
	 * @return
	 */
	public Result<Void> getAPI(AuthzTrans trans, HttpServletResponse resp, RServlet<AuthzTrans> rservlet);

	/**
	 * 
	 * @param trans
	 * @param resp
	 * @param typeCode
	 * @param optional
	 * @return
	 */
	public abstract Result<Void> getAPIExample(AuthzTrans trans, HttpServletResponse resp, String typeCode, boolean optional);

}
