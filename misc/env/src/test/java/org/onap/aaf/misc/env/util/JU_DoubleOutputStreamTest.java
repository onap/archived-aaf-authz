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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class JU_DoubleOutputStreamTest {

    @Mock
    private OutputStream stream1;

    @Mock
    private OutputStream stream2;

    private DoubleOutputStream doubleOutputStream;

    @Before
    public void setup() {
        stream1 = mock(OutputStream.class);
        stream2 = mock(OutputStream.class);
    }

    @Test
    public void testWriteInt() throws IOException {
        doubleOutputStream = new DoubleOutputStream(stream1, true, stream2, true);

        doubleOutputStream.write(123);

        verify(stream1, only()).write(123);
        verify(stream2, only()).write(123);
    }

    @Test
    public void testWriteByteArray() throws IOException {
        doubleOutputStream = new DoubleOutputStream(stream1, true, stream2, true);

        byte[] bytes = { 1, 2, 3, 4 };

        doubleOutputStream.write(bytes);

        verify(stream1, only()).write(bytes);
        verify(stream2, only()).write(bytes);

    }

    @Test
    public void testWriteByteArrayWithOffset() throws IOException {
        doubleOutputStream = new DoubleOutputStream(stream1, true, stream2, true);

        byte[] bytes = { 1, 2, 3, 4 };

        doubleOutputStream.write(bytes, 1, 3);
        verify(stream1, only()).write(bytes, 1, 3);
        verify(stream2, only()).write(bytes, 1, 3);
    }

    @Test
    public void testFlush() throws IOException {
        doubleOutputStream = new DoubleOutputStream(stream1, true, stream2, true);

        doubleOutputStream.flush();

        verify(stream1, only()).flush();
        verify(stream2, only()).flush();
    }

    @Test
    public void testClose() throws IOException {
        doubleOutputStream = new DoubleOutputStream(stream1, true, stream2, false);

        doubleOutputStream.close();

        verify(stream1, only()).close();
        verify(stream2, never()).close();
    }
}
