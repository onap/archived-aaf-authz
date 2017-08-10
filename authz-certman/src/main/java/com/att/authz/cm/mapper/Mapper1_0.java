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
package com.att.authz.cm.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import aaf.v2_0.Error;
import certman.v1_0.Artifacts;
import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.BaseRequest;
import certman.v1_0.CertInfo;
import certman.v1_0.CertificateDrop;
import certman.v1_0.CertificateRenew;
import certman.v1_0.CertificateRequest;

import com.att.authz.cm.data.CertDrop;
import com.att.authz.cm.data.CertRenew;
import com.att.authz.cm.data.CertReq;
import com.att.authz.cm.data.CertResp;
import com.att.authz.cm.validation.Validator;
import com.att.authz.env.AuthzTrans;
import com.att.authz.layer.Result;
import com.att.cadi.aaf.v2_0.AAFCon;
import com.att.cadi.util.Vars;
import com.att.dao.aaf.cass.ArtiDAO;
import com.att.dao.aaf.cass.ArtiDAO.Data;


public class Mapper1_0 implements Mapper<BaseRequest,CertInfo,Artifacts,Error> {
	
	@Override
	public Class<?> getClass(API api) {
		switch(api) {
			case CERT_REQ: return CertificateRequest.class;
			case CERT_RENEW: return CertificateRenew.class;
			case CERT_DROP: return CertificateDrop.class;
			case CERT: return CertInfo.class;
			case ARTIFACTS: return Artifacts.class;
			case ERROR: return Error.class;
			case VOID: return Void.class;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A> A newInstance(API api) {
		switch(api) {
			case CERT_REQ: return (A) new CertificateRequest();
			case CERT_RENEW: return (A) new CertificateRenew();
			case CERT_DROP: return (A) new CertificateDrop();
			case CERT: return (A) new CertInfo();
			case ARTIFACTS: return (A) new Artifacts();
			case ERROR: return (A)new Error();
			case VOID: return null;
		}
		return null;
	}

	//////////////  Mapping Functions /////////////
	@Override
	public Error errorFromMessage(StringBuilder holder, String msgID, String text, String... var) {
		Error err = new Error();
		err.setMessageId(msgID);
		// AT&T Restful Error Format requires numbers "%" placements
		err.setText(Vars.convert(holder, text, var));
		for(String s : var) {
			err.getVariables().add(s);
		}
		return err;
	}

	/* (non-Javadoc)
	 * @see com.att.authz.certman.mapper.Mapper#toCert(com.att.authz.env.AuthzTrans, com.att.authz.layer.Result)
	 */
	@Override
	public Result<CertInfo> toCert(AuthzTrans trans, Result<CertResp> in, String[] trustChain) throws IOException {
		if(in.isOK()) {
			CertResp cin = in.value;
			CertInfo cout = newInstance(API.CERT);
			cout.setPrivatekey(cin.privateString());
			String value;
			if((value=cin.challenge())!=null) {
				cout.setChallenge(value);
			}
			cout.getCerts().add(cin.asCertString());
			if(trustChain!=null) {
				for(String c : trustChain) {
					cout.getCerts().add(c);
				}
			}
			if(cin.notes()!=null) {
				boolean first = true;
				StringBuilder sb = new StringBuilder();
				for(String n : cin.notes()) {
					if(first) {
						first = false;
					} else {
						sb.append('\n');
					}
					sb.append(n);
				}
				cout.setNotes(sb.toString());
			}
			return Result.ok(cout);
		} else {
			return Result.err(in);
		}
	}

	/* (non-Javadoc)
	 * @see com.att.authz.certman.mapper.Mapper#toReq(com.att.authz.env.AuthzTrans, java.lang.Object)
	 */
	@Override
	public Result<CertReq> toReq(AuthzTrans trans, BaseRequest req) {
		CertificateRequest in;
		try {
			in = (CertificateRequest)req;
		} catch(ClassCastException e) {
			return Result.err(Result.ERR_BadData,"Request is not a CertificateRequest");
		}

		CertReq out = new CertReq();
		Validator v = new Validator();
		if(v.isNull("CertRequest", req)
			.nullOrBlank("MechID", out.mechid=in.getMechid())
			.nullBlankMin("FQDNs", out.fqdns=in.getFqdns(),1)
			.err()) {
			return Result.err(Result.ERR_BadData, v.errs());
		}
		out.emails = in.getEmail();
		out.sponsor=in.getSponsor();
		out.start = in.getStart();
		out.end = in.getEnd();
		return Result.ok(out);
	}

	/* (non-Javadoc)
	 * @see com.att.authz.certman.mapper.Mapper#toRenew(com.att.authz.env.AuthzTrans, java.lang.Object)
	 */
	@Override
	public Result<CertRenew> toRenew(AuthzTrans trans, BaseRequest req) {
		return Result.err(Result.ERR_NotImplemented,"Not Implemented... yet");
	}

	/* (non-Javadoc)
	 * @see com.att.authz.certman.mapper.Mapper#toDrop(com.att.authz.env.AuthzTrans, java.lang.Object)
	 */
	@Override
	public Result<CertDrop> toDrop(AuthzTrans trans, BaseRequest req) {
		return Result.err(Result.ERR_NotImplemented,"Not Implemented... yet");
	}

	/* (non-Javadoc)
	 * @see com.att.authz.cm.mapper.Mapper#toArtifact(com.att.authz.env.AuthzTrans, java.lang.Object)
	 */
	@Override
	public List<ArtiDAO.Data> toArtifact(AuthzTrans trans, Artifacts artifacts) {
		List<ArtiDAO.Data> ladd = new ArrayList<ArtiDAO.Data>();
		for(Artifact arti : artifacts.getArtifact()) {
			ArtiDAO.Data data = new ArtiDAO.Data();
			data.mechid = arti.getMechid();
			data.machine = arti.getMachine();
			data.type(true).addAll(arti.getType());
			data.ca = arti.getCa();
			data.dir = arti.getDir();
			data.os_user = arti.getOsUser();
			// Optional (on way in)
			data.appName = arti.getAppName();
			data.renewDays = arti.getRenewDays();
			data.notify = arti.getNotification();
			
			// Ignored on way in for create/update
			data.sponsor = arti.getSponsor();
			data.expires = null;
			
			// Derive Optional Data from Machine (Domain) if exists
			if(data.machine!=null) {
				if(data.ca==null) {
					if(data.machine.endsWith(".att.com")) {
						data.ca = "aaf"; // default
					}
				}
				if(data.appName==null ) {
					data.appName=AAFCon.reverseDomain(data.machine);
				}
			}

			ladd.add(data);
		}
		return ladd;
	}

	/* (non-Javadoc)
	 * @see com.att.authz.cm.mapper.Mapper#fromArtifacts(com.att.authz.layer.Result)
	 */
	@Override
	public Result<Artifacts> fromArtifacts(Result<List<Data>> lArtiDAO) {
		if(lArtiDAO.isOK()) {
			Artifacts artis = new Artifacts();
			for(ArtiDAO.Data arti : lArtiDAO.value) {
				Artifact a = new Artifact();
				a.setMechid(arti.mechid);
				a.setMachine(arti.machine);
				a.setSponsor(arti.sponsor);
				a.setAppName(arti.appName);
				a.setCa(arti.ca);
				a.setDir(arti.dir);
				a.getType().addAll(arti.type(false));
				a.setOsUser(arti.os_user);
				a.setRenewDays(arti.renewDays);
				a.setNotification(arti.notify);
				artis.getArtifact().add(a);
			}
			return Result.ok(artis);
		} else {
			return Result.err(lArtiDAO);
		}
	}
	
	

}
