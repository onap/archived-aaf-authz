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
package org.onap.aaf.auth.batch.temp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.BatchPrincipal;
import org.onap.aaf.auth.batch.helpers.CQLBatch;
import org.onap.aaf.auth.batch.helpers.CQLBatchLoop;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.Hash;
import org.onap.aaf.cadi.configure.Factory;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public class DataMigrateDublin extends Batch {
    private final SecureRandom sr;
    private final AuthzTrans noAvg;

    public DataMigrateDublin(AuthzTrans trans) throws APIException, IOException, OrganizationException {
        super(trans.env());
        trans.info().log("Starting Connection Process");
    
        noAvg = env.newTransNoAvg();
        noAvg.setUser(new BatchPrincipal("Migrate"));

        TimeTaken tt0 = trans.start("Cassandra Initialization", Env.SUB);
        try {
            TimeTaken tt = trans.start("Connect to Cluster", Env.REMOTE);
            try {
                session = cluster.connect();
            } finally {
                tt.done();
            }
        } finally {
            tt0.done();
        }
    
        sr = new SecureRandom();
    }

    @Override
    protected void run(AuthzTrans trans) {
        ///////////////////////////
        trans.info().log("Add UniqueTag to Passwords");

        CQLBatchLoop cbl = new CQLBatchLoop(new CQLBatch(noAvg.info(),session), 50, dryRun);
        try {
            ResultSet rs = session.execute("SELECT id,type,expires,cred,tag FROM authz.cred");
            Iterator<Row> iter = rs.iterator();
            Row row;
            int count = 0;
            byte[] babytes = new byte[6];
            Map<String, List<CredInfo>> mlci = new TreeMap<>();
            Map<String, String> ba_tag = new TreeMap<>();
            while(iter.hasNext()) {
                ++count;
                row = iter.next();
                String tag = row.getString(4);
                int type = row.getInt(1);
                switch(type) {
                    case CredDAO.BASIC_AUTH:
                    case CredDAO.BASIC_AUTH_SHA256:
                        String key = row.getString(0) + '|' + type + '|' + Hash.toHex(row.getBytesUnsafe(3).array()); 
                        String btag = ba_tag.get(key);
                        if(btag == null) {
                            if(tag==null || tag.isEmpty()) {
                                sr.nextBytes(babytes);
                                btag = Hash.toHexNo0x(babytes);
                            } else {
                                btag = tag;
                            }
                            ba_tag.put(key, btag);
                        }
                    
                        if(!btag.equals(tag)) {
                            update(cbl,row,btag);
                        }
                        break;
                    case CredDAO.CERT_SHA256_RSA:
                        if(tag==null || tag.isEmpty()) {
                            String id = row.getString(0);
                            List<CredInfo> ld = mlci.get(id);
                            if(ld==null) {
                                ld = new ArrayList<>();
                                mlci.put(id,ld);
                            }
                               ld.add(new CredInfo(id,row.getInt(1),row.getTimestamp(2)));
                        }
                            break;
                }
            }
            cbl.flush();
            trans.info().printf("Processes %d cred records, updated %d records in %d batches.", count, cbl.total(), cbl.batches());
            count = 0;
        
            cbl.reset();
        
            trans.info().log("Add Serial to X509 Creds");
            rs = session.execute("SELECT ca, id, x509 FROM authz.x509");
            iter = rs.iterator();
            while(iter.hasNext()) {
                ++count;
                row = iter.next();
                String ca = row.getString(0);
                String id = row.getString(1);
                List<CredInfo> list = mlci.get(id);
                if(list!=null) {
                    ByteBuffer bb = row.getBytesUnsafe(2);
                    if(bb!=null) {
                        Collection<? extends Certificate> x509s = Factory.toX509Certificate(bb.array());
                        for(Certificate c : x509s) {
                            X509Certificate xc = (X509Certificate)c;
                            for(CredInfo ci : list) {
                                if(xc.getNotAfter().equals(ci.expires)) {
                                    ci.update(cbl, ca + '|' + xc.getSerialNumber());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            cbl.flush();
            trans.info().printf("Processed %d x509 records, updated %d records in %d batches.", count, cbl.total(), cbl.batches());
            count = 0;
        } catch (Exception e) {
            trans.error().log(e);
        }
    }

    private static class CredInfo {
        public final String id;
        public final int type;
        public final Date expires;
    
        public CredInfo(String id, int type, Date expires) {
            this.id = id;
            this.type = type;
            this.expires = expires;
        }
    
        public void update(CQLBatchLoop cbl, String newtag) {
            StringBuilder sb = cbl.inc();
            sb.append("UPDATE authz.cred SET tag='");
            sb.append(newtag);
            sb.append("' WHERE id='");
            sb.append(id);
            sb.append("' AND type=");
            sb.append(type);
            sb.append(" AND expires=dateof(maxtimeuuid(");
            sb.append(expires.getTime());
            sb.append("));");
        }
    }
    
    private void update(CQLBatchLoop cbl, Row row, String newtag) {
        StringBuilder sb = cbl.inc();
        sb.append("UPDATE authz.cred SET tag='");
        sb.append(newtag);
        sb.append("' WHERE id='");
        sb.append(row.getString(0));
        sb.append("' AND type=");
        sb.append(row.getInt(1));
        sb.append(" AND expires=dateof(maxtimeuuid(");
        Date lc = row.getTimestamp(2);
        sb.append(lc.getTime());
        sb.append("));");
    }

    @Override
    protected void _close(AuthzTrans trans) {
        trans.info().log("End " + this.getClass().getSimpleName() + " processing" );
        session.close();
    }

}
