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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.*;
import org.mockito.*;
import org.onap.aaf.cadi.configure.PlaceArtifactOnStream;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.Trans;

import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.CertInfo;

public class JU_PlaceArtifactOnStream {

    @Mock private Trans transMock;
    @Mock private CertInfo certInfoMock;
    @Mock private Artifact artiMock;

    private static final String luggagePassword = "12345";  // That's the stupidest combination I've ever heard in my life
    private static final String privateKeyString = "I'm a private key!";

    private ByteArrayOutputStream outStream;

    private List<String> certs;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        certs = new ArrayList<>();
        certs.add("cert1");
        certs.add("cert2");

        when(certInfoMock.getChallenge()).thenReturn(luggagePassword);
        when(certInfoMock.getCerts()).thenReturn(certs);
        when(certInfoMock.getPrivatekey()).thenReturn(privateKeyString);

        outStream = new ByteArrayOutputStream();
    }

    @Test
    public void test() {
        PlaceArtifactOnStream placer = new PlaceArtifactOnStream(new PrintStream(outStream));
        placer.place(transMock, certInfoMock, artiMock, "machine");

        String[] output = outStream.toString().split(System.lineSeparator(), 0);

        String[] expected = {
                "Challenge:  " + luggagePassword,
                "PrivateKey:",
                privateKeyString,
                "Certificate Chain:",
                "cert1",
                "cert2"
        };

        assertThat(output.length, is(expected.length));
        for (int i = 0; i < output.length; i++) {
            assertThat(output[i], is(expected[i]));
        }

        // coverage
        when(certInfoMock.getNotes()).thenReturn("");
        placer.place(transMock, certInfoMock, artiMock, "machine");

        when(certInfoMock.getNotes()).thenReturn("Some Notes");
        when(transMock.info()).thenReturn(mock(LogTarget.class));
        placer.place(transMock, certInfoMock, artiMock, "machine");
    }

}
