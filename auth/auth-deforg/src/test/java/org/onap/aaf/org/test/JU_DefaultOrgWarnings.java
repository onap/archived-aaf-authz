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

package org.onap.aaf.org.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.org.DefaultOrgWarnings;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class JU_DefaultOrgWarnings {

    private DefaultOrgWarnings defaultOrgWarningsMock;
    private DefaultOrgWarnings defaultOrgWarnings;


    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);

        defaultOrgWarningsMock = PowerMockito.mock(DefaultOrgWarnings.class);

        defaultOrgWarnings = new DefaultOrgWarnings();
    }


    @Test
    public void testApprEmailInterval() {

        assertEquals(259200000, defaultOrgWarnings.apprEmailInterval() );
    }

    @Test
    public void testCredEmailInterval() {
        assertEquals(604800000, defaultOrgWarnings.credEmailInterval());

    }

    @Test
    public void testCredExpirationWarning() {
        assertEquals(2592000000L, defaultOrgWarnings.credExpirationWarning());
    }

    @Test
    public void testEmailUrgentWarning() {
        assertEquals(1209600000L, defaultOrgWarnings.emailUrgentWarning());
    }

    @Test
    public void testRoleEmailInterval() {
        assertEquals(604800000L, defaultOrgWarnings.roleEmailInterval());
    }

    @Test
    public void testRoleExpirationWarning() {
        assertEquals(2592000000L, defaultOrgWarnings.roleExpirationWarning());
    }

}
