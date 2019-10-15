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
import static org.junit.Assert.assertNotSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.org.DefaultOrg;
import org.onap.aaf.org.Identities;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
public class JU_Passwords {


    private DefaultOrg defaultOrg;


    Identities.Data data;

    @Mock
    Env envMock;

    @Mock
    AuthzTrans authzTransMock;

    @Mock
    TimeTaken ttMock;

    @Mock
    LogTarget logTargetMock;


    private static final String REALM = "org.osaaf";
    private static final String NAME = "Default Organization";

    String mailHost,mailFromUserId,summary,supportAddress;

    @Before
    public void setUp() throws OrganizationException{

        mailFromUserId = "frommail";
        mailHost = "hostmail";
        File file = new File("src/test/resources/");
        when(envMock.getProperty(REALM + ".name","Default Organization")).thenReturn(NAME);
        when(envMock.getProperty(REALM + ".mailHost",null)).thenReturn(mailHost);
        when(envMock.getProperty(REALM + ".mailFrom",null)).thenReturn(mailFromUserId);
        when(envMock.getProperty("aaf_data_dir")).thenReturn(file.getAbsolutePath());
        when(envMock.warn()).thenReturn(logTargetMock);
        when(authzTransMock.warn()).thenReturn(logTargetMock);
        when(authzTransMock.start(any(String.class),any(Integer.class))).thenReturn(ttMock);
        when(authzTransMock.error()).thenReturn(logTargetMock);
        when(authzTransMock.getProperty("CASS_ENV", "")).thenReturn("Cassandra env");

        defaultOrg = new DefaultOrg(envMock, REALM);

    }


    @Test
    public void testDefOrgPasswords() {
        // Accepts letters and one of (number, Special Char, Upper)
        assertEquals(defaultOrg.isValidPassword(authzTransMock, null, "newyou2", "Pilgrim"),"");
        assertEquals(defaultOrg.isValidPassword(authzTransMock, null, "newyou!", "Pilgrim"),"");
        assertEquals(defaultOrg.isValidPassword(authzTransMock, null, "newyou!", "Pilgrim"),"");
    
        // Don't accept just letters, Numbers or Special Chars, or without ANY letters
        assertNotSame(defaultOrg.isValidPassword(authzTransMock, null, "newyouA", "Pilgrim"),"");
        assertNotSame(defaultOrg.isValidPassword(authzTransMock, null, "NEWYOU", "Pilgrim"),"");
        assertNotSame(defaultOrg.isValidPassword(authzTransMock, null, "newyou", "Pilgrim"),"");
        assertNotSame(defaultOrg.isValidPassword(authzTransMock, null, "125343", "Pilgrim"),"");
        assertNotSame(defaultOrg.isValidPassword(authzTransMock, null, "#$@*^#", "Pilgrim"),"");
        assertNotSame(defaultOrg.isValidPassword(authzTransMock, null, "#$3333", "Pilgrim"),"");

        // Length
        assertNotSame(defaultOrg.isValidPassword(authzTransMock, null, "w2Yu!", "Pilgrim"),"");
        assertNotSame(defaultOrg.isValidPassword(authzTransMock, null, "", "Pilgrim"),"");
        assertNotSame(defaultOrg.isValidPassword(authzTransMock, null, "moreThan20somethingCharacters, even though good", "Pilgrim"),"");

        // May not contain ID
        assertNotSame(defaultOrg.isValidPassword(authzTransMock, null, "Pilgrim", "Pilgrim"),"");
        assertNotSame(defaultOrg.isValidPassword(authzTransMock, null, "Pilgrim1", "Pilgrim"),"");
        assertNotSame(defaultOrg.isValidPassword(authzTransMock, null, "Pilgrim#", "Pilgrim"),"");
        assertNotSame(defaultOrg.isValidPassword(authzTransMock, null, "aPilgrim1", "Pilgrim"),"");

        // Solid
        assertEquals(defaultOrg.isValidPassword(authzTransMock, null, "new2You!", "Pilgrim"),"");

    
    }

}
