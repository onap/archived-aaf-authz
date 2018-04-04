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
package org.onap.aaf.auth.env.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.PropAccess;
import org.powermock.modules.junit4.PowerMockRunner;

import junit.framework.Assert;

@RunWith(PowerMockRunner.class)
public class JU_AuthzEnv {
	private static final org.onap.aaf.cadi.Access.Level DEBUG = null;
	AuthzEnv authzEnv;
	enum Level {DEBUG, INFO, AUDIT, INIT, WARN, ERROR};

	@Before
	public void setUp(){
		PropAccess access = null;
		Properties props = null;
		authzEnv = new AuthzEnv();
		AuthzEnv authzEnv1 = new AuthzEnv("Test");
		AuthzEnv authzEnv2 = new AuthzEnv(props);
		AuthzEnv authzEnv3 = new AuthzEnv(access);
	}

	@Test
	public void testTransRate() {
	Long Result =	authzEnv.transRate();
	assertNotNull(Result);
	}

	@Test
	public void checkNewTransNoAvg() {

		Assert.assertNotNull(authzEnv.newTransNoAvg());
	}

	@Test
	public void checkNewTrans() {
		Assert.assertNotNull(authzEnv.newTrans());
	}

	@Test
	public void checkPropAccess() {
		Assert.assertNotNull(authzEnv.access());
	}

	@Test
	public void checkgetProperties() { //TODO:[GABE]No setter for this, add?
		Assert.assertNotNull(authzEnv.getProperties());
		Assert.assertNotNull(authzEnv.getProperties("test"));
	}

	@Test
	public void checkPropertyGetters(){
		authzEnv.setProperty("key","value");
		Assert.assertEquals(authzEnv.getProperty("key"), "value");
		Assert.assertEquals(authzEnv.getProperty("key","value"), "value");
	}

	@Test
	public void checkPropertySetters(){
		Assert.assertEquals(authzEnv.getProperty("key","value"), authzEnv.setProperty("key","value"));
	}

//	@Test(expected = IOException.class)				//TODO: AAF-111 make fail not happen
//	public void testDecryptException() throws IOException{
//		String encrypted = "enc:";
//		authzEnv.setProperty(Config.CADI_KEYFILE, "test");//TODO: Figure out setter for this
//		authzEnv.decrypt(encrypted, true);
//		authzEnv.decrypt("", false);		//TODO: AAF-111 fail without logging a fail
//	}

	@Test
	public void testDecrypt() throws IOException{
		String encrypted = "encrypted";
		String Result = authzEnv.decrypt(encrypted, true);
		assertEquals("encrypted",Result);
	}

	@Test
	public void testClassLoader() {
		ClassLoader cLoad = mock(ClassLoader.class);
		cLoad = authzEnv.classLoader();
		Assert.assertNotNull(cLoad);
	}

	@Test
	public void testLoad() throws IOException {
		InputStream is = mock(InputStream.class);
		authzEnv.load(is);
	}

	@Test
	public void testLog() {
		Access.Level lvl = Access.Level.DEBUG;
		Object msgs = null;
		authzEnv.log(lvl, msgs);
	}

	@Test
	public void testLog1() {
		Exception e = new Exception();
		Object msgs = null;
		authzEnv.log(e, msgs);
	}

	@Test
	public void testPrintf() {
		Access.Level lvl = Access.Level.DEBUG;
		Object msgs = null;
		authzEnv.printf(lvl, "Test", msgs);
	}

	@Test
	public void testWillLog() {
		Access.Level lvl = Access.Level.DEBUG;
		Access.Level lvl1 = Access.Level.AUDIT;
		boolean test = authzEnv.willLog(lvl);
		Assert.assertFalse(test);
		test = authzEnv.willLog(lvl1);
		Assert.assertTrue(test);

	}

	@Test
	public void testSetLogLevel() {
		Access.Level lvl = Access.Level.DEBUG;
		authzEnv.setLogLevel(lvl);
	}

}
