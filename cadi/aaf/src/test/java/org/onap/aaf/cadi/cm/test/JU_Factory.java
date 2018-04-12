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
package org.onap.aaf.cadi.cm.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.*;
import org.mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;

import javax.crypto.Cipher;

import org.onap.aaf.cadi.cm.Factory;
import org.onap.aaf.cadi.cm.Factory.StripperInputStream;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

public class JU_Factory {

	@Mock
	Trans transMock;

	@Mock
	TimeTaken timeTakenMock;

	@Mock
	LogTarget logTargetMock;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		when(transMock.start(anyString(), anyInt())).thenReturn(timeTakenMock);
		when(transMock.debug()).thenReturn(logTargetMock);
	}

	@Test
	public void generateKeyPairTest() throws Exception {
		String message = "The quick brown fox jumps over the lazy dog.";

		Cipher encryptor = Cipher.getInstance(Factory.KEY_ALGO);
		Cipher decryptor = Cipher.getInstance(Factory.KEY_ALGO);

		KeyPair kp1 = Factory.generateKeyPair(transMock);
		encryptor.init(Cipher.ENCRYPT_MODE, kp1.getPublic());
		decryptor.init(Cipher.DECRYPT_MODE, kp1.getPrivate());
		byte[] encrypedMessage1 = encryptor.doFinal(message.getBytes(StandardCharsets.UTF_8));
		String output1 = new String(decryptor.doFinal(encrypedMessage1));
		assertThat(output1, is(message));

		// coverage
		when(transMock.start("Generate KeyPair", Env.SUB)).thenReturn(null);
		KeyPair kp2 = Factory.generateKeyPair(transMock);
		encryptor.init(Cipher.ENCRYPT_MODE, kp2.getPublic());
		decryptor.init(Cipher.DECRYPT_MODE, kp2.getPrivate());
		byte[] encrypedMessage2 = encryptor.doFinal(message.getBytes(StandardCharsets.UTF_8));
		String output2 = new String(decryptor.doFinal(encrypedMessage2));
		assertThat(output2, is(message));

		KeyPair kp3 = Factory.generateKeyPair(null);
		encryptor.init(Cipher.ENCRYPT_MODE, kp3.getPublic());
		decryptor.init(Cipher.DECRYPT_MODE, kp3.getPrivate());
		byte[] encrypedMessage3 = encryptor.doFinal(message.getBytes(StandardCharsets.UTF_8));
		String output3 = new String(decryptor.doFinal(encrypedMessage3));
		assertThat(output3, is(message));
	}

	@Test
	public void keyToStringTest() throws IOException {
		KeyPair kp = Factory.generateKeyPair(transMock);

		String publicKeyString = Factory.toString(transMock, kp.getPublic());
		String privateKeyString = Factory.toString(transMock, kp.getPrivate());

		String[] publicKeyLines = publicKeyString.split("\n", 0);
		assertThat(publicKeyLines.length, is(9));
		assertThat(publicKeyLines[0], is("-----BEGIN PUBLIC KEY-----"));
		assertThat(publicKeyLines[8], is("-----END PUBLIC KEY-----"));

		String[] privateKeyLines = privateKeyString.split("\n", 0);
		assertThat(privateKeyLines.length, is(28));
		assertThat(privateKeyLines[0], is("-----BEGIN PRIVATE KEY-----"));
		assertThat(privateKeyLines[27], is("-----END PRIVATE KEY-----"));
	}
}
