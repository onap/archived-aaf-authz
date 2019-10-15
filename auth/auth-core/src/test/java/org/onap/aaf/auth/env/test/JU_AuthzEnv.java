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
import org.junit.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;

public class JU_AuthzEnv {

    AuthzEnv authzEnv;
    ByteArrayOutputStream outStream;
    ByteArrayOutputStream errStream;
    enum Level {DEBUG, INFO, AUDIT, INIT, WARN, ERROR};

    @Before
    public void setUp() {
        outStream = new ByteArrayOutputStream();
        errStream = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outStream));
        System.setErr(new PrintStream(errStream));

        authzEnv = new AuthzEnv();
    }

    @After
    public void tearDown() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Test
    @SuppressWarnings("unused")
    public void testConstructors() {
        AuthzEnv authzEnv1 = new AuthzEnv("Test");
        AuthzEnv authzEnv2 = new AuthzEnv((PropAccess)null);
        AuthzEnv authzEnv3 = new AuthzEnv((Properties)null);
    }

    @Test
    public void testTransRate() {
        Long Result = authzEnv.transRate();
        assertNotNull(Result);
    }

    @Test
    public void checkNewTransNoAvg() {
        assertNotNull(authzEnv.newTransNoAvg());
    }

    @Test
    public void checkNewTrans() {
        assertNotNull(authzEnv.newTrans());
    }

    @Test
    public void checkPropAccess() {
        assertNotNull(authzEnv.access());
    }

    @Test
    public void checkgetProperties() { //TODO:[GABE]No setter for this, add?
        assertNotNull(authzEnv.getProperties());
        assertNotNull(authzEnv.getProperties("test"));
    }

    @Test
    public void checkPropertyGetters(){
        authzEnv.setProperty("key","value");
        assertEquals(authzEnv.getProperty("key"), "value");
        assertEquals(authzEnv.getProperty("key","value"), "value");
    }

    @Test
    public void checkPropertySetters(){
        assertEquals(authzEnv.getProperty("key","value"), authzEnv.setProperty("key","value"));
    }

    @Test(expected = IOException.class)
    public void testDecryptException() throws IOException{
        authzEnv.setProperty(Config.CADI_KEYFILE, "test/keyfile");
        authzEnv.decrypt(null, false);
    }

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
        assertNotNull(cLoad);
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
        assertFalse(test);
        test = authzEnv.willLog(lvl1);
        assertTrue(test);
    }

    @Test
    public void testSetLogLevel() {
        Access.Level lvl = Access.Level.DEBUG;
        authzEnv.setLogLevel(lvl);
    }

}
