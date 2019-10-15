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

package org.onap.aaf.cadi.http.test;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.*;
import org.mockito.*;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.config.SecurityInfoC;

import org.onap.aaf.cadi.http.HTransferSS;
import org.onap.aaf.cadi.principal.TaggedPrincipal;

public class JU_HTransferSS {

    @Mock
    TaggedPrincipal princMock;

    @Mock
    HttpURLConnection hucMock;

    @Mock
    HttpsURLConnection hucsMock;

    @Mock
    SecurityInfoC<HttpURLConnection> siMock;

    @Mock
    SecurityInfoC<HttpURLConnection> siMockNoDefSS;

    @Mock
    SecuritySetter<HttpURLConnection> ssMock;

    private static final String princName = "name";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(princMock.getName()).thenReturn(princName);
        siMock.defSS = ssMock;
    }

    @Test
    public void test() throws IOException, CadiException {
        HTransferSS transfer = new HTransferSS(princMock, "string1");
        assertThat(transfer.setLastResponse(0), is(0));

        transfer = new HTransferSS(princMock, "string1", siMock);
        transfer.setSecurity(hucsMock);
        assertThat(transfer.getID(), is(princName));

        transfer = new HTransferSS(null, "string1", siMock);
        transfer.setSecurity(hucsMock);
        assertThat(transfer.getID(), is(""));
    }

    @Test(expected = CadiException.class)
    public void testThrows() throws CadiException {
        HTransferSS transfer = new HTransferSS(princMock, "string1", siMockNoDefSS);
        transfer.setSecurity(hucMock);
    }

}
