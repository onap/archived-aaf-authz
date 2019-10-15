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

public class Describe extends Cmd {
    private static final String PERM_PATH = "/authz/perm";
    public Describe(Perm parent) {
        super(parent,"describe", 
                new Param("type",true),
                new Param("instance", true),
                new Param("action", true),
                new Param("description",true)); 
    }

    @Override
    public int _exec(final int index, final String ... args) throws CadiException, APIException, LocatorException {
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                int idx = index;
                String type = args[idx++];
                String instance = args[idx++];
                String action = args[idx++];
                StringBuilder desc = new StringBuilder();
                while (idx < args.length) {
                    desc.append(args[idx++] + ' ');
                }
    
                PermRequest pr = new PermRequest();
                pr.setType(type);
                pr.setInstance(instance);
                pr.setAction(action);
                pr.setDescription(desc.toString());
    
                // Set Start/End commands
                setStartEnd(pr);
            
                Future<PermRequest> fp = null;
                int rv;

                fp = client.update(
                    PERM_PATH,
                    getDF(PermRequest.class),
                    pr
                    );

                if (fp.get(AAFcli.timeout())) {
                    rv=fp.code();
                    pw().println("Description added to Permission");
                } else {
                    if ((rv=fp.code())==202) {
                        pw().print("Adding description");
                        pw().println(" Accepted, but requires Approvals before actualizing");
                    } else {
                        error(fp);
                    }
                }
                return rv;
            }
        });
    }

    @Override
    public void detailedHelp(int indent, StringBuilder sb) {
        detailLine(sb,indent,"Add a description to a permission");
        api(sb,indent,HttpMethods.PUT,"authz/perm",PermRequest.class,true);
    }
}
