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
import java.io.StringWriter;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.gui.Table;
import org.onap.aaf.auth.gui.Table.Cells;
import org.onap.aaf.auth.gui.table.AbsCell;
import org.onap.aaf.auth.gui.table.RefCell;
import org.onap.aaf.auth.gui.table.TableData;
import org.onap.aaf.auth.gui.table.TextCell;
import org.onap.aaf.auth.validation.Validator;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.xgen.html.HTMLGen;

import aaf.v2_0.Nss;
import aaf.v2_0.Nss.Ns;
import aaf.v2_0.Perm;
import aaf.v2_0.Perms;
import aaf.v2_0.Role;
import aaf.v2_0.Roles;

public class NsDetail extends Page {

    public static final String HREF = "/gui/nsdetail";
    public static final String NAME = "NsDetail";
    public static enum NS_FIELD { OWNERS, ADMINS, ROLES, PERMISSIONS, CREDS};
    private static final String BLANK = "";
    private static Slot keySlot;
    private static Model model;
    private static String locate_url;


    public NsDetail(final AAF_GUI gui, Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env, NAME, HREF, new String[] {"ns"},
                new BreadCrumbs(breadcrumbs),
                new Table<AAF_GUI,AuthzTrans>("Namespace Details",gui.env.newTransNoAvg(),model=new Model(),"class=detail")
                );
        model.set(this);
        keySlot = gui.env.slot(NAME+".ns");
        locate_url = gui.env.getProperty(Config.AAF_LOCATE_URL);
        if (locate_url==null) {
            locate_url="";
        } else {
            locate_url+="/aaf/"+Config.AAF_DEFAULT_API_VERSION;
        }
    }

    /**
     * Implement the table content for Namespace Detail
     *
     * @author Jeremiah
     *
     */
    private static class Model extends TableData<AAF_GUI,AuthzTrans> {
        private NsDetail nd;

        public void set(NsDetail nsDetail) {
            nd=nsDetail;
        }

        @Override
        public Cells get(final AuthzTrans trans, final AAF_GUI gui) {
            final String nsName = trans.get(keySlot, null);
            Validator v = new Validator();
            v.ns(nsName);
            if (v.err()) {
                trans.warn().printf("Error in NsDetail Request: %s", v.errs());
                return Cells.EMPTY;
            }

            if (nsName==null) {
                return Cells.EMPTY;
            }
            final ArrayList<AbsCell[]> rv = new ArrayList<>();
            rv.add(new AbsCell[]{new TextCell("Name:"),new TextCell(nsName)});

            final TimeTaken tt = trans.start("AAF Namespace Details",Env.REMOTE);
            try {
                gui.clientAsUser(trans.getUserPrincipal(),new Retryable<Void>() {
                    @Override
                    public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                        Future<Nss> fn = client.read("/authz/nss/"+nsName,gui.getDF(Nss.class));

                        if (fn.get(AAF_GUI.TIMEOUT)) {
                            tt.done();
                            try {
//                                TimeTaken tt = trans.start("Load Data", Env.SUB);

                                for (Ns n : fn.value.getNs()) {
                                    String desc = (n.getDescription()!=null?n.getDescription():BLANK);
                                    rv.add(new AbsCell[]{new TextCell("Description:"),new TextCell(desc)});

                                    addField(trans, nsName, rv, n.getAdmin(), NS_FIELD.ADMINS);
                                    addField(trans, nsName, rv, n.getResponsible(), NS_FIELD.OWNERS);

                                    StringWriter sw = new StringWriter();
                                    HTMLGen hgen = nd.clone(sw);
                                    hgen.leaf(HTMLGen.A, "class=greenbutton","href="+CredDetail.HREF+"?ns="+nsName).text("Cred Details").end();
                                    rv.add(new AbsCell[] {
                                            new TextCell("Credentials"),
                                            new TextCell(sw.toString())
                                        });


                                    Future<Roles> fr = client.read(
                                                    "/authz/roles/ns/"+nsName,
                                                    gui.getDF(Roles.class)
                                                    );
                                    List<String> roles = new ArrayList<>();
                                    if (fr.get(AAFcli.timeout())) {
                                        for (Role r : fr.value.getRole()) {
                                            roles.add(r.getName());
                                        }
                                    }
                                    addField(trans, nsName, rv, roles, NS_FIELD.ROLES);


                                    Future<Perms> fp = client.read(
                                                    "/authz/perms/ns/"+nsName,
                                                    gui.getDF(Perms.class)
                                                    );
                                    List<String> perms = new ArrayList<>();

                                    if (fp.get(AAFcli.timeout())) {
                                        for (Perm p : fp.value.getPerm()) {
                                            perms.add(p.getType() + "|" + p.getInstance() + "|" + p.getAction());
                                        }
                                    }
                                    addField(trans, nsName, rv, perms, NS_FIELD.PERMISSIONS);
                                }
                                String historyLink = NsHistory.HREF
                                        + "?name=" + nsName;
                                rv.add(new AbsCell[] {new RefCell("See History",historyLink,false,"class=greenbutton")});
                            } finally {
                                tt.done();
                            }
                        } else {
                            rv.add(new AbsCell[] {new TextCell("*** Data Unavailable ***")});
                        }
                        return null;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                tt.done();
            }
            return new Cells(rv,null);
        }

        private void addField(AuthzTrans trans, String ns, List<AbsCell[]> rv, List<String> values, NS_FIELD field) {
            if (!values.isEmpty()) {
                switch(field) {
                case OWNERS:
                case ADMINS:
                case CREDS:
                    for (int i=0; i< values.size(); i++) {
                        AbsCell label = (i==0?new TextCell(sentenceCase(field)+":"):AbsCell.Null);
                        String user = values.get(i);
                        AbsCell userCell = (new TextCell(user));
                        rv.add(new AbsCell[] {
                                label,
                                userCell
                        });
                    }
                    break;
                case ROLES:
                    for (int i=0; i< values.size(); i++) {
                        String role = values.get(i);
                        AbsCell label = (i==0?new TextCell(sentenceCase(field)+":"):AbsCell.Null);
                        rv.add(new AbsCell[] {
                                label,
                                new RefCell(role,RoleDetail.HREF+"?role="+role+"&ns="+ns,false)
                        });
                    }
                    break;
                case PERMISSIONS:
                    for (int i=0; i< values.size(); i++) {
                        AbsCell label = (i==0?new TextCell(sentenceCase(field)+":","style=width:20%"):AbsCell.Null);
                        String perm = values.get(i);
                        String[] fields = perm.split("\\|");
                        String grantLink = locate_url
                                + PermGrantForm.HREF
                                + "?type=" + fields[0].trim()
                                + "&amp;instance=" + fields[1].trim()
                                + "&amp;action=" + fields[2].trim();

                        rv.add(new AbsCell[] {
                                label,
                                new TextCell(perm,"style=width:60%;"),
                                new RefCell("Grant", grantLink,false,"class=button","style=width:20%;")
                        });
                    }
                    break;
                }

            }
        }

        private String sentenceCase(NS_FIELD field) {
            String sField = field.toString();
            return sField.substring(0, 1).toUpperCase() + sField.substring(1).toLowerCase();
        }

    }
}
