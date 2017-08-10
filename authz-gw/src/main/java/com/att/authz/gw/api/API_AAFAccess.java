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

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.Principal;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.aft.dme2.internal.jetty.http.HttpStatus;
import com.att.authz.env.AuthzTrans;
import com.att.authz.gw.GwAPI;
import com.att.authz.gw.GwCode;
import com.att.authz.gw.facade.GwFacade;
import com.att.authz.gw.mapper.Mapper.API;
import com.att.authz.layer.Result;
import com.att.cache.Cache.Dated;
import com.att.cadi.CadiException;
import com.att.cadi.Locator;
import com.att.cadi.Locator.Item;
import com.att.cadi.LocatorException;
import com.att.cadi.aaf.AAFPermission;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cadi.client.Retryable;
import com.att.cadi.dme2.DME2Locator;
import com.att.cadi.principal.BasicPrincipal;
import com.att.cssa.rserv.HttpMethods;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.TimeTaken;

public class API_AAFAccess {
	private static final String AUTHZ_DME2_GUI = "com.att.authz.authz-gui";
	static final String AFT_ENVIRONMENT="AFT_ENVIRONMENT";
	static final String AFT_ENV_CONTEXT="AFT_ENV_CONTEXT";
	static final String AFTUAT="AFTUAT";
	
	private static final String PROD = "PROD";
	private static final String IST = "IST"; // main NONPROD system
	private static final String PERF = "PERF";
	private static final String TEST = "TEST";
	private static final String DEV = "DEV";
	
//	private static String service, version, envContext; 
	private static String routeOffer;

	private static final String GET_PERMS_BY_USER = "Get Perms by User";
	private static final String USER_HAS_PERM ="User Has Perm";
//	private static final String USER_IN_ROLE ="User Has Role";
	private static final String BASIC_AUTH ="AAF Basic Auth";
	
	/**
	 * Normal Init level APIs
	 * 
	 * @param gwAPI
	 * @param facade
	 * @throws Exception
	 */
	public static void init(final GwAPI gwAPI, GwFacade facade) throws Exception {
		String aftenv = gwAPI.env.getProperty(AFT_ENVIRONMENT);
		if(aftenv==null) throw new Exception(AFT_ENVIRONMENT + " must be set");
		
		int equals, count=0;
		for(int slash = gwAPI.aafurl.indexOf('/');slash>0;++count) {
			equals = gwAPI.aafurl.indexOf('=',slash)+1;
			slash = gwAPI.aafurl.indexOf('/',slash+1);
			switch(count) {
				case 2:
//					service = gwAPI.aafurl.substring(equals, slash);
					break;
				case 3:
//					version = gwAPI.aafurl.substring(equals, slash);
					break;
				case 4:
//					envContext = gwAPI.aafurl.substring(equals, slash);
					break;
				case 5:
					routeOffer = gwAPI.aafurl.substring(equals);
					break;
			}
		}
		if(count<6) throw new MalformedURLException(gwAPI.aafurl);
		
		gwAPI.route(HttpMethods.GET,"/authz/perms/user/:user",API.VOID,new GwCode(facade,GET_PERMS_BY_USER, true) {
			@Override
			public void handle(final AuthzTrans trans, final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
				TimeTaken tt = trans.start(GET_PERMS_BY_USER, Env.SUB);
				try {
					final String accept = req.getHeader("ACCEPT");
					final String user = pathParam(req,":user");
					if(!user.contains("@")) {
						context.error(trans,resp,Result.ERR_BadData,"User [%s] must be fully qualified with domain",user);
						return;
					}
					String key = trans.user() + user + (accept!=null&&accept.contains("xml")?"-xml":"-json");
					TimeTaken tt2 = trans.start("Cache Lookup",Env.SUB);
					Dated d;
					try {
						d = gwAPI.cacheUser.get(key);
					} finally {
						tt2.done();
					}
					
					if(d==null || d.data.isEmpty()) {
						tt2 = trans.start("AAF Service Call",Env.REMOTE);
						try {
							gwAPI.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
								@Override
								public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
									Future<String> fp = client.read("/authz/perms/user/"+user,accept);
									if(fp.get(5000)) {
										gwAPI.cacheUser.put(key, new Dated(new User(fp.code(),fp.body())));
										resp.setStatus(HttpStatus.OK_200);
										ServletOutputStream sos;
										try {
											sos = resp.getOutputStream();
											sos.print(fp.value);
										} catch (IOException e) {
											throw new CadiException(e);
										}
									} else {
										gwAPI.cacheUser.put(key, new Dated(new User(fp.code(),fp.body())));
										context.error(trans,resp,fp.code(),fp.body());
									}
									return null;
								}
							});
						} finally {
							tt2.done();
						}
					} else {
						User u = (User)d.data.get(0);
						resp.setStatus(u.code);
						ServletOutputStream sos = resp.getOutputStream();
						sos.print(u.resp);
					}
				} finally {
					tt.done();
				}
			}
		});

		gwAPI.route(gwAPI.env,HttpMethods.GET,"/authn/basicAuth",new GwCode(facade,BASIC_AUTH, true) {
			@Override
			public void handle(final AuthzTrans trans, final HttpServletRequest req, HttpServletResponse resp) throws Exception {
				Principal p = trans.getUserPrincipal();
				if(p == null) {
					trans.error().log("Transaction not Authenticated... no Principal");
					resp.setStatus(HttpStatus.FORBIDDEN_403);
				} else if (p instanceof BasicPrincipal) {
					// the idea is that if call is made with this credential, and it's a BasicPrincipal, it's ok
					// otherwise, it wouldn't have gotten here.
					resp.setStatus(HttpStatus.OK_200);
				} else {
					trans.checkpoint("Basic Auth Check Failed: This wasn't a Basic Auth Trans");
					// For Auth Security questions, we don't give any info to client on why failed
					resp.setStatus(HttpStatus.FORBIDDEN_403);
				}
			}
		},"text/plain","*/*","*");

		/**
		 * Query User Has Perm
		 */
		gwAPI.route(HttpMethods.GET,"/ask/:user/has/:type/:instance/:action",API.VOID,new GwCode(facade,USER_HAS_PERM, true) {
			@Override
			public void handle(final AuthzTrans trans, final HttpServletRequest req, HttpServletResponse resp) throws Exception {
				try {
					resp.getOutputStream().print(
							gwAPI.aafLurPerm.fish(pathParam(req,":user"), new AAFPermission(
								pathParam(req,":type"),
								pathParam(req,":instance"),
								pathParam(req,":action"))));
					resp.setStatus(HttpStatus.OK_200);
				} catch(Exception e) {
					context.error(trans, resp, Result.ERR_General, e.getMessage());
				}
			}
		});

		if(AFTUAT.equals(aftenv)) {
			gwAPI.route(HttpMethods.GET,"/ist/aaf/:version/:path*",API.VOID ,new GwCode(facade,"Access UAT GUI for AAF", true) {
				@Override
				public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
					try{
						redirect(trans, req, resp, context, 
								new DME2Locator(gwAPI.env, gwAPI.dme2Man, AUTHZ_DME2_GUI, pathParam(req,":version"), IST, routeOffer), 
								pathParam(req,":path"));
					} catch (LocatorException e) {
						context.error(trans, resp, Result.ERR_BadData, e.getMessage());
					} catch (Exception e) {
						context.error(trans, resp, Result.ERR_General, e.getMessage());
					}
				}
			});

			gwAPI.route(HttpMethods.GET,"/test/aaf/:version/:path*",API.VOID ,new GwCode(facade,"Access TEST GUI for AAF", true) {
				@Override
				public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
					try{
						redirect(trans, req, resp, context, 
								new DME2Locator(gwAPI.env, gwAPI.dme2Man, AUTHZ_DME2_GUI, pathParam(req,":version"), TEST, routeOffer), 
								pathParam(req,":path"));
					} catch (LocatorException e) {
						context.error(trans, resp, Result.ERR_BadData, e.getMessage());
					} catch (Exception e) {
						context.error(trans, resp, Result.ERR_General, e.getMessage());
					}
				}
			});

			gwAPI.route(HttpMethods.GET,"/perf/aaf/:version/:path*",API.VOID ,new GwCode(facade,"Access PERF GUI for AAF", true) {
				@Override
				public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
					try{
						redirect(trans, req, resp, context, 
								new DME2Locator(gwAPI.env, gwAPI.dme2Man, AUTHZ_DME2_GUI, pathParam(req,":version"), PERF, routeOffer), 
								pathParam(req,":path"));
					} catch (LocatorException e) {
						context.error(trans, resp, Result.ERR_BadData, e.getMessage());
					} catch (Exception e) {
						context.error(trans, resp, Result.ERR_General, e.getMessage());
					}
				}
			});

			gwAPI.route(HttpMethods.GET,"/dev/aaf/:version/:path*",API.VOID,new GwCode(facade,"Access DEV GUI for AAF", true) {
				@Override
				public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
					try {
						redirect(trans, req, resp, context, 
								new DME2Locator(gwAPI.env, gwAPI.dme2Man, AUTHZ_DME2_GUI, pathParam(req,":version"), DEV, routeOffer), 
								pathParam(req,":path"));
					} catch (LocatorException e) {
						context.error(trans, resp, Result.ERR_BadData, e.getMessage());
					} catch (Exception e) {
						context.error(trans, resp, Result.ERR_General, e.getMessage());
					}
				}
			});
		} else {
			gwAPI.route(HttpMethods.GET,"/aaf/:version/:path*",API.VOID,new GwCode(facade,"Access PROD GUI for AAF", true) {
				@Override
				public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
					try {
						redirect(trans, req, resp, context, 
								new DME2Locator(gwAPI.env, gwAPI.dme2Man, AUTHZ_DME2_GUI, pathParam(req,":version"), PROD, routeOffer), 
								pathParam(req,":path"));
					} catch (LocatorException e) {
						context.error(trans, resp, Result.ERR_BadData, e.getMessage());
					} catch (Exception e) {
						context.error(trans, resp, Result.ERR_General, e.getMessage());
					}
				}
			});
		}
		
	}
	
	public static void initDefault(final GwAPI gwAPI, GwFacade facade) throws Exception {
		String aftenv = gwAPI.env.getProperty(AFT_ENVIRONMENT);
		if(aftenv==null) throw new Exception(AFT_ENVIRONMENT + " must be set");
	
		String aftctx = gwAPI.env.getProperty(AFT_ENV_CONTEXT);
		if(aftctx==null) throw new Exception(AFT_ENV_CONTEXT + " must be set");

		/**
		 * "login" url
		 */
		gwAPI.route(HttpMethods.GET,"/login",API.VOID,new GwCode(facade,"Access " + aftctx + " GUI for AAF", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				try {
					redirect(trans, req, resp, context, 
							new DME2Locator(gwAPI.env, gwAPI.dme2Man, AUTHZ_DME2_GUI, "2.0", aftctx, routeOffer), 
							"login");
				} catch (LocatorException e) {
					context.error(trans, resp, Result.ERR_BadData, e.getMessage());
				} catch (Exception e) {
					context.error(trans, resp, Result.ERR_General, e.getMessage());
				}
			}
		});

		/**
		 * Default URL
		 */
		gwAPI.route(HttpMethods.GET,"/",API.VOID,new GwCode(facade,"Access " + aftctx + " GUI for AAF", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				try {
					redirect(trans, req, resp, context, 
							new DME2Locator(gwAPI.env, gwAPI.dme2Man, AUTHZ_DME2_GUI, "2.0", aftctx, routeOffer), 
							"gui/home");
				} catch (LocatorException e) {
					context.error(trans, resp, Result.ERR_BadData, e.getMessage());
				} catch (Exception e) {
					context.error(trans, resp, Result.ERR_General, e.getMessage());
				}
			}
		});
	}

	private static void redirect(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, GwFacade context, Locator loc, String path) throws IOException {
		try {
			if(loc.hasItems()) {
				Item item = loc.best();
				URI uri = (URI) loc.get(item);
				StringBuilder redirectURL = new StringBuilder(uri.toString()); 
				redirectURL.append('/');
				redirectURL.append(path);
				String str = req.getQueryString();
				if(str!=null) {
					redirectURL.append('?');
					redirectURL.append(str);
				}
				trans.info().log("Redirect to",redirectURL);
				resp.sendRedirect(redirectURL.toString());
			} else {
				context.error(trans, resp, Result.err(Result.ERR_NotFound,"%s is not valid",req.getPathInfo()));
			}
		} catch (LocatorException e) {
			context.error(trans, resp, Result.err(Result.ERR_NotFound,"No DME2 Endpoints found for %s",req.getPathInfo()));
		}
	}

	private static class User {
		public final int code;
		public final String resp;
		
		public User(int code, String resp) {
			this.code = code;
			this.resp = resp;
		}
	}
}
