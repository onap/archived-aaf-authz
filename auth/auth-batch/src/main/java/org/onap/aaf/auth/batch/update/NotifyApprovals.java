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

package org.onap.aaf.auth.batch.update;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map.Entry;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.BatchPrincipal;
import org.onap.aaf.auth.batch.actions.Email;
import org.onap.aaf.auth.batch.actions.EmailPrint;
import org.onap.aaf.auth.batch.actions.Message;
import org.onap.aaf.auth.batch.helpers.Approval;
import org.onap.aaf.auth.batch.helpers.Future;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.HistoryDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.OrganizationFactory;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.config.RegistrationPropHolder;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Chrono;

public class NotifyApprovals extends Batch {
    private static final String LINE = "----------------------------------------------------------------";
    private final HistoryDAO historyDAO;
    private final ApprovalDAO apprDAO;
    private final FutureDAO futureDAO;
    private Email email;
    private int maxEmails;
    private final PrintStream ps;
    private final AuthzTrans noAvg;

    public NotifyApprovals(AuthzTrans trans) throws APIException, IOException, OrganizationException, CadiException {
        super(trans.env());
        Access access = trans.env().access();
        RegistrationPropHolder rph = new RegistrationPropHolder(access, 0);
        String guiURL = rph.replacements(access.getProperty(GUI_URL,"https://%P/gui"),"","");
        noAvg = env.newTransNoAvg();
        noAvg.setUser(new BatchPrincipal("batch:NotifyApprovals"));

        historyDAO = new HistoryDAO(trans, cluster, CassAccess.KEYSPACE);
        session = historyDAO.getSession(trans);
        apprDAO = new ApprovalDAO(trans, historyDAO);
        futureDAO = new FutureDAO(trans, historyDAO);
        if (isDryRun()) {
            email = new EmailPrint();
            maxEmails=3;
        } else {
            email = new Email();
            maxEmails = Integer.parseInt(trans.getProperty("MAX_EMAILS","3"));
        }
        email.subject("AAF Approval Notification (ENV: %s)",batchEnv);
        email.preamble("AAF is the ONAP Authorization System." +
                "\n  Your approval is required, which you may enter on the following page:"
                + "\n\n\t%s/approve\n\n"
                ,guiURL);
        email.signature("Sincerely,\nAAF Team\n");

        Approval.load(trans, session, Approval.v2_0_17);
        Future.load(trans, session, Future.v2_0_17); // Skip the Construct Data
        
        ps = new PrintStream(new FileOutputStream(logDir() + "/email"+Chrono.dateOnlyStamp()+".log",true));
        ps.printf("### Approval Notify %s for %s%s\n",Chrono.dateTime(),batchEnv,dryRun?", DryRun":"");
    }

    @Override
    protected void run(AuthzTrans trans) {
        GregorianCalendar gc = new GregorianCalendar();
        Date now = gc.getTime();
        String today = Chrono.dateOnlyStamp(now);
        gc.add(GregorianCalendar.MONTH, -1);
        gc=null;


        Message msg = new Message();
        int emailCount = 0;
        List<Approval> pending = new ArrayList<>();
        boolean isOwner,isSupervisor;
        for (Entry<String, List<Approval>> es : Approval.byApprover.entrySet()) {
            isOwner = isSupervisor = false;
            String approver = es.getKey();
            if (approver.indexOf('@')<0) {
                approver += org.getRealm();
            }
            Date latestNotify=null, soonestExpire=null;
            GregorianCalendar latest=new GregorianCalendar();
            GregorianCalendar soonest=new GregorianCalendar();
            pending.clear();
            
            for (Approval app : es.getValue()) {
                Future f = app.getTicket()==null?null:Future.data.get(app.getTicket());
                if (f==null) { // only Ticketed Approvals are valid.. the others are records.
                    // Approvals without Tickets are no longer valid. 
                    if ("pending".equals(app.getStatus())) {
                        app.setStatus("lapsed");
                        app.update(noAvg,apprDAO,dryRun); // obeys dryRun
                    }
                } else {
                    if ((soonestExpire==null && f.expires()!=null) || (soonestExpire!=null && f.expires()!=null && soonestExpire.before(f.expires()))) {
                        soonestExpire=f.expires();
                    }

                    if ("pending".equals(app.getStatus())) {
                        if (!isOwner) {
                            isOwner = "owner".equals(app.getType());
                        }
                        if (!isSupervisor) {
                            isSupervisor = "supervisor".equals(app.getType());
                        }

                        if ((latestNotify==null && app.getLast_notified()!=null) ||(latestNotify!=null && app.getLast_notified()!=null && latestNotify.before(app.getLast_notified()))) {
                            latestNotify=app.getLast_notified();
                        }
                        pending.add(app);
                    }
                }
            }

            if (!pending.isEmpty()) {
                boolean go = false;
                if (latestNotify==null) { // never notified... make it so
                    go=true;
                } else {
                    if (!today.equals(Chrono.dateOnlyStamp(latest))) { // already notified today
                        latest.setTime(latestNotify);
                        soonest.setTime(soonestExpire);
                        int year;
                        int days = soonest.get(GregorianCalendar.DAY_OF_YEAR)-latest.get(GregorianCalendar.DAY_OF_YEAR);
                        days+=((year=soonest.get(GregorianCalendar.YEAR))-latest.get(GregorianCalendar.YEAR))*365 + 
                                (soonest.isLeapYear(year)?1:0);
                        if (days<7) { // If Expirations get within a Week (or expired), notify everytime.
                            go = true;
                        }
                    }
                }
                if (go) {
                    if (maxEmails>emailCount++) {
                        try {
                            Organization org = OrganizationFactory.obtain(env, approver);
                            Identity user = org.getIdentity(noAvg, approver);
                            if (user==null) {
                                ps.printf("Invalid Identity: %s\n", approver);
                            } else {
                                email.clear();
                                msg.clear();
                                email.addTo(user.email());
                                msg.line(LINE);
                                msg.line("Why are you receiving this Notification?\n");
                                if (isSupervisor) {
                                    msg.line("%sYou are the supervisor of one or more employees who need access to tools which are protected by AAF.  " + 
                                             "Your employees may ask for access to various tools and applications to do their jobs.  ASPR requires "
                                             + "that you are notified and approve their requests. The details of each need is provided when you click "
                                             + "on webpage above.\n",isOwner?"1) ":"");
                                    msg.line("Your participation in this process fulfills the ASPR requirement to re-authorize users in roles on a regular basis.\n\n");
                                }
                            
                                if (isOwner) {
                                    msg.line("%sYou are the listed owner of one or more AAF Namespaces. ASPR requires that those responsible for "
                                            + "applications and their access review them regularly for accuracy.  The AAF WIKI page for AT&T is https://wiki.web.att.com/display/aaf.  "
                                            + "More info regarding questions of being a Namespace Owner is available at https://wiki.web.att.com/pages/viewpage.action?pageId=594741363\n",isSupervisor?"2) ":"");
                                    msg.line("Additionally, Credentials attached to the Namespace must be renewed regularly.  While you may delegate certain functions to " + 
                                             "Administrators within your Namespace, you are ultimately responsible to make sure credentials do not expire.\n");
                                    msg.line("You may view the Namespaces you listed as Owner for in this AAF Env by viewing the following webpage:\n");
                                    msg.line("   %s/ns\n\n",env.getProperty(GUI_URL));
                                
                                }
                                msg.line("  If you are unfamiliar with AAF, you might like to peruse the following links:"
                                        + "\n\thttps://wiki.web.att.com/display/aaf/AAF+in+a+Nutshell"
                                        + "\n\thttps://wiki.web.att.com/display/aaf/The+New+Person%%27s+Guide+to+AAF");
                                msg.line("\n  SPECIAL NOTE about SWM Management Groups: Understand that SWM management Groups correlate one-to-one to AAF Namespaces. "
                                        + "(SWM uses AAF for the Authorization piece of Management Groups).  You may be assigned the SWM Management Group by asking "
                                        + "directly, or through any of the above stated automated processes.  Auto-generated Namespaces typically look like 'com.att.44444.PROD' "
                                        + "where '44444' is a MOTS ID, and 'PROD' is PROD|DEV|TEST, etc.  For your convenience, the MOTS link is http://ebiz.sbc.com/mots.\n");
                                msg.line("  Finally, realize that there are automated processes which create Machines and Resources via SWM, Kubernetes or other "
                                        + "such tooling.  If you or your predecessor requested them, you were set as the owner of the AAF Namespace created during "
                                        + "that process.\n");
                                msg.line("  For ALL QUESTIONS of why and how of SWM, and whether you or your reports can be removed, please contact SWM at "
                                        + "https://wiki.web.att.com/display/swm/Support\n");

                                email.msg(msg);
                                email.exec(noAvg, org,"");
                                if (!isDryRun()) {
                                    email.log(ps,"NotifyApprovals");
                                    for (Approval app : pending) {
                                        app.setLastNotified(now);
                                        app.update(noAvg, apprDAO, dryRun);
                                    }
                                }
                            }
                        } catch (OrganizationException e) {
                            trans.info().log(e);
                        }
                    }
                }
            }
        }
        trans.info().printf("%d emails sent for %s", emailCount,batchEnv);
    }
    
    @Override
    protected void _close(AuthzTrans trans) {
        futureDAO.close(trans);
        apprDAO.close(trans);
        historyDAO.close(trans);
        ps.close();
    }
}
