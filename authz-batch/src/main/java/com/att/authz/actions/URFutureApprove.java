/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.actions;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.att.authz.env.AuthzTrans;
import com.att.authz.helpers.UserRole;
import com.att.authz.layer.Result;
import com.att.authz.org.Organization.Expiration;
import com.att.authz.org.Organization.Identity;
import com.att.dao.aaf.cass.FutureDAO;
import com.att.dao.aaf.cass.NsDAO;
import com.att.dao.aaf.hl.Function;
import com.att.dao.aaf.hl.Question;
import com.att.inno.env.APIException;
import com.att.inno.env.util.Chrono;
import com.datastax.driver.core.Cluster;

public class URFutureApprove extends ActionDAO<UserRole, List<Identity>> implements Action<UserRole,List<Identity>>, Key<UserRole> {
	private final Date start, expires;

	public URFutureApprove(AuthzTrans trans, Cluster cluster) throws APIException, IOException {
		super(trans,cluster);
		GregorianCalendar gc = new GregorianCalendar();
		start = gc.getTime();
		expires = trans.org().expiration(gc, Expiration.Future).getTime();
	}
	
	public URFutureApprove(AuthzTrans trans, ActionDAO<?,?> adao) {
		super(trans, adao);
		GregorianCalendar gc = new GregorianCalendar();
		start = gc.getTime();
		expires = trans.org().expiration(gc, Expiration.Future).getTime();
	}

	@Override
	public Result<List<Identity>> exec(AuthzTrans trans, UserRole ur) {
		Result<NsDAO.Data> rns = q.deriveNs(trans, ur.ns);
		if(rns.isOK()) {
			
			FutureDAO.Data data = new FutureDAO.Data();
			data.id=null; // let Create function assign UUID
			data.target=Function.FOP_USER_ROLE;
			
			data.memo = key(ur);
			data.start = start;
			data.expires = expires;
			try {
				data.construct = ur.to().bytify();
			} catch (IOException e) {
				return Result.err(e);
			}
			Result<List<Identity>> rapprovers = f.createFuture(trans, data, Function.FOP_USER_ROLE, ur.user, rns.value, "U");
			return rapprovers;
		} else {
			return Result.err(rns);
		}
	}
	
	@Override
	public String key(UserRole ur) {
		String expire;
		if(expires.before(start)) {
			expire = "' - EXPIRED ";
		} else {
			expire = "' - expiring ";
		}
		
		if(Question.OWNER.equals(ur.rname)) {
			return "Re-Validate Ownership for AAF Namespace '" + ur.ns + expire + Chrono.dateOnlyStamp(ur.expires);
		} else if(Question.ADMIN.equals(ur.rname)) {
			return "Re-Validate as Administrator for AAF Namespace '" + ur.ns + expire + Chrono.dateOnlyStamp(ur.expires);
		} else {
			return "Re-Approval in Role '" + ur.role + expire + Chrono.dateOnlyStamp(ur.expires);
		}
	}

}