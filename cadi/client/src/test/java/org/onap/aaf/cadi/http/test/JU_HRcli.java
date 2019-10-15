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

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.*;
import org.mockito.*;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.EClient;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.cadi.http.HRcli;

public class JU_HRcli {

    @Mock
    SecuritySetter<HttpURLConnection> ssMock;

    @Mock
    Locator<URI> locMock;

    @Mock
    Locator.Item itemMock;

    private HMangr hman;
    private PropAccess access;
    private static URI uri;

    private static final String uriString = "example.com";

    @Before
    public void setup() throws LocatorException, URISyntaxException {
        MockitoAnnotations.initMocks(this);

        access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
        hman = new HMangr(access, locMock);
        uri = new URI(uriString);

        when(locMock.get(itemMock)).thenReturn(uri);
    }

    @Test(expected = CadiException.class)
    public void publicInterfaceTest() throws URISyntaxException, LocatorException, CadiException {
        HRcli hrcli = new HRcli(hman, itemMock, ssMock);
        assertThat(hrcli.setManager(hman), is(hrcli));
        assertThat(hrcli.toString(), is(uriString));

        hrcli.setSecuritySetter(ssMock);
        assertThat(hrcli.getSecuritySetter(), is(ssMock));

        // No throw
        hrcli.invalidate();
        // Throw
        doThrow(CadiException.class).when(locMock).invalidate(itemMock);
        hrcli.invalidate();
    }

    @Test(expected = CadiException.class)
    public void protectedInterfaceTest() throws CadiException, LocatorException {
        HRcliStub hrcli = new HRcliStub(hman, uri, itemMock, ssMock);
        HRcli clone = hrcli.clone(uri, ssMock);
        assertThat(clone.toString(), is(hrcli.toString()));

        EClient<HttpURLConnection> eclient = hrcli.client();
        assertThat(eclient, is(not(nullValue())));

        hrcli = new HRcliStub(hman, null, itemMock, ssMock);
        when(locMock.best()).thenReturn(itemMock);
        eclient = hrcli.client();
        assertThat(eclient, is(not(nullValue())));

        hrcli = new HRcliStub(hman, null, itemMock, ssMock);
        when(locMock.best()).thenReturn(null);
        eclient = hrcli.client();
    }

    private class HRcliStub extends HRcli {
        public HRcliStub(HMangr hman, URI uri, Item locItem, SecuritySetter<HttpURLConnection> secSet) {
            super(hman, uri, locItem, secSet);
        }
        public HRcli clone(URI uri, SecuritySetter<HttpURLConnection> ss) {
            return super.clone(uri, ss);
        }
        public EClient<HttpURLConnection> client() throws CadiException {
            return super.client();
        }
    }

}
