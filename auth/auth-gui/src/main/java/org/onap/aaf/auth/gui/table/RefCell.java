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

import static org.onap.aaf.misc.xgen.html.HTMLGen.A;

import org.onap.aaf.misc.xgen.html.HTMLGen;

/**
 * Write a Reference Link into a Cell
 * @author Jonathan
 *
 */
public class RefCell extends AbsCell {
    public final String name;
    public final String[] str;

    public RefCell(String name, String href, boolean newWindow, String... attributes) {
        this.name = name;
        if (newWindow) {
            str = new String[attributes.length+2];
            str[attributes.length]="target=_blank";
        } else {
            str = new String[attributes.length+1];
        }
        str[0]="href="+href;
        System.arraycopy(attributes, 0, str, 1, attributes.length);

    }

    @Override
    public void write(HTMLGen hgen) {
        hgen.leaf(A,str).text(name);
    }
}
