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

package org.onap.aaf.misc.xgen.xml;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class JU_XMLGenTest {

    @Mock
    private Writer writer;

    String XML_TAG = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    Map<Character, Integer> map = new TreeMap<>();

    @Before
    public void setUp() throws Exception {
        writer = mock(Writer.class);
    }

    @Test
    public void testXMLGenWriter() throws IOException {
        XMLGen xmlGen = new XMLGen(writer);

        xmlGen.xml();

        for (char ch : XML_TAG.toCharArray()) {
            Integer times = map.get(ch);
            map.put(ch, (times == null ? 0 : times) + 1);
        }

        for (char ch : map.keySet()) {
            verify(writer, times(map.get(ch))).write(ch);
        }
    }
}