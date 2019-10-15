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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.gui.Table;
import org.onap.aaf.auth.gui.Table.Cells;
import org.onap.aaf.auth.gui.table.AbsCell;
import org.onap.aaf.auth.gui.table.TableData;
import org.onap.aaf.auth.gui.table.TextCell;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.TimeTaken;

import aaf.v2_0.Approval;
import aaf.v2_0.Approvals;

public class RequestDetail extends Page {
    public static final String HREF = "/gui/requestdetail";
    public static final String NAME = "RequestDetail";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String[] FIELDS = {"ticket"};

    public RequestDetail(final AAF_GUI gui, Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env, NAME, HREF, FIELDS,
                new BreadCrumbs(breadcrumbs),
                new Table<AAF_GUI,AuthzTrans>("Request Details",gui.env.newTransNoAvg(),new Model(gui.env),"class=detail")
                );
    }

    /**
     * Implement the table content for Request Detail
     * <p>
     * @author Jeremiah
     *
     */
    private static class Model extends TableData<AAF_GUI,AuthzTrans> {
       static final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;
        private Slot sTicket;
        public Model(AuthzEnv env) {
            sTicket = env.slot(NAME+".ticket");
        }

        @Override
        public Cells get(final AuthzTrans trans, final AAF_GUI gui) {
            Cells rv=Cells.EMPTY;
            final String ticket = trans.get(sTicket, null);
            if (ticket!=null) {
                try {
                    rv = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Cells>() {
                        @Override
                        public Cells code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                            TimeTaken tt = trans.start("AAF Approval Details",Env.REMOTE);
                            ArrayList<AbsCell[]> rv = new ArrayList<>();
                            try {
                                Future<Approvals> fa = client.read(
                                    "/authz/approval/ticket/"+ticket, 
                                    gui.getDF(Approvals.class)
                                    );
                            
                                if (fa.get(AAF_GUI.TIMEOUT)) {
                                    Approval app = fa.value.getApprovals().get(0);
                                    if(app==null) {
                                        return Cells.EMPTY;
                                    } else {
                                        if (!(trans.user().equals(app.getUser()) ||
                                              trans.user().equals(app.getApprover()))) {
                                            HttpServletRequest req = trans.get(gui.slot_httpServletRequest,null);
                                            if(req==null || !req.isUserInRole(Define.ROOT_NS()+"|access|*|*")) {
                                                return Cells.EMPTY;
                                            }
                                        }
                                    }
                                    tt.done();
                                    tt = trans.start("Load Data", Env.SUB);
                                    boolean first = true;
                                    for ( Approval approval : fa.value.getApprovals()) {
                                        AbsCell[] approverLine = new AbsCell[4];
                                        // only print common elements once
                                        if (first) {
                                            DateFormat createdDF = new SimpleDateFormat(DATE_TIME_FORMAT);
                                            UUID id = UUID.fromString(approval.getId());
                                        
                                            rv.add(new AbsCell[]{new TextCell("Ticket ID:"),new TextCell(approval.getTicket(),"colspan=3")});
                                            rv.add(new AbsCell[]{new TextCell("Memo:"),new TextCell(approval.getMemo(),"colspan=3")});
                                            rv.add(new AbsCell[]{new TextCell("Requested On:"), 
                                                    new TextCell(createdDF.format((id.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH)/10000),"colspan=3")
                                            });
                                            rv.add(new AbsCell[]{new TextCell("Operation:"),new TextCell(decodeOp(approval.getOperation()),"colspan=3")});
                                            String user = approval.getUser();
                                            rv.add(new AbsCell[]{new TextCell("User:"),new TextCell(user,"colspan=3")});
                                        
                                            // headers for listing each approver
                                            rv.add(new AbsCell[]{new TextCell(" ","colspan=4","class=blank_line")});
                                            rv.add(new AbsCell[]{AbsCell.Null,
                                                    new TextCell("Approver","class=bold"), 
                                                    new TextCell("Type","class=bold"), 
                                                    new TextCell("Status","class=bold")});
                                            approverLine[0] = new TextCell("Approvals:");
                                        
                                            first = false;
                                        } else {
                                            approverLine[0] = AbsCell.Null;
                                        }
                                    
                                        approverLine[1] = new TextCell(approval.getApprover());
                                        String type = approval.getType();
                                        if ("owner".equalsIgnoreCase(type)) {
                                            type = "resource owner";
                                        }
                                    
                                        approverLine[2] = new TextCell(type);
                                        approverLine[3] = new TextCell(approval.getStatus());
                                        rv.add(approverLine);
                                
                                    }
                                } else {
                                    rv.add(new AbsCell[] {new TextCell("*** Data Unavailable ***")});
                                }
                            } finally {
                                tt.done();
                            }
                            return new Cells(rv,null);
                        }
                    });
                } catch (Exception e) {
                    trans.error().log(e);
                }
            }
            return rv;
        }

        private String decodeOp(String operation) {
            if ("C".equalsIgnoreCase(operation)) {
                return "Create";
            } else if ("D".equalsIgnoreCase(operation)) {
                return "Delete";
            } else if ("U".equalsIgnoreCase(operation)) {
                return "Update";
            } else if ("G".equalsIgnoreCase(operation)) {
                return "Grant";
            } else if ("UG".equalsIgnoreCase(operation)) {
                return "Un-Grant";
            }
            return operation;
        }
    }
}
