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
package org.onap.aaf.auth.dao.cached;

import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.auth.dao.CIDAO;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.env.AuthzTrans;

public class JU_CachedNSDAOTest {

    private long expiresIn;
    private CIDAO<AuthzTrans> info;
    @Mock
    private NsDAO dao;
    private AuthzTrans trans;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void test() {
        CachedNSDAO ccDao = new CachedNSDAO(dao, info, expiresIn);

        assertNotNull(ccDao);

    }

}
