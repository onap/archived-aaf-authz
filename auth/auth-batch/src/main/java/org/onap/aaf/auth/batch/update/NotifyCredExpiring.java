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
    private static final String AAF_INSTANTIATED_MECHID = "AAF INSTANTIATED MECHID";
    private static final String EXPIRATION_DATE = "EXPIRATION DATE";
    private static final String QUICK_LINK = "QUICK LINK TO UPDATE PAGE";
    private static final String DASH_1 = "-----------------------";
    private static final String DASH_2 = "---------------";
    private static final String DASH_3 = "----------------------------------------------------";
    private static final String LINE = "\n----------------------------------------------------------------";
    private Email email;
    private int maxEmails;
    private final PrintStream ps;
    private final AuthzTrans noAvg;
    private String supportEmailAddr;
	private CSV csv;
	private CSVInfo csvInfo;

    public NotifyCredExpiring(AuthzTrans trans) throws APIException, IOException, OrganizationException, CadiException {
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
        	csv = new CSV(env.access(),f);
        }
        
        if(args().length<2) {
        	System.err.println("Need Email Template");
        	//quit = true;
        }
        if(quit) {
        	System.exit(2);
        }
        
        csvInfo = new CSVInfo(System.err);
		csv.visit(csvInfo);
        
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

//        // Get a list of ONLY the ones needing email by Owner
//        for (Entry<String, List<Cred>> es : Cred.byNS.entrySet()) {
//            for (Cred c : es.getValue()) {
//                List<UserRole> ownerURList = UserRole.getByRole().get(es.getKey()+".owner");
//                if (ownerURList!=null) {
//                    for (UserRole ur:ownerURList) {
//                        String owner = ur.user();
//                        List<LastCred> llc = ownerCreds.get(owner);
//                        if (llc==null) {
//                            ownerCreds.put(owner, (llc=new ArrayList<>()));
//                        }
//                        llc.add(new LastCred(c,last));
//                    }
//                } else {
//                    noOwner.add(new LastCred(c,last));
//                }
//            }
//        }
//        
//        boolean bCritical,bNormal,bEarly;
//        Message msg = new Message();
//        Notification ownNotf;
//        StringBuilder logMessage = new StringBuilder();
//        for (Entry<String,List<LastCred>> es : ownerCreds.entrySet()) {
//            String owner = es.getKey();
//            boolean header = true;
//            try {
//                Organization org = OrganizationFactory.obtain(env, owner);
//                Identity user = org.getIdentity(noAvg, owner);
//                if (!UNKNOWN_ID.equals(owner) && user==null) {
//                    ps.printf("Invalid Identity: %s\n", owner);
//                } else {
//                    logMessage.setLength(0);
//                    if (maxEmails>emailCount) {
//                        bCritical=bNormal=bEarly = false;
//                        email.clear();
//                        msg.clear();
//                        email.addTo(user==null?supportEmailAddr:user.email());
//
//                        ownNotf = Notification.get(es.getKey(),TYPE.CN);
//                        if (ownNotf==null) {
//                            ownNotf = Notification.create(user==null?UNKNOWN_ID:user.fullID(), TYPE.CN);
//                        }
//                        last = ownNotf.last;
//                        // Get Max ID size for formatting purposes
//                        int length = AAF_INSTANTIATED_MECHID.length();
//                        for (LastCred lc : es.getValue()) {
//                            length = Math.max(length, lc.cred.id.length());
//                        }
//                        String id_exp_fmt = "\t%-"+length+"s  %15s  %s";
//
//                        Collections.sort(es.getValue(),LastCred.COMPARE);
//                        for (LastCred lc : es.getValue()) {
//                            if (lc.last.after(must) && lc.last.before(early) && 
//                                (ownNotf.last==null || ownNotf.last.before(withinLastWeek))) {
//                                if (!bEarly && header) {
//                                    msg.line("\tThe following are friendly 2 month reminders, just in case you need to schedule your updates early.  "
//                                            + "You will be reminded next month\n");
//                                    msg.line(id_exp_fmt, AAF_INSTANTIATED_MECHID,EXPIRATION_DATE, QUICK_LINK);
//                                    msg.line(id_exp_fmt, DASH_1, DASH_2, DASH_3);
//                                    header = false;
//                                }
//                                bEarly = true;
//                            } else if (lc.last.after(critical) && lc.last.before(must) && 
//                                    (ownNotf.last==null || ownNotf.last.before(withinLastWeek))) {
//                                if (!bNormal) {
//                                    boolean last2wks = lc.last.before(within2Weeks);
//                                    if (last2wks) {
//                                        try {
//                                            Identity supvsr = user.responsibleTo();
//                                            email.addCC(supvsr.email());
//                                        } catch (OrganizationException e) {
//                                            trans.error().log(e, "Supervisor cannot be looked up");
//                                        }
//                                    }
//                                    if (header) {
//                                        msg.line("\tIt is now important for you to update Passwords all all configurations using them for the following.\n" +
//                                                (last2wks?"\tNote: Your Supervisor is CCd\n":"\tNote: Your Supervisor will be notified if this is not being done before the last 2 weeks\n"));
//                                        msg.line(id_exp_fmt, AAF_INSTANTIATED_MECHID,EXPIRATION_DATE, QUICK_LINK);
//                                        msg.line(id_exp_fmt, DASH_1, DASH_2, DASH_3);
//                                    }
//                                    header = false;
//                                }
//                                bNormal=true;
//                            } else if (lc.last.after(tooLate) && lc.last.before(critical)) { // Email Every Day, with Supervisor
//                                if (!bCritical && header) {
//                                    msg.line("\t!!! WARNING: These Credentials will expire in LESS THAN ONE WEEK !!!!\n" +
//                                             "\tYour supervisor is added to this Email\n");
//                                    msg.line(id_exp_fmt, AAF_INSTANTIATED_MECHID,EXPIRATION_DATE, QUICK_LINK);
//                                    msg.line(id_exp_fmt, DASH_1, DASH_2, DASH_3);
//                                    header = false;
//                                }
//                                bCritical = true;
//                                try {
//                                    if (user!=null) {
//                                        Identity supvsr = user.responsibleTo();
//                                        if (supvsr!=null) {
//                                            email.addCC(supvsr.email());
//                                            supvsr = supvsr.responsibleTo();
//                                            if (supvsr!=null) {
//                                                email.addCC(supvsr.email());
//                                            }
//                                        }
//                                    }
//                                } catch (OrganizationException e) {
//                                    trans.error().log(e, "Supervisor cannot be looked up");
//                                }
//                            }
//                            if (bEarly || bNormal || bCritical) {
//                                if (logMessage.length()==0) {
//                                    logMessage.append("NotifyCredExpiring");
//                                }
//                                logMessage.append("\n\t");
//                                logMessage.append(lc.cred.id);
//                                logMessage.append('\t');
//                                logMessage.append(Chrono.dateOnlyStamp(lc.last));
//                                msg.line(id_exp_fmt, lc.cred.id, Chrono.dateOnlyStamp(lc.last)+"     ",env.getProperty(GUI_URL)+"/creddetail?ns="+Question.domain2ns(lc.cred.id));
//                            }
//                        }
//                        
//                        if (bEarly || bNormal || bCritical) {
//                            msg.line(LINE);
//                            msg.line("Why are you receiving this Notification?\n");
//                                msg.line("You are the listed owner of one or more AAF Namespaces. ASPR requires that those responsible for "
//                                        + "applications and their access review them regularly for accuracy.  The AAF WIKI page for AT&T is https://wiki.web.att.com/display/aaf.  "
//                                        + "You might like https://wiki.web.att.com/display/aaf/AAF+in+a+Nutshell.  More detailed info regarding questions of being a Namespace Owner is available at https://wiki.web.att.com/pages/viewpage.action?pageId=594741363\n");
//                                msg.line("You may view the Namespaces you listed as Owner for in this AAF Env by viewing the following webpage:\n");
//                                msg.line("   %s/ns\n\n",env.getProperty(GUI_URL));
//                            email.msg(msg);
//                            Result<Void> rv = email.exec(trans, org,"");
//                            if (rv.isOK()) {
//                                ++emailCount;
//                                if (!isDryRun()) {
//                                    ownNotf.update(noAvg, session, false);
//                                    // SET LastNotification
//                                }
//                                email.log(ps,logMessage.toString());
//                            } else {
//                                trans.error().log(rv.errorString());
//                            }
//                        }
//                    }
//                }
//            } catch (OrganizationException e) {
//                trans.info().log(e);
//            }
//        }
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
