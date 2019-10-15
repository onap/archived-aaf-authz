/*******************************************************************************
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

package org.onap.aaf.cadi.util.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import org.junit.*;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.util.Pool;
import org.onap.aaf.cadi.util.Pool.*;

public class JU_Pool {

    private StringBuilder sb = new StringBuilder();

    private class IntegerCreator implements Creator<Integer> {
        private int current = 0;

        @Override
        public Integer create() {
            return current++;
        }

        @Override
        public void destroy(Integer t) {
            t = 0;
        }

        @Override
        public boolean isValid(Integer t) {
            return (t & 0x1) == 0;
        }

        @Override
        public void reuse(Integer t) {
        }
    }

    private class CustomLogger implements Log {
        @Override
        public void log(Object... o) {
            for (Object item : o) {
                sb.append(item.toString());
            }
        }
    }

    @Test
    public void getTest() throws CadiException {
        Pool<Integer> intPool = new Pool<Integer>(new IntegerCreator());

        List<Pooled<Integer>> gotten = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            gotten.add(intPool.get());
            assertThat(gotten.get(i).content, is(i));
        }

        gotten.get(9).done();
        gotten.set(9, intPool.get());
        assertThat(gotten.get(9).content, is(9));

        for (int i = 0; i < 10; i++) {
            gotten.get(i).done();
        }

        for (int i = 0; i < 10; i++) {
            gotten.set(i, intPool.get());
            if (i < 5) {
                assertThat(gotten.get(i).content, is(i));
            } else {
                assertThat(gotten.get(i).content, is(i + 5));
            }
        }

        for (int i = 0; i < 10; i++) {
            gotten.get(i).toss();
            // Coverage calls
            gotten.get(i).toss();
            gotten.get(i).done();

            // only set some objects to null -> this is for the finalize coverage test
            if (i < 5) {
                gotten.set(i, null);
            }
        }

        // Coverage of finalize()
        System.gc();
    }

    @Test
    public void bulkTest() throws CadiException {
        Pool<Integer> intPool = new Pool<Integer>(new IntegerCreator());

        intPool.prime(10);
        // Remove all of the invalid items (in this case, odd numbers)
        assertFalse(intPool.validate());

        // Make sure we got them all
        assertTrue(intPool.validate());

        // Get an item from the pool
        Pooled<Integer> gotten = intPool.get();
        assertThat(gotten.content, is(0));

        // finalize that item, then check the next one to make sure we actually purged
        // the odd numbers
        gotten = intPool.get();
        assertThat(gotten.content, is(2));

        intPool.drain();

    }

    @Test
    public void setMaxTest() {
        Pool<Integer> intPool = new Pool<Integer>(new IntegerCreator());
        intPool.setMaxRange(10);
        assertThat(intPool.getMaxRange(), is(10));
        intPool.setMaxRange(-10);
        assertThat(intPool.getMaxRange(), is(0));
    }

    @Test
    public void loggingTest() {
        Pool<Integer> intPool = new Pool<Integer>(new IntegerCreator());

        // Log to Log.NULL for coverage
        intPool.log("Test log output");

        intPool.setLogger(new CustomLogger());
        intPool.log("Test log output");

        assertThat(sb.toString(), is("Test log output"));
    }

}
