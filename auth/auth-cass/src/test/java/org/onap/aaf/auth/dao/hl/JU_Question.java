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

package org.onap.aaf.auth.dao.hl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.cache.Cache;
import org.onap.aaf.auth.dao.DAOException;
import org.onap.aaf.auth.dao.cached.CachedCredDAO;
import org.onap.aaf.auth.dao.cached.CachedNSDAO;
import org.onap.aaf.auth.dao.cached.CachedPermDAO;
import org.onap.aaf.auth.dao.cached.CachedRoleDAO;
import org.onap.aaf.auth.dao.cached.CachedUserRoleDAO;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.DelegateDAO;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.NsSplit;
import org.onap.aaf.auth.dao.cass.NsType;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.hl.Question.Access;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Encryptor;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.PreparedId;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;

public class JU_Question {

	@Mock
	AuthzTrans trans;

	@Mock
	Cluster cluster;

	Question question;

	@Before
	public void setUp() {
		initMocks(this);
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).init();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).warn();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).info();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).audit();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).trace();
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("ApprovalDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on ApprovalDAO", Env.SUB);
		Mockito.doReturn(tt).when(trans).start(Mockito.anyString(), Mockito.anyInt());
	}

	@Test
	public void testConstructor() {
		Mockito.when(trans.getProperty(Config.AAF_USER_EXPIRES, Config.AAF_USER_EXPIRES_DEF)).thenReturn("20");
		Mockito.when(trans.getProperty(Config.AAF_CRED_WARN_DAYS, Config.AAF_CRED_WARN_DAYS_DFT)).thenReturn("20");
		Session sess = Mockito.mock(Session.class);
		PreparedStatement ps = Mockito.mock(PreparedStatement.class);
		Mockito.when(ps.getVariables()).thenReturn(Mockito.mock(ColumnDefinitions.class));
		Mockito.when(ps.getPreparedId()).thenReturn(Mockito.mock(PreparedId.class));

		Mockito.when(cluster.connect(Mockito.anyString())).thenReturn(sess);
		Mockito.when(sess.prepare(Mockito.anyString())).thenReturn(ps);
		// Mockito.doNothing().when(AbsCassDAO).primePSIs(trans);
		try {
			question = new Question(trans, cluster, "keyspace");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testClose() {
		testConstructor();
		question.close(trans);
	}

	@Test
	public void testPermForm() {
		testConstructor();
		Result<List<NsDAO.Data>> rs = new Result<List<NsDAO.Data>>(null, 0, "test", new Object[0]);
		// Mockito.when(question.nsDAO.read(trans, "type")).thenReturn(rs);
		CachedNSDAO nsDaoMock = Mockito.mock(CachedNSDAO.class);
		setField(question, nsDaoMock);
		Mockito.doReturn(rs).when(question.nsDAO()).read(trans, "type");
		question.permFrom(trans, "type", "instance", "action");

		List<NsDAO.Data> dataAL = new ArrayList<>();
		NsDAO.Data data = new NsDAO.Data();
		data.name = "name";
		dataAL.add(data);
		rs = new Result<List<NsDAO.Data>>(dataAL, 0, "test", new Object[0]);
		Mockito.doReturn(rs).when(nsDaoMock).read(trans, "type");
		question.permFrom(trans, "type", "instance", "action");

	}

	public void setField(Question ques, CachedNSDAO nsDao) {
		Field nsDaoField;
		try {
			nsDaoField = Question.class.getDeclaredField("nsDAO");

			nsDaoField.setAccessible(true);
			// remove final modifier from field
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			// modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() &
			// ~Modifier.FINAL);

			nsDaoField.set(ques, nsDao);
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
	public void setPermDaoField(Question ques, CachedPermDAO nsDao) {
		Field nsDaoField;
		try {
			nsDaoField = Question.class.getDeclaredField("permDAO");

			nsDaoField.setAccessible(true);
			// remove final modifier from field
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			// modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() &
			// ~Modifier.FINAL);

			nsDaoField.set(ques, nsDao);
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
	public void setDelegateDaoField(Question ques, DelegateDAO delDao) {
		Field nsDaoField;
		try {
			nsDaoField = Question.class.getDeclaredField("delegateDAO");

			nsDaoField.setAccessible(true);
			// remove final modifier from field
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			// modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() &
			// ~Modifier.FINAL);

			nsDaoField.set(ques, delDao);
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
	public void setCredDaoField(Question ques, CachedCredDAO credDao) {
		Field credDaoField;
		try {
			credDaoField = Question.class.getDeclaredField("credDAO");

			credDaoField.setAccessible(true);
			// remove final modifier from field
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			// modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() &
			// ~Modifier.FINAL);

			credDaoField.set(ques, credDao);
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
	public void setRoleDaoField(Question ques, CachedRoleDAO roleDao) {
		Field nsDaoField;
		try {
			nsDaoField = Question.class.getDeclaredField("roleDAO");

			nsDaoField.setAccessible(true);
			// remove final modifier from field
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			// modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() &
			// ~Modifier.FINAL);

			nsDaoField.set(ques, roleDao);
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
	public void setUserRoleDaoField(Question ques, CachedUserRoleDAO userRoleDao) {
		Field nsDaoField;
		try {
			nsDaoField = Question.class.getDeclaredField("userRoleDAO");

			nsDaoField.setAccessible(true);
			// remove final modifier from field
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			// modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() &
			// ~Modifier.FINAL);

			nsDaoField.set(ques, userRoleDao);
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
	@Test
	public void testGetPermsByUser() {
		testConstructor();
		List<UserRoleDAO.Data> dataAL = new ArrayList<>();
		UserRoleDAO.Data data = new UserRoleDAO.Data();
		data.rname = "name";
		data.expires = new Date();
		dataAL.add(data);
		Result<List<UserRoleDAO.Data>> rs = new Result<List<UserRoleDAO.Data>>(dataAL, 0, "test", new Object[0]);
		CachedUserRoleDAO userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		Mockito.doReturn(rs).when(userRoleDao).readByUser(trans, "user");

		question.getPermsByUser(trans, "user", true);
	}

	@Test
	public void testGetPermsByUserFromRolesFilter() {
		testConstructor();
		Result<List<UserRoleDAO.Data>> rs = new Result<List<UserRoleDAO.Data>>(null, 1, "test", new Object[0]);
		CachedUserRoleDAO userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		Mockito.doReturn(rs).when(userRoleDao).readByUser(trans, "user");

		question.getPermsByUserFromRolesFilter(trans, "user", "forUser");

		List<UserRoleDAO.Data> dataAL = new ArrayList<>();
		UserRoleDAO.Data data = new UserRoleDAO.Data();
		data.rname = "name";
		data.ns = "name";
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		data.expires = calendar.getTime();
		dataAL.add(data);
		rs = new Result<List<UserRoleDAO.Data>>(dataAL, 0, "test", new Object[0]);
		userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		Mockito.doReturn(rs).when(userRoleDao).readByUser(trans, "user");
		Mockito.doReturn(rs).when(userRoleDao).readByUser(trans, "forUser");

		List<RoleDAO.Data> dataRoleAL = new ArrayList<>();
		RoleDAO.Data roleData = new RoleDAO.Data();
		roleData.name="name";
		roleData.perms = new HashSet<>();
		roleData.perms.add("test");
		dataRoleAL.add(roleData);
		Result<List<RoleDAO.Data>> rsRole = new Result<List<RoleDAO.Data>>(dataRoleAL, 0, "test", new Object[0]);
		CachedRoleDAO roleDaoObj = Mockito.mock(CachedRoleDAO.class);
		setRoleDaoField(question, roleDaoObj);
		Mockito.doReturn(rsRole).when(roleDaoObj).read(trans, "name", "name");

		Result<List<PermDAO.Data>> retVal = question.getPermsByUserFromRolesFilter(trans, "user", "forUser");
//		assertTrue(retVal.status == 0);
		
		retVal = question.getPermsByUserFromRolesFilter(trans, "user", "user");
//		assertTrue(retVal.status == 0);
		
		rs = new Result<List<UserRoleDAO.Data>>(null, 1, "test", new Object[0]);
//		Mockito.doReturn(rs).when(roleDaoObj).readByUser(trans, "forUser");
		retVal = question.getPermsByUserFromRolesFilter(trans, "user", "forUser");
	}
	
	@Test
	public void testGetPermsByType() {
		testConstructor();
		List<UserRoleDAO.Data> dataAL = new ArrayList<>();
		UserRoleDAO.Data data = new UserRoleDAO.Data();
		data.rname = "name";
		data.expires = new Date();
		dataAL.add(data);
		Result<List<UserRoleDAO.Data>> rs = new Result<List<UserRoleDAO.Data>>(dataAL, 0, "test", new Object[0]);
		CachedUserRoleDAO userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		Mockito.doReturn(rs).when(userRoleDao).readByUser(trans, "user");

		CachedNSDAO nsDaoMock = Mockito.mock(CachedNSDAO.class);
		setField(question, nsDaoMock);
		List<NsDAO.Data> dataNsAL = new ArrayList<>();
		NsDAO.Data dataNs = new NsDAO.Data();
		dataNs.name = "name";
		dataNsAL.add(dataNs);
		Result<List<NsDAO.Data>> rsNsDao = new Result<List<NsDAO.Data>>(dataNsAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsNsDao).when(nsDaoMock).read(trans, "user");
		
		CachedPermDAO permDaoMock = Mockito.mock(CachedPermDAO.class);
		setPermDaoField(question, permDaoMock);
		question.getPermsByType(trans, "user");

		Mockito.doReturn(rsNsDao).when(nsDaoMock).read(trans, "name");
		question.getPermsByType(trans, "name");
	}
	@Test
	public void testGetPermsByName() {
		testConstructor();
		List<UserRoleDAO.Data> dataAL = new ArrayList<>();
		UserRoleDAO.Data data = new UserRoleDAO.Data();
		data.rname = "name";
		data.expires = new Date();
		dataAL.add(data);
		Result<List<UserRoleDAO.Data>> rs = new Result<List<UserRoleDAO.Data>>(dataAL, 0, "test", new Object[0]);
		CachedUserRoleDAO userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		Mockito.doReturn(rs).when(userRoleDao).readByUser(trans, "user");

		CachedNSDAO nsDaoMock = Mockito.mock(CachedNSDAO.class);
		setField(question, nsDaoMock);
		List<NsDAO.Data> dataNsAL = new ArrayList<>();
		NsDAO.Data dataNs = new NsDAO.Data();
		dataNs.name = "name";
		dataNsAL.add(dataNs);
		Result<List<NsDAO.Data>> rsNsDao = new Result<List<NsDAO.Data>>(dataNsAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsNsDao).when(nsDaoMock).read(trans, "user");
		
		CachedPermDAO permDaoMock = Mockito.mock(CachedPermDAO.class);
		setPermDaoField(question, permDaoMock);
		question.getPermsByName(trans, "user","test","test");

		Mockito.doReturn(rsNsDao).when(nsDaoMock).read(trans, "name");
		question.getPermsByName(trans, "name","test","test");
	}
	@Test
	public void testGetPermsByRole() {
		testConstructor();
		List<UserRoleDAO.Data> dataAL = new ArrayList<>();
		UserRoleDAO.Data data = new UserRoleDAO.Data();
		data.rname = "name";
		data.expires = new Date();
		dataAL.add(data);
		Result<List<UserRoleDAO.Data>> rs = new Result<List<UserRoleDAO.Data>>(dataAL, 0, "test", new Object[0]);
		CachedUserRoleDAO userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		Mockito.doReturn(rs).when(userRoleDao).readByUser(trans, "user");

		CachedNSDAO nsDaoMock = Mockito.mock(CachedNSDAO.class);
		setField(question, nsDaoMock);
		List<NsDAO.Data> dataNsAL = new ArrayList<>();
		NsDAO.Data dataNs = new NsDAO.Data();
		dataNs.name = "name";
		dataNsAL.add(dataNs);
		Result<List<NsDAO.Data>> rsNsDao = new Result<List<NsDAO.Data>>(dataNsAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsNsDao).when(nsDaoMock).read(trans, "user");
		
		question.getPermsByRole(trans, "user", true);

		Result<List<RoleDAO.Data>> rsRole = new Result<List<RoleDAO.Data>>(null, 0, "test", new Object[0]);
		CachedRoleDAO roleDao = Mockito.mock(CachedRoleDAO.class);
		setRoleDaoField(question, roleDao);
		Mockito.doReturn(rsRole).when(roleDao).read(trans, "name", "");
		
		Mockito.doReturn(rsNsDao).when(nsDaoMock).read(trans, "name");
		question.getPermsByRole(trans, "name", true);
		
		List<RoleDAO.Data> dataRoleAL = new ArrayList<>();
		RoleDAO.Data roleData = new RoleDAO.Data();
		roleData.name="name";
		roleData.perms = new HashSet<>();
		roleData.perms.add("test");
		dataRoleAL.add(roleData);
		rsRole = new Result<List<RoleDAO.Data>>(dataRoleAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsRole).when(roleDao).read(trans, "name", "");

		Mockito.doReturn(rsNsDao).when(nsDaoMock).read(trans, "name");
		Result<List<PermDAO.Data>> retVal = question.getPermsByRole(trans, "name", true);
		assertTrue(retVal.status == 4);
		
		dataRoleAL = new ArrayList<>();
		roleData = new RoleDAO.Data();
		roleData.name="name";
		roleData.perms = new HashSet<>();
		roleData.perms.add("test|test|test|test");
		dataRoleAL.add(roleData);
		rsRole = new Result<List<RoleDAO.Data>>(dataRoleAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsRole).when(roleDao).read(trans, "name", "");
		
		CachedPermDAO permDaoMock = Mockito.mock(CachedPermDAO.class);
		setPermDaoField(question, permDaoMock);
		Result<List<PermDAO.Data>> rsPerm = new Result<List<PermDAO.Data>>(null, 0, "test", new Object[0]);
		Mockito.doReturn(rsPerm).when(question.permDAO()).read(Mockito.any(), Mockito.any(PermDAO.Data.class));

		retVal = question.getPermsByRole(trans, "name", true);
		assertTrue(retVal.status == 0);
		
		retVal = question.getPermsByRole(trans, "name", false);
		assertTrue(retVal.status == 0);
		
		List<PermDAO.Data> dataPermAl = new ArrayList<>();
		PermDAO.Data dataPerm = new PermDAO.Data();
		dataPermAl.add(dataPerm);
		rsPerm = new Result<List<PermDAO.Data>>(dataPermAl, 0, "test", new Object[0]);
		Mockito.doReturn(rsPerm).when(permDaoMock).read(Mockito.any(), Mockito.any(PermDAO.Data.class));
		
		retVal = question.getPermsByRole(trans, "name", true);
		assertTrue(retVal.status == 0);
	}
	
	@Test
	public void testStartTimers() {
		testConstructor();
		AuthzEnv env= Mockito.mock(AuthzEnv.class);
		Mockito.when(env.getProperty(Cache.CACHE_CLEAN_INTERVAL,"60000")).thenReturn("20");
		Mockito.when(env.getProperty(Cache.CACHE_HIGH_COUNT,"5000")).thenReturn("20");
		Mockito.doReturn(env).when(trans).env();
		question.startTimers(env);
	}
	
	@Test
	public void testDaoGetterMethods() {
		testConstructor();
		question.historyDAO();
		question.certDAO();
		question.futureDAO();
		question.approvalDAO();
		question.locateDAO();
	}
	
	@Test
	public void testGetRolesByName() {
		testConstructor();
		List<NsDAO.Data> dataAL = new ArrayList<>();
		NsDAO.Data data = new NsDAO.Data();
		data.name = "name";
		dataAL.add(data);
		Result<List<NsDAO.Data>> rs = new Result<List<NsDAO.Data>>(dataAL, 0, "test", new Object[0]);
		CachedNSDAO nsDaoMock = Mockito.mock(CachedNSDAO.class);
		setField(question, nsDaoMock);
		Mockito.doReturn(rs).when(nsDaoMock).read(trans, "user");

		Result<List<RoleDAO.Data>> retVal = question.getRolesByName(trans, "user");
		assertTrue(retVal.status == 21);
		
		dataAL = new ArrayList<>();
		data = new NsDAO.Data();
		data.name = "user";
		dataAL.add(data);
		rs = new Result<List<NsDAO.Data>>(dataAL, 0, "test", new Object[0]);
		Mockito.doReturn(rs).when(nsDaoMock).read(trans, "user");
		
		List<RoleDAO.Data> dataRoleAL = new ArrayList<>();
		RoleDAO.Data roleData = new RoleDAO.Data();
		roleData.name="name";
		roleData.perms = new HashSet<>();
		roleData.perms.add("test");
		dataRoleAL.add(roleData);
		Result<List<RoleDAO.Data>> rsRole = new Result<List<RoleDAO.Data>>(dataRoleAL, 0, "test", new Object[0]);
		CachedRoleDAO roleDao = Mockito.mock(CachedRoleDAO.class);
		setRoleDaoField(question, roleDao);
		roleDao = Mockito.mock(CachedRoleDAO.class);
		Mockito.doReturn(rsRole).when(roleDao).read(trans, "name", "name");
		
		retVal = question.getRolesByName(trans, "user");
		
		dataAL = new ArrayList<>();
		data = new NsDAO.Data();
		data.name = "user";
		dataAL.add(data);
		rs = new Result<List<NsDAO.Data>>(dataAL, 0, "test", new Object[0]);
		Mockito.doReturn(rs).when(nsDaoMock).read(trans, "user.*");
		
		retVal = question.getRolesByName(trans, "user.*");
		
		dataAL = new ArrayList<>();
		data = new NsDAO.Data();
		data.name = "user";
		dataAL.add(data);
		rs = new Result<List<NsDAO.Data>>(dataAL, 0, "test", new Object[0]);
		Mockito.doReturn(rs).when(nsDaoMock).read(trans, "user..*");
		
		retVal = question.getRolesByName(trans, "user..*");
	}
	
	@Test
	public void testDeriveNs() {
		testConstructor();
		List<NsDAO.Data> dataAL = new ArrayList<>();
		NsDAO.Data data = new NsDAO.Data();
		data.name = "user.test";
		dataAL.add(data);
		Result<List<NsDAO.Data>> rs = new Result<List<NsDAO.Data>>(dataAL, 1, "test", new Object[0]);
		CachedNSDAO nsDaoMock = Mockito.mock(CachedNSDAO.class);
		setField(question, nsDaoMock);
		
		Mockito.doReturn(rs).when(nsDaoMock).read(Mockito.any(), Mockito.anyString());
		
		Result<NsDAO.Data> retVal = question.deriveNs(trans, null);
		assertTrue(retVal.status == 21);
		
		retVal = question.deriveNs(trans, "user.test");
		assertTrue(retVal.status == 21);
		
		rs = new Result<List<NsDAO.Data>>(dataAL, 0, "test", new Object[0]);

		Mockito.doReturn(rs).when(nsDaoMock).read(Mockito.any(), Mockito.anyString());
		retVal = question.deriveNs(trans, null);
		assertTrue(retVal.status == 0);
	}
	
	@Test
	public void testDeriveNsForType() {
		testConstructor();
		List<NsDAO.Data> dataAL = new ArrayList<>();
		NsDAO.Data data = new NsDAO.Data();
		data.name = "user.test";
		dataAL.add(data);
		Result<List<NsDAO.Data>> rs = new Result<List<NsDAO.Data>>(dataAL, 1, "test", new Object[0]);
		CachedNSDAO nsDaoMock = Mockito.mock(CachedNSDAO.class);
		setField(question, nsDaoMock);
		
		Mockito.doReturn(rs).when(nsDaoMock).read(Mockito.any(), Mockito.anyString());
		
		Result<NsDAO.Data> retVal = question.deriveFirstNsForType(trans, "str", NsType.APP);
		assertTrue(retVal.status == 21);
		
		retVal = question.deriveFirstNsForType(trans, "str.test", NsType.APP);
		assertTrue(retVal.status == 21);
		
		rs = new Result<List<NsDAO.Data>>(dataAL, 0, "test", new Object[0]);
		Mockito.doReturn(rs).when(nsDaoMock).read(Mockito.any(), Mockito.anyString());
		
		retVal = question.deriveFirstNsForType(trans, "str", NsType.APP);
		assertTrue(retVal.status == 21);
		
		retVal = question.deriveFirstNsForType(trans, "str.test", NsType.APP);
		assertTrue(retVal.status == 21);
		
		retVal = question.deriveFirstNsForType(trans, "str.test", NsType.DOT);
		assertTrue(retVal.status == 0);
		
		retVal = question.deriveFirstNsForType(trans, null, NsType.DOT);
		assertTrue(retVal.status == 6);
	}
	
	@Test
	public void testDeriveNsSplit() {
		testConstructor();
		List<NsDAO.Data> dataAL = new ArrayList<>();
		NsDAO.Data data = new NsDAO.Data();
		data.name = "user.test";
		dataAL.add(data);
		Result<List<NsDAO.Data>> rs = new Result<List<NsDAO.Data>>(dataAL, 1, "test", new Object[0]);
		CachedNSDAO nsDaoMock = Mockito.mock(CachedNSDAO.class);
		setField(question, nsDaoMock);
		
		Mockito.doReturn(rs).when(nsDaoMock).read(Mockito.any(), Mockito.anyString());
		
		Result<NsSplit> retVal = question.deriveNsSplit(trans, "str");
		assertTrue(retVal.status == 21);
		
	}
	
	@Test
	public void testValidNSOfDomain() {
		testConstructor();
		List<NsDAO.Data> dataAL = new ArrayList<>();
		NsDAO.Data data = new NsDAO.Data();
		data.name = "user.test";
		dataAL.add(data);
		Result<List<NsDAO.Data>> rs = new Result<List<NsDAO.Data>>(dataAL, 1, "test", new Object[0]);
		CachedNSDAO nsDaoMock = Mockito.mock(CachedNSDAO.class);
		setField(question, nsDaoMock);
		Mockito.doReturn(rs).when(nsDaoMock).read(Mockito.any(), Mockito.anyString());
	
		Organization org = Mockito.mock(Organization.class);
		Mockito.doReturn(org).when(trans).org();
		Mockito.doReturn("domain").when(org).getDomain();
		
		Result<NsDAO.Data> retVal = question.validNSOfDomain(trans, "str");
		assertTrue(retVal.status == 21);
		
		rs = new Result<List<NsDAO.Data>>(dataAL, 0, "test", new Object[0]);
		Mockito.doReturn(rs).when(nsDaoMock).read(Mockito.any(), Mockito.anyString());
		retVal = question.validNSOfDomain(trans, "str");
		assertTrue(retVal.status == 0);
		
		retVal = question.validNSOfDomain(trans, "domain");
		assertTrue(retVal.status == 21);

		retVal = question.validNSOfDomain(trans, "");
		assertTrue(retVal.status == 21);

		retVal = question.validNSOfDomain(trans, "test@domain");
		assertTrue(retVal.status == 21);
	}
	
	@Test
	public void testMayUser() {
		testConstructor();

		List<UserRoleDAO.Data> dataAL = new ArrayList<>();
		UserRoleDAO.Data data = new UserRoleDAO.Data();
		data.rname = "name";
		data.ns="ns";
		data.rname="rname";
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		data.expires = calendar.getTime();
		dataAL.add(data);
		Result<List<UserRoleDAO.Data>> rs = new Result<List<UserRoleDAO.Data>>(dataAL, 0, "test", new Object[0]);
		CachedUserRoleDAO userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		Mockito.doReturn(rs).when(userRoleDao).readByUser(trans, "user");
		Mockito.doReturn(rs).when(userRoleDao).readUserInRole(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		NsDAO.Data ndd = new NsDAO.Data();
		ndd.name="name";
		
		List<RoleDAO.Data> dataRoleAL = new ArrayList<>();
		RoleDAO.Data roleData = new RoleDAO.Data();
		roleData.name="name";
		roleData.perms = new HashSet<>();
		roleData.perms.add("test");
		roleData.perms=new HashSet<>();
		roleData.perms.add("name|access|:ns|read");
		dataRoleAL.add(roleData);
		Result<List<RoleDAO.Data>> rsRole = new Result<List<RoleDAO.Data>>(dataRoleAL, 0, "test", new Object[0]);
		CachedRoleDAO roleDao = Mockito.mock(CachedRoleDAO.class);
		setRoleDaoField(question, roleDao);
		Mockito.doReturn(rsRole).when(roleDao).read(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		
		Result<NsDAO.Data> retVal = question.mayUser(trans, "user", ndd, Access.read);
		assertTrue(retVal.status == 0);
		
		dataRoleAL = new ArrayList<>();
		roleData = new RoleDAO.Data();
		roleData.name="name";
		roleData.perms = new HashSet<>();
		roleData.perms.add("test");
		roleData.perms=new HashSet<>();
		roleData.perms.add("name|access|:ns|perms4");
		dataRoleAL.add(roleData);
		rsRole = new Result<List<RoleDAO.Data>>(dataRoleAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsRole).when(roleDao).read(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		ndd.name="name.name";
		retVal = question.mayUser(trans, "user", ndd, Access.read);
		assertTrue(retVal.status == 0);

		ndd.name="name";
		rs = new Result<List<UserRoleDAO.Data>>(dataAL, 1, "test", new Object[0]);
		Mockito.doReturn(rs).when(userRoleDao).readUserInRole(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		retVal = question.mayUser(trans, "user", ndd, Access.read);
		assertTrue(retVal.status == 2);
		
	}
	
	@Test
	public void testMayUser2() {
		testConstructor();
		RoleDAO.Data rdd = new RoleDAO.Data();
		rdd.name="name";
		rdd.ns="ns";

		List<NsDAO.Data> dataAL = new ArrayList<>();
		NsDAO.Data data = new NsDAO.Data();
		data.name = "name";
		dataAL.add(data);
		Result<List<NsDAO.Data>> rsNs = new Result<List<NsDAO.Data>>(dataAL, 0, "test", new Object[0]);
		CachedNSDAO nsDaoMock = Mockito.mock(CachedNSDAO.class);
		setField(question, nsDaoMock);
		Mockito.doReturn(rsNs).when(nsDaoMock).read(trans, "ns");
		
		List<UserRoleDAO.Data> dataURAL = new ArrayList<>();
		UserRoleDAO.Data dataUR = new UserRoleDAO.Data();
		dataUR.rname = "name";
		dataUR.ns="ns";
		dataUR.rname="rname";
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		dataUR.expires = calendar.getTime();
		dataURAL.add(dataUR);
		Result<List<UserRoleDAO.Data>> rs = new Result<List<UserRoleDAO.Data>>(dataURAL, 0, "test", new Object[0]);
		CachedUserRoleDAO userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		Mockito.doReturn(rs).when(userRoleDao).readUserInRole(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		
		Result<NsDAO.Data> retVal = question.mayUser(trans, "user", rdd, Access.read);
		assertTrue(retVal.status == 0);
		
		rdd.ns=null;
		rsNs = new Result<List<NsDAO.Data>>(dataAL, 1, "test", new Object[0]);
		Mockito.doReturn(rsNs).when(nsDaoMock).read(Mockito.any(), Mockito.any(Object.class));
		retVal = question.mayUser(trans, "user", rdd, Access.read);
		assertTrue(retVal.status == 21);
	}
	@Test
	public void testMayUser3() {
		testConstructor();
		NsDAO.Data ndd = new NsDAO.Data();
		ndd.name="name";
		ndd.description="description";
		RoleDAO.Data rdd = new RoleDAO.Data();
		rdd.name="name";
		rdd.ns="name";

//		List<NsDAO.Data> dataAL = new ArrayList<>();
//		NsDAO.Data data = new NsDAO.Data();
//		data.name = "name";
//		dataAL.add(data);
//		Result<List<NsDAO.Data>> rsNs = new Result<List<NsDAO.Data>>(dataAL, 0, "test", new Object[0]);
//		CachedNSDAO nsDaoMock = Mockito.mock(CachedNSDAO.class);
//		setField(question, nsDaoMock);
//		Mockito.doReturn(rsNs).when(nsDaoMock).read(trans, "ns");
//		
		List<UserRoleDAO.Data> dataURAL = new ArrayList<>();
		UserRoleDAO.Data dataUR = new UserRoleDAO.Data();
		dataUR.rname = "name";
		dataUR.ns="name";
		dataUR.rname="rname";
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		dataUR.expires = calendar.getTime();
		dataURAL.add(dataUR);
		Result<List<UserRoleDAO.Data>> rs = new Result<List<UserRoleDAO.Data>>(dataURAL, 1, "test", new Object[0]);
		Result<List<UserRoleDAO.Data>> rsSuccess = new Result<List<UserRoleDAO.Data>>(dataURAL, 0, "test", new Object[0]);
		CachedUserRoleDAO userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		Mockito.doReturn(rs).when(userRoleDao).readByUser(Mockito.any(), Mockito.anyString());
		Mockito.doReturn(rs).when(userRoleDao).readUserInRole(trans, "user", "name.name");
		Mockito.doReturn(rs).when(userRoleDao).readUserInRole(trans, "user", "name.admin");

		Result<NsDAO.Data> retVal = question.mayUser(trans, "user", ndd, rdd, Access.read);
		assertTrue(retVal.status == 2);
		
		rdd.ns="name.name";
		Mockito.doReturn(rs).when(userRoleDao).readUserInRole(trans, "user", "name.name.name");
		retVal = question.mayUser(trans, "user", ndd, rdd, Access.read);
		assertTrue(retVal.status == 2);

		Mockito.doReturn(rsSuccess).when(userRoleDao).readUserInRole(trans, "user", "name.name.name");
		retVal = question.mayUser(trans, "user", ndd, rdd, Access.read);
		assertTrue(retVal.status == 0);
		Mockito.doReturn(rs).when(userRoleDao).readUserInRole(trans, "user", "name.name.name");
		
		Mockito.doReturn(rsSuccess).when(userRoleDao).readUserInRole(trans, "user", "name.admin");
		retVal = question.mayUser(trans, "user", ndd, rdd, Access.read);
		assertTrue(retVal.status == 0);
		
		List<RoleDAO.Data> dataRoleAL = new ArrayList<>();
		RoleDAO.Data roleData = new RoleDAO.Data();
		roleData.name="name";
		roleData.perms = new HashSet<>();
		roleData.perms.add("test");
		roleData.perms=new HashSet<>();
		roleData.perms.add("name|access|:role:name|read");
		dataRoleAL.add(roleData);
		CachedRoleDAO roleDao = Mockito.mock(CachedRoleDAO.class);
		setRoleDaoField(question, roleDao);
		Result<List<RoleDAO.Data>> rsRole = new Result<List<RoleDAO.Data>>(dataRoleAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsRole).when(roleDao).read(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.doReturn(rsSuccess).when(userRoleDao).readByUser(Mockito.any(), Mockito.anyString());

		retVal = question.mayUser(trans, "user", ndd, rdd, Access.read);
		assertTrue(retVal.status == 0);
		
	}
	
	@Test
	public void testMayUser4() {
		testConstructor();
		PermDAO.Data pdd = new PermDAO.Data();
		pdd.action="action";
		pdd.ns = "ns";
		pdd.description="description";
		
		List<NsDAO.Data> dataAL = new ArrayList<>();
		NsDAO.Data data = new NsDAO.Data();
		data.name = "name";
		dataAL.add(data);
		Result<List<NsDAO.Data>> rs = new Result<List<NsDAO.Data>>(dataAL, 0, "test", new Object[0]);
		CachedNSDAO nsDaoMock = Mockito.mock(CachedNSDAO.class);
		setField(question, nsDaoMock);
		Mockito.doReturn(rs).when(nsDaoMock).read(trans, "ns");
		
		Result<List<UserRoleDAO.Data>> rsUR = new Result<List<UserRoleDAO.Data>>(null, 1, "test", new Object[0]);
		CachedUserRoleDAO userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		Mockito.doReturn(rsUR).when(userRoleDao).readByUser(Mockito.any(), Mockito.anyString());
		Mockito.doReturn(rs).when(userRoleDao).readUserInRole(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		
		Result<NsDAO.Data> retVal = question.mayUser(trans, "user", pdd, Access.read);
		assertTrue(retVal.status == 0);
		
		rs = new Result<List<NsDAO.Data>>(dataAL, 1, "test", new Object[0]);
		Mockito.doReturn(rs).when(nsDaoMock).read(trans, "ns");
		retVal = question.mayUser(trans, "user", pdd, Access.read);
		assertTrue(retVal.status == 21);
		
	}
	
	@Test
	public void testMayUser5() {
		testConstructor();
		NsDAO.Data ndd = new NsDAO.Data();
		ndd.name="name.name";
		PermDAO.Data pdd = new PermDAO.Data();
		pdd.ns="ns";
		pdd.instance = "role";
		
		List<NsDAO.Data> dataAL = new ArrayList<>();
		NsDAO.Data data = new NsDAO.Data();
		data.name = "name";
		dataAL.add(data);
		Result<List<NsDAO.Data>> rs = new Result<List<NsDAO.Data>>(dataAL, 0, "test", new Object[0]);
		CachedNSDAO nsDaoMock = Mockito.mock(CachedNSDAO.class);
		setField(question, nsDaoMock);
		Mockito.doReturn(rs).when(nsDaoMock).read(trans, "name");
		
		Result<List<UserRoleDAO.Data>> rsUR = new Result<List<UserRoleDAO.Data>>(null, 1, "test", new Object[0]);
		CachedUserRoleDAO userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		Mockito.doReturn(rsUR).when(userRoleDao).readByUser(Mockito.any(), Mockito.anyString());
		Mockito.doReturn(rsUR).when(userRoleDao).readUserInRole(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		
		Result<NsDAO.Data> retVal = question.mayUser(trans, "user", ndd, pdd, Access.read);
		assertTrue(retVal.status == 2);

		List<RoleDAO.Data> dataRoleAL = new ArrayList<>();
		RoleDAO.Data roleData = new RoleDAO.Data();
		roleData.name="name";
		roleData.perms = new HashSet<>();
		roleData.perms.add("test");
		roleData.perms=new HashSet<>();
		roleData.perms.add("name|access|:perm:null:role:null|read");
		dataRoleAL.add(roleData);
		CachedRoleDAO roleDao = Mockito.mock(CachedRoleDAO.class);
		setRoleDaoField(question, roleDao);
		Result<List<RoleDAO.Data>> rsRole = new Result<List<RoleDAO.Data>>(dataRoleAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsRole).when(roleDao).read(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		
		List<UserRoleDAO.Data> dataURAL = new ArrayList<>();
		UserRoleDAO.Data dataUR = new UserRoleDAO.Data();
		dataUR.rname = "name";
		dataUR.ns="ns";
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		dataUR.expires = calendar.getTime();
		dataURAL.add(dataUR);
		rsUR = new Result<List<UserRoleDAO.Data>>(dataURAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsUR).when(userRoleDao).readByUser(Mockito.any(), Mockito.anyString());
		retVal = question.mayUser(trans, "user", ndd, pdd, Access.read);
		assertTrue(retVal.status == 0);
	}
	
	@Test
	public void testMayUser6() {
		testConstructor();
		DelegateDAO.Data dd = new  DelegateDAO.Data();
		Mockito.doReturn("user@user").when(trans).user();
		Organization org = Mockito.mock(Organization.class);
		Mockito.doReturn(org).when(trans).org();
		
		Result<Void> retVal = question.mayUser(trans, dd, Access.create);
		
		List<NsDAO.Data> dataAL = new ArrayList<>();
		NsDAO.Data data = new NsDAO.Data();
		data.name = "name";
		dataAL.add(data);
		Result<List<NsDAO.Data>> rs = new Result<List<NsDAO.Data>>(dataAL, 0, "test", new Object[0]);
		CachedNSDAO nsDaoMock = Mockito.mock(CachedNSDAO.class);
		setField(question, nsDaoMock);
		Mockito.doReturn(rs).when(nsDaoMock).read(trans, "user");
		
		Result<List<UserRoleDAO.Data>> rsUR = new Result<List<UserRoleDAO.Data>>(null, 1, "test", new Object[0]);
		CachedUserRoleDAO userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		Mockito.doReturn(rsUR).when(userRoleDao).readByUser(Mockito.any(), Mockito.anyString());
		Mockito.doReturn(rsUR).when(userRoleDao).readUserInRole(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		
		retVal = question.mayUser(trans, dd, Access.read);
		assertTrue(retVal.status == 2);
		
		dd.user = "user@user";
		dd.delegate = "user@user";
		retVal = question.mayUser(trans, dd, Access.read);
		assertTrue(retVal.status == 0);
		
		retVal = question.mayUser(trans, dd, Access.create);
		assertTrue(retVal.status == 24);
		
		try {
			Mockito.doReturn(Mockito.mock(Identity.class)).when(org).getIdentity(trans, "user@user");
		} catch (OrganizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		retVal = question.mayUser(trans, dd, Access.create);
		assertTrue(retVal.status == 4);
		
		dd.user = "user";
		
		retVal = question.mayUser(trans, dd, Access.create);
		assertTrue(retVal.status == 24);	
		
		try {
			Mockito.doReturn(null).when(org).getIdentity(trans, "user@user");
			Mockito.doReturn(Mockito.mock(Identity.class)).when(org).getIdentity(trans, "user");
		} catch (OrganizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		retVal = question.mayUser(trans, dd, Access.create);
		assertTrue(retVal.status == 24);	

	}
	
	@Test
	public void testDoesUserCredMatch() {
		testConstructor();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).audit();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).trace();
		
		CachedCredDAO credDaoMock = Mockito.mock(CachedCredDAO.class);
		setCredDaoField(question, credDaoMock);
		Result<List<CredDAO.Data>> rsCd =  new Result<List<CredDAO.Data>>(null, 1, "test", new Object[0]);
		Mockito.doReturn(rsCd).when(question.credDAO()).readID(Mockito.any(), Mockito.anyString());
		
		byte[] creds = new byte[10];
		try {
			Result<Date> retVal = question.doesUserCredMatch(trans, "user", creds);
			assertTrue(retVal.status !=0);	
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		rsCd =  new Result<List<CredDAO.Data>>(null, 0, "test", new Object[0]);
		Mockito.doReturn(rsCd).when(credDaoMock).readID(Mockito.any(), Mockito.anyString());
		try {
			Result<Date> retVal = question.doesUserCredMatch(trans, "user", creds);
			assertTrue(retVal.status == 24);	
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<CredDAO.Data> cdDataAL = new ArrayList<>();
		CredDAO.Data cdData = new CredDAO.Data();
		cdData.id="id";
		cdData.expires = new Date();
		cdDataAL.add(cdData);
		rsCd =  new Result<List<CredDAO.Data>>(cdDataAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsCd).when(credDaoMock).readID(Mockito.any(), Mockito.anyString());
		try {
			Result<Date> retVal = question.doesUserCredMatch(trans, "user", creds);
			assertTrue(retVal.status == 1);	
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cdData.type=3;
		cdDataAL.add(cdData);
		rsCd =  new Result<List<CredDAO.Data>>(cdDataAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsCd).when(credDaoMock).readID(Mockito.any(), Mockito.anyString());
		try {
			Result<Date> retVal = question.doesUserCredMatch(trans, "user", creds);
			assertTrue(retVal.status == 1);	
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cdData.type=2;
		cdDataAL.add(cdData);
		rsCd =  new Result<List<CredDAO.Data>>(cdDataAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsCd).when(credDaoMock).readID(Mockito.any(), Mockito.anyString());
		try {
			Result<Date> retVal = question.doesUserCredMatch(trans, "user", creds);
			assertTrue(retVal.status == 1);	
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cdData.id="test";
		cdData.type=1;
		cdDataAL.add(cdData);
		rsCd =  new Result<List<CredDAO.Data>>(cdDataAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsCd).when(credDaoMock).readID(Mockito.any(), Mockito.anyString());
		try {
			Result<Date> retVal = question.doesUserCredMatch(trans, "user", creds);
			assertTrue(retVal.status == 1);	
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		cdData.expires = calendar.getTime();
		cdData.cred = ByteBuffer.wrap(new byte[]{1,34,5,3,25,0,2,5,3,4});
		cdDataAL.add(cdData);
		Question.specialLogOff(trans, "user");
		try {
			Result<Date> retVal = question.doesUserCredMatch(trans, "user", creds);
			assertTrue(retVal.status == 1);	
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cdData.expires = calendar.getTime();
		cdData.cred = ByteBuffer.wrap(new byte[]{1,34,5,3,25,0,2,5,3,4});
		cdDataAL.add(cdData);
		Question.specialLogOn(trans, "user");
		try {
			Result<Date> retVal = question.doesUserCredMatch(trans, "user", creds);
			assertTrue(retVal.status == 1);	
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cdData.other=1;
		cdData.type=2;
		cdData.expires = calendar.getTime();
		cdData.cred = ByteBuffer.wrap(new byte[]{1,34,5,3,25,0,2,5,3,4});
		cdDataAL.add(cdData);
		Question.specialLogOff(trans, "user");
		try {
			Result<Date> retVal = question.doesUserCredMatch(trans, "user", creds);
			assertTrue(retVal.status == 1);	
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cdData.other=1;
		cdData.type=2;
		cdData.expires = calendar.getTime();
		cdData.cred = ByteBuffer.wrap(new byte[]{1,34,5,3,25,0,2,5,3,4});
		cdDataAL.add(cdData);
		Question.specialLogOn(trans, "user");
		try {
			Result<Date> retVal = question.doesUserCredMatch(trans, "user", creds);
			assertTrue(retVal.status == 1);	
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		rsCd =  new Result<List<CredDAO.Data>>(null, 0, "test", new Object[0]);
		Mockito.doReturn(rsCd).when(credDaoMock).readID(Mockito.any(), Mockito.anyString());
		try {
			Result<Date> retVal = question.doesUserCredMatch(trans, "user", creds);
			assertTrue(retVal.status == 24);	
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testCheckLessThanDays() {
		testConstructor();
		CredDAO.Data credDataObj = new CredDAO.Data();
		credDataObj.expires = new Date();
		try {
			Method checkLessMtd = Question.class.getDeclaredMethod("checkLessThanDays", new Class[] {AuthzTrans.class, int.class, Date.class, CredDAO.Data.class});
			checkLessMtd.setAccessible(true);
			checkLessMtd.invoke(question, new Object[] {trans, 10, new Date(), credDataObj});
			
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DAY_OF_YEAR, 100000);
			credDataObj.expires = calendar.getTime();
			checkLessMtd.invoke(question, new Object[] {trans, 10, new Date(), credDataObj});
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUserCredSetup() {
		testConstructor();
		CredDAO.Data credDataObj = new CredDAO.Data();
		credDataObj.expires = new Date();
		credDataObj.type = 1;
		credDataObj.cred = ByteBuffer.wrap(new byte[]{1,34,5,3,25,0,2,5,3,4});
		
		Result<CredDAO.Data> retVal = question.userCredSetup(trans, credDataObj);
		assertTrue(retVal.status == 1);	
		
		credDataObj.type = -1;
		retVal = question.userCredSetup(trans, credDataObj);
		assertTrue(retVal.status == 0);	
	}

	@Test
	public void testUserCredCheck() {
		testConstructor();
		CredDAO.Data credDataObj = new CredDAO.Data();
		credDataObj.expires = new Date();
		credDataObj.type = 1;
		credDataObj.cred = ByteBuffer.wrap(new byte[]{1,34,5,3,25,0,2,5,3,4});
		
		Result<Boolean> retVal = question.userCredCheck(trans, credDataObj, new byte[]{1,34,5,3,25,0,2,5,3,4});
		assertTrue(retVal.status == 0);	
		
		credDataObj.type = 2;
		credDataObj.other=1;
		retVal = question.userCredCheck(trans, credDataObj, new byte[]{1,34,5,3,25,0,2,5,3,4});
		assertTrue(retVal.status == 0);	
		
		credDataObj.type = 3;
		credDataObj.other=1;
		retVal = question.userCredCheck(trans, credDataObj, new byte[]{1,34,5,3,25,0,2,5,3,4});
		assertTrue(retVal.status == 0);	
	}
	
	@Test
	public void testCanAddUser() {
		testConstructor();
		UserRoleDAO.Data urData = new UserRoleDAO.Data();
		List<ApprovalDAO.Data> aDataAL = new ArrayList<>();
		
		Result<Void> retVal = question.canAddUser(trans, urData, aDataAL);
		assertTrue(retVal.status == 0);	
		
		ApprovalDAO.Data dataObj = new ApprovalDAO.Data();
		dataObj.status = Question.REJECT;
		aDataAL.add(dataObj);
		retVal = question.canAddUser(trans, urData, aDataAL);
		assertTrue(retVal.status == 3);	
		
		aDataAL = new ArrayList<>();
		dataObj = new ApprovalDAO.Data();
		dataObj.status = Question.PENDING;
		aDataAL.add(dataObj);
		retVal = question.canAddUser(trans, urData, aDataAL);
		assertTrue(retVal.status == 8);	
	}
	
	@Test
	public void testClearCache() {
		testConstructor();
		
		Result<Void> retVal = question.clearCache(trans, "test");
		assertTrue(retVal.status == 4);	
		
		retVal = question.clearCache(trans, "all");
		assertTrue(retVal.status == 0);	

		retVal = question.clearCache(trans, "ns");
		assertTrue(retVal.status == 0);

		retVal = question.clearCache(trans, "perm");
		assertTrue(retVal.status == 0);

		retVal = question.clearCache(trans, "role");
		assertTrue(retVal.status == 0);

		retVal = question.clearCache(trans, "user_role");
		assertTrue(retVal.status == 0);

		retVal = question.clearCache(trans, "cred");
		assertTrue(retVal.status == 0);

		retVal = question.clearCache(trans, "x509");
		assertTrue(retVal.status == 0);
	}
	
	@Test
	public void testIsDelegated() {
		testConstructor();
		Map<String,Result<List<DelegateDAO.Data>>> rldd = new HashMap<>();
		DelegateDAO delegateDAO = Mockito.mock(DelegateDAO.class);
		setDelegateDaoField(question, delegateDAO);

		Result<List<DelegateDAO.Data>> rsDel =  new Result<List<DelegateDAO.Data>>(null, 1, "test", new Object[0]);
		Mockito.doReturn(rsDel).when(question.delegateDAO()).readByDelegate(Mockito.any(), Mockito.anyString());
		
		boolean retVal = question.isDelegated(trans, "user", "approver", rldd);
		assertFalse(retVal);

		rldd = new HashMap<>();
		List<DelegateDAO.Data> delDaoAL = new ArrayList<>();
		DelegateDAO.Data delData = new DelegateDAO.Data();
		delData.user = "user";
		delData.delegate = "delegate";
		delDaoAL.add(delData);
		rsDel =  new Result<List<DelegateDAO.Data>>(delDaoAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsDel).when(delegateDAO).readByDelegate(Mockito.any(), Mockito.anyString());
		
		retVal = question.isDelegated(trans, "user", "approver", rldd);
		assertFalse(retVal);
		
		rldd = new HashMap<>();
		delDaoAL = new ArrayList<>();
		delData = new DelegateDAO.Data();
		delData.user = "approver";
		delData.delegate = "user";
		delData.expires = new Date();
		delDaoAL.add(delData);
		rsDel =  new Result<List<DelegateDAO.Data>>(delDaoAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsDel).when(delegateDAO).readByDelegate(Mockito.any(), Mockito.anyString());
		
		retVal = question.isDelegated(trans, "user", "approver", rldd);
		assertFalse(retVal);
		
		rldd = new HashMap<>();
		delDaoAL = new ArrayList<>();
		delData = new DelegateDAO.Data();
		delData.user = "approver";
		delData.delegate = "user";
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		delData.expires = calendar.getTime();
		delDaoAL.add(delData);
		rsDel =  new Result<List<DelegateDAO.Data>>(delDaoAL, 0, "test", new Object[0]);
		Mockito.doReturn(rsDel).when(delegateDAO).readByDelegate(Mockito.any(), Mockito.anyString());
		
		retVal = question.isDelegated(trans, "user", "approver", rldd);
		assertTrue(retVal);
//		System.out.println(retVal);
	}
	
	@Test
	public void testLogEncryptTrace() {
		testConstructor();
		AuthzEnv env= Mockito.mock(AuthzEnv.class);
		Encryptor encryptor = Mockito.mock(Encryptor.class);
		Mockito.doReturn(env).when(trans).env();
		Mockito.doReturn(encryptor).when(env).encryptor();
		
		
		Question.logEncryptTrace(trans, "data");
	}
	
	@Test
	public void testCanMove() {
		testConstructor();
		
		assertTrue(question.canMove(NsType.APP));
		assertFalse(question.canMove(NsType.DOT));
	}
	
	@Test
	public void testIsAdmin() {
		testConstructor();
		List<UserRoleDAO.Data> dataAL = new ArrayList<>();
		UserRoleDAO.Data data = new UserRoleDAO.Data();
		data.rname = "name";
		data.expires = new Date();
		dataAL.add(data);
		Result<List<UserRoleDAO.Data>> rs = new Result<List<UserRoleDAO.Data>>(dataAL, 0, "test", new Object[0]);
		CachedUserRoleDAO userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		Mockito.doReturn(rs).when(userRoleDao).read(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		
		assertFalse(question.isAdmin(trans, "user", "ns"));
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		data.expires = calendar.getTime();
		dataAL.add(data);
		rs = new Result<List<UserRoleDAO.Data>>(dataAL, 0, "test", new Object[0]);
		Mockito.doReturn(rs).when(userRoleDao).read(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		
		assertTrue(question.isAdmin(trans, "user", "ns"));
		
		rs = new Result<List<UserRoleDAO.Data>>(null, 0, "test", new Object[0]);
		Mockito.doReturn(rs).when(userRoleDao).read(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		
		assertFalse(question.isAdmin(trans, "user", "ns"));
	}
	
	@Test
	public void testIsOwner() {
		testConstructor();
		List<UserRoleDAO.Data> dataAL = new ArrayList<>();
		UserRoleDAO.Data data = new UserRoleDAO.Data();
		data.rname = "name";
		data.expires = new Date();
		dataAL.add(data);
		Result<List<UserRoleDAO.Data>> rs = new Result<List<UserRoleDAO.Data>>(dataAL, 0, "test", new Object[0]);
		CachedUserRoleDAO userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		Mockito.doReturn(rs).when(userRoleDao).read(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		
		assertFalse(question.isOwner(trans, "user", "ns"));
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		data.expires = calendar.getTime();
		dataAL.add(data);
		rs = new Result<List<UserRoleDAO.Data>>(dataAL, 0, "test", new Object[0]);
		Mockito.doReturn(rs).when(userRoleDao).read(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		
		assertTrue(question.isOwner(trans, "user", "ns"));
		
		rs = new Result<List<UserRoleDAO.Data>>(null, 0, "test", new Object[0]);
		Mockito.doReturn(rs).when(userRoleDao).read(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		
		assertFalse(question.isOwner(trans, "user", "ns"));
	}
	
	@Test
	public void testCountOwner() {
		testConstructor();
		List<UserRoleDAO.Data> dataAL = new ArrayList<>();
		UserRoleDAO.Data data = new UserRoleDAO.Data();
		data.rname = "name";
		data.expires = new Date();
		dataAL.add(data);
		Result<List<UserRoleDAO.Data>> rs = new Result<List<UserRoleDAO.Data>>(dataAL, 0, "test", new Object[0]);
		CachedUserRoleDAO userRoleDao = Mockito.mock(CachedUserRoleDAO.class);
		setUserRoleDaoField(question, userRoleDao);
		Mockito.doReturn(rs).when(userRoleDao).readByRole(Mockito.any(), Mockito.anyString());
		
		assertTrue(question.countOwner(trans, "ns") == 0);
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		data.expires = calendar.getTime();
		dataAL.add(data);
		rs = new Result<List<UserRoleDAO.Data>>(dataAL, 0, "test", new Object[0]);
		Mockito.doReturn(rs).when(userRoleDao).readByRole(Mockito.any(), Mockito.anyString());
		
		assertTrue(question.countOwner(trans, "ns")==2);
		
		rs = new Result<List<UserRoleDAO.Data>>(null, 0, "test", new Object[0]);
		Mockito.doReturn(rs).when(userRoleDao).readByRole(Mockito.any(), Mockito.anyString());
		
		assertTrue(question.countOwner(trans, "ns")==0);
	}
	
	@Test
	public void testToUnique() {
		testConstructor();
		
		try {
			String retVal = Question.toUnique("name");
			assertTrue(retVal.contains("gogbgngf"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testFromUnique() {
		testConstructor();
		
		try {
			String retVal = Question.fromUnique("gogbgngf");
			assertTrue(retVal.contains("name"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
