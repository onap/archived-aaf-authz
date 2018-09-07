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

public class JU_StringBuilderOutputStreamTest {

    StringBuilderOutputStream streamBuilder;

    StringBuilder builder = new StringBuilder();

    @Before
    public void setUp() throws Exception {
        streamBuilder = new StringBuilderOutputStream(builder);
    }

    @Test
    public void testWriteIntAndReset() {
        streamBuilder.write(123);

        assertEquals("123", streamBuilder.toString());
        streamBuilder.reset();
        assertEquals("", streamBuilder.toString());
    }

    @Test
    public void testWriteByteArrayWithoutException() throws IOException {
        byte[] bytes = { 1, 2, 3, 4 };
        streamBuilder.write(bytes);
        assertEquals(4, streamBuilder.getBuffer().length());

        streamBuilder.write(bytes, 1, 2);
        assertEquals(6, streamBuilder.getBuffer().length());

        streamBuilder.write(bytes, 1, 0);
        assertEquals(6, streamBuilder.getBuffer().length());

        streamBuilder.append(bytes[0]);
        assertEquals(7, streamBuilder.getBuffer().length());
    }

    @Test
    public void testWriteByteArrayWithIndexOutOfBoundException() {
        byte[] bytes = { 1, 2, 3, 4 };

        try {
            streamBuilder.write(bytes, -1, 2);
            fail("This is supposed to throw IndexOutOfBounds Excetpion");
        } catch (IndexOutOfBoundsException e) {
        } catch (Exception e) {
            fail("This should throw only IndexOutOfBounds Exception");
        }
        assertEquals(0, streamBuilder.getBuffer().length());

    }

    @Test
    public void testDefaultConstructor() throws IOException {
        StringBuilderOutputStream stream = new StringBuilderOutputStream();

        assertNotNull(stream.getBuffer());
        stream.close();
    }

    @Test
    public void testConstructorWithPositiveDefaultCapacity() throws IOException {
        StringBuilderOutputStream stream = new StringBuilderOutputStream(10);

        assertNotNull(stream.getBuffer());
        assertEquals(10, stream.getBuffer().capacity());
        stream.close();
    }

    @Test
    public void testConstructorWithNegativeCapacityException() {
        try {
            StringBuilderOutputStream stream = new StringBuilderOutputStream(-1);
            fail("This should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        } catch (Exception e) {
            fail("This should throw only IllegalArgumentException");
        }
    }

    @Test
    public void testWriteString() {
        streamBuilder.write("1234");

        assertEquals("1234", streamBuilder.toString());

        streamBuilder.write("1234", 1, 2);
        assertEquals("12342", streamBuilder.toString());
    }

    @Test
    public void testAppendCharSequence() {
        streamBuilder.append("1234");
        assertEquals("1234", streamBuilder.toString());

        streamBuilder.append(null);
        assertEquals("1234null", streamBuilder.toString());

        streamBuilder.append("1234", 1, 2);
        assertEquals("1234null2", streamBuilder.toString());

        streamBuilder.append(null, 1, 2);
        assertEquals("1234null2u", streamBuilder.toString());
    }
}
