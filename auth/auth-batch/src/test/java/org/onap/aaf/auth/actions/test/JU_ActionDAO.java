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

package org.onap.aaf.auth.actions.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.actions.ActionDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Configuration;
import com.datastax.driver.core.Cluster.Initializer;
import com.datastax.driver.core.Host.StateListener;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

public class JU_ActionDAO {
    
    AuthzTrans aTrans;
    Cluster cluster;
    ActionDAOStub actionDAOStub;
    ActionDAOStub actionDAOStub1;

    private class ActionDAOStub extends ActionDAO {

        public ActionDAOStub(AuthzTrans trans, ActionDAO predecessor) {
            super(trans, predecessor);
            // TODO Auto-generated constructor stub
        }

        public ActionDAOStub(AuthzTrans trans, Cluster cluster, boolean dryRun) throws APIException, IOException {
            super(trans, cluster, dryRun);
            // TODO Auto-generated constructor stub
        }

        @Override
        public Result exec(AuthzTrans trans, Object data, Object t) {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
    
    @Before
    public void setUp() throws APIException, IOException {
//        Cluster.Initializer cInit = mock(Cluster.Initializer.class);
//        Cluster.Builder cBuild = new Cluster.Builder();
//        cBuild.addContactPoint("test");
//        cBuild.build();
//        cluster.buildFrom(cBuild);
//        cluster.builder();
//        cluster.init();
//        cluster.builder().getContactPoints();
        

        
//        aTrans = mock(AuthzTrans.class);
//        cluster = mock(Cluster.class);
//        actionDAOStub = new ActionDAOStub(aTrans,cluster,true);
//        actionDAOStub1 = new ActionDAOStub(aTrans, actionDAOStub);
    }

}
