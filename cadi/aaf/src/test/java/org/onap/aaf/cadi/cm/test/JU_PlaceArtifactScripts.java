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

package org.onap.aaf.cadi.cm.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.*;
import org.mockito.*;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.configure.PlaceArtifactScripts;
import org.onap.aaf.misc.env.Trans;

import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.CertInfo;

public class JU_PlaceArtifactScripts {

    @Mock private Trans transMock;
    @Mock private CertInfo certInfoMock;
    @Mock private Artifact artiMock;

    private static final String dirName = "src/test/resources/artifacts";
    private static final String nsName = "org.onap.test";
    private static final String luggagePassword = "12345";  // That's the stupidest combination I've ever heard in my life
    private static final String notification = "A notification";
    private static final String osUser = "user";  // That's the stupidest combination I've ever heard in my life

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(artiMock.getDir()).thenReturn(dirName);
        when(artiMock.getNs()).thenReturn(nsName);
        when(artiMock.getNotification()).thenReturn(notification);
        when(artiMock.getOsUser()).thenReturn(osUser);

        when(certInfoMock.getChallenge()).thenReturn(luggagePassword);
    }

    @AfterClass
    public static void tearDownOnce() {
        cleanup();
        PlaceArtifactScripts.clear();
    }

    @Test
    public void test() throws CadiException {
        PlaceArtifactScripts placer = new PlaceArtifactScripts();
        placer.place(transMock, certInfoMock, artiMock, "machine");

        assertThat(new File(dirName + '/' + nsName + ".crontab.sh").exists(), is(true));
        assertThat(new File(dirName + '/' + nsName + ".check.sh").exists(), is(true));

        //coverage
        when(artiMock.getNotification()).thenReturn("mailto: " + notification);
        placer.place(transMock, certInfoMock, artiMock, "machine");
    }

    private static void cleanup() {
        File dir = new File(dirName);
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                f.delete();
            }
            dir.delete();
        }
    }

}
