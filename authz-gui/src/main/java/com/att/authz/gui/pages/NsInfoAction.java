/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import java.io.IOException;
import java.net.ConnectException;
import java.text.ParseException;

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
import com.att.inno.env.util.Chrono;
import com.att.xgen.Cache;
import com.att.xgen.DynamicCode;
import com.att.xgen.html.HTMLGen;

import aaf.v2_0.CredRequest;

public class NsInfoAction extends Page {
	public NsInfoAction(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env,"Onboard",PassChangeForm.HREF, PassChangeForm.fields,
			new BreadCrumbs(breadcrumbs),
			new NamedCode(true,"content") {
				final Slot sID = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[0]);
				final Slot sCurrPass = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[1]);
				final Slot sPassword = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[2]);
				final Slot sPassword2 = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[3]);
				final Slot startDate = gui.env.slot(PassChangeForm.NAME+'.'+PassChangeForm.fields[4]);
				
				@Override
				public void code(Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
					cache.dynamic(hgen, new DynamicCode<HTMLGen,AuthGUI, AuthzTrans>() {
						@Override
						public void code(final AuthGUI gui, final AuthzTrans trans,Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
							String id = trans.get(sID,null);
							String currPass = trans.get(sCurrPass,null);
							String password = trans.get(sPassword,null);
							String password2 = trans.get(sPassword2,null);
							
							// Run Validations
							boolean fail = true;
							
							if (id==null || id.indexOf('@')<=0) {
								hgen.p("Data Entry Failure: Please enter a valid ID, including domain.");
							} else if(password == null || password2 == null || currPass == null) {
								hgen.p("Data Entry Failure: Both Password Fields need entries.");
							} else if(!password.equals(password2)) {
								hgen.p("Data Entry Failure: Passwords do not match.");
							} else { // everything else is checked by Server
								final CredRequest cred = new CredRequest();
								cred.setId(id);
								cred.setPassword(currPass);
								try {
									fail = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Boolean>() {
										@Override
										public Boolean code(Rcli<?> client)throws CadiException, ConnectException, APIException {
											TimeTaken tt = trans.start("Check Current Password",Env.REMOTE);
											try {
												Future<CredRequest> fcr = client.create( // Note: Need "Post", because of hiding password in SSL Data
															"/authn/validate",
															gui.credReqDF,
															cred
														);
												boolean go;
												boolean fail = true;
												fcr.get(5000);
												if(fcr.code() == 200) {
													hgen.p("Current Password validated");
													go = true;
												} else {
													hgen.p(String.format("Invalid Current Password: %d %s",fcr.code(),fcr.body()));
													go = false;
												}
												if(go) {
													tt.done();
													tt = trans.start("AAF Change Password",Env.REMOTE);
													try {
														// Change over Cred to reset mode
														cred.setPassword(password);
														String start = trans.get(startDate, null);
														if(start!=null) {
															try {
																cred.setStart(Chrono.timeStamp(Chrono.dateOnlyFmt.parse(start)));
															} catch (ParseException e) {
																throw new CadiException(e);
															}
														}
														
														fcr = client.create(
																"/authn/cred",
																gui.credReqDF,
																cred
																);
					
														if(fcr.get(5000)) {
															// Do Remote Call
															hgen.p("New Password has been added.");
															fail = false;
														} else {
															gui.writeError(trans, fcr, hgen);
														}
													} finally {
														tt.done();
													}
												}
 												return fail;
											} finally {
												tt.done();
											}
										}
									});

								} catch (Exception e) {
									hgen.p("Unknown Error");
									e.printStackTrace();
								}
							}
						hgen.br();
						if(fail) {
							hgen.incr("a",true,"href="+PassChangeForm.HREF+"?id="+id).text("Try again").end();
						} else {
							hgen.incr("a",true,"href="+Home.HREF).text("Home").end(); 
						}
					}
				});
			}
		});
	}
}
