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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.AbsCassDAO;
import org.onap.aaf.auth.dao.AbsCassDAO.CRUD;
import org.onap.aaf.auth.dao.AbsCassDAO.PSInfo;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.dao.cass.PermDAO.Data;
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

public class JU_PermDAO {

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
		Mockito.doReturn(tt).when(trans).start("PermDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on PermDAO", Env.SUB);
		try {
			Session session =  Mockito.mock(Session.class);
			PermDAO daoObj = new PermDAO(trans, cluster, "test");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		daoObj.
	}
	@Test
	public void testReadByStartAndTarget() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("PermDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on PermDAO", Env.SUB);
		PermDAO daoObj = null;
		try {
			Session session =  Mockito.mock(Session.class);
			daoObj = new PermDAO(trans, cluster, "test");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PSInfo psObj = Mockito.mock(PSInfo.class);
		setPsByStartAndTarget(daoObj, psObj, "psByType");
		
		Result<List<PermDAO.Data>>  rs1 = new Result<List<PermDAO.Data>>(null,0,"test",new String[0]);
		Mockito.doReturn(rs1).when(psObj).read(trans, "PermDAO READ", new Object[]{"test"});
		
		daoObj.readByType(trans, "test", "test");
	}
	@Test
	public void testReadChildren() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("PermDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on PermDAO", Env.SUB);
		PermDAO daoObj = null;
		try {
			Session session =  Mockito.mock(Session.class);
			daoObj = new PermDAO(trans, cluster, "test");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PSInfo psObj = Mockito.mock(PSInfo.class);
		setPsByStartAndTarget(daoObj, psObj, "psChildren");
		
		Result<List<PermDAO.Data>>  rs1 = new Result<List<PermDAO.Data>>(null,0,"test",new String[0]);
		Mockito.doReturn(rs1).when(psObj).read(trans, "PermDAO READ", new Object[]{"test"});
		
		daoObj.readChildren(trans, "test", "test");
	}
	@Test
	public void testReadNs() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("PermDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on PermDAO", Env.SUB);
		PermDAO daoObj = null;
		try {
			Session session =  Mockito.mock(Session.class);
			daoObj = new PermDAO(trans, cluster, "test");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PSInfo psObj = Mockito.mock(PSInfo.class);
		setPsByStartAndTarget(daoObj, psObj, "psNS");
		
		Result<List<PermDAO.Data>>  rs1 = new Result<List<PermDAO.Data>>(null,0,"test",new String[0]);
		Mockito.doReturn(rs1).when(psObj).read(trans, "PermDAO READ", new Object[]{"test"});
		
		daoObj.readNS(trans, "test");
	}
	@Test
	public void testAddRole() {
		PSInfo psObj = Mockito.mock(PSInfo.class);
		HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
		PermDAO.Data data = new PermDAO.Data();
		CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
		PermDAOImpl daoObj=null;
		try {
			daoObj = new PermDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, Mockito.mock(Session.class));
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("PermDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on PermDAO", Env.SUB);
		
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);

		Result<List<Data>>  rs1 = new Result<List<Data>>(null,0,"test",new String[0]);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "PermDAOImpl CREATE", data);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "PermDAOImpl UPDATE", data);
		Mockito.doReturn(rs1).when(psObj).read(trans, "PermDAOImpl READ", data);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "PermDAOImpl DELETE", data);
		Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
		
		Result<Void> rs2 = new Result<Void>(null,0,"test",new String[0]);
		Mockito.doReturn(rs2).when(cacheInfoDAO).touch(Mockito.any(AuthzTrans.class),Mockito.anyString(), Mockito.anyVararg());
		
		 rs1 = new Result<List<Data>>(null,1,"test",new String[0]);
		Mockito.doReturn(rs1).when(cacheInfoDAO).touch(Mockito.any(), Mockito.anyString(), Mockito.anyInt());
		Mockito.doReturn("test user").when(trans).user();
		Field cbField;
		try {
			cbField = CassAccess.class.getDeclaredField("cb");
			cbField.setAccessible(true);
			cbField.set(daoObj, null);
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
		
		PermDAO.Data perm = new PermDAO.Data();
		Result<Void> retVal = daoObj.addRole(trans, perm, "test");
		assertTrue(retVal.status == 9);
		
		Field owningField;
		try {
			owningField = AbsCassDAO.class.getDeclaredField("owningDAO");
			owningField.setAccessible(true);
			owningField.set(daoObj, null);
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
		retVal = daoObj.addRole(trans, perm, "test");
		assertTrue(retVal.status == 0);
	}
	
	@Test
	public void testDelRole() {
		PSInfo psObj = Mockito.mock(PSInfo.class);
		HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
		PermDAO.Data data = new PermDAO.Data();
		CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
		PermDAOImpl daoObj=null;
		try {
			daoObj = new PermDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, Mockito.mock(Session.class));
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("PermDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on PermDAO", Env.SUB);
		
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);

		Result<List<Data>>  rs1 = new Result<List<Data>>(null,0,"test",new String[0]);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "PermDAOImpl CREATE", data);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "PermDAOImpl UPDATE", data);
		Mockito.doReturn(rs1).when(psObj).read(trans, "PermDAOImpl READ", data);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "PermDAOImpl DELETE", data);
		Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
		
		Result<Void> rs2 = new Result<Void>(null,0,"test",new String[0]);
		Mockito.doReturn(rs2).when(cacheInfoDAO).touch(Mockito.any(AuthzTrans.class),Mockito.anyString(), Mockito.anyVararg());
		
		 rs1 = new Result<List<Data>>(null,1,"test",new String[0]);
		Mockito.doReturn(rs1).when(cacheInfoDAO).touch(Mockito.any(), Mockito.anyString(), Mockito.anyInt());
		Mockito.doReturn("test user").when(trans).user();
		
		PermDAO.Data perm = new PermDAO.Data();
		Field cbField;
		try {
			cbField = CassAccess.class.getDeclaredField("cb");
			cbField.setAccessible(true);
			cbField.set(daoObj, null);
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
		
		Result<Void> retVal = daoObj.delRole(trans, perm, "test");
		assertTrue(retVal.status == 9);
		
		Field owningDaoField;
		try {
			owningDaoField = AbsCassDAO.class.getDeclaredField("owningDAO");
			owningDaoField.setAccessible(true);
			owningDaoField.set(daoObj, null);
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
		retVal = daoObj.delRole(trans, perm, "test");
		assertTrue(retVal.status == 0);
	}
	
	@Test
	public void testAddDescription() {
		PSInfo psObj = Mockito.mock(PSInfo.class);
		HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
		PermDAO.Data data = new PermDAO.Data();
		CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
		PermDAOImpl daoObj=null;
		try {
			daoObj = new PermDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, Mockito.mock(Session.class));
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("PermDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on PermDAO", Env.SUB);
		
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);

		Result<List<Data>>  rs1 = new Result<List<Data>>(null,0,"test",new String[0]);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "PermDAOImpl CREATE", data);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "PermDAOImpl UPDATE", data);
		Mockito.doReturn(rs1).when(psObj).read(trans, "PermDAOImpl READ", data);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "PermDAOImpl DELETE", data);
		Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
		
		Result<Void> rs2 = new Result<Void>(null,1,"test",new String[0]);
		Mockito.doReturn(rs2).when(cacheInfoDAO).touch(Mockito.any(AuthzTrans.class),Mockito.anyString(), Mockito.anyVararg());
		
		 rs1 = new Result<List<Data>>(null,1,"test",new String[0]);
		Mockito.doReturn(rs1).when(cacheInfoDAO).touch(Mockito.any(), Mockito.anyString(), Mockito.anyInt());
		Mockito.doReturn("test user").when(trans).user();
		
		PermDAO.Data perm = new PermDAO.Data();
		Field cbField;
		try {
			cbField = CassAccess.class.getDeclaredField("cb");
			cbField.setAccessible(true);
			cbField.set(daoObj, null);
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
		
		Result<Void> retVal = daoObj.addDescription(trans, "test", "test", "test", "test", "test");
		assertTrue(retVal.status == 9);
		
		Field owningDaoField;
		try {
			owningDaoField = AbsCassDAO.class.getDeclaredField("owningDAO");
			owningDaoField.setAccessible(true);
			owningDaoField.set(daoObj, null);
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
		retVal = daoObj.addDescription(trans, "test", "test", "test", "test", "test");
		assertTrue(retVal.status == 0);
	}
	
	public void setPsByStartAndTarget(PermDAO PermDAOObj, PSInfo psInfoObj, String fieldName) {
		Field PermDAOField;
		try {
			PermDAOField = PermDAO.class.getDeclaredField(fieldName);
			
			PermDAOField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
//	        modifiersField.setInt(PermDAOField, PermDAOField.getModifiers() & ~Modifier.FINAL);
	        
	        PermDAOField.set(PermDAOObj, psInfoObj);
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
		Mockito.doReturn(tt).when(trans).start("PermDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on PermDAO", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("DELETE Future",Env.REMOTE);
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doNothing().when(tt).done();
		PermDAO.Data data  = new PermDAO.Data();
		PSInfo createPS = Mockito.mock(PSInfo.class);
		
		HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
		Result<ResultSet> rs1 = new Result<ResultSet>(null,0,"test",new String[0]);
		Result<Void> rs2 = new Result<Void>(null,0,"test",new String[0]);
		Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
		
		CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
		Mockito.doReturn(rs2).when(cacheInfoDAO).touch(Mockito.any(AuthzTrans.class),Mockito.anyString(), Mockito.anyVararg());
		
		PermDAOImpl daoObj = null;
		try {
			daoObj = new PermDAOImpl(trans, historyDAO, cacheInfoDAO, createPS );
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		daoObj.wasModified(trans, CRUD.create, data, new String[] {"test"});
		
		daoObj.wasModified(trans, CRUD.create, data, new String[] {});
		daoObj.wasModified(trans, CRUD.create, data, new String[] {null});
		daoObj.wasModified(trans, CRUD.create, data, new String[] {"test",null});
		daoObj.wasModified(trans, CRUD.create, data, new String[] {"test","test"});
		
		rs1 = new Result<ResultSet>(null,1,"test",new String[0]);
		Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
		daoObj.wasModified(trans, CRUD.delete, data, new String[] {"test","test"});
	}
	
	@Test
	public void testSecondConstructor() {
		HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
		CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);

		PermDAO daoObj = new PermDAO(trans, historyDAO, cacheInfoDAO);
		
	}

	@Test
	public void testFutureLoader(){
		Class<?> innerClass = null;
		Class<?>[] innerClassArr = PermDAO.class.getDeclaredClasses();
		for(Class indCls:innerClassArr) {
			if(indCls.getName().contains("PermLoader")) {
				innerClass = indCls;
				break;
			}
		}
		
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        
        try {
        	
			Object obj = constructor.newInstance(1);
			Method innnerClassMtd;
				
			PermDAO.Data data  = new PermDAO.Data();
			Row row = Mockito.mock(Row.class);
			ByteBuffer bbObj = ByteBuffer.allocateDirect(10);
			bbObj.limit(7);
			bbObj.put(0, new Byte("0"));
			bbObj.put(1, new Byte("1"));
			bbObj.put(2, new Byte("2"));
			Mockito.doReturn(bbObj).when(row).getBytesUnsafe(1);
			
			innnerClassMtd = innerClass.getMethod("load", new Class[] {PermDAO.Data.class, Row.class});
			innnerClassMtd.invoke(obj, new Object[] {data, row});
			
			innnerClassMtd = innerClass.getDeclaredMethod("key", new Class[] {PermDAO.Data.class, Integer.TYPE, Object[].class });
			innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test","test","test"} });
//			
			innnerClassMtd = innerClass.getDeclaredMethod("body", new Class[] {PermDAO.Data.class, Integer.TYPE, Object[].class });
			innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test","test","test","test","test","test","test","test","test"} });
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			innnerClassMtd = innerClass.getDeclaredMethod("marshal", new Class[] {PermDAO.Data.class, DataOutputStream.class });
			innnerClassMtd.invoke(obj, new Object[] {data, dos });

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			DataInputStream dis = new DataInputStream(bais);
			innnerClassMtd = innerClass.getDeclaredMethod("unmarshal", new Class[] {PermDAO.Data.class, DataInputStream.class });
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
		} 
	}
	
	@Test
	public void testData() {
		PermDAO.Data data = new PermDAO.Data();
		NsSplit nss = new NsSplit("test", "test");
		data = new PermDAO.Data(nss, "test", "test");
		
		data.toString();
		
		assertTrue("test.test|test|test".equalsIgnoreCase(data.fullPerm()));
		
		Question q = Mockito.mock( Question.class);
		
		Result<NsSplit> rs = new Result<NsSplit>(nss,0,"test",new Object[0]);
		Mockito.doReturn(rs).when(q).deriveNsSplit(trans, "test");
		Result<Data> retVal= PermDAO.Data.decode(trans, q, "test|||");
		assertTrue(retVal.status==0);
		Result<String[]> retVal1= PermDAO.Data.decodeToArray(trans, q, "test|||");
		assertTrue(retVal.status==0);
		retVal= PermDAO.Data.decode(trans, q, "test||");
		retVal1= PermDAO.Data.decodeToArray(trans, q, "test||");
		assertTrue(retVal.status==0);
		
		rs = new Result<NsSplit>(nss,1,"test",new Object[0]);
		Mockito.doReturn(rs).when(q).deriveNsSplit(trans, "test");
		retVal= PermDAO.Data.decode(trans, q, "test||");
		retVal1= PermDAO.Data.decodeToArray(trans, q, "test||");
		assertTrue(retVal.status==1);

		retVal= PermDAO.Data.decode(trans, q, "test|");
		retVal1= PermDAO.Data.decodeToArray(trans, q, "test|");
		assertTrue(retVal.status==4);
		
		NsDAO.Data ns = new NsDAO.Data();
		ns.name="test";
		PermDAO.Data.create(ns, "test");

		PermDAO.Data.create(trans,q, "test");
		rs = new Result<NsSplit>(nss,0,"test",new Object[0]);
		Mockito.doReturn(rs).when(q).deriveNsSplit(trans, "test");
		PermDAO.Data.create(trans,q, "test|test|test|test");
	}
	
}

class PermDAOImpl extends PermDAO{
	public PermDAOImpl(AuthzTrans trans, HistoryDAO historyDAO,CacheInfoDAO cacheDao, PSInfo readPS) throws APIException, IOException {
		super(trans, historyDAO, cacheDao);
		setPs(this, readPS, "createPS");
	}
	
	public PermDAOImpl(AuthzTrans trans, HistoryDAO historyDAO,CacheInfoDAO cacheDao, PSInfo readPS, Session session  ) throws APIException, IOException {
		super(trans, historyDAO, cacheDao);
		setPs(this, readPS, "createPS");
		setSession(this, session);
	}
	

	public void setPs(PermDAOImpl PermDAOObj, PSInfo psInfoObj, String methodName) {
		Field PermDAOField;
		try {
			PermDAOField = CassDAOImpl.class.getDeclaredField(methodName);
			
			PermDAOField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
//	        modifiersField.setInt(PermDAOField, PermDAOField.getModifiers() & ~Modifier.FINAL);
	        
	        PermDAOField.set(PermDAOObj, psInfoObj);
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
	
	public void setSession(PermDAOImpl approvalDaoObj, Session session) {
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
