/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.aaf.auth.cmd.role;

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

import aaf.v2_0.RoleRequest;

/**
 *
 * @author Jonathan
 *
 */
public class CreateDelete extends Cmd {
    private static final String ROLE_PATH = "/authz/role";
    private static final String[] options = {"create","delete"};
    public CreateDelete(Role parent) {
        super(parent,null, 
                new Param(optionsToString(options),true),
                new Param("name",true)); 
    }

    @Override
    public int _exec(final int index, final String ... args) throws CadiException, APIException, LocatorException {
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                int idx = index;
                String action = args[idx++];
                int option = whichOption(options, action);
    
                RoleRequest rr = new RoleRequest();
                rr.setName(args[idx++]);
    
                // Set Start/End commands
                setStartEnd(rr);
            
                Future<RoleRequest> fp = null;
                String verb = null;
                int rv;
                switch(option) {
                    case 0:
                        fp = client.create(
                            ROLE_PATH,
                            getDF(RoleRequest.class),
                            rr
                            );
                        verb = "Create";
                        break;
                    case 1:
                        // Send "Force" if set
                        setQueryParamsOn(client);
                        fp = client.delete(
                                ROLE_PATH, // +args[idx++], 
                                getDF(RoleRequest.class),
                                rr
                                );
                        verb = "Delete";
                        break;
                    default: // note, if not an option, whichOption throws Exception
                        break;
                    
                }
                boolean rolesSupplied = (args.length>idx);
                if (fp == null) {// This useless code brought to you by Sonar.
                    throw new CadiException("No call made.");  
                }
                if (fp.get(AAFcli.timeout())) {
                    rv=fp.code();
                    pw().print(verb);
                    pw().println("d Role");
                    if (rolesSupplied) {
                        for (;args.length>idx;++idx ) {
                            try {
                                if (201!=(rv=((Role)parent)._exec(0,new String[] {"user","add",rr.getName(),args[idx]}))) {
                                    rv = 206 /*HttpStatus.PARTIAL_CONTENT_206*/;
                                }
                            } catch (LocatorException e) {
                                throw new CadiException(e);
                            }
                        }
                    }
                } else {
                    if ((rv=fp.code())==202) {
                        pw().print("Role ");
                        pw().print(verb);
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
        detailLine(sb,indent,"Create OR Delete a Role");
        detailLine(sb,indent+2,"name - Name of Role to create");
        api(sb,indent,HttpMethods.POST,"authz/role",RoleRequest.class,true);
        api(sb,indent,HttpMethods.DELETE,"authz/role",RoleRequest.class,false);
    }

}
