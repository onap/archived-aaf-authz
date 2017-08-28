/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import com.att.authz.actions.Message;
import com.att.authz.env.AuthzTrans;
import com.att.authz.org.EmailWarnings;
import com.att.authz.org.Organization;
import com.att.authz.org.Organization.Notify;
import com.att.authz.org.Organization.Identity;
import com.att.authz.org.OrganizationException;
import com.att.authz.org.OrganizationFactory;
import org.onap.aaf.inno.env.Env;
import org.onap.aaf.inno.env.TimeTaken;
import org.onap.aaf.inno.env.Trans;
import org.onap.aaf.inno.env.util.Chrono;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class Notification {
	
    public static final TreeMap<String,List<Notification>> data = new TreeMap<String,List<Notification>>();
    public static final long now = System.currentTimeMillis();
    
    public final String user;
	public final Notify type;
	public final Date last;
	public final int checksum;
	public Message msg;
	private int current;
	public Organization org;
	public int count;
	private long graceEnds,lastdays;
	
	private Notification(String user, int type, Date last, int checksum) {
		this.user = user;
		this.type = Notify.from(type);
		this.last = last;
		this.checksum = checksum;
		current = 0;
		count = 0;
	}
	
	private Notification(String user, Notify type, Date last, int checksum) {
		this.user = user;
		this.type = type;
		this.last = last;
		this.checksum = checksum;
		current = 0;
		count = 0;
	}
	
	public static void load(Trans trans, Session session, Creator<Notification> creator ) {
		trans.info().log( "query: " + creator.select() );
        TimeTaken tt = trans.start("Load Notify", Env.REMOTE);
       
        ResultSet results;
		try {
	        Statement stmt = new SimpleStatement(creator.select());
	        results = session.execute(stmt);
        } finally {
        	tt.done();
        }
		int count = 0;
        tt = trans.start("Process Notify", Env.SUB);

        try {
        	for(Row row : results.all()) {
        		++count;
		        try {
		        	Notification not = creator.create(row);
		        	List<Notification> ln = data.get(not.user);
		        	if(ln==null) {
		        		ln = new ArrayList<Notification>();
		        		data.put(not.user, ln);
		        	}
		        	ln.add(not);
		        } finally {
		        	tt.done();
		        }
        	}
        } finally {
        	tt.done();
        	trans.info().log("Found",count,"Notify Records");
        }
	}
	
	public static Notification get(String user, Notify type) {
		List<Notification> ln = data.get(user);
		if(ln!=null) {
	    	for(Notification n : ln) {
	    		if(type.equals(n.type)) {
	    			return n;
	    		}
	    	}
		}
		return null;
	}

	private static Notification getOrCreate(String user, Notify type) {
		List<Notification> ln = data.get(user);
		Notification n = null;
		if(ln==null) {
			ln = new ArrayList<Notification>();
			data.put(user, ln);
		} else {
			for(Notification n2 : ln) {
	    		if(type.equals(n2.type)) {
	    			n=n2;
	    			break;
	    		}
	    	}
		}
		if(n==null) {
			n = new Notification(user, type, new Date(), 0);
			ln.add(n);
		}
		return n;
	}
	
	public static Notification add(AuthzTrans trans, UserRole ur) {
		Notification n = getOrCreate(ur.user,Notify.RoleExpiration);
		if(n.org==null) {
			try {
				n.org = OrganizationFactory.obtain(trans.env(), ur.ns);
			} catch (OrganizationException e) {
				trans.error().log(ur.ns, " does not have a Namespace");
			}
		}
		
		if(n.count==0) {
			EmailWarnings ew = n.org.emailWarningPolicy();
			n.graceEnds = ew.roleEmailInterval();
			n.lastdays = ew.emailUrgentWarning();
		}
		++n.count;

		/*
		StringBuilder sb = new StringBuilder();
		sb.append("ID: ");
		sb.append(ur.user);
		User ouser;
		try {
			ouser = n.org.getUser(trans, ur.user);
			if(ouser!=null) {
				sb.append(" (");
				sb.append(ouser.fullName());
				sb.append(')');
			}
		} catch (Exception e) {
		}
		sb.append("  Role: ");
		sb.append(ur.role);
		sb.append("  Expire");
		if(now<ur.expires.getTime()) {
			sb.append("s: ");
		} else {
			sb.append("d: ");
		}
		sb.append(Chrono.dateOnlyStamp(ur.expires));
		sb.append("\n  If you wish to extend, type\n");
		sb.append("\trole user extend ");
		sb.append(ur.role);
		sb.append(' ');
		sb.append(ur.user);
		sb.append("\n  If you wish to delete, type\n");
		sb.append("\trole user del ");
		sb.append(ur.role);
		sb.append(' ');
		sb.append(ur.user);
		sb.append('\n');
		n.msg.add(sb.toString());
		n.current=0;
		*/
		return n;
	}

	public static Notification addApproval(AuthzTrans trans, Identity ou) {
		Notification n = getOrCreate(ou.id(),Notify.Approval);
		if(n.org==null) {
			n.org = ou.org();
		}
		if(n.count==0) { // first time.
			EmailWarnings ew = n.org.emailWarningPolicy();
			n.graceEnds = ew.apprEmailInterval();
			n.lastdays = ew.emailUrgentWarning();
		}
		++n.count;
		return n;
	}

	public static Creator<Notification> v2_0_14 = new Creator<Notification>() {
		@Override
		public Notification create(Row row) {
			return new Notification(row.getString(0), row.getInt(1), row.getDate(2),row.getInt(3));
		}

		@Override
		public String select() {
			return "select user,type,last,checksum from authz.notify";
		}
	};

	public void set(Message msg) {
		this.msg = msg; 
	}

	public int checksum() {
		if(current==0) {
			for(String l : msg.lines) {
				for(byte b : l.getBytes()) {
					current+=b;
				}
			}
		}
		return current;
	}
	
	public boolean update(AuthzTrans trans, Session session, boolean dryRun) {
		String update = update();
		if(update!=null) {
			if(dryRun) {
				trans.info().log(update);
			} else {
				session.execute(update);
			}
			return true; // Updated info, expect to notify
		}
		return false;
	}

	/** 
	 * Returns an Update String for CQL if there is data.
	 * 
	 * Returns null if nothing to update
	 * @return
	 */
	private String update() {
		// If this has been done before, there is no change in checkSum and the last time notified is within GracePeriod
		if(checksum!=0 && checksum()==checksum && now < last.getTime()+graceEnds && now > last.getTime()+lastdays) {
			return null;
		} else {
			return "UPDATE authz.notify SET last = '" +
					Chrono.dateOnlyStamp(last) +
					"', checksum=" +
					current +
					" WHERE user='" +
					user + 
					"' AND type=" +
					type.getValue() +
					";";
		}
	}

//	public void text(Email email) {
//		for(String s : msg) {
//			email.line(s);
//		}
//	}
//
	public String toString() {
		return "\"" + user + "\",\"" + type.name() + "\",\""  + Chrono.dateOnlyStamp(last);
	}
}