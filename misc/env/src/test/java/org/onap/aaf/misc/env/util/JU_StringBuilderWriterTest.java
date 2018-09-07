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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class JU_StringBuilderWriterTest {

    StringBuilderWriter streamWriter;

    StringBuilder builder = new StringBuilder();

    @Before
    public void setUp() throws Exception {
        streamWriter = new StringBuilderWriter(builder);
    }

    @Test
    public void testWriteIntAndReset() {
        streamWriter.write(1);

        assertEquals(1, streamWriter.getBuffer().length());
        streamWriter.reset();
        assertEquals("", streamWriter.toString());
    }

    @Test
    public void testWriteByteArrayWithoutException() throws IOException {
        char[] bytes = { 1, 2, 3, 4 };
        streamWriter.write(bytes);
        assertEquals(4, streamWriter.getBuffer().length());

        streamWriter.write(bytes, 1, 2);
        assertEquals(6, streamWriter.getBuffer().length());

        streamWriter.write(bytes, 1, 0);
        assertEquals(6, streamWriter.getBuffer().length());

        streamWriter.append(bytes[0]);
        assertEquals(7, streamWriter.getBuffer().length());
    }

    @Test
    public void testWriteByteArrayWithIndexOutOfBoundException() {
        char[] bytes = { 1, 2, 3, 4 };

        try {
            streamWriter.write(bytes, -1, 2);
            fail("This is supposed to throw IndexOutOfBounds Excetpion");
        } catch (IndexOutOfBoundsException e) {
        } catch (Exception e) {
            fail("This should throw only IndexOutOfBounds Exception");
        }
        assertEquals(0, streamWriter.getBuffer().length());

    }

    @Test
    public void testDefaultConstructor() throws IOException {
        StringBuilderWriter stream = new StringBuilderWriter();

        assertNotNull(stream.getBuffer());
        stream.close();
    }

    @Test
    public void testConstructorWithPositiveDefaultCapacity() throws IOException {
        StringBuilderWriter stream = new StringBuilderWriter(10);

        assertNotNull(stream.getBuffer());
        assertEquals(10, stream.getBuffer().capacity());
        stream.close();
    }

    @Test
    public void testConstructorWithNegativeCapacityException() {
        try {
            StringBuilderWriter stream = new StringBuilderWriter(-1);
            fail("This should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            fail("This should throw only IllegalArgumentException");
        }
    }

    @Test
    public void testWriteString() {
        streamWriter.write("1234");

        assertEquals("1234", streamWriter.toString());

        streamWriter.write("1234", 1, 2);
        assertEquals("123423", streamWriter.toString());
    }

    @Test
    public void testAppendCharSequence() {
        streamWriter.append("1234");
        assertEquals("1234", streamWriter.toString());

        streamWriter.append(null);
        assertEquals("1234null", streamWriter.toString());

        streamWriter.append("1234", 1, 2);
        assertEquals("1234null2", streamWriter.toString());

        streamWriter.append(null, 1, 2);
        assertEquals("1234null2u", streamWriter.toString());
    }
}
