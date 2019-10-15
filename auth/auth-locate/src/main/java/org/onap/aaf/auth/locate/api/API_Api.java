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

package org.onap.aaf.auth.locate.api;

import static org.onap.aaf.auth.layer.Result.OK;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.locate.AAF_Locate;
import org.onap.aaf.auth.locate.LocateCode;
import org.onap.aaf.auth.locate.facade.LocateFacade;
import org.onap.aaf.auth.locate.mapper.Mapper.API;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.cadi.Symm;

/**
 * API Apis
 * @author Jonathan
 *
 */
public class API_Api {
    /**
     * Normal Init level APIs
     * <p>
     * @param gwAPI
     * @param facade
     * @throws Exception
     */
    public static void init(final AAF_Locate gwAPI, LocateFacade facade) throws Exception {
        ////////
        // Overall APIs
        ///////
        gwAPI.route(HttpMethods.GET,"/api",API.VOID,new LocateCode(facade,"Document API", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.getAPI(trans,resp,gwAPI);
                switch(r.status) {
                case OK:
                    resp.setStatus(HttpStatus.OK_200);
                    break;
                default:
                    context.error(trans,resp,r);
            }

            }
        });

        ////////
        // Overall Examples
        ///////
        gwAPI.route(HttpMethods.GET,"/api/example/*",API.VOID,new LocateCode(facade,"Document API", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                String pathInfo = req.getPathInfo();
                int question = pathInfo.lastIndexOf('?');
            
                pathInfo = pathInfo.substring(13, question<0?pathInfo.length():question);// IMPORTANT, this is size of "/api/example/"
                String nameOrContextType=Symm.base64noSplit.decode(pathInfo);
//                String param = req.getParameter("optional");
                Result<Void> r = context.getAPIExample(trans,resp,nameOrContextType,
                        question>=0 && "optional=true".equalsIgnoreCase(req.getPathInfo().substring(question+1))
                        );
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
