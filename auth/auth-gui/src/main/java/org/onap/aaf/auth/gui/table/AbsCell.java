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

public abstract class AbsCell {
    public static final AbsCell[] HLINE =    new AbsCell[0];
    private static final String[] NONE =     new String[0];
    protected static final String[] CENTER = new String[]{"class=center"};
    protected static final String[] LEFT =   new String[]{"class=left"};
    protected static final String[] RIGHT =  new String[]{"class=right"};

    /**
     * Write Cell Data with HTMLGen generator
     * @param hgen
     */
    public abstract void write(HTMLGen hgen);

    public final static AbsCell Null = new AbsCell() {
        @Override
        public void write(final HTMLGen hgen) {
        }
    };

    public String[] attrs() {
        return NONE;
    }
}
