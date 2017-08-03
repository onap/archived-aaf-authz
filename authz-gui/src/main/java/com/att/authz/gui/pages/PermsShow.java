/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import java.io.IOException;
import java.net.ConnectException;
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

import aaf.v2_0.Perm;
import aaf.v2_0.Perms;

/**
 * Page content for My Permissions
 * 
 *
 */
public class PermsShow extends Page {
	public static final String HREF = "/gui/myperms";
	
	public PermsShow(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env, "MyPerms",HREF, NO_FIELDS,
			new BreadCrumbs(breadcrumbs), 
			new Table<AuthGUI,AuthzTrans>("Permissions",gui.env.newTransNoAvg(),new Model(), "class=std"));
	}

	/**
	 * Implement the Table Content for Permissions by User
	 * 
	 *
	 */
	private static class Model implements Table.Data<AuthGUI,AuthzTrans> {
		private static final String[] headers = new String[] {"Type","Instance","Action"};

		@Override
		public String[] headers() {
			return headers;
		}
		
		@Override
		public Cells get(final AuthGUI gui, final AuthzTrans trans) {
			ArrayList<AbsCell[]> rv = new ArrayList<AbsCell[]>();
			TimeTaken tt = trans.start("AAF Perms by User",Env.REMOTE);
			try {
				gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
					@Override
					public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
						Future<Perms> fp = client.read("/authz/perms/user/"+trans.user(), gui.permsDF);
						if(fp.get(5000)) {
							TimeTaken ttld = trans.start("Load Data", Env.SUB);
							try {
								if(fp.value!=null) {	
									for(Perm p : fp.value.getPerm()) {
										AbsCell[] sa = new AbsCell[] {
											new RefCell(p.getType(),PermDetail.HREF
													+"?type="+p.getType()
													+"&amp;instance="+p.getInstance()
													+"&amp;action="+p.getAction()),
											new TextCell(p.getInstance()),
											new TextCell(p.getAction())
										};
										rv.add(sa);
									}
								} else {
									gui.writeError(trans, fp, null);
								}
							} finally {
								ttld.done();
							}
						}
						return null;
					}
				});
			} catch (Exception e) {
				trans.error().log(e);
			} finally {
				tt.done();
			}
			return new Cells(rv,null);
		}
	}
}
