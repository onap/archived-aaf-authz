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

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.NamedCode;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.OrganizationFactory;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.util.FQI;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.html.HTMLGen;

import certman.v1_0.Artifacts;
import certman.v1_0.Artifacts.Artifact;

public class CMArtiChangeForm extends Page {
    private static final String COPY_ARTIFACT = "copyArtifact";
    private static final String DELETE_ARTIFACT = "deleteArtifact";

    // Package on purpose
    static final String HREF = "/gui/artichange";
    static final String NAME = "ArtifactChange";
    static final String fields[] = {"id","machine","ns","directory","ca","osuser","renewal","notify","cmd","others","types[]","sans"};

    static final String types[] = {"pkcs12","jks","file","script"};
    static final String UPDATE = "Update";
    static final String CREATE = "Create";
    static final String COPY = "Copy";
    static final String DELETE = "Delete";

    public CMArtiChangeForm(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env,NAME,HREF, fields,
            new BreadCrumbs(breadcrumbs),
            new NamedCode(true,"content") {
            private final Slot sID = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[0]);
            private final Slot sMach = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[1]);
            private final Slot sNS = gui.env.slot(CMArtiChangeForm.NAME+'.'+CMArtiChangeForm.fields[2]);
        
            @Override
            public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                Mark js = new Mark();
                Mark fn = new Mark();
                hgen.js(js).function(fn,COPY_ARTIFACT)
                    .text("f=document.getElementById('"+fields[9]+"')")
                    .text("s=document.getElementById('theButton')")
                    .text("cmd=document.getElementById('"+fields[8]+"')")
                    .text("ins=document.getElementById('instruct')")
                    .text("c=document.getElementById('cbcopy')")
                    .text("trd=document.getElementById('trdelete')")
                    .li("if (c.checked==true) {" ,
                            "f.style.display=ins.style.display='block'",
                            "trd.style.display='none'",
                            "s.orig=s.value;",
                            "s.value='Copy'",
                            "cmd.setAttribute('value',s.value)",
                          "} else {",
                            "f.style.display=ins.style.display='none';",
                            "trd.style.display='block'",
                            "s.value=s.orig",
                            "cmd.setAttribute('value',s.orig)",
                            "}"
                            )
                    .end(fn)
                    .function(fn, DELETE_ARTIFACT)
                        .text("d=document.getElementById('cbdelete')")
                        .text("trc=document.getElementById('trcopy')")
                        .text("s=document.getElementById('theButton')")
                        .text("cmd=document.getElementById('"+fields[8]+"')")
                        .li("if (d.checked==true) {",
                              "s.orig=s.value;",
                              "s.value='Delete';",
                              "trc.style.display='none';",
                              "cmd.setAttribute('value',s.value);",
                            "} else {",
                              "s.value=s.orig;",
                              "trc.style.display='block';",
                              "cmd.setAttribute('value',s.orig);",
                            "}"
                            )
                    .end(js);

                hgen.leaf(HTMLGen.TITLE).text("Certificate Artifact Form").end();
                Mark form = new Mark();
                hgen.incr(form, "form","action="+HREF,"method=post");
            
                cache.dynamic(hgen, new DynamicCode<HTMLGen, AAF_GUI, AuthzTrans>() {
                    @Override
                    public void code(final AAF_GUI gui, final AuthzTrans trans,    final Cache<HTMLGen> cache, final HTMLGen hgen)    throws APIException, IOException {

                        final String incomingMach = trans.get(sMach,"");
                        String incomingNS = trans.get(sNS,"");
                        String id= trans.get(sID, "");
                    final String incomingID = id.indexOf('@')>=0?id:id+'@'+FQI.reverseDomain(incomingNS);

                        String submitText=UPDATE;
                        boolean delete=true;
                        try {
                            Artifact arti =gui.cmClientAsUser(trans.getUserPrincipal(), new Retryable<Artifact>() {
                                @Override
                                public Artifact code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                                    Future<Artifacts> fa = client.read("/cert/artifacts/"+incomingID+'/'+incomingMach, gui.artifactsDF);
                                    if (fa.get(AAFcli.timeout())) {
                                        for (Artifact arti : fa.value.getArtifact()) {
                                            return arti; // just need the first one
                                        }
                                    }
                                    return null;
                                }
                            });
                            if (arti==null) {
                                Organization org = OrganizationFactory.get(trans);
                                Identity user = org.getIdentity(trans, incomingID);
                                if (user==null) {
                                    hgen.p("The mechID you typed, \"" + incomingID + "\", is not a valid " + org.getName() + " ID");
                                    return;
                                }
                                arti = new Artifact();
                                arti.setMechid(incomingID);
                                Identity managedBy = user.responsibleTo();
                                if (managedBy == null) {
                                    arti.setSponsor("Unknown Sponsor");
                                } else {
                                    arti.setSponsor(managedBy.fullID());
                                }
                                arti.setMachine(incomingMach);
                                arti.setNs(incomingNS);
                                arti.setDir("");
                                arti.setCa("aaf");
                                arti.setOsUser("");
                                arti.setRenewDays(30);
                                arti.setNotification("mailto:"+user.email());
                                arti.getType().add(types[0]);
                                arti.getType().add(types[3]);
                                submitText = CREATE;
                                delete = false;
                            } else {
                                if (arti.getNotification()==null) {
                                    Organization org = OrganizationFactory.get(trans);
                                    Identity user = org.getIdentity(trans, incomingID);
                                    arti.setNotification("mailto:"+user.email());
                                }
                            }
                            Mark table = new Mark(TABLE);
                            hgen.incr(table)
                                .input(fields[0],"AppID*",true,"value="+arti.getMechid())
                                .input("sponsor", "Sponsor",false,"value="+arti.getSponsor(),"readonly","style=border:none;background-color:white;")
                                .input(fields[1],"FQDN*",true,"value="+arti.getMachine(),"style=width:130%;");
//                            if (maySans) {
                                hgen.incr(HTMLGen.TR).incr(HTMLGen.TD).end()
                                    .incr(HTMLGen.TD,"class=subtext").text("Use Fully Qualified Domain Names (that will be in DNS), ");
                                    if (!trans.fish(getPerm(arti.getCa(),"ip"))) {
                                        hgen.text("NO ");
                                    }
                                StringBuilder sb = null;
                                for (String s: arti.getSans()) {
                                    if (sb==null) {
                                        sb = new StringBuilder();
                                    } else {
                                        sb.append(", ");
                                    }
                                    sb.append(s);
                                }
                            
                                hgen.text("IPs allowed, separated by commas.").end()
                                    .input(fields[11], "SANs", false, "value="+(sb==null?"":sb.toString()),"style=width:130%;");
//                            }
                            hgen.input(fields[2],"Namespace",true,"value="+arti.getNs(),"style=width:130%;")
                                .input(fields[3],"Directory", true, "value="+arti.getDir(),"style=width:130%;")
                                .input(fields[4],"Certificate Authority",true,"value="+arti.getCa(),"style=width:130%;")
                                .input(fields[5],"O/S User",true,"value="+arti.getOsUser())
                                .input(fields[6],"Renewal Days before Expiration", true, "value="+arti.getRenewDays(),"style=width:20%;")
                                .input(fields[7],"Notification",true,"value="+arti.getNotification())
                                .incr(HTMLGen.TR)
                                .incr(HTMLGen.TD).leaf("label","for=types","required").text("Artifact Types").end(2)
                                .incr(HTMLGen.TD);
                            for (int i=0;i<types.length;++i) {
                                hgen.leaf("input","type=checkbox","name=types."+i,arti.getType().contains(types[i])?"checked":"").text(types[i]).end().br();
                            }
                        
                            Mark tr = new Mark();
                            hgen.incr(tr,HTMLGen.TR).incr(HTMLGen.TD,"id=trcopy")
                                    .leaf("input","id=cbcopy","type=checkbox","onclick="+COPY_ARTIFACT+"()").text("Copy Artifact").end(2)
                                .incr(HTMLGen.TD,"id=tdcopy","style:display:none;")
                                    .incr("label","id=instruct","style=font-style:italic;font-size:80%;display:none;")
                                        .text("Add full machine names, separated by commas.").end()
                                    .tagOnly("input","id="+fields[9],"name="+fields[9],"style=display:none;width:150%;").end(2)
                                .end(tr);
                            hgen.incr(tr,HTMLGen.TR,"id=trdelete").incr(HTMLGen.TD,"id=tddelete")
                                .leaf("input","id=cbdelete","type=checkbox","onclick="+DELETE_ARTIFACT+"()",delete?"style:display:none;":"").text("Delete Artifact").end(2)
                                .end(tr);
                            hgen.end(table);
                        
                            hgen.tagOnly("input","id="+fields[8],"name="+fields[8],"value="+submitText,"style=display:none;");
                            hgen.tagOnly("input","id=theButton","type=submit", "orig="+submitText,"value="+submitText);
                        
                        } catch (CadiException | LocatorException | OrganizationException e) {
                            throw new APIException(e);
                        }
                    }

                    });
                hgen.end(form);
                }
            });
    
    }
}
