/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.service.api;

import static org.onap.aaf.auth.layer.Result.OK;
import static org.onap.aaf.auth.rserv.HttpMethods.DELETE;
import static org.onap.aaf.auth.rserv.HttpMethods.POST;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.service.AAF_Service;
import org.onap.aaf.auth.service.Code;
import org.onap.aaf.auth.service.facade.AuthzFacade;
import org.onap.aaf.auth.service.mapper.Mapper.API;
import org.onap.aaf.cadi.taf.dos.DenialOfServiceTaf;
import org.onap.aaf.misc.env.Trans;

/**
 * User Role APIs
 * @author Jonathan
 *
 */
public class API_Mgmt {

    private static final String SUCCESS = "SUCCESS";
    private final static String PERM_DB_POOL_CLEAR=Define.ROOT_NS()+".db|pool|clear";
    private final static String PERM_DENY_IP = Define.ROOT_NS()+".deny|" + Define.ROOT_COMPANY() + "|ip";
    private final static String PERM_DENY_ID = Define.ROOT_NS()+".deny|" + Define.ROOT_COMPANY() + "|id";
    private final static String PERM_LOG_ID = Define.ROOT_NS()+".log|" + Define.ROOT_COMPANY() + "|id";

    /**
     * Normal Init level APIs
     * <p>
     * @param authzAPI
     * @param facade
     * @throws Exception
     */
    public static void init(final AAF_Service authzAPI, AuthzFacade facade) throws Exception {

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
                    if (req.isUserInRole(PERM_DB_POOL_CLEAR)) {
                        context.dbReset(trans);

                        trans.audit().log("DB Sessions have been cleared by "+trans.user());

                        trans.checkpoint(SUCCESS,Trans.ALWAYS);
                        resp.setStatus(HttpStatus.OK_200);
                        return;
                    }
                    context.error(trans,resp,Result.err(Result.ERR_Denied,"%s is not allowed to clear dbsessions",trans.user()));
                } catch (Exception e) {
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
                if (req.isUserInRole(PERM_DENY_IP)) {
                    if (DenialOfServiceTaf.denyIP(ip)) {
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
                if (req.isUserInRole(PERM_DENY_IP)) {
                    if (DenialOfServiceTaf.removeDenyIP(ip)) {
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
                if (req.isUserInRole(PERM_DENY_ID)) {
                    if (DenialOfServiceTaf.denyID(id)) {
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
                if (req.isUserInRole(PERM_DENY_ID)) {
                    if (DenialOfServiceTaf.removeDenyID(id)) {
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
                if (req.isUserInRole(PERM_LOG_ID)) {
                    if (Question.specialLogOn(trans,id)) {
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
                if (req.isUserInRole(PERM_LOG_ID)) {
                    if (Question.specialLogOff(trans,id)) {
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
