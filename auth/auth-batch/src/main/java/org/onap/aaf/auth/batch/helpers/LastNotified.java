/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Modifications Copyright (C) 2018 IBM.
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
package org.onap.aaf.auth.batch.helpers;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.onap.aaf.auth.dao.cass.UserRoleDAO;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class LastNotified {
	private Map<String,Date> lastNotified = new TreeMap<>();
	private Session session;
	private static final Date never = new Date(0);
	
	public LastNotified(Session session) {
		this.session = session;
	}
	
	public void add(Set<String> users) {
		StringBuilder query = new StringBuilder();
		startQuery(query);
		int cnt = 0;
    	for(String user : users) {
    		if(++cnt>1) {
    			query.append(',');
    		}
    		query.append('\'');
    		query.append(user);
    		query.append('\'');
    		if(cnt>=30) {
    			endQuery(query);
    			add(session.execute(query.toString()),lastNotified);
    			query.setLength(0);
    			startQuery(query);
    			cnt=0;
    		}
    	}
    	if(cnt>0) {
    		endQuery(query);
			add(session.execute(query.toString()),lastNotified);
    	}
	}

	/**
	 * Note: target_key CAN also contain a Pipe.
	 * 
	 * @param user
	 * @param target
	 * @param target_key
	 * @return
	 */
	public Date lastNotified(String user, String target, String target_key) {
		String key = user + '|' + target + '|' + target_key;
		return lastNotified(key);
	}
	
	public Date lastNotified(String key) {
		Date rv = lastNotified.get(key);
		if(rv==null) {
			rv = never;
			lastNotified.put(key, rv);
		}
		return rv;
	}
	
	private Date add(ResultSet result, Map<String, Date> lastNotified) {
		Date last = null;
    	for(Iterator<Row> iter = result.iterator(); iter.hasNext();) {
    		Row r = iter.next();
    		String key = r.getString(0) + '|' +
    				     r.getString(1) + '|' +
    				     r.getString(2);
    		
    		lastNotified.put(key, last=r.getTimestamp(3));
    	}
    	return last;
	}

	private void startQuery(StringBuilder query) {
		query.append("SELECT user,target,key,last FROM authz.notified WHERE user in (");
	}

	private void endQuery(StringBuilder query) {
		query.append(");");
	}

	public void update(StringBuilder query,String user, String target, String key) {
		query.append("UPDATE authz.notified SET last=dateof(now()) WHERE user='");
		query.append(user);
		query.append("' AND target='");
		query.append(target);
		query.append("' AND key='");
		query.append(key);
		query.append("';");
	}

	public static String newKey(UserRoleDAO.Data urdd) {
		return urdd.user + "|ur|" + urdd.role;
	}

}
