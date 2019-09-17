/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018 IBM.
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
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.cadi.util.CSV.Writer;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;
import org.onap.aaf.misc.env.util.Split;


public class ApprovedRpt extends Batch {
    
    private static final String APPR_RPT = "ApprovedRpt";
    private static final String CSV = ".csv";
    private Date now;
    private Writer approvedW;
    private CSV historyR;
    private static String yr_mon;
    
    public ApprovedRpt(AuthzTrans trans) throws APIException, IOException, OrganizationException {
        super(trans.env());
        trans.info().log("Starting Connection Process");
        
        TimeTaken tt0 = trans.start("Cassandra Initialization", Env.SUB);
        try {
//            TimeTaken tt = trans.start("Connect to Cluster", Env.REMOTE);
//            try {
//                session = cluster.connect();
//            } finally {
//                tt.done();
//            }
            
            now = new Date();
            String sdate = Chrono.dateOnlyStamp(now);
            File file = new File(logDir(),APPR_RPT + sdate +CSV);
            CSV csv = new CSV(env.access(),file);
            approvedW = csv.writer(false);
            
            historyR = new CSV(env.access(),args()[1]).setDelimiter('|');
            
            yr_mon = args()[0];
        } finally {
            tt0.done();
        }
    }

    @Override
    protected void run(AuthzTrans trans) {
        try {          
//            ResultSet results;
//            Statement stmt = new SimpleStatement( "select dateof(id), approver, status, user, type, memo from authz.approved;" );
//            results = session.execute(stmt);
//            Iterator<Row> iter = results.iterator();
//            Row row;
            /*
             *             while (iter.hasNext()) {
                ++totalLoaded;
                row = iter.next();
                d = row.getTimestamp(0);
                if(d.after(begin)) {
                    approvedW.row("aprvd",
                            Chrono.dateOnlyStamp(d),
                            row.getString(1),
                            row.getString(2),
                            row.getString(3),
                            row.getString(4),
                            row.getString(5)
                    );
                }
            }
             */
            GregorianCalendar gc = new GregorianCalendar();
            gc.add(GregorianCalendar.MONTH, -2);
            approvedW.comment("date, approver, status, user, role, memo");
            historyR.visit(row -> {
                String s = row.get(7);
                if(s.equals(yr_mon)) {
                    String target = row.get(5);
                    if("user_role".equals(target)) {
                        String action = row.get(1);
                        switch(action) {
                            case "create":
                                write("created",row);
                                break;
                            case "update":
                                write("approved",row);
                                break;
                            case "delete":
                                write("denied",row);
                                break;
                        }
                    }
                }
            });
            
        } catch (Exception e) {
            trans.info().log(e);
        }
    }
    
    private void write(String a_or_d, List<String> row) {
        String[] target = Split.splitTrim('|', row.get(4));
        
        if(target.length>1) {
            UUID id = UUID.fromString(row.get(0));
            Date date = Chrono.uuidToDate(id);
            String status;
            String memo;
            String approver = row.get(6);
            if("batch:JobChange".equals(approver)) {
                status = "reduced";
                memo = "existing role membership reduced to invoke reapproval";
            } else {
                status = a_or_d;
                memo = row.get(2);
            }
            if(!approver.equals(target[0])) {
                approvedW.row(
                    Chrono.niceDateStamp(date),
                    approver,
                    status,
                    target[0],
                    target[1],
                    memo
                );
            }
        }

        
    }

}
