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
package org.onap.aaf.auth.batch.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.auth.batch.actions.ApprovalAdd;
import org.onap.aaf.auth.batch.actions.FutureAdd;
import org.onap.aaf.auth.batch.approvalsets.DataView;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class BatchDataView implements DataView {
	private FutureAdd futureAdd;
	private ApprovalAdd approvalAdd;

	public BatchDataView(final AuthzTrans trans, final Cluster cluster, final boolean dryRun ) throws APIException, IOException {
		futureAdd = new FutureAdd(trans, cluster, dryRun);
		approvalAdd = new ApprovalAdd(trans, futureAdd);
	}

	public Session getSession(AuthzTrans trans) throws APIException, IOException {
		TimeTaken tt = trans.start("Get Session", Trans.SUB);
		try {
			return futureAdd.getSession(trans);
		} finally {
			tt.done();
		}
	}
	
	public Result<NsDAO.Data> ns(AuthzTrans trans, String id) {
		NS n;
		TimeTaken tt = trans.start("Get NS by ID %s", Trans.SUB, id);
		try {
			n=NS.data.get(id);
		} finally {
			tt.done();
		}
		
		if(n==null || n.ndd==null) {
			return Result.err(Result.ERR_Backend,"Namespace '%s' does not exist", id);
		}
		return Result.ok(n.ndd);
	}

	
	@Override
	public Result<RoleDAO.Data> roleByName(AuthzTrans trans, String name) {
		Role r = Role.byName.get(name);
		if(r==null || r.rdd==null) {
			return Result.err(Result.ERR_Backend,"Role '%s' does not exist", name);
		}
		return Result.ok(r.rdd);
	}

	@Override
	public Result<List<UserRoleDAO.Data>> ursByRole(AuthzTrans trans, String role) {
		List<UserRole> urs = UserRole.getByRole().get(role);
		if(urs==null) {
			return Result.err(Result.ERR_Backend, "UserRoles for Role '%s' does not exist", role);
		}
		return toLURDD(urs);
	}

	private Result<List<Data>> toLURDD(List<UserRole> urs) {
		List<UserRoleDAO.Data> rv = new ArrayList<>();
		if(urs!=null) {
			for(UserRole ur : urs) {
				rv.add(ur.urdd());
			}
		}
		return Result.ok(rv);
	}

	@Override
	public Result<List<UserRoleDAO.Data>> ursByUser(AuthzTrans trans, String user) {
		List<UserRole> urs = UserRole.getByUser().get(user);
		if(urs==null) {
			return Result.err(Result.ERR_Backend, "UserRoles for User '%s' does not exist", user);
		}
		return toLURDD(urs);
	}

	@Override
	public Result<FutureDAO.Data> write(AuthzTrans trans, FutureDAO.Data fdd) {
		return futureAdd.exec(trans, fdd, null);
	}

	@Override
	public Result<ApprovalDAO.Data> write(AuthzTrans trans, ApprovalDAO.Data add) {
		return approvalAdd.exec(trans, add, null);
	}

}
