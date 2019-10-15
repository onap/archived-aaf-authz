/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.aaf.cadi.http.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.X509KeyManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HX509SS;
import org.onap.aaf.misc.env.APIException;

public class JU_HX509SS {

    @Mock X509Certificate x509Mock;
    @Mock X509KeyManager keyManagerMock;
    @Mock PrivateKey privateKeyMock;
    @Mock SecurityInfoC<HttpURLConnection> siMock;
    @Mock HttpURLConnection hucMock;
    @Mock HttpsURLConnection hucsMock;

    private final static String alias = "Some alias";
    private final static String algName = "Some algName";
    private final static byte[] publicKeyBytes = "a public key".getBytes();

    private PropAccess access;
    private SecurityInfoC<HttpURLConnection> si;

    @Before
    public void setup() throws IOException, CadiException, CertificateEncodingException {
        MockitoAnnotations.initMocks(this);
    
        when(x509Mock.getSigAlgName()).thenReturn(algName);
        when(x509Mock.getEncoded()).thenReturn(publicKeyBytes);
    
        when(keyManagerMock.getCertificateChain(alias)).thenReturn(new X509Certificate[] {x509Mock});
        when(keyManagerMock.getPrivateKey(alias)).thenReturn(privateKeyMock);

        when(siMock.getKeyManagers()).thenReturn(new X509KeyManager[] {keyManagerMock});
    
        access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
        access.setProperty(Config.CADI_ALIAS, alias);
        // si = SecurityInfoC.instance(access, HttpURLConnectionStub.class);
    }

    @Test
    public void test() throws APIException, CadiException {
        HX509SS x509 = new HX509SS(alias, siMock);
        assertThat(x509.getID(), is(alias));
        assertThat(x509.setLastResponse(0), is(0));
        assertThat(x509.setLastResponse(1), is(0));
        assertThat(x509.setLastResponse(2), is(0));
    
        // coverage...
        x509.setSecurity(hucMock);
        x509.setSecurity(hucsMock);
    }

    // TODO: Test the setSecurity method - Ian
    // @Test
    // public void test2() throws APIException, CadiException {
        // HX509SS x509 = new HX509SS(si, false);
        // x509.setSecurity(hucMock);
        // x509.setSecurity(hucsMock);
    // }

    @Test(expected = APIException.class)
    public void throws1Test() throws APIException, CadiException {
        @SuppressWarnings("unused")
        HX509SS x509 = new HX509SS(siMock);
    }

    @Test(expected = APIException.class)
    public void throws3Test() throws APIException, CadiException {
        when(keyManagerMock.getCertificateChain(alias)).thenReturn(new X509Certificate[0]);
        @SuppressWarnings("unused")
        HX509SS x509 = new HX509SS(alias, siMock);
    }

}
