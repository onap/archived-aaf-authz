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

package org.onap.aaf.auth.cm;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

import javax.servlet.Filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.cm.ca.CA;
import org.onap.aaf.auth.cm.facade.Facade1_0;
import org.onap.aaf.auth.cm.facade.FacadeFactory;
import org.onap.aaf.auth.cm.mapper.Mapper.API;
import org.onap.aaf.auth.cm.service.CMService;
import org.onap.aaf.auth.cm.service.Code;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTransImpl;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.v2_0.AAFConHttp;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.impl.BasicEnv;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FacadeFactory.class)
public class JU_AAF_CM {

    @Mock
    AuthzEnv env;

    BasicEnv baseEnv;

    @Mock
    PropAccess access;

    AuthzTransImpl1 trans;

    AAF_CMImpl rosettaObj = null;

    @Before
    public void setUp() {
        initMocks(this);
    
        try {
            Mockito.doReturn(access).when(env).access();
            Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
            Properties props=new Properties();
            Mockito.doReturn(props).when(access).getProperties();
            props.setProperty("cm_ca.props", "test");
            Mockito.doReturn("test:2.1").when(access).getProperty(Config.AAF_LOCATOR_ENTRIES, null);
            Mockito.doReturn("test").when(access).getProperty("https.protocols","TLSv1.1,TLSv1.2");
            Mockito.doReturn("test").when(env).getProperty("cm_ca.props.perm_type",null);
            Mockito.doReturn("test").when(env).getProperty("cm_ca.props.baseSubject",null);
            Mockito.doReturn("10").when(env).getProperty("CACHE_CLEAN_INTERVAL","60000");
            Mockito.doReturn("10").when(env).getProperty("CACHE_HIGH_COUNT","5000");
            trans = new AuthzTransImpl1(env);
            Mockito.doReturn(trans).when(env).newTrans();
//            Mockito.doReturn("test").when(trans).getProperty("cm_ca.props.baseSubject",null);
//            Mockito.doReturn(Mockito.mock(TimeTaken.class)).when(trans).start("Clear Reset Deque",8);
        
            Mockito.doReturn("TLSv1.1").when(access).getProperty("cadi_protocols","test");
            Mockito.doReturn("https://www.google.com").when(access).getProperty(Config.AAF_URL,null);
            Mockito.doReturn("test").when(env).getProperty(Config.AAF_ENV);
            Mockito.doReturn("10").when(env).getProperty(Config.CADI_LATITUDE);
            Mockito.doReturn("10").when(env).getProperty(Config.CADI_LONGITUDE);
            Mockito.doReturn("org.onap.aaf.auth.cm.LocalCAImpl,test;test").when(env).getProperty("cm_ca.props");
            Mockito.doReturn("google.com").when(env).getProperty("cassandra.clusters",null);
//            Mockito.doReturn(Mockito.mock(AuthzTransImpl.class)).when(env).newTrans();
            Mockito.doReturn(Mockito.mock(LogTarget.class)).when(env).init();
            AAF_CM tempObj = Mockito.mock(AAF_CM.class);
            Field envField = tempObj.getClass().getField("env");
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(envField, envField.getModifiers() & ~Modifier.FINAL);
            envField.setAccessible(true);
            envField.set(tempObj, env);
            RosettaDF rosettaObjTemp = Mockito.mock(RosettaDF.class);
            Mockito.doReturn(rosettaObjTemp).when(rosettaObjTemp).in(Data.TYPE.JSON);
            Mockito.doReturn(rosettaObjTemp).when(env).newDataFactory(aaf.v2_0.Error.class);
            Mockito.doReturn(rosettaObjTemp).when(env).newDataFactory(certman.v1_0.CertificateRequest.class);
            Mockito.doReturn(rosettaObjTemp).when(env).newDataFactory(certman.v1_0.CertificateRenew.class);
            Mockito.doReturn(rosettaObjTemp).when(env).newDataFactory(certman.v1_0.CertificateDrop.class);
            Mockito.doReturn(rosettaObjTemp).when(env).newDataFactory(certman.v1_0.CertInfo.class);
            Mockito.doReturn(rosettaObjTemp).when(env).newDataFactory(certman.v1_0.Artifacts.class);
            Mockito.doReturn(Data.TYPE.XML).when(rosettaObjTemp).getOutType();

            Facade1_0 facadeObj = Mockito.mock(Facade1_0.class);
            PowerMockito.mockStatic(FacadeFactory.class);
            FacadeFactory factObj = PowerMockito.mock(FacadeFactory.class);
            PowerMockito.when(factObj.v1_0(tempObj,trans, null,Data.TYPE.JSON)).thenReturn(facadeObj);
        
//            Mockito.doReturn(Mockito.mock(Mapper.class)).when(facadeObj).mapper();

        
            rosettaObj = new AAF_CMImpl(env);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(e instanceof NullPointerException);
        }
    }

    @Test
    public void testTestCA() {
        CA obj = rosettaObj.getCA("props");
        assertTrue(obj instanceof CA);
    }

//    @Test
//    public void testRoute() {
//        try {
//            rosettaObj.route(null, "", null, null);
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
////        System.out.println(obj);
////        assertTrue(obj instanceof CA);
//    }

    @Test
    public void testFilters() {
        try {
            Filter[] obj = rosettaObj._filters(new Object[] {"props"});
            System.out.println(obj);
        } catch (CadiException | LocatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            assertTrue(e.getMessage().contains("Error initializing Context: TLS"));
        }
//        assertTrue(obj instanceof CA);
    }

    class AAF_CMImpl extends AAF_CM{

        public AAF_CMImpl(AuthzEnv env) throws Exception {
            super(env);
            // TODO Auto-generated constructor stub
        }
    
        @Override
        public synchronized AAFConHttp aafCon() throws CadiException, LocatorException {
            return Mockito.mock(AAFConHttp.class);
        }
    
        public CMService getService() {
            return Mockito.mock(CMService.class);
        }
    
        @Override
        public void route(HttpMethods meth, String path, API api, Code code) throws Exception {
        
        }
    }



    class AuthzTransImpl1 extends AuthzTransImpl{

        public AuthzTransImpl1(AuthzEnv env) {
            super(env);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected TimeTaken newTimeTaken(String name, int flag, Object ... values) {
            // TODO Auto-generated method stub
            TimeTaken tt= new TimeTaken("nameTest", Env.XML) {
            
                @Override
                public void output(StringBuilder sb) {
                    // TODO Auto-generated method stub
                
                }
            };
            return tt;
        }
    
        @Override
        public Metric auditTrail(int indent, StringBuilder sb, int ... flag) {
            return null;
        }
    
    }


}
