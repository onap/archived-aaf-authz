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
 */

package org.onap.aaf.auth.batch.helpers;

import org.onap.aaf.misc.env.LogTarget;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

public class CQLBatch {
	private Session session;
	private StringBuilder sb;
	private int hasAdded;
	private LogTarget log;

	public CQLBatch(LogTarget log, Session session) {
		this.log = log;
		this.session = session;
		sb = new StringBuilder();
		hasAdded = 0;
	}
	public StringBuilder begin() {
		sb.setLength(0);
		sb.append("BEGIN BATCH\n");
		hasAdded = sb.length();
		return sb;
	}
	
	private boolean end() {
		if(sb.length()==hasAdded) {
			return false;
		} else {
			sb.append("APPLY BATCH;\n");
			log.log(sb);
			return true;
		}
	}
	
	public ResultSet execute() {
		if(end()) {
			return session.execute(sb.toString());
		} else {
			return null;
		}
	}
	
	public ResultSet execute(boolean dryRun) {
		ResultSet rv = null;
		if(dryRun) {
			end();
		} else {
			rv = execute();
		}
		sb.setLength(0);
		return rv;
	}
	
	public ResultSet singleExec(StringBuilder query, boolean dryRun) {
		if(dryRun) {
			return null;
		} else {
			return session.execute(query.toString());
		}
	}
	
	public void touch(String table, int begin, int end, boolean dryRun) {
		StringBuilder sb = begin();
		for(int i=begin;i<end;++i) {
			sb.append("UPDATE cache SET touched=dateof(now()) WHERE name='");
			sb.append(table);
			sb.append("' AND seg=");
			sb.append(i);
			sb.append(";\n");
		}
		execute(dryRun);
	}
	
	public String toString() {
		return sb.toString();
	}
}
