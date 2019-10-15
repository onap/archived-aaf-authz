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
 */

package org.onap.aaf.auth.batch.helpers.test;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import org.junit.Test;
import org.onap.aaf.auth.batch.helpers.ExpireRange;
import org.onap.aaf.cadi.PropAccess;

public class JU_ExpireRange {
    @Test
    public void test() {
        ExpireRange expRange = new ExpireRange(new PropAccess());
        Date now = expRange.now();
    
        Set<String> names=expRange.names();
        assertTrue(names.contains("OneMonth"));
        assertTrue(names.contains("OneWeek"));
        assertTrue(names.contains("Delete"));
        assertFalse(names.contains(null));
        assertFalse(names.contains("bogus"));
    
        ExpireRange.Range r;
        GregorianCalendar gc = new GregorianCalendar();
        String[] all = new String[] {"ur","cred"};
    
        // Test 3 weeks prior
        gc.setTime(now);
        gc.add(GregorianCalendar.WEEK_OF_MONTH,-3);
        for(String rs : all) {
            r = expRange.getRange(rs, gc.getTime());
            assertNotNull(r);
            assertEquals("Delete",r.name());
        }
    
        // Test 1 week prior
        gc.setTime(now);
        gc.add(GregorianCalendar.WEEK_OF_MONTH,-1);
        for(String rs : all) {
            r = expRange.getRange(rs, gc.getTime());
            assertNull(r);
        }
    
        // Test Today
        r = expRange.getRange("cred", now);
        assertNotNull(r);
    }

}
