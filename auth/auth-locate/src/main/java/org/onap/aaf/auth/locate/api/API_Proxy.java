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

import java.net.ConnectException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.locate.AAF_Locate;
import org.onap.aaf.auth.locate.BasicAuthCode;
import org.onap.aaf.auth.locate.LocateCode;
import org.onap.aaf.auth.locate.facade.LocateFacade;
import org.onap.aaf.auth.locate.mapper.Mapper.API;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.oauth.OAuth2Principal;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;

/**
 * API Apis.. using Redirect for mechanism
 *
 * @author Jonathan
 *
 */
public class API_Proxy {

    /**
     * Normal Init level APIs
     *
     * @param gwAPI
     * @param facade
     * @throws Exception
     */
    public static void init(final AAF_Locate gwAPI, LocateFacade facade) throws Exception {

        String aafurl = gwAPI.access.getProperty(Config.AAF_URL,null);
        if (aafurl!=null) {
            ////////
            // Transferring APIs
            // But DO NOT transfer BasicAuth case... wastes resources.
            ///////
            final BasicAuthCode bac = new BasicAuthCode(gwAPI.aafAuthn,facade);

            gwAPI.routeAll(HttpMethods.GET,"/proxy/:path*",API.VOID,new LocateCode(facade,"Proxy GET", true) {
                @Override
                public void handle(final AuthzTrans trans, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
                    if ("/proxy/authn/basicAuth".equals(req.getPathInfo()) && !(req.getUserPrincipal() instanceof OAuth2Principal)) {
                        bac.handle(trans, req, resp);
                    } else {
                        TimeTaken tt = trans.start("Forward to AAF Service", Env.REMOTE);
                        try {
                            gwAPI.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
                                @Override
                                public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                                    Future<Void> ft = client.transfer(req,resp,pathParam(req, ":path"),HttpStatus.OK_200);
                                    ft.get(10000); // Covers return codes and err messages
                                    return null;
                                }
                            });

                        } catch (CadiException | APIException e) {
                            trans.error().log(e);
                        } finally {
                            tt.done();
                        }
                    }
                }
            });

            gwAPI.routeAll(HttpMethods.POST,"/proxy/:path*",API.VOID,new LocateCode(facade,"Proxy POST", true) {
                @Override
                public void handle(final AuthzTrans trans, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
                    TimeTaken tt = trans.start("Forward to AAF Service", Env.REMOTE);
                    try {
                        gwAPI.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
                            @Override
                            public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                                Future<Void> ft = client.transfer(req,resp,pathParam(req, ":path"),HttpStatus.CREATED_201);
                                ft.get(10000); // Covers return codes and err messages
                                return null;
                            }
                        });
                    } catch (CadiException | APIException e) {
                        trans.error().log(e);
                    } finally {
                        tt.done();
                    }
                }
            });

            gwAPI.routeAll(HttpMethods.PUT,"/proxy/:path*",API.VOID,new LocateCode(facade,"Proxy PUT", true) {
                @Override
                public void handle(final AuthzTrans trans, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
                    TimeTaken tt = trans.start("Forward to AAF Service", Env.REMOTE);
                    try {
                        gwAPI.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
                            @Override
                            public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                                Future<Void> ft = client.transfer(req,resp,pathParam(req, ":path"),HttpStatus.OK_200);
                                ft.get(10000); // Covers return codes and err messages
                                return null;
                            }
                        });
                    } catch (CadiException | APIException e) {
                        trans.error().log(e);
                    } finally {
                        tt.done();
                    }
                }
            });

            gwAPI.routeAll(HttpMethods.DELETE,"/proxy/:path*",API.VOID,new LocateCode(facade,"Proxy DELETE", true) {
                @Override
                public void handle(final AuthzTrans trans, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
                    TimeTaken tt = trans.start("Forward to AAF Service", Env.REMOTE);
                    try {
                        gwAPI.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
                            @Override
                            public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                                Future<Void> ft = client.transfer(req,resp,pathParam(req, ":path"),HttpStatus.OK_200);
                                ft.get(10000); // Covers return codes and err messages
                                return null;
                            }
                        });
                    } catch (CadiException | APIException e) {
                        trans.error().log(e);
                    } finally {
                        tt.done();
                    }
                }
            });
        }
    }
}
