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

/**
 * @author Jonathan
 *
 */
public class Create extends Cmd {
    private static final String COMMA = ",";

    public Create(NS parent) {
        super(parent,"create",
                new Param("ns-name",true),
                new Param("owner (id[,id]*)",true),
                new Param("admin (id[,id]*)",false));
    }

    @Override
    public int _exec(int _idx, final String ... args) throws CadiException, APIException, LocatorException {
            int idx = _idx;

        final NsRequest nr = new NsRequest();

        nr.setName(args[idx++]);
        String[] responsible = args[idx++].split(COMMA);
        for (String s : responsible) {
            nr.getResponsible().add(fullID(s));
        }
        String[] admin;
        if (args.length>idx) {
            admin = args[idx].split(COMMA);
        } else {
            admin = responsible;
        }
        for (String s : admin) {
            nr.getAdmin().add(fullID(s));
        }

        // Set Start/End commands
        setStartEnd(nr);

        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                // Requestable
                setQueryParamsOn(client);
                Future<NsRequest> fp = client.create(
                        "/authz/ns",
                        getDF(NsRequest.class),
                        nr
                        );
                if (fp.get(AAFcli.timeout())) {
                    pw().println("Created Namespace");
                } else {
                    if (fp.code()==202) {
                        pw().println("Namespace Creation Accepted, but requires Approvals before actualizing");
                    } else {
                        error(fp);
                    }
                }
                return fp.code();
            }
        });
    }

    @Override
    public void detailedHelp(int _indent, StringBuilder sb) {
            int indent = _indent;
        detailLine(sb,indent,"Create a Namespace");
        indent+=2;
        detailLine(sb,indent,"name        - Namespaces are dot-delimited, ex com.att.myapp");
        detailLine(sb,indent+14,"and must be created with parent credentials.");
        detailLine(sb,indent+14,"Ex: to create com.att.myapp, you must be admin for com.att");
        detailLine(sb,indent+14,"or com");
        detailLine(sb,indent,"owner       - This is the person(s) who is responsible for the ");
        detailLine(sb,indent+14,"app. These person or persons receive Notifications and");
        detailLine(sb,indent+14,"approves Requests regarding this Namespace. Companies have");
        detailLine(sb,indent+14,"Policies as to who may take on this responsibility");
        detailLine(sb,indent,"admin       - These are the people who are allowed to make changes on");
        detailLine(sb,indent+14,"the Namespace, including creating Roles, Permissions");
        detailLine(sb,indent+14,"and Credentials");
        sb.append('\n');
        detailLine(sb,indent,"Namespaces can be created even though there are Roles/Permissions which");
        detailLine(sb,indent,"start with the requested sub-namespace.  They are reassigned to the");
        detailLine(sb,indent,"Child Namespace");
        indent-=2;
        api(sb,indent,HttpMethods.POST,"authz/ns",NsRequest.class,true);
    }

}
