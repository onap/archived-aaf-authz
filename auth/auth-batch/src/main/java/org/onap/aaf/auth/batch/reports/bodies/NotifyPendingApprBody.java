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

import java.util.List;

import org.onap.aaf.auth.batch.reports.Notify;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.cadi.Access;

public class NotifyPendingApprBody extends NotifyBody {
	private final String explanation;

	public NotifyPendingApprBody(Access access) {
		super(access,"appr","PendingApproval");
		explanation = "The following Approvals are awaiting your action. ";
	}

	@Override
	public boolean body(AuthzTrans trans, StringBuilder sb, int indent, Notify n, String id) {
		println(sb,indent,explanation);
/*		println(sb,indent,"<table>");
		indent+=2;
		println(sb,indent,"<tr>");
		indent+=2;
		println(sb,indent,"<th>Fully Qualified ID</th>");
		println(sb,indent,"<th>Unique ID</th>");
		println(sb,indent,"<th>Type</th>");
		println(sb,indent,"<th>Expires</th>");
		println(sb,indent,"<th>Warnings</th>");
		indent-=2;
		println(sb,indent,"</tr>");
		String theid, type, info, expires, warnings;
		GregorianCalendar gc = new GregorianCalendar();
		for(List<String> row : rows.get(id)) {
			theid=row.get(1);
			switch(row.get(3)) {
				case "1":
				case "2":
					type = "Password";
					break;
				case "200":
					type = "x509 (Certificate)";
					break;
				default:
					type = "Unknown, see AAF GUI";
					break;
			}
			theid = "<a href=\""+n.guiURL+"/creddetail?ns="+row.get(2)+"\">"+theid+"</a>";
			gc.setTimeInMillis(Long.parseLong(row.get(5)));
			expires = Chrono.niceUTCStamp(gc);
			info = row.get(6);
			//TODO get Warnings 
			warnings = "";
			
			println(sb,indent,"<tr>");
			indent+=2;
			printCell(sb,indent,theid);
			printCell(sb,indent,info);
			printCell(sb,indent,type);
			printCell(sb,indent,expires);
			printCell(sb,indent,warnings);
			indent-=2;
			println(sb,indent,"</tr>");
		}
		indent-=2;
		println(sb,indent,"</table>");
		*/
		return true;
	}

	@Override
	public String user(List<String> row) {
		if( (row != null) && row.size()>1) {
			return row.get(1);
		}
		return null;
	}

	@Override
	public String subject() {
		return String.format("AAF Pending Approval Notification (ENV: %s)",env);
	}

}
