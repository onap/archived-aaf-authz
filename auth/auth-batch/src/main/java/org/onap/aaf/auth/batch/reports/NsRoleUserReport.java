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

package org.onap.aaf.auth.batch.reports;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.helpers.NS;
import org.onap.aaf.auth.batch.helpers.Role;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.batch.helpers.Visitor;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.cadi.util.CSV.Writer;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;


public class NsRoleUserReport extends Batch {
    
	private static final String REPORT = NsRoleUserReport.class.getSimpleName();
	private static final String CSV = ".csv";
	private Date now;
	private Writer report;
	private Map<String,Map<String,Integer>> theMap;
	
	public NsRoleUserReport(AuthzTrans trans) throws APIException, IOException, OrganizationException {
        super(trans.env());
        trans.info().log("Starting Connection Process");
        
        TimeTaken tt0 = trans.start("Cassandra Initialization", Env.SUB);
        try {
            TimeTaken tt = trans.start("Connect to Cluster", Env.REMOTE);
            try {
                session = cluster.connect();
            } finally {
                tt.done();
            }
            
            // Create Intermediate Output 
            now = new Date();
            String sdate = Chrono.dateOnlyStamp(now);
           	File file = new File(logDir(),REPORT + sdate +CSV);
            CSV csv = new CSV(env.access(),file);
            report = csv.writer(false);
            
            theMap = new TreeMap<>();

            NS.load(trans, session, NS.v2_0_11);
            Role.load(trans, session);
        } finally {
            tt0.done();
        }
    }

    @Override
    protected void run(AuthzTrans trans) {
		try {
			trans.info().log("Create Report on Roles by NS");
			
			final AuthzTrans transNoAvg = trans.env().newTransNoAvg();
			UserRole.load(transNoAvg, session, UserRole.v2_0_11, ur -> {
				if(ur.expires().after(now)) {
					Map<String, Integer> roleCount = theMap.get(ur.ns());
					Integer count;
					if(roleCount==null) {
						roleCount = new TreeMap<>();
						theMap.put(ur.ns(),roleCount);
						count = 0;
					} else {
						count = roleCount.get(ur.rname());
						if(count == null) {
							count = 0;
						}
					}
					roleCount.put(ur.rname(), count+1);
				}
			});
			
			for(Entry<String, Map<String, Integer>> ns_es : theMap.entrySet()) {
				for(Entry<String, Integer> r_es : ns_es.getValue().entrySet()) {
					report.row(ns_es.getKey(),r_es.getKey(),r_es.getValue());
				}
			}


		} finally {
		}
	}
 
	@Override
    protected void _close(AuthzTrans trans) {
        session.close();
        report.close();
    }

}
