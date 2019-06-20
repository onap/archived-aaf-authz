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
public class ListByUser extends Cmd {
    private static final String HEADER = "List Permissions by User";
    public ListByUser(List parent) {
        super(parent,"user", 
                new Param("id",true)); 
    }

    public int _exec( int idx, final String ... args) throws CadiException, APIException, LocatorException {
        final String user=fullID(args[idx]);
        
        return same(((List)parent).new ListPerms() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                StringBuilder sb = null;
                if ("true".equalsIgnoreCase(aafcli.forceString())) {
                    sb = new StringBuilder();
                    sb.append("?force");
                }
                if (aafcli.isDetailed()) {
                    if (sb==null) {
                        sb = new StringBuilder("?");
                    } else {
                        sb.append("&");
                    }
                    sb.append("ns");
                }
                Future<Perms> fp = client.read(
                        "/authz/perms/user/"+user+(sb==null?"":sb), 
                        getDF(Perms.class)
                        );
                return list(fp,HEADER, user);
            }
        });
    }
    
    @Override
    public void detailedHelp(int indent, StringBuilder sb) {
        detailLine(sb,indent,HEADER);
        api(sb,indent,HttpMethods.GET,"authz/perms/user/<user id>",Perms.class,true);
    }


}
