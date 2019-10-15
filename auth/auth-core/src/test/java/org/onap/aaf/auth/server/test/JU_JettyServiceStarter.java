/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * *
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/

package org.onap.aaf.auth.server.test;

import static org.junit.Assert.*;

import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.server.AbsService;
import org.onap.aaf.auth.server.JettyServiceStarter;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.register.Registrant;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.impl.BasicEnv;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.Filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

public class JU_JettyServiceStarter {
    private PropAccess propAccess = new PropAccess();
    private JettyServiceStarter<AuthzEnv,AuthzTrans> jss;
    class TestService extends AbsService<AuthzEnv,AuthzTrans>{

        public TestService(Access access, AuthzEnv env) throws CadiException {
            super(access, env);
            // TODO Auto-generated constructor stub
        }

        @Override
        public Filter[] _filters(Object ... additionalTafLurs) throws CadiException, LocatorException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Registrant<AuthzEnv>[] registrants(int port) throws CadiException, LocatorException {
            // TODO Auto-generated method stub
            return null;
        }

    }
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws OrganizationException, CadiException {
        Access access = mock(Access.class);

        BasicEnv bEnv = mock(BasicEnv.class);
        Trans trans = mock(Trans.class);  //TODO: Fix this once Gabe has services running to see correct output without mock
        //TestService testService = new TestService(access, bEnv);
        //jss = new JettyServiceStarter<AuthzEnv,AuthzTrans>(testService);
    }

//    @Test
//    public void netYetTested() {
//        fail("Tests not yet implemented");
//    }

    @Test
    public void testPropertyAdjustment() {
        //jss._propertyAdjustment();
    }

}
