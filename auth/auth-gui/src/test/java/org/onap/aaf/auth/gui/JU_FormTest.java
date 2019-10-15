/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.auth.gui;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.html.HTMLGen;

public class JU_FormTest {

    @Mock
    private Cache<HTMLGen> cache;

    @Mock
    private HTMLGen hgen;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void test() throws Exception {
        when(hgen.incr("p", "class=preamble")).thenReturn(hgen);
        when(hgen.text("preamable")).thenReturn(hgen);
        when(hgen.tagOnly("input", "type=submit", "value=Submit")).thenReturn(hgen);
        when(hgen.tagOnly("input", "type=reset", "value=Reset")).thenReturn(hgen);

        Form form = new Form(false, new BreadCrumbs(null));

        assertThat(form.idattrs(), equalTo(new String[] { "breadcrumbs" }));

        assertThat(form.preamble("preamable"), equalTo(form));

        form.code(cache, hgen);
    }

}
