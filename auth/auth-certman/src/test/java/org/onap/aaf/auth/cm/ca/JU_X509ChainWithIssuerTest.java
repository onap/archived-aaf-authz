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

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.io.Reader;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.cadi.configure.CertException;

public class JU_X509ChainWithIssuerTest {

    @Mock
    X509Certificate x509;

    @Mock
    X509ChainWithIssuer orig;
    @Mock
    Principal subject;
    @Mock
    Reader reader;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        // when(subject.get)
        when(x509.getSubjectDN()).thenReturn(subject);
        when(x509.getEncoded()).thenReturn("x509".getBytes());
    }

    @Test
    public void test() throws IOException, CertException {
        X509ChainWithIssuer x509Chain = new X509ChainWithIssuer(orig, x509);

        assertNull(x509Chain.getIssuerDN());
        Certificate[] certs = { x509 };
        // Certificate cert = ;
        x509Chain = new X509ChainWithIssuer(certs);
        List<Reader> rdrs = new ArrayList<Reader>();
        rdrs.add(null);

        x509Chain = new X509ChainWithIssuer(rdrs);
    }

}
