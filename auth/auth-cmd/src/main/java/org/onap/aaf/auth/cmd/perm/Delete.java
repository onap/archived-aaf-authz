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

import aaf.v2_0.PermRequest;

/**
 * p
 * @author Jonathan
 *
 */
public class Delete extends Cmd {
    public Delete(Perm parent) {
        super(parent,"delete",
                new Param("type",true),
                new Param("instance",true),
                new Param("action", true));
    }

    @Override
    public int _exec(final int index, final String ... args) throws CadiException, APIException, LocatorException {
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                int idx = index;
                // Object Style Delete
                PermRequest pk = new PermRequest();
                pk.setType(args[idx++]);
                pk.setInstance(args[idx++]);
                pk.setAction(args[idx++]);

                if(pk.getType().contains("@")) { // User Perm deletion... Must remove from hidden role
                    client.setQueryParams("force");
                } else {
                    // Set "Force" if set
                    setQueryParamsOn(client);
                }
                Future<PermRequest> fp = client.delete(
                        "/authz/perm",
                        getDF(PermRequest.class),
                        pk);
                if (fp.get(AAFcli.timeout())) {
                    pw().println("Deleted Permission");
                } else {
                    if (fp.code()==202) {
                        pw().println("Permission Deletion Accepted, but requires Approvals before actualizing");
                    } else {
                        error(fp);
                    }
                }
                return fp.code();
            }
        });
    }

    @Override
    public void detailedHelp(int indent, StringBuilder sb) {
        detailLine(sb,indent,"Delete a Permission with type,instance and action");
        detailLine(sb,indent+4,"see Create for definitions");
        api(sb,indent,HttpMethods.DELETE,"authz/perm",PermRequest.class,true);
    }

}
