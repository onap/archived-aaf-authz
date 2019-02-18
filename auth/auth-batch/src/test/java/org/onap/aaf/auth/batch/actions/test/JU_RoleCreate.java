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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.batch.actions.ActionDAO;
import org.onap.aaf.auth.batch.actions.RoleCreate;
import org.onap.aaf.auth.batch.helpers.Role;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.cached.CachedRoleDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO.Data;
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

public class JU_RoleCreate {
    
	@Mock
    AuthzTrans trans;
	@Mock
	Cluster cluster;
	@Mock
	PropAccess access;
	
	@Mock
	RoleCreate createObj;

    
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
			createObj = new RoleCreate(trans, cluster, true);
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
    }
    
    @Test
	public void testExec() {
    		Result<RoleDAO.Data> retVal = createObj.exec(trans,Mockito.mock(Role.class),"test");
    		assertTrue(retVal.toString().contains("Success"));
		
	}
    @Test
	public void testExecElse() {
    	Question ques = Mockito.mock(Question.class);
		try {
			Role roleObj = new Role("test","test","test",new HashSet());
	        
			CachedRoleDAO roleDaoObj = Mockito.mock(CachedRoleDAO.class);
			
			List<Data> dataAL = new ArrayList<Data>();
			Data data = new Data();
			data.expires = new Date();
			dataAL.add(data);
			Result<List<Data>> retVal1 = new Result<List<Data>>(dataAL,0,"test",new String[0]);

			Mockito.doReturn(retVal1).when(roleDaoObj).create(Mockito.any(), Mockito.any());
			
			createObj = new RoleCreateImpl(trans,  cluster, false, ques, roleDaoObj);
    		Result<RoleDAO.Data> session = createObj.exec(trans, roleObj, "test");
    		assertTrue(0 == session.status);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    
    @Test
	public void testExecElseDateLess() {
    	Question ques = Mockito.mock(Question.class);
		try {
			Role roleObj = new Role("test","test","test",new HashSet());
	        
			CachedRoleDAO userRoleDaoObj = Mockito.mock(CachedRoleDAO.class);
			
			List<Data> dataAL = new ArrayList<Data>();
			Data data = new Data();
			DateFormat sdf = new SimpleDateFormat("mm/dd/yyyy");
			try {
				data.expires = sdf.parse("01/01/2100");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dataAL.add(data);
			Result<List<Data>> retVal1 = new Result<List<Data>>(dataAL,0,"test",new String[0]);

			Mockito.doReturn(retVal1).when(userRoleDaoObj).create(Mockito.any(), Mockito.any());
			
			createObj = new RoleCreateImpl(trans,  cluster, false, ques, userRoleDaoObj);
    		Result<RoleDAO.Data> session = createObj.exec(trans, roleObj, "test");
    		assertTrue(0 == session.status);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    
    @Test
   	public void testExecElseNok() {
       	Question ques = Mockito.mock(Question.class);
   		try {
   			Role roleObj = new Role("test","test","test",new HashSet());
	        
   			CachedRoleDAO userRoleDaoObj = Mockito.mock(CachedRoleDAO.class);
   			
   			Result<Void> retVal1 = new Result<Void>(null,1,"test",new String[0]);

   			Mockito.doReturn(retVal1).when(userRoleDaoObj).create(Mockito.any(), Mockito.any());
   			
   			createObj = new RoleCreateImpl(trans,  cluster, false, ques, userRoleDaoObj);
       		Result<RoleDAO.Data> session = createObj.exec(trans, roleObj, "test");
       		assertTrue(session.toString().contains("test"));
   		} catch (APIException | IOException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		} catch (IllegalArgumentException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}
   		
   	}

    @Test
	public void test2Argonstructor() {
		createObj = new RoleCreate(trans, Mockito.mock(ActionDAO.class));
	}
   
    class RoleCreateImpl extends RoleCreate{

		public RoleCreateImpl(AuthzTrans trans, Cluster cluster, boolean dryRun, Question ques, CachedRoleDAO userRoleDaoObj) throws APIException, IOException {
			super(trans, cluster, dryRun);
			setQuestion(ques, userRoleDaoObj);
		}
		
		public void setQuestion(Question ques, CachedRoleDAO userRoleDaoObj) {
			Field field, nsDaoField;
			try {
				field = RoleCreateImpl.class.getSuperclass().getSuperclass().getDeclaredField("q");
				nsDaoField = Question.class.getDeclaredField("roleDAO");
				
				field.setAccessible(true);
				nsDaoField.setAccessible(true);
		        // remove final modifier from field
		        Field modifiersField = Field.class.getDeclaredField("modifiers");
		        modifiersField.setAccessible(true);
		        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		        modifiersField.setInt(nsDaoField, field.getModifiers() & ~Modifier.FINAL);
		        
		        field.set(this, ques);
		        nsDaoField.set(ques, userRoleDaoObj);
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
