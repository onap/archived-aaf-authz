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
package org.onap.aaf.misc.env.util.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.util.Pool;

public class JU_PoolTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() {
        Pool pool = new Pool<Integer>(new Pool.Creator<Integer>() {

            Integer content = 0;

            @Override
            public Integer create() throws APIException {
                return content++;
            }

            @Override
            public void destroy(Integer t) {

            }

            @Override
            public boolean isValid(Integer t) {
                return t == content;
            }

            @Override
            public void reuse(Integer t) {
                content = t;
            }
        });
        Pool.Pooled<Integer> pooled = new Pool.Pooled<Integer>(new Integer(123), pool, LogTarget.SYSOUT);
        Pool.Pooled<Integer> pooled1 = new Pool.Pooled<Integer>(new Integer(123), null, LogTarget.SYSOUT);
        try {
            // pool.drain();
            assertEquals("Should return intial value", 0, pool.get().content);
            // pooled.toss();
            pool.prime(LogTarget.SYSOUT, 23);
            assertEquals("Should Return 23 as added at last prime", 23, pool.get(LogTarget.SYSOUT).content);
            pool.prime(LogTarget.SYSERR, 13);
            assertEquals("Should add another 13 from SysErr and remove 1", 35, pool.get(LogTarget.SYSERR).content);
            assertEquals("Create a new creator with create method", 1, pool.get().content);
            assertEquals("Create a new creator with create method", 2, pool.get().content);
            assertEquals("Should remove last from pool", 34, pool.get(LogTarget.SYSOUT).content);

            pool.drain();
            assertEquals("Should remove last from pool", 17, pool.get(LogTarget.SYSOUT).content);
            pool.setMaxRange(10);
            assertEquals(10, pool.getMaxRange());
            pooled.toss();
            pooled1.toss();
        } catch (APIException e) {
        }
    }
}
