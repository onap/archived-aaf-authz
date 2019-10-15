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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.AbsCassDAO;
import org.onap.aaf.auth.dao.AbsCassDAO.CRUD;
import org.onap.aaf.auth.dao.AbsCassDAO.PSInfo;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;

public class JU_LocateDAO {

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
        try {
            LocateDAO daoObj = new LocateDAO(trans, cluster, "test");
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void testReadByStartAndTarget() {
        LocateDAO daoObj = null;
        try {
            daoObj = new LocateDAO(trans, cluster, "test");
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        PSInfo psObj = Mockito.mock(PSInfo.class);
        setPsByStartAndTarget(daoObj, psObj, "psName");
    
        Result<List<LocateDAO.Data>>  rs1 = new Result<List<LocateDAO.Data>>(null,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(psObj).read(trans, "LocateDAO READ", new Object[]{"test"});
    
        daoObj.readByName(trans, "test");
    }


    public void setPsByStartAndTarget(LocateDAO LocateDAOObj, PSInfo psInfoObj, String fieldName) {
        Field nsDaoField;
        try {
            nsDaoField = LocateDAO.class.getDeclaredField(fieldName);
        
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
        
            nsDaoField.set(LocateDAOObj, psInfoObj);
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
    
        LocateDAO.Data data  = new LocateDAO.Data();
    
        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
    
        LocateDAO daoObj = null;
        try {
            daoObj = new LocateDAO(trans, historyDAO );
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        daoObj.wasModified(trans, CRUD.create, data, new String[] {"test"});
    
    
    }

    @Test
    public void testSecondConstructor() {
        AbsCassDAO historyDAO = Mockito.mock(AbsCassDAO.class);

        try {
            LocateDAO daoObj = new LocateDAO(trans, historyDAO);
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
    }

    @Test
    public void testLocateLoader(){
        Class<?> innerClass = null;
        Class<?>[] innerClassArr = LocateDAO.class.getDeclaredClasses();
        for(Class indCls:innerClassArr) {
            if(indCls.getName().contains("LocateLoader")) {
                innerClass = indCls;
                break;
            }
        }
    
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
    
        try {
        
            Object obj = constructor.newInstance(1);
            Method innnerClassMtd;
            
            LocateDAO.Data data  = new LocateDAO.Data();
            Row row = Mockito.mock(Row.class);
            ByteBuffer bbObj = ByteBuffer.allocateDirect(10);
            bbObj.limit(7);
            bbObj.put(0, new Byte("0"));
            bbObj.put(1, new Byte("1"));
            bbObj.put(2, new Byte("2"));
            Mockito.doReturn(bbObj).when(row).getBytesUnsafe(1);
        
            innnerClassMtd = innerClass.getMethod("load", new Class[] {LocateDAO.Data.class, Row.class});
            innnerClassMtd.invoke(obj, new Object[] {data, row});
        
            innnerClassMtd = innerClass.getDeclaredMethod("key", new Class[] {LocateDAO.Data.class, Integer.TYPE, Object[].class });
            innnerClassMtd.invoke(obj, new Object[] {data, 0, new Object[] {"test","test","test"} });
        
            innnerClassMtd = innerClass.getDeclaredMethod("body", new Class[] {LocateDAO.Data.class, Integer.TYPE, Object[].class });
            innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test","test","test","test","test","test","test","test","test"} });
        
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            innnerClassMtd = innerClass.getDeclaredMethod("marshal", new Class[] {LocateDAO.Data.class, DataOutputStream.class });
            innnerClassMtd.invoke(obj, new Object[] {data, dos });

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            DataInputStream dis = new DataInputStream(bais);
            innnerClassMtd = innerClass.getDeclaredMethod("unmarshal", new Class[] {LocateDAO.Data.class, DataInputStream.class });
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
    public void testData(){
        LocateDAO.Data data  = new LocateDAO.Data();
        data.name="name";
        data.hostname="hostname";
        try {
            data.bytify();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        data.subprotocol(true);
    
        Set<String> subProt = new HashSet<String>();
        Field protField;
        try {
            protField = LocateDAO.Data.class.getDeclaredField("subprotocol");
        
            protField.setAccessible(true);
        
            protField.set(data, subProt);
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
    
        data.subprotocol(true);
        subProt = new TreeSet<String>();
        subProt.add("test");
        try {
            protField = LocateDAO.Data.class.getDeclaredField("subprotocol");
        
            protField.setAccessible(true);
        
            protField.set(data, subProt);
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
    
        data.subprotocol(true);
        data.subprotocol(false);
    
        LocateDAO.Data newDate = data.copy();
        assertTrue(data.name.equals(newDate.name));
    }

}

class LocateDAOImpl extends LocateDAO{


    public LocateDAOImpl(AuthzTrans trans, HistoryDAO historyDAO,PSInfo readPS  ) throws APIException, IOException {
        super(trans, historyDAO);
        setPs(this, readPS, "createPS");
    }


    public void setPs(LocateDAOImpl LocateDAOObj, PSInfo psInfoObj, String methodName) {
        Field nsDaoField;
        try {
            nsDaoField = CassDAOImpl.class.getDeclaredField(methodName);
        
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
        
            nsDaoField.set(LocateDAOObj, psInfoObj);
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
