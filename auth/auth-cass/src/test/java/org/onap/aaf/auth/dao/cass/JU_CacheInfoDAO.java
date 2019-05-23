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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.AbsCassDAO;
import org.onap.aaf.auth.dao.AbsCassDAO.CRUD;
import org.onap.aaf.auth.dao.AbsCassDAO.PSInfo;
import org.onap.aaf.auth.dao.cass.CacheInfoDAO.Data;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.PreparedId;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.DriverException;

import io.netty.util.concurrent.Future;

public class JU_CacheInfoDAO {

	@Mock
    AuthzTrans trans;
	@Mock
	Cluster cluster;
	@Mock
	Session session;
	@Mock
	AuthzEnv env;
	@Mock
	LogTarget logTarget;
	
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
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on CacheInfoDAO", Env.SUB);
		Mockito.doNothing().when(tt).done();
		CacheInfoDAO.Data data  = new CacheInfoDAO.Data();
		PSInfo createPS = Mockito.mock(PSInfo.class);
		Result<ResultSet> rs = new Result<ResultSet>(null,0,"test",new String[0]);
		Mockito.doReturn(rs).when(createPS).exec(trans, "CacheInfoDAOImpl CREATE", data);
		
		CacheInfoDAOImpl daoObj=null;
		try {
			daoObj = new CacheInfoDAOImpl(trans, cluster, "test",data, createPS);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		data.id
		Result<Data> retVal = daoObj.create(trans, data);
		assertTrue(retVal.status == 0);
	}
	
	@Test
	public void testTouch() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO Touch segments test: 1", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO Touch segments test: 1,2", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on CacheInfoDAO", Env.SUB);
		Mockito.doNothing().when(tt).done();

		Mockito.doReturn(logTarget).when(env).debug();
		
		CacheInfoDAO.Data data  = new CacheInfoDAO.Data();
		PSInfo createPS = Mockito.mock(PSInfo.class);
		Result<ResultSet> rs = new Result<ResultSet>(null,0,"test",new String[0]);
		Mockito.doReturn(rs).when(createPS).exec(trans, "CacheInfoDAOImpl CREATE", data);
		
		CacheInfoDAOImpl daoObj=null;
		try {
			daoObj = new CacheInfoDAOImpl(trans, cluster, "test",data, createPS);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		data.id
		Result<Void> retVal = daoObj.touch(trans, "test", 1, 2);
		assertTrue(retVal.status == 0);
		Mockito.doThrow(DriverException.class).when(session).executeAsync(Mockito.anyString());
		daoObj.startUpdate(env, Mockito.mock(HMangr.class), Mockito.mock(SecuritySetter.class), "12.0.0.1", 8080);
		retVal = daoObj.touch(trans, "test", 1, 2);
		
		
		try {
			daoObj = new CacheInfoDAOImpl(trans, cluster, "test",data, createPS);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(retVal.status == Result.ERR_Backend);
		Mockito.doThrow(APIException.class).when(session).executeAsync(Mockito.anyString());
		retVal = daoObj.touch(trans, "test", 1, 2);
		try {
			daoObj = new CacheInfoDAOImpl(trans, cluster, "test",data, createPS);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(retVal.status == Result.ERR_Backend);
		Mockito.doThrow(IOException.class).when(session).executeAsync(Mockito.anyString());
		try {
			daoObj = new CacheInfoDAOImpl(trans, cluster, "test",data, createPS);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		retVal = daoObj.touch(trans, "test", 1, 2);
		assertTrue(retVal.status == Result.ERR_Backend);
	}
	
	@Test
	public void testCheck() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Check Table Timestamps", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on CacheInfoDAO", Env.SUB);
		Mockito.doNothing().when(tt).done();
		CacheInfoDAO.Data data  = new CacheInfoDAO.Data();
		PSInfo createPS = Mockito.mock(PSInfo.class);
		Result<ResultSet> rs = new Result<ResultSet>(null,0,"test",new String[0]);
		Mockito.doReturn(rs).when(createPS).exec(trans, "CacheInfoDAOImpl CREATE", data);
		
		CacheInfoDAOImpl daoObj=null;
		PreparedStatement ps = Mockito.mock(PreparedStatement.class);
		Mockito.doReturn(ps).when(session).prepare(Mockito.anyString());
		Mockito.doReturn(Mockito.mock(ColumnDefinitions.class)).when(ps).getVariables();
		Mockito.doReturn(Mockito.mock(PreparedId.class)).when(ps).getPreparedId();
		try {
			daoObj = new CacheInfoDAOImpl(trans, cluster, "test",data, createPS);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		data.id
		ResultSet resultSet = Mockito.mock(ResultSet.class);
		List<Row> rowList = new ArrayList<>();
		Mockito.doReturn(rowList).when(resultSet).all();
		Mockito.doReturn(resultSet).when(session).execute(Mockito.any(Statement.class));
		Result<Void> retVal = daoObj.check(trans);
		assertTrue(retVal.status == 0);
		
		Row row  = Mockito.mock(Row.class);
		Mockito.doReturn("test").when(row).getString(Mockito.anyInt());
		rowList.add(row);
		row  = Mockito.mock(Row.class);
		Mockito.doReturn("test").when(row).getString(Mockito.anyInt());
		Mockito.doReturn(100).when(row).getInt(Mockito.anyInt());
		rowList.add(row);
		retVal = daoObj.check(trans);
		assertTrue(retVal.status == 0);

		Mockito.doThrow(DriverException.class).when(session).execute(Mockito.any(Statement.class));
		retVal = daoObj.check(trans);
		assertTrue(retVal.status == Result.ERR_Backend);
		try {
			daoObj = new CacheInfoDAOImpl(trans, cluster, "test",data, createPS);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Mockito.doThrow(APIException.class).when(session).execute(Mockito.any(Statement.class));
		retVal = daoObj.check(trans);
		assertTrue(retVal.status == Result.ERR_Backend);
		try {
			daoObj = new CacheInfoDAOImpl(trans, cluster, "test",data, createPS);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Mockito.doThrow(IOException.class).when(session).execute(Mockito.any(Statement.class));
		retVal = daoObj.check(trans);
		assertTrue(retVal.status == Result.ERR_Backend);
	}
	@Test
	public void testStopUpdate() {
		
		CacheInfoDAO.stopUpdate();
		
	}


	@Test
	public void testGet() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO Touch segments test1: 1011", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on CacheInfoDAO", Env.SUB);
		Mockito.doNothing().when(tt).done();
		CacheInfoDAO.Data data  = new CacheInfoDAO.Data();
		PSInfo createPS = Mockito.mock(PSInfo.class);
		Result<ResultSet> rs = new Result<ResultSet>(null,0,"test",new String[0]);
		Mockito.doReturn(rs).when(createPS).exec(trans, "CacheInfoDAOImpl CREATE", data);
		
		CacheInfoDAOImpl daoObj=null;
		try {
			daoObj = new CacheInfoDAOImpl(trans, cluster, "test",data, createPS);
			Date retVal = daoObj.get(trans, "test", 1011);
//			assertTrue(retVal.status == 0);
			retVal = daoObj.get(trans, "test1", 1011);
		} catch (APIException | IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testWasMOdified() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on CacheInfoDAO", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("DELETE APPROVAL",Env.REMOTE);
		Mockito.doNothing().when(tt).done();
		CacheInfoDAO.Data data  = new CacheInfoDAO.Data("test",1);
		PSInfo createPS = Mockito.mock(PSInfo.class);
		
		HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
		Result<ResultSet> rs1 = new Result<ResultSet>(null,0,"test",new String[0]);
		Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
		
		CacheInfoDAOImpl daoObj=null;
		try {
			daoObj = new CacheInfoDAOImpl(trans, cluster, "test", createPS);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		daoObj.wasModified(trans, CRUD.create, data, new String[] {"test"});
		
		
	}
	
	@Test
	public void testInfoLoader(){
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO Touch segments test1: 1011", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on CacheInfoDAO", Env.SUB);
		Mockito.doNothing().when(tt).done();
		CacheInfoDAO.Data data1  = new CacheInfoDAO.Data();
		PSInfo createPS = Mockito.mock(PSInfo.class);
		Result<ResultSet> rs = new Result<ResultSet>(null,0,"test",new String[0]);
		Mockito.doReturn(rs).when(createPS).exec(trans, "CacheInfoDAOImpl CREATE", data1);

		CacheInfoDAOImpl daoObj=null;
		try {
			daoObj = new CacheInfoDAOImpl(trans, cluster, "test", createPS);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Class[] classList = CacheInfoDAO.class.getDeclaredClasses();
		Class<?> innerClass = null;
		for(Class indCls:classList) {
			if(indCls.getName().contains("InfoLoader")) {
				innerClass = indCls;
			}
		}
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        try {
			Object obj = constructor.newInstance(1);
			Method innnerClassMtd;
				
			CacheInfoDAO.Data data  = new CacheInfoDAO.Data();
			Row row = Mockito.mock(Row.class);
			innnerClassMtd = innerClass.getMethod("load", new Class[] {CacheInfoDAO.Data.class, Row.class});
			innnerClassMtd.invoke(obj, new Object[] {data, row});
			
			innnerClassMtd = innerClass.getDeclaredMethod("key", new Class[] {CacheInfoDAO.Data.class, Integer.TYPE, Object[].class });
			innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test"}});

			innnerClassMtd = innerClass.getDeclaredMethod("body", new Class[] {CacheInfoDAO.Data.class, Integer.TYPE, Object[].class });
			innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test"}});
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
	public void testCacheUpdate(){
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO Touch segments test1: 1011", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on CacheInfoDAO", Env.SUB);
		Mockito.doNothing().when(tt).done();
		CacheInfoDAO.Data data1  = new CacheInfoDAO.Data();
		PSInfo createPS = Mockito.mock(PSInfo.class);
		Result<ResultSet> rs = new Result<ResultSet>(null,0,"test",new String[0]);
		Mockito.doReturn(rs).when(createPS).exec(trans, "CacheInfoDAOImpl CREATE", data1);

		CacheInfoDAOImpl daoObj=null;
		try {
			daoObj = new CacheInfoDAOImpl(trans, cluster, "test", createPS);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Class[] classList = CacheInfoDAO.class.getDeclaredClasses();
		Class<?> innerClass = null;
		for(Class indCls:classList) {
			if(indCls.getName().contains("CacheUpdate")) {
				innerClass = indCls;
			}
		}
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        try {
			Object obj = constructor.newInstance(env,Mockito.mock(HMangr.class), Mockito.mock(SecuritySetter.class), "12.0.0.1", 8080);

        	Class<?> innerInnerClass = Class.forName("org.onap.aaf.auth.dao.cass.CacheInfoDAO$CacheUpdate$CacheClear");
            Constructor<?> innerConstructor = innerInnerClass.getDeclaredConstructors()[0];
            innerConstructor.setAccessible(true);
            Object innerClassObj = innerConstructor.newInstance(obj, trans);
        	
			Method innnerClassMtd;
				
			CacheInfoDAO.Data data  = new CacheInfoDAO.Data();
			Row row = Mockito.mock(Row.class);
			Rcli rcli = Mockito.mock(Rcli.class);
			URI uri = new URI("ftp","admin:admin@geeksforgeeks.org:1234","/path/data","tt","ttd");
			Mockito.doReturn(uri).when(rcli).getURI();
			Mockito.doReturn(Mockito.mock(org.onap.aaf.cadi.client.Future.class)).when(rcli).delete("/mgmt/cache/null/null", "application/Void+json;q=1.0;charset=utf-8;version=2.0,application/json;q=1.0;version=2.0,*/*;q=1.0");
			
			innnerClassMtd = innerInnerClass.getMethod("code", new Class[] {Rcli.class});
			innnerClassMtd.invoke(innerClassObj, new Object[] {rcli});
			
			org.onap.aaf.cadi.client.Future futureObj = Mockito.mock(org.onap.aaf.cadi.client.Future.class);
			Mockito.doReturn(futureObj).when(rcli).delete("/mgmt/cache/null/null", "application/Void+json;q=1.0;charset=utf-8;version=2.0,application/json;q=1.0;version=2.0,*/*;q=1.0");
			Mockito.doReturn(true).when(futureObj).get(0);
			innnerClassMtd.invoke(innerClassObj, new Object[] {rcli});
			
			uri = new URI("ftp","12.0.0.1:8080","/path/data","tt","ttd");
			Mockito.doReturn(uri).when(rcli).getURI();
			innnerClassMtd.invoke(innerClassObj, new Object[] {rcli});
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
		}  catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	@Test
	public void testIntHolder(){
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO Touch segments test1: 1011", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on CacheInfoDAO", Env.SUB);
		Mockito.doNothing().when(tt).done();
		CacheInfoDAO.Data data1  = new CacheInfoDAO.Data();
		PSInfo createPS = Mockito.mock(PSInfo.class);
		Result<ResultSet> rs = new Result<ResultSet>(null,0,"test",new String[0]);
		Mockito.doReturn(rs).when(createPS).exec(trans, "CacheInfoDAOImpl CREATE", data1);

		CacheInfoDAOImpl daoObj=null;
		try {
			daoObj = new CacheInfoDAOImpl(trans, cluster, "test", createPS);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Class[] classList = CacheInfoDAO.class.getDeclaredClasses();
		Class<?> innerClass = null;
		for(Class indCls:classList) {
			if(indCls.getName().contains("CacheUpdate")) {
				innerClass = indCls;
			}
		}
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        try {
			Object obj = constructor.newInstance(env,Mockito.mock(HMangr.class), Mockito.mock(SecuritySetter.class), "12.0.0.1", 8080);

        	Class<?> innerInnerClass = Class.forName("org.onap.aaf.auth.dao.cass.CacheInfoDAO$CacheUpdate$IntHolder");
            Constructor<?> innerConstructor = innerInnerClass.getDeclaredConstructors()[0];
            innerConstructor.setAccessible(true);
            int[] a = new int[10];
            Object innerClassObj = innerConstructor.newInstance(obj, a);
        	
			Method innnerClassMtd=null;
				
			CacheInfoDAO.Data data  = new CacheInfoDAO.Data();
			Row row = Mockito.mock(Row.class);
			Rcli rcli = Mockito.mock(Rcli.class);
			URI uri = new URI("ftp","admin:admin@geeksforgeeks.org:1234","/path/data","tt","ttd");
			Mockito.doReturn(uri).when(rcli).getURI();
			Mockito.doReturn(Mockito.mock(org.onap.aaf.cadi.client.Future.class)).when(rcli).delete("/mgmt/cache/null/null", "application/Void+json;q=1.0;charset=utf-8;version=2.0,application/json;q=1.0;version=2.0,*/*;q=1.0");
			
			Method[] allMtds = innerInnerClass.getDeclaredMethods();
			for(Method indMtd:allMtds) {
				if(indMtd.getName().contains("add")) {
					innnerClassMtd = indMtd;
				}
			}
			innnerClassMtd.invoke(innerClassObj, new Object[] {a});
			
			
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
		}  catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (APIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	//@Test
	public void testSecondConstructor() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("CacheInfoDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on CacheInfoDAO", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("DELETE APPROVAL",Env.REMOTE);
		Mockito.doNothing().when(tt).done();
		CacheInfoDAO.Data data  = new CacheInfoDAO.Data();
		AbsCassDAO absCassDAO = Mockito.mock(AbsCassDAO.class);

		try {
			CacheInfoDAO daoObj = new CacheInfoDAO(trans, absCassDAO);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class CacheInfoDAOImpl extends CacheInfoDAO{

	public CacheInfoDAOImpl(AuthzTrans trans, Cluster cluster, String keyspace,CacheInfoDAO.Data data,PSInfo createPS  ) throws APIException, IOException {
		super(trans, cluster, keyspace);
		this.createPS = createPS;
//		setPs(this, createPS, "psByUser");
//		setPs(this, createPS, "psByApprover");
//		setPs(this, createPS, "psByTicket");
//		setPs(this, createPS, "psByStatus");
//		setSession(this, Mockito.mock(Session.class));
	}
	
	public CacheInfoDAOImpl(AuthzTrans trans, Cluster cluster, String keyspace,PSInfo readPS  ) throws APIException, IOException {
		super(trans, cluster, keyspace);
		this.readPS = readPS;
	}
	

	public void setPs(CacheInfoDAOImpl CacheInfoDAOObj, PSInfo psInfoObj, String methodName) {
		Field nsDaoField;
		try {
			nsDaoField = CacheInfoDAO.class.getDeclaredField(methodName);
			
			nsDaoField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
//	        modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
	        
	        nsDaoField.set(CacheInfoDAOObj, psInfoObj);
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

	
	public void setSession(CacheInfoDAOImpl CacheInfoDAOObj, Session session) {
		Field nsDaoField;
		try {
			nsDaoField = AbsCassDAO.class.getDeclaredField("session");
			
			nsDaoField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
//	        modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
	        nsDaoField.set(CacheInfoDAOObj, session);
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
