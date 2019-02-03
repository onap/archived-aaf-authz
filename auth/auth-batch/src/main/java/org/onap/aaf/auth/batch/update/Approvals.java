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
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.BatchPrincipal;
import org.onap.aaf.auth.batch.approvalsets.ApprovalSet;
import org.onap.aaf.auth.batch.approvalsets.URApprovalSet;
import org.onap.aaf.auth.batch.helpers.Approval;
import org.onap.aaf.auth.batch.helpers.BatchDataView;
import org.onap.aaf.auth.batch.helpers.Future;
import org.onap.aaf.auth.batch.helpers.NS;
import org.onap.aaf.auth.batch.helpers.Role;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Chrono;

public class Approvals extends Batch {
    private final AuthzTrans noAvg;
	private BatchDataView dataview;

    public Approvals(AuthzTrans trans) throws APIException, IOException, OrganizationException {
        super(trans.env());
        
        noAvg = env.newTransNoAvg();
        noAvg.setUser(new BatchPrincipal("batch:Approvals"));

        dataview = new BatchDataView(noAvg,cluster,dryRun);
        
        session = dataview.getSession(trans);
        
        Approval.load(trans, session, Approval.v2_0_17);
        Future.load(trans, session, Future.v2_0_17);
        Role.load(trans, session);
        NS.load(trans, session, NS.v2_0_11);
        UserRole.load(trans, session, UserRole.v2_0_11);
    }

    @Override
    protected void run(AuthzTrans trans) {
        // Create Intermediate Output 
        final GregorianCalendar now = new GregorianCalendar();
        
        List<File> approveFiles = new ArrayList<>();
        if(args().length>0) {
        	for(int i=0;i<args().length;++i) {
        		approveFiles.add(new File(logDir(), args()[i]));
        	}
        } else {
        	approveFiles.add(new File(logDir(),"OneMonth"+Chrono.dateOnlyStamp()+".csv"));
        }
        
        for(File f : approveFiles) {
        	trans.init().log("Processing File:",f.getAbsolutePath());
        }
        
//        GregorianCalendar gc = new GregorianCalendar();
//        Date now = gc.getTime();
//        String today = Chrono.dateOnlyStamp(now);
        for(File f : approveFiles) {
        	trans.info().log("Processing ",f.getAbsolutePath(),"for Approvals");
        	if(f.exists()) {
		        CSV approveCSV = new CSV(env.access(),f).processAll();
		        try {
					approveCSV.visit(row -> {
						switch(row.get(0)) {
							case "ur":
								UserRoleDAO.Data urdd = UserRole.row(row);
								List<Approval> apvs = Approval.byUser.get(urdd.user);
								
								System.out.println(row);
								if(apvs==null) {
									// Create an Approval
									ApprovalSet uras = new URApprovalSet(noAvg, now, dataview, () -> {
										return urdd;
									});
									Result<Void> rw = uras.write(noAvg);
									if(rw.notOK()) {
										System.out.println(rw.errorString());
									}
								} else {
									// Check that Existing Approval is still valid
									for(Approval a : apvs) {
										Future ticket = Future.data.get(a.add.ticket);
										if(ticket==null) {
											// Orphaned Approval - delete
										} else {
											
										}
									}
								}
								break;
							default:
								System.out.println(row);
								//noAvg.debug().printf("Ignoring %s",type);
						}
					});
				} catch (IOException | CadiException e) {
					e.printStackTrace();
					// .... but continue with next row
				}
		        
		        /*
        List<Approval> pending = new ArrayList<>();
        boolean isOwner,isSupervisor;
        for (Entry<String, List<Approval>> es : Approval.byApprover.entrySet()) {
            isOwner = isSupervisor = false;
            String approver = es.getKey();
            if (approver.indexOf('@')<0) {
                approver += org.getRealm();
            }
            Date latestNotify=null, soonestExpire=null;
            GregorianCalendar latest=new GregorianCalendar();
            GregorianCalendar soonest=new GregorianCalendar();
            pending.clear();
            
            for (Approval app : es.getValue()) {
                Future f = app.getTicket()==null?null:Future.data.get(app.getTicket());
                if (f==null) { // only Ticketed Approvals are valid.. the others are records.
                    // Approvals without Tickets are no longer valid. 
                    if ("pending".equals(app.getStatus())) {
                        app.setStatus("lapsed");
                        app.update(noAvg,apprDAO,dryRun); // obeys dryRun
                    }
                } else {
                    if ((soonestExpire==null && f.expires()!=null) || (soonestExpire!=null && f.expires()!=null && soonestExpire.before(f.expires()))) {
                        soonestExpire=f.expires();
                    }

                    if ("pending".equals(app.getStatus())) {
                        if (!isOwner) {
                            isOwner = "owner".equals(app.getType());
                        }
                        if (!isSupervisor) {
                            isSupervisor = "supervisor".equals(app.getType());
                        }

                        if ((latestNotify==null && app.getLast_notified()!=null) ||(latestNotify!=null && app.getLast_notified()!=null && latestNotify.before(app.getLast_notified()))) {
                            latestNotify=app.getLast_notified();
                        }
                        pending.add(app);
                    }
                }
            }

            if (!pending.isEmpty()) {
                boolean go = false;
                if (latestNotify==null) { // never notified... make it so
                    go=true;
                } else {
                    if (!today.equals(Chrono.dateOnlyStamp(latest))) { // already notified today
                        latest.setTime(latestNotify);
                        soonest.setTime(soonestExpire);
                        int year;
                        int days = soonest.get(GregorianCalendar.DAY_OF_YEAR)-latest.get(GregorianCalendar.DAY_OF_YEAR);
                        days+=((year=soonest.get(GregorianCalendar.YEAR))-latest.get(GregorianCalendar.YEAR))*365 + 
                                (soonest.isLeapYear(year)?1:0);
                        if (days<7) { // If Expirations get within a Week (or expired), notify everytime.
                            go = true;
                        }
                    }
                }
            }
          */
        	}
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
