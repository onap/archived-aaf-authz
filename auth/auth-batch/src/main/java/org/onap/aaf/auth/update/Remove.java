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

package org.onap.aaf.auth.update;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.onap.aaf.auth.Batch;
import org.onap.aaf.auth.BatchPrincipal;
import org.onap.aaf.auth.actions.CacheTouch;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.helpers.CQLBatch;
import org.onap.aaf.auth.helpers.Cred;
import org.onap.aaf.auth.helpers.UserRole;
import org.onap.aaf.auth.helpers.X509;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Holder;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;

public class Remove extends Batch {
    private final AuthzTrans noAvg;
	private CacheTouch cacheTouch;
	private CQLBatch cqlBatch;

    public Remove(AuthzTrans trans) throws APIException, IOException, OrganizationException {
        super(trans.env());
        trans.info().log("Starting Connection Process");
        
        noAvg = env.newTransNoAvg();
        noAvg.setUser(new BatchPrincipal("batch:RemoveExpired"));

        TimeTaken tt0 = trans.start("Cassandra Initialization", Env.SUB);
        try {
        	cacheTouch = new CacheTouch(trans, cluster, dryRun);
            TimeTaken tt2 = trans.start("Connect to Cluster", Env.REMOTE);
            try {
                session = cacheTouch.getSession(trans);
            } finally {
                tt2.done();
            }
            cqlBatch = new CQLBatch(session); 
            

        } finally {
            tt0.done();
        }
    }

    @Override
    protected void run(AuthzTrans trans) {
        final int maxBatch = 50;

        // Create Intermediate Output 
        File logDir = new File(logDir());
        
        File expired = new File(logDir,"Delete"+Chrono.dateOnlyStamp()+".csv");
        CSV expiredCSV = new CSV(expired);
        try {
        	final StringBuilder sb = cqlBatch.begin();
            final Holder<Integer> hi = new Holder<Integer>(0);
			expiredCSV.visit(new CSV.Visitor() {
				@Override
				public void visit(List<String> row) throws IOException, CadiException {
					int i = hi.get();
					if(i>=maxBatch) {
						cqlBatch.execute(dryRun);
						hi.set(0);
						cqlBatch.begin();
						i=0;
					}
					switch(row.get(0)) {
						case "ur":
							hi.set(++i);
							UserRole.row(sb,row);
							break;
						case "cred":
							hi.set(++i);
							Cred.row(sb,row);
					    	break;
						case "x509":
							hi.set(++i);
							X509.row(sb,row);
							break;
					}
				}
			});
			cqlBatch.execute(dryRun);
		} catch (IOException | CadiException e) {
			e.printStackTrace();
		}
    }
    
    @Override
    protected void _close(AuthzTrans trans) {
        session.close();
        cacheTouch.close(trans);
    }

}
