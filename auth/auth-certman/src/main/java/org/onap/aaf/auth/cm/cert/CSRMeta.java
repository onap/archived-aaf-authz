/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2019 IBM.
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
package org.onap.aaf.auth.cm.cert;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.onap.aaf.cadi.configure.CertException;
import org.onap.aaf.cadi.configure.Factory;
import org.onap.aaf.misc.env.Trans;

public class CSRMeta {
    private String cn;
    private String mechID;
    private String environment;
    private String email;
    private String challenge;
    private List<RDN> rdns;
    private ArrayList<String> sanList = new ArrayList<>();
    private KeyPair keyPair;
    private X500Name name = null;
    private SecureRandom random = new SecureRandom();

    public CSRMeta(List<RDN> rdns) {
        this.rdns = rdns;
    }

    public X500Name x500Name() {
        if (name==null) {
            X500NameBuilder xnb = new X500NameBuilder();
            xnb.addRDN(BCStyle.CN,cn);
            xnb.addRDN(BCStyle.E,email);
            if (mechID!=null) {
                if (environment==null) {
                    xnb.addRDN(BCStyle.OU,mechID);
                } else {
                    xnb.addRDN(BCStyle.OU,mechID+':'+environment);
                }
            }
            for (RDN rdn : rdns) {
                xnb.addRDN(rdn.aoi,rdn.value);
            }
            name = xnb.build();
        }
        return name;
    }


    public PKCS10CertificationRequest  generateCSR(Trans trans) throws IOException, CertException {
        PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(x500Name(),keypair(trans).getPublic());
        if (challenge!=null) {
            DERPrintableString password = new DERPrintableString(challenge);
            builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_challengePassword, password);
        }

        int plus = email==null?0:1;
        if (!sanList.isEmpty()) {
            GeneralName[] gna = new GeneralName[sanList.size()+plus];
            int i=-1;
            for (String s : sanList) {
                gna[++i]=new GeneralName(GeneralName.dNSName,s);
            }
            gna[++i]=new GeneralName(GeneralName.rfc822Name,email);

            builder.addAttribute(
                    PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
                    new Extensions(new Extension[] {
                            new Extension(Extension.subjectAlternativeName,false,new GeneralNames(gna).getEncoded())
                    })
            );
        }

        try {
            return builder.build(BCFactory.contentSigner(keypair(trans).getPrivate()));
        } catch (OperatorCreationException e) {
            throw new CertException(e);
        }
    }

    @SuppressWarnings("deprecation")
    public static void dump(PKCS10CertificationRequest csr) {
         Attribute[] certAttributes = csr.getAttributes();
         for (Attribute attribute : certAttributes) {
             if (!attribute.getAttrType().equals(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
                     continue;
                 }

                 Extensions extensions = Extensions.getInstance(attribute.getAttrValues().getObjectAt(0));
                 GeneralNames gns = GeneralNames.fromExtensions(extensions,Extension.subjectAlternativeName);
                 GeneralName[] names = gns.getNames();
                 for (int k=0; k < names.length; k++) {
                         String title = "";
                         if (names[k].getTagNo() == GeneralName.dNSName) {
                                 title = "dNSName";
                         } else if (names[k].getTagNo() == GeneralName.iPAddress) {
                                 title = "iPAddress";
                                 // Deprecated, but I don't see anything better to use.
                                 names[k].toASN1Object();
                         } else if (names[k].getTagNo() == GeneralName.otherName) {
                                 title = "otherName";
                         } else if (names[k].getTagNo() == GeneralName.rfc822Name) {
                                 title = "email";
                         }

                         System.out.println(title + ": "+ names[k].getName());
                 }
         }
    }

    public X509Certificate initialConversationCert(Trans trans) throws CertificateException, OperatorCreationException {
        GregorianCalendar gc = new GregorianCalendar();
        Date start = gc.getTime();
        gc.add(GregorianCalendar.DAY_OF_MONTH,2);
        Date end = gc.getTime();
        @SuppressWarnings("deprecation")
        X509v3CertificateBuilder xcb = new X509v3CertificateBuilder(
                x500Name(),
                new BigInteger(12,random), // replace with Serialnumber scheme
                start,
                end,
                x500Name(),
                new SubjectPublicKeyInfo(ASN1Sequence.getInstance(keypair(trans).getPublic().getEncoded()))
                );
        return new JcaX509CertificateConverter().getCertificate(
                xcb.build(BCFactory.contentSigner(keypair(trans).getPrivate())));
    }

    public CSRMeta san(String v) {
        sanList.add(v);
        return this;
    }

    public List<String> sans() {
        return sanList;
    }


    public KeyPair keypair(Trans trans) {
        if (keyPair == null) {
            keyPair = Factory.generateKeyPair(trans);
        }
        return keyPair;
    }

    /**
     * @return the cn
     */
    public String cn() {
        return cn;
    }


    /**
     * @param cn the cn to set
     */
    public void cn(String cn) {
        this.cn = cn;
    }

    /**
     * Environment of Service MechID is good for
     */
    public void environment(String env) {
        environment = env;
    }

    /**
     *
     * @return
     */
    public String environment() {
        return environment;
    }

    /**
     * @return the mechID
     */
    public String mechID() {
        return mechID;
    }


    /**
     * @param mechID the mechID to set
     */
    public void mechID(String mechID) {
        this.mechID = mechID;
    }


    /**
     * @return the email
     */
    public String email() {
        return email;
    }


    /**
     * @param email the email to set
     */
    public void email(String email) {
        this.email = email;
    }

    /**
     * @return the challenge
     */
    public String challenge() {
        return challenge;
    }


    /**
     * @param challenge the challenge to set
     */
    public void challenge(String challenge) {
        this.challenge = challenge;
    }

}
