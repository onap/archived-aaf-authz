/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
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
package com.att.authz.cm.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.att.authz.cm.api.API_Cert;
import com.att.authz.cm.ca.CA;
import com.att.authz.cm.cert.BCFactory;
import com.att.authz.cm.cert.CSRMeta;
import com.att.authz.cm.data.CertDrop;
import com.att.authz.cm.data.CertRenew;
import com.att.authz.cm.data.CertReq;
import com.att.authz.cm.data.CertResp;
import com.att.authz.cm.validation.Validator;
import com.att.authz.env.AuthzTrans;
import com.att.authz.layer.Result;
import com.att.authz.org.Organization;
import com.att.authz.org.Organization.Identity;
import com.att.authz.org.OrganizationException;
import com.att.cadi.Hash;
import com.att.cadi.aaf.AAFPermission;
import com.att.cadi.aaf.v2_0.AAFCon;
import com.att.cadi.cm.Factory;
import com.att.dao.CassAccess;
import com.att.dao.DAO;
import com.att.dao.aaf.cass.ArtiDAO;
import com.att.dao.aaf.cass.CacheInfoDAO;
import com.att.dao.aaf.cass.CertDAO;
import com.att.dao.aaf.cass.CredDAO;
import com.att.dao.aaf.cass.HistoryDAO;
import com.att.dao.aaf.cass.Status;
import com.att.dao.aaf.hl.Question;
import com.att.inno.env.APIException;
import com.att.inno.env.Slot;
import com.att.inno.env.util.Chrono;
import com.datastax.driver.core.Cluster;


public class CMService {
	// If we add more CAs, may want to parameterize
	private static final int STD_RENEWAL = 30;
	private static final int MAX_RENEWAL = 60;
	private static final int MIN_RENEWAL = 10;
	
	public static final String REQUEST = "request";
	public static final String RENEW = "renew";
	public static final String DROP = "drop";
	public static final String SANS = "san";
	
	private static final String[] NO_NOTES = new String[0];
	private Slot sCertAuth;
	private final CertDAO certDAO;
	private final CredDAO credDAO;
	private final ArtiDAO artiDAO;
	private DAO<AuthzTrans, ?>[] daos;

	@SuppressWarnings("unchecked")
	public CMService(AuthzTrans trans, CertManAPI certman) throws APIException, IOException {

		sCertAuth = certman.env.slot(API_Cert.CERT_AUTH);
		Cluster cluster;
		try {
			cluster = com.att.dao.CassAccess.cluster(certman.env,null);
		} catch (IOException e) {
			throw new APIException(e);
		}

		// jg 4/2015 SessionFilter unneeded... DataStax already deals with Multithreading well
		
		HistoryDAO hd = new HistoryDAO(trans,  cluster, CassAccess.KEYSPACE);
		CacheInfoDAO cid = new CacheInfoDAO(trans, hd);
		certDAO = new CertDAO(trans, hd, cid);
		credDAO = new CredDAO(trans, hd, cid);
		artiDAO = new ArtiDAO(trans, hd, cid);
		
		daos =(DAO<AuthzTrans, ?>[]) new DAO<?,?>[] {
				hd,cid,certDAO,credDAO,artiDAO
		};

		// Setup Shutdown Hooks for Cluster and Pooled Sessions
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				for(DAO<AuthzTrans,?> dao : daos) {
					dao.close(trans);
				}

//				sessionFilter.destroy();
				cluster.close();
			}
		}); 
	}
	
	public Result<CertResp> requestCert(AuthzTrans trans,Result<CertReq> req) {
		if(req.isOK()) {
			CA ca = trans.get(sCertAuth, null);
			if(ca==null) {
				return Result.err(Result.err(Result.ERR_BadData, "Invalid Cert Authority requested"));
			}

			// Allow only AAF CA without special permission
			if(!ca.getName().equals("aaf") && !trans.fish( new AAFPermission(ca.getPermType(), ca.getName(), REQUEST))) {
				return Result.err(Status.ERR_Denied, "'%s' does not have permission to request Certificates from Certificate Authority '%s'", 
						trans.user(),ca.getName());
			}

			List<String> notes = null;
			List<String> fqdns;
			String email = null;

			try {
				Organization org = trans.org();
				
				// Policy 1: Requests are only by Pre-Authorized Configurations
				ArtiDAO.Data add = null;
				try {
					for(InetAddress ia : InetAddress.getAllByName(trans.ip())) {
						Result<List<ArtiDAO.Data>> ra = artiDAO.read(trans, req.value.mechid,ia.getHostName());
						if(ra.isOKhasData()) {
							add = ra.value.get(0);
							break;
						}
					}
				} catch (UnknownHostException e1) {
					return Result.err(Result.ERR_BadData,"There is no host for %s",trans.ip());
				}
				
				if(add==null) {
					return Result.err(Result.ERR_BadData,"There is no configuration for %s",req.value.mechid);
				}
				
				// Policy 2: If Config marked as Expired, do not create or renew
				Date now = new Date();
				if(add.expires!=null && now.after(add.expires)) {
					return Result.err(Result.ERR_Policy,"Configuration for %s %s is expired %s",add.mechid,add.machine,Chrono.dateFmt.format(add.expires));
				}
				
				// Policy 3: MechID must be current
				Identity muser = org.getIdentity(trans, add.mechid);
				if(muser == null) {
					return Result.err(Result.ERR_Policy,"MechID must exist in %s",org.getName());
				}
				
				// Policy 4: Sponsor must be current
				Identity ouser = muser.owner();
				if(ouser==null) {
					return Result.err(Result.ERR_Policy,"%s does not have a current sponsor at %s",add.mechid,org.getName());
				} else if(!ouser.isFound() || !ouser.isResponsible()) {
					return Result.err(Result.ERR_Policy,"%s reports that %s cannot be responsible for %s",org.getName(),trans.user());
				}
				
					// Set Email from most current Sponsor
				email = ouser.email();
				
				// Policy 5: keep Artifact data current
				if(!ouser.fullID().equals(add.sponsor)) {
					add.sponsor = ouser.fullID();
					artiDAO.update(trans, add);
				}
		
				// Policy 6: Requester must be granted Change permission in Namespace requested
				String mechNS = AAFCon.reverseDomain(req.value.mechid);
				if(mechNS==null) {
					return Result.err(Status.ERR_Denied, "%s does not reflect a valid AAF Namespace",req.value.mechid);
				}
				
				// Policy 7: Caller must be the MechID or have specifically delegated permissions
				if(!trans.user().equals(req.value.mechid) && !trans.fish(new AAFPermission(mechNS + ".certman", ca.getName() , "request"))) {
					return Result.err(Status.ERR_Denied, "%s must have access to modify x509 certs in NS %s",trans.user(),mechNS);
				}
				
	
				// Policy 8: SANs only allowed by Exception... need permission
				fqdns = new ArrayList<String>();
				fqdns.add(add.machine);  // machine is first
				if(req.value.fqdns.size()>1 && !trans.fish(new AAFPermission(ca.getPermType(), ca.getName(), SANS))) {
					if(notes==null) {notes = new ArrayList<String>();}
					notes.add("Warning: Subject Alternative Names only allowed by Permission: Get CSO Exception.  This Certificate will be created, but without SANs");
				} else {
					for(String m : req.value.fqdns) {
						if(!add.machine.equals(m)) {
							fqdns.add(m);
						}
					}
				}
				
			} catch (Exception e) {
				trans.error().log(e);
				return Result.err(Status.ERR_Denied,"MechID Sponsorship cannot be determined at this time.  Try later");
			}
			
			CSRMeta csrMeta;
			try {
				csrMeta = BCFactory.createCSRMeta(
						ca, 
						req.value.mechid, 
						email, 
						fqdns);
				X509Certificate x509 = ca.sign(trans, csrMeta);
				if(x509==null) {
					return Result.err(Result.ERR_ActionNotCompleted,"x509 Certificate not signed by CA");
				}
				CertDAO.Data cdd = new CertDAO.Data();
				cdd.ca=ca.getName();
				cdd.serial=x509.getSerialNumber();
				cdd.id=req.value.mechid;
				cdd.x500=x509.getSubjectDN().getName();
				cdd.x509=Factory.toString(trans, x509);
				certDAO.create(trans, cdd);
				
				CredDAO.Data crdd = new CredDAO.Data();
				crdd.other = Question.random.nextInt();
				crdd.cred=getChallenge256SaltedHash(csrMeta.challenge(),crdd.other);
				crdd.expires = x509.getNotAfter();
				crdd.id = req.value.mechid;
				crdd.ns = Question.domain2ns(crdd.id);
				crdd.type = CredDAO.CERT_SHA256_RSA;
				credDAO.create(trans, crdd);
				
				CertResp cr = new CertResp(trans,x509,csrMeta, compileNotes(notes));
				return Result.ok(cr);
			} catch (Exception e) {
				trans.error().log(e);
				return Result.err(Result.ERR_ActionNotCompleted,e.getMessage());
			}
		} else {
			return Result.err(req);
		}
	}

    public Result<CertResp> renewCert(AuthzTrans trans, Result<CertRenew> renew) {
		if(renew.isOK()) {
			return Result.err(Result.ERR_NotImplemented,"Not implemented yet");
		} else {
			return Result.err(renew);
		}	
	}

	public Result<Void> dropCert(AuthzTrans trans, Result<CertDrop> drop) {
		if(drop.isOK()) {
			return Result.err(Result.ERR_NotImplemented,"Not implemented yet");
		} else {
			return Result.err(drop);
		}	
	}

	///////////////
	// Artifact
	//////////////
	public Result<Void> createArtifact(AuthzTrans trans, List<ArtiDAO.Data> list) {
		Validator v = new Validator().artisRequired(list, 1);
		if(v.err()) {
			return Result.err(Result.ERR_BadData,v.errs());
		}
		for(ArtiDAO.Data add : list) {
			try {
				// Policy 1: MechID must exist in Org
				Identity muser = trans.org().getIdentity(trans, add.mechid);
				if(muser == null) {
					return Result.err(Result.ERR_Denied,"%s is not valid for %s", add.mechid,trans.org().getName());
				}
				
				// Policy 2: MechID must have valid Organization Owner
				Identity ouser = muser.owner();
				if(ouser == null) {
					return Result.err(Result.ERR_Denied,"%s is not a valid Sponsor for %s at %s",
							trans.user(),add.mechid,trans.org().getName());
				}
				
				// Policy 3: Calling ID must be MechID Owner
				if(!trans.user().equals(ouser.fullID())) {
					return Result.err(Result.ERR_Denied,"%s is not the Sponsor for %s at %s",
							trans.user(),add.mechid,trans.org().getName());
				}

				// Policy 4: Renewal Days are between 10 and 60 (constants, may be parameterized)
				if(add.renewDays<MIN_RENEWAL) {
					add.renewDays = STD_RENEWAL;
				} else if(add.renewDays>MAX_RENEWAL) {
					add.renewDays = MAX_RENEWAL;
				}
				
				// Policy 5: If Notify is blank, set to Owner's Email
				if(add.notify==null || add.notify.length()==0) {
					add.notify = "mailto:"+ouser.email();
				}

				// Set Sponsor from Golden Source
				add.sponsor = ouser.fullID();
				
				
			} catch (OrganizationException e) {
				return Result.err(e);
			}
			// Add to DB
			Result<ArtiDAO.Data> rv = artiDAO.create(trans, add);
			// TODO come up with Partial Reporting Scheme, or allow only one at a time.
			if(rv.notOK()) {
				return Result.err(rv);
			}
		}
		return Result.ok();
	}

	public Result<List<ArtiDAO.Data>> readArtifacts(AuthzTrans trans, ArtiDAO.Data add) throws OrganizationException {
		Validator v = new Validator().keys(add);
		if(v.err()) {
			return Result.err(Result.ERR_BadData,v.errs());
		}
		String ns = AAFCon.reverseDomain(add.mechid);
		
		if( trans.user().equals(add.mechid)
			|| trans.fish(new AAFPermission(ns + ".access", "*", "read"))
			|| (trans.org().validate(trans,Organization.Policy.OWNS_MECHID,null,add.mechid))==null) {
				return artiDAO.read(trans, add);
		} else {
			return Result.err(Result.ERR_Denied,"%s is not %s, is not the sponsor, and doesn't have delegated permission.",trans.user(),add.mechid); // note: reason is set by 2nd case, if 1st case misses
		}

	}

	public Result<List<ArtiDAO.Data>> readArtifactsByMechID(AuthzTrans trans, String mechid) throws OrganizationException {
		Validator v = new Validator().nullOrBlank("mechid", mechid);
		if(v.err()) {
			return Result.err(Result.ERR_BadData,v.errs());
		}
		String ns = AAFCon.reverseDomain(mechid);
		
		String reason;
		if(trans.fish(new AAFPermission(ns + ".access", "*", "read"))
			|| (reason=trans.org().validate(trans,Organization.Policy.OWNS_MECHID,null,mechid))==null) {
			return artiDAO.readByMechID(trans, mechid);
		} else {
			return Result.err(Result.ERR_Denied,reason); // note: reason is set by 2nd case, if 1st case misses
		}

	}

	public Result<List<ArtiDAO.Data>> readArtifactsByMachine(AuthzTrans trans, String machine) {
		Validator v = new Validator().nullOrBlank("machine", machine);
		if(v.err()) {
			return Result.err(Result.ERR_BadData,v.errs());
		}
		
		// TODO do some checks?

		Result<List<ArtiDAO.Data>> rv = artiDAO.readByMachine(trans, machine);
		return rv;
	}

	public Result<Void> updateArtifact(AuthzTrans trans, List<ArtiDAO.Data> list) throws OrganizationException {
		Validator v = new Validator().artisRequired(list, 1);
		if(v.err()) {
			return Result.err(Result.ERR_BadData,v.errs());
		}
		
		// Check if requesting User is Sponsor
		//TODO - Shall we do one, or multiples?
		for(ArtiDAO.Data add : list) {
			// Policy 1: MechID must exist in Org
			Identity muser = trans.org().getIdentity(trans, add.mechid);
			if(muser == null) {
				return Result.err(Result.ERR_Denied,"%s is not valid for %s", add.mechid,trans.org().getName());
			}
			
			// Policy 2: MechID must have valid Organization Owner
			Identity ouser = muser.owner();
			if(ouser == null) {
				return Result.err(Result.ERR_Denied,"%s is not a valid Sponsor for %s at %s",
						trans.user(),add.mechid,trans.org().getName());
			}

			// Policy 3: Renewal Days are between 10 and 60 (constants, may be parameterized)
			if(add.renewDays<MIN_RENEWAL) {
				add.renewDays = STD_RENEWAL;
			} else if(add.renewDays>MAX_RENEWAL) {
				add.renewDays = MAX_RENEWAL;
			}

			// Policy 4: Data is always updated with the latest Sponsor
			// Add to Sponsor, to make sure we are always up to date.
			add.sponsor = ouser.fullID();

			// Policy 5: If Notify is blank, set to Owner's Email
			if(add.notify==null || add.notify.length()==0) {
				add.notify = "mailto:"+ouser.email();
			}

			// Policy 4: only Owner may update info
			if(trans.user().equals(add.sponsor)) {
				return artiDAO.update(trans, add);
			} else {
				return Result.err(Result.ERR_Denied,"%s may not update info for %s",trans.user(),muser.fullID());
			}
			
		}
		return Result.err(Result.ERR_BadData,"No Artifacts to update");
	}
	
	public Result<Void> deleteArtifact(AuthzTrans trans, String mechid, String machine) throws OrganizationException {
		Validator v = new Validator()
				.nullOrBlank("mechid", mechid)
				.nullOrBlank("machine", machine);
		if(v.err()) {
			return Result.err(Result.ERR_BadData,v.errs());
		}

		Result<List<ArtiDAO.Data>> rlad = artiDAO.read(trans, mechid, machine);
		if(rlad.notOKorIsEmpty()) {
			return Result.err(Result.ERR_NotFound,"Artifact for %s %s does not exist.",mechid,machine);
		}
		
		return deleteArtifact(trans,rlad.value.get(0));
	}
		
	private Result<Void> deleteArtifact(AuthzTrans trans, ArtiDAO.Data add) throws OrganizationException {
		// Policy 1: Record should be delete able only by Existing Sponsor.  
		String sponsor=null;
		Identity muser = trans.org().getIdentity(trans, add.mechid);
		if(muser != null) {
			Identity ouser = muser.owner();
			if(ouser!=null) {
				sponsor = ouser.fullID();
			}
		}
		// Policy 1.a: If Sponsorship is deleted in system of Record, then 
		// accept deletion by sponsor in Artifact Table
		if(sponsor==null) {
			sponsor = add.sponsor;
		}
		
		String ns = AAFCon.reverseDomain(add.mechid);

		if(trans.fish(new AAFPermission(ns + ".access", "*", "write"))
				|| trans.user().equals(sponsor)) {
			return artiDAO.delete(trans, add, false);
		}
		return null;
	}

	public Result<Void> deleteArtifact(AuthzTrans trans, List<ArtiDAO.Data> list) {
		Validator v = new Validator().artisRequired(list, 1);
		if(v.err()) {
			return Result.err(Result.ERR_BadData,v.errs());
		}

		try {
			boolean partial = false;
			Result<Void> result=null;
			for(ArtiDAO.Data add : list) {
				result = deleteArtifact(trans, add);
				if(result.notOK()) {
					partial = true;
				}
			}
			if(result == null) {
				result = Result.err(Result.ERR_BadData,"No Artifacts to delete"); 
			} else if(partial) {
				result.partialContent(true);
			}
			return result;
		} catch(Exception e) {
			return Result.err(e);
		}
	}

	private String[] compileNotes(List<String> notes) {
		String[] rv;
		if(notes==null) {
			rv = NO_NOTES;
		} else {
			rv = new String[notes.size()];
			notes.toArray(rv);
		}
		return rv;
	}

	private ByteBuffer getChallenge256SaltedHash(String challenge, int salt) throws NoSuchAlgorithmException {
		ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE + challenge.length());
		bb.putInt(salt);
		bb.put(challenge.getBytes());
		byte[] hash = Hash.hashSHA256(bb.array());
		return ByteBuffer.wrap(hash);
	}
}
