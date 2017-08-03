/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.reports;

import java.io.IOException;
import java.util.List;

import com.att.authz.Batch;
import com.att.authz.env.AuthzTrans;
import com.att.authz.helpers.NS;
import com.att.authz.helpers.NsAttrib;
import com.att.authz.helpers.Perm;
import com.att.authz.helpers.Role;
import com.att.dao.aaf.cass.NsType;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.TimeTaken;

public class CheckNS extends Batch{

	public CheckNS(AuthzTrans trans) throws APIException, IOException {
		super(trans.env());
		TimeTaken tt = trans.start("Connect to Cluster", Env.REMOTE);
		try {
			session = cluster.connect();
		} finally {
			tt.done();
		}
        NS.load(trans, session,NS.v2_0_11);
		Role.load(trans, session);
		Perm.load(trans, session);
		NsAttrib.load(trans, session, NsAttrib.v2_0_11);
	}

	@Override
	protected void run(AuthzTrans trans) {
		
		String msg;
		String query;
        trans.info().log(STARS, msg = "Checking for NS type mis-match", STARS);
		TimeTaken tt = trans.start(msg, Env.SUB);
		try {
			for(NS ns : NS.data.values()) {
				if(ns.description==null) {
					trans.warn().log("Namepace description is null. Changing to empty string.");
					if(dryRun) {
						trans.warn().log("Namepace description is null. Changing to empty string");
					} else {
	       		        query = "UPDATE authz.ns SET description='' WHERE name='" + ns.name +"';";
	       		        session.execute(query);
					}
				}
				int scope = count(ns.name,'.');
				NsType nt;
				switch(scope) {
					case 0:
						nt = NsType.DOT;
						break;
					case 1:
						nt = NsType.ROOT;
						break;
					case 2:
						nt = NsType.COMPANY;
						break;
					default:
						nt = NsType.APP;
						break;
				}
				if(ns.type!=nt.type || ns.scope !=scope) {
					if(dryRun) {
						trans.warn().log("Namepace",ns.name,"has no type.  Should change to ",nt.name());
					} else {
	       		        query = "UPDATE authz.ns SET type=" + nt.type + ", scope=" + scope + " WHERE name='" + ns.name +"';";
						trans.warn().log("Namepace",ns.name,"changing to",nt.name()+":",query);
	       		        session.execute(query);
					}
				}
			}
		} finally {
			tt.done();
		}
		

        trans.info().log(STARS, msg = "Checking for NS admin/owner mis-match", STARS);
		tt = trans.start(msg, Env.SUB);
		try {
	        /// Evaluate 
	        for(NS nk : NS.data.values()) {
	        	//String name; 
	        	String roleAdmin = nk.name+"|admin";
	        	String roleAdminPrev = nk.name+".admin";
	        	String roleOwner = nk.name+"|owner";
	        	String roleOwnerPrev = nk.name+".owner";
	        	String permAll = nk.name+"|access|*|*";
	        	String permAllPrev = nk.name+".access|*|*";
	        	String permRead = nk.name+"|access|*|read";
	        	String permReadPrev = nk.name+".access|*|read";
	        	// Admins
	        	
	        	Role rk = Role.keys.get(roleAdmin); // accomodate new role key
	        	// Role Admin should exist 
	        	if(rk==null) {
	        		if(dryRun) {
	        			trans.warn().log(nk.name + " is missing role: " + roleAdmin);
	        		} else {
	       		        query = "INSERT INTO authz.role(ns, name, description, perms) VALUES ('"
	       		        		+ nk.name 
	       		        		+ "','admin','Automatic Administration',"
	       		        		+ "{'" + nk.name + "|access|*|*'});";
	       		        session.execute(query);
	       		        env.info().log(query);
	       		        
	       		        
	       		        if(Role.keys.get(roleAdminPrev)!=null) {
			    			query = "UPDATE authz.role set perms = perms + "
			   		        		+ "{'" + roleAdminPrev + "'} "
			   		        		+ "WHERE ns='"+ nk.name + "' AND "
			   		        		+ "name='admin'"
			   		        		+ ";";
		       		        session.execute(query);
		       		        env.info().log(query);
	       		        }
	        		}
	        	} else {
	               	// Role Admin should be linked to Perm All 
	        		if(!rk.perms.contains(permAll)) {
		        		if(dryRun) {
		        			trans.warn().log(roleAdmin,"is not linked to",permAll);
		        		} else {
			    			query = "UPDATE authz.role set perms = perms + "
			   		        		+ "{'" + nk.name + "|access|*|*'} "
			   		        		+ "WHERE ns='"+ nk.name + "' AND "
			   		        		+ "name='admin'"
			   		        		+ ";";
			   		        session.execute(query);
			   		        env.info().log(query);
			   		        
			   		        if(rk.perms.contains(permAllPrev)) {
				    			query = "UPDATE authz.role set perms = perms - "
				   		        		+ "{'" + nk.name + ".access|*|*'} "
				   		        		+ "WHERE ns='"+ nk.name + "' AND "
				   		        		+ "name='admin'"
				   		        		+ ";";
				   		        session.execute(query);
				   		        env.info().log(query);
			   		        }
		        		}
	        		}
	               	// Role Admin should not be linked to Perm Read 
	        		if(rk.perms.contains(permRead)) {
		        		if(dryRun) {
		        			trans.warn().log(roleAdmin,"should not be linked to",permRead);
		        		} else {
			    			query = "UPDATE authz.role set perms = perms - "
			   		        		+ "{'" + nk.name + "|access|*|read'} "
			   		        		+ "WHERE ns='"+ nk.name + "' AND "
			   		        		+ "name='admin'"
			   		        		+ ";";
			   		        session.execute(query);
			   		        env.info().log(query);
		        		}
	        		}
	        	}
	        	
	        	Perm pk = Perm.keys.get(permAll);
	        	if(pk==null) {
	    			trans.warn().log(nk.name + " is missing perm: " + permAll);
	        		if(!dryRun) {
	       		        query = "INSERT INTO authz.perm(ns, type,instance,action,description, roles) VALUES ('"
	       		        		+ nk.name 
	       		        		+ "','access','*','*','Namespace Write',"
	       		        		+ "{'" + nk.name + "|admin'});";
	       		        session.execute(query);
	       		        env.info().log(query);
	
	        		}
	        	} else {
		        	// PermALL should be linked to Role Admin
		        	if(!pk.roles.contains(roleAdmin)) {
	        			trans.warn().log(permAll,"is not linked to",roleAdmin);
		        		if(!dryRun) {
			    			query = "UPDATE authz.perm set roles = roles + "
			   		        		+ "{'" + nk.name + "|admin'} WHERE "
			   		        		+ "ns='"+ pk.ns + "' AND "
			   		        		+ "type='access' AND instance='*' and action='*'"
			   		        		+ ";";
			   		        session.execute(query);
			   		        env.info().log(query);
			   		        
			   		        if(pk.roles.contains(roleAdminPrev)) {
				    			query = "UPDATE authz.perm set roles = roles - "
				   		        		+ "{'" + nk.name + ".admin'} WHERE "
				   		        		+ "ns='"+ pk.ns + "' AND "
				   		        		+ "type='access' AND instance='*' and action='*'"
				   		        		+ ";";
				   		        session.execute(query);
				   		        env.info().log(query);

			   		        }
		        		}
		        	}
		        	
		        	// PermALL should be not linked to Role Owner
		        	if(pk.roles.contains(roleOwner)) {
		        		trans.warn().log(permAll,"should not be linked to",roleOwner);
		        		if(!dryRun) {
			    			query = "UPDATE authz.perm set roles = roles - "
			   		        		+ "{'" + nk.name + "|owner'} WHERE "
			   		        		+ "ns='"+ pk.ns + "' AND "
			   		        		+ "type='access' AND instance='*' and action='*'"
			   		        		+ ";";
			   		        session.execute(query);
			   		        env.info().log(query);
		        		}
		        	}
	
	        	}
	
	        	
	        	
	        	// Owner
	        	rk = Role.keys.get(roleOwner);
	        	if(rk==null) {
	    			trans.warn().log(nk.name + " is missing role: " + roleOwner);
	        		if(!dryRun) {
	       		        query = "INSERT INTO authz.role(ns, name, description, perms) VALUES('"
	       		        		+ nk.name 
	       		        		+ "','owner','Automatic Owners',"
	       		        		+ "{'" + nk.name + "|access|*|read'});";
	       		        session.execute(query);
	       		        env.info().log(query);
	
	        		}
	        	} else { 
		        	// Role Owner should be linked to permRead
		        	if(!rk.perms.contains(permRead)) {
	        			trans.warn().log(roleOwner,"is not linked to",permRead);
		        		if(!dryRun) {
			    			query = "UPDATE authz.role set perms = perms + "
			   		        		+ "{'" + nk.name + "|access|*|read'} "
			   		        		+ "WHERE ns='"+ nk.name + "' AND "
			   		        		+ "name='owner'"
			   		        		+ ";";
			   		        session.execute(query);
			   		        env.info().log(query);
			   		        
			   		        if(rk.perms.contains(permReadPrev)) {
				    			query = "UPDATE authz.role set perms = perms - "
				   		        		+ "{'" + nk.name + ".access|*|read'} "
				   		        		+ "WHERE ns='"+ nk.name + "' AND "
				   		        		+ "name='owner'"
				   		        		+ ";";
				   		        session.execute(query);
				   		        env.info().log(query);

			   		        }
		        		}
		        	}
	               	// Role Owner should not be linked to PermAll 
	        		if(rk.perms.contains(permAll)) {
	        			trans.warn().log(roleAdmin,"should not be linked to",permAll);
		        		if(!dryRun) {
			    			query = "UPDATE authz.role set perms = perms - "
			   		        		+ "{'" + nk.name + "|access|*|*'} "
			   		        		+ "WHERE ns='"+ nk.name + "' AND "
			   		        		+ "name='admin'"
			   		        		+ ";";
			   		        session.execute(query);
			   		        env.info().log(query);
		        		}
	        		}
	
	        	}
	
	        	pk = Perm.keys.get(permRead);
	        	if(pk==null) {
	       			trans.warn().log(nk.name + " is missing perm: " + permRead);
	           		if(!dryRun) {
	       		        query = "INSERT INTO authz.perm(ns, type,instance,action,description, roles) VALUES ('"
	       		        		+ nk.name 
	       		        		+ "','access','*','read','Namespace Read',"
	       		        		+ "{'" + nk.name + "|owner'});";
	       		        session.execute(query);
	       		        env.info().log(query);
	        		}
	        	} else {
	        		// PermRead should be linked to roleOwner
	        		if(!pk.roles.contains(roleOwner)) {
	        			trans.warn().log(permRead, "is not linked to", roleOwner);
	        			if(!dryRun) {
			    			query = "UPDATE authz.perm set roles = roles + "
			   		        		+ "{'" + nk.name + "|owner'} WHERE "
			   		        		+ "ns='"+ pk.ns + "' AND "
			   		        		+ "type='access' AND instance='*' and action='read'"
			   		        		+ ";";
			   		        session.execute(query);
			   		        env.info().log(query);
			   		        
			   		        if(pk.roles.contains(roleOwnerPrev)) {
				    			query = "UPDATE authz.perm set roles = roles - "
				   		        		+ "{'" + nk.name + ".owner'} WHERE "
				   		        		+ "ns='"+ pk.ns + "' AND "
				   		        		+ "type='access' AND instance='*' and action='read'"
				   		        		+ ";";
				   		        session.execute(query);
				   		        env.info().log(query);

			   		        }
		        		}
		        	}
		        	// PermRead should be not linked to RoleAdmin
		        	if(pk.roles.contains(roleAdmin)) {
		        		if(dryRun) {
		        			trans.warn().log(permRead,"should not be linked to",roleAdmin);
		        		} else {
			    			query = "UPDATE authz.perm set roles = roles - "
			   		        		+ "{'" + nk.name + "|admin'} WHERE "
			   		        		+ "ns='"+ pk.ns + "' AND "
			   		        		+ "type='access' AND instance='*' and action='read'"
			   		        		+ ";";
			   		        session.execute(query);
			   		        env.info().log(query);
		        		}
		        	}
	        	}
	
	
	        	int dot = nk.name.lastIndexOf('.');
	        	String parent;
	        	if(dot<0) {
	        		parent = ".";
	        	} else {
	        		parent = nk.name.substring(0, dot);
	        	}
	        	
	        	if(!parent.equals(nk.parent)) {
	        		if(dryRun) {
	        			trans.warn().log(nk.name + " is missing namespace data");
	        		} else {
		   		        query = "UPDATE authz.ns SET parent='"+parent+"'" +
		   		        		" WHERE name='" + nk.name + "';";
		   		        session.execute(query);
		   		        env.info().log(query);
	        		}
	        	}
	        
	        // During Migration:
	        List<NsAttrib> swm = NsAttrib.byNS.get(nk.name);
	        boolean hasSwmV1 = false;
	        if(swm!=null) {for(NsAttrib na : swm) {
	        	if("swm".equals(na.key) && "v1".equals(na.value)) {
	        		hasSwmV1=true;
	        		break;
	        	}
	        }}
	        String roleMem = nk.name+"|member";
	        Role rm = Role.keys.get(roleMem); // Accommodate new role key
	        if(rm==null && hasSwmV1) {
	        	query = "INSERT INTO authz.role(ns, name, description, perms) VALUES ('"
   		        		+ nk.name 
   		        		+ "','member','Member',"
   		        		+ "{'" + nk.name + "|access|*|read'});";
   		        session.execute(query);
	   		     query = "UPDATE authz.role set perms = perms + "
			        		+ "{'" + nk.name + "|access|*|read'} "
			        		+ "WHERE ns='"+ nk.name + "' AND "
			        		+ "name='member'"
			        		+ ";";
	     		session.execute(query);
	     		env.info().log(query);
	        }
	        if(rm!=null)  {
	        	if(!rm.perms.contains(permRead)) {
	        		if(isDryRun()) {
	        		     env.info().log(nk.name+"|member needs " + nk.name + "|access|*|read");
	        		} else {
		        		query = "UPDATE authz.perm set roles = roles + "
		   		        		+ "{'" + nk.name + "|member'} WHERE "
		   		        		+ "ns='"+ pk.ns + "' AND "
		   		        		+ "type='access' AND instance='*' and action='read'"
		   		        		+ ";";
		        		session.execute(query);
		        		env.info().log(query);
		        		query = "UPDATE authz.role set perms = perms + "
		   		        		+ "{'" + nk.name + "|access|*|read'"
		   		        		+ (hasSwmV1?",'"+nk.name+"|swm.star|*|*'":"")
		   		        			+ "} "
		   		        		+ "WHERE ns='"+ nk.name + "' AND "
		   		        		+ "name='member'"
		   		        		+ ";";
		        		session.execute(query);
		        		env.info().log(query);
		        		if(hasSwmV1) {
			        		query = "UPDATE authz.perm set roles = roles + "
			   		        		+ "{'" + nk.name + "|member'} WHERE "
			   		        		+ "ns='"+ pk.ns + "' AND "
			   		        		+ "type='swm.star' AND instance='*' and action='*'"
			   		        		+ ";";
			        		session.execute(query);
			        		env.info().log(query);
		        		}
	        		}
	        	}
	        }
	        

	        
	        // Best Guess Owner
	        
//	        owner = Role.keys.get(ns.)
	        }
		} finally {
			tt.done();
		}
	
	}


	@Override
	protected void _close(AuthzTrans trans) {
        session.close();
        aspr.info("End " + this.getClass().getSimpleName() + " processing" );
	}
}
