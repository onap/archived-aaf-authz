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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.oauth.TimedToken;
import org.onap.aaf.cadi.oauth.TzHClient;
import org.onap.aaf.misc.env.APIException;

public class JU_TzHClient {
    
    @Mock private Retryable<Integer> retryableMock;
    @Mock private TimedToken tokenMock;
    @Mock private SecurityInfoC<HttpURLConnection> siMock;
    @Mock private Locator<URI> locMock;
    @Mock private Item itemMock;
    @Mock private Rcli<HttpURLConnection> clientMock;
    
    private PropAccess access;
    
    private ByteArrayOutputStream errStream;
    
    private final static String client_id = "id";
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
        access.setProperty(Config.CADI_LATITUDE, "38.62");  // St Louis approx lat
        access.setProperty(Config.CADI_LONGITUDE, "90.19");  // St Louis approx long
    	//access.setProperty("tag", "http://aaf.something.com");
    	
        errStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errStream));
    }
    
    @After
    public void tearDown() {
        System.setErr(System.err);
    }

    @Test
    public void test() throws CadiException, LocatorException, APIException, IOException {
        TzHClient client;
        try {
            client = new TzHClient(access, "tag");
        } catch (Exception e) {
            throw e;
        }
        try {
            client.best(retryableMock);
            fail("Should've thrown an exception");
        } catch (CadiException e) {
            assertThat(e.getMessage(), is("OAuth2 Token has not been set"));
        }
        client.setToken(client_id, tokenMock);
        when(tokenMock.expired()).thenReturn(true);
        try {
            client.best(retryableMock);
            fail("Should've thrown an exception");
        } catch (CadiException e) {
            assertThat(e.getMessage(), is("Expired Token"));
        }

        client = new TzHClient(access, siMock, locMock);
        when(tokenMock.expired()).thenReturn(false);
        doReturn(clientMock).when(retryableMock).lastClient();

        when(retryableMock.item()).thenReturn(itemMock);
        client.setToken(client_id, tokenMock);
        assertThat(client.best(retryableMock), is(nullValue()));
    }

}
