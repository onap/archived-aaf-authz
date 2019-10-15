/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.onap.aaf.auth.dao.cass.NsType;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.service.AAF_Service;
import org.onap.aaf.auth.service.Code;
import org.onap.aaf.auth.service.facade.AuthzFacade;
import org.onap.aaf.auth.service.mapper.Mapper.API;

public class API_NS {
    private static final String FULL = "full";
    private static final String TRUE = "true";

    public static void init(AAF_Service authzAPI, AuthzFacade facade) throws Exception {
        /**
         * puts a new Namespace in Authz DB
         *
         * TESTCASES: TC_NS1, TC_NSdelete1
         */
        authzAPI.route(POST,"/authz/ns",API.NS_REQ, new Code(facade,"Create a Namespace",true) {
                    @Override
                    public void handle(
                            AuthzTrans trans,
                            HttpServletRequest req,
                            HttpServletResponse resp) throws Exception {
                        NsType nst = NsType.fromString(req.getParameter("type"));
                        Result<Void> r = context.requestNS(trans, req, resp,nst);

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
         * removes a Namespace from Authz DB
         *
         * TESTCASES: TC_NS1, TC_NSdelete1
         */
        authzAPI.route(DELETE,"/authz/ns/:ns",API.VOID, new Code(facade,"Delete a Namespace",true) {
                @Override
                public void handle(
                        AuthzTrans trans,
                        HttpServletRequest req,
                        HttpServletResponse resp) throws Exception {
                    Result<Void> r = context.deleteNS(trans, req, resp, pathParam(req,":ns"));
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
         * Add an Admin in NS in Authz DB
         *
         * TESTCASES: TC_NS1
         */
        authzAPI.route(POST,"/authz/ns/:ns/admin/:id",API.VOID, new Code(facade,"Add an Admin to a Namespace",true) {
            @Override
            public void handle(
                AuthzTrans trans,
                HttpServletRequest req,
                HttpServletResponse resp) throws Exception {
                Result<Void> r = context.addAdminToNS(trans, resp, pathParam(req,":ns"), pathParam(req,":id"));
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
         * Removes an Admin from Namespace in Authz DB
         *
         * TESTCASES: TC_NS1
         */
        authzAPI.route(DELETE,"/authz/ns/:ns/admin/:id",API.VOID, new Code(facade,"Remove an Admin from a Namespace",true) {
            @Override
            public void handle(
                AuthzTrans trans,
                HttpServletRequest req,
                HttpServletResponse resp) throws Exception {
                    Result<Void> r = context.delAdminFromNS(trans, resp, pathParam(req,":ns"), pathParam(req,":id"));
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
     * Add an Admin in NS in Authz DB
     *
     * TESTCASES: TC_NS1
     */
        authzAPI.route(POST,"/authz/ns/:ns/responsible/:id",API.VOID, new Code(facade,"Add a Responsible Identity to a Namespace",true) {
            @Override
            public void handle(
                AuthzTrans trans,
                HttpServletRequest req,
                HttpServletResponse resp) throws Exception {
                Result<Void> r = context.addResponsibilityForNS(trans, resp, pathParam(req,":ns"), pathParam(req,":id"));
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
         *
         */
        authzAPI.route(GET,"/authz/nss/:id",API.NSS, new Code(facade,"Return Information about Namespaces", true) {
            @Override
            public void handle(
                AuthzTrans trans,
                HttpServletRequest req,
                HttpServletResponse resp) throws Exception {
                    Result<Void> r = context.getNSsByName(trans, resp, pathParam(req,":id"),TRUE.equals(req.getParameter(FULL)));
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
         * Get all Namespaces where user is an admin
         */
        authzAPI.route(GET,"/authz/nss/admin/:user",API.NSS, new Code(facade,"Return Namespaces where User is an Admin", true) {
            @Override
            public void handle(
                AuthzTrans trans,
                HttpServletRequest req,
                HttpServletResponse resp) throws Exception {
                    Result<Void> r = context.getNSsByAdmin(trans, resp, pathParam(req,":user"),TRUE.equals(req.getParameter(FULL)));
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
         * Get all Namespaces where user is a responsible party
         */
        authzAPI.route(GET,"/authz/nss/responsible/:user",API.NSS, new Code(facade,"Return Namespaces where User is Responsible", true) {
            @Override
            public void handle(
                AuthzTrans trans,
                HttpServletRequest req,
                HttpServletResponse resp) throws Exception {
                    Result<Void> r = context.getNSsByResponsible(trans, resp, pathParam(req,":user"),TRUE.equals(req.getParameter(FULL)));
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
         * Get all Namespaces where user is an admin or owner
         */
        authzAPI.route(GET,"/authz/nss/either/:user",API.NSS, new Code(facade,"Return Namespaces where User Admin or Owner", true) {
            @Override
            public void handle(
                AuthzTrans trans,
                HttpServletRequest req,
                HttpServletResponse resp) throws Exception {
                    Result<Void> r = context.getNSsByEither(trans, resp, pathParam(req,":user"),TRUE.equals(req.getParameter(FULL)));
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
         * Get all children Namespaces
         */
        authzAPI.route(GET,"/authz/nss/children/:id",API.NSS, new Code(facade,"Return Child Namespaces", true) {
            @Override
            public void handle(
                AuthzTrans trans,
                HttpServletRequest req,
                HttpServletResponse resp) throws Exception {
                    Result<Void> r = context.getNSsChildren(trans, resp, pathParam(req,":id"));
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
         * Set a description of a Namespace
         */
        authzAPI.route(PUT,"/authz/ns",API.NS_REQ,new Code(facade,"Set a Description for a Namespace",true) {
            @Override
            public void handle(
                    AuthzTrans trans,
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {

                Result<Void> r = context.updateNsDescription(trans, req, resp);
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
         * Removes an Owner from Namespace in Authz DB
         *
         * TESTCASES: TC_NS1
         */
        authzAPI.route(DELETE,"/authz/ns/:ns/responsible/:id",API.VOID, new Code(facade,"Remove a Responsible Identity from Namespace",true) {
            @Override
            public void handle(
                AuthzTrans trans,
                HttpServletRequest req,
                HttpServletResponse resp) throws Exception {
                    Result<Void> r = context.delResponsibilityForNS(trans, resp, pathParam(req,":ns"), pathParam(req,":id"));
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

        authzAPI.route(POST,"/authz/ns/:ns/attrib/:key/:value",API.VOID, new Code(facade,"Add an Attribute from a Namespace",true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                    Result<Void> r = context.createAttribForNS(trans, resp,
                        pathParam(req,":ns"),
                        pathParam(req,":key"),
                        pathParam(req,":value"));
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

        authzAPI.route(GET,"/authz/ns/attrib/:key",API.KEYS, new Code(facade,"get Ns Key List From Attribute",true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                    Result<Void> r = context.readNsByAttrib(trans, resp, pathParam(req,":key"));
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

        authzAPI.route(PUT,"/authz/ns/:ns/attrib/:key/:value",API.VOID, new Code(facade,"update an Attribute from a Namespace",true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                    Result<Void> r = context.updAttribForNS(trans, resp,
                        pathParam(req,":ns"),
                        pathParam(req,":key"),
                        pathParam(req,":value"));
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

        authzAPI.route(DELETE,"/authz/ns/:ns/attrib/:key",API.VOID, new Code(facade,"delete an Attribute from a Namespace",true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                    Result<Void> r = context.delAttribForNS(trans, resp,
                        pathParam(req,":ns"),
                        pathParam(req,":key"));
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
