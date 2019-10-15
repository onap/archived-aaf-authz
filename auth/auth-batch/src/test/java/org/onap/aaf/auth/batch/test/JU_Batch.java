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

package org.onap.aaf.auth.batch.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.org.OrganizationFactory;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;

import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Test;

public class JU_Batch {

    AuthzEnv env;
    Batch batch;

    private class BatchStub extends Batch {

        protected BatchStub(AuthzEnv env) throws APIException, IOException, OrganizationException {
            super(env);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void run(AuthzTrans trans) {
            // TODO Auto-generated method stub
        
        }

        @Override
        protected void _close(AuthzTrans trans) {
            // TODO Auto-generated method stub
        
        }

    }

    @Before
    public void setUp() throws OrganizationException {
        env = new AuthzEnv();
        env.access().setProperty(Config.CADI_LATITUDE, "38.550674");
        env.access().setProperty(Config.CADI_LONGITUDE, "-90.146942");
        env.setProperty("DRY_RUN", "test");
        env.setProperty("Organization.@aaf.com", "test");
        //env.setProperty("Organization.com.@aaf", "java.lang.Integer");
        env.setProperty("Organization.com.@aaf", "org.onap.aaf.auth.org.Organization");
        env.setProperty("CASS_ENV", "test");
        env.setProperty("test.VERSION", "test.VERSION");
    }

    @Test
    public void testIsSpecial() throws APIException, IOException, OrganizationException {
        //BatchStub bStub = new BatchStub(env);
        //bStub.isSpecial("user");
    }

}
