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

package org.onap.aaf.auth.cm.facade;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.cm.ca.CA;
import org.onap.aaf.auth.cm.mapper.Mapper;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;


/**
 *   
 * @author Jonathan
 *
 */
public interface Facade<REQ,CERT,ARTIFACTS,ERROR> {

/////////////////////  STANDARD ELEMENTS //////////////////
    /** 
     * @param trans
     * @param response
     * @param result
     */
    void error(AuthzTrans trans, HttpServletResponse response, Result<?> result);

    /**
     * <p>
     * @param trans
     * @param response
     * @param status
     */
    void error(AuthzTrans trans, HttpServletResponse response, int status,    String msg, Object ... detail);

    /**
     * Permission checker
     *
     * @param trans
     * @param resp
     * @param perm
     * @return
     * @throws IOException 
     */
    Result<Void> check(AuthzTrans trans, HttpServletResponse resp, String perm) throws IOException;

    /**
     * <p>
     * @return
     */
    public Mapper<REQ,CERT,ARTIFACTS,ERROR> mapper();

/////////////////////  STANDARD ELEMENTS //////////////////

    /**
     * <p>
     * @param trans
     * @param resp
     * @param rservlet
     * @return
     */
    public abstract Result<Void> requestCert(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, CA ca);

    /**
     * <p>
     * @param trans
     * @param resp
     * @param rservlet
     * @return
     */
    public abstract Result<Void> requestPersonalCert(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, CA ca);


    /**
     * <p>
     * @param trans
     * @param req
     * @param resp
     * @return
     */
    public abstract Result<Void> renewCert(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, boolean withTrust);

    /**
     * <p>
     * @param trans
     * @param req
     * @param resp
     * @return
     */
    public abstract Result<Void> dropCert(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);


    /**
     * <p>
     * @param trans
     * @param resp
     * @param pathParam
     * @return
     */
    public Result<Void> readCertsByMechID(AuthzTrans trans, HttpServletResponse resp, String mechID);


    /**
     * <p>
     * @param trans
     * @param req
     * @param resp
     * @return
     */
    Result<Void> createArtifacts(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    /**
     * <p>
     * @param trans
     * @param req
     * @param resp
     * @return
     */
    Result<Void> readArtifacts(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    /**
     * <p>
     * @param trans
     * @param resp
     * @param mechid
     * @param machine
     * @return
     */
    Result<Void> readArtifacts(AuthzTrans trans, HttpServletResponse resp, String mechid, String machine);

    /**
     * <p>
     * @param trans
     * @param req
     * @param resp
     * @return
     */
    Result<Void> updateArtifacts(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    /**
     * <p>
     * @param trans
     * @param req
     * @param resp
     * @return
     */
    Result<Void> deleteArtifacts(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    /**
     * <p>
     * @param trans
     * @param resp
     * @param mechid
     * @param machine
     * @return
     */
    Result<Void> deleteArtifacts(AuthzTrans trans, HttpServletResponse resp, String mechid, String machine);


}