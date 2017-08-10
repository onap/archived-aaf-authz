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

import java.text.ParseException;
import java.util.Date;

import com.att.cadi.CadiException;
import com.att.cadi.LocatorException;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cadi.client.Retryable;
import com.att.cmd.AAFcli;
import com.att.cmd.BaseCmd;
import com.att.cmd.Param;
import com.att.cssa.rserv.HttpMethods;
import com.att.inno.env.APIException;
import com.att.inno.env.util.Chrono;
import com.att.rosetta.env.RosettaDF;

import aaf.v2_0.DelgRequest;

public class Delg extends BaseCmd<User> {
	static final String AUTHZ_DELG = "/authz/delegate";
	private final static String[] options = {"add","upd","del"};

	public Delg(User user) throws APIException {
		super(user,"delegate",
				new Param(optionsToString(options),true),
				new Param("from",true),
				new Param("to REQ A&U",false),
				new Param("until (YYYY-MM-DD) REQ A", false)
		);
	}

	@Override
	public int _exec(final int index, final String ... args) throws CadiException, APIException, LocatorException {
		return same(new Retryable<Integer>() {
			@Override
			public Integer code(Rcli<?> client) throws CadiException, APIException {
				int idx = index;
				String realm = getOrgRealm();
				DelgRequest dr = new DelgRequest();
				setStartEnd(dr);
		
				int option= whichOption(options, args[idx++]);
				String user = args[idx++];
				if (user.indexOf('@') < 0 && realm != null) user += '@' + realm;
				dr.setUser(user);
				if(option<2) {
					String delegate = args[idx++];
					if (delegate.indexOf('@') < 0 && realm != null) delegate += '@' + realm;
					dr.setDelegate(delegate);
					if(option<2 && args.length>idx) {
						Date date;
						try {
							date = Chrono.dateOnlyFmt.parse(args[idx++]);
						} catch (ParseException e) {
							throw new CadiException(e);
						}
						dr.setEnd(Chrono.timeStamp(date));
					}
				}
		
				Future<DelgRequest> fp;
				RosettaDF<DelgRequest> df = getDF(DelgRequest.class);
				String verb;
				setQueryParamsOn(client);

				switch(option) {
					case 0: 
						fp = client.create(AUTHZ_DELG, df, dr);
						verb = "Added";
						break;
					case 1: 
						fp = client.update(AUTHZ_DELG, df, dr); 
						verb = "Updated";
						break;
					case 2: 
						fp = client.delete(AUTHZ_DELG, df, dr); 
						verb = "Deleted";
						break;
					default:
						throw new CadiException("Bad Argument");
				};
				
				if(fp.get(AAFcli.timeout())) {
					pw().append("Delegate ");
					pw().println(verb);
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
		detailLine(sb,indent,"Add, Update or Delete Delegate");
		indent+=2;
		detailLine(sb,indent,"A Delegate is a person who will temporarily cover the Approval and");
		detailLine(sb,indent,"Ownership questions on behalf of the person Responsible.");
		sb.append('\n');
		detailLine(sb,indent,"fromID - the person who is the Responsible person of record");
		detailLine(sb,indent,"toID   - the person who will be delegated (required for Add/Update)");
		detailLine(sb,indent,"until  - the end date for this delegation");
		indent-=2;
		api(sb,indent,HttpMethods.POST,AUTHZ_DELG,DelgRequest.class,true);
		api(sb,indent,HttpMethods.DELETE,AUTHZ_DELG,DelgRequest.class,false);
		api(sb,indent,HttpMethods.PUT,AUTHZ_DELG,DelgRequest.class,false);
	}

}
