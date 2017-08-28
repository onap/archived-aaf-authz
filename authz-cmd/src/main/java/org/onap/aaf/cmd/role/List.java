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
package org.onap.aaf.cmd.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.onap.aaf.cmd.AAFcli;
import org.onap.aaf.cmd.BaseCmd;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.inno.env.APIException;

import aaf.v2_0.Nss;
import aaf.v2_0.Pkey;
import aaf.v2_0.Roles;



public class List extends BaseCmd<Role> {
	private static final String LIST_ROLES_BY_NAME = "list roles for role";

	public List(Role parent) {
		super(parent,"list");
		cmds.add(new ListByUser(this));
		cmds.add(new ListByRole(this));
		cmds.add(new ListByNS(this));
		cmds.add(new ListByNameOnly(this));
		cmds.add(new ListByPerm(this));
		cmds.add(new ListActivity(this));
	}
	
	// Package Level on purpose
	abstract class ListRoles extends Retryable<Integer> {
		protected int list(Future<Roles> fp,Rcli<?> client, String header) throws APIException, CadiException {
			if(fp.get(AAFcli.timeout())) {
				Future<Nss> fn = null;
				ArrayList<String> roleNss = null;
				ArrayList<String> permNss = null;
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
							if (perm.getType().contains(roleNs))
								permNss.add(roleNs);
							else {
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
				report(fp,roleNss,permNss,null,header);
			} else {
				error(fp);
			}
			return fp.code();
		}
	}

	private final static String roleFormat = "%-50s\n";
	
	private static final Comparator<aaf.v2_0.Role> roleCompare = new Comparator<aaf.v2_0.Role>() {
		@Override
		public int compare(aaf.v2_0.Role a, aaf.v2_0.Role b) {
			return a.getName().compareTo(b.getName());
		}
	};
	public void report(Future<Roles> fp, ArrayList<String> roleNss, ArrayList<String> permNss,
			HashMap<String,Boolean> expiredMap, String ... str) {
		reportHead(str);
		if (fp != null && aafcli.isDetailed() && str[0].toLowerCase().contains(LIST_ROLES_BY_NAME)) {
			String description = fp.value.getRole().get(0).getDescription();
			if (description == null) description = "";
			reportColHead("%-80s\n","Description: " + description);
		} 			

		if(fp==null) {
			pw().println("<No Roles Found>");
		} else if (aafcli.isDetailed()){
			String permFormat = "   %-20s %-15s %-30s %-15s\n";
			String fullFormat = roleFormat+permFormat;
			reportColHead(fullFormat,"[ROLE NS].Name","PERM NS","Type","Instance","Action");
			Collections.sort(fp.value.getRole(),roleCompare);
			for(aaf.v2_0.Role p : fp.value.getRole()) {
				String roleNs = roleNss.remove(0);
				pw().format(roleFormat, "["+roleNs+"]"+p.getName().substring(roleNs.length()));
				for(Pkey perm : p.getPerms()) {
					String permNs = permNss.remove(0);
					pw().format(permFormat, 
							permNs,
							perm.getType().substring(permNs.length()+1),
							perm.getInstance(),
							perm.getAction());
				}
			}
		} else {
			String permFormat = "   %-30s %-30s %-15s\n";
			String fullFormat = roleFormat+permFormat;
			reportColHead(fullFormat,"ROLE Name","PERM Type","Instance","Action");
			Collections.sort(fp.value.getRole(),roleCompare);
			for(aaf.v2_0.Role p : fp.value.getRole()) {
				if (expiredMap != null) {
					String roleName = p.getName();
					Boolean b = expiredMap.get(roleName);
					if (b != null && b.booleanValue())
						pw().format(roleFormat, roleName+"*");
					else {
						pw().format(roleFormat, roleName);
						for(Pkey perm : p.getPerms()) {
							pw().format(permFormat, 
									perm.getType(),
									perm.getInstance(),
									perm.getAction());
						}
					}
				} else {
					pw().format(roleFormat, p.getName());
					for(Pkey perm : p.getPerms()) {
						pw().format(permFormat, 
								perm.getType(),
								perm.getInstance(),
								perm.getAction());
					}
				}
			}
		}
	}

}
