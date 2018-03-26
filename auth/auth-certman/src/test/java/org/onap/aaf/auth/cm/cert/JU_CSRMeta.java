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
 * *
 ******************************************************************************/
package org.onap.aaf.auth.cm.cert;

import static org.mockito.Mockito.mock;

import java.util.List;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.cadi.cm.CertException;
import org.onap.aaf.misc.env.Trans;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class JU_CSRMeta {
	
	private static CSRMeta csrmeta;
	private static Trans trans;
	private static PKCS10CertificationRequest req;
	
	@BeforeClass
	public static void setUp() throws CertException {
		trans = mock(Trans.class);
		List<RDN> lrdn = RDN.parse('/',"o=ATT Services, Inc/l=St Louis/st=Missouri/c=US");
		
		csrmeta = new CSRMeta(lrdn);
	}
	
//	@Test
//	public void x500Name() throws IOException {
//		
//		X500Name x500 = csrmeta.x500Name();
//		assertEquals(x500.toString(),"CN=CN,E=pupleti@ht.com,OU=HAKJH787,O=O,L=L,ST=ST,C=C");
//	}
//	
//	@Test
//	public void initialConversationCert() throws CertificateException, OperatorCreationException, IOException {
//		X509Certificate cert = csrmeta.initialConversationCert(trans);
//		assertEquals(cert.getBasicConstraints(),-1);
//	}
//	
//	@Test
//	public void generateCSR() throws IOException, CertException {
//		req = csrmeta.generateCSR(trans);
//		assertNotNull(req);
//	}
	
	@Rule
    public ExpectedException thrown= ExpectedException.none();
	
//	@Test
//	public void dump() throws IOException, CertException {
//		req = csrmeta.generateCSR(trans);
//		csrmeta.dump(req);
//	}
	
	@Test						//TODO: Temporary fix AAF-111
	public void netYetTested() {
		Assert.fail("Tests not yet implemented");
	}
	
}
