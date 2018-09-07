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

import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.hl.Function;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public abstract class ActionDAO<D,RV,T> implements Action<D,RV,T> {
    protected final Question q; 
    protected final Function f;
    private boolean clean;
    protected final boolean dryRun;

    public ActionDAO(AuthzTrans trans, Cluster cluster, boolean dryRun) throws APIException, IOException {
        q = new Question(trans, cluster, CassAccess.KEYSPACE, false);
        f = new Function(trans,q);
        clean = true;
        this.dryRun = dryRun;
    }
    
    public ActionDAO(AuthzTrans trans, ActionDAO<?,?,?> predecessor) {
        q = predecessor.q;
        f = new Function(trans,q);
        clean = false;
        dryRun = predecessor.dryRun;
    }
    
    public Session getSession(AuthzTrans trans) throws APIException, IOException {
        return q.historyDAO.getSession(trans);
    }
    
    public Question question() {
        return q;
    }
    
    public Function function() {
        return f;
    }

    public void close(AuthzTrans trans) {
        if(clean) {
            q.close(trans);
        }
    }

}
