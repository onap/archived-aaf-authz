/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

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
import com.att.cmd.AAFcli;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.Slot;
import com.att.inno.env.TimeTaken;
import com.att.inno.env.util.Chrono;

import aaf.v2_0.Nss;
import aaf.v2_0.Nss.Ns;
import aaf.v2_0.Perm;
import aaf.v2_0.Perms;
import aaf.v2_0.Role;
import aaf.v2_0.Roles;
import aaf.v2_0.Users;
import aaf.v2_0.Users.User;

public class NsDetail extends Page {
	
	public static final String HREF = "/gui/nsdetail";
	public static final String NAME = "NsDetail";
	static final String WEBPHONE = "http://webphone.att.com/cgi-bin/webphones.pl?id=";
	public static enum NS_FIELD { OWNERS, ADMINS, ROLES, PERMISSIONS, CREDS};
	private static final String BLANK = "";

	public NsDetail(final AuthGUI gui, Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env, NAME, HREF, new String[] {"name"}, 
				new BreadCrumbs(breadcrumbs),
				new Table<AuthGUI,AuthzTrans>("Namespace Details",gui.env.newTransNoAvg(),new Model(gui.env()),"class=detail")
				);
	}

	/**
	 * Implement the table content for Namespace Detail
	 * 
	 *
	 */
	private static class Model implements Table.Data<AuthGUI,AuthzTrans> {
		private static final String[] headers = new String[0];		
		private static final String CSP_ATT_COM = "@csp.att.com";
		private Slot name;
		public Model(AuthzEnv env) {
			name = env.slot(NAME+".name");
		}

		@Override
		public String[] headers() {
			return headers;
		}
		
		@Override
		public Cells get(final AuthGUI gui, final AuthzTrans trans) {
			final String nsName = trans.get(name, null);
			if(nsName==null) {
				return Cells.EMPTY;
			}
			ArrayList<AbsCell[]> rv = new ArrayList<AbsCell[]>();
			rv.add(new AbsCell[]{new TextCell("Name:"),new TextCell(nsName)});

			final TimeTaken tt = trans.start("AAF Namespace Details",Env.REMOTE);
			try {
				gui.clientAsUser(trans.getUserPrincipal(),new Retryable<Void>() {
					@Override
					public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
						Future<Nss> fn = client.read("/authz/nss/"+nsName,gui.nssDF);

						if(fn.get(AuthGUI.TIMEOUT)) {
							tt.done();
							try {
//								TimeTaken tt = trans.start("Load Data", Env.SUB);
								
								for(Ns n : fn.value.getNs()) {
									String desc = (n.getDescription()!=null?n.getDescription():BLANK);
									rv.add(new AbsCell[]{new TextCell("Description:"),new TextCell(desc)});
									
									addField(trans, rv, n.getAdmin(), NS_FIELD.ADMINS);
									addField(trans, rv, n.getResponsible(), NS_FIELD.OWNERS);
			
									Future<Users> fu = client.read(
													"/authn/creds/ns/"+nsName, 
													gui.usersDF
													);
									List<String> creds = new ArrayList<String>();
									if(fu.get(AAFcli.timeout())) {
										for (User u : fu.value.getUser()) {
											StringBuilder sb = new StringBuilder(u.getId());
											switch(u.getType()) {
												case 1: sb.append(" (U/Pass) "); break;
												case 10: sb.append(" (Cert) "); break;
												case 200: sb.append(" (x509) "); break;
												default:
													sb.append(" ");
											}
											sb.append(Chrono.niceDateStamp(u.getExpires()));
											creds.add(sb.toString());
										}
									}
									addField(trans, rv, creds, NS_FIELD.CREDS);
			
									Future<Roles> fr = client.read(
													"/authz/roles/ns/"+nsName, 
													gui.rolesDF
													);
									List<String> roles = new ArrayList<String>();
									if(fr.get(AAFcli.timeout())) {
										for (Role r : fr.value.getRole()) {
											roles.add(r.getName());
										}
									}
									addField(trans, rv, roles, NS_FIELD.ROLES);
									
									
									Future<Perms> fp = client.read(
													"/authz/perms/ns/"+nsName, 
													gui.permsDF
													);
									List<String> perms = new ArrayList<String>();
			
									if(fp.get(AAFcli.timeout())) {
										for (Perm p : fp.value.getPerm()) {
											perms.add(p.getType() + "|" + p.getInstance() + "|" + p.getAction());
										}
									}
									addField(trans, rv, perms, NS_FIELD.PERMISSIONS);
								}
								String historyLink = NsHistory.HREF 
										+ "?name=" + nsName;
								rv.add(new AbsCell[] {new RefCell("See History",historyLink)});
							} finally {
								tt.done();
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

		private void addField(AuthzTrans trans, ArrayList<AbsCell[]> rv, List<String> values, NS_FIELD field) {
			if (!values.isEmpty()) {
				switch(field) {
				case OWNERS:
				case ADMINS:
				case CREDS:
					for (int i=0; i< values.size(); i++) {
						AbsCell label = (i==0?new TextCell(sentenceCase(field)+":"):AbsCell.Null);
						String user = values.get(i);
						AbsCell userCell = (user.endsWith(CSP_ATT_COM)?
								new RefCell(user,WEBPHONE + user.substring(0,user.indexOf('@'))):new TextCell(user));
						rv.add(new AbsCell[] {
								label, 
								userCell
						});
					}
					break;
				case ROLES:
					for (int i=0; i< values.size(); i++) {
						AbsCell label = (i==0?new TextCell(sentenceCase(field)+":"):AbsCell.Null);
						rv.add(new AbsCell[] {
								label, 
								new TextCell(values.get(i))
						});
					}
					break;
				case PERMISSIONS:
					for (int i=0; i< values.size(); i++) {
						AbsCell label = (i==0?new TextCell(sentenceCase(field)+":"):AbsCell.Null);
						String perm = values.get(i);
						String[] fields = perm.split("\\|");
						String grantLink = PermGrantForm.HREF 
								+ "?type=" + fields[0].trim()
								+ "&amp;instance=" + fields[1].trim()
								+ "&amp;action=" + fields[2].trim();
						
						rv.add(new AbsCell[] {
								label, 
								new TextCell(perm),
								new RefCell("Grant This Perm", grantLink)
						});
					}
					break;
				}

			}
		}

		private String sentenceCase(NS_FIELD field) {
			String sField = field.toString();
			return sField.substring(0, 1).toUpperCase() + sField.substring(1).toLowerCase();
		}
	
	}
}
