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

package org.onap.aaf.misc.xgen.html;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.misc.env.util.IndentPrintWriter;
import org.onap.aaf.misc.xgen.Back;
import org.onap.aaf.misc.xgen.Mark;

public class JU_JSGenTest {

    @Mock
    private HTMLGen hg;
    @Mock
    private Mark mark;
    @Mock
    private IndentPrintWriter writer;
    @Mock
    private Mark jm;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFileNotFoundException() {
        JSGen gen = new JSGen(mark, hg);

        try {
            gen.inline("JSScript", 2);
            fail("This file should not be found.");
        } catch (Exception e) {

        }
    }

    @Test
    public void testJSRead() throws IOException {
        when(hg.getWriter()).thenReturn(writer);
        JSGen gen = new JSGen(mark, hg);

        gen.inline("./sampletest.js", 2);

        verify(writer).print("function myFunction() {");
        verify(writer).print("document.getElementById(\"demo\").innerHTML = \"Paragraph changed.\";");
        verify(writer).print("}");
        verify(writer, times(0)).println();
    }

    @Test
    public void testJSReadPrettyPrint() throws IOException {
        when(hg.getWriter()).thenReturn(writer);
        hg.pretty = true;
        JSGen gen = new JSGen(mark, hg);

        gen.inline("./sampletest.js", 2);

        verify(writer).print("function myFunction() {");
        verify(writer).print("document.getElementById(\"demo\").innerHTML = \"Paragraph changed.\";");
        verify(writer).print("}");
        verify(writer, times(3)).println();
        verify(hg).setIndent(0);
    }

    @Test
    public void testPst() throws IOException {
        when(hg.getWriter()).thenReturn(writer);
        when(hg.pushBack(any(Back.class))).thenReturn(3);
        hg.pretty = true;
        JSGen gen = new JSGen(mark, hg);

        gen.pst("line 1", "line 2");

        verify(writer).append('(');
        verify(writer).append("line 1");
        verify(writer).print("line 2");
        verify(writer, times(1)).print(", ");
    }

    @Test
    public void testPstWithMark() throws IOException {
        when(hg.getWriter()).thenReturn(writer);
        when(hg.pushBack(any(Back.class))).thenReturn(3);
        JSGen gen = new JSGen(mark, hg);

        gen.pst(jm, "line 1", "line 2");

        verify(writer).append('(');
        verify(writer).append("line 1");
        verify(writer).print("line 2");
        verify(writer, times(1)).print(", ");
    }

    @Test
    public void testPstWithNoLines() throws IOException {
        when(hg.getWriter()).thenReturn(writer);
        when(hg.pushBack(any(Back.class))).thenReturn(3);
        JSGen gen = new JSGen(mark, hg);

        gen.pst(jm);

        verify(writer).append('(');
    }

    @Test
    public void testLi() throws IOException {
        when(hg.getWriter()).thenReturn(writer);
        when(writer.getIndent()).thenReturn(3);

        JSGen gen = new JSGen(mark, hg);

        gen.li("line 1", "line 2");

        verify(writer).setIndent(3);
        verify(writer).inc();
        verify(writer).println();
        verify(writer).print("line 1");
        verify(writer).print("line 2");

        hg.pretty = true;
        gen.li("line 1", "line 2");
        verify(writer, times(3)).println();
    }

    @Test
    public void testText() throws IOException {
        when(hg.getWriter()).thenReturn(writer);
        hg.pretty = true;
        JSGen gen = new JSGen(mark, hg);

        gen.text("line 1");

        verify(writer).append("line 1");
        verify(writer).println();

        hg.pretty = false;
        gen.text("line 1");

        verify(writer, times(2)).append("line 1");
    }

    @Test
    public void testFunction() throws IOException {
        when(hg.getWriter()).thenReturn(writer);
        when(hg.pushBack(any(Back.class))).thenReturn(3);
        hg.pretty = true;
        JSGen gen = new JSGen(mark, hg);

        gen.function("line 1", "line 2", "line 3");

        verify(writer).print("function ");
        verify(writer).print("line 1");
        verify(writer).print('(');

        verify(writer).print("line 2");
        verify(writer).print("line 3");
        verify(writer, times(1)).print(", ");
        verify(writer).print(") {");
        verify(writer).inc();
        verify(writer).println();
    }

    @Test
    public void testFunctionWithMark() throws IOException {
        when(hg.getWriter()).thenReturn(writer);
        when(hg.pushBack(any(Back.class))).thenReturn(3);
        JSGen gen = new JSGen(mark, hg);

        gen.function(jm, "line 1", "line 2", "line 3");

        verify(writer).print("function ");
        verify(writer).print("line 1");
        verify(writer).print('(');

        verify(writer).print("line 2");
        verify(writer).print("line 3");
        verify(writer, times(1)).print(", ");
        verify(writer).print(") {");
        verify(writer, times(0)).inc();
        verify(writer, times(0)).println();
    }

}
