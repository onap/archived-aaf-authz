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
package org.onap.aaf.auth.batch.reports.bodies;

import java.io.IOException;
import java.util.List;

import org.onap.aaf.auth.batch.reports.Notify;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.Access;

public abstract class NotifyURBody extends NotifyBody {

	private final String explanation;
	public NotifyURBody(Access access, String name) throws IOException {
		super("ur",name);
		
		// Default
		explanation = "The Roles for the IDs listed will expire on the dates shown. If "
				+ "allowed to expire, the ID will no longer have access to the Permissions "
				+ "associated with that Role.";
	}

	@Override
	public boolean body(AuthzTrans trans, StringBuilder sb, int indent, Notify n, String id) {
		String fullname = "n/a";
		String kind = "Name";
		try {
			Identity identity = trans.org().getIdentity(trans, id);
			if(identity==null) {
				trans.warn().printf("Cannot find %s in Organization",id);
			} else {
				fullname = identity.fullName();
				if(!identity.isPerson()) {
					if((identity = identity.responsibleTo())!=null) {
						kind = "AppID Sponsor";
						fullname = identity.fullName();
					}
				}
			}
		} catch (OrganizationException e) {
			trans.error().log(e);
			fullname = "n/a";
		}
		println(sb,indent,explanation);
		println(sb,indent,"<table>");
		indent+=2;
		println(sb,indent,"<tr>");
		indent+=2;
		println(sb,indent,"<th>"+kind+"</th>");
		println(sb,indent,"<th>Fully Qualified ID</th>");
		println(sb,indent,"<th>Role</th>");
		println(sb,indent,"<th>Expires</th>");
		indent-=2;
		println(sb,indent,"</tr>");

		String name = null;
		String fqi = null;
		for(List<String> row : rows.get(id)) {
			println(sb,indent,"<tr>");
			indent+=2;
			name = printCell(sb,indent,fullname,name);
			fqi = printCell(sb,indent,row.get(1),fqi);
			printCell(sb,indent,row.get(2)+'.'+row.get(3));
			printCell(sb,indent,row.get(4));
			indent-=2;
			println(sb,indent,"</tr>");
		}
		indent-=2;
		println(sb,indent,"</table>");
		
		return true;
	}

	@Override
	public String user(List<String> row) {
		if( (row != null) && row.size()>1) {
			return row.get(1);
		}
		return null;
	}


}
