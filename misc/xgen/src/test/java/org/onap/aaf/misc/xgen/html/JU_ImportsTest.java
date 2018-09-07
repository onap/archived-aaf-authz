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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class JU_ImportsTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() {
        Imports imports = new Imports(2);
        imports.css("styles.css");
        imports.js("main.js");
        imports.theme("New Theme");

        assertEquals("New Theme", imports.themeResolve(null));
        assertEquals("New Theme", imports.themeResolve(""));
        assertEquals("The Theme", imports.themeResolve("The Theme"));

        assertEquals("build/../../", imports.dots(new StringBuilder("build/")).toString());
        assertEquals("../../Theme/", imports.themePath("Theme"));
        assertEquals("../../New Theme/", imports.themePath(""));
        assertEquals("../../New Theme/", imports.themePath(null));

        imports.theme(null);
        assertEquals("../../", imports.themePath(null));
    }

}
