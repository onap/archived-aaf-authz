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
import com.att.authz.gui.Page;
import com.att.authz.gui.Table;
import com.att.authz.gui.Table.Cells;
import com.att.authz.gui.table.AbsCell;
import com.att.authz.gui.table.RefCell;
import com.att.cadi.CadiException;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cadi.client.Retryable;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.Slot;
import com.att.inno.env.TimeTaken;

import aaf.v2_0.Nss;
import aaf.v2_0.Nss.Ns;

public class NssShow extends Page {
	public static final String HREF = "/gui/mynamespaces";

	public NssShow(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env, "MyNamespaces",HREF, NO_FIELDS,
				new BreadCrumbs(breadcrumbs), 
				new Table<AuthGUI,AuthzTrans>("Namespaces I administer",gui.env.newTransNoAvg(),new Model("admin",gui.env), 
						"class=std", "style=display: inline-block; width: 45%; margin: 10px;"),
				new Table<AuthGUI,AuthzTrans>("Namespaces I own",gui.env.newTransNoAvg(),new Model("responsible",gui.env),
						"class=std", "style=display: inline-block; width: 45%; margin: 10px;"));
	}
	
	private static class Model implements Table.Data<AuthGUI,AuthzTrans> {
		private String[] headers;
		private String privilege = null;
		public final Slot sNssByUser;
		private boolean isAdmin;

		public Model(String privilege,AuthzEnv env) {
			super();
			headers = new String[] {privilege};
			this.privilege = privilege;
			isAdmin = "admin".equals(privilege);
			sNssByUser = env.slot("NSS_SHOW_MODEL_DATA");
		}

		@Override
		public String[] headers() {
			return headers;
		}
		
		@Override
		public Cells get(final AuthGUI gui, final AuthzTrans trans) {
			ArrayList<AbsCell[]> rv = new ArrayList<AbsCell[]>();
			List<Ns> nss = trans.get(sNssByUser, null);
			if(nss==null) {
				TimeTaken tt = trans.start("AAF Nss by User for " + privilege,Env.REMOTE);
				try {
					nss = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<List<Ns>>() {
						@Override
						public List<Ns> code(Rcli<?> client) throws CadiException, ConnectException, APIException {
							List<Ns> nss = null;
							Future<Nss> fp = client.read("/authz/nss/either/" + trans.user(),gui.nssDF);
							if(fp.get(AuthGUI.TIMEOUT)) {
								TimeTaken tt = trans.start("Load Data for " + privilege, Env.SUB);
								try {
									if(fp.value!=null) {
										nss = fp.value.getNs();
										Collections.sort(nss, new Comparator<Ns>() {
											public int compare(Ns ns1, Ns ns2) {
												return ns1.getName().compareToIgnoreCase(ns2.getName());
											}
										});
										trans.put(sNssByUser,nss);
									} 
								} finally {
									tt.done();
								}
							}else {
								gui.writeError(trans, fp, null);
							}
							return nss;
						}
					});
				} catch (Exception e) {
					trans.error().log(e);
				} finally {
					tt.done();
				}
			}
			
			if(nss!=null) {
				for(Ns n : nss) {
					if((isAdmin && !n.getAdmin().isEmpty())
					  || (!isAdmin && !n.getResponsible().isEmpty())) {
						AbsCell[] sa = new AbsCell[] {
							new RefCell(n.getName(),NsDetail.HREF
									+"?name="+n.getName()),
						};
						rv.add(sa);
					}
				}
			}

			return new Cells(rv,null);
		}
	}
	

}
