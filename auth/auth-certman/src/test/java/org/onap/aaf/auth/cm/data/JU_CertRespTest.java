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
package org.onap.aaf.auth.cm.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.onap.aaf.auth.cm.ca.CA;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.cadi.configure.CertException;
import org.onap.aaf.misc.env.Trans;

public class JU_CertRespTest {

    @Mock
    CSRMeta csrMeta;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    Trans trans;

    @Mock
    X509Certificate x509;

    @Mock
    CA ca;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        CertDrop drop = new CertDrop();
        CertRenew renew = new CertRenew();

        PublicKey publicKey = new PublicKey() {

            @Override
            public String getFormat() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public byte[] getEncoded() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getAlgorithm() {
                // TODO Auto-generated method stub
                return null;
            }
        };
        PrivateKey privateKey = new PrivateKey() {

            @Override
            public String getFormat() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public byte[] getEncoded() {
                // TODO Auto-generated method stub
                return "privatekey".getBytes();
            }

            @Override
            public String getAlgorithm() {
                // TODO Auto-generated method stub
                return null;
            }
        };
        KeyPair keypair = new KeyPair(publicKey, privateKey);

        when(csrMeta.keypair(trans)).thenReturn(keypair);
        when(csrMeta.challenge()).thenReturn("challenge");
        when(x509.getSubjectDN()).thenReturn(null);
        when(x509.getEncoded()).thenReturn("x509Certificate".getBytes());

    }

    @Test
    public void testCertResp() throws IOException, GeneralSecurityException, CertException {
        CertResp resp = new CertResp("CERT");

        assertEquals("CERT", resp.asCertString());
        assertEquals("", resp.challenge());

        String[] trustChain = { "trustChain" };
        String[] notes = { "notes" };

        String[] caIssureDNs = { "caIssuer" };
        String[] trustedCAs = { "trustedCAs" };

        when(ca.getCaIssuerDNs()).thenReturn(caIssureDNs);
        when(ca.getEnv()).thenReturn("Env");
        when(ca.getTrustedCAs()).thenReturn(trustedCAs);

        resp = new CertResp(trans, ca, x509, csrMeta, trustChain, notes);

        assertNotNull(resp.privateString());
        assertEquals("challenge", resp.challenge());
        assertEquals("notes", resp.notes()[0]);
        assertEquals("trustChain", resp.trustChain()[0]);
        assertEquals("caIssuer", resp.caIssuerDNs()[0]);
        assertEquals("trustedCAs", resp.trustCAs()[0]);
        assertEquals("Env", resp.env());
        String cert="<[-----BEGIN CERTIFICATE-----eDUwOUNlcnRpZmljYXRl-----END CERTIFICATE-----]>";
        CertResp resp1=new CertResp(cert);
        assertEquals(cert,resp1.asCertString());
    }
}
