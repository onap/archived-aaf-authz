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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.onap.aaf.cadi.Access;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
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
	System.out.println("value of result " +Result); //Expected 300000
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

	@Test(expected = APIException.class)
	public void checkSetLog4JNames() throws APIException {//TODO: Find better way to test instead of just seeing if strings pass
		authzEnv.setLog4JNames("path", "root","service","audit","init","trace");
		authzEnv.setLog4JNames("path", "root",null,"audit","init","trace");
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

	@Test(expected = IOException.class)
	public void testDecryptException() throws IOException{
		String encrypted = "enc:";
		authzEnv.setProperty(Config.CADI_KEYFILE, "test");//TODO: Figure out setter for this
		authzEnv.decrypt(encrypted, true);
		authzEnv.decrypt("", false);
	}

	@Test
	public void testDecrypt() throws IOException{
		String encrypted = "encrypted";
		String Result = authzEnv.decrypt(encrypted, true);
		System.out.println("value of res " +Result);
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
