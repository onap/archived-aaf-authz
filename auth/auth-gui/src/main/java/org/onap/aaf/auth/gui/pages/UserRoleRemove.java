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

public class UserRoleRemove extends Page {
    public static final String HREF = "/gui/urRemove";
    static final String NAME = "Remove User Role";
    static final String fields[] = {"user","role"};

    public UserRoleRemove(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
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

                        TimeTaken tt = trans.start("Request a user role delete",Env.REMOTE);
                        try {
                            gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
                                @Override
                                public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                                    Future<Void> fv = client.delete(
                                                "/authz/userRole/"+user+"/"+role+"?request=true",Void.class);
                                
                                    if (fv.get(5000)) {
                                        // not sure if we'll ever hit this
                                        hgen.p("User ["+ user+"] Removed from Role [" +role+"]");
                                    } else {
                                        if (fv.code() == 202 ) {
                                            hgen.p("User ["+ user+"] Removal from Role [" +role+"] sent for Approval");
                                        } else {
                                            gui.writeError(trans, fv, hgen, 0);
                                        }
                                    }
                                    return null;
                                }
                            });
                        } catch (Exception e) {
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
