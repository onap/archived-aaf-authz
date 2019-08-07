/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.cmd.user;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;

import aaf.v2_0.UserRoleRequest;

/**
 * p
 * 
 * @author Jonathan
 *
 */
public class Role extends Cmd {
    private static final String[] options = {"add", "del", "extend"};
    public Role(User parent) {
        super(parent, "role", new Param(optionsToString(options), true), new Param("user", true),
                new Param("role[,role]*", false));
    }

    @Override
    public int _exec(final int index, final String ... args) throws CadiException, APIException, LocatorException {
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                int idx = index;
                String key = args[idx++];
                int option = whichOption(options, key);
                final String user = fullID(args[idx++]);

                UserRoleRequest urr = new UserRoleRequest();
                urr.setUser(user);
                // Set Start/End commands
                setStartEnd(urr);

                Future<?> fp = null;

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
                      case 2:
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
                            if (option==3) {
                                pw().println("Failed with code 404: UserRole is not found, or you do not have permission to view");
                                break;
                            }
                        default:
                            error(fp);
                        }
                    }
                }
                return fp == null ? 0 : fp.code();
            }
        });
    }

    @Override
    public void detailedHelp(int indent, StringBuilder sb) {
        detailLine(sb, indent, "Add or Delete a User to/from a Role OR extend Expiration");
        detailLine(sb, indent + 2, "user    - ID of User");
        detailLine(sb, indent + 2, "role(s) - Role or Roles to which to add the User");
        sb.append('\n');
        api(sb, indent, HttpMethods.POST, "authz/userRole", UserRoleRequest.class, true);
        api(sb, indent, HttpMethods.DELETE, "authz/userRole/<user>/<role>", Void.class, false);
        api(sb,indent,HttpMethods.PUT,"authz/userRole/extend/<user>/<role>",Void.class,false);

    }

}
