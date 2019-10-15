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
import org.onap.aaf.auth.cmd.BaseCmd;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;

public class Log extends BaseCmd<Mgmt> {
    private static final String[] options = {"add","del"};

    public Log(Mgmt mgmt) {
        super(mgmt, "log",
                new Param(optionsToString(options),true),
                new Param("id[,id]*",true));
    }

    @Override
    public int _exec(int idxValue, String ... args) throws CadiException, APIException, LocatorException {
        int rv=409;
        int idx = idxValue;
        final int option = whichOption(options, args[idx++]);

        for (String name : args[idx++].split(COMMA)) {
            final String fname;
            if (name.indexOf("@")<0) {
                fname=name+'@'+ access.getProperty(Config.AAF_DEFAULT_REALM,null);
            } else {
                fname = name;
            }
        
            rv = all(new Retryable<Integer>() {
                @Override
                public Integer code(Rcli<?> client) throws APIException, CadiException {
                    int rv = 409;
                    Future<Void> fp;
                    String str = "/mgmt/log/id/"+fname;
                    String msg;
                    switch(option) {
                        case 0:
                            fp = client.create(str,Void.class);
                            msg = "Added";
                            break;
                        case 1:
                            fp = client.delete(str,Void.class);
                            msg = "Deleted";
                            break;
                        default:
                            fp = null;
                            msg = "Ignored";
                    }
                        
                    if (fp!=null) {
                        if (fp.get(AAFcli.timeout())) {
                            pw().println(msg + " Special Log for " + fname + " on " + client);
                            rv=200;
                        } else {
                            if (rv==409) {
                                rv = fp.code();
                            };
                            error(fp);
                        }
                        return rv;
                    }
                    return rv;
                }
            });
        }
        return rv;
    }

    @Override
    public void detailedHelp(int indentValue, StringBuilder sb) {
            int indent = indentValue;
        detailLine(sb,indent,"Clear the cache for certain tables");
        indent+=2;
        detailLine(sb,indent,"name        - name of table or 'all'");
        detailLine(sb,indent+14,"Must have admin rights to '" + Define.ROOT_NS() + '\'');
        indent-=2;
        api(sb,indent,HttpMethods.DELETE,"mgmt/cache/:name",Void.class,true);
    }
}
