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

package org.onap.aaf.auth.reports;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.onap.aaf.auth.Batch;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.helpers.Cred;
import org.onap.aaf.auth.helpers.UserRole;
import org.onap.aaf.auth.helpers.Cred.Instance;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;

public class ExpiringNext extends Batch {
    
    public ExpiringNext(AuthzTrans trans) throws APIException, IOException, OrganizationException {
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

            UserRole.load(trans, session, UserRole.v2_0_11);
            Cred.load(trans, session);
        } finally {
            tt0.done();
        }
    }

    @Override
    protected void run(AuthzTrans trans) {
        GregorianCalendar gc = new GregorianCalendar();
        Date now = gc.getTime();
        gc.add(GregorianCalendar.WEEK_OF_MONTH, 2);
        Date twoWeeks = gc.getTime();
        // Set time way off
        gc.set(GregorianCalendar.YEAR, 3000);
        Date earliestUR = gc.getTime();
        Date earliestCred = gc.getTime();
        // Run for Roles
        List<String> expiring = new ArrayList<>();
        
        trans.info().log("Checking for Expired UserRoles");
        for(UserRole ur : UserRole.getData()) {
            if(ur.expires().after(now)) {
                if(ur.expires().before(twoWeeks)) {
                    expiring.add(Chrono.dateOnlyStamp(ur.expires()) + ":\t" + ur.user() + '\t' + ur.role());
                }
                if(ur.expires().before(earliestUR)) {
                    earliestUR = ur.expires();
                }
            }
        }

        if(expiring.size()>0) {
            Collections.sort(expiring,Collections.reverseOrder());
            for(String s : expiring) {
                System.err.print('\t');
                System.err.println(s);
            }
            trans.info().printf("Earliest Expiring UR is %s\n\n", Chrono.dateOnlyStamp(earliestUR));
        } else {
            trans.info().printf("No Expiring UserRoles within 2 weeks");
        }
        
        expiring.clear();
        
        trans.info().log("Checking for Expired Credentials");
        for( Cred creds : Cred.data.values()) {
            Instance lastInstance=null;
            for(Instance inst : creds.instances) {
                if(inst.type==CredDAO.BASIC_AUTH || inst.type==CredDAO.BASIC_AUTH_SHA256) {
                    if(lastInstance == null || inst.expires.after(lastInstance.expires)) {
                        lastInstance = inst;
                    }
                }
            }
            if(lastInstance!=null) {
                if(lastInstance.expires.after(now)) {
                    if(lastInstance.expires.before(twoWeeks)) {
                        expiring.add(Chrono.dateOnlyStamp(lastInstance.expires) + ": \t" + creds.id);
                    }
                }
                if(lastInstance.expires.before(earliestCred)) {
                    earliestCred = lastInstance.expires;
                }
            }
        }
        
        if(expiring.size()>0) {
            Collections.sort(expiring,Collections.reverseOrder());
            for(String s : expiring) {
                System.err.print('\t');
                System.err.println(s);
            }
            trans.info().printf("Earliest Expiring Cred is %s\n\n", Chrono.dateOnlyStamp(earliestCred));
        } else {
            trans.info().printf("No Expiring Creds within 2 weeks");
        }

    }
    
    @Override
    protected void _close(AuthzTrans trans) {
        session.close();
    }

}
