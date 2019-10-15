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
 *
 */

package org.onap.aaf.cadi.client.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.*;

import org.onap.aaf.cadi.client.Result;

public class JU_Result {

    private static final int OK = 200;
    private static final int NOT_FOUND = 404;

    @Test
    public void test() {
        Result<Integer> result;
        result = Result.ok(OK, 10);
        assertThat(result.toString(), is("Code: 200"));
        assertThat(result.isOK(), is(true));
    
        result = Result.err(NOT_FOUND, "File not found");
        assertThat(result.toString(), is("Code: 404 = File not found"));
        assertThat(result.isOK(), is(false));

        result = Result.err(result);
        assertThat(result.toString(), is("Code: 404 = File not found"));
        assertThat(result.isOK(), is(false));
    }

}
