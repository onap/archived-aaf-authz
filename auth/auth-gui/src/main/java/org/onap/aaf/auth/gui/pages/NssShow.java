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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.gui.Table;
import org.onap.aaf.auth.gui.Table.Cells;
import org.onap.aaf.auth.gui.table.AbsCell;
import org.onap.aaf.auth.gui.table.RefCell;
import org.onap.aaf.auth.gui.table.TableData;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.TimeTaken;

import aaf.v2_0.Nss;
import aaf.v2_0.Nss.Ns;

public class NssShow extends Page {
    public static final String HREF = "/gui/ns";

    public NssShow(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env, "MyNamespaces",HREF, NO_FIELDS,
                new BreadCrumbs(breadcrumbs), 
                new Table<AAF_GUI,AuthzTrans>("Namespaces I administer",gui.env.newTransNoAvg(),new Model(true,"Administrator",gui.env), 
                        "class=std", "style=display: inline-block; width: 45%; margin: 10px;"),
                new Table<AAF_GUI,AuthzTrans>("Namespaces I own",gui.env.newTransNoAvg(),new Model(false,"Owner",gui.env),
                        "class=std", "style=display: inline-block; width: 45%; margin: 10px;"));
    }

    private static class Model extends TableData<AAF_GUI,AuthzTrans> {
        private String[] headers;
        private String privilege = null;
        public final Slot sNssByUser;
        private boolean isAdmin;

        public Model(boolean admin, String privilege,AuthzEnv env) {
            super();
            headers = new String[] {privilege};
            this.privilege = privilege;
            isAdmin = admin;
            sNssByUser = env.slot("NSS_SHOW_MODEL_DATA");
        }

        @Override
        public String[] headers() {
            return headers;
        }
    
        @Override
        public Cells get(final AuthzTrans trans, final AAF_GUI gui) {
            ArrayList<AbsCell[]> rv = new ArrayList<>();
            List<Ns> nss = trans.get(sNssByUser, null);
            if (nss==null) {
                TimeTaken tt = trans.start("AAF Nss by User for " + privilege,Env.REMOTE);
                try {
                    nss = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<List<Ns>>() {
                        @Override
                        public List<Ns> code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                            List<Ns> nss = null;
                            Future<Nss> fp = client.read("/authz/nss/either/" + trans.user(),gui.getDF(Nss.class));
                            if (fp.get(AAF_GUI.TIMEOUT)) {
                                TimeTaken tt = trans.start("Load Data for " + privilege, Env.SUB);
                                try {
                                    if (fp.value!=null) {
                                        nss = fp.value.getNs();
                                        Collections.sort(nss, new Comparator<Ns>() {
                                            public int compare(Ns ns1, Ns ns2) {
                                                return ns1.getName().compareToIgnoreCase(ns2.getName());
                                            }
                                        });
                                        trans.put(sNssByUser,nss);
                                    } 
                                } finally {
                                    tt.done();
                                }
                            }else {
                                gui.writeError(trans, fp, null,0);
                            }
                            return nss;
                        }
                    });
                } catch (Exception e) {
                    trans.error().log(e);
                } finally {
                    tt.done();
                }
            }
        
            if (nss!=null) {
                for (Ns n : nss) {
                    if ((isAdmin && !n.getAdmin().isEmpty())
                      || (!isAdmin && !n.getResponsible().isEmpty())) {
                        AbsCell[] sa = new AbsCell[] {
                            new RefCell(n.getName(),NsDetail.HREF
                                    +"?ns="+n.getName(),false),
                        };
                        rv.add(sa);
                    }
                }
            }

            return new Cells(rv,null);
        }
    }


}
