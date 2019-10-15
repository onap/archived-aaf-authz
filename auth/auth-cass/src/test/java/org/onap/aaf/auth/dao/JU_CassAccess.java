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

package org.onap.aaf.auth.dao;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.powermock.modules.junit4.PowerMockRunner;

//import org.onap.aaf.auth.dao.CassAccess.Resettable;
import com.datastax.driver.core.Cluster.Builder;

@RunWith(PowerMockRunner.class)
public class JU_CassAccess {
    CassAccess cassAccess;

    public static final String KEYSPACE = "authz";
    public static final String CASSANDRA_CLUSTERS = "cassandra.clusters";
    public static final String CASSANDRA_CLUSTERS_PORT = "cassandra.clusters.port";
    public static final String CASSANDRA_CLUSTERS_USER_NAME = "cassandra.clusters.user";
    public static final String CASSANDRA_CLUSTERS_PASSWORD = "cassandra.clusters.password";
    public static final String CASSANDRA_RESET_EXCEPTIONS = "cassandra.reset.exceptions";
    public static final String LATITUDE = "LATITUDE";
    public static final String LONGITUDE = "LONGITUDE";
    //private static final List<Resettable> resetExceptions = new ArrayList<>();
    public static final String ERR_ACCESS_MSG = "Accessing Backend";
    private static Builder cb = null;
    @Mock
    Env envMock;
    String prefix=null;

    @Before
    public void setUp(){
        cassAccess = new CassAccess();
    }

//
//    @Test(expected=APIException.class)
//    public void testCluster() throws APIException, IOException {
////        cassAccess.cluster(envMock, prefix);
//
//    }

}
