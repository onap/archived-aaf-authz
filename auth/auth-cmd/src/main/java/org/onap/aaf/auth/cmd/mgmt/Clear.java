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

package org.onap.aaf.auth.cmd.mgmt;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;

/**
 *
 * @author Jonathan
 *
 */
public class Clear extends Cmd {
    public Clear(Cache parent) {
        super(parent,"clear",
                new Param("name[,name]*",true));
    }

    @Override
    public int _exec(int idxParam, String ... args) throws CadiException, APIException, LocatorException {
        int idx = idxParam;
        int rv=409;
        for (final String name : args[idx++].split(COMMA)) {
            rv = all(new Retryable<Integer>() {
                @Override
                public Integer code(Rcli<?> client) throws APIException, CadiException {
                    int rv = 409;
                    Future<Void> fp = client.delete(
                            "/mgmt/cache/"+name,
                            Void.class
                            );
                    if (fp.get(AAFcli.timeout())) {
                        pw().println("Cleared Cache for " + name + " on " + client);
                        rv=200;
                    } else {
                        if (rv==409) {
                            rv = fp.code();
                        }
                        error(fp);
                    }
                    return rv;
                }
            });
        }
        return rv;
    }

    @Override
    public void detailedHelp(int indentParam, StringBuilder sb) {
            int indent = indentParam;
        detailLine(sb,indent,"Clear the cache for certain tables");
        indent+=2;
        detailLine(sb,indent,"name        - name of table or 'all'");
        detailLine(sb,indent+14,"Must have admin rights to '" + Define.ROOT_NS() + '\'');
        indent-=2;
        api(sb,indent,HttpMethods.DELETE,"mgmt/cache/:name",Void.class,true);
    }

}
