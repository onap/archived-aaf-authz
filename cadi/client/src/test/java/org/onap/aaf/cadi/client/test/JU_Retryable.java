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

package org.onap.aaf.cadi.client.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.*;

import java.net.ConnectException;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;

public class JU_Retryable {

    @Test
    public void test() {
        RetryableStub retry = new RetryableStub();
        assertThat(retry.item(), is(nullValue()));
        assertThat(retry.lastClient(), is(nullValue()));
    
        Locator.Item item = null;
        assertThat(retry.item(item), is(item));
    
        retry = new RetryableStub(retry);
        assertThat(retry.item(), is(nullValue()));
        assertThat(retry.lastClient(), is(nullValue()));
        assertThat(retry.item(item), is(item));
    }

    private class RetryableStub extends Retryable<Integer> {
        public RetryableStub() { super(); }
        public RetryableStub(Retryable<?> ret) { super(ret); }
        @Override public Integer code(Rcli<?> client) throws CadiException, ConnectException, APIException { return null; }
    }

}
