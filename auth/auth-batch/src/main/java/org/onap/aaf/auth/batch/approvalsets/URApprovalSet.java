/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2019 IBM.
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
package org.onap.aaf.auth.batch.approvalsets;

import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;

import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO.Data;
import org.onap.aaf.auth.dao.hl.Function.FUTURE_OP;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.misc.env.util.Chrono;

public class URApprovalSet extends ApprovalSet {
	public static final String EXTEND_STRING = "Extend access of User [%s] to Role [%s] - Expires %s";
	
	public URApprovalSet(final AuthzTrans trans, final GregorianCalendar start, final DataView dv, final Loader<UserRoleDAO.Data> lurdd) throws IOException, CadiException {
		super(start, "user_role", dv);
		Organization org = trans.org();
		UserRoleDAO.Data urdd = lurdd.load();
		setConstruct(urdd.bytify());
		setMemo(String.format(EXTEND_STRING,urdd.user,urdd.role,Chrono.dateOnlyStamp(urdd.expires)));
		setExpires(org.expiration(null, Organization.Expiration.UserInRole));
		
		Result<RoleDAO.Data> r = dv.roleByName(trans, urdd.role);
		if(r.notOKorIsEmpty()) {
			throw new CadiException(String.format("Role '%s' does not exist: %s", urdd.role, r.details));
		}
		Result<NsDAO.Data> n = dv.ns(trans, urdd.ns);
		if(n.notOKorIsEmpty()) {
			throw new CadiException(String.format("Namespace '%s' does not exist: %s", urdd.ns));
		}
		UserRoleDAO.Data found = null;
		Result<List<Data>> lur = dv.ursByRole(trans, urdd.role);
		if(lur.isOK()) {
			for(UserRoleDAO.Data ur : lur.value) {
				if(urdd.user.equals(ur.user)) {
					found = ur;
					break;
				}
			}
		}
		if(found==null) {
			throw new CadiException(String.format("User '%s' in Role '%s' does not exist: %s", urdd.user,urdd.role));
		}
		
		// Primarily, Owners are responsible, unless it's owned by self
		boolean isOwner = false;
		Result<List<UserRoleDAO.Data>> owners = dv.ursByRole(trans, urdd.ns+".owner");
		if(owners.isOK()) {
			for(UserRoleDAO.Data owner : owners.value) {
				if(urdd.user.equals(owner.user)) {
					isOwner = true;
				} else {
					ApprovalDAO.Data add = newApproval(urdd);
					add.approver = owner.user;
					add.type="owner";
					ladd.add(add);
				}
			}
		}

		if(isOwner) {
			try {
				List<Identity> apprs = org.getApprovers(trans, urdd.user);
				if(apprs!=null) {
					for(Identity i : apprs) {
						ApprovalDAO.Data add = newApproval(urdd);
						Identity reportsTo = i.responsibleTo();
						if(reportsTo!=null) {
							add.approver = reportsTo.fullID();
						} else {
							throw new CadiException("No Supervisor for '" + urdd.user + '\'');
						}
						add.type = org.getApproverType();
						ladd.add(add);
					}
				}
			} catch (OrganizationException e) {
				throw new CadiException(e);
			}
		}
	}

	private ApprovalDAO.Data newApproval(Data urdd) {
		ApprovalDAO.Data add = new ApprovalDAO.Data();
		add.id = Chrono.dateToUUID(System.currentTimeMillis());
		add.ticket = fdd.id;
		add.user = urdd.user;
		add.operation = FUTURE_OP.A.name();
		add.status = ApprovalDAO.PENDING;
		add.memo = String.format("Re-Validate as Owner for AAF Namespace '%s' - expiring %s', ",
				   urdd.ns,
				   Chrono.dateOnlyStamp(urdd.expires));
		return add;
	}

}
