/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.cadi.client.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.*;
import org.mockito.*;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.AbsTransferSS;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.principal.TaggedPrincipal;

import java.net.HttpURLConnection;

public class JU_AbsTransferSS {

    @Mock TaggedPrincipal princMock;
    @Mock SecurityInfoC<HttpURLConnection> siMock;

    private static final String princName = "name";
    private static final String princTag = "tag";
    private static final String app = "app";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    
        when(princMock.getName()).thenReturn(princName);
        when(princMock.tag()).thenReturn(princTag);
    }

    @Test
    public void test() {
        TransferSSStub stub = new TransferSSStub(princMock, app);
        assertThat(stub.getID(), is(princName));
        assertThat(stub.getValue(), is(princName + ':' + app + ':' + princTag + ':' + "AS"));
    
        stub = new TransferSSStub(null, app, siMock);
        assertThat(stub.getID(), is(""));
        assertThat(stub.getValue(), is(nullValue()));
    }

    private class TransferSSStub extends AbsTransferSS<HttpURLConnection> {
        public TransferSSStub(TaggedPrincipal principal, String app) { super(principal, app); }
        public TransferSSStub(TaggedPrincipal principal, String app, SecurityInfoC<HttpURLConnection> si) { super(principal, app, si); }
        @Override public void setSecurity(HttpURLConnection client) throws CadiException { }
        @Override public int setLastResponse(int respCode) { return 0; }
        public String getValue() { return value; }
    }

}
