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
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.AbsCassDAO.CRUD;
import org.onap.aaf.auth.dao.AbsCassDAO.PSInfo;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public class JU_FutureDAO {

    @Mock
    AuthzTrans trans;
    @Mock
    Cluster cluster;

    @Before
    public void setUp() throws APIException, IOException {
        initMocks(this);
    }

    @Test
    public void testInit() {
        FutureDAO daoObj = new FutureDAO(trans, cluster, "test");
//        daoObj.
    }
    @Test
    public void testReadByStartAndTarget() {
        FutureDAO daoObj = new FutureDAO(trans, cluster, "test");
    
        PSInfo psObj = Mockito.mock(PSInfo.class);
        setPsByStartAndTarget(daoObj, psObj, "psByStartAndTarget");
    
        Result<List<FutureDAO.Data>>  rs1 = new Result<List<FutureDAO.Data>>(null,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(psObj).read(trans, "FutureDAO READ", new Object[]{"test"});
    
        daoObj.readByStartAndTarget(trans,new Date(), "test");
    }
    @Test
    public void testCreate() {
        PSInfo psObj = Mockito.mock(PSInfo.class);

        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        FutureDAO.Data data = new FutureDAO.Data();

        Result<ResultSet>  rs1 = new Result<ResultSet>(null,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "FutureDAOImpl CREATE", data);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
    
        FutureDAOImpl daoObj=null;
        try {
            daoObj = new FutureDAOImpl(trans, historyDAO, psObj);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn("test user").when(trans).user();
    
        Result<FutureDAO.Data> retVal = daoObj.create(trans,data, "test");
        assertTrue(retVal.status == 0);
    
        StringBuilder sb = new StringBuilder(trans.user());
        sb.append(data.target);
        sb.append(System.currentTimeMillis());
        data.id = UUID.nameUUIDFromBytes(sb.toString().getBytes());
    
        rs1 = new Result<ResultSet>(null,1,"test",new String[0]);
        Mockito.doReturn(rs1).when(psObj).exec(trans, "FutureDAOImpl CREATE", data);
    
        retVal = daoObj.create(trans,data, "test");
        assertTrue(retVal.status != 0);
    
    
    }

    public void setPsByStartAndTarget(FutureDAO FutureDAOObj, PSInfo psInfoObj, String fieldName) {
        Field nsDaoField;
        try {
            nsDaoField = FutureDAO.class.getDeclaredField(fieldName);
        
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
        
            nsDaoField.set(FutureDAOObj, psInfoObj);
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
        Mockito.doReturn(tt).when(trans).start("FutureDAO CREATE", Env.REMOTE);
        Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on FutureDAO", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("DELETE Future",Env.REMOTE);
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
        Mockito.doNothing().when(tt).done();
        FutureDAO.Data data  = new FutureDAO.Data();
        PSInfo createPS = Mockito.mock(PSInfo.class);
    
        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        Result<ResultSet> rs1 = new Result<ResultSet>(null,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
    
        FutureDAOImpl daoObj = null;
        try {
            daoObj = new FutureDAOImpl(trans, historyDAO, createPS );
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
        daoObj.wasModified(trans, CRUD.create, data, new String[] {"test","test"});
    }

    @Test
    public void testSecondConstructor() {
        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);

        FutureDAO daoObj = new FutureDAO(trans, historyDAO);
    
    }

    @Test
    public void testFutureLoader(){
        Class<?> innerClass = null;
        Class<?>[] innerClassArr = FutureDAO.class.getDeclaredClasses();
        for(Class indCls:innerClassArr) {
            if(indCls.getName().contains("FLoader")) {
                innerClass = indCls;
                break;
            }
        }
    
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[1];
        constructor.setAccessible(true);
    
        Constructor<?> constructor1 = innerClass.getDeclaredConstructors()[0];
        constructor1.setAccessible(true);
        try {
        
            Object obj = constructor.newInstance(1);
            obj = constructor1.newInstance();
            Method innnerClassMtd;
            
            FutureDAO.Data data  = new FutureDAO.Data();
            Row row = Mockito.mock(Row.class);
            ByteBuffer bbObj = ByteBuffer.allocateDirect(10);
            bbObj.limit(7);
            bbObj.put(0, new Byte("0"));
            bbObj.put(1, new Byte("1"));
            bbObj.put(2, new Byte("2"));
            Mockito.doReturn(bbObj).when(row).getBytesUnsafe(1);
        
            innnerClassMtd = innerClass.getMethod("load", new Class[] {FutureDAO.Data.class, Row.class});
            innnerClassMtd.invoke(obj, new Object[] {data, row});
        
            innnerClassMtd = innerClass.getDeclaredMethod("key", new Class[] {FutureDAO.Data.class, Integer.TYPE, Object[].class });
            innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test"} });
//        
            innnerClassMtd = innerClass.getDeclaredMethod("body", new Class[] {FutureDAO.Data.class, Integer.TYPE, Object[].class });
            innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test","test","test","test","test","test","test","test","test"} });
        
//            DataOutputStream dos = new DataOutputStream(new FileOutputStream("JU_FutureDAOTest.java"));
//            innnerClassMtd = innerClass.getDeclaredMethod("marshal", new Class[] {FutureDAO.Data.class, DataOutputStream.class });
//            innnerClassMtd.invoke(obj, new Object[] {data, dos });

//            DataInputStream dis = new DataInputStream(new FileInputStream("JU_FutureDAOTest.java"));
//            innnerClassMtd = innerClass.getDeclaredMethod("unmarshal", new Class[] {FutureDAO.Data.class, DataInputStream.class });
//            innnerClassMtd.invoke(obj, new Object[] {data, dis });
        
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

}

class FutureDAOImpl extends FutureDAO{


    public FutureDAOImpl(AuthzTrans trans, HistoryDAO historyDAO,PSInfo readPS  ) throws APIException, IOException {
        super(trans, historyDAO);
        setPs(this, readPS, "createPS");
    }


    public void setPs(FutureDAOImpl FutureDAOObj, PSInfo psInfoObj, String methodName) {
        Field nsDaoField;
        try {
            nsDaoField = CassDAOImpl.class.getDeclaredField(methodName);
        
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
        
            nsDaoField.set(FutureDAOObj, psInfoObj);
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
