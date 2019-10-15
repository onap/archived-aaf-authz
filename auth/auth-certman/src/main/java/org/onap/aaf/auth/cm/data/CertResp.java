/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2018 IBM.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.cm.data;

import java.io.IOException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

import org.onap.aaf.auth.cm.ca.CA;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.cadi.configure.CertException;
import org.onap.aaf.cadi.configure.Factory;
import org.onap.aaf.misc.env.Trans;

public class CertResp {
    private CA ca;
    private KeyPair keyPair;
    private String challenge;

    private String privateKey;
    private String certString;
    private String[] trustChain;
    private String[] notes;

    public CertResp(Trans trans, CA ca, X509Certificate x509, CSRMeta csrMeta, String[] trustChain, String[] notes) throws IOException, CertException {
        keyPair = csrMeta.keypair(trans);
        privateKey = Factory.toString(trans, keyPair.getPrivate());
        certString = Factory.toString(trans,x509);
        challenge=csrMeta.challenge();
        this.ca = ca;
        this.trustChain = trustChain;
        this.notes = notes;
    }

    // Use for Read Responses, etc
    public CertResp(String cert) {
        certString = cert;
    }


    public String asCertString() {
        return certString;
    }

    public String privateString() {
        return privateKey;
    }

    public String challenge() {
        return challenge==null?"":challenge;
    }

    public String[] notes() {
        return notes;
    }

    public String[] caIssuerDNs() {
        return ca.getCaIssuerDNs();
    }

    public String env() {
        return ca.getEnv();
    }

    public String[] trustChain() {
        return trustChain;
    }

    public String[] trustCAs() {
        return ca.getTrustedCAs();
    }
}
