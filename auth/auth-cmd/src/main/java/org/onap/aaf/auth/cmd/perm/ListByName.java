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

package org.onap.aaf.auth.cmd.perm;

import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.misc.env.APIException;

import aaf.v2_0.Perms;

/**
 *
 * @author Jonathan
 *
 */
public class ListByName extends Cmd {
    private static final String HEADER = "List Child Permissions";

    public ListByName(List parent) {
        super(parent,"name", 
                new Param("root perm name",true)); 
    }

    public int _exec(final int index, final String ... args) throws CadiException, APIException, LocatorException {
        return same(((List)parent).new ListPerms() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                String parentPerm=args[index];
            
                Future<Perms> fp = client.read(
                        "/authz/perms/"+parentPerm+(aafcli.isDetailed()?"?ns":""), 
                        getDF(Perms.class) 
                        );
                return list(fp,HEADER,parentPerm);
            }
        });
    }

    @Override
    public void detailedHelp(int indent, StringBuilder sb) {
        detailLine(sb,indent,HEADER);
        api(sb,indent,HttpMethods.GET,"authz/perms/<parent type>",Perms.class,true);
    }

}
