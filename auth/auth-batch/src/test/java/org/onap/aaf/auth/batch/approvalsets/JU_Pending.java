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

package org.onap.aaf.auth.batch.approvalsets;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.util.CSV.Writer;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;

public class JU_Pending {

    @Mock
    AuthzTrans trans;
    @Mock
    Cluster cluster;
    @Mock
    PropAccess access;

    @Mock
    DataView dv;

    @Before
    public void setUp() throws APIException, IOException {
        initMocks(this);
    }

    @Test
    public void testRow() {
        Writer approveCW = Mockito.mock(Writer.class);
        Mockito.doNothing().when(approveCW).row(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyInt());
        Pending pendingObj = new Pending(new Date());
        pendingObj.row(approveCW, "key");
        Date date = null;
        pendingObj = new Pending(date);
    }

    @Test
    public void testInc() {
        List<String> inpList = new ArrayList<>();
        inpList.add("test");
        inpList.add("test");
        inpList.add("test");
        inpList.add(null);
        inpList.add("10");
        try {
            Pending pendingObj = new Pending(inpList);
            pendingObj.inc();
			assertEquals(11, pendingObj.qty());

            Pending tempPending = new Pending(inpList);
            pendingObj.inc(tempPending);
			assertEquals(21, pendingObj.qty());

            tempPending.earliest = new Date();
            pendingObj.earliest = new Date();
            pendingObj.inc(tempPending);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            pendingObj.earliest = calendar.getTime();
            pendingObj.inc(tempPending);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void testEarliest() {
        List<String> inpList = new ArrayList<>();
        try {
            inpList.add("test");
            inpList.add("test");
            inpList.add("test");
            inpList.add("2019-01-01");
            inpList.add("10");
            Pending.create();

            Pending pendingObj = new Pending(inpList);
            pendingObj.earliest(null);

            pendingObj.earliest = null;
            pendingObj.earliest(new Date());

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            pendingObj.earliest(calendar.getTime());
			assertEquals(119, pendingObj.earliest().getYear());
            assertTrue(pendingObj.newApprovals());
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
