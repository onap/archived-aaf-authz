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

package org.onap.aaf.auth.org.test;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.OrganizationFactory;
import org.onap.aaf.misc.env.impl.BasicEnv;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import junit.framework.Assert;

@RunWith(PowerMockRunner.class)
public class JU_OrganizationFactory {
    private static final String ORG_SLOT = null;
    OrganizationFactory organizationFactory;
    BasicEnv bEnv;
    @Mock
    AuthzEnv authzEnvMock;
    String orgClass="orgclass";
    String orgNS="orgns";
    @Before
    public void setUp(){
        organizationFactory = new OrganizationFactory();
        bEnv = new BasicEnv();
    }

    @SuppressWarnings("static-access")
    @Test
    public void testInit() throws OrganizationException {
          Assert.assertEquals(null, organizationFactory.init(bEnv));
    }

    @SuppressWarnings("static-access")                //TODO:Fix this once real input is available AAF-111
    @Test
    public void testObtain() throws OrganizationException{
        PowerMockito.when(authzEnvMock.getProperty("Organization."+orgNS)).thenReturn("notnull");
        //organizationFactory.obtain(authzEnvMock, orgNS);
    }

    @Test
    public void testGet() throws OrganizationException {  //TODO: Fix with when then return on fail
        AuthzTrans trans = mock(AuthzTrans.class);
        //organizationFactory.get(trans);
    }
}
