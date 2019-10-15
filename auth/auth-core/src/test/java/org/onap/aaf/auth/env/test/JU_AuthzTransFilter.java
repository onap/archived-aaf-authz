/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * <p>
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/

package org.onap.aaf.auth.env.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import org.mockito.*;

import java.security.Principal;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransFilter;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Connector;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.TrustChecker;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.Trans.Metric;

public class JU_AuthzTransFilter {

    @Mock private AuthzEnv envMock;
    @Mock private Connector connectorMock;
    @Mock private TrustChecker tcMock;
    @Mock private AuthzTrans authzTransMock;
    @Mock private Object additionalTafLurs;

    private PropAccess access;

    @Before
    public void setUp() throws CadiException{
        MockitoAnnotations.initMocks(this);

        access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);

        when(envMock.access()).thenReturn(access);
    }

    // TODO: These tests only work on the AT&T network. Fix them - Ian
    @Test
    public void testAuthenticated() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, CadiException {
//        AuthzTransFilter filter = new AuthzTransFilter(envMock, connectorMock, tcMock);
//        AuthzTransFilter aTF = new AuthzTransFilter(authzEnvMock, connectorMock, trustCheckerMock, (Object)null);
//        Class<?> c = aTF.getClass();
//        Class<?>[] cArg = new Class[2];
//        cArg[0] = AuthzTrans.class;
//        cArg[1] = Principal.class;        //Steps to test a protected method
//        Method authenticatedMethod = c.getDeclaredMethod("authenticated", cArg);
//        authenticatedMethod.setAccessible(true);
//        authenticatedMethod.invoke(aTF, authzTransMock, null);
    }

    @Test
    public void testTallyHo() throws CadiException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        Slot specialLogSlot = authzEnvMock.slot("SPECIAL_LOG_SLOT");
//        LogTarget lt = mock(LogTarget.class);
//        AuthzTransFilter aTF = new AuthzTransFilter(authzEnvMock, connectorMock, trustCheckerMock, additionalTafLurs);
//        TaggedPrincipal tPrin = mock(TaggedPrincipal.class);
//        Metric met = new Metric();
//        met.total = 199.33F;
//        met.entries = 15;
//        met.buckets = new float[] {199.33F,99.33F};
//        Class<?> c = aTF.getClass();
//        Class<?>[] cArg = new Class[1];
//        cArg[0] = AuthzTrans.class;        //Steps to test a protected method
//        Method tallyHoMethod = c.getDeclaredMethod("tallyHo", cArg);
//
//        when(authzTransMock.auditTrail(((LogTarget)any()), anyInt(), (StringBuilder)any(), anyInt(), anyInt())).thenReturn(met);
//        tallyHoMethod.setAccessible(true);
//
//        when(authzTransMock.get(specialLogSlot, false)).thenReturn(false);
//        when(authzTransMock.warn()).thenReturn(lt);
//        when(authzTransMock.info()).thenReturn(lt);
//        tallyHoMethod.invoke(aTF, authzTransMock);
//
//        when(authzTransMock.getUserPrincipal()).thenReturn(tPrin);
//        tallyHoMethod.invoke(aTF, authzTransMock);
    }

}
