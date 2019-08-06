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

package org.onap.aaf.auth.batch.approvalsets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.PreparedId;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

public class JU_ApprovalSetTest {
    
	@Mock
    AuthzTrans trans;
	@Mock
	Cluster cluster;
	@Mock
	PropAccess access;
	
	@Mock
	ApprovalSet actionObj;

	@Mock
	DataView dv;
    
    @Before
    public void setUp() throws APIException, IOException {
    	initMocks(this);
    	Session sessionObj=Mockito.mock(Session.class);
    	PreparedStatement psObj =Mockito.mock(PreparedStatement.class);
		try {
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).init();
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).warn();
			Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
			Mockito.doReturn("10").when(trans).getProperty(Config.AAF_USER_EXPIRES, Config.AAF_USER_EXPIRES_DEF);
			Mockito.doReturn(Mockito.mock(TimeTaken.class)).when(trans).start(Mockito.anyString(),Mockito.anyInt());
			Mockito.doReturn(sessionObj).when(cluster).connect("authz");
			Mockito.doReturn(psObj).when(sessionObj).prepare(Mockito.anyString());
			
			Mockito.doReturn(Mockito.mock(ColumnDefinitions.class)).when(psObj).getVariables();
			Mockito.doReturn(Mockito.mock(PreparedId.class)).when(psObj).getPreparedId();
			Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
			Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
			Define.set(access);
			GregorianCalendar start= new GregorianCalendar();
			actionObj = new ApprovalSet(start, "test", dv);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   }
    
    @Test
	public void testPuntDate() {
    	actionObj.write(trans);
    	ApprovalDAO.Data dataObj = new ApprovalDAO.Data();
    	
    	Result<FutureDAO.Data> rs1 = new Result<FutureDAO.Data>(null,0,"test",new Object[0]);
    	Mockito.doReturn(rs1).when(dv).insert(Mockito.any(AuthzTrans.class), Mockito.any(FutureDAO.Data.class));
    	Mockito.doReturn(rs1).when(dv).insert(Mockito.any(AuthzTrans.class), Mockito.any(ApprovalDAO.Data.class));
    	actionObj.ladd.add(dataObj);
    	Result<Void> retVal = actionObj.write(trans);
    	
    	rs1 = new Result<FutureDAO.Data>(null,1,"test",new Object[0]);
    	Mockito.doReturn(rs1).when(dv).insert(Mockito.any(AuthzTrans.class), Mockito.any(ApprovalDAO.Data.class));
    	retVal = actionObj.write(trans);
    	assertTrue("Security - test".equals(retVal.details));
    	
    	actionObj.ladd.add(dataObj);
    	retVal = actionObj.write(trans);
    	assertTrue(retVal.details.contains("Security - test"));

    	Mockito.doReturn(rs1).when(dv).insert(Mockito.any(AuthzTrans.class), Mockito.any(FutureDAO.Data.class));
    	retVal = actionObj.write(trans);
    	assertTrue(retVal.details.contains("Security - test"));
    	
    	actionObj.setConstruct(null);
    	actionObj.setExpires(new GregorianCalendar());
    	actionObj.setMemo("");
    	actionObj.ladd = null;
    	actionObj.write(trans);
	}
    
    @Test
	public void testHasApprovals() {
    	assertFalse(actionObj.hasApprovals());
    	
    	ApprovalDAO.Data dataObj = new ApprovalDAO.Data();
    	actionObj.ladd.add(dataObj);
    	assertTrue(actionObj.hasApprovals());
    }
    
    @Test
	public void testApprovers() {
    	Set<String> retVal = actionObj.approvers();
    	assertTrue(retVal.size() == 0);
    	
    	ApprovalDAO.Data dataObj = new ApprovalDAO.Data();
    	actionObj.ladd.add(dataObj);
    	retVal = actionObj.approvers();
    	assertTrue(retVal.size() == 1);
    	
    }
}
