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

package org.onap.aaf.auth.batch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.OrganizationFactory;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.StaticSlot;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

public abstract class Batch {

    private static String rootNs;

    private static StaticSlot ssargs;

    protected static final String STARS = "*****";

    protected final Cluster cluster; 
    protected static AuthzEnv env;
    protected static Session session;
    protected static Set<String> specialNames;
    protected static boolean dryRun; 
    protected static String batchEnv;

    public static final String CASS_ENV = "CASS_ENV";
    public static final String LOG_DIR = "LOG_DIR";
    protected static final String PUNT="punt";
    protected static final String MAX_EMAILS="MAX_EMAILS";
    protected static final String VERSION="VERSION";
    public static final String GUI_URL="GUI_URL";
    
    protected final Organization org;


    
    protected Batch(AuthzEnv env) throws APIException, IOException, OrganizationException {
        // Be able to change Environments
        // load extra properties, i.e.
        // PERF.cassandra.clusters=....
        batchEnv = env.getProperty(CASS_ENV);
        if (batchEnv != null) {
            batchEnv = batchEnv.trim();
            env.info().log("Redirecting to ",batchEnv,"environment");
            String str;
            for (String key : new String[]{
                    CassAccess.CASSANDRA_CLUSTERS,
                    CassAccess.CASSANDRA_CLUSTERS_PORT,
                    CassAccess.CASSANDRA_CLUSTERS_USER_NAME,
                    CassAccess.CASSANDRA_CLUSTERS_PASSWORD,
                    VERSION,GUI_URL,PUNT,MAX_EMAILS,
                    LOG_DIR,
                    "SPECIAL_NAMES"
                    }) {
                if ((str = env.getProperty(batchEnv+'.'+key))!=null) {
                    env.setProperty(key, str);
                }
            }
        }

        // Setup for Dry Run
        cluster = CassAccess.cluster(env,batchEnv);
        env.info().log("cluster name - ",cluster.getClusterName());
        String dryRunStr = env.getProperty( "DRY_RUN" );
        if ( dryRunStr == null || "false".equals(dryRunStr.trim()) ) {
            dryRun = false;
        } else {
            dryRun = true;
            env.info().log("dryRun set to TRUE");
        }

        org = OrganizationFactory.init(env);
        org.setTestMode(dryRun);

        // Special names to allow behaviors beyond normal rules
        specialNames = new HashSet<>();
        String names = env.getProperty( "SPECIAL_NAMES" );
        if ( names != null )
        {
            env.info().log("Loading SPECIAL_NAMES");
            for (String s :names.split(",") )
            {
                env.info().log("\tspecial: " + s );
                specialNames.add( s.trim() );
            }
        }
    }

    protected abstract void run(AuthzTrans trans);
    protected abstract void _close(AuthzTrans trans);
    
    public String[] args() {
        return env.get(ssargs);
    }
    
    public boolean isDryRun()
    {
        return dryRun;
    }
    
    public boolean isSpecial(String user) {
        if (specialNames != null && specialNames.contains(user)) {
            env.info().log("specialName: " + user);

            return (true);
        } else {
            return (false);
        }
    }


    protected PrintStream fallout(PrintStream inFallout, String logType)
            throws IOException {
        PrintStream fallout = inFallout;
        if (fallout == null) {
            File dir = new File("logs");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File f = null;
            long uniq = System.currentTimeMillis();

            f = new File(dir, getClass().getSimpleName() + "_" + logType + "_"
                    + uniq + ".log");

            fallout = new PrintStream(new FileOutputStream(f, true));
        }
        return fallout;
    }

    public Organization getOrgFromID(AuthzTrans trans, String user) {
        Organization organization;
        try {
            organization = OrganizationFactory.obtain(trans.env(),user.toLowerCase());
        } catch (OrganizationException e1) {
            trans.error().log(e1);
            organization=null;
        }

        if (organization == null) {
            PrintStream fallout = null;

            try {
                fallout = fallout(fallout, "Fallout");
                fallout.print("INVALID_ID,");
                fallout.println(user);
            } catch (Exception e) {
                env.error().log("Could not write to Fallout File", e);
            }
            return (null);
        }

        return (organization);
    }
    
    public static Row executeDeleteQuery(Statement stmt) {
        Row row = null;
        if (!dryRun) {
            row = session.execute(stmt).one();
        }

        return (row);

    }
        
    public static int acquireRunLock(String className) {
        Boolean testEnv = true;
        String envStr = env.getProperty("AFT_ENVIRONMENT");

        if (envStr != null) {
            if ("AFTPRD".equals(envStr)) {
                testEnv = false;
            }
        } else {
            env.fatal()
                    .log("AFT_ENVIRONMENT property is required and was not found. Exiting.");
            System.exit(1);
        }

        if (testEnv) {
            env.info().log("TESTMODE: skipping RunLock");
            return (1);
        }

        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            env.warn().log("Unable to get hostname");
            return (0);
        }

        ResultSet existing = session.execute(String.format(
                "select * from authz.run_lock where class = '%s'", className));

        for (Row row : existing) {
            long curr = System.currentTimeMillis();
            ByteBuffer lastRun = row.getBytesUnsafe(2); // Can I get this field
                                                        // by name?

            long interval = (1 * 60 * 1000); // @@ Create a value in props file
                                                // for this
            long prev = lastRun.getLong();

            if ((curr - prev) <= interval) {
                env.warn().log(
                        String.format("Too soon! Last run was %d minutes ago.",
                                ((curr - prev) / 1000) / 60));
                env.warn().log(
                        String.format("Min time between runs is %d minutes ",
                                (interval / 1000) / 60));
                env.warn().log(
                        String.format("Last ran on machine: %s at %s",
                                row.getString("host"), row.getDate("start")));
                return (0);
            } else {
                env.info().log("Delete old lock");
                deleteLock(className);
            }
        }

        GregorianCalendar current = new GregorianCalendar();

        // We want our time in UTC, hence "+0000"
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+0000");
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));

        String cql = String
                .format("INSERT INTO authz.run_lock (class,host,start) VALUES ('%s','%s','%s') IF NOT EXISTS",
                        className, hostname, fmt.format(current.getTime()));

        env.info().log(cql);

        Row row = session.execute(cql).one();
        if (!row.getBool("[applied]")) {
            env.warn().log("Lightweight Transaction failed to write lock.");
            env.warn().log(
                    String.format("host with lock: %s, running at %s",
                            row.getString("host"), row.getDate("start")));
            return (0);
        }
        return (1);
    }
    
    private static void deleteLock( String className) {
        Row row = session.execute( String.format( "DELETE FROM authz.run_lock WHERE class = '%s' IF EXISTS", className ) ).one();
        if (! row.getBool("[applied]")) {
            env.info().log( "delete failed" );
        }
    }

    private static void transferVMProps(AuthzEnv env, String ... props) {
        String value;
        for (String key : props) {
            if ((value = System.getProperty(key))!=null) {
                env.setProperty(key, value);
            }
        }
    }
    
    // IMPORTANT! VALIDATE Organization isUser method
    protected void checkOrganizationAcccess(AuthzTrans trans, Question q) throws APIException, OrganizationException {
            Set<String> testUsers = new HashSet<>();
            Result<List<RoleDAO.Data>> rrd = q.roleDAO.readNS(trans, rootNs);
            if (rrd.isOK()) {
                for (RoleDAO.Data r : rrd.value) {
                    Result<List<UserRoleDAO.Data>> rur = q.userRoleDAO.readByRole(trans, r.fullName());
                    if (!rur.isOK()) {
                        continue;
                    }
                    for (UserRoleDAO.Data udd : rur.value) {
                        testUsers.add(udd.user);
                    }
                }
                if (testUsers.size() < 2) {
                    throw new APIException("Not enough Users in Roles for " + rootNs + " to Validate");
                }

                Identity iden;
                for (String user : testUsers) {
                    if ((iden = org.getIdentity(trans, user)) == null) {
                        throw new APIException("Failed Organization Entity Validation Check: " + user);
                    } else {
                        trans.info().log("Organization Validation Check: " + iden.id());
                    }
                }
            }
        }
    
    protected static String logDir() {
        String ld = env.getProperty(LOG_DIR);
        if (ld==null) {
            if (batchEnv==null) { // Deployed Batch doesn't use different ENVs, and a common logdir
                ld = "logs/";
            } else {
                ld = "logs/"+batchEnv;
            }
        }
        return ld;
    }
    protected int count(String str, char c) {
        if (str==null || str.isEmpty()) {
            return 0;
        } else {
            int count=1;
            for (int i=str.indexOf(c);i>=0;i=str.indexOf(c,i+1)) {
                ++count;
            }
            return count;
        }
    }

    public final void close(AuthzTrans trans) {
        _close(trans);
        cluster.close();
    }

    public static void main(String[] args) {
        PropAccess access = new PropAccess(args);
        InputStream is = null;
        String filename;
        String propLoc;
        try {
            Define.set(access);
            rootNs =Define.ROOT_NS();
            if(access.getProperty(Config.CADI_PROP_FILES)==null) {
	            File f = new File("authBatch.props");
	            try {
	                if (f.exists()) {
	                    filename = f.getAbsolutePath();
	                    is = new FileInputStream(f);
	                    propLoc = f.getPath();
	                } else {
	                    URL rsrc = ClassLoader.getSystemResource("authBatch.props");
	                    filename = rsrc.toString();
	                    is = rsrc.openStream();
	                    propLoc = rsrc.getPath();
	                }
	                access.load(is);
	            } finally {
	                if (is == null) {
	                    System.err.println("authBatch.props must exist in current dir, or in Classpath");
	                    System.exit(1);
	                }
	                is.close();
	            }
	            if (filename != null) {
	                access.log(Level.INFO,"Instantiated properties from", filename);
	            }

	            // Log where Config found
	            access.log(Level.INFO,"Configuring from", propLoc);

            }
            env = new AuthzEnv(access);

            transferVMProps(env, CASS_ENV, "DRY_RUN", "NS", "Organization");

            // Flow all Env Logs to Log4j, with ENV

//            LogFileNamer lfn;
//            lfn = new LogFileNamer(logDir(),"").noPID();
//            lfn.setAppender("authz-batch");
//            lfn.setAppender("aspr|ASPR");
//            lfn.setAppender("sync");
//            lfn.setAppender("jobchange");
//            lfn.setAppender("validateuser");
//            aspr = Logger.getLogger("aspr");
//            Log4JLogTarget.setLog4JEnv("authz-batch", env);
//            propLoc = null;

            Batch batch = null;
            // setup ATTUser and Organization Slots before starting this:
            // TODO redo this
            // env.slot(ATT.ATT_USERSLOT);
            //
            // OrganizationFactory.setDefaultOrg(env, ATT.class.getName());
            AuthzTrans trans = env.newTrans();

            TimeTaken tt = trans.start("Total Run", Env.SUB);
            try {
                int len = args.length;
                if (len > 0) {
                    String toolName = args[0];
                    len -= 1;
                    if (len < 0)
                        len = 0;
                    String nargs[] = new String[len];
                    if (len > 0) {
                        System.arraycopy(args, 1, nargs, 0, len);
                    }

                    env.put(ssargs = env.staticSlot("ARGS"), nargs);

                    /*
                     * Add New Batch Programs (inherit from Batch) here
                     */

                    // Might be a Report, Update or Temp Batch
                    Class<?> cls;
                    String classifier = "";
                    try {
                        cls = ClassLoader.getSystemClassLoader().loadClass("org.onap.aaf.auth.update." + toolName);
                        classifier = "Update:";
                    } catch (ClassNotFoundException e) {
                        try {
                            cls = ClassLoader.getSystemClassLoader().loadClass("org.onap.aaf.auth.reports." + toolName);
                            classifier = "Report:";
                        } catch (ClassNotFoundException e2) {
                            try {
                                cls = ClassLoader.getSystemClassLoader()
                                        .loadClass("org.onap.aaf.auth.temp." + toolName);
                                classifier = "Temp Utility:";
                            } catch (ClassNotFoundException e3) {
                                cls = null;
                            }
                        }
                    }
                    if (cls != null) {
                        Constructor<?> cnst = cls.getConstructor(new Class[] { AuthzTrans.class });
                        batch = (Batch) cnst.newInstance(trans);
                        env.info().log("Begin", classifier, toolName);
                    }
                

                    if (batch == null) {
                        trans.error().log("No Batch named", toolName, "found");
                    }
                    /*
                     * End New Batch Programs (inherit from Batch) here
                     */

                }
                if (batch != null) {
                    batch.run(trans);
                }
            } finally {
                tt.done();
                if (batch != null) {
                    batch.close(trans);
                }
                StringBuilder sb = new StringBuilder("Task Times\n");
                trans.auditTrail(4, sb, AuthzTrans.SUB, AuthzTrans.REMOTE);
                trans.info().log(sb);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            // Exceptions thrown by DB aren't stopping the whole process.
            System.exit(1);
        }
    }

}

