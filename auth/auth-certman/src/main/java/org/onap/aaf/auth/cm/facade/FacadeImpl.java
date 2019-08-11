/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 IBM.
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

package org.onap.aaf.auth.cm.facade;

import static org.onap.aaf.auth.layer.Result.ERR_ActionNotCompleted;
import static org.onap.aaf.auth.layer.Result.ERR_BadData;
import static org.onap.aaf.auth.layer.Result.ERR_ConflictAlreadyExists;
import static org.onap.aaf.auth.layer.Result.ERR_Denied;
import static org.onap.aaf.auth.layer.Result.ERR_NotFound;
import static org.onap.aaf.auth.layer.Result.ERR_NotImplemented;
import static org.onap.aaf.auth.layer.Result.ERR_Policy;
import static org.onap.aaf.auth.layer.Result.ERR_Security;
import static org.onap.aaf.auth.layer.Result.OK;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.cm.AAF_CM;
import org.onap.aaf.auth.cm.ca.CA;
import org.onap.aaf.auth.cm.data.CertResp;
import org.onap.aaf.auth.cm.mapper.Mapper;
import org.onap.aaf.auth.cm.mapper.Mapper.API;
import org.onap.aaf.auth.cm.service.CMService;
import org.onap.aaf.auth.dao.cass.ArtiDAO;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Split;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;

/**
 * AuthzFacade
 * 
 * This Service Facade encapsulates the essence of the API Service can do, and provides
 * a single created object for elements such as RosettaDF.
 *
 * The Responsibilities of this class are to:
 * 1) Interact with the Service Implementation (which might be supported by various kinds of Backend Storage)
 * 2) Validate incoming data (if applicable)
 * 3) Convert the Service response into the right Format, and mark the Content Type
 *         a) In the future, we may support multiple Response Formats, aka JSON or XML, based on User Request.
 * 4) Log Service info, warnings and exceptions as necessary
 * 5) When asked by the API layer, this will create and write Error content to the OutputStream
 * 
 * Note: This Class does NOT set the HTTP Status Code.  That is up to the API layer, so that it can be 
 * clearly coordinated with the API Documentation
 * 
 * @author Jonathan
 *
 */
public abstract class FacadeImpl<REQ,CERT,ARTIFACTS,ERROR> extends org.onap.aaf.auth.layer.FacadeImpl implements Facade<REQ,CERT,ARTIFACTS,ERROR> 
    {
    private static final String TRUE = "TRUE";
    private static final String REQUEST_CERT = "Request New Certificate";
    private static final String RENEW_CERT = "Renew Certificate";
    private static final String DROP_CERT = "Drop Certificate";
    private static final String READ_CERTS_MECHID = "Read Certificates by MechID";
    private static final String CREATE_ARTIFACTS = "Create Deployment Artifact";
    private static final String READ_ARTIFACTS = "Read Deployment Artifact";
    private static final String UPDATE_ARTIFACTS = "Update Deployment Artifact";
    private static final String DELETE_ARTIFACTS = "Delete Deployment Artifact";

    private CMService service;

    private final RosettaDF<ERROR>         errDF;
    private final RosettaDF<REQ>         certRequestDF, certRenewDF, certDropDF;
    private final RosettaDF<CERT>        certDF;
    private final RosettaDF<ARTIFACTS>    artiDF;
    private Mapper<REQ, CERT, ARTIFACTS, ERROR>     mapper;
//    private Slot sCertAuth;
    private final String voidResp;
    public FacadeImpl(AAF_CM certman,
                      CMService service, 
                      Mapper<REQ,CERT,ARTIFACTS,ERROR> mapper, 
                      Data.TYPE dataType) throws APIException {
        this.service = service;
        this.mapper = mapper;
        AuthzEnv env = certman.env;
        //TODO: Gabe [JUnit] Static issue, talk to Jonathan
        (errDF                 = env.newDataFactory(mapper.getClass(API.ERROR))).in(dataType).out(dataType);
        (certRequestDF         = env.newDataFactory(mapper.getClass(API.CERT_REQ))).in(dataType).out(dataType);
        (certRenewDF         = env.newDataFactory(mapper.getClass(API.CERT_RENEW))).in(dataType).out(dataType);
        (certDropDF         = env.newDataFactory(mapper.getClass(API.CERT_DROP))).in(dataType).out(dataType);
        (certDF             = env.newDataFactory(mapper.getClass(API.CERT))).in(dataType).out(dataType);
        (artiDF             = env.newDataFactory(mapper.getClass(API.ARTIFACTS))).in(dataType).out(dataType);
//        sCertAuth = env.slot(API_Cert.CERT_AUTH);
        if (artiDF.getOutType().name().contains("xml")) {
            voidResp = "application/Void+xml;charset=utf-8;version=1.0,application/xml;version=1.0,*/*";
        } else {
            voidResp = "application/Void+json;charset=utf-8;version=1.0,application/json;version=1.0,*/*";
        }
    }
    
    public Mapper<REQ,CERT,ARTIFACTS,ERROR> mapper() {
        return mapper;
    }
    
    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#error(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, int)
     * 
     * Note: Conforms to AT&T TSS RESTful Error Structure
     */
    @Override
    public void error(AuthzTrans trans, HttpServletResponse response, Result<?> result) {
        error(trans, response, result.status,
                result.details==null?"":result.details.trim(),
                result.variables==null?Result.EMPTY_VARS:result.variables);
    }
        
    @Override
    public void error(AuthzTrans trans, HttpServletResponse response, int status, final String _msg, final Object ... _detail) {
        String msgId;
        String prefix;
        boolean hidemsg=false;
        switch(status) {
            case 202:
            case ERR_ActionNotCompleted:
                msgId = "SVC1202";
                prefix = "Accepted, Action not complete";
                response.setStatus(/*httpstatus=*/202);
                break;

            case 403:
            case ERR_Policy:
            case ERR_Security:
            case ERR_Denied:
                msgId = "SVC1403";
                prefix = "Forbidden";
                response.setStatus(/*httpstatus=*/403);
                break;
                
            case 404:
            case ERR_NotFound:
                msgId = "SVC1404";
                prefix = "Not Found";
                response.setStatus(/*httpstatus=*/404);
                break;

            case 406:
            case ERR_BadData:
                msgId="SVC1406";
                prefix = "Not Acceptable";
                response.setStatus(/*httpstatus=*/406);
                break;
                
            case 409:
            case ERR_ConflictAlreadyExists:
                msgId = "SVC1409";
                prefix = "Conflict Already Exists";
                response.setStatus(/*httpstatus=*/409);
                break;
            
            case 501:
            case ERR_NotImplemented:
                msgId = "SVC1501";
                prefix = "Not Implemented"; 
                response.setStatus(/*httpstatus=*/501);
                break;
                

            default:
                msgId = "SVC1500";
                prefix = "General Service Error";
                response.setStatus(/*httpstatus=*/500);
                hidemsg=true;
                break;
        }

        try {
            StringBuilder holder = new StringBuilder();
            ERROR em = mapper().errorFromMessage(holder, msgId,prefix + ": " + _msg,_detail);
            trans.checkpoint(
                    "ErrResp [" + 
                    msgId +
                    "] " +
                    holder.toString(),
                    Env.ALWAYS);
            if (hidemsg) {
                holder.setLength(0);
                em = mapper().errorFromMessage(holder, msgId, "Server had an issue processing this request");
            }
            errDF.newData(trans).load(em).to(response.getOutputStream());
            
        } catch (Exception e) {
            trans.error().log(e,"unable to send response for",_msg);
        }
    }

    @Override
    public Result<Void> check(AuthzTrans trans, HttpServletResponse resp, String perm) throws IOException {
        String[] p = Split.split('|',perm);
        AAFPermission ap;
        switch(p.length) {
            case 3:
                 ap = new AAFPermission(null, p[0],p[1],p[2]);
                 break;
            case 4:
                ap = new AAFPermission(p[0],p[1],p[2],p[3]);
                break;
            default:
                return Result.err(Result.ERR_BadData,"Invalid Perm String");
        }
        if (AAF_CM.aafLurPerm.fish(trans.getUserPrincipal(), ap)) {
            resp.setContentType(voidResp);
            resp.getOutputStream().write(0);
            return Result.ok();
        } else {
            return Result.err(Result.ERR_Denied,"%s does not have %s",trans.user(),ap.getKey());
        }
    }

    /* (non-Javadoc)
     * @see com.att.auth.certman.facade.Facade#requestCert(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Result<Void> requestCert(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, CA ca) {
        TimeTaken tt = trans.start(REQUEST_CERT, Env.SUB|Env.ALWAYS);
        String wt;
        boolean withTrust=(wt=req.getParameter("withTrust"))!=null || TRUE.equalsIgnoreCase(wt);
        try {
            REQ request;
            try {
                Data<REQ> rd = certRequestDF.newData().load(req.getInputStream());
                request = rd.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,REQUEST_CERT);
                return Result.err(Result.ERR_BadData,"Invalid Input");
            }
            
            Result<CertResp> rcr = service.requestCert(trans,mapper.toReq(trans,request), ca);
            if (rcr.notOK()) {
                return Result.err(rcr);
            }
            
            Result<CERT> rc = mapper.toCert(trans, rcr, withTrust);
            if (rc.status == OK) {
                RosettaData<CERT> data = certDF.newData(trans).load(rc.value);
                data.to(resp.getOutputStream());

                setContentType(resp, certDF.getOutType());
                return Result.ok();
            }
            return Result.err(rc);

        } catch (Exception e) {
            trans.error().log(e,IN,REQUEST_CERT);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    /* (non-Javadoc)
     * @see org.onap.aaf.auth.cm.facade.Facade#requestPersonalCert(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, boolean)
     */
    @Override
    public Result<Void> requestPersonalCert(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, CA ca) {
        return Result.err(Result.ERR_NotImplemented, "not implemented yet");
    }

    @Override
    public Result<Void> renewCert(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, boolean withTrust) {
        TimeTaken tt = trans.start(RENEW_CERT, Env.SUB|Env.ALWAYS);
        try {
            REQ request;
            try {
                Data<REQ> rd = certRenewDF.newData().load(req.getInputStream());
                request = rd.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,RENEW_CERT);
                return Result.err(Result.ERR_BadData,"Invalid Input");
            }
            
            Result<CertResp> rcr = service.renewCert(trans,mapper.toRenew(trans,request));
            Result<CERT> rc = mapper.toCert(trans, rcr, withTrust);

            if (rc.status == OK) {
                RosettaData<CERT> data = certDF.newData(trans).load(rc.value);
                data.to(resp.getOutputStream());

                setContentType(resp, certDF.getOutType());
                return Result.ok();
            }
            return Result.err(rc);
        } catch (Exception e) {
            trans.error().log(e,IN,RENEW_CERT);
            return Result.err(e);
        } finally {
            tt.done();
        }

    }

    @Override
    public Result<Void> dropCert(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(DROP_CERT, Env.SUB|Env.ALWAYS);
        try {
            REQ request;
            try {
                Data<REQ> rd = certDropDF.newData().load(req.getInputStream());
                request = rd.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,DROP_CERT);
                return Result.err(Result.ERR_BadData,"Invalid Input");
            }
            
            Result<Void> rv = service.dropCert(trans,mapper.toDrop(trans, request));
            if (rv.status == OK) {
                setContentType(resp, certRequestDF.getOutType());
                return Result.ok();
            }
            return Result.err(rv);
        } catch (Exception e) {
            trans.error().log(e,IN,DROP_CERT);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.cm.facade.Facade#readCertsByMechID(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> readCertsByMechID(AuthzTrans trans, HttpServletResponse resp, String mechID) {
        TimeTaken tt = trans.start(READ_CERTS_MECHID, Env.SUB|Env.ALWAYS);
        try {
            Result<CERT> rc = mapper.toCert(trans, service.readCertsByMechID(trans,mechID));
            if (rc.status == OK) {
                RosettaData<CERT> data = certDF.newData(trans).load(rc.value);
                data.to(resp.getOutputStream());

                setContentType(resp, certDF.getOutType());
                return Result.ok();
            }
            return Result.err(rc);
        } catch (Exception e) {
            trans.error().log(e,IN,READ_CERTS_MECHID);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    ////////////////////////////
    // Artifacts
    ////////////////////////////
    @Override
    public Result<Void> createArtifacts(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(CREATE_ARTIFACTS, Env.SUB);
        try {
            ARTIFACTS arti;
            try {
                Data<ARTIFACTS> rd = artiDF.newData().load(req.getInputStream());
                arti = rd.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,CREATE_ARTIFACTS);
                return Result.err(Result.ERR_BadData,"Invalid Input");
            }
            
            return service.createArtifact(trans,mapper.toArtifact(trans,arti));
        } catch (Exception e) {

            trans.error().log(e,IN,CREATE_ARTIFACTS);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> readArtifacts(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(READ_ARTIFACTS, Env.SUB);
        try {
            String mechid = req.getParameter("mechid");
            String machine = req.getParameter("machine");
            String ns = req.getParameter("ns");
            
            Result<ARTIFACTS> ra;
            if ( machine !=null && mechid == null) {
                ra = mapper.fromArtifacts(service.readArtifactsByMachine(trans, machine));
            } else if (mechid!=null && machine==null) {
                ra = mapper.fromArtifacts(service.readArtifactsByMechID(trans, mechid));
            } else if (mechid!=null && machine!=null) {
                ArtiDAO.Data add = new ArtiDAO.Data();
                add.mechid = mechid;
                add.machine = machine;
                add.ns = ns;
                ra = mapper.fromArtifacts(service.readArtifacts(trans,add));
            } else if (ns!=null) {
                ra = mapper.fromArtifacts(service.readArtifactsByNs(trans, ns));
            } else {
                ra = Result.err(Status.ERR_BadData,"Invalid request inputs");
            }
            
            if (ra.isOK()) {
                RosettaData<ARTIFACTS> data = artiDF.newData(trans).load(ra.value);
                data.to(resp.getOutputStream());
                setContentType(resp,artiDF.getOutType());
                return Result.ok();
            } else {
                return Result.err(ra);
            }

        } catch (Exception e) {
            trans.error().log(e,IN,READ_ARTIFACTS);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> readArtifacts(AuthzTrans trans, HttpServletResponse resp, String mechid, String machine) {
        TimeTaken tt = trans.start(READ_ARTIFACTS, Env.SUB);
        try {
            ArtiDAO.Data add = new ArtiDAO.Data();
            add.mechid = mechid;
            add.machine = machine;
            Result<ARTIFACTS> ra = mapper.fromArtifacts(service.readArtifacts(trans,add));
            if (ra.isOK()) {
                RosettaData<ARTIFACTS> data = artiDF.newData(trans).load(ra.value);
                data.to(resp.getOutputStream());
                setContentType(resp,artiDF.getOutType());
                return Result.ok();
            } else {
                return Result.err(ra);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,READ_ARTIFACTS);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }


    @Override
    public Result<Void> updateArtifacts(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(UPDATE_ARTIFACTS, Env.SUB);
        try {
            ARTIFACTS arti;
            try {
                Data<ARTIFACTS> rd = artiDF.newData().load(req.getInputStream());
                arti = rd.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,UPDATE_ARTIFACTS);
                return Result.err(Result.ERR_BadData,"Invalid Input");
            }
            
            return service.updateArtifact(trans,mapper.toArtifact(trans,arti));
        } catch (Exception e) {
            trans.error().log(e,IN,UPDATE_ARTIFACTS);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> deleteArtifacts(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(DELETE_ARTIFACTS, Env.SUB);
        try {
            ARTIFACTS arti;
            try {
                Data<ARTIFACTS> rd = artiDF.newData().load(req.getInputStream());
                arti = rd.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,DELETE_ARTIFACTS);
                return Result.err(Result.ERR_BadData,"Invalid Input");
            }
            
            Result<Void> rv = service.deleteArtifact(trans,mapper.toArtifact(trans,arti));
            if (rv.status == OK) {
                setContentType(resp, artiDF.getOutType());
            }
            return rv;
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_ARTIFACTS);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> deleteArtifacts(AuthzTrans trans, HttpServletResponse resp, String mechid, String machine) {
        TimeTaken tt = trans.start(DELETE_ARTIFACTS, Env.SUB);
        try {
            Result<Void> rv = service.deleteArtifact(trans, mechid, machine);
            if (rv.status == OK) {
                setContentType(resp, artiDF.getOutType());
            }
            return rv;
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_ARTIFACTS);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }


}