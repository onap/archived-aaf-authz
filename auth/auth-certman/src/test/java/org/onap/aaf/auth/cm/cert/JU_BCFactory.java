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

package org.onap.aaf.auth.cm.cert;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.cadi.configure.CertException;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

@RunWith(MockitoJUnitRunner.class)
public class JU_BCFactory {

    private static BCFactory bcFactory = new BCFactory();

    private static BCFactory bcFact;

    private static PrivateKey pk;


    private static Trans trans;


    private static PKCS10CertificationRequest req;

    @BeforeClass
    public static void setUp() throws IOException {
        pk = new XYZKey();
        trans = mock(Trans.class);
        req = mock(PKCS10CertificationRequest.class);
        when(req.getEncoded()).thenReturn(new byte[1]);
        when(trans.start(Mockito.anyString(), Mockito.anyInt())).thenReturn(new TimeTaken(null, 0) {
        
            @Override
            public void output(StringBuilder sb) {
                // TODO Auto-generated method stub
            
            }
        });
        bcFact = mock(BCFactory.class);
    }

    @Test
    public void toStrin() throws OperatorCreationException, IOException, CertException {
        assertNotNull(bcFactory.toString(req));
    }

    @Test
    public void toStrinMoc() throws OperatorCreationException, IOException, CertException {
        assertNotNull(bcFact.toString(req));
    }

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Test
    public void toCSR()  {
        try {
            assertNotNull(bcFactory.toCSR(trans, new File("/random/path")));
            thrown.expect(FileNotFoundException.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

class XYZKey implements Key, PublicKey, PrivateKey {

    int rotValue;
    public XYZKey() {
        rotValue = 1200213;
    }
    public String getAlgorithm() {
        return "XYZ";
    }

    public String getFormat() {
        return "XYZ Special Format";
    }

    public byte[] getEncoded() {
        byte b[] = new byte[4];
        b[3] = (byte) ((rotValue << 24) & 0xff);
        b[2] = (byte) ((rotValue << 16) & 0xff);
        b[1] = (byte) ((rotValue << 8) & 0xff);
        b[0] = (byte) ((rotValue << 0) & 0xff);
        return b;
    }
}
