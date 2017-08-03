/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
// test for case where I'm an admin

package com.att.authz;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.att.authz.env.AuthzTrans;
import com.att.authz.org.Organization;
import com.att.authz.org.OrganizationFactory;
import com.att.inno.env.APIException;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class JobChange extends Batch
{
    private class UserRole
    {
        String user;
        String role;
    }
    private class UserCred
    {
        String user;
        String ns;
    }
    
    private class NamespaceOwner
    {
        String user;
        String ns;
        boolean responsible;
        int ownerCount;
    }
    

    private AuthzTrans myTrans;

	private Map<String, ArrayList<UserRole>> rolesMap = new HashMap<String, ArrayList<UserRole>>();
	private Map<String, ArrayList<NamespaceOwner>> ownersMap = new HashMap<String, ArrayList<NamespaceOwner>>();
    private Map<String, ArrayList<UserCred>> credsMap = new HashMap<String, ArrayList<UserCred>>();
    
    
    public static void createDirectory( String dir )
    {
        File f = new File( dir );

        if ( ! f.exists())
        {
            env.info().log( "creating directory: " + dir );
            boolean result = false;

            try
            {
                f.mkdir();
                result = true;
            } catch(SecurityException e){
                e.printStackTrace();
            }        
            if(result) {    
                System.out.println("DIR created");  
            }
        }        
    }
    
    public static String getJobChangeDataFile()
    {
        File outFile = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        String line;
        boolean errorFlag = false;

        try
        {
            createDirectory( "etc" );
            
            outFile = new File("etc/jobchange." + getCurrentDate() );
            if (!outFile.exists())
            {
                outFile.createNewFile();
            }
            else
            {
                return( "etc/jobchange." + getCurrentDate() );
            }
			
            env.info().log("Creating the local file with the webphone data");



            writer = new BufferedWriter(new FileWriter(
                                            outFile.getAbsoluteFile()));

            URL u = new URL(  "ftp://thprod37.sbc.com/jobchange_Delta.dat" );
            reader = new BufferedReader(new InputStreamReader(
                                            new BufferedInputStream(u.openStream())));
            while ((line = reader.readLine()) != null) {
                writer.write(line + "\n");
            }
			
            writer.close();
            reader.close();
            
            env.info().log("Finished fetching the data from the webphone ftp site.");
            return( "etc/jobchange." + getCurrentDate() );
            
        } catch (MalformedURLException e) {
            env.error().log("Could not open the remote job change data file.", e);
            errorFlag = true;

        } catch (IOException e) {
            env.error().log(
                "Error while opening or writing to the local data file.", e);
            errorFlag = true;

        } catch (Exception e) {
            env.error().log("Error while fetching the data file.", e);
            errorFlag = true;

        } finally {
            if (errorFlag)
                outFile.delete();
        }
		return null;
    }

    public static String getCurrentDate()
    {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public void loadUsersFromCred()
    {
        String query = "select id,ns from authz.cred" ;
                                      
        env.info().log( "query: " + query );

        Statement stmt = new SimpleStatement( query );
        ResultSet results = session.execute(stmt);

        Iterator<Row> iter = results.iterator();
        while( iter.hasNext() )
        {
            Row row = iter.next();
            String user = row.getString( "id" );
            String ns = row.getString( "ns" );
            String simpleUser = user.substring( 0, user.indexOf( "@" ) );

            if ( isMechID( simpleUser ) )
            {
                continue;
            }
            else if ( credsMap.get( simpleUser ) == null )
            {
                credsMap.put( simpleUser, new ArrayList<UserCred>() );
                
                UserCred newEntry = new UserCred();
                newEntry.user = user;
                newEntry.ns = ns;
            
                credsMap.get( simpleUser ).add( newEntry );
            }
            else 
            {
                UserCred newEntry = new UserCred();
                newEntry.user = user;
                newEntry.ns = ns;
            
                credsMap.get( simpleUser ).add( newEntry );
            }
                
            env.debug().log( String.format( "\tUser: %s NS: %s", user, ns ) );
        }
    }

    public void loadUsersFromRoles()
    {
        String query = "select user,role from authz.user_role" ;
                                      
        env.info().log( "query: " + query );

        Statement stmt = new SimpleStatement( query );
        ResultSet results = session.execute(stmt);
        int total=0, flagged=0;

        Iterator<Row> iter = results.iterator();
        while( iter.hasNext() )
        {
            Row row = iter.next();
            String user = row.getString( "user" );
            String role = row.getString( "role" );
            String simpleUser = user.substring( 0, user.indexOf( "@" ) );

            if ( isMechID( simpleUser ) )
            {
                continue;
            }
            else if ( rolesMap.get( simpleUser ) == null )
            {
                rolesMap.put( simpleUser, new ArrayList<UserRole>() );
                
                UserRole newEntry = new UserRole();
                newEntry.user = user;
                newEntry.role = role;
            
                rolesMap.get( simpleUser ).add( newEntry );
            }
            else
            {
                UserRole newEntry = new UserRole();
                newEntry.user = user;
                newEntry.role = role;
            
                rolesMap.get( simpleUser ).add( newEntry );
            }
                
            env.debug().log( String.format( "\tUser: %s Role: %s", user, role ) );

            ++total;
        }
        env.info().log( String.format( "rows read: %d expiring: %d", total, flagged ) );
    }

    public void loadOwnersFromNS()
    {
        String query = "select name,admin,responsible from authz.ns" ;
                                      
        env.info().log( "query: " + query );

        Statement stmt = new SimpleStatement( query );
        ResultSet results = session.execute(stmt);

        Iterator<Row> iter = results.iterator();
        while( iter.hasNext() )
        {
            Row row = iter.next();
            Set<String> responsibles = row.getSet( "responsible", String.class );

            for ( String user : responsibles )
            {
                env.info().log( String.format( "Found responsible %s", user ) );
                String simpleUser = user.substring( 0, user.indexOf( "@" ) );

                if ( isMechID( simpleUser ) )
                {
                    continue;
                }
                else if ( ownersMap.get( simpleUser ) == null )
                {
                    ownersMap.put( simpleUser, new ArrayList<NamespaceOwner>() );

                    NamespaceOwner newEntry = new NamespaceOwner();
                    newEntry.user = user;
                    newEntry.ns   = row.getString( "name" );
                    newEntry.ownerCount = responsibles.size();
                    newEntry.responsible = true;
                    ownersMap.get( simpleUser ).add( newEntry );
                }
                else 
                {
                    NamespaceOwner newEntry = new NamespaceOwner();
                    newEntry.user = user;
                    newEntry.ns = row.getString( "name" );
                    newEntry.ownerCount = responsibles.size();
                    newEntry.responsible = true;                    
                    ownersMap.get( simpleUser ).add( newEntry );
                }
            }                
            Set<String> admins = row.getSet( "admin", String.class );

            for ( String user : admins )
            {
                env.info().log( String.format( "Found admin %s", user ) );
                String simpleUser = user.substring( 0, user.indexOf( "@" ) );

                if ( isMechID( simpleUser ) )
                {
                    continue;
                }
                else if ( ownersMap.get( simpleUser ) == null )
                {
                    ownersMap.put( simpleUser, new ArrayList<NamespaceOwner>() );

                    NamespaceOwner newEntry = new NamespaceOwner();
                    newEntry.user = user;
                    newEntry.ns   = row.getString( "name" );
                    newEntry.responsible = false;
                    newEntry.ownerCount = -1; //                     
                    ownersMap.get( simpleUser ).add( newEntry );
                }
                else 
                {
                    NamespaceOwner newEntry = new NamespaceOwner();
                    newEntry.user = user;
                    newEntry.ns = row.getString( "name" );
                    newEntry.responsible = false;
                    newEntry.ownerCount = -1; //                                         
                    ownersMap.get( simpleUser ).add( newEntry );
                }
            }                

        }
    }

	/**
	 * Processes the specified JobChange data file obtained from Webphone. Each line is 
	 * read and processed and any fallout is written to the specified fallout file. 
	 * If fallout file already exists it is deleted and a new one is created. A
	 * comparison of the supervisor id in the job data file is done against the one returned 
	 * by the authz service and if the supervisor Id has changed then the record is updated
	 * using the authz service. An email is sent to the new supervisor to approve the roles 
	 * assigned to the user.
	 * 
	 * @param fileName - name of the file to process including its path
	 * @param falloutFileName - the file where the fallout entries have to be written
	 * @param validDate - the valid effective date when the user had moved to the new supervisor
	 * @throws Exception
	 */
	public void processJobChangeDataFile(String fileName,
                                         String falloutFileName, Date validDate) throws Exception
    {
        
		BufferedWriter writer = null;

		try {

            env.info().log("Reading file: " + fileName );

            FileInputStream fstream = new FileInputStream(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String strLine;

            while ((strLine = br.readLine()) != null)   {
                processLine( strLine, writer );
            }

            br.close();
			
			
		} catch (IOException e) {
            env.error().log( "Error while reading from the input data file: " + e );
			throw e;
        }
    }

    public void handleAdminChange( String user )
    {
        ArrayList<NamespaceOwner> val = ownersMap.get( user );
        
        for ( NamespaceOwner r : val )
        {
            env.info().log( "handleAdminChange: " + user );
            AuthzTrans trans = env.newTransNoAvg();

            
            if ( r.responsible )
            {
                env.info().log( String.format( "delete from NS owner: %s, NS: %s, count: %s",
                                           r.user, r.ns, r.ownerCount ) );

                aspr.info( String.format( "action=DELETE_NS_OWNER, user=%s, ns=%s",
                                      r.user, r.ns ) );
                if ( r.ownerCount < 2 )
                {
                    // send warning email to aaf-support, after this deletion, no owner for NS
                    ArrayList<String> toAddress = new ArrayList<String>();
                    toAddress.add( "XXX_EMAIL" );
                
                    env.warn().log( "removing last owner from namespace" );

                    Organization org = null;
                    org = getOrgFromID( myTrans, org, toAddress.get(0) );

                    env.info().log( "calling getOrgFromID with " + toAddress.get(0) );

                    if ( org != null )
                    {
                        try
                        {
                            aspr.info( String.format( "action=EMAIL_NO_OWNER_NS to=%s, user=%s, ns=%s",
                                                      toAddress.get(0), r.user, r.ns ) );
                            org.sendEmail( trans, toAddress,
                                           new ArrayList<String>(),
                                           String.format( "WARNING: no owners for AAF namespace '%s'", r.ns ), // subject:
                                           String.format( "AAF recieved a jobchange notification for user %s who was the owner of the '%s' namespace. Please identify a new owner for this namespace and update AAF.", r.user, r.ns ), // body of msg
                                           true );
                        } catch (Exception e) {
                            env.error().log("calling sendEmail()");
                        
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        env.error().log( "Failed getOrgFromID" );
                    }
                }
            }
            else
            {
                env.info().log( String.format( "delete from NS admin: %s, NS: %s",
                                           r.user, r.ns ) );

                aspr.info( String.format( "action=DELETE_NS_ADMIN, user=%s, ns=%s",
                                          r.user, r.ns ) );
            }                    
            
            String field = (r.responsible == true) ? "responsible" : "admin";
            
            String query = String.format( "update authz.ns set %s = %s - {'%s'} where name = '%s'",
                                          field, field, r.user, r.ns ) ;                                   
            env.info().log( "query: " + query );
            Statement stmt = new SimpleStatement( query );
            /*Row row = */session.execute(stmt).one();
            
            String attribQuery = String.format( "delete from authz.ns_attrib where ns = '%s' AND type='%s' AND name='%s'",
        			r.ns, field, r.user);
            env.info().log( "ns_attrib query: " + attribQuery);
            Statement attribStmt = new SimpleStatement( attribQuery );
            /*Row attribRow = */session.execute(attribStmt).one();
            
        }
    }

    public void handleRoleChange( String user )
    {
        ArrayList<UserRole> val = rolesMap.get( user );
        
        for ( UserRole r : val )
        {
            env.info().log( "handleRoleChange: " + user );

            env.info().log( String.format( "delete from %s from user_role: %s",
                                           r.user, r.role ) );

            aspr.info( String.format( "action=DELETE_FROM_ROLE, user=%s, role=%s",
                                      r.user, r.role ) );


            String query = String.format( "delete from authz.user_role where user = '%s' and role = '%s'",
                                          r.user, r.role );
                                      
            env.info().log( "query: " + query );

            Statement stmt = new SimpleStatement( query );
            /* Row row = */ session.execute(stmt).one();

        }
    }
    
    public void handleCredChange( String user )
    {
        ArrayList<UserCred> val = credsMap.get( user );
        
        for ( UserCred r : val )
        {
            env.info().log( "handleCredChange: " + user );

            env.info().log( String.format( "delete user %s cred from ns: %s",
                                           r.user, r.ns ) );

            aspr.info( String.format( "action=DELETE_FROM_CRED, user=%s, ns=%s",
                                      r.user, r.ns ) );

            String query = String.format( "delete from authz.cred where id = '%s'",
                                          r.user );
                                      
            env.info().log( "query: " + query );

            Statement stmt = new SimpleStatement( query );
            /*Row row = */session.execute(stmt).one();

        }

    }
    
    public boolean processLine(String line, BufferedWriter writer) throws IOException
    {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");
        boolean errorFlag = false;
        String errorMsg = "";

        try
        {
            String[] phoneInfo = line.split( "\\|" );

            if ((phoneInfo != null) && (phoneInfo.length >= 8)
                && (!phoneInfo[0].startsWith("#")))
            {
                String user = phoneInfo[0];
                String newSupervisor = phoneInfo[7];
                Date effectiveDate = sdfDate.parse(phoneInfo[8].trim());

                env.debug().log( String.format( "checking user: %s, newSupervisor: %s, date: %s",
                                                user, newSupervisor, effectiveDate ) );
                    
                // Most important case, user is owner of a namespace
                //
                if ( ownersMap.get( user ) != null )
                {
                    env.info().log( String.format( "Found %s as a namespace admin/owner", user ) );
                    handleAdminChange( user );
                }

                if ( credsMap.get( user ) != null )
                {
                    env.info().log( String.format( "Found %s in cred table", user ) );
                    handleCredChange( user );
                }

                if ( rolesMap.get( user ) != null )
                {
                    env.info().log( String.format( "Found %s in a role ", user ) );
                    handleRoleChange( user );
                }
            }
                
            else if (phoneInfo[0].startsWith("#"))
            {
                return true;
            }
            else
            {
                env.warn().log("Can't parse. Skipping the line." + line);
                errorFlag = true;
            }
        } catch (Exception e) {
            errorFlag = true;
            errorMsg = e.getMessage();
            env.error().log( "Error while processing line:" + line +  e );
            e.printStackTrace();
        } finally {
            if (errorFlag) {
                env.info().log( "Fallout enrty being written for line:" + line );
                writer.write(line + "|Failed to update supervisor for user:" + errorMsg + "\n");
            }
        }
        return true;
    }


	public JobChange(AuthzTrans trans) throws APIException, IOException {
		super( trans.env() );
        myTrans = trans;
		session = cluster.connect();
	}

    public Organization getOrgFromID( AuthzTrans trans, Organization _org, String user ) {
	Organization org = _org;
        if ( org == null || ! user.endsWith( org.getRealm() ) ) {
            int idx = user.lastIndexOf('.');
            if ( idx > 0 )
                idx = user.lastIndexOf( '.', idx-1 );

            org = null;
            if ( idx > 0 ) {
                try {
                    org = OrganizationFactory.obtain( trans.env(), user.substring( idx+1 ) );
                } catch (Exception e) {
                    trans.error().log(e,"Failure Obtaining Organization");
                }
            }

            if ( org == null ) {
                PrintStream fallout = null;

                try {
                    fallout= fallout(fallout, "Fallout");
                    fallout.print("INVALID_ID,");
                    fallout.println(user);
                } catch (Exception e) {
                    env.error().log("Could not write to Fallout File",e);
                } 
                return( null );
            }
        }
        return( org );
    }        

    public void dumpOwnersMap()
    {
        for ( Map.Entry<String, ArrayList<NamespaceOwner>> e : ownersMap.entrySet() )
        {
            String key = e.getKey();
            ArrayList<NamespaceOwner> values = e.getValue();

            env.info().log( "ns user: " + key );

            for ( NamespaceOwner r : values )
            {
                env.info().log( String.format( "\tNS-user: %s, NS-name: %s, ownerCount: %d",
                                               r.user, r.ns, r.ownerCount ) );

            }
        }
    }

    public void dumpRolesMap()
    {
        for ( Map.Entry<String, ArrayList<UserRole>> e : rolesMap.entrySet() )
        {
            String key = e.getKey();
            ArrayList<UserRole> values = e.getValue();

            env.info().log( "user: " + key );

            for ( UserRole r : values )
            {
                env.info().log( String.format( "\trole-user: %s, role-name: %s",
                                                r.user, r.role ) );
            }
        }
    }
    public void dumpCredMap()
    {
        for ( Map.Entry<String, ArrayList<UserCred>> e : credsMap.entrySet() )
        {
            String key = e.getKey();
            ArrayList<UserCred> values = e.getValue();

            env.info().log( "user: " + key );

            for ( UserCred r : values )
            {
                env.info().log( String.format( "\tcred-user: %s, ns: %s",
                                                r.user, r.ns ) );
            }

        }
    }

	@Override
	protected void run (AuthzTrans trans)
	{
        if ( acquireRunLock( this.getClass().getName() ) != 1 ) {
                env.warn().log( "Cannot acquire run lock, exiting" );
                System.exit( 1 );
        }

		try {
//            Map<String,EmailMsg> email = new HashMap<String,EmailMsg>();

            try
            {
                String workingDir = System.getProperty("user.dir");
                env.info().log( "Process jobchange file. PWD is " + workingDir );
                
                loadUsersFromRoles();
                loadOwnersFromNS();
                loadUsersFromCred();

                dumpRolesMap();
                dumpOwnersMap();
                dumpCredMap();
                
                String fname = getJobChangeDataFile();
                
                if ( fname == null )
                {
                    env.warn().log("getJobChangedatafile returned null");
                }
                else
                {
                    env.info().log("done with FTP");
                }
				processJobChangeDataFile( fname, "fallout", null );
			}
            catch (Exception e)
            {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            

		} catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
		} catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
		}
	}

/*
    private class EmailMsg {
        private boolean urgent = false;
        public String url;
        public Organization org;
        public String summary;

        public EmailMsg() {
            org = null;
            summary = "";
        }

        public boolean getUrgent() {
            return( this.urgent );
        }

        public void setUrgent( boolean val ) {
            this.urgent = val;
        }
        public void setOrg( Organization newOrg ) {
            this.org = newOrg;
        }
        public Organization getOrg() {
            return( this.org );
        }
    }
*/
	@Override
	protected void _close(AuthzTrans trans) {
        session.close();
	}
}


