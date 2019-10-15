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

package org.onap.aaf.auth.cmd.user;

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

import aaf.v2_0.CredRequest;

public class ID extends Cmd {
    private static final String CRED_PATH = "/authn/cred";
    private static final String[] options = {"add","del"};
    public ID(User parent) {
        super(parent,"fqi",
                new Param(optionsToString(options),true),
                new Param("id",true)
        );
    }

    @Override
    public int _exec(int _idx, final String ... args) throws CadiException, APIException, LocatorException { 
        int idx = _idx;
        String key = args[idx++];
        final int option = whichOption(options,key);

        final CredRequest cr = new CredRequest();
        cr.setId(args[idx++]);
        cr.setType(10);
        if (args.length>idx)
            cr.setEntry(args[idx]);
    
        // Set Start/End commands
        setStartEnd(cr);
        Integer ret = same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                Future<CredRequest> fp=null;
                String verb =null;
                switch(option) {
                    case 0:
                        fp = client.create(
                            CRED_PATH, 
                            getDF(CredRequest.class), 
                            cr
                            );
                        verb = "Added ID [";
                        break;
                    case 1:
                        setQueryParamsOn(client);
                        fp = client.delete(CRED_PATH,
                            getDF(CredRequest.class),
                            cr
                            );
                        verb = "Deleted ID [";
                        break;
                    default:
                        break;
                }
                if (fp==null) {
                    return null; // get by Sonar check.
                }
                if (fp.get(AAFcli.timeout())) {
                    pw().print(verb);
                    pw().print(cr.getId());
                    pw().println(']');
                } else if (fp.code()==202) {
                    pw().println("ID Action Accepted, but requires Approvals before actualizing");
                } else if (fp.code()==409 && option==0) {
                    pw().println("FQI already exists");
                } else if (fp.code()==406 && option==1) {
                    pw().println("FQI does not exist");
                } else {
                    pw().println(Cred.ATTEMPT_FAILED_SPECIFICS_WITHELD);
                }
                return fp.code();
            }
        });
        if (ret==null)ret = -1;
        return ret;
    }

    @Override
    public void detailedHelp(int _indent, StringBuilder sb) {
            int indent = _indent;
        detailLine(sb,indent,"Add or Delete Fully Qualified Identity: An ID attached to the Namespace");
        indent+=2;
        detailLine(sb,indent,"fqi      - the ID to create/delete within AAF");
        sb.append('\n');
        detailLine(sb,indent,"This usage has NO Credential, and serves only to allow IDs to be attached");
        detailLine(sb,indent,"to Roles before credentials such as Certificates are established.");
        detailLine(sb,indent,"The Domain can be related to any Namespace you have access to *");
        detailLine(sb,indent,"The Domain is in reverse order of Namespace, i.e. ");
        detailLine(sb,indent+2,"NS of com.att.myapp can create user of XY1234@myapp.att.com");
        indent-=2;
        api(sb,indent,HttpMethods.POST,"authn/cred",CredRequest.class,true);
        api(sb,indent,HttpMethods.DELETE,"authn/cred",CredRequest.class,false);
        api(sb,indent,HttpMethods.PUT,"authn/cred",CredRequest.class,false);
    }
}
