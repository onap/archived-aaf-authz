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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.CIDAO;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.PermDAO.Data;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransImpl;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.LogTarget;

public class JU_CachedPermDAOTest {

    @Mock
    private CIDAO<AuthzTrans> info;
    @Mock
    private PermDAO dao;
    
    @Mock
    RoleDAO.Data role;
    
    @Mock
    private PermDAO.Data perm;
    
    @Mock
    private AuthzTrans trans;
    @Mock
    private Result<List<PermDAO.Data>> value;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        when(dao.readNS(trans, "ns")).thenReturn(value);
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
    public void testReadNS() {
        when(value.isOKhasData()).thenReturn(true);
        when(value.isOK()).thenReturn(false);
        CachedPermDAO ccDao = new CachedPermDAO(dao, info, 100l);

        Result<List<Data>> result = ccDao.readNS(trans, "ns");

        assertEquals(result, value);

        when(value.isOKhasData()).thenReturn(false);

        result = ccDao.readNS(trans, "ns");

        assertEquals(result.status, Status.ERR_PermissionNotFound);

        ccDao.readChildren(trans, "ns", "type");

        verify(dao).readChildren(trans, "ns", "type");
    }
    
    @Test
    public void testReadByTypeSuccess() {
        CachedPermDAO roleDaoObj =new CachedPermDAO(dao,info, 10);//Mockito.mock(CachedRoleDAO.class);//
        Result<List<Data>> retVal1 = new Result<List<Data>>(null,1,"test4",new String[0]);
        Mockito.doReturn(retVal1).when(dao).readByType(trans, "test4","");
        Result<List<Data>> retVal = roleDaoObj.readByType(trans, "test4","");
//        System.out.println(retVal.status);
        //retVal.status = 0;
        assertEquals("1", Integer.toString(retVal.status));
    }    
    
    @Test
    public void testReadByTypeFailure() {
        CachedPermDAO roleDaoObj =new CachedPermDAO(dao,info, 10);//Mockito.mock(CachedRoleDAO.class);//
        Result<List<Data>> retVal1 = new Result<List<Data>>(null,0,"test3123",new String[0]);
        Mockito.doReturn(retVal1).when(dao).readByType(trans, "test3","");
        Result<List<Data>> retVal = roleDaoObj.readByType(trans, "test3","");
        //System.out.println(retVal.status);
        assertEquals("23", Integer.toString(retVal.status));
    }
    
    @Test
    public void testAddRole() {
        CachedPermDAO roleDaoObj =new CachedPermDAO(dao,info, 10);
        Result<Void> retVal1 = new Result<Void>(null,0,"testAddRole",new String[0]);
        Mockito.doReturn(retVal1).when(info).touch(trans, null,null);
        Mockito.doReturn(retVal1).when(dao).addRole(trans, perm,null);
        Result<Void> retVal = roleDaoObj.addRole(trans, perm, role);
//        System.out.println("ret value is::"+retVal);
        assertEquals("testAddRole", retVal.toString());
    }
    
    @Test
    public void testDelRole() {
        CachedPermDAO roleDaoObj =new CachedPermDAO(dao,info, 10);
        Result<Void> retVal1 = new Result<Void>(null,0,"testAddRole",new String[0]);
        Mockito.doReturn(retVal1).when(info).touch(trans, null,null);
        Mockito.doReturn(retVal1).when(dao).delRole(trans, perm,null);
        Result<Void> retVal = roleDaoObj.delRole(trans, perm, role);
//        System.out.println(retVal);
        assertEquals("testAddRole", retVal.toString());
    }

    @Test
    public void testAddDescription() {
        CachedPermDAO roleDaoObj =new CachedPermDAO(dao,info, 10);//Mockito.mock(CachedRoleDAO.class);//
        Result<Void> retVal1 = new Result<Void>(null,0,"test1",new String[0]);
        Mockito.doReturn(retVal1).when(dao).addDescription(trans, "","","","","");
        Result<Void> retVal = roleDaoObj.addDescription(trans, "", "","","","");
        //System.out.println(retVal.status);
        assertEquals("0", Integer.toString(retVal.status));
    }

}
