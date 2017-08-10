/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package com.att.authz.gw.api;

import java.net.ConnectException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.aft.dme2.internal.jetty.http.HttpStatus;
import com.att.authz.env.AuthzTrans;
import com.att.authz.gw.GwAPI;
import com.att.authz.gw.GwCode;
import com.att.authz.gw.facade.GwFacade;
import com.att.authz.gw.mapper.Mapper.API;
import com.att.cadi.CadiException;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cadi.client.Retryable;
import com.att.cadi.config.Config;
import com.att.cssa.rserv.HttpMethods;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.TimeTaken;

/**
 * API Apis.. using Redirect for mechanism
 * 
 *
 */
public class API_Proxy {

	/**
	 * Normal Init level APIs
	 * 
	 * @param gwAPI
	 * @param facade
	 * @throws Exception
	 */
	public static void init(final GwAPI gwAPI, GwFacade facade) throws Exception {
		
		String aafurl = gwAPI.env.getProperty(Config.AAF_URL);
		if(aafurl==null) {
		} else {

			////////
			// Transferring APIs
			///////
			gwAPI.routeAll(HttpMethods.GET,"/proxy/:path*",API.VOID,new GwCode(facade,"Proxy GET", true) {
				@Override
				public void handle(final AuthzTrans trans, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
					TimeTaken tt = trans.start("Forward to AAF Service", Env.REMOTE);
					try {
						gwAPI.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
							@Override
							public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
								Future<Void> ft = client.transfer(req,resp,pathParam(req, ":path"),HttpStatus.OK_200);
								ft.get(10000); // Covers return codes and err messages
								return null;
							}
						});
					
					} catch (CadiException | APIException e) {
						trans.error().log(e);
					} finally {
						tt.done();
					}
				}
			});
			
			gwAPI.routeAll(HttpMethods.POST,"/proxy/:path*",API.VOID,new GwCode(facade,"Proxy POST", true) {
				@Override
				public void handle(final AuthzTrans trans, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
					TimeTaken tt = trans.start("Forward to AAF Service", Env.REMOTE);
					try {
						gwAPI.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
							@Override
							public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
								Future<Void> ft = client.transfer(req,resp,pathParam(req, ":path"),HttpStatus.CREATED_201);
								ft.get(10000); // Covers return codes and err messages
								return null;
							}
						});
					} catch (CadiException | APIException e) {
						trans.error().log(e);
					} finally {
						tt.done();
					}
				}
			});
			
			gwAPI.routeAll(HttpMethods.PUT,"/proxy/:path*",API.VOID,new GwCode(facade,"Proxy PUT", true) {
				@Override
				public void handle(final AuthzTrans trans, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
					TimeTaken tt = trans.start("Forward to AAF Service", Env.REMOTE);
					try {
						gwAPI.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
							@Override
							public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
								Future<Void> ft = client.transfer(req,resp,pathParam(req, ":path"),HttpStatus.OK_200);
								ft.get(10000); // Covers return codes and err messages
								return null;
							}
						});
					} catch (CadiException | APIException e) {
						trans.error().log(e);
					} finally {
						tt.done();
					}
				}
			});
			
			gwAPI.routeAll(HttpMethods.DELETE,"/proxy/:path*",API.VOID,new GwCode(facade,"Proxy DELETE", true) {
				@Override
				public void handle(final AuthzTrans trans, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
					TimeTaken tt = trans.start("Forward to AAF Service", Env.REMOTE);
					try {
						gwAPI.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
							@Override
							public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
								Future<Void> ft = client.transfer(req,resp,pathParam(req, ":path"),HttpStatus.OK_200);
								ft.get(10000); // Covers return codes and err messages
								return null;
							}
						});
					} catch (CadiException | APIException e) {
						trans.error().log(e);
					} finally {
						tt.done();
					}
				}
			});
		}
	}
}
