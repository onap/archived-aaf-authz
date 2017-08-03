/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import java.io.IOException;
import java.net.ConnectException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;

import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import com.att.authz.gui.AuthGUI;
import com.att.authz.gui.BreadCrumbs;
import com.att.authz.gui.Page;
import com.att.authz.gui.Table;
import com.att.authz.gui.Table.Cells;
import com.att.authz.gui.table.AbsCell;
import com.att.authz.gui.table.RefCell;
import com.att.authz.gui.table.TextCell;
import com.att.cadi.CadiException;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cadi.client.Retryable;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.Slot;
import com.att.inno.env.TimeTaken;

import aaf.v2_0.Approval;
import aaf.v2_0.Approvals;

public class RequestDetail extends Page {
	public static final String HREF = "/gui/requestdetail";
	public static final String NAME = "RequestDetail";
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String[] FIELDS = {"ticket"};

	public RequestDetail(final AuthGUI gui, Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env, NAME, HREF, FIELDS,
				new BreadCrumbs(breadcrumbs),
				new Table<AuthGUI,AuthzTrans>("Request Details",gui.env.newTransNoAvg(),new Model(gui.env()),"class=detail")
				);
	}

	/**
	 * Implement the table content for Request Detail
	 * 
	 *
	 */
	private static class Model implements Table.Data<AuthGUI,AuthzTrans> {
		static final String WEBPHONE = "http://webphone.att.com/cgi-bin/webphones.pl?id=";
		private static final String CSP_ATT_COM = "@csp.att.com";
		final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;
		private static final String[] headers = new String[0];
		private Slot sTicket;
		public Model(AuthzEnv env) {
			sTicket = env.slot(NAME+".ticket");
		}

		@Override
		public String[] headers() {
			return headers;
		}
		
		@Override
		public Cells get(final AuthGUI gui, final AuthzTrans trans) {
			Cells rv=Cells.EMPTY;
			final String ticket = trans.get(sTicket, null);
			if(ticket!=null) {
				try {
					rv = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Cells>() {
						@Override
						public Cells code(Rcli<?> client) throws CadiException, ConnectException, APIException {
							TimeTaken tt = trans.start("AAF Approval Details",Env.REMOTE);
							ArrayList<AbsCell[]> rv = new ArrayList<AbsCell[]>();
							try {
								Future<Approvals> fa = client.read(
									"/authz/approval/ticket/"+ticket, 
									gui.approvalsDF
									);
								
								if(fa.get(AuthGUI.TIMEOUT)) {
									if (!trans.user().equals(fa.value.getApprovals().get(0).getUser())) {
										return Cells.EMPTY;
									}
									tt.done();
									tt = trans.start("Load Data", Env.SUB);
									boolean first = true;
									for ( Approval approval : fa.value.getApprovals()) {
										AbsCell[] approverLine = new AbsCell[4];
										// only print common elements once
										if (first) {
											DateFormat createdDF = new SimpleDateFormat(DATE_TIME_FORMAT);
											UUID id = UUID.fromString(approval.getId());
											
											rv.add(new AbsCell[]{new TextCell("Ticket ID:"),new TextCell(approval.getTicket(),"colspan=3")});
											rv.add(new AbsCell[]{new TextCell("Memo:"),new TextCell(approval.getMemo(),"colspan=3")});
											rv.add(new AbsCell[]{new TextCell("Requested On:"), 
													new TextCell(createdDF.format((id.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH)/10000),"colspan=3")
											});
											rv.add(new AbsCell[]{new TextCell("Operation:"),new TextCell(decodeOp(approval.getOperation()),"colspan=3")});
											String user = approval.getUser();
											if (user.endsWith(CSP_ATT_COM)) {
												rv.add(new AbsCell[]{new TextCell("User:"),
														new RefCell(user,WEBPHONE + user.substring(0, user.indexOf("@")),"colspan=3")});
											} else {
												rv.add(new AbsCell[]{new TextCell("User:"),new TextCell(user,"colspan=3")});
											}
											
											// headers for listing each approver
											rv.add(new AbsCell[]{new TextCell(" ","colspan=4","class=blank_line")});
											rv.add(new AbsCell[]{AbsCell.Null,
													new TextCell("Approver","class=bold"), 
													new TextCell("Type","class=bold"), 
													new TextCell("Status","class=bold")});
											approverLine[0] = new TextCell("Approvals:");
											
											first = false;
										} else {
										    approverLine[0] = AbsCell.Null;
										}
										
										String approver = approval.getApprover();
										String approverShort = approver.substring(0,approver.indexOf('@'));
										
										if (approver.endsWith(CSP_ATT_COM)) {
											approverLine[1] = new RefCell(approver, WEBPHONE + approverShort);
										} else {
											approverLine[1] = new TextCell(approval.getApprover());
										}
										
										String type = approval.getType();
										if ("owner".equalsIgnoreCase(type)) {
											type = "resource owner";
										}
										
										approverLine[2] = new TextCell(type);
										approverLine[3] = new TextCell(approval.getStatus());
										rv.add(approverLine);
									
									}
								} else {
									rv.add(new AbsCell[] {new TextCell("*** Data Unavailable ***")});
								}
							} finally {
								tt.done();
							}
							return new Cells(rv,null);
						}
					});
				} catch (Exception e) {
					trans.error().log(e);
				}
			}
			return rv;
		}

		private String decodeOp(String operation) {
			if ("C".equalsIgnoreCase(operation)) {
				return "Create";
			} else if ("D".equalsIgnoreCase(operation)) {
				return "Delete";
			} else if ("U".equalsIgnoreCase(operation)) {
				return "Update";
			} else if ("G".equalsIgnoreCase(operation)) {
				return "Grant";
			} else if ("UG".equalsIgnoreCase(operation)) {
				return "Un-Grant";
			}
			return operation;
		}
	}
}
