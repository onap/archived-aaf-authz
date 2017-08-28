/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import java.io.IOException;

import com.att.authz.env.AuthzTrans;
import com.att.dao.CassAccess;
import com.att.dao.aaf.hl.Function;
import com.att.dao.aaf.hl.Question;
import org.onap.aaf.inno.env.APIException;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public abstract class ActionDAO<T,RV> implements Action<T,RV> {
	protected final Question q; 
	protected final Function f;
	private boolean clean;

	public ActionDAO(AuthzTrans trans, Cluster cluster) throws APIException, IOException {
		q = new Question(trans, cluster, CassAccess.KEYSPACE, false);
		f = new Function(trans,q);
		clean = true;
	}
	
	public ActionDAO(AuthzTrans trans, ActionDAO<?,?> predecessor) {
		q = predecessor.q;
		f = new Function(trans,q);
		clean = false;
	}
	
	public Session getSession(AuthzTrans trans) throws APIException, IOException {
		return q.historyDAO.getSession(trans);
	}

	public void close(AuthzTrans trans) {
		if(clean) {
			q.close(trans);
		}
	}

}
