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

package org.onap.aaf.auth.common.test;

import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.Env;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class JU_Define {
    private static final String AAF_NS_DOT = "AAF_NS.";
    public static String ROOT_NS="NS.Not.Set";
    public static String ROOT_COMPANY=ROOT_NS;
    Access acc;
    @Mock
    Env envMock;


    @Before
    public void setUp() throws CadiException{
        acc = mock(Access.class);
    }

    @Test
    public void testSet() throws CadiException {
        PropAccess prop = new PropAccess();
        prop.setProperty(AAF_NS_DOT, AAF_NS_DOT);
        prop.setProperty(Config.AAF_ROOT_NS, ".ns_Test");
        prop.setProperty(Config.AAF_ROOT_COMPANY, "company_Test");
        Define.set(prop);
        Define.ROOT_NS();
        Define.ROOT_COMPANY();
    
        PropAccess prop1 = new PropAccess();
        prop1.setProperty(AAF_NS_DOT, AAF_NS_DOT);
        prop1.setProperty(Config.AAF_ROOT_NS, ".ns_Test");
        Define.set(prop1);
    }

//    @Test                    //TODO: AAF-111 exception fix
//    public void testRootNS() throws RuntimeException{
//        Define.ROOT_NS();
//    }
//
//    @Test
//    public void testRootCompany() throws RuntimeException{
//        Define.ROOT_COMPANY();
//    }

    @Test
    public void testVarReplace() {
        Define.varReplace(AAF_NS_DOT);
        Define.varReplace("test");
    }
}
