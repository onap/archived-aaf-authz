/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aai
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * Copyright © 2017 Amdocs
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package com.att.dao.aaf.hl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.att.authz.common.Define;
import com.att.authz.env.AuthzTrans;
import com.att.authz.env.AuthzTransFilter;
import com.att.authz.layer.Result;
import com.att.authz.org.Organization;
import com.att.authz.org.Organization.Identity;
import com.att.cadi.Hash;
import com.att.cadi.aaf.PermEval;
import com.att.dao.AbsCassDAO;
import com.att.dao.CachedDAO;
import com.att.dao.DAOException;
import com.att.dao.aaf.cached.CachedCertDAO;
import com.att.dao.aaf.cached.CachedCredDAO;
import com.att.dao.aaf.cached.CachedNSDAO;
import com.att.dao.aaf.cached.CachedPermDAO;
import com.att.dao.aaf.cached.CachedRoleDAO;
import com.att.dao.aaf.cached.CachedUserRoleDAO;
import com.att.dao.aaf.cass.ApprovalDAO;
import com.att.dao.aaf.cass.CacheInfoDAO;
import com.att.dao.aaf.cass.CertDAO;
import com.att.dao.aaf.cass.CredDAO;
import com.att.dao.aaf.cass.DelegateDAO;
import com.att.dao.aaf.cass.FutureDAO;
import com.att.dao.aaf.cass.HistoryDAO;
import com.att.dao.aaf.cass.NsDAO;
import com.att.dao.aaf.cass.NsDAO.Data;
import com.att.dao.aaf.cass.NsSplit;
import com.att.dao.aaf.cass.NsType;
import com.att.dao.aaf.cass.PermDAO;
import com.att.dao.aaf.cass.RoleDAO;
import com.att.dao.aaf.cass.Status;
import com.att.dao.aaf.cass.UserRoleDAO;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.Slot;
import com.att.inno.env.TimeTaken;
import com.att.inno.env.util.Chrono;
import com.datastax.driver.core.Cluster;

/**
 * Question HL DAO
 * 
 * A Data Access Combination Object which asks Security and other Questions
 * 
 *
 */
public class Question {
	// DON'T CHANGE FROM lower Case!!!
	public static enum Type {
		ns, role, perm, cred
	};

	public static final String OWNER="owner";
	public static final String ADMIN="admin";
	public static final String DOT_OWNER=".owner";
	public static final String DOT_ADMIN=".admin";
	static final String ASTERIX = "*";

	public static enum Access {
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
	public static final String ATTRIB = "attrib";


	public static final int MAX_SCOPE = 10;
	public static final int APP_SCOPE = 3;
	public static final int COMPANY_SCOPE = 2;
	static Slot PERMS;

	private static Set<String> specialLog = null;
	public static final SecureRandom random = new SecureRandom();
	private static long traceID = random.nextLong();
	private static final String SPECIAL_LOG_SLOT = "SPECIAL_LOG_SLOT";
	private static Slot specialLogSlot = null;
	private static Slot transIDSlot = null;


	public final HistoryDAO historyDAO;
	public final CachedNSDAO nsDAO;
	public final CachedRoleDAO roleDAO;
	public final CachedPermDAO permDAO;
	public final CachedUserRoleDAO userRoleDAO;
	public final CachedCredDAO credDAO;
	public final CachedCertDAO certDAO;
	public final DelegateDAO delegateDAO;
	public final FutureDAO futureDAO;
	public final ApprovalDAO approvalDAO;
	private final CacheInfoDAO cacheInfoDAO;

	// final ContactDAO contDAO;
	// private static final String DOMAIN = "@aaf.att.com";
	// private static final int DOMAIN_LENGTH = 0;

	public Question(AuthzTrans trans, Cluster cluster, String keyspace, boolean startClean) throws APIException, IOException {
		PERMS = trans.slot("USER_PERMS");
		trans.init().log("Instantiating DAOs");
		historyDAO = new HistoryDAO(trans, cluster, keyspace);

		// Deal with Cached Entries
		cacheInfoDAO = new CacheInfoDAO(trans, historyDAO);

		nsDAO = new CachedNSDAO(new NsDAO(trans, historyDAO, cacheInfoDAO),
				cacheInfoDAO);
		permDAO = new CachedPermDAO(
				new PermDAO(trans, historyDAO, cacheInfoDAO), cacheInfoDAO);
		roleDAO = new CachedRoleDAO(
				new RoleDAO(trans, historyDAO, cacheInfoDAO), cacheInfoDAO);
		userRoleDAO = new CachedUserRoleDAO(new UserRoleDAO(trans, historyDAO,
				cacheInfoDAO), cacheInfoDAO);
		credDAO = new CachedCredDAO(
				new CredDAO(trans, historyDAO, cacheInfoDAO), cacheInfoDAO);
		certDAO = new CachedCertDAO(
				new CertDAO(trans, historyDAO, cacheInfoDAO), cacheInfoDAO);

		futureDAO = new FutureDAO(trans, historyDAO);
		delegateDAO = new DelegateDAO(trans, historyDAO);
		approvalDAO = new ApprovalDAO(trans, historyDAO);

		// Only want to aggressively cleanse User related Caches... The others,
		// just normal refresh
		if(startClean) {
			CachedDAO.startCleansing(trans.env(), credDAO, userRoleDAO);
			CachedDAO.startRefresh(trans.env(), cacheInfoDAO);
		}
		// Set a Timer to Check Caches to send messages for Caching changes
		
		if(specialLogSlot==null) {
			specialLogSlot = trans.slot(SPECIAL_LOG_SLOT);
			transIDSlot = trans.slot(AuthzTransFilter.TRANS_ID_SLOT);
		}
		
		AbsCassDAO.primePSIs(trans);
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

	public Result<PermDAO.Data> permFrom(AuthzTrans trans, String type,
			String instance, String action) {
		Result<NsDAO.Data> rnd = deriveNs(trans, type);
		if (rnd.isOK()) {
			return Result.ok(new PermDAO.Data(new NsSplit(rnd.value, type),
					instance, action));
		} else {
			return Result.err(rnd);
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
		if(plPermNames.notOK()) {
			return Result.err(plPermNames);
		}
		
		Set<String> nss;
		if(forUser.equals(user)) {
			nss = null;
		} else {
			// Setup a TreeSet to check on Namespaces to 
			nss = new TreeSet<String>();
			PermLookup fUser = PermLookup.get(trans, this, forUser);
			Result<Set<String>> forUpn = fUser.getPermNames();
			if(forUpn.notOK()) {
				return Result.err(forUpn);
			}
			
			for(String pn : forUpn.value) {
				Result<String[]> decoded = PermDAO.Data.decodeToArray(trans, this, pn);
				if(decoded.isOKhasData()) {
					nss.add(decoded.value[0]);
				} else {
					trans.error().log(pn,", derived from a Role, is invalid:",decoded.errorString());
				}
			}
		}

		List<PermDAO.Data> rlpUser = new ArrayList<PermDAO.Data>();
		Result<PermDAO.Data> rpdd;
		PermDAO.Data pdd;
		for(String pn : plPermNames.value) {
			rpdd = PermDAO.Data.decode(trans, this, pn);
			if(rpdd.isOKhasData()) {
				pdd=rpdd.value;
				if(nss==null || nss.contains(pdd.ns)) {
					rlpUser.add(pdd);
				}
			} else {
				trans.error().log(pn,", derived from a Role, is invalid.  Run Data Cleanup:",rpdd.errorString());
			}
		}
		return Result.ok(rlpUser); 
	}

	public Result<List<PermDAO.Data>> getPermsByType(AuthzTrans trans, String perm) {
		Result<NsSplit> nss = deriveNsSplit(trans, perm);
		if (nss.notOK()) {
			return Result.err(nss);
		}
		return permDAO.readByType(trans, nss.value.ns, nss.value.name);
	}

	public Result<List<PermDAO.Data>> getPermsByName(AuthzTrans trans,
			String type, String instance, String action) {
		Result<NsSplit> nss = deriveNsSplit(trans, type);
		if (nss.notOK()) {
			return Result.err(nss);
		}
		return permDAO.read(trans, nss.value.ns, nss.value.name, instance,action);
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
		Set<String> permNames = new HashSet<String>();
		if (rlrd.isOKhasData()) {
			for (RoleDAO.Data drr : rlrd.value) {
				permNames.addAll(drr.perms(false));
			}
		}

		// Note: It should be ok for a Valid user to have no permissions -
		// 8/12/2013
		List<PermDAO.Data> perms = new ArrayList<PermDAO.Data>();
		for (String perm : permNames) {
			Result<PermDAO.Data> pr = PermDAO.Data.decode(trans, this, perm);
			if (pr.notOK()) {
				return Result.err(pr);
			}

			if(lookup) {
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

	public Result<List<RoleDAO.Data>> getRolesByName(AuthzTrans trans,
			String role) {
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
	 * For instance, if in the NS table, the parent "com.att" exists, but not
	 * "com.att.child" or "com.att.a.b.c", then passing in either
	 * "com.att.child" or "com.att.a.b.c" will return "com.att"
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
			int dot = child == null ? -1 : child.lastIndexOf('.');
			if (dot < 0) {
				return Result.err(Status.ERR_NsNotFound,
						"No Namespace for [%s]", child);
			} else {
				return deriveNs(trans, child.substring(0, dot));
			}
		}
	}

	public Result<NsDAO.Data> deriveFirstNsForType(AuthzTrans trans, String str, NsType type) {
		NsDAO.Data nsd;

		System.out.println("value of str before for loop ---------0---++++++++++++++++++" +str);
		for(int idx = str.indexOf('.');idx>=0;idx=str.indexOf('.',idx+1)) {
		//	System.out.println("printing value of str-----------------1------------++++++++++++++++++++++" +str);
			Result<List<Data>> rld = nsDAO.read(trans, str.substring(0,idx));
			System.out.println("value of idx is -----------------++++++++++++++++++++++++++" +idx);
			System.out.println("printing value of str.substring-----------------1------------++++++++++++++++++++++" + (str.substring(0,idx)));
			System.out.println("value of ResultListData ------------------2------------+++++++++++++++++++++++++++" +rld);
			if(rld.isOKhasData()) {
				System.out.println("In if loop -----------------3-------------- ++++++++++++++++");
				System.out.println("value of nsd=rld.value.get(0).type -----------4------++++++++++++++++++++++++++++++++++++" +(nsd=rld.value.get(0)).type);
				System.out.println("value of rld.value.get(0).name.toString()+++++++++++++++++++++++++++++++ " +rld.value.get(0).name);
				if(type.type == (nsd=rld.value.get(0)).type) {
					return Result.ok(nsd);
				}
			} else {
				System.out.println("In else loop ----------------4------------+++++++++++++++++++++++");
				return Result.err(Status.ERR_NsNotFound,"There is no valid Company Namespace for %s",str.substring(0,idx));
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
	 * i.e. myid1234@myapp.att.com results in domain of com.att.myapp
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
	 * i.e. myid1234@myapp.att.com results in domain of com.att.myapp
	 * 
	 * @param trans
	 * @param id
	 * @return
	 */
	public Result<NsDAO.Data> validNSOfDomain(AuthzTrans trans, String id) {
		// Take domain, reverse order, and check on NS
		String ns;
		if(id.indexOf('@')<0) { // it's already an ns, not an ID
			ns = id;
		} else {
			ns = domain2ns(id);
		}
		if (ns.length() > 0) {
			if(!trans.org().getDomain().equals(ns)) { 
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
		int last;
		do {
			if (isGranted(trans, user, ns, "access", ":ns", access.name())) {
				return Result.ok(ndd);
			}
			if ((last = ns.lastIndexOf('.')) >= 0) {
				ns = ns.substring(0, last);
			}
		} while (last >= 0);
		// <root ns>.ns|:<client ns>:ns|<access>
		// AAF-724 - Make consistent response for May User", and not take the
		// last check... too confusing.
		Result<NsDAO.Data> rv = mayUserVirtueOfNS(trans, user, ndd, ":"	+ ndd.name + ":ns", access.name());
		if (rv.isOK()) {
			return rv;
		} else if(rv.status==Result.ERR_Backend) {
			return Result.err(rv);
		} else {
			return Result.err(Status.ERR_Denied, "[%s] may not %s in NS [%s]",
					user, access.name(), ndd.name);
		}
	}

	public Result<NsDAO.Data> mayUser(AuthzTrans trans, String user, RoleDAO.Data rdd, Access access) {
		Result<NsDAO.Data> rnsd = deriveNs(trans, rdd.ns);
		if (rnsd.isOK()) {
			return mayUser(trans, user, rnsd.value, rdd, access);
		}
		return rnsd;
	}

	public Result<NsDAO.Data> mayUser(AuthzTrans trans, String user, NsDAO.Data ndd, RoleDAO.Data rdd, Access access) {
		// 1) Is User in the Role?
		Result<List<UserRoleDAO.Data>> rurd = userRoleDAO.readUserInRole(trans, user, rdd.fullName());
		if (rurd.isOKhasData()) {
			return Result.ok(ndd);
		}

		String roleInst = ":role:" + rdd.name;
		// <ns>.access|:role:<role name>|<read|write>
		String ns = rdd.ns;
		int last;
		do {
			if (isGranted(trans, user, ns,"access", roleInst, access.name())) {
				return Result.ok(ndd);
			}
			if ((last = ns.lastIndexOf('.')) >= 0) {
				ns = ns.substring(0, last);
			}
		} while (last >= 0);

		// Check if Access by Global Role perm
		// <root ns>.ns|:<client ns>:role:name|<access>
		Result<NsDAO.Data> rnsd = mayUserVirtueOfNS(trans, user, ndd, ":"
				+ rdd.ns + roleInst, access.name());
		if (rnsd.isOK()) {
			return rnsd;
		} else if(rnsd.status==Result.ERR_Backend) {
			return Result.err(rnsd);
		}

		// Check if Access to Whole NS
		// AAF-724 - Make consistent response for May User", and not take the
		// last check... too confusing.
		Result<com.att.dao.aaf.cass.NsDAO.Data> rv = mayUserVirtueOfNS(trans, user, ndd, 
				":" + rdd.ns + ":ns", access.name());
		if (rv.isOK()) {
			return rv;
		} else if(rnsd.status==Result.ERR_Backend) {
			return Result.err(rnsd);
		} else {
			return Result.err(Status.ERR_Denied, "[%s] may not %s Role [%s]",
					user, access.name(), rdd.fullName());
		}

	}

	public Result<NsDAO.Data> mayUser(AuthzTrans trans, String user,PermDAO.Data pdd, Access access) {
		Result<NsDAO.Data> rnsd = deriveNs(trans, pdd.ns);
		if (rnsd.isOK()) {
			return mayUser(trans, user, rnsd.value, pdd, access);
		}
		return rnsd;
	}

	public Result<NsDAO.Data> mayUser(AuthzTrans trans, String user,NsDAO.Data ndd, PermDAO.Data pdd, Access access) {
		if (isGranted(trans, user, pdd.ns, pdd.type, pdd.instance, pdd.action)) {
			return Result.ok(ndd);
		}
		String permInst = ":perm:" + pdd.type + ':' + pdd.instance + ':' + pdd.action;
		// <ns>.access|:role:<role name>|<read|write>
		String ns = ndd.name;
		int last;
		do {
			if (isGranted(trans, user, ns, "access", permInst, access.name())) {
				return Result.ok(ndd);
			}
			if ((last = ns.lastIndexOf('.')) >= 0) {
				ns = ns.substring(0, last);
			}
		} while (last >= 0);

		// Check if Access by NS perm
		// <root ns>.ns|:<client ns>:role:name|<access>
		Result<NsDAO.Data> rnsd = mayUserVirtueOfNS(trans, user, ndd, ":" + pdd.ns + permInst, access.name());
		if (rnsd.isOK()) {
			return rnsd;
		} else if(rnsd.status==Result.ERR_Backend) {
			return Result.err(rnsd);
		}

		// Check if Access to Whole NS
		// AAF-724 - Make consistent response for May User", and not take the
		// last check... too confusing.
		Result<NsDAO.Data> rv = mayUserVirtueOfNS(trans, user, ndd, ":"	+ pdd.ns + ":ns", access.name());
		if (rv.isOK()) {
			return rv;
		} else {
			return Result.err(Status.ERR_Denied,
					"[%s] may not %s Perm [%s|%s|%s]", user, access.name(),
					pdd.fullType(), pdd.instance, pdd.action);
		}

	}

	public Result<Void> mayUser(AuthzTrans trans, DelegateDAO.Data dd, Access access) {
		try {
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
				if (!trans.forceRequested() && dd.user != null && dd.user.equals(dd.delegate)) {
					return Result.err(Status.ERR_BadData,
							"[%s] cannot be a delegate for self", dd.user);
				}
				if (!isUser	&& !isGranted(trans, trans.user(), Define.ROOT_NS,DELG,
								org.getDomain(), Question.CREATE)) {
					return Result.err(Status.ERR_Denied,
							"[%s] may not create a delegate for [%s]",
							trans.user(), dd.user);
				}
				break;
			case read:
			case write:
				if (!isUser	&& !isDelegate && 
						!isGranted(trans, trans.user(), Define.ROOT_NS,DELG,org.getDomain(), access.name())) {
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
	private Result<NsDAO.Data> mayUserVirtueOfNS(AuthzTrans trans, String user,	NsDAO.Data nsd, String ns_and_type, String access) {
		String ns = nsd.name;

		// If an ADMIN of the Namespace, then allow
		
		Result<List<UserRoleDAO.Data>> rurd;
		if ((rurd = userRoleDAO.readUserInRole(trans, user, nsd.name+ADMIN)).isOKhasData()) {
			return Result.ok(nsd);
		} else if(rurd.status==Result.ERR_Backend) {
			return Result.err(rurd);
		}
		
		// If Specially granted Global Permission
		if (isGranted(trans, user, Define.ROOT_NS,NS, ns_and_type, access)) {
			return Result.ok(nsd);
		}

		// Check recur

		int dot = ns.length();
		if ((dot = ns.lastIndexOf('.', dot - 1)) >= 0) {
			Result<NsDAO.Data> rnsd = deriveNs(trans, ns.substring(0, dot));
			if (rnsd.isOK()) {
				rnsd = mayUserVirtueOfNS(trans, user, rnsd.value, ns_and_type,access);
			} else if(rnsd.status==Result.ERR_Backend) {
				return Result.err(rnsd);
			}
			if (rnsd.isOK()) {
				return Result.ok(nsd);
			} else if(rnsd.status==Result.ERR_Backend) {
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
							if(PermEval.evalAction(pd.action, action)) { // don't return action here, might miss other action 
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
			result = credDAO.readID(trans, user);
		} finally {
			tt.done();
		}

		Result<Date> rv = null;
		if(result.isOK()) {
			if (result.isEmpty()) {
				rv = Result.err(Status.ERR_UserNotFound, user);
				if (willSpecialLog(trans,user)) {
					trans.audit().log("Special DEBUG:", user, " does not exist in DB");
				}
			} else {
				Date now = new Date();//long now = System.currentTimeMillis();
				ByteBuffer md5=null;
	
				// Bug noticed 6/22. Sorting on the result can cause Concurrency Issues.	 
				List<CredDAO.Data> cddl;
				if(result.value.size() > 1) {
					cddl = new ArrayList<CredDAO.Data>(result.value.size());
					for(CredDAO.Data old : result.value) {
						if(old.type==CredDAO.BASIC_AUTH || old.type==CredDAO.BASIC_AUTH_SHA256) {
							cddl.add(old);
						}
					}
					if(cddl.size()>1) {
						Collections.sort(cddl,new Comparator<CredDAO.Data>() {
							@Override
							public int compare(com.att.dao.aaf.cass.CredDAO.Data a,
											   com.att.dao.aaf.cass.CredDAO.Data b) {
								return b.expires.compareTo(a.expires);
							}
						});
					}
				} else {
					cddl = result.value;
				}
	
				for (CredDAO.Data cdd : cddl) {
					if (cdd.expires.after(now)) {
						try {
							switch(cdd.type) {
								case CredDAO.BASIC_AUTH:
									if(md5==null) {
										md5=ByteBuffer.wrap(Hash.encryptMD5(cred));
									}
									if(md5.compareTo(cdd.cred)==0) {
										return Result.ok(cdd.expires);
									} else if (willSpecialLog(trans,user)) {
										trans.audit().log("Special DEBUG:", user, "Client sent: ", trans.encryptor().encrypt(new String(cred)) ,cdd.expires);
									}
									break;
								case CredDAO.BASIC_AUTH_SHA256:
									ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE + cred.length);
									bb.putInt(cdd.other);
									bb.put(cred);
									byte[] hash = Hash.hashSHA256(bb.array());
	
									ByteBuffer sha256 = ByteBuffer.wrap(hash);
									if(sha256.compareTo(cdd.cred)==0) {
										return Result.ok(cdd.expires);
									} else if (willSpecialLog(trans,user)) {
										trans.audit().log("Special DEBUG:", user, "Client sent: ", trans.encryptor().encrypt(new String(cred)) ,cdd.expires);
									}
									break;
								default:
									trans.error().log("Unknown Credential Type %s for %s, %s",Integer.toString(cdd.type),cdd.id, Chrono.dateTime(cdd.expires));
							}
						} catch (NoSuchAlgorithmException e) {
							trans.error().log(e);
						}
					} else {
						rv = Result.err(Status.ERR_Security,
								"Credentials expired " + cdd.expires.toString());
					}
				} // end for each
			}
		} else {
			return Result.err(result);
		}
		return rv == null ? Result.create((Date) null, Status.ERR_Security,
				"Wrong credential") : rv;
	}


	public Result<CredDAO.Data> userCredSetup(AuthzTrans trans, CredDAO.Data cred) {
		if(cred.type==CredDAO.RAW) {
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
			
		}
		return Result.err(Status.ERR_Security,"invalid/unreadable credential");
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
			int seg[] = series(NsDAO.CACHE_SEG);
			for(int i: seg) {cacheClear(trans, NsDAO.TABLE,i);}
			rv = cacheInfoDAO.touch(trans, NsDAO.TABLE, seg);
		}
		if (all || PermDAO.TABLE.equals(cname)) {
			int seg[] = series(NsDAO.CACHE_SEG);
			for(int i: seg) {cacheClear(trans, PermDAO.TABLE,i);}
			rv = cacheInfoDAO.touch(trans, PermDAO.TABLE,seg);
		}
		if (all || RoleDAO.TABLE.equals(cname)) {
			int seg[] = series(NsDAO.CACHE_SEG);
			for(int i: seg) {cacheClear(trans, RoleDAO.TABLE,i);}
			rv = cacheInfoDAO.touch(trans, RoleDAO.TABLE,seg);
		}
		if (all || UserRoleDAO.TABLE.equals(cname)) {
			int seg[] = series(NsDAO.CACHE_SEG);
			for(int i: seg) {cacheClear(trans, UserRoleDAO.TABLE,i);}
			rv = cacheInfoDAO.touch(trans, UserRoleDAO.TABLE,seg);
		}
		if (all || CredDAO.TABLE.equals(cname)) {
			int seg[] = series(NsDAO.CACHE_SEG);
			for(int i: seg) {cacheClear(trans, CredDAO.TABLE,i);}
			rv = cacheInfoDAO.touch(trans, CredDAO.TABLE,seg);
		}
		if (all || CertDAO.TABLE.equals(cname)) {
			int seg[] = series(NsDAO.CACHE_SEG);
			for(int i: seg) {cacheClear(trans, CertDAO.TABLE,i);}
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

	public boolean isDelegated(AuthzTrans trans, String user, String approver) {
		Result<List<DelegateDAO.Data>> userDelegatedFor = delegateDAO
				.readByDelegate(trans, user);
		for (DelegateDAO.Data curr : userDelegatedFor.value) {
			if (curr.user.equals(approver) && curr.delegate.equals(user)
					&& curr.expires.after(new Date())) {
				return true;
			}
		}
		return false;
	}

	public static boolean willSpecialLog(AuthzTrans trans, String user) {
		Boolean b = trans.get(specialLogSlot, null);
		if(b==null) {
			if(specialLog==null) {
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
			specialLog = new HashSet<String>();
		}
		boolean rc = specialLog.add(id);
		if(rc) {
			trans.trace().log("Trace on for",id);			
		}
		return rc;
	}

	public static synchronized boolean specialLogOff(AuthzTrans trans, String id) {
		if(specialLog==null) {
			return false;
		}
		boolean rv = specialLog.remove(id);
		if (specialLog.isEmpty()) {
			specialLog = null;
		}
		if(rv) {
			trans.trace().log("Trace off for",id);
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

	public Result<String> isOwnerSponsor(AuthzTrans trans, String user, String ns, Identity mechID) {
		
		Identity caller;
		Organization org = trans.org();
		try {
			caller = org.getIdentity(trans, user);
			if(caller==null || !caller.isFound()) {
				return Result.err(Status.ERR_NotFound,"%s is not a registered %s entity",user,org.getName());
			}
		} catch (Exception e) {
			return Result.err(e);
		}
		String sponsor = mechID.responsibleTo();
		Result<List<UserRoleDAO.Data>> rur = userRoleDAO.read(trans, user,ns+DOT_OWNER);
		boolean isOwner = false;
		if(rur.isOKhasData()) {for(UserRoleDAO.Data urdd : rur.value){
			if(urdd.expires.after(new Date())) {
				isOwner = true;
			}
		}};
		if(!isOwner) {
			return Result.err(Status.ERR_Policy,"%s is not a current owner of %s",user,ns);
		}
		
		if(!caller.id().equals(sponsor)) {
			return Result.err(Status.ERR_Denied,"%s is not the sponsor of %s",user,mechID.id());
		}
		return Result.ok(sponsor);
	}
	
	public boolean isAdmin(AuthzTrans trans, String user, String ns) {
		Date now = new Date();
		Result<List<UserRoleDAO.Data>> rur = userRoleDAO.read(trans, user,ns+ADMIN);
		if(rur.isOKhasData()) {for(UserRoleDAO.Data urdd : rur.value){
			if(urdd.expires.after(now)) {
				return true;
			}
		}};
		return false;
	}
	
	public boolean isOwner(AuthzTrans trans, String user, String ns) {
		Result<List<UserRoleDAO.Data>> rur = userRoleDAO.read(trans, user,ns+DOT_OWNER);
		Date now = new Date();
		if(rur.isOKhasData()) {for(UserRoleDAO.Data urdd : rur.value){
			if(urdd.expires.after(now)) {
				return true;
			}
		}};
		return false;
	}

	public int countOwner(AuthzTrans trans, String user, String ns) {
		Result<List<UserRoleDAO.Data>> rur = userRoleDAO.read(trans, user,ns+DOT_OWNER);
		Date now = new Date();
		int count = 0;
		if(rur.isOKhasData()) {for(UserRoleDAO.Data urdd : rur.value){
			if(urdd.expires.after(now)) {
				++count;
			}
		}};
		return count;
	}

}
