/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import java.io.IOException;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

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
import com.att.inno.env.TimeTaken;
import com.att.inno.env.util.Chrono;

import aaf.v2_0.UserRole;
import aaf.v2_0.UserRoles;


/**
 * Page content for My Roles
 * 
 *
 */
public class RolesShow extends Page {
	public static final String HREF = "/gui/myroles";
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd";
	private static SimpleDateFormat expiresDF;
	
	static {
		expiresDF = new SimpleDateFormat(DATE_TIME_FORMAT);
	}
	
	public RolesShow(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env, "MyRoles",HREF, NO_FIELDS,
			new BreadCrumbs(breadcrumbs), 
			new Table<AuthGUI,AuthzTrans>("Roles",gui.env.newTransNoAvg(),new Model(), "class=std"));
	}

	/**
	 * Implement the Table Content for Permissions by User
	 * 
	 *
	 */
	private static class Model implements Table.Data<AuthGUI,AuthzTrans> {
		private static final String[] headers = new String[] {"Role","Expires","Remediation","Actions"};

		@Override
		public String[] headers() {
			return headers;
		}
		
		@Override
		public Cells get(final AuthGUI gui, final AuthzTrans trans) {
			Cells rv = Cells.EMPTY;

			try {
				rv = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Cells>() {
					@Override
					public Cells code(Rcli<?> client) throws CadiException, ConnectException, APIException {
						ArrayList<AbsCell[]> rv = new ArrayList<AbsCell[]>();
						TimeTaken tt = trans.start("AAF Roles by User",Env.REMOTE);
						try {
							Future<UserRoles> fur = client.read("/authz/userRoles/user/"+trans.user(),gui.userrolesDF);
							if (fur.get(5000)) {
								if(fur.value != null) for (UserRole u : fur.value.getUserRole()) {
									if(u.getExpires().compare(Chrono.timeStamp()) < 0) {
										AbsCell[] sa = new AbsCell[] {
												new TextCell(u.getRole() + "*", "class=expired"),
												new TextCell(expiresDF.format(u.getExpires().toGregorianCalendar().getTime()),"class=expired"),
												new RefCell("Extend",
														UserRoleExtend.HREF + "?user="+trans.user()+"&role="+u.getRole(), 
														new String[]{"class=expired"}),
												new RefCell("Remove",
													UserRoleRemove.HREF + "?user="+trans.user()+"&role="+u.getRole(), 
													new String[]{"class=expired"})
														
											};
											rv.add(sa);
									} else {
										AbsCell[] sa = new AbsCell[] {
												new RefCell(u.getRole(),
														RoleDetail.HREF+"?role="+u.getRole()),
												new TextCell(expiresDF.format(u.getExpires().toGregorianCalendar().getTime())),
												AbsCell.Null,
												new RefCell("Remove",
														UserRoleRemove.HREF + "?user="+trans.user()+"&role="+u.getRole())
											};
											rv.add(sa);
									}
								}
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
			return rv;
		}
	}
}
