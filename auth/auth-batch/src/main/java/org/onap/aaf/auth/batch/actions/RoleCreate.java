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

import org.onap.aaf.auth.batch.helpers.Role;
import org.onap.aaf.auth.dao.cass.RoleDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;

public class RoleCreate extends ActionDAO<Role,Data,String> {
    public RoleCreate(AuthzTrans trans, Cluster cluster, boolean dryRun) throws APIException, IOException {
        super(trans, cluster,dryRun);
    }
    
    public RoleCreate(AuthzTrans trans, ActionDAO<?,?,?> adao) {
        super(trans, adao);
    }

    @Override
    public Result<Data> exec(AuthzTrans trans, Role r,String text) {
        if (dryRun) {
            trans.info().log("Would Create Role:",text,r.fullName());
            return Result.ok(r.rdd);
        } else {
            Result<Data> rv = q.roleDAO.create(trans, r.rdd); // need to read for undelete
            if (rv.isOK()) {
                trans.info().log("Created Role:",text,r.fullName());
            } else {
                trans.error().log("Error Creating Role -",rv.details,":",r.fullName());
            }
            return rv;
        }
    }
    
}