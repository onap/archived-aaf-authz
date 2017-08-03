/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import com.att.aft.dme2.internal.jetty.http.HttpStatus;
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

import aaf.v2_0.Perm;
import aaf.v2_0.Perms;

/**
 * Detail Page for Permissions
 *
 */
public class PermDetail extends Page {
	public static final String HREF = "/gui/permdetail";
	public static final String NAME = "PermDetail";
	private static final String BLANK = "";

	public PermDetail(final AuthGUI gui, Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env, NAME, HREF, new String[] {"type","instance","action"},
				new BreadCrumbs(breadcrumbs),
				new Table<AuthGUI,AuthzTrans>("Permission Details",gui.env.newTransNoAvg(),new Model(gui.env()),"class=detail")
				);
	}

	/**
	 * Implement the table content for Permissions Detail
	 * 
	 *
	 */
	private static class Model implements Table.Data<AuthGUI,AuthzTrans> {
		private static final String[] headers = new String[0];
		private Slot type, instance, action;
		public Model(AuthzEnv env) {
			type = env.slot(NAME+".type");
			instance = env.slot(NAME+".instance");
			action = env.slot(NAME+".action");
		}

		@Override
		public String[] headers() {
			return headers;
		}
		
		@Override
		public Cells get(final AuthGUI gui, final AuthzTrans trans) {
			final String pType = trans.get(type, null);
			final String pInstance = trans.get(instance, null);
			final String pAction = trans.get(action, null);
			if(pType==null || pInstance==null || pAction==null) {
				return Cells.EMPTY;
			}
			ArrayList<AbsCell[]> rv = new ArrayList<AbsCell[]>();
			rv.add(new AbsCell[]{new TextCell("Type:"),new TextCell(pType)});
			rv.add(new AbsCell[]{new TextCell("Instance:"),new TextCell(pInstance)});
			rv.add(new AbsCell[]{new TextCell("Action:"),new TextCell(pAction)});
			try {
				gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
					@Override
					public Void code(Rcli<?> client)throws CadiException, ConnectException, APIException {
						TimeTaken tt = trans.start("AAF Perm Details",Env.REMOTE);
						try {
							Future<Perms> fp= client.read("/authz/perms/"+pType + '/' + pInstance + '/' + pAction,gui.permsDF);
					
							if(fp.get(AuthGUI.TIMEOUT)) {
								tt.done();
								tt = trans.start("Load Data", Env.SUB);
								List<Perm> ps = fp.value.getPerm();
								if(!ps.isEmpty()) {
									Perm perm = fp.value.getPerm().get(0);
									String desc = (perm.getDescription()!=null?perm.getDescription():BLANK);
									rv.add(new AbsCell[]{new TextCell("Description:"),new TextCell(desc)});
									boolean first=true;
									for(String r : perm.getRoles()) {
										if(first){
											first=false;
											rv.add(new AbsCell[] {
													new TextCell("Associated Roles:"),
													new TextCell(r)
												});
										} else {
											rv.add(new AbsCell[] {
												AbsCell.Null,
												new TextCell(r)
											});
										}
									}
								}
								String historyLink = PermHistory.HREF 
										+ "?type=" + pType + "&instance=" + pInstance + "&action=" + pAction;
								
								rv.add(new AbsCell[] {new RefCell("See History",historyLink)});
							} else {
								rv.add(new AbsCell[] {new TextCell(
									fp.code()==HttpStatus.NOT_FOUND_404?
										"*** Implicit Permission ***":
										"*** Data Unavailable ***"
										)});
							}
						} finally {
							tt.done();
						}

						return null;
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new Cells(rv,null);
		}
	}
}		
		