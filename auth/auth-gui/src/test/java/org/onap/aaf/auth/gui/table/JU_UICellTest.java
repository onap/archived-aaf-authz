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

package org.onap.aaf.auth.gui.table;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.auth.gui.table.CheckBoxCell.ALIGN;
import org.onap.aaf.misc.xgen.html.HTMLGen;

public class JU_UICellTest {
    @Mock
    private HTMLGen hgen;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void testButtonCell() {
        String[] attrs = { "type=button", "value=null", "attribute1", "attribute2" };
        ButtonCell cell = new ButtonCell(null, "attribute1", "attribute2");

        when(hgen.incr("input", true, attrs)).thenReturn(hgen);

        cell.write(hgen);

        AbsCell.Null.write(hgen);

        assertThat(AbsCell.Null.attrs(), equalTo(new String[0]));

        assertThat(cell.attrs(), equalTo(AbsCell.CENTER));

        verify(hgen).end();
    }

    @Test
    public void testCheckBoxCellWithoutAlign() {
        String[] attrs = { "type=checkbox", "name=name", "value=attribute1", "attribute2" };
        CheckBoxCell cell = new CheckBoxCell("name", "attribute1", "attribute2");

        cell.write(hgen);

        assertThat(cell.attrs(), equalTo(AbsCell.CENTER));

        verify(hgen).tagOnly("input", attrs);
    }

    @Test
    public void testCheckBoxCellWithLeftAlign() {
        String[] attrs = { "type=checkbox", "name=name", "value=attribute1", "attribute2" };
        CheckBoxCell cell = new CheckBoxCell("name", ALIGN.left, "attribute1", "attribute2");

        cell.write(hgen);

        assertThat(cell.attrs(), equalTo(AbsCell.LEFT));

        verify(hgen).tagOnly("input", attrs);
    }

    @Test
    public void testCheckBoxCellWithRightAlign() {
        String[] attrs = { "type=checkbox", "name=name", "value=attribute1", "attribute2" };
        CheckBoxCell cell = new CheckBoxCell("name", ALIGN.right, "attribute1", "attribute2");

        cell.write(hgen);

        assertThat(cell.attrs(), equalTo(AbsCell.RIGHT));

        verify(hgen).tagOnly("input", attrs);
    }

    @Test
    public void testRadioCell() {
        String[] attrs = { "type=radio", "name=name", "class=attribute1", "value=attribute2" };
        RadioCell cell = new RadioCell("name", "attribute1", "attribute2");

        cell.write(hgen);

        assertThat(cell.attrs(), equalTo(AbsCell.CENTER));

        verify(hgen).tagOnly("input", attrs);
    }

    @Test
    public void testRefCellWithNewWindow() {
        String[] attrs = { "href=attribute1", "attribute2", null };
        RefCell cell = new RefCell("name", "attribute1", true, "attribute2");

        when(hgen.leaf(HTMLGen.A, attrs)).thenReturn(hgen);

        cell.write(hgen);

        assertThat(cell.attrs(), equalTo(new String[0]));
    }

    @Test
    public void testRefCellWithoutNewWindow() {
        String[] attrs = { "href=attribute1", "attribute2" };
        RefCell cell = new RefCell("name", "attribute1", false, "attribute2");

        when(hgen.leaf(HTMLGen.A, attrs)).thenReturn(hgen);

        cell.write(hgen);

        assertThat(cell.attrs(), equalTo(new String[0]));

    }

    @Test
    public void testTextAndRefCell() {
        String[] attrs = { "href=href", "attribute1", null };
        String[] attributes = { "attribute1" };
        TextAndRefCell cell = new TextAndRefCell("text", "name", "href", true, attributes);

        when(hgen.leaf(HTMLGen.A, attrs)).thenReturn(hgen);

        cell.write(hgen);

        verify(hgen).text("text");
    }

    @Test
    public void testTextCell() {
        String[] attrs = { "href" };
        TextCell cell = new TextCell("name", "href");

        cell.write(hgen);

        assertThat(cell.attrs(), equalTo(attrs));

        verify(hgen).text("name");
    }

    @Test
    public void testTextInputCell() {
        String[] attrs = { "href" };
        TextInputCell cell = new TextInputCell("name", "textClass", "value");

        cell.write(hgen);

        assertThat(cell.attrs(), equalTo(new String[0]));
    }
}
