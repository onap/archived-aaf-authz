/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2019 IBM.
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
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.helpers.Approval;
import org.onap.aaf.auth.batch.helpers.Cred;
import org.onap.aaf.auth.batch.helpers.Cred.Instance;
import org.onap.aaf.auth.batch.helpers.ExpireRange;
import org.onap.aaf.auth.batch.helpers.ExpireRange.Range;
import org.onap.aaf.auth.batch.helpers.Future;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.batch.helpers.X509;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.configure.Factory;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;


public class Expiring extends Batch {
    
	private static final String CSV = ".csv";
	private static final String INFO = "info";
	private static final String EXPIRED_OWNERS = "ExpiredOwners";
	private int minOwners;
	private Map<String, CSV.Writer> writerList;
	private ExpireRange expireRange;
	private Date deleteDate;
	private CSV.Writer deleteCW;
	
	public Expiring(AuthzTrans trans) throws APIException, IOException, OrganizationException {
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
            
            // Load Cred.  We don't follow Visitor, because we have to gather up everything into Identity Anyway
            Cred.load(trans, session);

            minOwners=1;

            // Create Intermediate Output 
            writerList = new HashMap<>();
            
            expireRange = new ExpireRange(trans.env().access());
            String sdate = Chrono.dateOnlyStamp(expireRange.now);
            for( List<Range> lr : expireRange.ranges.values()) {
            	for(Range r : lr ) {
            		if(writerList.get(r.name())==null) {
                    	File file = new File(logDir(),r.name() + sdate +CSV);
                    	CSV csv = new CSV(env.access(),file);
                    	CSV.Writer cw = csv.writer(false);
                    	cw.row(INFO,r.name(),Chrono.dateOnlyStamp(expireRange.now),r.reportingLevel());
                    	writerList.put(r.name(),cw);
                    	if("Delete".equals(r.name())) {
                    		deleteDate = r.getEnd();
                    		deleteCW = cw;
                    	}
                    	trans.init().log("Creating File:",file.getAbsolutePath());
            		}
            	}
            }
            Approval.load(trans, session, Approval.v2_0_17);
        } finally {
            tt0.done();
        }
    }

    @Override
    protected void run(AuthzTrans trans) {
    	
		////////////////////
		trans.info().log("Checking for Expired Futures");
		Future.load(trans, session, Future.v2_0_17, fut -> {
			if(fut.expires().before(expireRange.now)) {
				Future.row(deleteCW,fut);
				List<Approval> appls = Approval.byTicket.get(fut.id());
				if(appls!=null) {
					for(Approval a : appls) {
						Approval.row(deleteCW, a);
					}
				}
			}
		});
		
		try {
			File file = new File(logDir(), EXPIRED_OWNERS + Chrono.dateOnlyStamp(expireRange.now) + CSV);
			final CSV ownerCSV = new CSV(env.access(),file);

			Map<String, Set<UserRole>> owners = new TreeMap<String, Set<UserRole>>();
			trans.info().log("Process UserRoles");
			
			/**
			   Run through User Roles.  
			   Owners are treated specially in next section.
			   Regular roles are checked against Date Ranges.  If match Date Range, write out to appropriate file.
			*/
			UserRole.load(trans, session, UserRole.v2_0_11, ur -> {
				// Cannot just delete owners, unless there is at least one left. Process later
				if ("owner".equals(ur.rname())) {
					Set<UserRole> urs = owners.get(ur.role());
					if (urs == null) {
						urs = new HashSet<UserRole>();
						owners.put(ur.role(), urs);
					}
					urs.add(ur);
				} else {
					writeAnalysis(trans,ur);
				}
			});

			/**
			  Now Process Owners, one owner Role at a time, ensuring one is left,
			  preferably a good one. If so, process the others as normal. 
			  
			  Otherwise, write to ExpiredOwners Report
			*/
			if (!owners.values().isEmpty()) {
				// Lazy Create file
				CSV.Writer expOwner = null;
				try {
					for (Set<UserRole> sur : owners.values()) {
						int goodOwners = 0;
						for (UserRole ur : sur) {
							if (ur.expires().after(expireRange.now)) {
								++goodOwners;
							}
						}

						for (UserRole ur : sur) {
							if (goodOwners >= minOwners) {
								writeAnalysis(trans, ur);
							} else {
								if (expOwner == null) {
									expOwner = ownerCSV.writer();
									expOwner.row(INFO,EXPIRED_OWNERS,Chrono.dateOnlyStamp(expireRange.now),2);
								}
								expOwner.row("owner",ur.role(), ur.user(), Chrono.dateOnlyStamp(ur.expires()));
							}
						}
					}
				} finally {
					if(expOwner!=null) {
						expOwner.close();
					}
				}
			}
			
			/**
			 * Check for Expired Credentials
			 * 
			 * 
			 */
			trans.info().log("Checking for Expired Credentials");			
			for (Cred cred : Cred.data.values()) {
		    	List<Instance> linst = cred.instances;
		    	if(linst!=null) {
			    	Instance lastBath = null;
			    	for(Instance inst : linst) {
			    		// Special Behavior: only eval the LAST Instance
		    			if (inst.type == CredDAO.BASIC_AUTH || inst.type == CredDAO.BASIC_AUTH_SHA256) {
				        	if(deleteDate!=null && inst.expires.before(deleteDate)) {
				        		writeAnalysis(trans, cred, inst); // will go to Delete
				    		} else if(lastBath==null || lastBath.expires.before(inst.expires)) {
		    					lastBath = inst;
		    				}
		    			} else {
		    				writeAnalysis(trans, cred, inst);
		    			}
			    	}
			    	if(lastBath!=null) {
			    		writeAnalysis(trans, cred, lastBath);
			    	}
		    	}
			}

			////////////////////
			trans.info().log("Checking for Expired X509s");
			X509.load(trans, session, x509 -> {
				try {
					for(Certificate cert : Factory.toX509Certificate(x509.x509)) {
						writeAnalysis(trans, x509, (X509Certificate)cert);
					}
				} catch (CertificateException | IOException e) {
					trans.error().log(e, "Error Decrypting X509");
				}

			});

		} catch (FileNotFoundException e) {
			trans.info().log(e);
		}
		
		////////////////////
		trans.info().log("Checking for Orphaned Approvals");
		Approval.load(trans, session, Approval.v2_0_17, appr -> {
			UUID ticket = appr.add.ticket;
			if(ticket==null) {
				Approval.row(deleteCW,appr);
			}
		});
		

	}
    
 
	private void writeAnalysis(AuthzTrans trans, UserRole ur) {
		Range r = expireRange.getRange("ur", ur.expires());
		if(r!=null) {
			CSV.Writer cw = writerList.get(r.name());
			if(cw!=null) {
				ur.row(cw);
			}
		}
	}
    
    private void writeAnalysis(AuthzTrans trans, Cred cred, Instance inst) {
    	if(cred!=null && inst!=null) {
			Range r = expireRange.getRange("cred", inst.expires);
			if(r!=null) {
				CSV.Writer cw = writerList.get(r.name());
				if(cw!=null) {
					cred.row(cw,inst);
				}
			}
    	}
	}

    private void writeAnalysis(AuthzTrans trans, X509 x509, X509Certificate x509Cert) throws IOException {
		Range r = expireRange.getRange("x509", x509Cert.getNotAfter());
		if(r!=null) {
			CSV.Writer cw = writerList.get(r.name());
			if(cw!=null) {
				x509.row(cw,x509Cert);
			}
		}
	}

    /*
    private String[] contacts(final AuthzTrans trans, final String ns, final int levels) {
    	List<UserRole> owners = UserRole.getByRole().get(ns+".owner");
    	List<UserRole> current = new ArrayList<>();
    	for(UserRole ur : owners) {
    		if(expireRange.now.before(ur.expires())) {
    			current.add(ur);
    		}
    	}
    	if(current.isEmpty()) {
    		trans.warn().log(ns,"has no current owners");
    		current = owners;
    	}
    	
    	List<String> email = new ArrayList<>();
    	for(UserRole ur : current) {
    		Identity id;
    		int i=0;
    		boolean go = true;
    		try {
    			id = org.getIdentity(trans, ur.user());
        		do {
	    			if(id!=null) {
						email.add(id.email());
						if(i<levels) {
							id = id.responsibleTo();
						} else {
							go = false;
						}
	    			} else {
	    				go = false;
	    			}
        		} while(go);
			} catch (OrganizationException e) {
				trans.error().log(e);
			}
    	}
    	
    	return email.toArray(new String[email.size()]);
    }
*/
    
	@Override
    protected void _close(AuthzTrans trans) {
        session.close();
    	for(CSV.Writer cw : writerList.values()) {
    		cw.close();
    	}
    }

}
