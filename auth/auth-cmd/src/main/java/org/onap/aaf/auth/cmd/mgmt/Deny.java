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
import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;

public class Deny extends BaseCmd<Mgmt> {
    private static final String[] options = {"add","del"};

    public Deny(Mgmt mgmt) {
        super(mgmt, "deny");
        cmds.add(new DenySomething(this,"ip","ipv4or6[,ipv4or6]*"));
        cmds.add(new DenySomething(this,"id","identity[,identity]*"));
    }

    public class DenySomething extends Cmd {

        private boolean isID;

        public DenySomething(Deny deny, String type, String repeatable) {
            super(deny, type,
                new Param(optionsToString(options),true),
                new Param(repeatable,true));
            isID = "id".equals(type);
        }

        @Override
        protected int _exec(int idxValue, String... args) throws CadiException, APIException, LocatorException {
                int idx = idxValue;
            String action = args[idx++];
            final int option = whichOption(options, action);
            int rv=409;
            for (final String name : args[idx++].split(COMMA)) {
                final String append;
                if (isID && name.indexOf("@")<0) {
                    append='@'+ access.getProperty(Config.AAF_DEFAULT_REALM,null);
                } else {
                    append = "";
                }
                final String path = "/mgmt/deny/"+getName() + '/'+ name + append;
                rv = all(new Retryable<Integer>() {
                    @Override
                    public Integer code(Rcli<?> client) throws APIException, CadiException  {
                        int rv = 409;
                        Future<Void> fp;
                        String resp;
                        if(option == 0) {
                            fp = client.create(path, Void.class);
                            resp = " added";
                        } else {
                            fp = client.delete(path, Void.class);
                            resp = " deleted";
                        }
                        if (fp.get(AAFcli.timeout())) {
                            pw().println(name + append + resp + " on " + client);
                            rv=fp.code();
                        } else {
                            if (rv==409) { 
                                rv = fp.code();
                            };
                            error(fp);
                        }
                        return rv;
                    }
                });
            }
            return rv;
        }

    }

}
