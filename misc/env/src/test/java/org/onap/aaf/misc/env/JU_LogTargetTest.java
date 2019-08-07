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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class JU_LogTargetTest {

    @Mock
    Throwable t;

    @Before
    public void setup() {
        t = mock(Throwable.class);
    }

    @Test
    public void testLogTargetNull() {
        LogTarget nullTarget = LogTarget.NULL;

        // Expect methods doing nothing as no implemenation provided.
        nullTarget.log(new Throwable(), null, null);
        nullTarget.log("String", null);
        nullTarget.printf(null, null, null);

        assertFalse(nullTarget.isLoggable());
    }

    @Test
    public void testLogTargetSysOut() {
        LogTarget outTarget = LogTarget.SYSOUT;

        outTarget.printf("format", new Date());
        outTarget.log("null", null, null);

        outTarget.log(t);
        outTarget.log(t, "First String Object");

        assertTrue(outTarget.isLoggable());

        verify(t, times(2)).printStackTrace(System.out);
    }

    @Test
    public void testLogTargetSysErr() {
        LogTarget errTarget = LogTarget.SYSERR;

        errTarget.printf("format", new Date());
        errTarget.log("null", "null");

        errTarget.log(t);
        errTarget.log(t, "First String Object");

        assertTrue(errTarget.isLoggable());

        verify(t, times(2)).printStackTrace(System.err);
    }

}
