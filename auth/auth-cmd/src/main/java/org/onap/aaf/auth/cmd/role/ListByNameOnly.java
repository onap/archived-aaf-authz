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
 * Return Roles by NS
 * <p>
 * @author Jonathan
 *
 */
public class ListByNameOnly extends Cmd {
    private static final String HEADER = "List Roles by Name ";

    public ListByNameOnly(List parent) {
        super(parent,"name", 
                new Param("name",true)); 
    }

    @Override
    public int _exec( int idx, final String ... args) throws CadiException, APIException, LocatorException {
        final String name=args[idx];

        return same(((List)parent).new ListRoles() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                Future<Roles> fp = client.read(
                        "/authz/roles/name/"+name+(aafcli.isDetailed()?"?ns":""), 
                        getDF(Roles.class)
                        );
                return list(fp,client, HEADER+"["+name+"]");
            }
        });
    }

    @Override
    public void detailedHelp(int indent, StringBuilder sb) {
        detailLine(sb,indent,HEADER);
        api(sb,indent,HttpMethods.GET,"authz/roles/name/<name>",Roles.class,true);
    }


}
