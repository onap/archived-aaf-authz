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

package org.onap.aaf.auth.rserv.test;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.onap.aaf.auth.rserv.Version;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class JU_Version {
    Version version;
    Version versionTest;


    @Before
    public void setUp(){
        version = new Version("first\\.123");
        versionTest = new Version("first\\.124");
    }

    @Test
    public void testEquals(){    
        version.equals(versionTest);
        versionTest.equals(version);
        versionTest = new Version("fail\\.124");
        version.equals(versionTest);
        version.equals("This is not an object of version");
        versionTest = new Version("NoVersion\\.number");
        version.equals(versionTest);
    
    
    }

    @Test
    public void testToString(){
        String strVal = version.toString();
        assertNotNull(strVal);
    }

    @Test
    public void testHashCode() {
        Assert.assertNotNull(version.hashCode());
    }
}
