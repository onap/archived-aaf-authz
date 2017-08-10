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
package com.att.cmd.perm;

import com.att.cadi.CadiException;
import com.att.cadi.LocatorException;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cmd.Cmd;
import com.att.cmd.Param;
import com.att.cssa.rserv.HttpMethods;
import com.att.inno.env.APIException;

import aaf.v2_0.Perms;

/**
 * 
 *
 */
public class ListByUser extends Cmd {
	private static final String HEADER = "List Permissions by User";
	public ListByUser(List parent) {
		super(parent,"user", 
				new Param("id",true)); 
	}

	public int _exec( int idx, final String ... args) throws CadiException, APIException, LocatorException {
		String user=args[idx];
		String realm = getOrgRealm();
		final String fullUser;
		if (user.indexOf('@') < 0 && realm != null) 
			fullUser = user + '@' + realm;
		else
			fullUser = user;
		
		return same(((List)parent).new ListPerms() {
			@Override
			public Integer code(Rcli<?> client) throws CadiException, APIException {
				Future<Perms> fp = client.read(
						"/authz/perms/user/"+fullUser, 
						getDF(Perms.class)
						);
				return list(fp, client, HEADER, fullUser);
			}
		});
	}
	
	@Override
	public void detailedHelp(int indent, StringBuilder sb) {
		detailLine(sb,indent,HEADER);
		api(sb,indent,HttpMethods.GET,"authz/perms/user/<user id>",Perms.class,true);
	}


}
