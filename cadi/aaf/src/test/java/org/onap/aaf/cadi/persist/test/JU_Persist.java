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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Matchers.any;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.util.Holder;
import org.onap.aaf.cadi.client.Result;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.persist.Persist;
import org.onap.aaf.cadi.persist.Persist.Loader;
import org.onap.aaf.cadi.persist.Persistable;
import org.onap.aaf.cadi.persist.Persisting;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

public class JU_Persist {

    private static final String resourceDirString = "src/test/resources";
    private static final String tokenDirString = "tokenDir";
    private static final String key = "key";

    private static final int data = 5;

    private static final byte[] cred = "password".getBytes();

    private PropAccess access;
    private Result<Persistable<Integer>> result;

    @Mock private RosettaEnv envMock;
    @Mock private Persist<Integer, ?> persistMock;
    @Mock private RosettaDF<Integer> dfMock;
    @Mock private RosettaData<Integer> dataMock;
    @Mock private Persistable<Integer> ctMock1;
    @Mock private Persisting<Integer> ctMock2;
    @Mock private Loader<Persistable<Integer>> loaderMock;

    @Before
    public void setup() throws APIException, CadiException, LocatorException {
        MockitoAnnotations.initMocks(this);

        doReturn(dfMock).when(envMock).newDataFactory((Class<?>[]) any());
        when(dfMock.newData()).thenReturn(dataMock);
        when(dataMock.load(data)).thenReturn(dataMock);


        result = Result.ok(200, ctMock1);
        when(loaderMock.load(key)).thenReturn(result);

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
    public void test() throws CadiException, APIException, LocatorException, InterruptedException {
        Persist<Integer, Persistable<Integer>> persist = new PersistStub(access, envMock, null, tokenDirString);
        // Second call for coverage
        persist = new PersistStub(access, envMock, null, tokenDirString);
        assertThat(persist.getDF(), is(dfMock));
        persist.put(key, ctMock2);
        Result<Persistable<Integer>> output = persist.get(key, cred, loaderMock);
        assertThat(output.code, is(200));
        assertThat(output.isOK(), is(true));

        when(ctMock2.checkSyncTime()).thenReturn(true);
        when(ctMock2.hasBeenTouched()).thenReturn(true);
        output = persist.get(key, cred, loaderMock);
        assertThat(output.code, is(200));
        assertThat(output.isOK(), is(true));

        persist.delete(key);

        assertThat(persist.get(null, null, null), is(nullValue()));

        // Uncommenting this lets us begin to test the nested Clean class, but
        // will dramatically slow down every build that runs tests - We need to
        // either refactor or find a more creative way to test Clean
//        Thread.sleep(25000);

        persist.close();
    }

    private class PersistStub extends Persist<Integer, Persistable<Integer>> {
        public PersistStub(Access access, RosettaEnv env, Class<Integer> cls, String sub_dir)
                throws CadiException, APIException { super(access, env, cls, sub_dir); }
        @Override
        protected Persistable<Integer> newCacheable(Integer t, long expires_secsFrom1970, byte[] hash, Path path)
                throws APIException, IOException { return null; }
        @Override
        public<T> Path writeDisk(final RosettaDF<T> df, final T t, final byte[] cred, final Path target, final long expires) throws CadiException {
            return null;
        }
        @SuppressWarnings("unchecked")
        @Override
        public <T> T readDisk(final RosettaDF<T> df, final byte[] cred, final String filename,final Holder<Path> hp, final Holder<Long> hl) throws CadiException {
            return (T)new Integer(data);
        }

    }

}
