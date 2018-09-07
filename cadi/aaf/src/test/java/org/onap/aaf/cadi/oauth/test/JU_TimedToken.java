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

package org.onap.aaf.cadi.oauth.test;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.oauth.TimedToken;
import org.onap.aaf.cadi.persist.Persist;

import aafoauth.v2_0.Token;

public class JU_TimedToken {

    private static final byte[] hash = "hashstring".getBytes();

    private static final int expires = 10000;

    private Path path;

    @Mock private Persist<Token, ?> persistMock;
    @Mock private Token tokenMock;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        when(tokenMock.getExpiresIn()).thenReturn(expires);
        path = Files.createTempFile("fake", ".txt");
    }

    @Test
    public void test() {
        int actuallyExpires = ((int)(System.currentTimeMillis() / 1000)) + expires;
        TimedToken ttoken = new TimedToken(persistMock, tokenMock, hash, path);

        assertThat(ttoken.get(), is(tokenMock));
        assertThat(ttoken.checkSyncTime(), is(true));
        assertThat(ttoken.checkReloadable(), is(false));
        assertThat(ttoken.hasBeenTouched(), is(false));
        assertThat(Math.abs(ttoken.expires() - actuallyExpires) < 10, is(true));
        assertThat(ttoken.expired(), is(false));

        assertThat(ttoken.match(hash), is(true));
        assertThat(ttoken.getHash(), is(hash));

        assertThat(ttoken.path(), is(path));

        assertThat(ttoken.count(), is(0));
        ttoken.inc();
        assertThat(ttoken.count(), is(1));
        ttoken.clearCount();
        assertThat(ttoken.count(), is(0));
    }

}
