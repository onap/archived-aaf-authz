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

package org.onap.aaf.auth.server.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.servlet.Filter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.auth.rserv.RServlet;
import org.onap.aaf.auth.server.AbsService;
import org.onap.aaf.auth.server.AbsServiceStarter;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.register.Registrant;
import org.onap.aaf.misc.env.impl.BasicEnv;

public class JU_AbsServiceStarter {

    ByteArrayOutputStream outStream;
    AbsServiceStub absServiceStub;
    AbsServiceStarterStub absServiceStarterStub;

    private class AbsServiceStarterStub extends AbsServiceStarter {

        public AbsServiceStarterStub(AbsService service, boolean secure) {
            super(service,secure);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void _start(RServlet rserv) throws Exception {
            // TODO Auto-generated method stub
        
        }

        @Override
        public void _propertyAdjustment() {
            // TODO Auto-generated method stub
        
        }
    }

    private class AbsServiceStub extends AbsService {

        public AbsServiceStub(Access access, BasicEnv env) throws CadiException {
            super(access, env);
            // TODO Auto-generated constructor stub
        }

        @Override
        public Filter[] _filters(Object ... additionalTafLurs) throws CadiException, LocatorException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Registrant[] registrants(int port) throws CadiException, LocatorException {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Before
    public void setUp() {
        outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));
    }

    @After
    public void tearDown() {
        System.setOut(System.out);
    }


    @Test
    public void testStub() throws CadiException {
        BasicEnv bEnv = new BasicEnv();
        PropAccess prop = new PropAccess();
    
        prop.setProperty(Config.AAF_LOCATOR_ENTRIES, "te.st");
        prop.setProperty(Config.AAF_LOCATOR_VERSION, "te.st");
        prop.setLogLevel(Level.DEBUG);
        absServiceStub = new AbsServiceStub(prop, bEnv);
    
        absServiceStarterStub = new AbsServiceStarterStub(absServiceStub,true);
    }

//    @Test
//    public void testStart() throws Exception {
//        absServiceStarterStub.env();
//        absServiceStarterStub.start();
//    }

}



