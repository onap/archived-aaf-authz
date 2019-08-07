/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Modifications Copyright (C) 2018 IBM.
 * ==============================================================================
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

package org.onap.aaf.auth.dao.cass;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.dao.Loader;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

/**
 * FutureDAO stores Construction information to create 
 * elements at another time.
 * 
 * @author Jonathan
 * 8/20/2013
 */
public class FutureDAO extends CassDAOImpl<AuthzTrans,FutureDAO.Data> {
    private static final String TABLE = "future";
    private final HistoryDAO historyDAO;
    private PSInfo psByStartAndTarget;
    public static final int KEYLIMIT = 1;

    public FutureDAO(AuthzTrans trans, Cluster cluster, String keyspace) {
        super(trans, FutureDAO.class.getSimpleName(),cluster, keyspace, Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        historyDAO = new HistoryDAO(trans, this);
        init(trans);
    }

    public FutureDAO(AuthzTrans trans, HistoryDAO hDAO) {
        super(trans, FutureDAO.class.getSimpleName(),hDAO, Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        historyDAO=hDAO;
        init(trans);
    }


    public static class Data {
        public UUID         id;
        public String       target;
        public String       memo;
        public Date         start;
        public Date         expires;
        public String        target_key;
        public Date            target_date;
        public ByteBuffer   construct;  //   this is a blob in cassandra
    }

    private static class FLoader extends Loader<Data> {
        public FLoader() {
            super(KEYLIMIT);
        }

        public FLoader(int keylimit) {
            super(keylimit);
        }

        @Override
    public Data load(Data data, Row row) {
            data.id           = row.getUUID(0);
            data.target       = row.getString(1);
            data.memo         = row.getString(2);
            data.start        = row.getTimestamp(3);
            data.expires      = row.getTimestamp(4);
            data.target_key   = row.getString(5);
            data.target_date  = row.getTimestamp(6);
            data.construct    = row.getBytes(7);
            return data;
        }

        @Override
        protected void key(Data data, int idx, Object[] obj) {
            obj[idx] = data.id;
        }

        @Override
        protected void body(Data data, int _idx, Object[] obj) {
        int idx = _idx;

            obj[idx] = data.target;
            obj[++idx] = data.memo;
            obj[++idx] = data.start;
            obj[++idx] = data.expires;
            obj[++idx] = data.target_key;
            obj[++idx] = data.target_date;
            obj[++idx] = data.construct;
        }
    }

    private void init(AuthzTrans trans) {
        // Set up sub-DAOs
        String[] helpers = setCRUD(trans, TABLE, Data.class, new FLoader(KEYLIMIT));

        // Other SELECT style statements... match with a local Method
        psByStartAndTarget = new PSInfo(trans, SELECT_SP + helpers[FIELD_COMMAS] +
                " FROM future WHERE start <= ? and target = ? ALLOW FILTERING", new FLoader(2) {
            @Override
            protected void key(Data data, int _idx, Object[] obj) {
                    int idx = _idx;

                obj[idx]=data.start;
                obj[++idx]=data.target;
            }
        },readConsistency);
    }

    public Result<List<Data>> readByStartAndTarget(AuthzTrans trans, Date start, String target) {
        return psByStartAndTarget.read(trans, R_TEXT, new Object[]{start, target});
    }

    /**
     * Override create to add secondary ID to Subject in History, and create Data.ID, if it is null
     */
    public Result<FutureDAO.Data> create(AuthzTrans trans, FutureDAO.Data data, String id) {
        // If ID is not set (typical), create one.
        if (data.id==null) {
            StringBuilder sb = new StringBuilder(trans.user());
            sb.append(data.target);
            sb.append(System.currentTimeMillis());
            data.id = UUID.nameUUIDFromBytes(sb.toString().getBytes());
        }
        Result<ResultSet> rs = createPS.exec(trans, C_TEXT, data);
        if (rs.notOK()) {
            return Result.err(rs);
        }
        wasModified(trans, CRUD.create, data, null, id);
        return Result.ok(data);    
    }

    /**
     * Log Modification statements to History
     *
     * @param modified        which CRUD action was done
     * @param data            entity data that needs a log entry
     * @param overrideMessage if this is specified, we use it rather than crafting a history message based on data
     */
    @Override
    protected void wasModified(AuthzTrans trans, CRUD modified, Data data, String ... override) {
        boolean memo = override.length>0 && override[0]!=null;
        boolean subject = override.length>1 && override[1]!=null;
        HistoryDAO.Data hd = HistoryDAO.newInitedData();
        hd.user = trans.user();
        hd.action = modified.name();
        hd.target = TABLE;
        hd.subject = subject?override[1]:"";
        hd.memo = memo?String.format("%s by %s", override[0], hd.user):data.memo;
    
        if (historyDAO.create(trans, hd).status!=Status.OK) {
            trans.error().log("Cannot log to History");
        }
    }
    
}
