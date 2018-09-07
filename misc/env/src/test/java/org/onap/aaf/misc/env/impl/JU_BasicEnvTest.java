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
package org.onap.aaf.misc.env.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.applet.Applet;
import java.io.IOException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.misc.env.Decryptor;
import org.onap.aaf.misc.env.Encryptor;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

public class JU_BasicEnvTest {

    @Mock
    Decryptor decrypt;

    @Mock
    Encryptor encrypt;

    @Before
    public void setup() {
        decrypt = mock(Decryptor.class);
        encrypt = mock(Encryptor.class);
    }

    @Test
    public void testLogTarget() {
        Properties prop = new Properties();
        BasicEnv env = new BasicEnv(prop);

        assertEquals(env.fatal(), LogTarget.SYSERR);
        assertEquals(env.error(), LogTarget.SYSERR);
        assertEquals(env.audit(), LogTarget.SYSOUT);
        assertEquals(env.warn(), LogTarget.SYSERR);
        assertEquals(env.init(), LogTarget.SYSOUT);
        assertEquals(env.info(), LogTarget.SYSOUT);
        assertEquals(env.debug(), LogTarget.NULL);
        assertEquals(env.trace(), LogTarget.NULL);

        env.debug(LogTarget.SYSOUT);
        assertEquals(env.debug(), LogTarget.SYSOUT);

        assertNull(env.getProperty("key"));
        assertEquals("default", env.getProperty("key", "default"));

        env.setProperty("key", "value");
        assertEquals("value", env.getProperty("key", "default"));

        Properties filteredProperties = env.getProperties("key");
        assertEquals(filteredProperties.size(), 1);

        env.setProperty("key", null);
        assertEquals("default", env.getProperty("key", "default"));

        filteredProperties = env.getProperties("key1");
        assertEquals(filteredProperties.size(), 0);

        filteredProperties = env.getProperties();
        assertEquals(filteredProperties.size(), 0);

    }

    @Test
    public void testBasicEnv() {
        Applet applet = null;

        BasicEnv env = new BasicEnv(applet, "tag1", "tag2");

        TimeTaken tt = env.start("Name", 2);

        long end = tt.end();
        StringBuilder sb = new StringBuilder();

        assertEquals(tt.toString(), "Name " + (end - tt.start) / 1000000f + "ms ");
        tt.output(sb);
        assertEquals(sb.toString(), "XML Name " + (end - tt.start) / 1000000f + "ms");

        env.set(decrypt);
        assertEquals(env.decryptor(), decrypt);
        env.set(encrypt);
        assertEquals(env.encryptor(), encrypt);
    }

    @Test
    public void testBasicEnvDiffFlag() {
        Properties prop = new Properties();

        BasicEnv env = new BasicEnv("tag1", prop);

        TimeTaken tt = env.start("Name", 1);

        long end = tt.end();
        StringBuilder sb = new StringBuilder();

        assertEquals(tt.toString(), "Name " + (end - tt.start) / 1000000f + "ms ");
        tt.output(sb);
        assertEquals(sb.toString(), "REMOTE Name " + (end - tt.start) / 1000000f + "ms");

        tt = env.start("New Name", 4);
        tt.size(10);
        sb = new StringBuilder();
        tt.output(sb);
        assertEquals(tt.toString(), "New Name " + (end - tt.start) / 1000000f + "ms 10");
        assertEquals(sb.toString(), "JSON New Name " + (end - tt.start) / 1000000f + "ms size: 10");

        env.staticSlot("tag", "prop");

        if (System.getProperties().keySet().iterator().hasNext()) {
            String key = (String) System.getProperties().keySet().iterator().next();

            env.loadFromSystemPropsStartsWith(key);
            assertEquals(env.getProperty(key), System.getProperties().get(key));
        }

        BasicTrans trans = env.newTrans();
        assertEquals(trans.delegate, env);

    }

    @Test
    public void testLoadProperties() throws IOException {
        Properties prop = new Properties();

        BasicEnv env = new BasicEnv("tag1", prop);

        env.loadPropFiles("tag1", null);
        env.setProperty("tag1", "propfile.properties");
        env.loadPropFiles("tag1", null);

        assertEquals(env.getProperty("prop1"), "New Property");

        env.loadToSystemPropsStartsWith("prop1");

        assertTrue(System.getProperties().keySet().contains("prop1"));
        assertEquals(System.getProperties().get("prop1"), "New Property");
    }

    @After
    public void tearDown() throws IOException {
        /*
         * File file = new File("./log-Append" + ending + "_0.log"); if (file.exists())
         * { Files.delete(Paths.get(file.getAbsolutePath())); } file = new
         * File("./log-Append" + ending + "_1.log"); if (file.exists()) {
         * Files.delete(Paths.get(file.getAbsolutePath())); } file = new File("./Append"
         * + ending + "_0.log"); if (file.exists()) {
         * Files.delete(Paths.get(file.getAbsolutePath())); } file = new File("./Append"
         * + ending + "_1.log"); if (file.exists()) {
         * Files.delete(Paths.get(file.getAbsolutePath())); }
         */
    }
}
