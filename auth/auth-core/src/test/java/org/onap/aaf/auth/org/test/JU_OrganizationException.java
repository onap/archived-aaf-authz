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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.aaf.auth.org.OrganizationException;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class JU_OrganizationException {

    OrganizationException organizationException;
    OrganizationException organizationException1;
    OrganizationException organizationException2;
    OrganizationException organizationException3;
    OrganizationException organizationException4;

    @Test
    public void testOrganizationException() {
        Throwable thr = new Throwable();
        organizationException = new OrganizationException();
        organizationException1 = new OrganizationException("test");
        organizationException2 = new OrganizationException(thr);
        organizationException3 = new OrganizationException("test", thr);
        organizationException4 = new OrganizationException("test", thr, true, true);
    }

}
