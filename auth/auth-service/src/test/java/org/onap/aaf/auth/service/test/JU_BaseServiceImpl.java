/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.service.test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.cached.CachedCertDAO;
import org.onap.aaf.auth.dao.cached.CachedCredDAO;
import org.onap.aaf.auth.dao.cached.CachedNSDAO;
import org.onap.aaf.auth.dao.cached.CachedPermDAO;
import org.onap.aaf.auth.dao.cached.CachedRoleDAO;
import org.onap.aaf.auth.dao.cached.CachedUserRoleDAO;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.CacheInfoDAO;
import org.onap.aaf.auth.dao.cass.DelegateDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.HistoryDAO;
import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.service.AuthzCassServiceImpl;
import org.onap.aaf.auth.service.mapper.Mapper_2_0;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.org.DefaultOrg;
import org.onap.aaf.org.DefaultOrgIdentity;

import aaf.v2_0.Approvals;
import aaf.v2_0.Certs;
import aaf.v2_0.Delgs;
import aaf.v2_0.Error;
import aaf.v2_0.History;
import aaf.v2_0.Keys;
import aaf.v2_0.Nss;
import aaf.v2_0.Perms;
import aaf.v2_0.Pkey;
import aaf.v2_0.Request;
import aaf.v2_0.Roles;
import aaf.v2_0.UserRoles;
import aaf.v2_0.Users;

@RunWith(MockitoJUnitRunner.class)
public abstract class JU_BaseServiceImpl {
    protected AuthzCassServiceImpl<Nss, Perms, Pkey, Roles, Users, UserRoles, Delgs, Certs, Keys, Request, History, Error, Approvals> 
        acsi;
    protected Mapper_2_0 mapper;

    @Mock
    protected DefaultOrg org;
    @Mock
    protected DefaultOrgIdentity orgIdentity;

//
// NOTE: Annotation format (@Mock and @Spy) do NOT seem to always work as a Base Class,
//       so we construct manually.
//
// Mock Objects
    protected HistoryDAO historyDAO = mock(HistoryDAO.class);
    protected CacheInfoDAO cacheInfoDAO = mock(CacheInfoDAO.class);
    protected CachedNSDAO nsDAO = mock(CachedNSDAO.class);
    protected CachedPermDAO permDAO = mock(CachedPermDAO.class);
    protected CachedRoleDAO roleDAO = mock(CachedRoleDAO.class);
    protected CachedUserRoleDAO userRoleDAO = mock(CachedUserRoleDAO.class);
    protected CachedCredDAO credDAO = mock(CachedCredDAO.class);
    protected CachedCertDAO certDAO = mock(CachedCertDAO.class);
    protected LocateDAO locateDAO = mock(LocateDAO.class);
    protected FutureDAO futureDAO = mock(FutureDAO.class);
    protected DelegateDAO delegateDAO = mock(DelegateDAO.class);
    protected ApprovalDAO approvalDAO = mock(ApprovalDAO.class);

 // Spy Objects
    @Spy
    protected static PropAccess access = new PropAccess();
    @Spy
    protected static AuthzEnv env = new AuthzEnv(access);
    @Spy
    protected static AuthzTrans trans = env.newTransNoAvg();

    // @Spy doesn't seem to work on Question.
    @Spy
    protected Question question = spy(new Question(trans,
                historyDAO,cacheInfoDAO,nsDAO,permDAO,
                roleDAO,userRoleDAO,credDAO,certDAO,
                locateDAO,futureDAO,delegateDAO,approvalDAO));

    public void setUp() throws Exception {
        when(trans.org()).thenReturn(org);
        when(org.getDomain()).thenReturn("org.onap");
        Define.set(access);
        access.setProperty(Config.CADI_LATITUDE, "38.0");
        access.setProperty(Config.CADI_LONGITUDE, "-72.0");
    
        mapper = new Mapper_2_0(question);
        acsi = new AuthzCassServiceImpl<>(trans, mapper, question);
    }

    //////////
    //  Common Data Objects
    /////////
    protected List<NsDAO.Data> nsData(String name) {
        NsDAO.Data ndd = new NsDAO.Data();
        ndd.name=name;
        int dot = name.lastIndexOf('.');
        if(dot<0) {
            ndd.parent=".";
        } else {
            ndd.parent=name.substring(0,dot);
        }
        List<NsDAO.Data> rv = new ArrayList<NsDAO.Data>();
        rv.add(ndd);
        return rv;
    }

    /**
     * Setup Role Data for Mock Usages
     * @param trans
     * @param user
     * @param ns
     * @param role
     * @param exists
     * @param days
     */
    protected void whenRole(AuthzTrans trans, String user, String ns, String role, boolean exists, int days) {
        Result<List<UserRoleDAO.Data>> result;
        if(exists) {
            result = Result.ok(listOf(urData(user,ns,role,days)));
        } else {
            result = Result.ok(emptyList(UserRoleDAO.Data.class));
        }
        when(question.userRoleDAO().read(trans, user, ns+'.'+role)).thenReturn(result);
    }

    protected UserRoleDAO.Data urData(String user, String ns, String rname, int days) {
        UserRoleDAO.Data urdd = new UserRoleDAO.Data();
        urdd.user = user;
        urdd.ns = ns;
        urdd.rname = rname;
        urdd.role = ns + '.' + rname;
        GregorianCalendar gc = new GregorianCalendar();
        gc.add(GregorianCalendar.DAY_OF_YEAR, days);
        urdd.expires = gc.getTime();
        return urdd;
    }


    protected <T> List<T> listOf(T t) {
        List<T> list = new ArrayList<>();
        list.add(t);
        return list;
    }

    protected <T> List<T> emptyList(Class<T> cls) {
        return new ArrayList<>();
    }

}
