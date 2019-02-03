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

package org.onap.aaf.auth.batch.actions;

import java.io.IOException;

import org.onap.aaf.auth.batch.helpers.Approval;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;

public class ApprovalAdd extends ActionDAO<Approval,ApprovalDAO.Data,String> {
    public ApprovalAdd(AuthzTrans trans, Cluster cluster, boolean dryRun) throws APIException, IOException {
        super(trans, cluster,dryRun);
    }
    
    public ApprovalAdd(AuthzTrans trans, ActionDAO<?,?,?> adao) {
        super(trans, adao);
    }

    @Override
    public Result<ApprovalDAO.Data> exec(AuthzTrans trans, Approval app, String text) {
    	return exec(trans,app.add,text);
    }

    public Result<ApprovalDAO.Data> exec(AuthzTrans trans, ApprovalDAO.Data add, String text) {
    	if (dryRun) {
            trans.info().log("Would Add:",text,add.approver,add.memo);
            return Result.ok(add);
        } else {
            Result<ApprovalDAO.Data> rv = q.approvalDAO.create(trans, add);
            trans.info().log("Added:",text,add.approver,add.memo);
            return rv;
        }
    }
    
}