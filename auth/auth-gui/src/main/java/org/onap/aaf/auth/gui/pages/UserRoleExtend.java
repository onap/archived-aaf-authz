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

public class UserRoleExtend extends Page {
    public static final String HREF = "/gui/urExtend";
    static final String NAME = "Extend User Role";
    static final String fields[] = {"user","role"};

    public UserRoleExtend(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env,NAME, HREF, fields,
                new BreadCrumbs(breadcrumbs),
                new NamedCode(true, "content") {
            @Override
            public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                final Slot sUser = gui.env.slot(NAME+".user");
                final Slot sRole = gui.env.slot(NAME+".role");
            
            
                cache.dynamic(hgen, new DynamicCode<HTMLGen, AAF_GUI, AuthzTrans>() {
                    @Override
                    public void code(final AAF_GUI gui, final AuthzTrans trans,    final Cache<HTMLGen> cache, final HTMLGen hgen)    throws APIException, IOException {                    
                        final String user = trans.get(sUser, "");
                        final String role = trans.get(sRole, "");

                        TimeTaken tt = trans.start("Request to extend user role",Env.REMOTE);
                        try {
                            gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
                                @Override
                                public Void code(Rcli<?> client)throws CadiException, ConnectException, APIException {
                                    Future<Void> fv = client.update("/authz/userRole/extend/"+user+"/"+role+"?request=true");
                                    if (fv.get(5000)) {
                                        // not sure if we'll ever hit this
                                        hgen.p("Extended User ["+ user+"] in Role [" +role+"]");
                                    } else {
                                        if (fv.code() == 202 ) {
                                            hgen.p("User ["+ user+"] in Role [" +role+"] Extension sent for Approval");
                                        } else {
                                            gui.writeError(trans, fv, hgen,0);
                                        }
                                    }
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            trans.error().log(e);
                            e.printStackTrace();
                        } finally {
                            tt.done();
                        }
                    
                    
                    }
                });
            }
        
        });
    }
}

