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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.onap.aaf.auth.batch.reports.Analyze;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.util.Holder;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.cadi.util.CSV.Writer;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Chrono;

public class Approvals extends Batch {
    private final AuthzTrans noAvg;
    private BatchDataView dataview;
    private List<CSV> csvList;
    private Writer napproveCW;
    private final GregorianCalendar now;
    private final String sdate;
    private static final String CSV = ".csv";
    private static final String APPROVALS_NEW = "ApprovalsNew";

    public Approvals(AuthzTrans trans) throws APIException, IOException, OrganizationException {
        super(trans.env());
        now = new GregorianCalendar();
        sdate = Chrono.dateOnlyStamp(now);
        noAvg = env.newTransNoAvg();
        noAvg.setUser(new BatchPrincipal("batch:Approvals"));
        session = cluster.connect();
        dataview = new BatchDataView(noAvg,session,dryRun);
        NS.load(trans, session, NS.v2_0_11);
        Role.load(trans, session);
        UserRole.load(trans, session, UserRole.v2_0_11);

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
            f = new File(logDir(), Analyze.NEED_APPROVALS+Chrono.dateOnlyStamp()+".csv");
            if(f.exists()) {
                csvList.add(new CSV(env.access(),f).processAll());
            } else {
                trans.error().printf("CSV File %s does not exist",f.getAbsolutePath());
            }
        }


        File file = new File(logDir(),APPROVALS_NEW + sdate +CSV);
        CSV approveCSV = new CSV(env.access(),file);
        napproveCW = approveCSV.writer();
        napproveCW.row("info",APPROVALS_NEW,sdate,1);

    }

    @Override
    protected void run(AuthzTrans trans) {
        Map<String,Pending> mpending = new TreeMap<>();
        Pending p = Pending.create();

        Holder<Integer> count = new Holder<>(0);
        for(CSV neeedApproveCSV : csvList) {
            TimeTaken tt = trans.start("Processing %s's UserRoles",Trans.SUB,neeedApproveCSV.name());
            try {
                neeedApproveCSV.visit(row -> {
                    switch(row.get(0)) {
                        case UserRole.APPROVE_UR:
                            UserRoleDAO.Data urdd = UserRole.row(row);
                            // Create an Approval
                            ApprovalSet uras = new URApprovalSet(noAvg, now, dataview, () -> {
                                return urdd;
                            });
                            Result<Void> rw = uras.write(noAvg);
                            if(rw.isOK()) {
                                Set<String> approvers = uras.approvers();
                                if(approvers.isEmpty()) {
                                    trans.error().printf("No Approvers found for %s-%s (probably no owner)",urdd.user,urdd.role);
                                } else {
                                    for(String approver : approvers) {
                                        Pending mp = mpending.get(approver);
                                        if(mp==null) {
                                            mpending.put(approver, Pending.create());
                                        } else {
                                            mp.inc(p); // FYI, unlikely
                                        }
                                    }
                                    count.set(count.get()+1);
                                }
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

            tt = trans.start("Writing Approvals to %s",Trans.SUB,neeedApproveCSV.name());
            int cnt = 0;
            try {
                for(Entry<String, Pending> es : mpending.entrySet()) {
                    p.row(napproveCW,es.getKey());
                    ++cnt;
                }
            } finally {
                tt.done();
                trans.info().printf("Processed %d Reminders", cnt);
            }
         }
    }

    @Override
    protected void _close(AuthzTrans trans) {
        if(napproveCW!=null) {
            napproveCW.flush();
            napproveCW.close();
        }
    }
}
