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
package com.att.cmd.ns;

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

public class Attrib extends BaseCmd<NS> {
	private final static String[] options = {"add","upd","del"};

	public Attrib(NS ns) throws APIException {
		super(ns,"attrib",
				new Param(optionsToString(options),true),
				new Param("ns",true),
				new Param("key",true),
				new Param("value",false)
		);
	}

	@Override
	public int _exec(final int idx, final String ... args) throws CadiException, APIException, LocatorException {
		final int option = whichOption(options, args[idx]);
		final String ns = args[idx+1];
		final String key = args[idx+2];
		final String value;
		if(option!=2) {
			if(args.length<=idx+3) {
				throw new CadiException("Not added: Need more Data");
			}
			value = args[idx+3];
		} else {
			value = "";
		}
		
		return same(new Retryable<Integer>() {
			@Override
			public Integer code(Rcli<?> client) throws CadiException, APIException {	
				Future<Void> fp = null;
				String message;
				switch(option) {
					case 0: 
						fp = client.create("/authz/ns/"+ns+"/attrib/"+key+'/'+value,Void.class);
						message = String.format("Add Attrib %s=%s to %s",
								key,value,ns);
						break;
					case 1: 
						fp = client.update("/authz/ns/"+ns+"/attrib/"+key+'/'+value);
						message = String.format("Update Attrib %s=%s for %s",
								key,value,ns);
						break;
					case 2: 
						fp = client.delete("/authz/ns/"+ns+"/attrib/"+key,Void.class);
						message = String.format("Attrib %s deleted from %s",
								key,ns);
						break;
					default:
						throw new CadiException("Bad Argument");
				};
			
				if(fp.get(AAFcli.timeout())) {
					pw().println(message);
				} else {
					error(fp);
					return fp.code();
				}
					
				return fp==null?500:fp.code();
			}
		});
	}

	@Override
	public void detailedHelp(int _indent, StringBuilder sb) {
	    	int indent = _indent;
		detailLine(sb,indent,"Add or Delete Administrator to/from Namespace");
		indent+=4;
		detailLine(sb,indent,"name - Name of Namespace");
		detailLine(sb,indent,"id   - Credential of Person(s) to be Administrator");
		sb.append('\n');
		detailLine(sb,indent,"aafcli will call API on each ID presented.");
		indent-=4;
		api(sb,indent,HttpMethods.POST,"authz/ns/<ns>/admin/<id>",Void.class,true);
		api(sb,indent,HttpMethods.DELETE,"authz/ns/<ns>/admin/<id>",Void.class,false);
	}

}
