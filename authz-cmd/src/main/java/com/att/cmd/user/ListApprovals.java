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
package com.att.cmd.user;

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

import aaf.v2_0.Approvals;

/**
 * 
 *
 */
public class ListApprovals extends Cmd {
	private static final String HEADER = "List Approvals"; 
	private final static String[] options = {"user","approver","ticket"};
	public ListApprovals(List parent) {
		super(parent,"approvals", 
				new Param(optionsToString(options),true),
				new Param("value",true)); 
	}

	@Override
	public int _exec(int _idx, final String ... args) throws CadiException, APIException, LocatorException {
	        int idx = _idx;
		final String type = args[idx++];
		int option = whichOption(options,type);
		String value = args[idx++];
		final String fullValue;
		if (option != 2) {
			String realm = getOrgRealm();
			fullValue = (value.indexOf('@')<0 && realm != null)?value +'@'+realm:value;
		} else {
		    fullValue = value;
		}
		return same(new Retryable<Integer>() {
			@Override
			public Integer code(Rcli<?> client) throws CadiException, APIException {
				Future<Approvals> fp = client.read(
						"/authz/approval/"+type+'/'+fullValue, 
						getDF(Approvals.class)
						);
				if(fp.get(AAFcli.timeout())) {
					((List)parent).report(fp.value,HEADER + " by " + type,fullValue);
					if(fp.code()==404) {
					    return 200;
					}
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
		detailLine(sb,indent,"Approvals are used when the Requestor does not have the rights");
		detailLine(sb,indent,"to perform the action required.  Approvers are those listed as");
		detailLine(sb,indent,"responsible for Namespace associated with the request, and those");
		detailLine(sb,indent,"required by the Company by Policy.  This may be, for instance");
		detailLine(sb,indent,"the supervisor of the requestor");
		sb.append('\n');
		detailLine(sb,indent,"Delegates can be listed by User, Approver or Ticket.");
		indent-=2;
		api(sb,indent,HttpMethods.GET,"authz/approval/user/<value>",Approvals.class,true);
		api(sb,indent,HttpMethods.GET,"authz/approval/approver/<value>",Approvals.class,false);
		api(sb,indent,HttpMethods.GET,"authz/approval/ticket/<value>",Approvals.class,false);
	}


}
