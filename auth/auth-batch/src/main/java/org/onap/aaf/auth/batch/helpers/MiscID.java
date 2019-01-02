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

import java.util.Map;
import java.util.TreeMap;

import org.onap.aaf.auth.batch.BatchException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class MiscID  {
    public static final TreeMap<String,MiscID> data = new TreeMap<>();
    /*
    Sample Record
    aad890|mj9030|20040902|20120207

    **** Field Definitions ****
    MISCID - AT&T Miscellaneous ID - Non-User ID (Types: Internal Mechanized ID, External Mechanized ID, Datagate ID, Customer ID, Vendor ID, Exchange Mail ID, CLEC ID, Specialized ID, Training ID)
    SPONSOR_ATTUID - ATTUID of MiscID Sponsor (Owner)
    CREATE_DATE - Date when MiscID was created 
    LAST_RENEWAL_DATE - Date when MiscID Sponsorship was last renewed
    */
    public String id;
    public String sponsor;
    public String created;
    public String renewal;

    private static final String FIELD_STRING = "id,created,sponsor,renewal";
    
    /**
     * Load a Row of Strings (from CSV file).
     * 
     * Be CAREFUL that the Row lists match the Fields above!!!  If this changes, change
     * 1) This Object
     * 2) DB "suits.cql"
     * 3) Alter existing Tables
     * @param row
     * @throws BatchException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    public void set(String[] row ) throws BatchException {
        if (row.length<4) {
            throw new BatchException("Row of MiscID_XRef is too short");
        }
        id      = row[0];
        sponsor = row[1];
        created = row[2];
        renewal = row[3];
    }

    public void set(Row row) {
        id      = row.getString(0);
        sponsor = row.getString(1);
        created = row.getString(2);
        renewal = row.getString(3);
    }
    

    public static void load(Trans trans, Session session ) {
        load(trans, session,"SELECT " + FIELD_STRING + " FROM authz.miscid;",data);
    }

    public static void load(Trans trans, Session session, Map<String,MiscID> map ) {
        load(trans, session,"SELECT " + FIELD_STRING + " FROM authz.miscid;",map);
    }

    public static void loadOne(Trans trans, Session session, String id ) {
        load(trans, session,"SELECT " + FIELD_STRING + " FROM authz.miscid WHERE id ='" + id + "';", data);
    }

    public static void load(Trans trans, Session session, String query, Map<String,MiscID> map) {
        trans.info().log( "query: " + query );
        TimeTaken tt = trans.start("Read MiscID", Env.REMOTE);
       
        ResultSet results;
        try {
            Statement stmt = new SimpleStatement( query );
            results = session.execute(stmt);
        } finally {
            tt.done();
        }
        int count = 0;
        try {
            tt = trans.start("Load Map", Env.SUB);
            try {
                for ( Row row : results.all()) {
                    MiscID miscID = new MiscID();
                    miscID.set(row);
                    data.put(miscID.id,miscID);
                    ++count;
                }
            } finally {
                tt.done();
            }
        } finally {
            trans.info().log("Found",count,"miscID records");
        }
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
        if (obj!=null && obj instanceof MiscID) {
            return id.equals(((MiscID)obj).id);
        }
        return false;
    }

    public StringBuilder insertStmt() throws IllegalArgumentException, IllegalAccessException {
        StringBuilder sb = new StringBuilder("INSERT INTO authz.miscid (");
        sb.append(FIELD_STRING);
        sb.append(") VALUES ('");
        sb.append(id);
        sb.append("','");
        sb.append(sponsor);
        sb.append("','");
        sb.append(created);
        sb.append("','");
        sb.append(renewal);
        sb.append("')");
        return sb;
    }
    
    public StringBuilder updateStmt(MiscID source) {
        StringBuilder sb = null;
        if (id.equals(source.id)) {
            sb = addField(sb,"sponser",sponsor,source.sponsor);
            sb = addField(sb,"created",created,source.created);
            sb = addField(sb,"renewal",renewal,source.renewal);
        }
        if (sb!=null) {
            sb.append(" WHERE id='");
            sb.append(id);
            sb.append('\'');
        }
        return sb;
    }

    private StringBuilder addField(StringBuilder sb, String name, String a, String b) {
        if (!a.equals(b)) {
            if (sb==null) {
                sb = new StringBuilder("UPDATE authz.miscid SET ");        
            } else {
                sb.append(',');
            }
            sb.append(name);
            sb.append("='");
            sb.append(b);
            sb.append('\'');
        }
        return sb;
    }

        
}