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
package com.att.authz.cm.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.aft.dme2.internal.jetty.http.HttpStatus;
import com.att.authz.cm.ca.CA;
import com.att.authz.cm.mapper.Mapper.API;
import com.att.authz.cm.service.CertManAPI;
import com.att.authz.cm.service.Code;
import com.att.authz.env.AuthzTrans;
import com.att.authz.layer.Result;
import com.att.cssa.rserv.HttpMethods;
import com.att.inno.env.Slot;
import com.att.inno.env.TransStore;

/**
 * API Apis.. using Redirect for mechanism
 * 
 *
 */
public class API_Cert {
	public static final String CERT_AUTH = "CertAuthority";
	private static Slot sCertAuth;

	/**
	 * Normal Init level APIs
	 * 
	 * @param cmAPI
	 * @param facade
	 * @throws Exception
	 */
	public static void init(final CertManAPI cmAPI) throws Exception {
		// Check for Created Certificate Authorities in TRANS
		sCertAuth = ((TransStore) cmAPI.env).slot(CERT_AUTH);
		
		////////
		// Overall APIs
		///////
		cmAPI.route(HttpMethods.PUT,"/cert/:ca",API.CERT_REQ,new Code(cmAPI,"Request Certificate") {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				String key = pathParam(req, ":ca");
				CA ca;
				if((ca = cmAPI.getCA(key))==null) {
					context.error(trans,resp,Result.ERR_BadData,"CA %s is not supported",key);
				} else {
					trans.put(sCertAuth, ca);
					
					Result<Void> r = context.requestCert(trans, req, resp, req.getParameter("withTrust")!=null);
					if(r.isOK()) {
						resp.setStatus(HttpStatus.OK_200);
					} else {
						context.error(trans,resp,r);
					}
				}
			}
		});
		
		/**
		 * 
		 */
		cmAPI.route(HttpMethods.GET, "/cert/may/:perm", API.VOID, new Code(cmAPI,"Check Permission") {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				Result<Void> r = context.check(trans, resp, pathParam(req,"perm"));
				if(r.isOK()) {
					resp.setStatus(HttpStatus.OK_200);
				} else {
					trans.checkpoint(r.errorString());
					context.error(trans,resp,Result.err(Result.ERR_Denied,"%s does not have Permission.",trans.user()));
				}
			}
		});

	}
}
