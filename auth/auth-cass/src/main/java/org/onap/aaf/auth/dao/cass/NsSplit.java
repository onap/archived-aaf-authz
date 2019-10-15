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

package org.onap.aaf.auth.dao.cass;

public class NsSplit {
    public final String ns;
    public final String name;
    public final NsDAO.Data nsd;

    public NsSplit(NsDAO.Data nsd, String child) {
        this.nsd = nsd;
        if (child.startsWith(nsd.name)) {
            ns = nsd.name;
            int dot = ns.length();
            if (dot<child.length() && child.charAt(dot)=='.') {
                name = child.substring(dot+1);
            } else {
                name="";
            }
        } else {
            name=null;
            ns = null;
        }
    }

    public NsSplit(String ns, String name) {
        this.ns = ns;
        this.name = name;
        this.nsd = new NsDAO.Data();
        nsd.name = ns;
        int dot = ns.lastIndexOf('.');
        if (dot>=0) {
            nsd.parent = ns.substring(0, dot);
        } else {
            nsd.parent = ".";
        }
    }

    public boolean isOK() {
        return ns!=null && name !=null;
    }
}