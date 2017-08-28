/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import java.io.IOException;

import com.att.authz.env.AuthzTrans;
import com.att.authz.helpers.UserRole;
import com.att.authz.layer.Result;
import com.att.dao.aaf.cass.UserRoleDAO;
import com.att.dao.aaf.cass.UserRoleDAO.Data;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.util.Chrono;
import com.datastax.driver.core.Cluster;

public class URAdd extends ActionDAO<UserRole,UserRoleDAO.Data> {
	public URAdd(AuthzTrans trans, Cluster cluster) throws APIException, IOException {
		super(trans, cluster);
	}
	
	public URAdd(AuthzTrans trans, ActionDAO<?,?> adao) {
		super(trans, adao);
	}

	@Override
	public Result<Data> exec(AuthzTrans trans, UserRole ur) {
		UserRoleDAO.Data urd = new UserRoleDAO.Data();
		urd.user = ur.user;
		urd.role = ur.role;
		urd.ns=ur.ns;
		urd.rname = ur.rname;
		urd.expires = ur.expires;
		Result<Data> rv = q.userRoleDAO.create(trans, urd);
		trans.info().log("Added:",ur.role,ur.user,"on",Chrono.dateOnlyStamp(ur.expires));
		return rv;
	}
	
}