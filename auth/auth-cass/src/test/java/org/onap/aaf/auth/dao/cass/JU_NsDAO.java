/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.dao.cass;

import static org.junit.Assert.assertNull;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.AbsCassDAO;
import org.onap.aaf.auth.dao.AbsCassDAO.CRUD;
import org.onap.aaf.auth.dao.AbsCassDAO.PSInfo;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.dao.cass.NsDAO.Data;
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

public class JU_NsDAO {

    @Mock
    AuthzTrans trans;
    @Mock
    Cluster cluster;
    @Mock
    Session session;
    @Mock
    ResultSet rs;

    @Before
    public void setUp() throws APIException, IOException {
        initMocks(this);
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).warn();
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).info();
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).init();
        Mockito.doReturn(session).when(cluster).connect("test");
        Mockito.doReturn(Mockito.mock(TimeTaken.class)).when(trans).start(Mockito.anyString(), Mockito.anyInt());
        Mockito.doReturn("100").when(trans).getProperty(Config.CADI_LATITUDE);
        Mockito.doReturn("100").when(trans).getProperty(Config.CADI_LONGITUDE);

        Iterator<Row> ite = Mockito.mock(Iterator.class);
        Mockito.doReturn(ite).when(rs).iterator();
        Mockito.doReturn(rs).when(session).execute(Mockito.anyString());
    }

    @Test
    public void testInit() {
        try {
            Session session =  Mockito.mock(Session.class);
            new NsDAOImpl(trans, cluster, "test", session);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setPsByStartAndTarget(NsDAO NsDAOObj, PSInfo psInfoObj, String fieldName) {
        Field nsDaoField;
        try {
            nsDaoField = NsDAO.class.getDeclaredField(fieldName);
        
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
        
            nsDaoField.set(NsDAOObj, psInfoObj);
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
        Mockito.doReturn(tt).when(trans).start("NsDAO CREATE", Env.REMOTE);
        Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on NsDAO", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("DELETE Future",Env.REMOTE);
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
        Mockito.doNothing().when(tt).done();
        NsDAO.Data data  = new NsDAO.Data();
    
        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        Result<ResultSet> rs1 = new Result<ResultSet>(null,0,"test",new Object[0]);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
        CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
        Mockito.doReturn(rs1).when(cacheInfoDAO).touch(Mockito.any(), Mockito.anyString(), Mockito.anyInt());
    
        NsDAO daoObj = null;
        try {
            daoObj = new NsDAO(trans, historyDAO, cacheInfoDAO);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        daoObj.wasModified(trans, CRUD.create, data, new String[] {"test"});
    
        daoObj.wasModified(trans, CRUD.create, data, new String[] {});
        daoObj.wasModified(trans, CRUD.create, data, new String[] {null});
        daoObj.wasModified(trans, CRUD.create, data, new String[] {"test",null});
        daoObj.wasModified(trans, CRUD.create, data, new String[] {"test","test"});
    
        rs1 = new Result<ResultSet>(null,1,"test",new Object[0]);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
        daoObj.wasModified(trans, CRUD.create, data, new String[] {"test","test"});
    }

    @Test
    public void testSecondConstructor() {
        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);

        try {
            new NsDAO(trans, historyDAO, cacheInfoDAO);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
    }

    @Test
    public void testNSLoader(){
        Class<?> innerClass = null;
        Class<?>[] innerClassArr = NsDAO.class.getDeclaredClasses();
        for(Class<?> indCls:innerClassArr) {
            if(indCls.getName().contains("NSLoader")) {
                innerClass = indCls;
                break;
            }
        }
    
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
    
        try {
        
            Object obj = constructor.newInstance(1);
            Method innnerClassMtd;
            
            NsDAO.Data data  = new NsDAO.Data();
            Row row = Mockito.mock(Row.class);
            ByteBuffer bbObj = ByteBuffer.allocateDirect(10);
            bbObj.limit(7);
            bbObj.put(0, new Byte("0"));
            bbObj.put(1, new Byte("1"));
            bbObj.put(2, new Byte("2"));
            Mockito.doReturn(bbObj).when(row).getBytesUnsafe(1);
        
            innnerClassMtd = innerClass.getMethod("load", new Class[] {NsDAO.Data.class, Row.class});
            innnerClassMtd.invoke(obj, new Object[] {data, row});
        
            innnerClassMtd = innerClass.getDeclaredMethod("key", new Class[] {NsDAO.Data.class, Integer.TYPE, Object[].class });
            innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test"} });
//        
            innnerClassMtd = innerClass.getDeclaredMethod("body", new Class[] {NsDAO.Data.class, Integer.TYPE, Object[].class });
            innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test","test","test","test","test","test","test","test","test"} });
        
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            innnerClassMtd = innerClass.getDeclaredMethod("marshal", new Class[] {NsDAO.Data.class, DataOutputStream.class });
            innnerClassMtd.invoke(obj, new Object[] {data, dos });

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            DataInputStream dis = new DataInputStream(bais);
            innnerClassMtd = innerClass.getDeclaredMethod("unmarshal", new Class[] {NsDAO.Data.class, DataInputStream.class });
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
    public void testCreate() {
        PSInfo psObj = Mockito.mock(PSInfo.class);

        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        NsDAO.Data data = new NsDAO.Data();

        Result<ResultSet>  rs1 = new Result<ResultSet>(null,0,"test",new Object[0]);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl CREATE", data);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
        CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
        NsDAOImpl daoObj=null;
        try {
            daoObj = new NsDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, session);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn("test user").when(trans).user();
    
        Result<NsDAO.Data> retVal = daoObj.create(trans,data);
        assertTrue(retVal.status == 4);

        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);
        data.parent = "parent";
        data.attrib = new HashMap<>();
        data.attrib.put("test", "test");
    
        Field cbField;
        Mockito.doReturn(rs1).when(cacheInfoDAO).touch(Mockito.any(), Mockito.anyString(), Mockito.anyInt());
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
    
        retVal = daoObj.create(trans,data);
        assertTrue(retVal.status == 9);
    
        Field owningField;
        Mockito.doReturn(rs1).when(cacheInfoDAO).touch(Mockito.any(), Mockito.anyString(), Mockito.anyInt());
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
        retVal = daoObj.create(trans,data);
        assertTrue(retVal.status == 0);
    
    }

    @Test
    public void testUpdate() {
        PSInfo psObj = Mockito.mock(PSInfo.class);

        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        NsDAO.Data data = new NsDAO.Data();

        Result<ResultSet>  rs1 = new Result<ResultSet>(null,0,"test",new Object[0]);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl CREATE", data);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl UPDATE", data);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
        CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
        NsDAOImpl daoObj=null;
        try {
            daoObj = new NsDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, session);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn("test user").when(trans).user();
    
        Result<Void> retVal = daoObj.update(trans,data);
        assertTrue(retVal.status == 4);

        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);
        data.parent = "parent";
        data.attrib = new HashMap<>();
        data.attrib.put("test", "test");
        Field cbField;
        Mockito.doReturn(rs1).when(cacheInfoDAO).touch(Mockito.any(), Mockito.anyString(), Mockito.anyInt());
        try {
            cbField = AbsCassDAO.class.getDeclaredField("owningDAO");
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
    
        retVal = daoObj.update(trans,data);
        assertTrue(retVal.status == 0);
    
        ResultSet rsMock = Mockito.mock(ResultSet.class);
        Iterator<Row> iteMock = Mockito.mock(Iterator.class);
        Mockito.doReturn(iteMock).when(rsMock).iterator();
        Row rowMock = Mockito.mock(Row.class);
        Mockito.doReturn(rowMock).when(iteMock).next();
        Mockito.when(iteMock.hasNext()).thenReturn(true, false);
        Mockito.doReturn("test").when(rowMock).getString(Mockito.anyInt());
        Mockito.doReturn(rsMock).when(session).execute(Mockito.anyString());
        retVal = daoObj.update(trans,data);
        assertTrue(retVal.status == 0);
    }

    @Test
    public void testRead() {
        PSInfo psObj = Mockito.mock(PSInfo.class);
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);

        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        NsDAO.Data data = new NsDAO.Data();

        Result<List<Data>>  rs1 = new Result<List<Data>>(null,0,"test",new Object[0]);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl CREATE", data);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl UPDATE", data);
        Mockito.doReturn(rs1).when(psObj).read(trans, "NsDAOImpl READ", data);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
        CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
        NsDAOImpl daoObj=null;
        try {
            daoObj = new NsDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, session);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn("test user").when(trans).user();
    
        Result<List<Data>> retVal = daoObj.read(trans,data);
        assertTrue(retVal.status == 0);

        List<Data> dataAL= new ArrayList<>();
        dataAL.add(data);
        rs1 = new Result<List<Data>>(dataAL,0,"test",new Object[0]);
        Mockito.doReturn(rs1).when(psObj).read(trans, "NsDAOImpl READ", data);
        Field cbField;
        Mockito.doReturn(rs1).when(cacheInfoDAO).touch(Mockito.any(), Mockito.anyString(), Mockito.anyInt());
        try {
            cbField = AbsCassDAO.class.getDeclaredField("owningDAO");
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
        retVal = daoObj.read(trans,data);
        assertTrue(retVal.status == 0);

    }

    @Test
    public void testReadByObject() {
        PSInfo psObj = Mockito.mock(PSInfo.class);
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);

        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        NsDAO.Data data = new NsDAO.Data();

        Result<List<Data>>  rs1 = new Result<List<Data>>(null,0,"test",new Object[0]);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl CREATE", data);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl UPDATE", data);
        Mockito.doReturn(rs1).when(psObj).read(trans, "NsDAOImpl READ", new Object[] {});
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
        CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
        NsDAOImpl daoObj=null;
        try {
            daoObj = new NsDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, session);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn("test user").when(trans).user();
    
        Result<List<Data>> retVal = daoObj.read(trans,new Object[] {});
        assertTrue(retVal.status == 0);

        List<Data> dataAL= new ArrayList<>();
        dataAL.add(data);
        rs1 = new Result<List<Data>>(dataAL,0,"test",new Object[0]);
        Mockito.doReturn(rs1).when(psObj).read(trans, "NsDAOImpl READ", new Object[] {});
        Field cbField;
        Mockito.doReturn(rs1).when(cacheInfoDAO).touch(Mockito.any(), Mockito.anyString(), Mockito.anyInt());
        try {
            cbField = AbsCassDAO.class.getDeclaredField("owningDAO");
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
        retVal = daoObj.read(trans,new Object[] {});
        assertTrue(retVal.status == 0);

    }

    @Test
    public void testDelete() {
        PSInfo psObj = Mockito.mock(PSInfo.class);
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);

        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        NsDAO.Data data = new NsDAO.Data();

        Result<List<Data>>  rs1 = new Result<List<Data>>(null,0,"test",new Object[0]);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl CREATE", data);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl UPDATE", data);
        Mockito.doReturn(rs1).when(psObj).read(trans, "NsDAOImpl READ", data);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl DELETE", data);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
        CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
        NsDAOImpl daoObj=null;
        try {
            daoObj = new NsDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, session);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn("test user").when(trans).user();
        Field cbField;
        Mockito.doReturn(rs1).when(cacheInfoDAO).touch(Mockito.any(), Mockito.anyString(), Mockito.anyInt());
        try {
            cbField = AbsCassDAO.class.getDeclaredField("owningDAO");
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
    
        Result<Void> retVal = daoObj.delete(trans,data, false);
        assertTrue(retVal.status == 0);

        List<Data> dataAL= new ArrayList<>();
        dataAL.add(data);
        rs1 = new Result<List<Data>>(dataAL,0,"test",new Object[0]);
        Mockito.doReturn(rs1).when(psObj).read(trans, "NsDAOImpl READ", data);
    
        retVal = daoObj.delete(trans,data, false);
        assertTrue(retVal.status == 0);

    }

    @Test
    public void testReadNsByAttrib() {
        PSInfo psObj = Mockito.mock(PSInfo.class);
        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        NsDAO.Data data = new NsDAO.Data();
        CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
        NsDAOImpl daoObj=null;
        try {
            daoObj = new NsDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, session);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);

        Result<List<Data>>  rs1 = new Result<List<Data>>(null,0,"test",new Object[0]);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl CREATE", data);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl UPDATE", data);
        Mockito.doReturn(rs1).when(psObj).read(trans, "NsDAOImpl READ", data);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl DELETE", data);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
    
        Mockito.doReturn("test user").when(trans).user();
        Field cbField;
        try {
            cbField = AbsCassDAO.class.getDeclaredField("owningDAO");
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
    
        Result<Set<String>> retVal = daoObj.readNsByAttrib(trans,"test");
        assertTrue(retVal.status == 0);
    
        ResultSet rsMock = Mockito.mock(ResultSet.class);
        Iterator<Row> iteMock = Mockito.mock(Iterator.class);
        Mockito.doReturn(iteMock).when(rsMock).iterator();
        Row rowMock = Mockito.mock(Row.class);
        Mockito.doReturn(rowMock).when(iteMock).next();
        Mockito.when(iteMock.hasNext()).thenReturn(true, false);
        Mockito.doReturn("test").when(rowMock).getString(Mockito.anyInt());
        Mockito.doReturn(rsMock).when(session).execute(Mockito.anyString());

        retVal = daoObj.readNsByAttrib(trans,"test");
        assertTrue(retVal.status == 0);

    }
    @Test
    public void testAttribAdd() {
        PSInfo psObj = Mockito.mock(PSInfo.class);
        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        NsDAO.Data data = new NsDAO.Data();
        CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
        NsDAOImpl daoObj=null;
        try {
            daoObj = new NsDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, session);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);

        Result<List<Data>>  rs1 = new Result<List<Data>>(null,0,"test",new Object[0]);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl CREATE", data);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl UPDATE", data);
        Mockito.doReturn(rs1).when(psObj).read(trans, "NsDAOImpl READ", data);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl DELETE", data);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
    
        Mockito.doReturn("test user").when(trans).user();
        Field cbField;
        try {
            cbField = AbsCassDAO.class.getDeclaredField("owningDAO");
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
    
        Result<Void> retVal = daoObj.attribAdd(trans, "test", "test", "test");
        assertTrue(retVal.status == 0);
    }

    @Test
    public void testAttribRemove() {
        PSInfo psObj = Mockito.mock(PSInfo.class);
        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        NsDAO.Data data = new NsDAO.Data();
        CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
        NsDAOImpl daoObj=null;
        try {
            daoObj = new NsDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, session);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);

        Result<List<Data>>  rs1 = new Result<List<Data>>(null,0,"test",new Object[0]);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl CREATE", data);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl UPDATE", data);
        Mockito.doReturn(rs1).when(psObj).read(trans, "NsDAOImpl READ", data);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl DELETE", data);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
    
        Mockito.doReturn("test user").when(trans).user();
        Field cbField;
        try {
            cbField = AbsCassDAO.class.getDeclaredField("owningDAO");
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
    
        Result<Void> retVal = daoObj.attribRemove(trans, "test", "test");
        assertTrue(retVal.status == 0);
    }

    @Test
    public void testAddDescription() {
        PSInfo psObj = Mockito.mock(PSInfo.class);
        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        NsDAO.Data data = new NsDAO.Data();
        CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
        NsDAOImpl daoObj=null;
        try {
            daoObj = new NsDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, session);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_PORT,"9042");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,"100");
        Mockito.doReturn("100").when(trans).getProperty(CassAccess.CASSANDRA_CLUSTERS_USER_NAME,null);

        Result<List<Data>>  rs1 = new Result<List<Data>>(null,0,"test",new Object[0]);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl CREATE", data);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl UPDATE", data);
        Mockito.doReturn(rs1).when(psObj).read(trans, "NsDAOImpl READ", data);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "NsDAOImpl DELETE", data);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());

         rs1 = new Result<List<Data>>(null,1,"test",new String[0]);
        Mockito.doReturn(rs1).when(cacheInfoDAO).touch(Mockito.any(), Mockito.anyString(), Mockito.anyInt());
        Mockito.doReturn("test user").when(trans).user();
        Field cbField;
        try {
            cbField = AbsCassDAO.class.getDeclaredField("owningDAO");
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
    
        Result<Void> retVal = daoObj.addDescription(trans, "test", "test");
        assertTrue(retVal.status == 0);
    }

    @Test
    public void testGetChildren() {
        PSInfo psObj = Mockito.mock(PSInfo.class);
        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        CacheInfoDAO cacheInfoDAO = Mockito.mock(CacheInfoDAO.class);
        NsDAOImpl daoObj=null;
        try {
            daoObj = new NsDAOImpl(trans, historyDAO, cacheInfoDAO, psObj, session);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        Result<List<Data>> retVal = daoObj.getChildren(trans, "test");
        assertNull(retVal);
    }

    @Test
    public void testData() {
        NsDAO.Data data = new NsDAO.Data();
        data.attrib = null;
        data.attrib(true);

        data.attrib = new HashMap<>();
        data.attrib(true);

        data.attrib(false);
        data.attrib = new ConcurrentHashMap<>();
        data.attrib(true);
    
        data.name="123";
        data.split("test");
    
        data.toString();
    }

}

class NsDAOImpl extends NsDAO{


//    public NsDAOImpl(AuthzTrans trans, HistoryDAO historyDAO,PSInfo readPS  ) throws APIException, IOException {
//        super(trans, historyDAO);
//        setSession(this, Mockito.mock(Session.class));
//    }


    public NsDAOImpl(AuthzTrans trans, Cluster cluster, String keySpace, Session session)throws APIException, IOException {
        super(trans, cluster, keySpace);
        setSession(this, session);
    }


    public NsDAOImpl(AuthzTrans trans, HistoryDAO historyDAO, CacheInfoDAO cacheInfoDAO,
            org.onap.aaf.auth.dao.AbsCassDAO.PSInfo psObj, Session session) throws APIException, IOException {
        super(trans, historyDAO, cacheInfoDAO);
        setPs(this, psObj, "createPS");
        setPs(this, psObj, "updatePS");
        setPs(this, psObj, "readPS");
        setPs(this, psObj, "deletePS");
        setPsNs(this, psObj, "psNS");
        setSession(this, session);
    }
    public void setPsNs(NsDAOImpl NsDAOObj, PSInfo psInfoObj, String methodName) {
        Field nsDaoField;
        try {
            nsDaoField = NsDAO.class.getDeclaredField(methodName);
        
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
        
            nsDaoField.set(NsDAOObj, psInfoObj);
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

    public void setPs(NsDAOImpl NsDAOObj, PSInfo psInfoObj, String methodName) {
        Field nsDaoField;
        try {
            nsDaoField = CassDAOImpl.class.getDeclaredField(methodName);
        
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
        
            nsDaoField.set(NsDAOObj, psInfoObj);
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
    public void setSession(NsDAOImpl approvalDaoObj, Session session) {
        Field nsDaoField;
        try {
            nsDaoField = AbsCassDAO.class.getDeclaredField("session");
        
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
        
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
