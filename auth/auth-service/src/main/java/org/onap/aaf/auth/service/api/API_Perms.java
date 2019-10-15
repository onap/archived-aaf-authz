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
import static org.onap.aaf.auth.rserv.HttpMethods.GET;
import static org.onap.aaf.auth.rserv.HttpMethods.POST;
import static org.onap.aaf.auth.rserv.HttpMethods.PUT;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.service.AAF_Service;
import org.onap.aaf.auth.service.Code;
import org.onap.aaf.auth.service.facade.AuthzFacade;
import org.onap.aaf.auth.service.mapper.Mapper.API;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.util.Split;

public class API_Perms {
    public static void timeSensitiveInit(AAF_Service authzAPI, AuthzFacade facade) throws Exception {
        /** 
         *  gets all permissions by user name
         */
        authzAPI.route(GET, "/authz/perms/user/:user", API.PERMS, new Code(facade,"Get Permissions by User",true) {
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
            
                String scopes = req.getParameter("scopes");
                Result<Void> r;
                if (scopes==null) {
                    r = context.getPermsByUser(trans, resp, pathParam(req, "user"));
                } else {
                    r = context.getPermsByUserScope(trans, resp, pathParam(req, "user"),Split.split(':', scopes));
                }
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
         *  gets all permissions by user name
         */
        authzAPI.route(POST, "/authz/perms/user/:user", API.PERMS, new Code(facade,"Get Permissions by User, Query AAF Perms",true) {
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
            
                Result<Void> r = context.getPermsByUserWithAAFQuery(trans, req, resp, pathParam(req, "user"));
                switch(r.status) {
                    case OK: 
                        resp.setStatus(HttpStatus.OK_200); 
                        break;
                    default:
                        context.error(trans,resp,r);
                }
            }

        });


    } // end timeSensitiveInit

    public static void init(AAF_Service authzAPI, AuthzFacade facade) throws Exception {
        /**
         * Create a Permission
         */
        authzAPI.route(POST,"/authz/perm",API.PERM_REQ,new Code(facade,"Create a Permission",true) {
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
            
                Result<Void> r = context.createPerm(trans, req, resp);
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
         *  get details of Permission
         */
        authzAPI.route(GET, "/authz/perms/:type/:instance/:action", API.PERMS, new Code(facade,"Get Permissions by Key",true) {
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
            
                Result<Void> r = context.getPermsByName(trans, resp, 
                        pathParam(req, "type"),
                        URLDecoder.decode(pathParam(req, "instance"),Config.UTF_8),
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
         *  get children of Permission
         */
        authzAPI.route(GET, "/authz/perms/:type", API.PERMS, new Code(facade,"Get Permissions by Type",true) {
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
            
                Result<Void> r = context.getPermsByType(trans, resp, pathParam(req, "type"));
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
         * gets all permissions by role name
         */
        authzAPI.route(GET,"/authz/perms/role/:role",API.PERMS,new Code(facade,"Get Permissions by Role",true) {
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
            
                Result<Void> r = context.getPermsForRole(trans, resp, pathParam(req, "role"));
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
         * gets all permissions by Namespace
         */
        authzAPI.route(GET,"/authz/perms/ns/:ns",API.PERMS,new Code(facade,"Get PermsByNS",true) {
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
            
                Result<Void> r = context.getPermsByNS(trans, resp, pathParam(req, "ns"));
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
         * Set a perm's description
         */
        authzAPI.route(PUT,"/authz/perm",API.PERM_REQ,new Code(facade,"Set Description for Permission",true) {
            @Override
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
            
                Result<Void> r = context.updatePermDescription(trans, req, resp);
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
         * Update a permission with a rename
         */
        authzAPI.route(PUT,"/authz/perm/:type/:instance/:action",API.PERM_REQ,new Code(facade,"Update a Permission",true) {
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
            
                Result<Void> r = context.renamePerm(trans, req, resp, 
                        pathParam(req, "type"), 
                        URLDecoder.decode(pathParam(req, "instance"),Config.UTF_8), 
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
         * Delete a Permission
         */
        authzAPI.route(DELETE,"/authz/perm",API.PERM_REQ,new Code(facade,"Delete a Permission",true) {
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
            
                Result<Void> r = context.deletePerm(trans,req, resp);
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
         * Delete a Permission
         */
        authzAPI.route(DELETE,"/authz/perm/:name/:type/:action",API.PERM_KEY,new Code(facade,"Delete a Permission",true) {
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
            
                Result<Void> r = context.deletePerm(trans, resp,
                        pathParam(req, ":name"),
                        pathParam(req, ":type"),
                        pathParam(req, ":action"));
                switch(r.status) {
                    case OK: 
                        resp.setStatus(HttpStatus.OK_200); 
                        break;
                    default:
                        context.error(trans,resp,r);
                }
            }
        });

    } // end init
}



