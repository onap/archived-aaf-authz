/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.reports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.att.authz.Batch;
import com.att.authz.actions.Action;
import com.att.authz.actions.ActionDAO;
import com.att.authz.actions.CredDelete;
import com.att.authz.actions.CredPrint;
import com.att.authz.actions.FADelete;
import com.att.authz.actions.FAPrint;
import com.att.authz.actions.Key;
import com.att.authz.actions.URDelete;
import com.att.authz.actions.URFutureApprove;
import com.att.authz.actions.URFuturePrint;
import com.att.authz.actions.URPrint;
import com.att.authz.env.AuthzTrans;
import com.att.authz.helpers.Cred;
import com.att.authz.helpers.Cred.Instance;
import com.att.authz.helpers.Future;
import com.att.authz.helpers.Notification;
import com.att.authz.helpers.UserRole;
import com.att.authz.layer.Result;
import com.att.authz.org.Organization.Identity;
import com.att.dao.aaf.cass.CredDAO;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Env;
import org.onap.aaf.inno.env.TimeTaken;

public class Expiring extends Batch {
	
	private final Action<UserRole,Void> urDelete,urPrint;
	private final Action<UserRole,List<Identity>> urFutureApprove;
	private final Action<CredDAO.Data,Void> crDelete,crPrint;
	private final Action<Future,Void> faDelete;
//	private final Email email;
	private final Key<UserRole> memoKey;
	
	public Expiring(AuthzTrans trans) throws APIException, IOException {
		super(trans.env());
	    trans.info().log("Starting Connection Process");
	    TimeTaken tt0 = trans.start("Cassandra Initialization", Env.SUB);
	    try {
			urPrint = new URPrint("Expired:");
			crPrint = new CredPrint("Expired:");

			URFutureApprove ufr = new URFutureApprove(trans,cluster); 
			memoKey = ufr;
			
			if(isDryRun()) {
				urDelete = new URPrint("Would Delete:");
				// While Testing
//				urFutureApprove = ufr;
				urFutureApprove = new URFuturePrint("Would setup Future/Approvals");
				crDelete = new CredPrint("Would Delete:");
				faDelete = new FAPrint("Would Delete:");
//				email = new EmailPrint();

				TimeTaken tt = trans.start("Connect to Cluster", Env.REMOTE);
				try {
					session = cluster.connect();
				} finally {
					tt.done();
				}
	
			} else {
				TimeTaken tt = trans.start("Connect to Cluster with DAOs", Env.REMOTE);
				try {
					ActionDAO<UserRole,Void> adao;
					urDelete = adao = new URDelete(trans, cluster);
					urFutureApprove = new URFutureApprove(trans,adao);
					faDelete = new FADelete(trans, adao);

					crDelete = new CredDelete(trans, adao);
//					email = new Email();
					TimeTaken tt2 = trans.start("Connect to Cluster", Env.REMOTE);
					try {
						session = adao.getSession(trans);
					} finally {
						tt2.done();
					}
				} finally {
					tt.done();
				}
			}
			
			UserRole.load(trans, session, UserRole.v2_0_11);
			Cred.load(trans, session);
			Notification.load(trans, session, Notification.v2_0_14);
			Future.load(trans,session,Future.v2_0_15);
	    } finally {
	    	tt0.done();
	    }
	}

	@Override
	protected void run(AuthzTrans trans) {
		// Setup Date boundaries
		Date now = new Date();
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(now);
        gc.add(GregorianCalendar.MONTH, 1);
        Date future = gc.getTime();
        gc.setTime(now);
        gc.add(GregorianCalendar.MONTH, -1);
        Date tooLate = gc.getTime();
        int count = 0, deleted=0;
        
//        List<Notification> ln = new ArrayList<Notification>();
        TimeTaken tt;
                
        // Run for Expired Futures
        trans.info().log("Checking for Expired Futures");
        tt = trans.start("Delete old Futures", Env.REMOTE);
        try {
        	List<Future> delf = new ArrayList<Future>();
        	for(Future f : Future.data) {
        		AuthzTrans localTrans = env.newTransNoAvg();
        		if(f.expires.before(now)) {
        			faDelete.exec(localTrans, f);
        			delf.add(f);
        		}
        	}
        	Future.delete(delf);
        } finally {
        	tt.done();
        }

        // Run for Roles
        trans.info().log("Checking for Expired Roles");
        try {
        	for(UserRole ur : UserRole.data) {
        		AuthzTrans localTrans = env.newTransNoAvg();
        		if(ur.expires.before(tooLate)) {
        			if("owner".equals(ur.rname)) { // don't delete Owners, even if Expired
        				urPrint.exec(localTrans,ur);
        			} else {
            			urDelete.exec(localTrans,ur);
            			++deleted;
            			trans.logAuditTrail(trans.info());
        			}
        			++count;
        		} else if(ur.expires.before(future)) {
        			List<Future> fbm = Future.byMemo.get(memoKey.key(ur));
        			if(fbm==null || fbm.isEmpty()) {
	        			Result<List<Identity>> rapprovers = urFutureApprove.exec(localTrans, ur);
	        			if(rapprovers.isOK()) {
	        				for(Identity ou : rapprovers.value) {
//	        					Notification n = Notification.addApproval(localTrans,ou);
//	        					if(n.org==null) {
//	        						n.org = getOrgFromID(localTrans, ur.user);
//	        					}
//		        				ln.add(n);
		        				urPrint.exec(localTrans,ur);
		        				if(isDryRun()) {
		        					trans.logAuditTrail(trans.info());
		        				}
	        				}
	        			}
        			}
	        		++count;
	        	}
        	}
		} finally {
        	env.info().log("Found",count,"roles expiring before",future);
        	env.info().log("deleting",deleted,"roles expiring before",tooLate);
        }
        
//        // Email Approval Notification
//		email.subject("AAF Role Expiration Warning (ENV: %s)", batchEnv);
//		email.indent("");
//        for(Notification n: ln) {
//        	if(n.org==null) {
//        		trans.error().log("No Organization for Notification");
//        	} else if(n.update(trans, session, isDryRun())) {
//        		email.clear();
//        		email.addTo(n.user);
//				email.line(n.text(new StringBuilder()).toString());
//				email.exec(trans,n.org);
//        	}        	
//        }
        // Run for Creds
        trans.info().log("Checking for Expired Credentials");
        System.out.flush();
        count = 0;
        try {
        	CredDAO.Data crd = new CredDAO.Data();
        	Date last = null;
        	for( Cred creds : Cred.data.values()) {
        		AuthzTrans localTrans = env.newTransNoAvg();
				crd.id = creds.id;
        		for(int type : creds.types()) {
					crd.type = type;
        			for( Instance inst : creds.instances) {
        				if(inst.expires.before(tooLate)) {
        					crd.expires = inst.expires;
        					crDelete.exec(localTrans, crd);
        				} else if(last==null || inst.expires.after(last)) {
    						last = inst.expires;
    					}
        			}
        			if(last!=null) {
        				if(last.before(future)) {
        					crd.expires = last;
        					crPrint.exec(localTrans, crd);
	        				++count;
        				}
        			}
        		}
        	}
        } finally {
        	env.info().log("Found",count,"current creds expiring before",future);
        }
        
	}
	
	@Override
	protected void _close(AuthzTrans trans) {
        aspr.info("End " + this.getClass().getSimpleName() + " processing" );
        for(Action<?,?> action : new Action<?,?>[] {urDelete,crDelete}) {
        	if(action instanceof ActionDAO) {
        		((ActionDAO<?,?>)action).close(trans);
        	}
        }
        session.close();
	}

}
