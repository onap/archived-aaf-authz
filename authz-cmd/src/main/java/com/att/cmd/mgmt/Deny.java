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
package com.att.cmd.mgmt;

import com.att.cadi.CadiException;
import com.att.cadi.LocatorException;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cadi.client.Retryable;
import com.att.cmd.AAFcli;
import com.att.cmd.BaseCmd;
import com.att.cmd.Cmd;
import com.att.cmd.Param;
import com.att.inno.env.APIException;

public class Deny extends BaseCmd<Mgmt> {
	private final static String[] options = {"add","del"};

	public Deny(Mgmt mgmt) throws APIException {
		super(mgmt, "deny");
		cmds.add(new DenySomething(this,"ip","ipv4or6[,ipv4or6]*"));
		cmds.add(new DenySomething(this,"id","identity[,identity]*"));
	}
	
	public class DenySomething extends Cmd {

		private boolean isID;

		public DenySomething(Deny deny, String type, String repeatable) {
			super(deny, type,
				new Param(optionsToString(options),true),
				new Param(repeatable,true));
			isID = "id".equals(type);
		}

		@Override
		protected int _exec(int _idx, String... args) throws CadiException, APIException, LocatorException {
		        int idx = _idx;
			String action = args[idx++];
			final int option = whichOption(options, action);
			int rv=409;
			for(final String name : args[idx++].split(COMMA)) {
				final String append;
				if(isID && name.indexOf("@")<0) {
					append='@'+ env.getProperty(AAFcli.AAF_DEFAULT_REALM);
				} else {
					append = "";
				}
				final String path = "/mgmt/deny/"+getName() + '/'+ name + append;
				rv = all(new Retryable<Integer>() {
					@Override
					public Integer code(Rcli<?> client) throws APIException, CadiException  {
						int rv = 409;
						Future<Void> fp;
						String resp;
						switch(option) {
							case 0: 
								fp = client.create(path, Void.class);
								resp = " added";
								break;
							default: 
								fp = client.delete(path, Void.class);
								resp = " deleted";
						}
						if(fp.get(AAFcli.timeout())) {
							pw().println(name + append + resp + " on " + client);
							rv=fp.code();
						} else {
							if(rv==409)rv = fp.code();
							error(fp);
						}
						return rv;
					}
				});
			}
			return rv;
		}

	}

}
