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
import static com.att.cssa.rserv.HttpMethods.POST;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.aft.dme2.internal.jetty.http.HttpStatus;
import com.att.authz.common.Define;
import com.att.authz.env.AuthzTrans;
import com.att.authz.facade.AuthzFacade;
import com.att.authz.layer.Result;
import com.att.authz.service.AuthAPI;
import com.att.authz.service.Code;
import com.att.authz.service.mapper.Mapper.API;
import com.att.cadi.taf.dos.DenialOfServiceTaf;
import com.att.dao.aaf.cass.Status;
import com.att.dao.aaf.hl.Question;
import com.att.dao.session.SessionFilter;
import com.att.inno.env.Trans;

/**
 * User Role APIs
 *
 */
public class API_Mgmt {

	private static final String SUCCESS = "SUCCESS";

	/**
	 * Normal Init level APIs
	 * 
	 * @param authzAPI
	 * @param facade
	 * @throws Exception
	 */
	public static void init(final AuthAPI authzAPI, AuthzFacade facade) throws Exception {

		/**
		 * Clear Cache Segment
		 */
		authzAPI.route(DELETE,"/mgmt/cache/:area/:segments",API.VOID,new Code(facade,"Clear Cache by Segment", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				Result<Void> r = context.cacheClear(trans, pathParam(req,"area"), pathParam(req,"segments"));
				switch(r.status) {
					case OK:
						trans.checkpoint(SUCCESS,Trans.ALWAYS);
						resp.setStatus(HttpStatus.OK_200); 
						break;
					default:
						context.error(trans,resp,r);
				}
			}
		});
		
		/**
		 * Clear Cache
		 */
		authzAPI.route(DELETE,"/mgmt/cache/:area",API.VOID,new Code(facade,"Clear Cache", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				Result<Void> r;
				String area;
				r = context.cacheClear(trans, area=pathParam(req,"area"));
				switch(r.status) {
					case OK:
						trans.audit().log("Cache " + area + " has been cleared by "+trans.user());
						trans.checkpoint(SUCCESS,Trans.ALWAYS);
						resp.setStatus(HttpStatus.OK_200); 
						break;
					default:
						context.error(trans,resp,r);
				}
			}
		});

		/**
		 * Clear DB Sessions
		 */
		authzAPI.route(DELETE,"/mgmt/dbsession",API.VOID,new Code(facade,"Clear DBSessions", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				try {
					if(req.isUserInRole(Define.ROOT_NS+".db|pool|clear")) {
						SessionFilter.clear();
						context.dbReset(trans);

						trans.audit().log("DB Sessions have been cleared by "+trans.user());

						trans.checkpoint(SUCCESS,Trans.ALWAYS);
						resp.setStatus(HttpStatus.OK_200);
						return;
					}
					context.error(trans,resp,Result.err(Result.ERR_Denied,"%s is not allowed to clear dbsessions",trans.user()));
				} catch(Exception e) {
					trans.error().log(e, "clearing dbsession");
					context.error(trans,resp,Result.err(e));
				}
			}
		});

		/**
		 * Deny an IP 
		 */
		authzAPI.route(POST, "/mgmt/deny/ip/:ip", API.VOID, new Code(facade,"Deny IP",true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				String ip = pathParam(req,":ip");
				if(req.isUserInRole(Define.ROOT_NS+".deny|"+Define.ROOT_COMPANY+"|ip")) {
					if(DenialOfServiceTaf.denyIP(ip)) {
						trans.audit().log(ip+" has been set to deny by "+trans.user());
						trans.checkpoint(SUCCESS,Trans.ALWAYS);

						resp.setStatus(HttpStatus.CREATED_201);
					} else {
						context.error(trans,resp,Result.err(Status.ERR_ConflictAlreadyExists, 
								ip + " is already being denied"));
					}
				} else {
					trans.audit().log(trans.user(),"has attempted to deny",ip,"without authorization");
					context.error(trans,resp,Result.err(Status.ERR_Denied, 
						trans.getUserPrincipal().getName() + " is not allowed to set IP Denial"));
				}
			}
		});
		
		/**
		 * Stop Denying an IP
		 */
		authzAPI.route(DELETE, "/mgmt/deny/ip/:ip", API.VOID, new Code(facade,"Stop Denying IP",true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				String ip = pathParam(req,":ip");
				if(req.isUserInRole(Define.ROOT_NS+".deny|"+Define.ROOT_COMPANY+"|ip")) {
					if(DenialOfServiceTaf.removeDenyIP(ip)) {
						trans.audit().log(ip+" has been removed from denial by "+trans.user());
						trans.checkpoint(SUCCESS,Trans.ALWAYS);
						resp.setStatus(HttpStatus.OK_200);
					} else {
						context.error(trans,resp,Result.err(Status.ERR_NotFound, 
								ip + " is not on the denial list"));
					}
				} else {
					trans.audit().log(trans.user(),"has attempted to remove",ip," from being denied without authorization");
					context.error(trans,resp,Result.err(Status.ERR_Denied, 
						trans.getUserPrincipal().getName() + " is not allowed to remove IP Denial"));
				}
			}
		});

		/**
		 * Deny an ID 
		 */
		authzAPI.route(POST, "/mgmt/deny/id/:id", API.VOID, new Code(facade,"Deny ID",true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				String id = pathParam(req,":id");
				if(req.isUserInRole(Define.ROOT_NS+".deny|"+Define.ROOT_COMPANY+"|id")) {
					if(DenialOfServiceTaf.denyID(id)) {
						trans.audit().log(id+" has been set to deny by "+trans.user());
						trans.checkpoint(SUCCESS,Trans.ALWAYS);
						resp.setStatus(HttpStatus.CREATED_201);
					} else {
						context.error(trans,resp,Result.err(Status.ERR_ConflictAlreadyExists, 
								id + " is already being denied"));
					}
				} else {
					trans.audit().log(trans.user(),"has attempted to deny",id,"without authorization");
					context.error(trans,resp,Result.err(Status.ERR_Denied, 
						trans.getUserPrincipal().getName() + " is not allowed to set ID Denial"));
				}
			}
		});
		
		/**
		 * Stop Denying an ID
		 */
		authzAPI.route(DELETE, "/mgmt/deny/id/:id", API.VOID, new Code(facade,"Stop Denying ID",true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				String id = pathParam(req,":id");
				if(req.isUserInRole(Define.ROOT_NS+".deny|"+Define.ROOT_COMPANY+"|id")) {
					if(DenialOfServiceTaf.removeDenyID(id)) {
						trans.audit().log(id+" has been removed from denial by " + trans.user());
						trans.checkpoint(SUCCESS,Trans.ALWAYS);
						resp.setStatus(HttpStatus.OK_200);
					} else {
						context.error(trans,resp,Result.err(Status.ERR_NotFound, 
								id + " is not on the denial list"));
					}
				} else {
					trans.audit().log(trans.user(),"has attempted to remove",id," from being denied without authorization");
					context.error(trans,resp,Result.err(Status.ERR_Denied, 
						trans.getUserPrincipal().getName() + " is not allowed to remove ID Denial"));
				}
			}
		});

		/**
		 * Deny an ID 
		 */
		authzAPI.route(POST, "/mgmt/log/id/:id", API.VOID, new Code(facade,"Special Log ID",true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				String id = pathParam(req,":id");
				if(req.isUserInRole(Define.ROOT_NS+".log|"+Define.ROOT_COMPANY+"|id")) {
					if(Question.specialLogOn(trans,id)) {
						trans.audit().log(id+" has been set to special Log by "+trans.user());
						trans.checkpoint(SUCCESS,Trans.ALWAYS);
						resp.setStatus(HttpStatus.CREATED_201);
					} else {
						context.error(trans,resp,Result.err(Status.ERR_ConflictAlreadyExists, 
								id + " is already being special Logged"));
					}
				} else {
					trans.audit().log(trans.user(),"has attempted to special Log",id,"without authorization");
					context.error(trans,resp,Result.err(Status.ERR_Denied, 
						trans.getUserPrincipal().getName() + " is not allowed to set ID special Logging"));
				}
			}
		});
		
		/**
		 * Stop Denying an ID
		 */
		authzAPI.route(DELETE, "/mgmt/log/id/:id", API.VOID, new Code(facade,"Stop Special Log ID",true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				String id = pathParam(req,":id");
				if(req.isUserInRole(Define.ROOT_NS+".log|"+Define.ROOT_COMPANY+"|id")) {
					if(Question.specialLogOff(trans,id)) {
						trans.audit().log(id+" has been removed from special Logging by " + trans.user());
						trans.checkpoint(SUCCESS,Trans.ALWAYS);
						resp.setStatus(HttpStatus.OK_200);
					} else {
						context.error(trans,resp,Result.err(Status.ERR_NotFound, 
								id + " is not on the special Logging list"));
					}
				} else {
					trans.audit().log(trans.user(),"has attempted to remove",id," from being special Logged without authorization");
					context.error(trans,resp,Result.err(Status.ERR_Denied, 
						trans.getUserPrincipal().getName() + " is not allowed to remove ID special Logging"));
				}
			}
		});


	}
}
