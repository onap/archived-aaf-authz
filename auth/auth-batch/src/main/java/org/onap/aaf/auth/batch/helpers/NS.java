/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class    NS implements Comparable<NS> {
    public static final Map<String,NS> data = new TreeMap<>();

    public NsDAO.Data ndd;

    public static Creator<NS> v2_0_11 = new Creator<NS> () {
        @Override
        public NS create(Row row) {
            return new NS(row.getString(0),row.getString(1), row.getString(2),row.getInt(3),row.getInt(4));
        }

        @Override
        public String select() {
            return "SELECT name, description, parent, type, scope FROM authz.ns ";
        }
    };

    public NS(String name, String description, String parent, int type, int scope) {
        ndd = new NsDAO.Data();
        ndd.name = name;
        ndd.description = description;
        ndd.parent = parent;
        ndd.type = type;
        // ndd.attrib = 
    }

    public static void load(Trans trans, Session session, Creator<NS> creator) {
        load(trans,session,
                "select name, description, parent, type, scope from authz.ns;"
                ,creator
                , v -> data.put(v.ndd.name,v)
                );
    }

    public static void loadOne(Trans trans, Session session, Creator<NS> creator, String ns) {
        load(trans,session,
                ("select name, description, parent, type, scope from authz.ns WHERE name='" + ns + "';")
                ,creator
                , v -> data.put(v.ndd.name,v)
                );
    }

    public static void load(Trans trans, Session session, Creator<NS> creator, Visitor<NS> visitor) {
         load(trans,session,creator.query(null),creator, visitor);
    }

    public void row(final CSV.Writer csvw, String tag) {
        csvw.row(tag,ndd.name,ndd.type,ndd.parent);
    }


    private static void load(Trans trans, Session session, String query, Creator<NS> creator, Visitor<NS> visitor) {
        trans.info().log( "query: " + query );
        ResultSet results;
        TimeTaken tt;

        tt = trans.start("Read Namespaces", Env.REMOTE);
        try {
            Statement stmt = new SimpleStatement( query );
            results = session.execute(stmt);
        } finally {
            tt.done();
        }
    

        try {
            Iterator<Row> iter = results.iterator();
            Row row;
            tt = trans.start("Load Namespaces", Env.SUB);
            try {
                while (iter.hasNext()) {
                    row = iter.next();
                    NS ns = creator.create(row);
                    visitor.visit(ns);
                }
            } finally {
                tt.done();
            }
        } finally {
            trans.info().log("Found",data.size(),"Namespaces");
        }

    }

    public static long count(Trans trans, Session session) {
        String query = "select count(*) from authz.ns LIMIT 1000000;";
        trans.info().log( "query: " + query );
        TimeTaken tt = trans.start("Count Namespaces", Env.REMOTE);
        ResultSet results;
        try {
            Statement stmt = new SimpleStatement(query).setReadTimeoutMillis(12000);
            results = session.execute(stmt);
            return results.one().getLong(0);
        } finally {
            tt.done();
        }
    }
    
    public String toString() {
        return ndd.name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return ndd.name.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return ndd.name.equals(obj);
    }

    @Override
    public int compareTo(NS o) {
        return ndd.name.compareTo(o.ndd.name);
    }

    public static class NSSplit {
        public String ns;
        public String other;
        public NSSplit(String s, int dot) {
            ns = s.substring(0,dot);
            other = s.substring(dot + 1);
        }
    }
    public static NSSplit deriveParent(String dotted) {
        if (dotted==null) {
            return null;
        }
        for (int idx = dotted.lastIndexOf('.');idx >= 0; idx = dotted.lastIndexOf('.',idx - 1)) {
            if (data.get(dotted.substring(0, idx)) != null) {
                return new NSSplit(dotted,idx);
            }
        }
        return null;
    }

}