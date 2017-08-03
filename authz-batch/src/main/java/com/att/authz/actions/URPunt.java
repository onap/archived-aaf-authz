/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.att.authz.env.AuthzTrans;
import com.att.authz.helpers.UserRole;
import com.att.authz.layer.Result;
import com.att.dao.aaf.cass.UserRoleDAO;
import com.att.dao.aaf.cass.UserRoleDAO.Data;
import com.att.inno.env.APIException;
import com.att.inno.env.util.Chrono;
import com.datastax.driver.core.Cluster;

public class URPunt extends ActionPuntDAO<UserRole,Void> {
	public URPunt(AuthzTrans trans, Cluster cluster, int months, int range) throws APIException, IOException {
		super(trans,cluster, months, range);
	}

	public URPunt(AuthzTrans trans, ActionDAO<?,?> adao, int months, int range) {
		super(trans, adao, months, range);
	}

	public Result<Void> exec(AuthzTrans trans, UserRole ur) {
		Result<List<Data>> read = q.userRoleDAO.read(trans, ur.user, ur.role);
		if(read.isOK()) {
			for(UserRoleDAO.Data data : read.value) {
				Date from = data.expires;
				data.expires = puntDate();
				if(data.expires.before(from)) {
					trans.error().printf("Error: %s is before %s", Chrono.dateOnlyStamp(data.expires), Chrono.dateOnlyStamp(from));
				} else {
					q.userRoleDAO.update(trans, data);
					trans.info().log("Updated User",ur.user,"and Role", ur.role, "from",Chrono.dateOnlyStamp(from),"to",Chrono.dateOnlyStamp(data.expires));
				}
			}
			return Result.ok();
		} else {
			return Result.err(read);
		}
	}
}