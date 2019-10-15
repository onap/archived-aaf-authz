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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.AbsCassDAO;
import org.onap.aaf.auth.dao.AbsCassDAO.CRUD;
import org.onap.aaf.auth.dao.AbsCassDAO.PSInfo;
import org.onap.aaf.auth.dao.cass.ApprovalDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ExecutionInfo;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.util.concurrent.ListenableFuture;

public class JU_ApprovalDAO {

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
    public void testInit() {
        TimeTaken tt = Mockito.mock(TimeTaken.class);
        Mockito.doReturn(tt).when(trans).start("ApprovalDAO CREATE", Env.REMOTE);
        Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on ApprovalDAO", Env.SUB);
        Mockito.doNothing().when(tt).done();
        ApprovalDAO.Data data  = new ApprovalDAO.Data();
        PSInfo createPS = Mockito.mock(PSInfo.class);
        Result<ResultSet> rs = new Result<ResultSet>(null,0,"test",new String[0]);
        Mockito.doReturn(rs).when(createPS).exec(trans, "ApprovalDAOImpl CREATE", data);
    
        ApprovalDAOImpl daoObj = new ApprovalDAOImpl(trans, cluster, "test",data, createPS);
//        data.id
        Result<Data> retVal = daoObj.create(trans, data);
        assertTrue(retVal.status == 0);
    
        rs = new Result<ResultSet>(null,1,"test",new String[0]);
        Mockito.doReturn(rs).when(createPS).exec(trans, "ApprovalDAOImpl CREATE", data);
        retVal = daoObj.create(trans, data);
        assertTrue(retVal.status == 1);
    
        Result<List<ApprovalDAO.Data>> rs1 = new Result<List<ApprovalDAO.Data>>(null,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(createPS).read(trans, "ApprovalDAOImpl CREATE", new Object[]{"testUser"});
        Result<List<ApprovalDAO.Data>> retVal1 = daoObj.readByUser(trans, "testUser");
        assertNull(retVal1);
    
        Mockito.doReturn(rs1).when(createPS).read(trans, "ApprovalDAOImpl CREATE", new Object[]{"testApprover"});
        retVal1 = daoObj.readByApprover(trans, "testApprover");
        assertNull(retVal1);
    
        Mockito.doReturn(rs1).when(createPS).read(trans, "ApprovalDAOImpl CREATE", new Object[]{new UUID(0, 0)});
        retVal1 = daoObj.readByTicket(trans, new UUID(0, 0));
        assertNull(retVal1);
    
        Mockito.doReturn(rs1).when(createPS).read(trans, "ApprovalDAOImpl CREATE", new Object[]{"testStatus"});
        retVal1 = daoObj.readByStatus(trans, "testStatus");
        assertNull(retVal1);
    }

    @Test
    public void testDelete() {
        TimeTaken tt = Mockito.mock(TimeTaken.class);
        Mockito.doReturn(tt).when(trans).start("ApprovalDAO CREATE", Env.REMOTE);
        Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on ApprovalDAO", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("DELETE APPROVAL",Env.REMOTE);
        Mockito.doNothing().when(tt).done();
        ApprovalDAO.Data data  = new ApprovalDAO.Data();

        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        Result<ResultSet> rs1 = new Result<ResultSet>(null,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
    
        PSInfo createPS = Mockito.mock(PSInfo.class);
        ResultSet rsObj = new ResultSetImpl();
        Result<ResultSet> rs = new Result<ResultSet>(rsObj,0,"test",new String[0]);
        Mockito.doReturn(rs).when(createPS).exec(trans, "ApprovalDAOImpl READ", data);
        Mockito.doReturn(rs).when(createPS).exec(trans, "ApprovalDAOImpl DELETE", data);
    
        ApprovalDAOImpl daoObj = new ApprovalDAOImpl(trans, cluster, "test", createPS, historyDAO);
//        data.id
        Result<Void> retVal = daoObj.delete(trans, data, true);
        assertTrue(retVal.status == 0);

        rs = new Result<ResultSet>(rsObj,1,"test",new String[0]);
        Mockito.doReturn(rs).when(createPS).exec(trans, "ApprovalDAOImpl READ", data);
        retVal = daoObj.delete(trans, data, true);
        assertTrue(retVal.status == 1);
    
        data.status="approved";
        data.memo="test";
        retVal = daoObj.delete(trans, data, false);
        assertTrue(retVal.status == 0);
    
        daoObj.async(true);
        data.status="denied";
        retVal = daoObj.delete(trans, data, false);
        assertTrue(retVal.status == 0);

        data.status=null;
        retVal = daoObj.delete(trans, data, false);
    }

    @Test
    public void testWasMOdified() {
        TimeTaken tt = Mockito.mock(TimeTaken.class);
        Mockito.doReturn(tt).when(trans).start("ApprovalDAO CREATE", Env.REMOTE);
        Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on ApprovalDAO", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("DELETE APPROVAL",Env.REMOTE);
        Mockito.doNothing().when(tt).done();
        ApprovalDAO.Data data  = new ApprovalDAO.Data();
        PSInfo createPS = Mockito.mock(PSInfo.class);
    
        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
        Result<ResultSet> rs1 = new Result<ResultSet>(null,0,"test",new String[0]);
        Mockito.doReturn(rs1).when(historyDAO).create(Mockito.any(), Mockito.any());
    
        ApprovalDAOImpl daoObj = new ApprovalDAOImpl(trans, cluster, "test", createPS, historyDAO);
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
        TimeTaken tt = Mockito.mock(TimeTaken.class);
        Mockito.doReturn(tt).when(trans).start("ApprovalDAO CREATE", Env.REMOTE);
        Mockito.doReturn(tt).when(trans).start("Clear Reset Deque", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("New Cassandra Session", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("Preparing PSInfo CREATE on ApprovalDAO", Env.SUB);
        Mockito.doReturn(tt).when(trans).start("DELETE APPROVAL",Env.REMOTE);
        Mockito.doNothing().when(tt).done();
        ApprovalDAO.Data data  = new ApprovalDAO.Data();
        HistoryDAO historyDAO = Mockito.mock(HistoryDAO.class);
    
        ApprovalDAO daoObj = new ApprovalDAO(trans, historyDAO);
    }
}

class ResultSetImpl implements ResultSet{

    @Override
    public boolean isExhausted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isFullyFetched() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getAvailableWithoutFetching() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ListenableFuture<ResultSet> fetchMoreResults() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Row> all() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Iterator<Row> iterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExecutionInfo getExecutionInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ExecutionInfo> getAllExecutionInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Row one() {
        // TODO Auto-generated method stub
        Row rowObj = Mockito.mock(Row.class);
        Mockito.doReturn(Mockito.mock(ColumnDefinitions.class)).when(rowObj).getColumnDefinitions();
        return rowObj;
    }

    @Override
    public ColumnDefinitions getColumnDefinitions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean wasApplied() {
        // TODO Auto-generated method stub
        return false;
    }

}

class ApprovalDAOImpl extends ApprovalDAO{

    public ApprovalDAOImpl(AuthzTrans trans, Cluster cluster, String keyspace,ApprovalDAO.Data data,PSInfo createPS  ) {
        super(trans, cluster, keyspace);
        this.createPS = createPS;
        setPs(this, createPS, "psByUser");
        setPs(this, createPS, "psByApprover");
        setPs(this, createPS, "psByTicket");
        setPs(this, createPS, "psByStatus");
    }

    public ApprovalDAOImpl(AuthzTrans trans, Cluster cluster, String keyspace,PSInfo readPS  ) {
        super(trans, cluster, keyspace);
        this.readPS = readPS;
    }

    public ApprovalDAOImpl(AuthzTrans trans, Cluster cluster, String keyspace,PSInfo readPS, HistoryDAO historyDAO  ) {
        super(trans, cluster, keyspace);
        this.deletePS = readPS;
        this.readPS = readPS;
        setHistoryDao(this, historyDAO);
        setSession(this, Mockito.mock(Session.class));
    }

    public void setPs(ApprovalDAOImpl approvalDaoObj, PSInfo psInfoObj, String methodName) {
        Field nsDaoField;
        try {
            nsDaoField = ApprovalDAO.class.getDeclaredField(methodName);
        
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
        
            nsDaoField.set(approvalDaoObj, psInfoObj);
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

    public void setHistoryDao(ApprovalDAOImpl approvalDaoObj, HistoryDAO historyDAO) {
        Field nsDaoField;
        try {
            nsDaoField = ApprovalDAO.class.getDeclaredField("historyDAO");
        
            nsDaoField.setAccessible(true);
            // remove final modifier from field
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
//            modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
        
            nsDaoField.set(approvalDaoObj, historyDAO);
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
    public void setSession(ApprovalDAOImpl approvalDaoObj, Session session) {
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
