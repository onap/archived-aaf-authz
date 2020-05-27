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


import static org.onap.aaf.auth.layer.Result.ERR_ActionNotCompleted;
import static org.onap.aaf.auth.layer.Result.ERR_BadData;
import static org.onap.aaf.auth.layer.Result.ERR_ConflictAlreadyExists;
import static org.onap.aaf.auth.layer.Result.ERR_Denied;
import static org.onap.aaf.auth.layer.Result.ERR_NotFound;
import static org.onap.aaf.auth.layer.Result.ERR_NotImplemented;
import static org.onap.aaf.auth.layer.Result.ERR_Policy;
import static org.onap.aaf.auth.layer.Result.ERR_Security;
import static org.onap.aaf.auth.layer.Result.OK;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.FacadeImpl;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.locate.mapper.Mapper;
import org.onap.aaf.auth.locate.mapper.Mapper.API;
import org.onap.aaf.auth.locate.service.LocateService;
import org.onap.aaf.auth.locate.service.LocateServiceImpl;
import org.onap.aaf.auth.rserv.RServlet;
import org.onap.aaf.auth.rserv.RouteReport;
import org.onap.aaf.auth.rserv.doc.ApiDoc;
import org.onap.aaf.cadi.aaf.client.Examples;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;
import org.owasp.encoder.Encode;

import locate_local.v1_0.Api;


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
public abstract class LocateFacadeImpl<IN,OUT,ENDPOINTS,MGMT_ENDPOINTS,CONFIGURATION,ERROR> extends FacadeImpl implements LocateFacade
    {
    private LocateService<IN,OUT,ENDPOINTS,MGMT_ENDPOINTS,CONFIGURATION,ERROR> service;

    private final RosettaDF<ERROR>             errDF;
    private final RosettaDF<Api>                 apiDF;
    private final RosettaDF<ENDPOINTS>        epDF;
    private final RosettaDF<MGMT_ENDPOINTS>    mepDF;
    private final RosettaDF<CONFIGURATION>    confDF;


    private static long cacheClear = 0L, emptyCheck=0L;
    private final static Map<String,String> epsCache = new HashMap<>(); // protected manually, in getEndpoints

    public LocateFacadeImpl(AuthzEnv env, LocateService<IN,OUT,ENDPOINTS,MGMT_ENDPOINTS,CONFIGURATION,ERROR> service, Data.TYPE dataType) throws APIException {
        this.service = service;
        (errDF                 = env.newDataFactory(mapper().getClass(API.ERROR))).in(dataType).out(dataType);
        (apiDF                = env.newDataFactory(Api.class)).in(dataType).out(dataType);
        (epDF                = env.newDataFactory(mapper().getClass(API.ENDPOINTS))).in(dataType).out(dataType);
        (mepDF                = env.newDataFactory(mapper().getClass(API.MGMT_ENDPOINTS))).in(dataType).out(dataType);
        (confDF                = env.newDataFactory(mapper().getClass(API.CONFIG))).in(dataType).out(dataType);
    }

    public Mapper<IN,OUT,ENDPOINTS,MGMT_ENDPOINTS,CONFIGURATION,ERROR> mapper() {
        return service.mapper();
    }

    /* (non-Javadoc)
     * @see com.att.authz.facade.AuthzFacade#error(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, int)
     *
     * Note: Conforms to AT&T TSS RESTful Error Structure
     */
    @Override
    public void error(AuthzTrans trans, HttpServletResponse response, Result<?> result) {
        String msg = result.details==null?"":result.details.trim();
        String[] detail;
        if (result.variables==null) {
            detail = new String[1];
        } else {
            int l = result.variables.length;
            detail=new String[l+1];
            System.arraycopy(result.variables, 0, detail, 1, l);
        }
        error(trans, response, result.status,msg,detail);
    }

    @Override
    public void error(AuthzTrans trans, HttpServletResponse response, int status, String msg, String ... _detail) {
            String[] detail = _detail;
        if (detail.length==0) {
            detail=new String[1];
        }
        boolean hidemsg = false;
        String msgId;
        switch(status) {
            case 202:
            case ERR_ActionNotCompleted:
                msgId = "SVC1202";
                detail[0] = "Accepted, Action not complete";
                response.setStatus(/*httpstatus=*/202);
                break;

            case 403:
            case ERR_Policy:
            case ERR_Security:
            case ERR_Denied:
                msgId = "SVC1403";
                detail[0] = "Forbidden";
                response.setStatus(/*httpstatus=*/403);
                break;

            case 404:
            case ERR_NotFound:
                msgId = "SVC1404";
                detail[0] = "Not Found";
                response.setStatus(/*httpstatus=*/404);
                break;

            case 406:
            case ERR_BadData:
                msgId="SVC1406";
                detail[0] = "Not Acceptable";
                response.setStatus(/*httpstatus=*/406);
                break;

            case 409:
            case ERR_ConflictAlreadyExists:
                msgId = "SVC1409";
                detail[0] = "Conflict Already Exists";
                response.setStatus(/*httpstatus=*/409);
                break;

            case 501:
            case ERR_NotImplemented:
                msgId = "SVC1501";
                detail[0] = "Not Implemented";
                response.setStatus(/*httpstatus=*/501);
                break;

            default:
                msgId = "SVC1500";
                detail[0] = "General Service Error";
                response.setStatus(/*httpstatus=*/500);
                hidemsg = true;
                break;
        }

        try {
            StringBuilder holder = new StringBuilder();
            ERROR em = mapper().errorFromMessage(holder,msgId,msg,detail);
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
            Method[] meths = LocateServiceImpl.class.getDeclaredMethods();
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
            apiDF.newData(trans).load(api).to(resp.getOutputStream());
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
            resp.getOutputStream().print(Encode.forJava(content));
            setContentType(resp,content.contains("<?xml")?TYPE.XML:TYPE.JSON);
            return Result.ok();
        } catch (Exception e) {
            trans.error().log(e,IN,API_EXAMPLE);
            return Result.err(Result.ERR_NotImplemented,e.getMessage());
        } finally {
            tt.done();
        }
    }

    public final static String GET_ENDPOINTS = "getEndpoints";
    private final static Object LOCK = new Object();
    /* (non-Javadoc)
     * @see org.onap.aaf.auth.locate.facade.GwFacade#getEndpoints(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Result<Void> getEndpoints(AuthzTrans trans, HttpServletResponse resp, String key, String service, String version, String other) {
        TimeTaken tt = trans.start(GET_ENDPOINTS, Env.SUB);
        try {
            String output=null;
            long temp=System.currentTimeMillis();
            synchronized(LOCK) {
                if (cacheClear<temp) {
                    epsCache.clear();
                    cacheClear = temp+1000*60*2; // 2 mins standard cache clear
                } else {
                    output = epsCache.get(key);
                    if ("{}".equals(output) && emptyCheck<temp) {
                        output = null;
                        emptyCheck = temp+5000; // 5 second check
                    }
                }
            }
            if (output==null) {
                Result<ENDPOINTS> reps = this.service.getEndPoints(trans,service,version,other);
                if (reps.notOK()) {
                    return Result.err(reps);
                } else {
                    output = epDF.newData(trans).load(reps.value).asString();
                    synchronized(LOCK) {
                        epsCache.put(key, output);
                    }
                }
            }
            resp.getOutputStream().println(Encode.forJava(output));
            setContentType(resp,epDF.getOutType());
            return Result.ok();
        } catch (Exception e) {
            trans.error().log(e,IN,API_EXAMPLE);
            return Result.err(Result.ERR_NotImplemented,e.getMessage());
        } finally {
            tt.done();
        }
    }

    private static final String PUT_MGMT_ENDPOINTS = "Put Mgmt Endpoints";
    /* (non-Javadoc)
     * @see org.onap.aaf.auth.locate.facade.GwFacade#putMgmtEndpoints(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Result<Void> putMgmtEndpoints(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(PUT_MGMT_ENDPOINTS, Env.SUB|Env.ALWAYS);
        try {
            MGMT_ENDPOINTS rreq;
            try {
                RosettaData<MGMT_ENDPOINTS> data = mepDF.newData().load(req.getInputStream());
                rreq = data.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,PUT_MGMT_ENDPOINTS);
                return Result.err(Status.ERR_BadData,"Invalid Input");

            }
            Result<Void> rp = service.putMgmtEndPoints(trans, rreq);
            switch(rp.status) {
                case OK:
                    synchronized(LOCK) {
                        cacheClear = 0L;
                    }
                    setContentType(resp,mepDF.getOutType());
                    return Result.ok();
                default:
                    return rp;
            }
        } catch (Exception e) {
            trans.error().log(e,IN,PUT_MGMT_ENDPOINTS);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    private static final String DELETE_MGMT_ENDPOINTS = "Delete Mgmt Endpoints";
    /* (non-Javadoc)
     * @see org.onap.aaf.auth.locate.facade.GwFacade#removeMgmtEndpoints(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Result<Void> removeMgmtEndpoints(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(DELETE_MGMT_ENDPOINTS, Env.SUB|Env.ALWAYS);
        try {
            MGMT_ENDPOINTS rreq;
            try {
                RosettaData<MGMT_ENDPOINTS> data = mepDF.newData().load(req.getInputStream());
                rreq = data.asObject();
            } catch (APIException e) {
                trans.error().log("Invalid Input",IN,DELETE_MGMT_ENDPOINTS);
                return Result.err(Status.ERR_BadData,"Invalid Input");

            }
            Result<Void> rp = service.removeMgmtEndPoints(trans, rreq);
            switch(rp.status) {
                case OK:
                    synchronized(LOCK) {
                        cacheClear = 0L;
                    }
                    setContentType(resp,mepDF.getOutType());
                    return Result.ok();
                default:
                    return rp;
            }
        } catch (Exception e) {
            trans.error().log(e,IN,DELETE_MGMT_ENDPOINTS);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

    private static final String GET_CONFIG = "Get Configuration";
    @Override
    public Result<Void> getConfig(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, final String id, final String type) {
        TimeTaken tt = trans.start(GET_CONFIG, Env.SUB|Env.ALWAYS);
        try {
            Result<CONFIGURATION> rp = service.getConfig(trans, id, type);
            switch(rp.status) {
                case OK:
                    setContentType(resp,mepDF.getOutType());
                    confDF.newData(trans).load(rp.value).to(resp.getOutputStream());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,GET_CONFIG);
            return Result.err(e);
        } finally {
            tt.done();
        }
    }

}