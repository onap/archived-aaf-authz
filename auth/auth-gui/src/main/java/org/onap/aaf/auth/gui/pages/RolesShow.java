/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.gui.pages;

import java.io.IOException;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.gui.Table;
import org.onap.aaf.auth.gui.Table.Cells;
import org.onap.aaf.auth.gui.table.AbsCell;
import org.onap.aaf.auth.gui.table.RefCell;
import org.onap.aaf.auth.gui.table.TableData;
import org.onap.aaf.auth.gui.table.TextCell;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;

import aaf.v2_0.UserRole;
import aaf.v2_0.UserRoles;


/**
 * Page content for My Roles
 * 
 * @author Jonathan
 *
 */
public class RolesShow extends Page {
	public static final String HREF = "/gui/myroles";
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd";
	
	public RolesShow(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env, "MyRoles",HREF, NO_FIELDS,
			new BreadCrumbs(breadcrumbs), 
			new Table<AAF_GUI,AuthzTrans>("Roles",gui.env.newTransNoAvg(),new Model(), "class=std"));
	}

	/**
	 * Implement the Table Content for Permissions by User
	 * 
	 * @author Jonathan
	 *
	 */
	private static class Model extends TableData<AAF_GUI,AuthzTrans> {
		private static final String[] headers = new String[] {"Role","Expires","Remediation","Actions"};

		@Override
		public String[] headers() {
			return headers;
		}
		
		@Override
		public Cells get(final AuthzTrans trans, final AAF_GUI gui) {
			Cells rv = Cells.EMPTY;

			try {
				rv = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Cells>() {
					@Override
					public Cells code(Rcli<?> client) throws CadiException, ConnectException, APIException {
						ArrayList<AbsCell[]> rv = new ArrayList<AbsCell[]>();
						TimeTaken tt = trans.start("AAF Roles by User",Env.REMOTE);
						try {
							Future<UserRoles> fur = client.read("/authz/userRoles/user/"+trans.user(),gui.getDF(UserRoles.class));
							if (fur.get(5000)) {
								if(fur.value != null) for (UserRole u : fur.value.getUserRole()) {
									if(u.getExpires().compare(Chrono.timeStamp()) < 0) {
										AbsCell[] sa = new AbsCell[] {
												new TextCell(u.getRole() + "*", "class=expired"),
												new TextCell(new SimpleDateFormat(DATE_TIME_FORMAT).format(u.getExpires().toGregorianCalendar().getTime()),"class=expired"),
												new RefCell("Extend",
														UserRoleExtend.HREF + "?user="+trans.user()+"&role="+u.getRole(),
														false,
														new String[]{"class=expired"}),
												new RefCell("Remove",
													UserRoleRemove.HREF + "?user="+trans.user()+"&role="+u.getRole(),
													false,
													new String[]{"class=expired"})
														
											};
											rv.add(sa);
									} else {
										AbsCell[] sa = new AbsCell[] {
												new RefCell(u.getRole(),
														RoleDetail.HREF+"?role="+u.getRole(),
														false),
												new TextCell(new SimpleDateFormat(DATE_TIME_FORMAT).format(u.getExpires().toGregorianCalendar().getTime())),
												AbsCell.Null,
												new RefCell("Remove",
														UserRoleRemove.HREF + "?user="+trans.user()+"&role="+u.getRole(),
														false)
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
