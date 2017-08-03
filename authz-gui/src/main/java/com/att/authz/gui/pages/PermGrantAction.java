/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import java.io.IOException;
import java.net.ConnectException;

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
import com.att.xgen.html.HTMLGen;

import aaf.v2_0.Pkey;
import aaf.v2_0.RolePermRequest;

public class PermGrantAction extends Page {
	
	
	public PermGrantAction(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env,PermGrantForm.NAME, PermGrantForm.HREF, PermGrantForm.fields,
			new BreadCrumbs(breadcrumbs),
			new NamedCode(true,"content") {
				final Slot sType = gui.env.slot(PermGrantForm.NAME+'.'+PermGrantForm.fields[0]);
				final Slot sInstance = gui.env.slot(PermGrantForm.NAME+'.'+PermGrantForm.fields[1]);
				final Slot sAction = gui.env.slot(PermGrantForm.NAME+'.'+PermGrantForm.fields[2]);
				final Slot sRole = gui.env.slot(PermGrantForm.NAME+'.'+PermGrantForm.fields[3]);
				
				@Override
				public void code(Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
					cache.dynamic(hgen, new DynamicCode<HTMLGen,AuthGUI, AuthzTrans>() {
						@Override
						public void code(final AuthGUI gui, final AuthzTrans trans,Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {

							String type = trans.get(sType,null);
							String instance = trans.get(sInstance,null);
							String action = trans.get(sAction,null);
							String role = trans.get(sRole,null);
							
							String lastPage = PermGrantForm.HREF 
									+ "?type=" + type + "&instance=" + instance + "&action=" + action;
							
							// Run Validations
							boolean fail = true;
						
							TimeTaken tt = trans.start("AAF Grant Permission to Role",Env.REMOTE);
							try {
								
								final RolePermRequest grantReq = new RolePermRequest();
								Pkey pkey = new Pkey();
								pkey.setType(type);
								pkey.setInstance(instance);
								pkey.setAction(action);
								grantReq.setPerm(pkey);
								grantReq.setRole(role);
								
								fail = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Boolean>() {
									@Override
									public Boolean code(Rcli<?> client) throws CadiException, ConnectException, APIException {
										boolean fail = true;
										Future<RolePermRequest> fgrant = client.create(
												"/authz/role/perm",
												gui.rolePermReqDF,
												grantReq
												);

										if(fgrant.get(5000)) {
											hgen.p("Permission has been granted to role.");
											fail = false;
										} else {
											if (202==fgrant.code()) {
												hgen.p("Permission Grant Request sent, but must be Approved before actualizing");
												fail = false;
											} else {
												gui.writeError(trans, fgrant, hgen);
											}
										}
										return fail;
									}
								});
							} catch (Exception e) {
								hgen.p("Unknown Error");
								e.printStackTrace();
							} finally {
								tt.done();
							}
								
							hgen.br();
							hgen.incr("a",true,"href="+lastPage);
							if (fail) {
								hgen.text("Try again");
							} else {
								hgen.text("Grant this Permission to Another Role");
							}
							hgen.end();
							hgen.js()
								.text("alterLink('permgrant', '"+lastPage + "');")							
								.done();

						}
					});
				}
			});
	}
}
