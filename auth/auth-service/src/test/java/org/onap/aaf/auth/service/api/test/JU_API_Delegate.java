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

package org.onap.aaf.auth.service.api.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.aaf.auth.service.AAF_Service;
import org.onap.aaf.auth.service.api.API_Delegate;

import org.onap.aaf.auth.service.facade.AuthzFacade;

import org.powermock.modules.junit4.PowerMockRunner;
@RunWith(PowerMockRunner.class)
public class JU_API_Delegate {
    API_Delegate api_Delegate;
    @Mock
    AAF_Service authzAPI;
    AuthzFacade facade;
    @Before
    public void setUp() {

    }

    @SuppressWarnings("static-access")
    @Test
    public void testInit(){

        try {
            api_Delegate.init(authzAPI, facade);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
//
//    @Test
//    public void notYetImplemented() {
//        fail("Tests in this file should not be trusted");
//    }

}
