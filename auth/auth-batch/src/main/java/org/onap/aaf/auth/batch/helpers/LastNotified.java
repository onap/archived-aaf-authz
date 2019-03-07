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

import org.onap.aaf.auth.batch.helpers.Notification.TYPE;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class LastNotified {
	private Map<String,Date> lastNotified = new TreeMap<>();
	private Session session;
	
	public LastNotified(Session session) {
		this.session = session;
	}
	
	public void add(Set<String> users) {
		StringBuilder query = new StringBuilder();
		startNotifyQuery(query);
		int cnt = 0;
    	for(String user : users) {
    		if(++cnt>1) {
    			query.append(',');
    		}
    		query.append('\'');
    		query.append(user);
    		query.append('\'');
    		if(cnt>=30) {
    			endNotifyQuery(query, Notification.TYPE.OA);
    			add(session.execute(query.toString()),lastNotified);
    			query.setLength(0);
    			startNotifyQuery(query);
    			cnt=0;
    		}
    	}
    	if(cnt>0) {
    		endNotifyQuery(query, Notification.TYPE.OA);
			add(session.execute(query.toString()),lastNotified);
    	}
	}

	public Date lastNotified(String user) {
		return lastNotified.get(user);
	}
	
	private void add(ResultSet result, Map<String, Date> lastNotified) {
    	for(Iterator<Row> iter = result.iterator(); iter.hasNext();) {
    		Row r = iter.next();
    		lastNotified.put(r.getString(0), r.getTimestamp(1));
    	}
	}

	private void startNotifyQuery(StringBuilder query) {
		query.append("SELECT user,last FROM authz.notify WHERE user in (");
	}
    
    private void endNotifyQuery(StringBuilder query, TYPE oa) {
    	query.append(") AND type=");
    	query.append(oa.idx());
    	query.append(';');
    }
}
