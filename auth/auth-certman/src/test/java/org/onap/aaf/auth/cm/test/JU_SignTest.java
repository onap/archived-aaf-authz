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

import java.io.File;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.fail;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.cadi.cm.Factory;

public class JU_SignTest {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() throws Exception {
		AuthzEnv env = new AuthzEnv();
		AuthzTrans trans = env.newTrans();
		KeyPair kpair = Factory.generateKeyPair(trans);
		PrivateKey privateKey = kpair.getPrivate();
		String privateString = Factory.toString(trans, privateKey);
		System.out.println("Private as base64 encoded as PKCS8 Spec");
		System.out.println(privateString);
		
		PublicKey publicKey = kpair.getPublic();
		String publicString = Factory.toString(trans, publicKey); 
		System.out.println("public as base64 encoded as PKCS8 Spec");
		System.out.println(publicString);
		
		byte data[] = "Sign this please.".getBytes();
		byte sig[] = Factory.sign(trans, data, privateKey);
		System.out.println("Signature");
		System.out.println(Factory.toSignatureString(sig));
		
		Assert.assertTrue(Factory.verify(trans, data, sig, publicKey));
	}
	
//	@Test
//	public void test2() throws Exception {
//		AuthzEnv env = new AuthzEnv();
//		AuthzTrans trans = env.newTrans();
//		File key = new File("/opt/app/aaf/common/com.att.aaf.key");
//		PrivateKey privKey = Factory.toPrivateKey(trans, key);
//		RSAPrivateKey rPrivKey = (RSAPrivateKey)privKey;
//		BigInteger privMod, pubMod;
//		System.out.println((privMod = rPrivKey.getModulus()).toString(16));
//		
//		byte data[] = "Sign this please.".getBytes();
//		byte sig[] = Factory.sign(trans, data, privKey);
//		System.out.println("Signature");
//		System.out.println(Factory.toSignatureString(sig));
//
//		
//		File crt = new File("/opt/app/aaf/common/com.att.aaf.crt");
//		Collection<? extends Certificate> x509s = Factory.toX509Certificate(trans, crt);
//		X509Certificate cert = null;
//		for(Certificate c : x509s) {
//			cert = (X509Certificate)c;
//			break;
//		}
//		PublicKey pubKey = cert.getPublicKey();
//		RSAPublicKey rPubKey = (RSAPublicKey)pubKey;
//	
//		System.out.println((pubMod = rPubKey.getModulus()).toString(16));
//
//		Assert.assertTrue(Factory.verify(trans, data, sig, pubKey));
//		Assert.assertEquals(privMod,pubMod);
//		
//	}

	@Test						//TODO: Temporary fix AAF-111
	public void netYetTested() {
		fail("Tests not yet implemented");
	}
}
