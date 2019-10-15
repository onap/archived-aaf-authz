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

import static org.onap.aaf.misc.xgen.html.HTMLGen.TABLE;

import java.io.IOException;
import java.net.ConnectException;
import java.util.GregorianCalendar;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.NamedCode;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.OrganizationFactory;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.html.HTMLGen;

import aaf.v2_0.Users;

public class PassChangeForm extends Page {
    // Package on purpose
    static final String HREF = "/gui/passwd";
    static final String NAME = "PassChange";
    static final String fields[] = {"id","current","password","password2","startDate","ns"};

    public PassChangeForm(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env,NAME,HREF, fields,
            new BreadCrumbs(breadcrumbs),
            new NamedCode(true,NAME) {
                private final Slot sID = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[0]);
                @Override
                public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                
                    // p tags not closing right using .p() - causes issues in IE8 password form - so using leaf for the moment
                    hgen.incr(HTMLGen.H4,true,"style=margin: 0em 0em .4em 0em")
                        .text("You are <i>adding</i> a New Password in the AAF System.")
                        .end();

                    Mark form = new Mark();
                    hgen.incr(form,"form","method=post");
                
                    Mark table = new Mark(TABLE);
                    hgen.incr(table);

                    cache.dynamic(hgen, new DynamicCode<HTMLGen, AAF_GUI, AuthzTrans>() {
                        @Override
                        public void code(final AAF_GUI gui, final AuthzTrans trans,    final Cache<HTMLGen> cache, final HTMLGen hgen)    throws APIException, IOException {
                            String incomingID= trans.get(sID, "");
                            boolean skipCurrent = false;
                            if (incomingID.length()>0) {
                                try {
                                    Organization org = OrganizationFactory.obtain(trans.env(), incomingID);
                                    if (org==null) {
                                        hgen.incr(HTMLGen.H4,"style=color:red;").text("Error: There is no supported company for ").text(incomingID).end();
                                    } else {
                                        Identity user = org.getIdentity(trans, incomingID);
                                        if (user==null) {
                                            int at = incomingID.indexOf('@');
                                            hgen.incr(HTMLGen.H4,"style=color:red;").text("Error: You are not the sponsor of '").text(at<0?incomingID:incomingID.substring(0,at))
                                                .text("' defined at ").text(org.getName()).end();
                                            incomingID = "";
                                        } else {
                                            // Owners/or the IDs themselves are allowed to reset password without previous one
                                            skipCurrent=skipCurrent(trans, user);
                                        
                                            if (!skipCurrent) {
                                                final String id = incomingID;
                                                try {
                                                    skipCurrent=gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Boolean>() {
                                                        @Override
                                                        public Boolean code(Rcli<?> client)    throws CadiException, ConnectException, APIException {
                                                            Future<Users> fc = client.read("/authn/creds/id/"+id,gui.getDF(Users.class));
                                                            if (fc.get(AAFcli.timeout())) {
                                                                GregorianCalendar now = new GregorianCalendar();
                                                                for (aaf.v2_0.Users.User u : fc.value.getUser()) {
                                                                    if (u.getType()<10 && u.getType()>=1 && u.getExpires().toGregorianCalendar().after(now)) {
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
                                                } catch (LocatorException | CadiException e) {
                                                    trans.error().log(e);
                                                }
                                            }
                                        }
                                    }                                
                                } catch (OrganizationException e) {
                                    hgen.incr(HTMLGen.H4,"style=color:red;").text("Error: ")
                                        .text(e.getMessage()).end();
                                }
                            }
                        
                            hgen.input(fields[0],"ID*",true,"value="+incomingID,(incomingID.length()==0?"":"readonly"));
                            if (!skipCurrent) {
                                hgen.input(fields[1],"Current Password*",true,"type=password");
                            }
                            if (skipCurrent) {
                                hgen.input(fields[1],"",false,"type=hidden", "value=").end();
                            }

                            hgen.input(fields[2],"New Password*",true, "type=password")
                                .input(fields[3], "Reenter New Password*",true, "type=password")
            //                        .input(fields[3],"Start Date",false,"type=date", "value="+
            //                                Chrono.dateOnlyFmt.format(new Date(System.currentTimeMillis()))
            //                                )
                                .end(table);

                        }

                    });
                    hgen.tagOnly("input", "type=submit", "value=Submit")
                        .end(form)
                        .br()
                        .p("All AAF Passwords continue to be valid until their listed expiration dates.  ",
                           "This allows you to migrate services to this new password until the old ones expire.").br().br()
                        .p("Note: You must be an Admin of the Namespace where the MechID is defined.").br()
                        ;
                
                    Mark div = hgen.divID("passwordRules");
                    cache.dynamic(hgen, new DynamicCode<HTMLGen, AAF_GUI, AuthzTrans>() {
                        @Override
                        public void code(final AAF_GUI gui, final AuthzTrans trans,    final Cache<HTMLGen> cache, final HTMLGen hgen)    throws APIException, IOException {
                            try {
                                Organization org = OrganizationFactory.obtain(trans.env(),trans.getUserPrincipal().getName());
                                if (org!=null) {
                                    hgen.incr(HTMLGen.H4).text("Password Rules for ").text(org.getName()).end()
                                        .incr(HTMLGen.UL);
                                    for (String line : org.getPasswordRules()) {
                                        hgen.leaf(HTMLGen.LI).text(line).end();
                                    }
                                    hgen.end();
                                }
                            } catch (OrganizationException e) {
                                hgen.p("No Password Rules can be found for company of ID ",trans.getUserPrincipal().getName()).br();
                            }
                        }
                    });
                    hgen.end(div);
                }
            }
        );
    }

    // Package on Purpose
    static boolean skipCurrent(AuthzTrans trans, Identity user) throws OrganizationException {
        if (user!=null) {
            // Should this be an abstractable Policy?
            String tuser = trans.user();
            if (user.fullID().equals(trans.user())) {
                return true;
            } else {
                Identity manager = user.responsibleTo();
                if (tuser.equals(user.fullID()) || manager.isFound()) {
                    return true;
                }
            }
        }
        return false;
    }

}
