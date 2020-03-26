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

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.security.Principal;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.onap.aaf.auth.cache.Cache.Dated;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.locate.AAF_Locate;
import org.onap.aaf.auth.locate.BasicAuthCode;
import org.onap.aaf.auth.locate.LocateCode;
import org.onap.aaf.auth.locate.facade.LocateFacade;
import org.onap.aaf.auth.locate.mapper.Mapper.API;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;

import org.owasp.esapi.reference.DefaultHTTPUtilities;

public class API_AAFAccess {
//    private static String service, version, envContext;

    private static final String GET_PERMS_BY_USER = "Get Perms by User";
    private static final String USER_HAS_PERM ="User Has Perm";
//    private static final String USER_IN_ROLE ="User Has Role";

    /**
     * Normal Init level APIs
     *
     * @param gwAPI
     * @param facade
     * @throws Exception
     */
    public static void init(final AAF_Locate gwAPI, LocateFacade facade) throws Exception {


        gwAPI.route(HttpMethods.GET,"/authz/perms/user/:user",API.VOID,new LocateCode(facade,GET_PERMS_BY_USER, true) {
            @Override
            public void handle(final AuthzTrans trans, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
                TimeTaken tt = trans.start(GET_PERMS_BY_USER, Env.SUB);
                try {
                    final String accept = req.getHeader("ACCEPT");
                    final String user = pathParam(req,":user");
                    if (!user.contains("@")) {
                        context.error(trans,resp,Result.ERR_BadData,"User [%s] must be fully qualified with domain",user);
                        return;
                    }
                    final String key = trans.user() + user + (accept!=null&&accept.contains("xml")?"-xml":"-json");
                    TimeTaken tt2 = trans.start("Cache Lookup",Env.SUB);
                    Dated d;
                    try {
                        d = gwAPI.cacheUser.get(key);
                    } finally {
                        tt2.done();
                    }

                    if (d==null || d.data.isEmpty()) {
                        tt2 = trans.start("AAF Service Call",Env.REMOTE);
                        try {
                            gwAPI.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
                                @Override
                                public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                                    Future<String> fp = client.read("/authz/perms/user/"+user,accept);
                                    if (fp.get(5000)) {
                                        gwAPI.cacheUser.put(key, new Dated(new User(fp.code(),fp.body()),gwAPI.expireIn));
                                        resp.setStatus(HttpStatus.OK_200);
                                        ServletOutputStream sos;
                                        try {
                                            sos = resp.getOutputStream();
                                            sos.print(fp.value);
                                        } catch (IOException e) {
                                            throw new CadiException(e);
                                        }
                                    } else {
                                        gwAPI.cacheUser.put(key, new Dated(new User(fp.code(),fp.body()),gwAPI.expireIn));
                                        context.error(trans,resp,fp.code(),fp.body());
                                    }
                                    return null;
                                }
                            });
                        } finally {
                            tt2.done();
                        }
                    } else {
                        User u = (User)d.data.get(0);
                        resp.setStatus(u.code);
                        ServletOutputStream sos = resp.getOutputStream();
                        sos.print(u.resp);
                    }
                } finally {
                    tt.done();
                }
            }
        });


        gwAPI.route(gwAPI.env,HttpMethods.GET,"/authn/basicAuth",new BasicAuthCode(gwAPI.aafAuthn,facade)
                ,"text/plain","*/*","*");

        /**
         * Query User Has Perm is DEPRECATED
         *
         * Need to move towards NS declaration... is this even being used?
         * @deprecated
         */
        gwAPI.route(HttpMethods.GET,"/ask/:user/has/:type/:instance/:action",API.VOID,new LocateCode(facade,USER_HAS_PERM, true) {
            @Override
            public void handle(final AuthzTrans trans, final HttpServletRequest req, HttpServletResponse resp) throws Exception {
                try {
                    String type = pathParam(req,":type");
                    int idx = type.lastIndexOf('.');
                    String ns = type.substring(0,idx);
                    type = type.substring(idx+1);
                    resp.getOutputStream().print(
                            gwAPI.aafLurPerm.fish(new Principal() {
                                public String getName() {
                                    return pathParam(req,":user");
                                };
                            }, new AAFPermission(
                                ns,
                                type,
                                pathParam(req,":instance"),
                                pathParam(req,":action"))));
                    resp.setStatus(HttpStatus.OK_200);
                } catch (Exception e) {
                    context.error(trans, resp, Result.ERR_General, e.getMessage());
                }
            }
        });

        gwAPI.route(HttpMethods.GET,"/gui/:path*",API.VOID,new LocateCode(facade,"Short Access PROD GUI for AAF", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                try {
                    redirect(trans, req, resp, context,
                            gwAPI.getGUILocator(),
                            "gui/"+pathParam(req,":path"));
                } catch (LocatorException e) {
                    context.error(trans, resp, Result.ERR_BadData, e.getMessage());
                } catch (Exception e) {
                    context.error(trans, resp, Result.ERR_General, e.getMessage());
                }
            }
        });

        gwAPI.route(HttpMethods.GET,"/aaf/:version/:path*",API.VOID,new LocateCode(facade,"Access PROD GUI for AAF", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                try {
                    redirect(trans, req, resp, context,
                            gwAPI.getGUILocator(),
                            pathParam(req,":path"));
                } catch (LocatorException e) {
                    context.error(trans, resp, Result.ERR_BadData, e.getMessage());
                } catch (Exception e) {
                    context.error(trans, resp, Result.ERR_General, e.getMessage());
                }
            }
        });
    }

    public static void initDefault(final AAF_Locate gwAPI, LocateFacade facade) throws Exception {

        /**
         * "login" url
         */
        gwAPI.route(HttpMethods.GET,"/login",API.VOID,new LocateCode(facade,"Access Login GUI for AAF", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                try {
                    redirect(trans, req, resp, context,
                            gwAPI.getGUILocator(),
                            "login");
                } catch (LocatorException e) {
                    context.error(trans, resp, Result.ERR_BadData, e.getMessage());
                } catch (Exception e) {
                    context.error(trans, resp, Result.ERR_General, e.getMessage());
                }
            }
        });


        /**
         * Default URL
         */
        gwAPI.route(HttpMethods.GET,"/",API.VOID,new LocateCode(facade,"Access GUI for AAF", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                try {
                    redirect(trans, req, resp, context,
                            gwAPI.getGUILocator(),
                            "gui/home");
                } catch (Exception e) {
                    context.error(trans, resp, Result.ERR_General, e.getMessage());
                }
            }
        });

        /**
         * Configuration
         */
        gwAPI.route(HttpMethods.GET,"/configure/:id/:type",API.CONFIG,new LocateCode(facade,"Deliver Configuration Properties to AAF", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                try {
                    Result<Void> r = facade.getConfig(trans, req, resp, pathParam(req, ":id"),pathParam(req,":type"));
                    switch(r.status) {
                        case OK:
                            resp.setStatus(HttpStatus.OK_200);
                            break;
                        default:
                            context.error(trans,resp,r);
                    }

                } catch (Exception e) {
                    context.error(trans, resp, Result.ERR_General, e.getMessage());
                }
            }
        });
    }

    private static void redirect(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, LocateFacade context, Locator<URI> loc, String path) throws IOException {
        try {
            if (loc.hasItems()) {
                Item item = loc.best();
                URI uri = loc.get(item);
                StringBuilder redirectURL = new StringBuilder(uri.toString());
                redirectURL.append('/');
                redirectURL.append(path);
                String str = req.getQueryString();
                if (str!=null) {
                    redirectURL.append('?');
                    redirectURL.append(str);
                }
                trans.info().log("Redirect to",redirectURL);
                DefaultHTTPUtilities util = new DefaultHTTPUtilities();
                util.sendRedirect(redirectURL.toString()); 
            } else {
                context.error(trans, resp, Result.err(Result.ERR_NotFound,"No Locations found for redirection"));
            }
        } catch (LocatorException e) {
            context.error(trans, resp, Result.err(Result.ERR_NotFound,"No Endpoints found for %s",req.getPathInfo()));
        }
    }

    private static class User {
        public final int code;
        public final String resp;

        public User(int code, String resp) {
            this.code = code;
            this.resp = resp;
        }
    }
}
