/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import com.att.authz.gui.AuthGUI;
import com.att.authz.gui.BreadCrumbs;
import com.att.authz.gui.Form;
import com.att.authz.gui.NamedCode;
import com.att.authz.gui.Page;
import com.att.authz.gui.Table;
import com.att.authz.gui.Table.Cells;
import com.att.authz.gui.table.AbsCell;
import com.att.authz.gui.table.ButtonCell;
import com.att.authz.gui.table.RadioCell;
import com.att.authz.gui.table.RefCell;
import com.att.authz.gui.table.TextAndRefCell;
import com.att.authz.gui.table.TextCell;
import com.att.authz.org.Organization;
import com.att.authz.org.Organization.Identity;
import com.att.authz.org.OrganizationFactory;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Env;
import org.onap.aaf.inno.env.Slot;
import org.onap.aaf.inno.env.TimeTaken;
import com.att.xgen.Cache;
import com.att.xgen.DynamicCode;
import com.att.xgen.Mark;
import com.att.xgen.html.HTMLGen;

import aaf.v2_0.Approval;

public class ApprovalForm extends Page {
	// Package on purpose
	static final String NAME="Approvals";
	static final String HREF = "/gui/approve";
	static final String WEBPHONE = "http://webphone.att.com/cgi-bin/webphones.pl?id=";
	static final String[] FIELDS = new String[] {"line[]","user"};
	
	
	public ApprovalForm(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env,NAME,HREF, FIELDS,

			new BreadCrumbs(breadcrumbs),
			new NamedCode(false, "filterByUser") {
				@Override
				public void code(Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
					cache.dynamic(hgen, new DynamicCode<HTMLGen, AuthGUI, AuthzTrans>() {
						@Override
						public void code(AuthGUI gui, AuthzTrans trans,	Cache<HTMLGen> cache, HTMLGen hgen)	throws APIException, IOException {
							String user = trans.get(trans.env().slot(NAME+".user"),"");
							hgen.incr("p", "class=userFilter")
								.text("Filter by User:")
								.tagOnly("input", "type=text", "value="+user, "id=userTextBox")
								.tagOnly("input", "type=button", "onclick=userFilter('"+HREF+"');", "value=Go!")
								.end();
								}
					});
				}
			},
			new Form(true,new Table<AuthGUI,AuthzTrans>("Approval Requests", gui.env.newTransNoAvg(),new Model(gui.env()),"class=stdform"))
				.preamble("The following requires your Approval to proceed in the AAF System.</p><p class=subtext>Hover on Identity for Name; click for WebPhone"),
			new NamedCode(false, "selectAlljs") {
				@Override
				public void code(Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
					Mark jsStart = new Mark();
					hgen.js(jsStart);
					hgen.text("function selectAll(radioClass) {");
					hgen.text("var radios = document.querySelectorAll(\".\"+radioClass);");
					hgen.text("for (i = 0; i < radios.length; i++) {");
					hgen.text("radios[i].checked = true;");
					hgen.text("}");
					hgen.text("}");
					hgen.end(jsStart);
				}
			});
		
	}
	
	/**
	 * Implement the Table Content for Approvals
	 * 
	 *
	 */
	private static class Model implements Table.Data<AuthGUI,AuthzTrans> {
		private static final String[] headers = new String[] {"Identity","Request","Approve","Deny"};
		private static final Object THE_DOMAIN = null;
		private Slot sUser;
		
		public Model(AuthzEnv env) {
			sUser = env.slot(NAME+".user");
		}
		
		@Override
		public String[] headers() {
			return headers;
		}
		
		@Override
		public Cells get(final AuthGUI gui, final AuthzTrans trans) {
			final String userParam = trans.get(sUser, null);
			ArrayList<AbsCell[]> rv = new ArrayList<AbsCell[]>();
			String msg = null;
			TimeTaken tt = trans.start("AAF Get Approvals for Approver",Env.REMOTE);
			try {
				final List<Approval> pendingApprovals = new ArrayList<Approval>();
				final List<Integer> beginIndicesPerApprover = new ArrayList<Integer>();
				int numLeft = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Integer>() {
					@Override
					public Integer code(Rcli<?> client) throws CadiException, ConnectException, APIException {
						Future<aaf.v2_0.Approvals> fa = client.read("/authz/approval/approver/"+trans.user(),gui.approvalsDF);
						int numLeft = 0;
						if(fa.get(AuthGUI.TIMEOUT)) {
							
							if(fa.value!=null) {
								for (Approval appr : fa.value.getApprovals()) {
									if (appr.getStatus().equals("pending")) {
										if (userParam!=null) {
											if (!appr.getUser().equalsIgnoreCase(userParam)) {
												numLeft++;
												continue;
											}
										}
										pendingApprovals.add(appr);
									}
								}
							}
							
							String prevApprover = null;
							int overallIndex = 0;
								
							for (Approval appr : pendingApprovals) {
								String currApprover = appr.getApprover();
								if (!currApprover.equals(prevApprover)) {
									prevApprover = currApprover;
									beginIndicesPerApprover.add(overallIndex);
								}
								overallIndex++;
							}
						}
						return numLeft;
					}
				});
				
				if (pendingApprovals.size() > 0) {
					// Only add select all links if we have approvals
					AbsCell[] selectAllRow = new AbsCell[] {
							AbsCell.Null,
							AbsCell.Null,
							new ButtonCell("all", "onclick=selectAll('approve')", "class=selectAllButton"),
							new ButtonCell("all", "onclick=selectAll('deny')", "class=selectAllButton")
						};
					rv.add(selectAllRow);
				}
						
				int line=-1;
				
				while (beginIndicesPerApprover.size() > 0) {
					int beginIndex = beginIndicesPerApprover.remove(0);
					int endIndex = (beginIndicesPerApprover.isEmpty()?pendingApprovals.size():beginIndicesPerApprover.get(0));
					List<Approval> currApproverList = pendingApprovals.subList(beginIndex, endIndex);
					
					String currApproverFull = currApproverList.get(0).getApprover();
					String currApproverShort = currApproverFull.substring(0,currApproverFull.indexOf('@'));
					String currApprover = (trans.user().indexOf('@')<0?currApproverShort:currApproverFull);
					if (!currApprover.equals(trans.user())) {
						AbsCell[] approverHeader;
						if (currApproverFull.substring(currApproverFull.indexOf('@')).equals(THE_DOMAIN)) {
							approverHeader = new AbsCell[] { 
									new TextAndRefCell("Approvals Delegated to Me by ", currApprover,
											WEBPHONE + currApproverShort, 
											new String[] {"colspan=4", "class=head"})
							};
						} else {
							approverHeader = new AbsCell[] { 
									new TextCell("Approvals Delegated to Me by " + currApprover,
											new String[] {"colspan=4", "class=head"})
							};
						}
						rv.add(approverHeader);
					}
					
					// Sort by User Requesting
					Collections.sort(currApproverList, new Comparator<Approval>() {
						@Override
						public int compare(Approval a1, Approval a2) {
							return a1.getUser().compareTo(a2.getUser());
						}
					});
					
					String prevUser = null;
					for (Approval appr : currApproverList) {
						if(++line<MAX_LINE) { // limit number displayed at one time.
							AbsCell userCell;
							String user = appr.getUser();
							if(user.equals(prevUser)) {
								userCell = AbsCell.Null; 
							} else {
								String title;
								Organization org = OrganizationFactory.obtain(trans.env(), user);
								if(org==null) {
									title="";
								} else {
									Identity au = org.getIdentity(trans, user);
									if(au!=null) {
										if(au.type().equals("MECHID")) {
											title="title=Sponsor is " + au.responsibleTo();
										} else {
											title="title=" + au.fullName();
										}
									} else {
										title="";
									}
								}
								userCell = new RefCell(prevUser=user, 
									"" //TODO add Organization Link ability
									,title);
							}
							AbsCell[] sa = new AbsCell[] {
								userCell,
								new TextCell(appr.getMemo()),
								new RadioCell("line"+ line,"approve", "approved|"+appr.getTicket()),
								new RadioCell("line"+ line,"deny", "denied|"+appr.getTicket())
							};
							rv.add(sa);
						} else {
							++numLeft;
						}
					}
				}
				if(numLeft>0) {
					msg = "After these, there will be " + numLeft + " approvals left to process";
				}
				if(rv.size()==0) {
					if (numLeft>0) {
						msg = "No Approvals to process at this time for user " + userParam +". You have " 
							+ numLeft + " other approvals to process.";
					} else {
						msg = "No Approvals to process at this time";
					}
				}
			} catch (Exception e) {
				trans.error().log(e);
			} finally {
				tt.done();
			}
		return new Cells(rv,msg);
		}
	}
}
