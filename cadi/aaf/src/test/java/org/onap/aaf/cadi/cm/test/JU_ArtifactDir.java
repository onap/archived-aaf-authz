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
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.configure.Agent;
import org.onap.aaf.cadi.configure.ArtifactDir;
import org.onap.aaf.cadi.util.Chmod;
import org.onap.aaf.misc.env.Trans;

import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.CertInfo;

public class JU_ArtifactDir {

    @Mock private Trans transMock;
    @Mock private CertInfo certInfoMock;
    @Mock private Artifact artiMock;

    private static final String dirName = "src/test/resources/artifacts";
    private static final String nsName = "org.onap.test";
    private static final String luggagePassword = "12345";  // That's the stupidest combination I've ever heard in my life

    private List<String> issuers;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    
        issuers = new ArrayList<>();
        issuers.add("issuer1");
        issuers.add("issuer2");
    }

    @After
    public void tearDown() {
        ArtifactDir.clear();
    }

    @AfterClass
    public static void tearDownOnce() {
        cleanup();
    }

    @Test
    public void test() throws CadiException, IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        ArtifactDirStud artiDir = new ArtifactDirStud();

        try {
            artiDir.place(transMock, certInfoMock, artiMock, "machine");
            fail("Should've thrown an exception");
        } catch (CadiException e) {
            assertThat(e.getMessage(), is("File Artifacts require a path\nFile Artifacts require an AAF Namespace"));
        }
    
        when(artiMock.getDir()).thenReturn(dirName);
        try {
            artiDir.place(transMock, certInfoMock, artiMock, "machine");
            fail("Should've thrown an exception");
        } catch (CadiException e) {
            assertThat(e.getMessage(), is("File Artifacts require an AAF Namespace"));
        }
    
        when(artiMock.getNs()).thenReturn(nsName);
        when(certInfoMock.getCaIssuerDNs()).thenReturn(issuers);
        when(certInfoMock.getChallenge()).thenReturn(luggagePassword);
        artiDir.place(transMock, certInfoMock, artiMock, "machine");
    
        File writableFile = new File(dirName + '/' + nsName + "writable.txt");
        ArtifactDir.write(writableFile, Chmod.to755, "first data point", "second data point");
        try {
            ArtifactDir.write(writableFile, Chmod.to755, (String[])null);
            fail("Should've thrown an exception");
        } catch (NullPointerException e) {
        }
    
        KeyStore ks = KeyStore.getInstance(Agent.PKCS12);
        try {
            ArtifactDir.write(writableFile, Chmod.to755, ks, luggagePassword.toCharArray());
            fail("Should've thrown an exception");
        } catch (CadiException e) {
        }
    
        ks.load(null, null);
        ArtifactDir.write(writableFile, Chmod.to755, ks, luggagePassword.toCharArray());
    
        ArtifactDirStud artiDir2 = new ArtifactDirStud();
        artiDir2.place(transMock, certInfoMock, artiMock, "machine");

        // coverage
        artiDir.place(transMock, certInfoMock, artiMock, "machine");

        ArtifactDir.clear();
        artiDir.place(transMock, certInfoMock, artiMock, "machine");

    }

    public void throwsTest() throws CadiException {
        ArtifactDirStud artiDir = new ArtifactDirStud();
        when(artiMock.getDir()).thenReturn(dirName);
        when(artiMock.getNs()).thenReturn(nsName);
        artiDir.place(transMock, certInfoMock, artiMock, "machine");
    }

    private class ArtifactDirStud extends ArtifactDir {
        @Override
        protected boolean _place(Trans trans, CertInfo certInfo, Artifact arti) throws CadiException {
            // This is only here so that we have a concrete class to test
            return false;
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
