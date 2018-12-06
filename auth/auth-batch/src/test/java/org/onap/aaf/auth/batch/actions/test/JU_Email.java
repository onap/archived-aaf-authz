/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.batch.actions.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.batch.actions.Email;
import org.onap.aaf.auth.batch.actions.Message;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.OrganizationException;

import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;

import org.junit.Test;

public class JU_Email {
    
    private ByteArrayOutputStream outStream;
    private ByteArrayOutputStream errStream;
    Email email;
    Identity usersI;
    Message msg;
    PrintStream ps;
    
    @Before
    public void setUp() throws FileNotFoundException {
        outStream = new ByteArrayOutputStream();
        errStream = new ByteArrayOutputStream();
        ps = new PrintStream(errStream);
        System.setOut(new PrintStream(outStream));
        System.setErr(ps);
        
        usersI = mock(Identity.class);
        msg = new Message();
        email = new Email();
    }

    @Test
    public void testClear() {
        Assert.assertNotNull(email.clear());
    }
    
    @Test
    public void testIndent() {
        email.indent("indent");
    }
    
    @Test
    public void testPreamble() {
        email.preamble("format");
    }
    
    @Test
    public void testAddTo() {
        email.addTo(usersI);
        
//        Collection col = mock(Collection.class);
//        col.add("test");
//        email.addTo(col);
        
        email.addTo("email");
    }
    
    @Test
    public void testAddCC() {
        email.addCC(usersI);
        email.addCC("email");
    }
    
//    @Test
//    public void testAdd() throws OrganizationException {
//        email.add(usersI, true);
//    }
    
    @Test
    public void testSubject() {
        email.subject("format");
        email.subject("for%smat","format");
    }
    
    @Test
    public void testSignature() {
        email.signature("format","arg");
    }
    
    @Test
    public void testMsg() {
        email.msg(msg);
    }
    
    @Test
    public void testExec() {
        AuthzTrans trans = mock(AuthzTrans.class);
        Organization org = mock(Organization.class);
        email.preamble("format");
        email.msg(msg);
        email.signature("format","arg");
        
        email.exec(trans, org, "text");
    }
    
    @Test
    public void testLog() throws FileNotFoundException {
        email.addTo("email");
        email.addCC("email");
        email.log(ps, "email");
        email.addTo("emails");
        email.addCC("emails");
        email.log(ps, "emails");
    }
    
    @After
    public void cleanUp() {
        System.setErr(System.err);
        System.setOut(System.out);
    }

}
