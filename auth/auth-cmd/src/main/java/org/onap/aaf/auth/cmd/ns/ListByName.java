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
import aaf.v2_0.Perms;
import aaf.v2_0.Roles;
import aaf.v2_0.Users;

/**
 * p
 * @author Jonathan
 *
 */
public class ListByName extends Cmd {
    private static final String HEADER="List Namespaces by Name";

    public ListByName(List parent) {
        super(parent,"name", 
                new Param("ns-name",true));
    }

    @Override
    public int _exec(int idxValue, final String ... args) throws CadiException, APIException, LocatorException {
            int idx = idxValue;
        final String ns=args[idx++];
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                Future<Nss> fn = client.read("/authz/nss/"+ns,getDF(Nss.class));
                if (fn.get(AAFcli.timeout())) {
                    ((List)parent).report(fn,HEADER,ns);
                    if (fn.value!=null) {
                        for (Ns n : fn.value.getNs()) {
                            Future<Roles> fr = client.read("/authz/roles/ns/"+n.getName(), getDF(Roles.class));
                            if (fr.get(AAFcli.timeout())) {
                                ((List)parent).reportRole(fr);
                            }
                        }
                        for (Ns n : fn.value.getNs()) {
                            Future<Perms> fp = client.read("/authz/perms/ns/"+n.getName()+(aafcli.isDetailed()?"?ns":""), getDF(Perms.class));
                            if (fp.get(AAFcli.timeout())) {
                                ((List)parent).reportPerm(fp);
                            }
                        }
                        for (Ns n : fn.value.getNs()) {
                            Future<Users> fu = client.read("/authn/creds/ns/"+n.getName()+(aafcli.isDetailed()?"?ns":""), getDF(Users.class));
                            if (fu.get(AAFcli.timeout())) {
                                ((List)parent).reportCred(fu);
                            }
                        }
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
        api(sb,indent,HttpMethods.GET,"authz/nss/<ns>",Nss.class,true);
        detailLine(sb,indent,"Indirectly uses:");
        api(sb,indent,HttpMethods.GET,"authz/roles/ns/<ns>",Roles.class,false);
        api(sb,indent,HttpMethods.GET,"authz/perms/ns/<ns>",Perms.class,false);
        api(sb,indent,HttpMethods.GET,"authn/creds/ns/<ns>",Users.class,false);
    }

}
