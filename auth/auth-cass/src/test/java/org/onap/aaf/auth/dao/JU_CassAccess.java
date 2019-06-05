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

package org.onap.aaf.auth.dao;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.cass.HistoryDAO;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Decryptor;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;

import com.datastax.driver.core.Row;


public class JU_CassAccess {
    CassAccess cassAccess;
    
    @Mock
    Env env;
    
    String prefix=null;
    
    @Before
    public void setUp(){
    	initMocks(this);
        cassAccess = new CassAccess();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(env).info();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(env).init();
    }


    @Test
    public void testCluster() {
    	
    	setCbField();
    	try {
			CassAccess.cluster(env, null);
		} catch (APIException | IOException e) {
			assertTrue(e.getMessage().contains(" are not set"));
		}
    	setCbField();
		Mockito.doReturn("100").when(env).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
		Mockito.doReturn("100").when(env).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
		Mockito.doReturn("100").when(env).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
		Mockito.doReturn("100").when(env).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);
		try {
			CassAccess.cluster(env, null);
		} catch (APIException | IOException e) {
			assertTrue(e.getMessage().contains("No Password configured for"));
		}
		setCbField();
		Mockito.doReturn("100").when(env).getProperty(CassAccess.CASSANDRA_CLUSTERS_PASSWORD,"100");
		Mockito.doReturn("100").when(env).getProperty(CassAccess.CASSANDRA_CLUSTERS_PASSWORD,null);
		Decryptor decrypt = Mockito.mock(Decryptor.class);
		Mockito.doReturn(decrypt).when(env).decryptor();
		try {
			CassAccess.cluster(env, null);
		} catch (APIException | IOException e) {
			assertTrue(e.getMessage().contains("cadi_latitude and/or cadi_longitude are not set"));
		}
		
		setCbField();
		env = Mockito.mock(AuthzEnv.class);
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(env).info();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(env).init();
		Mockito.doReturn("100").when(env).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
		Mockito.doReturn("100").when(env).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
		Mockito.doReturn("100").when(env).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
		Mockito.doReturn("100").when(env).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);
		Mockito.doReturn("100").when(env).getProperty(CassAccess.CASSANDRA_CLUSTERS_PASSWORD,"100");
		Mockito.doReturn("100").when(env).getProperty(CassAccess.CASSANDRA_CLUSTERS_PASSWORD,null);
		decrypt = Mockito.mock(Decryptor.class);
		Mockito.doReturn(decrypt).when(env).decryptor();
		try {
			CassAccess.cluster(env, null);
		} catch (APIException | IOException e) {
			assertTrue(e.getMessage().contains("cadi_latitude and/or cadi_longitude are not set"));
		}
		setCbField();
		Mockito.doReturn("100").when(env).getProperty(Config.CADI_LATITUDE);
		Mockito.doReturn("100").when(env).getProperty(Config.CADI_LONGITUDE);
		Mockito.doReturn("google.com:90:90:90:90,google.com,google.com,google.com,google.com").when(env).getProperty("cassandra.clusters",null);
		try {
			CassAccess.cluster(env, null);
		} catch (APIException | IOException e) {
			assertTrue(e.getMessage().contains("cadi_latitude and/or cadi_longitude are not set"));
		}
		setCbField();
		Mockito.doReturn("100").when(env).getProperty(CassAccess.CASSANDRA_RESET_EXCEPTIONS,"100");
		Mockito.doReturn("100").when(env).getProperty(CassAccess.CASSANDRA_RESET_EXCEPTIONS,null);
		try {
			CassAccess.cluster(env, null);
		} catch (APIException | IOException e) {
			assertTrue(e.getMessage().contains("Declared Cassandra Reset Exception, 100, cannot be ClassLoaded"));
		}
    }
    
    private void setCbField() {
    	Field cbField;
		try {
			cbField = CassAccess.class.getDeclaredField("cb");
			cbField.setAccessible(true);
			cbField.set(cassAccess, null);
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
    public void testResettable() {
    	Class<?> innerClass = null;
		Class<?>[] innerClassArr = CassAccess.class.getDeclaredClasses();
		for(Class indCls:innerClassArr) {
			if(indCls.getName().contains("Resettable")) {
				innerClass = indCls;
				break;
			}
		}
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Method innnerClassMtd;
        try {
			Object obj = constructor.newInstance(env, null);
			obj = constructor.newInstance(env, "java.lang.String");
			obj = constructor.newInstance(env, "java.lang.String:java.lang.Exception");
			innnerClassMtd = innerClass.getMethod("matches", new Class[] {Exception.class});
			innnerClassMtd.invoke(obj, new Object[] {new Exception()});
			obj = constructor.newInstance(env, "java.lang.Exception");
			innnerClassMtd = innerClass.getMethod("matches", new Class[] {Exception.class});
			innnerClassMtd.invoke(obj, new Object[] {new Exception("test")});
			
			List<String> msg = new ArrayList<>();
			msg.add("test");
			Field innerField = innerClass.getDeclaredField("messages");
			innerField.setAccessible(true);
			innerField.set(obj, msg);
			
			innnerClassMtd = innerClass.getMethod("matches", new Class[] {Exception.class});
			innnerClassMtd.invoke(obj, new Object[] {new Exception("test")});
			
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
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }

    @Test
    public void testIsResetException() {
    	assertTrue(CassAccess.isResetException(Mockito.mock(Exception.class)));
    	assertTrue(CassAccess.isResetException(null));
    	
    }
}
