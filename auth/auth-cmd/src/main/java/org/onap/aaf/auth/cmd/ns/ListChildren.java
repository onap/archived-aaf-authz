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

import aaf.v2_0.Nss;
import aaf.v2_0.Nss.Ns;

/**
 * p
 * @author Jonathan
 *
 */
public class ListChildren extends Cmd {
    private static final String HEADER="List Child Namespaces";

    public ListChildren(List parent) {
        super(parent,"children",
                new Param("ns-name",true));
    }

    @Override
    public int _exec(int idxValue, final String ... args) throws CadiException, APIException, LocatorException {
            int idx = idxValue;
        final String ns=args[idx++];
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                Future<Nss> fn = client.read("/authz/nss/children/"+ns,getDF(Nss.class));
                if (fn.get(AAFcli.timeout())) {
                    parent.reportHead(HEADER);
                    for (Ns ns : fn.value.getNs()) {
                        pw().format(List.kformat, ns.getName());
                    }
                } else if (fn.code()==404) {
                    ((List)parent).report(null,HEADER,ns);
                    return 200;
                } else {
                    error(fn);
                }
                return fn.code();
            }
        });
    }

    @Override
    public void detailedHelp(int indent, StringBuilder sb) {
        detailLine(sb,indent,HEADER);
        api(sb,indent,HttpMethods.GET,"authz/nss/children/<ns>",Nss.class,true);
    }

}
