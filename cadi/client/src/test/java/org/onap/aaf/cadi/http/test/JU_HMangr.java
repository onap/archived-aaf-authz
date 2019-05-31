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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLHandshakeException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.misc.env.APIException;

import junit.framework.Assert;

public class JU_HMangr {
    
    @Mock Locator<URI> locMock;
    @Mock SecuritySetter<HttpURLConnection> ssMock;
    @Mock Retryable<Void> retryableMock;
    @Mock Retryable<Integer> goodRetry;
    @Mock Locator.Item itemMock;
    @Mock Rcli<Object> clientMock;
    
    private PropAccess access;
    private URI uri;
    private final static String uriString = "http://example.com";

    @Before
    public void setup() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);

        access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
        uri = new URI(uriString);
    }

    @Test
    public void sameTest() throws LocatorException, APIException, CadiException, ConnectException {
        HMangr hman = new HMangr(access, locMock);
        when(retryableMock.item()).thenReturn(itemMock);
        when(locMock.get(itemMock)).thenReturn(uri);
        assertThat(hman.same(ssMock, retryableMock), is(nullValue()));
        
        //coverage...
        when(retryableMock.lastClient()).thenReturn(clientMock);
        assertThat(hman.same(ssMock, retryableMock), is(nullValue()));
        
        CadiException cadiException;

        ConnectException connectException = new ConnectException();
        cadiException = new CadiException(connectException);
        doThrow(cadiException).when(retryableMock).code(clientMock);
        when(locMock.hasItems()).thenReturn(true).thenReturn(false);
        assertThat(hman.same(ssMock, retryableMock), is(nullValue()));

        SocketException socketException = new SocketException();
        cadiException = new CadiException(socketException);
        doThrow(cadiException).when(retryableMock).code(clientMock);
        when(locMock.hasItems()).thenReturn(true).thenReturn(false);
        assertThat(hman.same(ssMock, retryableMock), is(nullValue()));

        doThrow(connectException).when(retryableMock).code(clientMock);
        assertThat(hman.same(ssMock, retryableMock), is(nullValue()));

    }

    @Test(expected = LocatorException.class)
    public void throwsLocatorException1Test() throws LocatorException {
        @SuppressWarnings("unused")
        HMangr hman = new HMangr(access, null);
    }

    @Test(expected = LocatorException.class)
    public void throwsLocatorException2Test() throws LocatorException, APIException, CadiException {
        HMangr hman = new HMangr(access, locMock);
        hman.same(ssMock, retryableMock);
    }

    @Test(expected = LocatorException.class)
    public void throwsLocatorException3Test() throws LocatorException, APIException, CadiException {
        HMangr hman = new HMangr(access, locMock);
        when(locMock.best()).thenReturn(itemMock);
        when(locMock.hasItems()).thenReturn(true).thenReturn(false);
        hman.same(ssMock, retryableMock);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = CadiException.class)
    public void throwsCadiException1Test() throws LocatorException, APIException, CadiException, ConnectException {
        HMangr hman = new HMangr(access, locMock);
        when(retryableMock.item()).thenReturn(itemMock);
        when(locMock.get(itemMock)).thenReturn(uri);
        when(retryableMock.lastClient()).thenReturn(clientMock);
        when(retryableMock.code(clientMock)).thenThrow(CadiException.class);
        hman.same(ssMock, retryableMock);
    }

    @Test(expected = CadiException.class)
    public void throwsCadiException2Test() throws LocatorException, APIException, CadiException, ConnectException {
        HMangr hman = new HMangr(access, locMock);
        when(retryableMock.item()).thenReturn(itemMock);
        when(locMock.get(itemMock)).thenReturn(uri);
        when(retryableMock.lastClient()).thenReturn(clientMock);

        ConnectException connectException = new ConnectException();
        CadiException cadiException = new CadiException(connectException);
        doThrow(cadiException).when(retryableMock).code(clientMock);
        hman.same(ssMock, retryableMock);
    }

    @Test(expected = CadiException.class)
    public void throwsCadiException3Test() throws LocatorException, APIException, CadiException, ConnectException {
        HMangr hman = new HMangr(access, locMock);
        when(retryableMock.item()).thenReturn(itemMock);
        when(locMock.get(itemMock)).thenReturn(uri);
        when(retryableMock.lastClient()).thenReturn(clientMock);

        SocketException socketException = new SocketException();
        CadiException cadiException = new CadiException(socketException);
        doThrow(cadiException).when(retryableMock).code(clientMock);
        hman.same(ssMock, retryableMock);
    }

    @Test(expected = CadiException.class)
    public void throwsCadiException4Test() throws LocatorException, APIException, CadiException, ConnectException {
        HMangr hman = new HMangr(access, locMock);
        when(retryableMock.item()).thenReturn(itemMock);
        when(locMock.get(itemMock)).thenReturn(uri);
        when(retryableMock.lastClient()).thenReturn(clientMock);

        Exception e = new Exception();
        CadiException cadiException = new CadiException(e);
        doThrow(cadiException).when(retryableMock).code(clientMock);
        hman.same(ssMock, retryableMock);
    }

    @Test
    public void allTest() throws LocatorException, CadiException, APIException {
        HManagerStub hman = new HManagerStub(access, locMock);

        assertThat(hman.best(ssMock, retryableMock), is(nullValue()));
        try {
        	hman.all(ssMock, retryableMock, true);
        	Assert.fail("Should have thrown LocatorException");
        } catch (LocatorException e) {
        	assertEquals(e.getLocalizedMessage(),"No available clients to call");
        }
    }

    @Test
    public void oneOfTest() throws LocatorException, CadiException, APIException, ConnectException {
        HMangr hman = new HMangr(access, locMock);
        assertThat(hman.oneOf(ssMock, retryableMock, false, "host"), is(nullValue()));

        try {
            hman.oneOf(ssMock, retryableMock, true, "host");
            fail("Should've thrown an exception");
        } catch (LocatorException e) {
        }

        when(locMock.first()).thenReturn(itemMock);
        when(locMock.get(itemMock)).thenReturn(uri);

        // Branching coverage...
        assertThat(hman.oneOf(ssMock, retryableMock, false, null), is(nullValue()));
        assertThat(hman.oneOf(ssMock, retryableMock, false, "host"), is(nullValue()));

        assertThat(hman.oneOf(ssMock, retryableMock, false, uriString.substring(7)), is(nullValue()));
        
        CadiException cadiException;

        cadiException = new CadiException(new ConnectException());
        doThrow(cadiException).when(retryableMock).code((Rcli<?>) any());
        assertThat(hman.oneOf(ssMock, retryableMock, false, uriString.substring(7)), is(nullValue()));

        cadiException = new CadiException(new SSLHandshakeException(null));
        doThrow(cadiException).when(retryableMock).code((Rcli<?>) any());
        assertThat(hman.oneOf(ssMock, retryableMock, false, uriString.substring(7)), is(nullValue()));

        cadiException = new CadiException(new SocketException());
        doThrow(cadiException).when(retryableMock).code((Rcli<?>) any());
        try {
            hman.oneOf(ssMock, retryableMock, false, uriString.substring(7));
            fail("Should've thrown an exception");
        } catch (CadiException e) {
        }

        cadiException = new CadiException(new SocketException("java.net.SocketException: Connection reset"));
        doThrow(cadiException).when(retryableMock).code((Rcli<?>) any());
        try {
            hman.oneOf(ssMock, retryableMock, false, uriString.substring(7));
            fail("Should've thrown an exception");
        } catch (CadiException e) {
        }

        cadiException = new CadiException();
        doThrow(cadiException).when(retryableMock).code((Rcli<?>) any());
        try {
            hman.oneOf(ssMock, retryableMock, false, uriString.substring(7));
            fail("Should've thrown an exception");
        } catch (CadiException e) {
        }
        
        doThrow(new ConnectException()).when(retryableMock).code((Rcli<?>) any());
        assertThat(hman.oneOf(ssMock, retryableMock, false, uriString.substring(7)), is(nullValue()));

        when(goodRetry.code((Rcli<?>) any())).thenReturn(5);
        assertThat(hman.oneOf(ssMock, goodRetry, false, uriString.substring(7)), is(5));
    }

    @Test
    public void coverageTest() throws LocatorException {
        HMangr hman = new HMangr(access, locMock);
        hman.readTimeout(5);
        assertThat(hman.readTimeout(), is(5));
        hman.connectionTimeout(5);
        assertThat(hman.connectionTimeout(), is(5));
        hman.apiVersion("v1.0");
        assertThat(hman.apiVersion(), is("v1.0"));
        hman.close();

    }

    private class HManagerStub extends HMangr {
        public HManagerStub(Access access, Locator<URI> loc) throws LocatorException { super(access, loc); }
        @Override public<RET> RET same(SecuritySetter<HttpURLConnection> ss, Retryable<RET> retryable) {
            return null;
        }
        @Override public<RET> RET oneOf(SecuritySetter<HttpURLConnection> ss, Retryable<RET> retryable, boolean notify, String host) {
            return null;
        }
    }
        
}