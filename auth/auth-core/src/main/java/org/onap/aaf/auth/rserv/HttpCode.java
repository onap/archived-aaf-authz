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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.misc.env.Trans;

/**
 * HTTP Code element, which responds to the essential "handle Method".
 *
 * Use Native HttpServletRe[quest|sponse] calls for questions like QueryParameters (getParameter, etc)
 *
 * Use local "pathParam" method to obtain in an optimized manner the path parameter, which must be interpreted by originating string
 *
 * i.e. my/path/:id/:other/*
 *
 * @author Jonathan
 *
 * @param <TRANS>
 * @param <T>
 */
public abstract class HttpCode<TRANS extends Trans, CONTEXT> {
    protected CONTEXT context;
    private String desc;
    protected String [] roles;
    private boolean all;

    // Package by design... Set by Route when linked
    Match match;

    public HttpCode(CONTEXT context, String description, String ... roles) {
        this.context = context;
        desc = description;

        // Evaluate for "*" once...
        all = false;
        for (String srole : roles) {
            if ("*".equals(srole)) {
                all = true;
                break;
            }
        }
        this.roles = all?null:roles;
    }

    public abstract void handle(TRANS trans, HttpServletRequest req, HttpServletResponse resp) throws Exception;

    public String desc() {
        return desc;
    }

    /**
     * Get the variable element out of the Path Parameter, as set by initial Code
     *
     * @param req
     * @param key
     * @return
     */
    public String pathParam(HttpServletRequest req, String key) {
        String rv = req.getParameter(key);
        if (rv==null) {
            rv = match.param(req.getPathInfo(), key);
            if (rv!=null) {
                rv = rv.trim();
                if (rv.endsWith("/")) {
                    rv = rv.substring(0, rv.length()-1);
                }
            }
        }
        return rv;
    }

    // Note: get Query Params from Request

    /**
     * Check for Authorization when set.
     *
     * If no Roles set, then accepts all users
     *
     * @param req
     * @return
     */
    public boolean isAuthorized(HttpServletRequest req) {
        if (all)return true;
        if (roles!=null) {
            for (String srole : roles) {
                if (req.isUserInRole(srole)) return true;
            }
        }
        return false;
    }

    public boolean no_cache() {
        return false;
    }

    public String toString() {
        return desc;
    }
}