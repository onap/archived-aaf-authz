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

import aaf.v2_0.Approval;
import aaf.v2_0.Approvals;

public class ApprovalAction extends Page {
    public ApprovalAction(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env,"Approvals",ApprovalForm.HREF, ApprovalForm.FIELDS,
            new BreadCrumbs(breadcrumbs),
            new NamedCode(true,"content") {
                final Slot sAppr = gui.env.slot(ApprovalForm.NAME+'.'+ApprovalForm.FIELDS[0]);
                final Slot sUser = gui.env.slot(ApprovalForm.NAME+'.'+ApprovalForm.FIELDS[1]);
            
                @Override
                public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {            
                    cache.dynamic(hgen, new DynamicCode<HTMLGen,AAF_GUI, AuthzTrans>() {
                        @Override
                        public void code(final AAF_GUI gui, final AuthzTrans trans,final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                            String[] appr = trans.get(sAppr,null);
                            String user = trans.get(sUser,null);
                            String lastPage = ApprovalForm.HREF;
                            if (user != null) {
                                lastPage += "?user="+user;
                            }
                        
                            if (appr==null) {
                                hgen.p("No Approvals have been selected.");
                            } else {
                                Approval app;
                                final Approvals apps = new Approvals();
                                int count = 0;
                                for (String a : appr) {
                                    if (a!=null) {
                                        int idx = a.indexOf('|');
                                        if (idx>=0) {
                                            app = new Approval();
                                            app.setStatus(a.substring(0,idx));
                                            app.setTicket(a.substring(++idx));
                                            app.setApprover(trans.getUserPrincipal().getName());
                                            apps.getApprovals().add(app);
                                            ++count;
                                        }
                                    }
                                }
                                if (apps.getApprovals().isEmpty()) {
                                    hgen.p("No Approvals have been sent.");
                                } else {
                                    TimeTaken tt = trans.start("AAF Update Approvals",Env.REMOTE);
                                    try {
                                        final int total = count;
                                        gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Boolean>() {
                                            @Override
                                            public Boolean code(Rcli<?> client) throws APIException, CadiException  {
                                                boolean fail2 = true;
                                                Future<Approvals> fa = client.update("/authz/approval",gui.getDF(Approvals.class),apps);
                                                if (fa.get(AAF_GUI.TIMEOUT)) {
                                                    // Do Remote Call
                                                    fail2 = false;
                                                    hgen.p(total + (total==1?" Approval has":" Approvals have") + " been Saved");
                                                } else {
                                                    gui.writeError(trans, fa, hgen, 0);
                                                }
                                                return fail2;
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        tt.done();
                                    }
                                }

                            hgen.br();
                            hgen.incr("a",true,"class=greenbutton","href="+lastPage).text("Back").end();
                        }
                    }
                });
            }
        });
    }
}
