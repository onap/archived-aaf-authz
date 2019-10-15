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
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.service.AAF_Service;
import org.onap.aaf.auth.service.Code;
import org.onap.aaf.auth.service.facade.AuthzFacade;
import org.onap.aaf.auth.service.mapper.Mapper.API;

public class API_Delegate {
    public static void init(AAF_Service authzAPI, AuthzFacade facade) throws Exception {
        /**
         * Add a delegate
         */
        authzAPI.route(POST, "/authz/delegate",API.DELG_REQ,new Code(facade,"Add a Delegate", true) {

            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.createDelegate(trans, req, resp);
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
         * Update a delegate
         */
        authzAPI.route(PUT, "/authz/delegate",API.DELG_REQ,new Code(facade,"Update a Delegate", true) {

            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.updateDelegate(trans, req, resp);
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
         * DELETE delegates for a user
         */
        authzAPI.route(DELETE, "/authz/delegate",API.DELG_REQ,new Code(facade,"Delete delegates for a user", true) {

            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.deleteDelegate(trans, req, resp);
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
         * DELETE a delegate
         */
        authzAPI.route(DELETE, "/authz/delegate/:user_name",API.VOID,new Code(facade,"Delete a Delegate", true) {

            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.deleteDelegate(trans, pathParam(req, "user_name"));
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
         * Read who is delegating for User
         */
        authzAPI.route(GET, "/authz/delegates/user/:user",API.DELGS,new Code(facade,"Get Delegates by User", true) {

            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.getDelegatesByUser(trans, pathParam(req, "user"), resp);
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
         * Read for whom the User is delegating
         */
        authzAPI.route(GET, "/authz/delegates/delegate/:delegate",API.DELGS,new Code(facade,"Get Delegates by Delegate", true) {

            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.getDelegatesByDelegate(trans, pathParam(req, "delegate"), resp);
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
