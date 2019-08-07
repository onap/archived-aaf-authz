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

package org.onap.aaf.auth.direct.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.dao.cached.CachedCertDAO;
import org.onap.aaf.auth.dao.cass.CertDAO;
import org.onap.aaf.auth.direct.DirectCertIdentity;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class JU_DirectCertIdentity {

    public DirectCertIdentity directCertIdentity;

    @Before
    public void setUp() {
        directCertIdentity = new DirectCertIdentity();
    }

    @Mock
    HttpServletRequest req;
    X509Certificate cert;
    byte[] _certBytes;

    @Test
    public void testidentity() {

        try {
            Principal p = directCertIdentity.identity(req, cert, _certBytes);
            assertEquals(((p) == null), true);
            
            cert = Mockito.mock(X509Certificate.class);
            Mockito.when(cert.getEncoded()).thenReturn(new byte[128]);
            
            Result<List<CertDAO.Data>> rs = new Result<List<CertDAO.Data>>(null, 1, "test", new Object[0]);
            
            CachedCertDAO cacheDao = Mockito.mock(CachedCertDAO.class);
            Mockito.when(cacheDao.read(Mockito.any(AuthzTrans.class),Mockito.any(Object[].class))).thenReturn(rs);
            DirectCertIdentity.set(cacheDao);
            p = directCertIdentity.identity(req, cert, _certBytes);
            
            _certBytes = new byte[128];
            List<CertDAO.Data> dataAL = new ArrayList<>();
            CertDAO.Data data = new CertDAO.Data();
            dataAL.add(data);
            rs = new Result<List<CertDAO.Data>>(dataAL, 0, "test", new Object[0]);
            Mockito.when(cacheDao.read(Mockito.any(AuthzTrans.class),Mockito.any(Object[].class))).thenReturn(rs);
            DirectCertIdentity.set(cacheDao);
            p = directCertIdentity.identity(req, cert, _certBytes);
            assertTrue(p.toString().contains("X509 Authentication for null"));
            
            cert = null;
            directCertIdentity.identity(req, cert, _certBytes);
        } catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // assertTrue(true);

    }

}
