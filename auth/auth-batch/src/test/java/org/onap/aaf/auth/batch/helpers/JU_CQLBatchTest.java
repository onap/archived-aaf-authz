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

package org.onap.aaf.auth.batch.helpers;

import static org.junit.Assert.assertNull;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

public class JU_CQLBatchTest {

    @Mock
    AuthzTrans trans;

    @Mock
    Session session;

    @Mock
    LogTarget lg;

    CQLBatch cqlBatchObj;

    @Before
    public void setUp() throws APIException, IOException {
        initMocks(this);
        Mockito.doReturn(lg).when(trans).info();
        cqlBatchObj = new CQLBatch(lg, session);
        Mockito.doReturn(Mockito.mock(TimeTaken.class)).when(trans).start(
                Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());

        Mockito.doReturn(Mockito.mock(ResultSet.class)).when(session)
                .execute(Mockito.anyString());
    }

    @Test
    public void testExecute() {
        ResultSet retVal = cqlBatchObj.execute();
        assertNull(retVal);

        Field f;
        try {
            f = CQLBatch.class.getDeclaredField("sb");
            f.setAccessible(true);
            f.set(cqlBatchObj, new StringBuilder("test"));
            retVal = cqlBatchObj.execute();
        } catch (NoSuchFieldException | SecurityException
                | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testExecute2() {
        ResultSet retVal = cqlBatchObj.execute(false);
        assertNull(retVal);

        Field f;
        try {
            f = CQLBatch.class.getDeclaredField("sb");
            f.setAccessible(true);
            f.set(cqlBatchObj, new StringBuilder("test"));
            retVal = cqlBatchObj.execute(true);
            cqlBatchObj.toString();
        } catch (NoSuchFieldException | SecurityException
                | IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testSingleExec() {
        ResultSet retVal = cqlBatchObj.singleExec(new StringBuilder("test"),
                true);
        assertNull(retVal);

        retVal = cqlBatchObj.singleExec(new StringBuilder("test"), false);
    }

    @Test
    public void testTouch() {
        cqlBatchObj.touch("test", 0, 4, true);

    }

}
