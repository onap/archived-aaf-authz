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

package org.onap.aaf.cadi.client.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.*;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.AbsAuthentication;
import org.onap.aaf.cadi.config.SecurityInfoC;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;

public class JU_AbsAuthentication {

    private final static String ID = "id";
    private final static String PASSWORD = "password";
    private final static String WARNING = "Your service has 1000 consecutive bad service " +
                                            "logins to AAF. AAF Access will be disabled after 10000\n";

    private static ByteArrayOutputStream errStream;

    @Before
    public void setup() {
        errStream = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errStream));
    }

    @After
    public void tearDown() {
        System.setErr(System.err);
    }

    @Test
    public void test() throws IOException, InterruptedException {
        AuthStub stub = new AuthStub(null, null, null);
        assertThat(stub.getID(), is(nullValue()));
        assertThat(stub.headValue(), is(""));
        assertThat(stub.count(), is(0));
    
        stub.setUser(ID);
        assertThat(stub.getID(), is(ID));

        stub = new AuthStub(null, ID, PASSWORD.getBytes());
        assertThat(stub.getID(), is(ID));
        assertThat(stub.headValue(), is(PASSWORD));
        assertThat(stub.count(), is(0));
    
        assertThat(stub.setLastResponse(200), is(0));
        assertThat(stub.isDenied(), is(false));

        for (int i = 1; i <= 10; i++) {
            assertThat(stub.setLastResponse(401), is(i));
            assertThat(stub.isDenied(), is(false));
        }
        assertThat(stub.setLastResponse(401), is(11));
        assertThat(stub.isDenied(), is(true));

        stub.setCount(999);
        assertThat(stub.setLastResponse(401), is(1000));
        assertThat(errStream.toString(), is(WARNING));
    
        // coverage...
        stub.setLastMiss(1);
        assertThat(stub.isDenied(), is(false));
    }

    private class AuthStub extends AbsAuthentication<HttpURLConnection> {

        public AuthStub(SecurityInfoC<HttpURLConnection> securityInfo, String user, byte[] headValue)
                throws IOException { super(securityInfo, user, headValue); }

        @Override public void setSecurity(HttpURLConnection client) throws CadiException { }
        @Override public void setUser(String id) { super.setUser(id); }
        @Override public String headValue() throws IOException { return super.headValue(); }
    
        public void setLastMiss(long lastMiss) { this.lastMiss = lastMiss; }
        public void setCount(int count) { this.count = count; }
    }

}
