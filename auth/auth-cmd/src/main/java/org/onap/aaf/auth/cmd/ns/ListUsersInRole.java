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

package org.onap.aaf.auth.cmd.ns;

import java.util.HashSet;
import java.util.Set;

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

import aaf.v2_0.Nss;
import aaf.v2_0.Nss.Ns;
import aaf.v2_0.Role;
import aaf.v2_0.Roles;
import aaf.v2_0.Users;
import aaf.v2_0.Users.User;

/**
 * p
 * @author Jonathan
 *
 */
public class ListUsersInRole extends Cmd {
    private static final String HEADER="List Users in Roles of Namespace ";

    public ListUsersInRole(ListUsers parent) {
        super(parent,"role",
                new Param("ns-name",true));
    }

    @Override
    public int _exec(int _idx, final String ... args) throws CadiException, APIException, LocatorException {
            int idx = _idx;
        final String ns=args[idx++];
        final boolean detail = aafcli.isDetailed();
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                ((ListUsers)parent).report(HEADER,ns);
                Future<Nss> fn = client.read("/authz/nss/"+ns,getDF(Nss.class));
                if (fn.get(AAFcli.timeout())) {
                    if (fn.value!=null) {
                        Set<String> uset = detail?null:new HashSet<>();
                        for (Ns n : fn.value.getNs()) {
                            Future<Roles> fr = client.read("/authz/roles/ns/"+n.getName(), getDF(Roles.class));
                            if (fr.get(AAFcli.timeout())) {
                                for (Role r : fr.value.getRole()) {
                                    if (detail) {
                                        ((ListUsers)parent).report(r.getName());
                                    }
                                    Future<Users> fus = client.read(
                                            "/authz/users/role/"+r.getName(),
                                            getDF(Users.class)
                                            );
                                    if (fus.get(AAFcli.timeout())) {
                                        for (User u : fus.value.getUser()) {
                                            if (detail) {
                                                ((ListUsers)parent).report("  ",u);
                                            } else {
                                                uset.add(u.getId());
                                            }
                                        }
                                    } else if (fn.code()==404) {
                                        return 200;
                                    }
                                }
                            }
                        }
                        if (uset!=null) {
                            for (String u : uset) {
                                pw().print("  ");
                                pw().println(u);
                            }
                        }
                    }
                } else if (fn.code()==404) {
                    return 200;
                } else {
                    error(fn);
                }
                return fn.code();
            }
        });
    }

    @Override
    public void detailedHelp(int _indent, StringBuilder sb) {
            int indent = _indent;
        detailLine(sb,indent,HEADER);
        indent+=4;
        detailLine(sb,indent,"Report Users associated with this Namespace's Roles");
        sb.append('\n');
        detailLine(sb,indent,"If \"details\" is specified, then all roles are printed ");
        detailLine(sb,indent,"with the associated users and expiration dates");
        indent-=4;
        api(sb,indent,HttpMethods.GET,"authz/nss/<ns>",Nss.class,true);
        api(sb,indent,HttpMethods.GET,"authz/roles/ns/<ns>",Roles.class,false);
        api(sb,indent,HttpMethods.GET,"authz/users/role/<ns>",Users.class,false);
    }

}
