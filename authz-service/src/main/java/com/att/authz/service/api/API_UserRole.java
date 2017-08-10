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

import static com.att.authz.layer.Result.OK;
import static com.att.cssa.rserv.HttpMethods.DELETE;
import static com.att.cssa.rserv.HttpMethods.GET;
import static com.att.cssa.rserv.HttpMethods.POST;
import static com.att.cssa.rserv.HttpMethods.PUT;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.aft.dme2.internal.jetty.http.HttpStatus;
import com.att.authz.env.AuthzTrans;
import com.att.authz.facade.AuthzFacade;
import com.att.authz.layer.Result;
import com.att.authz.service.AuthAPI;
import com.att.authz.service.Code;
import com.att.authz.service.mapper.Mapper.API;

/**
 * User Role APIs
 *
 */
public class API_UserRole {
	/**
	 * Normal Init level APIs
	 * 
	 * @param authzAPI
	 * @param facade
	 * @throws Exception
	 */
	public static void init(final AuthAPI authzAPI, AuthzFacade facade) throws Exception {
		/**
		 * Request User Role Access
		 */
		authzAPI.route(POST,"/authz/userRole",API.USER_ROLE_REQ,new Code(facade,"Request User Role Access", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				Result<Void> r = context.requestUserRole(trans, req, resp);
				switch(r.status) {
					case OK:
						resp.setStatus(HttpStatus.CREATED_201); 
						break;
					default:
						context.error(trans,resp,r);
				}
			}
		});
		
		
		/**
		 * Get UserRoles by Role
		 */
		authzAPI.route(GET,"/authz/userRoles/role/:role",API.USER_ROLES,new Code(facade,"Get UserRoles by Role", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				Result<Void> r = context.getUserRolesByRole(trans, resp, pathParam(req,":role"));
				switch(r.status) {
					case OK:
						resp.setStatus(HttpStatus.OK_200); 
						break;
					default:
						context.error(trans,resp,r);
				}
			}
		});
		
		/**
		 * Get UserRoles by User
		 */
		authzAPI.route(GET,"/authz/userRoles/user/:user",API.USER_ROLES,new Code(facade,"Get UserRoles by User", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				Result<Void> r = context.getUserRolesByUser(trans, resp, pathParam(req,":user"));
				switch(r.status) {
					case OK:
						resp.setStatus(HttpStatus.OK_200); 
						break;
					default:
						context.error(trans,resp,r);
				}
			}
		});

		
		/**
		 * Update roles attached to user in path
		 */
		authzAPI.route(PUT,"/authz/userRole/user",API.USER_ROLE_REQ,new Code(facade,"Update Roles for a user", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				Result<Void> r = context.resetRolesForUser(trans, resp, req);
				switch(r.status) {
					case OK:
						resp.setStatus(HttpStatus.OK_200); 
						break;
					default:
						context.error(trans,resp,r);
				}
			}
		});
		
		
		/**
		 * Update users attached to role in path
		 */
		authzAPI.route(PUT,"/authz/userRole/role",API.USER_ROLE_REQ,new Code(facade,"Update Users for a role", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				Result<Void> r = context.resetUsersForRole(trans, resp, req);
				switch(r.status) {
					case OK:
						resp.setStatus(HttpStatus.OK_200); 
						break;
					default:
						context.error(trans,resp,r);
				}
			}
		});
		
		/**
		 * Extend Expiration Date (according to Organizational rules)
		 */
		authzAPI.route(PUT, "/authz/userRole/extend/:user/:role", API.VOID, new Code(facade,"Extend Expiration", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				Result<Void> r = context.extendUserRoleExpiration(trans,resp,pathParam(req,":user"),pathParam(req,":role"));
				switch(r.status) {
				case OK:
					resp.setStatus(HttpStatus.OK_200); 
					break;
				default:
					context.error(trans,resp,r);
			}
	
			}
			
		});
		
		
		/**
		 * Create a new ID/Credential
		 */
		authzAPI.route(DELETE,"/authz/userRole/:user/:role",API.VOID,new Code(facade,"Delete User Role", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				Result<Void> r = context.deleteUserRole(trans, resp, pathParam(req,":user"),pathParam(req,":role"));
				switch(r.status) {
					case OK:
						resp.setStatus(HttpStatus.OK_200); 
						break;
					default:
						context.error(trans,resp,r);
				}
			}
		});

	}
}
