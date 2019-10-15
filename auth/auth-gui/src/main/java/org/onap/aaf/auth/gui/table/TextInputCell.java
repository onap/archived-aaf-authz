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

import org.onap.aaf.misc.xgen.html.HTMLGen;

/**
 * Create an Input Cell for Text
 * @author Jonathan
 *
 */
public class TextInputCell extends AbsCell {
    private static final String[] NULL_ATTRS=new String[0];
    private String[] attrs;

    public TextInputCell(String name, String textClass, String value, String ... attributes) {
        attrs = new String[5 + attributes.length];
        attrs[0]="type=text";
        attrs[1]="name="+name;
        attrs[2]="class="+textClass;
        attrs[3]="value="+value;
        attrs[4]="style=font-size:100%;";
        System.arraycopy(attributes, 0, attrs, 5, attributes.length);
    }

    @Override
    public void write(HTMLGen hgen) {
        hgen.tagOnly("input",attrs);
    }

    @Override
    public String[] attrs() {
        return NULL_ATTRS;
    }
}
