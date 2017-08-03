/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import java.io.IOException;

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

import aaf.v2_0.Approval;
import aaf.v2_0.Approvals;

public class ApprovalAction extends Page {
	public ApprovalAction(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env,"Approvals",ApprovalForm.HREF, ApprovalForm.FIELDS,
			new BreadCrumbs(breadcrumbs),
			new NamedCode(true,"content") {
				final Slot sAppr = gui.env.slot(ApprovalForm.NAME+'.'+ApprovalForm.FIELDS[0]);
				final Slot sUser = gui.env.slot(ApprovalForm.NAME+'.'+ApprovalForm.FIELDS[1]);
				
				@Override
				public void code(Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {				
					cache.dynamic(hgen, new DynamicCode<HTMLGen,AuthGUI, AuthzTrans>() {
						@Override
						public void code(final AuthGUI gui, final AuthzTrans trans,Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
							boolean fail = true;
							String[] appr = trans.get(sAppr,null);
							String user = trans.get(sUser,null);
							String lastPage = ApprovalForm.HREF;
							if (user != null) {
								lastPage += "?user="+user;
							}
							
							if(appr==null) {
								hgen.p("No Approvals have been selected.");
							} else {
								Approval app;
								final Approvals apps = new Approvals();
								int count = 0;
								for(String a : appr) {
									if(a!=null) {
										int idx = a.indexOf('|');
										if(idx>=0) {
											app = new Approval();
											app.setStatus(a.substring(0,idx));
											app.setTicket(a.substring(++idx));
											app.setApprover(trans.getUserPrincipal().getName());
											apps.getApprovals().add(app);
											++count;
										}
									}
								}
								if(apps.getApprovals().isEmpty()) {
									hgen.p("No Approvals have been sent.");
								} else {
									TimeTaken tt = trans.start("AAF Update Approvals",Env.REMOTE);
									try {
										final int total = count;
										fail = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Boolean>() {
											@Override
											public Boolean code(Rcli<?> client) throws APIException, CadiException  {
												boolean fail2 = true;
												Future<Approvals> fa = client.update("/authz/approval",gui.approvalsDF,apps);
												if(fa.get(AuthGUI.TIMEOUT)) {
													// Do Remote Call
													fail2 = false;
													hgen.p(total + (total==1?" Approval has":" Approvals have") + " been Saved");
												} else {
													gui.writeError(trans, fa, hgen);
												}
												return fail2;
											}
										});
									} catch (Exception e) {
										e.printStackTrace();
									} finally {
										tt.done();
									}
								}

							hgen.br();
							if(fail) {
								hgen.incr("a",true,"href="+lastPage).text("Try again").end();
							} else {
								hgen.incr("a",true,"href="+Home.HREF).text("Home").end(); 
							}
							}
						}
					});
				}
			});
	}
}
