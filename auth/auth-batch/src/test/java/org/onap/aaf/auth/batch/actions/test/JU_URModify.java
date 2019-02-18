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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.batch.actions.ActionDAO;
import org.onap.aaf.auth.batch.actions.URModify;
import org.onap.aaf.auth.batch.actions.URPunt;
import org.onap.aaf.auth.batch.actions.URModify.Modify;
import org.onap.aaf.auth.batch.actions.test.JU_URPunt.URPuntImpl;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.cached.CachedUserRoleDAO;
import org.onap.aaf.auth.dao.cass.Status;
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

public class JU_URModify {
	@Mock
	AuthzTrans trans;
	@Mock
	Cluster cluster;
	@Mock
	PropAccess access;

	@Mock
	URModify modifyObj;

	@Before
	public void setUp() throws APIException, IOException {
		initMocks(this);
		Session sessionObj = Mockito.mock(Session.class);
		PreparedStatement psObj = Mockito.mock(PreparedStatement.class);
		try {
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).init();
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).warn();
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).info();
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
			Mockito.doReturn("10").when(trans).getProperty(Config.AAF_USER_EXPIRES, Config.AAF_USER_EXPIRES_DEF);
			Mockito.doReturn(Mockito.mock(TimeTaken.class)).when(trans).start(Mockito.anyString(), Mockito.anyInt());
			Mockito.doReturn(sessionObj).when(cluster).connect("authz");
			Mockito.doReturn(psObj).when(sessionObj).prepare(Mockito.anyString());

			Mockito.doReturn(Mockito.mock(ColumnDefinitions.class)).when(psObj).getVariables();
			Mockito.doReturn(Mockito.mock(PreparedId.class)).when(psObj).getPreparedId();
			Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
			Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS, "org.osaaf.aaf");
			Define.set(access);
			modifyObj = new URModify(trans, cluster, true);
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
	public void testURPrint() {
		UserRole ur = new UserRole("user", "ns", "rname", Calendar.getInstance().getTime());
		Result<Void> retVal = modifyObj.exec(trans, ur, Mockito.mock(Modify.class));
		assertTrue(retVal.toString().contains("Success"));
	}

	@Test
	public void testExecElse() {
		Question ques = Mockito.mock(Question.class);
		try {
			UserRole userRoleObj = new UserRole("test", "test", "test", new Date());
			CachedUserRoleDAO userRoleDaoObj = Mockito.mock(CachedUserRoleDAO.class);

			List<Data> dataAL = new ArrayList<Data>();
			Data data = new Data();
			data.expires = new Date();
			data.user="test";
			data.role="test.test";
			dataAL.add(data);
			Result<List<Data>> retVal = new Result<List<Data>>(dataAL, 0, "test", new String[0]);
			Result<List<Data>> retVal1 = new Result<List<Data>>(dataAL, 0, "test", new String[0]);

			Mockito.doReturn(retVal).when(userRoleDaoObj).read(trans, userRoleObj.user(), userRoleObj.role());
			Mockito.doReturn(retVal1).when(userRoleDaoObj).update(trans, data);

			modifyObj = new URModifyImpl(trans, cluster, false, ques, userRoleDaoObj);
			Result<Void> session = modifyObj.exec(trans, userRoleObj, Mockito.mock(Modify.class));
			assertTrue(Status.ERR_UserRoleNotFound == session.status);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void testExecElseUpdateFail() {
		Question ques = Mockito.mock(Question.class);
		try {
			UserRole userRoleObj = new UserRole("test", "test", "test", new Date());
			CachedUserRoleDAO userRoleDaoObj = Mockito.mock(CachedUserRoleDAO.class);

			List<Data> dataAL = new ArrayList<Data>();
			Data data = new Data();
			data.expires = new Date();
			data.user="test";
			data.role="test.test";
			dataAL.add(data);
			Result<List<Data>> retVal = new Result<List<Data>>(dataAL, 0, "test", new String[0]);
			Result<List<Data>> retVal1 = new Result<List<Data>>(dataAL, 1, "test", new String[0]);

			Mockito.doReturn(retVal).when(userRoleDaoObj).read(trans, userRoleObj.user(), userRoleObj.role());
			Mockito.doReturn(retVal1).when(userRoleDaoObj).update(trans, data);

			modifyObj = new URModifyImpl(trans, cluster, false, ques, userRoleDaoObj);
			Result<Void> session = modifyObj.exec(trans, userRoleObj, Mockito.mock(Modify.class));
			assertTrue(Status.ERR_UserRoleNotFound == session.status);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void testExecElseUserDiff() {
		Question ques = Mockito.mock(Question.class);
		try {
			UserRole userRoleObj = new UserRole("test", "test", "test", new Date());
			CachedUserRoleDAO userRoleDaoObj = Mockito.mock(CachedUserRoleDAO.class);

			List<Data> dataAL = new ArrayList<Data>();
			Data data = new Data();
			data.expires = new Date();
			dataAL.add(data);
			Result<List<Data>> retVal = new Result<List<Data>>(dataAL, 0, "test", new String[0]);

			Mockito.doReturn(retVal).when(userRoleDaoObj).read(trans, userRoleObj.user(), userRoleObj.role());

			modifyObj = new URModifyImpl(trans, cluster, false, ques, userRoleDaoObj);
			Result<Void> session = modifyObj.exec(trans, userRoleObj, Mockito.mock(Modify.class));
			assertTrue(Status.ERR_Denied == session.status);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void testExecElseReadNOK() {
		Question ques = Mockito.mock(Question.class);
		try {
			UserRole userRoleObj = new UserRole("test", "test", "test", new Date());
			CachedUserRoleDAO userRoleDaoObj = Mockito.mock(CachedUserRoleDAO.class);

			List<Data> dataAL = new ArrayList<Data>();
			Data data = new Data();
			data.expires = new Date();
			dataAL.add(data);
			Result<List<Data>> retVal = new Result<List<Data>>(dataAL, 1, "test", new String[0]);

			Mockito.doReturn(retVal).when(userRoleDaoObj).read(trans, userRoleObj.user(), userRoleObj.role());

			modifyObj = new URModifyImpl(trans, cluster, false, ques, userRoleDaoObj);
			Result<Void> session = modifyObj.exec(trans, userRoleObj, Mockito.mock(Modify.class));
			assertTrue(1 == session.status);
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
		modifyObj = new URModify(trans, Mockito.mock(ActionDAO.class));
	}
	
	class URModifyImpl extends URModify{

		public URModifyImpl(AuthzTrans trans, Cluster cluster, boolean dryRun, Question ques, CachedUserRoleDAO userRoleDaoObj) throws APIException, IOException {
			super(trans, cluster, dryRun);
			setQuestion(ques, userRoleDaoObj);
//			q =new Question(trans, cluster, CassAccess.KEYSPACE, false);
//			q = ques;
			// TODO Auto-generated constructor stub
		}
		
		public void setQuestion(Question ques, CachedUserRoleDAO userRoleDaoObj) {
			Field field, nsDaoField;
			try {
				field = URPuntImpl.class.getSuperclass().getSuperclass().getSuperclass().getDeclaredField("q");
				nsDaoField = Question.class.getDeclaredField("userRoleDAO");
				
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
