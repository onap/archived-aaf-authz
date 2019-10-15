/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * <p>
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/

package org.onap.aaf.auth.dao.hl;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.cached.CachedCredDAO;
import org.onap.aaf.auth.dao.cached.CachedNSDAO;
import org.onap.aaf.auth.dao.cached.CachedPermDAO;
import org.onap.aaf.auth.dao.cached.CachedRoleDAO;
import org.onap.aaf.auth.dao.cached.CachedUserRoleDAO;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.Namespace;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.NsSplit;
import org.onap.aaf.auth.dao.cass.NsType;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.hl.Function.FUTURE_OP;
import org.onap.aaf.auth.dao.hl.Function.Lookup;
import org.onap.aaf.auth.dao.hl.Question.Access;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTrans.REQD_TYPE;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Expiration;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.LogTarget;

public class JU_Function {

    private static final Object NO_PARAM = new Object[0];

    @Mock
    AuthzTrans trans;
    @Mock
    PropAccess access;

    @Mock
    Question ques;

    @Mock 
    Organization org;

    @Mock
    CachedNSDAO nsDAO;

    @Mock
    CachedRoleDAO roleDAO;

    @Mock
    CachedPermDAO permDAO;

    @Mock
    CachedCredDAO credDAO;

    @Mock
    CachedUserRoleDAO userRoleDAO;

    @Mock
    ApprovalDAO approvalDAO;

    @Mock
    FutureDAO futureDAO;

    @Before
    public void setUp() throws APIException, IOException {
        initMocks(this);
        Mockito.doReturn(org).when(trans).org();
        Mockito.doReturn(nsDAO).when(ques).nsDAO();
        Mockito.doReturn(roleDAO).when(ques).roleDAO();
        Mockito.doReturn(permDAO).when(ques).permDAO();
        Mockito.doReturn(credDAO).when(ques).credDAO();
        Mockito.doReturn(userRoleDAO).when(ques).userRoleDAO();
        Mockito.doReturn(approvalDAO).when(ques).approvalDAO();
        Mockito.doReturn(futureDAO).when(ques).futureDAO();

        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).info();
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
        Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
        Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
    
        try {
            Define.set(access);
        } catch (CadiException e) {
        }
    }

    @Test
    public void testCreateNs() {
        Namespace namespace = Mockito.mock(Namespace.class);
        namespace.name = "test.test";
        List<String> owner = new ArrayList<String>();
        namespace.owner = owner;

        Mockito.doReturn(Result.err(Result.ERR_NotFound, "Not Found")).when(roleDAO).read(trans, "test","test");
        Mockito.doReturn(Result.err(Result.ERR_NotFound, "Not Found")).when(permDAO).readByType(trans, "test","test");

        NsDAO.Data data = new NsDAO.Data();
        data.name="test";
        Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");

        Result<Void> retVal = new Result<Void>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(nsDAO).read(Mockito.any(), Mockito.anyString());    
        //setQuestion(ques, cachedNS);
    
        Function funcObj = new Function(trans, ques);
        Result<Void>  result = funcObj.createNS(trans, namespace, true);
        assertTrue(3 == result.status);
    }

    @Test
    public void testCreateNsReadSuccess() {
        Namespace namespace = Mockito.mock(Namespace.class);
        namespace.name = "test.test";
        List<String> owner = new ArrayList<String>();
        owner.add("test");
        namespace.owner = owner;
        List<String> admin = new ArrayList<String>();
        admin.add("test");
        namespace.admin= admin;
    
        Organization org = Mockito.mock(Organization.class);
        Mockito.doReturn(org).when(trans).org();
    
        NsDAO.Data data = new NsDAO.Data();
        data.name="test";
        Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");

        Result<Void> retVal = new Result<Void>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(nsDAO).read(Mockito.any(), Mockito.anyString());    
    
        Function funcObj = new Function(trans, ques);
        Result<Void>  result = funcObj.createNS(trans, namespace, true);
        assertTrue(3 == result.status);
    }

    @Test
    public void testCreateNsFromApprovaFalse() {
        Namespace namespace = Mockito.mock(Namespace.class);
        namespace.name = "test.test";
        List<String> owner = new ArrayList<String>();
        namespace.owner = owner;
    
        Organization org = Mockito.mock(Organization.class);
        Mockito.doReturn(org).when(trans).org();
    
        NsDAO.Data data = new NsDAO.Data();
        data.name="test";
        Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(data,1,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal1.value, Access.write);
    
        Function funcObj = new Function(trans, ques);
        Result<Void>  result = funcObj.createNS(trans, namespace, false);
        assertTrue(1 == result.status);
    
        Mockito.doReturn(retVal2).when(ques).deriveNs(trans, "test");
        funcObj = new Function(trans, ques);
        result = funcObj.createNS(trans, namespace, false);
        assertTrue(1 == result.status);
    }

    @Test
    public void testCreateNsownerLoop() {
        Namespace namespace = Mockito.mock(Namespace.class);
        namespace.name = "test.test";
        List<String> owner = new ArrayList<String>();
        owner.add("test");
        namespace.owner = owner;
    
        Organization org = Mockito.mock(Organization.class);
        Mockito.doReturn(org).when(trans).org();
        Mockito.doReturn(org).when(trans).org();
    
        Function funcObj = new Function(trans, ques);
        Result<Void>  result = funcObj.createNS(trans, namespace, true);
        assertTrue(result.status == Status.ERR_Policy);
        assertTrue(result.details.contains("is not a valid user at"));
    
        Identity iden=Mockito.mock(Identity.class);
        try {
            Mockito.doReturn(iden).when(org).getIdentity(trans, "test");
            Mockito.doReturn("test").when(iden).mayOwn();
        } catch (OrganizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        result = funcObj.createNS(trans, namespace, true);
        assertTrue(result.status == Status.ERR_Policy);
        assertTrue(result.details.contains("is an invalid Identity"));

        Mockito.doReturn(true).when(iden).isFound();
        result = funcObj.createNS(trans, namespace, true);
        assertTrue(result.status == Status.ERR_Policy);
        assertTrue(result.details.contains("cannot be the owner of the namespace "));
    
        Mockito.doReturn(true).when(org).isTestEnv();
        try {
            Mockito.doReturn("test").when(org).validate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
            result = funcObj.createNS(trans, namespace, true);
            assertTrue(result.status == Status.ERR_Policy);
            assertTrue(result.details.contains("cannot be the owner of the namespace "));
        } catch (OrganizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateNsownerLoopException() {
        Namespace namespace = Mockito.mock(Namespace.class);
        namespace.name = "test";
        List<String> owner = new ArrayList<String>();
        owner.add("test");
        namespace.owner = owner;
    
        Organization org = Mockito.mock(Organization.class);
        Mockito.doReturn(org).when(trans).org();
    
        Function funcObj = new Function(trans, ques);
    
        Identity iden=Mockito.mock(Identity.class);
        try {
            Mockito.doThrow(new OrganizationException()).when(org).getIdentity(trans, "test");
            Mockito.doReturn("test").when(iden).mayOwn();
        } catch (OrganizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        NsDAO.Data data = new NsDAO.Data();
        data.name="test";
        Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,1,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
    
        Result<Void> result = funcObj.createNS(trans, namespace, true);
        assertTrue(result.status == Status.ERR_Security);
        assertTrue(result.details.contains("may not create Root Namespaces"));
    
        Mockito.doReturn(true).when(ques).isGranted(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        retVal1 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, null);
    
        Result<Void> retVal = new Result<Void>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(nsDAO).read(Mockito.any(), Mockito.anyString());    
    
        result = funcObj.createNS(trans, namespace, true);
        assertTrue(24 == result.status);
    
    }

    @Test
    public void testCreateNsAdminLoop() {
        Namespace namespace = Mockito.mock(Namespace.class);
        namespace.name = "test.test";
        List<String> owner = new ArrayList<String>();
        owner.add("test");
        namespace.owner = owner;
        namespace.admin = owner;
    
        Organization org = Mockito.mock(Organization.class);
        Mockito.doReturn(org).when(trans).org();
    
        NsDAO.Data data = new NsDAO.Data();
        data.name="test";
        Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
    
        Result<Void> retVal = new Result<Void>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(nsDAO).read(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(retVal).when(nsDAO).create(Mockito.any(), Mockito.any());
        List<CredDAO.Data> dataObj = new ArrayList<>();
        CredDAO.Data indData = new CredDAO.Data();
        indData.id = "test";
        indData.notes = "test";
        DateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");
        try {
            indData.expires = sdf.parse("2090/01/01");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        dataObj.add(indData);
        Result<List<CredDAO.Data>> retVal2 = new Result<List<CredDAO.Data>>(dataObj,0,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(credDAO).readID(Mockito.any(), Mockito.anyString());    
    
        Identity iden=Mockito.mock(Identity.class);
        try {
            Mockito.doReturn(iden).when(org).getIdentity(trans, "test");
            Mockito.doReturn("test").when(iden).mayOwn();
            Mockito.doReturn(true).when(org).isTestEnv();
        } catch (OrganizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        Mockito.doReturn(Result.err(Result.ERR_NotFound, "Not Found")).when(roleDAO).read(trans, "test","test");
        Mockito.doReturn(Result.err(Result.ERR_NotFound, "Not Found")).when(permDAO).readByType(trans, "test","test");

        Function funcObj = new Function(trans, ques);
        Result<Void>  result = funcObj.createNS(trans, namespace, true);
        assertTrue(result.status == 1);
    
    }

    @Test
    public void testCreateNsAdminLoopCreateSucReadChildrenFailure() {
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
        Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
        Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
        try {
            Define.set(access);
        } catch (CadiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Namespace namespace = Mockito.mock(Namespace.class);
        namespace.name = "test.test";
        List<String> owner = new ArrayList<String>();
        owner.add("test");
        namespace.owner = owner;
        namespace.admin = owner;
    
        Organization org = Mockito.mock(Organization.class);
        Mockito.doReturn(org).when(trans).org();
    
        NsDAO.Data data = new NsDAO.Data();
        data.name="test";
        Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
    
        Result<Void> retVal = new Result<Void>(null,0,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(nsDAO).read(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(retVal).when(nsDAO).create(Mockito.any(), Mockito.any());
        List<CredDAO.Data> dataObj = new ArrayList<>();
        CredDAO.Data indData = new CredDAO.Data();
        indData.id = "test";
        indData.notes = "test";
        DateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");
        try {
            indData.expires = sdf.parse("2090/01/01");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        dataObj.add(indData);
        List<RoleDAO.Data> dataObj1 = new ArrayList<>();
        RoleDAO.Data indData1 = new RoleDAO.Data();
        indData1.ns = "test";
        indData1.name = "test";
        Set<String> permsSet = new HashSet<>();
        permsSet.add("test|test");
        indData1.perms = permsSet;
        dataObj1.add(indData1);
    
        List<UserRoleDAO.Data> dataObj4 = new ArrayList<>();
        UserRoleDAO.Data indData4 = new UserRoleDAO.Data();
        indData4.ns = "test";
        indData4.rname = "test";
        dataObj4.add(indData4);
    
        List<PermDAO.Data> dataObj5 = new ArrayList<>();
        PermDAO.Data indData5 = new PermDAO.Data();
        indData5.ns = "test";
        indData5.type = "test";
        dataObj5.add(indData5);
    
        Result<List<CredDAO.Data>> retVal2 = new Result<List<CredDAO.Data>>(dataObj,0,"test",NO_PARAM);
        Result<List<CredDAO.Data>> retVal6 = new Result<List<CredDAO.Data>>(dataObj,1,"test",NO_PARAM);
        Result<List<RoleDAO.Data>> retVal3 = new Result<List<RoleDAO.Data>>(dataObj1,0,"test",NO_PARAM);
        Result<List<UserRoleDAO.Data>> retVal4 = new Result<List<UserRoleDAO.Data>>(dataObj4,0,"test",NO_PARAM);
        Result<List<PermDAO.Data>> retVal5 = new Result<List<PermDAO.Data>>(dataObj5,0,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(credDAO).readID(Mockito.any(), Mockito.anyString());    
        Mockito.doReturn(retVal4).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(retVal2).when(userRoleDAO).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(retVal6).when(roleDAO).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(retVal6).when(roleDAO).delete(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(retVal2).when(permDAO).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(retVal5).when(permDAO).readChildren(trans, "test", "test");
        Mockito.doReturn(retVal5).when(permDAO).delete(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(retVal3).when(roleDAO).readChildren(trans, "test", "test");
    
        Identity iden=Mockito.mock(Identity.class);
        try {
            Mockito.doReturn(iden).when(org).getIdentity(trans, "test");
            Mockito.doReturn("test").when(iden).mayOwn();
            Mockito.doReturn(true).when(org).isTestEnv();
            Mockito.doReturn(new GregorianCalendar(2010, 01, 01)).when(org).expiration(null, Expiration.UserInRole);
        } catch (OrganizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        Function funcObj = new Function(trans, ques);
        Mockito.doReturn(Result.err(Result.ERR_NotFound, "Not Found")).when(roleDAO).read(trans, "test","test");
        Mockito.doReturn(Result.err(Result.ERR_NotFound, "Not Found")).when(permDAO).readByType(trans, "test","test");

        Result<Void>  result = funcObj.createNS(trans, namespace, true);
        assertTrue(result.status == Status.ERR_ActionNotCompleted);
    
    }

    @Test
    public void testCreateNsAdminLoopCreateSuc() {
        Namespace namespace = Mockito.mock(Namespace.class);
        namespace.name = "test.test";
        List<String> owner = new ArrayList<String>();
        owner.add("test");
        namespace.owner = owner;
        namespace.admin = owner;
    
        Organization org = Mockito.mock(Organization.class);
        Mockito.doReturn(org).when(trans).org();
    
        NsDAO.Data data = new NsDAO.Data();
        data.name="test";
        Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
    
        Result<Void> retVal = new Result<Void>(null,0,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(nsDAO).read(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(retVal).when(nsDAO).create(Mockito.any(), Mockito.any());
        List<CredDAO.Data> dataObj = new ArrayList<>();
        CredDAO.Data indData = new CredDAO.Data();
        indData.id = "test";
        indData.notes = "test";
        DateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");
        try {
            indData.expires = sdf.parse("2090/01/01");
        } catch (ParseException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        dataObj.add(indData);
        List<RoleDAO.Data> dataObj1 = new ArrayList<>();
        RoleDAO.Data indData1 = new RoleDAO.Data();
        indData1.ns = "test";
        indData1.name = "test";
        Set<String> permsSet = new HashSet<>();
        permsSet.add("test|test|test|test");
        indData1.perms = permsSet;
        dataObj1.add(indData1);
    
        List<UserRoleDAO.Data> dataObj4 = new ArrayList<>();
        UserRoleDAO.Data indData4 = new UserRoleDAO.Data();
        indData4.ns = "test";
        indData4.rname = "test";
        dataObj4.add(indData4);
    
        List<PermDAO.Data> dataObj5 = new ArrayList<>();
        PermDAO.Data indData5 = new PermDAO.Data();
        indData5.ns = "test";
        indData5.type = "test";
        Set<String> rolesSet = new HashSet<>();
        rolesSet.add("test|test|test|test");
        indData5.roles = rolesSet;
        dataObj5.add(indData5);
    
        Result<List<CredDAO.Data>> retVal2 = new Result<List<CredDAO.Data>>(dataObj,0,"test",NO_PARAM);
        Result<List<RoleDAO.Data>> retVal3 = new Result<List<RoleDAO.Data>>(dataObj1,0,"test",NO_PARAM);
        Result<List<UserRoleDAO.Data>> retVal4 = new Result<List<UserRoleDAO.Data>>(dataObj4,0,"test",NO_PARAM);
        Result<List<PermDAO.Data>> retVal5 = new Result<List<PermDAO.Data>>(dataObj5,0,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(credDAO).readID(Mockito.any(), Mockito.anyString());    
        Mockito.doReturn(retVal4).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(retVal2).when(userRoleDAO).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(retVal2).when(roleDAO).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(retVal2).when(roleDAO).delete(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(retVal2).when(permDAO).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(retVal5).when(permDAO).readChildren(trans, "test", "test");
        Mockito.doReturn(retVal5).when(permDAO).delete(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        Mockito.doReturn(retVal3).when(roleDAO).readChildren(trans, "test", "test");
    
        Identity iden=Mockito.mock(Identity.class);
        try {
            Mockito.doReturn(iden).when(org).getIdentity(trans, "test");
            Mockito.doReturn("test").when(iden).mayOwn();
            Mockito.doReturn(true).when(org).isTestEnv();
            Mockito.doReturn(new GregorianCalendar(2010, 01, 01)).when(org).expiration(null, Expiration.UserInRole);
        } catch (OrganizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        Mockito.doReturn(Result.err(Result.ERR_NotFound, "Not Found")).when(roleDAO).read(trans, "test","test");
        Mockito.doReturn(Result.err(Result.ERR_NotFound, "Not Found")).when(permDAO).readByType(trans, "test","test");

        Function funcObj = new Function(trans, ques);
        Result<Void>  result = funcObj.createNS(trans, namespace, true);
        assertTrue(result.status == 0);
    
    }

    @Test
    public void test4DeleteNs() {
        Result<Void> retVal = new Result<Void>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(nsDAO).read(Mockito.any(), Mockito.anyString());    
    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.deleteNS(trans, "test");
    
        assertTrue(result.status == Status.ERR_NsNotFound);
    }

    @Test
    public void test4DeleteCanMoveFail() {
        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
        NsDAO.Data dataObj = new NsDAO.Data();
        dataObj.type=1;
        dataAl.add(dataObj);
        Result<List<NsDAO.Data>> retVal = new Result<List<NsDAO.Data>>(dataAl,0,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(nsDAO).read(Mockito.any(), Mockito.anyString());    
    
        Mockito.doReturn(false).when(ques).canMove(Mockito.any());
        Mockito.doReturn(retVal).when(ques).mayUser(trans, null,retVal.value.get(0), Access.write);
    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.deleteNS(trans, "test");
        assertTrue(result.status == Status.ERR_Security);
    
    }

    @Test
    public void test4DeleteNsReadSuc() {
        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
        NsDAO.Data dataObj = new NsDAO.Data();
        dataObj.type=1;
        dataAl.add(dataObj);
        Result<List<NsDAO.Data>> retVal = new Result<List<NsDAO.Data>>(dataAl,0,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(nsDAO).read(Mockito.any(), Mockito.anyString());    
    
        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal.value.get(0), Access.write);
    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.deleteNS(trans, "test");
        assertTrue(result.status == 1);
    
    }

    @Test
    public void test4DeleteNsMayUserSuc() {
        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
        NsDAO.Data dataObj = new NsDAO.Data();
        dataObj.type=1;
        dataAl.add(dataObj);
        Result<List<NsDAO.Data>> retVal = new Result<List<NsDAO.Data>>(dataAl,0,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(nsDAO).read(Mockito.any(), Mockito.anyString());    
    
        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(null,0,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal.value.get(0), Access.write);
    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.deleteNS(trans, "test");
        assertTrue(result.status == 1);

        Mockito.doReturn(true).when(ques).isGranted(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        Mockito.doReturn(retVal2).when(credDAO).readNS(Mockito.any(), Mockito.anyString());    

        Result<List<PermDAO.Data>> retVal5 = new Result<List<PermDAO.Data>>(null,0,"test",NO_PARAM);
        Mockito.doReturn(retVal5).when(permDAO).readNS(trans, "test");
    
        List<UserRoleDAO.Data> dataObj4 = new ArrayList<>();
        UserRoleDAO.Data indData4 = new UserRoleDAO.Data();
        indData4.ns = "test";
        indData4.rname = "test";
        dataObj4.add(indData4);
        Result<List<UserRoleDAO.Data>> retVal4 = new Result<List<UserRoleDAO.Data>>(dataObj4,0,"test",NO_PARAM);
        Mockito.doReturn(retVal4).when(userRoleDAO).readByRole(trans, "test");
    
        List<RoleDAO.Data> dataObj1 = new ArrayList<>();
        RoleDAO.Data indData1 = new RoleDAO.Data();
        indData1.ns = "test";
        indData1.name = "test";
        Set<String> permsSet = new HashSet<>();
        permsSet.add("test|test");
        indData1.perms = permsSet;
        dataObj1.add(indData1);
        Result<List<RoleDAO.Data>> retVal3 = new Result<List<RoleDAO.Data>>(dataObj1,0,"test",NO_PARAM);
        Mockito.doReturn(retVal3).when(roleDAO).readNS(trans, "test");
        Mockito.doReturn(retVal3).when(roleDAO).read(trans, indData1);
    
        funcObj = new Function(trans, ques);
        result = funcObj.deleteNS(trans, "test");
        assertTrue(result.status == Status.ERR_DependencyExists);
    
        Mockito.doReturn(retVal4).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());
    
        Mockito.doReturn(true).when(trans).requested(REQD_TYPE.force);
        funcObj = new Function(trans, ques);
        result = funcObj.deleteNS(trans, "test");
        assertNull(result);
    }
    @Test
    public void test4DeleteNsDrivensFailure() {
        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
        NsDAO.Data dataObj = new NsDAO.Data();
        dataObj.type=1;
        dataAl.add(dataObj);
        Result<List<NsDAO.Data>> retVal = new Result<List<NsDAO.Data>>(dataAl,0,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(nsDAO).read(Mockito.any(), Mockito.anyString());    
    
        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(null,0,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal.value.get(0), Access.write);
    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.deleteNS(trans, "test");
        assertTrue(result.status == 1);

        Mockito.doReturn(true).when(ques).isGranted(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        Mockito.doReturn(retVal2).when(credDAO).readNS(Mockito.any(), Mockito.anyString());    

        List<PermDAO.Data> dataObj5 = new ArrayList<>();
        PermDAO.Data indData5 = new PermDAO.Data();
        indData5.ns = "test";
        indData5.type = "test";
        dataObj5.add(indData5);
        Result<List<PermDAO.Data>> retVal5 = new Result<List<PermDAO.Data>>(dataObj5,0,"test",NO_PARAM);
        Mockito.doReturn(retVal5).when(permDAO).readNS(trans, "test");
        Mockito.doReturn(retVal5).when(permDAO).readNS(trans, "test.test");
        Mockito.doReturn(retVal5).when(permDAO).read(trans, indData5);
    
    
        List<RoleDAO.Data> dataObj1 = new ArrayList<>();
        RoleDAO.Data indData1 = new RoleDAO.Data();
        indData1.ns = "test";
        indData1.name = "test";
        Set<String> permsSet = new HashSet<>();
        permsSet.add("test|test");
        indData1.perms = permsSet;
        dataObj1.add(indData1);
        Result<List<RoleDAO.Data>> retVal3 = new Result<List<RoleDAO.Data>>(dataObj1,0,"test",NO_PARAM);
        Mockito.doReturn(retVal3).when(roleDAO).readNS(trans, "test");
        Mockito.doReturn(retVal3).when(roleDAO).readNS(trans, "test.test");
        Mockito.doReturn(retVal3).when(roleDAO).read(trans, indData1);
    
        funcObj = new Function(trans, ques);
        result = funcObj.deleteNS(trans, "test");
        assertTrue(result.status == Status.ERR_DependencyExists);
    
        NsDAO.Data data = new NsDAO.Data();
        data.name="test";
        Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,1,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
    
        Mockito.doReturn(true).when(trans).requested(REQD_TYPE.force);
        funcObj = new Function(trans, ques);
        result = funcObj.deleteNS(trans, "test.test");
        assertTrue(result.status == 1);
    }

    @Test
    public void test4DeleteNsWithDot() {
        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
        NsDAO.Data dataObj = new NsDAO.Data();
        dataObj.type=1;
        dataAl.add(dataObj);
        Result<List<NsDAO.Data>> retVal = new Result<List<NsDAO.Data>>(dataAl,0,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(nsDAO).read(Mockito.any(), Mockito.anyString());    
    
        List<CredDAO.Data> nsDataList = new ArrayList<CredDAO.Data>();
        CredDAO.Data nsData = new CredDAO.Data();
        nsData.id="test";
        nsDataList.add(nsData);
        Result<List<CredDAO.Data>> retVal21 = new Result<List<CredDAO.Data>>(nsDataList,0,"test",NO_PARAM);
        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(null,0,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal.value.get(0), Access.write);
    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.deleteNS(trans, "test");
        assertTrue(result.status == 1);

        Mockito.doReturn(true).when(ques).isGranted(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        Mockito.doReturn(retVal21).when(credDAO).readNS(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(retVal21).when(credDAO).delete(Mockito.any(), Mockito.any(), Mockito.anyBoolean());    

        List<PermDAO.Data> dataObj5 = new ArrayList<>();
        PermDAO.Data indData5 = new PermDAO.Data();
        indData5.ns = "test";
        indData5.type = "test";
        dataObj5.add(indData5);
        Result<List<PermDAO.Data>> retVal5 = new Result<List<PermDAO.Data>>(dataObj5,0,"test",new Object[0]);
        Mockito.doReturn(retVal5).when(permDAO).readNS(trans, "test");
        Mockito.doReturn(retVal5).when(permDAO).readNS(trans, "test.test");
        Mockito.doReturn(retVal5).when(permDAO).read(trans, indData5);
    
        List<UserRoleDAO.Data> dataObj4 = new ArrayList<>();
        UserRoleDAO.Data indData4 = new UserRoleDAO.Data();
        indData4.ns = "test";
        indData4.rname = "test";
        dataObj4.add(indData4);
        Result<List<UserRoleDAO.Data>> retVal4 = new Result<List<UserRoleDAO.Data>>(dataObj4,0,"test",NO_PARAM);
        Mockito.doReturn(retVal4).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(retVal4).when(userRoleDAO).readByUser(Mockito.any(), Mockito.anyString());
    
        List<RoleDAO.Data> dataObj1 = new ArrayList<>();
        RoleDAO.Data indData1 = new RoleDAO.Data();
        indData1.ns = "test";
        indData1.name = "admin";
        Set<String> permsSet = new HashSet<>();
        permsSet.add("test|test");
        indData1.perms = permsSet;
        dataObj1.add(indData1);
        Result<List<RoleDAO.Data>> retVal3 = new Result<List<RoleDAO.Data>>(dataObj1,0,"test",NO_PARAM);
        Mockito.doReturn(retVal3).when(roleDAO).readNS(trans, "test");
        Mockito.doReturn(retVal3).when(roleDAO).readNS(trans, "test.test");
        Mockito.doReturn(retVal3).when(roleDAO).read(trans, indData1);
    
        funcObj = new Function(trans, ques);
        result = funcObj.deleteNS(trans, "test");
        assertTrue(result.status == Status.ERR_DependencyExists);
    
        NsDAO.Data data = new NsDAO.Data();
        data.name="test";
        Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
    
        Mockito.doReturn(true).when(trans).requested(REQD_TYPE.force);
        funcObj = new Function(trans, ques);
        result = funcObj.deleteNS(trans, "test.test");
        assertNull(result);
    }

    @Test
    public void testGetOwners() {
//        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
//        NsDAO.Data dataObj = new NsDAO.Data();
//        dataObj.type=1;
//        dataAl.add(dataObj);
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());    
//    
//        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(null,0,"test",NO_PARAM);
//        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal.value.get(0), Access.write);
//    
        Function funcObj = new Function(trans, ques);
        Result<List<String>> result = funcObj.getOwners(trans, "test", false);
        assertTrue(result.status == 1);
//    
    }

    @Test
    public void testDelOwner() {
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(retVal).when(userRoleDAO).read(Mockito.any(), Mockito.any( UserRoleDAO.Data.class));

        NsDAO.Data data = new NsDAO.Data();
        data.name="test";
        Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
    
        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(data,1,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal1.value, Access.write);
    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.delOwner(trans, "test", "test");
        assertTrue(result.status == 1);
    
        retVal1 = new Result<NsDAO.Data>(data,1,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
        result = funcObj.delOwner(trans, "test", "test");
        assertTrue(result.status == 1);
    
        retVal1 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
        result = funcObj.delOwner(trans, "test", "test");
        retVal2 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal1.value, Access.write);
        result = funcObj.delOwner(trans, "test", "test");
//    
    }

    @Test
    public void testGetAdmins() {
//        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
//        NsDAO.Data dataObj = new NsDAO.Data();
//        dataObj.type=1;
//        dataAl.add(dataObj);
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());    
//    
//        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(null,0,"test",NO_PARAM);
//        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal.value.get(0), Access.write);
//    
        Function funcObj = new Function(trans, ques);
        Result<List<String>> result = funcObj.getAdmins(trans, "test", false);
        assertTrue(result.status == 1);
//    
    }

    @Test
    public void testDelAdmin() {
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readUserInRole(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(retVal).when(userRoleDAO).read(Mockito.any(), Mockito.any( UserRoleDAO.Data.class));

        NsDAO.Data data = new NsDAO.Data();
        data.name="test";
        Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
    
        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(data,1,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal1.value, Access.write);
    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.delAdmin(trans, "test", "test");
        assertTrue(result.status == 1);
    
        retVal1 = new Result<NsDAO.Data>(data,1,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
        result = funcObj.delAdmin(trans, "test", "test");
        assertTrue(result.status == 1);
    
        retVal1 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
        result = funcObj.delOwner(trans, "test", "test");
        retVal2 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal1.value, Access.write);
        result = funcObj.delAdmin(trans, "test", "test");
//    
    }

    @Test
    public void testMovePerms() {
//        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
//        NsDAO.Data dataObj = new NsDAO.Data();
//        dataObj.type=1;
//        dataAl.add(dataObj);
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());    
    
        Mockito.doReturn(retVal).when(roleDAO).delPerm(Mockito.any(), Mockito.any(), Mockito.any());
    
        Mockito.doReturn(retVal).when(permDAO).create(Mockito.any(), Mockito.any());
    
        NsDAO.Data nsDataObj = new NsDAO.Data();
        nsDataObj.name="test";
        StringBuilder sb = new StringBuilder();
        Result<List<PermDAO.Data>> retVal1 = new Result<List<PermDAO.Data>>(null,1,"test",NO_PARAM);
    
        invokeMovePerms(nsDataObj, sb, retVal1);
    
        List<PermDAO.Data> dataObj5 = new ArrayList<>();
        PermDAO.Data indData5 = new PermDAO.Data();
        indData5.ns = "test";
        indData5.type = "test";
        Set<String> rolesSet = new HashSet<>();
        rolesSet.add("testRole");
        indData5.roles = rolesSet;
        dataObj5.add(indData5);
        indData5 = new PermDAO.Data();
        indData5.ns = "test";
        indData5.type = "access";
        dataObj5.add(indData5);
        retVal1 = new Result<List<PermDAO.Data>>(dataObj5,0,"test",NO_PARAM);

        Result<List<UserRoleDAO.Data>> retVal3 = new Result<List<UserRoleDAO.Data>>(null,0,"test",NO_PARAM);
        Mockito.doReturn(retVal3).when(permDAO).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(retVal3).when(permDAO).delete(Mockito.any(), Mockito.any(), Mockito.anyBoolean());

        NsSplit splitObj = new NsSplit("test", "test");
        Result<NsSplit> retVal2 = new Result<NsSplit>(splitObj,0,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).deriveNsSplit(Mockito.any(), Mockito.anyString());
    
        invokeMovePerms(nsDataObj, sb, retVal1);
    
        Result<List<UserRoleDAO.Data>> retVal4 = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal4).when(permDAO).create(Mockito.any(), Mockito.any());
        invokeMovePerms(nsDataObj, sb, retVal1);
    
        Mockito.doReturn(retVal3).when(permDAO).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(retVal4).when(permDAO).delete(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        invokeMovePerms(nsDataObj, sb, retVal1);
    
    }

    private void invokeMovePerms(NsDAO.Data nsDataObj, StringBuilder sb,Result<List<PermDAO.Data>> retVal1) {
        Function funcObj = new Function(trans, ques);
        Method met;
        try {
            met = Function.class.getDeclaredMethod("movePerms", AuthzTrans.class, NsDAO.Data.class, StringBuilder.class, Result.class);
            met.setAccessible(true);
            met.invoke(funcObj, trans, nsDataObj, sb, retVal1);
        } catch (NoSuchMethodException | SecurityException e) {
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
        }
    }

    @Test
    public void testMoveRoles() {
//        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
//        NsDAO.Data dataObj = new NsDAO.Data();
//        dataObj.type=1;
//        dataAl.add(dataObj);
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());    
    
        Mockito.doReturn(retVal).when(roleDAO).delPerm(Mockito.any(), Mockito.any(), Mockito.any());
    
        Mockito.doReturn(retVal).when(roleDAO).create(Mockito.any(), Mockito.any());
    
        NsDAO.Data nsDataObj = new NsDAO.Data();
        nsDataObj.name="test";
        StringBuilder sb = new StringBuilder();
        Result<List<RoleDAO.Data>> retVal1 = new Result<List<RoleDAO.Data>>(null,1,"test",NO_PARAM);
    
        invokeMoveRoles(nsDataObj, sb, retVal1);
    
        List<RoleDAO.Data> dataObj5 = new ArrayList<>();
        RoleDAO.Data indData5 = new RoleDAO.Data();
        indData5.ns = "test";
        indData5.name = "test";
        Set<String> rolesSet = new HashSet<>();
        rolesSet.add("testRole");
        indData5.perms = rolesSet;
        dataObj5.add(indData5);
        indData5 = new RoleDAO.Data();
        indData5.ns = "test";
        indData5.name = "admin";
        dataObj5.add(indData5);
        retVal1 = new Result<List<RoleDAO.Data>>(dataObj5,0,"test",NO_PARAM);
    
        Result<List<UserRoleDAO.Data>> retVal3 = new Result<List<UserRoleDAO.Data>>(null,0,"test",NO_PARAM);
        Mockito.doReturn(retVal3).when(roleDAO).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(retVal3).when(roleDAO).delete(Mockito.any(), Mockito.any(), Mockito.anyBoolean());

        NsSplit splitObj = new NsSplit("test", "test");
        Result<NsSplit> retVal2 = new Result<NsSplit>(splitObj,0,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).deriveNsSplit(Mockito.any(), Mockito.anyString());
    
        invokeMoveRoles(nsDataObj, sb, retVal1);
    
        Result<List<UserRoleDAO.Data>> retVal4 = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal4).when(roleDAO).create(Mockito.any(), Mockito.any());
        invokeMoveRoles(nsDataObj, sb, retVal1);
    
        Mockito.doReturn(retVal3).when(roleDAO).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(retVal4).when(roleDAO).delete(Mockito.any(), Mockito.any(), Mockito.anyBoolean());
        invokeMoveRoles(nsDataObj, sb, retVal1);
    
    }

    private void invokeMoveRoles(NsDAO.Data nsDataObj, StringBuilder sb,Result<List<RoleDAO.Data>> retVal1) {
        Function funcObj = new Function(trans, ques);
        Method met;
        try {
            met = Function.class.getDeclaredMethod("moveRoles", AuthzTrans.class, NsDAO.Data.class, StringBuilder.class, Result.class);
            met.setAccessible(true);
            met.invoke(funcObj, trans, nsDataObj, sb, retVal1);
        } catch (NoSuchMethodException | SecurityException e) {
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
        }
    }

    @Test
    public void testCreatePerm() {
        try {
            Define.set(access);
        } catch (CadiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<PermDAO.Data> dataAl = new ArrayList<PermDAO.Data>();
        PermDAO.Data perm = new PermDAO.Data();
        Set<String> rolesSet = new HashSet<>();
        rolesSet.add("testRole");
        perm.roles = rolesSet;
//        perm.type=1
        dataAl.add(perm);
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(ques).deriveNsSplit(Mockito.any(), Mockito.anyString());
    
        Mockito.doReturn(retVal).when(userRoleDAO).read(Mockito.any(), Mockito.any(UserRoleDAO.Data.class));
        Mockito.doReturn(retVal).when(userRoleDAO).create(Mockito.any(), Mockito.any(UserRoleDAO.Data.class));    
    
        Mockito.doReturn(retVal).when(permDAO).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(retVal).when(permDAO).read(trans, perm);    
    
        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,perm, Access.write);
    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.createPerm(trans, perm, false);
        assertTrue(result.status == 1);
    
        retVal2 = new Result<NsDAO.Data>(null,0,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,perm, Access.write);
        result = funcObj.createPerm(trans, perm, false);
        assertTrue(result.status == 1);

        NsSplit nsObj = new NsSplit("test","test");
        Result<NsSplit> retValNs = new Result<NsSplit>(nsObj,0,"test",NO_PARAM);
        Mockito.doReturn(retValNs).when(ques).deriveNsSplit(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(retVal2).when(ques).mayUser(Mockito.any(), Mockito.anyString(),Mockito.any(RoleDAO.Data.class), Mockito.any());
        Result<List<RoleDAO.Data>> retVal3 = Result.ok(new ArrayList<>());
        Mockito.doReturn(retVal3).when(roleDAO).read(Mockito.any(),Mockito.any(RoleDAO.Data.class));
        Result<List<RoleDAO.Data>> retVal4 = Result.err(Result.ERR_NotFound,"");
        Mockito.doReturn(retVal4).when(roleDAO).create(Mockito.any(),Mockito.any(RoleDAO.Data.class));
        result = funcObj.createPerm(trans, perm, false);
    
        Mockito.doReturn(retVal).when(permDAO).read(trans, perm);
        result = funcObj.createPerm(trans, perm, true);
        assertTrue(result.status == 1);

        Mockito.doReturn(retVal2).when(permDAO).create(Mockito.any(), Mockito.any());
        result = funcObj.createPerm(trans, perm, true);
        assertTrue(result.status == 0);
    
        Mockito.doReturn(false).when(trans).requested(REQD_TYPE.force);
        Result<List<PermDAO.Data>> retVal1 = new Result<List<PermDAO.Data>>(dataAl,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(permDAO).read(trans, perm);
        result = funcObj.createPerm(trans, perm, true);
        assertTrue(result.status == Status.ERR_ConflictAlreadyExists);
    
    }
    @Test
    public void testDeletePerm() {
        try {
            Define.set(access);
        } catch (CadiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        List<PermDAO.Data> dataAl = new ArrayList<PermDAO.Data>();
        PermDAO.Data perm = new PermDAO.Data();
        Set<String> rolesSet = new HashSet<>();
        rolesSet.add("testRole");
        perm.roles = rolesSet;
//        perm.type=1
        dataAl.add(perm);
    
        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,perm, Access.write);
    
//        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
//        NsDAO.Data dataObj = new NsDAO.Data();
//        dataObj.type=1;
//        dataAl.add(dataObj);
        Result<List<PermDAO.Data>> retVal = new Result<List<PermDAO.Data>>(dataAl,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());    
    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.deletePerm(trans, perm, true,false);
        assertTrue(result.status == 1);

//        Mockito.doReturn(retVal).when(cachedPermDAO).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(retVal).when(permDAO).read(trans, perm);    
    
        result = funcObj.deletePerm(trans, perm, true,true);
        assertTrue(result.status == Status.ERR_PermissionNotFound);

        retVal2 = new Result<NsDAO.Data>(null,0,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,perm, Access.write);
        Result<List<PermDAO.Data>> retVal3 = new Result<List<PermDAO.Data>>(dataAl,0,"test",NO_PARAM);
        Mockito.doReturn(retVal3).when(permDAO).read(trans, perm);
    
        NsSplit nsObj = new NsSplit("test","test");
        Result<NsSplit> retValNs = new Result<NsSplit>(nsObj,0,"test",NO_PARAM);
        Mockito.doReturn(retValNs).when(ques).deriveNsSplit(Mockito.any(), Mockito.anyString());
    
        Mockito.doReturn(retVal).when(roleDAO).delPerm(Mockito.any(), Mockito.any(), Mockito.any());
    
        result = funcObj.deletePerm(trans, perm, true,false);
        assertNull(result);
    
        Mockito.doReturn(retVal2).when(roleDAO).delPerm(Mockito.any(), Mockito.any(), Mockito.any());
        result = funcObj.deletePerm(trans, perm, true,false);
        assertNull(result);
    
        result = funcObj.deletePerm(trans, perm, false,false);
//        assertTrue(result.status == 1);
    }

    @Test
    public void testDeleteRole() {
        try {
            Define.set(access);
        } catch (CadiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        List<UserRoleDAO.Data> dataAlUser = new ArrayList<UserRoleDAO.Data>();
        UserRoleDAO.Data roleUser = new UserRoleDAO.Data();
        Set<String> rolesSetUser = new HashSet<>();
        rolesSetUser.add("testRole|test|test");
//        perm.roles = rolesSet;
//        perm.type=1
        dataAlUser.add(roleUser);
    
        List<RoleDAO.Data> dataAl = new ArrayList<RoleDAO.Data>();
        RoleDAO.Data role = new RoleDAO.Data();
        Set<String> rolesSet = new HashSet<>();
        rolesSet.add("testRole|test|test");
        role.perms = rolesSet;
//        perm.roles = rolesSet;
//        perm.type=1
        dataAl.add(role);
    
        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,role, Access.write);
    
//        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
//        NsDAO.Data dataObj = new NsDAO.Data();
//        dataObj.type=1;
//        dataAl.add(dataObj);
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());    
//    
//        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(null,0,"test",NO_PARAM);
//        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal.value.get(0), Access.write);
//    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.deleteRole(trans, role, true, false);
        assertTrue(result.status == 1);

        Result<List<RoleDAO.Data>> retVal1 = new Result<List<RoleDAO.Data>>(dataAl,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(roleDAO).read(Mockito.any(), Mockito.any(RoleDAO.Data.class));
        NsSplit splitObj = new NsSplit("test", "test");
        Result<NsSplit> retVal3 = new Result<NsSplit>(splitObj,0,"test",NO_PARAM);
        Mockito.doReturn(retVal3).when(ques).deriveNsSplit(Mockito.any(), Mockito.anyString());
//        Mockito.doReturn(retVal).when(cachedPermDAO).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(retVal).when(permDAO).delRole(Mockito.any(), Mockito.any(),Mockito.any());    
        result = funcObj.deleteRole(trans, role, true, true);
        assertNull(result);
    
        Mockito.doReturn(retVal1).when(permDAO).delRole(Mockito.any(), Mockito.any(),Mockito.any());
        result = funcObj.deleteRole(trans, role, true, true);
        assertNull(result);

        Mockito.doReturn(retVal).when(roleDAO).read(Mockito.any(), Mockito.any(RoleDAO.Data.class));
        result = funcObj.deleteRole(trans, role, true, true);
        assertTrue(result.status == Status.ERR_RoleNotFound);
    
        retVal = new Result<List<UserRoleDAO.Data>>(dataAlUser,0,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());
        result = funcObj.deleteRole(trans, role, false, true);
        assertTrue(result.status == Status.ERR_DependencyExists);
    }

    @Test
    public void testAddPermToRole() {
        List<PermDAO.Data> dataAlPerm = new ArrayList<PermDAO.Data>();
        PermDAO.Data rolePerm = new PermDAO.Data();
        Set<String> rolesSetUser = new HashSet<>();
        rolesSetUser.add("testRole|test|test");
//        perm.roles = rolesSet;
//        perm.type=1
        dataAlPerm.add(rolePerm);
    
        List<RoleDAO.Data> dataAl = new ArrayList<RoleDAO.Data>();
        RoleDAO.Data role = new RoleDAO.Data();
        Set<String> rolesSet = new HashSet<>();
        rolesSet.add("17623");
        role.perms = rolesSet;
//        perm.roles = rolesSet;
//        perm.type=1
        dataAl.add(role);
    
        NsDAO.Data nsObj = new NsDAO.Data();
        nsObj.name="test";
        NsDAO.Data nsObj1 = new NsDAO.Data();
        nsObj1.name="test12";
    
//        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
//        NsDAO.Data dataObj = new NsDAO.Data();
//        dataObj.type=1;
//        dataAl.add(dataObj);
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());    

        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(nsObj,0,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).deriveFirstNsForType(trans, role.ns, NsType.COMPANY);
        Mockito.doReturn(retVal2).when(ques).deriveFirstNsForType(trans, rolePerm.ns, NsType.COMPANY);
    
        Result<NsDAO.Data> retVal3 = new Result<NsDAO.Data>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal3).when(ques).mayUser(trans, null,rolePerm, Access.write);
        Mockito.doReturn(retVal3).when(ques).mayUser(trans, null,role, Access.write);
    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.addPermToRole(trans, role, rolePerm, false);
        assertTrue(result.status == 1);

        retVal2 = new Result<NsDAO.Data>(nsObj,1,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).deriveFirstNsForType(trans, role.ns, NsType.COMPANY);
        Mockito.doReturn(retVal2).when(ques).deriveFirstNsForType(trans, rolePerm.ns, NsType.COMPANY);
        result = funcObj.addPermToRole(trans, role, rolePerm, false);
        assertTrue(result.status == 1);
    
        role.ns="test2";
        retVal2 = new Result<NsDAO.Data>(nsObj,0,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).deriveFirstNsForType(trans, role.ns, NsType.COMPANY);
        result = funcObj.addPermToRole(trans, role, rolePerm, false);
        assertTrue(result.status == 1);
    
        retVal2 = new Result<NsDAO.Data>(nsObj,0,"test1",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).deriveFirstNsForType(trans, role.ns, NsType.COMPANY);
        Result<NsDAO.Data> retVal21 = new Result<NsDAO.Data>(nsObj1,0,"test1",NO_PARAM);
        Mockito.doReturn(retVal21).when(ques).deriveFirstNsForType(trans, rolePerm.ns, NsType.COMPANY);
        result = funcObj.addPermToRole(trans, role, rolePerm, false);
        assertTrue(result.status == 1);
    
        retVal3 = new Result<NsDAO.Data>(null,0,"test",NO_PARAM);
        Mockito.doReturn(retVal3).when(ques).mayUser(trans, null,rolePerm, Access.write);
        retVal2 = new Result<NsDAO.Data>(nsObj,0,"test1",NO_PARAM);
        Mockito.doReturn(retVal2).when(ques).deriveFirstNsForType(trans, role.ns, NsType.COMPANY);
        Mockito.doReturn(retVal2).when(ques).deriveFirstNsForType(trans, rolePerm.ns, NsType.COMPANY);
    
//        Mockito.doReturn(retVal).when(cachedPermDAO).create(Mockito.any(), Mockito.any());
        Mockito.doReturn(retVal).when(permDAO).read(Mockito.any(), Mockito.any(PermDAO.Data.class));    
    
        result = funcObj.addPermToRole(trans, role, rolePerm, false);
        assertTrue(result.status == Status.ERR_PermissionNotFound);
    
        Result<List<PermDAO.Data>> retValPerm= new Result<List<PermDAO.Data>>(dataAlPerm,0,"test1",NO_PARAM);
        Mockito.doReturn(retValPerm).when(permDAO).read(Mockito.any(), Mockito.any(PermDAO.Data.class));
    
        Mockito.doReturn(retVal3).when(roleDAO).read(trans, role);
    
        result = funcObj.addPermToRole(trans, role, rolePerm, true);
        assertTrue(result.status == 22);

        Mockito.doReturn(true).when(trans).requested(REQD_TYPE.force);
        result = funcObj.addPermToRole(trans, role, rolePerm, true);
        assertTrue(result.status == 2);
    
        retVal3 = new Result<NsDAO.Data>(null,0,"test",NO_PARAM);
        Mockito.doReturn(retVal3).when(ques).mayUser(trans, null,role, Access.write);
        Mockito.doReturn(retVal3).when(roleDAO).create(trans, role);
        result = funcObj.addPermToRole(trans, role, rolePerm, true);
//        System.out.println(result.status);
        assertNull(result);
    
        retVal3 = new Result<NsDAO.Data>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal3).when(roleDAO).create(trans, role);
        result = funcObj.addPermToRole(trans, role, rolePerm, true);
        assertTrue(result.status == 1);
    
        Result<List<RoleDAO.Data>> retVal31 = new Result<List<RoleDAO.Data>>(dataAl,0,"test",NO_PARAM);
        Mockito.doReturn(retVal31).when(roleDAO).read(trans, role);
        result = funcObj.addPermToRole(trans, role, rolePerm, true);
        assertTrue(result.status == 7);
    }

    @Test
    public void testDelPermFromRole() {
        List<PermDAO.Data> dataAlPerm = new ArrayList<PermDAO.Data>();
        PermDAO.Data rolePerm = new PermDAO.Data();
        Set<String> rolesSetUser = new HashSet<>();
        rolesSetUser.add("testRole|test|test");
//        perm.roles = rolesSet;
//        perm.type=1
        dataAlPerm.add(rolePerm);
    
        List<RoleDAO.Data> dataAl = new ArrayList<RoleDAO.Data>();
        RoleDAO.Data role = new RoleDAO.Data();
        Set<String> rolesSet = new HashSet<>();
        rolesSet.add("17623");
        role.perms = rolesSet;
        dataAl.add(role);
    
//        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
//        NsDAO.Data dataObj = new NsDAO.Data();
//        dataObj.type=1;
//        dataAl.add(dataObj);
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());    

        Result<NsDAO.Data> retValFail = new Result<NsDAO.Data>(null,1,"test",NO_PARAM);
        Result<NsDAO.Data> retValSuc = new Result<NsDAO.Data>(null,0,"test",NO_PARAM);
        Mockito.doReturn(retValFail).when(ques).mayUser(trans, null,rolePerm, Access.write);
        Mockito.doReturn(retValFail).when(ques).mayUser(trans, null,role, Access.write);
    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.delPermFromRole(trans, role, rolePerm, false);
        assertTrue(result.status == Status.ERR_Denied);
    
        Mockito.doReturn(retValFail).when(ques).mayUser(trans, null,rolePerm, Access.write);
        Mockito.doReturn(retValSuc).when(ques).mayUser(trans, null,role, Access.write);    
    
        Mockito.doReturn(retValFail).when(roleDAO).read(trans, role);
    
        Mockito.doReturn(retVal).when(permDAO).read(Mockito.any(), Mockito.any(PermDAO.Data.class));    
    
        result = funcObj.delPermFromRole(trans, role, rolePerm, false);
        assertTrue(result.status == 1);
    
        Result<List<PermDAO.Data>> retValPermSuc = new Result<List<PermDAO.Data>>(dataAlPerm,0,"test",NO_PARAM);
        Mockito.doReturn(retValPermSuc).when(permDAO).read(Mockito.any(), Mockito.any(PermDAO.Data.class));
        result = funcObj.delPermFromRole(trans, role, rolePerm, false);
        assertTrue(result.status == 1);
    
        Result<List<RoleDAO.Data>> retValRoleSuc = new Result<List<RoleDAO.Data>>(dataAl,0,"test",NO_PARAM);
        Mockito.doReturn(retValRoleSuc).when(roleDAO).read(Mockito.any(), Mockito.any(RoleDAO.Data.class));
        result = funcObj.delPermFromRole(trans, role, rolePerm, true);
        assertTrue(result.status == Status.ERR_PermissionNotFound);
    
        role.perms = null;
        dataAl.add(role);
        rolesSet.add("null|null|null|null");
        role.perms = rolesSet;
        dataAl.add(role);
        Mockito.doReturn(retValRoleSuc).when(roleDAO).read(Mockito.any(), Mockito.any(RoleDAO.Data.class));
        Mockito.doReturn(retVal).when(permDAO).delRole(Mockito.any(), Mockito.any(),Mockito.any(RoleDAO.Data.class));
        result = funcObj.delPermFromRole(trans, role, rolePerm, true);
        assertTrue(result.status == 1);
    
        Mockito.doReturn(true).when(trans).requested(REQD_TYPE.force);
        result = funcObj.delPermFromRole(trans, role, rolePerm, true);
        assertTrue(result.status == 1);
    
        Mockito.doReturn(retValRoleSuc).when(permDAO).delRole(Mockito.any(), Mockito.any(),Mockito.any(RoleDAO.Data.class));
        Mockito.doReturn(retVal).when(roleDAO).delPerm(Mockito.any(), Mockito.any(),Mockito.any(PermDAO.Data.class));    
        result = funcObj.delPermFromRole(trans, role, rolePerm, true);
        assertTrue(result.status == 1);
    
        Mockito.doReturn(retValPermSuc).when(roleDAO).delPerm(Mockito.any(), Mockito.any(),Mockito.any(PermDAO.Data.class));    
        result = funcObj.delPermFromRole(trans, role, rolePerm, true);
        assertTrue(result.status == 0);
    
        Mockito.doReturn(retVal).when(permDAO).read(Mockito.any(), Mockito.any(PermDAO.Data.class));
        result = funcObj.delPermFromRole(trans, role, rolePerm, true);
        assertTrue(result.status == 0);
    
        Mockito.doReturn(retVal).when(roleDAO).delPerm(Mockito.any(), Mockito.any(),Mockito.any(PermDAO.Data.class));
        result = funcObj.delPermFromRole(trans, role, rolePerm, true);
        assertTrue(result.status == 1);

        NsSplit splitObj = new NsSplit("test", "test");
        Result<NsSplit> retVal3 = new Result<NsSplit>(splitObj,0,"test",NO_PARAM);
        Mockito.doReturn(retVal3).when(ques).deriveNsSplit(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(retValFail).when(ques).mayUser(Mockito.any(), Mockito.anyString(),Mockito.any(RoleDAO.Data.class), Mockito.any());
        Mockito.doReturn(retValFail).when(ques).mayUser(Mockito.any(), Mockito.anyString(),Mockito.any(PermDAO.Data.class), Mockito.any());
        result = funcObj.delPermFromRole(trans, "test", rolePerm);
        assertTrue(result.status == 2);
    
        retVal3 = new Result<NsSplit>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal3).when(ques).deriveNsSplit(Mockito.any(), Mockito.anyString());
        result = funcObj.delPermFromRole(trans, "test", rolePerm);
        assertTrue(result.status == 1);
    }
    @Test
    public void testAddUserRole() {
        List<UserRoleDAO.Data> urDataAl = new ArrayList<>();
        UserRoleDAO.Data urData = new UserRoleDAO.Data();
        urData.ns="test";
        urData.rname="test";
        urData.user="test";
        urDataAl.add(urData);
    
        Organization org = Mockito.mock(Organization.class);
        Mockito.doReturn(org).when(trans).org();
        Mockito.doReturn(Mockito.mock(GregorianCalendar.class)).when(org).expiration(Mockito.any(), Mockito.any(), Mockito.anyString());
    
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Result<List<UserRoleDAO.Data>> retValSuc = new Result<List<UserRoleDAO.Data>>(urDataAl,0,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(retVal).when(userRoleDAO).read(Mockito.any(), Mockito.any(UserRoleDAO.Data.class));
        Mockito.doReturn(retVal).when(userRoleDAO).create(Mockito.any(), Mockito.any(UserRoleDAO.Data.class));
        Mockito.doReturn(retValSuc).when(roleDAO).read(Mockito.any(), Mockito.anyString(), Mockito.anyString());    

        Result<List<CredDAO.Data>> retVal2 = new Result<List<CredDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal2).when(credDAO).readID(Mockito.any(), Mockito.anyString());    
    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.addUserRole(trans, urData);
        assertTrue(result.status == 1);
    
        urData.rname=Question.ADMIN;
        result = funcObj.addUserRole(trans, urData);
        assertTrue(result.status == 1);
    
        NsDAO.Data data = new NsDAO.Data();
        data.name="test";
        Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",NO_PARAM);
        Mockito.doReturn(retVal1).when(ques).mayUser(trans, null,retVal1.value, Access.write);
        Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
        try {
            Mockito.doReturn(Mockito.mock(Identity.class)).when(org).getIdentity(trans, "test");
        } catch (OrganizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        urData.rname=Question.OWNER;
        result = funcObj.addUserRole(trans, urData);
        assertTrue(result.status == 1);

        Mockito.doReturn(retValSuc).when(userRoleDAO).create(Mockito.any(), Mockito.any(UserRoleDAO.Data.class));
        result = funcObj.addUserRole(trans, urData);
        assertTrue(result.status == 0);

        Mockito.doReturn(retVal).when(roleDAO).read(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        result = funcObj.addUserRole(trans, urData);
        assertTrue(result.status == Status.ERR_RoleNotFound);
    
        Mockito.doReturn(retValSuc).when(userRoleDAO).read(Mockito.any(), Mockito.any(UserRoleDAO.Data.class));
        result = funcObj.addUserRole(trans, urData);
        assertTrue(result.status == Status.ERR_ConflictAlreadyExists);
    
        result = funcObj.addUserRole(trans, "test", "test", "test");
        assertTrue(result.status == 1);
    
        try {
            Mockito.doReturn(null).when(org).getIdentity(trans, "test");
        } catch (OrganizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        result = funcObj.addUserRole(trans, "test", "test", "test");
        assertTrue(result.status == Result.ERR_BadData);
    
        try {
            Mockito.doThrow(OrganizationException.class).when(org).getIdentity(trans, "test");
        } catch (OrganizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        result = funcObj.addUserRole(trans, "test", "test", "test");
        assertTrue(result.status == 20);
    }

    @Test
    public void testExtendUserRole() {
        List<UserRoleDAO.Data> urDataAl = new ArrayList<>();
        UserRoleDAO.Data urData = new UserRoleDAO.Data();
        urData.ns="test";
        urData.rname="test";
        urData.user="test";
        urData.expires=new Date();
        urDataAl.add(urData);
    
//        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
//        NsDAO.Data dataObj = new NsDAO.Data();
//        dataObj.type=1;
//        dataAl.add(dataObj);
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Result<List<UserRoleDAO.Data>> retValSuc = new Result<List<UserRoleDAO.Data>>(urDataAl,0,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());    
        Mockito.doReturn(retValSuc).when(roleDAO).read(Mockito.any(), Mockito.anyString(), Mockito.anyString());

        Organization org = Mockito.mock(Organization.class);
        Mockito.doReturn(org).when(trans).org();
        Mockito.doReturn(Mockito.mock(GregorianCalendar.class)).when(org).expiration(Mockito.any(), Mockito.any());
    
        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.extendUserRole(trans, urData, false);
        assertNull(result);
    
        Mockito.doReturn(retVal).when(roleDAO).read(Mockito.any(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(retValSuc).when(userRoleDAO).read(trans, urData);
        result = funcObj.extendUserRole(trans, urData, true);
        assertTrue(result.status == Status.ERR_RoleNotFound);
    
        Mockito.doReturn(retVal).when(userRoleDAO).read(trans, urData);
        result = funcObj.extendUserRole(trans, urData, true);
        assertTrue(result.status == Status.ERR_UserRoleNotFound);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetUsersByRole() {
        List<UserRoleDAO.Data> urDataAl = new ArrayList<>();
        UserRoleDAO.Data urData = new UserRoleDAO.Data();
        urData.ns="test";
        urData.rname="test";
        urData.user="test";
        urData.expires=new Date();
        urDataAl.add(urData);
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(urDataAl,0,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());    

        Function funcObj = new Function(trans, ques);
        Result<List<String>> result = funcObj.getUsersByRole(trans, "test", false);
        assertTrue(result.status == 0);

        result = funcObj.getUsersByRole(trans, "test", true);
        assertTrue(result.status == 0);
    
        urData.expires=new Date(130,1,1);
        result = funcObj.getUsersByRole(trans, "test", true);
        assertTrue(result.status == 0);
//    
    }
    @Test
    public void testDelUserRole() {
        List<UserRoleDAO.Data> urDataAl = new ArrayList<>();
        UserRoleDAO.Data urData = new UserRoleDAO.Data();
        urData.ns="test";
        urData.rname="test";
        urData.user="test";
        urData.expires=new Date();
        urDataAl.add(urData);
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(urDataAl,0,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).read(Mockito.any(), Mockito.any( UserRoleDAO.Data.class));    

        Function funcObj = new Function(trans, ques);
        Result<Void> result = funcObj.delUserRole(trans, "test", "test", "test");
        assertNull(result);
    
        retVal = new Result<List<UserRoleDAO.Data>>(urDataAl,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).read(Mockito.any(), Mockito.any( UserRoleDAO.Data.class));
        result = funcObj.delUserRole(trans, "test", "test", "test");
//        assertTrue(result.status ==1);
//    
    }

    @Test
    public void testCreateFuture() {
        FutureDAO.Data data = new FutureDAO.Data();
        data.memo = "test";
        NsDAO.Data nsd = new NsDAO.Data();
        nsd.name = "test";
    
        List<UserRoleDAO.Data> urDataAl = new ArrayList<>();
        UserRoleDAO.Data urData = new UserRoleDAO.Data();
        urData.ns="test";
        urData.rname="test";
        urData.user="test";
        urData.expires=new Date();
        urDataAl.add(urData);
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(urDataAl,0,"test",NO_PARAM);
        Result<List<UserRoleDAO.Data>> retValFail = new Result<List<UserRoleDAO.Data>>(urDataAl,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).read(Mockito.any(), Mockito.any( UserRoleDAO.Data.class));

        Function funcObj = new Function(trans, ques);
        Result<String> result = funcObj.createFuture(trans, data, "test", "test", nsd, FUTURE_OP.A);
        assertTrue(result.status == 20);

        Identity iden=Mockito.mock(Identity.class);
        try {
            Mockito.doReturn(iden).when(org).getIdentity(trans, "test");
            Mockito.doReturn("test").when(iden).mayOwn();
        } catch (OrganizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        FutureDAO.Data futureData = new FutureDAO.Data();
        data.memo = "test";
        Result<FutureDAO.Data> retValFuture = new Result<FutureDAO.Data>(futureData,0,"test",NO_PARAM);
        Mockito.doReturn(retValFuture).when(futureDAO).create(Mockito.any(), Mockito.any( FutureDAO.Data.class), Mockito.anyString());
    
        ApprovalDAO.Data approvalData = new ApprovalDAO.Data();
        data.memo = "test";
        Result<ApprovalDAO.Data> retValApproval = new Result<ApprovalDAO.Data>(approvalData,0,"test",NO_PARAM);
        Mockito.doReturn(retValApproval).when(approvalDAO).create(Mockito.any(), Mockito.any( ApprovalDAO.Data.class));
    
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());
        result = funcObj.createFuture(trans, data, "test", "test", nsd, FUTURE_OP.A);
        assertTrue(result.status == 0);
    
        result = funcObj.createFuture(trans, data, "test", "test", null, FUTURE_OP.A);
        assertTrue(result.status == 20);

        Mockito.doReturn(retValFail).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());
        result = funcObj.createFuture(trans, data, "test", "test", nsd, FUTURE_OP.A);
        assertTrue(result.status == Result.ERR_NotFound);
    
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());
        try {
            Mockito.doReturn(null).when(org).getIdentity(trans, "test");
        } catch (OrganizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        result = funcObj.createFuture(trans, data, "test", "test", nsd, FUTURE_OP.A);
        assertTrue(result.status == Result.ERR_NotFound);
    
        try {
            Mockito.doReturn(iden).when(org).getIdentity(trans, "test");
        } catch (OrganizationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        result = funcObj.createFuture(trans, data, "test", "test", nsd, FUTURE_OP.C);
        assertTrue(result.status == 0);
    
        retValApproval = new Result<ApprovalDAO.Data>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retValApproval).when(approvalDAO).create(Mockito.any(), Mockito.any( ApprovalDAO.Data.class));
        result = funcObj.createFuture(trans, data, "test", "test", nsd, FUTURE_OP.A);
        assertTrue(result.status == 8);
    }
    @Test
    public void testUbLookup() {
        Object[] objArr = new Object[10];
        List<UserRoleDAO.Data> urDataAl = new ArrayList<>();
        UserRoleDAO.Data urData = new UserRoleDAO.Data();
        urData.ns="test";
        urData.rname="test";
        urData.user="test";
        urData.expires=new Date();
        urDataAl.add(urData);
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(urDataAl,0,"test",NO_PARAM);
        Result<List<UserRoleDAO.Data>> retValFail = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).read(trans, objArr);
    
        Function funcObj = new Function(trans, ques);
        funcObj.urDBLookup.get(trans, objArr);
    
        Mockito.doReturn(retValFail).when(userRoleDAO).read(trans, objArr);
        funcObj.urDBLookup.get(trans, objArr);
    }

    @Test
    public void testPerformFutureOp() {
        FutureDAO.Data futureDataDaoObj = new FutureDAO.Data();
        futureDataDaoObj.memo="test";
        futureDataDaoObj.target = "test";
        futureDataDaoObj.id = new UUID(10L,10L);
    
        final List<ApprovalDAO.Data> apprs = new ArrayList<>();
        ApprovalDAO.Data approvalObj = new ApprovalDAO.Data();
        approvalObj.status = "approved";
        approvalObj.type = "owner";
        apprs.add(approvalObj);
        Lookup<List<ApprovalDAO.Data>> lookupApprovalObj = new Lookup<List<ApprovalDAO.Data>>() {
            @Override
            public List<ApprovalDAO.Data> get(AuthzTrans trans, Object ... keys) {
                return apprs;
            }
        };
    
        final UserRoleDAO.Data userObj = new UserRoleDAO.Data();
        Lookup<UserRoleDAO.Data> lookupUserObj = new Lookup<UserRoleDAO.Data>() {
            @Override
            public UserRoleDAO.Data get(AuthzTrans trans, Object ... keys) {
                return userObj;
            }
        };
    
        FutureDAO.Data futureData = new FutureDAO.Data();
//        data.memo = "test";
        Result<FutureDAO.Data> retValFuture = new Result<FutureDAO.Data>(futureData,0,"test",NO_PARAM);
        Mockito.doReturn(retValFuture).when(futureDAO).delete(Mockito.any(), Mockito.any( FutureDAO.Data.class), Mockito.anyBoolean());
    
//        List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
//        NsDAO.Data dataObj = new NsDAO.Data();
//        dataObj.type=1;
//        dataAl.add(dataObj);
        Result<List<UserRoleDAO.Data>> retVal = new Result<List<UserRoleDAO.Data>>(null,1,"test",NO_PARAM);
        Mockito.doReturn(retVal).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());    
//    
//        Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(null,0,"test",NO_PARAM);
//        Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal.value.get(0), Access.write);
//    
        Function funcObj = new Function(trans, ques);
        Result<Function.OP_STATUS> result = funcObj.performFutureOp(trans, FUTURE_OP.A, futureDataDaoObj, lookupApprovalObj, lookupUserObj);
        assertTrue(result.status == 0);
    
        approvalObj.status = "approved";
        approvalObj.type = "supervisor";
        result = funcObj.performFutureOp(trans, FUTURE_OP.A, futureDataDaoObj, lookupApprovalObj, lookupUserObj);
        assertTrue(result.status == 0);
    
        approvalObj.status = "approved";
        approvalObj.type = "";
        result = funcObj.performFutureOp(trans, FUTURE_OP.A, futureDataDaoObj, lookupApprovalObj, lookupUserObj);
        assertTrue(result.status == 0);
    
        approvalObj.status = "pending";
        approvalObj.type = "supervisor";
        result = funcObj.performFutureOp(trans, FUTURE_OP.A, futureDataDaoObj, lookupApprovalObj, lookupUserObj);
        assertTrue(result.status == 0);
    
        approvalObj.status = "pending";
        approvalObj.type = "owner";
        result = funcObj.performFutureOp(trans, FUTURE_OP.A, futureDataDaoObj, lookupApprovalObj, lookupUserObj);
        assertTrue(result.status == 0);
    
        approvalObj.status = "pending";
        approvalObj.type = "";
        result = funcObj.performFutureOp(trans, FUTURE_OP.A, futureDataDaoObj, lookupApprovalObj, lookupUserObj);
        assertTrue(result.status == 0);
    
        approvalObj.status = "denied";
        approvalObj.type = "";
        result = funcObj.performFutureOp(trans, FUTURE_OP.A, futureDataDaoObj, lookupApprovalObj, lookupUserObj);
        assertTrue(result.status == 0);
    
        retValFuture = new Result<FutureDAO.Data>(futureData,1,"test",NO_PARAM);
        Mockito.doReturn(retValFuture).when(futureDAO).delete(Mockito.any(), Mockito.any( FutureDAO.Data.class), Mockito.anyBoolean());
        result = funcObj.performFutureOp(trans, FUTURE_OP.A, futureDataDaoObj, lookupApprovalObj, lookupUserObj);
        System.out.println(result);
        assertTrue(result.status == 0);
//    
    }
}
