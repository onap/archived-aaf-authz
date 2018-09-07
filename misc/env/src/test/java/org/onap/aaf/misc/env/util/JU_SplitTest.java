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

package org.onap.aaf.misc.env.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JU_SplitTest {

    @Test
    public void testSplit() {
        String[] splits = Split.split('c', "character c to break string");

        assertEquals(splits.length, 4);
        assertEquals(splits[0], "");
        assertEquals(splits[1], "hara");
        assertEquals(splits[2], "ter ");
        assertEquals(splits[3], " to break string");
    }

    @Test
    public void testSplitTrim() {
        String[] splits = Split.splitTrim('c', "character c to break string", 5);

        assertEquals(splits.length, 5);
        assertEquals(splits[0], "");
        assertEquals(splits[1], "hara");
        assertEquals(splits[2], "ter");
        assertEquals(splits[3], "to break string");
        assertEquals(splits[4], null);

        splits = Split.splitTrim('c', " character ", 1);
        assertEquals(splits.length, 1);
        assertEquals(splits[0], "character");
    }
}
