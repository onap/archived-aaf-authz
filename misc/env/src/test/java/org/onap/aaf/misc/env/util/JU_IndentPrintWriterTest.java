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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class JU_IndentPrintWriterTest {

    @Mock
    private OutputStream stream;

    @Mock
    private Writer writer;

    @Before
    public void setUp() throws Exception {
        stream = mock(OutputStream.class);
        writer = mock(Writer.class);
    }

    @Test
    public void testWriteInt() throws IOException {
        IndentPrintWriter indentWriter = new IndentPrintWriter(writer);

        indentWriter.write(123);

        verify(writer).write(123);

        assertEquals(indentWriter.getIndent(), 0);
    }

    @Test
    public void testWriteIntWithNewLineCharacter() throws IOException {
        IndentPrintWriter indentWriter = new IndentPrintWriter(writer);

        indentWriter.setIndent(12);

        indentWriter.println();

        indentWriter.write("123", 1, 2);

        verify(writer).write('\n');
        verify(writer).write('2');
        verify(writer).write('3');
        assertEquals(indentWriter.getIndent(), 12);
    }

    @Test
    public void testWriteString() throws IOException {
        IndentPrintWriter indentWriter = new IndentPrintWriter(writer);

        indentWriter.inc();

        indentWriter.write("123");

        verify(writer).write('1');
        verify(writer).write('2');
        verify(writer).write('3');
        assertEquals(indentWriter.getIndent(), 1);
    }

    @Test
    public void testSetIndent() throws IOException {
        IndentPrintWriter indentWriter = new IndentPrintWriter(stream);

        indentWriter.setIndent(12);
        indentWriter.dec();

        assertEquals(indentWriter.getIndent(), 11);
    }

    @Test
    public void testToCol() throws IOException {
        IndentPrintWriter indentWriter = new IndentPrintWriter(writer);

        indentWriter.toCol(5);
        char[] chars = { 'a', 'b', 'c' };
        indentWriter.write(chars, 1, 2);

        verify(writer, times(5)).write(' ');
        verify(writer).write('c');
        verify(writer).write('b');
    }
}