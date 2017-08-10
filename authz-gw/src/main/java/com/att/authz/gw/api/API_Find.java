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

import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.authz.env.AuthzTrans;
import com.att.authz.gw.GwAPI;
import com.att.authz.gw.GwCode;
import com.att.authz.gw.facade.GwFacade;
import com.att.authz.gw.mapper.Mapper.API;
import com.att.authz.layer.Result;
import com.att.cadi.Locator;
import com.att.cadi.Locator.Item;
import com.att.cadi.LocatorException;
import com.att.cadi.dme2.DME2Locator;
import com.att.cssa.rserv.HttpMethods;

/**
 * API Apis.. using Redirect for mechanism
 * 
 *
 */
public class API_Find {
	/**
	 * Normal Init level APIs
	 * 
	 * @param gwAPI
	 * @param facade
	 * @throws Exception
	 */
	public static void init(final GwAPI gwAPI, GwFacade facade) throws Exception {
		////////
		// Overall APIs
		///////
		gwAPI.route(HttpMethods.GET,"/dme2/:service/:version/:envContext/:routeOffer/:path*",API.VOID,new GwCode(facade,"Document API", true) {
			@Override
			public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
				//TODO cache this...
				try {
					Locator loc = new DME2Locator(gwAPI.env, gwAPI.dme2Man, 
						pathParam(req,":service"),
						pathParam(req,":version"),
						pathParam(req,":envContext"),
						pathParam(req,":routeOffer")
						);
					if(loc.hasItems()) {
						Item item = loc.best();
						URI uri = (URI) loc.get(item);
						String redirectURL = uri.toString() + '/' + pathParam(req,":path");
						trans.warn().log("Redirect to",redirectURL);
						resp.sendRedirect(redirectURL);
					} else {
						context.error(trans, resp, Result.err(Result.ERR_NotFound,"%s is not valid",req.getPathInfo()));
					}
				} catch (LocatorException e) {
					context.error(trans, resp, Result.err(Result.ERR_NotFound,"No DME2 Endpoints found for %s",req.getPathInfo()));
				}
			}
		});

	}
}
