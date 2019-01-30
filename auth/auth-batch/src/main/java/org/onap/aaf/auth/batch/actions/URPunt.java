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
import java.util.Date;
import java.util.List;

import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Chrono;

import com.datastax.driver.core.Cluster;

public class URPunt extends ActionPuntDAO<UserRole,Void,String> {
    public URPunt(AuthzTrans trans, Cluster cluster, int months, int range, boolean dryRun) throws APIException, IOException {
        super(trans,cluster, months, dryRun);
    }

    public URPunt(AuthzTrans trans, ActionDAO<?,?,?> adao, int months, int range) {
        super(trans, adao, months);
    }

    public Result<Void> exec(AuthzTrans trans, UserRole ur, String text) {
        if (dryRun) {
            trans.info().log("Would Update User",ur.user(),"and Role", ur.role(), text);
            return Result.ok();
        } else {
            Result<List<Data>> read = q.userRoleDAO.read(trans, ur.user(), ur.role());
            if (read.isOK()) {
                for (UserRoleDAO.Data data : read.value) {
                    Date from = data.expires;
                    data.expires = puntDate(from);
                    if (data.expires.compareTo(from)<=0) {
                        trans.debug().printf("Error: %s is same or before %s", Chrono.dateOnlyStamp(data.expires), Chrono.dateOnlyStamp(from));
                    } else {
                        trans.info().log("Updating User",ur.user(),"and Role", ur.role(), "from",Chrono.dateOnlyStamp(from),"to",Chrono.dateOnlyStamp(data.expires), text);
                        q.userRoleDAO.update(trans, data);
                    }
                }
                return Result.ok();
            } else {
                return Result.err(read);
            }
        }
    }
}