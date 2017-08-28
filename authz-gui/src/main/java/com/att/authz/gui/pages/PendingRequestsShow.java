/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import java.io.IOException;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.att.authz.env.AuthzTrans;
import com.att.authz.gui.AuthGUI;
import com.att.authz.gui.BreadCrumbs;
import com.att.authz.gui.NamedCode;
import com.att.authz.gui.Page;
import com.att.authz.gui.Table;
import com.att.authz.gui.Table.Cells;
import com.att.authz.gui.table.AbsCell;
import com.att.authz.gui.table.RefCell;
import com.att.authz.gui.table.TextCell;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Env;
import org.onap.aaf.inno.env.TimeTaken;
import com.att.xgen.Cache;
import com.att.xgen.DynamicCode;
import com.att.xgen.html.HTMLGen;

import aaf.v2_0.Approval;
import aaf.v2_0.Approvals;

public class PendingRequestsShow extends Page {
	public static final String HREF = "/gui/myrequests";
	public static final String NAME = "MyRequests";
	static final String WEBPHONE = "http://webphone.att.com/cgi-bin/webphones.pl?id=";
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd";
	
	public PendingRequestsShow(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env, NAME,HREF, NO_FIELDS,
			new BreadCrumbs(breadcrumbs), 
			new NamedCode(true,"expedite") {
			@Override
			public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
				cache.dynamic(hgen, new DynamicCode<HTMLGen, AuthGUI, AuthzTrans>() {
					@Override
					public void code(AuthGUI gui, AuthzTrans trans,	Cache<HTMLGen> cache, HTMLGen hgen)	throws APIException, IOException {
						hgen
							.leaf("p", "class=expedite_request").text("These are your submitted Requests that are awaiting Approval. ")
							.br()
							.text("To Expedite a Request: ")
							.leaf("a","href=#expedite_directions","onclick=divVisibility('expedite_directions');")
								.text("Click Here").end()
							.divID("expedite_directions", "style=display:none");
						hgen
							.incr(HTMLGen.OL)
							.incr(HTMLGen.LI)
							.leaf("a","href="+ApprovalForm.HREF+"?user="+trans.user(), "id=userApprove")
							.text("Copy This Link")
							.end()
							.end()
							.incr(HTMLGen.LI)
							.text("Send it to the Approver Listed")
							.end()
							.end()
							.text("NOTE: Using this link, the Approver will only see your requests. You only need to send this link once!")
							.end()
							.end();
					}
				});
			}
		},
			new Table<AuthGUI,AuthzTrans>("Pending Requests",gui.env.newTransNoAvg(),new Model(), "class=std")
		);
					

	}

	/**
	 * Implement the Table Content for Requests by User
	 * 
	 *
	 */
	private static class Model implements Table.Data<AuthGUI,AuthzTrans> {
		private static final String CSP_ATT_COM = "@csp.att.com";
		final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;
		private static final String[] headers = new String[] {"Request Date","Status","Memo","Approver"};

		@Override
		public String[] headers() {
			return headers;
		}
		
		@Override
		public Cells get(final AuthGUI gui, final AuthzTrans trans) {
			DateFormat createdDF = new SimpleDateFormat(DATE_TIME_FORMAT);
			ArrayList<AbsCell[]> rv = new ArrayList<AbsCell[]>();
			try {
				gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
					@Override
					public Void code(Rcli<?> client)throws CadiException, ConnectException, APIException {
						TimeTaken tt = trans.start("AAF Get Approvals by User",Env.REMOTE);
						try {
							Future<Approvals> fa = client.read("/authz/approval/user/"+trans.user(),gui.approvalsDF);
							if(fa.get(5000)) {
								tt.done();
								tt = trans.start("Load Data", Env.SUB);
								if(fa.value!=null) {
									List<Approval> approvals = fa.value.getApprovals();
									Collections.sort(approvals, new Comparator<Approval>() {
										@Override
										public int compare(Approval a1, Approval a2) {
											UUID id1 = UUID.fromString(a1.getId());
											UUID id2 = UUID.fromString(a2.getId());
											return id1.timestamp()<=id2.timestamp()?1:-1;
										}
									});
									
									String prevTicket = null;
									for(Approval a : approvals) {
										String approver = a.getApprover();
										String approverShort = approver.substring(0,approver.indexOf('@'));
										
										AbsCell tsCell = null;
										String ticket = a.getTicket();
										if (ticket.equals(prevTicket)) {
											tsCell = AbsCell.Null;
										} else {
											UUID id = UUID.fromString(a.getId());
											tsCell = new RefCell(createdDF.format((id.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH)/10000),
													RequestDetail.HREF + "?ticket=" + a.getTicket());
											prevTicket = ticket;
										}
										
										AbsCell approverCell = null;
										if (approver.endsWith(CSP_ATT_COM)) {
											approverCell = new RefCell(approver, WEBPHONE + approverShort);
										} else {
											approverCell = new TextCell(approver);
										}
										AbsCell[] sa = new AbsCell[] {
											tsCell,
											new TextCell(a.getStatus()),
											new TextCell(a.getMemo()),
											approverCell
										};
										rv.add(sa);
									}
								}
							} else {
								gui.writeError(trans, fa, null);
							}
						} finally {
							tt.done();
						}


						return null;
					}
				});
			} catch (Exception e) {
				trans.error().log(e);
			}
			return new Cells(rv,null);
		}
	}
}
