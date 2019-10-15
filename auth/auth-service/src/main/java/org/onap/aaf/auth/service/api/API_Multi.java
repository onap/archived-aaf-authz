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
import static org.onap.aaf.auth.rserv.HttpMethods.POST;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.service.AAF_Service;
import org.onap.aaf.auth.service.Code;
import org.onap.aaf.auth.service.facade.AuthzFacade;
import org.onap.aaf.auth.service.mapper.Mapper.API;

public class API_Multi {

    public static void init(AAF_Service authzAPI, AuthzFacade facade) throws Exception {

        authzAPI.route(POST,"/authz/multi",API.VOID, new Code(facade,"Multiple Request API",true) {
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
    }

}
