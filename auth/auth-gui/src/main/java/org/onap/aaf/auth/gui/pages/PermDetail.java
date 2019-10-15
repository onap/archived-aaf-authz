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
import java.util.List;

import org.eclipse.jetty.http.HttpStatus;
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
import org.onap.aaf.auth.gui.table.TextCell;
import org.onap.aaf.auth.validation.Validator;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.TimeTaken;

import aaf.v2_0.Perm;
import aaf.v2_0.Perms;

/**
 * Detail Page for Permissions
 * <p>
 * @author Jonathan
 *
 */
public class PermDetail extends Page {
    public static final String HREF = "/gui/permdetail";
    public static final String NAME = "PermDetail";
    private static final String BLANK = "";

    public PermDetail(final AAF_GUI gui, Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env, NAME, HREF, new String[] {"type","instance","action"},
                new BreadCrumbs(breadcrumbs),
                new Table<AAF_GUI,AuthzTrans>("Permission Details",gui.env.newTransNoAvg(),new Model(gui.env),"class=detail")
                );
    }

    /**
     * Implement the table content for Permissions Detail
     * <p>
     * @author Jonathan
     *
     */
    private static class Model extends TableData<AAF_GUI,AuthzTrans> {
        private Slot type, instance, action;
        public Model(AuthzEnv env) {
            type = env.slot(NAME+".type");
            instance = env.slot(NAME+".instance");
            action = env.slot(NAME+".action");
        }

        public Cells get(final AuthzTrans trans, final AAF_GUI gui) {
            final String pType = trans.get(type, null);
            final String pInstance = trans.get(instance, null);
            final String pAction = trans.get(action, null);
            Validator v = new Validator();
            v.permTypeWithUser(trans.user(),pType)
             .permInstance(pInstance)
             .permAction(pAction);
        
            if (v.err()) {
                trans.warn().printf("Error in PermDetail Request: %s", v.errs());
                return Cells.EMPTY;
            }
            final ArrayList<AbsCell[]> rv = new ArrayList<>();
            rv.add(new AbsCell[]{new TextCell("Type:"),new TextCell(pType)});
            rv.add(new AbsCell[]{new TextCell("Instance:"),new TextCell(pInstance)});
            rv.add(new AbsCell[]{new TextCell("Action:"),new TextCell(pAction)});
            try {
                gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
                    @Override
                    public Void code(Rcli<?> client)throws CadiException, ConnectException, APIException {
                        TimeTaken tt = trans.start("AAF Perm Details",Env.REMOTE);
                        try {
                            Future<Perms> fp= client.read("/authz/perms/"+pType + '/' + pInstance + '/' + pAction,gui.getDF(Perms.class));
                
                            if (fp.get(AAF_GUI.TIMEOUT)) {
                                tt.done();
                                tt = trans.start("Load Data", Env.SUB);
                                List<Perm> ps = fp.value.getPerm();
                                if (!ps.isEmpty()) {
                                    Perm perm = fp.value.getPerm().get(0);
                                    String desc = (perm.getDescription()!=null?perm.getDescription():BLANK);
                                    rv.add(new AbsCell[]{new TextCell("Description:"),new TextCell(desc)});
                                    boolean first=true;
                                    for (String r : perm.getRoles()) {
                                        if (first){
                                            first=false;
                                            rv.add(new AbsCell[] {
                                                    new TextCell("Associated Roles:"),
                                                    new TextCell(r)
                                                });
                                        } else {
                                            rv.add(new AbsCell[] {
                                                AbsCell.Null,
                                                new TextCell(r)
                                            });
                                        }
                                    }
                                }
                                String historyLink = PermHistory.HREF 
                                        + "?type=" + pType + "&instance=" + pInstance + "&action=" + pAction;
                            
                                rv.add(new AbsCell[] {new RefCell("See History",historyLink,false,"class=greenbutton")});
                            } else {
                                rv.add(new AbsCell[] {new TextCell(
                                    fp.code()==HttpStatus.NOT_FOUND_404?
                                        "*** Implicit Permission ***":
                                        "*** Data Unavailable ***"
                                        )});
                            }
                        } finally {
                            tt.done();
                        }

                        return null;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new Cells(rv,null);
        }
    }
}    
        