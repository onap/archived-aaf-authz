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

public class NotifyCredBody extends AbsCredBody {
	private final String explanation;
	public NotifyCredBody(Access access, String name) throws IOException {
		super(name);
		
		// Default
		explanation = "The following Credentials are expiring on the dates shown. "
				+ "Failure to act before the expiration date will cause your App's Authentications to fail.";
	}

	@Override
	public String body(AuthzTrans trans, Notify n, String id) {
		StringBuilder sb = new StringBuilder();
		sb.append(explanation);
		sb.append("<br>");
		sb.append("<tr>\n" + 
				"<th>Role</th>\n" + 
				"<th>Expires</th>\n" + 
				"</tr>\n");
		for(List<String> row : rows.get(id)) {
			
		}
		return sb.toString();
	}
}
