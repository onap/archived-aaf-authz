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

import org.onap.aaf.misc.xgen.Code;
import org.onap.aaf.misc.xgen.html.HTMLGen;

/**
 * Interface for which Page, etc can get Attributes, determine whether cached, etc
 * @author Jonathan
 *
 */
public interface ContentCode extends Code<HTMLGen> {
    public String[] idattrs();
    public void addAttr(boolean first, String attr);
    public boolean no_cache();
}
