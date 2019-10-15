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

import aaf.v2_0.NsRequest;

public class Describe extends Cmd {
    private static final String NS_PATH = "/authz/ns";
    public Describe(NS parent) {
        super(parent,"describe",
                new Param("ns-name",true),
                new Param("description",true));
    }

    @Override
    public int _exec(final int index, final String ... args) throws CadiException, APIException, LocatorException {
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                int idx = index;
                String name = args[idx++];
                StringBuilder desc = new StringBuilder();
                while (idx < args.length) {
                    desc.append(args[idx++] + ' ');
                }

                NsRequest nsr = new NsRequest();
                nsr.setName(name);
                nsr.setDescription(desc.toString());

                // Set Start/End commands
                setStartEnd(nsr);

                Future<NsRequest> fn = null;
                int rv;

                fn = client.update(
                    NS_PATH,
                    getDF(NsRequest.class),
                    nsr
                    );

                if (fn.get(AAFcli.timeout())) {
                    rv=fn.code();
                    pw().println("Description added to Namespace");
                } else {
                    if ((rv=fn.code())==202) {
                        pw().print("Adding description");
                        pw().println(" Accepted, but requires Approvals before actualizing");
                    } else {
                        error(fn);
                    }
                }
                return rv;
            }
        });
    }

    @Override
    public void detailedHelp(int indent, StringBuilder sb) {
        detailLine(sb,indent,"Add a description to a namespace");
        api(sb,indent,HttpMethods.PUT,"authz/ns",NsRequest.class,true);
    }
}