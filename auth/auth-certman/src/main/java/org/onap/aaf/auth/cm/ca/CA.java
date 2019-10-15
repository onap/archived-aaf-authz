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
package org.onap.aaf.auth.cm.ca;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bouncycastle.asn1.x500.style.BCStyle;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.auth.cm.cert.RDN;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.configure.CertException;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Split;

public abstract class CA {
    private static final String MUST_EXIST_TO_CREATE_CSRS_FOR = " must exist to create CSRs for ";
    //TODO figuring out what is an Issuing CA is a matter of convention.  Consider SubClassing for Open Source
    public static final String ISSUING_CA = "Issuing CA";
    public static final String CM_CA_PREFIX = "cm_ca.";
    public static final String CM_CA_BASE_SUBJECT = ".baseSubject";
    public static final String CM_CA_ENV_TAG = ".env_tag";
    protected static final String CM_PUBLIC_DIR = "cm_public_dir";
    private static final String CM_TRUST_CAS = "cm_trust_cas";
    protected static final String CM_BACKUP_CAS = "cm_backup_cas";

    public static final Set<String> EMPTY = Collections.unmodifiableSet(new HashSet<>());


    private final String name;
    private final String env;
    private MessageDigest messageDigest;
    private final String permNS;
    private final String permType;
    private final ArrayList<String> idDomains;
    private String[] trustedCAs;
    private String[] caIssuerDNs;
    private List<RDN> rdns;
    private final boolean env_tag;


    protected CA(Access access, String caName, String env) throws IOException, CertException {
        trustedCAs = new String[4]; // starting array
        this.name = caName;
        this.env = env;
        this.env_tag = env==null || env.isEmpty()?false:
                Boolean.parseBoolean(access.getProperty(CM_CA_ENV_TAG, Boolean.FALSE.toString()));
        permNS=null;
        String prefix = CM_CA_PREFIX + name;
        permType = access.getProperty(prefix + ".perm_type",null);
        if (permType==null) {
            throw new CertException(prefix + ".perm_type" + MUST_EXIST_TO_CREATE_CSRS_FOR + caName);
        }
        caIssuerDNs = Split.splitTrim(':', access.getProperty(Config.CADI_X509_ISSUERS, null));

        String tag = CA.CM_CA_PREFIX+caName+CA.CM_CA_BASE_SUBJECT;

        String fields = access.getProperty(tag, null);
        if (fields==null) {
            throw new CertException(tag + MUST_EXIST_TO_CREATE_CSRS_FOR + caName);
        }
        access.log(Level.INFO, tag, "=",fields);
        rdns = RDN.parse('/',fields);
        for (RDN rdn : rdns) {
            if (rdn.aoi==BCStyle.EmailAddress) { // Cert Specs say Emails belong in Subject
                throw new CertException("email address is not allowed in " + CM_CA_BASE_SUBJECT);
            }
        }

        idDomains = new ArrayList<>();
        StringBuilder sb = null;
        for (String s : Split.splitTrim(',', access.getProperty(CA.CM_CA_PREFIX+caName+".idDomains", ""))) {
            if (s.length()>0) {
                if (sb==null) {
                    sb = new StringBuilder();
                } else {
                    sb.append(", ");
                }
                idDomains.add(s);
                sb.append(s);
            }
        }
        if (sb!=null) {
            access.printf(Level.INIT, "CA '%s' supports Personal Certificates for %s", caName, sb);
        }

        String dataDir = access.getProperty(CM_PUBLIC_DIR,null);
        if (dataDir!=null) {
            File data = new File(dataDir);
            byte[] bytes;
            if (data.exists()) {
                String trustCas = access.getProperty(CM_TRUST_CAS,null);
                if (trustCas!=null) {
                    for (String fname : Split.splitTrim(',', trustCas)) {
                        File crt;
                        if (fname.contains("/")) {
                            crt = new File(fname);
                        } else {
                            crt = new File(data,fname);
                        }
                        if (crt.exists()) {
                            access.printf(Level.INIT, "Loading CA Cert from %s", crt.getAbsolutePath());
                            bytes = new byte[(int)crt.length()];
                            FileInputStream fis = new FileInputStream(crt);
                            try {
                                int read = fis.read(bytes);
                                if (read>0) {
                                    addTrustedCA(new String(bytes));
                                }
                            } finally {
                                fis.close();
                            }
                        } else {
                            access.printf(Level.INIT, "FAILED to Load CA Cert from %s", crt.getAbsolutePath());
                        }
                    }
                } else {
                    access.printf(Level.INIT, "Cannot load external TRUST CAs: No property %s",CM_TRUST_CAS);
                }
            } else {
                access.printf(Level.INIT, "Cannot load external TRUST CAs: %s doesn't exist, or is not accessible",data.getAbsolutePath());
            }
        }
    }

    protected void addCaIssuerDN(String issuerDN) {
        boolean changed = true;
        for (String id : caIssuerDNs) {
            if (id.equals(issuerDN)) {
                changed = false;
                break;
            }
        }
        if (changed) {
            String[] newsa = new String[caIssuerDNs.length+1];
            newsa[0]=issuerDN;
            System.arraycopy(caIssuerDNs, 0, newsa, 1, caIssuerDNs.length);
            caIssuerDNs = newsa;
        }
    }

    protected synchronized void addTrustedCA(final String crtString) {
        String crt;
        if (crtString.endsWith("\n")) {
            crt = crtString;
        } else {
            crt = crtString + '\n';
        }
        for (int i=0;i<trustedCAs.length;++i) {
            if (trustedCAs[i]==null) {
                trustedCAs[i]=crt;
                return;
            }
        }
        String[] temp = new String[trustedCAs.length+5];
        System.arraycopy(trustedCAs,0,temp, 0, trustedCAs.length);
        temp[trustedCAs.length]=crt;
        trustedCAs = temp;
    }

    public String[] getCaIssuerDNs() {
        return caIssuerDNs;
    }

    public String[] getTrustedCAs() {
        return trustedCAs;
    }

    public boolean shouldAddEnvTag() {
        return env_tag;
    }

    public String getEnv() {
        return env;
    }

    protected void setMessageDigest(MessageDigest md) {
        messageDigest = md;
    }

    /*
     * End Required Constructor calls
     */

    public String getName() {
        return name;
    }


    public String getPermNS() {
        return permNS;
    }

    public String getPermType() {
        return permType;
    }

    public abstract X509andChain sign(Trans trans, CSRMeta csrmeta) throws IOException, CertException;

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.cm.ca.CA#inPersonalDomains(java.security.Principal)
     */
    public boolean inPersonalDomains(Principal p) {
        int at = p.getName().indexOf('@');
        if (at>=0) {
            return idDomains.contains(p.getName().substring(at+1));
        } else {
            return false;
        }
    }

    public MessageDigest messageDigest() {
        return messageDigest;
    }

    public CSRMeta newCSRMeta() {
        return new CSRMeta(rdns);
    }

}
