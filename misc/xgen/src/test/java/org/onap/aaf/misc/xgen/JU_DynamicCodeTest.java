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

package org.onap.aaf.misc.xgen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Test;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.xgen.html.HTML4Gen;
import org.onap.aaf.misc.xgen.html.HTMLGen;
import org.onap.aaf.misc.xgen.html.State;

public class JU_DynamicCodeTest {

    @Test
    public void test() throws APIException, IOException {
        final Cache<HTMLGen> cache1 = new Cache<HTMLGen>() {

            @Override
            public void dynamic(HTMLGen hgen, Code<HTMLGen> code) {
            }
        };

        final HTMLGen xgen1 = new HTML4Gen(new PrintWriter(System.out));
        DynamicCode<HTMLGen, State<Env>, Trans> g = new DynamicCode<HTMLGen, State<Env>, Trans>() {

            @Override
            public void code(State<Env> state, Trans trans, Cache<HTMLGen> cache, HTMLGen xgen)
                    throws APIException, IOException {
                assertNull(state);
                assertNull(trans);
                assertEquals(cache, cache1);
                assertEquals(xgen, xgen1);
            }
        };

        g.code(cache1, xgen1);
    }

}
