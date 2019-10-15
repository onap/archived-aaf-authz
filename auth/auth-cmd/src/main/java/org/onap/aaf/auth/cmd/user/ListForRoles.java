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

import aaf.v2_0.Users;

/**
 * p
 * @author Jonathan
 *
 */
public class ListForRoles extends Cmd {
    private static final String HEADER = "List Users for Role";
    public ListForRoles(List parent) {
        super(parent,"role", new Param("role",true)); 
    }

    @Override
    public int _exec(int _idx, final String ... args) throws CadiException, APIException, LocatorException {
            int idx = _idx;
        final String role = args[idx++];
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                Future<Users> fp = client.read(
                        "/authz/users/role/"+role, 
                        getDF(Users.class)
                        );
                if (fp.get(AAFcli.timeout())) {
                    ((org.onap.aaf.auth.cmd.user.List)parent).report(fp.value,false, HEADER,role);
                    if (fp.code()==404)return 200;
                } else {
                    error(fp);
                }
                return fp.code();
            }
        });
    }

    @Override
    public void detailedHelp(int _indent, StringBuilder sb) {
            int indent = _indent;
        detailLine(sb,indent,HEADER);
        indent+=2;
        detailLine(sb,indent,"This report lists the users associated to Roles.");
        detailLine(sb,indent,"role - the Role name");
        indent-=2;
        api(sb,indent,HttpMethods.GET,"authz/users/role/<role>",Users.class,true);
    }

}
