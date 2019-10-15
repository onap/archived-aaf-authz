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

package org.onap.aaf.org.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.local.AbsData.Reuse;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.org.DefaultOrg;
import org.onap.aaf.org.DefaultOrgIdentity;
import org.onap.aaf.org.Identities;
import org.onap.aaf.org.Identities.Data;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.mockito.Mockito.*;
import java.io.IOException;

@RunWith(PowerMockRunner.class)
public class JU_DefaultOrgIdentity {

    private DefaultOrg defaultOrgMock;

    @Mock
    private Reuse rMock;

    @Mock
    AuthzTrans authzTransMock;

    @Mock
    private Data dataMock;

    @Mock
    private DefaultOrgIdentity defaultOrgIdentity;

    static String key = "iowna@deforg";
    static String orgDomain = "@deforg";

    @Before
    public void setUp() throws IOException, OrganizationException {
        MockitoAnnotations.initMocks(this);
        defaultOrgMock = PowerMockito.mock(DefaultOrg.class);
        defaultOrgMock.identities = mock(Identities.class);


        authzTransMock = PowerMockito.mock(AuthzTrans.class);

        when(defaultOrgMock.getDomain()).thenReturn(orgDomain);
        when(defaultOrgMock.identities.reuse()).thenReturn(rMock);
        when(defaultOrgMock.identities.find(eq(key),any(Reuse.class))).thenReturn(dataMock);

        defaultOrgIdentity = new DefaultOrgIdentity(authzTransMock, key, defaultOrgMock);

    }


    @Test
    public void testIdentify_returnIdentifiedEntity()  {

        assertTrue(defaultOrgIdentity.id() != null);

    }

    @Test
    public void testIdentify_returnIdentifiedEntityWithDataNull() throws IOException, OrganizationException {

        when(defaultOrgMock.identities.find(eq(key),any(Reuse.class))).thenReturn(null);

        DefaultOrgIdentity defaultOrgIdentityDataNull = new DefaultOrgIdentity(authzTransMock, key, defaultOrgMock);
        assertTrue(defaultOrgIdentityDataNull.id() != null);

    }

    @Test(expected = OrganizationException.class)
    public void testIdentify_returnThrowIOException() throws OrganizationException {

        when(defaultOrgMock.getDomain()).thenReturn(orgDomain);
        when(defaultOrgMock.identities.reuse()).thenThrow(IOException.class);
        DefaultOrgIdentity defaultOrgIdentityException = new DefaultOrgIdentity(authzTransMock, key, defaultOrgMock);

    }


    @Test
    public void testEquals_returnTrue() {

        Object b = defaultOrgIdentity;
        assertTrue(defaultOrgIdentity.equals(b) == true );
    }

    @Test
    public void testStatus_returnUnknown() {

        assertEquals(defaultOrgIdentity.type(), "Unknown");

    }

    @Test
    public void testHash_returnHashCode() {

        assertTrue(defaultOrgIdentity.hashCode() != 0 );

    }

    @Test
    public void testFullId_returnFullId() throws IOException, OrganizationException{
        String key="toto@deforg";
        String orgDomain="@deforg";
        when(defaultOrgMock.getDomain()).thenReturn(orgDomain);
        when(defaultOrgMock.identities.reuse()).thenReturn(rMock);
        when(defaultOrgMock.identities.find(eq(key),any(Reuse.class))).thenReturn(dataMock);
        defaultOrgIdentity = new DefaultOrgIdentity(authzTransMock, key, defaultOrgMock);

        assertTrue(defaultOrgIdentity.fullID().contains("@") );
    }

    @Test
    public void testEmail_returnEmail() {

        assertTrue(defaultOrgIdentity.email() != null  );
    }


    @Test
    public void testFullName_returnFullName() {

        assertTrue(defaultOrgIdentity.fullName() != null );
    }


    @Test
    public void testFirstName_returnFirstName() {

        assertTrue(defaultOrgIdentity.firstName() != null );
    }




}
