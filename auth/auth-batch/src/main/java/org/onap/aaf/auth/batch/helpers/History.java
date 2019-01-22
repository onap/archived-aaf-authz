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

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.UUID;

import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class History  {
    public final UUID id;
    public final String action;
    public final String memo;
    public final String reconstruct;
    public final String subject;
    public final String target;
    public final String user;
    public final int yrMon;

    public static Creator<History> sansConstruct = new Creator<History> () {
        @Override
        public History create(Row row) {
            return new History(
                    row.getUUID(0),
                    row.getString(1),
                    row.getString(2),
                    row.getString(3),
                    row.getString(4),
                    row.getString(5),
                    row.getInt(6));
        }

        @Override
        public String select() {
            return "SELECT id, action, memo, subject, target, user, yr_mon from authz.history LIMIT 10000000 ";
        }
    };

    public static Creator<History> avecConstruct = new Creator<History> () {
        private final StringBuilder sb = new StringBuilder();

        @Override
        public History create(Row row) {
            ByteBuffer bb = row.getBytes(3);
            sb.setLength(0);

            if (bb!=null && bb.hasRemaining()) {
                sb.append("0x");
                while (bb.hasRemaining()) {
                    sb.append(String.format("%02x",bb.get()));
                }
                bb.flip();
            }
            return new History(
                    row.getUUID(0),
                    row.getString(1),
                    row.getString(2),
                    sb.toString(),
                    row.getString(4),
                    row.getString(5),
                    row.getString(6),
                    row.getInt(7));
        }

        @Override
        public String select() {
            return "SELECT id, action, memo, reconstruct, subject, target, user, yr_mon from authz.history LIMIT 10000000 ";
        }
    };
    
    public History(UUID id, String action, String memo, String subject, String target, String user, int yrMon) {
        this.id = id;
        this.action = action;
        this.memo = memo;
        this.reconstruct = null;
        this.subject = subject;
        this.target = target;
        this.user = user;
        this.yrMon = yrMon;
    }
    
    public History(UUID id, String action, String memo, String reconstruct, String subject, String target, String user, int yrMon) {
        this.id = id;
        this.action = action;
        this.memo = memo;
        this.reconstruct = reconstruct;
        this.subject = subject;
        this.target = target;
        this.user = user;
        this.yrMon = yrMon;
    }

    public static void load(Trans trans, Session session, Creator<History> creator, Loader<History> loader) {
        trans.info().log( "query: " + creator.select() );
        TimeTaken tt = trans.start("Read History", Env.REMOTE);
       
        ResultSet results;
        try {
            Statement stmt = new SimpleStatement( creator.select() ).setReadTimeoutMillis(240000);
            results = session.execute(stmt);
        } finally {
            tt.done();
        }
        int count = 0;
        try {
            Iterator<Row> iter = results.iterator();
            Row row;
            tt = trans.start("Load History", Env.SUB);
            try {
                while (iter.hasNext()) {
                    ++count;
                    row = iter.next();
                    loader.exec(creator.create(row));
                }
            } finally {
                tt.done();
            }
        } finally {
            trans.info().log("Found",count,"histories");
        }
    }
    
    public String toString() {
        return String.format("%s %d %s, %s, %s, %s, %s", 
                id.toString(),
                yrMon,
                user,
                target,
                action,
                subject,
                memo);
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