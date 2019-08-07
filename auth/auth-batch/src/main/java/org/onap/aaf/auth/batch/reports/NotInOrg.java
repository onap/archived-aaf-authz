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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.helpers.Cred;
import org.onap.aaf.auth.batch.helpers.Cred.Instance;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.cadi.util.CSV.Writer;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;


public class NotInOrg extends Batch {
    
    private static final String NOT_IN_ORG = "NotInOrg";
    private static final String CSV = ".csv";
    private static final String INFO = "info";
    private Map<String, CSV.Writer> writerList;
    private Map<String, CSV.Writer> whichWriter; 
    private Date now;
    private Writer notInOrgW;
    private Writer notInOrgDeleteW;
    
    public NotInOrg(AuthzTrans trans) throws APIException, IOException, OrganizationException {
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
            
            // Load Cred.  We don't follow Visitor, because we have to gather up everything into Identity Anyway
            Cred.load(trans, session);

            // Create Intermediate Output 
            writerList = new HashMap<>();
            whichWriter = new TreeMap<>();

            now = new Date();
            String sdate = Chrono.dateOnlyStamp(now);
               File file = new File(logDir(),NOT_IN_ORG + sdate +CSV);
            CSV csv = new CSV(env.access(),file);
            notInOrgW = csv.writer(false);
            notInOrgW.row(INFO,NOT_IN_ORG,Chrono.dateOnlyStamp(now),0);
            writerList.put(NOT_IN_ORG,notInOrgW);
            
            // These will have been double-checked by the Organization, and can be deleted immediately.
            String fn = NOT_IN_ORG+"Delete";
            file = new File(logDir(),fn + sdate +CSV);
            CSV csvDelete = new CSV(env.access(),file);
            notInOrgDeleteW = csvDelete.writer(false);
            notInOrgDeleteW.row(INFO,fn,Chrono.dateOnlyStamp(now),0);
            writerList.put(NOT_IN_ORG,notInOrgW);
            
        } finally {
            tt0.done();
        }
    }

    @Override
    protected void run(AuthzTrans trans) {
        try {
            Map<String,Boolean> checked = new TreeMap<String, Boolean>();
            trans.info().log("Process Organization Identities");
            trans.info().log("User Roles");
            
            final AuthzTrans transNoAvg = trans.env().newTransNoAvg();
            UserRole.load(trans, session, UserRole.v2_0_11, ur -> {
                try {
                    if(!check(transNoAvg, checked, ur.user())) {
                        ur.row(whichWriter(transNoAvg,ur.user()),UserRole.UR);
                    }
                } catch (OrganizationException e) {
                    trans.error().log(e, "Error Decrypting X509");
                }
            });
            
            trans.info().log("Checking for Creds without IDs");
            
            for (Cred cred : Cred.data.values()) {
                if(!check(transNoAvg,checked, cred.id)) {
                    CSV.Writer cw = whichWriter(transNoAvg, cred.id);
                    for(Instance inst : cred.instances) {
                        cred.row(cw, inst);
                    }
                }
            }
            
        } catch (OrganizationException e) {
            trans.info().log(e);
        }
    }
    
 
    private Writer whichWriter(AuthzTrans transNoAvg, String id) {
        Writer w = whichWriter.get(id);
        if(w==null) {
            w = org.isRevoked(transNoAvg, id)?
                    notInOrgDeleteW:
                    notInOrgW;
            whichWriter.put(id,w);
        }
        return w;
    }

    private boolean check(AuthzTrans trans, Map<String, Boolean> checked, String id) throws OrganizationException {
        Boolean rv = checked.get(id);
        if(rv==null) {
            if(isSpecial(id)) { // do not check against org... too important to delete.
                return true; 
            }
            Organization org = trans.org();
            if(org != null) {
                Identity identity = org.getIdentity(trans, id);
                rv = identity!=null;
                checked.put(id, rv);
            } else {
                throw new OrganizationException("No Organization Found for " + id + ": required for processing");
            }
        }
        return rv;
    }

    
    @Override
    protected void _close(AuthzTrans trans) {
        session.close();
        for(CSV.Writer cw : writerList.values()) {
            cw.close();
        }
    }

}
