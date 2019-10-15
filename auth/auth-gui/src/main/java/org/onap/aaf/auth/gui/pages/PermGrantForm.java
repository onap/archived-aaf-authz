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

import static org.onap.aaf.misc.xgen.html.HTMLGen.TABLE;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
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

import aaf.v2_0.Role;
import aaf.v2_0.Roles;

public class PermGrantForm extends Page {
    static final String HREF = "/gui/permgrant";
    static final String NAME = "Permission Grant";
    static final String fields[] = {"type","instance","action","role"};

    public PermGrantForm(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env,NAME,HREF, fields,
            new BreadCrumbs(breadcrumbs),
            new NamedCode(true,"content") {
            @Override
            public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                final Slot type = gui.env.slot(NAME+".type");
                final Slot instance = gui.env.slot(NAME+".instance");
                final Slot action = gui.env.slot(NAME+".action");
                final Slot role = gui.env.slot(NAME+".role");
                // p tags not closing right using .p() - causes issues in IE8 password form - so using leaf for the moment
                hgen.leaf("p").text("Choose a role to grant to this permission").end()
                    .incr("form","method=post");
                Mark table = new Mark(TABLE);
                hgen.incr(table);
                cache.dynamic(hgen, new DynamicCode<HTMLGen, AAF_GUI, AuthzTrans>() {
                    @Override
                    public void code(final AAF_GUI gui, final AuthzTrans trans,    final Cache<HTMLGen> cache, final HTMLGen hgen)    throws APIException, IOException {
                    
                        Mark copyRoleJS = new Mark();
                        hgen.js(copyRoleJS);
                        hgen.text("function copyRole(role) {");
                        hgen.text("var txtRole = document.querySelector(\"#role\");");
//                        hgen.text("if (role==;");
                        hgen.text("txtRole.value=role;");
                        hgen.text("}");
                        hgen.end(copyRoleJS);
                    
                        String typeValue = trans.get(type, "");
                        String instanceValue = trans.get(instance, "");
                        String actionValue = trans.get(action, "");
                        String roleValue = trans.get(role,null);
                        List<String> myRoles = getMyRoles(gui, trans);
                        hgen
                        .input(fields[0],"Perm Type",true,"value="+typeValue,"disabled")
                        .input(fields[1],"Perm Instance",true,"value="+instanceValue,"disabled")
                        .input(fields[2],"Perm Action",true,"value="+actionValue,"disabled");
                    
                        // select & options are not an input type, so we must create table row & cell tags
                        Mark selectRow = new Mark();
                        hgen
                        .incr(selectRow, "tr")
                        .incr("td")
                        .incr("label", "for=myroles", "required").text("My Roles").end()
                        .end()
                        .incr("td")
                        .incr("select", "name=myroles", "id=myroles", "onchange=copyRole(this.value)")
                        .incr("option", "value=").text("Select one of my roles").end();
                        for (String role : myRoles) {
                            hgen.incr("option", "value="+role).text(role).end();
                        }
                        hgen
                        .incr("option", "value=").text("Other").end()                
                        .end(selectRow);
                        if (roleValue==null) {
                            hgen.input(fields[3],"Role", true, "placeholder=or type a role here");
                        } else {
                            hgen.input(fields[3],"Role",true, "value="+roleValue);
                        }
                        hgen.end();
                    }
                });
                hgen.end();
                hgen.tagOnly("input", "type=submit", "value=Submit")
                .end();

            }
        });
    }
    
    private static List<String> getMyRoles(final AAF_GUI gui, final AuthzTrans trans) {
        final List<String> myRoles = new ArrayList<>();
        try {
            gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
                @Override
                public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                    TimeTaken tt = trans.start("AAF get my roles",Env.REMOTE);
                    try {
                        Future<Roles> fr = client.read("/authz/roles/user/"+trans.user(),gui.getDF(Roles.class));
                        if (fr.get(5000)) {
                            tt.done();
                            tt = trans.start("Load Data", Env.SUB);
                            if (fr.value != null) for (Role r : fr.value.getRole()) {
                                myRoles.add(r.getName());
                            }
                        } else {
                            gui.writeError(trans, fr, null, 0);
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

        return myRoles;
    }
}
