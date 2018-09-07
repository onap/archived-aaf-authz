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

package org.onap.aaf.auth.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.cass.ApprovalDAO.Data;
import org.onap.aaf.auth.dao.hl.Function.FUTURE_OP;
import org.onap.aaf.auth.dao.hl.Function.Lookup;
import org.onap.aaf.auth.dao.hl.Function.OP_STATUS;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.helpers.Approval;
import org.onap.aaf.auth.helpers.Future;
import org.onap.aaf.auth.helpers.UserRole;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;

public class URFutureApproveExec extends ActionDAO<List<Approval>, OP_STATUS, Future> {

    public URFutureApproveExec(AuthzTrans trans, Cluster cluster, boolean dryRun) throws APIException, IOException {
        super(trans,cluster, dryRun);
    }
    
    public URFutureApproveExec(AuthzTrans trans, ActionDAO<?,?,?> adao) {
        super(trans, adao);
    }

    @Override
    public Result<OP_STATUS> exec(AuthzTrans trans, List<Approval> app, Future future) {
        if(dryRun) {
            return Result.err(Result.ERR_ActionNotCompleted,"Not Executed");
        } else {
            // Save on Lookups
            final List<ApprovalDAO.Data> apprs = new ArrayList<>();
            final List<UserRoleDAO.Data> urs = new ArrayList<>();
            for(Approval a : app) {
                apprs.add(a.add);
                UserRole ur = UserRole.get(a.add.user, future.role);
                if(ur!=null) {
                    urs.add(ur.urdd());
                }
            }
            Result<OP_STATUS> rv = f.performFutureOp(trans, FUTURE_OP.A, future.fdd,
                new Lookup<List<ApprovalDAO.Data>>() {
                    @Override
                    public List<Data> get(AuthzTrans trans, Object ... noop) {
                        return apprs;
                    }
                },
                new Lookup<UserRoleDAO.Data>() {
                    @Override
                    public UserRoleDAO.Data get(AuthzTrans trans, Object ... keys) {
                        List<UserRole> lur = UserRole.getByUser().get(keys[0]);
                        if(lur!=null) {
                            for(UserRole ur : lur) {
                                if(ur.role().equals(keys[1])) {
                                    return ur.urdd();
                                }
                            }
                        }
                        return null;
                    }
                });
            if(rv.isOK()) {
                switch(rv.value) {
                    case D:
                        trans.info().printf("Denied %s on %s", future.memo(),future.fdd.target);
                        break;
                    case E:
                        trans.info().printf("Completed %s on %s", future.memo(),future.fdd.target);
                        break;
                    case L:
                        trans.info().printf("Future %s on %s has lapsed", future.memo(),future.fdd.target);
                        break;
                    default:
                }
            } else {
                trans.error().log("Error completing",future.memo(),rv.errorString());
            }
            return rv;
        }
    }
}