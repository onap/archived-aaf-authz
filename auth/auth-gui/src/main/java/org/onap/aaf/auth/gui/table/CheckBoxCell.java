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

package org.onap.aaf.auth.gui.table;

import org.onap.aaf.misc.xgen.html.HTMLGen;

public class CheckBoxCell extends AbsCell {
    public enum ALIGN{ left, right, center };
    private String[] attrs;
    private ALIGN align;

    public CheckBoxCell(String name, ALIGN align, String value, String ... attributes) {
        this.align = align;
        attrs = new String[3 + attributes.length];
        attrs[0]="type=checkbox";
        attrs[1]="name="+name;
        attrs[2]="value="+value;
        System.arraycopy(attributes, 0, attrs, 3, attributes.length);
    }

    public CheckBoxCell(String name, String value, String ... attributes) {
        this.align = ALIGN.center;
        attrs = new String[3 + attributes.length];
        attrs[0]="type=checkbox";
        attrs[1]="name="+name;
        attrs[2]="value="+value;
        System.arraycopy(attributes, 0, attrs, 3, attributes.length);
    }

    @Override
    public void write(HTMLGen hgen) {
        hgen.tagOnly("input",attrs);
    }

    @Override
    public String[] attrs() {
        switch(align) {
            case left:
                return AbsCell.LEFT;
            case right:
                return AbsCell.RIGHT;
            case center:
                default:
                return AbsCell.CENTER;
            }
    }
}
