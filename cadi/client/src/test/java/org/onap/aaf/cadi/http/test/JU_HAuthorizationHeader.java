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

import org.junit.*;
import org.mockito.*;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.config.SecurityInfoC;

import org.onap.aaf.cadi.http.HAuthorizationHeader;

public class JU_HAuthorizationHeader {

    @Mock
    SecurityInfoC<HttpURLConnection> siMock;

    @Mock
    HttpsURLConnection hucsMock;

    @Mock
    HttpURLConnection hucMock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test() throws IOException, CadiException {
        HAuthorizationHeader header = new HAuthorizationHeader(siMock, null, null);
        header.setSecurity(hucsMock);
        header.setSecurity(hucMock);

        header = new HAuthorizationHeader(null, null, null);
        header.setSecurity(hucsMock);
    }

    @Test(expected = CadiException.class)
    public void throwsWhenDeniedTest() throws CadiException, IOException {
        HAuthorizationHeader header = new HAuthorizationHeader(siMock, "string1", "string2") {
            @Override public boolean isDenied() { return true; }
        };
        header.setSecurity(null);
    }

    @Test(expected = CadiException.class)
    public void throwsTest() throws CadiException, IOException {
        HAuthorizationHeader header = new HAuthorizationHeader(siMock, "string1", "string2") {
            @Override public String headValue() throws IOException { throw new IOException(); }
        };
        header.setSecurity(null);
    }

}
