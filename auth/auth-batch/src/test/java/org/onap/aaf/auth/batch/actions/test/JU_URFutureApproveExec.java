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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.batch.actions.ActionDAO;
import org.onap.aaf.auth.batch.actions.URFutureApproveExec;
import org.onap.aaf.auth.batch.actions.test.JU_URPunt.URPuntImpl;
import org.onap.aaf.auth.batch.helpers.Approval;
import org.onap.aaf.auth.batch.helpers.Future;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.hl.Function;
import org.onap.aaf.auth.dao.hl.Function.FUTURE_OP;
import org.onap.aaf.auth.dao.hl.Function.OP_STATUS;
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

import io.netty.util.internal.SystemPropertyUtil;



public class JU_URFutureApproveExec {
    
	@Mock
    AuthzTrans trans;
	@Mock
	Cluster cluster;
	@Mock
	PropAccess access;
	
	@Mock
	URFutureApproveExec actionObj;

    
    @Before
    public void setUp() throws APIException, IOException {
    	initMocks(this);
    	Session sessionObj=Mockito.mock(Session.class);
    	PreparedStatement psObj =Mockito.mock(PreparedStatement.class);
		try {
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).init();
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).warn();
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).info();
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
			Mockito.doReturn("10").when(trans).getProperty(Config.AAF_USER_EXPIRES, Config.AAF_USER_EXPIRES_DEF);
			Mockito.doReturn(Mockito.mock(TimeTaken.class)).when(trans).start(Mockito.anyString(),Mockito.anyInt());
			Mockito.doReturn(sessionObj).when(cluster).connect("authz");
			Mockito.doReturn(psObj).when(sessionObj).prepare(Mockito.anyString());
			
			Mockito.doReturn(Mockito.mock(ColumnDefinitions.class)).when(psObj).getVariables();
			Mockito.doReturn(Mockito.mock(PreparedId.class)).when(psObj).getPreparedId();
			Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
			Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
			Define.set(access);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Test
	public void testExec() {
		try {
			actionObj = new URFutureApproveExec(trans, cluster, true);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Approval approval = Mockito.mock(Approval.class);
    	List<Approval> approvalAL = new ArrayList<>();
    	approvalAL.add(approval);
   		Result<OP_STATUS> retVal = actionObj.exec(trans,approvalAL,Mockito.mock(Future.class));
   		assertTrue(8 == retVal.status);
		
	}
    
    @Test
   	public void testExecElseOpStatusD() {
		Result<OP_STATUS> retValD = new Result<OP_STATUS>(OP_STATUS.D, 0, "test", new String[0]);
		try {
			actionObj = new URFutureApproveExecImpl(trans, cluster, false, retValD);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       	Approval approval = new Approval(null, null, "", "test", "", "", "", "", 0L);
       	List<Approval> approvalAL = new ArrayList<>();
       	
       	Future futureObj = new Future(null, "", "", new Date(), new Date(), null);
       	
       	approvalAL.add(approval);
  		Result<OP_STATUS> retVal = actionObj.exec(trans,approvalAL,futureObj);
  		assertTrue(0 == retVal.status && "test".equals(retVal.toString()));
   		
   	}
    
    @Test
   	public void testExecElseOpStatusE() {
		Result<OP_STATUS> retValD = new Result<OP_STATUS>(OP_STATUS.E, 0, "test", new String[0]);
		try {
			actionObj = new URFutureApproveExecImpl(trans, cluster, false, retValD);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       	Approval approval = new Approval(null, null, "", "test", "", "", "", "", 0L);
       	List<Approval> approvalAL = new ArrayList<>();
       	
       	Future futureObj = new Future(null, "", "", new Date(), new Date(), null);
       	
       	approvalAL.add(approval);
  		Result<OP_STATUS> retVal = actionObj.exec(trans,approvalAL,futureObj);
  		assertTrue(0 == retVal.status && "test".equals(retVal.toString()));
   		
   	}
    
    @Test
   	public void testExecElseOpStatusL() {
		Result<OP_STATUS> retValD = new Result<OP_STATUS>(OP_STATUS.L, 0, "test", new String[0]);
		try {
			actionObj = new URFutureApproveExecImpl(trans, cluster, false, retValD);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       	Approval approval = new Approval(null, null, "", "test", "", "", "", "", 0L);
       	List<Approval> approvalAL = new ArrayList<>();
       	
       	Future futureObj = new Future(null, "", "", new Date(), new Date(), null);
       	
       	approvalAL.add(approval);
  		Result<OP_STATUS> retVal = actionObj.exec(trans,approvalAL,futureObj);
  		assertTrue(0 == retVal.status && "test".equals(retVal.toString()));
   		
   	}
    
    @Test
   	public void testExecElseOpStatusP() {
		Result<OP_STATUS> retValD = new Result<OP_STATUS>(OP_STATUS.P, 0, "test", new String[0]);
		try {
			actionObj = new URFutureApproveExecImpl(trans, cluster, false, retValD);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       	Approval approval = new Approval(null, null, "",  "test", "", "", "", "", 0L);
       	List<Approval> approvalAL = new ArrayList<>();
       	
       	Future futureObj = new Future(null, "", "", new Date(), new Date(), null);
       	
       	approvalAL.add(approval);
  		Result<OP_STATUS> retVal = actionObj.exec(trans,approvalAL,futureObj);
  		assertTrue(0 == retVal.status && "test".equals(retVal.toString()));
   		
   	}
    
    @Test
   	public void testExecElseNok() {
		Result<OP_STATUS> retValD = new Result<OP_STATUS>(null, 1, "test", new String[0]);
		try {
			actionObj = new URFutureApproveExecImpl(trans, cluster, false, retValD);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       	Approval approval = new Approval(null, null, "", "test", "", "", "", "", 0L);
       	List<Approval> approvalAL = new ArrayList<>();
       	
       	Future futureObj = new Future(null, "", "", new Date(), new Date(), null);
       	
       	approvalAL.add(approval);
  		Result<OP_STATUS> retVal = actionObj.exec(trans,approvalAL,futureObj);
  		System.out.println(retVal);
  		assertTrue(1 == retVal.status);
   		
   	}
    
    @Test
	public void test2Argonstructor() {
		actionObj = new URFutureApproveExec(trans, Mockito.mock(ActionDAO.class));
	}
   
    class URFutureApproveExecImpl extends URFutureApproveExec{
		
		public URFutureApproveExecImpl(AuthzTrans trans, Cluster cluster, boolean dryRun, Result<OP_STATUS> retValD)
				throws APIException, IOException {
			super(trans, cluster, dryRun);
			setFunction(Mockito.mock(Function.class));
			Mockito.doReturn(retValD).when(f).performFutureOp(Mockito.any(), Mockito.any(), Mockito.any(),Mockito.any(),Mockito.any());
		}

		public void setFunction(Function f) {
			Field field;
			try {
				field = URFutureApproveExecImpl.class.getSuperclass().getSuperclass().getDeclaredField("f");
				
				field.setAccessible(true);
		        // remove final modifier from field
		        Field modifiersField = Field.class.getDeclaredField("modifiers");
		        modifiersField.setAccessible(true);
		        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		        
		        field.set(this, f);
			} catch (NoSuchFieldException | SecurityException e) {
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
    }
}
