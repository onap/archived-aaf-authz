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

package org.onap.aaf.auth.cm.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.onap.aaf.auth.cm.AAF_CM;
import org.onap.aaf.auth.cm.mapper.Mapper.API;
import org.onap.aaf.auth.cm.service.Code;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.rserv.HttpMethods;

/**
 * API Deployment Artifact Apis.. using Redirect for mechanism
 *
 * @author Jonathan
 *
 */
public class API_Artifact {
    private static final String GET_ARTIFACTS = "Get Artifacts";
  private static final String CERT_ARTIFACTS = "/cert/artifacts";
    /**
     * Normal Init level APIs
     *
     * @param cmAPI
     * @throws Exception
     */
    public static void init(final AAF_CM cmAPI) throws Exception {
        cmAPI.route(HttpMethods.POST, CERT_ARTIFACTS, API.ARTIFACTS, new Code(cmAPI,"Create Artifacts") {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.createArtifacts(trans, req, resp);
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.CREATED_201);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });

        /**
         * Use Query Params to get Artifacts by Machine or MechID
         */
        cmAPI.route(HttpMethods.GET, CERT_ARTIFACTS, API.ARTIFACTS, new Code(cmAPI,GET_ARTIFACTS) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.readArtifacts(trans, req, resp);
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });


        cmAPI.route(HttpMethods.GET, "/cert/artifacts/:mechid/:machine", API.ARTIFACTS, new Code(cmAPI,GET_ARTIFACTS) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
            
                Result<Void> r = context.readArtifacts(trans, resp, pathParam(req,":mechid"), pathParam(req,":machine"));
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });
    
    
        cmAPI.route(HttpMethods.PUT, CERT_ARTIFACTS, API.ARTIFACTS, new Code(cmAPI,"Update Artifacts") {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.updateArtifacts(trans, req, resp);
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });

        cmAPI.route(HttpMethods.DELETE, "/cert/artifacts/:mechid/:machine", API.VOID, new Code(cmAPI,"Delete Artifacts") {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.deleteArtifacts(trans, resp, 
                        pathParam(req, ":mechid"), pathParam(req,":machine"));
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });
    

        cmAPI.route(HttpMethods.DELETE, CERT_ARTIFACTS, API.VOID, new Code(cmAPI,"Delete Artifacts") {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.deleteArtifacts(trans, req, resp);
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });
    

    }
}
