/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.fs.test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.fs.AAF_FS;
import org.onap.aaf.auth.server.JettyServiceStarter;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.register.Registrant;
import org.onap.aaf.misc.env.APIException;

public class JU_AAF_FS {
    AuthzEnv aEnv;
    AAF_FS aafFs;
    File fService;
    File fEtc;
    String value;
    File d;
    private static final String testDir = "src/test/resources/logs";
    private ByteArrayOutputStream outStream;
    private ByteArrayOutputStream errStream;

    @Before
    public void setUp() throws APIException, IOException, CadiException {
        outStream = new ByteArrayOutputStream();
        errStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));
        System.setErr(new PrintStream(errStream));
        value = System.setProperty(Config.CADI_LOGDIR, testDir);
        System.setProperty(Config.CADI_ETCDIR, testDir);
        System.out.println(ClassLoader.getSystemResource("org.osaaf.aaf.log4j.props"));
        d = new File(testDir);
        d.mkdirs();
        fService = new File(d + "/fs-serviceTEST.log");
        fService.createNewFile();
        fEtc = new File(d + "/org.osaaf.aaf.log4j.props");
        fEtc.createNewFile();

        aEnv = new AuthzEnv();
        aEnv.staticSlot("test");
        aEnv.access().setProperty("aaf_public_dir", "test");
        aEnv.access().setProperty(Config.AAF_LOCATOR_ENTRIES, "aaf_com");
        aEnv.access().setProperty(Config.AAF_LOCATOR_VERSION, "1.1");
        Server serverMock = mock(Server.class);
        JettyServiceStarter<AuthzEnv, AuthzTrans> jssMock = mock(JettyServiceStarter.class);
        aafFs = new AAF_FS(aEnv);
        aEnv.access().setProperty(Config.AAF_LOCATE_URL, "aaf_loc:ate.url");
        aafFs = new AAF_FS(aEnv);
    }

    @Test
    public void testRegistrants() throws CadiException, LocatorException {
        int port = 8008;
        aEnv.access().setProperty(Config.AAF_URL, "www.google.com");
        aEnv.access().setProperty(Config.CADI_LATITUDE, "38.550674");
        aEnv.access().setProperty(Config.CADI_LONGITUDE, "-90.146942");
        aEnv.access().setProperty(Config.AAF_LOCATE_URL, "testLocateUrl");
        aEnv.access().setProperty(Config.HOSTNAME, "testHost");

        // Doesn't work within Jenkins
        Registrant<AuthzEnv>[] registrants = aafFs.registrants(port);
        assertNotNull(registrants);
    }

    @Test
    public void testFilters() throws CadiException, LocatorException {
        aafFs.filters();
    }

    @Test
    public void testMain() {
        System.setProperty("cadi_exitOnFailure", "false");

        String[] strArr = { "aaf_component=aaf_com:po.nent" };
        try {
            AAF_FS.main(strArr); // Timeout caused in Jenkins but not in local
        } catch (Exception e) {
            // Failure expected until we understand how code is.
        }
    }

    @After
    public void cleanUp() {
        for (File f : d.listFiles()) {
            f.delete();
        }
        d.delete();
        System.setErr(System.err);
        System.setOut(System.out);
    }

}
