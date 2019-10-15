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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Chrono;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class Future implements CacheChange.Data, Comparable<Future> {
    public static final Map<UUID,Future> data = new TreeMap<>();
    public static final Map<String,List<Future>> byRole = new TreeMap<>();

    public final FutureDAO.Data fdd;
    public final String role; // derived
    private static final CacheChange<Future> cache = new CacheChange<>();

    public static Creator<Future> v2_0_17 = new Creator<Future>() {
        @Override
        public Future create(Row row) {
            return new Future(row.getUUID(0),row.getString(1),row.getString(2),
                    row.getTimestamp(3),row.getTimestamp(4), null);
        }

        @Override
        public String select() {
            return "select id,memo,target,start,expires from authz.future";
        }
    };

    public static Creator<Future> withConstruct = new Creator<Future>() {
        @Override
        public String select() {
            return "select id,memo,target,start,expires,construct from authz.future";
        }

        @Override
        public Future create(Row row) {
            return new Future(row.getUUID(0),row.getString(1),row.getString(2),
                    row.getTimestamp(3),row.getTimestamp(4), row.getBytes(5));
        }

    };


    public Future(UUID id, String memo, String target, Date start, Date expires, ByteBuffer construct) {
        fdd = new FutureDAO.Data();
        fdd.id = id;
        fdd.memo = memo;
        fdd.target = target;
        fdd.start = start;
        fdd.expires = expires;
        fdd.construct = construct;
        String role = null;
        if ("user_role".equals(target)) {
            UserRoleDAO.Data urdd = new UserRoleDAO.Data();
            try {
                urdd.reconstitute(construct);
                fdd.target_key = urdd.user + '|' + urdd.role;
                fdd.target_date = urdd.expires;
                role = urdd.role;
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
        this.role = role;
    }

    public final UUID id() {
        return fdd.id;
    }

    public final String memo() {
        return fdd.memo;
    }

    public final String target() {
        return fdd.target;
    }

    public final Date start() {
        return fdd.start;
    }

    public final Date expires() {
        return fdd.expires;
    }

    public static void load(Trans trans, Session session, Creator<Future> creator) {
        load(trans,session,creator, f -> {
            data.put(f.fdd.id,f);
            if (f.role==null) {
                return;
            }
            List<Future> lf = byRole.computeIfAbsent(f.role, k -> new ArrayList<>());
            lf.add(f);
        });
    }


    public static void load(Trans trans, Session session, Creator<Future> creator, Visitor<Future> visitor) {
        trans.info().log( "query: " + creator.select() );
        ResultSet results;
        TimeTaken tt = trans.start("Load Futures", Env.REMOTE);
        try {
            Statement stmt = new SimpleStatement(creator.select());
            results = session.execute(stmt);
        } finally {
            tt.done();
        }

        int count = 0;
        tt = trans.start("Process Futures", Env.SUB);
        try {
            for (Row row : results.all()) {
                ++count;
                visitor.visit(creator.create(row));
            }
        } finally {
            tt.done();
            trans.info().log("Found",count,"Futures");
        }
    }

    public Result<Void> delayedDelete(AuthzTrans trans, FutureDAO fd, boolean dryRun, String text) {
        Result<Void> rv;
        if (dryRun) {
            trans.info().log(text,"- Would Delete: ",fdd.id,fdd.memo,"expiring on",Chrono.dateOnlyStamp(fdd.expires));
            rv = Result.ok();
        } else {
            rv = fd.delete(trans, fdd, true); // need to read for undelete
            if (rv.isOK()) {
                trans.info().log(text, "- Deleted:",fdd.id,fdd.memo,"expiring on",Chrono.dateOnlyStamp(fdd.expires));
                cache.delayedDelete(this);
            } else {
                if (rv.status!=6) {
                    trans.info().log(text,"- Failed to Delete Future", fdd.id);
                }
            }
        }
        return rv;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.helpers.CacheChange.Data#resetLocalData()
     */
    @Override
    public void expunge() {
        data.remove(fdd.id);
        if (role!=null) {
            List<Future> lf = byRole.get(role);
            if (lf!=null) {
                lf.remove(this);
            }
        }
    }

    @Override
    public int compareTo(Future o) {
        if (o==null) {
            return -1;
        }
        return fdd.id.compareTo(o.fdd.id);
    }

    public static void resetLocalData() {
        cache.resetLocalData();
    }

    public static int sizeForDeletion() {
        return cache.cacheSize();
    }

    public static boolean pendingDelete(Future f) {
        return cache.contains(f);
    }

    public static void row(CSV.Writer cw, Future f) {
        cw.row("future",f.fdd.id,f.fdd.target,f.fdd.expires,f.role,f.fdd.memo);
    }


    public static void deleteByIDBatch(StringBuilder sb, String id) {
        sb.append("DELETE from authz.future where id=");
        sb.append(id);
        sb.append(";\n");
    }

}
