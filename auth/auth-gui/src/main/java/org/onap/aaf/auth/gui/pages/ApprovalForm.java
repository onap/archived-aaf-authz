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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.Form;
import org.onap.aaf.auth.gui.NamedCode;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.gui.Table;
import org.onap.aaf.auth.gui.Table.Cells;
import org.onap.aaf.auth.gui.table.AbsCell;
import org.onap.aaf.auth.gui.table.ButtonCell;
import org.onap.aaf.auth.gui.table.RadioCell;
import org.onap.aaf.auth.gui.table.TableData;
import org.onap.aaf.auth.gui.table.TextCell;
import org.onap.aaf.auth.gui.table.TextToolTipCell;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationFactory;
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

import aaf.v2_0.Approval;
import aaf.v2_0.Approvals;

public class ApprovalForm extends Page {
    // Package on purpose
    static final String NAME="Approvals";
    static final String HREF = "/gui/approve";
    static final String[] FIELDS = new String[] {"line[]","user","delegate_of","as_user"};


    public ApprovalForm(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env,NAME,HREF, FIELDS,

            new BreadCrumbs(breadcrumbs),
            new NamedCode(false, "filterByUser") {
                @Override
                public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                    cache.dynamic(hgen, new DynamicCode<HTMLGen, AAF_GUI, AuthzTrans>() {
                        @Override
                        public void code(final AAF_GUI gui, final AuthzTrans trans,    final Cache<HTMLGen> cache, final HTMLGen hgen)    throws APIException, IOException {
                            String user = trans.get(trans.env().slot(NAME+".user"),"");
                            hgen.incr("p", "class=userFilter")
                                .text("Filter by User:")
                                .tagOnly("input", "type=text", "value="+user, "id=userTextBox")
                                .tagOnly("input", "type=button", "onclick=userFilter('"+HREF+"');", "value=Go!")
                                .end();
                                }
                    });
                }
            },
            new Form(true,new Table<AAF_GUI,AuthzTrans>("Approval Requests", gui.env.newTransNoAvg(),new Model(gui.env),"class=stdform"))
                .preamble("The following requires your Approval to proceed in the AAF System.</p><p class=subtext>Hover on Name for Identity; If Deny is the only option, User is no longer valid."),
            new NamedCode(false, "selectAlljs") {
                @Override
                public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                    Mark jsStart = new Mark();
                    hgen.js(jsStart);
                    hgen.text("function selectAll(radioClass) {");
                    hgen.text("var radios = document.querySelectorAll(\".\"+radioClass);");
                    hgen.text("for (i = 0; i < radios.length; i++) {");
                    hgen.text("radios[i].checked = true;");
                    hgen.text("}");
                    hgen.text("}");
                    hgen.end(jsStart);
                }
            });
    }

    /**
     * Implement the Table Content for Approvals
     *
     * @author Jonathan
     *
     */
    private static class Model extends TableData<AAF_GUI,AuthzTrans> {
        //TODO come up with a generic way to do ILM Info (people page)
//        private static final String TODO_ILM_INFO = "TODO: ILM Info";
    
    
        private static final String[] headers = new String[] {"Identity","Request","Approve","Deny"};
        private Slot sUser;
        private Slot sAsDelegate;
        private Slot sAsUser;
    
        public Model(AuthzEnv env) {
            sUser = env.slot(NAME+".user");
            sAsDelegate = env.slot(NAME+".delegate_of");
            sAsUser = env.slot(NAME + ".as_user");
        }
    
        @Override
        public String[] headers() {
            return headers;
        }
    
        @Override
        public Cells get(final AuthzTrans trans, final AAF_GUI gui) {
            final String userParam = trans.get(sUser, null);
        
            final String asDelegate = trans.get(sAsDelegate, null);
            final String approver;
            if(asDelegate==null) {
                approver = trans.get(sAsUser,trans.user());
            } else {
                approver = asDelegate;
            }
         
            ArrayList<AbsCell[]> rv = new ArrayList<>();
            String msg = null;
            TimeTaken tt = trans.start("AAF Get Approvals for Approver",Env.REMOTE);
            try {
                final List<Approval> pendingApprovals = new ArrayList<>();
                final List<Integer> beginIndicesPerApprover = new ArrayList<>();
                int numLeft = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Integer>() {
                    @Override
                    public Integer code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                        Future<Approvals> fa = client.read("/authz/approval/approver/"+approver,gui.getDF(Approvals.class));
                        int numLeft = 0;
                        if (fa.get(AAF_GUI.TIMEOUT)) {
                        
                            if (fa.value!=null) {
                                for (Approval appr : fa.value.getApprovals()) {
                                    if ("pending".equals(appr.getStatus())) {
                                        if (userParam!=null && !appr.getUser().equalsIgnoreCase(userParam)) {
                                                numLeft++;
                                                continue;
                                        }
                                        pendingApprovals.add(appr);
                                    }
                                }
                            }
                        
                            String prevApprover = null;
                            int overallIndex = 0;
                            
                            for (Approval appr : pendingApprovals) {
                                String currApprover = appr.getApprover();
                                if (!currApprover.equals(prevApprover)) {
                                    prevApprover = currApprover;
                                    beginIndicesPerApprover.add(overallIndex);
                                }
                                overallIndex++;
                            }
                        }
                        return numLeft;
                    }
                });
            
                if (!pendingApprovals.isEmpty()) {
                    // Only add select all links if we have approvals
                    AbsCell[] selectAllRow = new AbsCell[] {
                            AbsCell.Null,
                            AbsCell.Null,
                            new ButtonCell("all", "onclick=selectAll('approve')", "class=selectAllButton"),
                            new ButtonCell("all", "onclick=selectAll('deny')", "class=selectAllButton")
                        };
                    rv.add(selectAllRow);
                }
                    
                int line=-1;
            
                while (!beginIndicesPerApprover.isEmpty()) {
                    int beginIndex = beginIndicesPerApprover.remove(0);
                    int endIndex = (beginIndicesPerApprover.isEmpty()?pendingApprovals.size():beginIndicesPerApprover.get(0));
                    List<Approval> currApproverList = pendingApprovals.subList(beginIndex, endIndex);
                
                    Identity iapprover = trans.org().getIdentity(trans,currApproverList.get(0).getApprover());
                    if(iapprover==null) {
                        rv.add(new AbsCell[] {
                                new TextCell(currApproverList.get(0).getApprover() + " is not part of Organization",
                                        new String[] {"colspan=4", "class=head"})
                        });
                    } else {
                        if (!iapprover.fullID().equals(trans.user())) {
                    
                            AbsCell[] approverHeader;
    //                        if (domainOfUser.equals(domainOfApprover)) {
    //                            approverHeader = new AbsCell[] { 
    //                                    new TextAndRefCell("Approvals Delegated to Me by ", currApproverFull,
    //                                            TODO_ILM_INFO + currApproverShort, 
    //                                            true,
    //                                            new String[] {"colspan=4", "class=head"})
    //                            };
    //                        } else {
                                approverHeader = new AbsCell[] { 
                                        new TextCell("Approvals Delegated to Me by " + iapprover.fullName() 
                                            + '(' + iapprover.id() + ')',
                                                new String[] {"colspan=4", "class=head"})
                                };
    //                        }
                            rv.add(approverHeader);
                        }
                    
                        // Sort by User Requesting
                        Collections.sort(currApproverList, new Comparator<Approval>() {
                            @Override
                            public int compare(Approval a1, Approval a2) {
                                return a1.getUser().compareTo(a2.getUser());
                            }
                        });
                    
                        String prevUser = null;
                        boolean userOK=true;
                        for (Approval appr : currApproverList) {
                            if (++line<MAX_LINE) { // limit number displayed at one time.
                                AbsCell userCell;
                                String user = appr.getUser();
                            
                                if (user.equals(prevUser)) {
                                    userCell = AbsCell.Null; 
                                } else if (user.endsWith(trans.org().getRealm())){
                                    userOK=true;
                                    String title;
                                    Organization org = OrganizationFactory.obtain(trans.env(), user);
                                    if (org==null) {
                                        title="";
                                        userCell = new TextCell(user);
                                    } else {
                                        Identity au = org.getIdentity(trans, user);
                                        if (au!=null) {
                                            if(au.isPerson()) {
                                                userCell = new TextToolTipCell(au.fullName(),"Identity: " + au.id());
                                            } else {
                                                Identity managedBy = au.responsibleTo();
                                                if (managedBy==null) {
                                                    title ="Identity: " + au.type();
                                                } else {
                                                    title="Sponsor: " + managedBy.fullName();                                            
                                                }
                                                userCell = new TextToolTipCell(au.fullID(),title);
                                            }
                                        } else {
                                            userOK=false;
                                            title="Not a User at " + org.getName();
                                            userCell = new TextToolTipCell(user,title);
                                        }
                                    }
    //                                userCell = new RefCell(prevUser,
    //                                    TODO_ILM_INFO+user.substring(0, user.length()-domainOfApprover.length()),
    //                                    true,
    //                                    title);
                                
                                } else {
                                    userCell = new TextCell(user);
                                }
                                AbsCell[] sa = new AbsCell[] {
                                    userCell,
                                    new TextCell(appr.getMemo()),
                                    userOK?new RadioCell("line."+ line,"approve", "approved|"+appr.getTicket()):new TextCell(""),
                                    new RadioCell("line."+ line,"deny", "denied|"+appr.getTicket())
                                };
                                rv.add(sa);
                                prevUser=user;
                            } else {
                                ++numLeft;
                            }
                        }
                    }
                    if (numLeft>0) {
                        msg = "After these, there will be " + numLeft + " approvals left to process";
                    }
                    if (rv.isEmpty()) {
                        if (numLeft>0) {
                            msg = "No Approvals to process at this time for user " + userParam +". You have " 
                                + numLeft + " other approvals to process.";
                        } else {
                            msg = "No Approvals to process at this time";
                        }
                    }
                }
            } catch (Exception e) {
                trans.error().log(e);
            } finally {
                tt.done();
            }
        return new Cells(rv,msg);
        }
    }
}
