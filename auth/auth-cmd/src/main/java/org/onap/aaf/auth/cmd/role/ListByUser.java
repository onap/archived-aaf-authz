/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.cmd.role;

import java.util.Map;
import java.util.TreeMap;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.util.Split;
import org.onap.aaf.misc.env.APIException;

import aaf.v2_0.Perm;
import aaf.v2_0.Perms;
import aaf.v2_0.Role;
import aaf.v2_0.Roles;
import aaf.v2_0.UserRole;
import aaf.v2_0.UserRoles;

/**
 * p
 * @author Jonathan
 *
 */
public class ListByUser extends Cmd {
    private static final String HEADER = "List Roles for User ";

    public ListByUser(List parent) {
        super(parent,"user", 
                new Param("id",true),
                new Param("detail", false)); 
    }

    @Override
    public int _exec( int idx, final String ... args) throws CadiException, APIException, LocatorException {
        final String user=fullID(args[idx]);
    

        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                Perms perms=null;
                UserRoles urs=null;
                Roles roles = null;
                int code;
                Future<UserRoles> fur = client.read(
                        "/authz/userRoles/user/"+user,
                        getDF(UserRoles.class)
                    );
                if (fur.get(AAFcli.timeout())) {
                    urs = fur.value;
                    code = fur.code();
                } else {
                    error(fur);
                    return fur.code();
                }

                if (aafcli.isDetailed()) {
                    roles = new Roles();
                    Future<Perms> fp = client.read(
                            "/authz/perms/user/"+user+"?ns&force", 
                            getDF(Perms.class)
                        );
                    if (fp.get(AAFcli.timeout())) {
                        Map<String, Role> rs = new TreeMap<>();
                        perms = fp.value;
                        for( Perm p : perms.getPerm()) {
                            for(String sr : p.getRoles()) {
                                Role r = rs.get(sr);
                                if(r==null) {
                                    r = new Role();
                                    String[] split = Split.split('|', sr);
                                    if(split.length>1) {
                                        r.setNs(split[0]);
                                        r.setName(split[1]);
                                    } else {
                                        r.setName(sr);
                                    }
                                    rs.put(sr, r);
                                    roles.getRole().add(r);
                                }
                                r.getPerms().add(p);
                            }
                        }
                    } 
                    code = fp.code();
                } else {
                    roles = new Roles();
                    java.util.List<Role> lr = roles.getRole();
                    Role r;
                    for(UserRole ur : urs.getUserRole()) {
                        r = new Role();
                        r.setName(ur.getRole());
                        lr.add(r);
                    }
                }
            
            
                ((List)parent).report(roles,perms,urs,HEADER,user);
                return code;
            }
        });
    }

    @Override
    public void detailedHelp(int indent, StringBuilder sb) {
        detailLine(sb,indent,HEADER);
        api(sb,indent,HttpMethods.GET,"authz/roles/user/<user>",Roles.class,true);
    }
}
