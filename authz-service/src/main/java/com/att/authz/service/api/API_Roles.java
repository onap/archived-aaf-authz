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
import com.att.dao.aaf.cass.Status;

public class API_Roles {
	public static void init(AuthAPI authzAPI, AuthzFacade facade) throws Exception {
		/**
		 * puts a new role in Authz DB
		 */
		authzAPI.route(POST,"/authz/role",API.ROLE_REQ, new Code(facade,"Create Role",true) {
					@Override
					public void handle(
							AuthzTrans trans,
							HttpServletRequest req, 
							HttpServletResponse resp) throws Exception {
						Result<Void> r = context.createRole(trans, req, resp);
							
						switch(r.status) {
							case OK:
								resp.setStatus(HttpStatus.CREATED_201); 
								break;
							case Status.ACC_Future:
								resp.setStatus(HttpStatus.ACCEPTED_202); 
								break;
							default:
								context.error(trans,resp,r);
						}
					}
				}
			);

		/** 
		 *  get Role by name
		 */
		authzAPI.route(GET, "/authz/roles/:role", API.ROLES, new Code(facade,"GetRolesByFullName",true) {
			public void handle(
					AuthzTrans trans, 
					HttpServletRequest req,
					HttpServletResponse resp) throws Exception {
				
				Result<Void> r = context.getRolesByName(trans, resp, pathParam(req, "role"));
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
		 *  gets all Roles by user name
		 */
		authzAPI.route(GET, "/authz/roles/user/:name", API.ROLES, new Code(facade,"GetRolesByUser",true) {
			public void handle(
					AuthzTrans trans, 
					HttpServletRequest req,
					HttpServletResponse resp) throws Exception {
				
				Result<Void> r = context.getRolesByUser(trans, resp, pathParam(req, "name"));
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
		 *  gets all Roles by Namespace
		 */
		authzAPI.route(GET, "/authz/roles/ns/:ns", API.ROLES, new Code(facade,"GetRolesByNS",true) {
			public void handle(
					AuthzTrans trans, 
					HttpServletRequest req,
					HttpServletResponse resp) throws Exception {
				
				Result<Void> r = context.getRolesByNS(trans, resp, pathParam(req, "ns"));
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
		 *  gets all Roles by Name without the Namespace
		 */
		authzAPI.route(GET, "/authz/roles/name/:name", API.ROLES, new Code(facade,"GetRolesByNameOnly",true) {
			public void handle(
					AuthzTrans trans, 
					HttpServletRequest req,
					HttpServletResponse resp) throws Exception {
				Result<Void> r = context.getRolesByNameOnly(trans, resp, pathParam(req, ":name"));
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
		 * Deletes a Role from Authz DB by Object
		 */
		authzAPI.route(DELETE,"/authz/role",API.ROLE_REQ, new Code(facade,"Delete Role",true) {
				@Override
				public void handle(
						AuthzTrans trans,
						HttpServletRequest req, 
						HttpServletResponse resp) throws Exception {
					Result<Void> r = context.deleteRole(trans, req, resp);
					
					switch(r.status) {
						case OK:
							resp.setStatus(HttpStatus.OK_200); 
							break;
						default:
							context.error(trans,resp,r);
					}
				}
			
			}
		);
	

		
		/**
		 * Deletes a Role from Authz DB by Key
		 */
		authzAPI.route(DELETE,"/authz/role/:role",API.ROLE, new Code(facade,"Delete Role",true) {
				@Override
				public void handle(
						AuthzTrans trans,
						HttpServletRequest req, 
						HttpServletResponse resp) throws Exception {
					Result<Void> r = context.deleteRole(trans, resp, pathParam(req,":role"));
						
					switch(r.status) {
						case OK:
							resp.setStatus(HttpStatus.OK_200); 
							break;
						default:
							context.error(trans,resp,r);
					}
				}
			
			}
		);
	

		/**
		 * Add a Permission to a Role (Grant)
		 */
		authzAPI.route(POST,"/authz/role/perm",API.ROLE_PERM_REQ, new Code(facade,"Add Permission to Role",true) {
				@Override
				public void handle(
						AuthzTrans trans,
						HttpServletRequest req, 
						HttpServletResponse resp) throws Exception {
					
					Result<Void> r = context.addPermToRole(trans, req, resp);
						
					switch(r.status) {
						case OK:
							resp.setStatus(HttpStatus.CREATED_201); 
							break;
						default:
							context.error(trans,resp,r);
					}
				}
			}
		);
		
		/**
		 * Get all Roles by Permission
		 */
		authzAPI.route(GET,"/authz/roles/perm/:type/:instance/:action",API.ROLES,new Code(facade,"GetRolesByPerm",true) {
			public void handle(
					AuthzTrans trans, 
					HttpServletRequest req,
					HttpServletResponse resp) throws Exception {
				
				Result<Void> r = context.getRolesByPerm(trans, resp, 
						pathParam(req, "type"),
						pathParam(req, "instance"),
						pathParam(req, "action"));
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
		 * Set a role's description
		 */
		authzAPI.route(PUT,"/authz/role",API.ROLE_REQ,new Code(facade,"Set Description for role",true) {
			@Override
			public void handle(
					AuthzTrans trans, 
					HttpServletRequest req,
					HttpServletResponse resp) throws Exception {
				
				Result<Void> r = context.updateRoleDescription(trans, req, resp);
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
		 * Set a permission's roles to roles given
		 */
		authzAPI.route(PUT,"/authz/role/perm",API.ROLE_PERM_REQ,new Code(facade,"Set a Permission's Roles",true) {
			@Override
			public void handle(
					AuthzTrans trans, 
					HttpServletRequest req,
					HttpServletResponse resp) throws Exception {
				
				Result<Void> r = context.resetPermRoles(trans, req, resp);
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
		 * Delete a Permission from a Role
		 */
		authzAPI.route(DELETE,"/authz/role/:role/perm",API.ROLE_PERM_REQ, new Code(facade,"Delete Permission from Role",true) {
			@Override
			public void handle(
					AuthzTrans trans,
					HttpServletRequest req, 
					HttpServletResponse resp) throws Exception {
				Result<Void> r = context.delPermFromRole(trans, req, resp);
					
				switch(r.status) {
					case OK:
						resp.setStatus(HttpStatus.OK_200); 
						break;
					default:
						context.error(trans,resp,r);
				}
			}
		
		}
	);

	}
}
