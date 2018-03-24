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

package org.onap.aaf.auth.cm.test;

import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.AfterClass;
import org.junit.Test;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.cadi.cm.CertException;
import org.onap.aaf.cadi.cm.Factory;

import junit.framework.Assert;

public class JU_KeyMarshaling {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		AuthzEnv env = new AuthzEnv();
		AuthzTrans trans = env.newTrans();
		try {
			KeyPair kpair = Factory.generateKeyPair(trans);
			String privateString = Factory.toString(trans, kpair.getPrivate());
			System.out.println("Private as base64 encoded as PKCS8 Spec");
			System.out.println(privateString);
			
			// Take String, and create Private Key
			PrivateKey pk = Factory.toPrivateKey(trans, privateString);
			Assert.assertEquals(kpair.getPrivate().getAlgorithm(), pk.getAlgorithm());
			Assert.assertEquals(kpair.getPrivate().getFormat(), pk.getFormat());
			Assert.assertEquals(kpair.getPrivate().getEncoded(), pk.getEncoded());
		
			
			String s = Factory.toString(trans, kpair.getPublic());
			System.out.println("Public as base64 encoded x509 Spec");
			System.out.println(s);
			
			PublicKey pub = Factory.toPublicKey(trans, s);
			Assert.assertEquals(kpair.getPublic().toString(), pub.toString());
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CertException e) {
			e.printStackTrace();
		} finally {
			StringBuilder sb = new StringBuilder("=== Timings ===\n");
			trans.auditTrail(1, sb);
			System.out.println(sb);
		}
	}

}
