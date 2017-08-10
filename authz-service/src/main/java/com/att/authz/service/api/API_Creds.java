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
package com.att.authz.service.api;

import static com.att.cssa.rserv.HttpMethods.DELETE;
import static com.att.cssa.rserv.HttpMethods.GET;
import static com.att.cssa.rserv.HttpMethods.POST;
import static com.att.cssa.rserv.HttpMethods.PUT;

import java.security.Principal;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.aft.dme2.internal.jetty.http.HttpStatus;
import com.att.authz.cadi.DirectAAFUserPass;
import com.att.authz.env.AuthzTrans;
import com.att.authz.facade.AuthzFacade;
import com.att.authz.layer.Result;
import com.att.authz.service.AuthAPI;
import com.att.authz.service.Code;
import com.att.authz.service.mapper.Mapper.API;
import com.att.cadi.CredVal;
import com.att.cadi.Symm;
import com.att.cadi.principal.BasicPrincipal;
import com.att.cadi.principal.X509Principal;
import com.att.cssa.rserv.HttpMethods;
import com.att.inno.env.Env;

/**
 * Initialize All Dispatches related to Credentials (AUTHN)
 *
 */
public class API_Creds {
	// Hide Public Interface
	private API_Creds() {}
	// needed to validate Creds even when already Authenticated x509
	/**
	 * TIME SENSITIVE APIs
	 * 
	 * These will be first in the list
	 * 
	 * @param env
	 * @param authzAPI
	 * @param facade
	 * @param directAAFUserPass 
	 * @throws Exception
	 */
	public static void timeSensitiveInit(Env env, AuthAPI authzAPI, AuthzFacade facade, final DirectAAFUserPass directAAFUserPass) throws Exception {
		/**
		 * Basic Auth, quick Validation
		 * 
		 * Responds OK or NotAuthorized
		 */
		authzAPI.route(env, HttpMethods.GET, "/authn/basicAuth", new Code(facade,"Is given BasicAuth valid?",true) {
			@Override
			public void handle(
					AuthzTrans trans, 
					HttpServletRequest req,
					HttpServletResponse resp) throws Exception {

				Principal p = trans.getUserPrincipal();
				if (p instanceof BasicPrincipal) {
					// the idea is that if call is made with this credential, and it's a BasicPrincipal, it's ok
					// otherwise, it wouldn't have gotten here.
					resp.setStatus(HttpStatus.OK_200);
				} else if (p instanceof X509Principal) {
					// have to check Basic Auth here, because it might be CSP.
					String ba = req.getHeader("Authorization");
					if(ba.startsWith("Basic ")) {
						String decoded = Symm.base64noSplit.decode(ba.substring(6));
						int colon = decoded.indexOf(':');
						if(directAAFUserPass.validate(
								decoded.substring(0,colon), 
								CredVal.Type.PASSWORD , 
								decoded.substring(colon+1).getBytes())) {
							
							resp.setStatus(HttpStatus.OK_200);
						} else {
							resp.setStatus(HttpStatus.FORBIDDEN_403);
						}
					}
				} else if(p == null) {
					trans.error().log("Transaction not Authenticated... no Principal");
					resp.setStatus(HttpStatus.FORBIDDEN_403);
				} else {
					trans.checkpoint("Basic Auth Check Failed: This wasn't a Basic Auth Trans");
					// For Auth Security questions, we don't give any info to client on why failed
					resp.setStatus(HttpStatus.FORBIDDEN_403);
				}
			}
		},"text/plain");
		
		/** 
		 *  returns whether a given Credential is valid
		 */
		authzAPI.route(POST, "/authn/validate", API.CRED_REQ, new Code(facade,"Is given Credential valid?",true) {
			@Override
			public void handle(
					AuthzTrans trans, 
					HttpServletRequest req,
					HttpServletResponse resp) throws Exception {
				
				Result<Date> r = context.doesCredentialMatch(trans, req, resp);
				if(r.isOK()) {
						resp.setStatus(HttpStatus.OK_200);
				} else {
						// For Security, we don't give any info out on why failed, other than forbidden
						resp.setStatus(HttpStatus.FORBIDDEN_403);
				}
			}
		});  

		/** 
		 *  returns whether a given Credential is valid
		 */
		authzAPI.route(GET, "/authn/cert/id/:id", API.CERTS, new Code(facade,"Get Cert Info by ID",true) {
			@Override
			public void handle(
					AuthzTrans trans, 
					HttpServletRequest req,
					HttpServletResponse resp) throws Exception {
				
				Result<Void> r = context.getCertInfoByID(trans, req, resp, pathParam(req,":id") );
				if(r.isOK()) {
						resp.setStatus(HttpStatus.OK_200); 
				} else {
						// For Security, we don't give any info out on why failed, other than forbidden
						resp.setStatus(HttpStatus.FORBIDDEN_403);
				}
			}
		});  




	}
	
	/**
	 * Normal Init level APIs
	 * 
	 * @param authzAPI
	 * @param facade
	 * @throws Exception
	 */
	public static void init(AuthAPI authzAPI, AuthzFacade facade) throws Exception {
		/**
		 * Create a new ID/Credential
		 */
		authzAPI.route(POST,"/authn/cred",API.CRED_REQ,new Code(facade,"Add a New ID/Credential", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				Result<Void> r = context.createUserCred(trans, req);
				if(r.isOK()) {
					resp.setStatus(HttpStatus.CREATED_201);
				} else {
					context.error(trans,resp,r);
				}
			}
		});
		
		/** 
		 *  gets all credentials by Namespace
		 */
		authzAPI.route(GET, "/authn/creds/ns/:ns", API.USERS, new Code(facade,"Get Creds for a Namespace",true) {
			@Override
			public void handle(
					AuthzTrans trans, 
					HttpServletRequest req,
					HttpServletResponse resp) throws Exception {
				
				Result<Void> r = context.getCredsByNS(trans, resp, pathParam(req, "ns"));
				if(r.isOK()) {
					resp.setStatus(HttpStatus.OK_200); 
				} else {
					context.error(trans,resp,r);
				}
			}

		});
		
		/** 
		 *  gets all credentials by ID
		 */
		authzAPI.route(GET, "/authn/creds/id/:id", API.USERS, new Code(facade,"Get Creds by ID",true) {
			@Override
			public void handle(
					AuthzTrans trans, 
					HttpServletRequest req,
					HttpServletResponse resp) throws Exception {
				
				Result<Void> r = context.getCredsByID(trans, resp, pathParam(req, "id"));
				if(r.isOK()) {
					resp.setStatus(HttpStatus.OK_200); 
				} else {
					context.error(trans,resp,r);
				}
			}

		});


		/**
		 * Update ID/Credential (aka reset)
		 */
		authzAPI.route(PUT,"/authn/cred",API.CRED_REQ,new Code(facade,"Update an ID/Credential", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				
				Result<Void> r = context.changeUserCred(trans, req);
				if(r.isOK()) {
					resp.setStatus(HttpStatus.OK_200);
				} else {
					context.error(trans,resp,r);
				}
			}
		});

		/**
		 * Extend ID/Credential
		 * This behavior will accelerate getting out of P1 outages due to ignoring renewal requests, or
		 * other expiration issues.
		 * 
		 * Scenario is that people who are solving Password problems at night, are not necessarily those who
		 * know what the passwords are supposed to be.  Also, changing Password, without changing Configurations
		 * using that password only exacerbates the P1 Issue.
		 */
		authzAPI.route(PUT,"/authn/cred/:days",API.CRED_REQ,new Code(facade,"Extend an ID/Credential", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				Result<Void> r = context.extendUserCred(trans, req, pathParam(req, "days"));
				if(r.isOK()) {
					resp.setStatus(HttpStatus.OK_200);
				} else {
					context.error(trans,resp,r);
				}
			}
		});

		/**
		 * Delete a ID/Credential by Object
		 */
		authzAPI.route(DELETE,"/authn/cred",API.CRED_REQ,new Code(facade,"Delete a Credential", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				Result<Void> r = context.deleteUserCred(trans, req);
				if(r.isOK()) {
					resp.setStatus(HttpStatus.OK_200);
				} else {
					context.error(trans,resp,r);
				}
			}
		});

	}
}
