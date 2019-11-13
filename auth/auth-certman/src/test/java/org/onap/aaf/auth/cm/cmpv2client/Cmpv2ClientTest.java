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

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.auth.cm.cert.RDN;
import org.onap.aaf.auth.cm.cmpv2client.impl.CAOfflineException;
import org.onap.aaf.auth.cm.cmpv2client.impl.CmpClientException;
import org.onap.aaf.auth.cm.cmpv2client.impl.CmpClientImpl;
import org.onap.aaf.cadi.configure.CertException;
import org.onap.aaf.misc.env.Trans;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpClients.class, ByteArrayOutputStream.class})
@PowerMockIgnore({"javax.net.ssl.*"})
public class Cmpv2ClientTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private CSRMeta csrMeta;
    private Optional<Date> notBefore;
    private Optional<Date> notAfter;

    @Mock
    Trans trans;

    @Mock
    KeyPairGenerator kpg;

    @Mock
    Certificate cert;

    @Mock
    CloseableHttpClient httpClient;

    @Mock
    CloseableHttpResponse httpResponse;

    @Mock
    HttpEntity httpEntity;


    private static KeyPair keyPair;
    private static ArrayList<RDN> rdns;

    @Rule
    public ExpectedException clientException = ExpectedException.none();

    @Before
      public  void setUp() throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyPairGenerator keyGenerator;
        keyGenerator = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME);
        keyGenerator.initialize(2048);
        keyPair = keyGenerator.generateKeyPair();
        rdns = new ArrayList<>();
        try {
            rdns.add(new RDN("O=CommonCompany"));
        } catch (CertException e) {
            e.printStackTrace();
        }
        initMocks(this);
    }

    @Test
    public void givenValidCsrWhenGenerateCertificateRequestMessageMethodCalledThenValidPkiMessageReturned()
        throws Exception {

        PowerMockito.mockStatic(HttpClients.class);
        Date beforeDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("2019/11/11 12:00:00");
        Date afterDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("2020/11/11 12:00:00");
        setCsrMetaValuesAndDateValues(rdns, "CN=CommonName", "CN=ManagementCA", "CommonName.com", "CommonName@cn.com",
            "password", "http://127.0.0.1/ejbca/publicweb/cmp/cmp", Optional.ofNullable(beforeDate),
            Optional.ofNullable(afterDate));
        when(HttpClients.createDefault()).thenReturn(httpClient);
        when(httpClient.execute(any())).thenReturn(httpResponse);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        BufferedInputStream bis = new BufferedInputStream(
            new FileInputStream("src/test/resources/ReturnedSuccessPKIMessageWithCertificateFile"));
        byte[] ba = IOUtils.toByteArray(bis);
        doAnswer( invocation -> {
            OutputStream os = (ByteArrayOutputStream) invocation.getArguments()[0];
            os.write(ba);
            return null;

        }).when(httpEntity).writeTo(any(OutputStream.class));

        CmpClientImpl cmpClient = new CmpClientImpl();
        Certificate certificate = null;
        try {
            certificate = cmpClient.createCertRequest("data", "RA", csrMeta, cert, notBefore, notAfter);
        } catch (CAOfflineException e) {
            e.printStackTrace();
        }
        assertNull(certificate);
    }

    @Test
    public void givenInvalidCsrWhenGenerateCertificateRequestMessageMethodCalledThenCmpClientExceptionThrown()
        throws CmpClientException, CAOfflineException, ParseException {
        clientException.expect(CmpClientException.class);
        clientException.expectMessage("Before Date is set after the After Date");
        Date beforeDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("2020/11/11 12:00:00");
        Date afterDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("2019/11/11 12:00:00");
        setCsrMetaValuesAndDateValues(rdns, "CN=CommonName", "CN=ManagementCA", "CommonName.com", "CommonName@cn.com",
            "password", "http://127.0.0.1/ejbca/publicweb/cmp/cmp", Optional.ofNullable(beforeDate),
            Optional.ofNullable(afterDate));
        CmpClientImpl cmpClient = new CmpClientImpl();
        cmpClient.createCertRequest("data", "RA", csrMeta, cert, notBefore, notAfter);
    }

    @Test
    public void givenValidCsrWhenGenerateCertificateRequestMessageMethodCalledAndNoServerUpThenCAOfflineException()
        throws CAOfflineException, CmpClientException, IOException, ParseException {
        clientException.expect(CAOfflineException.class);
        clientException.expectMessage("java.net.ConnectException");
        PowerMockito.mockStatic(HttpClients.class);
        Date beforeDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("2019/11/11 12:00:00");
        Date afterDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("2020/11/11 12:00:00");
        setCsrMetaValuesAndDateValues(rdns, "CN=CommonName", "CN=ManagementCA", "CommonName.com", "CommonName@cn.com",
            "password", "http://127.0.0.1/ejbca/publicweb/cmp/cmp", Optional.ofNullable(beforeDate),
            Optional.ofNullable(afterDate));
        when(HttpClients.createDefault()).thenReturn(httpClient);
        when(httpClient.execute(any())).thenThrow(ConnectException.class);
        CmpClientImpl cmpClient = new CmpClientImpl();
        cmpClient.createCertRequest("data", "RA", csrMeta, cert, notBefore, notAfter);
    }

    private void setCsrMetaValuesAndDateValues(List<RDN> rdns, String cn, String issuerCn, String san, String email,
        String password, String externalCaUrl, Optional<Date> notBefore, Optional<Date> notAfter){
        csrMeta = new CSRMeta(rdns);
        csrMeta.cn(cn);
        csrMeta.san(san);
        csrMeta.password(password);
        csrMeta.email(email);
        csrMeta.issuerCn(issuerCn);
        when(kpg.generateKeyPair()).thenReturn(keyPair);
        csrMeta.keypair(trans);
        csrMeta.externalCaUrl(externalCaUrl);

        this.notBefore = notBefore;
        this.notAfter = notAfter;
    }
}
