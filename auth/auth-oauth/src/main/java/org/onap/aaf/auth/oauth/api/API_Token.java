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

package org.onap.aaf.auth.oauth.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.oauth.AAF_OAuth;
import org.onap.aaf.auth.oauth.OACode;
import org.onap.aaf.auth.oauth.facade.OAFacade;
import org.onap.aaf.auth.oauth.mapper.Mapper.API;
import org.onap.aaf.auth.rserv.HttpMethods;

import aafoauth.v2_0.Introspect;

/**
 * API Apis
 * @author Jonathan
 *
 */
public class API_Token {
    // Hide Public Constructor
    private API_Token() {}

    /**
     * Normal Init level APIs
     *
     * @param authzAPI
     * @param facade
     * @throws Exception
     */
    public static void init(final AAF_OAuth authzAPI, OAFacade<Introspect> facade) throws Exception {
        ////////
        // Overall APIs
        ///////
        authzAPI.route(HttpMethods.POST,"/token",API.TOKEN,new OACode(facade,"OAuth Token", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.createBearerToken(trans,req, resp);
                if (r.isOK()) {
                    resp.setStatus(201/*HttpStatus.CREATED_201*/);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });
    
        authzAPI.route(HttpMethods.POST,"/introspect",API.INTROSPECT,new OACode(facade,"AAF Token Information", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.introspect(trans,req, resp);
                if (r.isOK()) {
                    resp.setStatus(200 /*HttpStatus.OK_200*/);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });

    }
}
