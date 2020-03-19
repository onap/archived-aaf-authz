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
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.log4j.Log4JAccessAppender;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.OrganizationFactory;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;
import org.onap.aaf.misc.env.util.Split;
import org.onap.aaf.misc.env.util.StringBuilderOutputStream;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

public abstract class Batch {
    protected static final String STARS = "*****";

    protected static Cluster cluster;
    protected static AuthzEnv env;
    protected static Session session;
    protected static Set<String> specialNames;
    protected static List<String> specialDomains;
    protected static boolean dryRun;
    protected static String batchEnv;

    private static File logdir;

	private static String[] batchArgs;

    public static final String CASS_ENV = "CASS_ENV";
    public static final String LOG_DIR = "LOG_DIR";
    protected static final String MAX_EMAILS="MAX_EMAILS";
    protected static final String VERSION="VERSION";
    public static final String GUI_URL="GUI_URL";

    protected final Organization org;
    protected String version;
    protected static final Date now = new Date();
    protected static final Date never = new Date(0);

    protected Batch(AuthzEnv env) throws APIException, IOException, OrganizationException {
        if (batchEnv != null) {
            env.info().log("Redirecting to ",batchEnv,"environment");
            String str;
            for (String key : new String[]{
                    CassAccess.CASSANDRA_CLUSTERS,
                    CassAccess.CASSANDRA_CLUSTERS_PORT,
                    CassAccess.CASSANDRA_CLUSTERS_USER_NAME,
                    CassAccess.CASSANDRA_CLUSTERS_PASSWORD,
                    VERSION,GUI_URL,MAX_EMAILS,
                    LOG_DIR,
                    "SPECIAL_NAMES",
                    "MAIL_TEST_TO"
                    }) {
                if ((str = env.getProperty(batchEnv+'.'+key))!=null) {
                    env.setProperty(key, str);
                }
            }
        }

        // Setup for Dry Run
        if(cluster==null) {
            cluster = CassAccess.cluster(env,batchEnv);
        }
        env.info().log("cluster name - ",cluster.getClusterName());
        String dryRunStr = env.getProperty( "DRY_RUN" );
        if ( dryRunStr == null || "false".equals(dryRunStr.trim()) ) {
            dryRun = false;
        } else {
            dryRun = true;
            env.info().log("dryRun set to TRUE");
        }

        org = OrganizationFactory.init(env);
        if(org==null) {
            throw new OrganizationException("Organization MUST be defined for Batch");
        }
        org.setTestMode(dryRun);

        // Special names to allow behaviors beyond normal rules
        specialNames = new HashSet<>();
        specialDomains = new ArrayList<>();
        String names = env.getProperty( "SPECIAL_NAMES" );
        if ( names != null )
        {
            env.info().log("Loading SPECIAL_NAMES");
            for (String s :names.split(",") ) {
                env.info().log("\tspecial: " + s );
                if(s.indexOf('@')>0) {
                    specialNames.add( s.trim() );
                } else {
                    specialDomains.add(s.trim());
                }
            }
        }

        version = env.getProperty(VERSION,Config.AAF_DEFAULT_API_VERSION);
    }

    protected abstract void run(AuthzTrans trans);
    protected void _close(AuthzTrans trans) {}

    public String[] args() {
        return batchArgs;
    }

    public boolean isDryRun()
    {
        return dryRun;
    }

    public boolean isSpecial(String user) {
        if(user==null) {
            return false;
        }
        if (specialNames != null && specialNames.contains(user)) {
            env.info().log("specialName: " + user);
            return (true);
        } else {
            if(specialDomains!=null) {
                for(String sd : specialDomains) {
                    if(user.endsWith(sd)) {
                        env.info().log("specialDomain: " + user + " matches " + sd);
                        return (true);
                    }
                }
            }
        }
        return (false);
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
            env.warn().log("Unable to get hostname : "+e.getMessage());
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

    protected static File logDir() {
        if(logdir == null) {
            String ld = env.getProperty(LOG_DIR);
            if (ld==null) {
                if (batchEnv==null) { // Deployed Batch doesn't use different ENVs, and a common logdir
                    ld = "logs/";
                } else {
                    ld = "logs/"+batchEnv;
                }
            }
            logdir = new File(ld);
            if(!logdir.exists()) {
                logdir.mkdirs();
            }
        }
        return logdir;
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
        if(session!=null) {
            session.close();
            session = null;
        }
        if(cluster!=null && !cluster.isClosed()) {
            cluster.close();
        }
    }

    public static void main(String[] args) {
        // Use a StringBuilder to save off logs until a File can be setup
        StringBuilderOutputStream sbos = new StringBuilderOutputStream();
        PropAccess access = new PropAccess(new PrintStream(sbos),args);
        access.log(Level.INFO, "------- Starting Batch ------\n  Args: ");
        for(String s: args) {
            sbos.getBuffer().append(s);
            sbos.getBuffer().append(' ');
        }
        sbos.getBuffer().append('\n');

        InputStream is = null;
        String filename;
        String propLoc;
        try {
            Define.set(access);

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

            // Be able to change Environments
            // load extra properties, i.e.
            // PERF.cassandra.clusters=....
            batchEnv = env.getProperty(CASS_ENV);
            if(batchEnv!=null) {
                batchEnv = batchEnv.trim();
            }

            File logFile = new File(logDir() + "/batch" + Chrono.dateOnlyStamp(new Date()) + ".log" );
            PrintStream batchLog = new PrintStream(new FileOutputStream(logFile,true));
            try {
                access.setStreamLogIt(batchLog);
                sbos.flush();
                batchLog.print(sbos.getBuffer());
                sbos = null;
                Logger.getRootLogger().addAppender(new Log4JAccessAppender(access));

                Batch batch = null;
                AuthzTrans trans = env.newTrans();

                TimeTaken tt = trans.start("Total Run", Env.SUB);
                try {
                    int len = args.length;
                    if (len > 0) {
                        String toolName = args[0];
                        len -= 1;
                        if (len < 0)
                            len = 0;
                        batchArgs = new String[len];
                        if (len > 0) {
                            System.arraycopy(args, 1, batchArgs, 0, len);
                        }
                        /*
                         * Add New Batch Programs (inherit from Batch) here
                         */

                        // Might be a Report, Update or Temp Batch
                        Class<?> cls = null;
                        String classifier = "";

                        String[] pkgs = new String[] {
                                "org.onap.aaf.auth.batch.update",
                                "org.onap.aaf.auth.batch.reports",
                                "org.onap.aaf.auth.batch.temp"
                                };

                        String ebp = env.getProperty("EXTRA_BATCH_PKGS");
                        if(ebp!=null) {
                            String[] ebps = Split.splitTrim(':', ebp);
                            String[] temp = new String[ebps.length + pkgs.length];
                            System.arraycopy(pkgs,0, temp, 0, pkgs.length);
                            System.arraycopy(ebps,0,temp,pkgs.length,ebps.length);
                            pkgs = temp;
                        }

                        for(String p : pkgs) {
                            try {
                                cls = ClassLoader.getSystemClassLoader().loadClass(p + '.' + toolName);
                                int lastDot = p.lastIndexOf('.');
                                if(p.length()>0 || p.length()!=lastDot) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(Character.toUpperCase(p.charAt(++lastDot)));
                                    while(++lastDot<p.length()) {
                                        sb.append(p.charAt(lastDot));
                                    }
                                    sb.append(':');
                                    classifier = sb.toString();
                                    break;
                                }
                            } catch (ClassNotFoundException e) {
                                cls = null;
                            }
                        }
                        if (cls != null) {
                            Constructor<?> cnst = cls.getConstructor(AuthzTrans.class);
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
                        try {
                            batch.run(trans);
                        } catch (Exception e) {
                        	trans.error().log(e);
                            if(cluster!=null && !cluster.isClosed()) {
                                cluster.close();
                            }
                            trans.error().log(e);
                        }
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
            	env.warn().log(e);
            } finally {
                batchLog.close();
            }

        } catch (Exception e) {
            if(cluster!=null && !cluster.isClosed()) {
                cluster.close();
            }
            env.warn().log(System.err);
        }
    }

}

