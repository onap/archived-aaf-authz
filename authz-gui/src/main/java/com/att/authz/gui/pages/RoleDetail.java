/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;

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
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Env;
import org.onap.aaf.inno.env.Slot;
import org.onap.aaf.inno.env.TimeTaken;

import aaf.v2_0.Pkey;
import aaf.v2_0.Role;
import aaf.v2_0.Roles;

/**
 * Detail Page for Permissions
 * 
 *
 */
public class RoleDetail extends Page {
	public static final String HREF = "/gui/roledetail";
	public static final String NAME = "RoleDetail";
	private static final String BLANK = "";

	public RoleDetail(final AuthGUI gui, Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env, NAME, HREF, new String[] {"role"},
				new BreadCrumbs(breadcrumbs),
				new Table<AuthGUI,AuthzTrans>("Role Details",gui.env.newTransNoAvg(),new Model(gui.env()),"class=detail")
				);
	}

	/**
	 * Implement the table content for Permissions Detail
	 * 
	 *
	 */
	private static class Model implements Table.Data<AuthGUI,AuthzTrans> {
		private static final String[] headers = new String[0];
		private Slot role;
		public Model(AuthzEnv env) {
			role = env.slot(NAME+".role");
		}

		@Override
		public String[] headers() {
			return headers;
		}
		
		@Override
		public Cells get(final AuthGUI gui, final AuthzTrans trans) {
			final String pRole = trans.get(role, null);
			Cells rv = Cells.EMPTY;
			if(pRole!=null) {
				try { 
					rv = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Cells>() {
						@Override
						public Cells code(Rcli<?> client) throws CadiException, ConnectException, APIException {
							ArrayList<AbsCell[]> rv = new ArrayList<AbsCell[]>();
							rv.add(new AbsCell[]{new TextCell("Role:"),new TextCell(pRole)});
							
							TimeTaken tt = trans.start("AAF Role Details",Env.REMOTE);
							try {
								
								Future<Roles> fr = client.read("/authz/roles/"+pRole,gui.rolesDF);
								if(fr.get(AuthGUI.TIMEOUT)) {
									tt.done();
									tt = trans.start("Load Data", Env.SUB);
									Role role = fr.value.getRole().get(0);
									String desc = (role.getDescription()!=null?role.getDescription():BLANK);
									rv.add(new AbsCell[]{new TextCell("Description:"),new TextCell(desc)});
									boolean first=true;
									for(Pkey r : role.getPerms()) {
										if(first){
											first=false;
											rv.add(new AbsCell[] {
													new TextCell("Associated Permissions:"),
													new TextCell(r.getType() +
															" | " + r.getInstance() +
															" | " + r.getAction()
															)
												});
										} else {
											rv.add(new AbsCell[] {
												AbsCell.Null,
												new TextCell(r.getType() +
														" | " + r.getInstance() +
														" | " + r.getAction()
														)
											});
										}
									}
									String historyLink = RoleHistory.HREF 
											+ "?role=" + pRole;
									rv.add(new AbsCell[] {new RefCell("See History",historyLink)});
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
	}
}		
		