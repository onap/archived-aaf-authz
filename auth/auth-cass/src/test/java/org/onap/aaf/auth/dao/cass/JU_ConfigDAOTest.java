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

import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
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
import org.onap.aaf.auth.dao.cass.ConfigDAO.Data;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Decryptor;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class JU_ConfigDAOTest {

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
		Mockito.doReturn(tt).when(trans).start("ConfigDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on ConfigDAO", Env.SUB);
		Mockito.doNothing().when(tt).done();
		ConfigDAO.Data data  = new ConfigDAO.Data();
		PSInfo createPS = Mockito.mock(PSInfo.class);
		Result<ResultSet> rs = new Result<ResultSet>(null,0,"test",new String[0]);
		Mockito.doReturn(rs).when(createPS).exec(trans, "ConfigDAOImpl CREATE", data);
		
		ConfigDAO daoObj=null;
		try {
			daoObj = new ConfigDAO(trans, cluster, "test");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void testConfigLoader(){
		
		Class<?> innerClass = ConfigDAO.class.getDeclaredClasses()[0];
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        try {
			Object obj = constructor.newInstance(1);
			Method innnerClassMtd;
				
			ConfigDAO.Data data  = new ConfigDAO.Data();
			Row row = Mockito.mock(Row.class);
			ByteBuffer bbObj = ByteBuffer.allocateDirect(10);
			bbObj.limit(7);
			bbObj.put(0, new Byte("0"));
			bbObj.put(1, new Byte("1"));
			bbObj.put(2, new Byte("2"));
			Mockito.doReturn(bbObj).when(row).getBytesUnsafe(1);
			
			innnerClassMtd = innerClass.getMethod("load", new Class[] {ConfigDAO.Data.class, Row.class});
			innnerClassMtd.invoke(obj, new Object[] {data, row});
			
			innnerClassMtd = innerClass.getDeclaredMethod("key", new Class[] {ConfigDAO.Data.class, Integer.TYPE, Object[].class });
			innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test"} });
//			
			innnerClassMtd = innerClass.getDeclaredMethod("body", new Class[] {ConfigDAO.Data.class, Integer.TYPE, Object[].class });
			innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test","test","test","test","test","test","test","test","test"} });
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			innnerClassMtd = innerClass.getDeclaredMethod("marshal", new Class[] {ConfigDAO.Data.class, DataOutputStream.class });
			innnerClassMtd.invoke(obj, new Object[] {data, dos });

			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			DataInputStream dis = new DataInputStream(bais);
			innnerClassMtd = innerClass.getDeclaredMethod("unmarshal", new Class[] {ConfigDAO.Data.class, DataInputStream.class });
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
	public void testWasMOdified() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("ConfigDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on ConfigDAO", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("DELETE APPROVAL",Env.REMOTE);
		Mockito.doNothing().when(tt).done();
		ConfigDAO.Data data  = new ConfigDAO.Data();
		PSInfo createPS = Mockito.mock(PSInfo.class);
		
		ConfigDAO daoObj = null;
		try {
			daoObj = new ConfigDAO(trans, cluster, "test");
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		daoObj.wasModified(trans, CRUD.create, data, new String[] {"test"});
	
		
		
	}
	
	@Test
	public void testRead() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("ConfigDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("ConfigDAO READ", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo READ on ConfigDAO", Env.SUB);
		Mockito.doReturn(tt).when(trans).start(Mockito.anyString(),Mockito.anyInt());
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
		Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);
		Mockito.doReturn(Mockito.mock(Decryptor.class)).when(trans).decryptor();
		Mockito.doNothing().when(tt).done();
		
		Result<List<Data>>  rs1 = new Result<List<Data>>(null,0,"test",new String[0]);
		

		PSInfo psObj = Mockito.mock(PSInfo.class);
		ConfigDAOImpl daoObj = null;
		try {
			daoObj = new ConfigDAOImpl(trans, cluster, "test",psObj);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Mockito.doReturn(rs1).when(psObj).read(trans, "ConfigDAO READ", new Object[]{"test"});
		daoObj.readName(trans, "test");
		
		
	}

	
	
	@Test
	public void testSecondConstructor() {
		TimeTaken tt = Mockito.mock(TimeTaken.class);
		Mockito.doReturn(tt).when(trans).start("ConfigDAO CREATE", Env.REMOTE);
		Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on ConfigDAO", Env.SUB);
		Mockito.doReturn(tt).when(trans).start("DELETE APPROVAL",Env.REMOTE);
		Mockito.doNothing().when(tt).done();
		AbsCassDAO absDAO = Mockito.mock(AbsCassDAO.class);

		try {
			ConfigDAO daoObj = new ConfigDAO(trans, absDAO);
		} catch (APIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

class ConfigDAOImpl extends ConfigDAO{

	
	public ConfigDAOImpl(AuthzTrans trans, Cluster cluster, String keyspace,PSInfo readPS  ) throws APIException, IOException {
		super(trans, cluster, keyspace);
		setPs(this, readPS, "psName");
	}
	

	public void setPs(ConfigDAOImpl ConfigDAOObj, PSInfo psInfoObj, String methodName) {
		Field nsDaoField;
		try {
			nsDaoField = ConfigDAO.class.getDeclaredField(methodName);
			
			nsDaoField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
//	        modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
	        
	        nsDaoField.set(ConfigDAOObj, psInfoObj);
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

	
	public void setSession(ConfigDAOImpl ConfigDAOObj, Session session) {
		Field nsDaoField;
		try {
			nsDaoField = AbsCassDAO.class.getDeclaredField("session");
			
			nsDaoField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
//	        modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
	        nsDaoField.set(ConfigDAOObj, session);
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
