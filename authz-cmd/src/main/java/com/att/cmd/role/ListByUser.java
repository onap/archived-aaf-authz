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
package com.att.cmd.role;

import java.util.ArrayList;
import java.util.HashMap;

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
import com.att.inno.env.util.Chrono;

import aaf.v2_0.Nss;
import aaf.v2_0.Pkey;
import aaf.v2_0.Roles;
import aaf.v2_0.Users;

/**
 * p
 *
 */
public class ListByUser extends Cmd {
	private static final String HEADER = "List Roles for User ";
	
	public ListByUser(List parent) {
		super(parent,"user", 
				new Param("id",true)); 
	}

	@Override
	public int _exec( int idx, final String ... args) throws CadiException, APIException, LocatorException {
		String user=args[idx];
		String realm = getOrgRealm();
		final String fullUser;
		if (user.indexOf('@') < 0 && realm != null) {
		    fullUser = user + '@' + realm;
		} else {
		    fullUser = user;
		}

		return same(new Retryable<Integer>() {
			@Override
			public Integer code(Rcli<?> client) throws CadiException, APIException {

				Future<Roles> fp = client.read(
						"/authz/roles/user/"+fullUser, 
						getDF(Roles.class)
						);
				if(fp.get(AAFcli.timeout())) {
					Future<Nss> fn = null;
					ArrayList<String> roleNss = null;
					ArrayList<String> permNss = null;
					HashMap<String, Boolean> expiredMap = new HashMap<String, Boolean>();
					if (aafcli.isDetailed()) {
						roleNss = new ArrayList<String>();
						permNss = new ArrayList<String>();
						for(aaf.v2_0.Role p : fp.value.getRole()) {
							String roleNs = p.getName();
							do {
								roleNs = p.getName().substring(0,roleNs.lastIndexOf('.'));
								fn = client.read("/authz/nss/"+roleNs,getDF(Nss.class));
							} while (!fn.get(AAFcli.timeout()));
							roleNss.add(roleNs);
	
							for(Pkey perm : p.getPerms()) {
								if (perm.getType().contains(roleNs)) {
								    permNss.add(roleNs);
								} else {
									Future<Nss> fpn = null;
									String permType = perm.getType();
									String permNs = permType;
									do {
										permNs = permType.substring(0,permNs.lastIndexOf('.'));
										fpn = client.read("/authz/nss/"+permNs,getDF(Nss.class));
									} while (!fpn.get(AAFcli.timeout()));
									permNss.add(permNs);
								}
							}
						}
					}
					
					if (fp.value != null) {
						for(aaf.v2_0.Role p : fp.value.getRole()) {
							Future<Users> fu = client.read(
									"/authz/userRole/"+fullUser+"/"+p.getName(), 
									getDF(Users.class)
									);
							if (fu.get(5000)) {
								if(fu.value != null) {
								    for (Users.User u : fu.value.getUser()) {
								    	if(u.getExpires().normalize().compare(Chrono.timeStamp().normalize()) > 0) {
								    		expiredMap.put(p.getName(), new Boolean(false));
								    	} else {
								    		expiredMap.put(p.getName(), new Boolean(true));
								    	}
								    }
								}
							}
						}	
					}
					
					((List)parent).report(fp,roleNss,permNss,expiredMap,HEADER,fullUser);
				} else {
					error(fp);
				}
				return fp.code();
			}
		});
	}
	
	@Override
	public void detailedHelp(int indent, StringBuilder sb) {
		detailLine(sb,indent,HEADER);
		api(sb,indent,HttpMethods.GET,"authz/roles/user/<user>",Roles.class,true);
	}


}
