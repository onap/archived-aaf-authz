/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Modifications Copyright (C) 2019 IBM.
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

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.onap.aaf.auth.batch.helpers.Cred.Instance;
import org.onap.aaf.auth.batch.helpers.ExpireRange.Range;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Split;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class LastNotified {
    private Map<String,Date> lastNotifiedVar = new TreeMap<>();
    private Session session;
    public static final Date NEVER = new Date(0);
    private static final String SELECT = "SELECT user,target,key,last FROM authz.notified";
    
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
                add(session.execute(query.toString()),lastNotifiedVar, (x,y) -> false);
                query.setLength(0);
                startQuery(query);
                cnt=0;
            }
        }
        if(cnt>0) {
            endQuery(query);
            add(session.execute(query.toString()),lastNotifiedVar, (x,y) -> false);
        }
    }

    /**
     * Note: target_key CAN also contain a Pipe.
     * 
     * @param user
     * @param target
     * @param targetkey
     * @return
     */
    public Date lastNotified(String user, String target, String targetkey) {
        String key = user + '|' + target + '|' + (targetkey==null?"":targetkey);
        return lastNotified(key);
    }
    
    public Date lastNotified(String key) {
        Date d = lastNotifiedVar.get(key);
        return d==null?NEVER:d;
    }
    
    private Date add(ResultSet result, Map<String, Date> lastNotified, MarkDelete md) {
        Date last = null;
        Row r;
        for(Iterator<Row> iter = result.iterator(); iter.hasNext();) {
            r = iter.next();
            String ttKey = r.getString(1) + '|' +
                            r.getString(2);
 
            String fullKey = r.getString(0) + '|' +
                             ttKey;
            last=r.getTimestamp(3);
            if(!md.process(fullKey, last)) {
                lastNotified.put(fullKey, last);
                Date d = lastNotified.get(ttKey);
                if(d==null || d.after(last)) { // put most recent, if different
                    lastNotified.put(ttKey, last);
                }
            }
        }
        return last;
    }
    
    @FunctionalInterface
    private interface MarkDelete {
        boolean process(String fullKey, Date last);
    }

    private void startQuery(StringBuilder query) {
        query.append(SELECT + " WHERE user in (");
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
        query.append("';\n");
    }

    public LastNotified loadAll(Trans trans, final Range delRange, final CSV.Writer cw) {
        trans.debug().log( "query: ",SELECT );
        TimeTaken tt = trans.start("Read all LastNotified", Env.REMOTE);

        ResultSet results;
        try {
            Statement stmt = new SimpleStatement( SELECT );
            results = session.execute(stmt);
            add(results,lastNotifiedVar, (fullKey, last) ->  {
                if(delRange.inRange(last)) {
                    String[] params = Split.splitTrim('|', fullKey,3);
                    if(params.length==3) {
                        cw.row("notified",params[0],params[1],params[2]);
                        return true;
                    }
                }
                return false;
            });
        } finally {
            tt.done();
        }
        return this;
    }

    public static String newKey(UserRole ur) {
        return "ur|" + ur.user() + '|'+ur.role();
    }

    public static String newKey(Cred cred, Instance inst) {
        return "cred|" + cred.id + '|' + inst.type + '|' + inst.tag;
    }

    public static String newKey(X509 x509, X509Certificate x509Cert) {
        return "x509|" + x509.id + '|' + x509Cert.getSerialNumber().toString();
    }

    public static void delete(StringBuilder query, List<String> row) {
        query.append("DELETE FROM authz.notified WHERE user='");
        query.append(row.get(1));
        query.append("' AND target='");
        query.append(row.get(2));
        query.append("' AND key='");
        query.append(row.get(3));
        query.append("';\n");
    }

}
