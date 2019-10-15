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

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.EmailWarnings;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Expiration;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.Organization.Notify;
import org.onap.aaf.auth.org.Organization.Policy;
import org.onap.aaf.auth.org.Organization.Response;
import org.onap.aaf.auth.org.OrganizationException;

import junit.framework.Assert;

public class JU_Organization {

    AuthzTrans trans;
    GregorianCalendar gc;
    @Before
    public void setUp() {
        gc = new GregorianCalendar(1900, 1, 1);
        trans = mock(AuthzTrans.class);
    }

    @Test
    public void test() throws OrganizationException {    
        //tests for Org null
        Assert.assertEquals("n/a",Organization.NULL.getName());
        Assert.assertEquals("n/a",Organization.NULL.getDomain());
        Assert.assertEquals("n/a",Organization.NULL.getRealm());
        Assert.assertTrue(Organization.NULL.getIdentity(trans, "test") instanceof Identity);
        Assert.assertEquals("n/a",Organization.NULL.isValidID(trans, null));
        Assert.assertEquals("n/a",Organization.NULL.isValidPassword(trans, null, null, null));
        Assert.assertTrue(Organization.NULL.getIdentityTypes() instanceof HashSet);
        Assert.assertTrue(Organization.NULL.notify(trans, Notify.PasswordExpiration, null, null, null, null, null) instanceof Response);
        Assert.assertEquals(0,Organization.NULL.sendEmail(trans, null, null, null, null, null));
        Assert.assertEquals(gc.getTime(),Organization.NULL.whenToValidate(null, null));
        Assert.assertEquals(gc,Organization.NULL.expiration(gc, Expiration.Password));
        Assert.assertTrue(Organization.NULL.getApprovers(trans, null) instanceof ArrayList);
        Assert.assertEquals("",Organization.NULL.getApproverType());
        Assert.assertEquals(0,Organization.NULL.startOfDay());
        Assert.assertFalse(Organization.NULL.canHaveMultipleCreds(null));
        Assert.assertFalse(Organization.NULL.isValidCred(trans, null));
        Assert.assertEquals("Null Organization rejects all Policies",Organization.NULL.validate(trans, Policy.CHANGE_JOB, null, null));
        Assert.assertFalse(Organization.NULL.isTestEnv());
        Organization.NULL.setTestMode(true);
    
        //tests for org emailWarnings
        Assert.assertTrue(Organization.NULL.emailWarningPolicy() instanceof EmailWarnings);
        Assert.assertEquals(604800000L, Organization.NULL.emailWarningPolicy().credEmailInterval());
        Assert.assertEquals(604800000L, Organization.NULL.emailWarningPolicy().roleEmailInterval());
        Assert.assertEquals(259200000L, Organization.NULL.emailWarningPolicy().apprEmailInterval());
        Assert.assertEquals(2592000000L, Organization.NULL.emailWarningPolicy().credExpirationWarning());
        Assert.assertEquals(2592000000L, Organization.NULL.emailWarningPolicy().roleExpirationWarning());
        Assert.assertEquals(1209600000L, Organization.NULL.emailWarningPolicy().emailUrgentWarning());
        Assert.assertTrue(Organization.NULL.getPasswordRules() instanceof String[]);

    }
}
