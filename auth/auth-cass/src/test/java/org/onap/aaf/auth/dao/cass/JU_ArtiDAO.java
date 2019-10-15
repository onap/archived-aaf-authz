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

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.AbsCassDAO;
import org.onap.aaf.auth.dao.AbsCassDAO.CRUD;
import org.onap.aaf.auth.dao.AbsCassDAO.PSInfo;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class JU_ArtiDAO {

    @Mock
    AuthzTrans trans;
    @Mock
    Cluster cluster;

    @Before
    public void setUp() throws APIException, IOException {
        initMocks(this);
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).warn();
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).info();
    }

    @Test
    public void testReadByMechID() {
        TimeTaken tt = Mockito.mock(TimeTaken.class);
        Mockito.doReturn(tt).when(trans).start("ArtiDAO CREATE", Env.REMOTE);
        Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on ArtiDAO", Env.SUB);
        Mockito.doNothing().when(tt).done();

        PSInfo psByMechIdObj = Mockito.mock(PSInfo.class);
        Result<ResultSet> rs = new Result<ResultSet>(null,0,"test",new String[0]);
//        Mockito.doReturn(rs).when(createPS).exec(trans, "ArtiDAOImpl CREATE", data);

        ArtiDAOImpl daoObj = new ArtiDAOImpl(trans, cluster, "test", psByMechIdObj);

        Result<List<ArtiDAO.Data>> rs1 = new Result<List<ArtiDAO.Data>>(null,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(psByMechIdObj).read(trans, "ArtiDAOImpl READ", new Object[]{"testMechId"});
        daoObj.readByMechID(trans, "testMechId");

        rs1 = new Result<List<ArtiDAO.Data>>(null,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(psByMechIdObj).read(trans, "ArtiDAOImpl READ", new Object[]{"testMachine"});
        daoObj.readByMachine(trans, "testMachine");

        rs1 = new Result<List<ArtiDAO.Data>>(null,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(psByMechIdObj).read(trans, "ArtiDAOImpl READ", new Object[]{"testNs"});
        daoObj.readByNs(trans, "testNs");
    }

    @Test
    public void testWasMOdified() {
        TimeTaken tt = Mockito.mock(TimeTaken.class);
        Mockito.doReturn(tt).when(trans).start("ArtiDAO CREATE", Env.REMOTE);
        Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on ArtiDAO", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("DELETE APPROVAL",Env.REMOTE);
        Mockito.doNothing().when(tt).done();
        ArtiDAO.Data data  = new ArtiDAO.Data();
        PSInfo createPS = Mockito.mock(PSInfo.class);

        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        Result<ResultSet> rs1 = new Result<ResultSet>(null,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());

        ArtiDAOImpl daoObj = new ArtiDAOImpl(trans, cluster, "test", createPS, historyDAO);
        daoObj.wasModified(trans, CRUD.create, data, new String[] {"test"});

        daoObj.wasModified(trans, CRUD.create, data, new String[] {});
        daoObj.wasModified(trans, CRUD.create, data, new String[] {null});
        daoObj.wasModified(trans, CRUD.create, data, new String[] {"test",null});
        daoObj.wasModified(trans, CRUD.create, data, new String[] {"test","test"});

        rs1 = new Result<ResultSet>(null,1,"test",new String[0]);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
        daoObj.wasModified(trans, CRUD.create, data, new String[] {"test","test"});

        data.type(true);
        daoObj.wasModified(trans, CRUD.delete, data, new String[] {"test","test"});

    }
    @Test
    public void testData(){
        ArtiDAO.Data data  = new ArtiDAO.Data();
        data.type(true);
        data.type(false);

        data.sans(true);
        data.sans(false);
        data.sans = new TreeSet();
        data.sans(false);
        data.sans(true);

        data.expires = new Date();
        data.toString();
    }

    @Test
    public void testArtifactLoader(){
        ArtiDAO daoObj = new ArtiDAO(trans, cluster, "test");
        Class<?> innerClass = ArtiDAO.class.getDeclaredClasses()[0];
        Constructor<?> constructor = innerClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        try {
            Object obj = constructor.newInstance(10);
            Method innnerClassMtd;

            ArtiDAO.Data data  = new ArtiDAO.Data();
            Row row = Mockito.mock(Row.class);
            innnerClassMtd = innerClass.getMethod("load", new Class[] {ArtiDAO.Data.class, Row.class});
            innnerClassMtd.invoke(obj, new Object[] {data, row});

            innnerClassMtd = innerClass.getDeclaredMethod("key", new Class[] {ArtiDAO.Data.class, Integer.TYPE, Object[].class });
            innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test"} });

            innnerClassMtd = innerClass.getDeclaredMethod("body", new Class[] {ArtiDAO.Data.class, Integer.TYPE, Object[].class });
            innnerClassMtd.invoke(obj, new Object[] {data, 1, new Object[] {"test","test","test","test","test","test","test","test","test","test","test"} });

//            DataInputStream in  = Mockito.mock(DataInputStream.class);
////            Mockito.doReturn(100).when(in).read();
////            Mockito.doReturn(100).when(in).readInt();
//            innnerClassMtd = innerClass.getDeclaredMethod("unmarshal", new Class[] {ArtiDAO.Data.class, DataInputStream.class });
//            innnerClassMtd.invoke(obj, new Object[] {data, in});
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
    public void testSecondConstructor() {
        TimeTaken tt = Mockito.mock(TimeTaken.class);
        Mockito.doReturn(tt).when(trans).start("ArtiDAO CREATE", Env.REMOTE);
        Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on ArtiDAO", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("DELETE APPROVAL",Env.REMOTE);
        Mockito.doNothing().when(tt).done();
        ArtiDAO.Data data  = new ArtiDAO.Data();
        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);

        ArtiDAO daoObj = new ArtiDAO(trans, historyDAO, Mockito.mock(CacheInfoDAO.class));
    }

}


class ArtiDAOImpl extends ArtiDAO{

    public ArtiDAOImpl(AuthzTrans trans, Cluster cluster, String keyspace, PSInfo createPS  ) {
        super(trans, cluster, keyspace);
        this.createPS = createPS;
        setPs(this, createPS, "psByMechID");
        setPs(this, createPS, "psByMachine");
        setPs(this, createPS, "psByNs");
    }

    public ArtiDAOImpl(AuthzTrans trans, Cluster cluster, String keyspace,PSInfo readPS, HistoryDAO historyDAO  ) {
        super(trans, cluster, keyspace);
        this.deletePS = readPS;
        this.readPS = readPS;
        setHistoryDao(this, historyDAO);
        setSession(this, Mockito.mock(Session.class));
    }

    public void setPs(ArtiDAOImpl ArtiDAOObj, PSInfo psInfoObj, String methodName) {
        Field nsDaoField;
        try {
            nsDaoField = ArtiDAO.class.getDeclaredField(methodName);

            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);

            nsDaoField.set(ArtiDAOObj, psInfoObj);
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

    public void setHistoryDao(ArtiDAOImpl ArtiDAOObj, HistoryDAO historyDAO) {
        Field nsDaoField;
        try {
            nsDaoField = ArtiDAO.class.getDeclaredField("historyDAO");

            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);

            nsDaoField.set(ArtiDAOObj, historyDAO);
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
    public void setSession(ArtiDAOImpl ArtiDAOObj, Session session) {
        Field nsDaoField;
        try {
            nsDaoField = AbsCassDAO.class.getDeclaredField("session");

            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);

            nsDaoField.set(ArtiDAOObj, session);
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
