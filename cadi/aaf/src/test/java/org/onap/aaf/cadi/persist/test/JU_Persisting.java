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
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.crypto.CipherInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.persist.Persist;
import org.onap.aaf.cadi.persist.PersistFile;
import org.onap.aaf.cadi.persist.Persisting;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;

public class JU_Persisting {

    private static final String resourceDirString = "src/test/resources";
    private static final String tokenDirString = "tokenDir";
    private static final String tokenFileName = "token";

    private static final int data = 5;
    private static final long expires = 10000;

    private static final byte[] cred = "password".getBytes();

    private PropAccess access;

    @Mock private Persist<Integer, ?> persistMock;
    @Mock private RosettaDF<Integer> dfMock;
    @Mock private RosettaData<Integer> dataMock;

    @Before
    public void setup() throws APIException {
        MockitoAnnotations.initMocks(this);

        when(dfMock.newData()).thenReturn(dataMock);
        when(dataMock.load(data)).thenReturn(dataMock);
        when(dataMock.load((CipherInputStream)any())).thenReturn(dataMock);

        access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
        access.setProperty(Config.CADI_TOKEN_DIR, resourceDirString);

        persistMock.access = access;
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
    public void test() throws CadiException, APIException {
        Path tokenPath = Paths.get(resourceDirString, tokenDirString);

        Persisting<Integer> persisting = new Persisting<>(persistMock, data, expires, cred, tokenPath);
        assertThat(persisting.get(), is(data));
        assertThat(persisting.expires(), is(expires));
        assertThat(persisting.expired(), is(true));
        assertThat(persisting.hasBeenTouched(), is(true));

        PersistFile persistFile = new PersistFile(access, tokenDirString);
        tokenPath = persistFile.writeDisk(dfMock, data, cred, tokenFileName, expires);
        persisting = new Persisting<>(persistMock, data, expires, cred, tokenPath);
        assertThat(persisting.hasBeenTouched(), is(false));

        persisting = new Persisting<>(persistMock, data, expires * (int)10e9, cred, tokenPath);
        assertThat(persisting.expired(), is(false));

        assertThat(persisting.checkSyncTime(), is(true));
        assertThat(persisting.checkSyncTime(), is(false));

        assertThat(persisting.checkReloadable(), is(false));

        assertThat(persisting.getHash(), is(cred));

        assertThat(persisting.match(null), is(false));
        assertThat(persisting.match("random!".getBytes()), is(false));
        assertThat(persisting.match("passwrod".getBytes()), is(false));
        assertThat(persisting.match(cred), is(true));

        persisting.clearCount();
        assertThat(persisting.count(), is(0));
        persisting.inc();
        assertThat(persisting.count(), is(1));

        assertThat(persisting.path(), is(tokenPath));
    }

}
