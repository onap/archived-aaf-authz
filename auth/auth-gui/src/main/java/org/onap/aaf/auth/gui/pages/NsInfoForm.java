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

import static org.onap.aaf.misc.xgen.html.HTMLGen.A;
import static org.onap.aaf.misc.xgen.html.HTMLGen.TABLE;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;

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
import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.html.HTMLGen;

import aaf.v2_0.Nss;
import aaf.v2_0.Nss.Ns;

public class NsInfoForm extends Page {

    // Package on purpose
    static final String HREF = "/gui/onboard";
    static final String NAME = "Onboarding";
    static final String fields[] = {"ns","description","mots","owners","admins"};

    public NsInfoForm(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env,NAME,HREF, fields,
            new BreadCrumbs(breadcrumbs),
            new NamedCode(true,"content") {

            private final Slot sID = gui.env.slot(NsInfoForm.NAME+'.'+NsInfoForm.fields[0]);
            @Override
            public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                // p tags not closing right using .p() - causes issues in IE8 password form - so using leaf for the moment
                hgen.leaf(HTMLGen.H2).text("Namespace Info").end()
                     .leaf("p").text("Hover over Fields for Tool Tips, or click ")
                         .leaf(A,"href="+gui.env.getProperty(AAF_URL_GUI_ONBOARD,"")).text("Here").end()
                         .text(" for more information")
                     .end()
                    .incr("form","method=post");
                Mark table = new Mark(TABLE);
                hgen.incr(table);
                cache.dynamic(hgen, new DynamicCode<HTMLGen, AAF_GUI, AuthzTrans>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void code(final AAF_GUI gui, final AuthzTrans trans, final Cache<HTMLGen> cache, final HTMLGen hgen)    throws APIException, IOException {
                        final String incomingID= trans.get(sID, "");
                        final String[] info = new String[fields.length];
                        final Object own_adm[] = new Object[2]; 
                        for (int i=0;i<info.length;++i) {
                            info[i]="";
                        }
                        if (incomingID.length()>0) {
                            TimeTaken tt = trans.start("AAF Namespace Info",Env.REMOTE);
                            try {
                                gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
                                    @Override
                                    public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                                        Future<Nss> fn = client.read("/authz/nss/"+incomingID,gui.getDF(Nss.class));
                                        if (fn.get(AAF_GUI.TIMEOUT)) {
                                            for (Ns ns : fn.value.getNs()) {
                                                info[0]=ns.getName();
                                                info[1]=ns.getDescription();
                                                for (Ns.Attrib attr: ns.getAttrib()) {
                                                    switch(attr.getKey()) {
                                                        case "mots":
                                                            info[2]=attr.getValue();
                                                        default:
                                                    }
                                                }
                                                own_adm[0]=ns.getResponsible();
                                                own_adm[1]=ns.getAdmin();
                                            }
                                        } else {
                                            trans.error().log(fn.body());
                                        }
                                        return null;
                                    }
                                });
                            } catch (Exception e) {
                                trans.error().log("Unable to access AAF for NS Info",incomingID);
                                e.printStackTrace();
                            } finally {
                                tt.done();
                            }
                        }
                        hgen.input(fields[0],"Namespace",false,"value="+info[0],"title=AAF Namespace")
                            .input(fields[1],"Description*",true,"value="+info[1],"title=Full Application Name, Tool Name or Group")
                            .input(fields[2],"MOTS ID",false,"value="+info[2],"title=MOTS ID if this is an Application, and has MOTS");
                        Mark endTD = new Mark(),endTR=new Mark();
                        // Owners
                        hgen.incr(endTR,HTMLGen.TR)
                                .incr(endTD,HTMLGen.TD)
                                    .leaf("label","for="+fields[3]).text("Responsible Party")
                                .end(endTD)
                                .incr(endTD,HTMLGen.TD)
                                    .tagOnly("input","id="+fields[3],"title=Owner of App, must be an Non-Bargained Employee");
                                    if (own_adm[0]!=null) {
                                        for (String s : (List<String>)own_adm[0]) {
                                            hgen.incr("label",true).text(s).end();
                                        }
                                    }
                            hgen.end(endTR);

                            // Admins
                            hgen.incr(endTR,HTMLGen.TR)
                                .incr(endTD,HTMLGen.TD)
                                    .leaf("label","for="+fields[4]).text("Administrators")
                                .end(endTD)
                                .incr(endTD,HTMLGen.TD)
                                    .tagOnly("input","id="+fields[4],"title=Admins may be employees, contractors or mechIDs");
                                    if (own_adm[1]!=null) {
                                        for (String s : (List<String>)own_adm[1]) {
                                            hgen.incr(HTMLGen.P,true).text(s).end();
                                        }
                                    }
                                hgen.end(endTR)
                        .end();
                    }
                });
                hgen.end();
                hgen.tagOnly("input", "type=submit", "value=Submit")
                    .end();

            }
        });
    }

}
