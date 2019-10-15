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

/**
 * p
 * @author Jonathan
 *
 */
public class Delete extends Cmd {
    public Delete(NS parent) {
        super(parent,"delete",
                new Param("ns-name",true));
    }

    @Override
    public int _exec(final int idx, final String ... args) throws CadiException, APIException, LocatorException {
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                int index = idx;
                StringBuilder path = new StringBuilder("/authz/ns/");
                path.append(args[index++]);

                // Send "Force" if set
                setQueryParamsOn(client);
                Future<Void> fp = client.delete(path.toString(),Void.class);

                if (fp.get(AAFcli.timeout())) {
                    pw().println("Deleted Namespace");
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
        detailLine(sb,indent,"Delete a Namespace");
        indent+=4;
        detailLine(sb,indent,"Namespaces cannot normally be deleted when there are still credentials,");
        detailLine(sb,indent,"permissions or roles associated with them. These can be deleted");
        detailLine(sb,indent,"automatically by setting \"force\" property.");
        detailLine(sb,indent,"i.e. set force=true or just starting with \"force\"");
        detailLine(sb,indent," (note force is unset after first use)");
        sb.append('\n');
        detailLine(sb,indent,"If \"set force=move\" is set, credentials are deleted, but ");
        detailLine(sb,indent,"Permissions and Roles are assigned to the Parent Namespace instead of");
        detailLine(sb,indent,"being deleted.  Similarly, Namespaces can be created even though there");
        detailLine(sb,indent,"are Roles/Perms whose type starts with the requested sub-namespace.");
        detailLine(sb,indent,"They are simply reassigned to the Child Namespace");
        indent-=4;
        api(sb,indent,HttpMethods.DELETE,"authz/ns/<ns>[?force=true]",Void.class,true);
    }

}
