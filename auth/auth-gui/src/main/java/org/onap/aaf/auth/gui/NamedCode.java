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

public abstract class NamedCode implements ContentCode {
    private final boolean no_cache;
    private String name;
    private String[] idattrs;

    /*
     *  Mark whether this code should not be cached, and any attributes 
     */
    public NamedCode(final boolean no_cache, final String name) {
        this.name = name;
        idattrs = new String[] {name};
        this.no_cache = no_cache;
    }

    public NamedCode(boolean no_cache, NamedCode content) {
        this.no_cache = no_cache;
        name=content.name;
        idattrs = content.idattrs;
    }

    /**
     * Return ID and Any Attributes needed to create a "div" section of this code
     * @return
     */
    public String[] idattrs() {
        return idattrs;
    }

    public void addAttr(boolean first, String attr) {
        String[] temp = new String[idattrs.length+1];
        if (first) {
            temp[0] = attr;
            System.arraycopy(idattrs, 0, temp, 1, idattrs.length);
        } else {
            temp[idattrs.length] = attr;
            System.arraycopy(idattrs, 0, temp, 0, idattrs.length);
        }
        idattrs = temp;
    }

    public boolean no_cache() {
        return no_cache;
    }
}
