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

import static org.onap.aaf.auth.layer.Result.ERR_ActionNotCompleted;
import static org.onap.aaf.auth.layer.Result.ERR_BadData;
import static org.onap.aaf.auth.layer.Result.ERR_ConflictAlreadyExists;
import static org.onap.aaf.auth.layer.Result.ERR_Denied;
import static org.onap.aaf.auth.layer.Result.ERR_NotFound;
import static org.onap.aaf.auth.layer.Result.ERR_NotImplemented;
import static org.onap.aaf.auth.layer.Result.ERR_Policy;
import static org.onap.aaf.auth.layer.Result.ERR_Security;
import static org.onap.aaf.auth.layer.Result.OK;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.dao.cass.OAuthTokenDAO;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.oauth.AAF_OAuth;
import org.onap.aaf.auth.oauth.mapper.Mapper;
import org.onap.aaf.auth.oauth.mapper.Mapper.API;
import org.onap.aaf.auth.oauth.service.OAuthService;
import org.onap.aaf.auth.oauth.service.OAuthService.GRANT_TYPE;
import org.onap.aaf.cadi.util.Holder;
import org.onap.aaf.cadi.oauth.OAuth2Principal;
import org.onap.aaf.cadi.principal.OAuth2FormPrincipal;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;

import aaf.v2_0.Perms;

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
public abstract class OAFacadeImpl<TOKEN_REQ,TOKEN,INTROSPECT,ERROR>
        extends DirectIntrospectImpl<INTROSPECT> implements OAFacade<INTROSPECT> {
    private static final String INVALID_INPUT = "Invalid Input";
    private final RosettaDF<TOKEN> tokenDF;
    private final RosettaDF<TOKEN_REQ> tokenReqDF;
    private final RosettaDF<INTROSPECT> introspectDF;
    private final RosettaDF<ERROR> errDF;
    public final RosettaDF<Perms> permsDF;
    private final Mapper<TOKEN_REQ, TOKEN, INTROSPECT, ERROR> mapper;

    public OAFacadeImpl(AAF_OAuth api,
                      OAuthService service,
                      Mapper<TOKEN_REQ,TOKEN,INTROSPECT,ERROR> mapper,
                      Data.TYPE dataType) throws APIException {
        super(service, mapper);
        this.mapper = mapper;
        AuthzEnv env = api.env;
        (tokenReqDF         = env.newDataFactory(mapper.getClass(API.TOKEN_REQ))).in(dataType).out(dataType);
        (tokenDF         = env.newDataFactory(mapper.getClass(API.TOKEN))).in(dataType).out(dataType);
        (introspectDF     = env.newDataFactory(mapper.getClass(API.INTROSPECT))).in(dataType).out(dataType);
        (permsDF         = env.newDataFactory(Perms.class)).in(dataType).out(dataType);
        (errDF             = env.newDataFactory(mapper.getClass(API.ERROR))).in(dataType).out(dataType);
    }

    ///////////////////////////
    // Tokens
    ///////////////////////////
    public static final String CREATE_TOKEN = "createToken";
    public static final String INTROSPECT = "introspect";

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.oauth.facade.OAFacade#getToken(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletResponse, org.onap.aaf.auth.oauth.service.OAuthAPI)
     */
    @Override
    public Result<Void> createBearerToken(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(CREATE_TOKEN, Env.SUB|Env.ALWAYS);
        try {
            TOKEN_REQ request;
            try {
                request = mapper.tokenReqFromParams(req);
                if (request==null) {
                    Data<TOKEN_REQ> rd = tokenReqDF.newData().load(req.getInputStream());
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,rd.asString());
                    }
                    request = rd.asObject();
                }
            } catch (APIException e) {
                trans.error().log(INVALID_INPUT,IN,CREATE_TOKEN);
                return Result.err(Status.ERR_BadData,INVALID_INPUT);
            }

            // Already validated for Oauth2FormPrincipal
//            Result<Void> rv = service.validate(trans,mapper.credsFromReq(request));
//            if (rv.notOK()) {
//                return rv;
//            }
            Holder<GRANT_TYPE> hgt = new Holder<GRANT_TYPE>(GRANT_TYPE.unknown);
            Result<OAuthTokenDAO.Data> rs = service.createToken(trans,req,mapper.clientTokenReq(request,hgt),hgt);
            Result<TOKEN> rp;
            if (rs.isOKhasData()) {
                rp = mapper.tokenFromData(rs);
            } else {
                rp = Result.err(rs);
            }
            switch(rp.status) {
                case OK:
                    RosettaData<TOKEN> data = tokenDF.newData(trans).load(rp.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    resp.getOutputStream().print('\n');
                    setContentType(resp,tokenDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rp);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,CREATE_TOKEN);
            return Result.err(e);
        } finally {
            tt.done();
        }

    }

/* (non-Javadoc)
     * @see org.onap.aaf.auth.oauth.facade.OAFacade#Introspect(org.onap.aaf.auth.env.test.AuthzTrans, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Result<Void> introspect(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) {
        TimeTaken tt = trans.start(INTROSPECT, Env.SUB|Env.ALWAYS);
        try {
            Principal p = req.getUserPrincipal();
            String token=null;
            if (p != null) {
                if (p instanceof OAuth2Principal) {
                    RosettaData<INTROSPECT> data = introspectDF.newData(trans).load(mapper.fromPrincipal((OAuth2Principal)p));
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    resp.getOutputStream().print('\n');
                    setContentType(resp,tokenDF.getOutType());
                    return Result.ok();
                } else if (p instanceof OAuth2FormPrincipal) {
                    token = req.getParameter("token");
                }
            }

            if (token==null) {
                token = req.getParameter("access_token");
                if (token==null || token.isEmpty()) {
                    token = req.getHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                    } else {
                        token = req.getParameter("token");
                        if (token==null) {
                            return Result.err(Result.ERR_Security,"token is required");
                        }
                    }
                }
            }

            Result<INTROSPECT> rti = mappedIntrospect(trans,token);
            switch(rti.status) {
                case OK:
                    RosettaData<INTROSPECT> data = introspectDF.newData(trans).load(rti.value);
                    if (Question.willSpecialLog(trans, trans.user())) {
                        Question.logEncryptTrace(trans,data.asString());
                    }
                    data.to(resp.getOutputStream());
                    resp.getOutputStream().print('\n');
                    setContentType(resp,tokenDF.getOutType());
                    return Result.ok();
                default:
                    return Result.err(rti);
            }
        } catch (Exception e) {
            trans.error().log(e,IN,INTROSPECT);
            return Result.err(e);
        } finally {
            tt.done();
        }
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
            ERROR em = mapper.errorFromMessage(holder, msgId,prefix + ": " + _msg,_detail);
            trans.checkpoint(
                    "ErrResp [" +
                    msgId +
                    "] " +
                    holder.toString(),
                    Env.ALWAYS);
            if (hidemsg) {
                holder.setLength(0);
                em = mapper.errorFromMessage(holder, msgId, "Server had an issue processing this request");
            }
            errDF.newData(trans).load(em).to(response.getOutputStream());

        } catch (Exception e) {
            trans.error().log(e,"unable to send response for",_msg);
        }
    }

    public Mapper<TOKEN_REQ,TOKEN,INTROSPECT,ERROR> mapper() {
        return mapper;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.oauth.facade.OAFacade#service()
     */
    @Override
    public OAuthService service() {
        return service;
    }
}