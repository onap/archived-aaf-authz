/*
 * Copyright (C) 2019 Ericsson Software Technology AB. All rights reserved.
 *
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
 * limitations under the License
 */

package org.onap.aaf.auth.cm.cmpv2client;

import static org.onap.aaf.auth.cm.cmpv2client.CmpUtil.createRandomBytes;
import static org.onap.aaf.auth.cm.cmpv2client.CmpUtil.createRandomInt;
import static org.onap.aaf.auth.cm.cmpv2client.CmpUtil.generateOptionalValidity;
import static org.onap.aaf.auth.cm.cmpv2client.CmpUtil.generatePkiHeader;
import static org.onap.aaf.auth.cm.cmpv2client.CmpUtil.generateProofOfPossession;
import static org.onap.aaf.auth.cm.cmpv2client.CmpUtil.generateProtectedBytes;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.cmp.PBMParameter;
import org.bouncycastle.asn1.cmp.PKIBody;
import org.bouncycastle.asn1.cmp.PKIHeader;
import org.bouncycastle.asn1.cmp.PKIMessage;
import org.bouncycastle.asn1.crmf.AttributeTypeAndValue;
import org.bouncycastle.asn1.crmf.CRMFObjectIdentifiers;
import org.bouncycastle.asn1.crmf.CertReqMessages;
import org.bouncycastle.asn1.crmf.CertReqMsg;
import org.bouncycastle.asn1.crmf.CertRequest;
import org.bouncycastle.asn1.crmf.CertTemplateBuilder;
import org.bouncycastle.asn1.crmf.OptionalValidity;
import org.bouncycastle.asn1.crmf.ProofOfPossession;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.onap.aaf.auth.cm.cmpv2client.impl.CmpClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CertificateRequestMessageGenerator {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateRequestMessageGenerator.class);
    private static final AlgorithmIdentifier OWF_ALGORITHM =
        new AlgorithmIdentifier(new ASN1ObjectIdentifier("1.3.14.3.2.26"));
    private static final AlgorithmIdentifier MAC_ALGORITHM =
        new AlgorithmIdentifier(new ASN1ObjectIdentifier("1.2.840.113549.2.9"));
    private static final ASN1ObjectIdentifier PASSWORD_BASED_MAC = new ASN1ObjectIdentifier("1.2.840.113533.7.66.13");
    private final int iterations = createRandomInt(5000);
    private final byte[] salt = createRandomBytes();
    private final int certReqId  = createRandomInt(100000000);

    //Required Parameters
    private X500Name issuerDn;
    private X500Name subjectDn;
    private KeyPair subjectKeyPair;
    private String externalCaAuthenticationPassword;

    //Optional Parameters
    private Date notBefore;
    private Date notAfter;
    private List<String> sansList;

    public static class CertificateRequestMessageGeneratorBuilder{
        //Required Parameters
        private X500Name issuerDn;
        private X500Name subjectDn;
        private KeyPair subjectKeyPair;
        private String externalCaAuthenticationPassword;

        //Optional Parameters
        private Date notBefore;
        private Date notAfter;
        private List<String> sansList;

        /**
         * Constructor for CertificateRequestMessageGeneratorBuilder, which will be used to build a
         * CertificateRequestMessageGenerator, which can be used to generate a cmp certificate request.
         * @param issuerDn Distinguished name of External CA
         * @param subjectDn Distinguished name of the subject sending the request
         * @param subjectKeyPair Key pair associated with the subjectDn
         * @param externalCaAuthenticationPassword password used to authenticate against the external CA
         */
        public CertificateRequestMessageGeneratorBuilder(X500Name issuerDn, X500Name subjectDn,
            KeyPair subjectKeyPair, String externalCaAuthenticationPassword) {
            this.issuerDn = issuerDn;
            this.subjectDn = subjectDn;
            this.subjectKeyPair = subjectKeyPair;
            this.externalCaAuthenticationPassword = externalCaAuthenticationPassword;
        }

        public CertificateRequestMessageGeneratorBuilder setNotBefore(Date notBefore) {
            this.notBefore = notBefore;
            return this;
        }

        public CertificateRequestMessageGeneratorBuilder setNotAfter(Date notAfter) {
            this.notAfter = notAfter;
            return this;
        }

        public CertificateRequestMessageGeneratorBuilder setSansList(List<String> sansList) {
            this.sansList = sansList;
            return this;
        }

        public CertificateRequestMessageGenerator build() {
            return new CertificateRequestMessageGenerator(this);
        }
    }

    private CertificateRequestMessageGenerator(CertificateRequestMessageGeneratorBuilder builder) {
        this.issuerDn = builder.issuerDn;
        this.subjectDn = builder.subjectDn;
        this.subjectKeyPair = builder.subjectKeyPair;
        this.externalCaAuthenticationPassword = builder.externalCaAuthenticationPassword;
        this.notBefore  = builder.notBefore;
        this.notAfter = builder.notAfter;
        this.sansList = builder.sansList;
    }

    /**
    * Generates a PKIMessage from values passed into constructor that can be used to request a certificate.
    * @return Protected PkiMessage containing the certificate request and all pertinent details
    * @throws CmpClientException Wraps several Exceptions into one general-purpose exception.
    */
    public PKIMessage generateCertificateRequestMessage()
        throws CmpClientException {

        LOGGER.info("Beginning process of generating a certificate request message");
        CertTemplateBuilder certTemplateBuilder = new CertTemplateBuilder();
        certTemplateBuilder.setIssuer(issuerDn);
        certTemplateBuilder.setSubject(subjectDn);
        if (notBefore != null || notAfter != null) {
            OptionalValidity optionalValidity = generateOptionalValidity(notBefore, notAfter);
            LOGGER.info("OptionalValidity exists, setting in certificate request");
            certTemplateBuilder.setValidity(optionalValidity);
        }
        certTemplateBuilder.setPublicKey(SubjectPublicKeyInfo.getInstance(subjectKeyPair.getPublic().getEncoded()));
        ExtensionsGenerator extgen = new ExtensionsGenerator();

        LOGGER.info("Reading through list of Subject Alternative Names");
        List<GeneralName> lsan = new ArrayList<>();
        for (String s : sansList) {
            lsan.add(new GeneralName(GeneralName.dNSName,s));
        }
        GeneralName[] sansGeneralNames = new GeneralName[lsan.size()];
        lsan.toArray(sansGeneralNames);
        LOGGER.info("Creating standard Extensions");
        KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment
            | KeyUsage.nonRepudiation);
        try {
            extgen.addExtension(Extension.keyUsage, false, new DERBitString(keyUsage));
            extgen.addExtension(Extension.subjectAlternativeName, false, new GeneralNames(sansGeneralNames));
        } catch (IOException ioe) {
            CmpClientException cmpClientException = new CmpClientException("IOException occurred while adding "
                + "extensions to PKIMessage", ioe);
            LOGGER.error("IOException thrown while adding extensions to PKIMessage");
            throw cmpClientException;
        }

        Extensions exts = extgen.generate();
        certTemplateBuilder.setExtensions(exts);

        CertRequest certRequest = new CertRequest(certReqId,
            certTemplateBuilder.build(), null);

        ProofOfPossession proofOfPossession = generateProofOfPossession(certRequest, subjectKeyPair);
        AttributeTypeAndValue regTokenValue = new AttributeTypeAndValue(CRMFObjectIdentifiers.id_regCtrl_regToken,
            new DERUTF8String(externalCaAuthenticationPassword));
        AttributeTypeAndValue[] avs = {regTokenValue};

        CertReqMsg certReqMsg = new CertReqMsg(certRequest, proofOfPossession, avs);
        CertReqMessages certReqMessages = new CertReqMessages(certReqMsg);

        AlgorithmIdentifier algorithmIdentifier = generateProtectionAlgorithmForCmpRequest();

        PKIHeader pkiHeader = generatePkiHeader(subjectDn, issuerDn, algorithmIdentifier);

        PKIBody pkiBody = new PKIBody(PKIBody.TYPE_INIT_REQ, certReqMessages);
        LOGGER.info("PKIHeader and PKIBody created, adding protection to PKIMessage");
        return protectPkiMessage(externalCaAuthenticationPassword, iterations, salt, pkiHeader, pkiBody);
    }

    /**
     * Generic code to create Algorithm Identifier for protection of PKIMessage.
     * @return Algorithm Identifier
     */
    private AlgorithmIdentifier generateProtectionAlgorithmForCmpRequest() {
        ASN1Integer iteration = new ASN1Integer(iterations);
        DEROctetString derSalt = new DEROctetString(salt);

        PBMParameter pp = new PBMParameter(derSalt, OWF_ALGORITHM, iteration, MAC_ALGORITHM);
        return new AlgorithmIdentifier(PASSWORD_BASED_MAC, pp);
    }

    public int getCertReqId(){
        return certReqId;
    }

    /**
     * Adds protection to the PKIMessage via a specified protection algorithm.
     * @param password password used to authenticate PkiMessage with external CA
     * @param iterations number of times algorithm should be iterated upon
     * @param salt random array of bytes used to help encrypt the algorithm
     * @param pkiHeader Header of PKIMessage containing generic details for any PKIMessage
     * @param pkiBody Body of PKIMessage containing specific details for certificate request
     * @return Protected Pki Message
     * @throws CmpClientException Wraps several Exceptions into one general-purpose exception.
     */
    private PKIMessage protectPkiMessage(String password, int iterations, byte[] salt,
        PKIHeader pkiHeader, PKIBody pkiBody)
        throws CmpClientException {

        byte[] raSecret = password.getBytes();
        byte[] basekey = new byte[raSecret.length + salt.length];
        System.arraycopy(raSecret, 0, basekey, 0, raSecret.length);
        for (int i = 0; i < salt.length; i++) {
            basekey[raSecret.length + i] = salt[i];
        }
        byte[] out;
        try {
            MessageDigest dig = MessageDigest.getInstance(OWF_ALGORITHM.getAlgorithm().getId(),
                BouncyCastleProvider.PROVIDER_NAME);
            for (int i = 0; i < iterations; i++) {
                basekey = dig.digest(basekey);
                dig.reset();
            }
            byte[] protectedBytes = generateProtectedBytes(pkiHeader, pkiBody);
            Mac mac = Mac.getInstance(MAC_ALGORITHM.getAlgorithm().getId(),
                BouncyCastleProvider.PROVIDER_NAME);
            SecretKey key = new SecretKeySpec(basekey, MAC_ALGORITHM.getAlgorithm().getId());
            mac.init(key);
            mac.reset();
            mac.update(protectedBytes, 0, protectedBytes.length);
            out = mac.doFinal();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException ex) {
            CmpClientException cmpClientException = new CmpClientException("Exception occurred while generating "
                + "proof of possession for PKIMessage", ex);
            LOGGER.error("Exception occured while generating the proof of possession for PKIMessage");
            throw cmpClientException;
        }
        DERBitString bs = new DERBitString(out);

        return new PKIMessage(pkiHeader, pkiBody, bs);
    }

}
