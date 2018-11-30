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
import java.util.List;

import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;

public class URModify extends ActionDAO<UserRole,Void,URModify.Modify> {
    public URModify(AuthzTrans trans, Cluster cluster, boolean dryRun) throws APIException, IOException {
        super(trans, cluster,dryRun);
    }
    
    public URModify(AuthzTrans trans, ActionDAO<?,?,?> adao) {
        super(trans, adao);
    }

    @Override
    public Result<Void> exec(AuthzTrans trans, UserRole ur,Modify modify) {
        if (dryRun) {
            trans.info().printf("Would Update %s %s", ur.user(), ur.role());
            return Result.ok();
        } else {
            Result<List<Data>> rr = q.userRoleDAO.read(trans, ur.user(),ur.role());
            if (rr.notOKorIsEmpty()) {
                return Result.err(rr);
            }
            for (Data d : rr.value) {
                modify.change(d);
                if (!(ur.expires().equals(d.expires))) {
                    ur.expires(d.expires);
                }
                if (ur.user().equals(d.user) && ur.role().equals(d.role)){
                    Result<Void> rv = q.userRoleDAO.update(trans, d);
                    if (rv.isOK()) {
                        trans.info().printf("Updated %s %s to %s", ur.user(), ur.role(), d.toString());
                    } else {
                        trans.info().log(rv.errorString());
                    }
                } else {
                    return Result.err(Status.ERR_Denied, "You cannot change the key of this Data");
                }
            }
            return Result.err(Status.ERR_UserRoleNotFound,"No User Role with %s %s",ur.user(),ur.role());
        }
    }
    
    public static interface Modify {
        void change(UserRoleDAO.Data ur);
    }
    
}