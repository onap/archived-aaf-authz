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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.EClient;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;

public class JU_Rcli {

    @Mock RosettaDF<HttpURLConnection> dfMock;
    @Mock RosettaData<HttpURLConnection> dataMock;
    @Mock HttpURLConnection conMock;
    @Mock HttpServletRequest reqMock;
    @Mock HttpServletResponse respMock;
    @Mock ServletInputStream isMock;

    private final static String uriString = "example.com";
    private final static String apiVersion = "v1.0";
    private final static String contentType = "contentType";

    private static URI uri;
    private static Enumeration<String> enumeration;

    private Client client;

    @Before
    public void setup() throws URISyntaxException, IOException {
        MockitoAnnotations.initMocks(this);
    
        when(dfMock.getTypeClass()).thenReturn(HttpURLConnection.class);
        when(dfMock.newData()).thenReturn(dataMock);
        when(dataMock.out((TYPE) any())).thenReturn(dataMock);
    
        when(reqMock.getInputStream()).thenReturn(isMock);
        when(isMock.read((byte[]) any())).thenReturn(-1);

        uri = new URI(uriString);
        enumeration = new CustomEnumeration();
        client = new Client();
    }

    @Test
    public void createTest() throws APIException, CadiException {
        RcliStub rcli = new RcliStub(uri);
        rcli.type(Data.TYPE.XML);

        rcli.create(null, contentType, dfMock, conMock);
        rcli.create("No question mark", contentType, dfMock, conMock);
        rcli.create("question?mark", contentType, dfMock, conMock);

        rcli.create(null, dfMock, conMock);
        rcli.create("No question mark", dfMock, conMock);
        rcli.create("question?mark", dfMock, conMock);

        rcli.create(null, HttpURLConnection.class, dfMock, conMock);
        rcli.create("No question mark", HttpURLConnection.class, dfMock, conMock);
        rcli.create("question?mark", HttpURLConnection.class, dfMock, conMock);

        rcli.create(null, HttpURLConnection.class);
        rcli.create("No question mark", HttpURLConnection.class);
        rcli.create("question?mark", HttpURLConnection.class);

        rcli.create(null, contentType);
        rcli.create("No question mark", contentType);
        rcli.create("question?mark", contentType);
    }

    @Test
    public void postFormTest() throws APIException, CadiException {
        RcliStub rcli = new RcliStub(uri);

        rcli.type(Data.TYPE.DEFAULT);
        rcli.postForm(null, dfMock);
        rcli.postForm("No question mark", dfMock);
        rcli.postForm("question?mark", dfMock);
    
        rcli.type(Data.TYPE.JSON);
        rcli.postForm("question?mark", dfMock);

        rcli.type(Data.TYPE.XML);
        rcli.postForm("question?mark", dfMock);

    }

    @Test
    public void readPostTest() throws APIException, CadiException {
        RcliStub rcli = new RcliStub(uri);
        rcli.type(Data.TYPE.DEFAULT);

        rcli.readPost(null, dfMock, conMock);
        rcli.readPost("No question mark", dfMock, conMock);
        rcli.readPost("question?mark", dfMock, conMock);

        rcli.readPost(null, dfMock, conMock, dfMock);
        rcli.readPost("No question mark", dfMock, conMock, dfMock);
        rcli.readPost("question?mark", dfMock, conMock, dfMock);

        rcli.readPost("First string", "Second string");
    }

    @Test
    public void readTest() throws APIException, CadiException {
        RcliStub rcli = new RcliStub(uri);
        rcli.type(Data.TYPE.DEFAULT);

        rcli.read("First string", "Second string", "Third string", "Fourth string");
        rcli.read("First string", "Second string", dfMock, "Third string", "Fourth string");
        rcli.read("First string", dfMock, "Third string", "Fourth string");
        rcli.read("First string", HttpURLConnection.class ,dfMock);
    }

    @Test
    public void updateTest() throws APIException, CadiException {
        RcliStub rcli = new RcliStub(uri);
        rcli.type(Data.TYPE.DEFAULT);

        rcli.update("First string", "Second string", dfMock, conMock);
        rcli.update("First string", dfMock, conMock);
        rcli.update("First string", HttpURLConnection.class, dfMock, conMock);
        rcli.update("First string");
        rcli.updateRespondString("First string", dfMock, conMock);
    }

    @Test
    public void deleteTest() throws APIException, CadiException {
        RcliStub rcli = new RcliStub(uri);
        rcli.type(Data.TYPE.DEFAULT);

        rcli.delete("First string", "Second string", dfMock, conMock);
        rcli.delete("First string", dfMock, conMock);
        rcli.delete("First string", HttpURLConnection.class, dfMock, conMock);
        rcli.delete("First string", HttpURLConnection.class);
        rcli.delete("First string", "Second string");
    }

    @Test
    public void transferTest() throws APIException, CadiException {
        RcliStub rcli = new RcliStub(uri);
        rcli.type(Data.TYPE.DEFAULT);

        when(reqMock.getRequestURI()).thenReturn(uriString);
        when(reqMock.getHeaderNames()).thenReturn(enumeration);
        rcli.transfer(reqMock, respMock, "string", 200);
    
        // coverage...
        when(reqMock.getMethod()).thenReturn("GET");
        rcli.transfer(reqMock, respMock, "string", 200);
    }

    @Test(expected = CadiException.class)
    public void transferThrowsTest() throws APIException, CadiException {
        RcliStub rcli = new RcliStub(uri);
        rcli.type(Data.TYPE.DEFAULT);

        rcli.transfer(reqMock, respMock, "string", 200);
    }

    @Test
    public void accessorMutatorTest() throws URISyntaxException {
        RcliStub rcli = new RcliStub();
        Rcli<?> rcliClone = rcli.forUser(null);
    
        rcli = new RcliStub(uri);
        assertThat(rcli.toString(), is(uriString));
        assertThat(rcli.getURI(), is(uri));
        assertThat(rcli.getReadTimeout(), is(5000));
        assertThat(rcli.getConnectionTimeout(), is(3000));
    
        rcli.connectionTimeout(3001);
        assertThat(rcli.getConnectionTimeout(), is(3001));
        rcli.readTimeout(5001);
        assertThat(rcli.getReadTimeout(), is(5001));
        rcli.apiVersion(apiVersion);
        assertThat(rcli.isApiVersion(apiVersion), is(true));
        rcli.type(Data.TYPE.XML);
        assertThat(rcli.typeString(HttpURLConnection.class), is("application/HttpURLConnection+xml;version=" + apiVersion));
        rcli.apiVersion(null);
        assertThat(rcli.typeString(HttpURLConnection.class), is("application/HttpURLConnection+xml"));
    
        rcliClone = rcli.forUser(null);
        assertThat(rcliClone.toString(), is(uriString));
    }

    private class RcliStub extends Rcli<HttpURLConnection> {
        public RcliStub() { super(); }
        public RcliStub(URI uri) { this.uri = uri; }
        @Override public void setSecuritySetter(SecuritySetter<HttpURLConnection> ss) { } 
        @Override public SecuritySetter<HttpURLConnection> getSecuritySetter() { return null; } 
        @Override protected Rcli<HttpURLConnection> clone(URI uri, SecuritySetter<HttpURLConnection> ss) { return this; } 
        @Override public void invalidate() throws CadiException { } 
        @Override protected EClient<HttpURLConnection> client() throws CadiException { return client; } 
        public int getReadTimeout() { return readTimeout; }
        public int getConnectionTimeout() { return connectionTimeout; }
    }

    private class CustomEnumeration implements Enumeration<String> {
        private int idx = 0;
        private final String[] elements = {"This", "is", "a", "test"};
        @Override
        public String nextElement() {
            return idx >= elements.length ? null : elements[idx++];
        }
        @Override
        public boolean hasMoreElements() {
            return idx < elements.length;
        }
    }

    private class Client implements EClient<HttpURLConnection> {
        private Transfer transfer;
        @Override public void setPayload(Transfer transfer) { this.transfer = transfer; }
        @Override public void setMethod(String meth) { } 
        @Override public void setPathInfo(String pathinfo) { } 
        @Override public void addHeader(String tag, String value) { } 
        @Override public void setQueryParams(String q) { } 
        @Override public void setFragment(String f) { } 
        @Override public void send() throws APIException {
            try {
                if (transfer != null) {
                    transfer.transfer(new PrintStream(new ByteArrayOutputStream()));
                }
            } catch (IOException e) {
            }
        } 
        @Override public <T> Future<T> futureCreate(Class<T> t) { return null; } 
        @Override public Future<String> futureReadString() { return null; } 
        @Override public <T> Future<T> futureRead(RosettaDF<T> df, TYPE type) { return null; } 
        @Override public <T> Future<T> future(T t) { return null; } 
        @Override public Future<Void> future(HttpServletResponse resp, int expected) throws APIException { return null; } 
    }

    //private class FutureStub implements Future<String> {
    //}
}
