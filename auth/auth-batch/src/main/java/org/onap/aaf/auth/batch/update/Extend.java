/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 IBM.
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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.BatchPrincipal;
import org.onap.aaf.auth.batch.helpers.CQLBatch;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.batch.reports.PrepExtend;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.CredDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Holder;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;

public class Extend extends Batch {
    private final CQLBatch cqlBatch;
    private final CredDAO credDAO;
    private final AuthzTrans noAvg;
    private List<File> extFiles;
    private final int extendBy;
    private int gcType;
    
    public Extend(AuthzTrans trans) throws APIException, IOException, OrganizationException {
        super(trans.env());
        trans.info().log("Starting Connection Process");
        
        noAvg = env.newTransNoAvg();
        noAvg.setUser(new BatchPrincipal("Extend"));

        TimeTaken tt0 = trans.start("Cassandra Initialization", Env.SUB);
        try {
            TimeTaken tt = trans.start("Connect to Cluster", Env.REMOTE);
            credDAO = new CredDAO(trans, cluster, CassAccess.KEYSPACE);
            try {
                session = credDAO.getSession(trans);
            } finally {
                tt.done();
            }
            cqlBatch = new CQLBatch(noAvg.info(),session); 
        } finally {
            tt0.done();
        }

        gcType = GregorianCalendar.WEEK_OF_YEAR;
        int weeks = 4;
        
        Set<String> cmd = new HashSet<>();
        for(int i=0; i< args().length;++i) {
            if("-weeks".equals(args()[i])) {
                if(args().length>i+1) {
                    weeks = Integer.parseInt(args()[++i]);
                }
            } else {
                cmd.add(args()[i]);
            }
        }
        
        if(weeks<1 || weeks > 24) {
            throw new APIException("Invalid --weeks");
        }
        extendBy = weeks;

        // Create Intermediate Output 
        File logDir = logDir();
        extFiles = new ArrayList<>();
        if(cmd.isEmpty()) {
            extFiles.add(new File(logDir,PrepExtend.PREP_EXTEND+Chrono.dateOnlyStamp()+".csv"));
        } else {
            for(String fn : cmd) {
                extFiles.add(new File(logDir, fn));
            }
        }
        
        // Load Cred.  We don't follow Visitor, because we have to gather up everything into Identity Anyway
        // to find the last one.
    }

    @Override
    protected void run(AuthzTrans trans) {
        final int maxBatch = 50;

        // Setup Date boundaries
        final Holder<GregorianCalendar> hgc = new Holder<>(new GregorianCalendar());
        final GregorianCalendar now = new GregorianCalendar();

        ///////////////////////////
        trans.info().log("Bulk Extend Expiring User-Roles and Creds");

        final Holder<List<String>> info = new Holder<>(null);
        final Holder<StringBuilder> hsb = new Holder<>(null);

        for(File f : extFiles) {
            CSV csv = new CSV(env.access(),f);
            try {
                csv.visit(new CSV.Visitor() {
                    final Holder<Integer> hi = new Holder<>(0); 

                    @Override
                    public void visit(List<String> row) throws IOException, CadiException {
                        GregorianCalendar gc;
                        int i = hi.get();
                        StringBuilder sb = hsb.get();
                        if(sb==null) {
                            hsb.set(sb=cqlBatch.begin());
                        }
                        switch(row.get(0)) {
                            case "info":
                                info.set(row);
                                break;
                            case "ur":
                                hi.set(++i);
                                gc = hgc.get();
                                gc.setTime(new Date(Long.parseLong(row.get(6))));
                                if(gc.before(now)) {
                                    gc.setTime(now.getTime());
                                }
                                gc.add(gcType, extendBy);
                                UserRole.batchExtend(sb,row,gc.getTime());
                                break;
                            case "cred":
                                int ctype = Integer.parseInt(row.get(3));
                                if(ctype == CredDAO.BASIC_AUTH_SHA256 || ctype == CredDAO.BASIC_AUTH) {
                                    Result<List<Data>> result = credDAO.readID(noAvg, row.get(1));
                                    if(result.isOKhasData()) {
                                        for(CredDAO.Data cd : result.value) {
                                            if(cd.type == CredDAO.BASIC_AUTH_SHA256 || cd.type == CredDAO.BASIC_AUTH) {
                                                String prev;
                                                prev=Chrono.dateOnlyStamp(cd.expires);
                                                if(row.get(4).equals(prev)){
                                                    gc = hgc.get();
                                                    gc.setTime(new Date(Long.parseLong(row.get(5))));
                                                    if(gc.before(now)) {
                                                        gc.setTime(now.getTime());
                                                    }
                                                    gc.add(gcType, extendBy);
                                                    cd.expires = gc.getTime();
                                                    if(dryRun) {
                                                        noAvg.info().printf("Would extend %s, %d - %s to %s",cd.id,cd.type,prev, Chrono.dateOnlyStamp(cd.expires));
                                                    } else {
                                                        Result<Void> r = credDAO.update(noAvg, cd, true);
                                                        noAvg.info().printf("%s %s, %d - %s to %s",
                                                                r.isOK()?"Extended":"Failed to Extend",
                                                                cd.id,cd.type,prev, Chrono.dateOnlyStamp(cd.expires));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                        }
                        if(i%maxBatch==0 && sb!=null) {
                            cqlBatch.execute(dryRun);
                            hi.set(1);
                            sb=null;
                            hsb.set(sb);
                        }
                    }
                });
            } catch (IOException | CadiException e) {
                e.printStackTrace();
            }
        }
        
        // Cleanup, if required.
        cqlBatch.execute(dryRun);

    }
    
    @Override
    protected void _close(AuthzTrans trans) {
        trans.info().log("End " + this.getClass().getSimpleName() + " processing" );
        credDAO.close(trans);
        session.close();
    }

}
