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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.batch.actions.EmailPrint;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;

import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class JU_EmailPrint {
    
    private ByteArrayOutputStream outStream;
    private ByteArrayOutputStream errStream;
    EmailPrint ePrint;
    AuthzTrans trans;
    Organization org;
    StringBuilder strBuilder;
    
    @Before
    public void setUp() {
        outStream = new ByteArrayOutputStream();
        errStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));
        System.setErr(new PrintStream(errStream));
        ePrint = new EmailPrint();
        trans = mock(AuthzTrans.class);
        org = mock(Organization.class);
        strBuilder = new StringBuilder();
        strBuilder.append("test\nte\nst");
        ePrint.addTo("test");
        ePrint.addTo("test1");
        ePrint.addTo("test2");
        ePrint.addCC("test");
        ePrint.addCC("test1");
        ePrint.addCC("test2");
        
    }

    @Test
    public void testExec() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class c = ePrint.getClass();
        Class[] cArg = new Class[3];
        cArg[0] = AuthzTrans.class;
        cArg[1] = Organization.class;
        cArg[2] = StringBuilder.class;//Steps to test a protected method
        Method execMethod = c.getDeclaredMethod("exec", cArg);
        execMethod.setAccessible(true);
        execMethod.invoke(ePrint, trans, org, strBuilder);
    }
    
    @After
    public void cleanUp() {
        System.setErr(System.err);
        System.setOut(System.out);
    }

}
