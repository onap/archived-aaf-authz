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

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.NamedCode;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Holder;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.util.Vars;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.util.IPValidator;
import org.onap.aaf.misc.env.util.Split;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.html.HTMLGen;

import aaf.v2_0.Error;
import certman.v1_0.Artifacts;
import certman.v1_0.Artifacts.Artifact;

public class CMArtiChangeAction extends Page {
    public CMArtiChangeAction(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env,CMArtiChangeForm.NAME,CMArtiChangeForm.HREF, CMArtiChangeForm.fields,
            new BreadCrumbs(breadcrumbs),
            new NamedCode(true,"content") {
                final Slot sID = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[0]);
                final Slot sMachine = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[1]);
                final Slot sNS = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[2]);
                final Slot sDirectory = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[3]);
                final Slot sCA = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[4]);
                final Slot sOSUser = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[5]);
                final Slot sRenewal = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[6]);
                final Slot sNotify = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[7]);
                final Slot sCmd = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[8]);
                final Slot sOther = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[9]);
                final Slot sType = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[10]);
                final Slot sSans = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[11]);

                @Override
                public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                    cache.dynamic(hgen, new DynamicCode<HTMLGen,AAF_GUI, AuthzTrans>() {
                        @Override
                        public void code(final AAF_GUI gui, final AuthzTrans trans,final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                            trans.info().log("Step 1");
                            final Artifact arti = new Artifact();
                            final String machine = trans.get(sMachine,null);
                            final String ca = trans.get(sCA, null);
                            final String sans = ((String)trans.get(sSans,null));
                            if (sans!=null) {
                                for (String s: Split.splitTrim(',', sans)) {
                                    arti.getSans().add(s);
                                }
                            }

                            // These checks to not apply to deletions
                            if(!CMArtiChangeForm.DELETE.equals(trans.get(sCmd, ""))) {
                                // Disallow IP entries, except by special Permission
                                if (!trans.fish(getPerm(ca,"ip"))) {
                                    boolean ok=true;
                                    if (IPValidator.ip(machine)) {
                                        ok=false;
                                    }
                                    if (ok) {
                                        for (String s: arti.getSans()) {
                                            if (IPValidator.ip(s)) {
                                                ok=false;
                                                break;
                                            }
                                        }
                                    }
                                    if (!ok) {
                                        hgen.p("Policy Failure: IPs in certificates are only allowed by Exception.");
                                        return;
                                    }
                                }

                            }

                            arti.setMechid((String)trans.get(sID,null));
                            arti.setMachine(machine);
                            arti.setNs((String)trans.get(sNS,null));
                            arti.setDir((String)trans.get(sDirectory,null));
                            arti.setCa(ca);
                            arti.setOsUser((String)trans.get(sOSUser, null));
                            arti.setRenewDays(Integer.parseInt((String)trans.get(sRenewal, null)));
                            arti.setNotification((String)trans.get(sNotify, null));
                            String[] checkbox = trans.get(sType,null);
                            for (int i=0;i<CMArtiChangeForm.types.length;++i) {
                                if ("on".equals(checkbox[i])) {
                                    arti.getType().add(CMArtiChangeForm.types[i]);
                                }
                            }

                            // Run Validations
                            if (arti.getMechid()==null || arti.getMechid().indexOf('@')<=0) {
                                hgen.p("Data Entry Failure: Please enter a valid ID, including domain.");
                            // VALIDATE OTHERS?
                            } else { // everything else is checked by Server

                                try {
                                    final Artifacts artifacts = new Artifacts();
                                    artifacts.getArtifact().add(arti);
                                    final Holder<Boolean> ok = new Holder<Boolean>(false);
                                    final Holder<Boolean> deleted = new Holder<Boolean>(false);
                                    Future<?> f = gui.cmClientAsUser(trans.getUserPrincipal(), new Retryable<Future<?>>() {
                                        @Override
                                        public Future<?> code(Rcli<?> client)throws CadiException, ConnectException, APIException {
                                            Future<?> rv = null;
                                            switch((String)trans.get(sCmd, "")) {
                                                case CMArtiChangeForm.CREATE:
                                                    Future<Artifacts> fc;
                                                    rv = fc = client.create("/cert/artifacts", gui.artifactsDF, artifacts);
                                                    if (fc.get(AAFcli.timeout())) {
                                                        hgen.p("Created Artifact " + arti.getMechid() + " on " + arti.getMachine());
                                                        ok.set(true);
                                                    }
                                                    break;
                                                case CMArtiChangeForm.UPDATE:
                                                    Future<Artifacts> fu = client.update("/cert/artifacts", gui.artifactsDF, artifacts);
                                                    rv=fu;
                                                    if(rv.get(AAFcli.timeout())) {
                                                    
                                                        hgen.p("Artifact " + arti.getMechid() + " on " + arti.getMachine() + " is updated");
                                                        ok.set(true);
                                                    }
                                                    break;
                                                case CMArtiChangeForm.COPY:
                                                    Future<Artifacts> future = client.read("/cert/artifacts/"+arti.getMechid()+'/'+arti.getMachine(), gui.artifactsDF);
                                                    rv = future;
                                                    if (future.get(AAFcli.timeout())) {
                                                        for (Artifact a : future.value.getArtifact()) { // only one, because these two are key
                                                            for (String newMachine :Split.split(',', trans.get(sOther, ""))) {
                                                                a.setMachine(newMachine);
                                                                Future<Artifacts> fup = client.update("/cert/artifacts", gui.artifactsDF, future.value);
                                                                if (fup.get(AAFcli.timeout())) {
                                                                    hgen.p("Copied to " + newMachine);
                                                                    ok.set(true);
                                                                }
                                                            }
                                                        }
                                                    }
                                                    break;
                                                case CMArtiChangeForm.DELETE:
                                                    Future<Void> fv;
                                                    rv = fv = client.delete("/cert/artifacts/"+arti.getMechid()+"/"+arti.getMachine(),"application/json");
                                                    if (fv.get(AAFcli.timeout())) {
                                                        hgen.p("Deleted " + arti.getMechid() + " on " + arti.getMachine());
                                                        ok.set(true);
                                                        deleted.set(true);
                                                    }
                                                    break;
                                            }
                                            return rv;
                                        }
                                    });
                                    if (!ok.get()) {
                                        if (f==null) {
                                            hgen.p("Unknown Command");
                                        } else {
                                            if (f.code() > 201) {
                                                Error err = gui.getDF(Error.class).newData().in(TYPE.JSON).load(f.body()).asObject();
                                                if(f.body().contains("%") ) {
                                                    hgen.p(Vars.convert(err.getText(),err.getVariables()));
                                                } else {
                                                    int colon = err.getText().indexOf(':');
                                                    if(colon>0) {
                                                        hgen.p(err.getMessageId() + ": " + err.getText().substring(0, colon));
                                                        Mark bq = new Mark();
                                                        hgen.incr(bq,"blockquote");
                                                        for(String em : Split.splitTrim('\n', err.getText().substring(colon+1))) {
                                                            hgen.p(em);
                                                        }
                                                        hgen.end(bq);
                                                    } else {
                                                        hgen.p(err.getMessageId() + ": " + err.getText());
                                                    }
                                                }
                                            } else {
                                                hgen.p(arti.getMechid() + " on " + arti.getMachine() + ": " + f.body());
                                            }
                                        }
                                    }
                                    hgen.br().leaf(HTMLGen.A,"class=greenbutton","href="+(deleted.get()?CMArtifactShow.HREF:CMArtiChangeForm.HREF)+
                                            "?id="+arti.getMechid()+
                                            "&amp;machine="+arti.getMachine() +
                                            "&amp;ns="+arti.getNs())
                                    .text("Back")
                                    .end();

                            } catch (Exception e) {
                                hgen.p("Unknown Error");
                                e.printStackTrace();
                            }

                        }
                        hgen.br();
                    }
                });
            }
        });
    }
}
