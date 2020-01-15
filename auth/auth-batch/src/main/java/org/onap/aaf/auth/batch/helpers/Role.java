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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class Role implements Comparable<Role> {
    public static final SortedMap<Role,Set<String>> data = new TreeMap<>();
    public static final SortedMap<String,Role> keys = new TreeMap<>();
    public static final SortedMap<String,Role> byName = new TreeMap<>();
    private static List<Role> deleteRoles = new ArrayList<>();

    public RoleDAO.Data rdd;
    private String full;
    private String encode;

    public Role(String full) {
        rdd = new RoleDAO.Data();
        rdd.ns = "";
        rdd.name = "";
        rdd.description = "";
        rdd.perms = new HashSet<>();
        this.full = full;
    }

    public Role(String ns, String name, String description,Set<String> perms) {
           rdd = new RoleDAO.Data();
        rdd.ns = ns;
        rdd.name = name;
        rdd.description = description;
        rdd.perms = perms;
        this.full = null;
        this.encode = null;
    }

    public String encode() {
        if (encode==null) {
            encode = rdd.ns + '|' + rdd.name;
        }
        return encode;
    }

    public String fullName() {
        if (full==null) {
            full = rdd.ns + '.' + rdd.name;
        }
        return full;
    }

    public static void load(Trans trans, Session session ) {
        load(trans,session,"select ns, name, description, perms from authz.role;");
    }

    public static void loadOneNS(Trans trans, Session session, String ns ) {
        load(trans,session,"select ns, name, description, perms from authz.role WHERE ns='" + ns + "';");
    }

    private static void load(Trans trans, Session session, String query) {
        trans.info().log( "query: " + query );
        TimeTaken tt = trans.start("Read Roles", Env.REMOTE);

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
            tt = trans.start("Load Roles", Env.SUB);
            try {
                while (iter.hasNext()) {
                    row = iter.next();
                    Role rk =new Role(row.getString(0),row.getString(1), row.getString(2),row.getSet(3,String.class));
                    keys.put(rk.encode(), rk);
                    data.put(rk,rk.rdd.perms);
                    byName.put(rk.fullName(), rk);
                }
            } finally {
                tt.done();
            }
        } finally {
            trans.info().log("Found",data.size(),"roles");
        }
    }

    public static long count(Trans trans, Session session) {
        String query = "select count(*) from authz.role LIMIT 1000000;";
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
    public int compareTo(Role o) {
        return encode().compareTo(o.encode());
    }

    public static String fullName(String role) {
        return role.replace('|', '.');
    }

    public static void stageRemove(Role r) {
        deleteRoles.add(r);
    }

    public static void executeRemove() {
        for (Role p : deleteRoles) {
            keys.remove(p.encode);
            data.remove(p);
        }
        deleteRoles.clear();
    }

    public static void clear() {
        data.clear();
        keys.clear();
        byName.clear();
        deleteRoles.clear();
    }

}