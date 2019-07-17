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

package org.onap.aaf.auth.cm.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.onap.aaf.auth.cm.data.CertDrop;
import org.onap.aaf.auth.cm.data.CertRenew;
import org.onap.aaf.auth.cm.data.CertReq;
import org.onap.aaf.auth.cm.data.CertResp;
import org.onap.aaf.auth.cm.validation.CertmanValidator;
import org.onap.aaf.auth.dao.cass.ArtiDAO;
import org.onap.aaf.auth.dao.cass.ArtiDAO.Data;
import org.onap.aaf.auth.dao.cass.CertDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.util.Vars;

import aaf.v2_0.Error;
import certman.v1_0.Artifacts;
import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.BaseRequest;
import certman.v1_0.CertInfo;
import certman.v1_0.CertificateDrop;
import certman.v1_0.CertificateRenew;
import certman.v1_0.CertificateRequest;


public class Mapper2_0 implements Mapper<BaseRequest,CertInfo,Artifacts,Error> {
    
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
    public Error errorFromMessage(StringBuilder holder, String msgID, String text, Object ... var) {
        Error err = new Error();
        err.setMessageId(msgID);
        // AT&T Restful Error Format requires numbers "%" placements
        err.setText(Vars.convert(holder, text, var));
        for (Object s : var) {
            err.getVariables().add(s.toString());
        }
        return err;
    }

    /* (non-Javadoc)
     * @see com.att.authz.certman.mapper.Mapper#toCert(org.onap.aaf.auth.env.test.AuthzTrans, org.onap.aaf.auth.layer.test.Result)
     */
    /* (non-Javadoc)
     * @see com.att.authz.certman.mapper.Mapper#toCert(org.onap.aaf.auth.env.test.AuthzTrans, org.onap.aaf.auth.layer.test.Result)
     */
    @Override
    public Result<CertInfo> toCert(AuthzTrans trans, Result<CertResp> in, boolean withTrustChain) throws IOException {
        if (!in.isOK()) {
            CertResp cin = in.value;
            CertInfo cout = newInstance(API.CERT);
            cout.setPrivatekey(cin.privateString());
            String value;
            if ((value=cin.challenge())!=null) {
                cout.setChallenge(value);
            }
            cout.getCerts().add(cin.asCertString());
            if (cin.trustChain()!=null) {
                for (String c : cin.trustChain()) {
                    cout.getCerts().add(c);
                }
            }
            if (cin.notes()!=null) {
                boolean first = true;
                StringBuilder sb = new StringBuilder();
                for (String n : cin.notes()) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append('\n');
                    }
                    sb.append(n);
                }
                cout.setNotes(sb.toString());
            }
            
            List<String> caIssuerDNs = cout.getCaIssuerDNs();
            for (String s : cin.caIssuerDNs()) {
                caIssuerDNs.add(s);
            }

            cout.setEnv(cin.env());
            return Result.ok(cout);
        } else {
            return Result.err(in);
        }
    }


    @Override
    public Result<CertInfo> toCert(AuthzTrans trans, Result<List<CertDAO.Data>> in) {
        if (in.isOK()) {
            CertInfo cout = newInstance(API.CERT);
            List<String> certs = cout.getCerts();
            for (CertDAO.Data cdd : in.value) {
                certs.add(cdd.x509);
            }
            return Result.ok(cout);
        } else {
            return Result.err(in);
        }
    }

    /* (non-Javadoc)
     * @see com.att.authz.certman.mapper.Mapper#toReq(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.Object)
     */
    @Override
    public Result<CertReq> toReq(AuthzTrans trans, BaseRequest req) {
        CertificateRequest in;
        try {
            in = (CertificateRequest)req;
        } catch (ClassCastException e) {
            return Result.err(Result.ERR_BadData,"Request is not a CertificateRequest");
        }

        CertReq out = new CertReq();
        CertmanValidator v = new CertmanValidator();
        v.isNull("CertRequest", req)
            .nullOrBlank("MechID", out.mechid=in.getMechid());
        v.nullBlankMin("FQDNs", out.fqdns=in.getFqdns(),1);
        if (v.err()) {
            return Result.err(Result.ERR_BadData, v.errs());
        }

        out.emails = in.getEmail();
        out.sponsor=in.getSponsor();
        out.start = in.getStart();
        out.end = in.getEnd();
        out.fqdns = in.getFqdns();
        return Result.ok(out);
    }

    /* (non-Javadoc)
     * @see com.att.authz.certman.mapper.Mapper#toRenew(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.Object)
     */
    @Override
    public Result<CertRenew> toRenew(AuthzTrans trans, BaseRequest req) {
        return Result.err(Result.ERR_NotImplemented,"Not Implemented... yet");
    }

    /* (non-Javadoc)
     * @see com.att.authz.certman.mapper.Mapper#toDrop(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.Object)
     */
    @Override
    public Result<CertDrop> toDrop(AuthzTrans trans, BaseRequest req) {
        return Result.err(Result.ERR_NotImplemented,"Not Implemented... yet");
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.cm.mapper.Mapper#toArtifact(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.Object)
     */
    @Override
    public List<ArtiDAO.Data> toArtifact(AuthzTrans trans, Artifacts artifacts) {
        List<ArtiDAO.Data> ladd = new ArrayList<>();
        for (Artifact arti : artifacts.getArtifact()) {
            ArtiDAO.Data data = new ArtiDAO.Data();
            data.mechid = trim(arti.getMechid());
            data.machine = trim(arti.getMachine());
            if(arti.getType()!=null) {
                Set<String> ss = data.type(true);
	            for(String t : arti.getType()) {
	            	ss.add(t.trim());
	            }
            }
            data.type(true).addAll(arti.getType());
            data.ca = trim(arti.getCa());
            data.dir = trim(arti.getDir());
            data.os_user = trim(arti.getOsUser());
            // Optional (on way in)
            data.ns = trim(arti.getNs());
            data.renewDays = arti.getRenewDays();
            data.notify = trim(arti.getNotification());
            
            // Ignored on way in for create/update
            data.sponsor = trim(arti.getSponsor());
            data.expires = null;
            if(arti.getSans()!=null) {
              Set<String> ss = data.sans(true);
              for(String s : arti.getSans()) {
            	  ss.add(s.trim());
              }
            }
            ladd.add(data);
        }
        return ladd;
    }

    private String trim(String s) {
    	if(s==null) {
    		return s;
    	} else {
    		return s.trim();
    	}
	}

	/* (non-Javadoc)
     * @see org.onap.aaf.auth.cm.mapper.Mapper#fromArtifacts(org.onap.aaf.auth.layer.test.Result)
     */
    @Override
    public Result<Artifacts> fromArtifacts(Result<List<Data>> lArtiDAO) {
        if (lArtiDAO.isOK()) {
            Artifacts artis = new Artifacts();
            for (ArtiDAO.Data arti : lArtiDAO.value) {
                Artifact a = new Artifact();
                a.setMechid(arti.mechid);
                a.setMachine(arti.machine);
                a.setSponsor(arti.sponsor);
                a.setNs(arti.ns);
                a.setCa(arti.ca);
                a.setDir(arti.dir);
                a.getType().addAll(arti.type(false));
                a.setOsUser(arti.os_user);
                a.setRenewDays(arti.renewDays);
                a.setNotification(arti.notify);
                a.getSans().addAll(arti.sans(false));
                artis.getArtifact().add(a);
            }
            return Result.ok(artis);
        } else {
            return Result.err(lArtiDAO);
        }
    }
    
    

}