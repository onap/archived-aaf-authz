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

package org.onap.aaf.auth.dao.hl;

import static org.onap.aaf.auth.layer.Result.OK;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.DAOException;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.DelegateDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.Namespace;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.NsDAO.Data;
import org.onap.aaf.auth.dao.cass.NsSplit;
import org.onap.aaf.auth.dao.cass.NsType;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.hl.Question.Access;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTrans.REQD_TYPE;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Executor;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Expiration;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.Organization.Policy;
import org.onap.aaf.auth.org.OrganizationException;

public class Function {

    private static final String CANNOT_BE_THE_OWNER_OF_A_NAMESPACE = "%s(%s) cannot be the owner of the namespace '%s'. Owners %s.";

    public enum FUTURE_OP {
        C("Create"),U("Update"),D("Delete"),G("Grant"),UG("UnGrant"),A("Approval");

        private String desc;

        private FUTURE_OP(String desc) {
            this.desc = desc;
        }

        public String desc() {
            return desc;
        }

        /**
         *  Same as valueOf(), but passes back null instead of throwing Exception
         * @param value
         * @return
         */
        public static FUTURE_OP toFO(String value) {
            if (value!=null) {
                for (FUTURE_OP fo : values()) {
                    if (fo.name().equals(value)){
                        return fo;
                    }
                }
            }
            return null;
        }
    }

    public enum OP_STATUS {
        E("Executed"),D("Denied"),P("Pending"),L("Lapsed");

        private String desc;
        public final static Result<OP_STATUS> RE = Result.ok(OP_STATUS.E);
        public final static Result<OP_STATUS> RD = Result.ok(OP_STATUS.D);
        public final static Result<OP_STATUS> RP = Result.ok(OP_STATUS.P);
        public final static Result<OP_STATUS> RL = Result.ok(OP_STATUS.L);

        private OP_STATUS(String desc) {
            this.desc = desc;
        }

        public String desc() {
            return desc;
        }

    }

    public static final String FOP_CRED = "cred";
    public static final String FOP_DELEGATE = "delegate";
    public static final String FOP_NS = "ns";
    public static final String FOP_PERM = "perm";
    public static final String FOP_ROLE = "role";
    public static final String FOP_USER_ROLE = "user_role";
    private static final List<Identity> NO_ADDL_APPROVE = new ArrayList<>();
    private static final String ROOT_NS = Define.ROOT_NS();
    // First Action should ALWAYS be "write", see "CreateRole"
    public final Question q;

    public Function(AuthzTrans trans, Question question) {
        q = question;
    }

    private class ErrBuilder {
        private StringBuilder sb;
        private List<String> ao;

        public void log(Result<?> result) {
            if (result.notOK()) {
                if (sb == null) {
                    sb = new StringBuilder();
                    ao = new ArrayList<>();
                }
                sb.append(String.format(result.details,result.variables));
                sb.append('\n');
            }
        }

        public String[] vars() {
            String[] rv = new String[ao.size()];
            ao.toArray(rv);
            return rv;
        }

        public boolean hasErr() {
            return sb != null;
        }

        @Override
        public String toString() {
            return sb == null ? "" : String.format(sb.toString(), ao);
        }
    }

    /**
     * createNS
     *
     * Create Namespace
     *
     * @param trans
     * @param org
     * @param ns
     * @param user
     * @return
     * @throws DAOException
     *
     *             To create an NS, you need to: 1) validate permission to
     *             modify parent NS 2) Does NS exist already? 3) Create NS with
     *             a) "user" as owner. NOTE: Per 10-15 request for AAF 1.0 4)
     *             Loop through Roles with Parent NS, and map any that start
     *             with this NS into this one 5) Loop through Perms with Parent
     *             NS, and map any that start with this NS into this one
     */
    public Result<Void> createNS(AuthzTrans trans, Namespace namespace, boolean fromApproval) {
        Result<?> rq;

        try {
            for (String u : namespace.owner) {
                Organization org = trans.org();
                Identity orgUser = org.getIdentity(trans, u);
                String reason;
                if (orgUser == null) {
                    return Result.err(Status.ERR_Policy,"%s is not a valid user at %s",u,org.getName());
                } else if ((reason=orgUser.mayOwn())!=null) {
                    if (org.isTestEnv()) {
                        String reason2;
                        if ((reason2=org.validate(trans, Policy.AS_RESPONSIBLE,new CassExecutor(trans, this), u))!=null) { // can masquerade as responsible
                            trans.debug().log(reason2);
                            return Result.err(Status.ERR_Policy,CANNOT_BE_THE_OWNER_OF_A_NAMESPACE,orgUser.fullName(),orgUser.id(),namespace.name,reason);
                        }
                        // a null means ok
                    } else {
                        if (orgUser.isFound()) {
                            return Result.err(Status.ERR_Policy,CANNOT_BE_THE_OWNER_OF_A_NAMESPACE,orgUser.fullName(),orgUser.id(),namespace.name, reason);
                        } else {
                            return Result.err(Status.ERR_Policy,u + " is an invalid Identity");
                        }
                    }
                }
            }
        } catch (Exception e) {
            trans.error().log(e,
                    "Could not contact Organization for User Validation");
        }

        String user = trans.user();
        // 1) May Change Parent?
        int idx = namespace.name.lastIndexOf('.');
        String parent;
        if (idx < 0) {
            if (!q.isGranted(trans, user, ROOT_NS,Question.NS, ".", "create")) {
                return Result.err(Result.ERR_Security,
                        "%s may not create Root Namespaces", user);
            }
            parent = null;
            fromApproval = true;
        } else {
            parent = namespace.name.substring(0, idx); // get Parent String
        }

        Result<NsDAO.Data> rparent = q.deriveNs(trans, parent);
        if (rparent.notOK()) {
            return Result.err(rparent);
        }
        parent = rparent.value.parent;
        if (!fromApproval) {
            rparent = q.mayUser(trans, user, rparent.value, Access.write);
            if (rparent.notOK()) {
                return Result.err(rparent);
            }
        }
        parent = namespace.parent = rparent.value.name; // Correct Namespace from real data
        String cname = parent.length()<1 || namespace.name.equals(parent)?null:namespace.name.substring(parent.length()+1);

        // 2) Does requested NS exist
        if (q.nsDAO().read(trans, namespace.name).isOKhasData()) {
            return Result.err(Status.ERR_ConflictAlreadyExists,
                    "Target Namespace already exists");
        }

        // 2.1) Does role exist with that name
        if(cname!=null && q.roleDAO().read(trans, parent, cname).isOKhasData()) {
            return Result.err(Status.ERR_ConflictAlreadyExists,
                    "Role exists with that name");
        }

        // 2.2) Do perms exist with that name
        if(cname!=null && q.permDAO().readByType(trans, parent, cname).isOKhasData()) {
            return Result.err(Status.ERR_ConflictAlreadyExists,
                    "Perms exist with that name");
        }

        // Someone must be responsible.
        if (namespace.owner == null || namespace.owner.isEmpty()) {
            return Result
                    .err(Status.ERR_Policy,
                            "Namespaces must be assigned at least one responsible party");
        }

        // 3) Create NS
        Date now = new Date();

        Result<Void> r;
        // 3a) Admin

        try {
            // Originally, added the enterer as Admin, but that's not necessary,
            // or helpful for Operations folks..
            // Admins can be empty, because they can be changed by lower level
            // NSs
            if (namespace.admin != null) {
                for (String u : namespace.admin) {
                    if ((r = checkValidID(trans, now, u)).notOK()) {
                        return r;
                    }
                }
            }

            // 3b) Responsible
            Organization org = trans.org();
            for (String u : namespace.owner) {
                Identity orgUser = org.getIdentity(trans, u);
                if (orgUser == null) {
                    return Result
                            .err(Status.ERR_BadData,
                                    "NS must be created with an %s approved Responsible Party",
                                    org.getName());
                }
            }
        } catch (Exception e) {
            return Result.err(Status.ERR_UserNotFound, e.getMessage());
        }

        // VALIDATIONS done... Add NS
        rq = q.nsDAO().create(trans, namespace.data());
        if (rq.notOK()) {
            return Result.err(rq);
        }

        // Since Namespace is now created, we need to grab all subsequent errors
        ErrBuilder eb = new ErrBuilder();

        // Add UserRole(s)
        UserRoleDAO.Data urdd = new UserRoleDAO.Data();
        urdd.expires = trans.org().expiration(null, Expiration.UserInRole).getTime();
        urdd.role(namespace.name, Question.ADMIN);
        for (String admin : namespace.admin) {
            urdd.user = admin;
            eb.log(q.userRoleDAO().create(trans, urdd));
        }
        urdd.role(namespace.name,Question.OWNER);
        for (String owner : namespace.owner) {
            urdd.user = owner;
            eb.log(q.userRoleDAO().create(trans, urdd));
        }

        addNSAdminRolesPerms(trans, eb, namespace.name);

        addNSOwnerRolesPerms(trans, eb, namespace.name);

        if (parent != null) {
            // Build up with any errors

            String targetNs = rparent.value.name; // Get the Parent Namespace,
                                                    // not target
            String targetName = namespace.name.substring(targetNs.length() + 1); // Remove the Parent Namespace from the
                                    // Target + a dot, and you'll get the name
            int targetNameDot = targetName.length() + 1;

            // 4) Change any roles with children matching this NS, and
            Result<List<RoleDAO.Data>> rrdc = q.roleDAO().readChildren(trans,    targetNs, targetName);
            if (rrdc.isOKhasData()) {
                for (RoleDAO.Data rdd : rrdc.value) {
                    // Remove old Role from Perms, save them off
                    List<PermDAO.Data> lpdd = new ArrayList<>();
                    for (String p : rdd.perms(false)) {
                        Result<PermDAO.Data> rpdd = PermDAO.Data.decode(trans,q,p);
                        if (rpdd.isOKhasData()) {
                            PermDAO.Data pdd = rpdd.value;
                            lpdd.add(pdd);
                            q.permDAO().delRole(trans, pdd, rdd);
                        } else{
                            trans.error().log(rpdd.errorString());
                        }
                    }

                    // Save off Old keys
                    String delP1 = rdd.ns;
                    String delP2 = rdd.name;

                    // Write in new key
                    rdd.ns = namespace.name;
                    rdd.name = (delP2.length() > targetNameDot) ? delP2
                            .substring(targetNameDot) : "";

                    // Need to use non-cached, because switching namespaces, not
                    // "create" per se
                    if ((rq = q.roleDAO().create(trans, rdd)).isOK()) {
                        // Put Role back into Perm, with correct info
                        for (PermDAO.Data pdd : lpdd) {
                            q.permDAO().addRole(trans, pdd, rdd);
                        }
                        // Change data for User Roles
                        Result<List<UserRoleDAO.Data>> rurd = q.userRoleDAO().readByRole(trans, rdd.fullName());
                        if (rurd.isOKhasData()) {
                            for (UserRoleDAO.Data urd : rurd.value) {
                                urd.ns = rdd.ns;
                                urd.rname = rdd.name;
                                q.userRoleDAO().update(trans, urd);
                            }
                        }
                        // Now delete old one
                        rdd.ns = delP1;
                        rdd.name = delP2;
                        if ((rq = q.roleDAO().delete(trans, rdd, false)).notOK()) {
                            eb.log(rq);
                        }
                    } else {
                        eb.log(rq);
                    }
                }
            }

            // 4) Change any Permissions with children matching this NS, and
            Result<List<PermDAO.Data>> rpdc = q.permDAO().readChildren(trans,targetNs, targetName);
            if (rpdc.isOKhasData()) {
                for (PermDAO.Data pdd : rpdc.value) {
                    // Remove old Perm from Roles, save them off
                    List<RoleDAO.Data> lrdd = new ArrayList<>();

                    for (String rl : pdd.roles(false)) {
                        Result<RoleDAO.Data> rrdd = RoleDAO.Data.decode(trans,q,rl);
                        if (rrdd.isOKhasData()) {
                            RoleDAO.Data rdd = rrdd.value;
                            lrdd.add(rdd);
                            q.roleDAO().delPerm(trans, rdd, pdd);
                        } else{
                            trans.error().log(rrdd.errorString());
                        }
                    }

                    // Save off Old keys
                    String delP1 = pdd.ns;
                    String delP2 = pdd.type;
                    pdd.ns = namespace.name;
                    pdd.type = (delP2.length() > targetNameDot) ? delP2
                            .substring(targetNameDot) : "";
                    if ((rq = q.permDAO().create(trans, pdd)).isOK()) {
                        // Put Role back into Perm, with correct info
                        for (RoleDAO.Data rdd : lrdd) {
                            q.roleDAO().addPerm(trans, rdd, pdd);
                        }

                        pdd.ns = delP1;
                        pdd.type = delP2;
                        if ((rq = q.permDAO().delete(trans, pdd, false)).notOK()) {
                            eb.log(rq);
                            // Need to invalidate directly, because we're
                            // switching places in NS, not normal cache behavior
                        }
                    } else {
                        eb.log(rq);
                    }
                }
            }
            if (eb.hasErr()) {
                return Result.err(Status.ERR_ActionNotCompleted,eb.sb.toString(), (Object[])eb.vars());
            }
        }
        return Result.ok();
    }

    private void addNSAdminRolesPerms(AuthzTrans trans, ErrBuilder eb, String ns) {
        // Admin Role/Perm
        RoleDAO.Data rd = new RoleDAO.Data();
        rd.ns = ns;
        rd.name = "admin";
        rd.description = "AAF Namespace Administrators";

        PermDAO.Data pd = new PermDAO.Data();
        pd.ns = ns;
        pd.type = "access";
        pd.instance = Question.ASTERIX;
        pd.action = Question.ASTERIX;
        pd.description = "AAF Namespace Write Access";

        rd.perms = new HashSet<>();
        rd.perms.add(pd.encode());
        eb.log(q.roleDAO().create(trans, rd));

        pd.roles = new HashSet<>();
        pd.roles.add(rd.encode());
        eb.log(q.permDAO().create(trans, pd));
    }

    private void addNSOwnerRolesPerms(AuthzTrans trans, ErrBuilder eb, String ns) {
        RoleDAO.Data rd = new RoleDAO.Data();
        rd.ns = ns;
        rd.name = "owner";
        rd.description = "AAF Namespace Owners";

        PermDAO.Data pd = new PermDAO.Data();
        pd.ns = ns;
        pd.type = "access";
        pd.instance = Question.ASTERIX;
        pd.action = Question.READ;
        pd.description = "AAF Namespace Read Access";

        rd.perms = new HashSet<>();
        rd.perms.add(pd.encode());
        eb.log(q.roleDAO().create(trans, rd));

        pd.roles = new HashSet<>();
        pd.roles.add(rd.encode());
        eb.log(q.permDAO().create(trans, pd));
    }

    /**
     * deleteNS
     *
     * Delete Namespace
     *
     * @param trans
     * @param org
     * @param ns
     * @param force
     * @param user
     * @return
     * @throws DAOException
     *
     *
     *             To delete an NS, you need to: 1) validate permission to
     *             modify this NS 2) Find all Roles with this NS, and 2a) if
     *             Force, delete them, else modify to Parent NS 3) Find all
     *             Perms with this NS, and modify to Parent NS 3a) if Force,
     *             delete them, else modify to Parent NS 4) Find all IDs
     *             associated to this NS, and deny if exists. 5) Remove NS
     */
    public Result<Void> deleteNS(AuthzTrans trans, String ns) {
        boolean force = trans.requested(REQD_TYPE.force);
        boolean move = trans.requested(REQD_TYPE.move);
        // 1) Validate
        Result<List<NsDAO.Data>> nsl;
        if ((nsl = q.nsDAO().read(trans, ns)).notOKorIsEmpty()) {
            return Result.err(Status.ERR_NsNotFound, "%s does not exist", ns);
        }
        NsDAO.Data nsd = nsl.value.get(0);
        NsType nt;
        if (move && !q.canMove(nt = NsType.fromType(nsd.type))) {
            return Result.err(Status.ERR_Denied, "Namespace Force=move not permitted for Type %s",nt.name());
        }

        Result<NsDAO.Data> dnr = q.mayUser(trans, trans.user(), nsd, Access.write);
        if (dnr.status != Status.OK) {
            return Result.err(dnr);
        }

        // 2) Find Parent
        String user = trans.user();
        int idx = ns.lastIndexOf('.');
        NsDAO.Data parent;
        if (idx < 0) {
            if (!q.isGranted(trans, user, ROOT_NS,Question.NS, ".", "delete")) {
                return Result.err(Result.ERR_Security,
                        "%s may not delete Root Namespaces", user);
            }
            parent = null;
        } else {
            Result<NsDAO.Data> rlparent = q.deriveNs(trans,    ns.substring(0, idx));
            if (rlparent.notOKorIsEmpty()) {
                return Result.err(rlparent);
            }
            parent = rlparent.value;
        }

        // Build up with any errors
        // If sb != null below is an indication of error
        StringBuilder sb = null;
        ErrBuilder er = new ErrBuilder();

        // 2a) Deny if any IDs on Namespace
        Result<List<CredDAO.Data>> creds = q.credDAO().readNS(trans, ns);
        if (creds.isOKhasData()) {
            if (force || move) {
                for (CredDAO.Data cd : creds.value) {
                    er.log(q.credDAO().delete(trans, cd, false));
                    // Since we're deleting all the creds, we should delete all
                    // the user Roles for that Cred
                    Result<List<UserRoleDAO.Data>> rlurd = q.userRoleDAO()
                            .readByUser(trans, cd.id);
                    if (rlurd.isOK()) {
                        for (UserRoleDAO.Data data : rlurd.value) {
                            q.userRoleDAO().delete(trans, data, false);
                        }
                    }

                }
            } else {
                // first possible StringBuilder Create.
                sb = new StringBuilder();
                sb.append('[');
                sb.append(ns);
                sb.append("] contains users");
            }
        }

        // 2b) Find (or delete if forced flag is set) dependencies
        // First, find if NS Perms are the only ones
        Result<List<PermDAO.Data>> rpdc = q.permDAO().readNS(trans, ns);
        if (rpdc.isOKhasData()) {
            // Since there are now NS perms, we have to count NON-NS perms.
            // FYI, if we delete them now, and the NS is not deleted, it is in
            // an inconsistent state.
            boolean nonaccess = false;
            for (PermDAO.Data pdd : rpdc.value) {
                if (!"access".equals(pdd.type)) {
                    nonaccess = true;
                    break;
                }
            }
            if (nonaccess && !force && !move) {
                if (sb == null) {
                    sb = new StringBuilder();
                    sb.append('[');
                    sb.append(ns);
                    sb.append("] contains ");
                } else {
                    sb.append(", ");
                }
                sb.append("permissions");
            }
        }

        Result<List<RoleDAO.Data>> rrdc = q.roleDAO().readNS(trans, ns);
        if (rrdc.isOKhasData()) {
            // Since there are now NS roles, we have to count NON-NS roles.
            // FYI, if we delete th)em now, and the NS is not deleted, it is in
            // an inconsistent state.
            int count = rrdc.value.size();
            for (RoleDAO.Data rdd : rrdc.value) {
                if ("admin".equals(rdd.name) || "owner".equals(rdd.name)) {
                    --count;
                }
            }
            if (count > 0 && !force && !move) {
                if (sb == null) {
                    sb = new StringBuilder();
                    sb.append('[');
                    sb.append(ns);
                    sb.append("] contains ");
                } else {
                    sb.append(", ");
                }
                sb.append("roles");
            }
        }

        // 2c) Deny if dependencies exist that would be moved to root level
        // parent is root level parent here. Need to find closest parent ns that
        // exists
        if (sb != null) {
            if (!force && !move) {
                sb.append(".\n  Delete dependencies and try again.  Note: using \"force=true\" will delete all. \"force=move\" will delete Creds, but move Roles and Perms to parent.");
                return Result.err(Status.ERR_DependencyExists, sb.toString());
            }

            if (move && (parent == null || parent.type == NsType.COMPANY.type)) {
                return Result
                        .err(Status.ERR_DependencyExists,
                                "Cannot move users, roles or permissions to [%s].\nDelete dependencies and try again",
                                parent.name);
            }
        } else if (move && parent != null) {
            sb = new StringBuilder();
            // 3) Change any roles with children matching this NS, and
            moveRoles(trans, parent, sb, rrdc);
            // 4) Change any Perms with children matching this NS, and
            movePerms(trans, parent, sb, rpdc);
        }

        if (sb != null && sb.length() > 0) {
            return Result.err(Status.ERR_DependencyExists, sb.toString());
        }

        if (er.hasErr()) {
            if (trans.debug().isLoggable()) {
                trans.debug().log(er.toString());
            }
            return Result.err(Status.ERR_DependencyExists,
                    "Namespace members cannot be deleted for %s", ns);
        }

        // 5) OK... good to go for NS Deletion...
        if (!rpdc.isEmpty()) {
            for (PermDAO.Data perm : rpdc.value) {
                deletePerm(trans, perm, true, true);
            }
        }
        if (!rrdc.isEmpty()) {
            for (RoleDAO.Data role : rrdc.value) {
                deleteRole(trans, role, true, true);
            }
        }

        return q.nsDAO().delete(trans, nsd, false);
    }

    public Result<List<String>> getOwners(AuthzTrans trans, String ns,
            boolean includeExpired) {
        return getUsersByRole(trans, ns + Question.DOT_OWNER, includeExpired);
    }

    private Result<Void> mayAddOwner(AuthzTrans trans, String ns, String id) {
        Result<NsDAO.Data> rq = q.deriveNs(trans, ns);
        if (rq.notOK()) {
            return Result.err(rq);
        }

        rq = q.mayUser(trans, trans.user(), rq.value, Access.write);
        if (rq.notOK()) {
            return Result.err(rq);
        }

        Identity user;
        Organization org = trans.org();
        try {
            if ((user = org.getIdentity(trans, id)) == null) {
                return Result.err(Status.ERR_Policy,
                        "%s reports that this is not a valid credential",
                        org.getName());
            }
            String reason;
            if ((reason=user.mayOwn())==null) {
                return Result.ok();
            } else {
                if (org.isTestEnv()) {
                    String reason2;
                    if ((reason2 = org.validate(trans, Policy.AS_RESPONSIBLE, new CassExecutor(trans, this), id))==null) {
                        return Result.ok();
                    } else {
                        trans.debug().log(reason2);
                    }
                }
                return Result.err(Status.ERR_Policy,CANNOT_BE_THE_OWNER_OF_A_NAMESPACE,user.fullName(),user.id(),ns, reason);
            }
        } catch (Exception e) {
            return Result.err(e);
        }
    }

    private Result<Void> mayAddAdmin(AuthzTrans trans, String ns,    String id) {
        // Does NS Exist?
        Result<Void> r = checkValidID(trans, new Date(), id);
        if (r.notOK()) {
            return r;
        }
        // Is id able to be an Admin
        Result<NsDAO.Data> rq = q.deriveNs(trans, ns);
        if (rq.notOK()) {
            return Result.err(rq);
        }

        rq = q.mayUser(trans, trans.user(), rq.value, Access.write);
        if (rq.notOK()) {
            Result<List<UserRoleDAO.Data>> ruinr = q.userRoleDAO().readUserInRole(trans, trans.user(),ns+".owner");
            if (!(ruinr.isOKhasData() && ruinr.value.get(0).expires.after(new Date()))) {
                return Result.err(rq);
            }
        }
        return r;
    }

    private Result<Void> checkValidID(AuthzTrans trans, Date now, String user) {
        Organization org = trans.org();
        if (org.supportsRealm(user)) {
            try {
                if (org.getIdentity(trans, user) == null) {
                    return Result.err(Status.ERR_Denied,
                            "%s reports that %s is an invalid ID", org.getName(),
                            user);
                }
                return Result.ok();
            } catch (Exception e) {
                return Result.err(Result.ERR_Security,
                        "%s is not a valid %s Credential", user, org.getName());
            }
        //TODO find out how to make sure good ALTERNATE OAUTH DOMAIN USER
//        } else if (user.endsWith(ALTERNATE OAUTH DOMAIN)) {
//            return Result.ok();
        } else {
            Result<List<CredDAO.Data>> cdr = q.credDAO().readID(trans, user);
            if (cdr.notOKorIsEmpty()) {
                return Result.err(Status.ERR_Security,
                        "%s is not a valid AAF Credential", user);
            }

            for (CredDAO.Data cd : cdr.value) {
                if (cd.expires.after(now)) {
                    return Result.ok();
                }
            }
        }
        return Result.err(Result.ERR_Security, "%s has expired", user);
    }

    public Result<Void> delOwner(AuthzTrans trans, String ns, String id) {
        Result<NsDAO.Data> rq = q.deriveNs(trans, ns);
        if (rq.notOK()) {
            return Result.err(rq);
        }

        rq = q.mayUser(trans, trans.user(), rq.value, Access.write);
        if (rq.notOK()) {
            return Result.err(rq);
        }

        return delUserRole(trans, id, ns,Question.OWNER);
    }

    public Result<List<String>> getAdmins(AuthzTrans trans, String ns, boolean includeExpired) {
        return getUsersByRole(trans, ns + Question.DOT_ADMIN, includeExpired);
    }

    public Result<Void> delAdmin(AuthzTrans trans, String ns, String id) {
        Result<NsDAO.Data> rq = q.deriveNs(trans, ns);
        if (rq.notOK()) {
            return Result.err(rq);
        }

        rq = q.mayUser(trans, trans.user(), rq.value, Access.write);
        if (rq.notOK()) {
            // Even though not a "writer", Owners still determine who gets to be an Admin
            Result<List<UserRoleDAO.Data>> ruinr = q.userRoleDAO().readUserInRole(trans, trans.user(),ns+".owner");
            if (!(ruinr.isOKhasData() && ruinr.value.get(0).expires.after(new Date()))) {
                return Result.err(rq);
            }
        }

        return delUserRole(trans, id, ns, Question.ADMIN);
    }

    /**
     * Helper function that moves permissions from a namespace being deleted to
     * its parent namespace
     *
     * @param trans
     * @param parent
     * @param sb
     * @param rpdc
     *            - list of permissions in namespace being deleted
     */
    private void movePerms(AuthzTrans trans, NsDAO.Data parent,
            StringBuilder sb, Result<List<PermDAO.Data>> rpdc) {

        Result<Void> rv;
        Result<PermDAO.Data> pd;

        if (rpdc.isOKhasData()) {
            for (PermDAO.Data pdd : rpdc.value) {
                String delP2 = pdd.type;
                if ("access".equals(delP2)) {
                    continue;
                }
                // Remove old Perm from Roles, save them off
                List<RoleDAO.Data> lrdd = new ArrayList<>();

                for (String rl : pdd.roles(false)) {
                    Result<RoleDAO.Data> rrdd = RoleDAO.Data.decode(trans,q,rl);
                    if (rrdd.isOKhasData()) {
                        RoleDAO.Data rdd = rrdd.value;
                        lrdd.add(rdd);
                        q.roleDAO().delPerm(trans, rdd, pdd);
                    } else{
                        trans.error().log(rrdd.errorString());
                    }
                }

                // Save off Old keys
                String delP1 = pdd.ns;
                NsSplit nss = new NsSplit(parent, pdd.fullType());
                pdd.ns = nss.ns;
                pdd.type = nss.name;
                // Use direct Create/Delete, because switching namespaces
                if ((pd = q.permDAO().create(trans, pdd)).isOK()) {
                    // Put Role back into Perm, with correct info
                    for (RoleDAO.Data rdd : lrdd) {
                        q.roleDAO().addPerm(trans, rdd, pdd);
                    }

                    pdd.ns = delP1;
                    pdd.type = delP2;
                    if ((rv = q.permDAO().delete(trans, pdd, false)).notOK()) {
                        sb.append(rv.details);
                        sb.append('\n');
                        // } else {
                        // Need to invalidate directly, because we're switching
                        // places in NS, not normal cache behavior
                        // q.permDAO().invalidate(trans,pdd);
                    }
                } else {
                    sb.append(pd.details);
                    sb.append('\n');
                }
            }
        }
    }

    /**
     * Helper function that moves roles from a namespace being deleted to its
     * parent namespace
     *
     * @param trans
     * @param parent
     * @param sb
     * @param rrdc
     *            - list of roles in namespace being deleted
     */
    private void moveRoles(AuthzTrans trans, NsDAO.Data parent,
            StringBuilder sb, Result<List<RoleDAO.Data>> rrdc) {

        Result<Void> rv;
        Result<RoleDAO.Data> rd;

        if (rrdc.isOKhasData()) {
            for (RoleDAO.Data rdd : rrdc.value) {
                String delP2 = rdd.name;
                if ("admin".equals(delP2) || "owner".equals(delP2)) {
                    continue;
                }
                // Remove old Role from Perms, save them off
                List<PermDAO.Data> lpdd = new ArrayList<>();
                for (String p : rdd.perms(false)) {
                    Result<PermDAO.Data> rpdd = PermDAO.Data.decode(trans,q,p);
                    if (rpdd.isOKhasData()) {
                        PermDAO.Data pdd = rpdd.value;
                        lpdd.add(pdd);
                        q.permDAO().delRole(trans, pdd, rdd);
                    } else{
                        trans.error().log(rpdd.errorString());
                    }
                }

                // Save off Old keys
                String delP1 = rdd.ns;

                NsSplit nss = new NsSplit(parent, rdd.fullName());
                rdd.ns = nss.ns;
                rdd.name = nss.name;
                // Use direct Create/Delete, because switching namespaces
                if ((rd = q.roleDAO().create(trans, rdd)).isOK()) {
                    // Put Role back into Perm, with correct info
                    for (PermDAO.Data pdd : lpdd) {
                        q.permDAO().addRole(trans, pdd, rdd);
                    }

                    rdd.ns = delP1;
                    rdd.name = delP2;
                    if ((rv = q.roleDAO().delete(trans, rdd, true)).notOK()) {
                        sb.append(rv.details);
                        sb.append('\n');
                        // } else {
                        // Need to invalidate directly, because we're switching
                        // places in NS, not normal cache behavior
                        // q.roleDAO().invalidate(trans,rdd);
                    }
                } else {
                    sb.append(rd.details);
                    sb.append('\n');
                }
            }
        }
    }

    /**
     * Create Permission (and any missing Permission between this and Parent) if
     * we have permission
     *
     * Pass in the desired Management Permission for this Permission
     *
     * If Force is set, then Roles listed will be created, if allowed,
     * pre-granted.
     */
    public Result<Void> createPerm(AuthzTrans trans, PermDAO.Data perm, boolean fromApproval) {
        String user = trans.user();
        // Next, see if User is allowed to Manage Parent Permission

        Result<NsDAO.Data> rnsd;
        if (!fromApproval) {
            rnsd = q.mayUser(trans, user, perm, Access.write);
            if (rnsd.notOK()) {
                return Result.err(rnsd);
            }
        } else {
            q.deriveNs(trans, perm.ns);
        }

        // Does Child exist?
        if (!trans.requested(REQD_TYPE.force)) {
            if (q.permDAO().read(trans, perm).isOKhasData()) {
                return Result.err(Status.ERR_ConflictAlreadyExists,
                        "Permission [%s.%s|%s|%s] already exists.", perm.ns,
                        perm.type, perm.instance, perm.action);
            }
        }

        // Attempt to add perms to roles, creating as possible
        Set<String> roles;
        String pstring = perm.encode();

        // For each Role
        for (String role : roles = perm.roles(true)) {
            Result<RoleDAO.Data> rdd = RoleDAO.Data.decode(trans,q,role);
            if (rdd.isOKhasData()) {
                RoleDAO.Data rd = rdd.value;
                if (!fromApproval) {
                    // May User write to the Role in question.
                    Result<NsDAO.Data> rns = q.mayUser(trans, user, rd,
                            Access.write);
                    if (rns.notOK()) {
                        // Remove the role from Add, because
                        roles.remove(role); // Don't allow adding
                        trans.warn()
                                .log("User [%s] does not have permission to relate Permissions to Role [%s]",
                                        user, role);
                    }
                }

                Result<List<RoleDAO.Data>> rlrd;
                if ((rlrd = q.roleDAO().read(trans, rd)).notOKorIsEmpty()) {
                    rd.perms(true).add(pstring);
                    if (q.roleDAO().create(trans, rd).notOK()) {
                        roles.remove(role); // Role doesn't exist, and can't be
                                            // created
                    }
                } else {
                    rd = rlrd.value.get(0);
                    if (!rd.perms.contains(pstring)) {
                        q.roleDAO().addPerm(trans, rd, perm);
                    }
                }
            }
        }

        Result<PermDAO.Data> pdr = q.permDAO().create(trans, perm);
        if (pdr.isOK()) {
            return Result.ok();
        } else {
            return Result.err(pdr);
        }
    }

    public Result<Void> deletePerm(final AuthzTrans trans, final PermDAO.Data perm, boolean force, boolean fromApproval) {
        String user = trans.user();

        // Next, see if User is allowed to Manage Permission
        Result<NsDAO.Data> rnsd;
        if (!fromApproval) {
            rnsd = q.mayUser(trans, user, perm, Access.write);
            if (rnsd.notOK()) {
                return Result.err(rnsd);
            }
        }
        // Does Perm exist?
        Result<List<PermDAO.Data>> pdr = q.permDAO().read(trans, perm);
        if (pdr.notOKorIsEmpty()) {
            return Result.err(Status.ERR_PermissionNotFound,"Permission [%s.%s|%s|%s] does not exist.",
                    perm.ns,perm.type, perm.instance, perm.action);
        }
        // Get perm, but with rest of data.
        PermDAO.Data fullperm = pdr.value.get(0);

        // Attached to any Roles?
        if (fullperm.roles != null) {
            if (force || fullperm.roles.contains(user+":user")) {
                for (String role : fullperm.roles) {
                    Result<Void> rv = null;
                    Result<RoleDAO.Data> rrdd = RoleDAO.Data.decode(trans, q, role);
                    if (rrdd.isOKhasData()) {
                        trans.debug().log("Removing", role, "from", fullperm, "on Perm Delete");
                        if ((rv = q.roleDAO().delPerm(trans, rrdd.value, fullperm)).notOK()) {
                            if (rv.notOK()) {
                                trans.error().log("Error removing Role during delFromPermRole: ",
                                                trans.getUserPrincipal(),
                                                rv.errorString());
                            }
                        }
                    } else {
                        return Result.err(rrdd);
                    }
                }
            } else if (!fullperm.roles.isEmpty()) {
                return Result
                        .err(Status.ERR_DependencyExists,
                                "Permission [%s] cannot be deleted as it is attached to 1 or more roles.",
                                fullperm.fullPerm());
            }
        }

        return q.permDAO().delete(trans, fullperm, false);
    }

    public Result<Void> deleteRole(final AuthzTrans trans, final RoleDAO.Data role, boolean force, boolean fromApproval) {
        String user = trans.user();

        // Next, see if User is allowed to Manage Role
        Result<NsDAO.Data> rnsd;
        if (!fromApproval) {
            rnsd = q.mayUser(trans, user, role, Access.write);
            if (rnsd.notOK()) {
                return Result.err(rnsd);
            }
        }

        // Are there any Users Attached to Role?
        Result<List<UserRoleDAO.Data>> urdr = q.userRoleDAO().readByRole(trans,role.fullName());
        if (force) {
            if (urdr.isOKhasData()) {
                for (UserRoleDAO.Data urd : urdr.value) {
                    q.userRoleDAO().delete(trans, urd, false);
                }
            }
        } else if (urdr.isOKhasData()) {
            return Result.err(Status.ERR_DependencyExists,
                            "Role [%s.%s] cannot be deleted as it is used by 1 or more Users.",
                            role.ns, role.name);
        }

        // Does Role exist?
        Result<List<RoleDAO.Data>> rdr = q.roleDAO().read(trans, role);
        if (rdr.notOKorIsEmpty()) {
            return Result.err(Status.ERR_RoleNotFound,
                    "Role [%s.%s] does not exist", role.ns, role.name);
        }
        RoleDAO.Data fullrole = rdr.value.get(0); // full key search

        // Remove Self from Permissions... always, force or not.  Force only applies to Dependencies (Users)
        if (fullrole.perms != null) {
            for (String perm : fullrole.perms(false)) {
                Result<PermDAO.Data> rpd = PermDAO.Data.decode(trans,q,perm);
                if (rpd.isOK()) {
                    trans.debug().log("Removing", perm, "from", fullrole,"on Role Delete");

                    Result<?> r = q.permDAO().delRole(trans, rpd.value, fullrole);
                    if (r.notOK()) {
                        trans.error().log("ERR_FDR1 unable to remove",fullrole,"from",perm,':',r.status,'-',r.details);
                    }
                } else {
                    trans.error().log("ERR_FDR2 Could not remove",perm,"from",fullrole);
                }
            }
        }
        return q.roleDAO().delete(trans, fullrole, false);
    }

    /**
     * Only owner of Permission may add to Role
     *
     * If force set, however, Role will be created before Grant, if User is
     * allowed to create.
     *
     * @param trans
     * @param role
     * @param pd
     * @return
     */
    public Result<Void> addPermToRole(AuthzTrans trans, RoleDAO.Data role,PermDAO.Data pd, boolean fromApproval) {
        String user = trans.user();

        if (!fromApproval) {
            Result<NsDAO.Data> rRoleCo = q.deriveFirstNsForType(trans, role.ns, NsType.COMPANY);
            if (rRoleCo.notOK()) {
                return Result.err(rRoleCo);
            }
            Result<NsDAO.Data> rPermCo = q.deriveFirstNsForType(trans, pd.ns, NsType.COMPANY);
            if (rPermCo.notOK()) {
                return Result.err(rPermCo);
            }

            // Not from same company
            if (!rRoleCo.value.name.equals(rPermCo.value.name)) {
                Result<Data> r;
                // Only grant if User ALSO has Write ability in Other Company
                if ((r = q.mayUser(trans, user, role, Access.write)).notOK()) {
                    return Result.err(r);
                }
            }


            // Must be Perm Admin, or Granted Special Permission
            Result<NsDAO.Data> ucp = q.mayUser(trans, user, pd, Access.write);
            if (ucp.notOK()) {
                // Don't allow CLI potential Grantees to change their own AAF
                // Perms,
                if ((ROOT_NS.equals(pd.ns) && Question.NS.equals(pd.type))
                        || !q.isGranted(trans, trans.user(),ROOT_NS,Question.PERM, rPermCo.value.name, "grant")) {
                // Not otherwise granted
                // TODO Needed?
                    return Result.err(ucp);
                }
                // Final Check... Don't allow Grantees to add to Roles they are
                // part of
                Result<List<UserRoleDAO.Data>> rlurd = q.userRoleDAO()
                        .readByUser(trans, trans.user());
                if (rlurd.isOK()) {
                    for (UserRoleDAO.Data ur : rlurd.value) {
                        if (role.ns.equals(ur.ns) && role.name.equals(ur.rname)) {
                            return Result.err(ucp);
                        }
                    }
                }
            }
        }

        Result<List<PermDAO.Data>> rlpd = q.permDAO().read(trans, pd);
        if (rlpd.notOKorIsEmpty()) {
            return Result.err(Status.ERR_PermissionNotFound,
                    "Permission must exist to add to Role");
        }

        Result<List<RoleDAO.Data>> rlrd = q.roleDAO().read(trans, role); // Already
                                                                        // Checked
                                                                        // for
                                                                        // can
                                                                        // change
                                                                        // Role
        Result<Void> rv;

        if (rlrd.notOKorIsEmpty()) {
            if (trans.requested(REQD_TYPE.force)) {
                Result<NsDAO.Data> ucr = q.mayUser(trans, user, role,
                        Access.write);
                if (ucr.notOK()) {
                    return Result
                            .err(Status.ERR_Denied,
                                    "Role [%s.%s] does not exist. User [%s] cannot create.",
                                    role.ns, role.name, user);
                }

                role.perms(true).add(pd.encode());
                Result<RoleDAO.Data> rdd = q.roleDAO().create(trans, role);
                if (rdd.isOK()) {
                    rv = Result.ok();
                } else {
                    rv = Result.err(rdd);
                }
            } else {
                return Result.err(Status.ERR_RoleNotFound,
                        "Role [%s.%s] does not exist.", role.ns, role.name);
            }
        } else {
            role = rlrd.value.get(0);
            if (role.perms(false).contains(pd.encode())) {
                return Result.err(Status.ERR_ConflictAlreadyExists,
                                "Permission [%s.%s] is already a member of role [%s,%s]",
                                pd.ns, pd.type, role.ns, role.name);
            }
            role.perms(true).add(pd.encode()); // this is added for Caching
                                                // access purposes... doesn't
                                                // affect addPerm
            rv = q.roleDAO().addPerm(trans, role, pd);
        }
        if (rv.status == Status.OK) {
            return q.permDAO().addRole(trans, pd, role);
            // exploring how to add information message to successful http
            // request
        }
        return rv;
    }

    /**
     * Either Owner of Role or Permission may delete from Role
     *
     * @param trans
     * @param role
     * @param pd
     * @return
     */
    public Result<Void> delPermFromRole(AuthzTrans trans, RoleDAO.Data role,PermDAO.Data pd, boolean fromApproval) {
        String user = trans.user();
        if (!fromApproval) {
            Result<NsDAO.Data> ucr = q.mayUser(trans, user, role, Access.write);
            Result<NsDAO.Data> ucp = q.mayUser(trans, user, pd, Access.write);

            // If Can't change either Role or Perm, then deny
            if (ucr.notOK() && ucp.notOK()) {
                return Result.err(Status.ERR_Denied,
                        "User [" + trans.user()
                                + "] does not have permission to delete ["
                                + pd.encode() + "] from Role ["
                                + role.fullName() + ']');
            }
        }

        Result<List<RoleDAO.Data>> rlr = q.roleDAO().read(trans, role);
        if (rlr.notOKorIsEmpty()) {
            // If Bad Data, clean out
            Result<List<PermDAO.Data>> rlp = q.permDAO().read(trans, pd);
            if (rlp.isOKhasData()) {
                for (PermDAO.Data pv : rlp.value) {
                    q.permDAO().delRole(trans, pv, role);
                }
            }
            return Result.err(rlr);
        }
        String perm1 = pd.encode();
        boolean notFound;
        if (trans.requested(REQD_TYPE.force)) {
            notFound = false;
        } else { // only check if force not set.
            notFound = true;
            for (RoleDAO.Data r : rlr.value) {
                if (r.perms != null) {
                    for (String perm : r.perms) {
                        if (perm1.equals(perm)) {
                            notFound = false;
                            break;
                        }
                    }
                    if (!notFound) {
                        break;
                    }
                }
            }
        }
        if (notFound) { // Need to check both, in case of corruption
            return Result.err(Status.ERR_PermissionNotFound,
                    "Permission [%s.%s|%s|%s] not associated with any Role",
                    pd.ns,pd.type,pd.instance,pd.action);
        }

        // Read Perm for full data
        Result<List<PermDAO.Data>> rlp = q.permDAO().read(trans, pd);
        Result<Void> rv = null;
        if (rlp.isOKhasData()) {
            for (PermDAO.Data pv : rlp.value) {
                if ((rv = q.permDAO().delRole(trans, pv, role)).isOK()) {
                    if ((rv = q.roleDAO().delPerm(trans, role, pv)).notOK()) {
                        trans.error().log(
                                "Error removing Perm during delFromPermRole:",
                                trans.getUserPrincipal(), rv.errorString());
                    }
                } else {
                    trans.error().log(
                            "Error removing Role during delFromPermRole:",
                            trans.getUserPrincipal(), rv.errorString());
                }
            }
        } else {
            rv = q.roleDAO().delPerm(trans, role, pd);
            if (rv.notOK()) {
                trans.error().log("Error removing Role during delFromPermRole",
                        rv.errorString());
            }
        }
        return rv == null ? Result.ok() : rv;
    }

    public Result<Void> delPermFromRole(AuthzTrans trans, String role,PermDAO.Data pd) {
        Result<NsSplit> nss = q.deriveNsSplit(trans, role);
        if (nss.notOK()) {
            return Result.err(nss);
        }
        RoleDAO.Data rd = new RoleDAO.Data();
        rd.ns = nss.value.ns;
        rd.name = nss.value.name;
        return delPermFromRole(trans, rd, pd, false);
    }

    /**
     * Add a User to Role
     *
     * 1) Role must exist 2) User must be a known Credential (i.e. mechID ok if
     * Credential) or known Organizational User
     *
     * @param trans
     * @param org
     * @param urData
     * @return
     * @throws DAOException
     */
    public Result<Void> addUserRole(AuthzTrans trans,UserRoleDAO.Data urData) {
        Result<Void> rv;
        if (Question.ADMIN.equals(urData.rname)) {
            rv = mayAddAdmin(trans, urData.ns, urData.user);
        } else if (Question.OWNER.equals(urData.rname)) {
            rv = mayAddOwner(trans, urData.ns, urData.user);
        } else {
            rv = checkValidID(trans, new Date(), urData.user);
        }
        if (rv.notOK()) {
            return rv;
        }

        // Check if record exists
        if (q.userRoleDAO().read(trans, urData).isOKhasData()) {
            return Result.err(Status.ERR_ConflictAlreadyExists,
                    "User Role exists");
        }
        if (q.roleDAO().read(trans, urData.ns, urData.rname).notOKorIsEmpty()) {
            return Result.err(Status.ERR_RoleNotFound,
                    "Role [%s.%s] does not exist", urData.ns, urData.rname);
        }

        urData.expires = trans.org().expiration(null, Expiration.UserInRole, urData.user).getTime();


        Result<UserRoleDAO.Data> udr = q.userRoleDAO().create(trans, urData);
        if (udr.status == OK) {
            return Result.ok();
        }
        return Result.err(udr);
    }

    public Result<Void> addUserRole(AuthzTrans trans, String user, String ns, String rname) {
        try {
            if (trans.org().getIdentity(trans, user)==null) {
                return Result.err(Result.ERR_BadData,user+" is an Invalid Identity for " + trans.org().getName());
            }
        } catch (OrganizationException e) {
            return Result.err(e);
        }
        UserRoleDAO.Data urdd = new UserRoleDAO.Data();
        urdd.ns = ns;
        urdd.role(ns, rname);
        urdd.user = user;
        return addUserRole(trans,urdd);
    }

    /**
     * Extend User Role.
     *
     * extend the Expiration data, according to Organization rules.
     *
     * @param trans
     * @param org
     * @param urData
     * @return
     */
    public Result<Void> extendUserRole(AuthzTrans trans, UserRoleDAO.Data urData, boolean checkForExist) {
        // Check if record still exists
        if (checkForExist && q.userRoleDAO().read(trans, urData).notOKorIsEmpty()) {
            return Result.err(Status.ERR_UserRoleNotFound,
                    "User Role does not exist");
        }

        if (q.roleDAO().read(trans, urData.ns, urData.rname).notOKorIsEmpty()) {
            return Result.err(Status.ERR_RoleNotFound,
                    "Role [%s.%s] does not exist", urData.ns,urData.rname);
        }
        // Special case for "Admin" roles. Issue brought forward with Prod
        // problem 9/26
        Date now = new Date();
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(now.after(urData.expires)?now:urData.expires);
        urData.expires = trans.org().expiration(gc, Expiration.UserInRole).getTime(); // get
                                                                                // Full
                                                                                // time
                                                                                // starting
                                                                                // today
        return q.userRoleDAO().update(trans, urData);
    }

    // ////////////////////////////////////////////////////
    // Special User Role Functions
    // These exist, because User Roles have Expiration dates, which must be
    // accounted for
    // Also, as of July, 2015, Namespace Owners and Admins are now regular User
    // Roles
    // ////////////////////////////////////////////////////
    public Result<List<String>> getUsersByRole(AuthzTrans trans, String role, boolean includeExpired) {
        Result<List<UserRoleDAO.Data>> rurdd = q.userRoleDAO().readByRole(trans,role);
        if (rurdd.notOK()) {
            return Result.err(rurdd);
        }
        Date now = new Date();
        List<UserRoleDAO.Data> list = rurdd.value;
        List<String> rv = new ArrayList<>(list.size()); // presize
        for (UserRoleDAO.Data urdd : rurdd.value) {
            if (includeExpired || urdd.expires.after(now)) {
                rv.add(urdd.user);
            }
        }
        return Result.ok(rv);
    }

    public Result<Void> delUserRole(AuthzTrans trans, String user, String ns, String rname) {
        UserRoleDAO.Data urdd = new UserRoleDAO.Data();
        urdd.user = user;
        urdd.role(ns,rname);
        Result<List<UserRoleDAO.Data>> r = q.userRoleDAO().read(trans, urdd);
        if (r.status == 404 || r.isEmpty()) {
            return Result.err(Status.ERR_UserRoleNotFound,
                    "UserRole [%s] [%s.%s]", user, ns, rname);
        }
        if (r.notOK()) {
            return Result.err(r);
        }

        return q.userRoleDAO().delete(trans, urdd, false);
    }

    public Result<String> createFuture(AuthzTrans trans, FutureDAO.Data data, String id, String user,
            NsDAO.Data nsd, FUTURE_OP op) {
        StringBuilder sb = new StringBuilder();
        try {
            Organization org = trans.org();
            // For Reapproval, only check Owners.. Do Supervisors, etc, separately
            List<Identity> approvers = op.equals(FUTURE_OP.A)?NO_ADDL_APPROVE:org.getApprovers(trans, user);
            List<Identity> owners = new ArrayList<>();
            if (nsd != null) {
                Result<List<UserRoleDAO.Data>> rrbr = q.userRoleDAO()
                        .readByRole(trans, nsd.name + Question.DOT_OWNER);
                if (rrbr.isOKhasData()) {
                    for (UserRoleDAO.Data urd : rrbr.value) {
                        Identity owner = org.getIdentity(trans, urd.user);
                        if (owner==null) {
                            return Result.err(Result.ERR_NotFound,urd.user + " is not a Valid Owner of " + nsd.name);
                        } else {
                            owners.add(owner);
                        }
                    }
                }
            }

            if (owners.isEmpty()) {
                return Result.err(Result.ERR_NotFound,"No Owners found for " + nsd.name);
            }

            // Create Future Object

            Result<FutureDAO.Data> fr = q.futureDAO().create(trans, data, id);
            if (fr.isOK()) {
                sb.append("Created Future: ");
                sb.append(data.id);
                // User Future ID as ticket for Approvals
                final UUID ticket = fr.value.id;
                sb.append(", Approvals: ");
                Boolean[] first = new Boolean[]{true};
                if (op!=FUTURE_OP.A) {
                    for (Identity u : approvers) {
                        Result<ApprovalDAO.Data> r = addIdentity(trans,sb,first,user,data.memo,op,u,ticket,org.getApproverType());
                        if (r.notOK()) {
                            return Result.err(r);
                        }
                    }
                }
                for (Identity u : owners) {
                    Result<ApprovalDAO.Data> r = addIdentity(trans,sb,first,user,data.memo,op,u,ticket,"owner");
                    if (r.notOK()) {
                        return Result.err(r);
                    }
                }
            }
        } catch (Exception e) {
            return Result.err(e);
        }

        return Result.ok(sb.toString());
    }

    /*
     * This interface is to allow performFutureOps with either Realtime Data, or Batched lookups (See Expiring)
     */
    public interface Lookup<T> {
        T get(AuthzTrans trans, Object ... keys);
    }

    public Lookup<UserRoleDAO.Data> urDBLookup = new Lookup<UserRoleDAO.Data>() {
        @Override
        public UserRoleDAO.Data get(AuthzTrans trans, Object ... keys) {
            Result<List<UserRoleDAO.Data>> r = q.userRoleDAO().read(trans, keys);
            if (r.isOKhasData()) {
                return r.value.get(0);
            } else {
                return null;
            }
        }
    };

    /**
     * Note: if "allApprovals for Ticket is null, it will be looked up.
     *       if "fdd" is null, it will be looked up, but
     *
     * They can be passed for performance reasons.
     *
     * @param trans
     * @param cd
     * @param allApprovalsForTicket
     * @return
     */
    public Result<OP_STATUS> performFutureOp(final AuthzTrans trans, FUTURE_OP fop, FutureDAO.Data curr, Lookup<List<ApprovalDAO.Data>> la, Lookup<UserRoleDAO.Data> lur) {
        // Pre-Evaluate if ReApproval is already done.
        UserRoleDAO.Data urdd = null;
        if (fop.equals(FUTURE_OP.A) && curr.target.equals(FOP_USER_ROLE) && curr.construct!=null) {
            try {
                // Get Expected UserRole from Future
                urdd = new UserRoleDAO.Data();
                urdd.reconstitute(curr.construct);
                // Get Current UserRole from lookup
                UserRoleDAO.Data lurdd = lur.get(trans, urdd.user,urdd.role);
                if (lurdd==null) {
                    q.futureDAO().delete(trans, curr, false);
                    return OP_STATUS.RL;
                } else {
                    if (curr.expires.compareTo(lurdd.expires)<0) {
                        q.futureDAO().delete(trans, curr, false);
                        return OP_STATUS.RL;
                    }
                }
            } catch (IOException e) {
                return Result.err(Result.ERR_BadData,"Cannot reconstitute %1",curr.memo);
            }
        }

        boolean aDenial = false;
        int cntSuper=0, appSuper=0,cntOwner=0, appOwner=0;
        for (ApprovalDAO.Data add : la.get(trans)) {
            switch(add.status) {
                case "approved":
                    if ("owner".equals(add.type)) {
                        ++cntOwner;
                        ++appOwner;
                    } else if ("supervisor".equals(add.type)) {
                        ++cntSuper;
                        ++appSuper;
                    }
                    break;
                case "pending":
                    if ("owner".equals(add.type)) {
                        ++cntOwner;
                    } else if ("supervisor".equals(add.type)) {
                        ++cntSuper;
                    }
                    break;
                case "denied":
                    aDenial=true;
                    break;
                default:
                    break;
            }
        }

        Result<OP_STATUS> ros=null;
        if (aDenial) {
            ros = OP_STATUS.RD;
            if (q.futureDAO().delete(trans, curr, false).notOK()) {
                trans.info().printf("Future %s could not be deleted", curr.id.toString());
            }  else {
                if (FOP_USER_ROLE.equalsIgnoreCase(curr.target)) {
                    // A Denial means we must remove UserRole
                    if (fop.equals(FUTURE_OP.U) || fop.equals(FUTURE_OP.A)) {
                        UserRoleDAO.Data data = new UserRoleDAO.Data();
                        try {
                            data.reconstitute(curr.construct);
                        } catch (IOException e) {
                            trans.error().log("Cannot reconstitue",curr.memo);
                        }
                        ros = set(OP_STATUS.RD,delUserRole(trans, data.user, data.ns, data.rname));
                    }
                }
            }
        }

        // Decision: If not Denied, and at least owner, if exists, and at least one Super, if exists
        boolean goDecision = (cntOwner>0?appOwner>0:true) && (cntSuper>0?appSuper>0:true);

        if (goDecision) {
            // should check if any other pendings before performing
            // actions
            try {
                if (FOP_ROLE.equalsIgnoreCase(curr.target)) {
                    RoleDAO.Data data = new RoleDAO.Data();
                    data.reconstitute(curr.construct);
                    switch(fop) {
                        case C:
                            ros = set(OP_STATUS.RE,q.roleDAO().dao().create(trans, data));
                            break;
                        case D:
                            ros = set(OP_STATUS.RE,deleteRole(trans, data, true, true));
                            break;
                        default:
                    }
                } else if (FOP_PERM.equalsIgnoreCase(curr.target)) {
                    PermDAO.Data pdd = new PermDAO.Data();
                    pdd.reconstitute(curr.construct);
                    Set<String> roles;
                    Result<RoleDAO.Data> rrdd;
                    switch(fop) {
                        case C:
                            ros = set(OP_STATUS.RE,createPerm(trans, pdd, true));
                            break;
                        case D:
                            ros = set(OP_STATUS.RE,deletePerm(trans, pdd, true, true));
                            break;
                        case G:
                            roles = pdd.roles(true);
                            for (String roleStr : roles) {
                                rrdd = RoleDAO.Data.decode(trans, q, roleStr);
                                if (rrdd.isOKhasData()) {
                                    ros = set(OP_STATUS.RE,addPermToRole(trans, rrdd.value, pdd, true));
                                } else {
                                    trans.error().log(rrdd.errorString());
                                }
                            }
                            break;
                        case UG:
                            roles = pdd.roles(true);
                            for (String roleStr : roles) {
                                rrdd = RoleDAO.Data.decode(trans, q, roleStr);
                                if (rrdd.isOKhasData()) {
                                    ros = set(OP_STATUS.RE,delPermFromRole(trans, rrdd.value, pdd,    true));
                                } else {
                                    trans.error().log(rrdd.errorString());
                                }
                            }
                            break;
                        default:
                    }
                } else if (FOP_USER_ROLE.equalsIgnoreCase(curr.target)) {
                    if (urdd==null) {
                        urdd = new UserRoleDAO.Data();
                        urdd.reconstitute(curr.construct);
                    }
                    // if I am the last to approve, create user role
                    switch(fop) {
                        case C:
                            ros = set(OP_STATUS.RE,addUserRole(trans, urdd));
                            break;
                        case U:
                        case A:
                            ros = set(OP_STATUS.RE,extendUserRole(trans,urdd,true));
                            break;
                        default:
                    }
                } else if (FOP_NS.equalsIgnoreCase(curr.target)) {
                    Namespace namespace = new Namespace();
                    namespace.reconstitute(curr.construct);
                    if (fop == FUTURE_OP.C) {
                        ros = set(OP_STATUS.RE, createNS(trans, namespace, true));
                    }
                } else if (FOP_DELEGATE.equalsIgnoreCase(curr.target)) {
                    DelegateDAO.Data data = new DelegateDAO.Data();
                    data.reconstitute(curr.construct);
                    switch(fop) {
                        case C:
                            ros = set(OP_STATUS.RE,q.delegateDAO().create(trans, data));
                            break;
                        case U:
                            ros = set(OP_STATUS.RE,q.delegateDAO().update(trans, data));
                            break;
                        default:
                    }
                } else if (FOP_CRED.equalsIgnoreCase(curr.target)) {
                    CredDAO.Data data = new CredDAO.Data();
                    data.reconstitute(curr.construct);
                    if (fop == FUTURE_OP.C) {
                        ros = set(OP_STATUS.RE, q.credDAO().dao().create(trans, data));
                    }
                }
            } catch (Exception e) {
                trans.error().log("Exception: ", e.getMessage(),
                    " \n occurred while performing", curr.memo,
                    " from Ticket ", curr.id.toString());
            }
            q.futureDAO().delete(trans, curr, false);
        } // end for goDecision
        if (ros==null) {
            //return Result.err(Status.ACC_Future, "Full Approvals not obtained: No action taken");
            ros = OP_STATUS.RP;
        }

        return ros;
    }

    // Convenience method for setting OPSTatus Results
    private Result<OP_STATUS> set(Result<OP_STATUS> rs, Result<?> orig) {
        if (orig.isOK()) {
            return rs;
        } else {
            return Result.err(orig);
        }
    }

    private Result<ApprovalDAO.Data>  addIdentity(AuthzTrans trans, StringBuilder sb,
                        Boolean[] first, String user, String memo, FUTURE_OP op, Identity u, UUID ticket, String type) throws OrganizationException {
        ApprovalDAO.Data ad = new ApprovalDAO.Data();
        // Note ad.id is set by ApprovalDAO Create
        ad.ticket = ticket;
        ad.user = user;
        ad.approver = u.fullID();
        ad.status = ApprovalDAO.PENDING;
        ad.memo = memo;
        ad.type = type;
        ad.operation = op.name();
        // Note ad.updated is created in System
        Result<ApprovalDAO.Data> r = q.approvalDAO().create(trans,ad);
        if (r.isOK()) {
            if (first[0]) {
                first[0] = false;
            } else {
                sb.append(", ");
            }
            sb.append(r.value.user);
            sb.append(':');
            sb.append(r.value.ticket);
            return r;
        } else {
            return Result.err(Status.ERR_ActionNotCompleted,
                    "Approval for %s, %s could not be created: %s",
                    ad.user, ad.approver,
                    r.details, sb.toString());
        }
    }

    public Executor newExecutor(AuthzTrans trans) {
        return new CassExecutor(trans, this);
    }

}
