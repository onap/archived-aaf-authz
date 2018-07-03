/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.helpers;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.onap.aaf.auth.actions.URDelete;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Chrono;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class UserRole implements Cloneable, CacheChange.Data  {
	public static final List<UserRole> data = new ArrayList<>();
    public static final TreeMap<String,List<UserRole>> byUser = new TreeMap<>();
    public static final TreeMap<String,List<UserRole>> byRole = new TreeMap<>();
	private final static CacheChange<UserRole> cache = new CacheChange<>(); 
	private static PrintStream urDelete=System.out,urRecover=System.err;
	private static int totalLoaded;
	private static int deleted;
	
	private Data urdd;

	public UserRole(String user, String ns, String rname, Date expires) {	
		urdd = new UserRoleDAO.Data();
		urdd.user = user;
		urdd.role = ns + '.' + rname;
		urdd.ns = ns;
		urdd.rname = rname;
		urdd.expires = expires;
	}

	public UserRole(String user, String role, String ns, String rname, Date expires) {
		urdd = new UserRoleDAO.Data();
		urdd.user = user;
		urdd.role = role;
		urdd.ns = ns;
		urdd.rname = rname;
		urdd.expires = expires;
	}

	public static void load(Trans trans, Session session, Creator<UserRole> creator ) {
		load(trans,session,creator,null);
	}

	public static void loadOneRole(Trans trans, Session session, Creator<UserRole> creator, String role) {
		load(trans,session,creator,"role='" + role +"' ALLOW FILTERING;");
	}
	
	public static void loadOneUser(Trans trans, Session session, Creator<UserRole> creator, String user ) {
		load(trans,session,creator,"role='"+ user +"';");
	}

	private static void load(Trans trans, Session session, Creator<UserRole> creator, String where) {
		String query = creator.query(where);
		trans.info().log( "query: " + query );
        TimeTaken tt = trans.start("Read UserRoles", Env.REMOTE);
       
        ResultSet results;
		try {
	        Statement stmt = new SimpleStatement( query );
	        results = session.execute(stmt);
        } finally {
        	tt.done();
        }
        try {
	        Iterator<Row> iter = results.iterator();
	        Row row;
	        tt = trans.start("Load UserRole", Env.SUB);
	        try {
		        while(iter.hasNext()) {
		        	++totalLoaded;
		        	row = iter.next();
		        	UserRole ur = creator.create(row);
		        	data.add(ur);
		        	
		        	List<UserRole> lur = byUser.get(ur.urdd.user);
		        	if(lur==null) {
		        		lur = new ArrayList<>();
			        	byUser.put(ur.urdd.user, lur);
		        	}
		        	lur.add(ur);
		        	
		        	lur = byRole.get(ur.urdd.role);
		        	if(lur==null) {
		        		lur = new ArrayList<>();
			        	byRole.put(ur.urdd.role, lur);
		        	}
		        	lur.add(ur);
		        }
	        } finally {
	        	tt.done();
	        }
        } finally {
        	trans.info().log("Loaded",totalLoaded,"UserRoles");
        }
	}
	
	public int totalLoaded() {
		return totalLoaded;
	}
	
	public int deleted() {
		return deleted;
	}
	
	@Override
	public void expunge() {
		data.remove(this);
		
		List<UserRole> lur = byUser.get(urdd.user);
		if(lur!=null) {
			lur.remove(this);
		}
	
		lur = byRole.get(urdd.role);
		if(lur!=null) {
			lur.remove(this);
		}
	}
	
	public static void setDeleteStream(PrintStream ds) {
		urDelete = ds;
	}

	public static void setRecoverStream(PrintStream ds) {
		urRecover = ds;
	}

	public static long count(Trans trans, Session session) {
		String query = "select count(*) from authz.user_role LIMIT 1000000;";
        trans.info().log( "query: " + query );
        TimeTaken tt = trans.start("Count Namespaces", Env.REMOTE);
        ResultSet results;
        try {
        	Statement stmt = new SimpleStatement(query).setReadTimeoutMillis(12000);
	        results = session.execute(stmt);
	        return results.one().getLong(0);
        } finally {
        	tt.done();
        }
	}


	public static Creator<UserRole> v2_0_11 = new Creator<UserRole>() {
		@Override
		public UserRole create(Row row) {
			return new UserRole(row.getString(0), row.getString(1), row.getString(2),row.getString(3),row.getTimestamp(4));
		}

		@Override
		public String select() {
			return "select user,role,ns,rname,expires from authz.user_role";
		}
	};

	public UserRoleDAO.Data urdd() {
		return urdd;
	}
	
	public String user() {
		return urdd.user;
	};
	
	public String role() {
		return urdd.role;
	}
	
	public String ns() {
		return urdd.ns;
	}
	
	public String rname() {
		return urdd.rname;
	}
	
	public Date expires() {
		return urdd.expires;
	}
	
	public void expires(Date time) {
		urdd.expires = time;
	}



	public String toString() {
		return "\"" + urdd.user + "\",\"" + urdd.role + "\",\""  + urdd.ns + "\",\"" + urdd.rname + "\",\""+ Chrono.dateOnlyStamp(urdd.expires);
	}

	public static UserRole get(String u, String r) {
		List<UserRole> lur = byUser.get(u);
		if(lur!=null) {
			for(UserRole ur : lur) {
				if(ur.urdd.role.equals(r)) {
					return ur;
				}
			}
		}
		return null;
	}
	
	// CACHE Calling
	private static final String logfmt = "%s UserRole - %s: %s-%s (%s, %s) expiring %s";
	private static final String replayfmt = "%s|%s|%s|%s|%s\n";
	private static final String deletefmt = "# %s\n"+replayfmt;
	
	// SAFETY - DO NOT DELETE USER ROLES DIRECTLY FROM BATCH FILES!!!
	// We write to a file, and validate.  If the size is iffy, we email Support
	public void delayDelete(AuthzTrans trans, String text, boolean dryRun) {
		String dt = Chrono.dateTime(urdd.expires);
		if(dryRun) {
			trans.info().printf(logfmt,text,"Would Delete",urdd.user,urdd.role,urdd.ns,urdd.rname,dt);
		} else {
			trans.info().printf(logfmt,text,"Staged Deletion",urdd.user,urdd.role,urdd.ns,urdd.rname,dt);
		}
		urDelete.printf(deletefmt,text,urdd.user,urdd.role,dt,urdd.ns,urdd.rname);
		urRecover.printf(replayfmt,urdd.user,urdd.role,dt,urdd.ns,urdd.rname);

		cache.delayedDelete(this);
		++deleted;
	}
	

	/**
	 * Calls expunge() for all deleteCached entries
	 */
	public static void resetLocalData() {
		cache.resetLocalData();
	}
	
	public static int sizeForDeletion() {
		return cache.cacheSize();
	}

	public static boolean pendingDelete(UserRole ur) {
		return cache.contains(ur);
	}

	public static void actuateDeletionNow(AuthzTrans trans, URDelete directDel) {
		for(UserRole ur : cache.getRemoved()) {
			directDel.exec(trans, ur, "Actuating UserRole Deletion");
		}
		cache.getRemoved().clear();
		cache.resetLocalData();
	}


}