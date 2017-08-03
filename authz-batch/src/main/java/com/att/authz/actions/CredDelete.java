/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import java.io.IOException;

import com.att.authz.env.AuthzTrans;
import com.att.authz.layer.Result;
import com.att.dao.aaf.cass.CredDAO;
import com.att.inno.env.APIException;
import com.att.inno.env.util.Chrono;
import com.datastax.driver.core.Cluster;

public class CredDelete extends ActionDAO<CredDAO.Data,Void> {
	
	public CredDelete(AuthzTrans trans, Cluster cluster) throws APIException, IOException {
		super(trans, cluster);
	}

	public CredDelete(AuthzTrans trans, ActionDAO<?,?> adao) {
		super(trans, adao);
	}

	@Override
	public Result<Void> exec(AuthzTrans trans, CredDAO.Data cred) {
		Result<Void> rv = q.credDAO.delete(trans, cred, true); // need to read for undelete
		trans.info().log("Deleted:",cred.id,CredPrint.type(cred.type),Chrono.dateOnlyStamp(cred.expires));
		return rv;
	}
}