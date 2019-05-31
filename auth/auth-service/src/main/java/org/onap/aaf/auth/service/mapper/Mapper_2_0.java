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

package org.onap.aaf.auth.service.mapper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.onap.aaf.auth.dao.Bytification;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.CertDAO;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.DelegateDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.HistoryDAO;
import org.onap.aaf.auth.dao.cass.Namespace;
import org.onap.aaf.auth.dao.cass.NsSplit;
import org.onap.aaf.auth.dao.cass.NsType;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.cass.DelegateDAO.Data;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.dao.hl.Question.Access;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTrans.REQD_TYPE;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Expiration;
import org.onap.aaf.auth.rserv.Pair;
import org.onap.aaf.auth.service.MayChange;
import org.onap.aaf.cadi.aaf.marshal.CertsMarshal;
import org.onap.aaf.cadi.util.Vars;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.util.Chrono;
import org.onap.aaf.misc.rosetta.Marshal;

import aaf.v2_0.Api;
import aaf.v2_0.Approval;
import aaf.v2_0.Approvals;
import aaf.v2_0.Certs;
import aaf.v2_0.Certs.Cert;
import aaf.v2_0.CredRequest;
import aaf.v2_0.Delg;
import aaf.v2_0.DelgRequest;
import aaf.v2_0.Delgs;
import aaf.v2_0.Error;
import aaf.v2_0.History;
import aaf.v2_0.History.Item;
import aaf.v2_0.Keys;
import aaf.v2_0.NsRequest;
import aaf.v2_0.Nss;
import aaf.v2_0.Nss.Ns;
import aaf.v2_0.Perm;
import aaf.v2_0.PermKey;
import aaf.v2_0.PermRequest;
import aaf.v2_0.Perms;
import aaf.v2_0.Pkey;
import aaf.v2_0.Request;
import aaf.v2_0.Role;
import aaf.v2_0.RolePermRequest;
import aaf.v2_0.RoleRequest;
import aaf.v2_0.Roles;
import aaf.v2_0.UserRole;
import aaf.v2_0.UserRoleRequest;
import aaf.v2_0.UserRoles;
import aaf.v2_0.Users;
import aaf.v2_0.Users.User;

public class Mapper_2_0 implements Mapper<Nss, Perms, Pkey, Roles, Users, UserRoles, Delgs, Certs, Keys, Request, History, Error, Approvals> {
    private Question q;

    public Mapper_2_0(Question q) {
        this.q = q;
    }
    
    /* (non-Javadoc)
     * @see org.onap.aaf.auth.service.mapper.Mapper#ns(java.lang.Object, org.onap.aaf.auth.service.mapper.Mapper.Holder)
     */
    @Override
    public Result<Namespace> ns(AuthzTrans trans, Request base) {
        NsRequest from = (NsRequest)base;
        Namespace namespace = new Namespace();
        namespace.name = from.getName();
        namespace.admin = from.getAdmin();
        namespace.owner = from.getResponsible();
        namespace.description = from.getDescription();
        trans.checkpoint(namespace.name, Env.ALWAYS);
        
        NsType nt = NsType.fromString(from.getType());
        if (nt.equals(NsType.UNKNOWN)) {
            String ns = namespace.name;
            int count = 0;
            for (int i=ns.indexOf('.');
                    i>=0;
                    i=ns.indexOf('.',i+1)) {
                ++count;
            }
            switch(count) {
                case 0: nt = NsType.ROOT;break;
                case 1: nt = NsType.COMPANY;break;
                default: nt = NsType.APP;
            }
        }
        namespace.type = nt.type;
        
        return Result.ok(namespace);
    }

    @Override
    public Result<Nss> nss(AuthzTrans trans, Namespace from, Nss to) {
        List<Ns> nss = to.getNs();
        Ns ns = new Ns();
        ns.setName(from.name);
        if (from.admin!=null)ns.getAdmin().addAll(from.admin);
        if (from.owner!=null)ns.getResponsible().addAll(from.owner);
        if (from.attrib!=null) {
            for (Pair<String,String> attrib : from.attrib) {
                Ns.Attrib toAttrib = new Ns.Attrib();
                toAttrib.setKey(attrib.x);
                toAttrib.setValue(attrib.y);
                ns.getAttrib().add(toAttrib);
            }
        }

        ns.setDescription(from.description);
        nss.add(ns);
        return Result.ok(to);
    }

    /**
     * Note: Prevalidate if NS given is allowed to be seen before calling
     */
    @Override
    public Result<Nss> nss(AuthzTrans trans, Collection<Namespace> from, Nss to) {
        List<Ns> nss = to.getNs();
        for (Namespace nd : from) {
            Ns ns = new Ns();
            ns.setName(nd.name);
            if (nd.admin!=null) {
                ns.getAdmin().addAll(nd.admin);
            }
            if (nd.owner!=null) {
                ns.getResponsible().addAll(nd.owner);
            }
            ns.setDescription(nd.description);
            if (nd.attrib!=null) {
                for (Pair<String,String> attrib : nd.attrib) {
                    Ns.Attrib toAttrib = new Ns.Attrib();
                    toAttrib.setKey(attrib.x);
                    toAttrib.setValue(attrib.y);
                    ns.getAttrib().add(toAttrib);
                }
            }

            nss.add(ns);
        }
        return Result.ok(to);
    }

    @Override
    public Result<Perms> perms(AuthzTrans trans, List<PermDAO.Data> from, Perms to, boolean filter) {
        List<Perm> perms = to.getPerm();
        final boolean addNS = trans.requested(REQD_TYPE.ns);
        TimeTaken tt = trans.start("Filter Perms before return", Env.SUB);
        try {
            if (from!=null) {
                for (PermDAO.Data data : from) {
                    if (!filter || q.mayUser(trans, trans.user(), data, Access.read).isOK()) {
                        Perm perm = new Perm();
                        perm.setType(data.fullType());
                        perm.setInstance(data.instance);
                        perm.setAction(data.action);
                        perm.setDescription(data.description);
                        if (addNS) {
                            perm.setNs(data.ns);
                        }
                        for (String role : data.roles(false)) {
                            perm.getRoles().add(role);
                        }
                        perms.add(perm);
                    }
                }
            }
        } finally {
            tt.done();
        }
         
        tt = trans.start("Sort Perms", Env.SUB);
        try {
            Collections.sort(perms, new Comparator<Perm>() {
                @Override
                public int compare(Perm perm1, Perm perm2) {
                    int typeCompare = perm1.getType().compareToIgnoreCase(perm2.getType());
                    if (typeCompare == 0) {
                        int instanceCompare = perm1.getInstance().compareToIgnoreCase(perm2.getInstance());
                        if (instanceCompare == 0) {
                            return perm1.getAction().compareToIgnoreCase(perm2.getAction());
                        }
                        return instanceCompare;
                    }
                    return typeCompare;
                }    
            });
        } finally {
            tt.done();
        }
        return Result.ok(to);
    }
    
    @Override
    public Result<Perms> perms(AuthzTrans trans, List<PermDAO.Data> from, Perms to, String[] nss, boolean filter) {
        List<Perm> perms = to.getPerm();
        TimeTaken tt = trans.start("Filter Perms before return", Env.SUB);
        try {
            if (from!=null) {
                boolean inNSS;
                for (PermDAO.Data data : from) {
                    inNSS=false;
                    for (int i=0;!inNSS && i<nss.length;++i) {
                        if (nss[i].equals(data.ns)) {
                            inNSS=true;
                        }
                    }
                    if (inNSS && (!filter || q.mayUser(trans, trans.user(), data, Access.read).isOK())) {
                        Perm perm = new Perm();
                        perm.setType(data.fullType());
                        perm.setInstance(data.instance);
                        perm.setAction(data.action);
                        for (String role : data.roles(false)) {
                            perm.getRoles().add(role);
                        }
                        perm.setDescription(data.description);
                        perms.add(perm);
                    }
                }
            }
        } finally {
            tt.done();
        }
         
        tt = trans.start("Sort Perms", Env.SUB);
        try {
            Collections.sort(perms, new Comparator<Perm>() {
                @Override
                public int compare(Perm perm1, Perm perm2) {
                    int typeCompare = perm1.getType().compareToIgnoreCase(perm2.getType());
                    if (typeCompare == 0) {
                        int instanceCompare = perm1.getInstance().compareToIgnoreCase(perm2.getInstance());
                        if (instanceCompare == 0) {
                            return perm1.getAction().compareToIgnoreCase(perm2.getAction());
                        }
                        return instanceCompare;
                    }
                    return typeCompare;
                }    
            });
        } finally {
            tt.done();
        }
        return Result.ok(to);
    }

    @Override
    public Result<List<PermDAO.Data>> perms(AuthzTrans trans, Perms perms) {
        List<PermDAO.Data> lpd = new ArrayList<>();
        for (Perm p : perms.getPerm()) {
            Result<NsSplit> nss = q.deriveNsSplit(trans, p.getType());
            PermDAO.Data pd = new PermDAO.Data();
            if (nss.isOK()) { 
                pd.ns=nss.value.ns;
                pd.type = nss.value.name;
                pd.instance = p.getInstance();
                pd.action = p.getAction();
                for (String role : p.getRoles()) {
                    pd.roles(true).add(role);
                }
                lpd.add(pd);
            } else {
                return Result.err(nss);
            }
        }
        return Result.ok(lpd);
    }

    
    @Override
    public Result<PermDAO.Data> permkey(AuthzTrans trans, Pkey from) {
        return q.permFrom(trans, from.getType(),from.getInstance(),from.getAction());
    }
    
    @Override
    public Result<PermDAO.Data> permFromRPRequest(AuthzTrans trans, Request req) {
        RolePermRequest from = (RolePermRequest)req;
        Pkey perm = from.getPerm();
        if (perm==null)return Result.err(Status.ERR_NotFound, "Permission not found");
        Result<NsSplit> nss = q.deriveNsSplit(trans, perm.getType());
        PermDAO.Data pd = new PermDAO.Data();
        if (nss.isOK()) { 
            pd.ns=nss.value.ns;
            pd.type = nss.value.name;
            pd.instance = from.getPerm().getInstance();
            pd.action = from.getPerm().getAction();
            trans.checkpoint(pd.fullPerm(), Env.ALWAYS);
            
            String[] roles = {};
            
            if (from.getRole() != null) {
                roles = from.getRole().split(",");
            }
            for (String role : roles) { 
                pd.roles(true).add(role);
            }
            return Result.ok(pd);
        } else {
            return Result.err(nss);
        }
    }
    
    @Override
    public Result<RoleDAO.Data> roleFromRPRequest(AuthzTrans trans, Request req) {
        RolePermRequest from = (RolePermRequest)req;
        Result<NsSplit> nss = q.deriveNsSplit(trans, from.getRole());
        RoleDAO.Data rd = new RoleDAO.Data();
        if (nss.isOK()) { 
            rd.ns = nss.value.ns;
            rd.name = nss.value.name;
            trans.checkpoint(rd.fullName(), Env.ALWAYS);
            return Result.ok(rd);
        } else {
            return Result.err(nss);
        }
    }
    
    @Override
    public Result<PermDAO.Data> perm(AuthzTrans trans, Request req) {
        PermRequest from = (PermRequest)req;
        Result<NsSplit> nss = q.deriveNsSplit(trans, from.getType());
        PermDAO.Data pd = new PermDAO.Data();
        if (nss.isOK()) { 
            pd.ns=nss.value.ns;
            pd.type = nss.value.name;
            pd.instance = from.getInstance();
            pd.action = from.getAction();
            pd.description = from.getDescription();
            trans.checkpoint(pd.fullPerm(), Env.ALWAYS);
            return Result.ok(pd);
        } else {
            return Result.err(nss);
        }
    }
    
    @Override
    public Request ungrantRequest(AuthzTrans trans, String role, String type, String instance, String action) {
        RolePermRequest rpr = new RolePermRequest();
        Pkey pkey = new Pkey();
        pkey.setType(type);
        pkey.setInstance(instance);
        pkey.setAction(action);
        rpr.setPerm(pkey);
        
        rpr.setRole(role);
        return rpr;
    }

    @Override
    public Result<RoleDAO.Data> role(AuthzTrans trans, Request base) {
        RoleRequest from = (RoleRequest)base;
        Result<NsSplit> nss = q.deriveNsSplit(trans, from.getName());
        if (nss.isOK()) {
            RoleDAO.Data to = new RoleDAO.Data();
            to.ns = nss.value.ns;
            to.name = nss.value.name;
            to.description = from.getDescription();
            trans.checkpoint(to.fullName(), Env.ALWAYS);

            return Result.ok(to);
        } else {
            return Result.err(nss);
        }
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.service.mapper.Mapper#roles(java.util.List)
     */
    @Override
    public Result<Roles> roles(AuthzTrans trans, List<RoleDAO.Data> from, Roles to, boolean filter) {
        final boolean needNS = trans.requested(REQD_TYPE.ns); 
        for (RoleDAO.Data frole : from) {
            // Only Add Data to view if User is allowed to see this Role
            if (!filter || q.mayUser(trans, trans.user(), frole,Access.read).isOK()) {
                Role role = new Role();
                role.setName(frole.ns + '.' + frole.name);
                role.setDescription(frole.description);
                if (needNS) {
                    role.setNs(frole.ns);
                }
                for (String p : frole.perms(false)) { // can see any Perms in the Role he has permission for
                    Result<String[]> rpa = PermDAO.Data.decodeToArray(trans,q,p);
                    if (rpa.notOK())
                        return Result.err(rpa);
                    
                    String[] pa = rpa.value;
                    Pkey pKey = new Pkey();
                    pKey.setType(pa[0]+'.'+pa[1]);
                    pKey.setInstance(pa[2]);
                    pKey.setAction(pa[3]);
                    role.getPerms().add(pKey);
                }
                to.getRole().add(role);
            }
        }
        return Result.ok(to);
    }

    /*
     * (non-Javadoc)
     * @see org.onap.aaf.auth.service.mapper.Mapper#users(java.util.Collection, java.lang.Object)
     * 
     * Note: Prevalidate all data for permission to view
     */
    @Override
    public Result<Users> users(AuthzTrans trans, Collection<UserRoleDAO.Data> from, Users to) {
        List<User> cu = to.getUser();
        for (UserRoleDAO.Data urd : from) {
            User user = new User();
            user.setId(urd.user);
            if (urd.expires!=null) {
                user.setExpires(Chrono.timeStamp(urd.expires));
            }
            cu.add(user);
        }
        return Result.ok(to);
    }

    /*
     * (non-Javadoc)
     * @see org.onap.aaf.auth.service.mapper.Mapper#users(java.util.Collection, java.lang.Object)
     * 
     * Note: Prevalidate all data for permission to view
     */
    @Override
    public Result<UserRoles> userRoles(AuthzTrans trans, Collection<UserRoleDAO.Data> from, UserRoles to) {
        List<UserRole> cu = to.getUserRole();
        for (UserRoleDAO.Data urd : from) {
            UserRole ur = new UserRole();
            ur.setUser(urd.user);
            ur.setRole(urd.role);
            ur.setExpires(Chrono.timeStamp(urd.expires));
            cu.add(ur);
        }
        return Result.ok(to);
    }

    @Override
    public Result<UserRoleDAO.Data> userRole(AuthzTrans trans, Request base) {
        try {
            UserRoleRequest from = (UserRoleRequest)base;

            // Setup UserRoleData, either for immediate placement, or for futureIt i
            UserRoleDAO.Data to = new UserRoleDAO.Data();
            if (from.getUser() != null) {
                to.user = from.getUser();
            }
            if (from.getRole() != null) {
                to.role(trans,q,from.getRole());
            }
            to.expires = getExpires(trans.org(),Expiration.UserInRole,base,from.getUser());
            trans.checkpoint(to.toString(), Env.ALWAYS);

            return Result.ok(to);
        } catch (Exception t) {
            return Result.err(Status.ERR_BadData,t.getMessage());
        }
    }

    @Override
    public Result<CredDAO.Data> cred(AuthzTrans trans, Request base, boolean requiresPass) {
        CredRequest from = (CredRequest)base;
        CredDAO.Data to = new CredDAO.Data();
        to.id=from.getId();
        to.ns = Question.domain2ns(to.id);
        to.type = from.getType();
        if(to.type!=null && to.type==CredDAO.FQI) {
        	to.cred = null;
        } else {
        	String passwd = from.getPassword();
	        if (requiresPass) {
	            String ok = trans.org().isValidPassword(trans, to.id,passwd);
	            if (ok.length()>0) {
	                return Result.err(Status.ERR_BadData,ok);
	            }
	        } else {
	            to.type=0;
	        }
	        if (passwd != null) {
	            to.cred = ByteBuffer.wrap(passwd.getBytes());
	            to.type = CredDAO.RAW; 
	        } else {
	            to.type = CredDAO.FQI;
	        }
        }
	        
        // Note: Ensure requested EndDate created will match Organization Password Rules
        //  P.S. Do not apply TempPassword rule here. Do that when you know you are doing a Create/Reset (see Service)
        to.expires = getExpires(trans.org(),Expiration.Password,base,from.getId());
        trans.checkpoint(to.id, Env.ALWAYS);

        return Result.ok(to);
    }
    
    @Override
    public Result<Users> cred(List<CredDAO.Data> from, Users to) {
        List<User> cu = to.getUser();
        for (CredDAO.Data cred : from) {
            User user = new User();
            user.setId(cred.id);
            user.setExpires(Chrono.timeStamp(cred.expires));
            user.setType(cred.type);
            user.setTag(cred.tag);
            cu.add(user);
        }
        return Result.ok(to);
    }
    
    @Override
    public Result<Certs> cert(List<CertDAO.Data> from, Certs to) {
        List<Cert> lc = to.getCert();
        for (CertDAO.Data fcred : from) {
            Cert cert = new Cert();
            cert.setId(fcred.id);
            cert.setX500(fcred.x500);
            /**TODO - change Interface 
             * @deprecated */
            cert.setFingerprint(fcred.serial.toByteArray());
            lc.add(cert);
        }
        return Result.ok(to);
    }

    /**
     * Analyze whether Requests should be acted on now, or in the future, based on Start Date, and whether the requester
     * is allowed to change this value directly
     * 
     * Returning Result.OK means it should be done in the future.
     * Returning Result.ACC_Now means to act on table change now.
     */
    @Override
    public Result<FutureDAO.Data> future(AuthzTrans trans, String table, Request from, 
                Bytification content, boolean enableApproval,  Memo memo, MayChange mc) {
        Result<?> rMayChange;
        boolean needsAppr = enableApproval?trans.requested(REQD_TYPE.future):false; 
        if (!needsAppr && (needsAppr = (rMayChange=mc.mayChange()).notOK())) {
            if (enableApproval) {
                if (!trans.requested(AuthzTrans.REQD_TYPE.future)) {
                    return Result.err(rMayChange);
                }
            } else {
                return Result.err(rMayChange);
            }
        }
        GregorianCalendar now = new GregorianCalendar(); 
        GregorianCalendar start = from.getStart()==null?now:from.getStart().toGregorianCalendar();
        
        GregorianCalendar expires = trans.org().expiration(start, Expiration.Future);
        XMLGregorianCalendar xgc;
        if ((xgc=from.getEnd())!=null) {
            GregorianCalendar fgc = xgc.toGregorianCalendar();
            expires = expires.before(fgc)?expires:fgc; // Min of desired expiration, and Org expiration
        }
        
        //TODO needs two answers from this.  What's the NSS, and may Change.
        FutureDAO.Data fto;
        if (start.after(now) || needsAppr ) {
            //String user = trans.user();
            fto = new FutureDAO.Data();
            fto.target=table;
            fto.memo = memo.get();
            fto.start = start.getTime();
            fto.expires = expires.getTime();
            if (needsAppr) { // Need to add Approvers...
                /*
                Result<Data> rslt = mc.getNsd();
                if (rslt.notOKorIsEmpty())return Result.err(rslt);
                appr.addAll(mc.getNsd().value.responsible);
                try {
                    //Note from 2013 Is this getting Approvers for user only?  What about Delegates?
                    // 3/25/2014.  Approvers are set by Corporate policy.  We don't have to worry here about what that means.
                    // It is important to get Delegates, if necessary, at notification time
                    // If we add delegates now, it will get all confused as to who is actually responsible.
                    for (Organization.User ou : org.getApprovers(trans, user)) {
                        appr.add(ou.email);
                    }
                } catch (Exception e) {
                    return Result.err(Status.ERR_Policy,org.getName() + " did not respond with Approvers: " + e.getLocalizedMessage());
                }
                */
            }
            try {
                fto.construct = content.bytify();
            } catch (Exception e) {
                return Result.err(Status.ERR_BadData,"Data cannot be saved for Future.");
            }
        } else {
            return Result.err(Status.ACC_Now, "Make Data changes now.");
        }
        return Result.ok(fto);
    }


    /* (non-Javadoc)
     * @see org.onap.aaf.auth.service.mapper.Mapper#history(java.util.List)
     */
    @Override
    public Result<History> history(AuthzTrans trans, List<HistoryDAO.Data> history, final int sort) {
        History hist = new History();
        List<Item> items = hist.getItem();
        for (HistoryDAO.Data data : history) {
            History.Item item = new History.Item();
            item.setYYYYMM(Integer.toString(data.yr_mon));
            Date date = Chrono.uuidToDate(data.id);
            item.setTimestamp(Chrono.timeStamp(date));
            item.setAction(data.action);
            item.setMemo(data.memo);
            item.setSubject(data.subject);
            item.setTarget(data.target);
            item.setUser(data.user);
            items.add(item);
        }
        
        if (sort != 0) {
            TimeTaken tt = trans.start("Sort ", Env.SUB);
            try {
                java.util.Collections.sort(items, new Comparator<Item>() {
                    @Override
                    public int compare(Item o1, Item o2) {
                        return sort*(o1.getTimestamp().compare(o2.getTimestamp()));
                    }
                });
            } finally {
                tt.done();
            }
        }
        return Result.ok(hist);
    }

    @Override
    public Error errorFromMessage(StringBuilder holder, String msgID, String text, String... var) {
        Error err = new Error();
        err.setMessageId(msgID);
        // AT&T Restful Error Format requires numbers "%" placements
        err.setText(Vars.convert(holder, text, (Object[])var));
        for (String s : var) {
            err.getVariables().add(s);
        }
        return err;
    }
    
    @Override
    public Class<?> getClass(API api) {
        switch(api) {
            case NSS:  return Nss.class;
            case NS_REQ: return NsRequest.class;
            case PERMS: return Perms.class;
            case PERM_KEY: return PermKey.class;
            case ROLES: return Roles.class;
            case ROLE: return Role.class;
            case USERS: return Users.class;
            case DELGS: return Delgs.class;
            case CERTS: return Certs.class;
            case DELG_REQ: return DelgRequest.class;
            case PERM_REQ: return PermRequest.class;
            case ROLE_REQ:  return RoleRequest.class;
            case CRED_REQ:  return CredRequest.class;
            case USER_ROLE_REQ:  return UserRoleRequest.class;
            case USER_ROLES: return UserRoles.class;
            case ROLE_PERM_REQ:  return RolePermRequest.class;
            case APPROVALS: return Approvals.class;
            case KEYS: return Keys.class;
            case HISTORY: return History.class;
//            case MODEL: return Model.class;
            case ERROR: return Error.class;
            case API: return Api.class;
            case VOID: return Void.class;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A> A newInstance(API api) {
        switch(api) {
            case NS_REQ: return (A) new NsRequest();
            case NSS: return (A) new Nss();
            case PERMS: return (A)new Perms();
            case PERM_KEY: return (A)new PermKey();
            case ROLES: return (A)new Roles();
            case ROLE: return (A)new Role();
            case USERS: return (A)new Users();
            case DELGS: return (A)new Delgs();
            case CERTS: return (A)new Certs();
            case PERM_REQ: return (A)new PermRequest();
            case CRED_REQ: return (A)new CredRequest();
            case ROLE_REQ:  return (A)new RoleRequest();
            case USER_ROLE_REQ:  return (A)new UserRoleRequest();
            case USER_ROLES:  return (A)new UserRoles();
            case ROLE_PERM_REQ:  return (A)new RolePermRequest();
            case HISTORY: return (A)new History();
            case KEYS: return (A)new Keys();
            //case MODEL: return (A)new Model();
            case ERROR: return (A)new Error();
            case API: return (A)new Api();
            case VOID: return null;
            
            case APPROVALS:    return (A) new Approvals();
            case DELG_REQ: return (A) new DelgRequest();
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    /**
     * Get Typed Marshaler as they are defined
     * 
     * @param api
     * @return
     */
    public <A> Marshal<A> getMarshal(API api) {
        switch(api) {
            case CERTS: return (Marshal<A>) new CertsMarshal();
            default:
                return null;
        }
    }

    @Override
    public Result<Approvals> approvals(List<ApprovalDAO.Data> lAppr) {
        Approvals apprs = new Approvals();
        List<Approval> lappr = apprs.getApprovals();
        Approval a;
        for (ApprovalDAO.Data appr : lAppr) {
            a = new Approval();
            a.setId(appr.id.toString());
            if (appr.ticket==null) {
                a.setTicket(null);
            } else {
                a.setTicket(appr.ticket.toString());
            }
            a.setUser(appr.user);
            a.setApprover(appr.approver);
            a.setType(appr.type);
            a.setStatus(appr.status);
            a.setMemo(appr.memo);
            a.setOperation(appr.operation);
            a.setUpdated(Chrono.timeStamp(appr.updated));
            lappr.add(a);
        }
        return Result.ok(apprs);
    }
    
    @Override
    public Result<List<ApprovalDAO.Data>> approvals(Approvals apprs) {
        List<ApprovalDAO.Data>  lappr = new ArrayList<>();
        for (Approval a : apprs.getApprovals()) {
            ApprovalDAO.Data ad = new ApprovalDAO.Data();
            String str = a.getId();
            if (str!=null)ad.id=UUID.fromString(str);
            str = a.getTicket();
            if (str!=null)ad.ticket=UUID.fromString(str);
            ad.user=a.getUser();
            ad.approver=a.getApprover();
            ad.type=a.getType();
            ad.status=a.getStatus();
            ad.operation=a.getOperation();
            ad.memo=a.getMemo();
            
            XMLGregorianCalendar xgc = a.getUpdated();
            if (xgc!=null)ad.updated=xgc.toGregorianCalendar().getTime();
            lappr.add(ad);
        }
        return Result.ok(lappr);
    }

    @Override
    public Result<Delgs> delegate(List<DelegateDAO.Data> lDelg) {
        Delgs delgs = new Delgs();
        List<Delg> ldelg = delgs.getDelgs();
        Delg d;
        for (DelegateDAO.Data del: lDelg) {
            d = new Delg();
            d.setUser(del.user);
            d.setDelegate(del.delegate);
            if (del.expires!=null)d.setExpires(Chrono.timeStamp(del.expires));
            ldelg.add(d);
        }
        return Result.ok(delgs);
    }

    @Override
    public Result<Data> delegate(AuthzTrans trans, Request base) {
        try {
            DelgRequest from = (DelgRequest)base;
            DelegateDAO.Data to = new DelegateDAO.Data();
            String user = from.getUser();
            to.user = user;
            String delegate = from.getDelegate();
            to.delegate = delegate;
            to.expires = getExpires(trans.org(),Expiration.UserDelegate,base,from.getUser());
            trans.checkpoint(to.user+"=>"+to.delegate, Env.ALWAYS);

            return Result.ok(to);
        } catch (Exception t) {
            return Result.err(Status.ERR_BadData,t.getMessage());
        }
    }

    /*
     * We want "Expired" dates to start at a specified time set by the Organization, and consistent wherever
     * the date is created from.
     */ 
    private Date getExpires(Organization org, Expiration exp, Request base, String id) {
        XMLGregorianCalendar end = base.getEnd();
        GregorianCalendar gc = end==null?new GregorianCalendar():end.toGregorianCalendar();
        GregorianCalendar orggc;
        orggc = org.expiration(gc,exp,id); 

        // We'll choose the lesser of dates to ensure Policy Compliance...
    
        GregorianCalendar endgc = end==null||gc.after(orggc)?orggc:gc;
        // Allow the Organization to determine when official "day Start" begins, Specifically when to consider something Expired.
        endgc = Chrono.firstMomentOfDay(endgc);
        endgc.set(GregorianCalendar.HOUR_OF_DAY, org.startOfDay());
        return endgc.getTime();
    }


    @Override
    public Result<Keys> keys(Collection<String> from) {
        Keys keys = new Keys();
        keys.getKey().addAll(from);
        return Result.ok(keys).emptyList(from.isEmpty());
    }

}