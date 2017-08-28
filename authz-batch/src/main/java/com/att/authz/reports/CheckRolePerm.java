/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.reports;

import java.io.IOException;
import java.util.Set;

import com.att.authz.Batch;
import com.att.authz.env.AuthzTrans;
import com.att.authz.helpers.NS;
import com.att.authz.helpers.Perm;
import com.att.authz.helpers.Role;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Env;
import org.onap.aaf.inno.env.TimeTaken;
import org.onap.aaf.inno.env.util.Split;

public class CheckRolePerm extends Batch{

	public CheckRolePerm(AuthzTrans trans) throws APIException, IOException {
		super(trans.env());
		TimeTaken tt = trans.start("Connect to Cluster", Env.REMOTE);
		try {
			session = cluster.connect();
		} finally {
			tt.done();
		}
		NS.load(trans,session,NS.v2_0_11);
		Role.load(trans, session);
		Perm.load(trans, session);
	}

	@Override
	protected void run(AuthzTrans trans) {
        // Run for Roles
        trans.info().log("Checking for Role/Perm mis-match");
		
		String query;
        /// Evaluate from Role side
        for(Role roleKey : Role.data.keySet()) {
        	for(String perm : Role.data.get(roleKey)) {
        		Perm pk = Perm.keys.get(perm);
        		if(pk==null) {
    				NS ns=null;
        			String msg = perm + " in role " + roleKey.fullName() + " does not exist";
        			String newPerm;
        			String[] s = Split.split('|', perm);
        			if(s.length==3) {
	    				int i;
	    				String find = s[0];
	    				for(i=find.lastIndexOf('.');ns==null && i>=0;i=find.lastIndexOf('.', i-1)) {
	    					ns = NS.data.get(find.substring(0,i));
	    				}
	    				if(ns==null) {
	    					newPerm = perm;
	    				} else {
	    					newPerm = ns.name + '|' + s[0].substring(i+1) + '|' + s[1] + '|' + s[2];
	    				}
        			} else {
        				newPerm = perm;
        			}
        			if(dryRun) {
        				if(ns==null) {
        					trans.warn().log(msg, "- would remove role from perm;");
        				} else {
        					trans.warn().log(msg, "- would update role in perm;");
        				}
					} else {
        				if(ns!=null) {
            				query = "UPDATE authz.role SET perms = perms + {'" +
            						newPerm + "'}" 
		       		        		+ (roleKey.description==null?", description='clean'":"")
		       		        		+ " WHERE "
		       		        		+ "ns='" + roleKey.ns 
		       		        		+ "' AND name='" + roleKey.name + "';";
		       		        trans.warn().log("Fixing role in perm",query);   
		       		        session.execute(query);
        				}

	       		        query = "UPDATE authz.role SET perms = perms - {'"
	       		        		+ perm.replace("'", "''") + "'}"
	       		        		+ (roleKey.description==null?", description='clean'":"")
	       		        		+ " WHERE "
	       		        		+ "ns='" + roleKey.ns 
	       		        		+ "' AND name='" + roleKey.name + "';";
	       		        session.execute(query);
	       		        trans.warn().log(msg, "- removing role from perm");
//       		        env.info().log( "query: " + query );
        			}
        		} else {
	        		Set<String> p_roles = Perm.data.get(pk);
	        		if(p_roles!=null && !p_roles.contains(roleKey.encode())) {
	       				String msg = perm + " does not have role: " + roleKey;
	        			if(dryRun) {
					    trans.warn().log(msg,"- should add this role to this perm;");
					} else {
		       		        query = "update authz.perm set roles = roles + {'"
		       		        		+ roleKey.encode() + "'}"
		       		        		+ (pk.description==null?", description=''":"")
		       		        		+ " WHERE "
		       		        		+ "ns='" + pk.ns
		       		        		+ "' AND type='" + pk.type
		       		        		+ "' AND instance='" + pk.instance
		       		        		+ "' AND action='" + pk.action 
		       		        		+ "';";
		       		        session.execute(query);
		       		        trans.warn().log(msg,"- adding perm to role");
	        			}
	       				
	        		}
        		}
        	}
        }

        for(Perm permKey : Perm.data.keySet()) {
        	for(String role : Perm.data.get(permKey)) {
        		Role rk = Role.keys.get(role);
        		if(rk==null) {
        			String s = role + " in perm " + permKey.encode() + " does not exist";
        			if(dryRun) {
				    trans.warn().log(s,"- would remove perm from role;");
				} else {
	       		        query = "update authz.perm set roles = roles - {'"
	       		        		+ role.replace("'","''") + "'}"
	       		        		+ (permKey.description==null?", description='clean'":"")
	       		        		+ " WHERE "
	       		        		+ "ns='" + permKey.ns
	       		        		+ "' AND type='" + permKey.type
	       		        		+ "' AND instance='" + permKey.instance
	       		        		+ "' AND action='" + permKey.action + "';";
	       		        session.execute(query);
	       		        trans.warn().log(s,"- removing role from perm");
        			}
        		} else {
	        		Set<String> r_perms = Role.data.get(rk);
	        		if(r_perms!=null && !r_perms.contains(permKey.encode())) {
	       				String s ="Role '" + role + "' does not have perm: '" + permKey + '\'';
	        			if(dryRun) {
					    trans.warn().log(s,"- should add this perm to this role;");
					} else {
		       		        query = "update authz.role set perms = perms + {'"
		       		        		+ permKey.encode() + "'}"
		       		        		+ (rk.description==null?", description=''":"")
		       		        		+ " WHERE "
		       		        		+ "ns='" + rk.ns
		       		        		+ "' AND name='" + rk.name + "';";
		       		        session.execute(query);
		       		        trans.warn().log(s,"- adding role to perm");
	        			}
	        		}
        		}
        	}
        }

	}


	@Override
	protected void _close(AuthzTrans trans) {
        session.close();
        aspr.info("End " + this.getClass().getSimpleName() + " processing" );
	}
}
