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

package org.onap.aaf.misc.env;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class JU_APIExceptionTest {

    private static final String EXCEPTION_MESSAGE = "New API Exception for test";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testNewAPIExceptionWithMessage() {
        APIException exception = new APIException(EXCEPTION_MESSAGE);

        assertEquals(exception.getMessage(), EXCEPTION_MESSAGE);
    }

    @Test
    public void testNewAPIExceptionCreatedWithMessageAndThrowable() {
        Throwable throwable = new Throwable();
        APIException exception = new APIException(EXCEPTION_MESSAGE, throwable);

        assertEquals(exception.getMessage(), EXCEPTION_MESSAGE);
        assertEquals(exception.getCause(), throwable);
    }

    @Test
    public void testNewAPIExceptionCreatedWithThrowable() {
        Throwable throwable = new Throwable();
        APIException exception = new APIException(throwable);

        assertEquals(exception.getCause(), throwable);
    }

    @Test
    public void testPayloadSetter() {
        Throwable throwable = new Throwable();
        Object payload = new Object();

        APIException exception = new APIException(throwable);

        exception.setPayload(payload);

        assertEquals(exception.getPayload(), payload);
    }
}
