/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2018 IBM.
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
 */

package org.onap.aaf.auth.batch.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.helpers.CQLBatch;
import org.onap.aaf.auth.batch.helpers.CQLBatchLoop;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

public class Upload extends Batch {

	private CQLBatchLoop cqlBatch;

	// APPROVALS
	private static final String APPR_INS_FMT="  INSERT INTO authz.approval "
			+ "(id,approver,last_notified,memo,operation,status,ticket,type,user) "
			+ "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s);\n";
	private static final Boolean[] APPR_QUOTES = new Boolean[]{false,true,true,true,true,true,false,true,true};

	// ARTIFACTS
	private static final String ARTI_INS_FMT="  INSERT INTO authz.artifact "
			+ "(mechid,machine,ca,dir,expires,notify,ns,os_user,renewdays,sans,sponsor,type) "
			+ "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s);\n";
	private static final Boolean[] ARTI_QUOTES = new Boolean[]
			{true,true,true,true,true,true,true,true,false,false,true,false};

	// CREDS
	private static final String CRED_INS_FMT="  INSERT INTO authz.cred "
			+ "(id,type,expires,cred,notes,ns,other,prev) "
			+ "VALUES (%s,%s,%s,%s,%s,%s,%s,%s);\n";
	private static final Boolean[] CRED_QUOTES = new Boolean[]
			{true,false,true,false,true,true,false,false};

	// NS
	private static final String NS_INS_FMT="  INSERT INTO authz.ns "
			+ "(name,description,parent,scope,type) "
			+ "VALUES (%s,%s,%s,%s,%s);\n";
	private static final Boolean[] NS_QUOTES = new Boolean[]
			{true,true,true,false,false};

	// x509
	private static final String X509_INS_FMT="  INSERT INTO authz.x509 "
			+ "(ca,serial,id,x500,x509) "
			+ "VALUES (%s,%s,%s,%s,%s);\n";
	private static final Boolean[] X509_QUOTES = new Boolean[]
			{true,false,true,true,true};

	// ROLE
	private static final String ROLE_INS_FMT="  INSERT INTO authz.role "
			+ "(ns,name,description,perms) "
			+ "VALUES (%s,%s,%s,%s);\n";
	private static final Boolean[] ROLE_QUOTES = new Boolean[]
			{true,true,true,false};
	// ROLE
	private static final String PERM_INS_FMT="  INSERT INTO authz.perm "
			+ "(ns,type,instance,action,description,roles) "
			+ "VALUES (%s,%s,%s,%s,%s,%s);\n";
	private static final Boolean[] PERM_QUOTES = new Boolean[]
			{true,true,true,true,true,false};

	public Upload(AuthzTrans trans) throws APIException, IOException, OrganizationException {
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
			
			cqlBatch = new CQLBatchLoop(new CQLBatch(LogTarget.NULL,session),50,dryRun);
	    } finally {
	    	tt0.done();
	    }
	}

	@Override
	protected void run(AuthzTrans trans) {
		String line;
		StringBuilder sb = new StringBuilder();
		List<String> array = new ArrayList<String>();
		for(String feed : args()) {
			File file;
			if(feed.endsWith(".dat")) {
				file = new File(feed);
				feed = file.getName();
				feed = feed.substring(0,feed.length()-4);
			} else {
				file = new File(feed+".dat");
			}
			TimeTaken tt = trans.start(file.getAbsolutePath(), Env.SUB);
			String msg = String.format("#### Running %s.dat Feed ####",feed);
			trans.info().log(msg);
			System.out.println(msg);
	    	BufferedReader br = null;
		    try {
				if(file.exists()) {
					try {
						br = new BufferedReader(new FileReader(file));
						try {
							while((line=br.readLine())!=null) {
								if(line.length()>5000) {
									cqlBatch.flush();
								}
								cqlBatch.preLoop();

								// Split into fields, first turning Escaped values into something we can convert back from
								char c=0;
								boolean inQuote = false;
								int fldcnt = 0;
								for(int i=0;i<line.length();++i) {
									switch(c=line.charAt(i)) {
										case '"':
											inQuote = !inQuote;
											break;
										case '|':
											if(inQuote) {
												sb.append(c);
											} else {
												addField(feed,fldcnt++,array,sb);
											}
											break;
										default:
											sb.append(c);
									}
								}
								addField(feed,fldcnt,array,sb);
								cqlBatch.inc().append(build(feed, array));
							}
							cqlBatch.flush();
						} catch (Exception t) {
							trans.error().log(t);
						} finally {
							br.close();
						}
					} catch (IOException e) {
						trans.error().log(e);
					}
				} else {
					trans.error().log("No file found: ", file.getAbsolutePath());
				}
			} finally {
				tt.done();
				System.err.flush();
				msg = String.format("\n%d applied in %d batches\n",cqlBatch.total(), cqlBatch.batches());
				trans.info().log(msg);
				System.out.println(msg);
			}
		}

	}


	private String build(String feed, List<String> array) {
		String rv;
		if(array.size()>0) {
			switch(feed) {
				case "approval":
					rv = String.format(APPR_INS_FMT,array.toArray());
					break;
				case "artifact":
					rv = String.format(ARTI_INS_FMT,array.toArray());
					break;
				case "cred":
					rv = String.format(CRED_INS_FMT,array.toArray());
					break;
				case "ns":
					rv = String.format(NS_INS_FMT,array.toArray());
					break;
				case "role":
					rv = String.format(ROLE_INS_FMT,array.toArray());
					break;
				case "perm":
					rv = String.format(PERM_INS_FMT,array.toArray());
					break;
				case "x509":
					rv = String.format(X509_INS_FMT,array.toArray());
					break;
				default:
					rv = "";
			}
			array.clear();
		} else {
			rv = ""; 
		}
		return rv;
	}
	
	private void addField(String feed, int fldcnt, List<String> array, StringBuilder sb) {
		Boolean[] ba;
		switch(feed) {
			case "approval":
				ba = APPR_QUOTES;
				break;
			case "artifact":
				ba = ARTI_QUOTES;
				break;
			case "cred":
				ba = CRED_QUOTES;
				break;
			case "ns":
				ba = NS_QUOTES;
				break;
			case "role":
				ba = ROLE_QUOTES;
				break;
			case "perm":
				ba = PERM_QUOTES;
				break;
			case "x509":
				ba = X509_QUOTES;
				break;
			default:
				ba = null;
		}
		if(ba!=null) {
			if(sb.toString().length()==0) {
				array.add("null");
			} else {
				if(ba[fldcnt]) {
					String s = null;
					if(sb.indexOf("'")>=0) {
						s = sb.toString().replace("'","''");
					}
					if(sb.indexOf("\\n")>=0) {
						if(s==null) {
							s = sb.toString().replace("\\n","\n");
						} else {
							s = s.replace("\\n","\n");
						}
					}
					if(sb.indexOf("\\t")>=0) {
						if(s==null) {
							s = sb.toString().replace("\\t","\t");
						} else {
							s = s.replace("\\t","\t");
						}
					}
					if(s==null) {
						array.add("'" + sb + '\'');
					} else {
						array.add("'" + s + '\'');
					}
				} else {
					array.add(sb.toString());
				}
			}
			sb.setLength(0);
		}
	}


	@Override
	protected void _close(AuthzTrans trans) {
        session.close();
	}

}

