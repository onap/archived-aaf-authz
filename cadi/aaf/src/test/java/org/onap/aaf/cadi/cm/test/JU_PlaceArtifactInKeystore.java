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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.configure.Agent;
import org.onap.aaf.cadi.configure.PlaceArtifactInKeystore;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.CertInfo;

public class JU_PlaceArtifactInKeystore {

    @Mock private Trans transMock;
    @Mock private CertInfo certInfoMock;
    @Mock private Artifact artiMock;

    private static final String caName = "onap";
    private static final String dirName = "src/test/resources/artifacts";
    private static final String nsName = "org.onap.test";
    private static final String mechID = "m12345";
    private static final String luggagePassword = "12345";  // That's the stupidest combination I've ever heard in my life

    private static String privateKeyString;
    private static String x509Chain;
    private static String x509String;

    private List<String> certs;

    @Before
    public void setup() throws FileNotFoundException, IOException, CertificateException {
        MockitoAnnotations.initMocks(this);

        x509Chain = fromFile(new File("src/test/resources/cert.pem"));
        x509String = fromFile(new File("src/test/resources/exampleCertificate.cer"));
        privateKeyString = fromFile(new File("src/test/resources/key.pem"));

        certs = new ArrayList<>();

        when(certInfoMock.getChallenge()).thenReturn(luggagePassword);
        when(certInfoMock.getCerts()).thenReturn(certs);

        when(artiMock.getCa()).thenReturn(caName);
        when(artiMock.getDir()).thenReturn(dirName);
        when(artiMock.getNs()).thenReturn(nsName);
        when(artiMock.getMechid()).thenReturn(mechID);

        when(certInfoMock.getPrivatekey()).thenReturn(privateKeyString);

        when(transMock.start("Reconstitute Private Key", Env.SUB)).thenReturn(mock(TimeTaken.class));
    }

    @AfterClass
    public static void tearDownOnce() {
        cleanup();
        PlaceArtifactInKeystore.clear();
    }

    @Test
    public void test() throws CadiException {
        // Note: PKCS12 can't be tested in JDK 7 and earlier.  Can't handle Trusting Certificates.
        PlaceArtifactInKeystore placer = new PlaceArtifactInKeystore(Agent.JKS);

        certs.add(x509String);
        certs.add(x509Chain);
        assertThat(placer.place(transMock, certInfoMock, artiMock, "machine"), is(true));
        for (String ext : new String[] { Agent.JKS, "trust.jks"}) {
            File f = new File(dirName + '/' + nsName + '.' + ext);
            assertThat(f.exists(), is(true));
        }

        // coverage
        assertThat(placer.place(transMock, certInfoMock, artiMock, "machine"), is(true));

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

    public String fromFile(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String line;
        baos.write(br.readLine().getBytes());
        // Here comes the hacky part
        baos.write("\n".getBytes());
        while ((line=br.readLine())!=null) {
            if (line.length()>0) {
                baos.write(line.getBytes());
                baos.write("\n".getBytes());
            }
        }
        br.close();
        return baos.toString();
    }
}
