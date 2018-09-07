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
import java.util.List;
import java.util.TreeMap;

import org.onap.aaf.auth.actions.Message;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Chrono;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class Notification {
    public enum TYPE {
        OA("Owner Approval",1),SA("Supervisor Approval",2),CN("Credential Expiration",20);
        
        private String desc;
        private int type;
    
        private TYPE(String desc,int type) {
            this.desc = desc;
            this.type = type;
        }
        
        public String desc() {
            return desc;
        }
        
        public int idx() {
            return type;
        }

        public static TYPE get(int idx) {
            for(TYPE nt : TYPE.values()) {
                if(idx==nt.type) {
                    return nt;
                }
            }
            return null;
        }
    }


    public static final TreeMap<String,List<Notification>> data = new TreeMap<>();
    public static final Date now = new Date();
    
    public final String user;
    public final TYPE type;
    public Date last;
    public int checksum;
    public Message msg;
    private int current;
    public Organization org;
    public int count;
    
    private Notification(String user, TYPE nt, Date last, int checksum) {
        this.user = user;
        this.type = nt;
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
                        ln = new ArrayList<>();
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
    
    public static Notification get(String user, TYPE type) {
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

    public static Notification create(String user, TYPE type) {
        return new Notification(user,type,null,0);
    }
    
    public static Creator<Notification> v2_0_18 = new Creator<Notification>() {
        @Override
        public Notification create(Row row) {
            int idx =row.getInt(1);
            TYPE type = TYPE.get(idx);
            if(type==null) {
                return null;
            }
            return new Notification(row.getString(0), type, row.getTimestamp(2), row.getInt(3));
        }

        @Override
        public String select() {
            return "SELECT user,type,last,checksum FROM authz.notify LIMIT 100000";
        }
    };

    
    public void set(Message msg) {
        this.msg = msg; 
    }

    public int checksum() {
        if(msg==null) {
            current=0;
        } else if(current==0) {
            for(String l : msg.lines) {
                for(byte b : l.getBytes()) {
                    current+=b;
                }
            }
        }
        return current;
    }
    
    public boolean update(AuthzTrans trans, Session session, boolean dryRun) {
        checksum();
        if(last==null || current==0 || current!=checksum) {
            last = now;
            current = checksum();
            String update = "UPDATE authz.notify SET " +
                    "last = '" + Chrono.utcStamp(last) +
                    "', checksum=" +
                    current +
                    " WHERE user='" +
                    user + 
                    "' AND type=" +
                    type.idx() +
                    ";";
            if(dryRun) {
                trans.info().log("Would",update);
            } else {
                session.execute(update);
            }
            return true;
        }
        return false;
    }

    public String toString() {
        return "\"" + user + "\",\"" + type.name() + "\",\"" 
                + Chrono.dateTime(last)+ "\", "  + checksum;
    }
}