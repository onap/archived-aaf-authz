/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright © 2018 IBM.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class Perm implements Comparable<Perm> {
    public static final TreeMap<Perm,Set<String>> data = new TreeMap<>();
    public static final TreeMap<String,Perm> keys = new TreeMap<>();
    private static List<Perm> deletePerms = new ArrayList<>();

    public final String ns;
    public final String type;
    public final String instance;
    public final String action;
    public final String description;
    private String fullType = null;
    private String fullPerm = null;
    private String encode = null;
    public final Set<String> roles;

    public Perm(String ns, String type, String instance, String action, String description, Set<String> roles) {
        this.ns = ns;
        this.type = type;
        this.instance = instance;
        this.action = action;
        this.description = description;
        this.roles = roles;
    }
    
    public String encode() {
        if (encode == null) {
            encode = ns + '|' + type + '|' + instance + '|' + action;
        }
        return encode;
    }
    
    public String fullType() {
        if (fullType==null) {
            fullType = ns + '.' + type;
        }
        return fullType;
    }
    
    public String fullPerm() {
        if (fullPerm==null) {
            fullPerm = ns + '.' + type  + '|' + instance + '|' + action;
        }
        return fullPerm;
    }

    public static void load(Trans trans, Session session) {
        load(trans, session, "select ns, type, instance, action, description, roles from authz.perm;");
    }
    
    public static void loadOneNS(Trans trans, Session session, String ns) {
        load(trans, session, "select ns, type, instance, action, description, roles from authz.perm WHERE ns='" + ns + "';");
        
    }

    private static void load(Trans trans, Session session, String query) {
        //
        trans.info().log( "query: " + query );
        TimeTaken tt = trans.start("Read Perms", Env.REMOTE);
        ResultSet results;
        try {
            Statement stmt = new SimpleStatement( query );
            results = session.execute(stmt);
        } finally {
            tt.done();
        }

        try {
            Iterator<Row> iter = results.iterator();
            Row row;
            tt = trans.start("Load Perms", Env.SUB);
            try {
                while (iter.hasNext()) {
                    row = iter.next();                    
                    Perm pk = new Perm(
                            row.getString(0),row.getString(1),row.getString(2),
                            row.getString(3), row.getString(4), row.getSet(5,String.class));
                    keys.put(pk.encode(), pk);
                    data.put(pk,pk.roles);
                }
            } finally {
                tt.done();
            }
        } finally {
            trans.info().log("Found",data.size(),"perms");
        }
    }

    public static long count(Trans trans, Session session) {
        String query = "select count(*) from authz.perm LIMIT 1000000;";
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
        return encode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return encode().hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return encode().equals(obj);
    }

    @Override
    public int compareTo(Perm o) {
        return encode().compareTo(o.encode());
    }

    public static void stageRemove(Perm p) {
        deletePerms.add(p);
    }
    
    public static void executeRemove() {
        for (Perm p : deletePerms) {
            keys.remove(p.encode);
            data.remove(p);
        }
        deletePerms.clear();
    }

}