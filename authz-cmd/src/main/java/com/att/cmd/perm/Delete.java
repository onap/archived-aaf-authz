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
package com.att.cmd.perm;


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

import aaf.v2_0.PermRequest;

/**
 *
 */
public class Delete extends Cmd {
	public Delete(Perm parent) {
		super(parent,"delete", 
				new Param("type",true), 
				new Param("instance",true),
				new Param("action", true));
	}

	@Override
	public int _exec(final int index, final String ... args) throws CadiException, APIException, LocatorException {
		return same(new Retryable<Integer>() {
			@Override
			public Integer code(Rcli<?> client) throws CadiException, APIException {
				int idx = index;
				// Object Style Delete
				PermRequest pk = new PermRequest();
				pk.setType(args[idx++]);
				pk.setInstance(args[idx++]);
				pk.setAction(args[idx++]);
		
				// Set "Force" if set
				setQueryParamsOn(client);
				Future<PermRequest> fp = client.delete(
						"/authz/perm", 
						getDF(PermRequest.class),
						pk);
				if(fp.get(AAFcli.timeout())) {
					pw().println("Deleted Permission");
				} else {
					if(fp.code()==202) {
						pw().println("Permission Deletion Accepted, but requires Approvals before actualizing");
					} else {
						error(fp);
					}
				}
				return fp.code();
			}
		});
	}

	@Override
	public void detailedHelp(int indent, StringBuilder sb) {
		detailLine(sb,indent,"Delete a Permission with type,instance and action");
		detailLine(sb,indent+4,"see Create for definitions");
		api(sb,indent,HttpMethods.DELETE,"authz/perm",PermRequest.class,true);
	}

}
