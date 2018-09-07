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
package org.onap.aaf.misc.xgen;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.misc.env.APIException;

public class JU_SectionTest {

    @Mock
    private Writer writer;

    @Before
    public void setup() {
        writer = mock(Writer.class);
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void test() throws APIException, IOException {
        Section section = new Section();
        section.forward = "Forward";
        section.backward = "Backward";

        section.setIndent(10);
        section.forward(writer);
        section.back(writer);

        assertEquals(section.use(null, null, null), section);
        assertEquals(section.getIndent(), 10);
        assertEquals(section.toString(), "Forward");

        verify(writer).write("Forward");
        verify(writer).write("Backward");
    }

}
