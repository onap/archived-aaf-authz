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

package org.onap.aaf.auth.reports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.onap.aaf.auth.Batch;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.helpers.Cred;
import org.onap.aaf.auth.helpers.Cred.Instance;
import org.onap.aaf.auth.helpers.UserRole;
import org.onap.aaf.auth.helpers.Visitor;
import org.onap.aaf.auth.helpers.X509;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.configure.Factory;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.cadi.util.CSV.Writer;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;


public class Expiring extends Batch {
    
    private int minOwners;
	private ArrayList<Writer> writerList;
	private File logDir;
	private Date now;
	private Date twoWeeksPast;
	private Writer twoWeeksPastCSV;
	private Date twoWeeksAway;
	private Writer twoWeeksAwayCSV;
	private Date oneMonthAway;
	private Writer oneMonthAwayCSV;
	private Date twoMonthsAway;
	private Writer twoMonthsAwayCSV;
	
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
            writerList = new ArrayList<CSV.Writer>();
            logDir = new File(logDir());
            logDir.mkdirs();
            
            GregorianCalendar gc = new GregorianCalendar();
            now = gc.getTime();
            gc.add(GregorianCalendar.WEEK_OF_MONTH, -2);
            twoWeeksPast = gc.getTime();
            File file = new File(logDir,"Expired"+Chrono.dateOnlyStamp(now)+".csv");
            twoWeeksPastCSV = new CSV(file).writer();
            writerList.add(twoWeeksPastCSV);
            
            gc.add(GregorianCalendar.WEEK_OF_MONTH, 2+2);
            twoWeeksAway = gc.getTime();
            file = new File(logDir,"TwoWeeksAway"+Chrono.dateOnlyStamp(now)+".csv");
            twoWeeksAwayCSV = new CSV(file).writer();
            writerList.add(twoWeeksAwayCSV);

            gc.add(GregorianCalendar.WEEK_OF_MONTH, -2);
            gc.add(GregorianCalendar.MONTH, 1);
            oneMonthAway = gc.getTime();
            file = new File(logDir,"OneMonthAway"+Chrono.dateOnlyStamp(now)+".csv");
            oneMonthAwayCSV = new CSV(file).writer();
            writerList.add(oneMonthAwayCSV);
            
            gc.add(GregorianCalendar.MONTH, 1);
            twoMonthsAway = gc.getTime();
            file = new File(logDir,"TwoMonthsAway"+Chrono.dateOnlyStamp(now)+".csv");
            twoMonthsAwayCSV = new CSV(file).writer();
            writerList.add(twoMonthsAwayCSV);
        } finally {
            tt0.done();
        }
    }

    @Override
    protected void run(AuthzTrans trans) {
		try {
			File file = new File(logDir, "AllOwnersExpired" + Chrono.dateOnlyStamp(now) + ".csv");
			final CSV ownerCSV = new CSV(file);

			Map<String, Set<UserRole>> owners = new TreeMap<String, Set<UserRole>>();
			trans.info().log("Process UserRoles");
			UserRole.load(trans, session, UserRole.v2_0_11, new Visitor<UserRole>() {
				@Override
				public void visit(UserRole ur) {
					// Cannot just delete owners, unless there is at least one left. Process later
					if ("owner".equals(ur.rname())) {
						Set<UserRole> urs = owners.get(ur.role());
						if (urs == null) {
							urs = new HashSet<UserRole>();
							owners.put(ur.role(), urs);
						}
						urs.add(ur);
					} else {
						writeAnalysis(ur);
					}
				}
			});

			// Now Process Owners, one owner Role at a time, ensuring one is left,
			// preferably
			// a good one. If so, process the others as normal. Otherwise, write
			// ExpiredOwners
			// report
			if (!owners.values().isEmpty()) {
				// Lazy Create file
				CSV.Writer expOwner = null;
				try {
					for (Set<UserRole> sur : owners.values()) {
						int goodOwners = 0;
						for (UserRole ur : sur) {
							if (ur.expires().after(now)) {
								++goodOwners;
							}
						}

						for (UserRole ur : sur) {
							if (goodOwners >= minOwners) {
								writeAnalysis(ur);
							} else {
								if (expOwner == null) {
									expOwner = ownerCSV.writer();
								}
								expOwner.row(ur.role(), ur.user(), ur.expires());
							}
						}
					}
				} finally {
					expOwner.close();
				}
			}
			
			trans.info().log("Checking for Expired Credentials");
			for (Cred cred : Cred.data.values()) {
		    	List<Instance> linst = cred.instances;
		    	if(linst!=null) {
			    	Instance lastBath = null;
			    	for(Instance inst : linst) {
			        	if(inst.expires.before(twoWeeksPast)) {
			        		cred.row(twoWeeksPastCSV,inst);
			    		} else if(inst.expires.after(now)){
			    			if (inst.type == CredDAO.BASIC_AUTH || inst.type == CredDAO.BASIC_AUTH_SHA256) {
			    				if(lastBath==null || lastBath.expires.before(inst.expires)) {
			    					lastBath = inst;
			    				}
			    			} else if(inst.type==CredDAO.CERT_SHA256_RSA) {
			    				writeAnalysis(cred, inst);
			    			}
			    		}
			    	}
			    	writeAnalysis(cred, lastBath);
		    	}
			}
			
			trans.info().log("Checking for Expired X509s");
			X509.load(trans, session, new Visitor<X509>() {
				@Override
				public void visit(X509 x509) {
					try {
						for(Certificate cert : Factory.toX509Certificate(x509.x509)) {
							writeAnalysis(x509, (X509Certificate)cert);
						}
					} catch (CertificateException | IOException e) {
						trans.error().log(e, "Error Decrypting X509");
					}
					
				}
			});
		} catch (FileNotFoundException e) {
			trans.info().log(e);
		}
	}
    
 
	protected void writeAnalysis(UserRole ur) {
    	if(ur.expires().before(twoWeeksPast)) {
    		ur.row(twoWeeksPastCSV);
		} else {
			if(ur.expires().after(now) && ur.expires().before(twoWeeksAway)) {
	    		ur.row(twoWeeksAwayCSV);
			} else {
				if(ur.expires().before(oneMonthAway)) {
		    		ur.row(oneMonthAwayCSV);
				} else {
					if(ur.expires().before(twoMonthsAway)) {
			    		ur.row(twoMonthsAwayCSV);
					}
				}
			}
		}
	}
    
    protected void writeAnalysis(Cred cred, Instance inst) {
    	if(inst!=null) {
			if(inst.expires.after(now) && inst.expires.before(twoWeeksAway)) {
	    		cred.row(twoWeeksAwayCSV, inst);
			} else {
				if(inst.expires.before(oneMonthAway)) {
		    		cred.row(oneMonthAwayCSV, inst);
				} else {
					if(inst.expires.before(twoMonthsAway)) {
			    		cred.row(twoMonthsAwayCSV, inst);
					}
				}
			}
		}
	}

    protected void writeAnalysis(X509 x509, X509Certificate x509Cert) throws IOException {
    	if(x509Cert!=null) {
	    	if(twoWeeksPast.after(x509Cert.getNotAfter())) {
				x509.row(twoWeeksPastCSV,x509Cert);
			}
    	}
	}

	@Override
    protected void _close(AuthzTrans trans) {
        session.close();
    	for(CSV.Writer cw : writerList) {
    		cw.close();
    	}
    }

}
