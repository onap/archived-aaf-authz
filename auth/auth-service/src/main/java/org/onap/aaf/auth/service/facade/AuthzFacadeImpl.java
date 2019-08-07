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

package org.onap.aaf.auth.service.facade;

import static org.onap.aaf.auth.dao.cass.Status.ERR_ChoiceNeeded;
import static org.onap.aaf.auth.dao.cass.Status.ERR_DelegateNotFound;
import static org.onap.aaf.auth.dao.cass.Status.ERR_DependencyExists;
import static org.onap.aaf.auth.dao.cass.Status.ERR_FutureNotRequested;
import static org.onap.aaf.auth.dao.cass.Status.ERR_InvalidDelegate;
import static org.onap.aaf.auth.dao.cass.Status.ERR_NsNotFound;
import static org.onap.aaf.auth.dao.cass.Status.ERR_PermissionNotFound;
import static org.onap.aaf.auth.dao.cass.Status.ERR_RoleNotFound;
import static org.onap.aaf.auth.dao.cass.Status.ERR_UserNotFound;
import static org.onap.aaf.auth.dao.cass.Status.ERR_UserRoleNotFound;
import static org.onap.aaf.auth.layer.Result.ERR_ActionNotCompleted;
import static org.onap.aaf.auth.layer.Result.ERR_Backend;
import static org.onap.aaf.auth.layer.Result.ERR_BadData;
import static org.onap.aaf.auth.layer.Result.ERR_ConflictAlreadyExists;
import static org.onap.aaf.auth.layer.Result.ERR_Denied;
import static org.onap.aaf.auth.layer.Result.ERR_NotFound;
import static org.onap.aaf.auth.layer.Result.ERR_NotImplemented;
import static org.onap.aaf.auth.layer.Result.ERR_Policy;
import static org.onap.aaf.auth.layer.Result.ERR_Security;
import static org.onap.aaf.auth.layer.Result.OK;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.dao.cass.NsType;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.FacadeImpl;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.rserv.RServlet;
import org.onap.aaf.auth.rserv.RouteReport;
import org.onap.aaf.auth.rserv.doc.ApiDoc;
import org.onap.aaf.auth.service.AuthzCassServiceImpl;
import org.onap.aaf.auth.service.AuthzService;
import org.onap.aaf.auth.service.mapper.Mapper;
import org.onap.aaf.auth.service.mapper.Mapper.API;
import org.onap.aaf.cadi.aaf.client.Examples;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;
import org.onap.aaf.misc.rosetta.Marshal;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;

import aaf.v2_0.Api;

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
 * @author Pavani & Jonathan
 *
 */
public abstract class AuthzFacadeImpl<NSS,PERMS,PERMKEY,ROLES,USERS,USERROLES,DELGS,CERTS,KEYS,REQUEST,HISTORY,ERR,APPROVALS> extends FacadeImpl implements AuthzFacade 
    {
    private static final String FORBIDDEN = "Forbidden";
    private static final String NOT_FOUND = "Not Found";
    private static final String NOT_ACCEPTABLE = "Not Acceptable";
    private static final String GENERAL_SERVICE_ERROR = "General Service Error";
    private static final String NO_DATA = "***No Data***";
    private AuthzService<NSS,PERMS,PERMKEY,ROLES,USERS,USERROLES,DELGS,CERTS,KEYS,REQUEST,HISTORY,ERR,APPROVALS> service = null;
    private final RosettaDF<NSS> nssDF;
    private final RosettaDF<PERMS> permsDF;
    private final RosettaDF<ROLES> roleDF;
    private final RosettaDF<USERS> usersDF;
    private final RosettaDF<USERROLES> userrolesDF;
    private final RosettaDF<CERTS> certsDF;
    private final RosettaDF<DELGS> delgDF;
    private final RosettaDF<REQUEST> permRequestDF;
    private final RosettaDF<REQUEST> roleRequestDF;
    private final RosettaDF<REQUEST> userRoleRequestDF;
    private final RosettaDF<REQUEST> rolePermRequestDF;
    private final RosettaDF<REQUEST> nsRequestDF;
    private final RosettaDF<REQUEST> credRequestDF;
    private final RosettaDF<REQUEST> delgRequestDF;
    private final RosettaDF<HISTORY> historyDF;
    private final RosettaDF<KEYS>    keysDF;

    private final RosettaDF<ERR>          errDF;
    private final RosettaDF<APPROVALS>  approvalDF;
    // Note: Api is not different per Version
    private final RosettaDF<Api>         apiDF;


    @SuppressWarnings("unchecked")
    public AuthzFacadeImpl(AuthzEnv env, AuthzService<NSS,PERMS,PERMKEY,ROLES,USERS,USERROLES,DELGS,CERTS,KEYS,REQUEST,HISTORY,ERR,APPROVALS> service, Data.TYPE dataType) throws APIException {
        this.service = service;
        (nssDF                 = env.newDataFactory(service.mapper().getClass(API.NSS))).in(dataType).out(dataType);
        (permRequestDF         = env.newDataFactory(service.mapper().getClass(API.PERM_REQ))).in(dataType).out(dataType);
        (permsDF             = env.newDataFactory(service.mapper().getClass(API.PERMS))).in(dataType).out(dataType);
//        (permKeyDF            = env.newDataFactory(service.mapper().getClass(API.PERM_KEY))).in(dataType).out(dataType);
        (roleDF             = env.newDataFactory(service.mapper().getClass(API.ROLES))).in(dataType).out(dataType);
        (roleRequestDF         = env.newDataFactory(service.mapper().getClass(API.ROLE_REQ))).in(dataType).out(dataType);
        (usersDF             = env.newDataFactory(service.mapper().getClass(API.USERS))).in(dataType).out(dataType);
        (userrolesDF             = env.newDataFactory(service.mapper().getClass(API.USER_ROLES))).in(dataType).out(dataType);
        (certsDF             = env.newDataFactory(service.mapper().getClass(API.CERTS))).in(dataType).out(dataType)
            .rootMarshal((Marshal<CERTS>) service.mapper().getMarshal(API.CERTS));
        ;
        (userRoleRequestDF     = env.newDataFactory(service.mapper().getClass(API.USER_ROLE_REQ))).in(dataType).out(dataType);
        (rolePermRequestDF     = env.newDataFactory(service.mapper().getClass(API.ROLE_PERM_REQ))).in(dataType).out(dataType);
        (nsRequestDF         = env.newDataFactory(service.mapper().getClass(API.NS_REQ))).in(dataType).out(dataType);
        (credRequestDF         = env.newDataFactory(service.mapper().getClass(API.CRED_REQ))).in(dataType).out(dataType);
        (delgRequestDF         = env.newDataFactory(service.mapper().getClass(API.DELG_REQ))).in(dataType).out(dataType);
        (historyDF             = env.newDataFactory(service.mapper().getClass(API.HISTORY))).in(dataType).out(dataType);
        ( keysDF             = env.newDataFactory(service.mapper().getClass(API.KEYS))).in(dataType).out(dataType);
        (delgDF             = env.newDataFactory(service.mapper().getClass(API.DELGS))).in(dataType).out(dataType);
        (approvalDF         = env.newDataFactory(service.mapper().getClass(API.APPROVALS))).in(dataType).out(dataType);
        (errDF                 = env.newDataFactory(service.mapper().getClass(API.ERROR))).in(dataType).out(dataType);
        (apiDF                = env.newDataFactory(Api.class)).in(dataType).out(dataType);
    }
    
    public Mapper<NSS,PERMS,PERMKEY,ROLES,USERS,USERROLES,DELGS,CERTS,KEYS,REQUEST,HISTORY,ERR,APPROVALS> mapper() {
        return service.mapper();
    }
    
    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#error(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, int)
     * 
     * Note: Conforms to AT&T TSS RESTful Error Structure
     */
    @Override
    public void error(AuthzTrans trans, HttpServletResponse response, Result<?> result) {
        String msg = result.details==null?"%s":"%s - " + result.details.trim();
        String msgId;
        String[] detail;
        boolean hidemsg = false;
        if (result.variables==null || result.variables.length<1) {
            detail = new String[1];
        } else {
            List<String> dlist = new ArrayList<String>();
            dlist.add(null);
            String os;
            for(Object s : result.variables) {
                if(s!=null && (os=s.toString()).length()>0) {
                    dlist.add(os);
                }
            }
            detail = new String[dlist.size()];
            dlist.toArray(detail);
        }
        //int httpstatus;
        
        switch(result.status) {
            case ERR_ActionNotCompleted:
                msgId = "SVC1202";
                detail[0] = "Accepted, Action not complete";
                response.setStatus(/*httpstatus=*/202);
                break;

            case ERR_Policy:
                msgId = "SVC3403";
                detail[0] = FORBIDDEN;
                response.setStatus(/*httpstatus=*/403);
                break;
            case ERR_Security:
                msgId = "SVC2403";
                detail[0] = FORBIDDEN;
                response.setStatus(/*httpstatus=*/403);
                break;
            case ERR_Denied:
                msgId = "SVC1403";
                detail[0] = FORBIDDEN;
                response.setStatus(/*httpstatus=*/403);
                break;
            // This is still forbidden to directly impact, but can be Requested when passed
            // with "request=true" query Param
            case ERR_FutureNotRequested:
                msgId = "SVC2403";
                detail[0] = msg;
                response.setStatus(/*httpstatus=*/403);
                break;
                
            case ERR_NsNotFound:
                msgId = "SVC2404";
                detail[0] = NOT_FOUND;
                response.setStatus(/*httpstatus=*/404);
                break;
            case ERR_RoleNotFound:
                msgId = "SVC3404";
                detail[0] = NOT_FOUND;
                response.setStatus(/*httpstatus=*/404);
                break;
            case ERR_PermissionNotFound:
                msgId = "SVC4404";
                detail[0] = NOT_FOUND;
                response.setStatus(/*httpstatus=*/404);
                break;
            case ERR_UserNotFound:
                msgId = "SVC5404";
                detail[0] = NOT_FOUND;
                response.setStatus(/*httpstatus=*/404);
                break;
            case ERR_UserRoleNotFound:
                msgId = "SVC6404";
                detail[0] = NOT_FOUND;
                response.setStatus(/*httpstatus=*/404);
                break;
            case ERR_DelegateNotFound:
                msgId = "SVC7404";
                detail[0] = NOT_FOUND;
                response.setStatus(/*httpstatus=*/404);
                break;
            case ERR_NotFound:
                msgId = "SVC1404";
                detail[0] = NOT_FOUND;
                response.setStatus(/*httpstatus=*/404);
                break;

            case ERR_InvalidDelegate:
                msgId="SVC2406";
                detail[0] = NOT_ACCEPTABLE;
                response.setStatus(/*httpstatus=*/406);
                break;
            case ERR_BadData:
                msgId="SVC1406";
                detail[0] = NOT_ACCEPTABLE;
                response.setStatus(/*httpstatus=*/406);
                break;
                
            case ERR_ConflictAlreadyExists:
                msgId = "SVC1409";
                detail[0] = "Conflict Already Exists";
                response.setStatus(/*httpstatus=*/409);
                break;
            
            case ERR_DependencyExists:
                msgId = "SVC1424";
                detail[0] = "Failed Dependency";
                response.setStatus(/*httpstatus=*/424);
                break;
            
            case ERR_NotImplemented:
                msgId = "SVC1501";
                detail[0] = "Not Implemented"; 
                response.setStatus(/*httpstatus=*/501);
                break;
                
            case Status.ACC_Future:
                msgId = "SVC1202";
                detail[0] = "Accepted for Future, pending Approvals";
                response.setStatus(/*httpstatus=*/202);
                break;
            case ERR_ChoiceNeeded:
                msgId = "SVC1300";
                detail[0] = "Choice Needed";
                response.setStatus(/*httpstatus=*/300);
                break;
            case ERR_Backend: 
                msgId = "SVC2500";
                detail[0] = GENERAL_SERVICE_ERROR;
                response.setStatus(/*httpstatus=*/500);
                hidemsg = true;
                break;

            default: 
                msgId = "SVC1500";
                detail[0] = GENERAL_SERVICE_ERROR;
                response.setStatus(/*httpstatus=*/500);
                hidemsg = true;
                break;
        }

        try {
            StringBuilder holder = new StringBuilder();
            ERR em = service.mapper().errorFromMessage(holder,msgId,msg,detail);
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
            trans.error().log(e,"unable to send response for",msg);
        }
    }
    
    ///////////////////////////
    // Namespace
    ///////////////////////////
    public static final String CREATE_NS = "createNamespace";
    public static final String ADD_NS_ADMIN = "addNamespaceAdmin";
    public static final String DELETE_NS_ADMIN = "delNamespaceAdmin";
    public static final String ADD_NS_RESPONSIBLE = "addNamespaceResponsible";
    public static final String DELETE_NS_RESPONSIBLE = "delNamespaceResponsible";
    public static final String GET_NS_BY_NAME = "getNamespaceByName";
    public static final String GET_NS_BY_ADMIN = "getNamespaceByAdmin";
    public static final String GET_NS_BY_RESPONSIBLE = "getNamespaceByResponsible";
    public static final String GET_NS_BY_EITHER = "getNamespaceByEither";
    public static final String GET_NS_CHILDREN = "getNamespaceChildren";
    public static final String UPDATE_NS_DESC = "updateNamespaceDescription";
    public static final String DELETE_NS = "deleteNamespace";
    

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#createNS(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Result<Void> requestNS(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, NsType type) {
        TimeTaken tt = trans.start(CREATE_NS, Env.SUB|Env.ALWAYS);
        try {
            REQUEST request;
            try {
                Data<REQUEST> rd = nsRequestDF.newData().load(req.getInputStream());
                if (Question.willSpecialLog(trans, trans.user())) {
                    Question.logEncryptTrace(trans,rd.asString());
                }
                request = rd.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,CREATE_NS);
                return Result.err(Status.ERR_BadData,"Invalid Input");
            }
            
            Result<Void> rp = service.createNS(trans,request,type);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,nsRequestDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,CREATE_NS);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#addAdminToNS(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String, java.lang.String)
     */
    @Override
    public Result<Void> addAdminToNS(AuthzTrans trans, HttpServletResponse resp, String ns, String id) {
        TimeTaken tt = trans.start(ADD_NS_ADMIN + ' ' + ns + ' ' + id, Env.SUB|Env.ALWAYS);
        try {
            Result<Void> rp = service.addAdminNS(trans,ns,id);
            switch(rp.status) {
                case OK: 
                    //TODO Perms??
                    setContentType(resp,nsRequestDF.getOutType());
                    resp.getOutputStream().println();
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,ADD_NS_ADMIN);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#delAdminFromNS(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String, java.lang.String)
     */
    @Override
    public Result<Void> delAdminFromNS(AuthzTrans trans, HttpServletResponse resp, String ns, String id) {
        TimeTaken tt = trans.start(DELETE_NS_ADMIN + ' ' + ns + ' ' + id, Env.SUB|Env.ALWAYS);
        try {
            Result<Void> rp = service.delAdminNS(trans, ns, id);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,nsRequestDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_NS_ADMIN);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#addAdminToNS(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String, java.lang.String)
     */
    @Override
    public Result<Void> addResponsibilityForNS(AuthzTrans trans, HttpServletResponse resp, String ns, String id) {
        TimeTaken tt = trans.start(ADD_NS_RESPONSIBLE + ' ' + ns + ' ' + id, Env.SUB|Env.ALWAYS);
        try {
            Result<Void> rp = service.addResponsibleNS(trans,ns,id);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,nsRequestDF.getOutType());
                    resp.getOutputStream().println();
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,ADD_NS_RESPONSIBLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#delAdminFromNS(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String, java.lang.String)
     */
    @Override
    public Result<Void> delResponsibilityForNS(AuthzTrans trans, HttpServletResponse resp, String ns, String id) {
        TimeTaken tt = trans.start(DELETE_NS_RESPONSIBLE + ' ' + ns + ' ' + id, Env.SUB|Env.ALWAYS);
        try {
            Result<Void> rp = service.delResponsibleNS(trans, ns, id);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,nsRequestDF.getOutType());
                    resp.getOutputStream().println();
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_NS_RESPONSIBLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getNSsByName(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getNSsByName(AuthzTrans trans, HttpServletResponse resp, String ns, boolean full) {
        TimeTaken tt = trans.start(GET_NS_BY_NAME + ' ' + ns, Env.SUB|Env.ALWAYS);
        try {
            Result<NSS> rp = service.getNSbyName(trans, ns, full );
            switch(rp.status) {
                case OK: 
                    RosettaData<NSS> data = nssDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,nssDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_NS_BY_NAME);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
//    TODO: uncomment when on cassandra 2.1.2 for MyNamespace GUI page
    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getNSsByAdmin(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getNSsByAdmin(AuthzTrans trans, HttpServletResponse resp, String user, boolean full){
        TimeTaken tt = trans.start(GET_NS_BY_ADMIN + ' ' + user, Env.SUB|Env.ALWAYS);
        try {
            Result<NSS> rp = service.getNSbyAdmin(trans, user, full);
            switch(rp.status) {
                case OK: 
                    RosettaData<NSS> data = nssDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,nssDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_NS_BY_ADMIN);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
//    TODO: uncomment when on cassandra 2.1.2 for MyNamespace GUI page
    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getNSsByResponsible(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getNSsByResponsible(AuthzTrans trans, HttpServletResponse resp, String user, boolean full){
        TimeTaken tt = trans.start(GET_NS_BY_RESPONSIBLE + ' ' + user, Env.SUB|Env.ALWAYS);
        try {
            Result<NSS> rp = service.getNSbyResponsible(trans, user, full);
            switch(rp.status) {
                case OK: 
                    RosettaData<NSS> data = nssDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());

                    setContentType(resp,nssDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_NS_BY_RESPONSIBLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getNSsByResponsible(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getNSsByEither(AuthzTrans trans, HttpServletResponse resp, String user, boolean full){
        TimeTaken tt = trans.start(GET_NS_BY_EITHER + ' ' + user, Env.SUB|Env.ALWAYS);
        try {
            Result<NSS> rp = service.getNSbyEither(trans, user, full);
            
            switch(rp.status) {
                case OK: 
                    RosettaData<NSS> data = nssDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());

                    setContentType(resp,nssDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_NS_BY_EITHER);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getNSsByResponsible(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getNSsChildren(AuthzTrans trans, HttpServletResponse resp, String parent){
        TimeTaken tt = trans.start(GET_NS_CHILDREN + ' ' + parent, Env.SUB|Env.ALWAYS);
        try {
            Result<NSS> rp = service.getNSsChildren(trans, parent);
            switch(rp.status) {
                case OK: 
                    RosettaData<NSS> data = nssDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,nssDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_NS_CHILDREN);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> updateNsDescription(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(UPDATE_NS_DESC, Env.SUB|Env.ALWAYS);
        try {
            REQUEST rreq;
            try {
                RosettaData<REQUEST> data = nsRequestDF.newData().load(req.getInputStream());
                if (Question.willSpecialLog(trans, trans.user())) {
                    Question.logEncryptTrace(trans,data.asString());
                }
                rreq = data.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,UPDATE_NS_DESC);
                return Result.err(Status.ERR_BadData,"Invalid Input");

            }
            Result<Void> rp = service.updateNsDescription(trans, rreq);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,nsRequestDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,UPDATE_NS_DESC);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#requestNS(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Result<Void> deleteNS(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, String ns) {
        TimeTaken tt = trans.start(DELETE_NS + ' ' + ns, Env.SUB|Env.ALWAYS);
        try {
            Result<Void> rp = service.deleteNS(trans,ns);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,nsRequestDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_NS);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    private final static String NS_CREATE_ATTRIB = "nsCreateAttrib";
    private final static String NS_UPDATE_ATTRIB = "nsUpdateAttrib";
    private final static String READ_NS_BY_ATTRIB = "readNsByAttrib";
    private final static String NS_DELETE_ATTRIB = "nsDeleteAttrib";
    
    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#createAttribForNS(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Result<Void> createAttribForNS(AuthzTrans trans, HttpServletResponse resp, String ns, String key, String value) {
        TimeTaken tt = trans.start(NS_CREATE_ATTRIB + ' ' + ns + ':'+key+':'+value, Env.SUB|Env.ALWAYS);
        try {
            Result<?> rp = service.createNsAttrib(trans,ns,key,value);
            switch(rp.status) {
                case OK: 
                    setContentType(resp, keysDF.getOutType());
                    resp.getOutputStream().println();
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,NS_CREATE_ATTRIB);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#readAttribForNS(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> readNsByAttrib(AuthzTrans trans, HttpServletResponse resp, String key) {
        TimeTaken tt = trans.start(READ_NS_BY_ATTRIB + ' ' + key, Env.SUB|Env.ALWAYS);
        try {
            Result<KEYS> rp = service.readNsByAttrib(trans, key);
            switch(rp.status) {
                case OK: 
                    RosettaData<KEYS> data = keysDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,keysDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,READ_NS_BY_ATTRIB);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#updAttribForNS(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Result<Void> updAttribForNS(AuthzTrans trans, HttpServletResponse resp, String ns, String key, String value) {
        TimeTaken tt = trans.start(NS_UPDATE_ATTRIB + ' ' + ns + ':'+key+':'+value, Env.SUB|Env.ALWAYS);
        try {
            Result<?> rp = service.updateNsAttrib(trans,ns,key,value);
            switch(rp.status) {
                case OK: 
                    setContentType(resp, keysDF.getOutType());
                    resp.getOutputStream().println();
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,NS_UPDATE_ATTRIB);
            return Result.err(e);
        } finally {
            tt.done();
        }

    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#delAttribForNS(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String, java.lang.String)
     */
    @Override
    public Result<Void> delAttribForNS(AuthzTrans trans, HttpServletResponse resp, String ns, String key) {
        TimeTaken tt = trans.start(NS_DELETE_ATTRIB + ' ' + ns + ':'+key, Env.SUB|Env.ALWAYS);
        try {
            Result<?> rp = service.deleteNsAttrib(trans,ns,key);
            switch(rp.status) {
                case OK: 
                    setContentType(resp, keysDF.getOutType());
                    resp.getOutputStream().println();
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,NS_DELETE_ATTRIB);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

//
// PERMISSION
//
    public static final String CREATE_PERMISSION = "createPermission";
    public static final String GET_PERMS_BY_TYPE = "getPermsByType";
    public static final String GET_PERMS_BY_NAME = "getPermsByName";
    public static final String GET_PERMISSIONS_BY_USER = "getPermissionsByUser";
    public static final String GET_PERMISSIONS_BY_USER_SCOPE = "getPermissionsByUserScope";
    public static final String GET_PERMISSIONS_BY_USER_WITH_QUERY = "getPermissionsByUserWithQuery";
    public static final String GET_PERMISSIONS_BY_ROLE = "getPermissionsByRole";
    public static final String GET_PERMISSIONS_BY_NS = "getPermissionsByNS";
    public static final String UPDATE_PERMISSION = "updatePermission";
    public static final String UPDATE_PERM_DESC = "updatePermissionDescription";
    public static final String SET_PERMISSION_ROLES_TO = "setPermissionRolesTo";
    public static final String DELETE_PERMISSION = "deletePermission";
    
    /*
     * (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#createOrUpdatePerm(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, boolean, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Result<Void> createPerm(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start( CREATE_PERMISSION, Env.SUB|Env.ALWAYS);    
        try {
            REQUEST rreq;
            try {
                RosettaData<REQUEST> data = permRequestDF.newData().load(req.getInputStream());
                if (Question.willSpecialLog(trans, trans.user())) {
                    Question.logEncryptTrace(trans,data.asString());
                }
                rreq = data.asObject();            
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,CREATE_PERMISSION);
                return Result.err(Status.ERR_BadData,"Invalid Input");
            }
            
            Result<Void> rp = service.createPerm(trans,rreq);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,permsDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,CREATE_PERMISSION);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getChildPerms(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getPermsByType(AuthzTrans trans, HttpServletResponse resp, String perm) {
        TimeTaken tt = trans.start(GET_PERMS_BY_TYPE + ' ' + perm, Env.SUB|Env.ALWAYS);
        try {
            
            Result<PERMS> rp = service.getPermsByType(trans, perm);
            switch(rp.status) {
                case OK:
                    RosettaData<PERMS> data = permsDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,permsDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_PERMS_BY_TYPE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    @Override
    public Result<Void> getPermsByName(AuthzTrans trans, HttpServletResponse resp, 
            String type, String instance, String action) {
        
        TimeTaken tt = trans.start(GET_PERMS_BY_NAME + ' ' + type
                + '|' + instance + '|' + action, Env.SUB|Env.ALWAYS);
        try {
            
            Result<PERMS> rp = service.getPermsByName(trans, type, instance, action);
            switch(rp.status) {
                case OK:
                    RosettaData<PERMS> data = permsDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,permsDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_PERMS_BY_TYPE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getPermissionByUser(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getPermsByUser(AuthzTrans trans, HttpServletResponse resp,    String user) {
        TimeTaken tt = trans.start(GET_PERMISSIONS_BY_USER + ' ' + user, Env.SUB|Env.ALWAYS);
        try {
            Result<PERMS> rp = service.getPermsByUser(trans, user);
            switch(rp.status) {
                case OK: 
                    RosettaData<PERMS> data = permsDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,permsDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_PERMISSIONS_BY_USER, user);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getPermissionByUser(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getPermsByUserScope(AuthzTrans trans, HttpServletResponse resp, String user, String[] scopes) {
        TimeTaken tt = trans.start(GET_PERMISSIONS_BY_USER_SCOPE + ' ' + user, Env.SUB|Env.ALWAYS);
        try {
            Result<PERMS> rp = service.getPermsByUserScope(trans, user, scopes);
            switch(rp.status) {
                case OK: 
                    RosettaData<PERMS> data = permsDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,permsDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_PERMISSIONS_BY_USER_SCOPE, user);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }


    
    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getPermissionByUser(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getPermsByUserWithAAFQuery(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, String user) {
        TimeTaken tt = trans.start(GET_PERMISSIONS_BY_USER_WITH_QUERY + ' ' + user, Env.SUB|Env.ALWAYS);
        try {
            PERMS perms;
            try {
                RosettaData<PERMS> data = permsDF.newData().load(req.getInputStream());
                if (Question.willSpecialLog(trans, trans.user())) {
                    Question.logEncryptTrace(trans,data.asString());
                }
                perms = data.asObject();            
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,GET_PERMISSIONS_BY_USER_WITH_QUERY);
                return Result.err(Status.ERR_BadData,"Invalid Input");
            }

            Result<PERMS> rp = service.getPermsByUser(trans, perms, user);
            switch(rp.status) {
                case OK: 
                    RosettaData<PERMS> data = permsDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,permsDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_PERMISSIONS_BY_USER_WITH_QUERY , user);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    
    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getPermissionsForRole(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getPermsForRole(AuthzTrans trans, HttpServletResponse resp,    String roleName) {
        TimeTaken tt = trans.start(GET_PERMISSIONS_BY_ROLE + ' ' + roleName, Env.SUB|Env.ALWAYS);
        try {
            Result<PERMS> rp = service.getPermsByRole(trans, roleName);
            switch(rp.status) {
                case OK:
                    RosettaData<PERMS> data = permsDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,permsDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_PERMISSIONS_BY_ROLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    @Override
    public Result<Void> getPermsByNS(AuthzTrans trans,HttpServletResponse resp,String ns) {
        TimeTaken tt = trans.start(GET_PERMISSIONS_BY_NS + ' ' + ns, Env.SUB|Env.ALWAYS);
        try {
            Result<PERMS> rp = service.getPermsByNS(trans, ns);
            switch(rp.status) {
                case OK:
                    RosettaData<PERMS> data = permsDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,permsDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_PERMISSIONS_BY_NS);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#createOrUpdatePerm(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, boolean, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Result<Void> renamePerm(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp,
            String origType, String origInstance, String origAction) {
        String cmdDescription = UPDATE_PERMISSION;
        TimeTaken tt = trans.start( cmdDescription    + ' ' + origType + ' ' + origInstance + ' ' + origAction, Env.SUB|Env.ALWAYS);    
        try {
            REQUEST rreq;
            try {
                RosettaData<REQUEST> data = permRequestDF.newData().load(req.getInputStream());
                if (Question.willSpecialLog(trans, trans.user())) {
                    Question.logEncryptTrace(trans,data.asString());
                }
                rreq = data.asObject();            
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,cmdDescription);
                return Result.err(Status.ERR_BadData,"Invalid Input");
            }
            
            Result<Void> rp = service.renamePerm(trans,rreq, origType, origInstance, origAction);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,permsDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,cmdDescription);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    @Override
    public Result<Void> updatePermDescription(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(UPDATE_PERM_DESC, Env.SUB|Env.ALWAYS);
        try {
            REQUEST rreq;
            try {
                RosettaData<REQUEST> data = permRequestDF.newData().load(req.getInputStream());
                if (Question.willSpecialLog(trans, trans.user())) {
                    Question.logEncryptTrace(trans,data.asString());
                }
                rreq = data.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,UPDATE_PERM_DESC);
                return Result.err(Status.ERR_BadData,"Invalid Input");

            }
            Result<Void> rp = service.updatePermDescription(trans, rreq);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,permRequestDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,UPDATE_PERM_DESC);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    
    @Override
    public Result<Void> resetPermRoles(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(SET_PERMISSION_ROLES_TO, Env.SUB|Env.ALWAYS);
        try {
            REQUEST rreq;
            try {
                RosettaData<REQUEST> data = rolePermRequestDF.newData().load(req.getInputStream());
                if (Question.willSpecialLog(trans, trans.user())) {
                    Question.logEncryptTrace(trans,data.asString());
                }
                rreq = data.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN, SET_PERMISSION_ROLES_TO);
                return Result.err(Status.ERR_BadData,"Invalid Input");
            }
            
            Result<Void> rp = service.resetPermRoles(trans, rreq);
            
            switch(rp.status) {
                case OK: 
                    setContentType(resp,permsDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,SET_PERMISSION_ROLES_TO);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    @Override
    public Result<Void> deletePerm(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(DELETE_PERMISSION, Env.SUB|Env.ALWAYS);
        try {
            REQUEST rreq;
            try {
                RosettaData<REQUEST> data = permRequestDF.newData().load(req.getInputStream());
                if (Question.willSpecialLog(trans, trans.user())) {
                    Question.logEncryptTrace(trans,data.asString());
                }
                rreq = data.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,DELETE_PERMISSION);
                return Result.err(Status.ERR_BadData,"Invalid Input");

            }

            Result<Void> rp = service.deletePerm(trans,rreq);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,permsDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_PERMISSION);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> deletePerm(AuthzTrans trans, HttpServletResponse resp, String type, String instance, String action) {
        TimeTaken tt = trans.start(DELETE_PERMISSION + type + ' ' + instance + ' ' + action, Env.SUB|Env.ALWAYS);
        try {
            Result<Void> rp = service.deletePerm(trans,type,instance,action);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,permsDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_PERMISSION);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    public static final String CREATE_ROLE = "createRole";
    public static final String GET_ROLES_BY_USER = "getRolesByUser";
    public static final String GET_ROLES_BY_NS = "getRolesByNS";
    public static final String GET_ROLES_BY_NAME_ONLY = "getRolesByNameOnly";
    public static final String GET_ROLES_BY_NAME = "getRolesByName";
    public static final String GET_ROLES_BY_PERM = "getRolesByPerm";
    public static final String UPDATE_ROLE_DESC = "updateRoleDescription"; 
    public static final String ADD_PERM_TO_ROLE = "addPermissionToRole";
    public static final String DELETE_PERM_FROM_ROLE = "deletePermissionFromRole";
    public static final String UPDATE_MGTPERM_ROLE = "updateMgtPermRole";
    public static final String DELETE_ROLE = "deleteRole";
    public static final String GET_CERT_BY_ID = "getCertByID";

    @Override
    public Result<Void> createRole(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(CREATE_ROLE, Env.SUB|Env.ALWAYS);
        try {
            REQUEST rreq;
            try {
                RosettaData<REQUEST> data = roleRequestDF.newData().load(req.getInputStream());
                if (Question.willSpecialLog(trans, trans.user())) {
                    Question.logEncryptTrace(trans,data.asString());
                }
                rreq = data.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,CREATE_ROLE);
                return Result.err(Status.ERR_BadData,"Invalid Input");

            }
            Result<Void> rp = service.createRole(trans, rreq);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,roleRequestDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,CREATE_ROLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getRolesByName(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getRolesByName(AuthzTrans trans, HttpServletResponse resp, String role) {
        TimeTaken tt = trans.start(GET_ROLES_BY_NAME + ' ' + role, Env.SUB|Env.ALWAYS);
        try {
            Result<ROLES> rp = service.getRolesByName(trans, role);
            switch(rp.status) {
                case OK: 
                    RosettaData<ROLES> data = roleDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,roleDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_ROLES_BY_NAME);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getRolesByUser(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getRolesByUser(AuthzTrans trans,HttpServletResponse resp, String user) {
        TimeTaken tt = trans.start(GET_ROLES_BY_USER + ' ' + user, Env.SUB|Env.ALWAYS);
        try {
            Result<ROLES> rp = service.getRolesByUser(trans, user);
            switch(rp.status) {
                case OK: 
                    RosettaData<ROLES> data = roleDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,roleDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_ROLES_BY_USER, user);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getRolesByUser(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getRolesByNS(AuthzTrans trans,HttpServletResponse resp, String ns) {
        TimeTaken tt = trans.start(GET_ROLES_BY_NS + ' ' + ns, Env.SUB|Env.ALWAYS);
        try {
            Result<ROLES> rp = service.getRolesByNS(trans, ns);
            switch(rp.status) {
                case OK: 
                    if (!rp.isEmpty()) {
                        RosettaData<ROLES> data = roleDF.newData(trans).load(rp.value);
                        if (Question.willSpecialLog(trans, trans.user())) {
                            Question.logEncryptTrace(trans,data.asString());
                        }
                        data.to(resp.getOutputStream());
                    } else {
                        Question.logEncryptTrace(trans, NO_DATA);
                    }
                    setContentType(resp,roleDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_ROLES_BY_NS);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }


    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getRolesByNameOnly(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getRolesByNameOnly(AuthzTrans trans,HttpServletResponse resp, String nameOnly) {
        TimeTaken tt = trans.start(GET_ROLES_BY_NAME_ONLY + ' ' + nameOnly, Env.SUB|Env.ALWAYS);
        try {
            Result<ROLES> rp = service.getRolesByNameOnly(trans, nameOnly);
            switch(rp.status) {
                case OK: 
                    if (!rp.isEmpty()) {
                        RosettaData<ROLES> data = roleDF.newData(trans).load(rp.value);
                        if (Question.willSpecialLog(trans, trans.user())) {
                            Question.logEncryptTrace(trans,data.asString());
                        }
                        data.to(resp.getOutputStream());
                    } else {
                        Question.logEncryptTrace(trans, NO_DATA);
                    }
                    setContentType(resp,roleDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_ROLES_BY_NAME_ONLY);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getRolesByUser(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getRolesByPerm(AuthzTrans trans,HttpServletResponse resp, String type, String instance, String action) {
        TimeTaken tt = trans.start(GET_ROLES_BY_PERM + type +' '+instance+' '+action, Env.SUB|Env.ALWAYS);
        try {
            Result<ROLES> rp = service.getRolesByPerm(trans, type,instance,action);
            switch(rp.status) {
                case OK: 
                    RosettaData<ROLES> data = roleDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,roleDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_ROLES_BY_PERM);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /*
     * (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#updateDescription(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Result<Void> updateRoleDescription(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(UPDATE_ROLE_DESC, Env.SUB|Env.ALWAYS);
        try {
            REQUEST rreq;
            try {
                RosettaData<REQUEST> data = roleRequestDF.newData().load(req.getInputStream());
                if (Question.willSpecialLog(trans, trans.user())) {
                    Question.logEncryptTrace(trans,data.asString());
                }
                rreq = data.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,UPDATE_ROLE_DESC);
                return Result.err(Status.ERR_BadData,"Invalid Input");

            }
            Result<Void> rp = service.updateRoleDescription(trans, rreq);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,roleRequestDF.getOutType());
                    return Result.ok();
                default:
                    return rp;
            }
        } catch (Exception e) {
            trans.error().log(e,IN,UPDATE_ROLE_DESC);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> addPermToRole(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(ADD_PERM_TO_ROLE, Env.SUB|Env.ALWAYS);
        try {
            REQUEST rreq;
            try {
                RosettaData<REQUEST> data = rolePermRequestDF.newData().load(req.getInputStream());
                if (Question.willSpecialLog(trans, trans.user())) {
                    Question.logEncryptTrace(trans,data.asString());
                }
                rreq = data.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,ADD_PERM_TO_ROLE);
                return Result.err(Status.ERR_BadData,"Invalid Input");

            }
            Result<Void> rp = service.addPermToRole(trans, rreq);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,permsDF.getOutType());
                    resp.getOutputStream().println();
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,ADD_PERM_TO_ROLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> delPermFromRole(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(DELETE_PERM_FROM_ROLE, Env.SUB|Env.ALWAYS);
        try {
            REQUEST rreq;
            try {
                RosettaData<REQUEST> data = rolePermRequestDF.newData().load(req.getInputStream());
                if (Question.willSpecialLog(trans, trans.user())) {
                    Question.logEncryptTrace(trans,data.asString());
                }
                rreq = data.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,DELETE_PERM_FROM_ROLE);
                return Result.err(Status.ERR_BadData,"Invalid Input");

            }
            Result<Void> rp = service.delPermFromRole(trans, rreq);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,permsDF.getOutType());
                    resp.getOutputStream().println();
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_PERM_FROM_ROLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#delPermFromRole(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Result<Void> delPermFromRole(AuthzTrans trans, HttpServletResponse resp, String role, String type,
            String instance, String action) {
        TimeTaken tt = trans.start(DELETE_PERM_FROM_ROLE, Env.SUB|Env.ALWAYS);
        try {
            Result<Void> rp = service.delPermFromRole(trans, role, type, instance, action);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,permsDF.getOutType());
                    resp.getOutputStream().println();
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_PERM_FROM_ROLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> deleteRole(AuthzTrans trans, HttpServletResponse resp, String role) {
        TimeTaken tt = trans.start(DELETE_ROLE + ' ' + role, Env.SUB|Env.ALWAYS);
        try {
            Result<Void> rp = service.deleteRole(trans, role);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,permsDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_ROLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> deleteRole(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(DELETE_ROLE, Env.SUB|Env.ALWAYS);
        try {
            REQUEST rreq;
            try {
                RosettaData<REQUEST> data = roleRequestDF.newData().load(req.getInputStream());
                if (Question.willSpecialLog(trans, trans.user())) {
                    Question.logEncryptTrace(trans,data.asString());
                }
                rreq = data.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,CREATE_ROLE);
                return Result.err(Status.ERR_BadData,"Invalid Input");
            }

            Result<Void> rp = service.deleteRole(trans, rreq);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,permsDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_ROLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    public static final String CREATE_CRED = "createUserCred";
    private static final String GET_CREDS_BY_NS = "getCredsByNS";
    private static final String GET_CREDS_BY_ID = "getCredsByID";
    public static final String UPDATE_CRED = "updateUserCred";
    public static final String EXTEND_CRED = "extendUserCred";
    public static final String DELETE_CRED = "deleteUserCred";
    public static final String DOES_CRED_MATCH = "doesCredMatch";
    public static final String VALIDATE_BASIC_AUTH = "validateBasicAuth";



    @Override
    /**
     * Create Credential
     * 
     */
    public Result<Void> createUserCred(AuthzTrans trans, HttpServletRequest req) {
        TimeTaken tt = trans.start(CREATE_CRED, Env.SUB|Env.ALWAYS);
        try {
            RosettaData<REQUEST> data = credRequestDF.newData().load(req.getInputStream());
            if (Question.willSpecialLog(trans, trans.user())) {
                Question.logEncryptTrace(trans,data.asString());
            }
            return service.createUserCred(trans, data.asObject());
        } catch (APIException e) {
            trans.error().log(e,"Bad Input data");
            return Result.err(Status.ERR_BadData, e.getLocalizedMessage());
        } catch (Exception e) {
            trans.error().log(e,IN,CREATE_CRED);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> changeUserCred(AuthzTrans trans, HttpServletRequest req) {
        TimeTaken tt = trans.start(UPDATE_CRED, Env.SUB|Env.ALWAYS);
        try {
            RosettaData<REQUEST> data = credRequestDF.newData().load(req.getInputStream());
            if (Question.willSpecialLog(trans, trans.user())) {
                Question.logEncryptTrace(trans,data.asString());
            }

            return service.resetUserCred(trans, data.asObject());
        } catch (APIException e) {
            trans.error().log(e,"Bad Input data");
            return Result.err(Status.ERR_BadData, e.getLocalizedMessage());
        } catch (Exception e) {
            trans.error().log(e,IN,UPDATE_CRED);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#extendUserCred(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, int)
     */
    @Override
    public Result<Void> extendUserCred(AuthzTrans trans, HttpServletRequest req, String days) {
        TimeTaken tt = trans.start(EXTEND_CRED, Env.SUB|Env.ALWAYS);
        try {
            RosettaData<REQUEST> data = credRequestDF.newData().load(req.getInputStream());
            if (Question.willSpecialLog(trans, trans.user())) {
                Question.logEncryptTrace(trans,data.asString());
            }

            return service.extendUserCred(trans, data.asObject(), days);
        } catch (APIException e) {
            trans.error().log(e,"Bad Input data");
            return Result.err(Status.ERR_BadData, e.getLocalizedMessage());
        } catch (Exception e) {
            trans.error().log(e,IN,EXTEND_CRED);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> getCredsByNS(AuthzTrans trans, HttpServletResponse resp, String ns) {
        TimeTaken tt = trans.start(GET_CREDS_BY_NS + ' ' + ns, Env.SUB|Env.ALWAYS);
        
        try {
            Result<USERS> ru = service.getCredsByNS(trans,ns);
            switch(ru.status) {
                case OK: 
                    RosettaData<USERS> data = usersDF.newData(trans).load(ru.value);
                    if (Question.willSpecialLog(trans,trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,usersDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(ru);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_CREDS_BY_NS);
            return Result.err(e);
        } finally {
            tt.done();
        }
        
    }
    
    
    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getCredsByID(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getCredsByID(AuthzTrans trans, HttpServletResponse resp, String id) {
        TimeTaken tt = trans.start(GET_CREDS_BY_ID + ' ' + id, Env.SUB|Env.ALWAYS);
        
        try {
            Result<USERS> ru = service.getCredsByID(trans,id);
            switch(ru.status) {
                case OK: 
                    RosettaData<USERS> data = usersDF.newData(trans).load(ru.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,usersDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(ru);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_CREDS_BY_ID);
            return Result.err(e);
        } finally {
            tt.done();
        }
        
    }

    @Override
    public Result<Void> deleteUserCred(AuthzTrans trans, HttpServletRequest req) {
        TimeTaken tt = trans.start(DELETE_CRED, Env.SUB|Env.ALWAYS);
        try {
            RosettaData<REQUEST> data = credRequestDF.newData().load(req.getInputStream());
            if (Question.willSpecialLog(trans, trans.user())) {
                Question.logEncryptTrace(trans,data.asString());
            }

            return service.deleteUserCred(trans, data.asObject());
        } catch (APIException e) {
            trans.error().log(e,"Bad Input data");
            return Result.err(Status.ERR_BadData, e.getLocalizedMessage());
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_CRED);
            return Result.err(e);
        } finally {
            tt.done();
        }    
    }
    
    
    @Override
    public Result<Date> doesCredentialMatch(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(DOES_CRED_MATCH, Env.SUB|Env.ALWAYS);
        try {
            RosettaData<REQUEST> data = credRequestDF.newData().load(req.getInputStream());
            if (Question.willSpecialLog(trans, trans.user())) {
                Question.logEncryptTrace(trans,data.asString());
            }

            return service.doesCredentialMatch(trans, data.asObject());
        } catch (APIException e) {
            trans.error().log(e,"Bad Input data");
            return Result.err(Status.ERR_BadData, e.getLocalizedMessage());
        } catch (IOException e) {
            trans.error().log(e,IN,DOES_CRED_MATCH);
            return Result.err(e);
        } finally {
            tt.done();
        }    
    }


    @Override
    public Result<Void> validBasicAuth(AuthzTrans trans, HttpServletResponse resp, String basicAuth) {
        TimeTaken tt = trans.start(VALIDATE_BASIC_AUTH, Env.SUB|Env.ALWAYS);
        try {
            Result<Date> result = service.validateBasicAuth(trans,basicAuth);
            switch(result.status){
                case OK:
                    resp.getOutputStream().write(Chrono.utcStamp(result.value).getBytes());
                    return Result.ok();
            }
            return Result.err(result);
        } catch (Exception e) {
            trans.error().log(e,IN,VALIDATE_BASIC_AUTH);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getCertInfoByID(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getCertInfoByID(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, String id) {
        TimeTaken tt = trans.start(GET_CERT_BY_ID, Env.SUB|Env.ALWAYS);
        try {    
            Result<CERTS> rci = service.getCertInfoByID(trans,req,id);
            
            switch(rci.status) {
                case OK: 
                    if (Question.willSpecialLog(trans, trans.user())) {
                        RosettaData<CERTS> data = certsDF.newData(trans).load(rci.value);
                        Question.logEncryptTrace(trans,data.asString());
                        data.to(resp.getOutputStream());
                    } else {
                        certsDF.direct(trans, rci.value, resp.getOutputStream());
                    }
                    setContentType(resp,certsDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rci);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_CERT_BY_ID);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    public static final String CREATE_DELEGATE = "createDelegate";
    public static final String UPDATE_DELEGATE = "updateDelegate";
    public static final String DELETE_DELEGATE = "deleteDelegate";
    public static final String GET_DELEGATE_USER = "getDelegatesByUser";
    public static final String GET_DELEGATE_DELG = "getDelegatesByDelegate";
    
    @Override
    public Result<Void> createDelegate(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(CREATE_DELEGATE, Env.SUB|Env.ALWAYS);
        try {    
            Data<REQUEST> data = delgRequestDF.newData().load(req.getInputStream());
            if (Question.willSpecialLog(trans, trans.user())) {
                Question.logEncryptTrace(trans,data.asString());
            }

            return service.createDelegate(trans, data.asObject());
        } catch (Exception e) {
            trans.error().log(e,IN,CREATE_DELEGATE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    @Override
    public Result<Void> updateDelegate(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(UPDATE_DELEGATE, Env.SUB|Env.ALWAYS);
        try {    
            Data<REQUEST> data = delgRequestDF.newData().load(req.getInputStream());
            if (Question.willSpecialLog(trans, trans.user())) {
                Question.logEncryptTrace(trans,data.asString());
            }

            return service.updateDelegate(trans, data.asObject());
        } catch (Exception e) {
            trans.error().log(e,IN,UPDATE_DELEGATE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    @Override
    public Result<Void> deleteDelegate(AuthzTrans trans,  HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(DELETE_DELEGATE, Env.SUB|Env.ALWAYS);
        try {
            Data<REQUEST> data = delgRequestDF.newData().load(req.getInputStream());
            if (Question.willSpecialLog(trans, trans.user())) {
                Question.logEncryptTrace(trans,data.asString());
            }

            return service.deleteDelegate(trans, data.asObject());
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_DELEGATE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    @Override
    public Result<Void> deleteDelegate(AuthzTrans trans, String userName) {
        TimeTaken tt = trans.start(DELETE_DELEGATE + ' ' + userName, Env.SUB|Env.ALWAYS);
        try {
            return service.deleteDelegate(trans, userName);
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_DELEGATE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    @Override
    public Result<Void> getDelegatesByUser(AuthzTrans trans, String user, HttpServletResponse resp) {
        TimeTaken tt = trans.start(GET_DELEGATE_USER, Env.SUB|Env.ALWAYS);
        try {
            Result<DELGS> rd = service.getDelegatesByUser(trans, user);
            
            switch(rd.status) {
                case OK: 
                    RosettaData<DELGS> data = delgDF.newData(trans).load(rd.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,delgDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rd);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_DELEGATE_USER);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> getDelegatesByDelegate(AuthzTrans trans, String delegate, HttpServletResponse resp) {
        TimeTaken tt = trans.start(GET_DELEGATE_DELG, Env.SUB|Env.ALWAYS);
        try {
            Result<DELGS> rd = service.getDelegatesByDelegate(trans, delegate);
            switch(rd.status) {
                case OK: 
                    RosettaData<DELGS> data = delgDF.newData(trans).load(rd.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    setContentType(resp,delgDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rd);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_DELEGATE_DELG);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    private static final String REQUEST_USER_ROLE = "createUserRole";
    private static final String GET_USERROLES = "getUserRoles";
    private static final String GET_USERROLES_BY_ROLE = "getUserRolesByRole";
    private static final String GET_USERROLES_BY_USER = "getUserRolesByUser";
//    private static final String SET_ROLES_FOR_USER = "setRolesForUser";
//    private static final String SET_USERS_FOR_ROLE = "setUsersForRole";
    private static final String EXTEND_USER_ROLE = "extendUserRole";
    private static final String DELETE_USER_ROLE = "deleteUserRole";
    @Override
    public Result<Void> requestUserRole(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(REQUEST_USER_ROLE, Env.SUB|Env.ALWAYS);
        try {
            REQUEST request;
            try {
                Data<REQUEST> data = userRoleRequestDF.newData().load(req.getInputStream());
                if (Question.willSpecialLog(trans, trans.user())) {
                    Question.logEncryptTrace(trans,data.asString());
                }

                request = data.asObject();
            } catch (APIException e) {
                return Result.err(Status.ERR_BadData,"Invalid Input");
            }
            
            Result<Void> rp = service.createUserRole(trans,request);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,permsDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,REQUEST_USER_ROLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    @Override
    public Result<Void> getUserInRole(AuthzTrans trans, HttpServletResponse resp, String user, String role) {
        TimeTaken tt = trans.start(GET_USERROLES + ' ' + user + '|' + role, Env.SUB|Env.ALWAYS);
        try {
            Result<USERS> ru = service.getUserInRole(trans,user,role);
            switch(ru.status) {
                case OK: 
                    RosettaData<USERS> data = usersDF.newData(trans).load(ru.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }

                    data.to(resp.getOutputStream());
                    setContentType(resp,usersDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(ru);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_USERROLES);
            return Result.err(e);
        } finally {
            tt.done();
        }

    }

    @Override
    public Result<Void> getUserRolesByUser(AuthzTrans trans, HttpServletResponse resp, String user) {
        TimeTaken tt = trans.start(GET_USERROLES_BY_USER + ' ' + user, Env.SUB|Env.ALWAYS);
        try {
            Result<USERROLES> ru = service.getUserRolesByUser(trans,user);
            switch(ru.status) {
                case OK: 
                    RosettaData<USERROLES> data = userrolesDF.newData(trans).load(ru.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }

                    data.to(resp.getOutputStream());
                    setContentType(resp,usersDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(ru);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_USERROLES_BY_USER);
            return Result.err(e);
        } finally {
            tt.done();
        }

    }
    
    @Override
    public Result<Void> getUserRolesByRole(AuthzTrans trans, HttpServletResponse resp, String role) {
        TimeTaken tt = trans.start(GET_USERROLES_BY_ROLE + ' ' + role, Env.SUB|Env.ALWAYS);
        try {
            Result<USERROLES> ru = service.getUserRolesByRole(trans,role);
            switch(ru.status) {
                case OK: 
                    RosettaData<USERROLES> data = userrolesDF.newData(trans).load(ru.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }

                    data.to(resp.getOutputStream());
                    setContentType(resp,usersDF.getOutType());
                    setCacheControlOff(resp);
                    return Result.ok();
                default:
                    return Result.err(ru);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_USERROLES_BY_ROLE);
            return Result.err(e);
        } finally {
            tt.done();
        }

    }
    

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#extendUserRoleExpiration(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, java.lang.String)
     */
    @Override
    public Result<Void> extendUserRoleExpiration(AuthzTrans trans, HttpServletResponse resp, String user, String role) {
        TimeTaken tt = trans.start(EXTEND_USER_ROLE + ' ' + user + ' ' + role, Env.SUB|Env.ALWAYS);
        try {
            return service.extendUserRole(trans,user,role);
        } catch (Exception e) {
            trans.error().log(e,IN,EXTEND_USER_ROLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> deleteUserRole(AuthzTrans trans, HttpServletResponse resp, String user, String role) {
        TimeTaken tt = trans.start(DELETE_USER_ROLE + ' ' + user + ' ' + role, Env.SUB|Env.ALWAYS);
        try {
            Result<Void> rp = service.deleteUserRole(trans,user,role);
            switch(rp.status) {
                case OK: 
                    setContentType(resp,permsDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_USER_ROLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    private static final String UPDATE_APPROVAL = "updateApproval";
    private static final String GET_APPROVALS_BY_USER = "getApprovalsByUser.";
    private static final String GET_APPROVALS_BY_TICKET = "getApprovalsByTicket.";
    private static final String GET_APPROVALS_BY_APPROVER = "getApprovalsByApprover.";
    
    @Override
    public Result<Void> updateApproval(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(UPDATE_APPROVAL, Env.SUB|Env.ALWAYS);
        try {
            Data<APPROVALS> data = approvalDF.newData().load(req.getInputStream());
            if (Question.willSpecialLog(trans, trans.user())) {
                Question.logEncryptTrace(trans,data.asString());
            }

            Result<Void> rp = service.updateApproval(trans, data.asObject());
            
            switch(rp.status) {
                case OK: 
                    setContentType(resp,approvalDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,UPDATE_APPROVAL);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    
    @Override
    public Result<Void> getApprovalsByUser(AuthzTrans trans, HttpServletResponse resp, String user) {
        TimeTaken tt = trans.start(GET_APPROVALS_BY_USER + ' ' + user, Env.SUB|Env.ALWAYS);
        try {
            Result<APPROVALS> rp = service.getApprovalsByUser(trans, user);
            switch(rp.status) {
                case OK: 
                    RosettaData<APPROVALS> data = approvalDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    
                    setContentType(resp,permsDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_APPROVALS_BY_USER, user);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> getApprovalsByApprover(AuthzTrans trans, HttpServletResponse resp, String approver) {
        TimeTaken tt = trans.start(GET_APPROVALS_BY_APPROVER + ' ' + approver, Env.SUB|Env.ALWAYS);
        try {
            Result<APPROVALS> rp = service.getApprovalsByApprover(trans, approver);
            switch(rp.status) {
                case OK: 
                    RosettaData<APPROVALS> data = approvalDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }

                    data.to(resp.getOutputStream());
                    setContentType(resp,permsDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_APPROVALS_BY_APPROVER,approver);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    @Override
    public Result<Void> getApprovalsByTicket(AuthzTrans trans, HttpServletResponse resp, String ticket) {
        TimeTaken tt = trans.start(GET_APPROVALS_BY_TICKET, Env.SUB|Env.ALWAYS);
        try {
            Result<APPROVALS> rp = service.getApprovalsByTicket(trans, ticket);
            switch(rp.status) {
                case OK: 
                    RosettaData<APPROVALS> data = approvalDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }

                    data.to(resp.getOutputStream());
                    setContentType(resp,permsDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_APPROVALS_BY_TICKET);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }


    
    public static final String GET_USERS_PERMISSION = "getUsersByPermission";
    public static final String GET_USERS_ROLE = "getUsersByRole";

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getUsersByRole(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getUsersByRole(AuthzTrans trans, HttpServletResponse resp, String role) {
        TimeTaken tt = trans.start(GET_USERS_ROLE + ' ' + role, Env.SUB|Env.ALWAYS);
        try {
            Result<USERS> ru = service.getUsersByRole(trans,role);
            switch(ru.status) {
                case OK: 
                    RosettaData<USERS> data = usersDF.newData(trans).load(ru.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }

                    data.to(resp.getOutputStream());
                    setContentType(resp,usersDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(ru);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_USERS_ROLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getUsersByPermission(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Result<Void> getUsersByPermission(AuthzTrans trans, HttpServletResponse resp, 
            String type, String instance, String action) {
        TimeTaken tt = trans.start(GET_USERS_PERMISSION + ' ' + type + ' ' + instance + ' ' +action, Env.SUB|Env.ALWAYS);
        try {
            Result<USERS> ru = service.getUsersByPermission(trans,type,instance,action);
            switch(ru.status) {
                case OK: 
                    RosettaData<USERS> data = usersDF.newData(trans).load(ru.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }

                    data.to(resp.getOutputStream());
                    setContentType(resp,usersDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(ru);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_USERS_PERMISSION);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    
    public static final String GET_HISTORY_USER = "getHistoryByUser";
    public static final String GET_HISTORY_ROLE = "getHistoryByRole";
    public static final String GET_HISTORY_PERM = "getHistoryByPerm";
    public static final String GET_HISTORY_NS = "getHistoryByNS";
    public static final String GET_HISTORY_SUBJECT = "getHistoryBySubject";
    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getHistoryByUser(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Result<Void> getHistoryByUser(AuthzTrans trans, HttpServletResponse resp, String user, int[] yyyymm, final int sort) {
        StringBuilder sb = new StringBuilder();
        sb.append(GET_HISTORY_USER);
        sb.append(' ');
        sb.append(user);
        sb.append(" for ");
        boolean first = true;
        for (int i : yyyymm) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(i);
        }
        TimeTaken tt = trans.start(sb.toString(), Env.SUB|Env.ALWAYS);

        try {
            Result<HISTORY> rh = service.getHistoryByUser(trans,user,yyyymm,sort);
            switch(rh.status) {
                case OK: 
                    RosettaData<HISTORY> data = historyDF.newData(trans).load(rh.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }

                    data.to(resp.getOutputStream());
                    setContentType(resp,historyDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rh);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_HISTORY_USER);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getHistoryByRole(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String, int[])
     */
    @Override
    public Result<Void> getHistoryByRole(AuthzTrans trans, HttpServletResponse resp, String role, int[] yyyymm, final int sort) {
        StringBuilder sb = new StringBuilder();
        sb.append(GET_HISTORY_ROLE);
        sb.append(' ');
        sb.append(role);
        sb.append(" for ");
        boolean first = true;
        for (int i : yyyymm) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(i);
        }
        TimeTaken tt = trans.start(sb.toString(), Env.SUB|Env.ALWAYS);
        try {
            Result<HISTORY> rh = service.getHistoryByRole(trans,role,yyyymm,sort);
            switch(rh.status) {
                case OK: 
                    RosettaData<HISTORY> data = historyDF.newData(trans).load(rh.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }

                    data.to(resp.getOutputStream());
                    setContentType(resp,historyDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rh);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_HISTORY_ROLE);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getHistoryByNS(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String, int[])
     */
    @Override
    public Result<Void> getHistoryByNS(AuthzTrans trans, HttpServletResponse resp, String ns, int[] yyyymm, final int sort) {
        StringBuilder sb = new StringBuilder();
        sb.append(GET_HISTORY_NS);
        sb.append(' ');
        sb.append(ns);
        sb.append(" for ");
        boolean first = true;
        for (int i : yyyymm) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(i);
        }
        TimeTaken tt = trans.start(sb.toString(), Env.SUB|Env.ALWAYS);
        try {
            Result<HISTORY> rh = service.getHistoryByNS(trans,ns,yyyymm,sort);
            switch(rh.status) {
                case OK: 
                    RosettaData<HISTORY> data = historyDF.newData(trans).load(rh.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }

                    data.to(resp.getOutputStream());
                    setContentType(resp,historyDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rh);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_HISTORY_NS);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getHistoryByPerm(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String, int[])
     */
    @Override
    public Result<Void> getHistoryByPerm(AuthzTrans trans, HttpServletResponse resp, String perm, int[] yyyymm, final int sort) {
        StringBuilder sb = new StringBuilder();
        sb.append(GET_HISTORY_PERM);
        sb.append(' ');
        sb.append(perm);
        sb.append(" for ");
        boolean first = true;
        for (int i : yyyymm) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(i);
        }
        TimeTaken tt = trans.start(sb.toString(), Env.SUB|Env.ALWAYS);
        try {
            Result<HISTORY> rh = service.getHistoryByPerm(trans,perm,yyyymm,sort);
            switch(rh.status) {
                case OK: 
                    RosettaData<HISTORY> data = historyDF.newData(trans).load(rh.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }

                    data.to(resp.getOutputStream());
                    setContentType(resp,historyDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rh);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_HISTORY_PERM);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getHistoryByUser(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Result<Void> getHistoryBySubject(AuthzTrans trans, HttpServletResponse resp, String subject, String target, int[] yyyymm, final int sort) {
        StringBuilder sb = new StringBuilder();
        sb.append(GET_HISTORY_SUBJECT);
        sb.append(' ');
        sb.append(subject);
        sb.append(" for ");
        boolean first = true;
        for (int i : yyyymm) {
            if (first) {
                first = false;
            } else {
                sb.append(',');
            }
            sb.append(i);
        }
        TimeTaken tt = trans.start(sb.toString(), Env.SUB|Env.ALWAYS);

        try {
            Result<HISTORY> rh = service.getHistoryBySubject(trans,subject,target,yyyymm,sort);
            switch(rh.status) {
                case OK: 
                    RosettaData<HISTORY> data = historyDF.newData(trans).load(rh.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }

                    data.to(resp.getOutputStream());
                    setContentType(resp,historyDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rh);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_HISTORY_USER);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    public final static String CACHE_CLEAR = "cacheClear "; 
//    public final static String CACHE_VALIDATE = "validateCache";
    
    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#cacheClear(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String)
     */
    @Override
    public Result<Void> cacheClear(AuthzTrans trans, String cname) {
        TimeTaken tt = trans.start(CACHE_CLEAR + cname, Env.SUB|Env.ALWAYS);
        try {
            return service.cacheClear(trans,cname);
        } catch (Exception e) {
            trans.error().log(e,IN,CACHE_CLEAR);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
 * @see com.att.authz.facade.AuthzFacade#cacheClear(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String, java.lang.Integer)
 */
    @Override
    public Result<Void> cacheClear(AuthzTrans trans, String cname,    String segments) {
        TimeTaken tt = trans.start(CACHE_CLEAR + cname + ", segments[" + segments + ']', Env.SUB|Env.ALWAYS);
        try {
            String[] segs = segments.split("\\s*,\\s*");
            int isegs[] = new int[segs.length];
            for (int i=0;i<segs.length;++i) {
                try {
                    isegs[i] = Integer.parseInt(segs[i]);
                } catch (NumberFormatException nfe) {
                    isegs[i] = -1;
                }
            }
            return service.cacheClear(trans,cname, isegs);
        } catch (Exception e) {
            trans.error().log(e,IN,CACHE_CLEAR);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#dbReset(org.onap.aaf.auth.env.test.AuthzTrans)
     */
    @Override
    public void dbReset(AuthzTrans trans) {
        service.dbReset(trans);
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getAPI(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse)
     */
    public final static String API_REPORT = "apiReport";
    @Override
    public Result<Void> getAPI(AuthzTrans trans, HttpServletResponse resp, RServlet<AuthzTrans> rservlet) {
        TimeTaken tt = trans.start(API_REPORT, Env.SUB);
        try {
            Api api = new Api();
            Api.Route ar;
            Method[] meths = AuthzCassServiceImpl.class.getDeclaredMethods();
            for (RouteReport rr : rservlet.routeReport()) {
                api.getRoute().add(ar = new Api.Route());
                ar.setMeth(rr.meth.name());
                ar.setPath(rr.path);
                ar.setDesc(rr.desc);
                ar.getContentType().addAll(rr.contextTypes);
                for (Method m : meths) {
                    ApiDoc ad;
                    if ((ad = m.getAnnotation(ApiDoc.class))!=null &&
                            rr.meth.equals(ad.method()) &&
                            rr.path.equals(ad.path())) {
                        for (String param : ad.params()) {
                            ar.getParam().add(param);
                        }
                        for (String text : ad.text()) {
                            ar.getComments().add(text);
                        }
                        ar.setExpected(ad.expectedCode());
                        for (int ec : ad.errorCodes()) {
                            ar.getExplicitErr().add(ec);
                        }
                    }
                }
            }
            RosettaData<Api> data = apiDF.newData(trans).load(api);
            if (Question.willSpecialLog(trans, trans.user())) {
                Question.logEncryptTrace(trans,data.asString());
            }

            data.to(resp.getOutputStream());
            setContentType(resp,apiDF.getOutType());
            return Result.ok();

        } catch (Exception e) {
            trans.error().log(e,IN,API_REPORT);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }
    

    public final static String API_EXAMPLE = "apiExample";

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#getAPIExample(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    @Override
    public Result<Void> getAPIExample(AuthzTrans trans, HttpServletResponse resp, String nameOrContentType, boolean optional) {
        TimeTaken tt = trans.start(API_EXAMPLE, Env.SUB);
        try {
            String content =Examples.print(apiDF.getEnv(), nameOrContentType, optional); 
            resp.getOutputStream().print(content);
            setContentType(resp,content.contains("<?xml")?TYPE.XML:TYPE.JSON);
            return Result.ok();
        } catch (Exception e) {
            trans.error().log(e,IN,API_EXAMPLE);
            return Result.err(Status.ERR_NotImplemented,e.getMessage());
        } finally {
            tt.done();
        }
    }

}