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

package org.onap.aaf.cadi.aaf.v2_0.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HClient;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.rosetta.env.RosettaDF;

import locate.v1_0.Endpoint;
import locate.v1_0.Endpoints;

public class JU_AAFLocator {

    @Mock private HClient clientMock;
    @Mock private Future<Endpoints> futureMock;
    @Mock private Endpoints endpointsMock;

    private PropAccess access;

    private ByteArrayOutputStream errStream;

    private static final String uriString = "https://example.com";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        doReturn(futureMock).when(clientMock).futureRead((RosettaDF<?>)any(), eq(TYPE.JSON));
        when(clientMock.timeout()).thenReturn(1);
        when(clientMock.getURI()).thenReturn(new URI(uriString));
        when(futureMock.get(1)).thenReturn(true);

        futureMock.value = endpointsMock;
        List<Endpoint> endpoints = new ArrayList<>();
        endpoints.add(new Endpoint());
        when(endpointsMock.getEndpoint()).thenReturn(endpoints);

        access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);

        errStream = new ByteArrayOutputStream();

        System.setErr(new PrintStream(errStream));
    }

    @After
    public void tearDown() {
        System.setErr(System.err);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        Field field = SecurityInfoC.class.getDeclaredField("sicMap");
        field.setAccessible(true);
        field.set(null, new HashMap<>());
    }

    @Test
    public void test() throws CadiException, URISyntaxException, LocatorException {
        access.setProperty(Config.CADI_LATITUDE, "38.62");  // St Louis approx lat
        access.setProperty(Config.CADI_LONGITUDE, "90.19");  // St Louis approx lon
        SecurityInfoC<HttpURLConnection> si = SecurityInfoC.instance(access, HttpURLConnection.class);
        URI locatorURI = new URI("https://somemachine.moc:10/com.att.aaf.service:2.0");
//        AbsAAFLocator<BasicTrans> al = new AAFLocator(si, locatorURI) {
//            @Override
//            protected HClient createClient(SecuritySetter<HttpURLConnection> ss, URI uri, int connectTimeout) throws LocatorException {
//                return clientMock;
//            }
//        };
        // Start over: This was originally calling a developer machine.
//        assertThat(al.refresh(), is(true));
//        when(futureMock.get(1)).thenReturn(false);
//        assertThat(al.refresh(), is(false));
//        String errorMessage = errStream.toString().split(": ", 2)[1];
//        assertThat(errorMessage, is("Error reading location information from " + uriString + ": 0 null\n \n"));
    }

}
