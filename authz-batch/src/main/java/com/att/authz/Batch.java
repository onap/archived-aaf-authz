/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz;

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
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import com.att.authz.org.Organization;
import com.att.authz.org.OrganizationException;
import com.att.authz.org.OrganizationFactory;
import com.att.dao.CassAccess;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.StaticSlot;
import com.att.inno.env.TimeTaken;
import com.att.inno.env.impl.Log4JLogTarget;
import com.att.inno.env.log4j.LogFileNamer;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

public abstract class Batch {
	private static StaticSlot ssargs;

	protected static final String STARS = "*****";

    protected final Cluster cluster; 
    protected static AuthzEnv env;
    protected static Session session;
    protected static Logger aspr;
    private static Set<String> specialNames = null;
    protected static boolean dryRun; 
	protected static String batchEnv;

	public static final String CASS_ENV = "CASS_ENV";
    protected final static String PUNT="punt";
    protected final static String VERSION="VERSION";
    public final static String GUI_URL="GUI_URL";
    
    protected final static String ORA_URL="ora_url";
    protected final static String ORA_PASSWORD="ora_password";


    
    protected Batch(AuthzEnv env) throws APIException, IOException {
    	// TODO  - Property Driven Organization
//    	try {
//			// att = new ATT(env);
//		} catch (OrganizationException e) {
//			throw new APIException(e);
//		}

    	// Be able to change Environments
    	// load extra properties, i.e.
    	// PERF.cassandra.clusters=....
    	batchEnv = env.getProperty(CASS_ENV);
    	if(batchEnv != null) {
    		batchEnv = batchEnv.trim();
    		env.info().log("Redirecting to ",batchEnv,"environment");
    		String str;
    		for(String key : new String[]{
    				CassAccess.CASSANDRA_CLUSTERS,
    				CassAccess.CASSANDRA_CLUSTERS_PORT,
    				CassAccess.CASSANDRA_CLUSTERS_USER_NAME,
    				CassAccess.CASSANDRA_CLUSTERS_PASSWORD,
    				VERSION,GUI_URL,PUNT,
    				// TEMP
    				ORA_URL, ORA_PASSWORD
    				}) {
    			if((str = env.getProperty(batchEnv+'.'+key))!=null) {
    			    env.setProperty(key, str);
    			}
    		}
    	}

    	// Setup for Dry Run
        cluster = CassAccess.cluster(env,batchEnv);
        env.info().log("cluster name - ",cluster.getClusterName());
        String dryRunStr = env.getProperty( "DRY_RUN" );
        if ( dryRunStr == null || dryRunStr.equals("false") ) {
		    dryRun = false;
		} else {
            dryRun = true;
            env.info().log("dryRun set to TRUE");
        }

        // Special names to allow behaviors beyond normal rules
        String names = env.getProperty( "SPECIAL_NAMES" );
        if ( names != null )
        {
            env.info().log("Loading SPECIAL_NAMES");
            specialNames = new HashSet<String>();
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
    	return (String[])env.get(ssargs);
    }
	
    public boolean isDryRun()
    {
        return( dryRun );
    }
    
	public boolean isSpecial(String user) {
		if (specialNames != null && specialNames.contains(user)) {
			env.info().log("specialName: " + user);

			return (true);
		} else {
			return (false);
		}
	}
	
	public boolean isMechID(String user) {
		if (user.matches("m[0-9][0-9][0-9][0-9][0-9]")) {
			return (true);
		} else {
			return (false);
		}
	}

	protected PrintStream fallout(PrintStream _fallout, String logType)
			throws IOException {
		PrintStream fallout = _fallout;
		if (fallout == null) {
			File dir = new File("logs");
			if (!dir.exists()) {
				dir.mkdirs();
			}

			File f = null;
			// String os = System.getProperty("os.name").toLowerCase();
			long uniq = System.currentTimeMillis();

			f = new File(dir, getClass().getSimpleName() + "_" + logType + "_"
					+ uniq + ".log");

			fallout = new PrintStream(new FileOutputStream(f, true));
		}
		return fallout;
	}

	public Organization getOrgFromID(AuthzTrans trans, String user) {
		Organization org;
		try {
			org = OrganizationFactory.obtain(trans.env(),user.toLowerCase());
		} catch (OrganizationException e1) {
			trans.error().log(e1);
			org=null;
		}

		if (org == null) {
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

		return (org);
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
			if (envStr.equals("AFTPRD")) {
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
		for(String key : props) {
			if((value = System.getProperty(key))!=null) {
			    env.setProperty(key, value);
			}
		}
		
	}
	
	protected int count(String str, char c) {
		int count=str==null||str.isEmpty()?0:1;
		for(int i=str.indexOf(c);i>=0;i=str.indexOf(c,i+1)) {
			++count;
		}
		return count;
	}

	public final void close(AuthzTrans trans) {
	    _close(trans);
	    cluster.close();
	}

	public static void main(String[] args) {
	        Properties props = new Properties();
	        InputStream is=null;
	        String filename;
	        String propLoc;
	        try {
	            File f = new File("etc/authBatch.props");
	            try {
	                if(f.exists()) {
	                	filename = f.getCanonicalPath();
	                    is = new FileInputStream(f);
	                    propLoc=f.getPath();
	                } else {
	                    URL rsrc = ClassLoader.getSystemResource("authBatch.props");
	                    filename = rsrc.toString();
	                    is = rsrc.openStream();
	                    propLoc=rsrc.getPath();
	                }
	                props.load(is);
	            } finally {
	                if(is==null) {
	                    System.err.println("authBatch.props must exist in etc dir, or in Classpath");
	                    System.exit(1);
	                }
	                is.close();
	            }
		
	            env = new AuthzEnv(props);
	            
	            transferVMProps(env,CASS_ENV,"DRY_RUN","NS","Organization");
				
	            // Flow all Env Logs to Log4j, with ENV
	            
	        	LogFileNamer lfn;
	        	if((batchEnv=env.getProperty(CASS_ENV))==null) {
	        		lfn = new LogFileNamer("logs/").noPID();
	        	} else {
	        		lfn = new LogFileNamer("logs/" + batchEnv+'/').noPID();
	        	}
	        	
	        	lfn.setAppender("authz-batch");
	        	lfn.setAppender("aspr|ASPR");
	        	lfn.setAppender("sync");
	        	lfn.setAppender("jobchange");
	        	lfn.setAppender("validateuser");
	    		aspr = Logger.getLogger("aspr");
	            Log4JLogTarget.setLog4JEnv("authz-batch", env);
	            if(filename!=null) {
	            	env.init().log("Instantiated properties from",filename);
	            }
	
				
	            // Log where Config found
	            env.info().log("Configuring from",propLoc);
	            propLoc=null;
		
	            Batch batch = null;
	            // setup ATTUser and Organization Slots before starting this:
	            //TODO Property Driven Organization
//	            env.slot(ATT.ATT_USERSLOT);
//	            OrganizationFactory.setDefaultOrg(env, ATT.class.getName());
	            AuthzTrans trans = env.newTrans();
	            
	            TimeTaken tt = trans.start("Total Run", Env.SUB);
	            try {
	            	int len = args.length;
	                if(len>0) {
	                	String toolName = args[0];
	                	len-=1;
	                	if(len<0)len=0;
	                	String nargs[] = new String[len];
	                	if(len>0) {
	                		System.arraycopy(args, 1, nargs, 0, len);
	                	}
	                	
	                	env.put(ssargs=env.staticSlot("ARGS"), nargs);
	                	
	                    /*
	                     * Add New Batch Programs (inherit from Batch) here
	                     */
	
	                    if( JobChange.class.getSimpleName().equals(toolName)) {
	                        aspr.info( "Begin jobchange processing" );
	                        batch = new JobChange(trans);
	                    }
	////                    else if( ValidateUsers.class.getSimpleName().equals(toolName)) {
	////                        aspr.info( "Begin ValidateUsers processing" );
	////                        batch = new ValidateUsers(trans);
	//                    }
	                    else if( UserRoleDataGeneration.class.getSimpleName().equals(toolName)) {
	                    	// This job duplicates User Role add/delete History items 
	                    	// so that we can search them by Role. Intended as a one-time
	                    	// script! but written as batch job because Java has better
	                    	// UUID support. Multiple runs will generate multiple copies of 
	                    	// these history elements!
	                    	aspr.info( "Begin User Role Data Generation Processing ");
	                    	batch = new UserRoleDataGeneration(trans);
	                    } else {  // Might be a Report, Update or Temp Batch
	                    	Class<?> cls;
	                    	String classifier = "";
	                    	try {
	                    		cls = ClassLoader.getSystemClassLoader().loadClass("com.att.authz.update."+toolName);
	                    		classifier = "Update:";
	                    	} catch(ClassNotFoundException e) {
	                    		try {
	                    			cls = ClassLoader.getSystemClassLoader().loadClass("com.att.authz.reports."+toolName);
	                        		classifier = "Report:";
	                    		} catch (ClassNotFoundException e2) {
	                        		try {
	                        			cls = ClassLoader.getSystemClassLoader().loadClass("com.att.authz.temp."+toolName);
	                            		classifier = "Temp Utility:";
	                        		} catch (ClassNotFoundException e3) {
	                        			cls = null;
	                        		}
	                    		}
	                    	}
	                    	if(cls!=null) {
	                    		Constructor<?> cnst = cls.getConstructor(new Class[]{AuthzTrans.class});
	                    		batch = (Batch)cnst.newInstance(trans);
		                    	env.info().log("Begin",classifier,toolName);
	                    	}
	                    }
	
	                    if(batch==null) {
	                        trans.error().log("No Batch named",toolName,"found");
	                    }
	                    /*
	                     * End New Batch Programs (inherit from Batch) here
	                     */
	
	                } 
	                if(batch!=null) {
	                    batch.run(trans);
	                }
	            } finally {
	            	tt.done();
	                if(batch!=null) {
	                    batch.close(trans);
	                }
	                StringBuilder sb = new StringBuilder("Task Times\n");
	                trans.auditTrail(4, sb, AuthzTrans.REMOTE);
	                trans.info().log(sb);
	            }
	        } catch (Exception e) {
	            e.printStackTrace(System.err);
	            // Exceptions thrown by DB aren't stopping the whole process.
	            System.exit(1);
	        }
	    }


}

