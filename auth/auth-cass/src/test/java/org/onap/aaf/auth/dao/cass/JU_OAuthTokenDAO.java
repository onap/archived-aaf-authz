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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jetty.util.ConcurrentHashSet;
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
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;

public class JU_OAuthTokenDAO {

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
        OAuthTokenDAO daoObj = new OAuthTokenDAO(trans, cluster, "test");
//        daoObj.
    }
    @Test
    public void testReadByUser() {
        OAuthTokenDAO daoObj = new OAuthTokenDAO(trans, cluster, "test");
    
        PSInfo psObj = Mockito.mock(PSInfo.class);
        setPsByStartAndTarget(daoObj, psObj, "psByUser");
    
        Result<List<OAuthTokenDAO.Data>>  rs1 = new Result<List<OAuthTokenDAO.Data>>(null,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(psObj).read(trans, "OAuthTokenDAO READ", new Object[]{"test"});
    
        daoObj.readByUser(trans, "test");
    }

    public void setPsByStartAndTarget(OAuthTokenDAO OAuthTokenDAOObj, PSInfo psInfoObj, String fieldName) {
        Field nsDaoField;
        try {
            nsDaoField = OAuthTokenDAO.class.getDeclaredField(fieldName);
        
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
        
            nsDaoField.set(OAuthTokenDAOObj, psInfoObj);
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
        Mockito.doReturn(tt).when(trans).start("OAuthTokenDAO CREATE", Env.REMOTE);
        Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on OAuthTokenDAO", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("DELETE Future",Env.REMOTE);
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
        Mockito.doNothing().when(tt).done();
        OAuthTokenDAO.Data data  = new OAuthTokenDAO.Data();

        OAuthTokenDAO daoObj = null;
        daoObj = new OAuthTokenDAO(trans, cluster, "test" );
        daoObj.wasModified(trans, CRUD.create, data, new String[] {"test"});
    
    }

    @Test
    public void testSecondConstructor() {
        AbsCassDAO absCassDAO = Mockito.mock(AbsCassDAO.class);

        OAuthTokenDAO daoObj = new OAuthTokenDAO(trans, absCassDAO);
    
    }

    @Test
    public void testData(){
        OAuthTokenDAO.Data data = new OAuthTokenDAO.Data();
        data.scopes = null;
        data.scopes(true);

        data.scopes = new HashSet<>();
        data.scopes(true);

        data.scopes(false);
        data.scopes = new ConcurrentHashSet<>();
        data.scopes(true);
    
        data.expires = new Date();
        data.user="test";
        data.id="id";
        data.toString();
    
        data.active=true;
        data.toString();
    
        try {
            ByteBuffer bb = data.bytify();
            data.reconstitute(bb);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void testOAuthLoader(){
        Class<?> innerClass = null;
        Class<?>[] innerClassArr = OAuthTokenDAO.class.getDeclaredClasses();
        for(Class indCls:innerClassArr) {
            if(indCls.getName().contains("OAuthLoader")) {
                innerClass = indCls;
                break;
            }
        }
    
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
    
        try {
        
            Object obj = constructor.newInstance(1);
            Method innnerClassMtd;
            
            OAuthTokenDAO.Data data  = new OAuthTokenDAO.Data();
            Row row = Mockito.mock(Row.class);
            ByteBuffer bbObj = ByteBuffer.allocateDirect(10);
            bbObj.limit(7);
            bbObj.put(0, new Byte("0"));
            bbObj.put(1, new Byte("1"));
            bbObj.put(2, new Byte("2"));
            Mockito.doReturn(bbObj).when(row).getBytesUnsafe(1);
        
            innnerClassMtd = innerClass.getMethod("load", new Class[] {OAuthTokenDAO.Data.class, Row.class});
            innnerClassMtd.invoke(obj, new Object[] {data, row});
        
            innnerClassMtd = innerClass.getDeclaredMethod("key", new Class[] {OAuthTokenDAO.Data.class, Integer.TYPE, Object[].class });
            innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test"} });
//        
            innnerClassMtd = innerClass.getDeclaredMethod("body", new Class[] {OAuthTokenDAO.Data.class, Integer.TYPE, Object[].class });
            innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test","test","test","test","test","test","test","test","test","test"} });
        
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            innnerClassMtd = innerClass.getDeclaredMethod("marshal", new Class[] {OAuthTokenDAO.Data.class, DataOutputStream.class });
            innnerClassMtd.invoke(obj, new Object[] {data, dos });

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            DataInputStream dis = new DataInputStream(bais);
            innnerClassMtd = innerClass.getDeclaredMethod("unmarshal", new Class[] {OAuthTokenDAO.Data.class, DataInputStream.class });
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

}

class OAuthTokenDAOImpl extends OAuthTokenDAO{


    public OAuthTokenDAOImpl(AuthzTrans trans, HistoryDAO historyDAO,PSInfo readPS  ) throws APIException, IOException {
        super(trans, historyDAO);
        setPs(this, readPS, "createPS");
    }


    public void setPs(OAuthTokenDAOImpl OAuthTokenDAOObj, PSInfo psInfoObj, String methodName) {
        Field nsDaoField;
        try {
            nsDaoField = CassDAOImpl.class.getDeclaredField(methodName);
        
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
        
            nsDaoField.set(OAuthTokenDAOObj, psInfoObj);
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
