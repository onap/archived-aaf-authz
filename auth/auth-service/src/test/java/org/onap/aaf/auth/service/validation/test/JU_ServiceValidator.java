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

package org.onap.aaf.auth.service.validation.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.service.validation.ServiceValidator;
import org.onap.aaf.auth.validation.Validator;

public class JU_ServiceValidator {

    ServiceValidator validator;

    @Before
    public void setUp() {
        validator = new ServiceValidator();
    }

    @Test
    public void permNotOk() {

        Result<PermDAO.Data> rpd = Result.err(1, "ERR_Security");

        validator.perm(rpd);
        assertTrue(validator.errs().equals("ERR_Security\n"));

    }

    @Test
    public void permInstance() {
        assertFalse(validator.permInstance("hello").err());
        assertFalse(validator.permInstance("hello32").err());
        assertFalse(validator.permInstance("hello-32").err());
        assertFalse(validator.permInstance(":asdf:*:sdf*:sdk").err());
        assertFalse(validator.permInstance(":asdf:*:sdf*:sdk*").err());
        // Perms may not end in ":"
        assertTrue(validator.permInstance(":").err());
        assertTrue(validator.permInstance(":hello:").err());
    }

    @Test
    public void permOkNull() {

        Result rpd = Result.ok();

        validator.perm(rpd);
        assertTrue(validator.errs().equals("Perm Data is null.\n"));

    }

    @Test
    public void roleOkNull() {

        Result rrd = Result.ok();

        validator.role(rrd);
        assertTrue(validator.errs().equals("Role Data is null.\n"));
    }

    @Test
    public void roleOk() {
        RoleDAO.Data to = new RoleDAO.Data();
        to.ns = "namespace";
        to.name = "name";
        to.description = "description";
        Set<String> permissions = new HashSet<>();
        permissions.add("perm1");
        to.perms = permissions;

        Result<RoleDAO.Data> rrd = Result.ok(to);

        validator.role(rrd);
        assertTrue(
                validator.errs().equals("Perm [perm1] in Role [namespace.name] is not correctly separated with '|'\n"));
    }

    @Test
    public void roleNotOk() {

        Result rrd = Result.err(1, "ERR_Security");

        validator.role(rrd);
        assertTrue(validator.errs().equals("ERR_Security\n"));
    }

}
