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

package org.onap.aaf.auth.env.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransFilter;
import org.onap.aaf.auth.env.AuthzTransOnlyFilter;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Connector;
import org.onap.aaf.cadi.TrustChecker;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.Trans.Metric;

@RunWith(MockitoJUnitRunner.class)
public class JU_AuthzTransOnlyFilter {
    AuthzTransFilter authzTransFilter;
    AuthzEnv authzEnvMock = mock(AuthzEnv.class);
    Connector connectorMock = mock(Connector.class);
    TrustChecker trustCheckerMock = mock(TrustChecker.class);
    AuthzTrans authzTransMock = mock(AuthzTrans.class);
    Object additionalTafLurs = mock(Object.class);
    ServletRequest servletRequestMock = mock(ServletRequest.class);
    AuthzTransOnlyFilter authzTransOnlyFilter;

    @Before
    public void setUp(){
        authzTransOnlyFilter = new AuthzTransOnlyFilter(authzEnvMock);
    }

    /*@Test
    public void testProtected() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Method newTransMethod = AuthzTransFilter.class.getDeclaredMethod("newTrans");
        newTransMethod.setAccessible(true);

        newTransMethod.invoke(authzTransFilter);
    }*/

    @Test
    public void testStart() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        AuthzTransOnlyFilter aTF = new AuthzTransOnlyFilter(authzEnvMock);
        Class c = aTF.getClass();
        Method startMethod = c.getDeclaredMethod("start", new Class[] {AuthzTrans.class});
        startMethod.setAccessible(true);
        //startMethod.invoke(aTF, authzTransMock, servletRequestMock);
    }

    @Test
    public void testAuthenticated() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, CadiException {
        TaggedPrincipal p = mock(TaggedPrincipal.class);
        AuthzTransOnlyFilter aTF = new AuthzTransOnlyFilter(authzEnvMock);
        Class c = aTF.getClass();
        Class[] cArg = new Class[2];
        cArg[0] = AuthzTrans.class;
        cArg[1] = TaggedPrincipal.class;        //Steps to test a protected method
        Method authenticatedMethod = c.getDeclaredMethod("authenticated", cArg);
        authenticatedMethod.setAccessible(true);
        authenticatedMethod.invoke(aTF,authzTransMock, null);
    }

    @Test
    public void testTallyHo() throws CadiException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        AuthzTransOnlyFilter aTF = new AuthzTransOnlyFilter(authzEnvMock);
        LogTarget log = mock(LogTarget.class);
        Metric met = new Metric();
        met.total = 199.33F;
        met.entries = 15;
        met.buckets = new float[] {199.33F,99.33F};
        Class c = aTF.getClass();
        Class[] cArg = new Class[1];
        cArg[0] = AuthzTrans.class;        //Steps to test a protected method
        StringBuilder sb = new StringBuilder("AuditTrail\n");
        when(authzTransMock.auditTrail(anyInt(),(StringBuilder)any(),anyInt(),anyInt())).thenReturn(met);
        when(authzTransMock.info()).thenReturn(log);
        doNothing().when(log).log((StringBuilder)any());
        Method tallyHoMethod = c.getDeclaredMethod("tallyHo", cArg);
        tallyHoMethod.setAccessible(true);
        tallyHoMethod.invoke(aTF,authzTransMock);
    }

}
