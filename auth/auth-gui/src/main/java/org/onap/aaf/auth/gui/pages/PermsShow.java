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
import java.util.ArrayList;

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
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;

import aaf.v2_0.Perm;
import aaf.v2_0.Perms;

/**
 * Page content for My Permissions
 *
 * @author Jonathan
 *
 */
public class PermsShow extends Page {
    public static final String HREF = "/gui/myperms";

    public PermsShow(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env, "MyPerms",HREF, NO_FIELDS,
            new BreadCrumbs(breadcrumbs), 
            new Table<AAF_GUI,AuthzTrans>("Permissions",gui.env.newTransNoAvg(),new Model(), "class=std"));
    }

    /**
     * Implement the Table Content for Permissions by User
     *
     * @author Jonathan
     *
     */
    private static class Model extends TableData<AAF_GUI,AuthzTrans> {
        private static final String[] headers = new String[] {"Type","Instance","Action"};

        @Override
        public String[] headers() {
            return headers;
        }
    
        @Override
        public Cells get(final AuthzTrans trans, final AAF_GUI gui) {
            final ArrayList<AbsCell[]> rv = new ArrayList<>();
            TimeTaken tt = trans.start("AAF Perms by User",Env.REMOTE);
            try {
                gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
                    @Override
                    public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                        Future<Perms> fp = client.read("/authz/perms/user/"+trans.user(), gui.getDF(Perms.class));
                        if (fp.get(5000)) {
                            TimeTaken ttld = trans.start("Load Data", Env.SUB);
                            try {
                                if (fp.value!=null) {
                                    for (Perm p : fp.value.getPerm()) {
                                        AbsCell[] sa = new AbsCell[] {
                                            new RefCell(p.getType(),PermDetail.HREF
                                                    +"?type="+p.getType()
                                                    +"&amp;instance="+p.getInstance()
                                                    +"&amp;action="+p.getAction(),
                                                    false),
                                            new TextCell(p.getInstance()),
                                            new TextCell(p.getAction())
                                        };
                                        rv.add(sa);
                                    }
                                } else {
                                    gui.writeError(trans, fp, null,0);
                                }
                            } finally {
                                ttld.done();
                            }
                        }
                        return null;
                    }
                });
            } catch (Exception e) {
                trans.error().log(e);
            } finally {
                tt.done();
            }
            return new Cells(rv,null);
        }
    }
}
