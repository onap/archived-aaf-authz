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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.local.AbsData.Reuse;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.org.DefaultOrg;
import org.onap.aaf.org.Identities;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
public class JU_DefaultOrg {


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


    private static final String PROPERTY_IS_REQUIRED = " property is Required";
    private static final String DOMAIN = "osaaf.com";
    private static final String REALM = "com.osaaf";
    private static final String NAME = "Default Organization";
    private static final String NO_PASS = NAME + " does not support Passwords.  Use AAF";

    private static final String URL = "www.deforg.com";
    private static final String IDENT = "ccontra|iowna";
    private static final String CCS = "mmanager|bdevl";
    String mailHost,mailFromUserId,summary,supportAddress;

    private final static int TIMEOUT = Integer.parseInt(Config.AAF_CONN_TIMEOUT_DEF);



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
    public void testDefOrg_returnDataIdentityNotNull() throws OrganizationException {


        try {
            defaultOrg.identities.open(authzTransMock, TIMEOUT);
            try {
                Reuse r = defaultOrg.identities.reuse();
                data = defaultOrg.identities.find("iowna", defaultOrg.identities.reuse());
                System.out.println("here is identities data: "+ data.toString());

            } finally {
                defaultOrg.identities.close(authzTransMock);
            }
        } catch (IOException e) {
            throw new OrganizationException(e);
        }


        assertTrue(data.toString() != null);

    }



    @Test
    public void testDefOrg_returnDefOrgEntity()  {


        assertTrue(defaultOrg != null);

    }


    @Test
    public void testDefOrgNotifyApproval_returnResponseOK() {

        summary = "Approval";
        Boolean urgent = false;
        DefaultOrg.Response response = defaultOrg.notify(authzTransMock, DefaultOrg.Notify.Approval, URL, IDENT.split("\\|"), CCS.split("\\|"), summary, urgent);
        assertEquals(response.name(), "OK");

    }

    @Test
    public void testDefOrgPasswords() {
        assertEquals(defaultOrg.isValidPassword(authzTransMock, null, "new2You!", "Pilgrim"),"");
        assertEquals(defaultOrg.isValidPassword(authzTransMock, null, "new2you!", "Pilgrim"),"");
        assertNotSame(defaultOrg.isValidPassword(authzTransMock, null, "newtoyou", "Pilgrim"),"");
    }

    @Test
    public void testDefOrgNotifyPasswordExpiration_returnResponseOK() {

        summary = "PasswordExpiration";
        Boolean urgent = false;
        DefaultOrg.Response response = defaultOrg.notify(authzTransMock, DefaultOrg.Notify.PasswordExpiration, URL, IDENT.split("\\|"), CCS.split("\\|"), summary, urgent);
        assertEquals(response.name(), "OK");

    }

    @Test
    public void testDefOrgNotifyRoleExpiration_returnResponseOK() {

        summary = "RoleExpiration";
        Boolean urgent = false;
        DefaultOrg.Response response = defaultOrg.notify(authzTransMock, DefaultOrg.Notify.RoleExpiration, URL, IDENT.split("\\|"), CCS.split("\\|"), summary, urgent);
        assertEquals(response.name(), "OK");
    }

    @Test
    public void testDefOrgNotifyRoleExpirationUrgent_returnResponseOK() {

        summary = "RoleExpirationUrgent";
        Boolean urgent = true;
        when(authzTransMock.info()).thenReturn(logTargetMock);
        DefaultOrg.Response response = defaultOrg.notify(authzTransMock, DefaultOrg.Notify.RoleExpiration, URL, IDENT.split("\\|"), CCS.split("\\|"), summary, urgent);
        assertEquals(response.name(), "OK");

    }

    @Test
    public void testDefOrgNotifyModeTest_returnResponseOK()  {

        summary = "ModeTest";
        Boolean urgent = false;
        when(authzTransMock.info()).thenReturn(logTargetMock);
        defaultOrg.setTestMode(true);
        DefaultOrg.Response response = defaultOrg.notify(authzTransMock, DefaultOrg.Notify.RoleExpiration, URL, IDENT.split("\\|"), CCS.split("\\|"), summary, urgent);
        assertEquals(response.name(), "OK");

    }





    //@Test    //(expected=OrganizationException.class)
    public void testMultipleCreds() throws OrganizationException{
        String id = "test";
        boolean canHaveMultipleCreds;
        canHaveMultipleCreds = defaultOrg.canHaveMultipleCreds(id );
        System.out.println("value of canHaveMultipleCreds:  " + canHaveMultipleCreds);
        assertTrue(canHaveMultipleCreds);
    }


    //@Test
    public void testGetIdentityTypes() throws OrganizationException{
        Set<String> identityTypes = defaultOrg.getIdentityTypes();
        System.out.println("value of IdentityTypes:  " + identityTypes);
        assertTrue(identityTypes.size() == 4);
    }


    //@Test
    public void testGetRealm() throws OrganizationException{
        String realmTest = defaultOrg.getRealm();
        System.out.println("value of realm:  " + realmTest);
        assertTrue(realmTest == REALM);
    }

    public void supportsRealm() {
        String otherRealm = "org.ossaf.something";
        defaultOrg.addSupportedRealm(otherRealm);
        assertTrue(defaultOrg.supportsRealm(otherRealm));
    }
    //@Test
    public void testGetName() throws OrganizationException{
        String testName = defaultOrg.getName();
        System.out.println("value of name:  " + testName);
        assertTrue(testName == NAME);
    }


    //@Test
    public void testGetDomain() throws OrganizationException{
        String testDomain = defaultOrg.getDomain();
        System.out.println("value of domain:  " + testDomain);
        assertTrue(testDomain == DOMAIN);
    }

    // @Test
    // public void testIsValidID(){
    //     String Result = defaultOrg.isValidID(Matchers.anyString());
    //     System.out.println("value of res " +Result);
    //     assertNotNull(Result);
    // }

    @Test
    public void testResponsible() throws OrganizationException {
        Identity id = defaultOrg.getIdentity(authzTransMock, "osaaf");
        Identity rt = id.responsibleTo();
        assertTrue(rt.id().equals("bdevl"));
    
    }

    //@Test
    public void notYetImplemented() {
        fail("Tests in this file should not be trusted");
    }

}
