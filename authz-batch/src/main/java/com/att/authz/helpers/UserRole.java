/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import com.att.dao.aaf.cass.UserRoleDAO;
import com.att.inno.env.Env;
import com.att.inno.env.TimeTaken;
import com.att.inno.env.Trans;
import com.att.inno.env.util.Chrono;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class UserRole implements Cloneable {
	public static final List<UserRole> data = new ArrayList<UserRole>();
    public static final TreeMap<String,List<UserRole>> byUser = new TreeMap<String,List<UserRole>>();
    public static final TreeMap<String,List<UserRole>> byRole = new TreeMap<String,List<UserRole>>();

	public final String user, role, ns, rname;
	public final Date expires;

	public UserRole(String user, String ns, String rname, Date expires) {
		this.user = user;
		this.role = ns + '.' + rname;
		this.ns = ns;
		this.rname = rname;
		this.expires = expires;
	}

	public UserRole(String user, String role, String ns, String rname, Date expires) {
		this.user = user;
		this.role = role;
		this.ns = ns;
		this.rname = rname;
		this.expires = expires;
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
		int count = 0;
        try {
	        Iterator<Row> iter = results.iterator();
	        Row row;
	        tt = trans.start("Load UserRole", Env.SUB);
	        try {
		        while(iter.hasNext()) {
		        	++count;
		        	row = iter.next();
		        	UserRole ur = creator.create(row);
		        	data.add(ur);
		        	
		        	List<UserRole> lur = byUser.get(ur.user);
		        	if(lur==null) {
		        		lur = new ArrayList<UserRole>();
			        	byUser.put(ur.user, lur);
		        	}
		        	lur.add(ur);
		        	
		        	lur = byRole.get(ur.role);
		        	if(lur==null) {
		        		lur = new ArrayList<UserRole>();
			        	byRole.put(ur.role, lur);
		        	}
		        	lur.add(ur);
		        }
	        } finally {
	        	tt.done();
	        }
        } finally {
        	trans.info().log("Found",count,"UserRoles");
        }


	}

	public static Creator<UserRole> v2_0_11 = new Creator<UserRole>() {
		@Override
		public UserRole create(Row row) {
			return new UserRole(row.getString(0), row.getString(1), row.getString(2),row.getString(3),row.getDate(4));
		}

		@Override
		public String select() {
			return "select user,role,ns,rname,expires from authz.user_role";
		}
	};

	public UserRoleDAO.Data to() {
		UserRoleDAO.Data urd = new UserRoleDAO.Data();
		urd.user = user;
		urd.role = role;
		urd.ns = ns;
		urd.rname = rname;
		urd.expires = expires;
		return urd;
	}
	
	public String toString() {
		return "\"" + user + "\",\"" + role + "\",\""  + ns + "\",\"" + rname + "\",\""+ Chrono.dateOnlyStamp(expires);
	}

}