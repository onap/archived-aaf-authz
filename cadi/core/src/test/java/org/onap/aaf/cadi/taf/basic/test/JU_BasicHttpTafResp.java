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

package org.onap.aaf.cadi.taf.basic.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.cadi.taf.TafResp.RESP;
import org.onap.aaf.cadi.taf.basic.BasicHttpTafResp;

public class JU_BasicHttpTafResp {

    private final static String realm = "realm";
    private final static String description = "description";

    private PropAccess access;

    @Mock private HttpServletResponse respMock;
    @Mock private TaggedPrincipal princMock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
    }

    @Test
    public void test() throws IOException {
        BasicHttpTafResp tafResp = new BasicHttpTafResp(access, princMock, description, RESP.IS_AUTHENTICATED, respMock, realm, false);

        assertThat(tafResp.authenticate(), is(RESP.HTTP_REDIRECT_INVOKED));
        assertThat(tafResp.isAuthenticated(), is (RESP.IS_AUTHENTICATED));
        assertThat(tafResp.isFailedAttempt(), is(false));
    }

}
