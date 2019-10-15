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

package org.onap.aaf.auth.env;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.rserv.TransOnlyFilter;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans.Metric;

public class AuthzTransOnlyFilter extends TransOnlyFilter<AuthzTrans> {
    private AuthzEnv env;
    public Metric serviceMetric;

    public static final int BUCKETSIZE = 2;

    public AuthzTransOnlyFilter(AuthzEnv env) {
        this.env = env;
        serviceMetric = new Metric();
        serviceMetric.buckets = new float[BUCKETSIZE]; 
    }

    @Override
    protected AuthzTrans newTrans(HttpServletRequest req, HttpServletResponse resp) {
        AuthzTrans trans = env.newTrans();
        trans.set(req, resp);
        return trans;
    }

    @Override
    protected TimeTaken start(AuthzTrans trans) {
        return trans.start("Trans " + //(context==null?"n/a":context.toString()) +
        " IP: " + trans.ip() +
        " Port: " + trans.port()
        , Env.SUB);
    }

    @Override
    protected void authenticated(AuthzTrans trans, TaggedPrincipal p) {
        trans.setUser(p);
    }

    @Override
    protected void tallyHo(AuthzTrans trans) {
        // Transaction is done, now post
        StringBuilder sb = new StringBuilder("AuditTrail\n");
        // We'll grab sub-metrics for Remote Calls and JSON
        // IMPORTANT!!! if you add more entries here, change "BUCKETSIZE"!!!
        Metric m = trans.auditTrail(1, sb, Env.REMOTE,Env.JSON);
        // Add current Metrics to total metrics
        serviceMetric.total+= m.total;
        for (int i=0;i<serviceMetric.buckets.length;++i) {
            serviceMetric.buckets[i]+=m.buckets[i];
        }
        // Log current info
        sb.append("  Total: ");
        sb.append(m.total);
        sb.append(" Remote: ");
        sb.append(m.buckets[0]);
        sb.append(" JSON: ");
        sb.append(m.buckets[1]);
        trans.info().log(sb);
    }

}
