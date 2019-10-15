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
import org.onap.aaf.auth.dao.CIDAO;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO.Data;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransImpl;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.Slot;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class JU_CachedUserRoleDAO {

    @Mock
    UserRoleDAO dao;

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

    private class TaggedPrincipalStub extends TaggedPrincipal {
        String name="TaggedPrincipalStub";
        public TaggedPrincipalStub() { super(); }
        public TaggedPrincipalStub(final TagLookup tl) { super(tl); }
        @Override public String getName() { return name; }
        @Override public String tag() { return null; }
    }

    @Test
    public void testReadName() {
        CachedUserRoleDAO roleDaoObj =new CachedUserRoleDAO(dao,info, 10L);
        Result<List<Data>> retVal1 = new Result<List<Data>>(null,0,"test4",new String[0]);
        Mockito.doReturn(retVal1).when(dao).readByUser(trans, "test4");
//        Mockito.when(roleDaoObj.get(Mockito.any(), Mockito.any(String.class), Mockito.any())).thenReturn(retVal1);
        Result<List<Data>> retVal = roleDaoObj.readByUser(trans, "test4");
        //System.out.println(retVal.status);
        //retVal.status = 0;
        assertEquals("25", Integer.toString(retVal.status));
    }

    @Test
    public void testReadNameUser() {
        CachedUserRoleDAO roleDaoObj =new CachedUserRoleDAO(dao,info, 10L);
        Result<List<Data>> retVal1 = new Result<List<Data>>(null,1,"TaggedPrincipalStub",new String[0]);
        AuthzEnv env = Mockito.mock(AuthzEnv.class);
        AuthzTransImpl transTemp = new AuthzTransImpl(env) {
            @Override
            public<T> T get(Slot slot, T deflt) {
                Object o=null;
                return (T)o;
            }
        
        };
        transTemp.setUser(new TaggedPrincipalStub());
        Mockito.doReturn(retVal1).when(info).touch(trans, null,null);
        Mockito.doReturn(retVal1).when(dao).readByUser(transTemp, "TaggedPrincipalStub");
        roleDaoObj.invalidate("TaggedPrincipalStub");
        Result<List<Data>> retVal = roleDaoObj.readByUser(transTemp, "TaggedPrincipalStub");
//        System.out.println(retVal.status);
        assertEquals("1", Integer.toString(retVal.status));
    }

    @Test
    public void testReadByRoleSuccess() {
        CachedUserRoleDAO roleDaoObj =new CachedUserRoleDAO(dao,info, 0);//Mockito.mock(CachedRoleDAO.class);//
        Result<List<Data>> retVal1 = new Result<List<Data>>(null,1,"test",new String[0]);
        Mockito.doReturn(retVal1).when(dao).readByRole(trans, "");
        roleDaoObj.invalidate("");
        Result<List<Data>> retVal = roleDaoObj.readByRole(trans, "");
        //System.out.println(retVal.status);
        assertEquals("1", Integer.toString(retVal.status));
    }
    @Test
    public void testReadByRoleFailure() {
        CachedUserRoleDAO roleDaoObj =new CachedUserRoleDAO(dao,info, 0);//Mockito.mock(CachedRoleDAO.class);//
        Result<List<Data>> retVal1 = new Result<List<Data>>(null,0,"test1",new String[0]);
        Mockito.doReturn(retVal1).when(dao).readByRole(trans, "");
        roleDaoObj.invalidate("");
        Result<List<Data>> retVal = roleDaoObj.readByRole(trans, "");
        //System.out.println(retVal.status);
        assertEquals("25", Integer.toString(retVal.status));
    }

    @Test
    public void testReadUserInRole() {
        CachedUserRoleDAO roleDaoObj =new CachedUserRoleDAO(dao,info, 10);//Mockito.mock(CachedRoleDAO.class);//
        Result<List<Data>> retVal1 = new Result<List<Data>>(null,0,"TaggedPrincipalStub",new String[0]);
        AuthzEnv env = Mockito.mock(AuthzEnv.class);
        AuthzTransImpl transTemp = new AuthzTransImpl(env) {
            @Override
            public<T> T get(Slot slot, T deflt) {
                Object o=null;
                return (T)o;
            }
        
        };
        transTemp.setUser(new TaggedPrincipalStub());
        Mockito.doReturn(retVal1).when(info).touch(trans, null,null);
        Mockito.doReturn(retVal1).when(dao).readByUserRole(transTemp, "","");
        Mockito.doReturn(retVal1).when(dao).readByUser(transTemp, "TaggedPrincipalStub");
        Result<List<Data>> retVal = roleDaoObj.readUserInRole(transTemp, "TaggedPrincipalStub","");
        //System.out.println(retVal.status);
        assertEquals("25", Integer.toString(retVal.status));
    }


}