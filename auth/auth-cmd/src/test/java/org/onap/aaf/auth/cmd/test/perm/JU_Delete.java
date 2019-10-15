/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * *
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/

package org.onap.aaf.auth.cmd.test.perm;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.hamcrest.core.AnyOf;
import org.junit.Before;

import org.onap.aaf.auth.cmd.test.HMangrStub;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.perm.Delete;
import org.onap.aaf.auth.cmd.perm.Perm;
import org.onap.aaf.auth.cmd.role.Role;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.misc.env.APIException;

@RunWith(MockitoJUnitRunner.class)
public class JU_Delete {

    @Mock private SecuritySetter<HttpURLConnection> ssMock;
    @Mock private Locator<URI> locMock;
    @Mock private Writer wrtMock;
    @Mock private Rcli<HttpURLConnection> clientMock;
    @Mock private Future<String> futureMock;

    private static Delete del;

    private PropAccess access;
    private HMangrStub hman;
    private AuthzEnv aEnv;
    private AAFcli aafcli;

    @Before
    public void setUp() throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
        MockitoAnnotations.initMocks(this);

        when(clientMock.create(any(), any(), any(String.class))).thenReturn(futureMock);
        when(clientMock.delete(any(), any(), any(String.class))).thenReturn(futureMock);
        when(clientMock.update(any(), any(), any(String.class))).thenReturn(futureMock);

        hman = new HMangrStub(access, locMock, clientMock);
        access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
        aEnv = new AuthzEnv();
        aafcli = new AAFcli(access, aEnv, wrtMock, hman, null, ssMock);

        Role role = new Role(aafcli);
        Perm perm = new Perm(role);

        del = new Delete(perm);
    }

    @Test
    public void testExecError() throws APIException, LocatorException, CadiException, URISyntaxException {
        del._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
    }

    @Test
    public void testExecSuccess1() throws APIException, LocatorException, CadiException, URISyntaxException {
        when(futureMock.code()).thenReturn(202);
        del._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
    }

    @Test
    public void testExecSuccess2() throws APIException, LocatorException, CadiException, URISyntaxException {
        when(futureMock.get(any(Integer.class))).thenReturn(true);
        del._exec(0, new String[] {"grant","ungrant","setTo","grant","ungrant","setTo"});
    }

    @Test
    public void testDetailedHelp() {
        StringBuilder sb = new StringBuilder();
        del.detailedHelp(0, sb);
    }

}
