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

import aaf.v2_0.UserRoleRequest;

/**
 * p
 * 
 *
 */
public class Role extends Cmd {
	private static final String[] options = {"add", "del", "setTo","extend"};
	public Role(User parent) {
		super(parent, "role", new Param(optionsToString(options), true), new Param("user", true), new Param(
				"role[,role]* (!REQ S)", false));
	}

	@Override
	public int _exec(final int index, final String ... args) throws CadiException, APIException, LocatorException {
		return same(new Retryable<Integer>() {
			@Override
			public Integer code(Rcli<?> client) throws CadiException, APIException {
				int idx = index;
				String key = args[idx++];
				int option = whichOption(options, key);
				String user = args[idx++];
				String realm = getOrgRealm();

				UserRoleRequest urr = new UserRoleRequest();
				if (user.indexOf('@') < 0 && realm != null) user += '@' + realm;
				urr.setUser(user);
				// Set Start/End commands
				setStartEnd(urr);

				Future<?> fp = null;

				if (option != 2) {
					if (args.length < 5) {
						throw new CadiException(build(new StringBuilder("Too few args: "), null).toString());                        
					}
					String[] roles = args[idx++].split(",");
					for (String role : roles) {
						String verb = null,participle=null;
						urr.setRole(role);
						// You can request to be added or removed from role.
						setQueryParamsOn(client);
						switch(option) {
						  case 0:
							fp = client.create("/authz/userRole", getDF(UserRoleRequest.class), urr);
							verb = "Added";
							participle = "] to User [" ;
							break;
						  case 1:
							fp = client.delete("/authz/userRole/" + urr.getUser() + '/' + urr.getRole(), Void.class);
							verb = "Removed";
							participle = "] from User [" ;
							break;
						  case 3:
							fp = client.update("/authz/userRole/extend/" + urr.getUser() + '/' + urr.getRole());
							verb = "Extended";
							participle = "] to User [" ;
							break;
						  default:
							throw new CadiException("Invalid action [" + key + ']');
						}
						if (fp.get(AAFcli.timeout())) {
							pw().print(verb);
							pw().print(" Role [");
							pw().print(urr.getRole());
							pw().print(participle);
							pw().print(urr.getUser());
							pw().println(']');
						} else {
							switch(fp.code()) {
							case 202:
								pw().print("UserRole ");
								pw().print(option == 0 ? "Creation" : option==1?"Deletion":"Extension");
								pw().println(" Accepted, but requires Approvals before actualizing");
								break;
							case 404:
								if(option==3) {
									pw().println("Failed with code 404: UserRole is not found, or you do not have permission to view");
									break;
								}
							default:
								error(fp);
							}
						}
					}
				} else {
					// option 2 is setTo command (an update call)
					String allRoles = "";
					if (idx < args.length)
						allRoles = args[idx++];

					urr.setRole(allRoles);
					fp = client.update("/authz/userRole/user", getDF(UserRoleRequest.class), urr);
					if (fp.get(AAFcli.timeout())) {
						pw().println("Set User's Roles to [" + allRoles + "]");
					} else {
						error(fp);
					}
				}
				return fp == null ? 0 : fp.code();
			}
		});
	}

	@Override
	public void detailedHelp(int indent, StringBuilder sb) {
		detailLine(sb, indent, "Add OR Delete a User to/from a Role OR");
		detailLine(sb, indent, "Set a User's Roles to the roles supplied");
		detailLine(sb, indent + 2, "user    - ID of User");
		detailLine(sb, indent + 2, "role(s) - Role or Roles to which to add the User");
		sb.append('\n');
		detailLine(sb, indent + 2, "Note: this is the same as \"role user add...\" except allows");
		detailLine(sb, indent + 2, "assignment of user to multiple roles");
		detailLine(sb, indent + 2, "WARNING: Roles supplied with setTo will be the ONLY roles attached to this user");
		detailLine(sb, indent + 2, "If no roles are supplied, user's roles are reset.");
		api(sb, indent, HttpMethods.POST, "authz/userRole", UserRoleRequest.class, true);
		api(sb, indent, HttpMethods.DELETE, "authz/userRole/<user>/<role>", Void.class, false);
		api(sb, indent, HttpMethods.PUT, "authz/userRole/<user>", UserRoleRequest.class, false);
	}

}
