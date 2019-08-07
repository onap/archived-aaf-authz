/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
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
import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.auth.batch.approvalsets.DataView;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.Hash;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Chrono;

import com.datastax.driver.core.Session;

public class BatchDataView implements DataView {
    private static final String QUOTE_PAREN_SEMI = "');\n";
    private static final String QUOTE_COMMA = "',";
    private static final String QUOTE_COMMA_QUOTE = "','";
    private static final String COMMA_QUOTE = ",'";
    private final CQLBatchLoop cqlBatch;
    private final Session session;

    public BatchDataView(final AuthzTrans trans, final Session session, final boolean dryRun ) throws APIException, IOException {
        this.session = session;
        cqlBatch = new CQLBatchLoop(new CQLBatch(trans.info(),session),50,dryRun);
    }

    public Session getSession(AuthzTrans trans) throws APIException, IOException {
        return session;
    }
    
    public Result<NsDAO.Data> ns(AuthzTrans trans, String id) {
        NS n;
        TimeTaken tt = trans.start("Get NS by ID %s", Trans.SUB, id);
        try {
            n=NS.data.get(id);
        } finally {
            tt.done();
        }
        
        if(n==null || n.ndd==null) {
            return Result.err(Result.ERR_Backend,"Namespace '%s' does not exist", id);
        }
        return Result.ok(n.ndd);
    }

    
    @Override
    public Result<RoleDAO.Data> roleByName(AuthzTrans trans, String name) {
        Role r = Role.byName.get(name);
        if(r==null || r.rdd==null) {
            return Result.err(Result.ERR_Backend,"Role '%s' does not exist", name);
        }
        return Result.ok(r.rdd);
    }

    @Override
    public Result<List<UserRoleDAO.Data>> ursByRole(AuthzTrans trans, String role) {
        List<UserRole> urs = UserRole.getByRole().get(role);
        if(urs==null) {
            return Result.err(Result.ERR_Backend, "UserRoles for Role '%s' does not exist", role);
        }
        return toLURDD(urs);
    }

    private Result<List<Data>> toLURDD(List<UserRole> urs) {
        List<UserRoleDAO.Data> rv = new ArrayList<>();
        if(urs!=null) {
            for(UserRole ur : urs) {
                rv.add(ur.urdd());
            }
        }
        return Result.ok(rv);
    }

    @Override
    public Result<List<UserRoleDAO.Data>> ursByUser(AuthzTrans trans, String user) {
        List<UserRole> urs = UserRole.getByUser().get(user);
        if(urs==null) {
            return Result.err(Result.ERR_Backend, "UserRoles for User '%s' does not exist", user);
        }
        return toLURDD(urs);
    }

    @Override
    public Result<FutureDAO.Data> delete(AuthzTrans trans, FutureDAO.Data fdd) {
        StringBuilder sb = cqlBatch.inc();
        sb.append("DELETE from authz.future WHERE id = ");
        sb.append(fdd.id.toString());
        return Result.ok(fdd);        
    }
    
    @Override
    public Result<ApprovalDAO.Data> delete(AuthzTrans trans, ApprovalDAO.Data add) {
        StringBuilder sb = cqlBatch.inc();
        sb.append("DELETE from authz.approval WHERE id = ");
        sb.append(add.id.toString());
        return Result.ok(add);        
    }


    @Override
    public Result<ApprovalDAO.Data> insert(AuthzTrans trans, ApprovalDAO.Data add) {
        StringBuilder sb = cqlBatch.inc();
        sb.append("INSERT INTO authz.approval (id,approver,memo,operation,status,ticket,type,user) VALUES ("); 
        sb.append(add.id.toString());
        sb.append(COMMA_QUOTE);
        sb.append(add.approver);
//        sb.append(QUOTE_COMMA_QUOTE);
//        sb.append(Chrono.utcStamp(add.last_notified));
        sb.append(QUOTE_COMMA_QUOTE);
        sb.append(add.memo.replace("'", "''"));
        sb.append(QUOTE_COMMA_QUOTE);
        sb.append(add.operation);
        sb.append(QUOTE_COMMA_QUOTE);
        sb.append(add.status);
        sb.append(QUOTE_COMMA);
        sb.append(add.ticket.toString());
        sb.append(COMMA_QUOTE);
        sb.append(add.type);
        sb.append(QUOTE_COMMA_QUOTE);
        sb.append(add.user);
        sb.append(QUOTE_PAREN_SEMI);
        return Result.ok(add);
    }

    @Override
    public Result<FutureDAO.Data> insert(AuthzTrans trans, FutureDAO.Data fdd) {
        StringBuilder sb = cqlBatch.inc();
        sb.append("INSERT INTO authz.future (id,construct,expires,memo,start,target,target_key,target_date) VALUES ("); 
        sb.append(fdd.id.toString());
        sb.append(',');
        fdd.construct.hasArray();
        sb.append(Hash.toHex(fdd.construct.array()));
        sb.append(COMMA_QUOTE);
        sb.append(Chrono.utcStamp(fdd.expires));
        sb.append(QUOTE_COMMA_QUOTE);
        sb.append(fdd.memo.replace("'", "''"));
        sb.append(QUOTE_COMMA_QUOTE);
        sb.append(Chrono.utcStamp(fdd.expires));
        sb.append(QUOTE_COMMA_QUOTE);
        sb.append(fdd.target);
        if(fdd.target_key==null) {
            sb.append("',,'");
        } else {
            sb.append(QUOTE_COMMA_QUOTE);
            sb.append(fdd.target_key==null?"":fdd.target_key);
            sb.append(QUOTE_COMMA_QUOTE);
        }
        sb.append(Chrono.utcStamp(fdd.target_date));
        sb.append(QUOTE_PAREN_SEMI);
        return Result.ok(fdd);
    }
    
    @Override
    public void flush() {
        cqlBatch.flush();
    }
}
