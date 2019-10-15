/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.TransStore;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;

public abstract class AbsCassDAO<TRANS extends TransStore,DATA> {
    protected static final char DOT = '.';
    protected static final char DOT_PLUS_ONE = '.'+1;
    protected static final String FIRST_CHAR = Character.toString((char)0);
    protected static final String LAST_CHAR = Character.toString((char)Character.MAX_VALUE);
    protected static final int FIELD_COMMAS = 0;
    protected static final int QUESTION_COMMAS = 1;
    protected static final int ASSIGNMENT_COMMAS = 2;
    protected static final int WHERE_ANDS = 3;

    private Cluster cluster; 
    /*
     * From DataStax
     * com.datastax.driver.core.Session
        A session holds connections to a Cassandra cluster, allowing it to be queried. Each session maintains multiple connections to the cluster nodes, 
        provides policies to choose which node to use for each query (round-robin on all nodes of the cluster by default), and handles retries for 
        failed query (when it makes sense), etc...
        Session instances are thread-safe and usually a single instance is enough per application. However, a given session can only be set to one 
        keyspace at a time, so one instance per keyspace is necessary.
     */
    private Session session;
    private final String keyspace;
    // If this is null, then we own session
    private final AbsCassDAO<TRANS,?> owningDAO;
    protected Class<DATA> dataClass;
    private final String name;
//    private static Slot sessionSlot; // not used since 2015
    private static final ArrayList<AbsCassDAO<? extends TransStore,?>.PSInfo> psinfos = new ArrayList<>();
    private static final List<Object> EMPTY = new ArrayList<>(0);
    private static final Deque<ResetRequest> resetDeque = new ConcurrentLinkedDeque<ResetRequest>();
    private static boolean resetTrigger = false;
    private static long nextAvailableReset = 0;

    public AbsCassDAO(TRANS trans, String name, Cluster cluster, String keyspace, Class<DATA> dataClass) {
        this.name = name;
        this.cluster = cluster;
        this.keyspace = keyspace;
        owningDAO = null;  // we own session
        session = null;
        this.dataClass = dataClass;
    }

    public AbsCassDAO(TRANS trans, String name, AbsCassDAO<TRANS,?> aDao, Class<DATA> dataClass) {
        this.name = name;
        cluster = aDao.cluster;
        keyspace = aDao.keyspace;
        session = null;
        // We do not own session
        owningDAO = aDao;
        this.dataClass = dataClass;
    }

// Not used since 2015
//    public static void setSessionSlot(Slot slot) {
//        sessionSlot = slot;
//    }

    //Note: Lower case ON PURPOSE. These names used to create History Messages
    public enum CRUD {
        create,read,update,delete;
    }

    public class PSInfo {
        private PreparedStatement ps;
        private final int size;
        private final Loader<DATA> loader;
        private final CRUD crud; // Store CRUD, because it makes a difference in Object Order, see Loader
        private final String cql;
        private final ConsistencyLevel consistency;


        /**
         * Create a PSInfo and create Prepared Statement
         * <p>
         * @param trans
         * @param theCQL
         * @param loader
         */
        public PSInfo(TRANS trans, String theCQL, Loader<DATA> loader, ConsistencyLevel consistency) {
            this.loader = loader;
            this.consistency=consistency;
            psinfos.add(this);

            cql = theCQL.trim().toUpperCase();
            if (cql.startsWith("INSERT")) {
                crud = CRUD.create;
            } else if (cql.startsWith("UPDATE")) {
                crud = CRUD.update;
            } else if (cql.startsWith("DELETE")) {
                crud = CRUD.delete;
            } else {
                crud = CRUD.read;
            }
        
            int idx = 0, count=0;
            while ((idx=cql.indexOf('?',idx))>=0) {
                ++idx;
                ++count;
            }
            size=count;
        }
    
        public synchronized void reset() {
            ps = null;
        }
    
        private synchronized BoundStatement ps(TransStore trans) throws APIException, IOException {
            /* From Datastax
                You should prepare only once, and cache the PreparedStatement in your application (it is thread-safe). 
                If you call prepare multiple times with the same query string, the driver will log a warning.
            */
            if (ps==null) {
                TimeTaken tt = trans.start("Preparing PSInfo " + crud.toString().toUpperCase() + " on " + name,Env.SUB);
                try {
                    ps = getSession(trans).prepare(cql);
                    ps.setConsistencyLevel(consistency);
                } catch (DriverException e) {
                    reportPerhapsReset(trans,e);
                    throw e;
                } finally {
                    tt.done();
                }
            }
            // BoundStatements are NOT threadsafe... need a new one each time.
            return new BoundStatement(ps);
        }

        /**
         * Execute a Prepared Statement by extracting from DATA object
         * <p>
         * @param trans
         * @param text
         * @param data
         * @return
         */
        public Result<ResultSetFuture> execAsync(TRANS trans, String text, DATA data) {
            TimeTaken tt = trans.start(text, Env.REMOTE);
            try {
                return Result.ok(getSession(trans).executeAsync(
                        ps(trans).bind(loader.extract(data, size, crud))));
            } catch (DriverException | APIException | IOException e) {
                AbsCassDAO.this.reportPerhapsReset(trans,e);
                return Result.err(Status.ERR_Backend,"%s-%s executing %s",e.getClass().getName(),e.getMessage(), cql);
            } finally {
                tt.done();
            }
        }

        /**
         * Execute a Prepared Statement on Object[] key
         * <p>
         * @param trans
         * @param text
         * @param objs
         * @return
         */
        public Result<ResultSetFuture> execAsync(TRANS trans, String text, Object ... objs) {
            TimeTaken tt = trans.start(text, Env.REMOTE);
            try {
                return Result.ok(getSession(trans).executeAsync(ps(trans).bind(objs)));
            } catch (DriverException | APIException | IOException e) {
                AbsCassDAO.this.reportPerhapsReset(trans,e);
                return Result.err(Status.ERR_Backend,"%s-%s executing %s",e.getClass().getName(),e.getMessage(), cql);
            } finally {
                tt.done();
            }
        }
    
        /* 
         * Note:
         * <p>
         */

        /**
         * Execute a Prepared Statement by extracting from DATA object
         * <p>
         * @param trans
         * @param text
         * @param data
         * @return
         */
        public Result<ResultSet> exec(TRANS trans, String text, DATA data) {
            TimeTaken tt = trans.start(text, Env.REMOTE);
            try {
                /*
                 * "execute" (and executeAsync)
                 * Executes the provided query.
                    This method blocks until at least some result has been received from the database. However, 
                    for SELECT queries, it does not guarantee that the result has been received in full. But it 
                    does guarantee that some response has been received from the database, and in particular 
                    guarantee that if the request is invalid, an exception will be thrown by this method.

                    Parameters:
                    statement - the CQL query to execute (that can be any Statement).
                    Returns:
                        the result of the query. That result will never be null but can be empty (and will 
                        be for any non SELECT query).
                 */
                return Result.ok(getSession(trans).execute(
                        ps(trans).bind(loader.extract(data, size, crud))));
            } catch (DriverException | APIException | IOException e) {
                AbsCassDAO.this.reportPerhapsReset(trans,e);
                return Result.err(Status.ERR_Backend,"%s-%s executing %s",e.getClass().getName(),e.getMessage(), cql);
            } finally {
                tt.done();
            }
        }

        /**
         * Execute a Prepared Statement on Object[] key
         * <p>
         * @param trans
         * @param text
         * @param objs
         * @return
         */
        public Result<ResultSet> exec(TRANS trans, String text, Object ... objs) {
            TimeTaken tt = trans.start(text, Env.REMOTE);
            try {
                return Result.ok(getSession(trans).execute(ps(trans).bind(objs)));
            } catch (DriverException | APIException | IOException e) {
                AbsCassDAO.this.reportPerhapsReset(trans,e);
                return Result.err(Status.ERR_Backend,"%s-%s executing %s",e.getClass().getName(),e.getMessage(), cql);
            } finally {
                tt.done();
            }
        }

        /**
         * Read the Data from Cassandra given a Prepared Statement (defined by the
         * DAO Instance)
         *
         * This is common behavior among all DAOs.
         * @throws DAOException
         */
        public Result<List<DATA>> read(TRANS trans, String text, Object[] key) {
            TimeTaken tt = trans.start(text,Env.REMOTE);
        
            ResultSet rs;
            try {
                rs = getSession(trans).execute(key==null?ps(trans):ps(trans).bind(key));
/// TEST CODE for Exception            
//                boolean force = true; 
//                if (force) {
//                    Map<InetSocketAddress, Throwable> misa = new HashMap<>();
//                    //misa.put(new InetSocketAddress(444),new Exception("no host was tried"));
//                    misa.put(new InetSocketAddress(444),new Exception("Connection has been closed"));
//                    throw new com.datastax.driver.core.exceptions.NoHostAvailableException(misa);
////                    throw new com.datastax.driver.core.exceptions.AuthenticationException(new InetSocketAddress(9999),"no host was tried");
//                }
//// END TEST CODE
            } catch (DriverException | APIException | IOException e) {
                AbsCassDAO.this.reportPerhapsReset(trans,e);
                return Result.err(Status.ERR_Backend,"%s-%s executing %s",e.getClass().getName(),e.getMessage(), cql);
            } finally {
                tt.done();
            }
        
            return extract(loader,rs,null /*let Array be created if necessary*/,dflt);
        }
    
        public Result<List<DATA>> read(TRANS trans, String text, DATA data) {
            return read(trans,text, loader.extract(data, size, crud));
        }
    
        public Object[] keyFrom(DATA data) {
            return loader.extract(data, size, CRUD.delete); // Delete is key only
        }

        /*
         * Note: in case PSInfos are deleted, we want to remove them from list.  This is not expected, 
         * but we don't want a data leak if it does.  Finalize doesn't have to happen quickly
         */
        @Override
        protected void finalize() throws Throwable {
            psinfos.remove(this);
        }
    }

    protected final Accept<DATA> dflt = new Accept<DATA>() {
        @Override
        public boolean ok(DATA data) {
            return true;
        }
    };


    @SuppressWarnings("unchecked")
    protected final Result<List<DATA>> extract(Loader<DATA> loader, ResultSet rs, List<DATA> indata, Accept<DATA> accept) {
        List<Row> rows = rs.all();
        if (rows.isEmpty()) {
            return Result.ok((List<DATA>)EMPTY); // Result sets now .emptyList(true);
        } else {
            DATA d;
            List<DATA> data = indata==null?new ArrayList<>(rows.size()):indata;
        
            for (Row row : rows) {
                try {
                    d = loader.load(dataClass.newInstance(),row);
                    if (accept.ok(d)) {
                        data.add(d);
                    }
                } catch (Exception e) {
                    return Result.err(e);
                }
            }
            return Result.ok(data);
        }
    }

    private static final String NEW_CASSANDRA_SESSION_CREATED = "New Cassandra Session Created";
    private static final String NEW_CASSANDRA_CLUSTER_OBJECT_CREATED = "New Cassandra Cluster Object Created";
    private static final String NEW_CASSANDRA_SESSION = "New Cassandra Session";
    private static final Object LOCK = new Object();

    private static class ResetRequest {
        //package on purpose
        Session session;
        long timestamp;
    
        public ResetRequest(Session session) {
            this.session = session;
            timestamp = System.currentTimeMillis();
        }
    }


    public static final void primePSIs(TransStore trans) throws APIException, IOException {
        for (AbsCassDAO<? extends TransStore, ?>.PSInfo psi : psinfos) {
            if (psi.ps==null) {
                psi.ps(trans);
            }
        }
    }

    public final Session getSession(TransStore trans) throws APIException, IOException {
        // SessionFilter unused since 2015
        // Try to use Trans' session, if exists
//        if (sessionSlot!=null) { // try to get from Trans
//            Session sess = trans.get(sessionSlot, null);
//            if (sess!=null) {
//                return sess;
//            }
//        }
    
        // If there's an owning DAO, use it's session
        if (owningDAO!=null) { 
            return owningDAO.getSession(trans);
        }
    
        // OK, nothing else works... get our own.
        if (session==null || resetTrigger) {
            Cluster tempCluster = null;
            Session tempSession = null;
            try {
                synchronized(LOCK) {
                    boolean reset = false;
                    for (ResetRequest r : resetDeque) {
                        if (r.session == session) {
                            if (r.timestamp>nextAvailableReset) {
                                reset=true;
                                nextAvailableReset = System.currentTimeMillis() + 60000;
                                tempCluster = cluster;
                                tempSession = session;
                                break;
                            } else {
                                trans.warn().log("Cassandra Connection Reset Ignored: Recent Reset");
                            }
                        }
                    }

                    if (reset || session == null) {
                        TimeTaken tt = trans.start(NEW_CASSANDRA_SESSION, Env.SUB);
                        try {
                            // Note: Maitrayee recommended not closing the cluster, just
                            // overwrite it. Jonathan 9/30/2016 assuming same for Session
                            // This was a bad idea.  Ran out of File Handles as I suspected, Jonathan
                            if (reset) {
                                for (AbsCassDAO<? extends TransStore, ?>.PSInfo psi : psinfos) {
                                    psi.reset();
                                }
                            }
                            if (reset || cluster==null) {
                                cluster = CassAccess.cluster(trans, keyspace);
                                trans.warn().log(NEW_CASSANDRA_CLUSTER_OBJECT_CREATED);
                            }
                            if (reset || session==null) {
                                session = cluster.connect(keyspace);
                                trans.warn().log(NEW_CASSANDRA_SESSION_CREATED);
                            }
                        } finally {
                            resetTrigger=false;
                            tt.done();
                        }
                    }
                }
            } finally {
                TimeTaken tt = trans.start("Clear Reset Deque", Env.SUB);
                try {
                    resetDeque.clear();
                    // Not clearing Session/Cluster appears to kill off FileHandles
                    if (tempSession!=null && !tempSession.isClosed()) {
                        tempSession.close();
                    }
                    if (tempCluster!=null && !tempCluster.isClosed()) {
                        tempCluster.close();
                    }
                } finally {
                    tt.done();
                }
            }
        }
        return session;
    }

    public final boolean reportPerhapsReset(TransStore trans, Exception e) {
        if (owningDAO!=null) {
            return owningDAO.reportPerhapsReset(trans, e);
        } else {
            boolean rv = false;
            if (CassAccess.isResetException(e)) {
                trans.warn().printf("Session Reset called for %s by %s ",session==null?"":session,e==null?"Mgmt Command":e.getClass().getName());
                resetDeque.addFirst(new ResetRequest(session));
                rv = resetTrigger = true;
            } 
            trans.error().log(e);
            return rv;
        }
    }

    public void close(TransStore trans) {
        if (owningDAO==null) {
            if (session!=null) {
                TimeTaken tt = trans.start("Cassandra Session Close", Env.SUB);
                try {
                    session.close();
                } finally {
                    tt.done();
                }
                session = null;
            } else {
                trans.debug().log("close called(), Session already closed");
            }
        } else {
            owningDAO.close(trans);
        }
    }

    protected void wasModified(TRANS trans, CRUD modified, DATA data, String ... override) {
    }

    protected interface Accept<DATA> {
        public boolean ok(DATA data);
    }

}



