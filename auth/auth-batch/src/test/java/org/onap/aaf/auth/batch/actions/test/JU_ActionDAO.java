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

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.batch.actions.ActionDAO;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.hl.Function;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.PreparedId;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

public class JU_ActionDAO {
    
	@Mock
    AuthzTrans trans;
	@Mock
	Cluster cluster;
	@Mock
	PropAccess access;
	
	@Mock
	ActionDAO actionObj;

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
    	initMocks(this);
    	Session sessionObj=Mockito.mock(Session.class);
    	PreparedStatement psObj =Mockito.mock(PreparedStatement.class);
		try {
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).init();
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).warn();
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
			Mockito.doReturn("10").when(trans).getProperty(Config.AAF_USER_EXPIRES, Config.AAF_USER_EXPIRES_DEF);
			Mockito.doReturn(Mockito.mock(TimeTaken.class)).when(trans).start(Mockito.anyString(),Mockito.anyInt());
			Mockito.doReturn(sessionObj).when(cluster).connect("authz");
			Mockito.doReturn(psObj).when(sessionObj).prepare(Mockito.anyString());
			
			Mockito.doReturn(Mockito.mock(ColumnDefinitions.class)).when(psObj).getVariables();
			Mockito.doReturn(Mockito.mock(PreparedId.class)).when(psObj).getPreparedId();
			Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
			Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
			Define.set(access);
			actionObj = new ActionDAOStub(trans, cluster, true);
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
    
    @Test
	public void testGetSession() {
    	try {
    		Session session = actionObj.getSession(trans);
    		assertTrue(session.toString().contains("Mock for Session"));
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
    
    @Test
	public void testQuestion() {
    		Question retVal = actionObj.question();
    		assertTrue(retVal.toString().contains("org.onap.aaf.auth.dao.hl.Question"));
	}
    
    @Test
	public void testFunction() {
    	Function retVal = actionObj.function();
   		assertTrue(retVal.toString().contains("org.onap.aaf.auth.dao.hl.Function"));
	}
    
    @Test
	public void testClose() {
    		actionObj.close(trans);
//    		assertTrue(session.toString().contains("Mock for Session"));
	}
    
    @Test
   	public void testCloseFalse() {
    	actionObj = new ActionDAOStub(trans, Mockito.mock(ActionDAO.class));
       		actionObj.close(trans);
//       		assertTrue(session.toString().contains("Mock for Session"));
   	}

}
