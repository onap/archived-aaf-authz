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

package org.onap.aaf.auth.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.onap.aaf.auth.Batch;
import org.onap.aaf.auth.BatchPrincipal;
import org.onap.aaf.auth.actions.Action;
import org.onap.aaf.auth.actions.ActionDAO;
import org.onap.aaf.auth.actions.CacheTouch;
import org.onap.aaf.auth.actions.URDelete;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.helpers.UserRole;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.util.Split;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;

public class ExpiringP2 extends Batch {
    private final URDelete urDelete;
    private final CacheTouch cacheTouch;
    private final AuthzTrans noAvg;
    private final BufferedReader urDeleteF;

    public ExpiringP2(AuthzTrans trans) throws APIException, IOException, OrganizationException {
        super(trans.env());
        trans.info().log("Starting Connection Process");
        
        noAvg = env.newTransNoAvg();
        noAvg.setUser(new BatchPrincipal("batch:ExpiringP2"));

        TimeTaken tt0 = trans.start("Cassandra Initialization", Env.SUB);
        try {
                urDelete = new URDelete(trans, cluster,isDryRun());
                TimeTaken tt2 = trans.start("Connect to Cluster", Env.REMOTE);
                try {
                    session = urDelete.getSession(trans);
                } finally {
                    tt2.done();
                }
            cacheTouch = new CacheTouch(trans,urDelete);
            
            File data_dir = new File(env.getProperty("aaf_data_dir"));
            if (!data_dir.exists() || !data_dir.canWrite() || !data_dir.canRead()) {
                throw new IOException("Cannot read/write to Data Directory "+ data_dir.getCanonicalPath() + ": EXITING!!!");
            }
            urDeleteF = new BufferedReader(new FileReader(new File(data_dir,"UserRoleDeletes.dat")));
            
        } finally {
            tt0.done();
        }
    }

    @Override
    protected void run(AuthzTrans trans) {
        deleteURs(noAvg, urDeleteF, urDelete, cacheTouch);
    }
    
    public static void deleteURs(AuthzTrans trans, BufferedReader urDeleteF, URDelete urDelete, CacheTouch cacheTouch) {
        String line,prev="";
        try {
            UserRole ur;
            Map<String,Count> tally = new HashMap<>();
            int count=0;
            try {
                while ((line=urDeleteF.readLine())!=null) {
                    if (line.startsWith("#")) {
                        Count cnt = tally.get(line);
                        if (cnt==null) {
                            tally.put(line, cnt=new Count());
                        }
                        cnt.inc();
                        prev = line;
                    } else {
                        String[] l = Split.splitTrim('|', line);
                        try {
                            // Note: following default order from "COPY TO"
                            ur = new UserRole(l[0],l[1],l[3],l[4],Chrono.iso8601Fmt.parse(l[2]));
                            urDelete.exec(trans, ur, prev);
                            ++count;
                        } catch (ParseException e) {
                            trans.error().log(e);
                        }
                    }
                }
                
                System.out.println("Tallies of UserRole Deletions");
                for (Entry<String, Count> es : tally.entrySet()) {
                    System.out.printf("  %6d\t%20s\n", es.getValue().cnt,es.getKey());
                }
            } finally {
                if (cacheTouch!=null && count>0) {
                        cacheTouch.exec(trans, "user_roles", "Removing UserRoles");
                }
            }
        } catch (IOException e) {
            trans.error().log(e);
        }
        
    }
    private static class Count {
        private int cnt=0;
        
        public /*synchonized*/ void inc() {
            ++cnt;
        }
        
        public String toString() {
            return Integer.toString(cnt);
        }
    }
    
    @Override
    protected void _close(AuthzTrans trans) {
        trans.info().log("End",this.getClass().getSimpleName(),"processing" );
        for (Action<?,?,?> action : new Action<?,?,?>[] {urDelete,cacheTouch}) {
                if (action instanceof ActionDAO) {
                    ((ActionDAO<?,?,?>)action).close(trans);
                }
        }
        session.close();
        try {
            urDeleteF.close();
        } catch (IOException e) {
            trans.error().log(e);
        }
    }

}
