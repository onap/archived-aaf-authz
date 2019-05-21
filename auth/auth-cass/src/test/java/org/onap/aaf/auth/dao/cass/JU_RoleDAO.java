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
import org.onap.aaf.auth.dao.cass.RoleDAO.Data;
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

public class JU_RoleDAO {

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
		Mockito.doReturn(tt).when(trans).start("RoleDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on RoleDAO", Env.SUB);
		try {
			new RoleDAO(trans, cluster, "test");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		daoObj.
	}
	@Test
	public void testReadByStartAndTarget() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("RoleDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on RoleDAO", Env.SUB);
		RoleDAO daoObj = null;
		try {
			daoObj = new RoleDAO(trans, cluster, "test");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PSInfo psObj = Mockito.mock(PSInfo.class);
		setPsByStartAndTarget(daoObj, psObj, "psName");
		
		Result<List<RoleDAO.Data>>  rs1 = new Result<List<RoleDAO.Data>>(null,0,"test",new Object[0]);
		Mockito.doReturn(rs1).when(psObj).read(trans, "RoleDAO READ", new Object[]{"test"});
		
		daoObj.readName(trans, "test");
	}
	@Test
	public void testReadChildren() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("RoleDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on RoleDAO", Env.SUB);
		RoleDAO daoObj = null;
		try {
			daoObj = new RoleDAO(trans, cluster, "test");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PSInfo psObj = Mockito.mock(PSInfo.class);
		setPsByStartAndTarget(daoObj, psObj, "psChildren");
		
		Result<List<RoleDAO.Data>>  rs1 = new Result<List<RoleDAO.Data>>(null,0,"test",new Object[0]);
		Mockito.doReturn(rs1).when(psObj).read(trans, "RoleDAO READ", new Object[]{"test"});
		
		daoObj.readChildren(trans, "test", "test");
		
		daoObj.readChildren(trans, "test", "*");
		daoObj.readChildren(trans, "test", "");
	}
	@Test
	public void testReadNs() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("RoleDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on RoleDAO", Env.SUB);
		RoleDAO daoObj = null;
		try {
			daoObj = new RoleDAO(trans, cluster, "test");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PSInfo psObj = Mockito.mock(PSInfo.class);
		setPsByStartAndTarget(daoObj, psObj, "psNS");
		
		Result<List<RoleDAO.Data>>  rs1 = new Result<List<RoleDAO.Data>>(null,0,"test",new Object[0]);
		Mockito.doReturn(rs1).when(psObj).read(trans, "RoleDAO READ", new Object[]{"test"});
		
		daoObj.readNS(trans, "test");
	}
	@Test
	public void testAddRole() {
		PSInfo psObj = Mockito.mock(PSInfo.class);
		HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
		RoleDAO.Data data = new RoleDAO.Data();
		CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
		RoleDAOImpl daoObj=null;
		try {
			daoObj = new RoleDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, Mockito.mock(Session.class));
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("RoleDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on RoleDAO", Env.SUB);
		
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);

		Result<List<Data>>  rs1 = new Result<List<Data>>(null,0,"test",new Object[0]);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "RoleDAOImpl CREATE", data);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "RoleDAOImpl UPDATE", data);
		Mockito.doReturn(rs1).when(psObj).read(trans, "RoleDAOImpl READ", data);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "RoleDAOImpl DELETE", data);
		Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
		
		Result<Void> rs2 = new Result<Void>(null,0,"test",new Object[0]);
		Mockito.doReturn(rs2).when(cacheInfoDAO).touch(Mockito.any(AuthzTrans.class),Mockito.anyString(), Mockito.anyVararg());
		
		 rs1 = new Result<List<Data>>(null,1,"test",new Object[0]);
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
		RoleDAO.Data role = new RoleDAO.Data();
		Result<Void> retVal = daoObj.addPerm(trans, role, perm);
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
		retVal = daoObj.addPerm(trans, role, perm);
		assertTrue(retVal.status == 0);
	}
	
	@Test
	public void testDelRole() {
		PSInfo psObj = Mockito.mock(PSInfo.class);
		HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
		RoleDAO.Data data = new RoleDAO.Data();
		CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
		RoleDAOImpl daoObj=null;
		try {
			daoObj = new RoleDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, Mockito.mock(Session.class));
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("RoleDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on RoleDAO", Env.SUB);
		
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);

		Result<List<Data>>  rs1 = new Result<List<Data>>(null,0,"test",new Object[0]);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "RoleDAOImpl CREATE", data);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "RoleDAOImpl UPDATE", data);
		Mockito.doReturn(rs1).when(psObj).read(trans, "RoleDAOImpl READ", data);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "RoleDAOImpl DELETE", data);
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
		RoleDAO.Data role = new RoleDAO.Data();
		Result<Void> retVal = daoObj.delPerm(trans, role, perm);
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
		retVal = daoObj.delPerm(trans,role, perm);
		assertTrue(retVal.status == 0);
	}
	
	@Test
	public void testAddDescription() {
		PSInfo psObj = Mockito.mock(PSInfo.class);
		HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
		RoleDAO.Data data = new RoleDAO.Data();
		CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
		RoleDAOImpl daoObj=null;
		try {
			daoObj = new RoleDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, Mockito.mock(Session.class));
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("RoleDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on RoleDAO", Env.SUB);
		
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);

		Result<List<Data>>  rs1 = new Result<List<Data>>(null,0,"test",new String[0]);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "RoleDAOImpl CREATE", data);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "RoleDAOImpl UPDATE", data);
		Mockito.doReturn(rs1).when(psObj).read(trans, "RoleDAOImpl READ", data);
		Mockito.doReturn(rs1).when(psObj).exec(trans, "RoleDAOImpl DELETE", data);
		Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
		
		Result<Void> rs2 = new Result<Void>(null,1,"test",new String[0]);
		Mockito.doReturn(rs2).when(cacheInfoDAO).touch(Mockito.any(AuthzTrans.class),Mockito.anyString(), Mockito.anyVararg());
		
		 rs1 = new Result<List<Data>>(null,1,"test",new String[0]);
		Mockito.doReturn(rs1).when(cacheInfoDAO).touch(Mockito.any(), Mockito.anyString(), Mockito.anyInt());
		Mockito.doReturn("test user").when(trans).user();
		
		RoleDAO.Data perm = new RoleDAO.Data();
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
		
		Result<Void> retVal = daoObj.addDescription(trans, "test", "test", "test");
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
		retVal = daoObj.addDescription(trans, "test", "test", "test");
		assertTrue(retVal.status == 0);
	}
	
	public void setPsByStartAndTarget(RoleDAO RoleDAOObj, PSInfo psInfoObj, String fieldName) {
		Field RoleDAOField;
		try {
			RoleDAOField = RoleDAO.class.getDeclaredField(fieldName);
			
			RoleDAOField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
//	        modifiersField.setInt(RoleDAOField, RoleDAOField.getModifiers() & ~Modifier.FINAL);
	        
	        RoleDAOField.set(RoleDAOObj, psInfoObj);
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
		Mockito.doReturn(tt).when(trans).start("RoleDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on RoleDAO", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("DELETE Future",Env.REMOTE);
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doNothing().when(tt).done();
		RoleDAO.Data data  = new RoleDAO.Data();
		PSInfo createPS = Mockito.mock(PSInfo.class);
		
		HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
		Result<ResultSet> rs1 = new Result<ResultSet>(null,0,"test",new String[0]);
		Result<Void> rs2 = new Result<Void>(null,0,"test",new String[0]);
		Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
		
		CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
		Mockito.doReturn(rs2).when(cacheInfoDAO).touch(Mockito.any(AuthzTrans.class),Mockito.anyString(), Mockito.anyVararg());
		
		RoleDAOImpl daoObj = null;
		try {
			daoObj = new RoleDAOImpl(trans, historyDAO, cacheInfoDAO, createPS );
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

		RoleDAO daoObj = new RoleDAO(trans, historyDAO, cacheInfoDAO);
		
	}

	@Test
	public void testFutureLoader(){
		Class<?> innerClass = null;
		Class<?>[] innerClassArr = RoleDAO.class.getDeclaredClasses();
		for(Class indCls:innerClassArr) {
			if(indCls.getName().contains("RoleLoader")) {
				innerClass = indCls;
				break;
			}
		}
		
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        
        try {
        	
			Object obj = constructor.newInstance(1);
			Method innnerClassMtd;
				
			RoleDAO.Data data  = new RoleDAO.Data();
			Row row = Mockito.mock(Row.class);
			ByteBuffer bbObj = ByteBuffer.allocateDirect(10);
			bbObj.limit(7);
			bbObj.put(0, new Byte("0"));
			bbObj.put(1, new Byte("1"));
			bbObj.put(2, new Byte("2"));
			Mockito.doReturn(bbObj).when(row).getBytesUnsafe(1);
			
			innnerClassMtd = innerClass.getMethod("load", new Class[] {RoleDAO.Data.class, Row.class});
			innnerClassMtd.invoke(obj, new Object[] {data, row});
			
			innnerClassMtd = innerClass.getDeclaredMethod("key", new Class[] {RoleDAO.Data.class, Integer.TYPE, Object[].class });
			innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test","test","test"} });
//			
			innnerClassMtd = innerClass.getDeclaredMethod("body", new Class[] {RoleDAO.Data.class, Integer.TYPE, Object[].class });
			innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test","test","test","test","test","test","test","test","test"} });
			
			DataOutputStream dos = new DataOutputStream(new FileOutputStream("JU_RoleDAOTest.java"));
			innnerClassMtd = innerClass.getDeclaredMethod("marshal", new Class[] {RoleDAO.Data.class, DataOutputStream.class });
			innnerClassMtd.invoke(obj, new Object[] {data, dos });

			DataInputStream dis = new DataInputStream(new FileInputStream("JU_RoleDAOTest.java"));
			innnerClassMtd = innerClass.getDeclaredMethod("unmarshal", new Class[] {RoleDAO.Data.class, DataInputStream.class });
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
		RoleDAO.Data data = new RoleDAO.Data();
		NsSplit nss = new NsSplit("test", "test");
		data = new RoleDAO.Data();
		
		data.toString();
		
		
		Question q = Mockito.mock( Question.class);
		
		Result<NsSplit> rs = new Result<NsSplit>(nss,0,"test",new Object[0]);
		Mockito.doReturn(rs).when(q).deriveNsSplit(trans, "test");
		Result<Data> retVal= RoleDAO.Data.decode(trans, q, "test|||");
		assertTrue(retVal.status==0);
		Result<String[]> retVal1= RoleDAO.Data.decodeToArray(trans, q, "test|||");
		assertTrue(retVal.status==0);
		retVal= RoleDAO.Data.decode(trans, q, "test");
		retVal1= RoleDAO.Data.decodeToArray(trans, q, "test");
		assertTrue(retVal.status==0);
		
		rs = new Result<NsSplit>(nss,1,"test",new Object[0]);
		Mockito.doReturn(rs).when(q).deriveNsSplit(trans, "test");
		retVal= RoleDAO.Data.decode(trans, q, "test");
		retVal1= RoleDAO.Data.decodeToArray(trans, q, "test");
		assertTrue(retVal.status==1);

		retVal= RoleDAO.Data.decode(trans, q, "test");
		retVal1= RoleDAO.Data.decodeToArray(trans, q, "test");
		assertTrue(retVal.status==1);
		
		NsDAO.Data ns = new NsDAO.Data();
		ns.name="test";
		RoleDAO.Data.create(ns, "test");

		UserRoleDAO.Data urdd = new UserRoleDAO.Data();
		urdd.ns="test";
		RoleDAO.Data dd=RoleDAO.Data.decode(urdd);
		assertTrue("test".equals(dd.ns));
		
		assertTrue(data.encode().contains("null"));
		
		data.perms = null;
		data.perms(true);

		data.perms = new HashSet<>();
		data.perms(true);

		data.perms(false);
		data.perms = new TreeSet<>();
		data.perms(true);
	}
	
}

class RoleDAOImpl extends RoleDAO{
	public RoleDAOImpl(AuthzTrans trans, HistoryDAO historyDAO,CacheInfoDAO cacheDao, PSInfo readPS) throws APIException, IOException {
		super(trans, historyDAO, cacheDao);
		setPs(this, readPS, "createPS");
	}
	
	public RoleDAOImpl(AuthzTrans trans, HistoryDAO historyDAO,CacheInfoDAO cacheDao, PSInfo readPS, Session session  ) throws APIException, IOException {
		super(trans, historyDAO, cacheDao);
		setPs(this, readPS, "createPS");
		setSession(this, session);
	}
	

	public void setPs(RoleDAOImpl RoleDAOObj, PSInfo psInfoObj, String methodName) {
		Field RoleDAOField;
		try {
			RoleDAOField = CassDAOImpl.class.getDeclaredField(methodName);
			
			RoleDAOField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
//	        modifiersField.setInt(RoleDAOField, RoleDAOField.getModifiers() & ~Modifier.FINAL);
	        
	        RoleDAOField.set(RoleDAOObj, psInfoObj);
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
	
	public void setSession(RoleDAOImpl approvalDaoObj, Session session) {
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
