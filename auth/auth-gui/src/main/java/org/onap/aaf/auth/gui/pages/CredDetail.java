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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.gui.SlotCode;
import org.onap.aaf.auth.gui.Table;
import org.onap.aaf.auth.gui.Table.Cells;
import org.onap.aaf.auth.gui.table.AbsCell;
import org.onap.aaf.auth.gui.table.TableData;
import org.onap.aaf.auth.gui.table.TextCell;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.util.FQI;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.html.HTMLGen;

import aaf.v2_0.Users;
import aaf.v2_0.Users.User;
import certman.v1_0.Artifacts;
import certman.v1_0.Artifacts.Artifact;

public class CredDetail extends Page {
    
    public static final String HREF = "/gui/creddetail";
    public static final String NAME = "CredDetail";
    private static Model model;
    private static SlotCode<AuthzTrans> slotCode;
    enum Params {id,ns};


    public CredDetail(final AAF_GUI gui, Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env, NAME, HREF, Params.values(), 
                new BreadCrumbs(breadcrumbs),
                new Table<AAF_GUI,AuthzTrans>("Cred Details",gui.env.newTransNoAvg(),model = new Model(),
                slotCode = new SlotCode<AuthzTrans>(false,gui.env,NAME,Params.values()) {
                    @Override
                    public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                        cache.dynamic(hgen, new DynamicCode<HTMLGen,AAF_GUI,AuthzTrans>() {
                        @Override
                        public void code(AAF_GUI state, AuthzTrans trans, final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                            String ns = get(trans, Params.ns,"");
                            String domain = FQI.reverseDomain(ns);
                            Mark js = new Mark(), fn=new Mark();
                            hgen.js(js).function(fn,"newArtifact")
                            .text("id=document.getElementById('id');")
                            .text("if (id.value=='') {alert('Enter the id in box');} else {")
                            .text("window.open('"+CMArtiChangeForm.HREF+"?id='+id.value+'&ns="+ns+"','_self');}"
                                )
                            .end(fn)
                            .function("newPassword")
                            .text("id=document.getElementById('id');")
                            .text("if (id.value=='') {alert('Enter the id in box');} else {")
                            .text("window.open('"+PassChangeForm.HREF+"?id='+id.value+'@"+domain+"&ns="+ns+"','_self');}"
                                )
                            .end(js);
                            hgen.leaf("i","style=margin:1em 0em 1em 1em;").text("ID:").end()
                                .leaf("input","id=id","style=width:10%;").end().text("@").text(domain).br()
                                .leaf(HTMLGen.A,"class=greenbutton","href=javascript:newArtifact()","style=color:white;margin:1.2em 0em 1em 1em;").text("As Cert Artifact").end()
                                .leaf(HTMLGen.A,"class=greenbutton","href=javascript:newPassword()","style=color:white;margin:1.2em 0em 1em 1em;").text("w/Password").end()
                                ;
                        }
                    });
                    }
                },"class=std")
                
                );
        // Setting so we can get access to HTMLGen clone
        model.set(this,slotCode);
    }



    /**
     * Implement the table content for Cred Detail
     * 
     * @author Jeremiah
     *
     */
    private static class Model extends TableData<AAF_GUI,AuthzTrans> {
        private static final String STYLE_WIDTH_5 = "style=width:5%;";
        private static final String STYLE_WIDTH_10 = "style=width:10%;";
        private static final String STYLE_WIDTH_15 = "style=width:15%;";
        private static final String STYLE_WIDTH_20 = "style=width:20%;";
        private static final String STYLE_WIDTH_70 = "style=width:70%;";
        private SlotCode<AuthzTrans> sc;
        private CredDetail cd;
        // Covering for Constructor Order
        private void set(CredDetail credDetail, SlotCode<AuthzTrans> slotCode) {
            cd = credDetail;
            sc = slotCode;
        }
        
        @Override
        public void prefix(AAF_GUI state, AuthzTrans trans, final Cache<HTMLGen> cache, final HTMLGen hgen) {
        }

        @Override
        public Cells get(final AuthzTrans trans, final AAF_GUI gui) {
            final String ns = sc.get(trans, Params.ns, "");
            final String id = sc.get(trans, Params.id, "");
            if (ns==null) {
                return Cells.EMPTY;
            }
            final ArrayList<AbsCell[]> rv = new ArrayList<>();
            final TimeTaken tt = trans.start("AAF Cred Details",Env.REMOTE);
            List<Artifact> la; 
            try {
                    la = gui.cmClientAsUser(trans.getUserPrincipal(), new Retryable<List<Artifact>>() {
                    @Override
                    public List<Artifact> code(Rcli<?> client)throws CadiException, ConnectException, APIException {
                        Future<Artifacts> fa = client.read("/cert/artifacts?ns="+ns,gui.artifactsDF);
                        if (fa.get(AAFcli.timeout())) {
                            return fa.value.getArtifact();
                        } else {
                            return null;
                        }
                    }

                });
                final Set<String> lns = new HashSet<>();
                if (la!=null) {
                    for (Artifact a : la){
                        lns.add(a.getMechid());
                    }
                }
                gui.clientAsUser(trans.getUserPrincipal(),new Retryable<Void>() {
                    @Override
                    public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                        Future<Users> fu = client.read("/authn/creds/ns/"+ns,gui.getDF(Users.class));
                        if (fu.get(AAFcli.timeout())) {
                            // Organize User entries
                            Map<String,List<Map<Integer,List<User>>>> users = new HashMap<>();
        
                            List<Map<Integer,List<User>>> lmu=null;
                            Map<Integer, List<User>> mu = null;
                            List<User> lu = null;
                            
                            for (User u : fu.value.getUser()) {
                                if (u.getType() == 200) {
                                    lns.remove(u.getId());
                                }
                                lmu = users.get(u.getId());
                                if (lmu==null) {
                                    users.put(u.getId(),lmu=new ArrayList<>());
                                }
                                mu=null;
                                for (Map<Integer,List<User>> xmu : lmu) {
                                    if (xmu.containsKey(u.getType())) {
                                        mu = xmu;
                                    }
                                }
                                
                                if (mu==null) {
                                    lmu.add(mu=new HashMap<>());
                                }
                                
                                lu = mu.get(u.getType());
                                if (lu==null) {
                                    mu.put(u.getType(),lu = new ArrayList<>());
                                }
                                lu.add(u);
                            }

                            int count=0;
                            for (Entry<String, List<Map<Integer, List<User>>>> ulm : users.entrySet()) {
                                String key = "cred_"+count++;
                                StringWriter buttons = new StringWriter();
                                HTMLGen hgen = cd.clone(buttons);
                                hgen.leaf("button","onclick=divVisibility('"+key+"');","class=button").text("Expand").end();
                                hgen.leaf(HTMLGen.A,"class=button","class=greenbutton","href="+CredHistory.HREF+"?user="+ulm.getKey()).text("History").end();
                                
                                StringWriter creds = new StringWriter();
                                hgen = cd.clone(creds);
                                Mark div = hgen.divID(key,ulm.getKey().equals(id)?"":"style=display:none;");
                                    for (Map<Integer, List<User>> miu : ulm.getValue()) {
                                        Mark utable = new Mark();
                                        hgen.leaf(utable,HTMLGen.TABLE);

                                        Mark uRow = new Mark();
                                        String cls;
                                        boolean first = true;
                                        
                                        for ( Entry<Integer, List<User>> es : miu.entrySet()) {
                                            Collections.sort(es.getValue(),new Comparator<User>() {
                                                @Override
                                                public int compare(User u1, User u2) {
                                                    int rv = u1.getType().compareTo(u2.getType());
                                                    return rv==0?u2.getExpires().compare(u1.getExpires()):rv;
                                                }
                                            });
                                            int xcnt = 0;
                                            XMLGregorianCalendar oldest=null, newest=null;
                                            String id = null;
                                            for (User u: es.getValue()) {
                                                if (id==null) {
                                                    id = u.getId();
                                                }
                                                // Need to compile entries for Certificates on this screen
                                                if (es.getKey()==200) {
                                                    ++xcnt;
                                                    if (oldest==null || oldest.compare(u.getExpires())<0) {
                                                        oldest = u.getExpires();
                                                    }
                                                    if (newest==null || newest.compare(u.getExpires())<0) {
                                                        newest = u.getExpires();
                                                    }
                                                } else {
                                                    hgen.leaf(uRow,HTMLGen.TR);
                                                    if (first) {
                                                        hgen.leaf(HTMLGen.TD,cls="class=detailFirst",STYLE_WIDTH_10);
                                                        switch(es.getKey()) {
                                                            case 1:   
                                                            case 2:      hgen.text("Password"); 
                                                                    break;
                                                            case 10:  hgen.text("Certificate"); break;
                                                        }
                                                    } else {
                                                        hgen.leaf(HTMLGen.TD,cls="class=detail",STYLE_WIDTH_10+"text-align:center;").text("\"");
                                                    }
                                                    hgen.end();
                                                    hgen.incr(HTMLGen.TD,cls,STYLE_WIDTH_20);
                                                    
                                                    hgen.leaf(HTMLGen.A,
                                                            "class=button",
                                                            "href="+PassDeleteAction.HREF+
                                                                "?id="+id+
                                                                "&amp;ns="+ns+
                                                                "&amp;date="+u.getExpires().toXMLFormat() +
                                                                "&amp;type="+u.getType())
                                                        .text("Delete").end();
                                                    if (first && es.getKey()<10) { // Change Password Screen
                                                        hgen.leaf(HTMLGen.A,"class=button","href="+PassChangeForm.HREF+"?id="+id+"&amp;ns="+ns)
                                                            .text("Add")
                                                            .end();
                                                    }
                                                    first=false;
                                                    hgen.end().leaf(HTMLGen.TD,cls,STYLE_WIDTH_70)
                                                        .text(Chrono.niceDateStamp(u.getExpires()))
                                                        .end();
                                        
                                                    hgen.end(uRow);
                                                }
                                            }
                                            if (xcnt>0) { // print compilations, if any, of Certificate
                                                hgen.leaf(uRow,HTMLGen.TR)
                                                    .leaf(HTMLGen.TD,cls="class=detailFirst",STYLE_WIDTH_10).text("x509").end()
                                                    .leaf(HTMLGen.TD, cls,STYLE_WIDTH_20)
                                                        .leaf(HTMLGen.A,"class=button","href="+CMArtifactShow.HREF+"?id="+id+"&amp;ns="+ns)
                                                            .text("View All")
                                                            .end(2)
                                                    .leaf(HTMLGen.TD, cls,STYLE_WIDTH_70).text(String.format(
                                                            xcnt>0?"%d Certificate%s, ranging from %s to %s"
                                                                  :"%d Certificate%s",
                                                            xcnt,
                                                            xcnt==1?"":"s",
                                                            Chrono.niceDateStamp(oldest),
                                                            Chrono.niceDateStamp(newest)))
                                                    .end(uRow);

                                            }
                                         
                                        }
                                        hgen.end(utable);
                                    }
                                    
                                hgen.end(div);

                                rv.add(new AbsCell[] {
                                        new TextCell(ulm.getKey(),STYLE_WIDTH_15), 
                                        new TextCell(buttons.toString(),STYLE_WIDTH_5),
                                        new TextCell(creds.toString(),STYLE_WIDTH_70)
                                    });
                            }

                            for (String missing : lns) {
                                StringWriter buttons = new StringWriter();
                                HTMLGen hgen = cd.clone(buttons);
                                hgen.leaf(HTMLGen.A,"class=button","href="+CMArtifactShow.HREF+"?id="+missing+"&amp;ns="+ns)
                                    .text("View All")
                                    .end(2);
                                rv.add(new AbsCell[] {
                                        new TextCell(missing,STYLE_WIDTH_15),
                                        new TextCell(buttons.toString(),STYLE_WIDTH_5),
                                        new TextCell("No X509 Credential Instantiated")
                                });
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

        @Override
        public void postfix(AAF_GUI state, AuthzTrans trans, final Cache<HTMLGen> cache, final HTMLGen hgen) {
        }


    }
}
