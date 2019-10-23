/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 IBM.
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
import java.text.ParseException;
import java.util.GregorianCalendar;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.user.Cred;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.NamedCode;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.OrganizationFactory;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
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
import aaf.v2_0.Users;

public class PassChangeAction extends Page {

    public PassChangeAction(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env,PassChangeForm.NAME,PassChangeForm.HREF, PassChangeForm.fields,
            new BreadCrumbs(breadcrumbs),
            new NamedCode(true,"content") {
                final Slot sID = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[0]);
                final Slot sCurrPass = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[1]);
                final Slot sPassword = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[2]);
                final Slot sPassword2 = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[3]);
                final Slot startDate = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[4]);
                final Slot sNS = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[5]);

                @Override
                public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                    cache.dynamic(hgen, new DynamicCode<HTMLGen,AAF_GUI, AuthzTrans>() {
                    	private static final String CLASS = "greenbutton";
                        @Override
                        public void code(final AAF_GUI gui, final AuthzTrans trans,final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                            final String id = trans.get(sID,null);
                            final String currPass = trans.get(sCurrPass,null);
                            final String password = trans.get(sPassword,null);
                            final String password2 = trans.get(sPassword2,null);
                            final String ns = trans.get(sNS, null);

                            // Run Validations
                            boolean fail = true;

                            if (id==null || id.indexOf('@')<=0) {
                                hgen.p("Data Entry Failure: Please enter a valid ID, including domain.");
                            } else if (password == null || password2 == null) {
                                hgen.p("Data Entry Failure: Both Password Fields need entries.");
                            } else if (!password.equals(password2)) {
                                hgen.p("Data Entry Failure: Passwords do not match.");
                            } else { // everything else is checked by Server
                                final CredRequest cred = new CredRequest();
                                cred.setId(id);
                                cred.setPassword("".equals(currPass)?null:currPass);
                                try {
                                    fail = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Boolean>() {
                                        @Override
                                        public Boolean code(Rcli<?> client)throws CadiException, ConnectException, APIException {
                                            boolean fail = true;
                                            boolean go = false;
                                            try {
                                                Organization org = OrganizationFactory.obtain(trans.env(), id);
                                                if (org!=null) {
                                                    go = PassChangeForm.skipCurrent(trans, org.getIdentity(trans, id));
                                                }
                                            } catch (OrganizationException e) {
                                                trans.error().log(e);
                                            }

                                            if (cred.getPassword()==null) {
                                                try {
                                                    if (!go) {
                                                        go=gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Boolean>() {
                                                            @Override
                                                            public Boolean code(Rcli<?> client)    throws CadiException, ConnectException, APIException {
                                                                Future<Users> fc = client.read("/authn/creds/id/"+id,gui.getDF(Users.class));
                                                                if (fc.get(AAFcli.timeout())) {
                                                                    GregorianCalendar now = new GregorianCalendar();
                                                                    for (aaf.v2_0.Users.User u : fc.value.getUser()) {
                                                                        if (u.getType()<10 && u.getExpires().toGregorianCalendar().after(now)) {
                                                                            return false; // an existing, non expired, password type exists
                                                                        }
                                                                    }
                                                                    return true; // no existing, no expired password
                                                                } else {
                                                                    if (fc.code()==404) { // not found...
                                                                        return true;
                                                                    } else {
                                                                        trans.error().log(gui.aafCon.readableErrMsg(fc));
                                                                    }
                                                                }
                                                                return false;
                                                            }
                                                        });
                                                    }
                                                    if (!go) {
                                                        hgen.p("Current Password required").br();
                                                    }
                                                } catch (LocatorException e) {
                                                    trans.error().log(e);
                                                }

                                            } else {
                                                TimeTaken tt = trans.start("Check Current Password",Env.REMOTE);
                                                try {
                                                    // Note: Need "Post", because of hiding password in SSL Data
                                                    Future<CredRequest> fcr = client.create("/authn/validate",gui.getDF(CredRequest.class),cred);
                                                    fcr.get(5000);
                                                    if (fcr.code() == 200) {
                                                        hgen.p("Current Password validated").br();
                                                        go = true;
                                                    } else {
                                                        hgen.p(Cred.ATTEMPT_FAILED_SPECIFICS_WITHELD).br();
                                                        trans.info().log("Failed Validation",fcr.code(),fcr.body());
                                                        go = false;
                                                    }
                                                } finally {
                                                    tt.done();
                                                }
                                            }
                                            if (go) {
                                                TimeTaken tt = trans.start("AAF Change Password",Env.REMOTE);
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

                                                    Future<CredRequest> fcr = gui.clientAsUser(trans.getUserPrincipal()).create("/authn/cred",gui.getDF(CredRequest.class),cred);
                                                    if (fcr.get(AAFcli.timeout())) {
                                                        // Do Remote Call
                                                        hgen.p("New Password has been added.  The previous one is still valid until Expiration.");
                                                        fail = false;
                                                    } else {
                                                        hgen.p(Cred.ATTEMPT_FAILED_SPECIFICS_WITHELD).br();
                                                        trans.info().log("Failed Validation",fcr.code(),fcr.body());
                                                    }
                                                } finally {
                                                    tt.done();
                                                }
                                            }
                                            return fail;
                                        }

                                    });
                            } catch (Exception e) {
                                hgen.p("Unknown Error");
                                e.printStackTrace();
                            }

                        }
                        hgen.br();
                        if (fail) {
                            hgen.incr(HTMLGen.A,true,CLASS,"href="+PassChangeForm.HREF+"?id="+id).text("Try again").end();
                        } else {
                            if (ns==null) {
                                hgen.incr(HTMLGen.A,true,CLASS,"href="+Home.HREF).text("Back").end();
                            } else {
                                hgen.incr(HTMLGen.A,true,CLASS,"href="+CredDetail.HREF+"?id="+id+"&ns="+ns).text("Back").end();
                            }
                        }
                    }
                });
            }
        });
    }
}
