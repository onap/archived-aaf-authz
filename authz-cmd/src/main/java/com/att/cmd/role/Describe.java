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
package com.att.cmd.role;

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

import aaf.v2_0.RoleRequest;

public class Describe extends Cmd {
	private static final String ROLE_PATH = "/authz/role";
	public Describe(Role parent) {
		super(parent,"describe", 
				new Param("name",true),
				new Param("description",true)); 
	}

	@Override
	public int _exec(final int index, final String ... args) throws CadiException, APIException, LocatorException {
		return same(new Retryable<Integer>() {
			@Override
			public Integer code(Rcli<?> client) throws CadiException, APIException {
				int idx = index;
				String role = args[idx++];
				StringBuilder desc = new StringBuilder();
				while (idx < args.length) {
					desc.append(args[idx++] + ' ');
				}
		
				RoleRequest rr = new RoleRequest();
				rr.setName(role);
				rr.setDescription(desc.toString());
		
				// Set Start/End commands
				setStartEnd(rr);
				
				Future<RoleRequest> fp = null;
				int rv;

				fp = client.update(
					ROLE_PATH,
					getDF(RoleRequest.class),
					rr
					);

				if(fp.get(AAFcli.timeout())) {
					rv=fp.code();
					pw().println("Description added to role");
				} else {
					if((rv=fp.code())==202) {
						pw().print("Adding description");
						pw().println(" Accepted, but requires Approvals before actualizing");
					} else {
						error(fp);
					}
				}
				return rv;
			}
		});
	}

	@Override
	public void detailedHelp(int indent, StringBuilder sb) {
		detailLine(sb,indent,"Add a description to a role");
		api(sb,indent,HttpMethods.PUT,"authz/role",RoleRequest.class,true);
	}
}
