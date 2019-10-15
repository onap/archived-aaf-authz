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

package org.onap.aaf.auth.oauth.facade;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.oauth.service.OAuthService;


/**
 *
 * @author Jonathan
 *
 */
public interface OAFacade<INTROSPECT> {

/////////////////////  STANDARD ELEMENTS //////////////////
    /**
     * @param trans
     * @param response
     * @param result
     */
    public void error(AuthzTrans trans, HttpServletResponse response, Result<?> result);

    /**
     *
     * @param trans
     * @param response
     * @param status
     */
    public void error(AuthzTrans trans, HttpServletResponse response, int status, String msg, Object ... detail);

    public Result<Void> createBearerToken(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    public Result<Void> introspect(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    public OAuthService service();


/////////////////////  STANDARD ELEMENTS //////////////////




}