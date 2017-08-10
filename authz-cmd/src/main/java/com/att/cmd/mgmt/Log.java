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

import com.att.authz.common.Define;
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

public class Log extends BaseCmd<Mgmt> {
	private final static String[] options = {"add","del"};

	public Log(Mgmt mgmt) throws APIException {
		super(mgmt, "log",
				new Param(optionsToString(options),true),
				new Param("id[,id]*",true));
	}
	
	@Override
	public int _exec(int _idx, String ... args) throws CadiException, APIException, LocatorException {
		int rv=409;
		int idx = _idx;
		final int option = whichOption(options, args[idx++]);

		for(String name : args[idx++].split(COMMA)) {
			final String fname;
			if(name.indexOf("@")<0) {
				fname=name+'@'+ env.getProperty(AAFcli.AAF_DEFAULT_REALM);
			} else {
				fname = name;
			}
			
			

			rv = all(new Retryable<Integer>() {
				@Override
				public Integer code(Rcli<?> client) throws APIException, CadiException {
					int rv = 409;
					Future<Void> fp;
					String str = "/mgmt/log/id/"+fname;
					String msg;
					switch(option) {
						case 0:	
							fp = client.create(str,Void.class);
							msg = "Added";
							break;
						case 1:
							fp = client.delete(str,Void.class);
							msg = "Deleted";
							break;
						default:
							fp = null;
							msg = "Ignored";
					}
							
					if(fp!=null) {
						if(fp.get(AAFcli.timeout())) {
							pw().println(msg + " Special Log for " + fname + " on " + client);
							rv=200;
						} else {
							if(rv==409)rv = fp.code();
							error(fp);
						}
						return rv;
					}
					return rv;
				}
			});
		}
		return rv;
	}

	@Override
	public void detailedHelp(int _indent, StringBuilder sb) {
	        int indent = _indent;
		detailLine(sb,indent,"Clear the cache for certain tables");
		indent+=2;
		detailLine(sb,indent,"name        - name of table or 'all'");
		detailLine(sb,indent+14,"Must have admin rights to '" + Define.ROOT_NS + '\'');
		indent-=2;
		api(sb,indent,HttpMethods.DELETE,"mgmt/cache/:name",Void.class,true);
	}
}
