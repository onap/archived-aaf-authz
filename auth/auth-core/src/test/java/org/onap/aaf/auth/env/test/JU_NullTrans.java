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

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.NullTrans;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.misc.env.Decryptor;
import org.onap.aaf.misc.env.Encryptor;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.TimeTaken;

@RunWith(MockitoJUnitRunner.class)
public class JU_NullTrans {
    NullTrans nullTrans;

    @Before
    public void setUp(){
        nullTrans = new NullTrans();
    }

    @Test
    public void testAuditTrail() {
        Assert.assertNull(nullTrans.auditTrail(0, null, 0));
    }

    @Test
    public void testSingleton() {
        AuthzTrans single = nullTrans.singleton();
        Assert.assertTrue(single instanceof AuthzTrans);
    }

    @Test
    public void testCheckpoints() {
        nullTrans.checkpoint("Test");
        nullTrans.checkpoint(null, 0);
    }

    @Test
    public void testFatal() {
        LogTarget log = nullTrans.fatal();
        Assert.assertEquals(LogTarget.NULL, log);
    }

    @Test
    public void testError() {
        LogTarget log = nullTrans.error();
        Assert.assertEquals(LogTarget.NULL, log);
    }

    @Test
    public void testAudit() {
        LogTarget log = nullTrans.audit();
        Assert.assertEquals(LogTarget.NULL, log);
    }

    @Test
    public void testInit() {
        LogTarget log = nullTrans.init();
        Assert.assertEquals(LogTarget.NULL, log);
    }

    @Test
    public void testWarn() {
        LogTarget log = nullTrans.warn();
        Assert.assertEquals(LogTarget.NULL, log);
    }

    @Test
    public void testInfo() {
        LogTarget log = nullTrans.info();
        Assert.assertEquals(LogTarget.NULL, log);
    }

    @Test
    public void testDebug() {
        LogTarget log = nullTrans.debug();
        Assert.assertEquals(LogTarget.NULL, log);
    }

    @Test
    public void testTrace() {
        LogTarget log = nullTrans.trace();
        Assert.assertEquals(LogTarget.NULL, log);
    }

    @Test
    public void testStart() {
        TimeTaken test = nullTrans.start("test", 1);
        StringBuilder sb = new StringBuilder();
        test.output(sb);
        StringBuilder sb1 = new StringBuilder();
        sb1.append(test);
        String s = sb.toString();
        String s1 = sb1.toString();
        s1 = s1.trim();
        Assert.assertEquals(s,s1);
    }

    @Test
    public void testSetProperty() {
        String tag = "tag";
        String value = "value";
        nullTrans.setProperty(tag, value);
        String expected = nullTrans.getProperty(tag, value);
        Assert.assertEquals(expected, value);
        String expectedTag = nullTrans.getProperty(tag);
        Assert.assertEquals(expectedTag, tag);
    }

    @Test
    public void testDecryptor() {
        Decryptor decry = nullTrans.decryptor();
        Assert.assertNull(decry);
    }

    @Test
    public void testEncryptor() {
        Encryptor encry = nullTrans.encryptor();
        Assert.assertNull(encry);
    }

    @Test
    public void testSet() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        AuthzTrans set = nullTrans.set(req,res);
        Assert.assertNull(set);
    }

    @Test
    public void testUser() {
        String user = nullTrans.user();
        Assert.assertNull(user);
    }

    @Test
    public void testGetUserPrincipal() {
        Principal principal = nullTrans.getUserPrincipal();
        Assert.assertNull(principal);
    }

    @Test
    public void testIp() {
        String ip = nullTrans.ip();
        Assert.assertNull(ip);
    }

    @Test
    public void testMeth() {
        String meth = nullTrans.meth();
        Assert.assertNull(meth);
    }

    @Test
    public void testPort() {
        int port = nullTrans.port();
        Assert.assertEquals(port,0);
    }

    @Test
    public void testPath() {
        String path = nullTrans.path();
        Assert.assertNull(path);
    }

    @Test
    public void testPut() {
        nullTrans.put(null, nullTrans);
    }

    @Test
    public void testSetUser() {
        Principal principal = mock(Principal.class);
        //nullTrans.setUser(principal);
    }

    @Test
    public void testSlot() {
        Slot slot = nullTrans.slot(null);
        Assert.assertNull(slot);
    }

    @Test
    public void testEnv() {
        AuthzEnv env = nullTrans.env();
        Assert.assertNull(env);
    }

    @Test
    public void testAgent() {
        String agent = nullTrans.agent();
        Assert.assertNull(agent);
    }

    @Test
    public void testSetLur() {
        nullTrans.setLur(null);
    }

    @Test
    public void testFish() {
        Permission perm = mock(Permission.class);
        Boolean fish = nullTrans.fish(perm);
        Assert.assertFalse(fish);
    }

    @Test
    public void testOrg() {
        Organization org = nullTrans.org();
        Assert.assertEquals(Organization.NULL, org);
    }

    @Test
    public void testLogAuditTrail() {
        LogTarget lt = mock(LogTarget.class);
        nullTrans.logAuditTrail(lt);
    }

    @Test
    public void testRequested() {
        Boolean reqd = nullTrans.requested(null);
        Assert.assertFalse(reqd);
        nullTrans.requested(null, true);
    }

//  This is very inconsistent, and rather pointless
//    @Test
//    public void testNow() {
//        Date date = new Date();
//        Assert.assertEquals(date,nullTrans.now());
//        //when(nullTrans.now()).thenReturn(null);
//    }



}
