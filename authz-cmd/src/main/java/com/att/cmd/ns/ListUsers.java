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

import javax.xml.datatype.XMLGregorianCalendar;

import com.att.cmd.BaseCmd;

import aaf.v2_0.Users.User;

public class ListUsers extends BaseCmd<List> {
	
	public ListUsers(List parent) {
		super(parent,"user");
		cmds.add(new ListUsersWithPerm(this));
		cmds.add(new ListUsersInRole(this));
	}

	public void report(String header, String ns) {
		((List)parent).report(null, header,ns);
	}

	public void report(String subHead) {
		pw().println(subHead);
	}

	private static final String uformat = "%s%-50s expires:%02d/%02d/%04d\n";
	public void report(String prefix, User u) {
		XMLGregorianCalendar xgc = u.getExpires();
		pw().format(uformat,prefix,u.getId(),xgc.getMonth()+1,xgc.getDay(),xgc.getYear());
	}

}
