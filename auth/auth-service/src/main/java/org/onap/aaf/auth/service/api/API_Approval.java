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

import static org.onap.aaf.auth.rserv.HttpMethods.GET;
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

public class API_Approval {
    // Hide Public Constructor
    private API_Approval() {}

    public static void init(AAF_Service authzAPI, AuthzFacade facade) throws Exception {

        /**
         * Get Approvals by User
         */
        authzAPI.route(GET, "/authz/approval/user/:user",API.APPROVALS,
                new Code(facade,"Get Approvals by User", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.getApprovalsByUser(trans, resp, pathParam(req,"user"));
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });

        /**
         * Get Approvals by Ticket
         */
        authzAPI.route(GET, "/authz/approval/ticket/:ticket",API.APPROVALS,new Code(facade,"Get Approvals by Ticket ", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.getApprovalsByTicket(trans, resp, pathParam(req,"ticket"));
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });

        /**
         * Get Approvals by Approver
         */
        authzAPI.route(GET, "/authz/approval/approver/:approver",API.APPROVALS,new Code(facade,"Get Approvals by Approver", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.getApprovalsByApprover(trans, resp, pathParam(req,"approver"));
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                        context.error(trans,resp,r);
                }
            }
        });


        /**
         * Update an approval
         */
        authzAPI.route(PUT, "/authz/approval",API.APPROVALS,new Code(facade,"Update approvals", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.updateApproval(trans, req, resp);
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });
    }
}
