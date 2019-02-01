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
 *
 */

package org.onap.aaf.auth.batch.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.BatchPrincipal;
import org.onap.aaf.auth.batch.actions.Email;
import org.onap.aaf.auth.batch.actions.EmailPrint;
import org.onap.aaf.auth.batch.helpers.Notification;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;


public class NotifyCredExpiring extends Batch {

    private static final String UNKNOWN_ID = "unknown@deprecated.id";
    private Email email;
    private int maxEmails;
    private final PrintStream ps;
    private final AuthzTrans noAvg;
	private CSV csv;
	private CSVInfo csvInfo;

    public NotifyCredExpiring(AuthzTrans trans) throws APIException, IOException, OrganizationException {
        super(trans.env());
        TimeTaken tt = trans.start("Connect to Cluster", Env.REMOTE);
        try {
            session = cluster.connect();
        } finally {
            tt.done();
        }
        
        noAvg = env.newTransNoAvg();
        noAvg.setUser(new BatchPrincipal("batch:NotifyCredExpiring"));
        
        if (isDryRun()) {
            email = new EmailPrint();
            maxEmails=3;
            maxEmails = Integer.parseInt(trans.getProperty("MAX_EMAILS","3"));
        } else {
            email = new Email();
            maxEmails = Integer.parseInt(trans.getProperty("MAX_EMAILS","3"));
        }
        
        email.subject("AAF Password Expiration Notification (ENV: %s)",batchEnv);
        email.preamble("AAF (MOTS 22830) is the AT&T Authorization System used by many AT&T Tools and Applications.\n\n" +
                "  The following Credentials are expiring on the dates shown. Failure to act before the expiration date "
                + "will cause your App's Authentications to fail.\n");
        email.signature("Sincerely,\nAAF Team (Our MOTS# 22830)\n"
                + "https://wiki.web.att.com/display/aaf/Contact+Us\n"
                + "(Use 'Other Misc Requests (TOPS)')");
        
        boolean quit = false;
        if(args().length<1) {
        	System.err.println("Need CSV formatted Expiring Report");
        	quit = true;
        } else {
        	File f = new File(logDir(),args()[0]);
        	System.out.println("Reading " + f.getCanonicalPath());
        	csv = new CSV(f);
        }
        
        if(args().length<2) {
        	System.err.println("Need Email Template");
        }
        if(quit) {
        	System.exit(2);
        }
        
        csvInfo = new CSVInfo(System.err);
        try {
			csv.visit(csvInfo);
		} catch (CadiException e) {
			throw new APIException(e);
		}
        
        Notification.load(trans, session, Notification.v2_0_18);
        
        ps = new PrintStream(new FileOutputStream(logDir() + "/email"+Chrono.dateOnlyStamp()+".log",true));
        ps.printf("### Approval Notify %s for %s%s\n",Chrono.dateTime(),batchEnv,dryRun?", DryRun":"");
    }
    
    @Override
    protected void run(AuthzTrans trans) {
        
        // Temp structures
        Map<String,List<LastCred>> ownerCreds = new TreeMap<>();
        

        List<LastCred> noOwner = new ArrayList<>();
        ownerCreds.put(UNKNOWN_ID,noOwner);
        int emailCount=0;
        trans.info().printf("%d emails sent for %s", emailCount,batchEnv);
    }
    
    
    private static class CSVInfo implements CSV.Visitor {
    	private PrintStream out;
    	private Set<String> unsupported;
    	private NotifyCredVisitor credv;
    	private List<LastCred> llc;
    	
    	public CSVInfo(PrintStream out) {
    		this.out = out;
    		credv = new NotifyCredVisitor(llc = new ArrayList<>());
    	}
    	
		@Override
		public void visit(List<String> row) throws IOException, CadiException {
			
			switch(row.get(0)) {
			   case NotifyCredVisitor.SUPPORTS:
				   credv.visit(row);
				   break;
			   default:
				   if(unsupported==null) {
					   unsupported = new HashSet<String>();
				   }
				   if(!unsupported.contains(row.get(0))) {
					   unsupported.add(row.get(0));
					   out.println("Unsupported Type: " + row.get(0));
				   }
			}
		}
    }
    
    private static class Contact {
    	public List<String> contacts;
		private List<UserRole> owners;
    	
    	public Contact(final String ns) {
    		contacts = new ArrayList<>();
    		loadFromNS(ns);
    	}
    	
    	public void loadFromNS(final String ns) {
    		owners = UserRole.getByRole().get(ns+".owner");
    	}
    }
    
    private static class LastCred extends Contact {
    	public final String id;
    	public final int type;
        public final Date expires;
        
        public LastCred(final String id, final String ns, final int type, final Date expires) {
			super(ns);
			this.id = id;
			this.type = type;
			this.expires = expires;
		}
    }
    
    private static class NotifyCredVisitor implements CSV.Visitor {
    	public static final String SUPPORTS = "cred";
		private final List<LastCred> lastCred;
    	
    	public NotifyCredVisitor(final List<LastCred> lastCred) {
    		this.lastCred = lastCred;
    	}
    	
		@Override
		public void visit(List<String> row) throws IOException, CadiException {
			 try {
				lastCred.add(new LastCred(
					row.get(1), 
					row.get(2),
					Integer.parseInt(row.get(3)), 
					Chrono.dateOnlyFmt.parse(row.get(4))
					)
				);
			} catch (NumberFormatException | ParseException e) {
				throw new CadiException(e);
			}
		}
    }
    
    @Override
    protected void _close(AuthzTrans trans) {
        session.close();
        ps.close();
    }
}
