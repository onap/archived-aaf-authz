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

import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Chrono;

import com.datastax.driver.core.Cluster;

public class URAdd extends ActionDAO<UserRole,UserRoleDAO.Data,String> {
    public URAdd(AuthzTrans trans, Cluster cluster, boolean dryRun) throws APIException, IOException {
        super(trans, cluster,dryRun);
    }
    
    public URAdd(AuthzTrans trans, ActionDAO<?,?,?> adao) {
        super(trans, adao);
    }

    @Override
    public Result<Data> exec(AuthzTrans trans, UserRole ur, String text) {
        if (dryRun) {
            trans.info().log("Would Add:",text,ur.role(),ur.user(),"on",Chrono.dateOnlyStamp(ur.expires()));
            return Result.ok(ur.urdd());
        } else {
            Result<Data> rv = q.userRoleDAO.create(trans, ur.urdd());
            trans.info().log("Added:",text,ur.role(),ur.user(),"on",Chrono.dateOnlyStamp(ur.expires()));
            return rv;
        }
    }
    
}