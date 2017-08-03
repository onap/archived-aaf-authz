/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import java.io.IOException;

import com.att.authz.env.AuthzTrans;
import com.att.authz.helpers.UserRole;
import com.att.authz.layer.Result;
import com.att.dao.aaf.cass.UserRoleDAO;
import com.att.inno.env.APIException;
import com.att.inno.env.util.Chrono;
import com.datastax.driver.core.Cluster;

public class URDelete extends ActionDAO<UserRole,Void> {
	public URDelete(AuthzTrans trans, Cluster cluster) throws APIException, IOException {
		super(trans, cluster);
	}
	
	public URDelete(AuthzTrans trans, ActionDAO<?,?> adao) {
		super(trans, adao);
	}

	@Override
	public Result<Void> exec(AuthzTrans trans, UserRole ur) {
		UserRoleDAO.Data urd = new UserRoleDAO.Data();
		urd.user = ur.user;
		urd.role = ur.role;
		Result<Void> rv = q.userRoleDAO.delete(trans, urd, true); // need to read for undelete
		trans.info().log("Deleted:",ur.role,ur.user,"on",Chrono.dateOnlyStamp(ur.expires));
		return rv;
	}
	
}