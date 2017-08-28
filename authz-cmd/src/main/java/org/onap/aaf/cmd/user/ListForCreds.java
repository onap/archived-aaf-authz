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
package org.onap.aaf.cmd.user;

import java.util.Collections;
import java.util.Comparator;

import org.onap.aaf.cmd.AAFcli;
import org.onap.aaf.cmd.Cmd;
import org.onap.aaf.cmd.Param;
import org.onap.aaf.cssa.rserv.HttpMethods;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.inno.env.APIException;

import aaf.v2_0.Users;
import aaf.v2_0.Users.User;

/**
 * List for Creds
 *
 */
public class ListForCreds extends Cmd {
	private final static String[] options = {"ns","id"};

	private static final String HEADER = "List creds for ";
	public ListForCreds(List parent) {
		super(parent,"cred",
				new Param(optionsToString(options),true),
				new Param("value",true)); 
	}

	@Override
	public int _exec(int _idx, final String ... args) throws CadiException, APIException, LocatorException {
	        int idx = _idx;
		final int option = whichOption(options, args[idx++]);
		final String which = options[option];
		final String value = args[idx++];
		return same(new Retryable<Integer>() {
			@Override
			public Integer code(Rcli<?> client) throws CadiException, APIException {
				Future<Users> fp = client.read(
						"/authn/creds/"+which+'/'+value, 
						getDF(Users.class)
						);
				if(fp.get(AAFcli.timeout())) {
					if (aafcli.isTest())
						Collections.sort(fp.value.getUser(), new Comparator<User>() {
							@Override
							public int compare(User u1, User u2) {
								return u1.getId().compareTo(u2.getId());
							}			
						});
					((org.onap.aaf.cmd.user.List)parent).report(fp.value,option==1,HEADER+which,value);
					if(fp.code()==404)return 200;
				} else {
					error(fp);
				}
				return fp.code();
			}
		});
	}
	
	@Override
	public void detailedHelp(int _indent, StringBuilder sb) {
	        int indent = _indent;
		detailLine(sb,indent,HEADER);
		indent+=2;
		detailLine(sb,indent,"This report lists the users associated to Roles.");
		detailLine(sb,indent,"role - the Role name");
		indent-=2;
		api(sb,indent,HttpMethods.GET,"authz/users/role/<role>",Users.class,true);
	}

}
