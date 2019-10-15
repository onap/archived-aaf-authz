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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTransImpl;
import org.onap.aaf.auth.env.AuthzTrans.REQD_TYPE;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.OrganizationFactory;
import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.powermock.modules.junit4.PowerMockRunner;

import junit.framework.Assert;

@RunWith(PowerMockRunner.class)
public class JU_AuthzTransImpl {

    AuthzTransImpl authzTransImpl;
    @Mock
    AuthzEnv authzEnvMock;
    AuthzTransImpl trans1;

    private Organization org=null;
    private AuthzTransImpl mockAuthzTransImpl;
    private static HttpServletRequest req;
    private static HttpServletResponse res;
    private Lur lur1 = mock(Lur.class);

    @Before
    public void setUp(){
        authzTransImpl = new AuthzTransImpl(authzEnvMock);
        req = mock(HttpServletRequest.class);
        res = mock(HttpServletResponse.class);
        authzTransImpl.set(req,res);
        when(req.getParameter("request")).thenReturn("NotNull");
        authzTransImpl.set(req,res);
        when(req.getParameter("request")).thenReturn("");
        authzTransImpl.set(req,res);
    }

    @Test
    public void testOrg() {
        Organization result=null;
        result = authzTransImpl.org();
        OrganizationFactory test = mock(OrganizationFactory.class);
        //result = OrganizationFactory.obtain(authzTransImpl.env(), authzTransImpl.user());
        authzTransImpl.org();
        //when(test).thenReturn(null);
        //assertTrue(true);
    }

    @Mock
    LogTarget logTargetMock;

    @Test
    public void testLogAuditTrail(){
    
        when(logTargetMock.isLoggable()).thenReturn(false);
        authzTransImpl.logAuditTrail(logTargetMock);
        when(logTargetMock.isLoggable()).thenReturn(true);
        Env delegate = mock(Env.class);
        //when(logTargetMock.isLoggable()).thenReturn(true);//TODO: Figure this out
        //authzTransImpl.logAuditTrail(logTargetMock);
    }

//    @Test                            //TODO:Fix this AAF-111
//    public void testSetUser() {
//        Principal user = mock(Principal.class);
//        authzTransImpl.setUser(user);
//        Principal user1 = authzTransImpl.getUserPrincipal();
//        String username = user1.getName();
//        Assert.assertNotNull(user1);
//    }

//    @Test                            //TODO:Fix this AAF-111
//    public void testUser() {
//        Assert.assertEquals("n/a", authzTransImpl.user());
//        Principal user = mock(Principal.class); //Unsure how to modify name
//        when(user.toString()).thenReturn("name");
//        when(user.getName()).thenReturn("name");
//        authzTransImpl.setUser(user);
//        Assert.assertEquals("name", authzTransImpl.user());
//    }
//
    @Test
    public void testRequested() {
        REQD_TYPE user = REQD_TYPE.move;
        REQD_TYPE user1 = REQD_TYPE.future;
        HttpServletRequest req = mock(HttpServletRequest.class);
        String p = user1.name();
        boolean boolUser = authzTransImpl.requested(user);
        Assert.assertEquals(false, boolUser);
        Assert.assertNotNull(p);
        authzTransImpl.requested(user,true);
        when(authzTransImpl.requested(user)).thenReturn(null);
        Assert.assertEquals(true, authzTransImpl.requested(user));
    /*    String p1 = req.getParameter(user1.name());  //unable to access private method call in all instances
        when(req.getParameter(user1.name())).thenReturn("test");
        authzTransImpl.requested(user,false);
        */
    
    
    }

    @Test
    public void testFish() {
        mockAuthzTransImpl = mock(AuthzTransImpl.class);
        Permission p = mock(Permission.class);
        authzTransImpl.fish(p);
        String str = "Test";
        lur1.createPerm(str);
        when(p.match(p)).thenReturn(true);
        authzTransImpl.setLur(lur1);
        authzTransImpl.fish(p);
    }

    @Test
    public void testSetVariables() { //TODO: refactor this better
        Assert.assertNull(authzTransImpl.agent());
        Assert.assertNull(authzTransImpl.ip());
        Assert.assertNull(authzTransImpl.path());
        Assert.assertNotNull(authzTransImpl.port());
        Assert.assertNull(authzTransImpl.meth());
        Assert.assertNull(authzTransImpl.getUserPrincipal());
        Assert.assertNotNull(authzTransImpl.user());
    }

    @Test
    public void testNow() {
        Date date = authzTransImpl.now();
        Assert.assertEquals(date,authzTransImpl.now());
        when(authzTransImpl.now()).thenReturn(null);
    }

}
