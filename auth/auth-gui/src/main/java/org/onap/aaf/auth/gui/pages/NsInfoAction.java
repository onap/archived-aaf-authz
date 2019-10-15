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
import java.text.ParseException;

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
import org.onap.aaf.misc.env.util.Chrono;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.html.HTMLGen;

import aaf.v2_0.CredRequest;

public class NsInfoAction extends Page {
    public NsInfoAction(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env,"Onboard",PassChangeForm.HREF, PassChangeForm.fields,
            new BreadCrumbs(breadcrumbs),
            new NamedCode(true,"content") {
                final Slot sID = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[0]);
                final Slot sCurrPass = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[1]);
                final Slot sPassword = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[2]);
                final Slot sPassword2 = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[3]);
                final Slot startDate = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[4]);
            
                @Override
                public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                    cache.dynamic(hgen, new DynamicCode<HTMLGen,AAF_GUI, AuthzTrans>() {
                        @Override
                        public void code(final AAF_GUI gui, final AuthzTrans trans,final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                            String id = trans.get(sID,null);
                            String currPass = trans.get(sCurrPass,null);
                            final String password = trans.get(sPassword,null);
                            String password2 = trans.get(sPassword2,null);
                        
                            // Run Validations
                            boolean fail = true;
                        
                            if (id==null || id.indexOf('@')<=0) {
                                hgen.p("Data Entry Failure: Please enter a valid ID, including domain.");
                            } else if (password == null || password2 == null || currPass == null) {
                                hgen.p("Data Entry Failure: Both Password Fields need entries.");
                            } else if (!password.equals(password2)) {
                                hgen.p("Data Entry Failure: Passwords do not match.");
                            } else { // everything else is checked by Server
                                final CredRequest cred = new CredRequest();
                                cred.setId(id);
                                cred.setPassword(currPass);
                                try {
                                    fail = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Boolean>() {
                                        @Override
                                        public Boolean code(Rcli<?> client)throws CadiException, ConnectException, APIException {
                                            TimeTaken tt = trans.start("Check Current Password",Env.REMOTE);
                                            try {
                                                Future<CredRequest> fcr = client.create( // Note: Need "Post", because of hiding password in SSL Data
                                                            "/authn/validate",
                                                            gui.getDF(CredRequest.class),
                                                            cred
                                                        );
                                                boolean go;
                                                boolean fail = true;
                                                fcr.get(5000);
                                                if (fcr.code() == 200) {
                                                    hgen.p("Current Password validated");
                                                    go = true;
                                                } else {
                                                    hgen.p(String.format("Invalid Current Password: %d %s",fcr.code(),fcr.body()));
                                                    go = false;
                                                }
                                                if (go) {
                                                    tt.done();
                                                    tt = trans.start("AAF Change Password",Env.REMOTE);
                                                    try {
                                                        // Change over Cred to reset mode
                                                        cred.setPassword(password);
                                                        String start = trans.get(startDate, null);
                                                        if (start!=null) {
                                                            try {
                                                                cred.setStart(Chrono.timeStamp(Chrono.dateOnlyFmt.parse(start)));
                                                            } catch (ParseException e) {
                                                                throw new CadiException(e);
                                                            }
                                                        }
                                                    
                                                        fcr = client.create(
                                                                "/authn/cred",
                                                                gui.getDF(CredRequest.class),
                                                                cred
                                                                );
                
                                                        if (fcr.get(5000)) {
                                                            // Do Remote Call
                                                            hgen.p("New Password has been added.");
                                                            fail = false;
                                                        } else {
                                                            gui.writeError(trans, fcr, hgen, 0);
                                                        }
                                                    } finally {
                                                        tt.done();
                                                    }
                                                }
                                                 return fail;
                                            } finally {
                                                tt.done();
                                            }
                                        }
                                    });

                                } catch (Exception e) {
                                    hgen.p("Unknown Error");
                                    e.printStackTrace();
                                }
                            }
                        hgen.br();
                        if (fail) {
                            hgen.incr("a",true,"href="+PassChangeForm.HREF+"?id="+id).text("Try again").end();
                        } else {
                            hgen.incr("a",true,"href="+Home.HREF).text("Home").end(); 
                        }
                    }
                });
            }
        });
    }
}
