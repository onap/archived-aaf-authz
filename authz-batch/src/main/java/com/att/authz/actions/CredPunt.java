/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.att.authz.env.AuthzTrans;
import com.att.authz.layer.Result;
import com.att.dao.aaf.cass.CredDAO;
import com.att.inno.env.APIException;
import com.att.inno.env.util.Chrono;
import com.datastax.driver.core.Cluster;

public class CredPunt extends ActionPuntDAO<CredDAO.Data,Void> {
	
	public CredPunt(AuthzTrans trans, Cluster cluster, int months, int range) throws IOException, APIException {
		super(trans,cluster,months,range);
	}

	public CredPunt(AuthzTrans trans, ActionDAO<?,?> adao, int months, int range) throws IOException {
		super(trans, adao, months,range);
	}

	public Result<Void> exec(AuthzTrans trans, CredDAO.Data cdd) {
		Result<Void> rv = null;
		Result<List<CredDAO.Data>> read = q.credDAO.read(trans, cdd);
		if(read.isOKhasData()) {
			for(CredDAO.Data data : read.value) {
				Date from = data.expires;
				data.expires = puntDate();
				if(data.expires.before(from)) {
					trans.error().printf("Error: %s is before %s", Chrono.dateOnlyStamp(data.expires), Chrono.dateOnlyStamp(from));
				} else {
					rv = q.credDAO.update(trans, data);
					trans.info().log("Updated Cred",cdd.id, CredPrint.type(cdd.type), "from",Chrono.dateOnlyStamp(from),"to",Chrono.dateOnlyStamp(data.expires));
				}
			}
		}
		if(rv==null) {
			rv=Result.err(read);
		}
		return rv;
	}
}