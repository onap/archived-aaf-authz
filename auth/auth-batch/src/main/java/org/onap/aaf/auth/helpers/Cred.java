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
 *
 */

package org.onap.aaf.auth.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class Cred  {
    public static final TreeMap<String,Cred> data = new TreeMap<>();
    public static final TreeMap<String,List<Cred>> byNS = new TreeMap<>();

    public final String id;
    public final List<Instance> instances;
    public final String ns;
    
    public Cred(String id) {
        this.id = id;
        instances = new ArrayList<>();
        ns=Question.domain2ns(id);
    }
    
    public static class Instance {
        public final int type;
        public final Date expires,written;
        public final Integer other;
        
        public Instance(int type, Date expires, Integer other, long written) {
            this.type = type;
            this.expires = expires;
            this.other = other;
            this.written = new Date(written);
        }
    }
    
    public Date last(final int ... types) {
        Date last = null;
        for(Instance i : instances) {
            if(types.length>0) { // filter by types, if requested
                boolean quit = true;
                for(int t : types) {
                    if(t==i.type) {
                        quit=false;
                        break;
                    }
                }
                if(quit) {
                    continue;
                }
            }
            if(last==null || i.expires.after(last)) {
                last = i.expires;
            }
        }
        return last;
    }

    
    public Set<Integer> types() {
        Set<Integer> types = new HashSet<>();
        for(Instance i : instances) {
            types.add(i.type);
        }
        return types;
    }

    public static void load(Trans trans, Session session, int ... types ) {
        load(trans, session,"select id, type, expires, other, writetime(cred) from authz.cred;",types);
        
    }

    public static void loadOneNS(Trans trans, Session session, String ns,int ... types ) {
        load(trans, session,"select id, type, expires, other, writetime(cred) from authz.cred WHERE ns='" + ns + "';");
    }

    private static void load(Trans trans, Session session, String query, int ...types) {

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
            int type; // for filtering
            String id;
            tt = trans.start("Load Credentials", Env.SUB);
            try {
                while(iter.hasNext()) {
                    ++count;
                    row = iter.next();
                    id = row.getString(0);
                    type = row.getInt(1);
                    if(types.length>0) { // filter by types, if requested
                        boolean quit = true;
                        for(int t : types) {
                            if(t==type) {
                                quit=false;
                                break;
                            }
                        }
                        if(quit) {
                            continue;
                        }
                    }
                    Cred cred = data.get(id);
                    if(cred==null) {
                        cred = new Cred(id);
                        data.put(id, cred);
                    }
                    cred.instances.add(new Instance(type, row.getTimestamp(2), row.getInt(3), row.getLong(4)/1000));
                    
                    List<Cred> lscd = byNS.get(cred.ns);
                    if(lscd==null) {
                        byNS.put(cred.ns, (lscd=new ArrayList<>()));
                    }
                    boolean found = false;
                    for(Cred c : lscd) {
                        if(c.id.equals(cred.id)) {
                            found=true;
                            break;
                        }
                    }
                    if(!found) {
                        lscd.add(cred);
                    }
                }
            } finally {
                tt.done();
            }
        } finally {
            trans.info().log("Found",count,"creds");
        }
    }
    
    /** 
     * Count entries in Cred data.
     * Note, as opposed to other methods, need to load the whole cred table for the Types.
     * @param numbuckets 
     * @return
     */
    public static CredCount count(int numbuckets) {
        CredCount cc = new CredCount(numbuckets);
        for(Cred c : data.values()) {
            for (Instance ci : c.instances) {
                cc.inc(ci.type,ci.written, ci.expires);
            }
        }
        return cc;
//        String query = "select count(*) from authz.cred LIMIT 1000000;";
//        trans.info().log( "query: " + query );
//        TimeTaken tt = trans.start("Count Credentials", Env.REMOTE);
//        ResultSet results;
//        try {
//            Statement stmt = new SimpleStatement(query).setReadTimeoutMillis(12000);
//            results = session.execute(stmt);
//            return results.one().getLong(0);
//        } finally {
//            tt.done();
//        }
    }

    public static class CredCount {
        public int raw[];
        public int basic_auth[];
        public int basic_auth_256[];
        public int cert[];
        public int x509Added[];
        public int x509Expired[];
        public Date dates[];
        
        public CredCount(int numbuckets) {
            raw = new int[numbuckets];
            basic_auth = new int[numbuckets];
            basic_auth_256 = new int[numbuckets];
            cert = new int[numbuckets];
            x509Added = new int[numbuckets];
            x509Expired = new int[numbuckets];
            dates = new Date[numbuckets];
            GregorianCalendar gc = new GregorianCalendar();
            dates[0]=gc.getTime(); // now
            gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
            gc.set(GregorianCalendar.HOUR, 0);
            gc.set(GregorianCalendar.MINUTE, 0);
            gc.set(GregorianCalendar.SECOND,0);
            gc.set(GregorianCalendar.MILLISECOND,0);
            gc.add(GregorianCalendar.MILLISECOND, -1); // last milli of month
            for(int i=1;i<numbuckets;++i) {
                dates[i] = gc.getTime();
                gc.add(GregorianCalendar.MONTH, -1);
            }
            
        }
        
        public void inc(int type, Date start, Date expires) {
            for(int i=0;i<dates.length-1;++i) {
                if(start.before(dates[i])) {
                    if(type==CredDAO.CERT_SHA256_RSA) {
                        if(start.after(dates[i+1])) {
                            ++x509Added[i];
                        }
                    }
                    if(expires.after(dates[i])) {
                        switch(type) {
                            case CredDAO.RAW:
                                ++raw[i];
                                break;
                            case CredDAO.BASIC_AUTH:
                                ++basic_auth[i];
                                break;
                            case CredDAO.BASIC_AUTH_SHA256:
                                ++basic_auth_256[i];
                                break;
                            case CredDAO.CERT_SHA256_RSA:
                                ++cert[i];
                                break;
                        }
                    }
                }
            }
        }

        public long authCount(int idx) {
            return (long)basic_auth[idx]+basic_auth_256[idx];
        }
        
        public long x509Count(int idx) {
            return cert[idx];
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