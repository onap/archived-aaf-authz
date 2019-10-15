/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aaf.auth.batch;

import java.io.IOException;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.impl.Log4JLogTarget;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.exceptions.InvalidQueryException;

public abstract class CassBatch extends Batch {

    protected CassBatch(AuthzTrans trans, String log4JName) throws APIException, IOException, OrganizationException {
        super(trans.env());
        // Flow all Env Logs to Log4j
        Log4JLogTarget.setLog4JEnv(log4JName, env);
    
        TimeTaken tt = trans.start("Connect to Cluster", Env.REMOTE);
        try {
            session = cluster.connect();
        } finally {
            tt.done();
        }
    }

    @Override
    protected void _close(AuthzTrans trans) {
        session.close();
        trans.info().log("Closed Session");
    }

    public ResultSet executeQuery(String cql) {
        return executeQuery(cql,"");
    }

    public ResultSet executeQuery(String cql, String extra) {
        if (isDryRun() && !cql.startsWith("SELECT")) {
            if (extra!=null) {
                env.info().log("Would query" + extra + ": " + cql);
            }
        } else {
            if (extra!=null) {
                env.info().log("query" + extra + ": " + cql);
            }
            try {
                return session.execute(cql);
            } catch (InvalidQueryException e) {
                if (extra==null) {
                    env.info().log("query: " + cql);
                }
                throw e;
            }
        } 
        return null;
    }

}