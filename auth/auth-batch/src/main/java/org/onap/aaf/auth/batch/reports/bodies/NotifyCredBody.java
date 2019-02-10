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
import org.onap.aaf.cadi.Access;

public abstract class NotifyCredBody extends NotifyBody {

	private final String explanation;
	public NotifyCredBody(Access access, String name) throws IOException {
		super("cred",name);
		
		// Default
		explanation = "The following Credentials are expiring on the dates shown. "
				+ "Failure to act before the expiration date will cause your App's Authentications to fail.";
	}

	@Override
	public boolean body(AuthzTrans trans, StringBuilder sb, int indent, Notify n, String id) {
		println(sb,indent,explanation);
		println(sb,indent,"<br><br>");
		println(sb,indent,"<table>");
		indent+=2;
		println(sb,indent,"<tr>");
		indent+=2;
		println(sb,indent,"<th>Fully Qualified ID</th>");
		println(sb,indent,"<th>Type</th>");
		println(sb,indent,"<th>Details</th>");
		println(sb,indent,"<th>Expires</th>");
		println(sb,indent,"<th>Cred Detail Page</th>");
		indent-=2;
		println(sb,indent,"</tr>");
		String theid, type, info, gui, expires, notes;
		String p_theid=null, p_type=null, p_gui=null, p_expires=null;
		for(List<String> row : rows.get(id)) {
			theid=row.get(1);
			switch(row.get(3)) {
				case "1":
				case "2":
					type = "Password";
				case "200":
					type = "x509 (Certificate)";
					break;
				default:
					type = "Unknown, see AAF GUI";
					break;
			}
			gui = "<a href=\""+n.guiURL+"/creddetail?ns="+row.get(2)+"\">"+row.get(2)+"</a>";
			expires = row.get(4);
			info = row.get(6);
			notes = row.get(8);
			if(notes!=null && !notes.isEmpty()) {
				info += "<br>" + notes; 
			}
			
			println(sb,indent,"<tr>");
			indent+=2;
			printCell(sb,indent,theid,p_theid);
			printCell(sb,indent,type,p_type);
			printCell(sb,indent,info,null);
			printCell(sb,indent,expires,p_expires);
			printCell(sb,indent,gui,p_gui);
			indent-=2;
			println(sb,indent,"</tr>");
			p_theid=theid;
			p_type=type;
			p_gui=gui;
			p_expires=expires;
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
