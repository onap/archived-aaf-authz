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
import java.util.Date;
import java.util.List;

import org.onap.aaf.auth.batch.reports.Notify;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.misc.env.util.Chrono;

public abstract class NotifyURBody extends NotifyBody {

	private final String explanation;
	public NotifyURBody(Access access, String name) throws IOException {
		super(access,"ur",name);
		
		// Default
		explanation = "The Roles for the IDs associated with you will expire on the dates shown. If "
				+ "allowed to expire, the ID will no longer authorized in that role on that date.<br><br>"
		        + "It is the responsibility of the Designated Approvers to approve, but you can monitor "
		        + "their progress by clicking the ID Link.";	
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
		println(sb,indent,"<br><br>");
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
			String rid = row.get(1);
			String fqiCell = "<a href=\"" + gui_url + "/myrequests\">" + rid + "</a>";
			fqi = printCell(sb,indent,fqiCell,fqi);
			printCell(sb,indent,row.get(2));
			Date expires = new Date(Long.parseLong(row.get(6)));
			printCell(sb,indent,Chrono.niceUTCStamp(expires));
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
