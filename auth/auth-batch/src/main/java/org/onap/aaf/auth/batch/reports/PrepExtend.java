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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.helpers.Cred;
import org.onap.aaf.auth.batch.helpers.Cred.Instance;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.cadi.util.CSV.Writer;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;

public class PrepExtend extends Batch {

    public static final String PREP_EXTEND = "PrepExtend";
    private static final String CSV = ".csv";
    private static final String INFO = "info";

    /**
     * Create a list of Creds and UserRoles to extend
     * Note: Certificates cannot be renewed in this way.
     * 
     * Arguments From (0 = today, -2 = 2 weeks back) and To (weeks from today)
     * 
     * @param trans
     * @throws APIException
     * @throws IOException
     * @throws OrganizationException
     */
    public PrepExtend(AuthzTrans trans) throws APIException, IOException, OrganizationException {
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
        } finally {
            tt0.done();
        }
    }

    @Override
    protected void run(AuthzTrans trans) {
        GregorianCalendar gc = new GregorianCalendar();
        Date now = gc.getTime();
        
        int ifrom = 0;
        int ito = 4;
        
        for(int i=0; i< args().length;++i) {
            switch(args()[i]) {
                case "-from":
                    if(args().length>i+1) {
                        ifrom = Integer.parseInt(args()[i++ +1]); 
                    }
                    break;
                case "-to":
                    if(args().length>i+1) {
                        ito = Integer.parseInt(args()[i++ +1]);
                    }
                    break;
            }
        }
        if(ifrom < -4) {
            System.err.println("Invalid -from param");
            return;
        }
        
        if(ito<=0 || ito>24 || ifrom>ito) {
            System.err.println("Invalid -to param");
            return;
        }
        
        // Make sure to is Zero based from today.
        if(ifrom<0) {
            ito+= ifrom*-1;
        }
        
        gc.add(GregorianCalendar.WEEK_OF_MONTH, ifrom);
        Date from = gc.getTime();
        
        gc.add(GregorianCalendar.WEEK_OF_MONTH, ito /* with From calculated in */);
        Date to = gc.getTime();
        
        try {
            File file = new File(logDir(), PREP_EXTEND + Chrono.dateOnlyStamp(now) + CSV);
            final CSV puntCSV = new CSV(env.access(),file);
            final Writer cw = puntCSV.writer();
            cw.row(INFO,PREP_EXTEND,Chrono.dateOnlyStamp(now),0);

            try {
                trans.info().log("Process UserRoles for Extending");
                /**
                   Run through User Roles.  
                   If match Date Range, write out to appropriate file.
                */
                UserRole.load(trans, session, UserRole.v2_0_11, ur -> {
                    if(from.before(ur.expires()) && to.after(ur.expires())) {
                        ur.row(cw,UserRole.UR);
                    }
                });
                
                trans.info().log("Process BasicAuth for Extending");
                TimeTaken tt0 = trans.start("Load Credentials", Env.REMOTE);
                try {
                    // Load only Valid Basic Auth
                    Cred.load(trans, session, CredDAO.BASIC_AUTH_SHA256);
                } finally {
                    tt0.done();
                }


                /**
                   Run through Creds.  
                   If match Date Range, write out to appropriate file.
                */
                Map<Integer,Instance> imap = new HashMap<>();
                Instance prev;
                for(Cred cred : Cred.data.values()) {
                    imap.clear();
                    for(Instance i : cred.instances) {
                        if(from.before(i.expires) && to.after(i.expires)) {
                            prev = imap.get(i.other);
                            // Only do LATEST instance of same cred (accounts for previously extended creds)
                            if(prev==null || prev.expires.before(i.expires)) {
                                imap.put(i.other,i);
                            }
                        }
                    };
                    for(Instance i: imap.values()) {
                        cred.row(cw,i);
                    }
                }
            } finally {
                cw.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void _close(AuthzTrans trans) {
        session.close();
    }


}
