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
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.CoreMatchers.*;
import org.junit.*;
import org.mockito.*;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.EClient.Transfer;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.http.HClient;
import org.onap.aaf.cadi.http.HClient.HFuture;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;

public class JU_HClient {

    @Mock private SecuritySetter<HttpURLConnection> ssMock;
    @Mock private Transfer transferMock;
    @Mock private HttpURLConnection hucMock;
    @Mock private HttpServletResponse respMock;
    @Mock private RosettaDF<HttpURLConnection> dfMock;
    @Mock private RosettaData<HttpURLConnection> dataMock;

    private static final String uriString = "http://example.com:8080/path/to/a/file.txt";
    private static final String fragment = "fragment";
    private static final String method = "method";
    private static final String pathinfo = "pathinfo";
    private static final String queryParams = "queryParams";

    private static final String errorString = "error string";
    private static final String successString = "success string";

    private static final String tag1 = "tag1";
    private static final String tag2 = "tag2";
    private static final String value1 = "value1";
    private static final String value2 = "value2";

    private URI uri;

    @Before
    public void setup() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);

        uri = new URI(uriString);
    }

    @Test
    public void accessorsMutatorsTest() throws LocatorException {
        HClient client = new HClient(ssMock, uri, 0);
        client.setFragment(fragment);
        client.setMethod(method);
        client.setPathInfo(pathinfo);
        client.setPayload(transferMock);
        client.setQueryParams(queryParams);
        assertThat(client.getURI(), is(uri));
        assertThat(client.timeout(), is(0));
        assertThat(client.toString(), is("HttpURLConnection Client configured to " + uri.toString()));
    }

    @Test
    public void sendTest() throws LocatorException, APIException, URISyntaxException {
        HClientStub client;
        client = new HClientStub(ssMock, uri, 0, null);
        client.send();
    
        client.setPathInfo("/pathinfo");
        client.send();

        client.setPathInfo("pathinfo");
        client.send();

        client = new HClientStub(null, uri, 0, null);
        client.send();

        client.addHeader(tag1, value1);
        client.addHeader(tag2, value2);
        client.send();

        client.setPayload(transferMock);
        client.send();
    }

    @Test(expected = APIException.class)
    public void sendThrows1Test() throws APIException, LocatorException, URISyntaxException {
        HClientStub client = new HClientStub(ssMock, new URI("mailto:me@domain.com"), 0, null);
        client.send();
    }

    @Test(expected = APIException.class)
    public void sendThrows2Test() throws APIException, LocatorException, URISyntaxException {
        HClientStub client = new HClientStub(ssMock, new URI("mailto:me@domain.com"), 0, null);
        client.addHeader(tag1, value1);
        client.addHeader(tag2, value2);
        client.send();
    }

    @Test
    public void futureCreateTest() throws LocatorException, CadiException, IOException {
        HClient client = new HClientStub(ssMock, uri, 0, hucMock);
        HFuture<HttpURLConnection> future = (HFuture<HttpURLConnection>) client.futureCreate(HttpURLConnection.class);

        // Test a bad response code (default 0) without output
        assertThat(future.get(0), is(false));
        assertThat(future.body().length(), is(0));

        // Test a bad response code (default 0) with output
        ByteArrayInputStream bais = new ByteArrayInputStream(errorString.getBytes());
        when(hucMock.getInputStream()).thenReturn(bais);
        assertThat(future.get(0), is(false));
        assertThat(future.body(), is(errorString));

        // Test a good response code
        when(hucMock.getResponseCode()).thenReturn(201);
        assertThat(future.get(0), is(true));
    }

    @Test
    public void futureReadStringTest() throws LocatorException, CadiException, IOException {
        HClient client = new HClientStub(ssMock, uri, 0, hucMock);
        Future<String> future = client.futureReadString();

        // Test a bad response code (default 0) without output
        assertThat(future.get(0), is(false));
        assertThat(future.body().length(), is(0));

        // Test a bad response code (default 0) with output
        when(hucMock.getInputStream()).thenReturn(new ByteArrayInputStream(errorString.getBytes()));
        assertThat(future.get(0), is(false));
        assertThat(future.body(), is(errorString));

        // Test a good response code
        when(hucMock.getInputStream()).thenReturn(new ByteArrayInputStream(successString.getBytes()));
        when(hucMock.getResponseCode()).thenReturn(200);
        assertThat(future.get(0), is(true));
        assertThat(future.body(), is(successString));
    }

    @Test
    public void futureReadTest() throws LocatorException, CadiException, IOException, APIException {
        HClient client = new HClientStub(ssMock, uri, 0, hucMock);
        Future<HttpURLConnection> future = client.futureRead(dfMock, null);

        // Test a bad response code (default 0) without output
        assertThat(future.get(0), is(false));
        assertThat(future.body().length(), is(0));

        // Test a bad response code (default 0) with output
        when(hucMock.getInputStream()).thenReturn(new ByteArrayInputStream(errorString.getBytes()));
        assertThat(future.get(0), is(false));
        assertThat(future.body(), is(errorString));

        // Test a good response code
        when(hucMock.getInputStream()).thenReturn(new ByteArrayInputStream(successString.getBytes()));
        when(dfMock.newData()).thenReturn(dataMock);
        when(dataMock.in(null)).thenReturn(dataMock);
        when(dataMock.load((InputStream)any())).thenReturn(dataMock);
        when(dataMock.asObject()).thenReturn(hucMock);
        when(dataMock.asString()).thenReturn(successString);
        when(hucMock.getResponseCode()).thenReturn(200);
        assertThat(future.get(0), is(true));
        assertThat(future.body(), is(successString));
    }

    @Test
    public void future1Test() throws LocatorException, CadiException, IOException, APIException {
        HClient client = new HClientStub(ssMock, uri, 0, hucMock);
        Future<HttpURLConnection> future = client.future(hucMock);

        // Test a good response code
        when(hucMock.getInputStream()).thenReturn(new ByteArrayInputStream(successString.getBytes()));
        when(hucMock.getResponseCode()).thenReturn(200);
        assertThat(future.get(0), is(true));
        assertThat(future.body(), is("200"));

        // Test a bad response code
        when(hucMock.getResponseCode()).thenReturn(0);
        when(hucMock.getInputStream()).thenReturn(new ByteArrayInputStream(errorString.getBytes()));
        assertThat(future.get(0), is(false));
        assertThat(future.body(), is(errorString));
    }

    @Test
    public void future2Test() throws LocatorException, CadiException, IOException, APIException {
        HClient client = new HClientStub(ssMock, uri, 0, hucMock);
        Future<Void> future = client.future(respMock, 200);

        ServletOutputStream sos = new ServletOutputStream() {
            @Override public void write(int arg0) throws IOException { }
        };
        when(respMock.getOutputStream()).thenReturn(sos);

        // Test a good response code
        when(hucMock.getInputStream()).thenReturn(new ByteArrayInputStream(successString.getBytes()));
        when(hucMock.getResponseCode()).thenReturn(200);
        assertThat(future.get(0), is(true));
        assertThat(future.body(), is(nullValue()));

        // Test a bad response code
        when(hucMock.getResponseCode()).thenReturn(0);
        when(hucMock.getInputStream()).thenReturn(new ByteArrayInputStream(errorString.getBytes()));
        assertThat(future.get(0), is(false));
        assertThat(future.body(), is(""));
    }

    @Test
    public void hfutureTest() throws CadiException, IOException, LocatorException {
        HClient client = new HClientStub(ssMock, uri, 0, hucMock);
        HFutureStub future = new HFutureStub(client, hucMock);
        assertThat(future.get(0), is(false));

        // Test a bad response code (default 0) with output
        when(hucMock.getInputStream()).thenReturn(new ByteArrayInputStream(errorString.getBytes()));
        assertThat(future.get(0), is(false));

        assertThat(future.get(0), is(false));

        when(hucMock.getResponseCode()).thenReturn(200);
        assertThat(future.get(0), is(true));

        StringBuilder sb = future.inputStreamToString(new ByteArrayInputStream(errorString.getBytes()));
        assertThat(sb.toString(), is(errorString));

        assertThat(future.code(), is(200));
        assertThat(future.huc(), is(hucMock));

        assertThat(future.exception(), is(nullValue()));
        assertThat(future.header("string"), is(nullValue()));

        // coverage...
        future.setHuc(null);
        future.close();
    }

    @Test
    public void headerTest() throws LocatorException {
        HClient client = new HClientStub(ssMock, uri, 0, hucMock);
        String tag1 = "tag1";
        String tag2 = "tag2";
        String value1 = "value1";
        String value2 = "value2";
        client.addHeader(tag1, value1);
        client.addHeader(tag2, value2);
    }

    @Test(expected = LocatorException.class)
    public void throws1Test() throws LocatorException {
        @SuppressWarnings("unused")
        HClient client = new HClient(ssMock, null, 0);
    }

    private class HClientStub extends HClient {
        public HClientStub(SecuritySetter<HttpURLConnection> ss, URI uri, int connectTimeout, HttpURLConnection huc) throws LocatorException {
            super(ss, uri, connectTimeout);
            setHuc(huc);
        }
        public void setHuc(HttpURLConnection huc) {
            Field field;
            try {
                field = HClient.class.getDeclaredField("huc");
                field.setAccessible(true);
                field.set(this, huc);
                field.setAccessible(false);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
                fail("Caught an exception: " + e.getMessage());
            }
        }
        @Override
        public HttpURLConnection getConnection(URI uri, StringBuilder pi) throws IOException {
            return hucMock;
        }
    }

    private class HFutureStub extends HFuture<HttpURLConnection> {
        public HFutureStub(HClient hClient, HttpURLConnection huc) {
            hClient.super(huc);
        }

        @Override public String body() { return null; }
        public void setHuc(HttpURLConnection huc) { this.huc = huc; }
    }

}
