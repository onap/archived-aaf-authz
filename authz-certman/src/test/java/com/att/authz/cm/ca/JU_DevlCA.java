/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package com.att.authz.cm.ca;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

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
import java.util.Date;
import java.util.Set;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.att.aft.dme2.api.http.HttpResponse;
import com.att.aft.dme2.request.HttpRequest;
import com.att.authz.cm.cert.CSRMeta;
import com.att.cadi.cm.CertException;
import com.att.dao.aaf.cached.CachedCertDAO;
import com.att.dao.aaf.cass.CertDAO;
import com.att.inno.env.Trans;


@RunWith(MockitoJUnitRunner.class)
public class JU_DevlCA {
	
	@Mock
	private static CachedCertDAO certDAO;
	
	@Mock
	private static HttpServletRequest req;
	
	@Mock
	private static CSRMeta csrMeta;
	
	static Trans trans;
	
	static X509Certificate cert;
	static byte [] name = {1,23,4,54,6,56};
	
	private static DevlCA devICA;
	
	@BeforeClass
	public static void setUp() throws CertificateException, CertException, IOException {
		String str = "core java api";
        byte[] b = str.getBytes();
		Principal prc = new X500Principal("CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US");
		req = mock(HttpServletRequest.class);
		devICA = mock(DevlCA.class);
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
		when(devICA.sign(Mockito.any(Trans.class), Mockito.any(CSRMeta.class))).thenReturn(cert);
		certDAO = mock(CachedCertDAO.class, CALLS_REAL_METHODS);
	}
	
	@Test
	public void identity_True() throws CertificateException, IOException, CertException {
		assertNotNull(devICA.sign(trans, csrMeta));
	}
	
	
	@Test
	public void identityNull() throws CertificateException {
		try {
			assertNotNull(devICA.sign(null, csrMeta));
		} catch (IOException e) {
		
			e.printStackTrace();
		} catch (CertException e) {
			
			e.printStackTrace();
		}
	}
	
	@Test
	public void identityBothNull() throws CertificateException {
		try {
			assertNotNull(devICA.sign(null, null));
		} catch (IOException e) {
		
			e.printStackTrace();
		} catch (CertException e) {
			
			e.printStackTrace();
		}
	}

}
