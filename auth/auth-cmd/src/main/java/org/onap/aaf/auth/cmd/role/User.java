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

package org.onap.aaf.auth.cmd.role;

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
 * @author Jonathan
 *
 */
public class User extends Cmd {
    private final static String[] options = {"add","del","extend"};
    public User(Role parent) {
        super(parent,"user",
                new Param(optionsToString(options),true),
                new Param("role",true),
                new Param("id[,id]*",false));
    }

    @Override
    public int _exec(final int index, final String ... args) throws CadiException, APIException, LocatorException {
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                int idx = index;
                String action = args[idx++];
                int option = whichOption(options, action);
                UserRoleRequest urr = new UserRoleRequest();
                urr.setRole(args[idx++]);
                // Set Start/End commands
                setStartEnd(urr);

                Future<?> fp = null;

                String[] ids = args[idx].split(",");
                String verb=null,participle=null;
                // You can request to be added or removed from role.
                setQueryParamsOn(client);

                for (String id: ids) {
                    id=fullID(id);
                    urr.setUser(id);
                    switch(option) {
                        case 0:
                            fp = client.create(
                                    "/authz/userRole",
                                    getDF(UserRoleRequest.class),
                                    urr);
                            verb = "Added";
                            participle = "] to Role [" ;
                            break;
                        case 1:
                            fp = client.delete(
                                    "/authz/userRole/"+urr.getUser()+'/'+urr.getRole(),
                                    Void.class);
                            verb = "Removed";
                            participle = "] from Role [" ;
                            break;
                        case 2:
                            fp = client.update("/authz/userRole/extend/" + urr.getUser() + '/' + urr.getRole());
                            verb = "Extended";
                            participle = "] in Role [" ;
                            break;

                        default: // actually, should never get here...
                            throw new CadiException("Invalid action [" + action + ']');
                    }
                    if (fp.get(AAFcli.timeout())) {
                        pw().print(verb);
                        pw().print(" User [");
                        pw().print(urr.getUser());
                        pw().print(participle);
                        pw().print(urr.getRole());
                        pw().println(']');
                    } else {
                        switch(fp.code()) {
                            case 202:
                                pw().print("User Role ");
                                pw().print(action);
                                pw().println(" is Accepted, but requires Approvals before actualizing");
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
                return fp==null?0:fp.code();
            }
        });
    }

    @Override
    public void detailedHelp(int indent, StringBuilder sb) {
        detailLine(sb,indent,"Add OR Delete a User to/from a Role OR extend Expiration");
        detailLine(sb,indent+2,"role  - Name of Role to create");
        detailLine(sb,indent+2,"id(s) - ID or IDs to add to the Role");
        sb.append('\n');
        api(sb,indent,HttpMethods.POST,"authz/userRole",UserRoleRequest.class,true);
        api(sb,indent,HttpMethods.DELETE,"authz/userRole/<user>/<role>",Void.class,false);
        api(sb,indent,HttpMethods.PUT,"authz/userRole/extend/<user>/<role>",Void.class,false);
    }

}
