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
import org.onap.aaf.auth.batch.CassBatch;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.misc.env.APIException;

import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Test;

public class JU_CassBatch {

    AuthzTrans aTrans;

    private class CassBatchStub extends CassBatch {

        protected CassBatchStub(AuthzTrans trans, String log4jName)
                throws APIException, IOException, OrganizationException {
            super(trans, log4jName);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected void run(AuthzTrans trans) {
            // TODO Auto-generated method stub
        
        }
    
    }

    @Before
    public void setUp() throws APIException, IOException, OrganizationException {
        aTrans = mock(AuthzTrans.class);
        //CassBatchStub cassBatchStub = new CassBatchStub(aTrans,"log");        //Cannot do until Batch is understood
    }

}
