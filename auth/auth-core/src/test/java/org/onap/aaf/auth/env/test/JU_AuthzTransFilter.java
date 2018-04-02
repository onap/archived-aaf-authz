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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import java.security.Principal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransFilter;
import org.onap.aaf.auth.env.AuthzTransImpl;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Connector;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.TrustChecker;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.Trans.Metric;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)  
public class JU_AuthzTransFilter {
AuthzTransFilter authzTransFilter;
AuthzEnv authzEnvMock = mock(AuthzEnv.class);
Connector connectorMock = mock(Connector.class);
TrustChecker trustCheckerMock = mock(TrustChecker.class);
AuthzTrans authzTransMock = mock(AuthzTrans.class);
Object additionalTafLurs = mock(Object.class);

	@Before
	public void setUp() throws CadiException{
		when(authzEnvMock.access()).thenReturn(new PropAccess());
		//when(authzEnvMock.newTrans()).thenReturn(new AuthzTransImpl(authzEnvMock));
		authzTransFilter = new AuthzTransFilter(authzEnvMock, connectorMock, trustCheckerMock, additionalTafLurs);
		
		
	}
	
	@Test
	public void testAuthenticated() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, CadiException {
		Principal p = mock(Principal.class);
		AuthzTransFilter aTF = new AuthzTransFilter(authzEnvMock, connectorMock, trustCheckerMock, null);
		Class c = aTF.getClass();
		Class[] cArg = new Class[2];
		cArg[0] = AuthzTrans.class;
		cArg[1] = Principal.class;		//Steps to test a protected method
		Method authenticatedMethod = c.getDeclaredMethod("authenticated", cArg);
		authenticatedMethod.setAccessible(true);
		authenticatedMethod.invoke(aTF,authzTransMock, null);
	}
	
	@Test
	public void testTallyHo() throws CadiException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Slot specialLogSlot = authzEnvMock.slot("SPECIAL_LOG_SLOT");
		LogTarget lt = mock(LogTarget.class);
		AuthzTransFilter aTF = new AuthzTransFilter(authzEnvMock, connectorMock, trustCheckerMock, additionalTafLurs);
		TaggedPrincipal tPrin = mock(TaggedPrincipal.class);
		Metric met = new Metric();
		met.total = 199.33F;
		met.entries = 15;
		met.buckets = new float[] {199.33F,99.33F};
		Class c = aTF.getClass();
		Class[] cArg = new Class[1];
		cArg[0] = AuthzTrans.class;		//Steps to test a protected method
		Method tallyHoMethod = c.getDeclaredMethod("tallyHo", cArg);
		StringBuilder sb = new StringBuilder("AuditTrail\n");
		when(authzTransMock.auditTrail(((LogTarget)any()), anyInt(),(StringBuilder)any(),anyInt(),anyInt())).thenReturn(met);
		tallyHoMethod.setAccessible(true);
		when(authzTransMock.get(specialLogSlot, false)).thenReturn(false);
		when(authzTransMock.warn()).thenReturn(lt);
		when(authzTransMock.info()).thenReturn(lt);
		tallyHoMethod.invoke(aTF,authzTransMock);
		when(authzTransMock.getUserPrincipal()).thenReturn(tPrin);
		tallyHoMethod.invoke(aTF,authzTransMock);
		
	}
	
	
	
	
}
