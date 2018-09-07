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

package org.onap.aaf.auth.update;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.onap.aaf.auth.Batch;
import org.onap.aaf.auth.BatchPrincipal;
import org.onap.aaf.auth.actions.Email;
import org.onap.aaf.auth.actions.EmailPrint;
import org.onap.aaf.auth.actions.Message;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.helpers.Cred;
import org.onap.aaf.auth.helpers.Notification;
import org.onap.aaf.auth.helpers.UserRole;
import org.onap.aaf.auth.helpers.Notification.TYPE;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.EmailWarnings;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.OrganizationFactory;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;

import java.util.TreeMap;


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
        
        if ((supportEmailAddr = env.getProperty("mailFromUserId"))==null) {
            throw new APIException("mailFromUserId property must be set");
        }
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

        Cred.load(trans, session,CredDAO.BASIC_AUTH, CredDAO.BASIC_AUTH_SHA256);
        Notification.load(trans, session, Notification.v2_0_18);
        UserRole.load(trans, session, UserRole.v2_0_11);
        
        ps = new PrintStream(new FileOutputStream(logDir() + "/email"+Chrono.dateOnlyStamp()+".log",true));
        ps.printf("### Approval Notify %s for %s%s\n",Chrono.dateTime(),batchEnv,dryRun?", DryRun":"");
    }
    
    @Override
    protected void run(AuthzTrans trans) {
        
        EmailWarnings ewp = org.emailWarningPolicy();
        long now = System.currentTimeMillis();
        Date early = new Date(now+(ewp.credExpirationWarning()*2)); // 2 months back
        Date must = new Date(now+ewp.credExpirationWarning()); // 1 months back
        Date critical = new Date(now+ewp.emailUrgentWarning()); // 1 week
        Date within2Weeks = new Date(now+604800000 * 2);
        Date withinLastWeek = new Date(now-604800000);
        Date tooLate = new Date(now);
        
        // Temp structures
        Map<String,Cred> lastCred = new HashMap<>();
        Map<String,List<LastCred>> ownerCreds = new TreeMap<>();
        Date last;
        

        List<LastCred> noOwner = new ArrayList<>();
        ownerCreds.put(UNKNOWN_ID,noOwner);

        // Get a list of ONLY the ones needing email by Owner
        for (Entry<String, List<Cred>> es : Cred.byNS.entrySet()) {
            lastCred.clear();
            for (Cred c : es.getValue()) {
                last = c.last(CredDAO.BASIC_AUTH,CredDAO.BASIC_AUTH_SHA256);
                if (last!=null && last.after(tooLate) && last.before(early)) {
                    List<UserRole> ownerURList = UserRole.getByRole().get(es.getKey()+".owner");
                    if (ownerURList!=null) {
                        for (UserRole ur:ownerURList) {
                            String owner = ur.user();
                            List<LastCred> llc = ownerCreds.get(owner);
                            if (llc==null) {
                                ownerCreds.put(owner, (llc=new ArrayList<>()));
                            }
                            llc.add(new LastCred(c,last));
                        }
                    } else {
                        noOwner.add(new LastCred(c,last));
                    }
                }
            }
        }
        
        boolean bCritical,bNormal,bEarly;
        int emailCount=0;
        Message msg = new Message();
        Notification ownNotf;
        StringBuilder logMessage = new StringBuilder();
        for (Entry<String,List<LastCred>> es : ownerCreds.entrySet()) {
            String owner = es.getKey();
            boolean header = true;
            try {
                Organization org = OrganizationFactory.obtain(env, owner);
                Identity user = org.getIdentity(noAvg, owner);
                if (!UNKNOWN_ID.equals(owner) && user==null) {
                    ps.printf("Invalid Identity: %s\n", owner);
                } else {
                    logMessage.setLength(0);
                    if (maxEmails>emailCount) {
                        bCritical=bNormal=bEarly = false;
                        email.clear();
                        msg.clear();
                        email.addTo(user==null?supportEmailAddr:user.email());

                        ownNotf = Notification.get(es.getKey(),TYPE.CN);
                        if (ownNotf==null) {
                            ownNotf = Notification.create(user==null?UNKNOWN_ID:user.fullID(), TYPE.CN);
                        }
                        last = ownNotf.last;
                        // Get Max ID size for formatting purposes
                        int length = AAF_INSTANTIATED_MECHID.length();
                        for (LastCred lc : es.getValue()) {
                            length = Math.max(length, lc.cred.id.length());
                        }
                        String id_exp_fmt = "\t%-"+length+"s  %15s  %s";

                        Collections.sort(es.getValue(),LastCred.COMPARE);
                        for (LastCred lc : es.getValue()) {
                            if (lc.last.after(must) && lc.last.before(early) && 
                                (ownNotf.last==null || ownNotf.last.before(withinLastWeek))) {
                                if (!bEarly && header) {
                                    msg.line("\tThe following are friendly 2 month reminders, just in case you need to schedule your updates early.  "
                                            + "You will be reminded next month\n");
                                    msg.line(id_exp_fmt, AAF_INSTANTIATED_MECHID,EXPIRATION_DATE, QUICK_LINK);
                                    msg.line(id_exp_fmt, DASH_1, DASH_2, DASH_3);
                                    header = false;
                                }
                                bEarly = true;
                            } else if (lc.last.after(critical) && lc.last.before(must) && 
                                    (ownNotf.last==null || ownNotf.last.before(withinLastWeek))) {
                                if (!bNormal) {
                                    boolean last2wks = lc.last.before(within2Weeks);
                                    if (last2wks) {
                                        try {
                                            Identity supvsr = user.responsibleTo();
                                            email.addCC(supvsr.email());
                                        } catch (OrganizationException e) {
                                            trans.error().log(e, "Supervisor cannot be looked up");
                                        }
                                    }
                                    if (header) {
                                        msg.line("\tIt is now important for you to update Passwords all all configurations using them for the following.\n" +
                                                (last2wks?"\tNote: Your Supervisor is CCd\n":"\tNote: Your Supervisor will be notified if this is not being done before the last 2 weeks\n"));
                                        msg.line(id_exp_fmt, AAF_INSTANTIATED_MECHID,EXPIRATION_DATE, QUICK_LINK);
                                        msg.line(id_exp_fmt, DASH_1, DASH_2, DASH_3);
                                    }
                                    header = false;
                                }
                                bNormal=true;
                            } else if (lc.last.after(tooLate) && lc.last.before(critical)) { // Email Every Day, with Supervisor
                                if (!bCritical && header) {
                                    msg.line("\t!!! WARNING: These Credentials will expire in LESS THAN ONE WEEK !!!!\n" +
                                             "\tYour supervisor is added to this Email\n");
                                    msg.line(id_exp_fmt, AAF_INSTANTIATED_MECHID,EXPIRATION_DATE, QUICK_LINK);
                                    msg.line(id_exp_fmt, DASH_1, DASH_2, DASH_3);
                                    header = false;
                                }
                                bCritical = true;
                                try {
                                    if (user!=null) {
                                        Identity supvsr = user.responsibleTo();
                                        if (supvsr!=null) {
                                            email.addCC(supvsr.email());
                                            supvsr = supvsr.responsibleTo();
                                            if (supvsr!=null) {
                                                email.addCC(supvsr.email());
                                            }
                                        }
                                    }
                                } catch (OrganizationException e) {
                                    trans.error().log(e, "Supervisor cannot be looked up");
                                }
                            }
                            if (bEarly || bNormal || bCritical) {
                                if (logMessage.length()==0) {
                                    logMessage.append("NotifyCredExpiring");
                                }
                                logMessage.append("\n\t");
                                logMessage.append(lc.cred.id);
                                logMessage.append('\t');
                                logMessage.append(Chrono.dateOnlyStamp(lc.last));
                                msg.line(id_exp_fmt, lc.cred.id, Chrono.dateOnlyStamp(lc.last)+"     ",env.getProperty(GUI_URL)+"/creddetail?ns="+Question.domain2ns(lc.cred.id));
                            }
                        }
                        
                        if (bEarly || bNormal || bCritical) {
                            msg.line(LINE);
                            msg.line("Why are you receiving this Notification?\n");
                                msg.line("You are the listed owner of one or more AAF Namespaces. ASPR requires that those responsible for "
                                        + "applications and their access review them regularly for accuracy.  The AAF WIKI page for AT&T is https://wiki.web.att.com/display/aaf.  "
                                        + "You might like https://wiki.web.att.com/display/aaf/AAF+in+a+Nutshell.  More detailed info regarding questions of being a Namespace Owner is available at https://wiki.web.att.com/pages/viewpage.action?pageId=594741363\n");
                                msg.line("You may view the Namespaces you listed as Owner for in this AAF Env by viewing the following webpage:\n");
                                msg.line("   %s/ns\n\n",env.getProperty(GUI_URL));
                            email.msg(msg);
                            Result<Void> rv = email.exec(trans, org,"");
                            if (rv.isOK()) {
                                ++emailCount;
                                if (!isDryRun()) {
                                    ownNotf.update(noAvg, session, false);
                                    // SET LastNotification
                                }
                                email.log(ps,logMessage.toString());
                            } else {
                                trans.error().log(rv.errorString());
                            }
                        }
                    }
                }
            } catch (OrganizationException e) {
                trans.info().log(e);
            }
        }
        trans.info().printf("%d emails sent for %s", emailCount,batchEnv);
    }
    
    private static class LastCred {
        public Cred cred; 
        public Date last;
        
        public LastCred(Cred cred, Date last) {
            this.cred = cred;
            this.last = last;
        }
        
        // Reverse Sort (Oldest on top)
        public static Comparator<LastCred> COMPARE = new Comparator<LastCred>() {
            @Override
            public int compare(LastCred o1, LastCred o2) {
                return o2.last.compareTo(o1.last);
            }
        };
        
        public String toString() {
            return Chrono.dateTime(last) + cred.toString();
        }
    }
    
    @Override
    protected void _close(AuthzTrans trans) {
        session.close();
        ps.close();
    }
}
