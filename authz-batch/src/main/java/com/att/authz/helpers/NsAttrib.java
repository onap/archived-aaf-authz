/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.onap.aaf.inno.env.Env;
import org.onap.aaf.inno.env.TimeTaken;
import org.onap.aaf.inno.env.Trans;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class NsAttrib  {
	public static final List<NsAttrib> data = new ArrayList<NsAttrib>();
    public static final TreeMap<String,List<NsAttrib>> byKey = new TreeMap<String,List<NsAttrib>>();
    public static final TreeMap<String,List<NsAttrib>> byNS = new TreeMap<String,List<NsAttrib>>();

	public final String ns,key,value;
	
	public NsAttrib(String ns, String key, String value) {
		this.ns = ns;
		this.key = key;
		this.value = value;
	}
	
	public static void load(Trans trans, Session session, Creator<NsAttrib> creator ) {
		trans.info().log( "query: " + creator.select() );
        ResultSet results;
        TimeTaken tt = trans.start("Load NsAttributes", Env.REMOTE);
		try {
	        Statement stmt = new SimpleStatement(creator.select());
	        results = session.execute(stmt);
        } finally {
        	tt.done();
        }
		int count = 0;
        tt = trans.start("Process NsAttributes", Env.SUB);

        try {
        	for(Row row : results.all()) {
        		++count;
	        	NsAttrib ur = creator.create(row);
	        	data.add(ur);
	        	
	        	List<NsAttrib> lna = byKey.get(ur.key);
	        	if(lna==null) {
	        		lna = new ArrayList<NsAttrib>();
		        	byKey.put(ur.key, lna);
	        	}
	        	lna.add(ur);
	        	
	        	lna = byNS.get(ur.ns);
	        	if(lna==null) {
	        		lna = new ArrayList<NsAttrib>();
		        	byNS.put(ur.ns, lna);
	        	}
	        	lna.add(ur);
        	}
        } finally {
        	tt.done();
        	trans.info().log("Found",count,"NS Attributes");
        }
	}

	public static Creator<NsAttrib> v2_0_11 = new Creator<NsAttrib>() {
		@Override
		public NsAttrib create(Row row) {
			return new NsAttrib(row.getString(0), row.getString(1), row.getString(2));
		}

		@Override
		public String select() {
			return "select ns,key,value from authz.ns_attrib";
		}
	};


	public String toString() {
		return "\"" + ns + "\",\"" + key + "\",\""  + value;
	}

}