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

import aaf.v2_0.Pkey;
import aaf.v2_0.RolePermRequest;

/**
 * 
 * @author Jonathan
 *
 */
public class Grant extends Cmd {
    private final static String[] options = {"grant","ungrant","setTo"};

    public Grant(Perm parent) {
        super(parent,null,
            new Param(optionsToString(options),true),
            new Param("type",true),
            new Param("instance",true),
            new Param("action",true),
            new Param("role[,role]* (!REQ S)",false)
            ); 
    }

    @Override
    public int _exec(final int index, final String ... args) throws CadiException, APIException, LocatorException {
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                int idx = index;
                String action = args[idx++];
                int option = whichOption(options, action);
        
                RolePermRequest rpr = new RolePermRequest();
                Pkey pk = new Pkey();
                pk.setType(args[idx++]);
                pk.setInstance(args[idx++]);
                pk.setAction(args[idx++]);
                rpr.setPerm(pk);
                setStartEnd(rpr);
                
                Future<RolePermRequest> frpr = null;
        
                if (option != 2) {
                    String[] roles = args[idx++].split(",");
                    String strA,strB;
                    for(String role : roles) {
                        rpr.setRole(role);
                        if(option==0) {
                            // You can request to Grant Permission to a Role
                            setQueryParamsOn(client);
                            frpr = client.create(
                                    "/authz/role/perm", 
                                    getDF(RolePermRequest.class),
                                    rpr
                                    );
                            strA = "Granted Permission [";
                            strB = "] to Role [";
                        } else {
                            // You can request to UnGrant Permission to a Role
                            setQueryParamsOn(client);
                            frpr = client.delete(
                                    "/authz/role/" + role + "/perm", 
                                    getDF(RolePermRequest.class),
                                    rpr
                                    );
                            strA = "UnGranted Permission [";
                            strB = "] from Role [";
                        }
                        if(frpr.get(AAFcli.timeout())) {
                            pw().println(strA + pk.getType() + '|' + pk.getInstance() + '|' + pk.getAction() 
                                    + strB + role +']');
                        } else {
                            if (frpr.code()==202) {
                                pw().print("Permission Role ");
                                pw().print(option==0?"Granted":"Ungranted");
                                pw().println(" Accepted, but requires Approvals before actualizing");
                            } else {
                                error(frpr);
                                idx=Integer.MAX_VALUE;
                            }            
                        }
                    }
                } else {
                    String allRoles = "";
                    if (idx < args.length) 
                        allRoles = args[idx++];
                        
                    rpr.setRole(allRoles);
                    frpr = client.update(
                            "/authz/role/perm", 
                            getDF(RolePermRequest.class), 
                            rpr);
                    if(frpr.get(AAFcli.timeout())) {
                        pw().println("Set Permission's Roles to [" + allRoles + "]");
                    } else {
                        error(frpr);
                    }            
                } 
                return frpr==null?0:frpr.code();
            }
        });
    }

    @Override
    public void detailedHelp(int indent, StringBuilder sb) {
        detailLine(sb,indent,"Grant a Permission to a Role or Roles  OR");
        detailLine(sb,indent,"Ungrant a Permission from a Role or Roles  OR");
        detailLine(sb,indent,"Set a Permission's roles to roles supplied.");
        detailLine(sb,indent+4,"WARNING: Roles supplied with setTo will be the ONLY roles attached to this permission");
        detailLine(sb,indent+8,"If no roles are supplied, permission's roles are reset.");
        detailLine(sb,indent,"see Create for definitions of type,instance and action");
        api(sb,indent,HttpMethods.POST,"authz/role/perm",RolePermRequest.class,true);
        api(sb,indent,HttpMethods.DELETE,"authz/role/<role>/perm",RolePermRequest.class,false);
        api(sb,indent,HttpMethods.PUT,"authz/role/perm",RolePermRequest.class,false);

    }

}
