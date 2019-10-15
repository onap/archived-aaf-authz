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
import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import org.mockito.*;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.configure.PlaceArtifactInFiles;
import org.onap.aaf.misc.env.Trans;

import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.CertInfo;

public class JU_PlaceArtifactInFiles {

    @Mock private Trans transMock;
    @Mock private CertInfo certInfoMock;
    @Mock private Artifact artiMock;

    private static final String dirName = "src/test/resources/artifacts";
    private static final String nsName = "org.onap.test";
    private static final String luggagePassword = "12345";  // That's the stupidest combination I've ever heard in my life

    private List<String> certs;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        certs = new ArrayList<>();
        certs.add("cert1");
        certs.add("cert2");

        when(certInfoMock.getChallenge()).thenReturn(luggagePassword);
        when(certInfoMock.getCerts()).thenReturn(certs);

        when(artiMock.getDir()).thenReturn(dirName);
        when(artiMock.getNs()).thenReturn(nsName);
    }

    @AfterClass
    public static void tearDownOnce() {
        cleanup();
        PlaceArtifactInFiles.clear();
    }

    @Test
    public void test() throws CadiException {
        PlaceArtifactInFiles placer = new PlaceArtifactInFiles();
        placer.place(transMock, certInfoMock, artiMock, "machine");
        assertThat(placer._place(transMock, certInfoMock, artiMock), is(true));
        assertThat(new File(dirName + '/' + nsName + ".crt").exists(), is(true));
        assertThat(new File(dirName + '/' + nsName + ".key").exists(), is(true));
    
        when(certInfoMock.getCerts()).thenReturn(null);
        try {
            placer._place(transMock, certInfoMock, artiMock);
            fail("Should've thrown an exception");
        } catch (Exception e) {
        }
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
