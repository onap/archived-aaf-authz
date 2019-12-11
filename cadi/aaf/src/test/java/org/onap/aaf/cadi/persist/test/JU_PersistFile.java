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

package org.onap.aaf.cadi.persist.test;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import javax.crypto.CipherInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.util.Holder;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.persist.PersistFile;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;

public class JU_PersistFile {

    private static final String resourceDirString = "src/test/resources";
    private static final String tokenDirString = "tokenDir";
    private static final String tokenFileName = "token";

    private static final int data = 5;
    private static final long expires = 10000;

    private static final byte[] cred = "password".getBytes();

    private PropAccess access;
    private Holder<Path> hp = new Holder<Path>(null);
    private Holder<Long> hl = new Holder<Long>(null);

    @Mock private RosettaDF<Integer> dfMock;
    @Mock private RosettaData<Integer> dataMock;
    @Mock private Holder<Path> hpMock;

    @Before
    public void setup() throws APIException {
        MockitoAnnotations.initMocks(this);

        when(dfMock.newData()).thenReturn(dataMock);
        when(dataMock.load(data)).thenReturn(dataMock);
        when(dataMock.load((CipherInputStream)any())).thenReturn(dataMock);

        access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
        access.setProperty(Config.CADI_TOKEN_DIR, resourceDirString);
    }

    @After
    public void tearDown() {
        File dir = new File(resourceDirString + '/' + tokenDirString);
        for (File f : dir.listFiles()) {
            f.delete();
        }
        dir.delete();
    }

    @Test
    public void test() throws CadiException, APIException, IOException {
        PersistFile persistFile = new PersistFile(access, tokenDirString);
        // Second call is for coverage
        persistFile = new PersistFile(access, tokenDirString);
        Path filepath = persistFile.writeDisk(dfMock, data, cred, tokenFileName, expires);
        persistFile.readDisk(dfMock, cred, tokenFileName, hp, hl);
        assertThat(persistFile.readExpiration(filepath), is(expires));

        FileTime ft1 = persistFile.getFileTime(tokenFileName, hp);
        FileTime ft2 = persistFile.getFileTime(tokenFileName, hpMock);
        assertThat(ft1.toMillis(), is(ft2.toMillis()));

        persistFile.deleteFromDisk(filepath);
        persistFile.deleteFromDisk(resourceDirString + '/' + tokenDirString + '/' + tokenFileName);
        assertThat(persistFile.readExpiration(filepath), is(0L));

        persistFile.getPath(resourceDirString + '/' + tokenDirString + '/' + tokenFileName);

        persistFile.writeDisk(dfMock, data, null, tokenFileName, expires);
        try {
            persistFile.readDisk(dfMock, cred, tokenFileName, hp, hl);
            fail("Should've thrown an exception");
        } catch (CadiException e) {
            assertThat(e.getMessage(), is(CadiException.class.getName() + ": Hash does not match in Persistence"));
        }
    }

}
