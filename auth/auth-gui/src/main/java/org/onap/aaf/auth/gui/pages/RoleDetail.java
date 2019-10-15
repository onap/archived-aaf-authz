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

import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.gui.Table;
import org.onap.aaf.auth.gui.Table.Cells;
import org.onap.aaf.auth.gui.table.AbsCell;
import org.onap.aaf.auth.gui.table.CheckBoxCell;
import org.onap.aaf.auth.gui.table.CheckBoxCell.ALIGN;
import org.onap.aaf.auth.gui.table.RefCell;
import org.onap.aaf.auth.gui.table.TableData;
import org.onap.aaf.auth.gui.table.TextCell;
import org.onap.aaf.auth.gui.table.TextInputCell;
import org.onap.aaf.auth.validation.Validator;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.html.HTMLGen;

import aaf.v2_0.Pkey;
import aaf.v2_0.Role;
import aaf.v2_0.Roles;
import aaf.v2_0.UserRole;
import aaf.v2_0.UserRoles;

/**
 * Detail Page for Permissions
 * <p>
 * @author Jonathan
 *
 */
public class RoleDetail extends Page {
    public static final String HREF = "/gui/roledetail";
    public static final String NAME = "RoleDetail";
    private static final String BLANK = "";

    public RoleDetail(final AAF_GUI gui, Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env, NAME, HREF, new String[] {"role","ns"},
                new BreadCrumbs(breadcrumbs),
                new Table<AAF_GUI,AuthzTrans>("Role Details",gui.env.newTransNoAvg(),
                        new Model(gui.env),"class=detail")
            );
    }

    /**
     * Implement the table content for Permissions Detail
     * <p>
     * @author Jonathan
     *
     */
    private static class Model extends TableData<AAF_GUI,AuthzTrans> {
        private static final String ACCESS = "access";
        private Slot sRoleName,sRole,sUserRole,sMayWrite,sMayApprove,sMark,sNS;
        public Model(AuthzEnv env) {
            sRoleName = env.slot(NAME+".role");
            sRole = env.slot(NAME+".data.role");
            sUserRole = env.slot(NAME+".data.userrole");
            sMayWrite = env.slot(NAME+"mayWrite");
            sMayApprove = env.slot(NAME+"mayApprove");
            sMark = env.slot(NAME+"mark");
            sNS = env.slot(NAME+".ns");
        }

        /* (non-Javadoc)
         * @see org.onap.aaf.auth.gui.table.TableData#prefix(org.onap.aaf.misc.xgen.html.State, com.att.inno.env.Trans, org.onap.aaf.misc.xgen.Cache, org.onap.aaf.misc.xgen.html.HTMLGen)
         */
        @Override
        public void prefix(final AAF_GUI gui, final AuthzTrans trans, final Cache<HTMLGen> cache, final HTMLGen hgen) {
            final String pRole = trans.get(sRoleName, null);
            Validator v = new Validator();
            if(!v.isNull("Role",pRole).err()) {
                if(!pRole.startsWith(trans.user())) {
                    v.role(pRole);
                }
            }
            if (v.err()) {
                trans.warn().printf("Error in PermDetail Request: %s", v.errs());
                return;
            }

    
            try { 
                gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Boolean>() {
                    @Override
                    public Boolean code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                        TimeTaken tt = trans.start("AAF Role Details",Env.REMOTE);
                        try {
                            Future<Roles> fr = client.read("/authz/roles/"+pRole+"?ns",gui.getDF(Roles.class));
                            Future<UserRoles> fur = client.read("/authz/userRoles/role/"+pRole,gui.getDF(UserRoles.class));
                            if (fr.get(AAF_GUI.TIMEOUT)) {
                                List<Role> roles = fr.value.getRole();
                                if (!roles.isEmpty()) {
                                    Role role = fr.value.getRole().get(0);
                                    trans.put(sRole, role);
                                    Boolean mayWrite = trans.fish(new AAFPermission(role.getNs(),ACCESS,":role:"+role.getName(),"write"));
                                    trans.put(sMayWrite,mayWrite);
                                    Boolean mayApprove = trans.fish(new AAFPermission(role.getNs(),ACCESS,":role:"+role.getName(),"approve"));
                                    trans.put(sMayApprove, mayApprove);
                                
                                    if (mayWrite || mayApprove) {
                                        Mark js = new Mark();
                                        Mark fn = new Mark();
                                        hgen.js(js)
                                            .function(fn,"touchedDesc")
                                            .li("d=document.getElementById('descText');",
                                                "if (d.orig == undefined ) {",
                                                "  d.orig = d.value;",
                                                "  d.addEventListener('keyup',changedDesc);",
                                                "  d.removeEventListener('keypress',touchedDesc);",
                                                "}").end(fn)
                                            .function(fn,"changedDesc")
                                            .li(
                                                "dcb=document.getElementById('descCB');",
                                                "d=document.getElementById('descText');",
                                                "dcb.checked= (d.orig != d.value)"
                                            ).end(fn)
                                            .end(js);

                                        Mark mark = new Mark();
                                        hgen.incr(mark,"form","method=post");
                                        trans.put(sMark, mark);
                                    }
                                }
                            } else {
                                trans.error().printf("Error calling AAF for Roles in GUI, Role Detail %d: %s",fr.code(),fr.body());
                                return false;
                            }
                        
                            if (fur.get(AAF_GUI.TIMEOUT)) {
                                trans.put(sUserRole, fur.value.getUserRole());
                            } else {
                                trans.error().printf("Error calling AAF for UserRoles in GUI, Role Detail %d: %s",fr.code(),fr.body());
                                return false;
                            }

                            return true;
                        } finally {
                            tt.done();
                        }
                    }
                });
            } catch (Exception e) {
                trans.error().log(e);
            }
        }

        @Override
        public Cells get(final AuthzTrans trans, final AAF_GUI gui) {
            final String pRole = trans.get(sRoleName, null);
            final Role role = trans.get(sRole,null);
            ArrayList<AbsCell[]> rv = new ArrayList<>();
        
            if (role!=null) {
                boolean mayWrite = trans.get(sMayWrite, false);
                boolean mayApprove = trans.get(sMayApprove, false);

                String desc = (role.getDescription()!=null?role.getDescription():BLANK);
                rv.add(new AbsCell[]{
                        new TextCell("Role:","width=45%"),
                        new TextCell(pRole)});
                if (mayWrite) {
                    rv.add(new AbsCell[]{
                            new TextCell("Description:","width=45%"),
                            new TextInputCell("description","textInput",desc,"id=descText","onkeypress=touchedDesc()"),
                            new CheckBoxCell("desc",ALIGN.left, "changed","id=descCB", "style=visibility: hidden"),
                            });
                    rv.add(AbsCell.HLINE);
                    rv.add(new AbsCell[] {
                            new TextCell("Associated Permissions:","width=25%"),
                            new TextCell("UnGrant","width=10%"),
                        });
                } else {
                    rv.add(new AbsCell[]{
                            new TextCell("Description:","width=45%"),
                            new TextCell(desc)});
                }
                boolean protectedRole = role.getName().endsWith(".owner") ||
                                        role.getName().endsWith(".admin");
                boolean first = true;
                for (Pkey r : role.getPerms()) {
                    String key=r.getType() + '|' + r.getInstance() + '|' + r.getAction();
                    if (mayWrite) {
                        rv.add(new AbsCell[] {
                            AbsCell.Null,
                            protectedRole && r.getType().endsWith(".access")
                                ?new TextCell("protected","class=protected") // Do not allow ungranting of basic NS perms
                                :new CheckBoxCell("perm.ungrant",key),
                            new TextCell("","width=10%"),
                            new TextCell(key)
                        });
                    } else {
                        if (first) {
                            rv.add(new AbsCell[] {
                                    new TextCell("Associated Permissions:","width=45%"),
                                    new TextCell(key)
                                });
                            first=false;
                        } else {
                            rv.add(new AbsCell[] {
                                    AbsCell.Null,
                                    new TextCell(key)
                            });
                        }
                    }
                }
                    
                if (mayApprove) {
                    rv.add(AbsCell.HLINE);

                    // 
                    rv.add(new AbsCell[] {
                            new TextCell("Users in Role:","width=25%"),
                            new TextCell("Delete","width=10%"),
                            new TextCell("Extend","width=10%")
                        });

                    List<UserRole> userroles = trans.get(sUserRole,null);
                    if (userroles!=null) {
                        for (UserRole ur : userroles) {
                            String tag = "userrole";
                        
                            rv.add(new AbsCell[] {
                                AbsCell.Null,
                                new CheckBoxCell(tag+".delete", ur.getUser()),
                                new CheckBoxCell(tag+".extend", ur.getUser()),
                                new TextCell(ur.getUser()),
                                new TextCell(Chrono.dateOnlyStamp(ur.getExpires())
                            )});
                        }
                    }
                }
                    
                // History 
                rv.add(new AbsCell[] {
                        new RefCell("See History",RoleHistory.HREF + "?role=" + pRole,false,"class=greenbutton")
                    });
            } else {
                rv.add(new AbsCell[]{
                        new TextCell("Role:"),
                        new TextCell(pRole)});

                rv.add(new AbsCell[] {new TextCell("*** Data Unavailable ***")});
            }
            return new Cells(rv, null);
        }

        /* (non-Javadoc)
         * @see org.onap.aaf.auth.gui.table.TableData#postfix(org.onap.aaf.misc.xgen.html.State, com.att.inno.env.Trans, org.onap.aaf.misc.xgen.Cache, org.onap.aaf.misc.xgen.html.HTMLGen)
         */
        @Override
        public void postfix(AAF_GUI state, AuthzTrans trans, final Cache<HTMLGen> cache, final HTMLGen hgen) {
            final Mark mark = trans.get(sMark, null);
            if (mark!=null) {
                hgen.tagOnly("input", "type=submit", "value=Submit");
                final String pNS = trans.get(sNS, null);
                if (pNS!=null && pNS.length()>0) {
                    hgen.leaf(mark,HTMLGen.A,"href="+NsDetail.HREF+"?ns="+pNS,"class=greenbutton").text("Back").end(mark);
                }
                hgen.end(mark);
            }

        }
    }
}    
        