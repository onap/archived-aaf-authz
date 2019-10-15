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

package org.onap.aaf.auth.hello;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.hello.AAF_Hello.API;
import org.onap.aaf.auth.rserv.HttpCode;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;

/**
 * API Apis
 * @author Jonathan
 *
 */
public class API_Hello {


    private static final String APPLICATION_JSON = "application/json";
    protected static final byte[] NOT_JSON = "Data does not look like JSON".getBytes();

    // Hide Public Constructor
    private API_Hello() {}

    /**
     * Normal Init level APIs
     *
     * @param oauthHello
     * @param facade
     * @throws Exception
     */
    public static void init(final AAF_Hello oauthHello){
        ////////
        // Simple "GET" API
        ///////
    
        oauthHello.route(HttpMethods.GET,"/hello/:perm*",API.TOKEN,new HttpCode<AuthzTrans, AAF_Hello>(oauthHello,"Hello OAuth"){
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                resp.setStatus(200 /* OK */);
                ServletOutputStream os = resp.getOutputStream();
                os.print("Hello AAF ");
                String perm = pathParam(req, "perm");
                if (perm!=null && perm.length()>0) {
                    os.print('(');
                    os.print(req.getUserPrincipal().getName());
                    TimeTaken tt = trans.start("Authorize perm", Env.REMOTE);
                    try {
                        if (req.isUserInRole(perm)) {
                            os.print(" has ");
                        } else {
                            os.print(" does not have ");
                        }
                    } finally {
                        tt.done();
                    }
                    os.print("Permission: ");
                    os.print(perm);
                    os.print(')');
                }
                os.println();
            
                trans.info().printf("Said 'Hello' to %s, Authentication type: %s",trans.getUserPrincipal().getName(),trans.getUserPrincipal().getClass().getSimpleName());
            }
        }); 

////////////////
// REST APIs
////////////////

        ////////////////
        // CREATE/POST
        ////////////////
        oauthHello.route(oauthHello.env,HttpMethods.POST,"/resthello/:id",new HttpCode<AuthzTrans, AAF_Hello>(oauthHello,"REST Hello Create") {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
                StringBuilder sb = new StringBuilder();
                while (br.ready()) {
                    sb.append(br.readLine());
                }
                String content = sb.toString();
                trans.info().printf("Content from %s: %s\n", pathParam(req, ":id"),content);
                if (content.startsWith("{") && content.endsWith("}")) {
                    resp.setStatus(201 /* OK */);
                } else {
                    resp.getOutputStream().write(NOT_JSON);
                    resp.setStatus(406);
                }
            }
        },APPLICATION_JSON); 


        ////////////////
        // READ/GET
        ////////////////
        oauthHello.route(oauthHello.env,HttpMethods.GET,"/resthello/:id",new HttpCode<AuthzTrans, AAF_Hello>(oauthHello,"REST Hello Read") {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                resp.setStatus(200 /* OK */);
                StringBuilder sb = new StringBuilder("{\"resp\": \"Hello REST AAF\",\"principal\": \"");
                sb.append(req.getUserPrincipal().getName());
                sb.append('"');
                String perm = pathParam(req, "perm");
                trans.info().printf("Read request from %s: %s\n", pathParam(req, ":id"),perm);
                if (perm!=null && perm.length()>0) {
                    TimeTaken tt = trans.start("Authorize perm", Env.REMOTE);
                    try {
                        sb.append(",\"validation\": { \"permission\" : \"");
                        sb.append(perm);
                        sb.append("\",\"has\" : \"");
                        sb.append(req.isUserInRole(perm));
                        sb.append("\"}");
                    } finally {
                        tt.done();
                    }
                }
                sb.append("}");
                ServletOutputStream os = resp.getOutputStream();
                os.println(sb.toString());
                trans.info().printf("Said 'RESTful Hello' to %s, Authentication type: %s",trans.getUserPrincipal().getName(),trans.getUserPrincipal().getClass().getSimpleName());
            }
        },APPLICATION_JSON); 
    
        ////////////////
        // UPDATE/PUT
        ////////////////
        oauthHello.route(oauthHello.env,HttpMethods.PUT,"/resthello/:id",new HttpCode<AuthzTrans, AAF_Hello>(oauthHello,"REST Hello Update") {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
                StringBuilder sb = new StringBuilder();
                while (br.ready()) {
                    sb.append(br.readLine());
                }
                String content = sb.toString();
                trans.info().printf("Content from %s: %s\n", pathParam(req, ":id"),content);
                if (content.startsWith("{") && content.endsWith("}")) {
                    resp.setStatus(200 /* OK */);
                    resp.getOutputStream().print(content);
                } else {
                    resp.getOutputStream().write(NOT_JSON);
                    resp.setStatus(406);
                }
            }
        },APPLICATION_JSON); 


        ////////////////
        // DELETE
        ////////////////
        oauthHello.route(oauthHello.env,HttpMethods.DELETE,"/resthello/:id",new HttpCode<AuthzTrans, AAF_Hello>(oauthHello,"REST Hello Delete") {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                trans.info().printf("Delete requested on %s\n", pathParam(req, ":id"));
                resp.setStatus(200 /* OK */);
            }
        },APPLICATION_JSON); 

    }
}
