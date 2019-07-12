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

package org.onap.aaf.auth.service;

import static org.onap.aaf.auth.env.AuthzTrans.REQD_TYPE.force;
import static org.onap.aaf.auth.env.AuthzTrans.REQD_TYPE.future;
import static org.onap.aaf.auth.layer.Result.OK;
import static org.onap.aaf.auth.rserv.HttpMethods.DELETE;
import static org.onap.aaf.auth.rserv.HttpMethods.GET;
import static org.onap.aaf.auth.rserv.HttpMethods.POST;
import static org.onap.aaf.auth.rserv.HttpMethods.PUT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.DAOException;
import org.onap.aaf.auth.dao.cached.CachedPermDAO;
import org.onap.aaf.auth.dao.cached.CachedRoleDAO;
import org.onap.aaf.auth.dao.cached.CachedUserRoleDAO;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.CertDAO;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.DelegateDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.HistoryDAO;
import org.onap.aaf.auth.dao.cass.Namespace;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.NsDAO.Data;
import org.onap.aaf.auth.dao.cass.NsSplit;
import org.onap.aaf.auth.dao.cass.NsType;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.hl.CassExecutor;
import org.onap.aaf.auth.dao.hl.Function;
import org.onap.aaf.auth.dao.hl.Function.FUTURE_OP;
import org.onap.aaf.auth.dao.hl.Function.Lookup;
import org.onap.aaf.auth.dao.hl.Function.OP_STATUS;
import org.onap.aaf.auth.dao.hl.PermLookup;
import org.onap.aaf.auth.dao.hl.Question;
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
import org.onap.aaf.auth.rserv.doc.ApiDoc;
import org.onap.aaf.auth.service.mapper.Mapper;
import org.onap.aaf.auth.service.mapper.Mapper.API;
import org.onap.aaf.auth.service.validation.ServiceValidator;
import org.onap.aaf.auth.validation.Validator;
import org.onap.aaf.cadi.aaf.Defaults;
import org.onap.aaf.cadi.principal.BasicPrincipal;
import org.onap.aaf.cadi.util.FQI;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;
import org.onap.aaf.misc.env.util.Split;

import aaf.v2_0.CredRequest;

/**
 * AuthzCassServiceImpl implements AuthzCassService for 
 * 
 * @author Jonathan
 *
 * @param <NSS>
 * @param <PERMS>
 * @param <PERMKEY>
 * @param <ROLES>
 * @param <USERS>
 * @param <DELGS>
 * @param <REQUEST>
 * @param <HISTORY>
 * @param <ERR>
 * @param <APPROVALS>
 */
public class AuthzCassServiceImpl    <NSS,PERMS,PERMKEY,ROLES,USERS,USERROLES,DELGS,CERTS,KEYS,REQUEST,HISTORY,ERR,APPROVALS>
    implements AuthzService            <NSS,PERMS,PERMKEY,ROLES,USERS,USERROLES,DELGS,CERTS,KEYS,REQUEST,HISTORY,ERR,APPROVALS> {
    
    private static final String TWO_SPACE = "  ";
	private Mapper                    <NSS,PERMS,PERMKEY,ROLES,USERS,USERROLES,DELGS,CERTS,KEYS,REQUEST,HISTORY,ERR,APPROVALS> mapper;
    @Override
    public Mapper                    <NSS,PERMS,PERMKEY,ROLES,USERS,USERROLES,DELGS,CERTS,KEYS,REQUEST,HISTORY,ERR,APPROVALS> mapper() {return mapper;}
    
    private static final String ASTERIX = "*";
    private static final String CACHE = "cache";
    private static final String ROOT_NS = Define.ROOT_NS();
    private static final String ROOT_COMPANY = Define.ROOT_COMPANY();

    private final Question ques;
    private final Function func;
    
    public AuthzCassServiceImpl(AuthzTrans trans, Mapper<NSS,PERMS,PERMKEY,ROLES,USERS,USERROLES,DELGS,CERTS,KEYS,REQUEST,HISTORY,ERR,APPROVALS> mapper,Question question) {
        this.ques = question;
        func = new Function(trans, question);
        this.mapper = mapper;
        
    }

/***********************************
 * NAMESPACE 
 ***********************************/
    /**
     * createNS
     * @throws DAOException 
     * @see org.onap.aaf.auth.service.AuthzService#createNS(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String, java.lang.String)
     */
    @ApiDoc( 
            method = POST,  
            path = "/authz/ns",
            params = {},
            expectedCode = 201,
            errorCodes = { 403,404,406,409 }, 
            text = { "Namespace consists of: ",
                    "<ul><li>name - What you want to call this Namespace</li>",
                    "<li>responsible(s) - Person(s) who receive Notifications and approves Requests ",
                    "regarding this Namespace. Companies have Policies as to who may take on ",
                    "this Responsibility. Separate multiple identities with commas</li>",
                    "<li>admin(s) - Person(s) who are allowed to make changes on the namespace, ",
                    "including creating Roles, Permissions and Credentials. Separate multiple ",
                    "identities with commas</li></ul>",
                    "Note: Namespaces are dot-delimited (i.e. com.myCompany.myApp) and must be ",
                    "created with parent credentials (i.e. To create com.myCompany.myApp, you must ",
                    "be an admin of com.myCompany or com"
                    }
            )
    @Override
    public Result<Void> createNS(final AuthzTrans trans, REQUEST from, NsType type) {
        final Result<Namespace> rnamespace = mapper.ns(trans, from);
        final ServiceValidator v = new ServiceValidator();
        if (v.ns(rnamespace).err()) { 
            return Result.err(Status.ERR_BadData,v.errs());
        }
        final Namespace namespace = rnamespace.value;
        final Result<NsDAO.Data> parentNs = ques.deriveNs(trans,namespace.name);
        if (parentNs.notOK()) {
            return Result.err(parentNs);
        }
        
        // Note: Data validate occurs in func.createNS
        if (namespace.name.lastIndexOf('.')<0) { // Root Namespace... Function will check if allowed
            return func.createNS(trans, namespace, false);
        }
        
        Result<FutureDAO.Data> fd = mapper.future(trans, NsDAO.TABLE,from,namespace,true, 
                new Mapper.Memo() {
                    @Override
                    public String get() {
                        return "Create Namespace [" + namespace.name + ']';
                    }
                },
                new MayChange() {
                    private Result<NsDAO.Data> rnd;
                    @Override
                    public Result<?> mayChange() {
                        if (rnd==null) {
                            rnd = ques.mayUser(trans, trans.user(), parentNs.value,Access.write);
                        }
                        return rnd;
                    }
                });
            switch(fd.status) {
                case OK:
                    Result<String> rfc = func.createFuture(trans, fd.value, namespace.name, trans.user(),parentNs.value, FUTURE_OP.C);
                    if (rfc.isOK()) {
                        return Result.err(Status.ACC_Future, "NS [%s] is saved for future processing",namespace.name);
                    } else { 
                        return Result.err(rfc);
                    }
                case Status.ACC_Now:
                    return func.createNS(trans, namespace, false);
                default:
                    return Result.err(fd);
            }
    }
    
    @ApiDoc(
            method = POST,  
            path = "/authz/ns/:ns/admin/:id",
            params = {     "ns|string|true",
                        "id|string|true" 
                    },
            expectedCode = 201,
            errorCodes = { 403,404,406,409 }, 
            text = {     "Add an Identity :id to the list of Admins for the Namespace :ns", 
                        "Note: :id must be fully qualified (i.e. ab1234@people.osaaf.org)" }
            )
    @Override
    public Result<Void> addAdminNS(AuthzTrans trans, String ns, String id) {
        return func.addUserRole(trans, id, ns,Question.ADMIN);
    }

    @ApiDoc(
            method = DELETE,  
            path = "/authz/ns/:ns/admin/:id",
            params = {     "ns|string|true",
                        "id|string|true" 
                    },
            expectedCode = 200,
            errorCodes = { 403,404 }, 
            text = {     "Remove an Identity :id from the list of Admins for the Namespace :ns",
                        "Note: :id must be fully qualified (i.e. ab1234@people.osaaf.org)" }
            )
    @Override
    public Result<Void> delAdminNS(AuthzTrans trans, String ns, String id) {
        return func.delAdmin(trans,ns,id);
    }

    @ApiDoc(
            method = POST,  
            path = "/authz/ns/:ns/responsible/:id",
            params = {     "ns|string|true",
                        "id|string|true" 
                    },
            expectedCode = 201,
            errorCodes = { 403,404,406,409 }, 
            text = {     "Add an Identity :id to the list of Responsibles for the Namespace :ns",
                        "Note: :id must be fully qualified (i.e. ab1234@people.osaaf.org)" }
            )
    @Override
    public Result<Void> addResponsibleNS(AuthzTrans trans, String ns, String id) {
        return func.addUserRole(trans,id,ns,Question.OWNER);
    }

    @ApiDoc(
            method = DELETE,  
            path = "/authz/ns/:ns/responsible/:id",
            params = {     "ns|string|true",
                        "id|string|true" 
                    },
            expectedCode = 200,
            errorCodes = { 403,404 }, 
            text = {     "Remove an Identity :id to the list of Responsibles for the Namespace :ns",
                        "Note: :id must be fully qualified (i.e. ab1234@people.osaaf.org)",
                        "Note: A namespace must have at least 1 responsible party"
                    }
            )
    @Override
    public Result<Void> delResponsibleNS(AuthzTrans trans, String ns, String id) {
        return func.delOwner(trans,ns,id);
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.service.AuthzService#applyModel(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.Object)
     */
    @ApiDoc(
            method = POST,  
            path = "/authz/ns/:ns/attrib/:key/:value",
            params = {     "ns|string|true",
                        "key|string|true",
                        "value|string|true"},
            expectedCode = 201,
            errorCodes = { 403,404,406,409 },  
            text = {     
                "Create an attribute in the Namespace",
                "You must be given direct permission for key by AAF"
                }
            )
    @Override
    public Result<Void> createNsAttrib(AuthzTrans trans, String ns, String key, String value) {
        TimeTaken tt = trans.start("Create NsAttrib " + ns + ':' + key + ':' + value, Env.SUB);
        try {
            // Check inputs
            final Validator v = new ServiceValidator();
            if (v.ns(ns).err() ||
               v.key(key).err() ||
               v.value(value).err()) {
                return Result.err(Status.ERR_BadData,v.errs());
            }

            // Check if exists already
            Result<List<Data>> rlnsd = ques.nsDAO().read(trans, ns);
            if (rlnsd.notOKorIsEmpty()) {
                return Result.err(rlnsd);
            }
            NsDAO.Data nsd = rlnsd.value.get(0);

            // Check for Existence
            if (nsd.attrib.get(key)!=null) {
                return Result.err(Status.ERR_ConflictAlreadyExists, "NS Property %s:%s exists", ns, key);
            }
            
            // Check if User may put
            if (!ques.isGranted(trans, trans.user(), ROOT_NS, Question.ATTRIB, 
                    ":"+trans.org().getDomain()+".*:"+key, Access.write.name())) {
                return Result.err(Status.ERR_Denied, "%s may not create NS Attrib [%s:%s]", trans.user(),ns, key);
            }

            // Add Attrib
            nsd.attrib.put(key, value);
            ques.nsDAO().dao().attribAdd(trans,ns,key,value);
            ques.nsDAO().invalidate(trans, nsd);
            return Result.ok();
        } finally {
            tt.done();
        }
    }
    
    @ApiDoc(
            method = GET,  
            path = "/authz/ns/attrib/:key",
            params = {     "key|string|true" },
            expectedCode = 200,
            errorCodes = { 403,404 },  
            text = {     
                "Read Attributes for Namespace"
                }
            )
    @Override
    public Result<KEYS> readNsByAttrib(AuthzTrans trans, String key) {
        // Check inputs
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("Key",key).err()) {
              return Result.err(Status.ERR_BadData,v.errs());
        }

        // May Read
        if (!ques.isGranted(trans, trans.user(), ROOT_NS, Question.ATTRIB, 
                    ":"+trans.org().getDomain()+".*:"+key, Question.READ)) {
            return Result.err(Status.ERR_Denied,"%s may not read NS by Attrib '%s'",trans.user(),key);
        }

        Result<Set<String>> rsd = ques.nsDAO().dao().readNsByAttrib(trans, key);
        if (rsd.notOK()) {
            return Result.err(rsd);
        }
        return mapper().keys(rsd.value);
    }


    @ApiDoc(
            method = PUT,  
            path = "/authz/ns/:ns/attrib/:key/:value",
            params = {     "ns|string|true",
                        "key|string|true"},
            expectedCode = 200,
            errorCodes = { 403,404 },  
            text = {     
                "Update Value on an existing attribute in the Namespace",
                "You must be given direct permission for key by AAF"
                }
            )
    @Override
    public Result<?> updateNsAttrib(AuthzTrans trans, String ns, String key, String value) {
        TimeTaken tt = trans.start("Update NsAttrib " + ns + ':' + key + ':' + value, Env.SUB);
        try {
            // Check inputs
            final Validator v = new ServiceValidator();
            if (v.ns(ns).err() ||
               v.key(key).err() ||
               v.value(value).err()) {
                return Result.err(Status.ERR_BadData,v.errs());
            }

            // Check if exists already (NS must exist)
            Result<List<Data>> rlnsd = ques.nsDAO().read(trans, ns);
            if (rlnsd.notOKorIsEmpty()) {
                return Result.err(rlnsd);
            }
            NsDAO.Data nsd = rlnsd.value.get(0);

            // Check for Existence
            if (nsd.attrib.get(key)==null) {
                return Result.err(Status.ERR_NotFound, "NS Property %s:%s exists", ns, key);
            }
            
            // Check if User may put
            if (!ques.isGranted(trans, trans.user(), ROOT_NS, Question.ATTRIB, 
                    ":"+trans.org().getDomain()+".*:"+key, Access.write.name())) {
                return Result.err(Status.ERR_Denied, "%s may not create NS Attrib [%s:%s]", trans.user(),ns, key);
            }

            // Add Attrib
            nsd.attrib.put(key, value);
            ques.nsDAO().invalidate(trans, nsd);
            return ques.nsDAO().update(trans,nsd);
 
        } finally {
            tt.done();
        }
    }

    @ApiDoc(
            method = DELETE,  
            path = "/authz/ns/:ns/attrib/:key",
            params = {     "ns|string|true",
                        "key|string|true"},
            expectedCode = 200,
            errorCodes = { 403,404 },  
            text = {     
                "Delete an attribute in the Namespace",
                "You must be given direct permission for key by AAF"
                }
            )
    @Override
    public Result<Void> deleteNsAttrib(AuthzTrans trans, String ns, String key) {
        TimeTaken tt = trans.start("Delete NsAttrib " + ns + ':' + key, Env.SUB);
        try {
            // Check inputs
            final Validator v = new ServiceValidator();
            if (v.nullOrBlank("NS",ns).err() ||
               v.nullOrBlank("Key",key).err()) {
                return Result.err(Status.ERR_BadData,v.errs());
            }

            // Check if exists already
            Result<List<Data>> rlnsd = ques.nsDAO().read(trans, ns);
            if (rlnsd.notOKorIsEmpty()) {
                return Result.err(rlnsd);
            }
            NsDAO.Data nsd = rlnsd.value.get(0);

            // Check for Existence
            if (nsd.attrib.get(key)==null) {
                return Result.err(Status.ERR_NotFound, "NS Property [%s:%s] does not exist", ns, key);
            }
            
            // Check if User may del
            if (!ques.isGranted(trans, trans.user(), ROOT_NS, "attrib", ":" + ROOT_COMPANY + ".*:"+key, Access.write.name())) {
                return Result.err(Status.ERR_Denied, "%s may not delete NS Attrib [%s:%s]", trans.user(),ns, key);
            }

            // Add Attrib
            nsd.attrib.remove(key);
            ques.nsDAO().dao().attribRemove(trans,ns,key);
            ques.nsDAO().invalidate(trans, nsd);
            return Result.ok();
        } finally {
            tt.done();
        }
    }

    @ApiDoc(
            method = GET,  
            path = "/authz/nss/:id",
            params = {     "id|string|true" },
            expectedCode = 200,
            errorCodes = { 404,406 }, 
            text = {     
                "Lists the Owner(s), Admin(s), Description, and Attributes of Namespace :id",
            }
            )
    @Override
    public Result<NSS> getNSbyName(AuthzTrans trans, String ns, boolean includeExpired) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("NS", ns).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        
        Result<List<NsDAO.Data>> rlnd = ques.nsDAO().read(trans, ns);
        if (rlnd.isOK()) {
            if (rlnd.isEmpty()) {
                return Result.err(Status.ERR_NotFound, "No data found for %s",ns);
            }
            Result<NsDAO.Data> rnd = ques.mayUser(trans, trans.user(), rlnd.value.get(0), Access.read);
            if (rnd.notOK()) {
                return Result.err(rnd); 
            }
            
            
            Namespace namespace = new Namespace(rnd.value);
            Result<List<String>> rd = func.getOwners(trans, namespace.name, includeExpired);
            if (rd.isOK()) {
                namespace.owner = rd.value;
            }
            rd = func.getAdmins(trans, namespace.name, includeExpired);
            if (rd.isOK()) {
                namespace.admin = rd.value;
            }
            
            NSS nss = mapper.newInstance(API.NSS);
            return mapper.nss(trans, namespace, nss);
        } else {
            return Result.err(rlnd);
        }
    }

    @ApiDoc(
            method = GET,  
            path = "/authz/nss/admin/:id",
            params = {     "id|string|true" },
            expectedCode = 200,
            errorCodes = { 403,404 }, 
            text = {     "Lists all Namespaces where Identity :id is an Admin", 
                        "Note: :id must be fully qualified (i.e. ab1234@people.osaaf.org)" 
                    }
            )
    @Override
    public Result<NSS> getNSbyAdmin(AuthzTrans trans, String user, boolean full) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("User", user).err()) {
            return Result.err(Status.ERR_BadData, v.errs());
        }
        
        Result<Collection<Namespace>> rn = loadNamepace(trans, user, ".admin", full);
        if (rn.notOK()) {
            return Result.err(rn);
        }
        if (rn.isEmpty()) {
            return Result.err(Status.ERR_NotFound, "[%s] is not an admin for any namespaces",user);        
        }
        NSS nss = mapper.newInstance(API.NSS);
        // Note: "loadNamespace" already validates view of Namespace
        return mapper.nss(trans, rn.value, nss);
    }

    @ApiDoc(
            method = GET,  
            path = "/authz/nss/either/:id",
            params = {     "id|string|true" },
            expectedCode = 200,
            errorCodes = { 403,404 }, 
            text = {     "Lists all Namespaces where Identity :id is either an Admin or an Owner", 
                        "Note: :id must be fully qualified (i.e. ab1234@people.osaaf.org)" 
                    }
            )
    @Override
    public Result<NSS> getNSbyEither(AuthzTrans trans, String user, boolean full) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("User", user).err()) {
            return Result.err(Status.ERR_BadData, v.errs());
        }
        
        Result<Collection<Namespace>> rn = loadNamepace(trans, user, null, full);
        if (rn.notOK()) {
            return Result.err(rn);
        }
        if (rn.isEmpty()) {
            return Result.err(Status.ERR_NotFound, "[%s] is not an admin or owner for any namespaces",user);        
        }
        NSS nss = mapper.newInstance(API.NSS);
        // Note: "loadNamespace" already validates view of Namespace
        return mapper.nss(trans, rn.value, nss);
    }

    private Result<Collection<Namespace>> loadNamepace(AuthzTrans trans, String user, String endsWith, boolean full) {
        Result<List<UserRoleDAO.Data>> urd = ques.userRoleDAO().readByUser(trans, user);
        if (urd.notOKorIsEmpty()) {
            return Result.err(urd);
        }
        Map<String, Namespace> lm = new HashMap<>();
        Map<String, Namespace> other = full || endsWith==null?null:new TreeMap<>();
        for (UserRoleDAO.Data urdd : urd.value) {
            if (full) {
                if (endsWith==null || urdd.role.endsWith(endsWith)) {
                    RoleDAO.Data rd = RoleDAO.Data.decode(urdd);
                    Result<NsDAO.Data> nsd = ques.mayUser(trans, user, rd, Access.read);
                    if (nsd.isOK()) {
                        Namespace namespace = lm.get(nsd.value.name);
                        if (namespace==null) {
                            namespace = new Namespace(nsd.value);
                            lm.put(namespace.name,namespace);
                        }
                        Result<List<String>> rls = func.getAdmins(trans, namespace.name, false);
                        if (rls.isOK()) {
                            namespace.admin=rls.value;
                        }
                        
                        rls = func.getOwners(trans, namespace.name, false);
                        if (rls.isOK()) {
                            namespace.owner=rls.value;
                        }
                    }
                }
            } else { // Shortened version.  Only Namespace Info available from Role.
                if (Question.ADMIN.equals(urdd.rname) || Question.OWNER.equals(urdd.rname)) {
                    RoleDAO.Data rd = RoleDAO.Data.decode(urdd);
                    Result<NsDAO.Data> nsd = ques.mayUser(trans, user, rd, Access.read);
                    if (nsd.isOK()) {
                        Namespace namespace = lm.get(nsd.value.name);
                        if (namespace==null) {
                            if (other!=null) {
                                namespace = other.remove(nsd.value.name);
                            }
                            if (namespace==null) {
                                namespace = new Namespace(nsd.value);
                                namespace.admin=new ArrayList<>();
                                namespace.owner=new ArrayList<>();
                            }
                            if (endsWith==null || urdd.role.endsWith(endsWith)) {
                                lm.put(namespace.name,namespace);
                            } else { 
                                other.put(namespace.name,namespace);
                            }
                        }
                        if (Question.OWNER.equals(urdd.rname)) {
                            namespace.owner.add(urdd.user);
                        } else {
                            namespace.admin.add(urdd.user);
                        }
                    }
                }
            }
        }
        return Result.ok(lm.values());
    }

    @ApiDoc(
            method = GET,  
            path = "/authz/nss/responsible/:id",
            params = {     "id|string|true" },
            expectedCode = 200,
            errorCodes = { 403,404 }, 
            text = {     "Lists all Namespaces where Identity :id is a Responsible Party", 
                        "Note: :id must be fully qualified (i.e. ab1234@people.osaaf.org)"
                    }
            )
    @Override
    public Result<NSS> getNSbyResponsible(AuthzTrans trans, String user, boolean full) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("User", user).err()) {
            return Result.err(Status.ERR_BadData, v.errs());
        }
        Result<Collection<Namespace>> rn = loadNamepace(trans, user, ".owner",full);
        if (rn.notOK()) {
            return Result.err(rn);
        }
        if (rn.isEmpty()) {
            return Result.err(Status.ERR_NotFound, "[%s] is not an owner for any namespaces",user);        
        }
        NSS nss = mapper.newInstance(API.NSS);
        // Note: "loadNamespace" prevalidates
        return mapper.nss(trans, rn.value, nss);
    }
    
    @ApiDoc(
            method = GET,  
            path = "/authz/nss/children/:id",
            params = {     "id|string|true" },
            expectedCode = 200,
            errorCodes = { 403,404 }, 
            text = {     "Lists all Child Namespaces of Namespace :id", 
                        "Note: This is not a cached read"
                    }
            )
    @Override
    public Result<NSS> getNSsChildren(AuthzTrans trans, String parent) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("NS", parent).err())  {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        
        Result<NsDAO.Data> rnd = ques.deriveNs(trans, parent);
        if (rnd.notOK()) {
            return Result.err(rnd);
        }
        rnd = ques.mayUser(trans, trans.user(), rnd.value, Access.read);
        if (rnd.notOK()) {
            return Result.err(rnd); 
        }

        Set<Namespace> lm = new HashSet<>();
        Result<List<NsDAO.Data>> rlnd = ques.nsDAO().dao().getChildren(trans, parent);
        if (rlnd.isOK()) {
            if (rlnd.isEmpty()) {
                return Result.err(Status.ERR_NotFound, "No data found for %s",parent);
            }
            for (NsDAO.Data ndd : rlnd.value) {
                Namespace namespace = new Namespace(ndd);
                Result<List<String>> rls = func.getAdmins(trans, namespace.name, false);
                if (rls.isOK()) {
                    namespace.admin=rls.value;
                }
                
                rls = func.getOwners(trans, namespace.name, false);
                if (rls.isOK()) {
                    namespace.owner=rls.value;
                }

                lm.add(namespace);
            }
            NSS nss = mapper.newInstance(API.NSS);
            return mapper.nss(trans,lm, nss);
        } else {
            return Result.err(rlnd);
        }
    }


    @ApiDoc(
            method = PUT,  
            path = "/authz/ns",
            params = {},
            expectedCode = 200,
            errorCodes = { 403,404,406 }, 
            text = { "Replace the Current Description of a Namespace with a new one"
                    }
            )
    @Override
    public Result<Void> updateNsDescription(AuthzTrans trans, REQUEST from) {
        final Result<Namespace> nsd = mapper.ns(trans, from);
        final ServiceValidator v = new ServiceValidator();
        if (v.ns(nsd).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        if (v.nullOrBlank("description", nsd.value.description).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        Namespace namespace = nsd.value;
        Result<List<NsDAO.Data>> rlnd = ques.nsDAO().read(trans, namespace.name);
        
        if (rlnd.notOKorIsEmpty()) {
            return Result.err(Status.ERR_NotFound, "Namespace [%s] does not exist",namespace.name);
        }
        
        if (ques.mayUser(trans, trans.user(), rlnd.value.get(0), Access.write).notOK()) {
            return Result.err(Status.ERR_Denied, "You do not have approval to change %s",namespace.name);
        }

        Result<Void> rdr = ques.nsDAO().dao().addDescription(trans, namespace.name, namespace.description);
        if (rdr.isOK()) {
            return Result.ok();
        } else {
            return Result.err(rdr);
        }
    }
    
    /**
     * deleteNS
     * @throws DAOException 
     * @see org.onap.aaf.auth.service.AuthzService#deleteNS(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String, java.lang.String)
     */
    @ApiDoc(
            method = DELETE,  
            path = "/authz/ns/:ns",
            params = {     "ns|string|true" },
            expectedCode = 200,
            errorCodes = { 403,404,424 }, 
            text = {     "Delete the Namespace :ns. Namespaces cannot normally be deleted when there ",
                        "are still credentials associated with them, but they can be deleted by setting ",
                        "the \"force\" property. To do this: Add 'force=true' as a query parameter",
                        "<p>WARNING: Using force will delete all credentials attached to this namespace. Use with care.</p>"
                        + "if the \"force\" property is set to 'force=move', then Permissions and Roles are not deleted,"
                        + "but are retained, and assigned to the Parent Namespace.  'force=move' is not permitted "
                        + "at or below Application Scope"
                        }
            )
    @Override
    public Result<Void> deleteNS(AuthzTrans trans, String ns) {
        return func.deleteNS(trans, ns);
    }


/***********************************
 * PERM 
 ***********************************/

    /*
     * (non-Javadoc)
     * @see org.onap.aaf.auth.service.AuthzService#createOrUpdatePerm(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.Object, boolean, java.lang.String, java.lang.String, java.lang.String, java.util.List, java.util.List)
     */
    @ApiDoc( 
            method = POST,  
            path = "/authz/perm",
            params = {},
            expectedCode = 201,
            errorCodes = {403,404,406,409}, 
            text = { "Permission consists of:",
                     "<ul><li>type - a Namespace qualified identifier specifying what kind of resource "
                     + "is being protected</li>",
                     "<li>instance - a key, possibly multi-dimensional, that identifies a specific "
                     + " instance of the type</li>",
                     "<li>action - what kind of action is allowed</li></ul>",
                     "Note: instance and action can be an *"
                     }
            )
    @Override
    public Result<Void> createPerm(final AuthzTrans trans,REQUEST rreq) {        
        final Result<PermDAO.Data> newPd = mapper.perm(trans, rreq);

        final ServiceValidator v = new ServiceValidator();
        if (v.perm(newPd).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        // User Permission mechanism
        if(newPd.value.ns.indexOf('@')>0) {
        	PermDAO.Data pdd = newPd.value;
        	if(trans.user().equals(newPd.value.ns)) {
        		CachedPermDAO permDAO = ques.permDAO();
        		Result<List<PermDAO.Data>> rlpdd = permDAO.read(trans, pdd);
        		if(rlpdd.notOK()) {
        			return Result.err(rlpdd);
        		}
        		if(!rlpdd.isEmpty()) {
        			return Result.err(Result.ERR_ConflictAlreadyExists,"Permission already exists"); 
        		}

				RoleDAO.Data rdd = new RoleDAO.Data();
				rdd.ns = pdd.ns;
				rdd.name = "user";

				pdd.roles(true).add(rdd.fullName());
				Result<PermDAO.Data> rpdd = permDAO.create(trans, pdd);
				if(rpdd.notOK()) {
					return Result.err(rpdd);
				}
				
        		CachedRoleDAO roleDAO = ques.roleDAO();
        		Result<List<RoleDAO.Data>> rlrdd = roleDAO.read(trans, rdd);
        		if(rlrdd.notOK()) {
        			return Result.err(rlrdd);
        		} else {
        			if(!rlrdd.isEmpty()) {
        				rdd = rlrdd.value.get(0);
        			}
        		}
        		
        		String eperm = pdd.encode();
        		rdd.perms(true).add(eperm);
        		Result<Void> rv = roleDAO.update(trans, rdd);
        		if(rv.notOK()) {
        			return rv;
        		}
        		 
        		CachedUserRoleDAO urDAO = ques.userRoleDAO();
    			UserRoleDAO.Data urdd = new UserRoleDAO.Data();
    			urdd.user = trans.user();
    			urdd.ns = rdd.ns;
    			urdd.rname = rdd.name;
    			urdd.role = rdd.fullName();
        		Result<List<UserRoleDAO.Data>> rlurdd = urDAO.read(trans, urdd);
        		if(rlurdd.notOK()) {
        			return Result.err(rlrdd);
        		} else if(rlurdd.isEmpty()) {
        			GregorianCalendar gc = trans.org().expiration(null, Expiration.UserInRole);
        			if(gc==null) {
        				return Result.err(Result.ERR_Policy,"Organzation does not grant Expiration for UserRole");
        			} else {
        				urdd.expires = gc.getTime();
        			}
        			Result<UserRoleDAO.Data> rurdd = urDAO.create(trans, urdd);
        			return Result.err(rurdd);
        		}
        		return rv;
        	} else {
        		return Result.err(Result.ERR_Security,"Only the User can create User Permissions");
        	}
        } else {
	        // Does Perm Type exist as a Namespace?
	        if(newPd.value.type.isEmpty() || ques.nsDAO().read(trans, newPd.value.fullType()).isOKhasData()) {
	            return Result.err(Status.ERR_ConflictAlreadyExists,
	                    "Permission Type exists as a Namespace");
	        }
	        
	        Result<FutureDAO.Data> fd = mapper.future(trans, PermDAO.TABLE, rreq, newPd.value,false,
	            new Mapper.Memo() {
	                @Override
	                public String get() {
	                    return "Create Permission [" + 
	                        newPd.value.fullType() + '|' + 
	                        newPd.value.instance + '|' + 
	                        newPd.value.action + ']';
	                }
	            },
	            new MayChange() {
	                private Result<NsDAO.Data> nsd;
	                @Override
	                public Result<?> mayChange() {
	                    if (nsd==null) {
	                        nsd = ques.mayUser(trans, trans.user(), newPd.value, Access.write);
	                    }
	                    return nsd;
	                }
	            });
	        
	        Result<List<NsDAO.Data>> nsr = ques.nsDAO().read(trans, newPd.value.ns);
	        if (nsr.notOKorIsEmpty()) {
	            return Result.err(nsr);
	        }
	        switch(fd.status) {
	            case OK:
	                Result<String> rfc = func.createFuture(trans,fd.value, 
	                        newPd.value.fullType() + '|' + newPd.value.instance + '|' + newPd.value.action,
	                        trans.user(),
	                        nsr.value.get(0),
	                        FUTURE_OP.C);
	                if (rfc.isOK()) {
	                    return Result.err(Status.ACC_Future, "Perm [%s.%s|%s|%s] is saved for future processing",
	                            newPd.value.ns,
	                            newPd.value.type,
	                            newPd.value.instance,
	                            newPd.value.action);
	                } else {
	                    return Result.err(rfc);
	                }
	            case Status.ACC_Now:
	                return func.createPerm(trans, newPd.value, true);
	            default:
	                return Result.err(fd);
	        }
        }
    }

    @ApiDoc( 
            method = GET,  
            path = "/authz/perms/:type",
            params = {"type|string|true"},
            expectedCode = 200,
            errorCodes = { 404,406 }, 
            text = { "List All Permissions that match the :type element of the key" }
            )
    @Override
    public Result<PERMS> getPermsByType(AuthzTrans trans, final String permType) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("PermType", permType).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        Result<List<PermDAO.Data>> rlpd = ques.getPermsByType(trans, permType);
        if (rlpd.notOK()) {
            return Result.err(rlpd);
        }

//        We don't have instance & action for mayUserView... do we want to loop through all returned here as well as in mapper?
//        Result<NsDAO.Data> r;
//        if ((r = ques.mayUserViewPerm(trans, trans.user(), permType)).notOK())return Result.err(r);
        
        PERMS perms = mapper.newInstance(API.PERMS);
        if (!rlpd.isEmpty()) {
            // Note: Mapper will restrict what can be viewed
            return mapper.perms(trans, rlpd.value, perms, true);
        }
        return Result.ok(perms);
    }
    
    @ApiDoc( 
            method = GET,  
            path = "/authz/perms/:type/:instance/:action",
            params = {"type|string|true",
                      "instance|string|true",
                      "action|string|true"},
            expectedCode = 200,
            errorCodes = { 404,406 }, 
            text = { "List Permissions that match key; :type, :instance and :action" }
            )
    @Override
    public Result<PERMS> getPermsByName(AuthzTrans trans, String type, String instance, String action) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("PermType", type).err()
                || v.nullOrBlank("PermInstance", instance).err()
                || v.nullOrBlank("PermAction", action).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        
        Result<List<PermDAO.Data>> rlpd = ques.getPermsByName(trans, type, instance, action);
        if (rlpd.notOK()) {
            return Result.err(rlpd);
        }

        PERMS perms = mapper.newInstance(API.PERMS);
        if (!rlpd.isEmpty()) {
            // Note: Mapper will restrict what can be viewed
            return mapper.perms(trans, rlpd.value, perms, true);
        }
        return Result.ok(perms);
    }

    @ApiDoc( 
            method = GET,  
            path = "/authz/perms/user/:user",
            params = {"user|string|true"},
            expectedCode = 200,
            errorCodes = { 404,406 }, 
            text = { "List All Permissions that match user :user",
                     "<p>'user' must be expressed as full identity (ex: id@full.domain.com)</p>"}
            )
    @Override
    public Result<PERMS> getPermsByUser(AuthzTrans trans, String user) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("User", user).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        PermLookup pl = PermLookup.get(trans,ques,user);
        Result<List<PermDAO.Data>> rlpd = pl.getPerms(trans.requested(force));
        if (rlpd.notOK()) {
            return Result.err(rlpd);
        }
        
        PERMS perms = mapper.newInstance(API.PERMS);
        
        if (rlpd.isEmpty()) {
            return Result.ok(perms);
        }
        // Note: Mapper will restrict what can be viewed
        //   if user is the same as that which is looked up, no filtering is required
        return mapper.perms(trans, rlpd.value, 
                perms, 
                !user.equals(trans.user()));
    }

    @ApiDoc( 
            method = GET,  
            path = "/authz/perms/user/:user/scope/:scope",
            params = {"user|string|true","scope|string|true"},
            expectedCode = 200,
            errorCodes = { 404,406 }, 
            text = { "List All Permissions that match user :user, filtered by NS (Scope)",
                     "<p>'user' must be expressed as full identity (ex: id@full.domain.com)</p>",
                     "<p>'scope' must be expressed as NSs separated by ':'</p>"
                    }
            )
    @Override
    public Result<PERMS> getPermsByUserScope(AuthzTrans trans, String user, String[] scopes) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("User", user).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        Result<List<PermDAO.Data>> rlpd = ques.getPermsByUser(trans, user, trans.requested(force));
        if (rlpd.notOK()) {
            return Result.err(rlpd);
        }
        
        PERMS perms = mapper.newInstance(API.PERMS);
        
        if (rlpd.isEmpty()) {
            return Result.ok(perms);
        }
        // Note: Mapper will restrict what can be viewed
        //   if user is the same as that which is looked up, no filtering is required
        return mapper.perms(trans, rlpd.value, 
                perms, 
                scopes,
                !user.equals(trans.user()));
    }

    @ApiDoc( 
            method = POST,  
            path = "/authz/perms/user/:user",
            params = {"user|string|true"},
            expectedCode = 200,
            errorCodes = { 404,406 }, 
            text = { "List All Permissions that match user :user",
                     "<p>'user' must be expressed as full identity (ex: id@full.domain.com)</p>",
                     "",
                     "Present Queries as one or more Permissions (see ContentType Links below for format).",
                     "",
                     "If the Caller is Granted this specific Permission, and the Permission is valid",
                     "  for the User, it will be included in response Permissions, along with",
                     "  all the normal permissions on the 'GET' version of this call.  If it is not",
                     "  valid, or Caller does not have permission to see, it will be removed from the list",
                     "",
                     "  *Note: This design allows you to make one call for all expected permissions",
                     " The permission to be included MUST be:",
                     "     <user namespace>.access|:<ns|role|perm>[:key]|<create|read|write>",
                     "   examples:",
                     "     com.att.myns.access|:ns|write",
                     "     com.att.myns.access|:role:myrole|create",
                     "     com.att.myns.access|:perm:mytype:myinstance:myaction|read",
                     ""
                     }
            )
    @Override
    public Result<PERMS> getPermsByUser(AuthzTrans trans, PERMS _perms, String user) {
            PERMS perms = _perms;
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("User", user).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        
        //////////////
        PermLookup pl = PermLookup.get(trans,ques,user);
        Result<List<PermDAO.Data>> rlpd = pl.getPerms(trans.requested(force));
        if (rlpd.notOK()) {
            return Result.err(rlpd);
        }
        
        /*//TODO 
          1) See if allowed to query
          2) See if User is allowed
          */
        Result<List<PermDAO.Data>> in = mapper.perms(trans, perms);
        if (in.isOKhasData()) {
            List<PermDAO.Data> out = rlpd.value;
            boolean ok;
            for (PermDAO.Data pdd : in.value) {
                ok = false;
                if ("access".equals(pdd.type)) {
                    Access access = Access.valueOf(pdd.action);
                    String[] mdkey = Split.splitTrim(':',pdd.instance);
                    if (mdkey.length>1) {
                        String type = mdkey[1];
                        if ("role".equals(type)) {
                            if (mdkey.length>2) {
                                RoleDAO.Data rdd = new RoleDAO.Data();
                                rdd.ns=pdd.ns;
                                rdd.name=mdkey[2];
                                ok = ques.mayUser(trans, trans.user(), rdd, Access.read).isOK() && ques.mayUser(trans, user, rdd , access).isOK();
                            }
                        } else if ("perm".equals(type)) {
                            if (mdkey.length>4) { // also need instance/action
                                PermDAO.Data p = new PermDAO.Data();
                                p.ns=pdd.ns;
                                p.type=mdkey[2];
                                p.instance=mdkey[3];
                                p.action=mdkey[4];
                                ok = ques.mayUser(trans, trans.user(), p, Access.read).isOK() && ques.mayUser(trans, user, p , access).isOK();
                            }
                        } else if ("ns".equals(type)) {
                            NsDAO.Data ndd = new NsDAO.Data();
                            ndd.name=pdd.ns;
                            ok = ques.mayUser(trans, trans.user(), ndd, Access.read).isOK() && ques.mayUser(trans, user, ndd , access).isOK();
                        }
                    }
                }
                if (ok) {
                    out.add(pdd);
                }
            }
        }        
        
        perms = mapper.newInstance(API.PERMS);
        if (rlpd.isEmpty()) {
            return Result.ok(perms);
        }
        // Note: Mapper will restrict what can be viewed
        //   if user is the same as that which is looked up, no filtering is required
        return mapper.perms(trans, rlpd.value, 
                perms, 
                !user.equals(trans.user()));
    }
    
    @ApiDoc( 
            method = GET,  
            path = "/authz/perms/role/:role",
            params = {"role|string|true"},
            expectedCode = 200,
            errorCodes = { 404,406 }, 
            text = { "List All Permissions that are granted to :role" }
            )
    @Override
    public Result<PERMS> getPermsByRole(AuthzTrans trans,String role) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("Role", role).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        Result<RoleDAO.Data> rrdd = RoleDAO.Data.decode(trans, ques,role);
        if (rrdd.notOK()) {
            return Result.err(rrdd);
        }

        Result<NsDAO.Data> r = ques.mayUser(trans, trans.user(), rrdd.value, Access.read);
        if (r.notOK()) {
            return Result.err(r);
        }

        PERMS perms = mapper.newInstance(API.PERMS);

        Result<List<PermDAO.Data>> rlpd = ques.getPermsByRole(trans, role, trans.requested(force));
        if (rlpd.isOKhasData()) {
            // Note: Mapper will restrict what can be viewed
            return mapper.perms(trans, rlpd.value, perms, true);
        }
        return Result.ok(perms);
    }

    @ApiDoc( 
            method = GET,  
            path = "/authz/perms/ns/:ns",
            params = {"ns|string|true"},
            expectedCode = 200,
            errorCodes = { 404,406 }, 
            text = { "List All Permissions that are in Namespace :ns" }
            )
    @Override
    public Result<PERMS> getPermsByNS(AuthzTrans trans,String ns) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("NS", ns).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        Result<NsDAO.Data> rnd = ques.deriveNs(trans, ns);
        if (rnd.notOK()) {
            return Result.err(rnd);
        }

        rnd = ques.mayUser(trans, trans.user(), rnd.value, Access.read);
        if (rnd.notOK()) {
            return Result.err(rnd);     
        }
        
        Result<List<PermDAO.Data>> rlpd = ques.permDAO().readNS(trans, ns);
        if (rlpd.notOK()) {
            return Result.err(rlpd);
        }

        PERMS perms = mapper.newInstance(API.PERMS);
        if (!rlpd.isEmpty()) {
            // Note: Mapper will restrict what can be viewed
            return mapper.perms(trans, rlpd.value,perms, true);
        }
        return Result.ok(perms);
    }
    
    @ApiDoc( 
            method = PUT,  
            path =     "/authz/perm/:type/:instance/:action",
            params = {"type|string|true",
                      "instance|string|true",
                        "action|string|true"},
            expectedCode = 200,
            errorCodes = { 404,406, 409 }, 
            text = { "Rename the Permission referenced by :type :instance :action, and "
                    + "rename (copy/delete) to the Permission described in PermRequest" }
            )
    @Override
    public Result<Void> renamePerm(final AuthzTrans trans,REQUEST rreq, String origType, String origInstance, String origAction) {
        final Result<PermDAO.Data> newPd = mapper.perm(trans, rreq);
        final ServiceValidator v = new ServiceValidator();
        if (v.perm(newPd).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        if (ques.mayUser(trans, trans.user(), newPd.value,Access.write).notOK()) {
            return Result.err(Status.ERR_Denied, "You do not have approval to change Permission [%s.%s|%s|%s]",
                    newPd.value.ns,newPd.value.type,newPd.value.instance,newPd.value.action);
        }
        
        Result<NsSplit> nss = ques.deriveNsSplit(trans, origType);
        Result<List<PermDAO.Data>> origRlpd = ques.permDAO().read(trans, nss.value.ns, nss.value.name, origInstance, origAction); 
        
        if (origRlpd.notOKorIsEmpty()) {
            return Result.err(Status.ERR_PermissionNotFound, 
                    "Permission [%s|%s|%s] does not exist",
                    origType,origInstance,origAction);
        }
        
        PermDAO.Data origPd = origRlpd.value.get(0);

        if (!origPd.ns.equals(newPd.value.ns)) {
            return Result.err(Status.ERR_Denied, "Cannot change namespace with rename command. " +
                    "<new type> must start with [" + origPd.ns + "]");
        }
        
        if ( origPd.type.equals(newPd.value.type) && 
                origPd.action.equals(newPd.value.action) && 
                origPd.instance.equals(newPd.value.instance) ) {
            return Result.err(Status.ERR_ConflictAlreadyExists, "New Permission must be different than original permission");
        }
        
        Set<String> origRoles = origPd.roles(false);
        if (!origRoles.isEmpty()) {
            Set<String> roles = newPd.value.roles(true);
            for (String role : origPd.roles) {
                roles.add(role); 
            }
        }    
        
        newPd.value.description = origPd.description;
        
        Result<Void> rv = null;
        
        rv = func.createPerm(trans, newPd.value, false);
        if (rv.isOK()) {
            rv = func.deletePerm(trans, origPd, true, false);
        }
        return rv;
    }
    
    @ApiDoc( 
            method = PUT,  
            path = "/authz/perm",
            params = {},
            expectedCode = 200,
            errorCodes = { 404,406 }, 
            text = { "Add Description Data to Perm" }
            )
    @Override
    public Result<Void> updatePermDescription(AuthzTrans trans, REQUEST from) {
        final Result<PermDAO.Data> pd = mapper.perm(trans, from);
        final ServiceValidator v = new ServiceValidator();
        if (v.perm(pd).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        if (v.nullOrBlank("description", pd.value.description).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        final PermDAO.Data perm = pd.value;
        if (ques.permDAO().read(trans, perm.ns, perm.type, perm.instance,perm.action).notOKorIsEmpty()) {
            return Result.err(Status.ERR_NotFound, "Permission [%s.%s|%s|%s] does not exist",
                perm.ns,perm.type,perm.instance,perm.action);
        }

        if (ques.mayUser(trans, trans.user(), perm, Access.write).notOK()) {
            return Result.err(Status.ERR_Denied, "You do not have approval to change Permission [%s.%s|%s|%s]",
                    perm.ns,perm.type,perm.instance,perm.action);
        }

        Result<List<NsDAO.Data>> nsr = ques.nsDAO().read(trans, pd.value.ns);
        if (nsr.notOKorIsEmpty()) {
            return Result.err(nsr);
        }

        Result<Void> rdr = ques.permDAO().addDescription(trans, perm.ns, perm.type, perm.instance,
                perm.action, perm.description);
        if (rdr.isOK()) {
            return Result.ok();
        } else {
            return Result.err(rdr);
        }

    }
    
    @ApiDoc(
            method = PUT,
            path = "/authz/role/perm",
            params = {},
            expectedCode = 201,
            errorCodes = {403,404,406,409},
            text = { "Set a permission's roles to roles given" }
           )

    @Override
    public Result<Void> resetPermRoles(final AuthzTrans trans, REQUEST rreq) {
        final Result<PermDAO.Data> updt = mapper.permFromRPRequest(trans, rreq);
        if (updt.notOKorIsEmpty()) {
            return Result.err(updt);
        }

        final ServiceValidator v = new ServiceValidator();
        if (v.perm(updt).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        Result<NsDAO.Data> nsd = ques.mayUser(trans, trans.user(), updt.value, Access.write);
        if (nsd.notOK()) {
            return Result.err(nsd);
        }

        // Read full set to get CURRENT values
        Result<List<PermDAO.Data>> rcurr = ques.permDAO().read(trans, 
                updt.value.ns, 
                updt.value.type, 
                updt.value.instance, 
                updt.value.action);
        
        if (rcurr.notOKorIsEmpty()) {
            return Result.err(Status.ERR_PermissionNotFound, 
                    "Permission [%s.%s|%s|%s] does not exist",
                     updt.value.ns,updt.value.type,updt.value.instance,updt.value.action);
        }
        
        // Create a set of Update Roles, which are in Internal Format
        Set<String> updtRoles = new HashSet<>();
        Result<NsSplit> nss;
        for (String role : updt.value.roles(false)) {
            nss = ques.deriveNsSplit(trans, role);
            if (nss.isOK()) {
                updtRoles.add(nss.value.ns + '|' + nss.value.name);
            } else {
                trans.error().log(nss.errorString());
            }
        }

        Result<Void> rv = null;
        
        for (PermDAO.Data curr : rcurr.value) {
            Set<String> currRoles = curr.roles(false);
            // must add roles to this perm, and add this perm to each role 
            // in the update, but not in the current            
            for (String role : updtRoles) {
                if (!currRoles.contains(role)) {
                    Result<RoleDAO.Data> key = RoleDAO.Data.decode(trans, ques, role);
                    if (key.isOKhasData()) {
                        Result<List<RoleDAO.Data>> rrd = ques.roleDAO().read(trans, key.value);
                        if (rrd.isOKhasData()) {
                            for (RoleDAO.Data r : rrd.value) {
                                rv = func.addPermToRole(trans, r, curr, false);
                                if (rv.notOK() && rv.status!=Result.ERR_ConflictAlreadyExists) {
                                    return Result.err(rv);
                                }
                            }
                        } else {
                            return Result.err(rrd);
                        }
                    }
                }
            }
            // similarly, must delete roles from this perm, and delete this perm from each role
            // in the update, but not in the current
            for (String role : currRoles) {
                if (!updtRoles.contains(role)) {
                    Result<RoleDAO.Data> key = RoleDAO.Data.decode(trans, ques, role);
                    if (key.isOKhasData()) {
                        Result<List<RoleDAO.Data>> rdd = ques.roleDAO().read(trans, key.value);
                        if (rdd.isOKhasData()) {
                            for (RoleDAO.Data r : rdd.value) {
                                rv = func.delPermFromRole(trans, r, curr, true);
                                if (rv.notOK() && rv.status!=Status.ERR_PermissionNotFound) {
                                    return Result.err(rv);
                                }
                            }
                        }
                    }
                }
            }                
        } 
        return rv==null?Result.ok():rv;        
    }
    
    @ApiDoc( 
            method = DELETE,
            path = "/authz/perm",
            params = {},
            expectedCode = 200,
            errorCodes = { 404,406 }, 
            text = { "Delete the Permission referenced by PermKey.",
                    "You cannot normally delete a permission which is still granted to roles,",
                    "however the \"force\" property allows you to do just that. To do this: Add",
                    "'force=true' as a query parameter.",
                    "<p>WARNING: Using force will ungrant this permission from all roles. Use with care.</p>" }
            )
    @Override
    public Result<Void> deletePerm(final AuthzTrans trans, REQUEST from) {
        Result<PermDAO.Data> pd = mapper.perm(trans, from);
        if (pd.notOK()) {
            return Result.err(pd);
        }
        final ServiceValidator v = new ServiceValidator();
        if (v.nullOrBlank(pd.value).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        final PermDAO.Data perm = pd.value;
        if (ques.permDAO().read(trans, perm).notOKorIsEmpty()) {
            return Result.err(Status.ERR_PermissionNotFound, "Permission [%s.%s|%s|%s] does not exist",
                    perm.ns,perm.type,perm.instance,perm.action    );
        }
        
        Result<FutureDAO.Data> fd = mapper.future(trans,PermDAO.TABLE,from,perm,false,
                new Mapper.Memo() {
                    @Override
                    public String get() {
                        return "Delete Permission [" + perm.fullPerm() + ']';
                    }
                },
            new MayChange() {
                private Result<NsDAO.Data> nsd;
                @Override
                public Result<?> mayChange() {
                    if (nsd==null) {
                        nsd = ques.mayUser(trans, trans.user(), perm, Access.write);
                    }
                    return nsd;
                }
            });
        
        switch(fd.status) {
        case OK:
            Result<List<NsDAO.Data>> nsr = ques.nsDAO().read(trans, perm.ns);
            if (nsr.notOKorIsEmpty()) {
                return Result.err(nsr);
            }
            
            Result<String> rfc = func.createFuture(trans, fd.value, 
                    perm.encode(), trans.user(),nsr.value.get(0),FUTURE_OP.D);
            if (rfc.isOK()) {
                return Result.err(Status.ACC_Future, "Perm Deletion [%s] is saved for future processing",perm.encode());
            } else { 
                return Result.err(rfc);
            }
        case Status.ACC_Now:
            return func.deletePerm(trans,perm,trans.requested(force), false);
        default:
            return Result.err(fd);
        }            
    }    
    
    @ApiDoc( 
            method = DELETE,
            path = "/authz/perm/:name/:type/:action",
            params = {"type|string|true",
                      "instance|string|true",
                          "action|string|true"},
            expectedCode = 200,
            errorCodes = { 404,406 }, 
            text = { "Delete the Permission referenced by :type :instance :action",
                    "You cannot normally delete a permission which is still granted to roles,",
                    "however the \"force\" property allows you to do just that. To do this: Add",
                    "'force=true' as a query parameter",
                    "<p>WARNING: Using force will ungrant this permission from all roles. Use with care.</p>"}
            )
    @Override
    public Result<Void> deletePerm(AuthzTrans trans, String type, String instance, String action) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("Type",type)
            .nullOrBlank("Instance",instance)
            .nullOrBlank("Action",action)
            .err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        
        Result<PermDAO.Data> pd = ques.permFrom(trans, type, instance, action);
        if (pd.isOK()) {
            return func.deletePerm(trans, pd.value, trans.requested(force), false);
        } else {
            return Result.err(pd);
        }
    }

/***********************************
 * ROLE 
 ***********************************/
    @ApiDoc(
            method = POST,
            path = "/authz/role",
            params = {},
            expectedCode = 201,
            errorCodes = {403,404,406,409},
            text = {

                "Roles are part of Namespaces",
                "Examples:",
                "<ul><li>org.onap.aaf - The team that created and maintains AAF</li>",
                "Roles do not include implied permissions for an App.  Instead, they contain explicit Granted Permissions by any Namespace in AAF (See Permissions)",
                "Restrictions on Role Names:",
                "<ul><li>Must start with valid Namespace name, terminated by . (dot/period)</li>",
                "<li>Allowed Characters are a-zA-Z0-9._-</li>",
                "<li>role names are Case Sensitive</li></ul>",
                "The right questions to ask for defining and populating a Role in AAF, therefore, are:",
                "<ul><li>'What Job Function does this represent?'</li>",
                "<li>'Does this person perform this Job Function?'</li></ul>" }
           )

    @Override
    public Result<Void> createRole(final AuthzTrans trans, REQUEST from) {
        final Result<RoleDAO.Data> rd = mapper.role(trans, from);
        // Does Perm Type exist as a Namespace?
        if(rd.value.name.isEmpty() || ques.nsDAO().read(trans, rd.value.fullName()).isOKhasData()) {
            return Result.err(Status.ERR_ConflictAlreadyExists,
                    "Role exists as a Namespace");
        }
        final ServiceValidator v = new ServiceValidator();
        if (v.role(rd).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        final RoleDAO.Data role = rd.value;
        if (ques.roleDAO().read(trans, role.ns, role.name).isOKhasData()) {
            return Result.err(Status.ERR_ConflictAlreadyExists, "Role [" + role.fullName() + "] already exists");
        }

        Result<FutureDAO.Data> fd = mapper.future(trans,RoleDAO.TABLE,from,role,false,
            new Mapper.Memo() {
                @Override
                public String get() {
                    return "Create Role [" + 
                        rd.value.fullName() + 
                        ']';
                }
            },
            new MayChange() {
                private Result<NsDAO.Data> nsd;
                @Override
                public Result<?> mayChange() {
                    if (nsd==null) {
                        nsd = ques.mayUser(trans, trans.user(), role, Access.write);
                    }
                    return nsd;
                }
            });
        
        Result<List<NsDAO.Data>> nsr = ques.nsDAO().read(trans, rd.value.ns);
        if (nsr.notOKorIsEmpty()) {
            return Result.err(nsr);
        }

        switch(fd.status) {
            case OK:
                Result<String> rfc = func.createFuture(trans, fd.value, 
                        role.encode(), trans.user(),nsr.value.get(0),FUTURE_OP.C);
                if (rfc.isOK()) {
                    return Result.err(Status.ACC_Future, "Role [%s.%s] is saved for future processing",
                            rd.value.ns,
                            rd.value.name);
                } else { 
                    return Result.err(rfc);
                }
            case Status.ACC_Now:
                Result<RoleDAO.Data> rdr = ques.roleDAO().create(trans, role);
                if (rdr.isOK()) {
                    return Result.ok();
                } else {
                    return Result.err(rdr);
                }
            default:
                return Result.err(fd);
        }
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.service.AuthzService#getRolesByName(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String)
     */
    @ApiDoc(
            method = GET,
            path = "/authz/roles/:role",
            params = {"role|string|true"}, 
            expectedCode = 200,
            errorCodes = {404,406},
            text = { "List Roles that match :role",
                     "Note: You must have permission to see any given role"
                   }
           )
    @Override
    public Result<ROLES> getRolesByName(AuthzTrans trans, String role) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("Role", role).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        
        // Determine if User can ask this question
        Result<RoleDAO.Data> rrdd = RoleDAO.Data.decode(trans, ques, role);
        if (rrdd.isOKhasData()) {
            Result<NsDAO.Data> r;
            if ((r = ques.mayUser(trans, trans.user(), rrdd.value, Access.read)).notOK()) {
                return Result.err(r);
            }
        } else {
            return Result.err(rrdd);
        }
        
        // Look up data
        int query = role.indexOf('?');
        Result<List<RoleDAO.Data>> rlrd = ques.getRolesByName(trans, query<0?role:role.substring(0, query));
        if (rlrd.isOK()) {
            // Note: Mapper will restrict what can be viewed
            ROLES roles = mapper.newInstance(API.ROLES);
            return mapper.roles(trans, rlrd.value, roles, true);
        } else {
            return Result.err(rlrd);
        }
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.service.AuthzService#getRolesByUser(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String)
     */
    @ApiDoc(
            method = GET,
            path = "/authz/roles/user/:name",
            params = {"name|string|true"},
            expectedCode = 200,
            errorCodes = {404,406},
            text = { "List all Roles that match user :name",
                     "'user' must be expressed as full identity (ex: id@full.domain.com)",
                        "Note: You must have permission to see any given role"
            }
           )

    @Override
    public Result<ROLES> getRolesByUser(AuthzTrans trans, String user) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("User", user).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        ROLES roles = mapper.newInstance(API.ROLES);
        // Get list of roles per user, then add to Roles as we go
        Result<List<RoleDAO.Data>> rlrd;
        Result<List<UserRoleDAO.Data>> rlurd = ques.userRoleDAO().readByUser(trans, user);
        if (rlurd.isOKhasData()) {
            for (UserRoleDAO.Data urd : rlurd.value ) {
                rlrd = ques.roleDAO().read(trans, urd.ns,urd.rname);
                // Note: Mapper will restrict what can be viewed
                //   if user is the same as that which is looked up, no filtering is required
                if (rlrd.isOKhasData()) {
                    mapper.roles(trans, rlrd.value,roles, !user.equals(trans.user()));
                }
            }
        }
        return Result.ok(roles);
    }

    /*
     * (non-Javadoc)
     * @see org.onap.aaf.auth.service.AuthzService#getRolesByNS(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String)
     */
    @ApiDoc(
            method = GET,
            path = "/authz/roles/ns/:ns",
            params = {"ns|string|true"},
            expectedCode = 200,
            errorCodes = {404,406},
            text = { "List all Roles for the Namespace :ns", 
                         "Note: You must have permission to see any given role"
            }
           )

    @Override
    public Result<ROLES> getRolesByNS(AuthzTrans trans, String ns) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("NS", ns).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        
        // check if user is allowed to view NS
        Result<NsDAO.Data> rnsd = ques.deriveNs(trans, ns); 
        if (rnsd.notOK()) {
            return Result.err(rnsd);     
        }
        rnsd = ques.mayUser(trans, trans.user(), rnsd.value, Access.read);
        if (rnsd.notOK()) {
            return Result.err(rnsd);     
        }

        TimeTaken tt = trans.start("MAP Roles by NS to Roles", Env.SUB);
        try {
            ROLES roles = mapper.newInstance(API.ROLES);
            // Get list of roles per user, then add to Roles as we go
            Result<List<RoleDAO.Data>> rlrd = ques.roleDAO().readNS(trans, ns);
            if (rlrd.isOK()) {
                if (!rlrd.isEmpty()) {
                    // Note: Mapper doesn't need to restrict what can be viewed, because we did it already.
                    mapper.roles(trans,rlrd.value,roles,false);
                }
                return Result.ok(roles);
            } else {
                return Result.err(rlrd);
            }
        } finally {
            tt.done();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.onap.aaf.auth.service.AuthzService#getRolesByNS(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String)
     */
    @ApiDoc(
            method = GET,
            path = "/authz/roles/name/:name",
            params = {"name|string|true"},
            expectedCode = 200,
            errorCodes = {404,406},
            text = { "List all Roles for only the Name of Role (without Namespace)", 
                         "Note: You must have permission to see any given role"
            }
           )
    @Override
    public Result<ROLES> getRolesByNameOnly(AuthzTrans trans, String name) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("Name", name).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        
        // User Mapper to make sure user is allowed to view NS

        TimeTaken tt = trans.start("MAP Roles by Name to Roles", Env.SUB);
        try {
            ROLES roles = mapper.newInstance(API.ROLES);
            // Get list of roles per user, then add to Roles as we go
            Result<List<RoleDAO.Data>> rlrd = ques.roleDAO().readName(trans, name);
            if (rlrd.isOK()) {
                if (!rlrd.isEmpty()) {
                    // Note: Mapper will restrict what can be viewed
                    mapper.roles(trans,rlrd.value,roles,true);
                }
                return Result.ok(roles);
            } else {
                return Result.err(rlrd);
            }
        } finally {
            tt.done();
        }
    }

    @ApiDoc(
            method = GET,
            path = "/authz/roles/perm/:type/:instance/:action",
            params = {"type|string|true",
                      "instance|string|true",
                      "action|string|true"},
            expectedCode = 200,
            errorCodes = {404,406},
            text = { "Find all Roles containing the given Permission." +
                     "Permission consists of:",
                     "<ul><li>type - a Namespace qualified identifier specifying what kind of resource "
                     + "is being protected</li>",
                     "<li>instance - a key, possibly multi-dimensional, that identifies a specific "
                     + " instance of the type</li>",
                     "<li>action - what kind of action is allowed</li></ul>",
                     "Notes: instance and action can be an *",
                     "       You must have permission to see any given role"
                     }
           )

    @Override
    public Result<ROLES> getRolesByPerm(AuthzTrans trans, String type, String instance, String action) {
        final Validator v = new ServiceValidator();
        if (v.permType(type)
            .permInstance(instance)
            .permAction(action)
            .err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        
        TimeTaken tt = trans.start("Map Perm Roles Roles", Env.SUB);
        try {
            ROLES roles = mapper.newInstance(API.ROLES);
            // Get list of roles per user, then add to Roles as we go
            Result<NsSplit> nsSplit = ques.deriveNsSplit(trans, type);
            if (nsSplit.isOK()) {
                PermDAO.Data pdd = new PermDAO.Data(nsSplit.value, instance, action);
                Result<?> res;
                if ((res=ques.mayUser(trans, trans.user(), pdd, Question.Access.read)).notOK()) {
                    return Result.err(res);
                }
                
                Result<List<PermDAO.Data>> pdlr = ques.permDAO().read(trans, pdd);
                if (pdlr.isOK())for (PermDAO.Data pd : pdlr.value) {
                    Result<List<RoleDAO.Data>> rlrd;
                    for (String r : pd.roles) {
                        Result<String[]> rs = RoleDAO.Data.decodeToArray(trans, ques, r);
                        if (rs.isOK()) {
                            rlrd = ques.roleDAO().read(trans, rs.value[0],rs.value[1]);
                            // Note: Mapper will restrict what can be viewed
                            if (rlrd.isOKhasData()) {
                                mapper.roles(trans,rlrd.value,roles,true);
                            }
                        }
                    }
                }
            }
            return Result.ok(roles);
        } finally {
            tt.done();
        }
    }

    @ApiDoc(
            method = PUT,
            path = "/authz/role",
            params = {},
            expectedCode = 200,
            errorCodes = {404,406},
            text = { "Add Description Data to a Role" }
           )

    @Override
    public Result<Void> updateRoleDescription(AuthzTrans trans, REQUEST from) {
        final Result<RoleDAO.Data> rd = mapper.role(trans, from);
        final ServiceValidator v = new ServiceValidator();
        if (v.role(rd).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        } {
        if (v.nullOrBlank("description", rd.value.description).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        }
        final RoleDAO.Data role = rd.value;
        if (ques.roleDAO().read(trans, role.ns, role.name).notOKorIsEmpty()) {
            return Result.err(Status.ERR_NotFound, "Role [" + role.fullName() + "] does not exist");
        }

        if (ques.mayUser(trans, trans.user(), role, Access.write).notOK()) {
            return Result.err(Status.ERR_Denied, "You do not have approval to change " + role.fullName());
        }

        Result<List<NsDAO.Data>> nsr = ques.nsDAO().read(trans, rd.value.ns);
        if (nsr.notOKorIsEmpty()) {
            return Result.err(nsr);
        }

        Result<Void> rdr = ques.roleDAO().addDescription(trans, role.ns, role.name, role.description);
        if (rdr.isOK()) {
            return Result.ok();
        } else {
            return Result.err(rdr);
        }

    }
    
    @ApiDoc(
            method = POST,
            path = "/authz/role/perm",
            params = {},
            expectedCode = 201,
            errorCodes = {403,404,406,409},
            text = { "Grant a Permission to a Role",
                     "Permission consists of:", 
                     "<ul><li>type - a Namespace qualified identifier specifying what kind of resource "
                     + "is being protected</li>",
                     "<li>instance - a key, possibly multi-dimensional, that identifies a specific "
                     + " instance of the type</li>",
                     "<li>action - what kind of action is allowed</li></ul>",
                     "Note: instance and action can be an *",
                     "Note: Using the \"force\" property will create the Permission, if it doesn't exist AND the requesting " +
                     " ID is allowed to create.  It will then grant",
                     "  the permission to the role in one step. To do this: add 'force=true' as a query parameter."
                    }
           )

    @Override
    public Result<Void> addPermToRole(final AuthzTrans trans, REQUEST rreq) {
        // Translate Request into Perm and Role Objects
        final Result<PermDAO.Data> rpd = mapper.permFromRPRequest(trans, rreq);
        if (rpd.notOKorIsEmpty()) {
            return Result.err(rpd);
        }
        final Result<RoleDAO.Data> rrd = mapper.roleFromRPRequest(trans, rreq);
        if (rrd.notOKorIsEmpty()) {
            return Result.err(rrd);
        }
        
        // Validate Role and Perm values
        final ServiceValidator v = new ServiceValidator();
        if (v.perm(rpd.value)
            .role(rrd.value)
            .err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        Result<List<RoleDAO.Data>> rlrd = ques.roleDAO().read(trans, rrd.value.ns, rrd.value.name);
        if (rlrd.notOKorIsEmpty()) {
            return Result.err(Status.ERR_RoleNotFound, "Role [%s] does not exist", rrd.value.fullName());
        }
        
        // Check Status of Data in DB (does it exist)
        Result<List<PermDAO.Data>> rlpd = ques.permDAO().read(trans, rpd.value.ns, 
                rpd.value.type, rpd.value.instance, rpd.value.action);
        PermDAO.Data createPerm = null; // if not null, create first
        if (rlpd.notOKorIsEmpty()) { // Permission doesn't exist
            if (trans.requested(force)) {
                // Remove roles from perm data object so we just create the perm here
                createPerm = rpd.value;
                createPerm.roles.clear();
            } else {
                return Result.err(Status.ERR_PermissionNotFound,"Permission [%s.%s|%s|%s] does not exist", 
                        rpd.value.ns,rpd.value.type,rpd.value.instance,rpd.value.action);
            }
        } else {
            if (rlpd.value.get(0).roles(false).contains(rrd.value.encode())) {
                return Result.err(Status.ERR_ConflictAlreadyExists,
                        "Permission [%s.%s|%s|%s] already granted to Role [%s.%s]",
                        rpd.value.ns,rpd.value.type,rpd.value.instance,rpd.value.action,
                        rrd.value.ns,rrd.value.name
                    );
            }
        }

        
        Result<FutureDAO.Data> fd = mapper.future(trans, PermDAO.TABLE, rreq, rpd.value,true, // Allow grants to create Approvals
                new Mapper.Memo() {
                    @Override
                    public String get() {
                        return "Grant Permission [" + rpd.value.fullPerm() + ']' +
                            " to Role [" + rrd.value.fullName() + "]";
                    }
                },
                new MayChange() {
                    private Result<NsDAO.Data> nsd;
                    @Override
                    public Result<?> mayChange() {
                        if (nsd==null) {
                            nsd = ques.mayUser(trans, trans.user(), rpd.value, Access.write);
                            if(nsd.notOK()) {
                            	trans.requested(REQD_TYPE.future,true);
                            }
                        }
                        return nsd;
                    }
                });
        Result<List<NsDAO.Data>> nsr = ques.nsDAO().read(trans, rpd.value.ns);
        if (nsr.notOKorIsEmpty()) {
            return Result.err(nsr);
        }
        switch(fd.status) {
	        case OK:
	            Result<String> rfc = func.createFuture(trans,fd.value, 
	                    rpd.value.fullPerm(),
	                    trans.user(),
	                    nsr.value.get(0),
	                    FUTURE_OP.G);
	            if (rfc.isOK()) {
	                return Result.err(Status.ACC_Future, "Perm [%s.%s|%s|%s] is saved for future processing",
	                        rpd.value.ns,
	                        rpd.value.type,
	                        rpd.value.instance,
	                        rpd.value.action);
	            } else { 
	                return Result.err(rfc);
	            }
	        case Status.ACC_Now:
	            Result<Void> rv = null;
	            if (createPerm!=null) {// has been validated for creating
	                rv = func.createPerm(trans, createPerm, false);
	            }
	            if (rv==null || rv.isOK()) {
	                rv = func.addPermToRole(trans, rrd.value, rpd.value, false);
	            }
	            return rv;
	        default:
	            return Result.err(fd);
        }
        
    }

    /**
     * Delete Perms from Roles (UnGrant)
     * @param trans
     * @param roleFullName
     * @return
     */
    @ApiDoc(
            method = DELETE,
            path = "/authz/role/:role/perm",
            params = {"role|string|true"},
            expectedCode = 200,
            errorCodes = {404,406},
            text = { "Ungrant a permission from Role :role" }
           )

    @Override
    public Result<Void> delPermFromRole(final AuthzTrans trans, REQUEST rreq) {
        final Result<PermDAO.Data> updt = mapper.permFromRPRequest(trans, rreq);
        if (updt.notOKorIsEmpty()) {
            return Result.err(updt);
        }
        final Result<RoleDAO.Data> rrd = mapper.roleFromRPRequest(trans, rreq);
        if (rrd.notOKorIsEmpty()) {
            return Result.err(rrd);
        }

        final ServiceValidator v = new ServiceValidator();
        if (v.nullOrBlank(updt.value)
            .nullOrBlank(rrd.value)
            .err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        return delPermFromRole(trans, updt.value,rrd.value, rreq);
    }
        
    private Result<Void> delPermFromRole(final AuthzTrans trans, PermDAO.Data pdd, RoleDAO.Data rdd, REQUEST rreq) {        
        Result<List<PermDAO.Data>> rlpd = ques.permDAO().read(trans, pdd.ns, pdd.type, 
                pdd.instance, pdd.action);
        
        if (rlpd.notOKorIsEmpty()) {
            return Result.err(Status.ERR_PermissionNotFound, 
                "Permission [%s.%s|%s|%s] does not exist",
                    pdd.ns,pdd.type,pdd.instance,pdd.action);
        }
        
        Result<FutureDAO.Data> fd = mapper.future(trans, PermDAO.TABLE, rreq, pdd,true, // allow ungrants requests
                new Mapper.Memo() {
                    @Override
                    public String get() {
                        return "Ungrant Permission [" + pdd.fullPerm() + ']' +
                            " from Role [" + rdd.fullName() + "]";
                    }
                },
                new MayChange() {
                    private Result<NsDAO.Data> nsd;
                    @Override
                    public Result<?> mayChange() {
                        if (nsd==null) {
                            nsd = ques.mayUser(trans, trans.user(), pdd, Access.write);
                        }
                        return nsd;
                    }
                });
        Result<List<NsDAO.Data>> nsr = ques.nsDAO().read(trans, pdd.ns);
        if (nsr.notOKorIsEmpty()) {
            return Result.err(nsr);
        }
        switch(fd.status) {
            case OK:
                Result<String> rfc = func.createFuture(trans,fd.value, 
                        pdd.fullPerm(),
                        trans.user(),
                        nsr.value.get(0),
                        FUTURE_OP.UG
                        );
                if (rfc.isOK()) {
                    return Result.err(Status.ACC_Future, "Perm [%s.%s|%s|%s] is saved for future processing",
                            pdd.ns,
                            pdd.type,
                            pdd.instance,
                            pdd.action);
                } else {
                    return Result.err(rfc);
                }
            case Status.ACC_Now:
                return func.delPermFromRole(trans, rdd, pdd, false);
            default:
                return Result.err(fd);
        }
    }
    
/*
    @ApiDoc(
            method = DELETE,
            path = "/authz/role/:role/perm/:type/:instance/:action",
            params = {"role|string|true",
                         "perm type|string|true",
                         "perm instance|string|true",
                         "perm action|string|true"
                },
            expectedCode = 200,
            errorCodes = {404,406},
            text = { "Ungrant a single permission from Role :role with direct key" }
           )
*/
    @Override
    public Result<Void> delPermFromRole(AuthzTrans trans, String role, String type, String instance, String action) {
        Result<Data> rpns = ques.deriveNs(trans, type);
        if (rpns.notOKorIsEmpty()) {
            return Result.err(rpns);
        }
        
            final Validator v = new ServiceValidator();
        if (v.role(role)
            .permType(rpns.value.name,rpns.value.parent)
            .permInstance(instance)
            .permAction(action)
            .err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        
            Result<Data> rrns = ques.deriveNs(trans, role);
            if (rrns.notOKorIsEmpty()) {
                return Result.err(rrns);
            }
            
        final Result<List<RoleDAO.Data>> rrd = ques.roleDAO().read(trans, rrns.value.parent, rrns.value.name);
        if (rrd.notOKorIsEmpty()) {
            return Result.err(rrd);
        }
        
        final Result<List<PermDAO.Data>> rpd = ques.permDAO().read(trans, rpns.value.parent, rpns.value.name, instance, action);
        if (rpd.notOKorIsEmpty()) {
            return Result.err(rpd);
        }

        
        return delPermFromRole(trans,rpd.value.get(0), rrd.value.get(0), mapper.ungrantRequest(trans, role, type, instance, action));
    }
    
    @ApiDoc(
            method = DELETE,
            path = "/authz/role/:role",
            params = {"role|string|true"},
            expectedCode = 200,
            errorCodes = {404,406},
            text = { "Delete the Role named :role"}
           )

    @Override
    public Result<Void> deleteRole(AuthzTrans trans, String role)  {
        Result<RoleDAO.Data> rrdd = RoleDAO.Data.decode(trans,ques,role);
        if (rrdd.isOKhasData()) {
            final ServiceValidator v = new ServiceValidator();
            if (v.nullOrBlank(rrdd.value).err()) { 
                return Result.err(Status.ERR_BadData,v.errs());
            }
            return func.deleteRole(trans, rrdd.value, false, false);
        } else {
            return Result.err(rrdd);
        }
    }

    @ApiDoc(
            method = DELETE,
            path = "/authz/role",
            params = {},
            expectedCode = 200,
            errorCodes = { 404,406 },
            text = { "Delete the Role referenced by RoleKey",
                    "You cannot normally delete a role which still has permissions granted or users assigned to it,",
                    "however the \"force\" property allows you to do just that. To do this: Add 'force=true'",
                    "as a query parameter.",
                    "<p>WARNING: Using force will remove all users and permission from this role. Use with care.</p>"}
           )

    @Override
    public Result<Void> deleteRole(final AuthzTrans trans, REQUEST from) {
        final Result<RoleDAO.Data> rd = mapper.role(trans, from);
        final ServiceValidator v = new ServiceValidator();
        if (rd==null) {
            return Result.err(Status.ERR_BadData,"Request does not contain Role");
        }
        if (v.nullOrBlank(rd.value).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        final RoleDAO.Data role = rd.value;
        if (ques.roleDAO().read(trans, role).notOKorIsEmpty() && !trans.requested(force)) {
            return Result.err(Status.ERR_RoleNotFound, "Role [" + role.fullName() + "] does not exist");
        }

        Result<FutureDAO.Data> fd = mapper.future(trans,RoleDAO.TABLE,from,role,false,
            () -> "Delete Role [" + role.fullName() + ']'
                    + " and all attached user roles",
            new MayChange() {
                private Result<NsDAO.Data> nsd;
                @Override
                public Result<?> mayChange() {
                    if (nsd==null) {
                        nsd = ques.mayUser(trans, trans.user(), role, Access.write);
                    }
                    return nsd;
                }
            });
        
        switch(fd.status) {
        case OK:
            Result<List<NsDAO.Data>> nsr = ques.nsDAO().read(trans, rd.value.ns);
            if (nsr.notOKorIsEmpty()) {
                return Result.err(nsr);
            }
            
            Result<String> rfc = func.createFuture(trans, fd.value, 
                    role.encode(), trans.user(),nsr.value.get(0),FUTURE_OP.D);
            if (rfc.isOK()) {
                return Result.err(Status.ACC_Future, "Role Deletion [%s.%s] is saved for future processing",
                        rd.value.ns,
                        rd.value.name);
            } else { 
                return Result.err(rfc);
            }
        case Status.ACC_Now:
            return func.deleteRole(trans,role,trans.requested(force), true /*preapproved*/);
        default:
            return Result.err(fd);
    }

    }

/***********************************
 * CRED 
 ***********************************/
    private class MayCreateCred implements MayChange {
        private Result<NsDAO.Data> nsd;
        private AuthzTrans trans;
        private CredDAO.Data cred;
        private Executor exec;
        
        public MayCreateCred(AuthzTrans trans, CredDAO.Data cred, Executor exec) {
            this.trans = trans;
            this.cred = cred;
            this.exec = exec;
        }

        @Override
        public Result<?> mayChange() {
            if (nsd==null) {
                nsd = ques.validNSOfDomain(trans, cred.id);
            }
            // is Ns of CredID valid?
            if (nsd.isOK()) {
                try {
                    // Check Org Policy
                    if (trans.org().validate(trans,Policy.CREATE_MECHID, exec, cred.id)==null) {
                        return Result.ok(); 
                    } else {
                       Result<?> rmc = ques.mayUser(trans, trans.user(), nsd.value, Access.write);
                       if (rmc.isOKhasData()) {
                           return rmc;
                       }
                    }
                } catch (Exception e) {
                    trans.warn().log(e);
                }
            } else {
                trans.warn().log(nsd.errorString());
            }
            return Result.err(Status.ERR_Denied,"%s is not allowed to create %s in %s",trans.user(),cred.id,cred.ns);
        }
    }

    private class MayChangeCred implements MayChange {
        private static final String EXTEND = "extend";
		private static final String RESET = "reset";
		private static final String DELETE = "delete";
		private Result<NsDAO.Data> nsd;
        private AuthzTrans trans;
        private CredDAO.Data cred;
		private String action;
        public MayChangeCred(AuthzTrans trans, CredDAO.Data cred, String action) {
            this.trans = trans;
            this.cred = cred;
            this.action = action;
        }

        @Override
        public Result<?> mayChange() {
            // User can change himself (but not create)
            if (nsd==null) {
                nsd = ques.validNSOfDomain(trans, cred.id);
            }
            // Get the Namespace
            if (nsd.isOK()) {
        		String ns = nsd.value.name;
        		String user = trans.user();
            	String company;
            	String temp[] = Split.split('.',ns);
            	switch(temp.length) {
            		case 0:
            			company = Defaults.AAF_NS;
            			break;
            		case 1:
            			company = temp[0];
            			break;
            		default:
            			company = temp[0] + '.' + temp[1];
            	}
            	switch(action) {
            		case DELETE:
            			if(ques.isOwner(trans, user,ns) ||
                     		   ques.isAdmin(trans, user,ns) ||
         					   ques.isGranted(trans, user, ROOT_NS,"password",company,DELETE)) {
                     				return Result.ok();
            			}
            			break;
            		case RESET:
            		case EXTEND:
                        if (ques.isGranted(trans, trans.user(), ROOT_NS,"password",company,action)) {
                            return Result.ok();
                        }
                        break;
            	}
            }
            return Result.err(Status.ERR_Denied,"%s is not allowed to %s %s in %s",trans.user(),action,cred.id,cred.ns);
        }
    }

    private final long DAY_IN_MILLIS = 24*3600*1000L;
    
    @ApiDoc( 
            method = POST,  
            path = "/authn/cred",
            params = {},
            expectedCode = 201,
            errorCodes = {403,404,406,409}, 
            text = { "A credential consists of:",
                     "<ul><li>id - the ID to create within AAF. The domain is in reverse",
                     "order of Namespace (i.e. Users of Namespace com.att.myapp would be",
                     "AB1234@myapp.att.com</li>",
                     "<li>password - Company Policy Compliant Password</li></ul>",
                     "Note: AAF does support multiple credentials with the same ID.",
                     "Check with your organization if you have this implemented."
                     }
            )
    @Override
    public Result<Void> createUserCred(final AuthzTrans trans, REQUEST from) {
        final String cmdDescription = ("Create User Credential");
        TimeTaken tt = trans.start(cmdDescription, Env.SUB);
        
        try {
            Result<CredDAO.Data> rcred = mapper.cred(trans, from, true);
            if (rcred.isOKhasData()) {
                rcred = ques.userCredSetup(trans, rcred.value);
                
                final ServiceValidator v = new ServiceValidator();
                
                if (v.cred(trans, trans.org(),rcred,true).err()) { // Note: Creates have stricter Validations 
                    return Result.err(Status.ERR_BadData,v.errs());
                }
                

                // 2016-4 Jonathan, New Behavior - If MechID is not registered with Org, deny creation
                Identity mechID =  null;
                Organization org = trans.org();
                try {
                    mechID = org.getIdentity(trans, rcred.value.id);
                } catch (Exception e1) {
                    trans.error().log(e1,rcred.value.id,"cannot be validated at this time");
                }
                if (mechID==null || !mechID.isFound()) { 
                    return Result.err(Status.ERR_Policy,"MechIDs must be registered with %s before provisioning in AAF",org.getName());
                }

                Result<List<NsDAO.Data>> nsr = ques.nsDAO().read(trans, rcred.value.ns);
                if (nsr.notOKorIsEmpty()) {
                    return Result.err(Status.ERR_NsNotFound,"Cannot provision %s on non-existent Namespace %s",mechID.id(),rcred.value.ns);
                }
                

                boolean firstID = false;
                MayChange mc;
                
                CassExecutor exec = new CassExecutor(trans, func);
                Result<List<CredDAO.Data>> rlcd = ques.credDAO().readID(trans, rcred.value.id);
                if (rlcd.isOKhasData()) {
                    if (!org.canHaveMultipleCreds(rcred.value.id)) {
                        return Result.err(Status.ERR_ConflictAlreadyExists, "Credential exists");
                    }
                    Result<Boolean> rb;
                    for (CredDAO.Data curr : rlcd.value) {
                        // May not use the same password in the list
                        // Note: ASPR specifies character differences, but we don't actually store the
                        // password to validate char differences.
                        
//                      byte[] rawCred = rcred.value.type==CredDAO.RAW?null:;                            return Result.err(Status.ERR_ConflictAlreadyExists, "Credential with same Expiration Date exists");
                    	if(rcred.value.type==CredDAO.FQI ) {
                    		if(curr.type==CredDAO.FQI) {
                    	        return Result.err(Status.ERR_ConflictAlreadyExists, "Credential with same Expiration Date exists");
                    		}
                    	} else {
	
	                        rb = ques.userCredCheck(trans, curr, rcred.value.cred!=null?rcred.value.cred.array():null);
	                        if (rb.notOK()) {
	                            return Result.err(rb);
	                        } else if (rb.value){
	                            return Result.err(Status.ERR_Policy, "Credential content cannot be reused.");
	                        } else if ((Chrono.dateOnlyStamp(curr.expires).equals(Chrono.dateOnlyStamp(rcred.value.expires)) && curr.type==rcred.value.type)) {
	                            return Result.err(Status.ERR_ConflictAlreadyExists, "Credential with same Expiration Date exists");
	                        }
                    	}
                    }    
                } else {
                    try {
                    // 2016-04-12 Jonathan If Caller is the Sponsor and is also an Owner of NS, allow without special Perm
                        String theMechID = rcred.value.id;
                        Boolean otherMechIDs = false;
                        // find out if this is the only mechID.  other MechIDs mean special handling (not automated)
                        for (CredDAO.Data cd : ques.credDAO().readNS(trans,nsr.value.get(0).name).value) {
                            if (!cd.id.equals(theMechID)) {
                                otherMechIDs = true;
                                break;
                            }
                        }
                        String reason;
                        // We can say "ID does not exist" here
                        if ((reason=org.validate(trans, Policy.CREATE_MECHID, exec, theMechID,trans.user(),otherMechIDs.toString()))!=null) {
                            return Result.err(Status.ERR_Denied, reason); 
                        }
                        firstID=true;
                    } catch (Exception e) {
                        return Result.err(e);
                    }
                }
    
                mc = new MayCreateCred(trans, rcred.value, exec);
                
                final CredDAO.Data cdd = rcred.value;
                Result<FutureDAO.Data> fd = mapper.future(trans,CredDAO.TABLE,from, rcred.value,false, // may want to enable in future.
                    new Mapper.Memo() {
                        @Override
                        public String get() {
                            return cmdDescription + " [" + 
                                cdd.id + '|' 
                                + cdd.type + '|' 
                                + cdd.expires + ']';
                        }
                    },
                    mc);
                
                switch(fd.status) {
                    case OK:
                        Result<String> rfc = func.createFuture(trans, fd.value, 
                                rcred.value.id + '|' + rcred.value.type.toString() + '|' + rcred.value.expires,
                                trans.user(), nsr.value.get(0), FUTURE_OP.C);
                        if (rfc.isOK()) {
                            return Result.err(Status.ACC_Future, "Credential Request [%s|%s|%s] is saved for future processing",
                                    rcred.value.id,
                                    Integer.toString(rcred.value.type),
                                    rcred.value.expires.toString());
                        } else { 
                            return Result.err(rfc);
                        }
                    case Status.ACC_Now:
                        try {
                            if (firstID) {
    //                            && !nsr.value.get(0).isAdmin(trans.getUserPrincipal().getName())) {
                                Result<List<String>> admins = func.getAdmins(trans, nsr.value.get(0).name, false);
                                // OK, it's a first ID, and not by NS Admin, so let's set TempPassword length
                                // Note, we only do this on First time, because of possibility of 
                                // prematurely expiring a production id
                                if (admins.isOKhasData() && !admins.value.contains(trans.user())) {
                                    rcred.value.expires = org.expiration(null, Expiration.TempPassword).getTime();
                                }
                            }
                        } catch (Exception e) {
                            trans.error().log(e, "While setting expiration to TempPassword");
                        }
                        
                        Result<?>udr = ques.credDAO().create(trans, rcred.value);
                        if (udr.isOK()) {
                            return Result.ok();
                        }
                        return Result.err(udr);
                    default:
                        return Result.err(fd);
                }

            } else {
                return Result.err(rcred);
            }
        } finally {
            tt.done();
        }
    }

    @ApiDoc(   
            method = GET,  
            path = "/authn/creds/ns/:ns",
            params = {"ns|string|true"},
            expectedCode = 200,
            errorCodes = {403,404,406}, 
            text = { "Return all IDs in Namespace :ns"
                     }
            )
    @Override
    public Result<USERS> getCredsByNS(AuthzTrans trans, String ns) {
        final Validator v = new ServiceValidator();
        if (v.ns(ns).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        
        // check if user is allowed to view NS
        Result<NsDAO.Data> rnd = ques.deriveNs(trans,ns);
        if (rnd.notOK()) {
            return Result.err(rnd); 
        }
        rnd = ques.mayUser(trans, trans.user(), rnd.value, Access.read);
        if (rnd.notOK()) {
            return Result.err(rnd); 
        }
    
        TimeTaken tt = trans.start("MAP Creds by NS to Creds", Env.SUB);
        try {            
            USERS users = mapper.newInstance(API.USERS);
            Result<List<CredDAO.Data>> rlcd = ques.credDAO().readNS(trans, ns);
                    
            if (rlcd.isOK()) {
                if (!rlcd.isEmpty()) {
                    return mapper.cred(rlcd.value, users);
                }
                return Result.ok(users);        
            } else {
                return Result.err(rlcd);
            }
        } finally {
            tt.done();
        }
            
    }

    @ApiDoc(   
            method = GET,  
            path = "/authn/creds/id/:ns",
            params = {"id|string|true"},
            expectedCode = 200,
            errorCodes = {403,404,406}, 
            text = { "Return all IDs in for ID"
                    ,"(because IDs are multiple, due to multiple Expiration Dates)"
                     }
            )
    @Override
    public Result<USERS> getCredsByID(AuthzTrans trans, String id) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("ID",id).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        
        String ns = Question.domain2ns(id);
        // check if user is allowed to view NS
        Result<NsDAO.Data> rnd = ques.deriveNs(trans,ns);
        if (rnd.notOK()) {
            return Result.err(rnd); 
        }
        rnd = ques.mayUser(trans, trans.user(), rnd.value, Access.read);
        if (rnd.notOK()) {
            return Result.err(rnd); 
        }
    
        TimeTaken tt = trans.start("MAP Creds by ID to Creds", Env.SUB);
        try {            
            USERS users = mapper.newInstance(API.USERS);
            Result<List<CredDAO.Data>> rlcd = ques.credDAO().readID(trans, id);
                    
            if (rlcd.isOK()) {
                if (!rlcd.isEmpty()) {
                    return mapper.cred(rlcd.value, users);
                }
                return Result.ok(users);        
            } else {
                return Result.err(rlcd);
            }
        } finally {
            tt.done();
        }
            
    }

    @ApiDoc(   
            method = GET,  
            path = "/authn/certs/id/:id",
            params = {"id|string|true"},
            expectedCode = 200,
            errorCodes = {403,404,406}, 
            text = { "Return Cert Info for ID"
                   }
            )
    @Override
    public Result<CERTS> getCertInfoByID(AuthzTrans trans, HttpServletRequest req, String id) {
        TimeTaken tt = trans.start("Get Cert Info by ID", Env.SUB);
        try {            
            CERTS certs = mapper.newInstance(API.CERTS);
            Result<List<CertDAO.Data>> rlcd = ques.certDAO().readID(trans, id);
                    
            if (rlcd.isOK()) {
                if (!rlcd.isEmpty()) {
                    return mapper.cert(rlcd.value, certs);
                }
                return Result.ok(certs);        
            } else { 
                return Result.err(rlcd);
            }
        } finally {
            tt.done();
        }

    }

    @ApiDoc( 
            method = PUT,  
            path = "/authn/cred",
            params = {},
            expectedCode = 200,
            errorCodes = {300,403,404,406}, 
            text = { "Reset a Credential Password. If multiple credentials exist for this",
                        "ID, you will need to specify which entry you are resetting in the",
                        "CredRequest object"
                     }
            )
    @Override
    public Result<Void> resetUserCred(final AuthzTrans trans, REQUEST from) {
        final String cmdDescription = "Update User Credential";
        TimeTaken tt = trans.start(cmdDescription, Env.SUB);
        try {
            Result<CredDAO.Data> rcred = mapper.cred(trans, from, true);
            if (rcred.isOKhasData()) {
                rcred = ques.userCredSetup(trans, rcred.value);
    
                final ServiceValidator v = new ServiceValidator();
                
                if (v.cred(trans, trans.org(),rcred,false).err()) {// Note: Creates have stricter Validations 
                    return Result.err(Status.ERR_BadData,v.errs());
                }
                Result<List<CredDAO.Data>> rlcd = ques.credDAO().readID(trans, rcred.value.id);
                if (rlcd.notOKorIsEmpty()) {
                    return Result.err(Status.ERR_UserNotFound, "Credential does not exist");
                } 
                
                MayChange mc = new MayChangeCred(trans, rcred.value,MayChangeCred.RESET);
                Result<?> rmc = mc.mayChange(); 
                if (rmc.notOK()) {
                    return Result.err(rmc);
                }
                
                List<CredDAO.Data> lcdd = filterList(rlcd.value,CredDAO.BASIC_AUTH, CredDAO.BASIC_AUTH_SHA256);
                
                Result<Integer> ri = selectEntryIfMultiple((CredRequest)from, lcdd, MayChangeCred.RESET);
                if (ri.notOK()) {
                    return Result.err(ri);
                }
                int entry = ri.value;
    
                
                final CredDAO.Data cred = rcred.value;
                
                Result<FutureDAO.Data> fd = mapper.future(trans,CredDAO.TABLE,from, rcred.value,false,
                new Mapper.Memo() {
                    @Override
                    public String get() {
                        return cmdDescription + " [" + 
                            cred.id + '|' 
                            + cred.type + '|' 
                            + cred.expires + ']';
                    }
                },
                mc);
                
                Result<List<NsDAO.Data>> nsr = ques.nsDAO().read(trans, rcred.value.ns);
                if (nsr.notOKorIsEmpty()) {
                    return Result.err(nsr);
                }
    
                switch(fd.status) {
                    case OK:
                        Result<String> rfc = func.createFuture(trans, fd.value, 
                                rcred.value.id + '|' + rcred.value.type.toString() + '|' + rcred.value.expires,
                                trans.user(), nsr.value.get(0), FUTURE_OP.U);
                        if (rfc.isOK()) {
                            return Result.err(Status.ACC_Future, "Credential Request [%s|%s|%s]",
                                    rcred.value.id,
                                    Integer.toString(rcred.value.type),
                                    rcred.value.expires.toString());
                        } else { 
                            return Result.err(rfc);
                        }
                    case Status.ACC_Now:
                        Result<?>udr = null;
                        // If we are Resetting Password on behalf of someone else (am not the Admin)
                        //  use TempPassword Expiration time.
                        Expiration exp;
                        if (ques.isAdmin(trans, trans.user(), nsr.value.get(0).name)) {
                            exp = Expiration.Password;
                        } else {
                            exp = Expiration.TempPassword;
                        }
                        
                        Organization org = trans.org();
                        CredDAO.Data current = rlcd.value.get(entry);
                        // If user resets password in same day, we will have a primary key conflict, so subtract 1 day
                        if (current.expires.equals(rcred.value.expires) 
                                    && rlcd.value.get(entry).type==rcred.value.type) {
                            GregorianCalendar gc = org.expiration(null, exp,rcred.value.id);
                            gc = Chrono.firstMomentOfDay(gc);
                            gc.set(GregorianCalendar.HOUR_OF_DAY, org.startOfDay());                        
                            rcred.value.expires = new Date(gc.getTimeInMillis() - DAY_IN_MILLIS);
                        } else {
                            rcred.value.expires = org.expiration(null,exp).getTime();
                        }

                        udr = ques.credDAO().create(trans, rcred.value);
                        if (udr.isOK()) {
                            udr = ques.credDAO().delete(trans, rlcd.value.get(entry),false);
                        }
                        if (udr.isOK()) {
                            return Result.ok();
                        }
    
                        return Result.err(udr);
                    default:
                        return Result.err(fd);
                }
            } else {
                return Result.err(rcred);
            }
        } finally {
            tt.done();
        }
    }

    @ApiDoc( 
            method = PUT,  
            path = "/authn/cred/:days",
            params = {"days|string|true"},
            expectedCode = 200,
            errorCodes = {300,403,404,406}, 
            text = { "Extend a Credential Expiration Date. The intention of this API is",
                        "to avoid an outage in PROD due to a Credential expiring before it",
                        "can be configured correctly. Measures are being put in place ",
                        "so that this is not abused."
                     }
            )
    @Override
    public Result<Void> extendUserCred(final AuthzTrans trans, REQUEST from, String days) {
        TimeTaken tt = trans.start("Extend User Credential", Env.SUB);
        try {
            Result<CredDAO.Data> cred = mapper.cred(trans, from, false);
            Organization org = trans.org();
            final ServiceValidator v = new ServiceValidator();
            if (v.notOK(cred).err() || 
               v.nullOrBlank(cred.value.id, "Invalid ID").err() ||
               v.user(org,cred.value.id).err())  {
                 return Result.err(Status.ERR_BadData,v.errs());
            }
            
            try {
                String reason;
                if ((reason=org.validate(trans, Policy.MAY_EXTEND_CRED_EXPIRES, new CassExecutor(trans,func)))!=null) {
                    return Result.err(Status.ERR_Policy,reason);
                }
            } catch (Exception e) {
                String msg;
                trans.error().log(e, msg="Could not contact Organization for User Validation");
                return Result.err(Status.ERR_Denied, msg);
            }
    
            // Get the list of Cred Entries
            Result<List<CredDAO.Data>> rlcd = ques.credDAO().readID(trans, cred.value.id);
            if (rlcd.notOKorIsEmpty()) {
                return Result.err(Status.ERR_UserNotFound, "Credential does not exist");
            }
            
            // Only Passwords can be extended
            List<CredDAO.Data> lcdd = filterList(rlcd.value,CredDAO.BASIC_AUTH, CredDAO.BASIC_AUTH_SHA256);

            //Need to do the "Pick Entry" mechanism
            // Note, this sorts
            Result<Integer> ri = selectEntryIfMultiple((CredRequest)from, lcdd, MayChangeCred.EXTEND);
            if (ri.notOK()) {
                return Result.err(ri);
            }

            CredDAO.Data found = lcdd.get(ri.value);
            CredDAO.Data cd = cred.value;
            // Copy over the cred
            cd.id = found.id;
            cd.cred = found.cred;
            cd.other = found.other;
            cd.type = found.type;
            cd.ns = found.ns;
            cd.notes = "Extended";
            cd.tag = found.tag;
            cd.expires = org.expiration(null, Expiration.ExtendPassword,days).getTime();
            if(cd.expires.before(found.expires)) {
            	return Result.err(Result.ERR_BadData,String.format("Credential's expiration date is more than %s days in the future",days));
            }
            
            cred = ques.credDAO().create(trans, cd);
            if (cred.isOK()) {
                return Result.ok();
            }
            return Result.err(cred);
        } finally {
            tt.done();
        }
    }    

    @ApiDoc( 
	        method = DELETE,  
	        path = "/authn/cred",
	        params = {},
	        expectedCode = 200,
	        errorCodes = {300,403,404,406}, 
	        text = { "Delete a Credential. If multiple credentials exist for this",
	                "ID, you will need to specify which entry you are deleting in the",
	                "CredRequest object."
	                 }
	        )
	@Override
	public Result<Void> deleteUserCred(AuthzTrans trans, REQUEST from)  {
	    final Result<CredDAO.Data> cred = mapper.cred(trans, from, false);
	    final Validator v = new ServiceValidator();
	    if (v.nullOrBlank("cred", cred.value.id).err()) {
	        return Result.err(Status.ERR_BadData,v.errs());
	    }

	    MayChange mc = new MayChangeCred(trans,cred.value,MayChangeCred.DELETE);
	    Result<?> rmc = mc.mayChange(); 
	    if (rmc.notOK()) {
	        return Result.err(rmc);
	    }
	    
	    boolean doForce = trans.requested(force);
	    Result<List<CredDAO.Data>> rlcd = ques.credDAO().readID(trans, cred.value.id);
	    if (rlcd.notOKorIsEmpty()) {
	        // Empty Creds should not have user_roles.
	        Result<List<UserRoleDAO.Data>> rlurd = ques.userRoleDAO().readByUser(trans, cred.value.id);
	        if (rlurd.isOKhasData()) {
	            for (UserRoleDAO.Data data : rlurd.value) {
	                ques.userRoleDAO().delete(trans, data, false);
	            }
        	}
	        return Result.err(Status.ERR_UserNotFound, "Credential does not exist");
	    }
	    boolean isLastCred = rlcd.value.size()==1;
	    
	    int entry;
	    CredRequest cr = (CredRequest)from;
	    if(isLastCred) {
	    	if(cr.getEntry()==null || "1".equals(cr.getEntry())) {
	    		entry = 0;
	    	} else {
	            return Result.err(Status.ERR_BadData, "User chose invalid credential selection");
	    	}
	    } else {
		    entry = -1;
	    	int fentry = entry;
		    if(cred.value.type==CredDAO.FQI) {
		    	entry = -1;
		    	for(CredDAO.Data cdd : rlcd.value) {
		    		++fentry;
		    		if(cdd.type == CredDAO.FQI) {
		    			entry = fentry;
		    			break; 
		    		}
		    	}
		    } else {
			    if (!doForce) {
			        if (rlcd.value.size() > 1) {
			            String inputOption = cr.getEntry();
			            if (inputOption == null) {
			            	List<CredDAO.Data> list = filterList(rlcd.value,CredDAO.BASIC_AUTH,CredDAO.BASIC_AUTH_SHA256,CredDAO.CERT_SHA256_RSA);
			                String message = selectCredFromList(list, MayChangeCred.DELETE);
			                Object[] variables = buildVariables(list);
			                return Result.err(Status.ERR_ChoiceNeeded, message, variables);
			            } else {
			                try {
			                    if (inputOption.length()>5) { // should be a date
			                        Date d = Chrono.xmlDatatypeFactory.newXMLGregorianCalendar(inputOption).toGregorianCalendar().getTime();
			                        for (CredDAO.Data cd : rlcd.value) {
			                        	++fentry;
			                            if (cd.type.equals(cr.getType()) && cd.expires.equals(d)) {
			                            	entry = fentry;
			                                break;
			                            }
			                        }
			                    } else {
		                        	entry = Integer.parseInt(inputOption) - 1;
		                        	int count = 0;
			                        for (CredDAO.Data cd : rlcd.value) {
			                        	if(cd.type!=CredDAO.BASIC_AUTH && cd.type!=CredDAO.BASIC_AUTH_SHA256 && cd.type!=CredDAO.CERT_SHA256_RSA) {
			                        		++entry;
			                        	}
			                        	if(++count>entry) {
			                        		break;
			                        	}
			                        }
			                    }
			                } catch (NullPointerException e) {
			                    return Result.err(Status.ERR_BadData, "Invalid Date Format for Entry");
			                } catch (NumberFormatException e) {
			                    return Result.err(Status.ERR_BadData, "User chose invalid credential selection");
			                }
			            }
			            isLastCred = (entry==-1)?true:false;
			        } else {
			            isLastCred = true;
			        }
			        if (entry < -1 || entry >= rlcd.value.size()) {
			            return Result.err(Status.ERR_BadData, "User chose invalid credential selection");
			        }
			    }
		    }
	    }
	    
	    Result<FutureDAO.Data> fd = mapper.future(trans,CredDAO.TABLE,from,cred.value,false,
	        () -> "Delete Credential [" +
	            cred.value.id +
	            ']',
	        mc);
	
	    Result<List<NsDAO.Data>> nsr = ques.nsDAO().read(trans, cred.value.ns);
	    if (nsr.notOKorIsEmpty()) {
	        return Result.err(nsr);
	    }
	
	    switch(fd.status) {
	        case OK:
	            Result<String> rfc = func.createFuture(trans, fd.value, cred.value.id,
	                    trans.user(), nsr.value.get(0), FUTURE_OP.D);
	
	            if (rfc.isOK()) {
	                return Result.err(Status.ACC_Future, "Credential Delete [%s] is saved for future processing",cred.value.id);
	            } else { 
	                return Result.err(rfc);
	            }
	        case Status.ACC_Now:
	            Result<?>udr = null;
	            if (!trans.requested(force)) {
	                if (entry<0 || entry >= rlcd.value.size()) {
	                	if(cred.value.type==CredDAO.FQI) {
	                		return Result.err(Status.ERR_BadData,"FQI does not exist");
	                	} else {
	                		return Result.err(Status.ERR_BadData,"Invalid Choice [" + entry + "] chosen for Delete [%s] is saved for future processing",cred.value.id);
	                	}
	                }
	                udr = ques.credDAO().delete(trans, rlcd.value.get(entry),false);
	            } else {
	                for (CredDAO.Data curr : rlcd.value) {
	                    udr = ques.credDAO().delete(trans, curr, false);
	                    if (udr.notOK()) {
	                        return Result.err(udr);
	                    }
	                }
	            }
	            if (isLastCred) {
	                Result<List<UserRoleDAO.Data>> rlurd = ques.userRoleDAO().readByUser(trans, cred.value.id);
	                if (rlurd.isOK()) {
	                    for (UserRoleDAO.Data data : rlurd.value) {
	                        ques.userRoleDAO().delete(trans, data, false);
	                    }
	                }
	            }
	            if (udr==null) {
	                return Result.err(Result.ERR_NotFound,"No User Data found");
	            }
	            if (udr.isOK()) {
	                return Result.ok();
	            }
	            return Result.err(udr);
	        default:
	            return Result.err(fd);
	    }
	
	}

	/*
	 * Codify the way to get Either Choice Needed or actual Integer from Credit Request
	 */
	private Result<Integer> selectEntryIfMultiple(final CredRequest cr, List<CredDAO.Data> lcd, String action) {
	    int entry = 0;
	    if (lcd.size() > 1) {
	        String inputOption = cr.getEntry();
	        if (inputOption == null) {
	            String message = selectCredFromList(lcd, action);
	            Object[] variables = buildVariables(lcd);
	            return Result.err(Status.ERR_ChoiceNeeded, message, variables);
	        } else {
	        	if(MayChangeCred.EXTEND.equals(action)) {
	        		// might be Tag
	        		if(inputOption.length()>4) { //Tag is at least 12
	        			int e = 0;
	        			CredDAO.Data last = null;
	        			int lastIdx = -1;
	        			for(CredDAO.Data cdd : lcd) {
	        				if(inputOption.equals(cdd.tag)) {
	        					if(last==null) {
	        						last = cdd;
	        						lastIdx = e;
	        					} else {
	        						if(last.expires.before(cdd.expires)) {
	        							last = cdd;
	        							lastIdx = e;
	        						}
	        					}
	        				}
	        				++e;
	        			}
	        			if(last!=null) {
	        				return Result.ok(lastIdx);
	        			}
	        			return Result.err(Status.ERR_BadData, "User chose unknown Tag");
	        		}
	        	}
	            entry = Integer.parseInt(inputOption) - 1;
	        }
	        if (entry < 0 || entry >= lcd.size()) {
	            return Result.err(Status.ERR_BadData, "User chose invalid credential selection");
	        }
	    }
	    return Result.ok(entry);
	}

	private List<CredDAO.Data> filterList(List<CredDAO.Data> orig, Integer ... types) {
    	List<CredDAO.Data> rv = new ArrayList<>();
        for(CredDAO.Data cdd : orig) {
        	if(cdd!=null) {
	        	for(int t : types) {
	        		if(t==cdd.type) {
	           			rv.add(cdd);
	        		}
	        	}
        	}
        }
        Collections.sort(rv, (o1,o2) -> {
        	if(o1.type==o2.type) {
        		return o1.expires.compareTo(o2.expires);
        	} else {
        		return o1.type.compareTo(o2.type);
        	}
        });
		return rv;
	}

	private String[] buildVariables(List<CredDAO.Data> value) {
        String [] vars = new String[value.size()];
        CredDAO.Data cdd;
        
        for (int i = 0; i < value.size(); i++) {
        	cdd = value.get(i);
        	vars[i] = cdd.id + TWO_SPACE + Define.getCredType(cdd.type) + TWO_SPACE + Chrono.niceUTCStamp(cdd.expires) + TWO_SPACE + cdd.tag;
        }
        return vars;
    }
    
    private String selectCredFromList(List<CredDAO.Data> value, String action) {
        StringBuilder errMessage = new StringBuilder();
        String userPrompt = MayChangeCred.DELETE.equals(action)?
        		"Select which cred to delete (set force=true to delete all):":
        		"Select which cred to " + action + ':';
        int numSpaces = value.get(0).id.length() - "Id".length();
        
        errMessage.append(userPrompt + '\n');
        errMessage.append("        ID");
        for (int i = 0; i < numSpaces; i++) {
            errMessage.append(' ');
        }
        errMessage.append("  Type  Expires               Tag " + '\n');
        for (int i=0;i<value.size();++i) {
            errMessage.append("    %s\n");
        }
        if(MayChangeCred.EXTEND.equals(action)) {
            errMessage.append("Run same command again with chosen entry or Tag as last parameter");
        } else {
        	errMessage.append("Run same command again with chosen entry as last parameter");
        }
        return errMessage.toString();
        
    }

    @Override
    public Result<Date> doesCredentialMatch(AuthzTrans trans, REQUEST credReq) {
        TimeTaken tt = trans.start("Does Credential Match", Env.SUB);
        try {
            // Note: Mapper assigns RAW type
            Result<CredDAO.Data> data = mapper.cred(trans, credReq,false);
            if (data.notOKorIsEmpty()) {
                return Result.err(data);
            }
            CredDAO.Data cred = data.value;    // of the Mapped Cred
            if (cred.cred==null) {
                return Result.err(Result.ERR_BadData,"No Password");
            } else {
                return ques.doesUserCredMatch(trans, cred.id, cred.cred.array());
            }

        } catch (DAOException e) {
            trans.error().log(e,"Error looking up cred");
            return Result.err(Status.ERR_Denied,"Credential does not match");
        } finally {
            tt.done();
        }
    }

    @ApiDoc( 
            method = POST,  
            path = "/authn/validate",
            params = {},
            expectedCode = 200,
            errorCodes = { 403 }, 
            text = { "Validate a Credential given a Credential Structure.  This is a more comprehensive validation, can "
                    + "do more than BasicAuth as Credential types exp" }
            )
    @Override
    public Result<Date> validateBasicAuth(AuthzTrans trans, String basicAuth) {
        //TODO how to make sure people don't use this in browsers?  Do we care?
        TimeTaken tt = trans.start("Validate Basic Auth", Env.SUB);
        try {
            BasicPrincipal bp = new BasicPrincipal(basicAuth,trans.org().getRealm());
            Result<Date> rq = ques.doesUserCredMatch(trans, bp.getName(), bp.getCred());
            // Note: Only want to log problem, don't want to send back to end user
            if (rq.isOK()) {
                return rq;
            } else {
                trans.audit().log(rq.errorString());
            }
        } catch (Exception e) {
            trans.warn().log(e);
        } finally {
            tt.done();
        }
        return Result.err(Status.ERR_Denied,"Bad Basic Auth");
    }

@ApiDoc( 
	        method = GET,  
	        path = "/authn/basicAuth",
	        params = {},
	        expectedCode = 200,
	        errorCodes = { 403 }, 
	        text = { "!!!! DEPRECATED without X509 Authentication STOP USING THIS API BY DECEMBER 2017, or use Certificates !!!!\n" 
	                + "Use /authn/validate instead\n"
	                + "Note: Validate a Password using BasicAuth Base64 encoded Header. This HTTP/S call is intended as a fast"
	                + " User/Password lookup for Security Frameworks, and responds 200 if it passes BasicAuth "
	            + "security, and 403 if it does not." }
	        )
	private void basicAuth() {
	    // This is a place holder for Documentation.  The real BasicAuth API does not call Service.
	}

/***********************************
 * USER-ROLE 
 ***********************************/
    @ApiDoc( 
            method = POST,  
            path = "/authz/userRole",
            params = {},
            expectedCode = 201,
            errorCodes = {403,404,406,409}, 
            text = { "Create a UserRole relationship (add User to Role)",
                     "A UserRole is an object Representation of membership of a Role for limited time.",
                     "If a shorter amount of time for Role ownership is required, use the 'End' field.",
                     "** Note: Owners of Namespaces will be required to revalidate users in these roles ",
                     "before Expirations expire.  Namespace owners will be notified by email."
                   }
            )
    @Override
    public Result<Void> createUserRole(final AuthzTrans trans, REQUEST from) {
        TimeTaken tt = trans.start("Create UserRole", Env.SUB);
        try {
            Result<UserRoleDAO.Data> urr = mapper.userRole(trans, from);
            if (urr.notOKorIsEmpty()) {
                return Result.err(urr);
            }
            final UserRoleDAO.Data userRole = urr.value;
            
            final ServiceValidator v = new ServiceValidator();
            if (v.user_role(trans.user(),userRole).err() ||
               v.user(trans.org(), userRole.user).err()) {
                return Result.err(Status.ERR_BadData,v.errs());
            }


             
            // Check if user can change first
            Result<FutureDAO.Data> fd = mapper.future(trans,UserRoleDAO.TABLE,from,urr.value,true, // may request Approvals
                () -> "Add User [" + userRole.user + "] to Role [" +
                        userRole.role +
                        ']',
                new MayChange() {
                    private Result<NsDAO.Data> nsd;
                    @Override
                    public Result<?> mayChange() {
                    	if(urr.value.role.startsWith(urr.value.user)) {
                    		return Result.ok((NsDAO.Data)null);
                    	}
                        if (nsd==null) {
                            RoleDAO.Data r = RoleDAO.Data.decode(userRole);
                            nsd = ques.mayUser(trans, trans.user(), r, Access.write);
                        }
                        return nsd;
                    }
                });
            
            NsDAO.Data ndd;
            if(userRole.role.startsWith(userRole.user)) {
            	userRole.ns=userRole.user;
            	userRole.rname="user";
            	ndd = null;
            } else {
	            Result<NsDAO.Data> nsr = ques.deriveNs(trans, userRole.role);
	            if (nsr.notOK()) {
	                return Result.err(nsr);
	            }
	            ndd = nsr.value;
            }

            switch(fd.status) {
                case OK:
                    Result<String> rfc = func.createFuture(trans, fd.value, userRole.user+'|'+userRole.ns + '.' + userRole.rname, 
                            userRole.user, ndd, FUTURE_OP.C);
                    if (rfc.isOK()) {
                        return Result.err(Status.ACC_Future, "UserRole [%s - %s.%s] is saved for future processing",
                                userRole.user,
                                userRole.ns,
                                userRole.rname);
                    } else { 
                        return Result.err(rfc);
                    }
                case Status.ACC_Now:
                    return func.addUserRole(trans, userRole);
                default:
                    return Result.err(fd);
            }
        } finally {
            tt.done();
        }
    }
    
        /**
         * getUserRolesByRole
         */
        @ApiDoc(
                method = GET,
                path = "/authz/userRoles/role/:role",
                params = {"role|string|true"},
                expectedCode = 200,
                errorCodes = {404,406},
                text = { "List all Users that are attached to Role specified in :role",
                        }
               )
        @Override
        public Result<USERROLES> getUserRolesByRole(AuthzTrans trans, String role) {
            final Validator v = new ServiceValidator();
            if (v.nullOrBlank("Role",role).err()) {
                return Result.err(Status.ERR_BadData,v.errs());
            }
            
            Result<RoleDAO.Data> rrdd;
            rrdd = RoleDAO.Data.decode(trans,ques,role);
            if (rrdd.notOK()) {
                return Result.err(rrdd);
            }
            // May Requester see result?
            Result<NsDAO.Data> ns = ques.mayUser(trans,trans.user(), rrdd.value,Access.read);
            if (ns.notOK()) {
                return Result.err(ns);
            }
    
    //        boolean filter = true;        
    //        if (ns.value.isAdmin(trans.user()) || ns.value.isResponsible(trans.user()))
    //            filter = false;
            
            // Get list of roles per user, then add to Roles as we go
            HashSet<UserRoleDAO.Data> userSet = new HashSet<>();
            Result<List<UserRoleDAO.Data>> rlurd = ques.userRoleDAO().readByRole(trans, role);
            if (rlurd.isOK()) {
                for (UserRoleDAO.Data data : rlurd.value) {
                    userSet.add(data);
                }
            }
            
            @SuppressWarnings("unchecked")
            USERROLES users = (USERROLES) mapper.newInstance(API.USER_ROLES);
            // Checked for permission
            mapper.userRoles(trans, userSet, users);
            return Result.ok(users);
        }
        /**
         * getUserRolesByRole
         */
        @ApiDoc(
                method = GET,
                path = "/authz/userRoles/user/:user",
                params = {"role|string|true"},
                expectedCode = 200,
                errorCodes = {404,406},
                text = { "List all UserRoles for :user",
                        }
               )
        @Override
        public Result<USERROLES> getUserRolesByUser(AuthzTrans trans, String user) {
            final Validator v = new ServiceValidator();
            if (v.nullOrBlank("User",user).err()) {
                return Result.err(Status.ERR_BadData,v.errs());
            }
            
            // Get list of roles per user, then add to Roles as we go
            Result<List<UserRoleDAO.Data>> rlurd = ques.userRoleDAO().readByUser(trans, user);
            if (rlurd.notOK()) { 
                return Result.err(rlurd);
            }
            
            /* Check for
             *   1) is User 
             *   2) is User's Supervisor
             *   3) Has special global access =read permission
             *   
             *   If none of the 3, then filter results to NSs in which Calling User has Ns.access * read
             */
            boolean mustFilter;
            String callingUser = trans.getUserPrincipal().getName();
            NsDAO.Data ndd = new NsDAO.Data();

            if (user.equals(callingUser)) {
                mustFilter = false;
            } else {
                Organization org = trans.org();
                try {
                    Identity orgID = org.getIdentity(trans, user);
                    Identity manager = orgID==null?null:orgID.responsibleTo();
                    if (orgID!=null && (manager!=null && callingUser.equals(manager.fullID()))) {
                        mustFilter = false;
                    } else if (ques.isGranted(trans, callingUser, ROOT_NS, Question.ACCESS, "*", Access.read.name())) {
                        mustFilter=false;
                    } else {
                        mustFilter = true;
                    }
                } catch (OrganizationException e) {
                    trans.env().log(e);
                    mustFilter = true;
                }
            }
            
            List<UserRoleDAO.Data> content;
            if (mustFilter) {
                content = new ArrayList<>(rlurd.value.size()); // avoid multi-memory redos
                
                for (UserRoleDAO.Data data : rlurd.value) {
                    ndd.name=data.ns;
                    Result<Data> mur = ques.mayUser(trans, callingUser, ndd, Access.read);
                    if (mur.isOK()){
                        content.add(data);
                    }
                }
                
            } else {
                content = rlurd.value;
            }


            @SuppressWarnings("unchecked")
            USERROLES users = (USERROLES) mapper.newInstance(API.USER_ROLES);
            // Checked for permission
            mapper.userRoles(trans, content, users);
            return Result.ok(users);
        }

        
 
    
     @ApiDoc(
            method = GET,
            path = "/authz/userRole/extend/:user/:role",
            params = {    "user|string|true",
                        "role|string|true"
                    },
            expectedCode = 200,
            errorCodes = {403,404,406},
            text = { "Extend the Expiration of this User Role by the amount set by Organization",
                     "Requestor must be allowed to modify the role"
                    }
           )
    @Override
    public Result<Void> extendUserRole(AuthzTrans trans, String user, String role) {
        Organization org = trans.org();
        final ServiceValidator v = new ServiceValidator();
        if (v.user(org, user)
            .role(role)
            .err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
    
        Result<RoleDAO.Data> rrdd = RoleDAO.Data.decode(trans,ques,role);
        if (rrdd.notOK()) {
            return Result.err(rrdd);
        }
        
        Result<NsDAO.Data> rcr = ques.mayUser(trans, trans.user(), rrdd.value, Access.write);
        boolean mayNotChange;
        if ((mayNotChange = rcr.notOK()) && !trans.requested(future)) {
            return Result.err(rcr);
        }
        
        Result<List<UserRoleDAO.Data>> rr = ques.userRoleDAO().read(trans, user,role);
        if (rr.notOK()) {
            return Result.err(rr);
        }
        for (UserRoleDAO.Data userRole : rr.value) {
            if (mayNotChange) { // Function exited earlier if !trans.futureRequested
                FutureDAO.Data fto = new FutureDAO.Data();
                fto.target=UserRoleDAO.TABLE;
                fto.memo = "Extend User ["+userRole.user+"] in Role ["+userRole.role+"]";
                GregorianCalendar now = new GregorianCalendar();
                fto.start = now.getTime();
                fto.expires = org.expiration(now, Expiration.Future).getTime();
                try {
                    fto.construct = userRole.bytify();
                } catch (IOException e) {
                    trans.error().log(e, "Error while bytifying UserRole for Future");
                    return Result.err(e);
                }

                Result<String> rfc = func.createFuture(trans, fto, 
                        userRole.user+'|'+userRole.role, userRole.user, rcr.value, FUTURE_OP.U);
                if (rfc.isOK()) {
                    return Result.err(Status.ACC_Future, "UserRole [%s - %s] is saved for future processing",
                            userRole.user,
                            userRole.role);
                } else {
                    return Result.err(rfc);
                }
            } else {
                return func.extendUserRole(trans, userRole, false);
            }
        }
        return Result.err(Result.ERR_NotFound,"This user and role doesn't exist");
    }

    @ApiDoc( 
            method = DELETE,  
            path = "/authz/userRole/:user/:role",
            params = {    "user|string|true",
                        "role|string|true"
                    },
            expectedCode = 200,
            errorCodes = {403,404,406}, 
            text = { "Remove Role :role from User :user."
                   }
            )
    @Override
    public Result<Void> deleteUserRole(AuthzTrans trans, String usr, String role) {
        Validator val = new ServiceValidator();
        if (val.nullOrBlank("User", usr)
              .nullOrBlank("Role", role).err()) {
            return Result.err(Status.ERR_BadData, val.errs());
        }

        boolean mayNotChange;
        Result<RoleDAO.Data> rrdd = RoleDAO.Data.decode(trans,ques,role);
        if (rrdd.notOK()) {
            return Result.err(rrdd);
        }
        
        RoleDAO.Data rdd = rrdd.value;
        Result<NsDAO.Data> rns = ques.mayUser(trans, trans.user(), rdd, Access.write);

        // Make sure we don't delete the last owner of valid NS
        if (rns.isOKhasData() && Question.OWNER.equals(rdd.name) && ques.countOwner(trans,rdd.ns)<=1) {
            return Result.err(Status.ERR_Denied,"You may not delete the last Owner of " + rdd.ns );
        }
        
        if (mayNotChange=rns.notOK()) {
            if (!trans.requested(future)) {
                return Result.err(rns);
            }
        }

        Result<List<UserRoleDAO.Data>> rulr;
        if ((rulr=ques.userRoleDAO().read(trans, usr, role)).notOKorIsEmpty()) {
            return Result.err(Status.ERR_UserRoleNotFound, "User [ "+usr+" ] is not "
                    + "Assigned to the Role [ " + role + " ]");
        }

        UserRoleDAO.Data userRole = rulr.value.get(0);
        if (mayNotChange) { // Function exited earlier if !trans.futureRequested
            FutureDAO.Data fto = new FutureDAO.Data();
            fto.target=UserRoleDAO.TABLE;
            fto.memo = "Remove User ["+userRole.user+"] from Role ["+userRole.role+"]";
            GregorianCalendar now = new GregorianCalendar();
            fto.start = now.getTime();
            fto.expires = trans.org().expiration(now, Expiration.Future).getTime();

            Result<String> rfc = func.createFuture(trans, fto, 
                    userRole.user+'|'+userRole.role, userRole.user, rns.value, FUTURE_OP.D);
            if (rfc.isOK()) {
                return Result.err(Status.ACC_Future, "UserRole [%s - %s] is saved for future processing", 
                        userRole.user,
                        userRole.role);
            } else { 
                return Result.err(rfc);
            }
        } else {
            return ques.userRoleDAO().delete(trans, rulr.value.get(0), false);
        }
    }

    @ApiDoc( 
            method = GET,  
            path = "/authz/userRole/:user/:role",
            params = {"user|string|true",
                      "role|string|true"},
            expectedCode = 200,
            errorCodes = {403,404,406}, 
            text = { "Returns the User (with Expiration date from listed User/Role) if it exists"
                   }
            )
    @Override
    public Result<USERS> getUserInRole(AuthzTrans trans, String user, String role) {
        final Validator v = new ServiceValidator();
        if (v.role(role).nullOrBlank("User", user).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

//        Result<NsDAO.Data> ns = ques.deriveNs(trans, role);
//        if (ns.notOK()) return Result.err(ns);
//        
//        Result<NsDAO.Data> rnd = ques.mayUser(trans, trans.user(), ns.value, Access.write);
        // May calling user see by virtue of the Role
        Result<RoleDAO.Data> rrdd = RoleDAO.Data.decode(trans, ques, role);
        if (rrdd.notOK()) {
            return Result.err(rrdd);
        }
        Result<NsDAO.Data> rnd = ques.mayUser(trans, trans.user(), rrdd.value,Access.read);
        if (rnd.notOK()) {
            return Result.err(rnd); 
        }
        
        HashSet<UserRoleDAO.Data> userSet = new HashSet<>();
        Result<List<UserRoleDAO.Data>> rlurd = ques.userRoleDAO().readUserInRole(trans, user, role);
        if (rlurd.isOK()) {
            for (UserRoleDAO.Data data : rlurd.value) {
                userSet.add(data);
            }
        }
        
        @SuppressWarnings("unchecked")
        USERS users = (USERS) mapper.newInstance(API.USERS);
        mapper.users(trans, userSet, users);
        return Result.ok(users);
    }

    @ApiDoc( 
            method = GET,  
            path = "/authz/users/role/:role",
            params = {"user|string|true",
                      "role|string|true"},
            expectedCode = 200,
            errorCodes = {403,404,406}, 
            text = { "Returns the User (with Expiration date from listed User/Role) if it exists"
                   }
            )
    @Override
    public Result<USERS> getUsersByRole(AuthzTrans trans, String role) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("Role",role).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

//        Result<NsDAO.Data> ns = ques.deriveNs(trans, role);
//        if (ns.notOK()) return Result.err(ns);
//        
//        Result<NsDAO.Data> rnd = ques.mayUser(trans, trans.user(), ns.value, Access.write);
        // May calling user see by virtue of the Role
        Result<RoleDAO.Data> rrdd = RoleDAO.Data.decode(trans, ques, role);
        if (rrdd.notOK()) {
            return Result.err(rrdd);
        }
        
        boolean contactOnly = false;
        // Allow the request of any valid user to find the contact of the NS (Owner)
        Result<NsDAO.Data> rnd = ques.mayUser(trans, trans.user(), rrdd.value,Access.read);
        if (rnd.notOK()) {
            if (Question.OWNER.equals(rrdd.value.name)) {
                contactOnly = true;
            } else {
                return Result.err(rnd);
            }
        }
        
        HashSet<UserRoleDAO.Data> userSet = new HashSet<>();
        Result<List<UserRoleDAO.Data>> rlurd = ques.userRoleDAO().readByRole(trans, role);
        if (rlurd.isOK()) { 
            for (UserRoleDAO.Data data : rlurd.value) {
                if (contactOnly) { //scrub data
                    // Can't change actual object, or will mess up the cache.
                    UserRoleDAO.Data scrub = new UserRoleDAO.Data();
                    scrub.ns = data.ns;
                    scrub.rname = data.rname;
                    scrub.role = data.role;
                    scrub.user = data.user;
                    userSet.add(scrub);
                } else {
                    userSet.add(data);
                }
            }
        }
        
        @SuppressWarnings("unchecked")
        USERS users = (USERS) mapper.newInstance(API.USERS);
        mapper.users(trans, userSet, users);
        return Result.ok(users);
    }

    /**
     * getUsersByPermission
     */
    @ApiDoc(
            method = GET,
            path = "/authz/users/perm/:type/:instance/:action",
            params = {    "type|string|true",
                        "instance|string|true",
                        "action|string|true"
                    },
            expectedCode = 200,
            errorCodes = {404,406},
            text = { "List all Users that have Permission specified by :type :instance :action",
                    }
           )
    @Override
    public Result<USERS> getUsersByPermission(AuthzTrans trans, String type, String instance, String action) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("Type",type)
            .nullOrBlank("Instance",instance)
            .nullOrBlank("Action",action)            
            .err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        Result<NsSplit> nss = ques.deriveNsSplit(trans, type);
        if (nss.notOK()) {
            return Result.err(nss);
        }
        
        Result<List<NsDAO.Data>> nsd = ques.nsDAO().read(trans, nss.value.ns);
        if (nsd.notOK()) {
            return Result.err(nsd);
        }
        
        boolean allInstance = ASTERIX.equals(instance);
        boolean allAction = ASTERIX.equals(action);
        // Get list of roles per Permission, 
        // Then loop through Roles to get Users
        // Note: Use Sets to avoid processing or responding with Duplicates
        Set<String> roleUsed = new HashSet<>();
        Set<UserRoleDAO.Data> userSet = new HashSet<>();
        
        if (!nss.isEmpty()) {
            Result<List<PermDAO.Data>> rlp = ques.permDAO().readByType(trans, nss.value.ns, nss.value.name);
            if (rlp.isOKhasData()) {
                for (PermDAO.Data pd : rlp.value) {
                    if ((allInstance || pd.instance.equals(instance)) && 
                            (allAction || pd.action.equals(action))) {
                        if (ques.mayUser(trans, trans.user(),pd,Access.read).isOK()) {
                            for (String role : pd.roles) {
                                if (!roleUsed.contains(role)) { // avoid evaluating Role many times
                                    roleUsed.add(role);
                                    Result<List<UserRoleDAO.Data>> rlurd = ques.userRoleDAO().readByRole(trans, role.replace('|', '.'));
                                    if (rlurd.isOKhasData()) {
                                        for (UserRoleDAO.Data urd : rlurd.value) {
                                            userSet.add(urd);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        @SuppressWarnings("unchecked")
        USERS users = (USERS) mapper.newInstance(API.USERS);
        mapper.users(trans, userSet, users);
        return Result.ok(users);
    }

/***********************************
 * HISTORY 
 ***********************************/    
    @Override
    public Result<HISTORY> getHistoryByUser(final AuthzTrans trans, String user, final int[] yyyymm, final int sort) {    
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("User",user).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        Result<NsDAO.Data> rnd;
        // Users may look at their own data
         if (trans.user().equals(user)) {
                // Users may look at their own data
         } else {
            int at = user.indexOf('@');
            if (at>=0 && trans.org().getRealm().equals(user.substring(at+1))) {
                NsDAO.Data nsd  = new NsDAO.Data();
                nsd.name = Question.domain2ns(user);
                rnd = ques.mayUser(trans, trans.user(), nsd, Access.read);
                if (rnd.notOK()) {
                    return Result.err(rnd);
                }
            } else {
                rnd = ques.validNSOfDomain(trans, user);
                if (rnd.notOK()) {
                    return Result.err(rnd);
                }

                rnd = ques.mayUser(trans, trans.user(), rnd.value, Access.read);
                if (rnd.notOK()) {
                    return Result.err(rnd);
                }
            }
         }
        Result<List<HistoryDAO.Data>> resp = ques.historyDAO().readByUser(trans, user, yyyymm);
        if (resp.notOK()) {
            return Result.err(resp);
        }
        return mapper.history(trans, resp.value,sort);
    }

    @Override
    public Result<HISTORY> getHistoryByRole(AuthzTrans trans, String role, int[] yyyymm, final int sort) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("Role",role).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        Result<RoleDAO.Data> rrdd = RoleDAO.Data.decode(trans, ques, role);
        if (rrdd.notOK()) {
            return Result.err(rrdd);
        }
        
        Result<NsDAO.Data> rnd = ques.mayUser(trans, trans.user(), rrdd.value, Access.read);
        if (rnd.notOK()) {
            return Result.err(rnd);
        }
        Result<List<HistoryDAO.Data>> resp = ques.historyDAO().readBySubject(trans, role, "role", yyyymm); 
        if (resp.notOK()) {
            return Result.err(resp);
        }
        return mapper.history(trans, resp.value,sort);
    }

    @Override
    public Result<HISTORY> getHistoryByPerm(AuthzTrans trans, String type, int[] yyyymm, final int sort) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("Type",type)
            .err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        // May user see Namespace of Permission (since it's only one piece... we can't check for "is permission part of")
        Result<List<HistoryDAO.Data>> resp;
        if(type.startsWith(trans.user())) {
        	resp = ques.historyDAO().readBySubject(trans, type, "perm", yyyymm);
        } else {
            Result<NsDAO.Data> rnd = ques.deriveNs(trans,type);
	        if (rnd.notOK()) {
	            return Result.err(rnd);
	        }
	        rnd = ques.mayUser(trans, trans.user(), rnd.value, Access.read);
	        if (rnd.notOK()) {
	            return Result.err(rnd);    
	        }
	        resp = ques.historyDAO().readBySubject(trans, type, "perm", yyyymm);
        }
        
        if (resp.notOK()) {
            return Result.err(resp);
        }
        return mapper.history(trans, resp.value,sort);
    }

    @Override
    public Result<HISTORY> getHistoryByNS(AuthzTrans trans, String ns, int[] yyyymm, final int sort) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("NS",ns).err()) { 
            return Result.err(Status.ERR_BadData,v.errs());
        }

        Result<NsDAO.Data> rnd = ques.deriveNs(trans,ns);
        if (rnd.notOK()) {
            return Result.err(rnd);
        }
        rnd = ques.mayUser(trans, trans.user(), rnd.value, Access.read);
        if (rnd.notOK()) {
            return Result.err(rnd);    
        }

        Result<List<HistoryDAO.Data>> resp = ques.historyDAO().readBySubject(trans, ns, "ns", yyyymm);
        if (resp.notOK()) {
            return Result.err(resp);
        }
        return mapper.history(trans, resp.value,sort);
    }

    @Override
    public Result<HISTORY> getHistoryBySubject(AuthzTrans trans, String subject, String target, int[] yyyymm, final int sort) {
    	NsDAO.Data ndd = new NsDAO.Data();
    	ndd.name = FQI.reverseDomain(subject);
        Result<Data> rnd = ques.mayUser(trans, trans.user(), ndd, Access.read);
        if (rnd.notOK()) {
            return Result.err(rnd);    
        }

        Result<List<HistoryDAO.Data>> resp = ques.historyDAO().readBySubject(trans, subject, target, yyyymm);
        if (resp.notOK()) {
            return Result.err(resp);
        }
        return mapper.history(trans, resp.value,sort);
    }

/***********************************
 * DELEGATE 
 ***********************************/
    @Override
    public Result<Void> createDelegate(final AuthzTrans trans, REQUEST base) {
        return createOrUpdateDelegate(trans, base, Question.Access.create);
    }

    @Override
    public Result<Void> updateDelegate(AuthzTrans trans, REQUEST base) {
        return createOrUpdateDelegate(trans, base, Question.Access.write);
    }


    private Result<Void> createOrUpdateDelegate(final AuthzTrans trans, REQUEST base, final Access access) {
        final Result<DelegateDAO.Data> rd = mapper.delegate(trans, base);
        final ServiceValidator v = new ServiceValidator();
        if (v.delegate(trans.org(),rd).err()) { 
            return Result.err(Status.ERR_BadData,v.errs());
        }

        final DelegateDAO.Data dd = rd.value;
        
        Result<List<DelegateDAO.Data>> ddr = ques.delegateDAO().read(trans, dd);
        if (access==Access.create && ddr.isOKhasData()) {
            return Result.err(Status.ERR_ConflictAlreadyExists, "[%s] already delegates to [%s]", dd.user, ddr.value.get(0).delegate);
        } else if (access!=Access.create && ddr.notOKorIsEmpty()) { 
            return Result.err(Status.ERR_NotFound, "[%s] does not have a Delegate Record to [%s].",dd.user,access.name());
        }
        Result<Void> rv = ques.mayUser(trans, dd, access);
        if (rv.notOK()) {
            return rv;
        }
        
        Result<FutureDAO.Data> fd = mapper.future(trans,DelegateDAO.TABLE,base, dd, false,
            () -> {
                StringBuilder sb = new StringBuilder();
                sb.append(access.name());
                sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
                sb.append("Delegate ");
                sb.append(access==Access.create?"[":"to [");
                sb.append(rd.value.delegate);
                sb.append("] for [");
                sb.append(rd.value.user);
                sb.append(']');
                return sb.toString();
            },
            () -> {
                return Result.ok(); // Validate in code above
            });
        
        switch(fd.status) {
            case OK:
                Result<String> rfc = func.createFuture(trans, fd.value, 
                        dd.user, trans.user(),null, access==Access.create?FUTURE_OP.C:FUTURE_OP.U);
                if (rfc.isOK()) { 
                    return Result.err(Status.ACC_Future, "Delegate for [%s]",
                            dd.user);
                } else { 
                    return Result.err(rfc);
                }
            case Status.ACC_Now:
                if (access==Access.create) {
                    Result<DelegateDAO.Data> rdr = ques.delegateDAO().create(trans, dd);
                    if (rdr.isOK()) {
                        return Result.ok();
                    } else {
                        return Result.err(rdr);
                    }
                } else {
                    return ques.delegateDAO().update(trans, dd);
                }
            default:
                return Result.err(fd);
        }
    }

    @Override
    public Result<Void> deleteDelegate(AuthzTrans trans, REQUEST base) {
        final Result<DelegateDAO.Data> rd = mapper.delegate(trans, base);
        final Validator v = new ServiceValidator();
        if (v.notOK(rd).nullOrBlank("User", rd.value.user).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        
        Result<List<DelegateDAO.Data>> ddl;
        if ((ddl=ques.delegateDAO().read(trans, rd.value)).notOKorIsEmpty()) {
            return Result.err(Status.ERR_DelegateNotFound,"Cannot delete non-existent Delegate");
        }
        final DelegateDAO.Data dd = ddl.value.get(0);
        Result<Void> rv = ques.mayUser(trans, dd, Access.write);
        if (rv.notOK()) {
            return rv;
        }
        
        return ques.delegateDAO().delete(trans, dd, false);
    }

    @Override
    public Result<Void> deleteDelegate(AuthzTrans trans, String userName) {
        DelegateDAO.Data dd = new DelegateDAO.Data();
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("User", userName).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        dd.user = userName;
        Result<List<DelegateDAO.Data>> ddl;
        if ((ddl=ques.delegateDAO().read(trans, dd)).notOKorIsEmpty()) {
            return Result.err(Status.ERR_DelegateNotFound,"Cannot delete non-existent Delegate");
        }
        dd = ddl.value.get(0);
        Result<Void> rv = ques.mayUser(trans, dd, Access.write);
        if (rv.notOK()) {
            return rv;
        }
        
        return ques.delegateDAO().delete(trans, dd, false);
    }
    
    @Override
    public Result<DELGS> getDelegatesByUser(AuthzTrans trans, String user) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("User", user).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        DelegateDAO.Data ddd = new DelegateDAO.Data();
        ddd.user = user;
        ddd.delegate = null;
        Result<Void> rv = ques.mayUser(trans, ddd, Access.read);
        if (rv.notOK()) {
            return Result.err(rv);
        }
        
        TimeTaken tt = trans.start("Get delegates for a user", Env.SUB);

        Result<List<DelegateDAO.Data>> dbDelgs = ques.delegateDAO().read(trans, user);
        try {
            if (dbDelgs.isOKhasData()) {
                return mapper.delegate(dbDelgs.value);
            } else {
                return Result.err(Status.ERR_DelegateNotFound,"No Delegate found for [%s]",user);
            }
        } finally {
            tt.done();
        }        
    }

    @Override
    public Result<DELGS> getDelegatesByDelegate(AuthzTrans trans, String delegate) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("Delegate", delegate).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }

        DelegateDAO.Data ddd = new DelegateDAO.Data();
        ddd.user = delegate;
        Result<Void> rv = ques.mayUser(trans, ddd, Access.read);
        if (rv.notOK()) {
            return Result.err(rv);
        }

        TimeTaken tt = trans.start("Get users for a delegate", Env.SUB);

        Result<List<DelegateDAO.Data>> dbDelgs = ques.delegateDAO().readByDelegate(trans, delegate);
        try {
            if (dbDelgs.isOKhasData()) {
                return mapper.delegate(dbDelgs.value);
            } else {
                return Result.err(Status.ERR_DelegateNotFound,"Delegate [%s] is not delegating for anyone.",delegate);
            }
        } finally {
            tt.done();
        }        
    }

/***********************************
 * APPROVAL 
 ***********************************/
    private static final String APPR_FMT = "actor=%s, action=%s, operation=\"%s\", requestor=%s, delegator=%s";
    @Override
    public Result<Void> updateApproval(AuthzTrans trans, APPROVALS approvals) {
        Result<List<ApprovalDAO.Data>> rlad = mapper.approvals(approvals);
        if (rlad.notOK()) {
            return Result.err(rlad);
        }
        int numApprs = rlad.value.size();
        if (numApprs<1) {
            return Result.err(Status.ERR_NoApprovals,"No Approvals sent for Updating");
        }
        int numProcessed = 0;
        String user = trans.user();
        
        Result<List<ApprovalDAO.Data>> curr;
        Lookup<List<ApprovalDAO.Data>> apprByTicket=null;
        for (ApprovalDAO.Data updt : rlad.value) {
            if (updt.ticket!=null) {
                curr = ques.approvalDAO().readByTicket(trans, updt.ticket);
                if (curr.isOKhasData()) {
                    final List<ApprovalDAO.Data> add = curr.value;
                    // Store a Pre-Lookup
                    apprByTicket = (trans1, noop) -> add;
                }
            } else if (updt.id!=null) {
                curr = ques.approvalDAO().read(trans, updt);
            } else if (updt.approver!=null) {
                curr = ques.approvalDAO().readByApprover(trans, updt.approver);
            } else {
                return Result.err(Status.ERR_BadData,"Approvals need ID, Ticket or Approval data to update");
            }

            if (curr.isOKhasData()) {
                Map<String, Result<List<DelegateDAO.Data>>> delegateCache = new HashMap<>();
                Map<UUID, FutureDAO.Data> futureCache = new HashMap<>();
                FutureDAO.Data hasDeleted = new FutureDAO.Data();
                
                for (ApprovalDAO.Data cd : curr.value) {
                    if ("pending".equals(cd.status)) {
                        // Check for right record.  Need ID, or (Ticket&Trans.User==Appr)
                        // If Default ID
                        boolean delegatedAction = ques.isDelegated(trans, user, cd.approver, delegateCache);
                        String delegator = cd.approver;
                        if (updt.id!=null || 
                            (updt.ticket!=null && user.equals(cd.approver)) ||
                            (updt.ticket!=null && delegatedAction)) {
                            if (updt.ticket.equals(cd.ticket)) {
                                Changed ch = new Changed();
                                cd.id = ch.changed(cd.id,updt.id);
//                                cd.ticket = changed(cd.ticket,updt.ticket);
                                cd.user = ch.changed(cd.user,updt.user);
                                cd.approver = ch.changed(cd.approver,updt.approver);
                                cd.type = ch.changed(cd.type,updt.type);
                                cd.status = ch.changed(cd.status,updt.status);
                                cd.memo = ch.changed(cd.memo,updt.memo);
                                cd.operation = ch.changed(cd.operation,updt.operation);
                                cd.updated = ch.changed(cd.updated,updt.updated==null?new Date():updt.updated);
//                                if (updt.status.equals("denied")) {
//                                    cd.last_notified = null;
//                                }
                                if (cd.ticket!=null) {
                                    FutureDAO.Data fdd = futureCache.get(cd.ticket);
                                    if (fdd==null) { // haven't processed ticket yet
                                        Result<FutureDAO.Data> rfdd = ques.futureDAO().readPrimKey(trans, cd.ticket);
                                        if (rfdd.isOK()) {
                                            fdd = rfdd.value; // null is ok
                                        } else {
                                            fdd = hasDeleted;
                                        }
                                        futureCache.put(cd.ticket, fdd); // processed this Ticket... don't do others on this ticket
                                    }
                                    if (fdd==hasDeleted) { // YES, by Object
                                        cd.ticket = null;
                                        cd.status = "ticketDeleted";
                                        ch.hasChanged(true);
                                    } else {
                                        FUTURE_OP fop = FUTURE_OP.toFO(cd.operation);
                                        if (fop==null) {
                                            trans.info().printf("Approval Status %s is not actionable",cd.status);
                                        } else if (apprByTicket!=null) {
                                            Result<OP_STATUS> rv = func.performFutureOp(trans, fop, fdd, apprByTicket,func.urDBLookup);
                                            if (rv.isOK()) {
                                                switch(rv.value) {
                                                    case E:
                                                        if (delegatedAction) {
                                                            trans.audit().printf(APPR_FMT,user,updt.status,cd.memo,cd.user,delegator);
                                                        }
                                                        futureCache.put(cd.ticket, hasDeleted);
                                                        break;
                                                    case D:
                                                    case L:
                                                        ch.hasChanged(true);
                                                        trans.audit().printf(APPR_FMT,user,rv.value.desc(),cd.memo,cd.user,delegator);
                                                        futureCache.put(cd.ticket, hasDeleted);
                                                        break;
                                                    default:
                                                }
                                            } else {
                                                trans.info().log(rv.toString());
                                            }
                                        }

                                    }
                                    ++numProcessed;
                                }
                                if (ch.hasChanged()) {
                                    ques.approvalDAO().update(trans, cd, true);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (numApprs==numProcessed) {
            return Result.ok();
        }
        return Result.err(Status.ERR_ActionNotCompleted,numProcessed + " out of " + numApprs + " completed");

    }
    
    private static class Changed {
        private boolean hasChanged = false;

        public<T> T changed(T src, T proposed) {
            if (proposed==null || (src!=null && src.equals(proposed))) {
                return src;
            }
            hasChanged=true;
            return proposed;
        }

        public void hasChanged(boolean b) {
            hasChanged=b;
        }

        public boolean hasChanged() {
            return hasChanged;
        }
    }

    @Override
    public Result<APPROVALS> getApprovalsByUser(AuthzTrans trans, String user) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("User", user).err()) { 
            return Result.err(Status.ERR_BadData,v.errs());
        }

        Result<List<ApprovalDAO.Data>> rapd = ques.approvalDAO().readByUser(trans, user);
        if (rapd.isOK()) {
            return mapper.approvals(rapd.value);
        } else {
            return Result.err(rapd);
        }
}

    @Override
    public Result<APPROVALS> getApprovalsByTicket(AuthzTrans trans, String ticket) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("Ticket", ticket).err()) { 
            return Result.err(Status.ERR_BadData,v.errs());
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(ticket);
        } catch (IllegalArgumentException e) {
            return Result.err(Status.ERR_BadData,e.getMessage());
        }
    
        Result<List<ApprovalDAO.Data>> rapd = ques.approvalDAO().readByTicket(trans, uuid);
        if (rapd.isOK()) {
            return mapper.approvals(rapd.value);
        } else {
            return Result.err(rapd);
        }
    }
    
    @Override
    public Result<APPROVALS> getApprovalsByApprover(AuthzTrans trans, String approver) {
        final Validator v = new ServiceValidator();
        if (v.nullOrBlank("Approver", approver).err()) {
            return Result.err(Status.ERR_BadData,v.errs());
        }
        
        List<ApprovalDAO.Data> listRapds = new ArrayList<>();
        
        Result<List<ApprovalDAO.Data>> myRapd = ques.approvalDAO().readByApprover(trans, approver);
        if (myRapd.notOK()) {
            return Result.err(myRapd);
        }
        
        listRapds.addAll(myRapd.value);
        
        Result<List<DelegateDAO.Data>> delegatedFor = ques.delegateDAO().readByDelegate(trans, approver);
        if (delegatedFor.isOK()) {
            for (DelegateDAO.Data dd : delegatedFor.value) {
                if (dd.expires.after(new Date())) {
                    String delegator = dd.user;
                    Result<List<ApprovalDAO.Data>> rapd = ques.approvalDAO().readByApprover(trans, delegator);
                    if (rapd.isOK()) {
                        for (ApprovalDAO.Data d : rapd.value) { 
                            if (!d.user.equals(trans.user())) {
                                listRapds.add(d);
                            }
                        }
                    }
                }
            }
        }
        
        return mapper.approvals(listRapds);
    }
    
    /* (non-Javadoc)
     * @see org.onap.aaf.auth.service.AuthzService#clearCache(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String)
     */
    @Override
    public Result<Void> cacheClear(AuthzTrans trans, String cname) {
        if (ques.isGranted(trans,trans.user(),ROOT_NS,CACHE,cname,"clear")) {
            return ques.clearCache(trans,cname);
        }
        return Result.err(Status.ERR_Denied, "%s does not have AAF Permission '%s.%s|%s|clear",
                trans.user(),ROOT_NS,CACHE,cname);
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.service.AuthzService#cacheClear(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String, java.lang.Integer)
     */
    @Override
    public Result<Void> cacheClear(AuthzTrans trans, String cname, int[] segment) {
        if (ques.isGranted(trans,trans.user(),ROOT_NS,CACHE,cname,"clear")) {
            Result<Void> v=null;
            for (int i: segment) {
                v=ques.cacheClear(trans,cname,i);
            }
            if (v!=null) {
                return v;
            }
        }
        return Result.err(Status.ERR_Denied, "%s does not have AAF Permission '%s.%s|%s|clear",
                trans.user(),ROOT_NS,CACHE,cname);
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.service.AuthzService#dbReset(org.onap.aaf.auth.env.test.AuthzTrans)
     */
    @Override
    public void dbReset(AuthzTrans trans) {
        ques.historyDAO().reportPerhapsReset(trans, null);
    }

}

