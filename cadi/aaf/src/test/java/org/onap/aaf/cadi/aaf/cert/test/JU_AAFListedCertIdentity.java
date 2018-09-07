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

package org.onap.aaf.cadi.aaf.cert.test;

import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.*;
import org.mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.cert.AAFListedCertIdentity;
import org.onap.aaf.cadi.aaf.v2_0.AAFCon;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Chrono;
import org.onap.aaf.misc.rosetta.env.RosettaDF;

import aaf.v2_0.Certs;
import aaf.v2_0.Certs.Cert;
import aaf.v2_0.Users;
import aaf.v2_0.Users.User;

public class JU_AAFListedCertIdentity {

    @Mock private AAFCon<?> conMock;
    @Mock private Rcli<Object> rcliMock;
    @Mock private RosettaDF<Users> userDFMock;
    @Mock private RosettaDF<Certs> certDFMock;
    @Mock private Future<Users> futureUsersMock;
    @Mock private Future<Certs> futureCertsMock;

    @Mock private Users usersMock;
    @Mock private User userMock1;
    @Mock private User userMock2;
    @Mock private User userMock3;

    @Mock private Certs certsMock;
    @Mock private Cert certMock1;
    @Mock private Cert certMock2;
    @Mock private Cert certMock3;

    @Mock private HttpServletRequest reqMock;
    @Mock private X509Certificate x509Mock;

    private List<User> usersList;
    private List<Cert> certsList;

    private PropAccess access;

    private ByteArrayOutputStream outStream;

    private static final String USERS = "user1,user2,user3";
    private static final String ID = "id";
    private static final String FINGERPRINT = "fingerprint";

    private static final byte[] certBytes = "certificate".getBytes();

    @Before
    public void setup() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        MockitoAnnotations.initMocks(this);

        certsList = new ArrayList<>();
        certsList.add(certMock1);
        certsList.add(certMock2);
        certsList.add(certMock3);

        usersList = new ArrayList<>();
        usersList.add(userMock1);
        usersList.add(userMock2);
        usersList.add(userMock3);

        outStream = new ByteArrayOutputStream();
        access = new PropAccess(new PrintStream(outStream), new String[0]);
        outStream.reset();
        access.setProperty(Config.AAF_CERT_IDS, USERS);
        setFinal(conMock, conMock.getClass().getField("usersDF"), userDFMock);
        setFinal(conMock, conMock.getClass().getField("certsDF"), certDFMock);
        setFinal(conMock, conMock.getClass().getField("access"), access);
    }

    @Test
    public void test() throws APIException, CadiException, CertificateException {
        doReturn(rcliMock).when(conMock).client(Config.AAF_DEFAULT_VERSION);
        when(rcliMock.read("/authz/users/perm/com.att.aaf.trust/tguard/authenticate", Users.class, userDFMock)).thenReturn(futureUsersMock);
        when(rcliMock.read("/authz/users/perm/com.att.aaf.trust/basicAuth/authenticate", Users.class, userDFMock)).thenReturn(futureUsersMock);
        when(rcliMock.read("/authz/users/perm/com.att.aaf.trust/csp/authenticate", Users.class, userDFMock)).thenReturn(futureUsersMock);

        when(futureUsersMock.get(5000)).thenReturn(true);
        futureUsersMock.value = usersMock;
        when(usersMock.getUser()).thenReturn(usersList);

        when(rcliMock.read("/authn/cert/id/user1", Certs.class, conMock.certsDF)).thenReturn(futureCertsMock);
        when(rcliMock.read("/authn/cert/id/user2", Certs.class, conMock.certsDF)).thenReturn(futureCertsMock);
        when(rcliMock.read("/authn/cert/id/user3", Certs.class, conMock.certsDF)).thenReturn(futureCertsMock);

        when(futureCertsMock.get(5000)).thenReturn(true);
        futureCertsMock.value = certsMock;
        when(certsMock.getCert()).thenReturn(certsList);

        when(userMock1.getId()).thenReturn("user1");
        when(userMock2.getId()).thenReturn("user2");
        when(userMock3.getId()).thenReturn("user3");

        prepareCert(certMock1);
        prepareCert(certMock2);
        prepareCert(certMock3);

        AAFListedCertIdentity certID = new AAFListedCertIdentity(access, conMock);

        when(x509Mock.getEncoded()).thenReturn(certBytes);
        certID.identity(reqMock, null, null);
        certID.identity(reqMock, null, certBytes);
        certID.identity(reqMock, x509Mock, null);
        certID.identity(reqMock, x509Mock, certBytes);

        Set<String> hashSetOfUsers = AAFListedCertIdentity.trusted("basicAuth");
        assertThat(hashSetOfUsers.contains("user1"), is(true));
        assertThat(hashSetOfUsers.contains("user2"), is(true));
        assertThat(hashSetOfUsers.contains("user3"), is(true));

    }

    private void setFinal(Object object, Field field, Object newValue) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & Modifier.FINAL);

        field.set(object, newValue);
    }

    private void prepareCert(Cert cert) {
        Date date = new Date();
        when(cert.getExpires()).thenReturn(Chrono.timeStamp(new Date(date.getTime() + (60 * 60 * 24))));
        when(cert.getId()).thenReturn(ID);
        when(cert.getFingerprint()).thenReturn(FINGERPRINT.getBytes());
    }

}
