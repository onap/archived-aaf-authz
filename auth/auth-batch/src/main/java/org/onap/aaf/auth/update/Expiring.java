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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import org.onap.aaf.auth.Batch;
import org.onap.aaf.auth.BatchPrincipal;
import org.onap.aaf.auth.actions.Action;
import org.onap.aaf.auth.actions.ActionDAO;
import org.onap.aaf.auth.actions.CacheTouch;
import org.onap.aaf.auth.actions.CredDelete;
import org.onap.aaf.auth.actions.CredPrint;
import org.onap.aaf.auth.actions.Email;
import org.onap.aaf.auth.actions.Message;
import org.onap.aaf.auth.actions.URDelete;
import org.onap.aaf.auth.actions.URFutureApprove;
import org.onap.aaf.auth.actions.URFutureApproveExec;
import org.onap.aaf.auth.actions.URPrint;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.hl.Function.FUTURE_OP;
import org.onap.aaf.auth.dao.hl.Function.OP_STATUS;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.helpers.Approval;
import org.onap.aaf.auth.helpers.Cred;
import org.onap.aaf.auth.helpers.Future;
import org.onap.aaf.auth.helpers.NS;
import org.onap.aaf.auth.helpers.Role;
import org.onap.aaf.auth.helpers.UserRole;
import org.onap.aaf.auth.helpers.Cred.Instance;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;

public class Expiring extends Batch {
    private CredPrint crPrint;
    private URFutureApprove urFutureApprove;
    private URFutureApproveExec urFutureApproveExec;
    private CredDelete crDelete;
    private URDelete urDelete;
    private final CacheTouch cacheTouch;
    private final AuthzTrans noAvg;
    private final ApprovalDAO apprDAO;
    private final FutureDAO futureDAO;
    private final PrintStream urDeleteF,urRecoverF;
    private final URPrint urPrint;
    private Email email;
    private File deletesFile;

    public Expiring(AuthzTrans trans) throws APIException, IOException, OrganizationException {
        super(trans.env());
        trans.info().log("Starting Connection Process");
        
        noAvg = env.newTransNoAvg();
        noAvg.setUser(new BatchPrincipal("batch:Expiring"));
        
        TimeTaken tt0 = trans.start("Cassandra Initialization", Env.SUB);
        try {
            crPrint = new CredPrint("Expired:");

            TimeTaken tt = trans.start("Connect to Cluster with DAOs", Env.REMOTE);
            try {
                urFutureApprove = new URFutureApprove(trans, cluster,isDryRun());
                checkOrganizationAcccess(trans, urFutureApprove.question());
                urFutureApproveExec = new URFutureApproveExec(trans, urFutureApprove);
                urPrint = new URPrint("User Roles:");
                crDelete = new CredDelete(trans, urFutureApprove);
                urDelete = new URDelete(trans,urFutureApprove);
                cacheTouch = new CacheTouch(trans, urFutureApprove);
                
                // Reusing... don't destroy
                apprDAO = urFutureApprove.question().approvalDAO;
                futureDAO = urFutureApprove.question().futureDAO;

                TimeTaken tt2 = trans.start("Connect to Cluster", Env.REMOTE);
                try {
                    session = urFutureApprove.getSession(trans);
                } finally {
                    tt2.done();
                }
            } finally {
                tt.done();
            }
            
            File data_dir = new File(env.getProperty("aaf_data_dir"));
            if (!data_dir.exists() || !data_dir.canWrite() || !data_dir.canRead()) {
                throw new IOException("Cannot read/write to Data Directory "+ data_dir.getCanonicalPath() + ": EXITING!!!");
            }
            UserRole.setDeleteStream(
                urDeleteF = new PrintStream(new FileOutputStream(deletesFile = new File(data_dir,"UserRoleDeletes.dat"),false)));
            UserRole.setRecoverStream(
                urRecoverF = new PrintStream(new FileOutputStream(new File(data_dir,"UserRoleRecover.dat"),false)));
            UserRole.load(trans, session, UserRole.v2_0_11);
            
            Cred.load(trans, session);
            NS.load(trans, session,NS.v2_0_11);
            Future.load(trans,session,Future.withConstruct);
            Approval.load(trans,session,Approval.v2_0_17);
            Role.load(trans, session);
            
            email = new Email();
            email.subject("AAF Expiring Process Alert (ENV: %s)",batchEnv);
            email.preamble("Expiring Process Alert for %s",batchEnv);
            email.signature("Sincerely,\nAAF Expiring Batch Process\n");
            String address = env.getProperty("ALERT_TO_ADDRESS");
            if (address==null) {
                throw new APIException("ALERT_TO_ADDRESS property is required");
            }
            email.addTo(address);
    
        } catch (OrganizationException e) {
            throw new APIException("Error getting valid Organization",e);
        } finally {
            tt0.done();
        }
    }

    @Override
    protected void run(AuthzTrans trans) {
        // Setup Date boundaries
        
        final GregorianCalendar gc = new GregorianCalendar();
        final Date now = gc.getTime();
        
        gc.add(GregorianCalendar.MONTH, 1);
        Date future = gc.getTime();
//        Date earliest = null;

        // reset
        gc.setTime(now);
        gc.add(GregorianCalendar.DAY_OF_MONTH, -7); // save Expired Roles for 7 days.
        Date tooLate = gc.getTime();
        
        TimeTaken tt;
       
        // Clean out Approvals UserRoles are fixed up.
        String memo;
        for (List<Approval> la : Approval.byUser.values()) {
            for (Approval a : la ) {
                memo = a.getMemo();
                if (memo!=null && (memo.contains("Re-Approval") || memo.contains("Re-Validate"))) {
                        String role = a.getRole();
                        if (role!=null) {
                        UserRole ur = UserRole.get(a.getUser(), a.getRole());
                        Future f=null;
                        if (ur!=null) {
                            if (ur.expires().after(future)) { // no need for Approval anymore
                                a.delayDelete(noAvg, apprDAO, dryRun, "User Role already Extended");
                                UUID tkt = a.getTicket();
                                if (tkt!=null && Future.data.containsKey(tkt)) {
                                    f = Future.data.get(a.getTicket());
                                }
                            }
                        } else {
                            a.delayDelete(noAvg, apprDAO, dryRun, "User Role does not exist");
                            UUID tkt = a.getTicket();
                            if (tkt !=null && Future.data.containsKey(tkt)) {
                                f = Future.data.get(a.getTicket());
                            }
                        }
                        if (f!=null) {
                            f.delayedDelete(noAvg, futureDAO, dryRun, "Approvals removed");
                        }
                        }
                }
            }
        }
        try {
                trans.info().log("### Removed",Future.sizeForDeletion(),"Future and",Approval.sizeForDeletion(),"Approvals");
                Future.resetLocalData();
            Approval.resetLocalData();
            } catch (Exception t) {
                t.printStackTrace();
            }
    
        // Run for Expired Futures
        trans.info().log("Checking for Expired Approval/Futures");
        tt = trans.start("Delete old Futures", Env.REMOTE);
        trans.info().log("### Running Future Execution on ",Future.data.size(), "Items");
        // Execute any Futures waiting
            for (Future f : Future.data.values()) {
                if (f.memo().contains("Re-Approval") || f.memo().contains("Re-Validate")) {
                    List<Approval> la = Approval.byTicket.get(f.id());
                    if (la!=null) {
                        Result<OP_STATUS> ruf = urFutureApproveExec.exec(noAvg,la,f);
                        if (ruf.isOK()) {
                            switch(ruf.value) {
                                case P:
                                    break;
                                case E:
                                case D:
                                case L:
                                    f.delayedDelete(noAvg, futureDAO, dryRun,OP_STATUS.L.desc());
                                    Approval.delayDelete(noAvg, apprDAO, dryRun, la,OP_STATUS.L.desc());
                                    break;
                            }
                        }
                    }
                }
            }
            try {
                trans.info().log("### Removed",Future.sizeForDeletion(),"Future and",Approval.sizeForDeletion(),"Approvals");
                Future.resetLocalData();
            Approval.resetLocalData();
            } catch (Exception t) {
                t.printStackTrace();
            }

        
        trans.info().log("### Remove Expired on ",Future.data.size(), "Items, or premature ones");
        // Remove Expired
        String expiredBeforeNow = "Expired before " + tooLate;
        String expiredAfterFuture = "Expired after " + future;
        try {
                for (Future f : Future.data.values()) {
                    if (f.expires().before(tooLate)) {
                        f.delayedDelete(noAvg,futureDAO,dryRun, expiredBeforeNow);
                        Approval.delayDelete(noAvg, apprDAO, dryRun, Approval.byTicket.get(f.id()), expiredBeforeNow);
                    } else if (f.expires().after(future)) {
                        f.delayedDelete(noAvg,futureDAO,dryRun, expiredAfterFuture);
                        Approval.delayDelete(noAvg,apprDAO,dryRun, Approval.byTicket.get(f.id()), expiredAfterFuture);
                    }
                }
                try {
                    trans.info().log("### Removed",Future.sizeForDeletion(),"Future and",Approval.sizeForDeletion(),"Approvals");
                    Future.resetLocalData();
                Approval.resetLocalData();
                } catch (Exception t) {
                    t.printStackTrace();
                }
        } finally {
                tt.done();    
        }
        
        trans.info().log("### Checking Approvals valid (",Approval.byApprover.size(),"Items)");
        // Make sure users of Approvals are still valid
        for (List<Approval> lapp : Approval.byTicket.values()) {
                for (Approval app : lapp) {
                    Future f;
                    if (app.getTicket()==null) {
                        f = null;
                    } else {
                        f = Future.data.get(app.getTicket());
                        if (Future.pendingDelete(f)) {
                            f=null;
                        }
                    }
                    String msg;
                    if (f!=null && app.getRole()!=null && Role.byName.get(app.getRole())==null) {
                        f.delayedDelete(noAvg,futureDAO,dryRun,msg="Role '" + app.getRole() + "' no longer exists");
                        Approval.delayDelete(noAvg,apprDAO,dryRun, Approval.byTicket.get(f.id()), msg);
                        continue;
                    }
                    
                    switch(app.getStatus()) {
                        case "pending":
                            if (f==null) {
                            app.delayDelete(noAvg,apprDAO, isDryRun(), "ticketDeleted");
                            continue;
                            }
                            switch(app.getType()) {
                                case "owner":
                                boolean anOwner=false;
                                    String approle = app.getRole();
                                    if (approle!=null) {
                                        Role role = Role.byName.get(approle);
                                        if (role==null) {
                                        app.delayDelete(noAvg, apprDAO, dryRun, "Role No Longer Exists");
                                        continue;
                                    } else {
                                        // Make sure Owner Role exists
                                        String owner = role.ns + ".owner";
                                        if (Role.byName.containsKey(owner)) {
                                                List<UserRole> lur = UserRole.getByRole().get(owner);
                                                if (lur != null) {
                                                    for (UserRole ur : lur) {
                                                        if (ur.user().equals(app.getApprover())) {
                                                            anOwner = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                        }
                                        }
                                        if (!anOwner) {
                                        app.delayDelete(noAvg, apprDAO, dryRun, "No longer Owner");
                                        }
    
                                    }
                                    break;
                                case "supervisor":
                                try {
                                    Identity identity = org.getIdentity(noAvg, app.getUser());
                                    if (identity==null) {
                                        if (f!=null) {
                                                f.delayedDelete(noAvg,futureDAO,dryRun,msg = app.getUser() + " is no longer associated with " + org.getName());
                                                Approval.delayDelete(noAvg,apprDAO,dryRun, Approval.byTicket.get(f.id()), msg);
                                        }
                                    } else {
                                        if (!app.getApprover().equals(identity.responsibleTo().fullID())) {
                                            if (f!=null) {
                                                    f.delayedDelete(noAvg,futureDAO,dryRun,msg = app.getApprover() + " is no longer a Supervisor of " + app.getUser());
                                                    Approval.delayDelete(noAvg,apprDAO,dryRun, Approval.byTicket.get(f.id()), msg);
                                            }
                                        }
                                    }
                                } catch (OrganizationException e) {
                                    e.printStackTrace();
                                }
                                break;
                            }
                            break;
                    }
                }
        }
            try {
                trans.info().log("### Removed",Future.sizeForDeletion(),"Future and",Approval.sizeForDeletion(),"Approvals");
                Future.resetLocalData();
            Approval.resetLocalData();
            } catch (Exception t) {
                t.printStackTrace();
            }
        
        int count = 0, deleted=0, delayedURDeletes = 0;

        // Run for User Roles
        trans.info().log("Checking for Expired User Roles");
        try {
                for (UserRole ur : UserRole.getData()) {
                    if (org.getIdentity(noAvg, ur.user())==null) {  // if not part of Organization;
                        if (isSpecial(ur.user())) {
                            trans.info().log(ur.user(),"is not part of organization, but may not be deleted");
                        } else {
                            ur.delayDelete(noAvg, "Not Part of Organization", dryRun);
                            ++deleted;
                            ++delayedURDeletes;
                        }
                    } else {
                        if (NS.data.get(ur.ns())==null) {
                            ur.delayDelete(noAvg,"Namespace " + ur.ns() + " does not exist.",dryRun);
                            ++delayedURDeletes;
                            ++deleted;
                        } else if (!Role.byName.containsKey(ur.role())) {
                            ur.delayDelete(noAvg,"Role " + ur.role() + " does not exist.",dryRun);
                            ++deleted;
                            ++delayedURDeletes;
                        } else if (ur.expires().before(tooLate)) {
                            if ("owner".equals(ur.rname())) { // don't delete Owners, even if Expired
                                urPrint.exec(noAvg,ur,"Owner Expired (but not deleted)");
                            } else {
                                // In this case, when UR is expired, not dependent on other lookups, we delete straight out.
                                urDelete.exec(noAvg, ur,"Expired before " + tooLate);
                                ++deleted;
                            }
                            //trans.logAuditTrail(trans.info());
                        } else if (ur.expires().before(future) && ur.expires().after(now)) {
                            ++count;
                            // Is there an Approval set already
                            boolean needNew = true;
                            if (ur.role()!=null && ur.user()!=null) {
                                List<Approval> abm = Approval.byUser.get(ur.user());
                                if (abm!=null) {
                                    for (Approval a : abm) {
                                        if (a.getOperation().equals(FUTURE_OP.A.name()) && ur.role().equals(a.getRole())) {
                                            if (Future.data.get(a.getTicket())!=null) {
                                                needNew = false;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            if (needNew) {
                                urFutureApprove.exec(noAvg, ur,"");
                            }
                        }
                    }
                }
        } catch (OrganizationException e) {
            env.info().log(e,"Exiting ...");
        } finally {
                env.info().log("Found",count,"user roles expiring before",future);
                env.info().log("deleting",deleted,"user roles expiring before",tooLate);
        }
        
        // Actualize UR Deletes, or send Email
        if (UserRole.sizeForDeletion()>0) {
                count+=UserRole.sizeForDeletion();
            double onePercent = 0.01;
            if (((double)UserRole.sizeForDeletion())/UserRole.getData().size() > onePercent) {
                    Message msg = new Message();
                    try {
                    msg.line("Found %d of %d UserRoles marked for Deletion in file %s", 
                        delayedURDeletes,UserRole.getData().size(),deletesFile.getCanonicalPath());
                } catch (IOException e) {
                    msg.line("Found %d of %d UserRoles marked for Deletion.\n", 
                            delayedURDeletes);
                }
                    msg.line("Review the File.  If data is ok, Use ExpiringP2 BatchProcess to complete the deletions");
                    
                    email.msg(msg);
                    email.exec(trans, org, "Email Support");
            } else {
                    urDeleteF.flush();
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(deletesFile));
                        try {
                            ExpiringP2.deleteURs(noAvg, br, urDelete, null /* don't touch Cache here*/);
                        } finally {
                            br.close();
                        }
                    } catch (IOException io) {
                        noAvg.error().log(io);
                    }
            }
        }
        if (count>0) {
                String str = String.format("%d UserRoles modified or deleted", count);
                cacheTouch.exec(trans, "user_role", str);
        }
        
        // Run for Creds
        trans.info().log("Checking for Expired Credentials");
        System.out.flush();
        count = 0;
        try {
                CredDAO.Data crd = new CredDAO.Data();
                Date last = null;
                for ( Cred creds : Cred.data.values()) {
                crd.id = creds.id;
                    for (int type : creds.types()) {
                    crd.type = type;
                        for ( Instance inst : creds.instances) {
                            if (inst.expires.before(tooLate)) {
                                crd.expires = inst.expires;
                                crDelete.exec(noAvg, crd,"Expired before " + tooLate);
                            } else if (last==null || inst.expires.after(last)) {
                                last = inst.expires;
                            }
                        }
                        if (last!=null) {
                            if (last.before(future)) {
                                crd.expires = last;
                                crPrint.exec(noAvg, crd,"");
                                ++count;
                            }
                        }
                    }
                }
        } finally {
                String str = String.format("Found %d current creds expiring before %s", count, Chrono.dateOnlyStamp(future));
                if (count>0) {
                    cacheTouch.exec(trans, "cred", str);
                }
        }
        
    }
    
    @Override
    protected void _close(AuthzTrans trans) {
        aspr.info("End " + this.getClass().getSimpleName() + " processing" );
        for (Action<?,?,?> action : new Action<?,?,?>[] {crDelete}) {
            if (action instanceof ActionDAO) {
                ((ActionDAO<?,?,?>)action).close(trans);
            }
        }
        session.close();
        urDeleteF.close();
        urRecoverF.close();
    }

}
