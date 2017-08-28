/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import java.io.IOException;
import java.util.List;

import com.att.authz.env.AuthzTrans;
import com.att.authz.helpers.Future;
import com.att.authz.layer.Result;
import com.att.dao.aaf.cass.ApprovalDAO;
import com.att.dao.aaf.cass.FutureDAO;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.util.Chrono;
import com.datastax.driver.core.Cluster;

public class FADelete extends ActionDAO<Future,Void> {
	public FADelete(AuthzTrans trans, Cluster cluster) throws APIException, IOException {
		super(trans, cluster);
	}
	
	public FADelete(AuthzTrans trans, ActionDAO<?,?> adao) {
		super(trans, adao);
	}

	@Override
	public Result<Void> exec(AuthzTrans trans, Future f) {
		FutureDAO.Data fdd = new FutureDAO.Data();
		fdd.id=f.id;
		Result<Void> rv = q.futureDAO.delete(trans, fdd, true); // need to read for undelete
		if(rv.isOK()) {
			trans.info().log("Deleted:",f.id,f.memo,"expiring on",Chrono.dateOnlyStamp(f.expires));
		} else {
			trans.info().log("Failed to Delete Approval");
		}
		
		Result<List<ApprovalDAO.Data>> ral = q.approvalDAO.readByTicket(trans, f.id);
		if(ral.isOKhasData()) {
			for(ApprovalDAO.Data add : ral.value) {
				rv = q.approvalDAO.delete(trans, add, false);
				if(rv.isOK()) {
					trans.info().log("Deleted: Approval",add.id,"on ticket",add.ticket,"for",add.approver);
				} else {
					trans.info().log("Failed to Delete Approval");
				}
			}
		}
		return rv;
	}
	
}