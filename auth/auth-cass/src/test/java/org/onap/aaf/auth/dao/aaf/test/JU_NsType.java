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

package org.onap.aaf.auth.dao.aaf.test;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Test;
import org.onap.aaf.auth.dao.cass.NsType;

public class JU_NsType {

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() {
        NsType nt,nt2;
        String[] tests = new String[] {"DOT","ROOT","COMPANY","APP","STACKED_APP","STACK"};
        for (String s : tests) {
            nt = NsType.valueOf(s);
            assertEquals(s,nt.name());
        
            nt2 = NsType.fromString(s);
            assertEquals(nt,nt2);
        
            int t = nt.type;
            nt2 = NsType.fromType(t);
            assertEquals(nt,nt2);
        }
    
        nt  = NsType.fromType(Integer.MIN_VALUE);
        assertEquals(nt,NsType.UNKNOWN);
        nt = NsType.fromString("Garbage");
        assertEquals(nt,NsType.UNKNOWN);
    }

}
