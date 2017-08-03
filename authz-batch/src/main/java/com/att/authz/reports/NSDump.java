/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.reports;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;

import com.att.authz.Batch;
import com.att.authz.env.AuthzTrans;
import com.att.authz.helpers.Cred;
import com.att.authz.helpers.NS;
import com.att.authz.helpers.Perm;
import com.att.authz.helpers.Role;
import com.att.authz.helpers.UserRole;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.TimeTaken;

public class NSDump extends Batch{
	private PrintStream out = System.out;
	private final String ns, admin, owner;
	
	public NSDump(AuthzTrans trans) throws APIException, IOException {
		super(trans.env());
		if(args().length>0) {
			ns = args()[0];
		} else {
			throw new APIException("NSDump requires \"NS\" parameter");
		}
		admin = ns + "|admin";
		owner = ns + "|owner";

		TimeTaken tt = trans.start("Connect to Cluster", Env.REMOTE);
		try {
			session = cluster.connect();
		} finally {
			tt.done();
		}

		NS.loadOne(trans, session,NS.v2_0_11,ns);
		Role.loadOneNS(trans, session, ns);
		if(Role.data.keySet().size()>5) {
			UserRole.load(trans, session,UserRole.v2_0_11);
		} else {
			for(Role r : Role.data.keySet()) {
				UserRole.loadOneRole(trans, session, UserRole.v2_0_11, r.fullName());
			}
		}
		Perm.loadOneNS(trans,session,ns);
		Cred.loadOneNS(trans, session, ns);
	}

	@Override
	protected void run(AuthzTrans trans) {
		Date now = new Date();
		for(NS ns : NS.data.values()) {
			out.format("# Data for Namespace [%s] - %s\n",ns.name,ns.description);
			out.format("ns create %s",ns);
			boolean first = true;
			List<UserRole> owners = UserRole.byRole.get(owner);
			if(owners!=null)for(UserRole ur : owners) {
				if(first) {
					out.append(' ');
					first = false;
				} else {
					out.append(',');
				}
				out.append(ur.user);
			}
			first = true;
			List<UserRole> admins = UserRole.byRole.get(admin); 
			if(admins!=null)for(UserRole ur : admins) {
				if(first) {
					out.append(' ');
					first = false;
				} else {
					out.append(',');
				}
				out.append(ur.user);
			}
			out.println();
			
			// Load Creds
			Date last;
			for(Cred c : Cred.data.values()) {
				for(int i : c.types()) {
					last = c.last(i);
					if(last!=null && now.before(last)) {
						switch(i) {
							case 1:
								out.format("    user cred add %s %s\n", c.id,"new2you!");
								break;
							case 200:
								out.format("    # CERT needs registering for %s\n", c.id);
								break;
							default:
								out.format("    # Unknown Type for %s\n", c.id);
						}
					}
				}
			}
			
			// Load Roles
			for(Role r : Role.data.keySet()) {
				if(!"admin".equals(r.name) && !"owner".equals(r.name)) {
					out.format("  role create %s\n",r.fullName());
					List<UserRole> lur = UserRole.byRole.get(r.fullName());
					if(lur!=null)for(UserRole ur : lur) {
						if(ur.expires.after(now)) {
							out.format("    request role user add %s %s\n", ur.role,ur.user);
						}
					}
				}
			}

			// Load Perms
			for(Perm r : Perm.data.keySet()) {
				out.format("  perm create %s.%s %s %s\n",r.ns,r.type,r.instance,r.action);
				for(String role : r.roles) {
					out.format("    request perm grant %s.%s %s %s %s\n", r.ns,r.type,r.instance,r.action,Role.fullName(role));
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
