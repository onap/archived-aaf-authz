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

package org.onap.aaf.auth.batch.update;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.BatchPrincipal;
import org.onap.aaf.auth.batch.approvalsets.ApprovalSet;
import org.onap.aaf.auth.batch.approvalsets.Pending;
import org.onap.aaf.auth.batch.approvalsets.URApprovalSet;
import org.onap.aaf.auth.batch.helpers.BatchDataView;
import org.onap.aaf.auth.batch.helpers.NS;
import org.onap.aaf.auth.batch.helpers.Role;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Holder;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Chrono;

public class Approvals extends Batch {
    private final AuthzTrans noAvg;
	private BatchDataView dataview;
	private List<CSV> csvList;
	private GregorianCalendar now;

    public Approvals(AuthzTrans trans) throws APIException, IOException, OrganizationException {
        super(trans.env());
        
        noAvg = env.newTransNoAvg();
        noAvg.setUser(new BatchPrincipal("batch:Approvals"));
        session = cluster.connect();
        dataview = new BatchDataView(noAvg,session,dryRun);
        NS.load(trans, session, NS.v2_0_11);
        Role.load(trans, session);
        UserRole.load(trans, session, UserRole.v2_0_11);

        now = new GregorianCalendar();
        
        csvList = new ArrayList<>();
        File f;
        if(args().length>0) {
        	for(int i=0;i<args().length;++i) {
        		f = new File(logDir(), args()[i]);
        		if(f.exists()) {
	        		csvList.add(new CSV(env.access(),f).processAll());
        		} else {
	            	trans.error().printf("CSV File %s does not exist",f.getAbsolutePath());
        		}
        	}
        } else {
        	f = new File(logDir(), "Approvals"+Chrono.dateOnlyStamp()+".csv");
        	if(f.exists()) {
        		csvList.add(new CSV(env.access(),f).processAll());
			} else {
	        	trans.error().printf("CSV File %s does not exist",f.getAbsolutePath());
			}
        }
        
        
    }

    @Override
    protected void run(AuthzTrans trans) {
    	Map<String,Pending> mpending = new TreeMap<>();
		Holder<Integer> count = new Holder<>(0);
        for(CSV approveCSV : csvList) {
        	TimeTaken tt = trans.start("Load Analyzed Reminders",Trans.SUB,approveCSV.name());
        	try {
				approveCSV.visit(row -> {
					switch(row.get(0)) {
						case Pending.REMIND:
							try {
								Pending p = new Pending(row);
								Pending mp = mpending.get(row.get(1));
								if(mp==null) {
									mpending.put(row.get(1), p);
								} else {
									mp.inc(p); // FYI, unlikely
								}
								count.set(count.get()+1);
							} catch (ParseException e) {
								trans.error().log(e);
							} 
						break;
					}
				});
			} catch (IOException | CadiException e) {
				e.printStackTrace();
				// .... but continue with next row
        	} finally {
        		tt.done();
        	}
        }
        trans.info().printf("Processed %d Reminder Rows", count.get());

        count.set(0);
        for(CSV approveCSV : csvList) {
        	TimeTaken tt = trans.start("Processing %s's UserRoles",Trans.SUB,approveCSV.name());
        	try {
				approveCSV.visit(row -> {
					switch(row.get(0)) {
						case UserRole.APPROVE_UR:
							UserRoleDAO.Data urdd = UserRole.row(row);
							// Create an Approval
							ApprovalSet uras = new URApprovalSet(noAvg, now, dataview, () -> {
								return urdd;
							});
							Result<Void> rw = uras.write(noAvg);
							if(rw.isOK()) {
								Pending p = new Pending();
								Pending mp = mpending.get(urdd.user);
								if(mp==null) {
									mpending.put(urdd.user, p);
								} else {
									mp.inc(p);
								}
								count.set(count.get()+1);
							} else {
								trans.error().log(rw.errorString());
							}
							break;
					}
				});
				dataview.flush();
			} catch (IOException | CadiException e) {
				e.printStackTrace();
				// .... but continue with next row
	    	} finally {
	    		tt.done();
	    	}
            trans.info().printf("Processed %d UserRoles", count.get());

            count.set(0);
        	tt = trans.start("Notify for Pending", Trans.SUB);
        	try {
        		
        	} finally {
        		tt.done();
        	}
            trans.info().printf("Created %d Notifications", count.get());
	    }
    }
    
    @Override
    protected void _close(AuthzTrans trans) {
    	if(session!=null) {
    		session.close();
    		session = null;
    	}
    }
}
