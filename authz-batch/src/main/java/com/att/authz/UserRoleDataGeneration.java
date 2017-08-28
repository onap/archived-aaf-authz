/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import com.att.authz.env.AuthzTrans;
import org.onap.aaf.inno.env.APIException;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class UserRoleDataGeneration extends Batch {

	protected UserRoleDataGeneration(AuthzTrans trans) throws APIException,	IOException {
		super(trans.env());
		session = cluster.connect();

	}

	@Override
	protected void run(AuthzTrans trans) {
		
		String query = "select * from authz.history" ;
        
        env.info().log( "query: " + query );

        Statement stmt = new SimpleStatement( query );
        ResultSet results = session.execute(stmt);
        int total=0;

        Iterator<Row> iter = results.iterator();

		Random rand = new Random();
		
		int min = 1;
		int max = 32;
        
        while( iter.hasNext() ) {
        	Row row = iter.next();
        	if (row.getString("target").equals("user_role")) {
        		int randomNum = rand.nextInt((max - min) + 1) + min;
        		
        		String newId = modifiedTimeuid(row.getUUID("id").toString(), randomNum);
        		String subject = row.getString("subject");
        		String newSubject = subject.split("\\|")[1];
 
        		String newInsert = insertStmt(row, newId, "role", newSubject);
           		Statement statement = new SimpleStatement(newInsert);
           		session.executeAsync(statement);

           		total++;        		
        	}
        }
        
        env.info().log(total+ " history elements inserted for user roles");
    
	}

	private String insertStmt(Row row, String newId, String newTarget, String newSubject) {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO authz.history (id,action,memo,reconstruct,subject,target,user,yr_mon) VALUES (");
		sb.append(newId+",");
		sb.append("'"+row.getString("action")+"',");
		sb.append("'"+row.getString("memo")+"',");
		sb.append("null,");
		sb.append("'"+newSubject+"',");
		sb.append("'"+newTarget+"',");
		sb.append("'"+row.getString("user")+"',");
		sb.append(row.getInt("yr_mon"));
		sb.append(")");
		
		return sb.toString();
	}

	private String modifiedTimeuid(String origTimeuuid, int rand) {
		UUID uuid = UUID.fromString(origTimeuuid);
		
		long bottomBits = uuid.getLeastSignificantBits();
		long newBottomBits = bottomBits + (1 << rand);
		if (newBottomBits - bottomBits == 0)
			env.info().log("Duplicate!\t"+uuid + " not duplicated for role history function.");
		
		UUID newUuid = new UUID(uuid.getMostSignificantBits(),newBottomBits);
		return newUuid.toString();
	}

	@Override
	protected void _close(AuthzTrans trans) {
        session.close();
        aspr.info( "End UserRoleDataGeneration processing" );

	}

}
