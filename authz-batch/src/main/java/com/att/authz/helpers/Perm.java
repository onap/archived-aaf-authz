/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.helpers;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import com.att.inno.env.Env;
import com.att.inno.env.TimeTaken;
import com.att.inno.env.Trans;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class Perm implements Comparable<Perm> {
    public static final TreeMap<Perm,Set<String>> data = new TreeMap<Perm,Set<String>>();
    public static final TreeMap<String,Perm> keys = new TreeMap<String,Perm>();

	public final String ns, type, instance, action,description;
	private String fullType = null, fullPerm = null, encode = null;
	public final Set<String> roles;
	
	public String encode() {
		if(encode == null) {
			encode = ns + '|' + type + '|' + instance + '|' + action;
		}
		return encode;
	}
	
	public String fullType() {
		if(fullType==null) {
			fullType = ns + '.' + type;
		}
		return fullType;
	}
	
	public String fullPerm() {
		if(fullPerm==null) {
			fullPerm = ns + '.' + type  + '|' + instance + '|' + action;
		}
		return fullPerm;
	}
	
	public Perm(String ns, String type, String instance, String action, String description, Set<String> roles) {
		this.ns = ns;
		this.type = type;
		this.instance = instance;
		this.action = action;
		this.description = description;
		// 2.0.11
//		this.full = encode();//ns+'.'+type+'|'+instance+'|'+action;
		this.roles = roles;
	}

	public static void load(Trans trans, Session session) {
        load(trans, session, "select ns, type, instance, action, description, roles from authz.perm;");
	}
	
	public static void loadOneNS(Trans trans, Session session, String ns) {
        load(trans, session, "select ns, type, instance, action, description, roles from authz.perm WHERE ns='" + ns + "';");
        
	}

	private static void load(Trans trans, Session session, String query) {
        //
        trans.info().log( "query: " + query );
        TimeTaken tt = trans.start("Read Perms", Env.REMOTE);
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
	        tt = trans.start("Load Perms", Env.SUB);
	        try {
		        while(iter.hasNext()) {
		        	row = iter.next();
		        	Perm pk = new Perm(row.getString(0),row.getString(1),row.getString(2),row.getString(3), row.getString(4), row.getSet(5,String.class));
		        	keys.put(pk.encode(), pk);
		        	data.put(pk,pk.roles);
		        }
	        } finally {
	        	tt.done();
	        }
        } finally {
        	trans.info().log("Found",data.size(),"perms");
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
	public int compareTo(Perm o) {
		return encode().compareTo(o.encode());
	}

}