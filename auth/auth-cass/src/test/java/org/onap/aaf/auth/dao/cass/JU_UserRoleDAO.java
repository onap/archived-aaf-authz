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

package org.onap.aaf.auth.dao.cass;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.AbsCassDAO;
import org.onap.aaf.auth.dao.AbsCassDAO.CRUD;
import org.onap.aaf.auth.dao.AbsCassDAO.PSInfo;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.dao.cass.UserRoleDAO.Data;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class JU_UserRoleDAO {

	@Mock
    AuthzTrans trans;
	@Mock
	Cluster cluster;
	@Mock
	Session session;
	
	@Before
	public void setUp() throws APIException, IOException {
		initMocks(this);
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).warn();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).info();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).init();
		Mockito.doReturn("100").when(trans).getProperty(Config.CADI_LATITUDE);
		Mockito.doReturn("100").when(trans).getProperty(Config.CADI_LONGITUDE);
		Mockito.doReturn(session).when(cluster).connect("test");
	}

	@Test
	public void testInit() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("UserRoleDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on UserRoleDAO", Env.SUB);
		try {
			new UserRoleDAO(trans, cluster, "test");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		daoObj.
	}
	@Test
	public void testReadByUser() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("UserRoleDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on UserRoleDAO", Env.SUB);
		UserRoleDAO daoObj = null;
		try {
			daoObj = new UserRoleDAO(trans, cluster, "test");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PSInfo psObj = Mockito.mock(PSInfo.class);
		setPsByStartAndTarget(daoObj, psObj, "psByUser");
		
		Result<List<UserRoleDAO.Data>>  rs1 = new Result<List<UserRoleDAO.Data>>(null,0,"test",new Object[0]);
		Mockito.doReturn(rs1).when(psObj).read(trans, "UserRoleDAO READ", new Object[]{"test"});
		
		daoObj.readByUser(trans, "test");
	}
	@Test
	public void testReadByRole() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("UserRoleDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on UserRoleDAO", Env.SUB);
		UserRoleDAO daoObj = null;
		try {
			daoObj = new UserRoleDAO(trans, cluster, "test");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PSInfo psObj = Mockito.mock(PSInfo.class);
		setPsByStartAndTarget(daoObj, psObj, "psByRole");
		
		Result<List<UserRoleDAO.Data>>  rs1 = new Result<List<UserRoleDAO.Data>>(null,0,"test",new Object[0]);
		Mockito.doReturn(rs1).when(psObj).read(trans, "UserRoleDAO READ", new Object[]{"test"});
		
		daoObj.readByRole(trans, "test");
	}
	@Test
	public void testReadByUserRole() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("UserRoleDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on UserRoleDAO", Env.SUB);
		UserRoleDAO daoObj = null;
		try {
			daoObj = new UserRoleDAO(trans, cluster, "test");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PSInfo psObj = Mockito.mock(PSInfo.class);
		setPsByStartAndTarget(daoObj, psObj, "psUserInRole");
		
		Result<List<UserRoleDAO.Data>>  rs1 = new Result<List<UserRoleDAO.Data>>(null,0,"test",new Object[0]);
		Mockito.doReturn(rs1).when(psObj).read(trans, "UserRoleDAO READ", new Object[]{"test"});
		
		daoObj.readByUserRole(trans, "test","test");
	}
	
	
	public void setPsByStartAndTarget(UserRoleDAO UserRoleDAOObj, PSInfo psInfoObj, String fieldName) {
		Field UserRoleDAOField;
		try {
			UserRoleDAOField = UserRoleDAO.class.getDeclaredField(fieldName);
			
			UserRoleDAOField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
//	        modifiersField.setInt(UserRoleDAOField, UserRoleDAOField.getModifiers() & ~Modifier.FINAL);
	        
	        UserRoleDAOField.set(UserRoleDAOObj, psInfoObj);
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
	public void testWasMOdified() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("UserRoleDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on UserRoleDAO", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("DELETE Future",Env.REMOTE);
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doNothing().when(tt).done();
		UserRoleDAO.Data data  = new UserRoleDAO.Data();
		PSInfo createPS = Mockito.mock(PSInfo.class);
		
		HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
		Result<ResultSet> rs1 = new Result<ResultSet>(null,0,"test",new Object[0]);
		Result<Void> rs2 = new Result<Void>(null,0,"test",new Object[0]);
		Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
		
		CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
		Mockito.doReturn(rs2).when(cacheInfoDAO).touch(Mockito.any(AuthzTrans.class),Mockito.anyString(), Mockito.anyVararg());
		
		UserRoleDAOImpl daoObj = null;
		try {
			daoObj = new UserRoleDAOImpl(trans, historyDAO, cacheInfoDAO, createPS );
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		daoObj.wasModified(trans, CRUD.create, data, new String[] {"test"});
		
		daoObj.wasModified(trans, CRUD.update, data, new String[] {});
		daoObj.wasModified(trans, CRUD.create, data, new String[] {null});
		daoObj.wasModified(trans, CRUD.create, data, new String[] {"test",null});
		daoObj.wasModified(trans, CRUD.update, data, new String[] {"test","test"});

		daoObj.wasModified(trans, CRUD.delete, data, new String[] {"test","test"});
		daoObj.wasModified(trans, CRUD.delete, data, new String[] {});
		
		rs2 = new Result<Void>(null,1,"test",new Object[0]);
		Mockito.doReturn(rs2).when(cacheInfoDAO).touch(Mockito.any(AuthzTrans.class),Mockito.anyString(), Mockito.anyVararg());
		daoObj.wasModified(trans, CRUD.read, data, new String[] {"test","test"});
		daoObj.wasModified(trans, CRUD.read, data, new String[] {});
		
		rs1 = new Result<ResultSet>(null,1,"test",new String[0]);
		Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
		daoObj.wasModified(trans, CRUD.delete, data, new String[] {"test","test"});
	}
	
	@Test
	public void testSecondConstructor() {
		HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
		CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);

		UserRoleDAO daoObj = new UserRoleDAO(trans, historyDAO, cacheInfoDAO);
		
	}

	@Test
	public void testFutureLoader(){
		Class<?> innerClass = null;
		Class<?>[] innerClassArr = UserRoleDAO.class.getDeclaredClasses();
		for(Class indCls:innerClassArr) {
			if(indCls.getName().contains("URLoader")) {
				innerClass = indCls;
				break;
			}
		}
		
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        
        try {
        	
			Object obj = constructor.newInstance(1);
			Method innnerClassMtd;
				
			UserRoleDAO.Data data  = new UserRoleDAO.Data();
			Row row = Mockito.mock(Row.class);
			ByteBuffer bbObj = ByteBuffer.allocateDirect(10);
			bbObj.limit(7);
			bbObj.put(0, new Byte("0"));
			bbObj.put(1, new Byte("1"));
			bbObj.put(2, new Byte("2"));
			Mockito.doReturn(bbObj).when(row).getBytesUnsafe(1);
			
			innnerClassMtd = innerClass.getMethod("load", new Class[] {UserRoleDAO.Data.class, Row.class});
			innnerClassMtd.invoke(obj, new Object[] {data, row});
			
			innnerClassMtd = innerClass.getDeclaredMethod("key", new Class[] {UserRoleDAO.Data.class, Integer.TYPE, Object[].class });
			innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test","test","test"} });
//			
			innnerClassMtd = innerClass.getDeclaredMethod("body", new Class[] {UserRoleDAO.Data.class, Integer.TYPE, Object[].class });
			innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test","test","test","test","test","test","test","test","test"} });
			
			DataOutputStream dos = new DataOutputStream(new FileOutputStream("JU_UserRoleDAOTest.java"));
			innnerClassMtd = innerClass.getDeclaredMethod("marshal", new Class[] {UserRoleDAO.Data.class, DataOutputStream.class });
			innnerClassMtd.invoke(obj, new Object[] {data, dos });

			DataInputStream dis = new DataInputStream(new FileInputStream("JU_UserRoleDAOTest.java"));
			innnerClassMtd = innerClass.getDeclaredMethod("unmarshal", new Class[] {UserRoleDAO.Data.class, DataInputStream.class });
			innnerClassMtd.invoke(obj, new Object[] {data, dis });
			
		} catch (InstantiationException e) {
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
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	@Test
	public void testData() {
		UserRoleDAO.Data data = new UserRoleDAO.Data();
		NsSplit nss = new NsSplit("test", "test");
		data = new UserRoleDAO.Data();
		
		data.toString();
		data.role("test", "test");
		assertTrue("test".equals(data.ns));
		
		RoleDAO.Data rdd = new RoleDAO.Data();
		rdd.ns="test";
		data.role(rdd);
		assertTrue("test".equals(data.ns));
		
		Question q = Mockito.mock( Question.class);
		Result<NsSplit> rs = new Result<NsSplit>(nss,0,"test",new Object[0]);
		Mockito.doReturn(rs).when(q).deriveNsSplit(trans, "test");
		
		data.role(trans, q, "test");
		
		rs = new Result<NsSplit>(nss,1,"test",new Object[0]);
		Mockito.doReturn(rs).when(q).deriveNsSplit(trans, "test");
		
		data.role(trans, q, "test");
	}
	
}

class UserRoleDAOImpl extends UserRoleDAO{
	public UserRoleDAOImpl(AuthzTrans trans, HistoryDAO historyDAO,CacheInfoDAO cacheDao, PSInfo readPS) throws APIException, IOException {
		super(trans, historyDAO, cacheDao);
		setPs(this, readPS, "createPS");
	}
	
	public UserRoleDAOImpl(AuthzTrans trans, HistoryDAO historyDAO,CacheInfoDAO cacheDao, PSInfo readPS, Session session  ) throws APIException, IOException {
		super(trans, historyDAO, cacheDao);
		setPs(this, readPS, "createPS");
		setSession(this, session);
	}
	

	public void setPs(UserRoleDAOImpl UserRoleDAOObj, PSInfo psInfoObj, String methodName) {
		Field UserRoleDAOField;
		try {
			UserRoleDAOField = CassDAOImpl.class.getDeclaredField(methodName);
			
			UserRoleDAOField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
//	        modifiersField.setInt(UserRoleDAOField, UserRoleDAOField.getModifiers() & ~Modifier.FINAL);
	        
	        UserRoleDAOField.set(UserRoleDAOObj, psInfoObj);
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
	
	public void setSession(UserRoleDAOImpl approvalDaoObj, Session session) {
		Field nsDaoField;
		try {
			nsDaoField = AbsCassDAO.class.getDeclaredField("session");
			
			nsDaoField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
//	        modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
	        
	        nsDaoField.set(approvalDaoObj, session);
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
