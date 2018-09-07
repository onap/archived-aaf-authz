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

import aaf.v2_0.Perms;
import aaf.v2_0.Roles;
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
                Future<Roles> fr = client.read(
                        "/authz/roles/user/"+user+(aafcli.isDetailed()?"?ns":""), 
                        getDF(Roles.class)
                        );
                Future<UserRoles> fur = client.read(
                        "/authz/userRoles/user/"+user,
                        getDF(UserRoles.class)
                    );
                if (fr.get(AAFcli.timeout())) {
                    if (aafcli.isDetailed()) {
                        Future<Perms> fp = client.read(
                                "/authz/perms/user/"+user+(aafcli.isDetailed()?"?ns":""), 
                                getDF(Perms.class)
                            );
                        if (fp.get(AAFcli.timeout())) {
                            perms = fp.value;
                        }
                    }
                    if (fur.get(AAFcli.timeout())) {
                        urs = fur.value;
                    }
                    
                    ((List)parent).report(fr.value,perms,urs,HEADER,user);
                } else {
                    error(fr);
                }
                return fr.code();
            }
        });
    }
    
    @Override
    public void detailedHelp(int indent, StringBuilder sb) {
        detailLine(sb,indent,HEADER);
        api(sb,indent,HttpMethods.GET,"authz/roles/user/<user>",Roles.class,true);
    }
}
