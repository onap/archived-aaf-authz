/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.auth.cm.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.auth.cm.ca.CA;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.cadi.configure.CertException;

public class JU_CertReqTest {

    @Mock
    CA ca;

    @Mock
    CSRMeta csr;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(ca.newCSRMeta()).thenReturn(csr);
        when(csr.cn()).thenReturn("cn123");
        when(csr.mechID()).thenReturn("mechId");
    }

    @Test
    public void testCertResp() throws IOException, GeneralSecurityException, CertException {
        CertReq req = new CertReq();
        req.certAuthority = ca;
        req.fqdns = new ArrayList<String>();

        assertEquals(csr, req.getCSRMeta());
    }
}
