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
package org.onap.aaf.auth.dao.cached;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.dao.CIDAO;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransImpl;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.LogTarget;


@RunWith(MockitoJUnitRunner.class) 
public class JU_CachedRoleDAO {

	@Mock
	RoleDAO dao;
	
	@Mock
	CIDAO<AuthzTrans> info;
	
	@Mock
	AuthzTransImpl trans;
	
	@Mock
	RoleDAO.Data data;
	
	@Mock
	PermDAO.Data permData;
	
	@Before
	public void setUp() throws Exception {
		initMocks(this);
		when(trans.debug()).thenReturn(new LogTarget() {
            
            @Override
            public void printf(String fmt, Object... vars) {}
            
            @Override
            public void log(Throwable e, Object... msgs) {
                e.getMessage();
                e.printStackTrace();
                msgs.toString();
                
            }
            
            @Override
            public void log(Object... msgs) {
            }
            
            @Override
            public boolean isLoggable() {
                
                return true;
            }
        });
	}
	
	@Test
	public void testReadNameSuccess() {
		CachedRoleDAO roleDaoObj =new CachedRoleDAO(dao,info, 10);//Mockito.mock(CachedRoleDAO.class);//
		Result<List<Data>> retVal1 = new Result<List<Data>>(null,1,"test4",new String[0]);
		roleDaoObj.invalidate("");
		Mockito.doReturn(retVal1).when(dao).readName(trans, "test4");
//		Mockito.when(roleDaoObj.get(Mockito.any(), Mockito.any(String.class), Mockito.any())).thenReturn(retVal1);
		Result<List<Data>> retVal = roleDaoObj.readName(trans, "test4");
//		System.out.println(retVal.status);
		//retVal.status = 0;
		assertEquals("1", Integer.toString(retVal.status));
	}	
	
	@Test
	public void testReadNameFailure() {
		CachedRoleDAO roleDaoObj =new CachedRoleDAO(dao,info, 10);//Mockito.mock(CachedRoleDAO.class);//
		Result<List<Data>> retVal1 = new Result<List<Data>>(null,0,"test3123",new String[0]);
		Mockito.doReturn(retVal1).when(dao).readName(trans, "test3");
//		Mockito.when(roleDaoObj.get(Mockito.any(), Mockito.any(String.class), Mockito.any())).thenReturn(retVal1);
		Result<List<Data>> retVal = roleDaoObj.readName(trans, "test3");
//		System.out.println(retVal.status);
		assertEquals("22", Integer.toString(retVal.status));
	}
	@Test
	public void testReadNSSuccess() {
		CachedRoleDAO roleDaoObj =new CachedRoleDAO(dao,info, 10);//Mockito.mock(CachedRoleDAO.class);//
		Result<List<Data>> retVal1 = new Result<List<Data>>(null,1,"test",new String[0]);
		Mockito.doReturn(retVal1).when(dao).readNS(trans, "");
//		Mockito.when(roleDaoObj.get(Mockito.any(), Mockito.any(String.class), Mockito.any())).thenReturn(retVal1);
		Result<List<Data>> retVal = roleDaoObj.readNS(trans, "");
//		System.out.println(retVal.status);
		assertEquals("1", Integer.toString(retVal.status));
	}	
	@Test
	public void testReadNSFailure() {
		CachedRoleDAO roleDaoObj =new CachedRoleDAO(dao,info, 10);//Mockito.mock(CachedRoleDAO.class);//
		Result<List<Data>> retVal1 = new Result<List<Data>>(null,0,"test1",new String[0]);
		Mockito.doReturn(retVal1).when(dao).readNS(trans, "");
//		Mockito.when(roleDaoObj.get(Mockito.any(), Mockito.any(String.class), Mockito.any())).thenReturn(retVal1);
		Result<List<Data>> retVal = roleDaoObj.readNS(trans, "");
//		System.out.println(retVal.status);
		assertEquals("22", Integer.toString(retVal.status));
	}
	
	@Test
	public void testReadChildren() {
		CachedRoleDAO roleDaoObj =new CachedRoleDAO(dao,info, 10);//Mockito.mock(CachedRoleDAO.class);//
		Result<List<Data>> retVal1 = new Result<List<Data>>(null,0,"test1",new String[0]);
		Mockito.doReturn(retVal1).when(dao).readChildren(trans, "","");
		Result<List<Data>> retVal = roleDaoObj.readChildren(trans, "", "");
		//System.out.println(retVal.status);
		assertEquals("0", Integer.toString(retVal.status));
	}
	
	@Test
	public void testAddPerm() {
		CachedRoleDAO roleDaoObj =new CachedRoleDAO(dao,info, 10);
		Result<Void> retVal1 = new Result<Void>(null,0,"testAddPerm",new String[0]);
		Mockito.doReturn(retVal1).when(info).touch(trans, null,null);
		Mockito.doReturn(retVal1).when(dao).addPerm(trans, data,permData);
		Result<Void> retVal = roleDaoObj.addPerm(trans, data, permData);
		assertEquals("testAddPerm", retVal.toString());
	}
	
	@Test
	public void testDelPerm() {
		CachedRoleDAO roleDaoObj =new CachedRoleDAO(dao,info, 10);
		Result<Void> retVal1 = new Result<Void>(null,0,"testAddPerm",new String[0]);
		Mockito.doReturn(retVal1).when(info).touch(trans, null,null);
		Mockito.doReturn(retVal1).when(dao).delPerm(trans, data,permData);
		Result<Void> retVal = roleDaoObj.delPerm(trans, data, permData);
		System.out.println(retVal);
		assertEquals("testAddPerm", retVal.toString());
	}

	@Test
	public void testAddDescription() {
		CachedRoleDAO roleDaoObj =new CachedRoleDAO(dao,info, 10);//Mockito.mock(CachedRoleDAO.class);//
		Result<Void> retVal1 = new Result<Void>(null,0,"test1",new String[0]);
		Mockito.doReturn(retVal1).when(dao).addDescription(trans, "","","");
		Result<Void> retVal = roleDaoObj.addDescription(trans, "", "","");
		//System.out.println(retVal.status);
		assertEquals("0", Integer.toString(retVal.status));
	}
}