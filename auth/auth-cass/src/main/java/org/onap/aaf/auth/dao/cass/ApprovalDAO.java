/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modification Copyright (c) 2019 IBM
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

package org.onap.aaf.auth.dao.cass;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.dao.Loader;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.DriverException;


public class ApprovalDAO extends CassDAOImpl<AuthzTrans,ApprovalDAO.Data> {
    public static final String PENDING = "pending";
    public static final String DENIED = "denied";
    public static final String APPROVED = "approved";

    private static final String TABLE = "approval";
    private static final String TABLELOG = "approved";
    private HistoryDAO historyDAO;
    private PSInfo psByUser;
    private PSInfo psByApprover;
    private PSInfo psByTicket;
    private PSInfo psByStatus;

    private static final int KEYLIMIT = 1;

    public ApprovalDAO(AuthzTrans trans, Cluster cluster, String keyspace) {
        super(trans, ApprovalDAO.class.getSimpleName(),cluster,keyspace,Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        historyDAO = new HistoryDAO(trans, this);
        init(trans);
    }


    public ApprovalDAO(AuthzTrans trans, HistoryDAO hDAO) {
        super(trans, ApprovalDAO.class.getSimpleName(),hDAO,Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        historyDAO=hDAO;
        init(trans);
    }

    public static class Data {
        public UUID   id;
        public UUID   ticket;
        public String user;
        public String approver;
        public String type;
        public String status;
        public String memo;
        public String operation;
        public Date updated;
    }

    private static class ApprovalLoader extends Loader<Data> {
        public static final ApprovalLoader deflt = new ApprovalLoader(KEYLIMIT);
    
        public ApprovalLoader(int keylimit) {
            super(keylimit);
        }
    
        @Override
        public Data load(Data data, Row row) {
            data.id = row.getUUID(0);
            data.ticket = row.getUUID(1);
            data.user = row.getString(2);
            data.approver = row.getString(3);
            data.type = row.getString(4);
            data.status = row.getString(5);
            data.memo = row.getString(6);
            data.operation = row.getString(7);
            // This is used to get "WRITETIME(STATUS)" from Approval, which gives us an "updated" 
            if (row.getColumnDefinitions().size()>8) {
                // Rows reported in MicroSeconds
                data.updated = new Date(row.getLong(8)/1000);
            }
            return data;
        }

        @Override
        protected void key(Data data, int idx, Object[] obj) {
            obj[idx]=data.id;
        }

        @Override
        protected void body(Data data, int idxParam, Object[] obj) {
                int idx = idxParam;
            obj[idx]=data.ticket;
            obj[++idx]=data.user;
            obj[++idx]=data.approver;
            obj[++idx]=data.type;
            obj[++idx]=data.status;
            obj[++idx]=data.memo;
            obj[++idx]=data.operation;
        }
    }

    private void init(AuthzTrans trans) {
        String[] helpers = setCRUD(trans, TABLE, Data.class, ApprovalLoader.deflt,8);
        psByUser = new PSInfo(trans, SELECT_SP + helpers[FIELD_COMMAS] + ", WRITETIME(status) FROM " + TABLE + 
                " WHERE user = ?", new ApprovalLoader(1) {
            @Override
            protected void key(Data data, int idx, Object[] obj) {
                obj[idx]=data.user;
            }
        }, readConsistency);
    
        psByApprover = new PSInfo(trans, SELECT_SP + helpers[FIELD_COMMAS] + ", WRITETIME(status) FROM " + TABLE + 
                " WHERE approver = ?", new ApprovalLoader(1) {
            @Override
            protected void key(Data data, int idx, Object[] obj) {
                obj[idx]=data.approver;
            }
        }, readConsistency);

        psByTicket = new PSInfo(trans, SELECT_SP + helpers[FIELD_COMMAS] + ", WRITETIME(status) FROM " + TABLE + 
                " WHERE ticket = ?", new ApprovalLoader(1) {
            @Override
            protected void key(Data data, int idx, Object[] obj) {
                obj[idx]=data.ticket;
            }
        }, readConsistency);

        psByStatus = new PSInfo(trans, SELECT_SP + helpers[FIELD_COMMAS] + ", WRITETIME(status) FROM " + TABLE + 
                " WHERE status = ?", new ApprovalLoader(1) {
            @Override
            protected void key(Data data, int idx, Object[] obj) {
                obj[idx]=data.status;
            }
        }, readConsistency);


    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.dao.CassDAOImpl#create(com.att.inno.env.TransStore, java.lang.Object)
     */
    @Override
    public Result<Data> create(AuthzTrans trans, Data data) {
        // If ID is not set (typical), create one.
        if (data.id==null) {
            data.id = Chrono.dateToUUID(System.currentTimeMillis());
        }
        Result<ResultSet> rs = createPS.exec(trans, C_TEXT, data);
        if (rs.notOK()) {
            return Result.err(rs);
        }
        return Result.ok(data);
    }


    public Result<List<ApprovalDAO.Data>> readByUser(AuthzTrans trans, String user) {
        return psByUser.read(trans, R_TEXT, new Object[]{user});
    }

    public Result<List<ApprovalDAO.Data>> readByApprover(AuthzTrans trans, String approver) {
        return psByApprover.read(trans, R_TEXT, new Object[]{approver});
    }

    public Result<List<ApprovalDAO.Data>> readByTicket(AuthzTrans trans, UUID ticket) {
        return psByTicket.read(trans, R_TEXT, new Object[]{ticket});
    }

    public Result<List<ApprovalDAO.Data>> readByStatus(AuthzTrans trans, String status) {
        return psByStatus.read(trans, R_TEXT, new Object[]{status});
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.dao.CassDAOImpl#delete(com.att.inno.env.TransStore, java.lang.Object, boolean)
     */
    @Override
    public Result<Void> delete(AuthzTrans trans, Data data, boolean reread) {
        if (reread || data.status == null) { // if Memo is empty, likely not full record
            Result<ResultSet> rd = readPS.exec(trans, R_TEXT, data);
            if (rd.notOK()) {
                return Result.err(rd);
            }
            ApprovalLoader.deflt.load(data, rd.value.one());
        }
        if (APPROVED.equals(data.status) || DENIED.equals(data.status)) { 
            StringBuilder sb = new StringBuilder("BEGIN BATCH\n");
            sb.append("INSERT INTO ");
            sb.append(TABLELOG);
            sb.append(" (id,user,approver,type,status,memo,operation) VALUES (");
            sb.append(data.id);
            sb.append(",'");
            sb.append(data.user);
            sb.append("','");
            sb.append(data.approver);
            sb.append("','");
            sb.append(data.type);
            sb.append("','");
            sb.append(data.status);
            sb.append("','");
            sb.append(data.memo.replace("'", "''"));
            sb.append("','");
            sb.append(data.operation);
            sb.append("');\n");
            sb.append("DELETE FROM ");
            sb.append(TABLE);
            sb.append(" WHERE id=");
            sb.append(data.id);
            sb.append(";\n");
            sb.append("APPLY BATCH;\n");
            TimeTaken tt = trans.start("DELETE APPROVAL",Env.REMOTE);
            try {
                if (async) {
                    getSession(trans).executeAsync(sb.toString());
                    return Result.ok();
                } else {
                    getSession(trans).execute(sb.toString());
                    return Result.ok();
                }
            } catch (DriverException | APIException | IOException e) {
                reportPerhapsReset(trans,e);
                return Result.err(Result.ERR_Backend, CassAccess.ERR_ACCESS_MSG);
            } finally {
                tt.done();
            }
        } else {
            return super.delete(trans, data, false);
        }

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
        hd.subject = subject?override[1]:data.user + "|" + data.approver;
        hd.memo = memo
                ? String.format("%s by %s", override[0], hd.user)
                : (modified.name() + "d approval for " + data.user);
        // Detail?
        // Reconstruct?
        if (historyDAO.create(trans, hd).status!=Status.OK) {
            trans.error().log("Cannot log to History");
        }
    }
}
