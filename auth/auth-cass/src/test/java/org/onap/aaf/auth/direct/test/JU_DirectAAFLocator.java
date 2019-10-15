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
package org.onap.aaf.auth.direct.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.dao.cass.LocateDAO.Data;
import org.onap.aaf.auth.direct.DirectAAFLocator;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;

import junit.framework.Assert;


@RunWith(MockitoJUnitRunner.class)
public class JU_DirectAAFLocator {

    @Mock
    LocateDAO ldao;

    @Mock
    AuthzEnv env;

    @Mock
    AuthzTrans trans;

    @Mock
    PropAccess access;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void testConstructorVersionExcpetion() {
        Mockito.doReturn(access).when(env).access();
        Mockito.doReturn("20").when(access).getProperty(Config.CADI_LATITUDE,null);
        Mockito.doReturn("20").when(access).getProperty(Config.CADI_LONGITUDE,null);
        Mockito.doReturn("20").when(access).getProperty(Config.AAF_LOCATOR_CONTAINER,"");
        try {
            DirectAAFLocator aafLocatorObj=new DirectAAFLocator(env, ldao,"test","test");
        } catch (LocatorException e) {
//            System.out.println(e.getMessage());
            assertEquals("Invalid Version String: test", e.getMessage());
        }
        try {
            DirectAAFLocator aafLocatorObj=new DirectAAFLocator(env, ldao,"test","20.test");
        } catch (LocatorException e) {
//            System.out.println(e.getMessage());
            assertEquals("Invalid Version String: 20.test", e.getMessage());
        }try {
            DirectAAFLocator aafLocatorObj=new DirectAAFLocator(env, ldao,"test","20.20.test");
        } catch (LocatorException e) {
//            System.out.println(e.getMessage());
            assertEquals("Invalid Version String: 20.20.test", e.getMessage());
        }try {
            DirectAAFLocator aafLocatorObj=new DirectAAFLocator(env, ldao,"test","20.20.20.test");
        } catch (LocatorException e) {
//            System.out.println(e.getMessage());
            assertEquals("Invalid Version String: 20.20.20.test", e.getMessage());
        }
    }

    @Test
    public void testConstructorExcpetion() {
        Mockito.doReturn(access).when(env).access();
        Mockito.doReturn("20").when(access).getProperty(Config.CADI_LATITUDE,null);
        Mockito.doReturn("20").when(access).getProperty(Config.CADI_LONGITUDE,null);
        Mockito.doReturn("").when(access).getProperty(Config.AAF_LOCATOR_CONTAINER,"");
        Mockito.doReturn("").when(access).getProperty(Config.AAF_LOCATOR_CONTAINER_NS,"");
        Mockito.doReturn("20").when(access).getProperty(Config.AAF_LOCATOR_APP_NS,"AAF_NS");
        try {
            DirectAAFLocator aafLocatorObj=new DirectAAFLocator(env, ldao,"test",null);
        } catch (LocatorException e) {
//            System.out.println(e.getMessage());
        }
    }

    // NOTE: These mocks to not well represent the DirectAAFLocator Class.
    @Test
    public void testConstructorUriExcpetion() {
        Mockito.doReturn(access).when(env).access();
        Mockito.doReturn("20").when(access).getProperty(Config.CADI_LATITUDE,null);
        Mockito.doReturn("20").when(access).getProperty(Config.CADI_LONGITUDE,null);
        Mockito.doReturn("").when(access).getProperty(Config.AAF_LOCATOR_CONTAINER,"");
        Mockito.doReturn("").when(access).getProperty(Config.AAF_LOCATOR_CONTAINER_NS,"");
        Mockito.doReturn("20 30").when(access).getProperty(Config.AAF_URL,null);
        try {
            DirectAAFLocator aafLocatorObj=new DirectAAFLocator(env, ldao,"test","192.0.0.1");
        } catch (LocatorException e) {
//            System.out.println(e.getMessage());
            assertTrue(e.getMessage().contains("Illegal character in path at index 2:"));
        }
    }


    @Test
    public void testRefresh() {

        DirectAAFLocator aafLocatorObj=null;
        PropAccess access = Mockito.mock(PropAccess.class);
        Mockito.doReturn(access).when(env).access();
        Mockito.doReturn(trans).when(env).newTransNoAvg();
        Mockito.doReturn("20").when(access).getProperty(Config.CADI_LATITUDE,null);
        Mockito.doReturn("20").when(access).getProperty(Config.CADI_LONGITUDE,null);
        Mockito.doReturn("").when(access).getProperty(Config.AAF_LOCATOR_CONTAINER,"");
        Mockito.doReturn("").when(access).getProperty(Config.AAF_LOCATOR_CONTAINER_NS,"");
        Mockito.doReturn("http://aafurl.com").when(access).getProperty(Config.AAF_URL,null);
        try {
            aafLocatorObj = new DirectAAFLocator(env, ldao,"test","30.20.30.30");
        } catch (LocatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<List<Data>> retVal1 = new Result<List<Data>>(null,0,"",new Object[0]);

        Data data= new Data();
        data.major=30;
        data.minor=30;
        data.patch=30;
        data.pkg=30;
        retVal1.value = new ArrayList<Data>();
        retVal1.value.add(data);

        Mockito.doReturn(retVal1).when(ldao).readByName(trans,"test");
        boolean retVal = aafLocatorObj.refresh();
//        System.out.println(retVal);
        assertTrue(retVal);
    }

    @Test
    public void testRefreshNOK() {

        DirectAAFLocator aafLocatorObj=null;
        PropAccess access = Mockito.mock(PropAccess.class);
        Mockito.doReturn(access).when(env).access();
        Mockito.doReturn(trans).when(env).newTransNoAvg();
        Mockito.doReturn("20").when(access).getProperty(Config.CADI_LATITUDE,null);
        Mockito.doReturn("20").when(access).getProperty(Config.CADI_LONGITUDE,null);
        Mockito.doReturn("").when(access).getProperty(Config.AAF_LOCATOR_CONTAINER,"");
        Mockito.doReturn("").when(access).getProperty(Config.AAF_LOCATOR_CONTAINER_NS,"");
        Mockito.doReturn("http://aafurl.com").when(access).getProperty(Config.AAF_URL,null);
        try {
            aafLocatorObj = new DirectAAFLocator(env, ldao,"test","30.20.30.30");
        } catch (LocatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Result<List<Data>> retVal1 = new Result<List<Data>>(null,1,"",new String[0]);

        Mockito.doReturn(retVal1).when(ldao).readByName(trans,"test");
        boolean retVal = aafLocatorObj.refresh();
//        System.out.println(retVal);
        assertFalse(retVal);
    }

}