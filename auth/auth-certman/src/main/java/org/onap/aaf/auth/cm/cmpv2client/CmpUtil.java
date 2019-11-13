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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.cmp.PKIBody;
import org.bouncycastle.asn1.cmp.PKIHeader;
import org.bouncycastle.asn1.cmp.PKIHeaderBuilder;
import org.bouncycastle.asn1.crmf.CertRequest;
import org.bouncycastle.asn1.crmf.CertTemplateBuilder;
import org.bouncycastle.asn1.crmf.OptionalValidity;
import org.bouncycastle.asn1.crmf.POPOSigningKey;
import org.bouncycastle.asn1.crmf.ProofOfPossession;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.onap.aaf.auth.cm.cmpv2client.impl.CmpClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CmpUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmpUtil.class);
    private static final SecureRandom secureRandom = new SecureRandom();

    private CmpUtil(){

    }

    /**
     * Validates specified object reference is not null.
     *
     * @param argument T - the type of the reference.
     * @param message  message - detail message to be used in the event that a NullPointerException is thrown.
     * @return The Object if not null
     */
    public static <T> T notNull(T argument, String message) {
        return Objects.requireNonNull(argument, message + " must not be null");
    }

    /**
     * Validates String object reference is not null and not empty.
     *
     * @param stringArg String Object that need to be validated.
     * @return boolean
     */
    public static boolean isNullOrEmpty(String stringArg) {
        return (stringArg != null && !stringArg.trim().isEmpty());
    }

    /**
     * Creates a random number than can be used for sendernonce, transactionId and salts.
     * @return bytes containing a random number string representing a nonce
     */
    static byte[] createRandomBytes() {
        LOGGER.info("Generating random array of bytes");
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        return randomBytes;
    }

    /**
     * Creates a random integer than can be used to represent a transactionId or determine the number iterations
     * in a protection algorithm.
     * @return bytes containing a random number string representing a nonce
     */
    static int createRandomInt(int range) {
        LOGGER.info("Generating random integer");
        return secureRandom.nextInt(range) + 1000;
    }

    /**
     * Generates protected bytes of a combined PKIHeader and PKIBody.
     * @param header Header of PKIMessage containing common parameters
     * @param body Body of PKIMessage containing specific information for message
     * @return bytes representing the PKIHeader and PKIBody thats to be protected
     */
    static byte[] generateProtectedBytes(PKIHeader header, PKIBody body) throws CmpClientException {
        LOGGER.info("Generating array of bytes representing PkiHeader and PkiBody");
        byte[] res;
        ASN1EncodableVector vector = new ASN1EncodableVector();
        vector.add(header);
        vector.add(body);
        ASN1Encodable protectedPart = new DERSequence(vector);
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            DEROutputStream out = new DEROutputStream(bao);
            out.writeObject(protectedPart);
            res = bao.toByteArray();
        } catch (IOException ioe) {
            CmpClientException cmpClientException = new CmpClientException(
                "IOException occurred while creating protectedBytes", ioe);
            LOGGER.error("IOException occurred while creating protectedBytes");
            throw cmpClientException;
        }
        return res;
    }

    /**
     * Generates a PKIHeader Builder object.
     * @param subjectDn distinguished name of Subject
     * @param issuerDn distinguished name of external CA
     * @param protectionAlg protection Algorithm used to protect PKIMessage
     * @return PKIHeaderBuilder
     */
    static PKIHeader generatePkiHeader(X500Name subjectDn, X500Name issuerDn,
        AlgorithmIdentifier protectionAlg) {
        LOGGER.info("Generating a Pki Header Builder");
        PKIHeaderBuilder pkiHeaderBuilder = new PKIHeaderBuilder(
            PKIHeader.CMP_2000, new GeneralName(subjectDn), new GeneralName(issuerDn));

        pkiHeaderBuilder.setMessageTime(new ASN1GeneralizedTime(new Date()));
        pkiHeaderBuilder.setSenderNonce(new DEROctetString(createRandomBytes()));
        pkiHeaderBuilder.setTransactionID(new DEROctetString(createRandomBytes()));
        pkiHeaderBuilder.setProtectionAlg(protectionAlg);

        return pkiHeaderBuilder.build();
    }

    /**
     * Generates Proof of Possession that can be passed alongside the certificate request.
     * @param certRequest Certificate request that requires proof of possession
     * @param keypair keypair associated with the subject sending the certificate request
     * @return ProofOfPossession associated with the certificate request
     * @throws CmpClientException Wraps several Exceptions into one general-purpose exception.
     */
    static ProofOfPossession generateProofOfPossession(CertRequest certRequest, KeyPair keypair)
        throws CmpClientException {

        LOGGER.info("Generating proof of possession from certficate request");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DEROutputStream mout = new DEROutputStream(baos);
        ProofOfPossession proofOfPossession;

        try {
            mout.writeObject(certRequest);
            mout.close();

            byte[] popoProtectionBytes = baos.toByteArray();
            final String sigalg = PKCSObjectIdentifiers.sha256WithRSAEncryption.getId();
            final Signature signature = Signature.getInstance(sigalg, BouncyCastleProvider.PROVIDER_NAME);
            signature.initSign(keypair.getPrivate());
            signature.update(popoProtectionBytes);
            DERBitString bs = new DERBitString(signature.sign());

            proofOfPossession = new ProofOfPossession(new POPOSigningKey(null,
                new AlgorithmIdentifier(new ASN1ObjectIdentifier(sigalg)), bs));
        } catch (IOException | NoSuchProviderException | NoSuchAlgorithmException | InvalidKeyException
            | SignatureException ex) {
            CmpClientException cmpClientException = new CmpClientException("Exception occurred while creating proof "
                + "of possession for PKIMessage", ex);
            LOGGER.error("Exception occurred while creating proof of possession for PKIMessage");
            throw cmpClientException;
        }
        return proofOfPossession;
    }

    /**
     * creates an Optional Validity, which is used to specify how long the returned cert should be valid for.
     *
     * @param notBefore Date specifying certificate is not valid before this date
     * @param notAfter  Date specifying certificate is not valid after this date
     * @return OptionalValidity that can be set for certificate on external CA
     */
    static OptionalValidity generateOptionalValidity(final Optional<Date> notBefore, final Optional<Date> notAfter) {
        LOGGER.info("Generating Optional Validity from Date objects");
        ASN1EncodableVector optionalValidityV = new ASN1EncodableVector();
        if (notBefore.isPresent()) {
            Time nb = new Time(notBefore.get());
            optionalValidityV.add(new DERTaggedObject(true, 0, nb));
        }
        if (notAfter.isPresent()) {
            Time na = new Time(notAfter.get());
            optionalValidityV.add(new DERTaggedObject(true, 1, na));
        }
        return OptionalValidity.getInstance(new DERSequence(optionalValidityV));
    }
}
