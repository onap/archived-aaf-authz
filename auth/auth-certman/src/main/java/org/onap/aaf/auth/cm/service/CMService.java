/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2018 IBM.
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

package org.onap.aaf.auth.cm.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.onap.aaf.auth.cm.AAF_CM;
import org.onap.aaf.auth.cm.ca.CA;
import org.onap.aaf.auth.cm.ca.X509andChain;
import org.onap.aaf.auth.cm.cert.BCFactory;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.auth.cm.data.CertDrop;
import org.onap.aaf.auth.cm.data.CertRenew;
import org.onap.aaf.auth.cm.data.CertReq;
import org.onap.aaf.auth.cm.data.CertResp;
import org.onap.aaf.auth.cm.validation.CertmanValidator;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.cass.ArtiDAO;
import org.onap.aaf.auth.dao.cass.CacheInfoDAO;
import org.onap.aaf.auth.dao.cass.CertDAO;
import org.onap.aaf.auth.dao.cass.CertDAO.Data;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.HistoryDAO;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.Hash;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.configure.Factory;
import org.onap.aaf.cadi.util.FQI;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Chrono;

public class CMService {
    // If we add more CAs, may want to parameterize
    private static final int STD_RENEWAL = 30;
    private static final int MAX_RENEWAL = 60;
    private static final int MIN_RENEWAL = 10;
    // Limit total requests
    private static final int MAX_X509s = 200; // Need a "LIMIT Exception" DB.
    private static final String MAX_X509S_TAG = "cm_max_x509s"; // be able to adjust limit in future

    public static final String REQUEST = "request";
    public static final String IGNORE_IPS = "ignoreIPs";
    public static final String RENEW = "renew";
    public static final String DROP = "drop";
    public static final String DOMAIN = "domain";
    public static final String DYNAMIC_SANS="dynamic_sans";

    private static final String CERTMAN = "certman";
    private static final String ACCESS = "access";

    private static final String[] NO_NOTES = new String[0];
    private final Permission root_read_permission;
    private final CertDAO certDAO;
    private final CredDAO credDAO;
    private final ArtiDAO artiDAO;
    private AAF_CM certManager;
    private Boolean allowIgnoreIPs;
    private AAFPermission limitOverridePerm;
    private int max_509s;

    // @SuppressWarnings("unchecked")
    public CMService(final AuthzTrans trans, AAF_CM certman) throws APIException, IOException {
        // Jonathan 4/2015 SessionFilter unneeded... DataStax already deals with
        // Multithreading well

        HistoryDAO hd = new HistoryDAO(trans, certman.cluster, CassAccess.KEYSPACE);
        CacheInfoDAO cid = new CacheInfoDAO(trans, hd);
        certDAO = new CertDAO(trans, hd, cid);
        credDAO = new CredDAO(trans, hd, cid);
        artiDAO = new ArtiDAO(trans, hd, cid);

        this.certManager = certman;

        root_read_permission=new AAFPermission(
                trans.getProperty(Config.AAF_ROOT_NS, Config.AAF_ROOT_NS_DEF),
                ACCESS,
                "*",
                "read"
        );
        try {
            max_509s = Integer.parseInt(trans.env().getProperty(MAX_X509S_TAG,Integer.toString(MAX_X509s)));
        } catch (Exception e) {
            trans.env().log(e, "");
            max_509s = MAX_X509s;
        }
        limitOverridePerm = new AAFPermission(Define.ROOT_NS(),"certman","quantity","override");
        allowIgnoreIPs = Boolean.valueOf(certman.access.getProperty(Config.CM_ALLOW_IGNORE_IPS, "false"));
        if(allowIgnoreIPs) {
            trans.env().access().log(Level.INIT, "Allowing DNS Evaluation to be turned off with <ns>.certman|<ca name>|"+IGNORE_IPS);
        }
    }

    public Result<CertResp> requestCert(final AuthzTrans trans, final Result<CertReq> req, final CA ca) {
        if (req.isOK()) {
            if (req.value.fqdns.isEmpty()) {
                return Result.err(Result.ERR_BadData, "No Machines passed in Request");
            }

            String key = req.value.fqdns.get(0);

            // Policy 6: Requester must be granted Change permission in Namespace requested
            String mechNS = FQI.reverseDomain(req.value.mechid);
            if (mechNS == null) {
                return Result.err(Status.ERR_Denied, "%s does not reflect a valid AAF Namespace", req.value.mechid);
            }

            List<String> notes = null;
            List<String> fqdns;
            boolean domain_based = false;
            boolean dynamic_sans = false;

            if(req.value.fqdns.isEmpty()) {
                fqdns = new ArrayList<>();
            } else {
                // Only Template or Dynamic permitted to pass in FQDNs
                if (req.value.fqdns.get(0).startsWith("*")) { // Domain set
                    if (trans.fish(new AAFPermission(null,ca.getPermType(), ca.getName(), DOMAIN))) {
                        domain_based = true;
                    } else {
                        return Result.err(Result.ERR_Denied,
                              "Domain based Authorizations (" + req.value.fqdns.get(0) + ") requires Exception");
                    }
                } else {
                    if(trans.fish(new AAFPermission(null, ca.getPermType(), ca.getName(),DYNAMIC_SANS))) {
                        dynamic_sans = true;
                    } else {
                        return Result.err(Result.ERR_Denied,
                            "Dynamic SANs for (" + req.value.mechid + ") requires Permission");
                    }
                }
                fqdns = new ArrayList<>(req.value.fqdns);
            }

            String email = null;

            try {
                Organization org = trans.org();

                boolean ignoreIPs;
                if(allowIgnoreIPs) {
                    ignoreIPs = trans.fish(new AAFPermission(mechNS,CERTMAN, ca.getName(), IGNORE_IPS));
                } else {
                    ignoreIPs = false;
                }


                InetAddress primary = null;
                // Organize incoming information to get to appropriate Artifact
                if (!fqdns.isEmpty()) { // Passed in FQDNS, validated above
                    // Accept domain wild cards, but turn into real machines
                    // Need *domain.com:real.machine.domain.com:san.machine.domain.com:...
                    if (domain_based) { // Domain set
                        // check for Permission in Add Artifact?
                        String domain = fqdns.get(0).substring(1); // starts with *, see above
                        fqdns.remove(0);
                        if (fqdns.isEmpty()) {
                            return Result.err(Result.ERR_Denied,
                                "Requests using domain require machine declaration");
                        }

                        if (!ignoreIPs) {
                            InetAddress ia = InetAddress.getByName(fqdns.get(0));
                            if (ia == null) {
                                return Result.err(Result.ERR_Denied,
                                     "Request not made from matching IP matching domain");
                            } else if (ia.getHostName().endsWith(domain)) {
                                primary = ia;
                            }
                        }

                    } else {
                        // Passed in FQDNs, but not starting with *
                        if (!ignoreIPs) {
                            for (String cn : req.value.fqdns) {
                                try {
                                    InetAddress[] ias = InetAddress.getAllByName(cn);
                                    Set<String> potentialSanNames = new HashSet<>();
                                    for (InetAddress ia1 : ias) {
                                        InetAddress ia2 = InetAddress.getByAddress(ia1.getAddress());
                                        if (primary == null && ias.length == 1 && trans.ip().equals(ia1.getHostAddress())) {
                                            primary = ia1;
                                        } else if (!cn.equals(ia1.getHostName())
                                                && !ia2.getHostName().equals(ia2.getHostAddress())) {
                                            potentialSanNames.add(ia1.getHostName());
                                        }
                                    }
                                } catch (UnknownHostException e1) {
                                    trans.debug().log(e1);
                                    return Result.err(Result.ERR_BadData, "There is no DNS lookup for %s", cn);
                                }
                            }
                        }
                    }
                }

                final String host;
                if (ignoreIPs) {
                    host = req.value.fqdns.get(0);
                } else if (primary == null) {
                    return Result.err(Result.ERR_Denied, "Request not made from matching IP (%s)", req.value.fqdns.get(0));
                } else {
                    String thost = primary.getHostName();
                    host = thost==null?primary.getHostAddress():thost;
                }

                ArtiDAO.Data add = null;
                Result<List<ArtiDAO.Data>> ra = artiDAO.read(trans, req.value.mechid, host);
                if (ra.isOKhasData()) {
                    add = ra.value.get(0); // single key
                    if(dynamic_sans && (add.sans!=null && !add.sans.isEmpty())) { // do not allow both Dynamic and Artifact SANS
                        return Result.err(Result.ERR_Denied,"Authorization must not include SANS when doing Dynamic SANS (%s, %s)", req.value.mechid, key);
                    }
                } else {
                    if(domain_based) {
                        ra = artiDAO.read(trans, req.value.mechid, key);
                        if (ra.isOKhasData()) { // is the Template available?
                            add = ra.value.get(0);
                            add.machine = host;
                            for (String s : fqdns) {
                                if (!s.equals(add.machine)) {
                                    add.sans(true).add(s);
                                }
                            }
                            Result<ArtiDAO.Data> rc = artiDAO.create(trans, add); // Create new Artifact from Template
                            if (rc.notOK()) {
                                return Result.err(rc);
                            }
                        } else {
                            return Result.err(Result.ERR_Denied,"No Authorization Template for %s, %s", req.value.mechid, key);
                        }
                    } else {
                        return Result.err(Result.ERR_Denied,"No Authorization found for %s, %s", req.value.mechid, key);
                    }
                }

                // Add Artifact listed FQDNs
                if(!dynamic_sans) {
                    if (add.sans != null) {
                        for (String s : add.sans) {
                            if (!fqdns.contains(s)) {
                                fqdns.add(s);
                            }
                        }
                    }
                }

                // Policy 2: If Config marked as Expired, do not create or renew
                Date now = new Date();
                if (add.expires != null && now.after(add.expires)) {
                    return Result.err(Result.ERR_Policy, "Configuration for %s %s is expired %s", add.mechid,
                            add.machine, Chrono.dateFmt.format(add.expires));
                }

                // Policy 3: MechID must be current
                Identity muser = org.getIdentity(trans, add.mechid);
                if (muser == null) {
                    return Result.err(Result.ERR_Policy, "MechID must exist in %s", org.getName());
                }

                // Policy 4: Sponsor must be current
                Identity ouser = muser.responsibleTo();
                if (ouser == null) {
                    return Result.err(Result.ERR_Policy, "%s does not have a current sponsor at %s", add.mechid,
                            org.getName());
                } else if (!ouser.isFound() || ouser.mayOwn() != null) {
                    return Result.err(Result.ERR_Policy, "%s reports that %s cannot be responsible for %s",
                            org.getName(), trans.user());
                }

                // Set Email from most current Sponsor
                email = ouser.email();

                // Policy 5: keep Artifact data current
                if (!ouser.fullID().equals(add.sponsor)) {
                    add.sponsor = ouser.fullID();
                    artiDAO.update(trans, add);
                }

                // Policy 7: Caller must be the MechID or have specifically delegated
                // permissions
                if (!(trans.user().equals(req.value.mechid)
                        || trans.fish(new AAFPermission(mechNS,CERTMAN, ca.getName(), REQUEST)))) {
                    return Result.err(Status.ERR_Denied, "%s must have access to modify x509 certs in NS %s",
                            trans.user(), mechNS);
                }

                // Make sure Primary is the first in fqdns
                if (fqdns.size() > 1) {
                    for (int i = 0; i < fqdns.size(); ++i) {
                        if (primary==null && !ignoreIPs) {
                            trans.error().log("CMService var primary is null");
                        } else {
                            String fg = fqdns.get(i);
                            if (fg!=null && primary!=null && fg.equals(primary.getHostName())) {
                                if (i != 0) {
                                    String tmp = fqdns.get(0);
                                    fqdns.set(0, primary.getHostName());
                                    fqdns.set(i, tmp);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                trans.error().log(e);
                return Result.err(Status.ERR_Denied,
                        "AppID Sponsorship cannot be determined at this time.  Try later.");
            }

            CSRMeta csrMeta;
            try {
                csrMeta = BCFactory.createCSRMeta(ca, req.value.mechid, email, fqdns);
                csrMeta.environment(ca.getEnv());

                // Before creating, make sure they don't have too many
                if(!trans.fish(limitOverridePerm)) {
                    Result<List<CertDAO.Data>> existing = certDAO.readID(trans, req.value.mechid);
                    if(existing.isOK()) {
                        String cn = "CN=" + csrMeta.cn();
                        int count = 0;
                        Date now = new Date();
                        for (CertDAO.Data cdd : existing.value) {
                            Collection<? extends Certificate> certs = Factory.toX509Certificate(cdd.x509);
                            for(Iterator<? extends Certificate> iter = certs.iterator(); iter.hasNext();) {
                                X509Certificate x509 = (X509Certificate)iter.next();
                                if((x509.getNotAfter().after(now) && x509.getSubjectDN().getName().contains(cn))&&(++count>max_509s)) {
                                        break;
                                     }
                            }
                        }
                        if(count>max_509s) {
                            return Result.err(Result.ERR_Denied, "There are too many Certificates generated for " + cn + " for " + req.value.mechid);
                        }
                    }
                }
                // Here is where we send off to CA for Signing.
                X509andChain x509ac = ca.sign(trans, csrMeta);
                if (x509ac == null) {
                    return Result.err(Result.ERR_ActionNotCompleted, "x509 Certificate not signed by CA");
                }
                trans.info().printf("X509 Subject: %s", x509ac.getX509().getSubjectDN());

                X509Certificate x509 = x509ac.getX509();
                CertDAO.Data cdd = new CertDAO.Data();
                cdd.ca = ca.getName();
                cdd.serial = x509.getSerialNumber();
                cdd.id = req.value.mechid;
                cdd.x500 = x509.getSubjectDN().getName();
                cdd.x509 = Factory.toString(trans, x509);

                certDAO.create(trans, cdd);

                CredDAO.Data crdd = new CredDAO.Data();
                crdd.other = Question.random.nextInt();
                crdd.cred = getChallenge256SaltedHash(csrMeta.challenge(), crdd.other);
                crdd.expires = x509.getNotAfter();
                crdd.id = req.value.mechid;
                crdd.ns = Question.domain2ns(crdd.id);
                crdd.type = CredDAO.CERT_SHA256_RSA;
                crdd.tag = cdd.ca + '|' + cdd.serial.toString();
                credDAO.create(trans, crdd);

                CertResp cr = new CertResp(trans, ca, x509, csrMeta, x509ac.getTrustChain(), compileNotes(notes));
                return Result.ok(cr);
            } catch (Exception e) {
                trans.debug().log(e);
                return Result.err(Result.ERR_ActionNotCompleted, e.getMessage());
            }
        } else {
            return Result.err(req);
        }
    }

    public Result<CertResp> renewCert(AuthzTrans trans, Result<CertRenew> renew) {
        if (renew.isOK()) {
            return Result.err(Result.ERR_NotImplemented, "Not implemented yet");
        } else {
            return Result.err(renew);
        }
    }

    public Result<Void> dropCert(AuthzTrans trans, Result<CertDrop> drop) {
        if (drop.isOK()) {
            return Result.err(Result.ERR_NotImplemented, "Not implemented yet");
        } else {
            return Result.err(drop);
        }
    }

    public Result<List<Data>> readCertsByMechID(AuthzTrans trans, String mechID) {
        // Policy 1: To Read, must have NS Read or is Sponsor
        String ns = Question.domain2ns(mechID);
        try {
            if (trans.user().equals(mechID) || trans.fish(new AAFPermission(ns,ACCESS, "*", "read"))
                    || (trans.org().validate(trans, Organization.Policy.OWNS_MECHID, null, mechID)) == null) {
                return certDAO.readID(trans, mechID);
            } else {
                return Result.err(Result.ERR_Denied, "%s is not the ID, Sponsor or NS Owner/Admin for %s at %s",
                        trans.user(), mechID, trans.org().getName());
            }
        } catch (OrganizationException e) {
            return Result.err(e);
        }
    }

    public Result<CertResp> requestPersonalCert(AuthzTrans trans, CA ca) {
        if (ca.inPersonalDomains(trans.getUserPrincipal())) {
            Organization org = trans.org();

            // Policy 1: MechID must be current
            Identity ouser;
            try {
                ouser = org.getIdentity(trans, trans.user());
            } catch (OrganizationException e1) {
                trans.debug().log(e1);
                ouser = null;
            }
            if (ouser == null) {
                return Result.err(Result.ERR_Policy, "Requesting User must exist in %s", org.getName());
            }

            // Set Email from most current Sponsor

            CSRMeta csrMeta;
            try {
                csrMeta = BCFactory.createPersonalCSRMeta(ca, trans.user(), ouser.email());
                X509andChain x509ac = ca.sign(trans, csrMeta);
                if (x509ac == null) {
                    return Result.err(Result.ERR_ActionNotCompleted, "x509 Certificate not signed by CA");
                }
                X509Certificate x509 = x509ac.getX509();
                CertDAO.Data cdd = new CertDAO.Data();
                cdd.ca = ca.getName();
                cdd.serial = x509.getSerialNumber();
                cdd.id = trans.user();
                cdd.x500 = x509.getSubjectDN().getName();
                cdd.x509 = Factory.toString(trans, x509);
                certDAO.create(trans, cdd);

                CertResp cr = new CertResp(trans, ca, x509, csrMeta, x509ac.getTrustChain(), compileNotes(null));
                return Result.ok(cr);
            } catch (Exception e) {
                trans.debug().log(e);
                return Result.err(Result.ERR_ActionNotCompleted, e.getMessage());
            }
        } else {
            return Result.err(Result.ERR_Denied, trans.user(), " not supported for CA", ca.getName());
        }
    }

    ///////////////
    // Artifact
    //////////////
    public Result<Void> createArtifact(AuthzTrans trans, List<ArtiDAO.Data> list) {
        CertmanValidator v = new CertmanValidator().artisRequired(list, 1);
        if (v.err()) {
            return Result.err(Result.ERR_BadData, v.errs());
        }
        for (ArtiDAO.Data add : list) {
            try {
                // Policy 1: MechID must exist in Org
                Identity muser = trans.org().getIdentity(trans, add.mechid);
                if (muser == null) {
                    return Result.err(Result.ERR_Denied, "%s is not valid for %s", add.mechid, trans.org().getName());
                }

                // Policy 2: MechID must have valid Organization Owner
                Identity emailUser;
                if (muser.isPerson()) {
                    emailUser = muser;
                } else {
                    Identity ouser = muser.responsibleTo();
                    if (ouser == null) {
                        return Result.err(Result.ERR_Denied, "%s is not a valid Sponsor for %s at %s", trans.user(),
                                add.mechid, trans.org().getName());
                    }

                    // Policy 3: Calling ID must be MechID Owner
                    if (!trans.user().startsWith(ouser.id())) {
                        return Result.err(Result.ERR_Denied, "%s is not the Sponsor for %s at %s", trans.user(),
                                add.mechid, trans.org().getName());
                    }
                    emailUser = ouser;
                }

                // Policy 4: Renewal Days are between 10 and 60 (constants, may be
                // parameterized)
                if (add.renewDays < MIN_RENEWAL) {
                    add.renewDays = STD_RENEWAL;
                } else if (add.renewDays > MAX_RENEWAL) {
                    add.renewDays = MAX_RENEWAL;
                }

                // Policy 5: If Notify is blank, set to Owner's Email
                if (add.notify == null || add.notify.length() == 0) {
                    add.notify = "mailto:" + emailUser.email();
                }

                // Policy 6: Only do Domain by Exception
                if (add.machine.startsWith("*")) { // Domain set
                    CA ca = certManager.getCA(add.ca);
                    if (!trans.fish(new AAFPermission(ca.getPermNS(),ca.getPermType(), add.ca, DOMAIN))) {
                        return Result.err(Result.ERR_Denied, "Domain Artifacts (%s) requires specific Permission",
                                add.machine);
                    }
                }

                // Set Sponsor from Golden Source
                add.sponsor = emailUser.fullID();

            } catch (OrganizationException e) {
                return Result.err(e);
            }
            // Add to DB
            Result<ArtiDAO.Data> rv = artiDAO.create(trans, add);
            //  come up with Partial Reporting Scheme, or allow only one at a time.
            if (rv.notOK()) {
                return Result.err(rv);
            }
        }
        return Result.ok();
    }

    public Result<List<ArtiDAO.Data>> readArtifacts(AuthzTrans trans, ArtiDAO.Data add) throws OrganizationException {
        CertmanValidator v = new CertmanValidator().keys(add);
        if (v.err()) {
            return Result.err(Result.ERR_BadData, v.errs());
        }
        Result<List<ArtiDAO.Data>> data = artiDAO.read(trans, add);
        if (data.notOKorIsEmpty()) {
            return data;
        }
        add = data.value.get(0);
        if (trans.user().equals(add.mechid)
                || trans.fish(root_read_permission,
                new AAFPermission(add.ns,ACCESS, "*", "read"),
                new AAFPermission(add.ns,CERTMAN, add.ca, "read"),
                new AAFPermission(add.ns,CERTMAN, add.ca, REQUEST))
                || (trans.org().validate(trans, Organization.Policy.OWNS_MECHID, null, add.mechid)) == null) {
            return data;
        } else {
            return Result.err(Result.ERR_Denied,
                    "%s is not %s, is not the sponsor, and doesn't have delegated permission.", trans.user(),
                    add.mechid, add.ns + ".certman|" + add.ca + "|read or ...|request"); // note: reason is set by 2nd
            // case, if 1st case misses
        }

    }

    public Result<List<ArtiDAO.Data>> readArtifactsByMechID(AuthzTrans trans, String mechid)
            throws OrganizationException {
        CertmanValidator v = new CertmanValidator();
        v.nullOrBlank("mechid", mechid);
        if (v.err()) {
            return Result.err(Result.ERR_BadData, v.errs());
        }
        String ns = FQI.reverseDomain(mechid);

        String reason;
        if (trans.fish(new AAFPermission(ns, ACCESS, "*", "read"))
                || (reason = trans.org().validate(trans, Organization.Policy.OWNS_MECHID, null, mechid)) == null) {
            return artiDAO.readByMechID(trans, mechid);
        } else {
            return Result.err(Result.ERR_Denied, reason); // note: reason is set by 2nd case, if 1st case misses
        }

    }

    public Result<List<ArtiDAO.Data>> readArtifactsByMachine(AuthzTrans trans, String machine) {
        CertmanValidator v = new CertmanValidator();
        v.nullOrBlank("machine", machine);
        if (v.err()) {
            return Result.err(Result.ERR_BadData, v.errs());
        }

        //  do some checks?

        return artiDAO.readByMachine(trans, machine);
    }

    public Result<List<ArtiDAO.Data>> readArtifactsByNs(AuthzTrans trans, String ns) {
        CertmanValidator v = new CertmanValidator();
        v.nullOrBlank("ns", ns);
        if (v.err()) {
            return Result.err(Result.ERR_BadData, v.errs());
        }

        //  do some checks?
        return artiDAO.readByNs(trans, ns);
    }

    public Result<Void> updateArtifact(AuthzTrans trans, List<ArtiDAO.Data> list) throws OrganizationException {
        CertmanValidator v = new CertmanValidator();
        v.artisRequired(list, 1);
        if (v.err()) {
            return Result.err(Result.ERR_BadData, v.errs());
        }

        // Check if requesting User is Sponsor
        //  Shall we do one, or multiples?
        for (ArtiDAO.Data add : list) {
            // Policy 1: MechID must exist in Org
            Identity muser = trans.org().getIdentity(trans, add.mechid);
            if (muser == null) {
                return Result.err(Result.ERR_Denied, "%s is not valid for %s", add.mechid, trans.org().getName());
            }

            // Policy 2: MechID must have valid Organization Owner
            Identity ouser = muser.responsibleTo();
            if (ouser == null) {
                return Result.err(Result.ERR_Denied, "%s is not a valid Sponsor for %s at %s", trans.user(), add.mechid,
                        trans.org().getName());
            }

            // Policy 3: Renewal Days are between 10 and 60 (constants, may be
            // parameterized)
            if (add.renewDays < MIN_RENEWAL) {
                add.renewDays = STD_RENEWAL;
            } else if (add.renewDays > MAX_RENEWAL) {
                add.renewDays = MAX_RENEWAL;
            }

            // Policy 4: Data is always updated with the latest Sponsor
            // Add to Sponsor, to make sure we are always up to date.
            add.sponsor = ouser.fullID();

            // Policy 5: If Notify is blank, set to Owner's Email
            if (add.notify == null || add.notify.length() == 0) {
                add.notify = "mailto:" + ouser.email();
            }
            // Policy 6: Only do Domain by Exception
            if (add.machine.startsWith("*")) { // Domain set
                CA ca = certManager.getCA(add.ca);
                if (ca == null) {
                    return Result.err(Result.ERR_BadData, "CA is required in Artifact");
                }
                if (!trans.fish(new AAFPermission(null,ca.getPermType(), add.ca, DOMAIN))) {
                    return Result.err(Result.ERR_Denied, "Domain Artifacts (%s) requires specific Permission",
                            add.machine);
                }
            }

            // Policy 7: only Owner may update info
            if (trans.user().startsWith(ouser.id())) {
                return artiDAO.update(trans, add);
            } else {
                return Result.err(Result.ERR_Denied, "%s may not update info for %s", trans.user(), muser.fullID());
            }
        }
        return Result.err(Result.ERR_BadData, "No Artifacts to update");
    }

    public Result<Void> deleteArtifact(AuthzTrans trans, String mechid, String machine) throws OrganizationException {
        CertmanValidator v = new CertmanValidator();
        v.nullOrBlank("mechid", mechid).nullOrBlank("machine", machine);
        if (v.err()) {
            return Result.err(Result.ERR_BadData, v.errs());
        }

        Result<List<ArtiDAO.Data>> rlad = artiDAO.read(trans, mechid, machine);
        if (rlad.notOKorIsEmpty()) {
            return Result.err(Result.ERR_NotFound, "Artifact for %s %s does not exist.", mechid, machine);
        }

        return deleteArtifact(trans, rlad.value.get(0));
    }

    private Result<Void> deleteArtifact(AuthzTrans trans, ArtiDAO.Data add) throws OrganizationException {
        // Policy 1: Record should be delete able only by Existing Sponsor.
        String sponsor = null;
        Identity muser = trans.org().getIdentity(trans, add.mechid);
        if (muser != null) {
            Identity ouser = muser.responsibleTo();
            if (ouser != null) {
                sponsor = ouser.fullID();
            }
        }
        // Policy 1.a: If Sponsorship is deleted in system of Record, then
        // accept deletion by sponsor in Artifact Table
        if (sponsor == null) {
            sponsor = add.sponsor;
        }

        String ns = FQI.reverseDomain(add.mechid);

        if (trans.fish(new AAFPermission(ns,ACCESS, "*", "write")) || trans.user().equals(sponsor)) {
            return artiDAO.delete(trans, add, false);
        }
        return Result.err(Result.ERR_Denied, "%1 is not allowed to delete this item", trans.user());
    }

    public Result<Void> deleteArtifact(AuthzTrans trans, List<ArtiDAO.Data> list) {
        CertmanValidator v = new CertmanValidator().artisRequired(list, 1);
        if (v.err()) {
            return Result.err(Result.ERR_BadData, v.errs());
        }

        try {
            boolean partial = false;
            Result<Void> result = null;
            for (ArtiDAO.Data add : list) {
                result = deleteArtifact(trans, add);
                if (result.notOK()) {
                    partial = true;
                }
            }
            if (result == null) {
                result = Result.err(Result.ERR_BadData, "No Artifacts to delete");
            } else if (partial) {
                result.partialContent(true);
            }
            return result;
        } catch (Exception e) {
            return Result.err(e);
        }
    }

    private String[] compileNotes(List<String> notes) {
        String[] rv;
        if (notes == null) {
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
