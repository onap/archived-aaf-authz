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

package org.onap.aaf.auth.rserv;

import java.io.IOException;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

public abstract class RServlet<TRANS extends Trans> implements Servlet {
    private Routes<TRANS> routes = new Routes<TRANS>();

    private ServletConfig config;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    public void route(Env env, HttpMethods meth, String path, HttpCode<TRANS, ?> code, String ... moreTypes) {
        Route<TRANS> r = routes.findOrCreate(meth,path);
        r.add(code,moreTypes);
        env.init().log(r.report(code),code);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;

        @SuppressWarnings("unchecked")
        TRANS trans = (TRANS)req.getAttribute(TransFilter.TRANS_TAG);
        if (trans==null) {
            response.setStatus(404); // Not Found, because it didn't go through TransFilter
            return;
        }

        Route<TRANS> route;
        HttpCode<TRANS,?> code=null;
        String ct = req.getContentType();
        TimeTaken tt = trans.start("Resolve to Code", Env.SUB);
        try {
            // routes have multiple code sets.  This object picks the best code set
            // based on Accept or Content-Type
            CodeSetter<TRANS> codesetter = new CodeSetter<TRANS>(trans,request,response);
            // Find declared route
            route = routes.derive(request, codesetter);
            if (route==null) {
                String method = request.getMethod();
                trans.checkpoint("No Route matches "+ method + ' ' + request.getPathInfo());
                response.setStatus(404); // Not Found
            } else {
                // Find best Code in Route based on "Accepts (Get) or Content-Type" (if exists)
                code = codesetter.code();// route.getCode(trans, request, response);
            }
        } finally {
            tt.done();
        }

        if (route!=null && code!=null) {
            StringBuilder sb = new StringBuilder(72);
            sb.append(route.auditText);
            sb.append(',');
            sb.append(code.desc());
            if (ct!=null) {
                sb.append(", ContentType: ");
                sb.append(ct);
            }
            tt = trans.start(sb.toString(),Env.SUB);
            try {
                /*obj = */
                code.handle(trans, request, response);
                response.flushBuffer();
            } catch (ServletException e) {
                trans.error().log(e);
                throw e;
            } catch (Exception e) {
                trans.error().log(e,request.getMethod(),request.getPathInfo());
                throw new ServletException(e);
            } finally {
                tt.done();
            }
        }
    }

    @Override
    public String getServletInfo() {
        return "RServlet for Jetty";
    }

    /**
     * Allow Service to instantiate certain actions after service starts up
     * @throws LocatorException
     * @throws CadiException
     * @throws APIException
     */
    public void postStartup(String hostname, int port) throws APIException {
    }

    @Override
    public void destroy() {
    }

    public String applicationJSON(Class<?> cls, String version) {
        StringBuilder sb = new StringBuilder();
        sb.append("application/");
        sb.append(cls.getSimpleName());
        sb.append("+json");
        sb.append(";charset=utf-8");
        sb.append(";version=");
        sb.append(version);
        return sb.toString();
    }

    public String applicationXML(Class<?> cls, String version) {
        StringBuilder sb = new StringBuilder();
        sb.append("application/");
        sb.append(cls.getSimpleName());
        sb.append("+xml");
        sb.append(";charset=utf-8");
        sb.append(";version=");
        sb.append(version);
        return sb.toString();
    }

    public List<RouteReport> routeReport() {
        return routes.routeReport();
    }
}
