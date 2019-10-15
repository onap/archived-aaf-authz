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
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.NamedCode;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.gui.table.TableData;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.util.Split;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.html.HTMLGen;

import aaf.v2_0.Pkey;
import aaf.v2_0.RolePermRequest;
import aaf.v2_0.RoleRequest;

public class RoleDetailAction extends Page {
    public RoleDetailAction(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env,RoleDetail.NAME, RoleDetail.HREF, TableData.headers,
            new BreadCrumbs(breadcrumbs),
            new NamedCode(true,"content") {
                final Slot sReq = gui.env.slot(AAF_GUI.HTTP_SERVLET_REQUEST);
            
                @Override
                public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                    cache.dynamic(hgen, new DynamicCode<HTMLGen,AAF_GUI, AuthzTrans>() {
                        @Override
                        public void code(final AAF_GUI gui, final AuthzTrans trans,final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                            final HttpServletRequest req = trans.get(sReq, null);
                            final String role = getSingleParam(req,"role");
                            if (role==null) {
                                hgen.text("Parameter 'role' is required").end(); 
                            } else {
                                // Run Validations
//                                boolean fail;
                                try {
                                    /*fail =*/ gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Boolean>() {
                                        @Override
                                        public Boolean code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                                            List<TypedFuture> ltf = new ArrayList<>();
                                            String text;
                                            Map<String, String[]> pm = (Map<String, String[]>)req.getParameterMap();
                                            for (final Entry<String, String[]> es : pm.entrySet()) {
                                                for (final String v : es.getValue()) {
                                                    TimeTaken tt = null; 
                                                    try {
                                                        switch(es.getKey()) {
                                                            case "desc": // Check box set
                                                                String desc = getSingleParam(req, "description");
                                                                if (desc!=null) {
                                                                    text = "Setting Description on " + role + " to " + desc;
                                                                    tt = trans.start(text, Env.REMOTE);
                                                                    RoleRequest rr = new RoleRequest();
                                                                    rr.setName(role);
                                                                    rr.setDescription(desc);
                                                                    ltf.add(new TypedFuture(ActionType.desc, text, 
                                                                            client.update("/authz/role",
                                                                                    gui.getDF(RoleRequest.class),rr
                                                                        )));
                                                                }
                                                                break;
                                                            case "perm.ungrant":
                                                                text = "Ungranting Permission '" + v + "' from '" + role + '\'';
                                                                tt = trans.start(text, Env.REMOTE);
                                                                String[] pf = Split.splitTrim('|', v);
                                                                if (pf.length==3) {
                                                                    Pkey perm = new Pkey();
                                                                    perm.setType(pf[0]);
                                                                    perm.setInstance(pf[1]);
                                                                    perm.setAction(pf[2]);
                                                                    RolePermRequest rpr = new RolePermRequest();
                                                                    rpr.setPerm(perm);
                                                                    rpr.setRole(role);
                                                                    ltf.add(new TypedFuture(ActionType.ungrant,text,
                                                                            client.delete("/authz/role/" + role + "/perm", 
                                                                                gui.getDF(RolePermRequest.class),rpr
                                                                            )));
                                                                } else {
                                                                    hgen.p(v + " is not a valid Perm for ungranting");
                                                                }
                                                                break;
                                                            case "userrole.extend":
                                                                text = "Extending " + v + " in " + role;
                                                                tt = trans.start(text, Env.REMOTE);
                                                                ltf.add(new TypedFuture(ActionType.extendUR,text,
                                                                        client.update("/authz/userRole/extend/" + v + '/' + role)));
                                                                break;
                                                            case "userrole.delete":
                                                                text = "Deleting " + v + " from " + role;
                                                                tt = trans.start(text, Env.REMOTE);
                                                                ltf.add(new TypedFuture(ActionType.deleteUR,text,
                                                                        client.delete("/authz/userRole/" + v + '/' + role, Void.class)));
                                                                break;

                                                            default:
//                                                                System.out.println(es.getKey() + "=" + v);
                                                        }
                                                    } finally {
                                                        if (tt!=null) {
                                                            tt.done();
                                                            tt=null;
                                                        }
                                                    }
                                                }
                                            }
                                        
                                            if (ltf.isEmpty()) {
                                                hgen.p("No Changes");
                                            } else {
                                                for (TypedFuture tf : ltf) {
                                                    if (tf.future.get(5000)) {
                                                        hgen.p("<font color=\"green\"><i>Success</i>:</font> " + tf.text);
                                                    } else {
                                                        // Note: if handling of special Error codes is required, use 
                                                        // switch(tf.type) {
                                                        // }
                                                        hgen.p(tf.text);
                                                        gui.writeError(trans, tf.future, hgen,4);
                                                    }
                                                }
                                            }
                                            return true;
                                        }
                                    });
                                } catch (Exception e) {
                                    hgen.p("Unknown Error");
                                    e.printStackTrace();
                                }
                            }
                        }

                    });
                }
            });
    }

    enum ActionType {desc, ungrant, deleteUR, extendUR};
    private static class TypedFuture {
//        public final ActionType type;
        public final Future<?> future;
        public final String text;
    
        public TypedFuture(ActionType type, String text, Future<?> future) {
//            this.type = type;
            this.future = future;
            this.text = text;
        }
    }
}
