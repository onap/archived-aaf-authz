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

import java.nio.ByteBuffer;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;

import org.onap.aaf.cadi.Hash;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Chrono;
import org.onap.aaf.misc.env.util.Split;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class X509 {
    public final String ca;
    public final String id;
    public final String x500;
    public final String x509;
    public ByteBuffer serial;
    
    public X509(String ca, String id, String x500, String x509, ByteBuffer serial) {
        this.ca = ca;
        this.id = id;
        this.x500 = x500;
        this.x509 = x509;
        this.serial = serial;
    }
    

    public static void load(Trans trans, Session session, Visitor<X509> visitor) {
        load(trans,session, "" , visitor);
    }
    
    public static void load(Trans trans, Session session, String where, Visitor<X509> visitor) {
        load(trans,session, visitor,"select ca, id, x500, x509, serial from authz.x509 " + where +';');
    }


    private static void load(Trans trans, Session session, Visitor<X509> visitor, String query) {
        trans.info().log( "query: " + query );
        TimeTaken tt = trans.start("Read X509", Env.REMOTE);
       
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
            tt = trans.start("Load X509s", Env.SUB);
            try {
                while (iter.hasNext()) {
                    ++count;
                    row = iter.next();
                    visitor.visit(new X509(row.getString(0),row.getString(1), row.getString(2),row.getString(3),row.getBytes(4)));
                }
            } finally {
                tt.done();
            }
        } finally {
            trans.info().log("Found",count,"X509 Certificates");
        }
    }
    
    public static long count(Trans trans, Session session) {
        String query = "select count(*) from authz.x509 LIMIT 1000000;";
        trans.info().log( "query: " + query );
        TimeTaken tt = trans.start("Count x509s", Env.REMOTE);
        ResultSet results;
        try {
            Statement stmt = new SimpleStatement(query).setReadTimeoutMillis(12000);
            results = session.execute(stmt);
            return results.one().getLong(0);
        } finally {
            tt.done();
        }
    }
    

    public void row(CSV.Writer cw, X509Certificate x509Cert) {
        cw.row("x509",ca,Hash.toHex(serial.array()),Chrono.dateOnlyStamp(x509Cert.getNotAfter()),x500);
    }

    public void row(CSV.Writer cw, X509Certificate x509Cert,String reason) {
        cw.row("x509",ca,Hash.toHex(serial.array()),Chrono.dateOnlyStamp(x509Cert.getNotAfter()),x500,reason);
    }


    public static void row(StringBuilder sb, List<String> row) {
        sb.append("DELETE from authz.x509 WHERE ca='");
        sb.append(row.get(1));
        sb.append("' AND serial=");
        sb.append(row.get(2));
        sb.append(";\n");
    }

    public static void batchDelete(StringBuilder sb, List<String> row) {
        sb.append("DELETE from authz.x509 WHERE ca='");
        sb.append(row.get(1));
        sb.append("' AND serial=");
        sb.append(row.get(2));
        sb.append(";\n");
    }
    public static String histSubject(List<String> row) {
        return row.get(4);
    }


    public static String histMemo(String fmt, List<String> row) {
        String id="n/a";
        for(String s : Split.splitTrim(',', row.get(4))) {
            if(s.startsWith("OU=") && s.indexOf('@')>=0) {
                int colon = s.indexOf(':');
                if(colon<0) {
                    colon=s.length();
                }
                id=s.substring(3,colon);
                break;
            }
        }
        return String.format(fmt, "Cert for " + id,"CA " + row.get(1),row.get(3));
    }

}