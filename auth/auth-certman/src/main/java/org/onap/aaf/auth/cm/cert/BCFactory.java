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
package org.onap.aaf.auth.cm.cert;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.List;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.onap.aaf.auth.cm.ca.CA;
import org.onap.aaf.auth.cm.validation.CertmanValidator;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.configure.CertException;
import org.onap.aaf.cadi.configure.Factory;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;


/**
 * Additional Factory mechanisms for CSRs, and BouncyCastle.  The main Factory
 * utilizes only Java abstractions, and is useful in Client code.
 *
 * @author JonathanGathman
 *
 */
public class BCFactory extends Factory {
    private static final JcaContentSignerBuilder jcsb;


    static {
        // Bouncy
        jcsb = new JcaContentSignerBuilder(Factory.SIG_ALGO);
    }

    public static ContentSigner contentSigner(PrivateKey pk) throws OperatorCreationException {
        return jcsb.build(pk);
    }

    public static String toString(PKCS10CertificationRequest csr) throws IOException, CertException {
        if (csr==null) {
            throw new CertException("x509 Certificate Request not built");
        }
        return textBuilder("CERTIFICATE REQUEST",csr.getEncoded());
    }

    public static PKCS10CertificationRequest toCSR(Trans trans, File file) throws IOException {
        TimeTaken tt = trans.start("Reconstitute CSR", Env.SUB);
        try {
            FileReader fr = new FileReader(file);
            return new PKCS10CertificationRequest(decode(strip(fr)));
        } finally {
            tt.done();
        }
    }

    public static byte[] sign(Trans trans, ASN1Object toSign, PrivateKey pk) throws IOException, InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        TimeTaken tt = trans.start("Encode Security Object", Env.SUB);
        try {
            return sign(trans,toSign.getEncoded(),pk);
        } finally {
            tt.done();
        }
    }

    public static CSRMeta createCSRMeta(CA ca, String mechid, String sponsorEmail, List<String> fqdns) throws CertException {
        CSRMeta csr = ca.newCSRMeta();
        boolean first = true;
        // Set CN (and SAN)
        for (String fqdn : fqdns) {
            if (first) {
                first = false;
                csr.cn(fqdn);
            }
            csr.san(fqdn); // duplicate CN in SAN, per RFC 5280 section 4.2.1.6
        }

        csr.challenge(new String(Symm.randomGen(24)));
        csr.mechID(mechid);
        csr.email(sponsorEmail);
        String errs;
        if ((errs=validateApp(csr))!=null) {
            throw new CertException(errs);
        }
        return csr;
    }

    private static String validateApp(CSRMeta csr) {
        CertmanValidator v = new CertmanValidator();
        if (v.nullOrBlank("cn", csr.cn())
            .nullOrBlank("mechID", csr.mechID())
//            .nullOrBlank("email", csr.email())
            .err()) {
            return v.errs();
        } else {
            return null;
        }
    }

    public static CSRMeta createPersonalCSRMeta(CA ca, String personal, String email) throws CertException {
        CSRMeta csr = ca.newCSRMeta();
        csr.cn(personal);
        csr.challenge(new String(Symm.randomGen(24)));
        csr.email(email);
        String errs;
        if ((errs=validatePersonal(csr))!=null) {
            throw new CertException(errs);
        }
        return csr;
    }

    private static String validatePersonal(CSRMeta csr) {
        CertmanValidator v = new CertmanValidator();
        if (v.nullOrBlank("cn", csr.cn())
            .nullOrBlank("email", csr.email())
            .err()) {
            return v.errs();
        } else {
            return null;
        }
    }


}
