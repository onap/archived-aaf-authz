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
import org.onap.aaf.auth.cmd.BaseCmd;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;

public class Owner extends BaseCmd<NS> {
    private static final String[] options = {"add","del"};

    public Owner(NS ns) {
        super(ns,"owner",
                new Param(optionsToString(options),true),
                new Param("ns-name",true),
                new Param("id[,id]*",true)
        );
    }

    @Override
    public int _exec(int idxParam, final String ... args) throws CadiException, APIException, LocatorException {
            int idx = idxParam;

        final int option = whichOption(options, args[idx++]);
        final String ns = args[idx++];
        final String ids[] = args[idx++].split(",");

        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                Future<Void> fp=null;
                for (String id : ids) {
                    id=fullID(id);
                    String verb;
                    switch(option) {
                        case 0:
                            fp = client.create("/authz/ns/"+ns+"/responsible/"+id,Void.class);
                            verb = " is now ";
                            break;
                        case 1:
                            fp = client.delete("/authz/ns/"+ns+"/responsible/"+id,Void.class);
                            verb = " is no longer ";
                            break;
                        default:
                            throw new CadiException("Bad Argument");
                    };

                    if (fp.get(AAFcli.timeout())) {
                        pw().append(id);
                        pw().append(verb);
                        pw().append("responsible for ");
                        pw().println(ns);
                    } else {
                        error(fp);
                        return fp.code();
                    }
                }
                return fp==null?500:fp.code();
            }
        });
    }

    @Override
    public void detailedHelp(int indentParam, StringBuilder sb) {
            int indent = indentParam;
        detailLine(sb,indent,"Add or Delete Responsible person to/from Namespace");
        indent+=2;
        detailLine(sb,indent,"Namespace Owners are responsible to receive Notifications and ");
        detailLine(sb,indent,"approve Requests regarding this Namespace. Companies have ");
        detailLine(sb,indent,"Policies as to who may take on this responsibility");

        indent+=2;
        detailLine(sb,indent,"name - Name of Namespace");
        detailLine(sb,indent,"id   - Credential of Person(s) to be made responsible");
        sb.append('\n');
        detailLine(sb,indent,"aafcli will call API on each ID presented.");
        indent-=4;
        api(sb,indent,HttpMethods.POST,"authz/ns/<ns>/responsible/<id>",Void.class,true);
        api(sb,indent,HttpMethods.DELETE,"authz/ns/<ns>/responsible/<id>",Void.class,false);
    }


}
