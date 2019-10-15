/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.gui.pages;

import java.io.IOException;
import java.net.ConnectException;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.NamedCode;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.html.HTMLGen;

import aaf.v2_0.Pkey;
import aaf.v2_0.RolePermRequest;

public class PermGrantAction extends Page {


    public PermGrantAction(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env,PermGrantForm.NAME, PermGrantForm.HREF, PermGrantForm.fields,
            new BreadCrumbs(breadcrumbs),
            new NamedCode(true,"content") {
                final Slot sType = gui.env.slot(PermGrantForm.NAME+'.'+PermGrantForm.fields[0]);
                final Slot sInstance = gui.env.slot(PermGrantForm.NAME+'.'+PermGrantForm.fields[1]);
                final Slot sAction = gui.env.slot(PermGrantForm.NAME+'.'+PermGrantForm.fields[2]);
                final Slot sRole = gui.env.slot(PermGrantForm.NAME+'.'+PermGrantForm.fields[3]);
            
                @Override
                public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                    cache.dynamic(hgen, new DynamicCode<HTMLGen,AAF_GUI, AuthzTrans>() {
                        @Override
                        public void code(final AAF_GUI gui, final AuthzTrans trans,final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {

                            String type = trans.get(sType,null);
                            String instance = trans.get(sInstance,null);
                            String action = trans.get(sAction,null);
                            String role = trans.get(sRole,null);
                        
                            String lastPage = PermGrantForm.HREF 
                                    + "?type=" + type + "&instance=" + instance + "&action=" + action;
                        
                            // Run Validations
                            boolean fail = true;
                    
                            TimeTaken tt = trans.start("AAF Grant Permission to Role",Env.REMOTE);
                            try {
                            
                                final RolePermRequest grantReq = new RolePermRequest();
                                Pkey pkey = new Pkey();
                                pkey.setType(type);
                                pkey.setInstance(instance);
                                pkey.setAction(action);
                                grantReq.setPerm(pkey);
                                grantReq.setRole(role);
                            
                                fail = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Boolean>() {
                                    @Override
                                    public Boolean code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                                        boolean fail = true;
                                        Future<RolePermRequest> fgrant = client.create(
                                                "/authz/role/perm",
                                                gui.getDF(RolePermRequest.class),
                                                grantReq
                                                );

                                        if (fgrant.get(5000)) {
                                            hgen.p("Permission has been granted to role.");
                                            fail = false;
                                        } else {
                                            if (202==fgrant.code()) {
                                                hgen.p("Permission Grant Request sent, but must be Approved before actualizing");
                                                fail = false;
                                            } else {
                                                gui.writeError(trans, fgrant, hgen, 0);
                                            }
                                        }
                                        return fail;
                                    }
                                });
                            } catch (Exception e) {
                                hgen.p("Unknown Error");
                                e.printStackTrace();
                            } finally {
                                tt.done();
                            }
                            
                            hgen.br();
                            hgen.incr("a",true,"href="+lastPage);
                            if (fail) {
                                hgen.text("Try again");
                            } else {
                                hgen.text("Grant this Permission to Another Role");
                            }
                            hgen.end();
                            hgen.js()
                                .text("alterLink('permgrant', '"+lastPage + "');")                        
                                .done();

                        }
                    });
                }
            });
    }
}
