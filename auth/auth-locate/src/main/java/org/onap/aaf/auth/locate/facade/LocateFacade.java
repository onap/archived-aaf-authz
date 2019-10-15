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

package org.onap.aaf.auth.locate.facade;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.rserv.RServlet;


/**
 *
 * @author Jonathan
 *
 */
public interface LocateFacade {

/////////////////////  STANDARD ELEMENTS //////////////////
    /**
     * @param trans
     * @param response
     * @param result
     */
    void error(AuthzTrans trans, HttpServletResponse response, Result<?> result);

    /**
     *
     * @param trans
     * @param response
     * @param status
     */
    void error(AuthzTrans trans, HttpServletResponse response, int status,    String msg, String ... detail);


    /**
     *
     * @param trans
     * @param resp
     * @param rservlet
     * @return
     */
    public Result<Void> getAPI(AuthzTrans trans, HttpServletResponse resp, RServlet<AuthzTrans> rservlet);

    /**
     *
     * @param trans
     * @param resp
     * @param typeCode
     * @param optional
     * @return
     */
    public abstract Result<Void> getAPIExample(AuthzTrans trans, HttpServletResponse resp, String typeCode, boolean optional);

    /**
     *
     * @param trans
     * @param resp
     * @param service
     * @param version
     * @param other
     * @param string
     * @return
     */
    public abstract Result<Void> getEndpoints(AuthzTrans trans, HttpServletResponse resp, String key,
            String service, String version, String other);

    /**
     *
     * @param trans
     * @param req
     * @param resp
     * @return
     */
    public abstract Result<Void> putMgmtEndpoints(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    /**
     *
     * @param trans
     * @param req
     * @param resp
     * @return
     */
    public abstract Result<Void> removeMgmtEndpoints(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    /**
     *
     * @param trans
     * @param req
     * @param resp
     * @return
     */
    public Result<Void> getConfig(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, String id, String type);

}