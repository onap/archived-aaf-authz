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

package org.onap.aaf.auth.service.api;

import static org.onap.aaf.auth.rserv.HttpMethods.DELETE;
import static org.onap.aaf.auth.rserv.HttpMethods.GET;
import static org.onap.aaf.auth.rserv.HttpMethods.POST;
import static org.onap.aaf.auth.rserv.HttpMethods.PUT;

import java.security.Principal;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.onap.aaf.auth.direct.DirectAAFUserPass;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.auth.service.AAF_Service;
import org.onap.aaf.auth.service.Code;
import org.onap.aaf.auth.service.facade.AuthzFacade;
import org.onap.aaf.auth.service.mapper.Mapper.API;
import org.onap.aaf.cadi.CredVal;
import org.onap.aaf.cadi.CredVal.Type;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.principal.BasicPrincipal;
import org.onap.aaf.cadi.principal.X509Principal;
import org.onap.aaf.cadi.taf.basic.BasicHttpTaf;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;

/**
 * Initialize All Dispatches related to Credentials (AUTHN)
 * @author Jonathan
 *
 */
public class API_Creds {
    // Hide Public Interface
    private API_Creds() {}
    // needed to validate Creds even when already Authenticated x509
    /**
     * TIME SENSITIVE APIs
     * 
     * These will be first in the list
     * 
     * @param env
     * @param authzAPI
     * @param facade
     * @param directAAFUserPass 
     * @throws Exception
     */
    public static void timeSensitiveInit(Env env, AAF_Service authzAPI, AuthzFacade facade, final DirectAAFUserPass directAAFUserPass) throws Exception {
        /**
         * Basic Auth, quick Validation
         * 
         * Responds OK or NotAuthorized
         */
        authzAPI.route(env, HttpMethods.GET, "/authn/basicAuth", new Code(facade,"Is given BasicAuth valid?",true) {
            @Override
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {

                Principal p = trans.getUserPrincipal();
                if (p instanceof BasicPrincipal) {
                    // the idea is that if call is made with this credential, and it's a BasicPrincipal, it's ok
                    // otherwise, it wouldn't have gotten here.
                    resp.setStatus(HttpStatus.OK_200);
                } else if (p instanceof X509Principal) {
                    // have to check Basic Auth here, because it might be CSP.
                    String authz = req.getHeader("Authorization");
                    if (authz.startsWith("Basic ")) {
                        BasicHttpTaf bht = ((X509Principal)p).getBasicHttpTaf(); 
                        if (bht!=null) {
                            BasicPrincipal bp = new BasicPrincipal(authz,"");
                            CredVal cv = bht.getCredVal(bp.getDomain());
                            if (cv!=null) {
                                if (cv.validate(bp.getName(), Type.PASSWORD, bp.getCred(), null) ) {
                                    resp.setStatus(HttpStatus.OK_200);
                                } else {
                                    resp.setStatus(HttpStatus.UNAUTHORIZED_401);
                                }
                            }
                        } else {
                            String decoded = Symm.base64noSplit.decode(authz.substring(6));
                            int colon = decoded.indexOf(':');
                            TimeTaken tt = trans.start("Direct Validation", Env.REMOTE);
                            try {
                                if (directAAFUserPass.validate(
                                        decoded.substring(0,colon), 
                                        CredVal.Type.PASSWORD , 
                                        decoded.substring(colon+1).getBytes(),trans)) {
                                    resp.setStatus(HttpStatus.OK_200);
                                } else {
                                    // DME2 at this version crashes without some sort of response
                                    resp.getOutputStream().print("");
                                    resp.setStatus(HttpStatus.FORBIDDEN_403);
                                }
                            } finally {
                                tt.done();
                            }
                        }
                    }
                } else if (p == null) {
                    trans.error().log("Transaction not Authenticated... no Principal");
                    resp.setStatus(HttpStatus.FORBIDDEN_403);
                } else {
                    trans.checkpoint("Basic Auth Check Failed: This wasn't a Basic Auth Trans");
                    // For Auth Security questions, we don't give any info to client on why failed
                    resp.setStatus(HttpStatus.FORBIDDEN_403);
                }
            }
        },"text/plain","*/*","*");
        
        /** 
         *  returns whether a given Credential is valid
         */
        authzAPI.route(POST, "/authn/validate", API.CRED_REQ, new Code(facade,"Is given Credential valid?",true) {
            @Override
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
                // will be a valid Entity.  Do we need to add permission
            	//if(trans.fish("ns","password","request")) or the like
                Result<Date> r = context.doesCredentialMatch(trans, req, resp);
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                    // For Security, we don't give any info out on why failed, other than forbidden
                    // Can't do "401", because that is on the call itself
                	// 403 Implies you MAY NOT Ask.
                    resp.setStatus(HttpStatus.NOT_ACCEPTABLE_406);
                }
            }
        });  

        /** 
         *  returns whether a given Credential is valid
         */
        authzAPI.route(GET, "/authn/cert/id/:id", API.CERTS, new Code(facade,"Get Cert Info by ID",true) {
            @Override
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
                
                Result<Void> r = context.getCertInfoByID(trans, req, resp, pathParam(req,":id") );
                if (r.isOK()) {
                        resp.setStatus(HttpStatus.OK_200); 
                } else {
                        // For Security, we don't give any info out on why failed, other than forbidden
                        resp.setStatus(HttpStatus.FORBIDDEN_403);
                }
            }
        });  




    }
    
    /**
     * Normal Init level APIs
     * 
     * @param authzAPI
     * @param facade
     * @throws Exception
     */
    public static void init(AAF_Service authzAPI, AuthzFacade facade) throws Exception {
        /**
         * Create a new ID/Credential
         */
        authzAPI.route(POST,"/authn/cred",API.CRED_REQ,new Code(facade,"Add a New ID/Credential", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {                
                Result<Void> r = context.createUserCred(trans, req);
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.CREATED_201);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });
        
        /** 
         *  gets all credentials by Namespace
         */
        authzAPI.route(GET, "/authn/creds/ns/:ns", API.USERS, new Code(facade,"Get Creds for a Namespace",true) {
            @Override
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
                
                Result<Void> r = context.getCredsByNS(trans, resp, pathParam(req, "ns"));
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200); 
                } else {
                    context.error(trans,resp,r);
                }
            }

        });
        
        /** 
         *  gets all credentials by ID
         */
        authzAPI.route(GET, "/authn/creds/id/:id", API.USERS, new Code(facade,"Get Creds by ID",true) {
            @Override
            public void handle(
                    AuthzTrans trans, 
                    HttpServletRequest req,
                    HttpServletResponse resp) throws Exception {
                
                Result<Void> r = context.getCredsByID(trans, resp, pathParam(req, "id"));
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200); 
                } else {
                    context.error(trans,resp,r);
                }
            }

        });


        /**
         * Update ID/Credential (aka reset)
         */
        authzAPI.route(PUT,"/authn/cred",API.CRED_REQ,new Code(facade,"Update an ID/Credential", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                
                Result<Void> r = context.changeUserCred(trans, req);
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });

        /**
         * Extend ID/Credential
         * This behavior will accelerate getting out of P1 outages due to ignoring renewal requests, or
         * other expiration issues.
         * 
         * Scenario is that people who are solving Password problems at night, are not necessarily those who
         * know what the passwords are supposed to be.  Also, changing Password, without changing Configurations
         * using that password only exacerbates the P1 Issue.
         */
        authzAPI.route(PUT,"/authn/cred/:days",API.CRED_REQ,new Code(facade,"Extend an ID/Credential", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.extendUserCred(trans, req, pathParam(req, "days"));
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });

        /**
         * Delete a ID/Credential by Object
         */
        authzAPI.route(DELETE,"/authn/cred",API.CRED_REQ,new Code(facade,"Delete a Credential", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                Result<Void> r = context.deleteUserCred(trans, req);
                if (r.isOK()) {
                    resp.setStatus(HttpStatus.OK_200);
                } else {
                    context.error(trans,resp,r);
                }
            }
        });

    }
}
