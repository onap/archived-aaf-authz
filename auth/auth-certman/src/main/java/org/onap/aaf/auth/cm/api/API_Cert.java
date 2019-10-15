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

package org.onap.aaf.auth.cm.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.onap.aaf.auth.cm.AAF_CM;
import org.onap.aaf.auth.cm.ca.CA;
import org.onap.aaf.auth.cm.mapper.Mapper.API;
import org.onap.aaf.auth.cm.service.Code;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.misc.env.Slot;

/**
 * API Apis.. using Redirect for mechanism
 * <p>
 * @author Jonathan
 *
 */
public class API_Cert {
    public static final String CERT_AUTH = "CertAuthority";
    private static Slot sCertAuth;

    /**
     * Normal Init level APIs
     * <p>
     * @param aafCM
     * @param facade
     * @throws Exception
     */
    public static void init(final AAF_CM aafCM) throws Exception {
        // Check for Created Certificate Authorities in TRANS
        sCertAuth = aafCM.env.slot(CERT_AUTH);
    
        ////////
        // Overall APIs
        ///////
        aafCM.route(HttpMethods.PUT,"/cert/:ca",API.CERT_REQ,new Code(aafCM,"Request Certificate") {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                String key = pathParam(req, ":ca");
                CA ca;
                if ((ca = aafCM.getCA(key))==null) {
                    context.error(trans,resp,Result.ERR_BadData,"CA %s is not supported",key);
                } else {
                    trans.put(sCertAuth, ca);
                    Result<Void> r = context.requestCert(trans, req, resp, ca);
                    if (r.isOK()) {
                        resp.setStatus(HttpStatus.OK_200);
                    } else {
                        context.error(trans,resp,r);
                    }
                }
            }
        });
    
        aafCM.route(HttpMethods.GET,"/cert/:ca/personal",API.CERT,new Code(aafCM,"Request Personal Certificate") {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                String key = pathParam(req, ":ca");
                CA ca;
                if ((ca = aafCM.getCA(key))==null) {
                    context.error(trans,resp,Result.ERR_BadData,"CA %s is not supported",key);
                } else {
                    trans.put(sCertAuth, ca);
                    Result<Void> r = context.requestPersonalCert(trans, req, resp, ca);
                    if (r.isOK()) {
                        resp.setStatus(HttpStatus.OK_200);
                    } else {
                        context.error(trans,resp,r);
                    }
                }
            }
        });

    
        /**
         * <p>
         */
        aafCM.route(HttpMethods.GET, "/cert/may/:perm", API.VOID, new Code(aafCM,"Check Permission") {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.check(trans, resp, pathParam(req,"perm"));
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                    trans.checkpoint(r.errorString());
                    context.error(trans,resp,Result.err(Result.ERR_Denied,"%s does not have Permission.",trans.user()));
                }
            }
        });

        /**
         * Get Cert by ID and Machine 
         */

    
        /**
         * Get Certs by ID
         */
        aafCM.route(HttpMethods.GET, "/cert/id/:id", API.CERT, new Code(aafCM,"GetByID") {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.readCertsByMechID(trans, resp, pathParam(req,"id"));
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });

    
        /**
         * Get Certs by Machine
         */
    
    }
}
