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
package org.onap.aaf.auth.dao.hl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.cached.CachedRoleDAO;
import org.onap.aaf.auth.dao.cached.CachedUserRoleDAO;
import org.onap.aaf.auth.dao.cass.PermDAO.Data;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.misc.env.LogTarget;


@RunWith(MockitoJUnitRunner.class) 
public class JU_PermLookup {



    @Mock
    AuthzTrans trans;

    @Mock
    Question q;

    @Mock
    Access access;

    @Mock
    CachedRoleDAO roleDAO;

    @Mock
    CachedUserRoleDAO userRoleDAO;

    Function f;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        Mockito.doReturn(userRoleDAO).when(q).userRoleDAO();
        Mockito.doReturn(roleDAO).when(q).roleDAO();
    
        try {
            Mockito.doReturn("0.0").when(access).getProperty("aaf_root_ns","org.osaaf.aaf");
            Mockito.doReturn(new Properties()).when(access).getProperties();
            Define.set(access);
        
            when(trans.error()).thenReturn(new LogTarget() {
            
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
        } catch (CadiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        f =new Function(trans, q);
    }


//    @Test
//    public void testPerm() {
//    
//        PermLookup cassExecutorObj =PermLookup.get(trans, q,"");
//    
////        System.out.println(cassExecutorObj);
////        assertFalse(retVal);
//    }

    @Test
    public void testGetUserRole() {
        @SuppressWarnings("unchecked")
        Result<List<UserRoleDAO.Data>> retVal1 = Mockito.mock(Result.class);
        retVal1.value = new ArrayList<UserRoleDAO.Data>();
        UserRoleDAO.Data dataObj = Mockito.mock( UserRoleDAO.Data.class);
    
        dataObj.expires = new Date();
    
        retVal1.value.add(dataObj);
        Mockito.doReturn(true).when(retVal1).isOKhasData();
        Mockito.doReturn(retVal1).when(userRoleDAO).readByUser(trans,"");
        PermLookup cassExecutorObj =PermLookup.get(trans, q,"");
        Result<List<UserRoleDAO.Data>> userRoles = cassExecutorObj.getUserRoles();
    
        //System.out.println(""+userRoles.status);
        assertEquals(24,userRoles.status);
    }

    @Test
    public void testGetUserRolesFirstIf() {
        @SuppressWarnings("unchecked")
        Result<List<UserRoleDAO.Data>> retVal1 = Mockito.mock(Result.class);
        retVal1.value = new ArrayList<UserRoleDAO.Data>();
            
        Mockito.doReturn(false).when(retVal1).isOKhasData();
        Mockito.doReturn(retVal1).when(userRoleDAO).readByUser(trans,"");
        PermLookup cassExecutorObj =PermLookup.get(trans, q,"");
        Result<List<UserRoleDAO.Data>> userRoles = cassExecutorObj.getUserRoles();
    
//        System.out.println("output is"+userRoles.status);
        assertEquals(0,userRoles.status);
    }

    @Test
    public void testGetUserRolesSecondIf() {
        @SuppressWarnings("unchecked")
        Result<List<UserRoleDAO.Data>> retVal1 = Mockito.mock(Result.class);
        retVal1.value = new ArrayList<UserRoleDAO.Data>();
        UserRoleDAO.Data dataObj = Mockito.mock( UserRoleDAO.Data.class);
    
        Date dt = new Date();
        Calendar c = Calendar.getInstance(); 
        c.setTime(dt); 
        c.add(Calendar.DATE, 1);
        dataObj.expires = c.getTime();
    
        retVal1.value.add(dataObj);
        Mockito.doReturn(true).when(retVal1).isOKhasData();
        Mockito.doReturn(retVal1).when(userRoleDAO).readByUser(trans,"");
        PermLookup cassExecutorObj =PermLookup.get(trans, q,"");
        Result<List<UserRoleDAO.Data>> userRoles = cassExecutorObj.getUserRoles();
    
        //System.out.println(userRoles.status);
        assertEquals("Success",userRoles.details);
        Result<List<UserRoleDAO.Data>> userRoles1 = cassExecutorObj.getUserRoles();

        //System.out.println(userRoles1.status);
        assertEquals(0, userRoles1.status);
    }

    @Test
    public void testGetRole() {
        @SuppressWarnings("unchecked")
        Result<List<UserRoleDAO.Data>> retVal1 = Mockito.mock(Result.class);
        retVal1.value = new ArrayList<UserRoleDAO.Data>();
        UserRoleDAO.Data dataObj = Mockito.mock( UserRoleDAO.Data.class);
    
        dataObj.expires = new Date();
    
        retVal1.value.add(dataObj);
        Mockito.doReturn(false).when(retVal1).isOKhasData();
        Mockito.doReturn(true).when(retVal1).isOK();
        Mockito.doReturn(retVal1).when(userRoleDAO).readByUser(trans,"");
        PermLookup cassExecutorObj =PermLookup.get(trans, q,"");
        Result<List<RoleDAO.Data>> userRoles = cassExecutorObj.getRoles();
    
        //System.out.println(""+userRoles.status);
        assertEquals(4,userRoles.status);
    }

    @Test
    public void testGetRoleFirstIf() {
        @SuppressWarnings("unchecked")
        Result<List<UserRoleDAO.Data>> retVal1 = Mockito.mock(Result.class);
        retVal1.value = new ArrayList<UserRoleDAO.Data>();
        UserRoleDAO.Data dataObj = Mockito.mock( UserRoleDAO.Data.class);
    
        dataObj.expires = new Date();
        dataObj.ns="";
        dataObj.rname="";
    
        retVal1.value.add(dataObj);
        Mockito.doReturn(false).when(retVal1).isOKhasData();
        Mockito.doReturn(false).when(retVal1).isOK();
        Mockito.doReturn(retVal1).when(userRoleDAO).readByUser(trans,"");
        Mockito.doReturn(retVal1).when(roleDAO).read(trans,"","");
        PermLookup cassExecutorObj =PermLookup.get(trans, q,"");
        Result<List<RoleDAO.Data>> userRoles = cassExecutorObj.getRoles();
    
//        System.out.println(""+userRoles.status);
        assertEquals(0,userRoles.status);
    }

    @Test
    public void testGetRoleSecondIf() {
        @SuppressWarnings("unchecked")
        Result<List<UserRoleDAO.Data>> retVal1 = Mockito.mock(Result.class);
        retVal1.value = new ArrayList<UserRoleDAO.Data>();
        UserRoleDAO.Data dataObj = Mockito.mock( UserRoleDAO.Data.class);
    
        dataObj.expires = new Date();
        dataObj.ns="";
        dataObj.rname="";
    
        retVal1.value.add(dataObj);
        Mockito.doReturn(false).when(retVal1).isOKhasData();
        Mockito.doReturn(true).when(retVal1).isOK();
        Mockito.doReturn(retVal1).when(userRoleDAO).readByUser(trans,"");
        Mockito.doReturn(retVal1).when(roleDAO).read(trans,"","");
        PermLookup cassExecutorObj =PermLookup.get(trans, q,"");
        Result<List<RoleDAO.Data>> userRoles = cassExecutorObj.getRoles();
        userRoles = cassExecutorObj.getRoles();
    
//        System.out.println(""+userRoles.status);
        assertEquals(0,userRoles.status);
    }
    @Test
    public void testGetPerms() {
        @SuppressWarnings("unchecked")
        Result<List<UserRoleDAO.Data>> retVal1 = Mockito.mock(Result.class);
        retVal1.value = new ArrayList<UserRoleDAO.Data>();
        Mockito.doReturn(false).when(retVal1).isOKhasData();
        Mockito.doReturn(true).when(retVal1).isOK();
        Mockito.doReturn(retVal1).when(userRoleDAO).readByUser(trans,"");
        PermLookup cassExecutorObj =PermLookup.get(trans, q,"");
        Result<Set<String>> userRoles = cassExecutorObj.getPermNames();
        userRoles = cassExecutorObj.getPermNames();
    
        //System.out.println(""+userRoles.status);
        assertEquals(0,userRoles.status);
    }
    @Test
    public void testGetPermsRrldOk() {
        @SuppressWarnings("unchecked")
        Result<List<UserRoleDAO.Data>> retVal1 = Mockito.mock(Result.class);
        retVal1.value = new ArrayList<UserRoleDAO.Data>();
        UserRoleDAO.Data dataObj = Mockito.mock( UserRoleDAO.Data.class);
    
        dataObj.expires = new Date();
    
        retVal1.value.add(dataObj);
        Mockito.doReturn(false).when(retVal1).isOKhasData();
        Mockito.doReturn(true).when(retVal1).isOK();
        Mockito.doReturn(retVal1).when(userRoleDAO).readByUser(trans,"");
        PermLookup cassExecutorObj =PermLookup.get(trans, q,"");
        Result<Set<String>> userRoles = cassExecutorObj.getPermNames();
    
        //System.out.println(""+userRoles.status);
        assertEquals(4,userRoles.status);
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testGetPerm() {
        Result<List<UserRoleDAO.Data>> retVal1 = Mockito.mock(Result.class);
        Result<List<RoleDAO.Data>> retVal2 = Mockito.mock(Result.class);
    
        retVal1.value = new ArrayList<UserRoleDAO.Data>();
        retVal2.value = new ArrayList<RoleDAO.Data>();
        UserRoleDAO.Data dataObj = Mockito.mock( UserRoleDAO.Data.class);
    
        Date dt = new Date();
        Calendar c = Calendar.getInstance(); 
        c.setTime(dt); 
        c.add(Calendar.DATE, 1);
        dataObj.expires = c.getTime();
        dataObj.ns = "";
        dataObj.rname="";
    
        RoleDAO.Data dataObj1 = Mockito.mock( RoleDAO.Data.class);
        Set<String> permSet = new HashSet<String>();
        permSet.add("test");
        Mockito.doReturn(permSet).when(dataObj1).perms(false);
    
        dt = new Date();
        c = Calendar.getInstance(); 
        c.setTime(dt); 
        c.add(Calendar.DATE, 1);
        dataObj1.ns = "test";
        dataObj1.perms = permSet;
    
        retVal1.value.add(dataObj);
        retVal2.value.add(dataObj1);
        Mockito.doReturn(true).when(retVal1).isOKhasData();
        Mockito.doReturn(true).when(retVal1).isOK();
        Mockito.doReturn(true).when(retVal2).isOK();
        Mockito.doReturn(retVal1).when(userRoleDAO).readByUser(trans,"");
        Mockito.doReturn(retVal2).when(roleDAO).read(trans,"","");

    
        PermLookup cassExecutorObj =PermLookup.get(trans, q,"");
//    
//        Mockito.doReturn(retVal2).when(cassExecutorObj).getPermNames();
        Result<List<Data>> userRoles = cassExecutorObj.getPerms(true);
//        userRoles = cassExecutorObj.getPerms(false);
    
//        System.out.println(""+userRoles.status);
        assertEquals(0,userRoles.status);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetPermFalse() {
        Result<List<UserRoleDAO.Data>> retVal1 = Mockito.mock(Result.class);
        Result<List<RoleDAO.Data>> retVal2 = Mockito.mock(Result.class);
    
        retVal1.value = new ArrayList<UserRoleDAO.Data>();
        retVal2.value = new ArrayList<RoleDAO.Data>();
        UserRoleDAO.Data dataObj = Mockito.mock( UserRoleDAO.Data.class);
    
        Date dt = new Date();
        Calendar c = Calendar.getInstance(); 
        c.setTime(dt); 
        c.add(Calendar.DATE, 1);
        dataObj.expires = c.getTime();
        dataObj.ns = "";
        dataObj.rname="";
    
        RoleDAO.Data dataObj1 = Mockito.mock( RoleDAO.Data.class);
        Set<String> permSet = new HashSet<String>();
        permSet.add("test");
        Mockito.doReturn(permSet).when(dataObj1).perms(false);
    
        dt = new Date();
        c = Calendar.getInstance(); 
        c.setTime(dt); 
        c.add(Calendar.DATE, 1);
        dataObj1.ns = "test";
        dataObj1.perms = permSet;
    
        retVal1.value.add(dataObj);
        retVal2.value.add(dataObj1);
        Mockito.doReturn(true).when(retVal1).isOKhasData();
        Mockito.doReturn(true).when(retVal1).isOK();
        Mockito.doReturn(true).when(retVal2).isOK();
        Mockito.doReturn(retVal1).when(userRoleDAO).readByUser(trans,"");
        Mockito.doReturn(retVal2).when(roleDAO).read(trans,"","");

    
        PermLookup cassExecutorObj =PermLookup.get(trans, q,"");
//    
//        Mockito.doReturn(retVal2).when(cassExecutorObj).getPermNames();
        Result<List<Data>> userRoles = cassExecutorObj.getPerms(false);
        userRoles = cassExecutorObj.getPerms(false);
    
//        System.out.println(""+userRoles.status);
        assertEquals(0,userRoles.status);
    }

}