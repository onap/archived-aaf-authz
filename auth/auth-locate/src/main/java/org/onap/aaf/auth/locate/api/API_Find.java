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

package org.onap.aaf.auth.locate.api;

import static org.onap.aaf.auth.layer.Result.OK;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.locate.AAF_Locate;
import org.onap.aaf.auth.locate.LocateCode;
import org.onap.aaf.auth.locate.facade.LocateFacade;
import org.onap.aaf.auth.locate.mapper.Mapper.API;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.misc.env.util.Split;

/**
 * API Apis.. using Redirect for mechanism
 *
 * @author Jonathan
 *
 */
public class API_Find {
    /**
     * Normal Init level APIs
     *
     * @param gwAPI
     * @param facade
     * @throws Exception
     */
    public static void init(final AAF_Locate gwAPI, LocateFacade facade) throws Exception {
        ////////
        // Overall APIs
        ///////
    
        final LocateCode locationInfo = new LocateCode(facade,"Location Information", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                String service = pathParam(req, ":service");
                String version = pathParam(req, ":version");
                String other = pathParam(req, ":other");
                if (service.indexOf(':')>=0) {
                    String split[] = Split.split(':', service);
                    switch(split.length) {
                        case 3:
                            other=split[2];
                        case 2:
                            version = split[1];
                            service = split[0];
                    }
                }
                service=Define.varReplace(service);
                Result<Void> r = context.getEndpoints(trans,resp,
                    req.getPathInfo(), // use as Key
                    service,version,other                
                );
                switch(r.status) {
                    case OK:
                        resp.setStatus(HttpStatus.OK_200);
                        break;
                    default:
                        context.error(trans,resp,r);
                }
            }
        };

        gwAPI.route(HttpMethods.GET,"/locate/:service/:version",API.ENDPOINTS,locationInfo);
        gwAPI.route(HttpMethods.GET,"/locate/:service/:version/:other",API.ENDPOINTS,locationInfo);
        gwAPI.route(HttpMethods.GET,"/locate/:service",API.ENDPOINTS,locationInfo);
    
    
        gwAPI.route(HttpMethods.GET,"/download/agent", API.VOID, new LocateCode(facade,"Redirect to latest Agent",false) {
            @Override
            public void handle(AuthzTrans arg0, HttpServletRequest arg1, HttpServletResponse arg2) throws Exception {
            }
        });

        gwAPI.route(HttpMethods.PUT,"/registration",API.MGMT_ENDPOINTS,new LocateCode(facade,"Put Location Information", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.putMgmtEndpoints(trans,req,resp);
                switch(r.status) {
                    case OK:
                        resp.setStatus(HttpStatus.OK_200);
                        break;
                    default:
                        context.error(trans,resp,r);
                }

            }
        });

        gwAPI.route(HttpMethods.DELETE,"/registration",API.MGMT_ENDPOINTS,new LocateCode(facade,"Remove Location Information", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.removeMgmtEndpoints(trans,req,resp);
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
