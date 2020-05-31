/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Modification Copyright © 2020 IBM.
 * ===========================================================================
 *
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

package org.onap.aaf.auth.batch.helpers;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.LogTarget;

import com.datastax.driver.core.Session;

public class JU_CQLBatchLoopTest {

    @Mock
    AuthzTrans trans;

    @Mock
    Session session;

    @Mock
    LogTarget lg;

    @Mock
    CQLBatch cqlBatchObj;

    CQLBatchLoop cqlBatchLoopObj;

    @Before
    public void setUp() throws APIException, IOException {
        initMocks(this);
        Mockito.doReturn(lg).when(trans).info();
        Mockito.doReturn(new StringBuilder()).when(cqlBatchObj).begin();
        cqlBatchLoopObj = new CQLBatchLoop(cqlBatchObj, 0, false);
    }

    @Test
    public void testShowProgress() {
        CQLBatchLoop tempLoopObj = cqlBatchLoopObj.showProgress();
        Field f;
        try {
            f = CQLBatchLoop.class.getDeclaredField("showProgress");
            f.setAccessible(true);
            assertEquals(cqlBatchLoopObj.total(), 0);
            assertTrue(f.getBoolean(tempLoopObj));
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testInc() {
        StringBuilder sb = cqlBatchLoopObj.inc();
        sb = cqlBatchLoopObj.inc();
        assertEquals(1, cqlBatchLoopObj.batches());
        Field f;
        try {
            f = CQLBatchLoop.class.getDeclaredField("showProgress");
            f.setAccessible(true);
            f.set(cqlBatchLoopObj, true);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cqlBatchLoopObj.inc();
        assertEquals(2, cqlBatchLoopObj.batches());
        System.out.println(sb.toString());
    }

    @Test
    public void testFlush() {
        Field f, f1;
        try {
            f1 = CQLBatchLoop.class.getDeclaredField("i");
            f1.setAccessible(true);

            f1.set(cqlBatchLoopObj, -1);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cqlBatchLoopObj.flush();
        try {
            f = CQLBatchLoop.class.getDeclaredField("current");
            f1 = CQLBatchLoop.class.getDeclaredField("i");
            f.setAccessible(true);
            f1.setAccessible(true);

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);

            StringBuilder sb = new StringBuilder("tets");
            for (int i = 1; i < 25600; i++) {
                sb = sb.append("test");
            }
            f.set(cqlBatchLoopObj, sb);
            f1.set(cqlBatchLoopObj, 10);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        cqlBatchLoopObj.flush();
    }

    @Test
    public void testReset() {
        cqlBatchLoopObj.reset();
        assertEquals(0, cqlBatchLoopObj.batches());
        System.out.println(cqlBatchLoopObj.toString());
    }
}
