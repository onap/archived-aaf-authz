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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.AbsCassDAO;
import org.onap.aaf.auth.dao.CachedDAO;
import org.onap.aaf.auth.dao.DAOException;
import org.onap.aaf.auth.dao.cached.CachedCertDAO;
import org.onap.aaf.auth.dao.cached.CachedCredDAO;
import org.onap.aaf.auth.dao.cached.CachedNSDAO;
import org.onap.aaf.auth.dao.cached.CachedPermDAO;
import org.onap.aaf.auth.dao.cached.CachedRoleDAO;
import org.onap.aaf.auth.dao.cached.CachedUserRoleDAO;
import org.onap.aaf.auth.dao.cached.FileGetter;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.CacheInfoDAO;
import org.onap.aaf.auth.dao.cass.CertDAO;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.CredDAO.Data;
import org.onap.aaf.auth.dao.cass.DelegateDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.HistoryDAO;
import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.NsSplit;
import org.onap.aaf.auth.dao.cass.NsType;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTrans.REQD_TYPE;
import org.onap.aaf.auth.env.AuthzTransFilter;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.cadi.Hash;
import org.onap.aaf.cadi.aaf.PermEval;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;

import com.datastax.driver.core.Cluster;

/**
 * Question HL DAO
 * 
 * A Data Access Combination Object which asks Security and other Questions
 * 
 * @author Jonathan
 *
 */
public class Question {

    // DON'T CHANGE FROM lower Case!!!
    public enum Type {
        ns, role, perm, cred
    };

    public static final String OWNER="owner";
    public static final String ADMIN="admin";
    public static final String DOT_OWNER=".owner";
    public static final String DOT_ADMIN=".admin";
    public static final String ACCESS = "access";

    static final String ASTERIX = "*";

    public enum Access {
        read, write, create
    };

    public static final String READ = Access.read.name();
    public static final String WRITE = Access.write.name();
    public static final String CREATE = Access.create.name();

    public static final String ROLE = Type.role.name();
    public static final String PERM = Type.perm.name();
    public static final String NS = Type.ns.name();
    public static final String CRED = Type.cred.name();
    private static final String DELG = "delg";
    public static final String ROOT_NS = Define.isInitialized() ? Define.ROOT_NS() : "undefined";
    public static final String ATTRIB = "attrib";


    public static final int MAX_SCOPE = 10;
    public static final int APP_SCOPE = 3;
    public static final int COMPANY_SCOPE = 2;
    static Slot PERMS;

    private static Set<String> specialLog = null;
    public static final SecureRandom random = new SecureRandom();
    private static long traceID = random.nextLong();
    private static Slot specialLogSlot = null;
    private static Slot transIDSlot = null;


    private final HistoryDAO historyDAO;
    public HistoryDAO historyDAO() {
        return historyDAO;
    }
    
    private final CachedNSDAO nsDAO;
    public CachedNSDAO nsDAO() {
        return nsDAO;
    }
    
    private final CachedRoleDAO roleDAO;
    public CachedRoleDAO roleDAO() {
        return roleDAO;
    }
    
    private final CachedPermDAO permDAO;
    public CachedPermDAO permDAO() {
        return permDAO;
    }
    
    private final CachedUserRoleDAO userRoleDAO;
    public CachedUserRoleDAO userRoleDAO() {
        return userRoleDAO;
    }
    
    private final CachedCredDAO credDAO;
    public CachedCredDAO credDAO() {
        return credDAO;
    }
    
    private final CachedCertDAO certDAO;
    public CachedCertDAO certDAO() {
        return certDAO;
    }
    
    private final DelegateDAO delegateDAO;
    public DelegateDAO delegateDAO() {
        return delegateDAO;
    }
    
    private final FutureDAO futureDAO;
    public FutureDAO futureDAO() {
        return futureDAO;
    }
    
    private final ApprovalDAO approvalDAO;
    public ApprovalDAO approvalDAO() {
        return approvalDAO;
    }
    
    public final LocateDAO locateDAO;
    public LocateDAO locateDAO() {
        return locateDAO;
    }
    
    private final CacheInfoDAO cacheInfoDAO;
    private final int cldays;
    private final boolean alwaysSpecial;

    public Question(AuthzTrans trans, Cluster cluster, String keyspace) throws APIException, IOException {
        PERMS = trans.slot("USER_PERMS");
        trans.init().log("Instantiating DAOs");
        long expiresIn = Long.parseLong(trans.getProperty(Config.AAF_USER_EXPIRES, Config.AAF_USER_EXPIRES_DEF));
        historyDAO = new HistoryDAO(trans, cluster, keyspace);

        // Deal with Cached Entries
        cacheInfoDAO = new CacheInfoDAO(trans, historyDAO);

        nsDAO = new CachedNSDAO(new NsDAO(trans, historyDAO, cacheInfoDAO),cacheInfoDAO, expiresIn);
        permDAO = new CachedPermDAO(new PermDAO(trans, historyDAO, cacheInfoDAO), cacheInfoDAO, expiresIn);
        roleDAO = new CachedRoleDAO(new RoleDAO(trans, historyDAO, cacheInfoDAO), cacheInfoDAO, expiresIn);
        userRoleDAO = new CachedUserRoleDAO(new UserRoleDAO(trans, historyDAO,cacheInfoDAO), cacheInfoDAO, expiresIn);
        // Create if aaf_file_cred exists with file
        FileGetter.singleton(trans.env().access());
        credDAO = new CachedCredDAO(new CredDAO(trans, historyDAO, cacheInfoDAO), cacheInfoDAO, expiresIn);
        certDAO = new CachedCertDAO(new CertDAO(trans, historyDAO, cacheInfoDAO), cacheInfoDAO, expiresIn);

        locateDAO = new LocateDAO(trans,historyDAO);
        futureDAO = new FutureDAO(trans, historyDAO);
        delegateDAO = new DelegateDAO(trans, historyDAO);
        approvalDAO = new ApprovalDAO(trans, historyDAO);

        if (specialLogSlot==null) {
            specialLogSlot = trans.slot(AuthzTransFilter.SPECIAL_LOG_SLOT);
        }
        
        if (transIDSlot==null) {
            transIDSlot = trans.slot(AuthzTransFilter.TRANS_ID_SLOT);
        }
        
        AbsCassDAO.primePSIs(trans);
        
        cldays = Integer.parseInt(trans.getProperty(Config.AAF_CRED_WARN_DAYS, Config.AAF_CRED_WARN_DAYS_DFT));
        
        alwaysSpecial = Boolean.parseBoolean(trans.getProperty("aaf_always_special", Boolean.FALSE.toString()));
    }
    
    /**
     * Note: This Constructor created for JUNIT Purposes.  Do not use otherwise.
     */
    public Question(AuthzTrans trans, HistoryDAO historyDAO, CacheInfoDAO cacheInfoDAO,
            CachedNSDAO nsDAO, CachedPermDAO permDAO, CachedRoleDAO roleDAO,
            CachedUserRoleDAO userRoleDAO, CachedCredDAO credDAO, CachedCertDAO certDAO,
            LocateDAO locateDAO,FutureDAO futureDAO, DelegateDAO delegateDAO,
            ApprovalDAO approvalDAO ) {
        this.historyDAO = historyDAO;
        this.cacheInfoDAO = cacheInfoDAO;
        this.nsDAO = nsDAO;
        this.permDAO = permDAO;
        this.roleDAO = roleDAO;
        this.userRoleDAO = userRoleDAO;
        this.credDAO = credDAO;
        this.certDAO = certDAO;
        this.locateDAO = locateDAO;
        this.futureDAO = futureDAO;
        this.delegateDAO = delegateDAO;
        this.approvalDAO = approvalDAO;

        cldays = Integer.parseInt(trans.getProperty(Config.AAF_CRED_WARN_DAYS, Config.AAF_CRED_WARN_DAYS_DFT));
        alwaysSpecial = Boolean.parseBoolean(trans.getProperty("aaf_always_special", Boolean.FALSE.toString()));
    }

    public void startTimers(AuthzEnv env) {
        // Only want to aggressively cleanse User related Caches... The others,
        // just normal refresh
        CachedDAO.startCleansing(env, credDAO, userRoleDAO);
        CachedDAO.startRefresh(env, cacheInfoDAO);
    }
    
    public void close(AuthzTrans trans) {
        historyDAO.close(trans);
        cacheInfoDAO.close(trans);
        nsDAO.close(trans);
        permDAO.close(trans);
        roleDAO.close(trans);
        userRoleDAO.close(trans);
        credDAO.close(trans);
        certDAO.close(trans);
        delegateDAO.close(trans);
        futureDAO.close(trans);
        approvalDAO.close(trans);
    }

    public Result<PermDAO.Data> permFrom(AuthzTrans trans, String type, String instance, String action) {
        if(type.indexOf('@') >= 0) {
            int colon = type.indexOf(':');
            if(colon>=0) {
                PermDAO.Data pdd = new PermDAO.Data();
                pdd.ns = type.substring(0, colon);
                pdd.type = type.substring(colon+1);
                pdd.instance = instance;
                pdd.action = action;
            
                return Result.ok(pdd);
            } else {
                return Result.err(Result.ERR_BadData,"Could not extract ns and type from " + type);
            }
        } else {
            Result<NsDAO.Data> rnd = deriveNs(trans, type);
            if (rnd.isOK()) {
                return Result.ok(new PermDAO.Data(new NsSplit(rnd.value, type),
                        instance, action));
            } else {
                return Result.err(rnd);
            }
        }
    }

    /**
     * getPermsByUser
     * 
     * Because this call is frequently called internally, AND because we already
     * look for it in the initial Call, we cache within the Transaction
     * 
     * @param trans
     * @param user
     * @return
     */
    public Result<List<PermDAO.Data>> getPermsByUser(AuthzTrans trans, String user, boolean lookup) {
        return PermLookup.get(trans, this, user).getPerms(lookup);
    }
    
    public Result<List<PermDAO.Data>> getPermsByUserFromRolesFilter(AuthzTrans trans, String user, String forUser) {
        PermLookup plUser = PermLookup.get(trans, this, user);
        Result<Set<String>> plPermNames = plUser.getPermNames();
        if (plPermNames.notOK()) {
            return Result.err(plPermNames);
        }
        
        Set<String> nss;
        if (forUser.equals(user)) {
            nss = null;
        } else {
            // Setup a TreeSet to check on Namespaces to 
            nss = new TreeSet<>();
            PermLookup fUser = PermLookup.get(trans, this, forUser);
            Result<Set<String>> forUpn = fUser.getPermNames();
            if (forUpn.notOK()) {
                return Result.err(forUpn);
            }
            
            for (String pn : forUpn.value) {
                Result<String[]> decoded = PermDAO.Data.decodeToArray(trans, this, pn);
                if (decoded.isOKhasData()) {
                    nss.add(decoded.value[0]);
                } else {
                    trans.error().log(pn,", derived from a Role, is invalid:",decoded.errorString());
                }
            }
        }

        List<PermDAO.Data> rlpUser = new ArrayList<>();
        Result<PermDAO.Data> rpdd;
        PermDAO.Data pdd;
        for (String pn : plPermNames.value) {
            rpdd = PermDAO.Data.decode(trans, this, pn);
            if (rpdd.isOKhasData()) {
                pdd=rpdd.value;
                if (nss==null || nss.contains(pdd.ns)) {
                    rlpUser.add(pdd);
                }
            } else {
                trans.error().log(pn,", derived from a Role, is invalid.  Run Data Cleanup:",rpdd.errorString());
            }
        }
        return Result.ok(rlpUser); 
    }

    public Result<List<PermDAO.Data>> getPermsByType(AuthzTrans trans, String type) {
        if(type.indexOf('@') >= 0) {
            int colon = type.indexOf(':');
            if(colon>=0) {
                return permDAO.readByType(trans, type.substring(0, colon),type.substring(colon+1));
            } else {
                return Result.err(Result.ERR_BadData, "%s is malformed",type);
            }
        } else {
            Result<NsSplit> nss = deriveNsSplit(trans, type);
            if (nss.notOK()) {
                return Result.err(nss);
            }
            return permDAO.readByType(trans, nss.value.ns, nss.value.name);
        }
    }

    public Result<List<PermDAO.Data>> getPermsByName(AuthzTrans trans, String type, String instance, String action) {
        if(type.indexOf('@') >= 0) {
            int colon = type.indexOf(':');
            if(colon>=0) {
                return permDAO.read(trans, type.substring(0, colon),type.substring(colon+1), instance,action);
            } else {
                return Result.err(Result.ERR_BadData, "%s is malformed",type);
            }
        } else {
            Result<NsSplit> nss = deriveNsSplit(trans, type);
            if (nss.notOK()) {
                return Result.err(nss);
            }
            
            return permDAO.read(trans, nss.value.ns, nss.value.name, instance,action);
        }
    }

    public Result<List<PermDAO.Data>> getPermsByRole(AuthzTrans trans, String role, boolean lookup) {
        Result<NsSplit> nss = deriveNsSplit(trans, role);
        if (nss.notOK()) {
            return Result.err(nss);
        }

        Result<List<RoleDAO.Data>> rlrd = roleDAO.read(trans, nss.value.ns,
                nss.value.name);
        if (rlrd.notOKorIsEmpty()) {
            return Result.err(rlrd);
        }
        // Using Set to avoid duplicates
        Set<String> permNames = new HashSet<>();
        if (rlrd.isOKhasData()) {
            for (RoleDAO.Data drr : rlrd.value) {
                permNames.addAll(drr.perms(false));
            }
        }

        // Note: It should be ok for a Valid user to have no permissions -
        // Jonathan 8/12/2013
        List<PermDAO.Data> perms = new ArrayList<>();
        for (String perm : permNames) {
            Result<PermDAO.Data> pr = PermDAO.Data.decode(trans, this, perm);
            if (pr.notOK()) {
                return Result.err(pr);
            }

            if (lookup) {
                Result<List<PermDAO.Data>> rlpd = permDAO.read(trans, pr.value);
                if (rlpd.isOKhasData()) {
                    for (PermDAO.Data pData : rlpd.value) {
                        perms.add(pData);
                    }
                }
            } else {
                perms.add(pr.value);
            }
        }

        return Result.ok(perms);
    }

    public Result<List<RoleDAO.Data>> getRolesByName(AuthzTrans trans, String role) {
        if(role.startsWith(trans.user()) ) {
            if(role.endsWith(":user")) {
                return roleDAO.read(trans,trans.user(), "user");
            } else {
                return Result.err(Result.ERR_BadData,"%s is a badly formatted role",role);
            }
        }
        Result<NsSplit> nss = deriveNsSplit(trans, role);
        if (nss.notOK()) {
            return Result.err(nss);
        }
        String r = nss.value.name;
        if (r.endsWith(".*")) { // do children Search
            return roleDAO.readChildren(trans, nss.value.ns,
                    r.substring(0, r.length() - 2));
        } else if (ASTERIX.equals(r)) {
            return roleDAO.readChildren(trans, nss.value.ns, ASTERIX);
        } else {
            return roleDAO.read(trans, nss.value.ns, r);
        }
    }

    /**
     * Derive NS
     * 
     * Given a Child Namespace, figure out what the best Namespace parent is.
     * 
     * For instance, if in the NS table, the parent "org.osaaf" exists, but not
     * "org.osaaf.child" or "org.osaaf.a.b.c", then passing in either
     * "org.osaaf.child" or "org.osaaf.a.b.c" will return "org.osaaf"
     * 
     * Uses recursive search on Cached DAO data
     * 
     * @param trans
     * @param child
     * @return
     */
    public Result<NsDAO.Data> deriveNs(AuthzTrans trans, String child) {
        Result<List<NsDAO.Data>> r = nsDAO.read(trans, child);
        
        if (r.isOKhasData()) {
            return Result.ok(r.value.get(0));
        } else {
            int dot = child.lastIndexOf('.');
            if (dot < 0) {
                return Result.err(Status.ERR_NsNotFound, "No Namespace for [%s]", child);
            } else {
                return deriveNs(trans, child.substring(0, dot));
            }
        }
    }

    public Result<NsDAO.Data> deriveFirstNsForType(AuthzTrans trans, String str, NsType type) {
        NsDAO.Data nsd;

        for (String lookup = str;!".".equals(lookup) && lookup!=null;) {
            Result<List<NsDAO.Data>> rld = nsDAO.read(trans, lookup);
            if (rld.isOKhasData()) {
                nsd=rld.value.get(0);
                lookup = nsd.parent;
                if (type.type == nsd.type) {
                    return Result.ok(nsd);
                } else {
                    int dot = str.lastIndexOf('.');
                    
                    if (dot < 0) {
                        return Result.err(Status.ERR_NsNotFound, "No Namespace for [%s]", str);
                    } else {
                        return deriveFirstNsForType(trans, str.substring(0, dot),type);
                    }
                }
            } else {
                int dot = str.lastIndexOf('.');
                
                if (dot < 0) {
                    return Result.err(Status.ERR_NsNotFound,"There is no valid Company Namespace for %s",str);
                } else {
                    return deriveFirstNsForType(trans, str.substring(0, dot),type);
                }
            }
        }
        return Result.err(Status.ERR_NotFound, str + " does not contain type " + type.name());
    }

    public Result<NsSplit> deriveNsSplit(AuthzTrans trans, String child) {
        Result<NsDAO.Data> ndd = deriveNs(trans, child);
        if (ndd.isOK()) {
            NsSplit nss = new NsSplit(ndd.value, child);
            if (nss.isOK()) {
                return Result.ok(nss);
            } else {
                return Result.err(Status.ERR_NsNotFound,
                        "Cannot split [%s] into valid namespace elements",
                        child);
            }
        }
        return Result.err(ndd);
    }

    /**
     * Translate an ID into it's domain
     * 
     * i.e. myid1234@aaf.att.com results in domain of com.att.aaf
     * 
     * @param id
     * @return
     */
    public static String domain2ns(String id) {
        int at = id.indexOf('@');
        if (at >= 0) {
            String[] domain = id.substring(at + 1).split("\\.");
            StringBuilder ns = new StringBuilder(id.length());
            boolean first = true;
            for (int i = domain.length - 1; i >= 0; --i) {
                if (first) {
                    first = false;
                } else {
                    ns.append('.');
                }
                ns.append(domain[i]);
            }
            return ns.toString();
        } else {
            return "";
        }

    }

    /**
     * Validate Namespace of ID@Domain
     * 
     * Namespace is reverse order of Domain.
     * 
     * @param trans
     * @param id
     * @return
     */
    public Result<NsDAO.Data> validNSOfDomain(AuthzTrans trans, String id) {
        // Take domain, reverse order, and check on NS
        String ns;
        if (id.indexOf('@')<0) { // it's already an ns, not an ID
            ns = id;
        } else {
            ns = domain2ns(id);
        }
        if (ns.length() > 0) {
            if (!trans.org().getDomain().equals(ns)) { 
                Result<List<NsDAO.Data>> rlnsd = nsDAO.read(trans, ns);
                if (rlnsd.isOKhasData()) {
                    return Result.ok(rlnsd.value.get(0));
                }
            }
        }
        return Result.err(Status.ERR_NsNotFound,
                "A Namespace is not available for %s", id);
    }

    public Result<NsDAO.Data> mayUser(AuthzTrans trans, String user,NsDAO.Data ndd, Access access) {
        // <ns>.access|:role:<role name>|<read|write>
        String ns = ndd.name;
        boolean isRoot = ns.startsWith(Define.ROOT_NS());
        int last;
        do {
            if (isGranted(trans, user, ns, ACCESS, ":ns", access.name())) {
                return Result.ok(ndd);
            }
            if(isRoot) {
                break;
            }
            if ((last = ns.lastIndexOf('.')) >= 0) {
                ns = ns.substring(0, last);
            }
        } while (last >= 0);
        
        // SAFETY - Do not allow these when NS is Root
        if(!isRoot) {
            // com.att.aaf.ns|:<client ns>:ns|<access>
            // AAF-724 - Make consistent response for May User", and not take the
            // last check... too confusing.
            Result<NsDAO.Data> rv = mayUserVirtueOfNS(trans, user, ndd, ":"    + ndd.name + ":ns", access.name());
                if (rv.isOK()) {
                    return rv;
                } else if (rv.status==Result.ERR_Backend) {
                    return Result.err(rv);
                }
            }
        return Result.err(Status.ERR_Denied, "[%s] may not %s in NS [%s]",
                user, access.name(), ndd.name);

    }

    public Result<NsDAO.Data> mayUser(AuthzTrans trans, String user, RoleDAO.Data rdd, Access access) {
        if(trans.user().equals(rdd.ns)) {
            return Result.ok((NsDAO.Data)null);
        }
        Result<NsDAO.Data> rnsd = deriveNs(trans, rdd.ns);
        if (rnsd.isOK()) {
            return mayUser(trans, user, rnsd.value, rdd, access);
        }
        return rnsd;
    }

    public Result<NsDAO.Data> mayUser(AuthzTrans trans, String user, NsDAO.Data ndd, RoleDAO.Data rdd, Access access) {
        // 1) For "read", Is User in the Role is enough
        if(Access.read.equals(access)) {
            Result<List<UserRoleDAO.Data>> rurd = userRoleDAO.readUserInRole(trans, user, rdd.fullName());
            if (rurd.isOKhasData()) {
                return Result.ok(ndd);
            }
        }

        String roleInst = ":role:" + rdd.name;
        // <ns>.access|:role:<role name>|<read|write>
        String ns = rdd.ns;
        boolean isRoot = ns.startsWith(Define.ROOT_NS());
        int last;
        do {
            if (isGranted(trans, user, ns,ACCESS, roleInst, access.name())) {
                return Result.ok(ndd);
            }
            if(isRoot) {
                break;
            }
            if ((last = ns.lastIndexOf('.')) >= 0) {
                ns = ns.substring(0, last);
            }
        } while (last >= 0);

        // SAFETY - Do not allow these when NS is Root
        if(!isRoot) {
            // Check if Access by Global Role perm
            // com.att.aaf.ns|:<client ns>:role:name|<access>
            Result<NsDAO.Data> rnsd = mayUserVirtueOfNS(trans, user, ndd, ":"
                    + rdd.ns + roleInst, access.name());
            if (rnsd.isOK()) {
                return rnsd;
            } else if (rnsd.status==Result.ERR_Backend) {
                return Result.err(rnsd);
            }

            // Check if Access to Whole NS
            // AAF-724 - Make consistent response for May User", and not take the
            // last check... too confusing.
            Result<org.onap.aaf.auth.dao.cass.NsDAO.Data> rv = mayUserVirtueOfNS(trans, user, ndd, 
                    ":" + rdd.ns + ":ns", access.name());
            if (rv.isOK()) {
                return rv;
            } else if (rnsd.status==Result.ERR_Backend) {
                return Result.err(rnsd);
            }
        }
        return Result.err(Status.ERR_Denied, "[%s] may not %s Role [%s]",
                    user, access.name(), rdd.fullName());
    }

    public Result<NsDAO.Data> mayUser(AuthzTrans trans, String user,PermDAO.Data pdd, Access access) {
        if(pdd.ns.indexOf('@')>-1) {
            if(user.equals(pdd.ns) || isGranted(trans,user,Define.ROOT_NS(),"access",pdd.instance,READ)) {
                NsDAO.Data ndd = new NsDAO.Data();
                ndd.name = user;
                ndd.type = NsDAO.USER;
                ndd.parent = "";
                return Result.ok(ndd);
            } else {
                return Result.err(Result.ERR_Security,"Only a User may modify User");
            }
        }
        Result<NsDAO.Data> rnsd = deriveNs(trans, pdd.ns);
        if (rnsd.isOK()) {
            return mayUser(trans, user, rnsd.value, pdd, access);
        }
        return rnsd;
    }

    public Result<NsDAO.Data> mayUser(AuthzTrans trans, String user,NsDAO.Data ndd, PermDAO.Data pdd, Access access) {
        // Most common occurrence... if granted Permission
        if (isGranted(trans, user, pdd.ns, pdd.type, pdd.instance, pdd.action)) {
            return Result.ok(ndd);
        }
        
        String permInst = ":perm:" + pdd.type + ':' + pdd.instance + ':' + pdd.action;
        // <ns>.access|:role:<role name>|<read|write>
        String ns = ndd.name;
        boolean isRoot = ns.startsWith(Define.ROOT_NS());
        int last;
        do {
            if (isGranted(trans, user, ns, ACCESS, permInst, access.name())) {
                return Result.ok(ndd);
            }
            if(isRoot) {
                break;
            }
            if ((last = ns.lastIndexOf('.')) >= 0) {
                ns = ns.substring(0, last);
            }
        } while (last >= 0);

        // SAFETY - Do not allow these when NS is Root
        if(!isRoot) {
            // Check if Access by NS perm
            // com.att.aaf.ns|:<client ns>:role:name|<access>
            Result<NsDAO.Data> rnsd = mayUserVirtueOfNS(trans, user, ndd, ":" + pdd.ns + permInst, access.name());
            if (rnsd.isOK()) {
                return rnsd;
            } else if (rnsd.status==Result.ERR_Backend) {
                return Result.err(rnsd);
            }

            // Check if Access to Whole NS
            // AAF-724 - Make consistent response for May User", and not take the
            // last check... too confusing.
            Result<NsDAO.Data> rv = mayUserVirtueOfNS(trans, user, ndd, ":"    + pdd.ns + ":ns", access.name());
            if (rv.isOK()) {
                return rv;
            }
        }
        return Result.err(Status.ERR_Denied,
                "[%s] may not %s Perm [%s|%s|%s]", user, access.name(),
                pdd.fullType(), pdd.instance, pdd.action);
    }

    public Result<Void> mayUser(AuthzTrans trans, DelegateDAO.Data dd, Access access) {
        try {
            Result<NsDAO.Data> rnsd = deriveNs(trans, domain2ns(trans.user()));
            if (rnsd.isOKhasData() && mayUserVirtueOfNS(trans,trans.user(),rnsd.value, ":"    + rnsd.value.name + ":ns", access.name()).isOK()) {
                return Result.ok();
            }
            boolean isUser = trans.user().equals(dd.user);
            boolean isDelegate = dd.delegate != null
                    && (dd.user.equals(dd.delegate) || trans.user().equals(
                            dd.delegate));
            Organization org = trans.org();
            switch (access) {
            case create:
                if (org.getIdentity(trans, dd.user) == null) {
                    return Result.err(Status.ERR_UserNotFound,
                            "[%s] is not a user in the company database.",
                            dd.user);
                }
                if (!dd.user.equals(dd.delegate) && org.getIdentity(trans, dd.delegate) == null) {
                    return Result.err(Status.ERR_UserNotFound,
                            "[%s] is not a user in the company database.",
                            dd.delegate);
                }
                if (!trans.requested(REQD_TYPE.force) && dd.user != null && dd.user.equals(dd.delegate)) {
                    return Result.err(Status.ERR_BadData,
                            "[%s] cannot be a delegate for self", dd.user);
                }
                if (!isUser    && !isGranted(trans, trans.user(), ROOT_NS,DELG,
                                org.getDomain(), Question.CREATE)) {
                    return Result.err(Status.ERR_Denied,
                            "[%s] may not create a delegate for [%s]",
                            trans.user(), dd.user);
                }
                break;
            case read:
            case write:
                if (!isUser    && !isDelegate && 
                        !isGranted(trans, trans.user(), ROOT_NS,DELG,org.getDomain(), access.name())) {
                    return Result.err(Status.ERR_Denied,
                            "[%s] may not %s delegates for [%s]", trans.user(),
                            access.name(), dd.user);
                }
                break;
            default:
                return Result.err(Status.ERR_BadData,"Unknown Access type [%s]", access.name());
            }
        } catch (Exception e) {
            return Result.err(e);
        }
        return Result.ok();
    }

    /*
     * Check (recursively, if necessary), if able to do something based on NS
     */
    private Result<NsDAO.Data> mayUserVirtueOfNS(AuthzTrans trans, String user,    NsDAO.Data nsd, String ns_and_type, String access) {
        String ns = nsd.name;

        // If an ADMIN of the Namespace, then allow
        
        Result<List<UserRoleDAO.Data>> rurd;
        if ((rurd = userRoleDAO.readUserInRole(trans, user, ns+DOT_ADMIN)).isOKhasData()) {
            return Result.ok(nsd);
        } else if (rurd.status==Result.ERR_Backend) {
            return Result.err(rurd);
        }
        
        // If Specially granted Global Permission
        if (isGranted(trans, user, ROOT_NS,NS, ns_and_type, access)) {
            return Result.ok(nsd);
        }

        // Check recur

        int dot = ns.length();
        if ((dot = ns.lastIndexOf('.', dot - 1)) >= 0) {
            Result<NsDAO.Data> rnsd = deriveNs(trans, ns.substring(0, dot));
            if (rnsd.isOK()) {
                rnsd = mayUserVirtueOfNS(trans, user, rnsd.value, ns_and_type,access);
            } else if (rnsd.status==Result.ERR_Backend) {
                return Result.err(rnsd);
            }
            if (rnsd.isOK()) {
                return Result.ok(nsd);
            } else if (rnsd.status==Result.ERR_Backend) {
                return Result.err(rnsd);
            }
        }
        return Result.err(Status.ERR_Denied, "%s may not %s %s", user, access,
                ns_and_type);
    }

    
    /**
     * isGranted
     * 
     * Important function - Check internal Permission Schemes for Permission to
     * do things
     * 
     * @param trans
     * @param type
     * @param instance
     * @param action
     * @return
     */
    public boolean isGranted(AuthzTrans trans, String user, String ns, String type,String instance, String action) {
        Result<List<PermDAO.Data>> perms = getPermsByUser(trans, user, false);
        if (perms.isOK()) {
            for (PermDAO.Data pd : perms.value) {
                if (ns.equals(pd.ns)) {
                    if (type.equals(pd.type)) {
                        if (PermEval.evalInstance(pd.instance, instance)) {
                            if (PermEval.evalAction(pd.action, action)) { // don't return action here, might miss other action 
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public Result<Date> doesUserCredMatch(AuthzTrans trans, String user, byte[] cred) throws DAOException {
        Result<List<CredDAO.Data>> result;
        TimeTaken tt = trans.start("Read DB Cred", Env.REMOTE);
        try {
            result = credDAO.readIDBAth(trans, user);
        } finally {
            tt.done();
        }

        Result<Date> rv = null;
        if (result.isOK()) {
            if (result.isEmpty()) {
                rv = Result.err(Status.ERR_UserNotFound, user);
                if (willSpecialLog(trans,user)) {
                    trans.audit().log("Special DEBUG:", user, " does not exist in DB");
                }
            } else {
                Date now = new Date();
                // Bug noticed 6/22. Sorting on the result can cause Concurrency Issues.     
                List<CredDAO.Data> cddl;
                if (result.value.size() > 1) {
                    cddl = new ArrayList<>(result.value.size());
                    for (CredDAO.Data old : result.value) {
                        if (old.type==CredDAO.BASIC_AUTH || old.type==CredDAO.BASIC_AUTH_SHA256) {
                            cddl.add(old);
                        }
                    }
                    if (cddl.size()>1) {
                        Collections.sort(cddl, (a, b) -> b.expires.compareTo(a.expires));
                    }
                } else {
                    cddl = result.value;
                }
    
                Date expired = null;
                StringBuilder debug = willSpecialLog(trans,user)?new StringBuilder():null;
                for (CredDAO.Data cdd : cddl) {
                    if (!cdd.id.equals(user)) {
                        trans.error().log("doesUserCredMatch DB call does not match for user: " + user);
                    }
                    if (cdd.expires.after(now)) {
                        byte[] dbcred = cdd.cred.array();
                        
                        try {
                            switch(cdd.type) {
                                case CredDAO.BASIC_AUTH:
                                    byte[] md5=Hash.hashMD5(cred);
                                    if (Hash.compareTo(md5,dbcred)==0) {
                                        checkLessThanDays(trans,cldays,now,cdd);
                                        trans.setTag(cdd.tag);
                                        return Result.ok(cdd.expires);
                                    } else if (debug!=null) {
                                        load(debug, cdd);
                                    }
                                    break;
                                case CredDAO.BASIC_AUTH_SHA256:
                                    ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE + cred.length);
                                    bb.putInt(cdd.other);
                                    bb.put(cred);
                                    byte[] hash = Hash.hashSHA256(bb.array());
    
                                    if (Hash.compareTo(hash,dbcred)==0) {
                                        checkLessThanDays(trans,cldays,now,cdd);
                                        trans.setTag(cdd.tag);
                                        return Result.ok(cdd.expires);
                                    } else if (debug!=null) {
                                        load(debug, cdd);
                                    }
                                    break;
                                default:
                                    trans.error().log("Unknown Credential Type %s for %s, %s",Integer.toString(cdd.type),cdd.id, Chrono.dateTime(cdd.expires));
                            }
                        } catch (NoSuchAlgorithmException e) {
                            trans.error().log(e);
                        }
                    } else {
                        if (expired==null || expired.before(cdd.expires)) {
                            expired = cdd.expires;
                            trans.setTag(cdd.tag);
                        }
                    }
                } // end for each
                
                if (expired!=null) {
                    // Note: this is only returned if there are no good Credentials
                    rv = Result.err(Status.ERR_Security,
                            "Credentials expired %s",Chrono.utcStamp(expired));
                } else {
                    if (debug==null && alwaysSpecial) {
                        debug = new StringBuilder();
                    }
                    if (debug!=null) {
                        debug.append(trans.env().encryptor().encrypt(new String(cred)));
                        rv = Result.err(Status.ERR_Security,String.format("invalid password - %s",debug.toString()));
                    }
                }
            }
        } else {
            return Result.err(result);
        }
        return rv == null ? Result.err(Status.ERR_Security, "Wrong credential") : rv;
    }


    private void load(StringBuilder debug, Data cdd) {
        debug.append("\nDB Entry: user=");
        debug.append(cdd.id);
        debug.append(",type=");
        debug.append(cdd.type);
        debug.append(",expires=");
        debug.append(Chrono.dateTime(cdd.expires));
        debug.append(",tag=");
        debug.append(cdd.tag);
        debug.append('\n');
    }


    private void checkLessThanDays(AuthzTrans trans, int days, Date now, Data cdd) {
        long close = now.getTime() + (days * 86400000);
        long cexp=cdd.expires.getTime();
        if (cexp<close) {
            int daysLeft = days-(int)((close-cexp)/86400000);
            trans.audit().printf("user=%s,ip=%s,expires=%s,days=%d,tag=%s,msg=\"Password expires in less than %d day%s\"",
                cdd.id,trans.ip(),Chrono.dateOnlyStamp(cdd.expires),daysLeft, cdd.tag, 
                daysLeft,daysLeft==1?"":"s");
        }
    }


    public Result<CredDAO.Data> userCredSetup(AuthzTrans trans, CredDAO.Data cred) {
        if (cred.type==CredDAO.RAW) {
            TimeTaken tt = trans.start("Hash Cred", Env.SUB);
            try {
                cred.type = CredDAO.BASIC_AUTH_SHA256;
                cred.other = random.nextInt();
                ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE + cred.cred.capacity());
                bb.putInt(cred.other);
                bb.put(cred.cred);
                byte[] hash = Hash.hashSHA256(bb.array());
                cred.cred = ByteBuffer.wrap(hash);
                return Result.ok(cred);
            } catch (NoSuchAlgorithmException e) {
                return Result.err(Status.ERR_General,e.getLocalizedMessage());
            } finally {
                tt.done();
            }
            
        } else if (cred.type==CredDAO.FQI) {
            cred.cred = null;
            return Result.ok(cred);
        }
        return Result.err(Status.ERR_Security,"invalid/unreadable credential");
    }
    
    public Result<Boolean> userCredCheck(AuthzTrans trans, CredDAO.Data orig, final byte[] raw) {
        Result<Boolean> rv;
        TimeTaken tt = trans.start("CheckCred Cred", Env.SUB);
        try {
            switch(orig.type) {
                case CredDAO.BASIC_AUTH_SHA256:
                    ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE + raw.length);
                    bb.putInt(orig.other);
                    bb.put(raw);
                    rv = Result.ok(Hash.compareTo(orig.cred.array(),Hash.hashSHA256(bb.array()))==0);
                case CredDAO.BASIC_AUTH:
                    rv= Result.ok( Hash.compareTo(orig.cred.array(), Hash.hashMD5(raw))==0);
                case CredDAO.FQI:
                default:
                    rv = Result.ok(false);
            }
        } catch (NoSuchAlgorithmException e) {
            rv = Result.err(Status.ERR_General,e.getLocalizedMessage());
        } finally {
            tt.done();
        }
        return rv;
    }

    public static final String APPROVED = "APPROVE";
    public static final String REJECT = "REJECT";
    public static final String PENDING = "PENDING";

    public Result<Void> canAddUser(AuthzTrans trans, UserRoleDAO.Data data,
            List<ApprovalDAO.Data> approvals) {
        // get the approval policy for the organization

        // get the list of approvals with an accept status

        // validate the approvals against the policy

        // for now check if all approvals are received and return
        // SUCCESS/FAILURE/SKIP
        boolean bReject = false;
        boolean bPending = false;

        for (ApprovalDAO.Data approval : approvals) {
            if (approval.status.equals(REJECT)) {
                bReject = true;
            } else if (approval.status.equals(PENDING)) {
                bPending = true;
            }
        }
        if (bReject) {
            return Result.err(Status.ERR_Policy,
                    "Approval Polocy not conformed");
        }
        if (bPending) {
            return Result.err(Status.ERR_ActionNotCompleted,
                    "Required Approvals not received");
        }

        return Result.ok();
    }

    private static final String NO_CACHE_NAME = "No Cache Data named %s";

    public Result<Void> clearCache(AuthzTrans trans, String cname) {
        boolean all = "all".equals(cname);
        Result<Void> rv = null;

        if (all || NsDAO.TABLE.equals(cname)) {
            int[] seg = series(NsDAO.CACHE_SEG);
            for (int i: seg) {cacheClear(trans, NsDAO.TABLE,i);}
            rv = cacheInfoDAO.touch(trans, NsDAO.TABLE, seg);
        }
        if (all || PermDAO.TABLE.equals(cname)) {
            int[] seg = series(PermDAO.CACHE_SEG);
            for (int i: seg) {cacheClear(trans, PermDAO.TABLE,i);}
            rv = cacheInfoDAO.touch(trans, PermDAO.TABLE,seg);
        }
        if (all || RoleDAO.TABLE.equals(cname)) {
            int[] seg = series(RoleDAO.CACHE_SEG);
            for (int i: seg) {cacheClear(trans, RoleDAO.TABLE,i);}
            rv = cacheInfoDAO.touch(trans, RoleDAO.TABLE,seg);
        }
        if (all || UserRoleDAO.TABLE.equals(cname)) {
            int[] seg = series(UserRoleDAO.CACHE_SEG);
            for (int i: seg) {cacheClear(trans, UserRoleDAO.TABLE,i);}
            rv = cacheInfoDAO.touch(trans, UserRoleDAO.TABLE,seg);
        }
        if (all || CredDAO.TABLE.equals(cname)) {
            int[] seg = series(CredDAO.CACHE_SEG);
            for (int i: seg) {cacheClear(trans, CredDAO.TABLE,i);}
            rv = cacheInfoDAO.touch(trans, CredDAO.TABLE,seg);
        }
        if (all || CertDAO.TABLE.equals(cname)) {
            int[] seg = series(CertDAO.CACHE_SEG);
            for (int i: seg) {cacheClear(trans, CertDAO.TABLE,i);}
            rv = cacheInfoDAO.touch(trans, CertDAO.TABLE,seg);
        }

        if (rv == null) {
            rv = Result.err(Status.ERR_BadData, NO_CACHE_NAME, cname);
        }
        return rv;
    }

    public Result<Void> cacheClear(AuthzTrans trans, String cname,Integer segment) {
        Result<Void> rv;
        if (NsDAO.TABLE.equals(cname)) {
            rv = nsDAO.invalidate(segment);
        } else if (PermDAO.TABLE.equals(cname)) {
            rv = permDAO.invalidate(segment);
        } else if (RoleDAO.TABLE.equals(cname)) {
            rv = roleDAO.invalidate(segment);
        } else if (UserRoleDAO.TABLE.equals(cname)) {
            rv = userRoleDAO.invalidate(segment);
        } else if (CredDAO.TABLE.equals(cname)) {
            rv = credDAO.invalidate(segment);
        } else if (CertDAO.TABLE.equals(cname)) {
            rv = certDAO.invalidate(segment);
        } else {
            rv = Result.err(Status.ERR_BadData, NO_CACHE_NAME, cname);
        }
        return rv;
    }

    private int[] series(int max) {
        int[] series = new int[max];
        for (int i = 0; i < max; ++i)
            series[i] = i;
        return series;
    }

    public boolean isDelegated(AuthzTrans trans, String user, String approver, Map<String,Result<List<DelegateDAO.Data>>> rldd ) {
        Result<List<DelegateDAO.Data>> userDelegatedFor = rldd.get(user);
        if (userDelegatedFor==null) {
            userDelegatedFor=delegateDAO.readByDelegate(trans, user);
            rldd.put(user, userDelegatedFor);
        }
        if (userDelegatedFor.isOKhasData()) {
            for (DelegateDAO.Data curr : userDelegatedFor.value) {
                if (curr.user.equals(approver) && curr.delegate.equals(user)
                        && curr.expires.after(new Date())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean willSpecialLog(AuthzTrans trans, String user) {
        Boolean b = trans.get(specialLogSlot, null);
        if (b==null) { // we haven't evaluated in this trans for Special Log yet
            if (specialLog==null) {
                return false;
            } else {
                b = specialLog.contains(user);
                trans.put(specialLogSlot, b);
            }
        }
        return b;
    }
    
    public static void logEncryptTrace(AuthzTrans trans, String data) {
        long ti;
        trans.put(transIDSlot, ti=nextTraceID());
        trans.trace().log("id="+Long.toHexString(ti)+",data=\""+trans.env().encryptor().encrypt(data)+'"');
    }

    private synchronized static long nextTraceID() {
        return ++traceID;
    }

    public static synchronized boolean specialLogOn(AuthzTrans trans, String id) {
        if (specialLog == null) {
            specialLog = new HashSet<>();
        }
        boolean rc = specialLog.add(id);
        if (rc) {
            trans.trace().printf("Trace on for %s requested by %s",id,trans.user());            
        }
        return rc;
    }

    public static synchronized boolean specialLogOff(AuthzTrans trans, String id) {
        if (specialLog==null) {
            return false;
        }
        boolean rv = specialLog.remove(id);
        if (specialLog.isEmpty()) {
            specialLog = null;
        }
        if (rv) {
            trans.trace().printf("Trace off for %s requested by %s",id,trans.user());            
        }
        return rv;
    }

    /** 
     * canMove
     * Which Types can be moved
     * @param nsType
     * @return
     */
    public boolean canMove(NsType nsType) {
        boolean rv;
        switch(nsType) {
            case DOT:
            case ROOT:
            case COMPANY:
            case UNKNOWN:
                rv = false;
                break;
            default:
                rv = true;
        }
        return rv;
    }

    public boolean isAdmin(AuthzTrans trans, String user, String ns) {
        Result<List<UserRoleDAO.Data>> rur = userRoleDAO.read(trans, user,ns+DOT_ADMIN);
        if (rur.isOKhasData()) {
            Date now = new Date();
            for (UserRoleDAO.Data urdd : rur.value){
                if (urdd.expires.after(now)) {
                    return true;
                }
            }
        };
        return false;
    }
    
    public boolean isOwner(AuthzTrans trans, String user, String ns) {
        Result<List<UserRoleDAO.Data>> rur = userRoleDAO().read(trans, user,ns+DOT_OWNER);
        if (rur.isOKhasData()) {for (UserRoleDAO.Data urdd : rur.value){
            Date now = new Date();
            if (urdd.expires.after(now)) {
                return true;
            }
        }};
        return false;
    }

    public int countOwner(AuthzTrans trans, String ns) {
        Result<List<UserRoleDAO.Data>> rur = userRoleDAO().readByRole(trans,ns+DOT_OWNER);
        Date now = new Date();
        int count = 0;
        if (rur.isOKhasData()) {for (UserRoleDAO.Data urdd : rur.value){
            if (urdd.expires.after(now)) {
                ++count;
            }
        }};
        return count;
    }
    
    /**
     * Return a Unique String, (same string, if it is already unique), with only
     * lowercase letters, digits and the '.' character.
     * 
     * @param name
     * @return
     * @throws IOException 
     */
    public static String toUnique(String name) throws IOException {
        byte[] from = name.getBytes();
        StringBuilder sb = new StringBuilder();
        byte f;
        for (int i=0;i<from.length;++i) {
            f=(byte)(from[i]); // printables;
            sb.append((char)((f>>4)+0x61));
            sb.append((char)((f&0x0F)+0x61));
        }
        return sb.toString();
    }
    
    public static String fromUnique(String name) throws IOException {
        byte[] from = name.getBytes();
        StringBuilder sb = new StringBuilder();
        char c;
        for (int i=0;i<from.length;++i) {
            c = (char)((from[i]-0x61)<<4);
            c |= (from[++i]-0x61);
            sb.append(c);
        }
        return sb.toString();
    }

}
