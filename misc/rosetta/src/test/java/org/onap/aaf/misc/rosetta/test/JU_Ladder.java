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

package org.onap.aaf.misc.rosetta.test;

import org.junit.Test;
import org.onap.aaf.misc.rosetta.Ladder;

import static org.junit.Assert.*;

public class JU_Ladder {

    @Test
    public void test() {
        Ladder<String> ladder = new Ladder<String>();
    
        for (int i=0;i<30;++i) {
            for (int j=0;j<i;++j)ladder.ascend();
            String str = "Rung " + i;
            assertEquals(ladder.peek(),null);
            ladder.push(str);
            assertEquals(str,ladder.peek());
            assertEquals(str,ladder.pop());
            assertEquals(null,ladder.peek());
            for (int j=0;j<i;++j)ladder.descend();
        }
        assertEquals(ladder.height(),32); // Sizing, when naturally created is by 8
    
        ladder.cutTo(8);
        assertEquals(ladder.height(),8); 
    
        for (int i=0;i<30;++i) {
            ladder.jumpTo(i);
            String str = "Rung " + i;
            assertEquals(ladder.peek(),null);
            ladder.push(str);
            assertEquals(ladder.peek(),str);
        }

        ladder.bottom();
    
        for (int i=0;i<30;++i) {
            assertEquals("Rung " + i,ladder.peek());
            ladder.ascend();
        }
    
        ladder.bottom();
        ladder.top();
        assertEquals("Rung 29",ladder.peek());
    
        for (int i=0;i<30;++i) {
            ladder.jumpTo(i);
            assertEquals("Rung " + i,ladder.peek());
        }

    }

}
