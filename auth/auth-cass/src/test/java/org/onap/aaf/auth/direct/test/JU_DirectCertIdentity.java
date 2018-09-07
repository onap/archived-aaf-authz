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

import static org.junit.Assert.*;

import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.onap.aaf.auth.dao.cached.CachedCertDAO;
import org.onap.aaf.auth.direct.DirectCertIdentity;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class JU_DirectCertIdentity {
    
    public DirectCertIdentity directCertIdentity;
    
    @Before
    public void setUp(){
        directCertIdentity = new DirectCertIdentity();
    }


    @Mock
    HttpServletRequest req;
    X509Certificate cert;
    byte[] _certBytes;
    
    @Test
    public void testidentity(){
        
        try {
        Principal p = directCertIdentity.identity(req, cert, _certBytes);
        assertEquals(( (p) == null),true);
            
        } catch (CertificateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //assertTrue(true);
        
    }

}
