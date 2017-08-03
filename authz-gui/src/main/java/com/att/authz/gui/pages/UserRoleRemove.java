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

public class UserRoleRemove extends Page {
	public static final String HREF = "/gui/urRemove";
	static final String NAME = "Remove User Role";
	static final String fields[] = {"user","role"};

	public UserRoleRemove(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env,NAME, HREF, fields,
				new BreadCrumbs(breadcrumbs),
				new NamedCode(true, "content") {
			@Override
			public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
				final Slot sUser = gui.env.slot(NAME+".user");
				final Slot sRole = gui.env.slot(NAME+".role");
				
				
				cache.dynamic(hgen, new DynamicCode<HTMLGen, AuthGUI, AuthzTrans>() {
					@Override
					public void code(AuthGUI gui, AuthzTrans trans,	Cache<HTMLGen> cache, HTMLGen hgen)	throws APIException, IOException {						
						final String user = trans.get(sUser, "");
						final String role = trans.get(sRole, "");

						TimeTaken tt = trans.start("Request a user role delete",Env.REMOTE);
						try {
							gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
								@Override
								public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
									Future<Void> fv = client.setQueryParams("request=true").delete(
												"/authz/userRole/"+user+"/"+role,Void.class);
									
									if(fv.get(5000)) {
										// not sure if we'll ever hit this
										hgen.p("User ["+ user+"] Removed from Role [" +role+"]");
									} else {
										if (fv.code() == 202 ) {
											hgen.p("User ["+ user+"] Removal from Role [" +role+"] sent for Approval");
										} else {
											gui.writeError(trans, fv, hgen);
										}
									}
									return null;
								}
							});
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							tt.done();
						}
					}
				});
			}
			
		});
	}
}
