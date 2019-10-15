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
package org.onap.aaf.auth.gui;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.auth.gui.Table.Data;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.TransStore;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.Code;
import org.onap.aaf.misc.xgen.html.HTMLGen;

public class JU_TableTest {

    @Mock
    private TransStore trans;
    private Code<HTMLGen> other;
    @Mock
    private Data data;
    @Mock
    private Cache cache;
    @Mock
    private HTMLGen hgen;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void test() throws APIException, IOException {
        when(hgen.leaf("caption", "class=title")).thenReturn(hgen);
        when(hgen.text("title")).thenReturn(hgen);
        when(data.headers()).thenReturn(new String[0]);

        Table table = new Table("title", trans, data, other, "name", "attr1", "attr1");
        Table.Cells cells = new Table.Cells(new ArrayList(), "");

        table.code(cache, hgen);

        verify(hgen).end();
    }

}
