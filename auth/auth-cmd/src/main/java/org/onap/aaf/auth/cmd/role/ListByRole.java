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

import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.misc.env.APIException;

import aaf.v2_0.Roles;

/**
 *
 * @author Jonathan
 *
 */
public class ListByRole extends Cmd {
    private static final String HEADER="List Roles for Role";

    public ListByRole(List parent) {
        super(parent,"role",
                new Param("role",true));
    }

    @Override
    public int _exec(final int idx, final String ... args) throws CadiException, APIException, LocatorException {
        return same(((List)parent).new ListRoles() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                String role=args[idx];
                Future<Roles> fp = client.read(
                        "/authz/roles/"+role+(aafcli.isDetailed()?"?ns":""),
                        getDF(Roles.class)
                        );
                return list(fp,client,HEADER+"["+role+"]");
            }
        });
    }

    @Override
    public void detailedHelp(int indent, StringBuilder sb) {
        detailLine(sb,indent,HEADER);
        api(sb,indent,HttpMethods.GET,"authz/roles/<role>",Roles.class,true);
    }

}
