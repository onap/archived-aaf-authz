/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.auth.dao.hl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.cass.NsSplit;
import org.onap.aaf.auth.dao.cass.UserRoleDAO.Data;
import org.onap.aaf.auth.env.AuthzTransImpl;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;


@RunWith(MockitoJUnitRunner.class) 
public class JU_CassExecutor {



    private static final Object NO_PARAM = new Object[0];

    @Mock
    AuthzTransImpl trans;

    @Mock
    Question q;

    @Mock
    Access access;

    Function f;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        try {
            Mockito.doReturn("0.0").when(access).getProperty("aaf_root_ns","org.osaaf.aaf");
            Mockito.doReturn(new Properties()).when(access).getProperties();
            Define.set(access);
        } catch (CadiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        f =new Function(trans, q);
    }

    @Test
    public void testHasPermission() {
    
        CassExecutor cassExecutorObj =new CassExecutor(trans, f);
        Mockito.doReturn(false).when(q).isGranted(trans, "","","","","");
        boolean retVal = cassExecutorObj.hasPermission("", "", "", "", "");
//        System.out.println(retVal);
        assertFalse(retVal);
    }

    @Test
    public void testInRole() {
    
        CassExecutor cassExecutorObj =new CassExecutor(trans, f);
        Result<NsSplit> retVal1 = new Result<NsSplit>(null,1,"",NO_PARAM);
        Mockito.doReturn(retVal1).when(q).deriveNsSplit(trans, "test");
    
        boolean retVal = cassExecutorObj.inRole("test");
//        System.out.println(retVal);
        assertFalse(retVal);
    }

    @Test
    public void testNamespace() {
        f =new Function(trans, q);
        CassExecutor cassExecutorObj =new CassExecutor(trans, f);
        Result<Data> retVal1 = new Result<Data>(null,1,"",NO_PARAM);
        Mockito.doReturn(retVal1).when(q).validNSOfDomain(trans, null);
    
        String retVal="";
        try {
            retVal = cassExecutorObj.namespace();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            assertEquals("33", e.getMessage());
        }
        System.out.println(retVal);
//        assertFalse(retVal);
    }

    @Test
    public void testId() {
        Mockito.doReturn("").when(trans).user();
        CassExecutor cassExecutorObj =new CassExecutor(trans, f);
        String retVal = cassExecutorObj.id();
        assertEquals("", retVal);
    }

    @Test
    public void testNamespaceSuccess() {
        Mockito.doAnswer(new Answer<Object>() {
            private int count = 0;

            public Object answer(InvocationOnMock invocation) {
                if (count++ == 1)
                    return "test@test.com";

                return null;
            }
        }).when(trans).user();
        f =new Function(trans, q);
        CassExecutor cassExecutorObj =new CassExecutor(trans, f);
        Result<Data> retVal1 = new Result<Data>(null,0,"",NO_PARAM);
        Mockito.doReturn(retVal1).when(q).validNSOfDomain(trans, null);
    
    
//        String retVal="";
        try {
            /*retVal =*/ cassExecutorObj.namespace();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
//            assertNull( e.getMessage());
        }
//        System.out.println(retVal);
//        assertFalse(retVal);
    }

}