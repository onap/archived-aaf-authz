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

package org.onap.aaf.auth.dao;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.dao.Loader;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.TransStore;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;

@RunWith(PowerMockRunner.class)
public class JU_CassDAOImpl {

public static final String CASS_READ_CONSISTENCY="cassandra.readConsistency";
public static final String CASS_WRITE_CONSISTENCY="cassandra.writeConsistency";

CassDAOImpl cassDAOImpl;


@Mock
TransStore transStoreMock;
@SuppressWarnings("rawtypes")
Class dcMock;
@SuppressWarnings("rawtypes")
Loader loaderMock;
Cluster clusterMock;
Class<Data> classDataMock;
ConsistencyLevel consistencyLevelMock;
Trans transMock;

@Mock
AuthzTrans authzTransMock;



    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Before
    public void setUp()
    {
        String name = "name";
        String keySpace = "keySpace";
        String table = "table";
        cassDAOImpl = new CassDAOImpl(transStoreMock, name, clusterMock, keySpace, classDataMock, table, consistencyLevelMock, consistencyLevelMock);
    }

    //TODO: Gabe [JUnit] Visibility issue
    @Test 
    public void testReadConsistency() {
        String table = "users";
        PowerMockito.when(authzTransMock.getProperty(CASS_READ_CONSISTENCY+'.'+table)).thenReturn("TWO");
        ConsistencyLevel consistencyLevel = cassDAOImpl.readConsistency(authzTransMock, table);
        System.out.println("Consistency level" + consistencyLevel.name());
        assertEquals("TWO", consistencyLevel.name());
    }

    @Test 
    public void testWriteConsistency() {
        String table = "users";
        PowerMockito.when(authzTransMock.getProperty(CASS_WRITE_CONSISTENCY+'.'+table)).thenReturn(null);
        ConsistencyLevel consistencyLevel = cassDAOImpl.writeConsistency(authzTransMock, table);
        System.out.println("Consistency level" + consistencyLevel.name());
        assertEquals("ONE", consistencyLevel.name());
    }

}
