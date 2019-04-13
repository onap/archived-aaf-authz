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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.helpers.CQLBatch;
import org.onap.aaf.auth.batch.helpers.CQLBatchLoop;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Split;

public class Upload extends Batch {

	private static final String DAT = ".dat";

	private CQLBatch cqlBatch;

	private Map<String,Feed> feeds;


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
			
			cqlBatch = new CQLBatch(LogTarget.NULL,session);
			
			feeds=new HashMap<>();
			new Feed(feeds,"ns",1,"name,description,parent,scope=int,type=int",300);
			new Feed(feeds,"notified",3,"user,target,key,last",300);
			new Feed(feeds,"approval",1,"id=UUID,approver,last_notified,memo,operation,status,ticket=UUID,type,user",200);
			new Feed(feeds,"artifact",2,"mechid,machine,ca,dir,expires,notify,ns,os_user,renewdays=int,sans=set,sponsor,type=set",200);
			new Feed(feeds,"cred",1,"id,type=int,expires,cred=blob,notes,ns,other=int,prev=blob,tag",200);
			new Feed(feeds,"x509",2,"ca,serial=blob,id,x500,x509=C/R",200);
			new Feed(feeds,"role",2,"ns,name,description,perms=set",200);
			new Feed(feeds,"perm",4,"ns,type,instance,action,description,roles=set",200);
			new Feed(feeds,"history",1,"id=UUID,action,memo,reconstruct=blob,subject,target,user,yr_mon=int",300);

	    } finally {
	    	tt0.done();
	    }
	}


	@Override
	protected void run(AuthzTrans trans) {
		List<File> files = new ArrayList<>();
		if(args().length>0) {
			File dir = new File(args()[0]);
			if(dir.isDirectory()) {
				for(File f : dir.listFiles(pathname -> {
					return pathname.getName().endsWith(DAT);
				})) {
					files.add(f);
				}
			} else {
				File f;
				for(String arg : args()) {
					if(arg.endsWith(DAT)) {
						f=new File(arg);
					} else {
						f=new File(arg+DAT);
					}
					files.add(f);
				}
			}
		}
		for(File file : files) {
			String f = file.getName();
			final Feed feed = feeds.get(f.substring(0,f.length()-4));
			if(feed!=null) {
				TimeTaken tt = trans.start(file.getAbsolutePath(), Env.SUB);
				String msg = String.format("#### Running %s.dat Feed ####",feed.getName());
				trans.info().log(msg);
				System.out.println(msg);
				CQLBatchLoop cbl = new CQLBatchLoop(cqlBatch,feed.batchSize,dryRun).showProgress();
				
			    try {
					if(file.exists()) {
						CSV csv = new CSV(trans.env().access(),file).setDelimiter('|');
						csv.visit( row -> {
							feed.insert(cbl.inc(),row);
						});
					}
					cbl.flush();
				} catch (Throwable e) {
					e.printStackTrace();
				} finally {
					tt.done();
					System.err.flush();
					msg = String.format("\n%d applied in %d batches\n",cbl.total(), cbl.batches());
					trans.info().log(msg);
					System.out.println(msg);
				}
			}
		}
	}
	
	@Override
	protected void _close(AuthzTrans trans) {
        session.close();
	}

	private class Feed {
		private final String name;
		private final String[] flds;
		private final String[] types;
		private final int key;
		private final int batchSize;
		public Feed(Map<String, Feed> feeds, String feed, int keyLength, String fields,int batchSize) {
			name=feed;
			key = keyLength;
			flds = Split.splitTrim(',', fields);
			types = new String[flds.length];
			this.batchSize = batchSize;
			int equals;
			for(int i=0;i<flds.length;++i) {
				if((equals = flds[i].indexOf('='))>0) {
					types[i]=flds[i].substring(equals+1);
					flds[i]=flds[i].substring(0, equals);
				}
			}
			feeds.put(feed,this);
		}
		
		public String getName() {
			return name;
		}

		public void insert(StringBuilder sb,List<String> row) {
			sb.append("INSERT INTO authz.");
			sb.append(name);
			sb.append(" (");
			boolean first = true;
			StringBuilder values = new StringBuilder(") VALUES (");
			String value;
			String type;
			for(int idx=0;idx<row.size();++idx) {
				value = row.get(idx).trim();
				if(idx<key || !(value.isEmpty() || "null".equals(value))) {
					if(first) {
						first = false;
					} else {
						sb.append(',');
						values.append(',');
					}
					sb.append(flds[idx]);
					type=types[idx];
					if(type==null) { // String is default.
						switch(value) {
						  case "":
							if(idx<key) {
								// Key value has to be something, but can't be actual null
								values.append("''");
							} else {
								values.append("null");
							}
							break;
						  default:
							values.append('\'');
							values.append(value.replaceAll("'","''"));							
							values.append('\'');
						}
					} else switch(type) {
							case "C/R":
								values.append('\'');
								values.append(value.replaceAll("\n", "\\n"));
								values.append('\'');
								break;
							default:
								values.append(value);
								break;
								
					}
				}
			}
			sb.append(values);
			sb.append(");\n");
		}
	}
}

