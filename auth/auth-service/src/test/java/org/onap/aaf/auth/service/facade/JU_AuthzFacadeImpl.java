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

package org.onap.aaf.auth.service.facade;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
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
import org.onap.aaf.auth.dao.cass.NsType;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.rserv.RServlet;
import org.onap.aaf.auth.service.AuthzService;
import org.onap.aaf.auth.service.mapper.Mapper;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.env.Encryptor;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;

import aaf.v2_0.Api;

public class JU_AuthzFacadeImpl<NSS, PERMS, PERMKEY, ROLES, USERS, USERROLES, DELGS, CERTS, KEYS, REQUEST, HISTORY, ERR, APPROVALS> {

    AuthzFacadeImpl<NSS, PERMS, PERMKEY, ROLES, USERS, USERROLES, DELGS, CERTS, KEYS, REQUEST, HISTORY, ERR, APPROVALS> facadeImplObj;

    @Mock
    AuthzEnv env;
    @Mock
    AuthzTrans trans;
    @Mock
    AuthzService authzService;
    @Mock
    Mapper mapper;

    @Mock
    RosettaDF rossetaObj;

    @Mock
    LogTarget lg;
    @Mock
    TimeTaken tt;
    //
    // NOTE: Annotation format (@Mock and @Spy) do NOT seem to always work as a
    // Base Class,
    // so we construct manually.
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

    @Before
    public void setUp() throws APIException, IOException {
        initMocks(this);

        Mockito.doReturn(lg).when(trans).info();
        Mockito.doReturn(lg).when(trans).error();
        Mockito.doReturn(lg).when(trans).trace();
        Mockito.doReturn(tt).when(trans).start("createNamespace", 40);
        Mockito.doReturn(tt).when(trans).start(Mockito.anyString(),
                Mockito.anyInt());
        Mockito.doReturn(Mockito.mock(Encryptor.class)).when(env).encryptor();
        Mockito.doReturn(mapper).when(authzService).mapper();
        Mockito.doReturn(rossetaObj).when(env).newDataFactory(Mockito.any());
        Mockito.doReturn(rossetaObj).when(rossetaObj).in(Mockito.any());
        Mockito.doReturn(rossetaObj).when(rossetaObj).out(Mockito.any());
        facadeImplObj = new AuthzFacadeImplImpl(env, authzService,
                Data.TYPE.XML);
    }

    @Test
    public void testMapper() {
        facadeImplObj.mapper();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testError() {
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class),
                Mockito.mock(Result.class));

        Result<?> rs = new Result(null, Result.ERR_ActionNotCompleted, "test",
                new Object[2]);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, Result.ERR_Policy, "test",
                new Object[]{"test", "test"});
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, Result.ERR_Security, "test",
                new Object[]{"", "test"});
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, Result.ERR_Denied, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, 33, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, 21, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, 22, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);
        rs = new Result(null, 23, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);
        rs = new Result(null, 24, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);
        rs = new Result(null, 25, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, 26, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, 27, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, 28, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, 4, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, 5, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, 6, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, 7, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, 31, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, 32, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);

        rs = new Result(null, 9, "test", null);
        facadeImplObj.error(trans, Mockito.mock(HttpServletResponse.class), rs);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRequestNS() {
        RosettaData<?> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<Void> retVal = (Result<Void>) facadeImplObj.requestNS(trans,
                Mockito.mock(HttpServletRequest.class),
                Mockito.mock(HttpServletResponse.class), NsType.APP);
        assertTrue(retVal.status == Status.ERR_BadData);

        Data<?> rd = Mockito.mock(Data.class);
        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.requestNS(trans,
                Mockito.mock(HttpServletRequest.class),
                Mockito.mock(HttpServletResponse.class), NsType.APP);
        assertTrue(retVal.status == 20);

        Result<Void> rsVoid = new Result<Void>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).createNS(Mockito.any(),
                Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.requestNS(trans,
                Mockito.mock(HttpServletRequest.class),
                Mockito.mock(HttpServletResponse.class), NsType.APP);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<Void>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).createNS(Mockito.any(),
                Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.requestNS(trans,
                Mockito.mock(HttpServletRequest.class),
                Mockito.mock(HttpServletResponse.class), NsType.APP);
        assertTrue(retVal.status == Status.OK);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.requestNS(trans,
                Mockito.mock(HttpServletRequest.class),
                Mockito.mock(HttpServletResponse.class), NsType.APP);
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddAdminToNS() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj.addAdminToNS(trans,
                respObj, "ns", "id");
        assertTrue(retVal.status == 20);

        Result<Void> rsVoid = new Result<Void>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).addAdminNS(trans, "ns",
                "id");
        retVal = (Result<Void>) facadeImplObj.addAdminToNS(trans, respObj, "ns",
                "id");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<Void>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).addAdminNS(trans, "ns",
                "id");
        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.addAdminToNS(trans, respObj, "ns",
                "id");
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDelAdminFromNS() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj.delAdminFromNS(trans,
                respObj, "ns", "id");
        assertTrue(retVal.status == 20);

        Result<Void> rsVoid = new Result<Void>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).delAdminNS(trans, "ns",
                "id");
        retVal = (Result<Void>) facadeImplObj.delAdminFromNS(trans, respObj,
                "ns", "id");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<Void>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).delAdminNS(trans, "ns",
                "id");
        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.delAdminFromNS(trans, respObj,
                "ns", "id");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testAddResponsibilityForNS() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj
                .addResponsibilityForNS(trans, respObj, "ns", "id");
        assertTrue(retVal.status == 20);

        Result<Void> rsVoid = new Result<Void>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).addResponsibleNS(trans,
                "ns", "id");
        retVal = (Result<Void>) facadeImplObj.addResponsibilityForNS(trans,
                respObj, "ns", "id");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<Void>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).addResponsibleNS(trans,
                "ns", "id");
        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.addResponsibilityForNS(trans,
                respObj, "ns", "id");
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDelResponsibilityForNS() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj
                .delResponsibilityForNS(trans, respObj, "ns", "id");
        assertTrue(retVal.status == 20);

        Result<Void> rsVoid = new Result<Void>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).delResponsibleNS(trans,
                "ns", "id");
        retVal = (Result<Void>) facadeImplObj.delResponsibilityForNS(trans,
                respObj, "ns", "id");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<Void>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).delResponsibleNS(trans,
                "ns", "id");
        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.delResponsibilityForNS(trans,
                respObj, "ns", "id");
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetNSsByName() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj.getNSsByName(trans,
                respObj, "ns", true);
        assertTrue(retVal.status == 20);

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getNSbyName(trans, "ns",
                true);
        retVal = (Result<Void>) facadeImplObj.getNSsByName(trans, respObj, "ns",
                true);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getNSbyName(trans, "ns",
                true);
        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RosettaData<NSS> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        retVal = (Result<Void>) facadeImplObj.getNSsByName(trans, respObj, "ns",
                true);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getNSsByName(trans, respObj, "ns",
                true);
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetNSsByAdmin() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj.getNSsByAdmin(trans,
                respObj, "ns", true);
        assertTrue(retVal.status == 20);

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getNSbyAdmin(trans, "ns",
                true);
        retVal = (Result<Void>) facadeImplObj.getNSsByAdmin(trans, respObj,
                "ns", true);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getNSbyAdmin(trans, "ns",
                true);
        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RosettaData<NSS> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        retVal = (Result<Void>) facadeImplObj.getNSsByAdmin(trans, respObj,
                "ns", true);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getNSsByAdmin(trans, respObj,
                "ns", true);
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetNSsByResponsible() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj
                .getNSsByResponsible(trans, respObj, "ns", true);
        assertTrue(retVal.status == 20);

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getNSbyResponsible(trans,
                "ns", true);
        retVal = (Result<Void>) facadeImplObj.getNSsByResponsible(trans,
                respObj, "ns", true);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getNSbyResponsible(trans,
                "ns", true);
        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RosettaData<NSS> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        retVal = (Result<Void>) facadeImplObj.getNSsByResponsible(trans,
                respObj, "ns", true);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getNSsByResponsible(trans,
                respObj, "ns", true);
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetNSsByEither() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj.getNSsByEither(trans,
                respObj, "ns", true);
        assertTrue(retVal.status == 20);

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getNSbyEither(trans, "ns",
                true);
        retVal = (Result<Void>) facadeImplObj.getNSsByEither(trans, respObj,
                "ns", true);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getNSbyEither(trans, "ns",
                true);
        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RosettaData<NSS> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        retVal = (Result<Void>) facadeImplObj.getNSsByEither(trans, respObj,
                "ns", true);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getNSsByEither(trans, respObj,
                "ns", true);
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetNSsChildren() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj.getNSsChildren(trans,
                respObj, "ns");
        assertTrue(retVal.status == 20);

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getNSsChildren(trans, "ns");
        retVal = (Result<Void>) facadeImplObj.getNSsChildren(trans, respObj,
                "ns");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getNSsChildren(trans, "ns");
        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RosettaData<NSS> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        retVal = (Result<Void>) facadeImplObj.getNSsChildren(trans, respObj,
                "ns");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getNSsChildren(trans, respObj,
                "ns");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testUpdateNsDescription() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj.updateNsDescription(
                trans, Mockito.mock(HttpServletRequest.class), respObj);
        assertTrue(retVal.status == 20);

        RosettaData<?> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.updateNsDescription(trans,
                Mockito.mock(HttpServletRequest.class), respObj);
        assertTrue(retVal.status == 4);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.updateNsDescription(trans,
                Mockito.mock(HttpServletRequest.class), respObj);
        assertTrue(retVal.status == 20);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.updateNsDescription(trans,
                Mockito.mock(HttpServletRequest.class), respObj);
        assertTrue(retVal.status == 20);

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .updateNsDescription(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.updateNsDescription(trans,
                Mockito.mock(HttpServletRequest.class), respObj);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .updateNsDescription(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.updateNsDescription(trans,
                Mockito.mock(HttpServletRequest.class), respObj);
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteNS() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj.deleteNS(trans,
                Mockito.mock(HttpServletRequest.class), respObj, "ns");
        assertTrue(retVal.status == 20);

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).deleteNS(Mockito.any(),
                Mockito.any());
        retVal = (Result<Void>) facadeImplObj.deleteNS(trans,
                Mockito.mock(HttpServletRequest.class), respObj, "ns");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).deleteNS(Mockito.any(),
                Mockito.any());
        retVal = (Result<Void>) facadeImplObj.deleteNS(trans,
                Mockito.mock(HttpServletRequest.class), respObj, "ns");
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateAttribForNS() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj
                .createAttribForNS(trans, respObj, "ns", "key", "value");
        assertTrue(retVal.status == 20);

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).createNsAttrib(trans, "ns",
                "key", "value");
        retVal = (Result<Void>) facadeImplObj.createAttribForNS(trans, respObj,
                "ns", "key", "value");
        assertTrue(retVal.status == 31);

        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).createNsAttrib(trans, "ns",
                "key", "value");
        retVal = (Result<Void>) facadeImplObj.createAttribForNS(trans, respObj,
                "ns", "key", "value");
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testReadNsByAttrib() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj.readNsByAttrib(trans,
                respObj, "key");
        assertTrue(retVal.status == 20);

        Result<KEYS> rsVoid = new Result<KEYS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).readNsByAttrib(trans,
                "key");
        retVal = (Result<Void>) facadeImplObj.readNsByAttrib(trans, respObj,
                "key");
        assertTrue(retVal.status == 31);

        RosettaData<KEYS> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rsVoid = new Result<KEYS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).readNsByAttrib(trans,
                "key");
        retVal = (Result<Void>) facadeImplObj.readNsByAttrib(trans, respObj,
                "key");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.readNsByAttrib(trans, respObj,
                "key");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testUpdAttribForNS() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj.updAttribForNS(trans,
                respObj, "ns", "key", "value");
        assertTrue(retVal.status == 20);

        Result<KEYS> rsVoid = new Result<KEYS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).updateNsAttrib(trans, "ns",
                "key", "value");
        retVal = (Result<Void>) facadeImplObj.updAttribForNS(trans, respObj,
                "ns", "key", "value");
        assertTrue(retVal.status == 31);

        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rsVoid = new Result<KEYS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).updateNsAttrib(trans, "ns",
                "key", "value");
        retVal = (Result<Void>) facadeImplObj.updAttribForNS(trans, respObj,
                "ns", "key", "value");
        assertTrue(retVal.status == 0);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDelAttribForNS() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj.delAttribForNS(trans,
                respObj, "ns", "key");
        assertTrue(retVal.status == 20);

        Result<KEYS> rsVoid = new Result<KEYS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).deleteNsAttrib(trans, "ns",
                "key");
        retVal = (Result<Void>) facadeImplObj.delAttribForNS(trans, respObj,
                "ns", "key");
        assertTrue(retVal.status == 31);

        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rsVoid = new Result<KEYS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).deleteNsAttrib(trans, "ns",
                "key");
        retVal = (Result<Void>) facadeImplObj.delAttribForNS(trans, respObj,
                "ns", "key");
        assertTrue(retVal.status == 0);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreatePerm() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        RosettaData<?> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<Void> retVal = (Result<Void>) facadeImplObj.createPerm(trans,
                Mockito.mock(HttpServletRequest.class), respObj);
        assertTrue(retVal.status == 4);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.createPerm(trans,
                Mockito.mock(HttpServletRequest.class), respObj);
        assertTrue(retVal.status == 20);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.createPerm(trans,
                Mockito.mock(HttpServletRequest.class), respObj);
        assertTrue(retVal.status == 20);

        Result<Void> rsVoid = new Result<Void>(null, 0, "test", new Object[0]);
        try {
            Mockito.when(authzService.createPerm(trans, dataObj.asObject()))
                    .thenReturn(rsVoid);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.createPerm(trans,
                Mockito.mock(HttpServletRequest.class), respObj);
        assertTrue(retVal.status == 0);

        rsVoid = new Result<Void>(null, 31, "test", new Object[0]);
        try {
            Mockito.when(authzService.createPerm(trans, dataObj.asObject()))
                    .thenReturn(rsVoid);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.createPerm(trans,
                Mockito.mock(HttpServletRequest.class), respObj);
        assertTrue(retVal.status == 31);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetPermsByType() {
        RosettaData<PERMS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.getPermsByType(trans,
                respObj, "perm");
        assertTrue(retVal.status == 20);

        Result<PERMS> rsVoid = new Result<PERMS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getPermsByType(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.getPermsByType(trans, respObj,
                "perm");
        assertTrue(retVal.status == 31);

        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rsVoid = new Result<PERMS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getPermsByType(Mockito.any(), Mockito.any());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getPermsByType(trans, respObj,
                "perm");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        Mockito.doReturn(Mockito.mock(Encryptor.class)).when(env).encryptor();
        retVal = (Result<Void>) facadeImplObj.getPermsByType(trans, respObj,
                "perm");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPermsByName() {
        RosettaData<PERMS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.getPermsByName(trans,
                respObj, "type", "instance", "action");
        assertTrue(retVal.status == 20);

        Result<PERMS> rsVoid = new Result<PERMS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getPermsByName(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.getPermsByName(trans, respObj,
                "type", "instance", "action");
        assertTrue(retVal.status == 31);

        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rsVoid = new Result<PERMS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getPermsByName(
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getPermsByName(trans, respObj,
                "type", "instance", "action");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        Mockito.doReturn(Mockito.mock(Encryptor.class)).when(env).encryptor();
        retVal = (Result<Void>) facadeImplObj.getPermsByName(trans, respObj,
                "type", "instance", "action");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPermsByUser() {
        RosettaData<PERMS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.getPermsByUser(trans,
                respObj, "user");
        assertTrue(retVal.status == 20);

        Result<PERMS> rsVoid = new Result<PERMS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getPermsByUser(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.getPermsByUser(trans, respObj,
                "user");
        assertTrue(retVal.status == 31);

        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rsVoid = new Result<PERMS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getPermsByUser(Mockito.any(), Mockito.any());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getPermsByUser(trans, respObj,
                "user");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        Mockito.doReturn(Mockito.mock(Encryptor.class)).when(env).encryptor();
        retVal = (Result<Void>) facadeImplObj.getPermsByUser(trans, respObj,
                "user");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetPermsByUserScope() {
        RosettaData<PERMS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj
                .getPermsByUserScope(trans, respObj, "user", new String[]{});
        assertTrue(retVal.status == 20);

        Result<PERMS> rsVoid = new Result<PERMS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getPermsByUserScope(trans,
                "user", new String[]{});
        retVal = (Result<Void>) facadeImplObj.getPermsByUserScope(trans,
                respObj, "user", new String[]{});
        assertTrue(retVal.status == 31);

        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rsVoid = new Result<PERMS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getPermsByUserScope(trans,
                "user", new String[]{});
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getPermsByUserScope(trans,
                respObj, "user", new String[]{});
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        Mockito.doReturn(Mockito.mock(Encryptor.class)).when(env).encryptor();
        retVal = (Result<Void>) facadeImplObj.getPermsByUserScope(trans,
                respObj, "user", new String[]{});
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetPermsByUserWithAAFQuery() {
        RosettaData<PERMS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj
                .getPermsByUserWithAAFQuery(trans, reqObj, respObj, "user");
        assertTrue(retVal.status == 20);

        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getPermsByUserWithAAFQuery(trans,
                reqObj, respObj, "user");
        assertTrue(retVal.status == Status.ERR_BadData);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getPermsByUserWithAAFQuery(trans,
                reqObj, respObj, "user");
        assertTrue(retVal.status == 20);

        Result<PERMS> rsVoid = new Result<PERMS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getPermsByUser(
                Mockito.any(), Mockito.any(), Mockito.anyString());
        retVal = (Result<Void>) facadeImplObj.getPermsByUserWithAAFQuery(trans,
                reqObj, respObj, "user");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<PERMS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getPermsByUser(
                Mockito.any(), Mockito.any(), Mockito.anyString());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getPermsByUserWithAAFQuery(trans,
                reqObj, respObj, "user");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getPermsByUserWithAAFQuery(trans,
                reqObj, respObj, "user");
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetPermsForRole() {
        RosettaData<PERMS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj
                .getPermsForRole(trans, respObj, "roleName");
        assertTrue(retVal.status == 20);

        Result<PERMS> rsVoid = new Result<PERMS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getPermsByRole(Mockito.any(), Mockito.anyString());
        retVal = (Result<Void>) facadeImplObj.getPermsForRole(trans, respObj,
                "roleName");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<PERMS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getPermsByRole(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getPermsForRole(trans, respObj,
                "roleName");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getPermsForRole(trans, respObj,
                "roleName");
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetPermsByNS() {
        RosettaData<PERMS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.getPermsByNS(trans,
                respObj, "ns");
        assertTrue(retVal.status == 20);

        Result<PERMS> rsVoid = new Result<PERMS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getPermsByNS(Mockito.any(),
                Mockito.anyString());
        retVal = (Result<Void>) facadeImplObj.getPermsByNS(trans, respObj,
                "ns");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<PERMS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getPermsByNS(Mockito.any(),
                Mockito.anyString());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getPermsByNS(trans, respObj,
                "ns");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getPermsByNS(trans, respObj,
                "ns");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testRenamePerm() {
        RosettaData<PERMS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.renamePerm(trans,
                reqObj, respObj, "origType", "origInstance", "origAction");
        assertTrue(retVal.status == 20);

        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.renamePerm(trans, reqObj, respObj,
                "origType", "origInstance", "origAction");
        assertTrue(retVal.status == Status.ERR_BadData);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.renamePerm(trans, reqObj, respObj,
                "origType", "origInstance", "origAction");
        assertTrue(retVal.status == 20);

        Result<PERMS> rsVoid = new Result<PERMS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).renamePerm(Mockito.any(),
                Mockito.any(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());
        retVal = (Result<Void>) facadeImplObj.renamePerm(trans, reqObj, respObj,
                "origType", "origInstance", "origAction");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<PERMS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).renamePerm(Mockito.any(),
                Mockito.any(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.renamePerm(trans, reqObj, respObj,
                "origType", "origInstance", "origAction");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.renamePerm(trans, reqObj, respObj,
                "origType", "origInstance", "origAction");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testUpdatePermDescription() {
        RosettaData<PERMS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj
                .updatePermDescription(trans, reqObj, respObj);
        assertTrue(retVal.status == 20);

        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.updatePermDescription(trans,
                reqObj, respObj);
        assertTrue(retVal.status == Status.ERR_BadData);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.updatePermDescription(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 20);

        Result<PERMS> rsVoid = new Result<PERMS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .updatePermDescription(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.updatePermDescription(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<PERMS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .updatePermDescription(Mockito.any(), Mockito.any());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.updatePermDescription(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.updatePermDescription(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testResetPermRoles() {
        RosettaData<PERMS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.resetPermRoles(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 20);

        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.resetPermRoles(trans, reqObj,
                respObj);
        assertTrue(retVal.status == Status.ERR_BadData);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.resetPermRoles(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 20);

        Result<PERMS> rsVoid = new Result<PERMS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .resetPermRoles(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.resetPermRoles(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<PERMS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .resetPermRoles(Mockito.any(), Mockito.any());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.resetPermRoles(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.resetPermRoles(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testDeletePerm() {
        RosettaData<PERMS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.deletePerm(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 20);

        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.deletePerm(trans, reqObj,
                respObj);
        assertTrue(retVal.status == Status.ERR_BadData);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.deletePerm(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 20);

        Result<PERMS> rsVoid = new Result<PERMS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).deletePerm(Mockito.any(),
                Mockito.any());
        retVal = (Result<Void>) facadeImplObj.deletePerm(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<PERMS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).deletePerm(Mockito.any(),
                Mockito.any());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.deletePerm(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.deletePerm(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testDeletePermWithTypeInstanceAction() {
        RosettaData<PERMS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.deletePerm(trans,
                respObj, "type", "instance", "action");
        assertTrue(retVal.status == 20);

        Result<PERMS> rsVoid = new Result<PERMS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).deletePerm(Mockito.any(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        retVal = (Result<Void>) facadeImplObj.deletePerm(trans, respObj, "type",
                "instance", "action");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<PERMS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).deletePerm(Mockito.any(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.deletePerm(trans, respObj, "type",
                "instance", "action");
        assertTrue(retVal.status == 0);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateRole() {
        RosettaData<PERMS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.createRole(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 20);

        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.createRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == Status.ERR_BadData);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.createRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 20);

        Result<PERMS> rsVoid = new Result<PERMS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).createRole(Mockito.any(),
                Mockito.any());
        retVal = (Result<Void>) facadeImplObj.createRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<PERMS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).createRole(Mockito.any(),
                Mockito.any());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.createRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.createRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetRolesByName() {
        RosettaData<ROLES> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.getRolesByName(trans,
                respObj, "role");
        assertTrue(retVal.status == 20);

        Result<ROLES> rsVoid = new Result<ROLES>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getRolesByName(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.getRolesByName(trans, respObj,
                "role");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<ROLES>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getRolesByName(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getRolesByName(trans, respObj,
                "role");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getRolesByName(trans, respObj,
                "role");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetRolesByUser() {
        RosettaData<ROLES> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.getRolesByUser(trans,
                respObj, "user");
        assertTrue(retVal.status == 20);

        Result<ROLES> rsVoid = new Result<ROLES>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getRolesByUser(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.getRolesByUser(trans, respObj,
                "user");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<ROLES>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getRolesByUser(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getRolesByUser(trans, respObj,
                "user");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getRolesByUser(trans, respObj,
                "user");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetRolesByNS() {
        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        RosettaData<ROLES> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.getRolesByNS(trans,
                respObj, "ns");
        assertTrue(retVal.status == 20);

        Result<ROLES> rsVoid = new Result<ROLES>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getRolesByNS(Mockito.any(),
                Mockito.any());
        retVal = (Result<Void>) facadeImplObj.getRolesByNS(trans, respObj,
                "ns");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<ROLES>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getRolesByNS(Mockito.any(),
                Mockito.anyString());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getRolesByNS(trans, respObj,
                "ns");
        assertTrue(retVal.status == 0);

        rsVoid = Mockito.mock(Result.class);
        Mockito.doReturn(false).when(rsVoid).isEmpty();
        Mockito.doReturn(rsVoid).when(authzService).getRolesByNS(Mockito.any(),
                Mockito.anyString());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getRolesByNS(trans, respObj,
                "ns");
        assertTrue(retVal.status == 0);

        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getRolesByNS(trans, respObj,
                "ns");
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetRolesByNameOnly() {
        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        RosettaData<ROLES> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj
                .getRolesByNameOnly(trans, respObj, "ns");
        assertTrue(retVal.status == 20);

        Result<ROLES> rsVoid = new Result<ROLES>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getRolesByNameOnly(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.getRolesByNameOnly(trans, respObj,
                "ns");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<ROLES>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getRolesByNameOnly(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getRolesByNameOnly(trans, respObj,
                "ns");
        assertTrue(retVal.status == 0);

        rsVoid = Mockito.mock(Result.class);
        Mockito.doReturn(false).when(rsVoid).isEmpty();
        Mockito.doReturn(rsVoid).when(authzService)
                .getRolesByNameOnly(Mockito.any(), Mockito.anyString());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getRolesByNameOnly(trans, respObj,
                "ns");
        assertTrue(retVal.status == 0);

        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getRolesByNameOnly(trans, respObj,
                "ns");
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetRolesByPerm() {
        RosettaData<ROLES> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.getRolesByPerm(trans,
                respObj, "type", "instance", "action");
        assertTrue(retVal.status == 20);

        Result<ROLES> rsVoid = new Result<ROLES>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getRolesByPerm(trans,
                "type", "instance", "action");
        retVal = (Result<Void>) facadeImplObj.getRolesByPerm(trans, respObj,
                "type", "instance", "action");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<ROLES>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getRolesByPerm(trans,
                "type", "instance", "action");
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getRolesByPerm(trans, respObj,
                "type", "instance", "action");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getRolesByPerm(trans, respObj,
                "type", "instance", "action");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testUpdateRoleDescription() {
        RosettaData<PERMS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj
                .updateRoleDescription(trans, reqObj, respObj);
        assertTrue(retVal.status == 20);

        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.updateRoleDescription(trans,
                reqObj, respObj);
        assertTrue(retVal.status == Status.ERR_BadData);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.updateRoleDescription(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 20);

        Result<PERMS> rsVoid = new Result<PERMS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .updateRoleDescription(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.updateRoleDescription(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<PERMS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .updateRoleDescription(Mockito.any(), Mockito.any());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.updateRoleDescription(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.updateRoleDescription(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testAddPermToRole() {
        RosettaData<REQUEST> dataObj = Mockito.mock(RosettaData.class);
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.addPermToRole(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 20);

        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.addPermToRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == Status.ERR_BadData);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.addPermToRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 20);

        Result<REQUEST> rsRequest = new Result<REQUEST>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsRequest).when(authzService)
                .addPermToRole(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.addPermToRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 31);

        rsRequest = new Result<REQUEST>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsRequest).when(authzService)
                .addPermToRole(Mockito.any(), Mockito.any());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsRequest.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.addPermToRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.addPermToRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testDelPermFromRole() {
        RosettaData<REQUEST> dataObj = Mockito.mock(RosettaData.class);
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj
                .delPermFromRole(trans, reqObj, respObj);
        assertTrue(retVal.status == 20);

        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.delPermFromRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == Status.ERR_BadData);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.delPermFromRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 20);

        Result<REQUEST> rsRequest = new Result<REQUEST>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsRequest).when(authzService)
                .delPermFromRole(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.delPermFromRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 31);

        rsRequest = new Result<REQUEST>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsRequest).when(authzService)
                .delPermFromRole(Mockito.any(), Mockito.any());
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsRequest.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.delPermFromRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.delPermFromRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testDelPermFromRoleWithAddlParams() {
        RosettaData<REQUEST> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.delPermFromRole(
                trans, respObj, "role", "type", "instance", "action");
        assertTrue(retVal.status == 20);

        Result<REQUEST> rsRequest = new Result<REQUEST>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsRequest).when(authzService).delPermFromRole(trans,
                "role", "type", "instance", "action");
        retVal = (Result<Void>) facadeImplObj.delPermFromRole(trans, respObj,
                "role", "type", "instance", "action");
        assertTrue(retVal.status == 31);

        rsRequest = new Result<REQUEST>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsRequest).when(authzService).delPermFromRole(trans,
                "role", "type", "instance", "action");
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsRequest.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.delPermFromRole(trans, respObj,
                "role", "type", "instance", "action");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.delPermFromRole(trans, respObj,
                "role", "type", "instance", "action");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteRole() {
        RosettaData<REQUEST> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.deleteRole(trans,
                respObj, "role");
        assertTrue(retVal.status == 20);

        Result<REQUEST> rsRequest = new Result<REQUEST>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsRequest).when(authzService).deleteRole(trans,
                "role");
        retVal = (Result<Void>) facadeImplObj.deleteRole(trans, respObj,
                "role");
        assertTrue(retVal.status == 31);

        rsRequest = new Result<REQUEST>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsRequest).when(authzService).deleteRole(trans,
                "role");
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsRequest.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.deleteRole(trans, respObj,
                "role");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.deleteRole(trans, respObj,
                "role");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteRole2() {
        RosettaData<Void> dataObj = Mockito.mock(RosettaData.class);
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.deleteRole(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 20);

        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.deleteRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == Status.ERR_BadData);

        Result<Void> rsRequest = new Result<Void>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsRequest).when(authzService).deleteRole(trans, null);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.deleteRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 20);

        rsRequest = new Result<Void>(null, 0, "test", new Object[0]);
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsRequest.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.deleteRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 20);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.deleteRole(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 20);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateUserCred() {
        RosettaData<REQUEST> dataObj = Mockito.mock(RosettaData.class);
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.createUserCred(trans,
                reqObj);
        assertTrue(retVal.status == 20);

        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.createUserCred(trans, reqObj);
        assertTrue(retVal.status == Status.ERR_BadData);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<Void> rsRequest = new Result<Void>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsRequest).when(authzService)
                .createUserCred(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.createUserCred(trans, reqObj);

        assertTrue(retVal.status == 31);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.createUserCred(trans, reqObj);
        assertTrue(retVal.status == 31);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testChangeUserCred() {
        RosettaData<REQUEST> dataObj = Mockito.mock(RosettaData.class);
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.changeUserCred(trans,
                reqObj);
        assertTrue(retVal.status == 20);

        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.changeUserCred(trans, reqObj);
        assertTrue(retVal.status == Status.ERR_BadData);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<Void> rsRequest = new Result<Void>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsRequest).when(authzService)
                .resetUserCred(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.changeUserCred(trans, reqObj);

        assertTrue(retVal.status == 31);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.changeUserCred(trans, reqObj);
        assertTrue(retVal.status == 31);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExtendUserCred() {
        RosettaData<REQUEST> dataObj = Mockito.mock(RosettaData.class);
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.extendUserCred(trans,
                reqObj, "10");
        assertTrue(retVal.status == 20);

        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.extendUserCred(trans, reqObj,
                "10");
        assertTrue(retVal.status == Status.ERR_BadData);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<Void> rsRequest = new Result<Void>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsRequest).when(authzService).extendUserCred(
                Mockito.any(), Mockito.any(), Mockito.anyString());
        retVal = (Result<Void>) facadeImplObj.extendUserCred(trans, reqObj,
                "10");

        assertTrue(retVal.status == 31);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.extendUserCred(trans, reqObj,
                "10");
        assertTrue(retVal.status == 31);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetCredsByNS() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj.getCredsByNS(trans,
                respObj, "ns");
        assertTrue(retVal.status == 20);

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getCredsByNS(trans, "ns");
        retVal = (Result<Void>) facadeImplObj.getCredsByNS(trans, respObj,
                "ns");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getCredsByNS(trans, "ns");
        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RosettaData<NSS> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        retVal = (Result<Void>) facadeImplObj.getCredsByNS(trans, respObj,
                "ns");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getCredsByNS(trans, respObj,
                "ns");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetCredsByID() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj.getCredsByID(trans,
                respObj, "id");
        assertTrue(retVal.status == 20);

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getCredsByID(trans, "id");
        retVal = (Result<Void>) facadeImplObj.getCredsByID(trans, respObj,
                "id");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getCredsByID(trans, "id");
        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        RosettaData<NSS> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        retVal = (Result<Void>) facadeImplObj.getCredsByID(trans, respObj,
                "id");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getCredsByID(trans, respObj,
                "id");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteUserCred() {
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        Result<Void> retVal = (Result<Void>) facadeImplObj.deleteUserCred(trans,
                reqObj);
        assertTrue(retVal.status == 20);

        RosettaData<REQUEST> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.deleteUserCred(trans, reqObj);
        assertTrue(retVal.status == 4);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .deleteUserCred(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.deleteUserCred(trans, reqObj);
        assertTrue(retVal.status == 31);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.deleteUserCred(trans, reqObj);
        assertTrue(retVal.status == 31);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testDoesCredentialMatch() {
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        RosettaData<REQUEST> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<Date> retVal = (Result<Date>) facadeImplObj
                .doesCredentialMatch(trans, reqObj, respObj);
        assertTrue(retVal.status == 4);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .doesCredentialMatch(Mockito.any(), Mockito.any());
        retVal = (Result<Date>) facadeImplObj.doesCredentialMatch(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 31);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Date>) facadeImplObj.doesCredentialMatch(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 31);

        try {
            Mockito.doThrow(new IOException("test exception")).when(reqObj)
                    .getInputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Date>) facadeImplObj.doesCredentialMatch(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 20);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testValidBasicAuth() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).validateBasicAuth(trans,
                "basicAuth");
        Result<Void> retVal = (Result<Void>) facadeImplObj.validBasicAuth(trans,
                respObj, "basicAuth");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).validateBasicAuth(trans,
                "basicAuth");
        retVal = (Result<Void>) facadeImplObj.validBasicAuth(trans, respObj,
                "basicAuth");
        assertTrue(retVal.status == 20);

        try {
            Mockito.doReturn(Mockito.mock(ServletOutputStream.class))
                    .when(respObj).getOutputStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).validateBasicAuth(trans,
                "basicAuth");
        retVal = (Result<Void>) facadeImplObj.validBasicAuth(trans, respObj,
                "basicAuth");
        assertTrue(retVal.status == 0);

    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetCertInfoByID() {
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getCertInfoByID(trans,
                reqObj, "id");
        Result<Void> retVal = (Result<Void>) facadeImplObj
                .getCertInfoByID(trans, reqObj, respObj, "id");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getCertInfoByID(trans,
                reqObj, "id");
        retVal = (Result<Void>) facadeImplObj.getCertInfoByID(trans, reqObj,
                respObj, "id");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        RosettaData<NSS> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getCertInfoByID(trans, reqObj,
                respObj, "id");
        assertTrue(retVal.status == 0);

        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getCertInfoByID(trans, reqObj,
                respObj, "id");
        assertTrue(retVal.status == 20);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateDelegate() {
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.createDelegate(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 20);

        RosettaData<REQUEST> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doReturn(Mockito.mock(ServletInputStream.class))
                    .when(reqObj).getInputStream();

            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<Void> rsVoid = new Result<Void>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .createDelegate(Mockito.any(), Mockito.anyObject());

        retVal = (Result<Void>) facadeImplObj.createDelegate(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());

        rsVoid = new Result<Void>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .createDelegate(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.createDelegate(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);

    }
    @Test
    @SuppressWarnings("unchecked")
    public void testUpdateDelegate() {
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.updateDelegate(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 20);

        RosettaData<REQUEST> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doReturn(Mockito.mock(ServletInputStream.class))
                    .when(reqObj).getInputStream();

            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<Void> rsVoid = new Result<Void>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .updateDelegate(Mockito.any(), Mockito.anyObject());

        retVal = (Result<Void>) facadeImplObj.updateDelegate(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());

        rsVoid = new Result<Void>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .updateDelegate(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.updateDelegate(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteDelegate() {
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<Void> retVal = (Result<Void>) facadeImplObj.deleteDelegate(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 20);

        RosettaData<REQUEST> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doReturn(Mockito.mock(ServletInputStream.class))
                    .when(reqObj).getInputStream();

            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<Void> rsVoid = new Result<Void>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .deleteDelegate(Mockito.any(), Mockito.anyObject());

        retVal = (Result<Void>) facadeImplObj.deleteDelegate(trans, reqObj,
                respObj);
        assertNull(retVal);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());

        rsVoid = new Result<Void>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .deleteDelegate(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.deleteDelegate(trans, reqObj,
                respObj);
        assertNull(retVal);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteDelegate2() {

        Result<Void> rsVoid = new Result<Void>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).deleteDelegate(trans,
                "userName");

        Result<Void> retVal = (Result<Void>) facadeImplObj.deleteDelegate(trans,
                "userName");
        assertTrue(retVal.status == 0);

        Mockito.doThrow(new RuntimeException("test exception"))
                .when(authzService).deleteDelegate(trans, "userName");
        retVal = (Result<Void>) facadeImplObj.deleteDelegate(trans, "userName");
        assertTrue(retVal.status == 20);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetDelegatesByUser() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getDelegatesByUser(trans,
                "user");
        Result<Void> retVal = (Result<Void>) facadeImplObj
                .getDelegatesByUser(trans, "user", respObj);
        assertTrue(retVal.status == 31);

        RosettaData<NSS> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getDelegatesByUser(trans,
                "user");
        retVal = (Result<Void>) facadeImplObj.getDelegatesByUser(trans, "user",
                respObj);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getDelegatesByUser(trans, "user",
                respObj);
        assertTrue(retVal.status == 0);

        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getDelegatesByUser(trans, "user",
                respObj);
        assertTrue(retVal.status == 20);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetDelegatesByDelegate() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Result<NSS> rsVoid = new Result<NSS>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getDelegatesByDelegate(trans, "delegate");
        Result<Void> retVal = (Result<Void>) facadeImplObj
                .getDelegatesByDelegate(trans, "delegate", respObj);
        assertTrue(retVal.status == 31);

        RosettaData<NSS> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rsVoid = new Result<NSS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getDelegatesByDelegate(trans, "delegate");
        retVal = (Result<Void>) facadeImplObj.getDelegatesByDelegate(trans,
                "delegate", respObj);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getDelegatesByDelegate(trans,
                "delegate", respObj);
        assertTrue(retVal.status == 0);

        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getDelegatesByDelegate(trans,
                "delegate", respObj);
        assertTrue(retVal.status == 20);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRequestUserRole() {
        RosettaData<?> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doThrow(new APIException("test exception")).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<Void> retVal = (Result<Void>) facadeImplObj.requestUserRole(
                trans, Mockito.mock(HttpServletRequest.class),
                Mockito.mock(HttpServletResponse.class));
        assertTrue(retVal.status == Status.ERR_BadData);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.requestUserRole(trans,
                Mockito.mock(HttpServletRequest.class),
                Mockito.mock(HttpServletResponse.class));
        assertTrue(retVal.status == 20);

        Result<Void> rsVoid = new Result<Void>(null, 31, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .createUserRole(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.requestUserRole(trans,
                Mockito.mock(HttpServletRequest.class),
                Mockito.mock(HttpServletResponse.class));
        assertTrue(retVal.status == 31);

        rsVoid = new Result<Void>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .createUserRole(Mockito.any(), Mockito.any());
        retVal = (Result<Void>) facadeImplObj.requestUserRole(trans,
                Mockito.mock(HttpServletRequest.class),
                Mockito.mock(HttpServletResponse.class));
        assertTrue(retVal.status == Status.OK);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.requestUserRole(trans,
                Mockito.mock(HttpServletRequest.class),
                Mockito.mock(HttpServletResponse.class));
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetUserInRole() {
        RosettaData<USERS> dataObj = Mockito.mock(RosettaData.class);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<Void> retVal = (Result<Void>) facadeImplObj.getUserInRole(trans,
                Mockito.mock(HttpServletResponse.class), "user", "role");
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getUserInRole(trans, "user",
                "role");
        retVal = (Result<Void>) facadeImplObj.getUserInRole(trans,
                Mockito.mock(HttpServletResponse.class), "user", "role");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getUserInRole(trans, "user",
                "role");
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getUserInRole(trans,
                Mockito.mock(HttpServletResponse.class), "user", "role");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getUserInRole(trans,
                Mockito.mock(HttpServletResponse.class), "user", "role");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetUserRolesByUser() {
        RosettaData<USERS> dataObj = Mockito.mock(RosettaData.class);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<Void> retVal = (Result<Void>) facadeImplObj.getUserRolesByUser(
                trans, Mockito.mock(HttpServletResponse.class), "user");
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getUserRolesByUser(trans,
                "user");
        retVal = (Result<Void>) facadeImplObj.getUserRolesByUser(trans,
                Mockito.mock(HttpServletResponse.class), "user");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getUserRolesByUser(trans,
                "user");
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getUserRolesByUser(trans,
                Mockito.mock(HttpServletResponse.class), "user");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getUserRolesByUser(trans,
                Mockito.mock(HttpServletResponse.class), "user");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetUserRolesByRole() {
        RosettaData<USERS> dataObj = Mockito.mock(RosettaData.class);

        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<Void> retVal = (Result<Void>) facadeImplObj.getUserRolesByRole(
                trans, Mockito.mock(HttpServletResponse.class), "role");
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getUserRolesByRole(trans,
                "role");
        retVal = (Result<Void>) facadeImplObj.getUserRolesByRole(trans,
                Mockito.mock(HttpServletResponse.class), "role");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getUserRolesByRole(trans,
                "role");
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        retVal = (Result<Void>) facadeImplObj.getUserRolesByRole(trans,
                Mockito.mock(HttpServletResponse.class), "role");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getUserRolesByRole(trans,
                Mockito.mock(HttpServletResponse.class), "role");
        assertTrue(retVal.status == 0);
    }

    @Test
    public void testExtendUserRoleExpiration() {

        Mockito.doThrow(new RuntimeException("test exception"))
                .when(authzService).extendUserRole(trans, "user", "role");
        Result<Void> retVal = (Result<Void>) facadeImplObj
                .extendUserRoleExpiration(trans,
                        Mockito.mock(HttpServletResponse.class), "user",
                        "role");
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).extendUserRole(trans,
                "user", "role");
        retVal = (Result<Void>) facadeImplObj.extendUserRoleExpiration(trans,
                Mockito.mock(HttpServletResponse.class), "user", "role");
        assertTrue(retVal.status == 31);

    }
    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteUserRole() {

        Mockito.doThrow(new RuntimeException("test exception"))
                .when(authzService).deleteUserRole(trans, "user", "role");
        Result<Void> retVal = (Result<Void>) facadeImplObj.deleteUserRole(trans,
                Mockito.mock(HttpServletResponse.class), "user", "role");
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).deleteUserRole(trans,
                "user", "role");
        retVal = (Result<Void>) facadeImplObj.deleteUserRole(trans,
                Mockito.mock(HttpServletResponse.class), "user", "role");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).deleteUserRole(trans,
                "user", "role");

        retVal = (Result<Void>) facadeImplObj.deleteUserRole(trans,
                Mockito.mock(HttpServletResponse.class), "user", "role");
        assertTrue(retVal.status == 0);

    }
    @Test
    @SuppressWarnings("unchecked")
    public void testUpdateApproval() {
        RosettaData<USERS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        HttpServletRequest reqObj = Mockito.mock(HttpServletRequest.class);

        Mockito.doReturn(dataObj).when(rossetaObj).newData();
        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(ServletInputStream.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<Void> retVal = (Result<Void>) facadeImplObj.updateApproval(trans,
                reqObj, respObj);
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .updateApproval(Mockito.any(), Mockito.anyObject());
        retVal = (Result<Void>) facadeImplObj.updateApproval(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .updateApproval(Mockito.any(), Mockito.anyObject());
        retVal = (Result<Void>) facadeImplObj.updateApproval(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.updateApproval(trans, reqObj,
                respObj);
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetApprovalsByUser() {
        RosettaData<USERS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        Result<Void> retVal = (Result<Void>) facadeImplObj
                .getApprovalsByUser(trans, respObj, "user");
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn(rsVoid).when(authzService).getApprovalsByUser(trans,
                "user");
        retVal = (Result<Void>) facadeImplObj.getApprovalsByUser(trans, respObj,
                "user");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getApprovalsByUser(trans,
                "user");
        retVal = (Result<Void>) facadeImplObj.getApprovalsByUser(trans, respObj,
                "user");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getApprovalsByUser(trans, respObj,
                "user");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetApprovalsByApprover() {
        RosettaData<USERS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        Result<Void> retVal = (Result<Void>) facadeImplObj
                .getApprovalsByApprover(trans, respObj, "approver");
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn(rsVoid).when(authzService)
                .getApprovalsByApprover(trans, "approver");
        retVal = (Result<Void>) facadeImplObj.getApprovalsByApprover(trans,
                respObj, "approver");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService)
                .getApprovalsByApprover(trans, "approver");
        retVal = (Result<Void>) facadeImplObj.getApprovalsByApprover(trans,
                respObj, "approver");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getApprovalsByApprover(trans,
                respObj, "approver");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetApprovalsByTicket() {
        RosettaData<USERS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        Result<Void> retVal = (Result<Void>) facadeImplObj
                .getApprovalsByTicket(trans, respObj, "ticket");
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn(rsVoid).when(authzService).getApprovalsByTicket(trans,
                "ticket");
        retVal = (Result<Void>) facadeImplObj.getApprovalsByTicket(trans,
                respObj, "ticket");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getApprovalsByTicket(trans,
                "ticket");
        retVal = (Result<Void>) facadeImplObj.getApprovalsByTicket(trans,
                respObj, "ticket");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getApprovalsByTicket(trans,
                respObj, "ticket");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetUsersByRole() {
        RosettaData<USERS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        Result<Void> retVal = (Result<Void>) facadeImplObj.getUsersByRole(trans,
                respObj, "role");
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn(rsVoid).when(authzService).getUsersByRole(trans,
                "role");
        retVal = (Result<Void>) facadeImplObj.getUsersByRole(trans, respObj,
                "role");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getUsersByRole(trans,
                "role");
        retVal = (Result<Void>) facadeImplObj.getUsersByRole(trans, respObj,
                "role");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getUsersByRole(trans, respObj,
                "role");
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetUsersByPermission() {
        RosettaData<USERS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        Result<Void> retVal = (Result<Void>) facadeImplObj.getUsersByPermission(
                trans, respObj, "type", "instance", "action");
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn(rsVoid).when(authzService).getUsersByPermission(trans,
                "type", "instance", "action");
        retVal = (Result<Void>) facadeImplObj.getUsersByPermission(trans,
                respObj, "type", "instance", "action");
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getUsersByPermission(trans,
                "type", "instance", "action");
        retVal = (Result<Void>) facadeImplObj.getUsersByPermission(trans,
                respObj, "type", "instance", "action");
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getUsersByPermission(trans,
                respObj, "type", "instance", "action");
        assertTrue(retVal.status == 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetHistoryByUser() {
        RosettaData<USERS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        Result<Void> retVal = (Result<Void>) facadeImplObj
                .getHistoryByUser(trans, respObj, "user", new int[]{201907}, 1);
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn(rsVoid).when(authzService).getHistoryByUser(trans,
                "user", new int[]{201907}, 1);
        retVal = (Result<Void>) facadeImplObj.getHistoryByUser(trans, respObj,
                "user", new int[]{201907}, 1);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getHistoryByUser(trans,
                "user", new int[]{201907}, 1);
        retVal = (Result<Void>) facadeImplObj.getHistoryByUser(trans, respObj,
                "user", new int[]{201907}, 1);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getHistoryByUser(trans, respObj,
                "user", new int[]{201907}, 1);
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetHistoryByRole() {
        RosettaData<USERS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        Result<Void> retVal = (Result<Void>) facadeImplObj
                .getHistoryByRole(trans, respObj, "role", new int[]{201907}, 1);
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn(rsVoid).when(authzService).getHistoryByRole(trans,
                "role", new int[]{201907}, 1);
        retVal = (Result<Void>) facadeImplObj.getHistoryByRole(trans, respObj,
                "role", new int[]{201907}, 1);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getHistoryByRole(trans,
                "role", new int[]{201907}, 1);
        retVal = (Result<Void>) facadeImplObj.getHistoryByRole(trans, respObj,
                "role", new int[]{201907}, 1);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getHistoryByRole(trans, respObj,
                "role", new int[]{201907}, 1);
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetHistoryByNS() {
        RosettaData<USERS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        Result<Void> retVal = (Result<Void>) facadeImplObj.getHistoryByNS(trans,
                respObj, "ns", new int[]{201907}, 1);
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn(rsVoid).when(authzService).getHistoryByNS(trans, "ns",
                new int[]{201907}, 1);
        retVal = (Result<Void>) facadeImplObj.getHistoryByNS(trans, respObj,
                "ns", new int[]{201907}, 1);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getHistoryByNS(trans, "ns",
                new int[]{201907}, 1);
        retVal = (Result<Void>) facadeImplObj.getHistoryByNS(trans, respObj,
                "ns", new int[]{201907}, 1);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getHistoryByNS(trans, respObj,
                "ns", new int[]{201907}, 1);
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetHistoryByPerm() {
        RosettaData<USERS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        Result<Void> retVal = (Result<Void>) facadeImplObj
                .getHistoryByPerm(trans, respObj, "perm", new int[]{201907}, 1);
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn(rsVoid).when(authzService).getHistoryByPerm(trans,
                "perm", new int[]{201907}, 1);
        retVal = (Result<Void>) facadeImplObj.getHistoryByPerm(trans, respObj,
                "perm", new int[]{201907}, 1);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getHistoryByPerm(trans,
                "perm", new int[]{201907}, 1);
        retVal = (Result<Void>) facadeImplObj.getHistoryByPerm(trans, respObj,
                "perm", new int[]{201907}, 1);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getHistoryByPerm(trans, respObj,
                "perm", new int[]{201907}, 1);
        assertTrue(retVal.status == 0);
    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetHistoryBySubject() {
        RosettaData<USERS> dataObj = Mockito.mock(RosettaData.class);
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);

        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        Result<Void> retVal = (Result<Void>) facadeImplObj.getHistoryBySubject(
                trans, respObj, "subject", "target", new int[]{201907}, 1);
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        try {
            Mockito.doReturn(dataObj).when(dataObj).load(rsVoid.value);
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Mockito.doReturn(rsVoid).when(authzService).getHistoryBySubject(trans,
                "subject", "target", new int[]{201907}, 1);
        retVal = (Result<Void>) facadeImplObj.getHistoryBySubject(trans,
                respObj, "subject", "target", new int[]{201907}, 1);
        assertTrue(retVal.status == 31);

        rsVoid = new Result<USERS>(null, 0, "test", new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).getHistoryBySubject(trans,
                "subject", "target", new int[]{201907}, 1);
        retVal = (Result<Void>) facadeImplObj.getHistoryBySubject(trans,
                respObj, "subject", "target", new int[]{201907}, 1);
        assertTrue(retVal.status == 0);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        retVal = (Result<Void>) facadeImplObj.getHistoryBySubject(trans,
                respObj, "subject", "target", new int[]{201907}, 1);
        assertTrue(retVal.status == 0);
    }
    @Test
    public void testCacheClear() {

        Mockito.doThrow(new RuntimeException("test exception"))
                .when(authzService).cacheClear(trans, "cname");
        Result<Void> retVal = (Result<Void>) facadeImplObj.cacheClear(trans,
                "cname");
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).cacheClear(trans, "cname");
        retVal = (Result<Void>) facadeImplObj.cacheClear(trans, "cname");
        assertTrue(retVal.status == 31);

    }
    @Test
    public void testCacheClear2() {

        Mockito.doThrow(new RuntimeException("test exception"))
                .when(authzService)
                .cacheClear(trans, "cname", new int[]{1, -1});
        Result<Void> retVal = (Result<Void>) facadeImplObj.cacheClear(trans,
                "cname", "1,a");
        assertTrue(retVal.status == 20);

        Result<USERS> rsVoid = new Result<USERS>(null, 31, "test",
                new Object[0]);
        Mockito.doReturn(rsVoid).when(authzService).cacheClear(trans, "cname",
                new int[]{1, 1});
        retVal = (Result<Void>) facadeImplObj.cacheClear(trans, "cname", "1,1");
        assertTrue(retVal.status == 31);

    }

    @Test
    public void testDbReset() {
        facadeImplObj.dbReset(trans);
    }
    @Test
    public void testGetAPI() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        RServlet<AuthzTrans> rservlet = Mockito.mock(RServlet.class);
        facadeImplObj.getAPI(trans, respObj, rservlet);

        RosettaData<Api> dataObj = Mockito.mock(RosettaData.class);
        Mockito.doReturn(dataObj).when(rossetaObj).newData(trans);
        try {
            Mockito.doReturn(dataObj).when(dataObj)
                    .load(Mockito.any(Api.class));
        } catch (APIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        facadeImplObj.getAPI(trans, respObj, rservlet);

        Mockito.doReturn("user").when(trans).user();
        Mockito.doReturn(env).when(trans).env();
        Question.specialLogOn(trans, trans.user());
        facadeImplObj.getAPI(trans, respObj, rservlet);
    }
    @Test
    public void testGetAPIExample() {
        HttpServletResponse respObj = Mockito.mock(HttpServletResponse.class);
        facadeImplObj.getAPIExample(trans, respObj, "nameOrContentType", true);
    }
}
class AuthzFacadeImplImpl extends AuthzFacadeImpl {

    public AuthzFacadeImplImpl(AuthzEnv env, AuthzService service,
            TYPE dataType) throws APIException {
        super(env, service, dataType);
    }

}
