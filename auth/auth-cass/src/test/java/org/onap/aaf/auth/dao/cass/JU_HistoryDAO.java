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
import org.onap.aaf.auth.dao.cass.HistoryDAO.Data;
import org.onap.aaf.auth.dao.AbsCassDAO;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.dao.Loader;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public class JU_HistoryDAO {

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
        HistoryDAO daoObj = new HistoryDAO(trans, cluster, "test");
    }
    @Test
    public void testNewInitedData() {
        HistoryDAO daoObj = new HistoryDAO(trans, cluster, "test");
        HistoryDAO.Data data = daoObj.newInitedData();
        assertTrue( Integer.toString(((new Date()).getYear())+1900).equalsIgnoreCase(Integer.toString(data.yr_mon).substring(0,4)) );
    }

    @Test
    public void testCreateBatch() {
        HistoryDAO daoObj = new HistoryDAO(trans, cluster, "test");
        StringBuilder sb = new StringBuilder();
        HistoryDAO.Data data = new HistoryDAO.Data();
        daoObj.createBatch(sb, data);
        assertTrue(sb.toString().contains("INSERT INTO history"));
    }

    @Test
    public void testReadByYYYYMM() {
        HistoryDAO daoObj = new HistoryDAO(trans, cluster, "test");
        AbsCassDAO<AuthzTrans, Data>.PSInfo psInfoObj = Mockito.mock(PSInfo.class);
        setAbsCassDAO(daoObj, psInfoObj, "readByYRMN");
    
        ResultSet rs = Mockito.mock(ResultSet.class);
        Result<ResultSet>  rs1 = new Result<ResultSet>(rs,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(psInfoObj).exec(trans, "yr_mon", 201905);
    
        Result<List<Data>> retVal = daoObj.readByYYYYMM(trans, 201905);
        assertTrue(retVal.status !=1);
    
        rs1 = new Result<ResultSet>(rs,1,"test",new String[0]);
        Mockito.doReturn(rs1).when(psInfoObj).exec(trans, "yr_mon", 201905);
        retVal = daoObj.readByYYYYMM(trans, 201905);
        assertTrue(retVal.status !=0);
    }

    @Test
    public void testReadByUser() {
        HistoryDAO daoObj = new HistoryDAO(trans, cluster, "test");
        AbsCassDAO<AuthzTrans, Data>.PSInfo psInfoObj = Mockito.mock(PSInfo.class);
        setAbsCassDAO(daoObj, psInfoObj, "readByUser");
    
        ResultSet rs = Mockito.mock(ResultSet.class);
        Result<ResultSet>  rs1 = new Result<ResultSet>(rs,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(psInfoObj).exec(trans, "user", "test");
    
        Result<List<Data>> retVal = daoObj.readByUser(trans, "test", 201905);
        assertTrue(retVal.status !=1);
    
        rs1 = new Result<ResultSet>(rs,1,"test",new String[0]);
        Mockito.doReturn(rs1).when(psInfoObj).exec(trans, "user", "test");
        retVal = daoObj.readByUser(trans,"test", 201905);
        assertTrue(retVal.status !=0);
    
        retVal = daoObj.readByUser(trans,"test");
        assertTrue(retVal.status !=0);
    }

    @Test
    public void testReadBySubject() {
        HistoryDAO daoObj = new HistoryDAO(trans, cluster, "test");
        AbsCassDAO<AuthzTrans, Data>.PSInfo psInfoObj = Mockito.mock(PSInfo.class);
        setAbsCassDAO(daoObj, psInfoObj, "readBySubject");
    
        ResultSet rs = Mockito.mock(ResultSet.class);
        Result<ResultSet>  rs1 = new Result<ResultSet>(rs,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(psInfoObj).exec(trans, "subject", "test", "test");
    
        Result<List<Data>> retVal = daoObj.readBySubject(trans, "test", "test", 201905);
        assertTrue(retVal.status !=1);
    
        rs1 = new Result<ResultSet>(rs,1,"test",new String[0]);
        Mockito.doReturn(rs1).when(psInfoObj).exec(trans, "subject", "test", "test");
        retVal = daoObj.readBySubject(trans,"test", "test", 201905);
        assertTrue(retVal.status !=0);
    
        retVal = daoObj.readBySubject(trans,"test", "test");
        assertTrue(retVal.status !=0);
    }

    public void setAbsCassDAO(HistoryDAO HistoryDAOObj, PSInfo psInfoObj, String fieldName) {
        Field nsDaoField;
        try {
            nsDaoField = HistoryDAO.class.getDeclaredField(fieldName);
        
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
        
            nsDaoField.set(HistoryDAOObj, psInfoObj);
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
    public void testSecondConstructor() {
        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);

        HistoryDAO daoObj = new HistoryDAO(trans, historyDAO);
    
    }

    @Test
    public void testHistoryLoader(){
        Class<?> innerClass = null;
        Class<?>[] innerClassArr = HistoryDAO.class.getDeclaredClasses();
        for(Class indCls:innerClassArr) {
            if(indCls.getName().contains("HistLoader")) {
                innerClass = indCls;
                break;
            }
        }
    
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
    
        try {
            Object obj = constructor.newInstance(1);
            Method innnerClassMtd;
            
            HistoryDAO.Data data  = new HistoryDAO.Data();
            Row row = Mockito.mock(Row.class);
            ByteBuffer bbObj = ByteBuffer.allocateDirect(10);
            bbObj.limit(7);
            bbObj.put(0, new Byte("0"));
            bbObj.put(1, new Byte("1"));
            bbObj.put(2, new Byte("2"));
            Mockito.doReturn(bbObj).when(row).getBytesUnsafe(1);
        
            innnerClassMtd = innerClass.getMethod("load", new Class[] {HistoryDAO.Data.class, Row.class});
            innnerClassMtd.invoke(obj, new Object[] {data, row});
        
            innnerClassMtd = innerClass.getDeclaredMethod("key", new Class[] {HistoryDAO.Data.class, Integer.TYPE, Object[].class });
            innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test"} });
//        
            innnerClassMtd = innerClass.getDeclaredMethod("body", new Class[] {HistoryDAO.Data.class, Integer.TYPE, Object[].class });
            innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test","test","test","test","test","test","test","test","test"} });
        
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
    public void testYYYYMM(){
        Class<?> innerClass = null;
        Class<?>[] innerClassArr = HistoryDAO.class.getDeclaredClasses();
        for(Class indCls:innerClassArr) {
            if(indCls.getName().contains("YYYYMM")) {
                innerClass = indCls;
                break;
            }
        }
    
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        int yyyymm[] = new int[2];
        try {
            Object obj = constructor.newInstance(new HistoryDAO(trans, cluster, "test"), yyyymm);
            Method innnerClassMtd;
            
            HistoryDAO.Data data  = new HistoryDAO.Data();
            Row row = Mockito.mock(Row.class);
            ByteBuffer bbObj = ByteBuffer.allocateDirect(10);
            bbObj.limit(7);
            bbObj.put(0, new Byte("0"));
            bbObj.put(1, new Byte("1"));
            bbObj.put(2, new Byte("2"));
            Mockito.doReturn(bbObj).when(row).getBytesUnsafe(1);
        
            innnerClassMtd = innerClass.getMethod("ok", new Class[] {HistoryDAO.Data.class});
            innnerClassMtd.invoke(obj, new Object[] {data});
        
            data.yr_mon=201904;
            innnerClassMtd.invoke(obj, new Object[] {data});
        
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

class HistoryDAOImpl extends HistoryDAO{


    public HistoryDAOImpl(AuthzTrans trans, HistoryDAO historyDAO,PSInfo readPS  ) throws APIException, IOException {
        super(trans, historyDAO);
        setPs(this, readPS, "createPS");
    }

    public void setPs(HistoryDAOImpl HistoryDAOObj, PSInfo psInfoObj, String methodName) {
        Field nsDaoField;
        try {
            nsDaoField = CassDAOImpl.class.getDeclaredField(methodName);
        
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
        
            nsDaoField.set(HistoryDAOObj, psInfoObj);
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
