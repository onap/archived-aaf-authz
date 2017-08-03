/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import static com.att.xgen.html.HTMLGen.A;
import static com.att.xgen.html.HTMLGen.TABLE;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;

import com.att.authz.env.AuthzTrans;
import com.att.authz.gui.AuthGUI;
import com.att.authz.gui.BreadCrumbs;
import com.att.authz.gui.NamedCode;
import com.att.authz.gui.Page;
import com.att.cadi.CadiException;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cadi.client.Retryable;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.Slot;
import com.att.inno.env.TimeTaken;
import com.att.xgen.Cache;
import com.att.xgen.DynamicCode;
import com.att.xgen.Mark;
import com.att.xgen.html.HTMLGen;

import aaf.v2_0.Nss;
import aaf.v2_0.Nss.Ns;
import aaf.v2_0.Nss.Ns.Attrib;

public class NsInfoForm extends Page {
	// Package on purpose
	static final String HREF = "/gui/onboard";
	static final String NAME = "Onboarding";
	static final String fields[] = {"ns","description","mots","owners","admins"};
	
	public NsInfoForm(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env,NAME,HREF, fields,
			new BreadCrumbs(breadcrumbs),
			new NamedCode(true,"content") {

			private final Slot sID = gui.env.slot(NsInfoForm.NAME+'.'+NsInfoForm.fields[0]);
			@Override
			public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
				// p tags not closing right using .p() - causes issues in IE8 password form - so using leaf for the moment
				hgen.leaf(HTMLGen.H2).text("Namespace Info").end()
				     .leaf("p").text("Hover over Fields for Tool Tips, or click ")
				     	.leaf(A,"href="+gui.env.getProperty("aaf_url.gui_onboard","")).text("Here").end()
				     	.text(" for more information")
				     .end()
					.incr("form","method=post");
				Mark table = new Mark(TABLE);
				hgen.incr(table);
				cache.dynamic(hgen, new DynamicCode<HTMLGen, AuthGUI, AuthzTrans>() {
					@SuppressWarnings("unchecked")
					@Override
					public void code(final AuthGUI gui, AuthzTrans trans, Cache<HTMLGen> cache, HTMLGen hgen)	throws APIException, IOException {
						final String incomingID= trans.get(sID, "");
						final String[] info = new String[fields.length];
						final Object own_adm[] = new Object[2]; 
						for(int i=0;i<info.length;++i) {
							info[i]="";
						}
						if(incomingID.length()>0) {
							TimeTaken tt = trans.start("AAF Namespace Info",Env.REMOTE);
							try {
								gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
									@Override
									public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
										Future<Nss> fn = client.read("/authz/nss/"+incomingID,gui.nssDF);
										if(fn.get(AuthGUI.TIMEOUT)) {
											for(Ns ns : fn.value.getNs()) {
												info[0]=ns.getName();
												info[1]=ns.getDescription();
												for(Attrib attr: ns.getAttrib()) {
													switch(attr.getKey()) {
														case "mots":
															info[2]=attr.getValue();
														default:
													}
												}
												own_adm[0]=ns.getResponsible();
												own_adm[1]=ns.getAdmin();
											}
										} else {
											trans.error().log(fn.body());
										}
										return null;
									}
								});
							} catch (Exception e) {
								trans.error().log("Unable to access AAF for NS Info",incomingID);
								e.printStackTrace();
							} finally {
								tt.done();
							}
						}
						hgen.input(fields[0],"Namespace",false,"value="+info[0],"title=AAF Namespace")
							.input(fields[1],"Description*",true,"value="+info[1],"title=Full Application Name, Tool Name or Group")
							.input(fields[2],"MOTS ID",false,"value="+info[2],"title=MOTS ID if this is an Application, and has MOTS");
						Mark endTD = new Mark(),endTR=new Mark();
						// Owners
						hgen.incr(endTR,HTMLGen.TR)
								.incr(endTD,HTMLGen.TD)
									.leaf("label","for="+fields[3]).text("Responsible Party")
								.end(endTD)
								.incr(endTD,HTMLGen.TD)
									.tagOnly("input","id="+fields[3],"title=Owner of App, must be an Non-Bargained Employee");
									if(own_adm[0]!=null) {
										for(String s : (List<String>)own_adm[0]) {
											hgen.incr("label",true).text(s).end();
										}
									}
							hgen.end(endTR);

							// Admins
							hgen.incr(endTR,HTMLGen.TR)
								.incr(endTD,HTMLGen.TD)
									.leaf("label","for="+fields[4]).text("Administrators")
								.end(endTD)
								.incr(endTD,HTMLGen.TD)
									.tagOnly("input","id="+fields[4],"title=Admins may be employees, contractors or mechIDs");
									if(own_adm[1]!=null) {
										for(String s : (List<String>)own_adm[1]) {
											hgen.incr(HTMLGen.P,true).text(s).end();
										}
									}
								hgen.end(endTR)
						.end();
					}
				});
				hgen.end();
				hgen.tagOnly("input", "type=submit", "value=Submit")
					.end();

			}
		});
	}

}
