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
package com.att.cmd.mgmt;

import com.att.authz.common.Define;
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

/**
 * p
 *
 */
public class SessClear extends Cmd {
	public SessClear(Session parent) {
		super(parent,"clear",
				new Param("machine",true));
	}

	@Override
	public int _exec(int idx, String ... args) throws CadiException, APIException, LocatorException {
		int rv=409;
		String machine = args[idx++];
		rv = oneOf(new Retryable<Integer>() {
			@Override
			public Integer code(Rcli<?> client) throws APIException, CadiException {
				int rv = 409;
				Future<Void> fp = client.delete(
						"/mgmt/dbsession", 
						Void.class
						);
				if(fp.get(AAFcli.timeout())) {
					pw().println("Cleared DBSession on " + client);
					rv=200;
				} else {
					if(rv==409)rv = fp.code();
					error(fp);
				}
				return rv;
			}
		},machine);
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
