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
import java.io.StringWriter;
import java.net.ConnectException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.gui.SlotCode;
import org.onap.aaf.auth.gui.Table;
import org.onap.aaf.auth.gui.Table.Cells;
import org.onap.aaf.auth.gui.table.AbsCell;
import org.onap.aaf.auth.gui.table.TextCell;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.configure.Factory;
import org.onap.aaf.cadi.util.FQI;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.html.HTMLGen;

import certman.v1_0.Artifacts;
import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.CertInfo;

public class CMArtifactShow extends Page {

    public static final String HREF = "/gui/cmarti";
    public static final String NAME = "ArtifactsShow";
    private static ArtiTable arti;
    public static SlotCode<AuthzTrans> slotCode;
    private enum Params{id,ns};


    public CMArtifactShow(final AAF_GUI gui, Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env, NAME, HREF, Params.values() , 
                new BreadCrumbs(breadcrumbs),
                arti = new ArtiTable(gui.env)
                );
        // Setting so we can get access to HTMLGen clone and Slots
        arti.set(this,slotCode);
    }

    private static class ArtiTable extends Table<AAF_GUI, AuthzTrans> {
        private static Model model;
        private SlotCode<AuthzTrans> sc;
        enum Params {id,ns};
        public ArtiTable(AuthzEnv env) {
            super((String)null,env.newTransNoAvg(),model = new Model(),
                    slotCode = new SlotCode<AuthzTrans>(false,env,NAME,Params.values()) {
                        @Override
                        public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                            cache.dynamic(hgen, new DynamicCode<HTMLGen,AAF_GUI,AuthzTrans>() {
                            @Override
                            public void code(AAF_GUI state, AuthzTrans trans, final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                                Mark js = new Mark();
                                hgen.js(js).function("newArtifact")
                                .text("machine=document.getElementById('machine');")
                                .text("window.open('"
                                        +CMArtiChangeForm.HREF+
                                        "?id="+get(trans, Params.id,"")+
                                        "&ns="+get(trans, Params.ns,"")+
                                        "&machine='+machine.value,'_self');"
                                    ).end(js);
                                hgen.leaf("input","id=machine","style=margin:1em 1em 1em 1em;width:30%").end();
                                hgen.leaf(HTMLGen.A,"class=greenbutton","href=javascript:newArtifact()","style=color:white;").text("New FQDN").end();
                            }
                        });
                        }
                    },"class=std");
        }
    

        public void set(CMArtifactShow cmArtifactShow, SlotCode<AuthzTrans> sc) {
            this.sc = sc;
            model.set(cmArtifactShow,sc);
        }
    
        @Override
        protected String title(AuthzTrans trans) {
            StringBuilder sb = new StringBuilder("X509 Certificates");
            if (sc!=null) { // initialized
                sb.append(" for ");
                String id = sc.get(trans,Params.id,"");
                sb.append(id);
                if (id.indexOf('@')<0) {
                    sb.append('@');
                    sb.append(FQI.reverseDomain(sc.get(trans, Params.ns,"missingDomain")));
                }
            }
            return sb.toString();
        }
    }
    /**
     * Implement the table content for Cred Detail
     * <p>
     * @author Jeremiah
     *
     */
    private static class Model implements Table.Data<AAF_GUI,AuthzTrans> {
        private CMArtifactShow cas;
        private SlotCode<AuthzTrans> sc;

        // Covering for Constructor Order
        private void set(CMArtifactShow cas, SlotCode<AuthzTrans> sc) {
            this.cas = cas;
            this.sc = sc;
        }

        private static final String[] headers = new String[]{"FQDN","Directory","CA","Renews","Expires",""};
        @Override
        public String[] headers() {
            return headers;
        }
    
        @Override
        public Cells get(final AuthzTrans trans, final AAF_GUI gui) {
            String str = sc.get(trans,Params.id, null);
            if (str==null) {
                return Cells.EMPTY;
            }
            final String id = str.indexOf('@')>=0?str:str + '@' + FQI.reverseDomain(sc.get(trans,Params.ns, ""));
            final ArrayList<AbsCell[]> rv = new ArrayList<>();
            final TimeTaken tt = trans.start("AAF X509 Details",Env.REMOTE);
            try {
                gui.cmClientAsUser(trans.getUserPrincipal(),new Retryable<Void>() {
                    @Override
                    public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                        Future<CertInfo>  fuCI = client.read("/cert/id/"+id,gui.certInfoDF);
                        Future<Artifacts> fuArt = client.read("/cert/artifacts?mechid="+id, gui.artifactsDF);
                    
                        X509Certificate[] lc;
                        if (fuCI.get(AAFcli.timeout())) {
                            TimeTaken tt1 = trans.start("x509Certificate", Env.SUB);
                            try {
                                Collection<? extends Certificate> xcs = Factory.toX509Certificate(fuCI.value.getCerts());
                                 lc = new X509Certificate[xcs.size()];
                                xcs.toArray(lc);
                            } catch (CertificateException e) {
                                trans.error().log(e,"Bad Certificate entry");
                                throw new CadiException(e);
                            } finally {
                                tt1.done();
                            }
                        } else {
                            lc = null;
                            trans.error().log("Cannot retrieve Certificates for " + id);
                        }
                        if (fuArt.get(AAFcli.timeout())) {
                            for (Artifact arti : fuArt.value.getArtifact()) {
                                StringWriter sw = new StringWriter();
                                HTMLGen hgen = cas.clone(sw);
                                Mark mark = new Mark();
                                hgen.leaf(HTMLGen.A,"class=button",
                                        "href="+CMArtiChangeForm.HREF+"?id="+arti.getMechid() +"&machine="+arti.getMachine()+"&ns="+arti.getNs())
                                        .text("Details")
                                    .end(mark);
                                Date last = null;
                                if (lc!=null) {
                                    for (X509Certificate xc : lc) {
                                        if (xc.getSubjectDN().getName().contains("CN="+arti.getMachine())) {
                                            if (last==null || last.before(xc.getNotAfter())) {
                                                last = xc.getNotAfter();
                                            }
                                        }
                                    }
                                }
                                GregorianCalendar renew;
                                if (last!=null) {
                                    renew = new GregorianCalendar();
                                    renew.setTime(last);
                                    renew.add(GregorianCalendar.DAY_OF_MONTH,arti.getRenewDays()*-1);
                                } else {
                                    renew = null;
                                }

                                rv.add(new AbsCell[] {
                                    new TextCell(arti.getMachine(),"style=width:20%;"), 
                                    new TextCell(arti.getDir(),"style=width:25%;"),
                                    new TextCell(arti.getCa(),"style=width:2%;text-align:center;"),
                                    new TextCell(renew==null?
                                            arti.getRenewDays().toString() + " days before Exp":
                                            Chrono.dateOnlyStamp(renew),"style=width:6%;text-align:center;"),
                                    new TextCell(last==null?"None Deployed":Chrono.dateOnlyStamp(last),"style=width:5%;text-align:center;"),
                                    new TextCell(sw.toString(),"style=width:10%;text-align:center;")
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
        public void prefix(AAF_GUI state, AuthzTrans trans, final Cache<HTMLGen> cache, final HTMLGen hgen) {
        }

        @Override
        public void postfix(AAF_GUI state, AuthzTrans trans, final Cache<HTMLGen> cache, final HTMLGen hgen) {
        }

    }

}
