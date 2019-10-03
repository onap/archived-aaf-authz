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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.approvalsets.Pending;
import org.onap.aaf.auth.batch.approvalsets.Ticket;
import org.onap.aaf.auth.batch.helpers.Approval;
import org.onap.aaf.auth.batch.helpers.Cred;
import org.onap.aaf.auth.batch.helpers.Cred.Instance;
import org.onap.aaf.auth.batch.helpers.ExpireRange;
import org.onap.aaf.auth.batch.helpers.ExpireRange.Range;
import org.onap.aaf.auth.batch.helpers.Future;
import org.onap.aaf.auth.batch.helpers.LastNotified;
import org.onap.aaf.auth.batch.helpers.Role;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.batch.helpers.X509;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.configure.Factory;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Chrono;


public class Analyze extends Batch {
    private static final int unknown=0;
    private static final int owner=1;
    private static final int supervisor=2;
    private static final int total=0;
    private static final int pending=1;
    private static final int approved=2;
    
    
    public static final String NEED_APPROVALS = "NeedApprovals";
    private static final String EXTEND = "Extend";
    private static final String EXPIRED_OWNERS = "ExpiredOwners";
    private static final String CSV = ".csv";
    private static final String INFO = "info";
    private int minOwners;
    private Map<String, CSV.Writer> writerList;
    private ExpireRange expireRange;
    private Date deleteDate;
    private CSV.Writer deleteCW;
    private CSV.Writer needApproveCW;
    private CSV.Writer extendCW;
    private Range futureRange;
    private final String sdate;
    private LastNotified ln;
    
    public Analyze(AuthzTrans trans) throws APIException, IOException, OrganizationException {
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
            

            minOwners=1;

            // Create Intermediate Output 
            writerList = new HashMap<>();
            
            expireRange = new ExpireRange(trans.env().access());
            sdate = Chrono.dateOnlyStamp(now);
            for( List<Range> lr : expireRange.ranges.values()) {
                for(Range r : lr ) {
                    if(writerList.get(r.name())==null) {
                        File file = new File(logDir(),r.name() + sdate +CSV);
                        CSV csv = new CSV(env.access(),file);
                        CSV.Writer cw = csv.writer(false);
                        cw.row(INFO,r.name(),sdate,r.reportingLevel());
                        writerList.put(r.name(),cw);
                        if("Delete".equals(r.name())) {
                            deleteDate = r.getEnd();
                            deleteCW = cw;
                        }
                        trans.init().log("Creating File:",file.getAbsolutePath());
                    }
                }
            }
            
            // Setup New Approvals file
            futureRange = expireRange.newFutureRange();
            File file = new File(logDir(),NEED_APPROVALS + sdate +CSV);
            CSV approveCSV = new CSV(env.access(),file);
            needApproveCW = approveCSV.writer();
            needApproveCW.row(INFO,NEED_APPROVALS,sdate,1);
            writerList.put(NEED_APPROVALS,needApproveCW);
            
            // Setup Extend Approvals file
            file = new File(logDir(),EXTEND + sdate +CSV);
            CSV extendCSV = new CSV(env.access(),file);
            extendCW = extendCSV.writer();
            extendCW.row(INFO,EXTEND,sdate,1);
            writerList.put(EXTEND,extendCW);
            
            // Load full data of the following
            ln = new LastNotified(session);

        } finally {
            tt0.done();
        }
    }

    @Override
    protected void run(AuthzTrans trans) {
        TimeTaken tt;
        AuthzTrans noAvg = trans.env().newTransNoAvg();
        
        ////////////////////
        // Load all Notifieds, and either add to local Data, or mark for Deletion.
        ln.loadAll(noAvg,expireRange.approveDelete,deleteCW);
        
        // Hold Good Tickets to keyed User/Role for UserRole Step
        Map<String,Ticket> mur = new TreeMap<>();

        try {
            Approval.load(trans, session, Approval.v2_0_17);
    
            ////////////////////
            final Map<UUID,Ticket> goodTickets = new TreeMap<>();
            tt = trans.start("Analyze Expired Futures",Trans.SUB);
            try {
                Future.load(noAvg, session, Future.withConstruct, fut -> {
                    List<Approval> appls = Approval.byTicket.get(fut.id());
                    if(!futureRange.inRange(fut.expires())) {
                        deleteCW.comment("Future %s expired", fut.id());
                        Future.row(deleteCW,fut);
                        if(appls!=null) {
                            for(Approval a : appls) {
                                Approval.row(deleteCW, a);
                            }
                        }
                    } else if(appls==null) { // Orphaned Future (no Approvals)
                        deleteCW.comment("Future is Orphaned");
                        Future.row(deleteCW,fut);
                    } else  {
                        goodTickets.put(fut.fdd.id, new Ticket(fut));
                    }
                });
            } finally {
                tt.done();
            }
            
            Set<String> approvers = new TreeSet<>();
            tt = trans.start("Connect Approvals with Futures",Trans.SUB);
            try {
                for(Approval appr : Approval.list) {
                    Ticket ticket=null;
                    UUID ticketID = appr.getTicket();
                    if(ticketID!=null) {
                        ticket = goodTickets.get(appr.getTicket());
                    }
                    if(ticket == null) { // Orphaned Approvals, no Futures
                        deleteCW.comment("Approval is Orphaned");
                        Approval.row(deleteCW, appr);
                    } else {
                        // for users and approvers still valid
                        String user = appr.getUser();
                        
                        if(org.isRevoked(noAvg, appr.getApprover())) {
                            deleteCW.comment("Approver ID is revoked");
                            Approval.row(deleteCW, appr);
                        } else if(user!=null && !user.isEmpty() && org.isRevoked(noAvg, user)) {
                            deleteCW.comment("USER ID is revoked");
                            Approval.row(deleteCW, appr);
                        } else {
                            ticket.approvals.add(appr); // add to found Ticket
                            approvers.add(appr.getApprover());
                        }
                    }
                }
            } finally {
                tt.done();
            }
    
            /* Run through all Futures, and see if 
             * 1) they have been executed (no longer valid)
             * 2) The current Approvals indicate they can proceed 
             */
            Map<String,Pending> pendingApprs = new HashMap<>();
            Map<String,Pending> pendingTemp = new HashMap<>();
    
            String approver;
            
            tt = trans.start("Analyze Good Tickets",Trans.SUB);
            try {
                for(Ticket ticket : goodTickets.values()) {
                    try {
                        pendingTemp.clear();
                        switch(ticket.f.target()) {
                            case "user_role":
                                int state[][] = new int[3][3];
                                int type;
                                        
                                for(Approval appr : ticket.approvals) {
                                    switch(appr.getType()) {
                                        case "owner":
                                            type=owner;
                                            break;
                                        case "supervisor":
                                            type=supervisor;
                                            break;
                                        default:
                                            type=0;
                                    }
                                    ++state[type][total]; // count per type
                                    switch(appr.getStatus()) {
                                        case "pending":
                                            ++state[type][pending];
                                            approver = appr.getApprover();
                                            Pending n = pendingTemp.get(approver);
                                            if(n==null) {
                                                Date lastNotified = ln.lastNotified(approver,"pending",null);
                                                pendingTemp.put(approver,new Pending(lastNotified));
                                            } else {
                                                n.inc();
                                            }
                                            break;
                                        case "approved":
                                            ++state[type][approved];
                                            break;
                                        default:
                                            ++state[type][unknown];
                                    }
                                }
                                
                                // To Approve:
                                // Always must have at least 1 owner
                                if((state[owner][total]>0 && state[owner][approved]>0) &&
                                    // If there are no Supervisors, that's ok
                                    (state[supervisor][total]==0 || 
                                    // But if there is a Supervisor, they must have approved 
                                    (state[supervisor][approved]>0))) {
                                        UserRoleDAO.Data urdd = new UserRoleDAO.Data();
                                        try {
                                            urdd.reconstitute(ticket.f.fdd.construct);
                                            if(urdd.expires.before(ticket.f.expires())) {
                                                extendCW.row("extend_ur",urdd.user,urdd.role,ticket.f.expires());
                                            }
                                        } catch (IOException e) {
                                            trans.error().log("Could not reconstitute UserRole");
                                        }
                                } else { // Load all the Pending.
                                    for(Entry<String, Pending> es : pendingTemp.entrySet()) {
                                        Pending p = pendingApprs.get(es.getKey());
                                        if(p==null) {
                                            pendingApprs.put(es.getKey(), es.getValue());
                                        } else {
                                            p.inc(es.getValue());
                                        }
                                    }
                                }
                                break;
                        }
                    } finally {
                        if("user_role".equals(ticket.f.fdd.target)) {
                            String key = ticket.f.fdd.target_key; 
                            if(key!=null) {
                                mur.put(key, ticket);
                            }
                        }
                    }
                }
            } finally {
                tt.done();
            }
            // Good Tickets no longer needed
            goodTickets.clear();
    
            /**
             * Decide to Notify about Approvals, based on activity/last Notified
             */
            tt = trans.start("Analyze Approval Reminders", Trans.SUB);
            try {
                GregorianCalendar gc = new GregorianCalendar();
                gc.add(GregorianCalendar.DAY_OF_WEEK, 5);
                Date remind = gc.getTime();
                
                for(Entry<String, Pending> es : pendingApprs.entrySet()) {
                    Pending p = es.getValue();
                    if(p.newApprovals() 
                            || p.earliest() == LastNotified.NEVER // yes, equals. 
                            || p.earliest().after(remind)) {
                        p.row(needApproveCW,es.getKey());
                    }
                }
            } finally {
                tt.done();
            }
            
            // clear out Approval Intermediates
            pendingTemp = null;
            pendingApprs = null;
        } finally {
        }
            
        /**
           Run through User Roles.  
           Owners are treated specially in next section.
           Regular roles are checked against Date Ranges.  If match Date Range, write out to appropriate file.
        */    
        
        try {
            Role.load(trans, session);
    
            try {
                tt = trans.start("Analyze UserRoles, storing Owners",Trans.SUB);
                Set<String> specialCommented = new HashSet<>();
                Map<String, Set<UserRole>> owners = new TreeMap<>();
                 try {
                    UserRole.load(noAvg, session, UserRole.v2_0_11, ur -> {
                        Identity identity;
                        try {
                            identity = trans.org().getIdentity(noAvg,ur.user());
                            if(identity==null) {
                                // Candidate for Delete, but not Users if Special
                                String id = ur.user();
                                for(String s : specialDomains) {
                                    if(id.endsWith(s)) {
                                        if(!specialCommented.contains(id)) {
                                            deleteCW.comment("ID %s is part of special Domain %s (UR Org Check)", id,s);
                                            specialCommented.add(id);
                                        }
                                        return;
                                    }
                                }
                                if(specialNames.contains(id)) {
                                    if(!specialCommented.contains(id)) {
                                        deleteCW.comment("ID %s is a special ID  (UR Org Check)", id);
                                        specialCommented.add(id);
                                    }
                                    return;
                                }
                                ur.row(deleteCW, UserRole.UR,"Not in Organization");
                                return;
                            } else if(Role.byName.get(ur.role())==null) {
                                ur.row(deleteCW, UserRole.UR,String.format("Role %s does not exist", ur.role()));
                                return;
                            }
                            // Just let expired UserRoles sit until deleted
                            if(futureRange.inRange(ur.expires())&&(!mur.containsKey(ur.user() + '|' + ur.role()))) {    
                                    // Cannot just delete owners, unless there is at least one left. Process later
                                    if ("owner".equals(ur.rname())) {
                                        Set<UserRole> urs = owners.get(ur.role());
                                        if (urs == null) {
                                            urs = new HashSet<UserRole>();
                                            owners.put(ur.role(), urs);
                                        }
                                        urs.add(ur);
                                    } else {
                                        Range r = writeAnalysis(noAvg,ur);
                                        if(r!=null) {
                                            Approval existing = findApproval(ur);
                                            if(existing==null) {
                                                ur.row(needApproveCW,UserRole.APPROVE_UR);
                                            }
                                        }
                                    }
                             }
                        } catch (OrganizationException e) {
                            noAvg.error().log(e);
                        }
                    });
                 } finally {
                     tt.done();
                 }
                 mur.clear();
                 
                /**
                  Now Process Owners, one owner Role at a time, ensuring one is left,
                  preferably a good one. If so, process the others as normal. 
                  
                  Otherwise, write to ExpiredOwners Report
                */
                 tt = trans.start("Analyze Owners Separately",Trans.SUB);
                 try {
                    if (!owners.values().isEmpty()) {
                        File file = new File(logDir(), EXPIRED_OWNERS + sdate + CSV);
                        final CSV ownerCSV = new CSV(env.access(),file);
                        CSV.Writer expOwner = ownerCSV.writer();
                        expOwner.row(INFO,EXPIRED_OWNERS,sdate,2);
    
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
                                        Range r = writeAnalysis(noAvg, ur);
                                        if(r!=null) {
                                            Approval existing = findApproval(ur);
                                            if(existing==null) {
                                                ur.row(needApproveCW,UserRole.APPROVE_UR);
                                            }
                                        }
                                    } else {
                                        expOwner.row("owner",ur.role(), ur.user(), Chrono.dateOnlyStamp(ur.expires()));
                                        Approval existing = findApproval(ur);
                                        if(existing==null) {
                                            ur.row(needApproveCW,UserRole.APPROVE_UR);
                                        }
                                    }
                                }
                            }
                        } finally {
                            if(expOwner!=null) {
                                expOwner.close();
                            }
                        }
                    }
                 } finally {
                     tt.done();
                 }
            } finally {
                Role.clear();
                UserRole.clear();
            }
            
            /**
             * Check for Expired Credentials
             */
            try {
                 // Load Cred.  We don't follow Visitor, because we have to gather up everything into Identity Anyway
                 Cred.load(trans, session);
    
                tt = trans.start("Analyze Expired Credentials",Trans.SUB);
                try {
                    for (Cred cred : Cred.data.values()) {
                        List<Instance> linst = cred.instances;
                        if(linst!=null) {
                            Instance lastBath = null;
                            for(Instance inst : linst) {
                                // All Creds go through Life Cycle
                                if(deleteDate!=null && inst.expires.before(deleteDate)) {
                                    writeAnalysis(noAvg, cred, inst); // will go to Delete
                                // Basic Auth has Pre-EOL notifications IF there is no Newer Credential
                                } else if (inst.type == CredDAO.BASIC_AUTH || inst.type == CredDAO.BASIC_AUTH_SHA256) {
                                    if(lastBath==null || lastBath.expires.before(inst.expires)) {
                                        lastBath = inst;
                                    }
                                }
                            }
                            if(lastBath!=null) {
                                writeAnalysis(noAvg, cred, lastBath);
                            }
                        }
                    }
                } finally {
                    tt.done();
                }
            } finally {
                Cred.clear();
            }
    
            ////////////////////
            tt = trans.start("Analyze Expired X509s",Trans.SUB);
            try {
                X509.load(noAvg, session, x509 -> {
                    try {
                        for(Certificate cert : Factory.toX509Certificate(x509.x509)) {
                            writeAnalysis(noAvg, x509, (X509Certificate)cert);
                        }
                    } catch (CertificateException | IOException e) {
                        noAvg.error().log(e, "Error Decrypting X509");
                    }
                });
            } finally {
                tt.done();
            }
        } catch (FileNotFoundException e) {
            noAvg.info().log(e);
        }
    }
 
    private Approval findApproval(UserRole ur) {
        Approval existing = null;
        List<Approval> apprs = Approval.byUser.get(ur.user());
        if(apprs!=null) {
            for(Approval appr : apprs) {
                if(ur.role().equals(appr.getRole()) &&
                    appr.getMemo().contains(Chrono.dateOnlyStamp(ur.expires()))) {
                        existing = appr; 
                }
            }
        }
        return existing;
    }

    private Range writeAnalysis(AuthzTrans noAvg, UserRole ur) {
        Range r = expireRange.getRange("ur", ur.expires());
        if(r!=null) {
            Date lnd = ln.lastNotified(LastNotified.newKey(ur));
            // Note: lnd is NEVER null
            Identity i;
            try {
                i = org.getIdentity(noAvg, ur.user());
            } catch (OrganizationException e) {
                i=null;
            }
            if(r.needsContact(lnd,i)) {                
                CSV.Writer cw = writerList.get(r.name());
                if(cw!=null) {
                    ur.row(cw,UserRole.UR);
                }
            }
        }
        return r;
    }
    
    private void writeAnalysis(AuthzTrans noAvg, Cred cred, Instance inst) {
        if(cred!=null && inst!=null) {
            Range r = expireRange.getRange("cred", inst.expires);
            if(r!=null) {
                Date lnd = ln.lastNotified(LastNotified.newKey(cred,inst));
                // Note: lnd is NEVER null
                Identity i;
                try {
                    i = org.getIdentity(noAvg, cred.id);
                } catch (OrganizationException e) {
                    i=null;
                }
                if(r.needsContact(lnd,i)) {                
                    CSV.Writer cw = writerList.get(r.name());
                    if(cw!=null) {
                        cred.row(cw,inst);
                    }
                }
            }
        }
    }

    private void writeAnalysis(AuthzTrans noAvg, X509 x509, X509Certificate x509Cert) throws IOException {
        Range r = expireRange.getRange("x509", x509Cert.getNotAfter());
        if(r!=null) {
            Date lnd = ln.lastNotified(LastNotified.newKey(x509,x509Cert));
            // Note: lnd is NEVER null
            Identity i;
            try {
                i = org.getIdentity(noAvg, x509.id);
            } catch (OrganizationException e) {
                i=null;
            }
            if(r.needsContact(lnd,i)) {
                CSV.Writer cw = writerList.get(r.name());
                if(cw!=null) {
                    x509.row(cw,x509Cert);
                }
            }
        }
    }
    
    @Override
    protected void _close(AuthzTrans trans) {
        session.close();
        for(CSV.Writer cw : writerList.values()) {
            cw.close();
        }
    }

}
