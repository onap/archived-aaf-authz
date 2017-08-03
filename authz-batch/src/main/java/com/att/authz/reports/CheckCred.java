/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.reports;

import java.io.IOException;

import com.att.authz.Batch;
import com.att.authz.env.AuthzTrans;
import com.att.authz.helpers.Cred;
import com.att.authz.helpers.Cred.Instance;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.TimeTaken;
import com.att.inno.env.util.Chrono;

public class CheckCred extends Batch{

	public CheckCred(AuthzTrans trans) throws APIException, IOException {
		super(trans.env());
		TimeTaken tt = trans.start("Connect to Cluster", Env.REMOTE);
		try {
			session = cluster.connect();
		} finally {
			tt.done();
		}
    	
		Cred.load(trans, session);
	}

	@Override
	protected void run(AuthzTrans trans) {
		String query;
		for(Cred cred : Cred.data.values()) {
			for(Instance inst : cred.instances) {
				if(inst.other==0) {
					if(dryRun) {
	    				trans.warn().log("Ensuring 'other' is numeric");
	        		} else {
	       		        query = "UPDATE authz.cred SET other=0 WHERE "
	       		        			+ "id='" + cred.id   
	       		        			+ "' AND type=" + inst.type
	       		        			+ " AND expires='" + Chrono.dateStamp(inst.expires)
	       		        			+ "';";
	       		        session.execute(query);
	       		        trans.warn().log("resetting 'other'",query);
					}
				}
			}
		}

	}        
		/*
        /// Evaluate 
		for(UserRole urKey : UserRole.data) {
        	NSSplit nss = NS.deriveParent(urKey.role);
        	if(nss==null && NS.data.size()>0 ) { // there is no Namespace for this UserRole
        		if(dryRun) {
					trans.warn().printf("Would delete %s %s, which has no corresponding Namespace",urKey.user,urKey.role);
        		} else {
	   		        query = "DELETE FROM authz.user_role WHERE "
			        			+ "user='" + urKey.user 
			        			+ "' AND role='" + urKey.role
			        			+ "';";
			        session.execute(query);
					trans.warn().printf("Deleting %s %s, which has no corresponding Namespace",urKey.user,urKey.role);
        		}
        	} else if(urKey.ns == null || urKey.rname == null || !urKey.role.equals(urKey.ns+'.'+urKey.rname)) {
        		if(dryRun) {
    				trans.warn().log(urKey,"needs to be split and added to Record (", urKey.ns, urKey.rname,")");
        		} else {
       		        query = "UPDATE authz.user_role SET ns='" + nss.ns 
       		        			+ "', rname='" + nss.other
       		        			+ "' WHERE "
       		        			+ "user='" + urKey.user 
       		        			+ "' AND role='" + urKey.role
       		        			+ "';";
       		        session.execute(query);
       		        trans.warn().log("Setting ns and rname",query);
				}
			}
		}
	}
	*/
	@Override
	protected void _close(AuthzTrans trans) {
        session.close();
        aspr.info("End " + this.getClass().getSimpleName() + " processing" );
	}
}
