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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.AbsCassDAO;
import org.onap.aaf.auth.dao.AbsCassDAO.PSInfo;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;

public class JU_DelegateDAO {

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
        DelegateDAO daoObj = new DelegateDAO(trans, cluster, "test");
//        daoObj.
    }
    @Test
    public void testReadByDelegate() {
        DelegateDAO daoObj = new DelegateDAO(trans, cluster, "test");
        
        PSInfo psObj = Mockito.mock(PSInfo.class);
        setPsDelegate(daoObj, psObj, "psByDelegate");
        
        Result<List<DelegateDAO.Data>>  rs1 = new Result<List<DelegateDAO.Data>>(null,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(psObj).read(trans, "DelegateDAO READ", new Object[]{"test"});
        
        daoObj.readByDelegate(trans, "test");
    }
    
    public void setPsDelegate(DelegateDAO delegateDAOObj, PSInfo psInfoObj, String fieldName) {
        Field nsDaoField;
        try {
            nsDaoField = DelegateDAO.class.getDeclaredField(fieldName);
            
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
            
            nsDaoField.set(delegateDAOObj, psInfoObj);
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
        AbsCassDAO absDAO = Mockito.mock(AbsCassDAO.class);

        DelegateDAO daoObj = new DelegateDAO(trans, absDAO);
        
    }

    @Test
    public void testDelegateLoader(){
        Class<?> innerClass = null;
        Class<?>[] innerClassArr = DelegateDAO.class.getDeclaredClasses();
        for(Class indCls:innerClassArr) {
            if(indCls.getName().contains("DelegateLoader")) {
                innerClass = indCls;
                break;
            }
        }
        
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        try {
            Object obj = constructor.newInstance(1);
            Method innnerClassMtd;
                
            DelegateDAO.Data data  = new DelegateDAO.Data();
            Row row = Mockito.mock(Row.class);
            ByteBuffer bbObj = ByteBuffer.allocateDirect(10);
            bbObj.limit(7);
            bbObj.put(0, new Byte("0"));
            bbObj.put(1, new Byte("1"));
            bbObj.put(2, new Byte("2"));
            Mockito.doReturn(bbObj).when(row).getBytesUnsafe(1);
            
            innnerClassMtd = innerClass.getMethod("load", new Class[] {DelegateDAO.Data.class, Row.class});
            innnerClassMtd.invoke(obj, new Object[] {data, row});
            
            innnerClassMtd = innerClass.getDeclaredMethod("key", new Class[] {DelegateDAO.Data.class, Integer.TYPE, Object[].class });
            innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test"} });
//            
            innnerClassMtd = innerClass.getDeclaredMethod("body", new Class[] {DelegateDAO.Data.class, Integer.TYPE, Object[].class });
            innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test","test","test","test","test","test","test","test","test"} });
            
//            DataOutputStream dos = new DataOutputStream(new FileOutputStream("JU_DelegateDAOTest.java"));
//            innnerClassMtd = innerClass.getDeclaredMethod("marshal", new Class[] {DelegateDAO.Data.class, DataOutputStream.class });
//            innnerClassMtd.invoke(obj, new Object[] {data, dos });

//            DataInputStream dis = new DataInputStream(new FileInputStream("JU_DelegateDAOTest.java"));
//            innnerClassMtd = innerClass.getDeclaredMethod("unmarshal", new Class[] {DelegateDAO.Data.class, DataInputStream.class });
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
    
    @Test
    public void testData(){
        DelegateDAO.Data data  = new DelegateDAO.Data();
        data.user="user";
        data.delegate="delegate";
        data.expires = new Date();
        try {
            data.bytify();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
