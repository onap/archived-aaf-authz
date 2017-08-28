/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.helpers;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.onap.aaf.inno.env.Env;
import org.onap.aaf.inno.env.TimeTaken;
import org.onap.aaf.inno.env.Trans;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class Role implements Comparable<Role> {
    public static final TreeMap<Role,Set<String>> data = new TreeMap<Role,Set<String>>();
    public static final TreeMap<String,Role> keys = new TreeMap<String,Role>();

	public final String ns, name, description;
	private String full, encode;
	public final Set<String> perms;
	
	public Role(String full) {
		ns = name = description = "";
		this.full = full;
		perms = new HashSet<String>();
	}
	
	public Role(String ns, String name, String description,Set<String> perms) {
		this.ns = ns;
		this.name = name;
		this.description = description;
		this.full = null;
		this.encode = null;
		this.perms = perms;
	}
	
	public String encode() {
		if(encode==null) {
			encode = ns + '|' + name;
		} 
		return encode;
	}

	public String fullName() {
		if(full==null) {
			full = ns + '.' + name;
		} 
		return full;
	}

	public static void load(Trans trans, Session session ) {
		load(trans,session,"select ns, name, description, perms from authz.role;");
	}

	public static void loadOneNS(Trans trans, Session session, String ns ) {
		load(trans,session,"select ns, name, description, perms from authz.role WHERE ns='" + ns + "';");
	}

	private static void load(Trans trans, Session session, String query) {
        trans.info().log( "query: " + query );
        TimeTaken tt = trans.start("Read Roles", Env.REMOTE);
       
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
	        tt = trans.start("Load Roles", Env.SUB);
	        try {
		        while(iter.hasNext()) {
		        	row = iter.next();
		        	Role rk =new Role(row.getString(0),row.getString(1), row.getString(2),row.getSet(3,String.class));
		        	keys.put(rk.encode(), rk);
		        	data.put(rk,rk.perms);
		        }
	        } finally {
	        	tt.done();
	        }
        } finally {
        	trans.info().log("Found",data.size(),"roles");
        }


	}
	public String toString() {
		return encode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return encode().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return encode().equals(obj);
	}

	@Override
	public int compareTo(Role o) {
		return encode().compareTo(o.encode());
	}

	public static String fullName(String role) {
		return role.replace('|', '.');
	}
}