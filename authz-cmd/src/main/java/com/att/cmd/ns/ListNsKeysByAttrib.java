/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aai
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * Copyright © 2017 Amdocs
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
package com.att.cmd.ns;

import com.att.cadi.CadiException;
import com.att.cadi.LocatorException;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cadi.client.Retryable;
import com.att.cmd.AAFcli;
import com.att.cmd.Cmd;
import com.att.cmd.Param;
import com.att.cssa.rserv.HttpMethods;
import com.att.inno.env.APIException;

import aaf.v2_0.Keys;
import aaf.v2_0.Nss;
import aaf.v2_0.Perms;
import aaf.v2_0.Roles;
import aaf.v2_0.Users;

/**
 * p
 *
 */
public class ListNsKeysByAttrib extends Cmd {
	private static final String HEADER="List Namespace Names by Attribute";
	
	public ListNsKeysByAttrib(List parent) {
		super(parent,"keys", 
				new Param("attrib",true)); 
	}

	@Override
	public int _exec(final int idx, final String ... args) throws CadiException, APIException, LocatorException {
		final String attrib=args[idx];
		return same(new Retryable<Integer>() {
			@Override
			public Integer code(Rcli<?> client) throws CadiException, APIException {
				Future<Keys> fn = client.read("/authz/ns/attrib/"+attrib,getDF(Keys.class));
				if(fn.get(AAFcli.timeout())) {
					parent.reportHead(HEADER);
					for(String key : fn.value.getKey()) {
						pw().printf(List.kformat, key);
					}
				} else if(fn.code()==404) {
					parent.reportHead(HEADER);
					pw().println("    *** No Namespaces Found ***");
					return 200;
				} else {	
					error(fn);
				}
				return fn.code();
			}
		});
	}

	@Override
	public void detailedHelp(int indent, StringBuilder sb) {
		detailLine(sb,indent,HEADER);
		api(sb,indent,HttpMethods.GET,"authz/nss/<ns>",Nss.class,true);
		detailLine(sb,indent,"Indirectly uses:");
		api(sb,indent,HttpMethods.GET,"authz/roles/ns/<ns>",Roles.class,false);
		api(sb,indent,HttpMethods.GET,"authz/perms/ns/<ns>",Perms.class,false);
		api(sb,indent,HttpMethods.GET,"authn/creds/ns/<ns>",Users.class,false);
	}

}
