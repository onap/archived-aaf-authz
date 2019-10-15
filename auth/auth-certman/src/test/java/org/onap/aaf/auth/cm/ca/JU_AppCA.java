/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * <p>
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/

package org.onap.aaf.auth.cm.ca;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.auth.dao.cached.CachedCertDAO;
import org.onap.aaf.cadi.configure.CertException;
import org.onap.aaf.misc.env.Trans;

//TODO: Gabe [JUnit] Import does not exist
@RunWith(MockitoJUnitRunner.class)
public class JU_AppCA {

    @Mock
    private static CachedCertDAO certDAO;

    @Mock
    private static HttpServletRequest req;

    @Mock
    private static CSRMeta csrMeta;

    static Trans trans;

    static X509andChain cert1;
    static byte [] name = {1,23,4,54,6,56};

    private static LocalCA localCA;

    @BeforeClass
    public static void setUp() throws CertificateException, CertException, IOException {
        String str = "core java api";
        byte[] b = str.getBytes();
        Principal prc = new X500Principal("CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US");
        req = mock(HttpServletRequest.class);
        localCA = mock(LocalCA.class);
        X509Certificate cert = new X509Certificate() {
        
            @Override
            public boolean hasUnsupportedCriticalExtension() {
                return false;
            }
        
            @Override
            public Set<String> getNonCriticalExtensionOIDs() {
             
                return null;
            }
        
            @Override
            public byte[] getExtensionValue(String oid) {
             
                return null;
            }
        
            @Override
            public Set<String> getCriticalExtensionOIDs() {
             
                return null;
            }
        
            @Override
            public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException,
                    InvalidKeyException, NoSuchProviderException, SignatureException {
             
            
            }
        
            @Override
            public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException,
                    NoSuchProviderException, SignatureException {
             
            
            }
        
            @Override
            public String toString() {
             
                return null;
            }
        
            @Override
            public PublicKey getPublicKey() {
             
                return null;
            }
        
            @Override
            public byte[] getEncoded() throws CertificateEncodingException {
             
                return null;
            }
        
            @Override
            public int getVersion() {
             
                return 0;
            }
        
            @Override
            public byte[] getTBSCertificate() throws CertificateEncodingException {
             
                return null;
            }
        
            @Override
            public boolean[] getSubjectUniqueID() {
             
                return null;
            }
        
            @Override
            public Principal getSubjectDN() {
             
                return null;
            }
        
            @Override
            public byte[] getSignature() {
             
                return null;
            }
        
            @Override
            public byte[] getSigAlgParams() {
             
                return null;
            }
        
            @Override
            public String getSigAlgOID() {
             
                return null;
            }
        
            @Override
            public String getSigAlgName() {
             
                return null;
            }
        
            @Override
            public BigInteger getSerialNumber() {
             
                return null;
            }
        
            @Override
            public Date getNotBefore() {
             
                return null;
            }
        
            @Override
            public Date getNotAfter() {
             
                return null;
            }
        
            @Override
            public boolean[] getKeyUsage() {
             
                return null;
            }
        
            @Override
            public boolean[] getIssuerUniqueID() {
             
                return null;
            }
        
            @Override
            public Principal getIssuerDN() {
             
                return null;
            }
        
            @Override
            public int getBasicConstraints() {
             
                return 0;
            }
        
            @Override
            public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
             
            
            }
        
            @Override
            public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
            
            }
        };
        X509andChain xac = new X509andChain(cert, new ArrayList<>());
        when(localCA.sign(Mockito.any(Trans.class), Mockito.any(CSRMeta.class))).thenReturn(xac);
        certDAO = mock(CachedCertDAO.class, CALLS_REAL_METHODS);
    }

    @Test
    public void identity_True() throws CertificateException, IOException, CertException {
        assertNotNull(localCA.sign(trans, csrMeta));
    }


    @Test
    public void identityNull() throws CertificateException {
        try {
            assertNotNull(localCA.sign(null, csrMeta));
        } catch (IOException e) {
    
            e.printStackTrace();
        } catch (CertException e) {
        
            e.printStackTrace();
        }
    }

    @Test
    public void identityBothNull() throws CertificateException {
        try {
            assertNotNull(localCA.sign(null, null));
        } catch (IOException e) {
    
            e.printStackTrace();
        } catch (CertException e) {
        
            e.printStackTrace();
        }
    }

}
