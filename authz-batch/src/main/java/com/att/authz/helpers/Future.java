/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import org.onap.aaf.inno.env.Env;
import org.onap.aaf.inno.env.TimeTaken;
import org.onap.aaf.inno.env.Trans;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class Future {
	public static final List<Future> data = new ArrayList<Future>();
	public static final TreeMap<String,List<Future>> byMemo = new TreeMap<String,List<Future>>();
	
	public final UUID id;
	public final String memo, target;
	public final Date start, expires;
	public Future(UUID id, String memo, String target, Date start, Date expires) {
		this.id = id;
		this.memo = memo;
		this.target = target;
		this.start = start;
		this.expires = expires;
	}

	public static void load(Trans trans, Session session, Creator<Future> creator) {
		trans.info().log( "query: " + creator.select() );
		ResultSet results;
		TimeTaken tt = trans.start("Load Futures", Env.REMOTE);
		try {
	        Statement stmt = new SimpleStatement(creator.select());
	        results = session.execute(stmt);
		} finally {
			tt.done();
		}
		
		int count = 0;
		tt = trans.start("Process Futures", Env.SUB);
		try {
        	for(Row row : results.all()) {
        		++count;
        		Future f = creator.create(row);
        		data.add(f);
        		
        		List<Future> lf = byMemo.get(f.memo);
        		if(lf == null) {
        			lf = new ArrayList<Future>();
        			byMemo.put(f.memo, lf);
        		}
        		lf.add(f);
        		
        	}
		} finally {
			trans.info().log("Found",count,"Futures");
		}
	}
	
	public static Creator<Future> v2_0_15 = new Creator<Future>() {
		@Override
		public Future create(Row row) {
			return new Future(row.getUUID(0),row.getString(1),row.getString(2),
					row.getDate(3),row.getDate(4));
		}

		@Override
		public String select() {
			return "select id,memo,target,start,expires from authz.future";
		}
	};
	
	public static void delete(List<Future> fl) {
		if(fl==null || fl.isEmpty()) {
			return;
		}
		for(Future f : fl) {
			data.remove(f);
		}
		// Faster to start over, then look for entries.
		byMemo.clear();
		for(Future f : data) {
			List<Future> lf = byMemo.get(f.memo);
			if(lf == null) {
				lf = new ArrayList<Future>();
				byMemo.put(f.memo, lf);
			}
			lf.add(f);
		}
	}
}
