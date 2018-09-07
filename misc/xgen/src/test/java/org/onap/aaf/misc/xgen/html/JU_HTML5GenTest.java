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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class JU_HTML5GenTest {

//    private final static String DOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\""
//            + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";

    private String charset = "utf-8";

    private final String CHARSET_LINE = "<meta charset=\"" + charset + "\">";

    @Mock
    Writer w;

    @Before
    public void setUp() throws Exception {

        w = mock(Writer.class);
    }

    @Test
    public void testHTML() throws IOException {

        HTML5Gen gen = new HTML5Gen(w);

        gen.html("attributes");

        Map<Character, Integer> map = new TreeMap<>();

        for (char ch : "html".toCharArray()) {
            Integer times = map.get(ch);
            map.put(ch, (times == null ? 0 : times) + 1);
        }

        for (char ch : map.keySet()) {
            verify(w, atLeast(map.get(ch))).write(ch);
        }
        verify(w, atLeast(1)).write(anyInt());
    }

    @Test
    public void testHead() throws IOException {

        HTML5Gen gen = new HTML5Gen(w);

        gen.head();

        Map<Character, Integer> map = new TreeMap<>();

        for (char ch : "head".toCharArray()) {
            Integer times = map.get(ch);
            map.put(ch, (times == null ? 0 : times) + 1);
        }

        for (char ch : map.keySet()) {
            verify(w, atLeast(map.get(ch))).write(ch);
        }
    }

    @Test
    public void testBody() throws IOException {

        HTML5Gen gen = new HTML5Gen(w);

        gen.body("attributes");

        Map<Character, Integer> map = new TreeMap<>();

        for (char ch : "body".toCharArray()) {
            Integer times = map.get(ch);
            map.put(ch, (times == null ? 0 : times) + 1);
        }
        for (char ch : "attributes".toCharArray()) {
            Integer times = map.get(ch);
            map.put(ch, (times == null ? 0 : times) + 1);
        }

        for (char ch : map.keySet()) {
            verify(w, atLeast(map.get(ch))).write(ch);
        }
    }

    @Test
    public void testCharSet() throws IOException {

        HTML5Gen gen = new HTML5Gen(w);

        gen.charset(charset);

        Map<Character, Integer> map = new TreeMap<>();

        for (char ch : CHARSET_LINE.toCharArray()) {
            Integer times = map.get(ch);
            map.put(ch, (times == null ? 0 : times) + 1);
        }

        for (char ch : map.keySet()) {
            verify(w, atLeast(map.get(ch))).write(ch);
        }
    }
}
