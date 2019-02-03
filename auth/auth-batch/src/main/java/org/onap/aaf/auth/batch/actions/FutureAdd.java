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

import org.onap.aaf.auth.batch.helpers.Future;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;

public class FutureAdd extends ActionDAO<Future,FutureDAO.Data,String> {
    public FutureAdd(AuthzTrans trans, Cluster cluster, boolean dryRun) throws APIException, IOException {
        super(trans, cluster,dryRun);
    }
    
    public FutureAdd(AuthzTrans trans, ActionDAO<?,?,?> adao) {
        super(trans, adao);
    }

    @Override
    public Result<FutureDAO.Data> exec(AuthzTrans trans, Future f, String text) {
    	return exec(trans,f.fdd,text);
    }
    	
    public Result<FutureDAO.Data> exec(AuthzTrans trans, FutureDAO.Data fdd, String text) {
        if (dryRun) {
            trans.info().log("Would Add:",text,fdd.id, fdd.memo);
            return Result.ok(fdd);
        } else {
            Result<FutureDAO.Data> rv = q.futureDAO.create(trans, fdd);
            trans.info().log("Added:",text,fdd.id, fdd.memo);
            return rv;
        }
    }
    
}