/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz;

import java.io.IOException;

import com.att.authz.env.AuthzTrans;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.TimeTaken;
import com.att.inno.env.impl.Log4JLogTarget;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public abstract class CassBatch extends Batch {

	protected CassBatch(AuthzTrans trans, String log4JName) throws APIException, IOException {
		super(trans.env());
		// Flow all Env Logs to Log4j
		Log4JLogTarget.setLog4JEnv(log4JName, env);
		
		TimeTaken tt = trans.start("Connect to Cluster", Env.REMOTE);
		try {
			session = cluster.connect();
		} finally {
			tt.done();
		}
	}

	@Override
	protected void _close(AuthzTrans trans) {
	    session.close();
		trans.info().log("Closed Session");
	}

	public ResultSet executeQuery(String cql) {
		return executeQuery(cql,"");
	}

	public ResultSet executeQuery(String cql, String extra) {
		if(isDryRun() && !cql.startsWith("SELECT")) {
			if(extra!=null)env.info().log("Would query" + extra + ": " + cql);
		} else {
			if(extra!=null)env.info().log("query" + extra + ": " + cql);
			try {
				return session.execute(cql);
			} catch (InvalidQueryException e) {
				if(extra==null) {
					env.info().log("query: " + cql);
				}
				throw e;
			}
		} 
		return null;
	}

}