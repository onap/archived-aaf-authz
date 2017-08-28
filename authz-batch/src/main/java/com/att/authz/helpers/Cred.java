/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

public class Cred  {
    public static final TreeMap<String,Cred> data = new TreeMap<String,Cred>();

	public final String id;
	public final List<Instance> instances;
	
	public Cred(String id) {
		this.id = id;
		instances = new ArrayList<Instance>();
	}
	
	public static class Instance {
		public final int type;
		public final Date expires;
		public final Integer other;
		
		public Instance(int type, Date expires, Integer other) {
			this.type = type;
			this.expires = expires;
			this.other = other;
		}
	}
	
	public Date last(final int type) {
		Date last = null;
		for(Instance i : instances) {
			if(i.type==type && (last==null || i.expires.after(last))) {
				last = i.expires;
			}
		}
		return last;
	}

	
	public Set<Integer> types() {
		Set<Integer> types = new HashSet<Integer>();
		for(Instance i : instances) {
			types.add(i.type);
		}
		return types;
	}

	public static void load(Trans trans, Session session ) {
		load(trans, session,"select id, type, expires, other from authz.cred;");
		
	}

	public static void loadOneNS(Trans trans, Session session, String ns ) {
		load(trans, session,"select id, type, expires, other from authz.cred WHERE ns='" + ns + "';");
	}

	private static void load(Trans trans, Session session, String query) {

        trans.info().log( "query: " + query );
        TimeTaken tt = trans.start("Read Creds", Env.REMOTE);
       
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
	        tt = trans.start("Load Roles", Env.SUB);
	        try {
		        while(iter.hasNext()) {
		        	++count;
		        	row = iter.next();
		        	String id = row.getString(0);
		        	Cred cred = data.get(id);
		        	if(cred==null) {
		        		cred = new Cred(id);
		        		data.put(id, cred);
		        	}
		        	cred.instances.add(new Instance(row.getInt(1), row.getDate(2), row.getInt(3)));
		        }
	        } finally {
	        	tt.done();
	        }
        } finally {
        	trans.info().log("Found",count,"creds");
        }


	}
	public String toString() {
		StringBuilder sb = new StringBuilder(id);
		sb.append('[');
		for(Instance i : instances) {
			sb.append('{');
			sb.append(i.type);
			sb.append(",\"");
			sb.append(i.expires);
			sb.append("\"}");
		}
		sb.append(']');
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return id.equals(obj);
	}

}