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

import org.onap.aaf.auth.gui.Table;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.html.HTMLGen;
import org.onap.aaf.misc.xgen.html.State;

public abstract class TableData<S extends State<Env>, TRANS extends Trans> implements Table.Data<S,TRANS>{
    public static final String[] headers = new String[0];

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.gui.Table.Data#prefix(org.onap.aaf.misc.xgen.html.State, com.att.inno.env.Trans, org.onap.aaf.misc.xgen.Cache, org.onap.aaf.misc.xgen.html.HTMLGen)
     */
    @Override
    public void prefix(final S state, final TRANS trans, final Cache<HTMLGen> cache, final HTMLGen hgen) {
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.gui.Table.Data#postfix(org.onap.aaf.misc.xgen.html.State, com.att.inno.env.Trans, org.onap.aaf.misc.xgen.Cache, org.onap.aaf.misc.xgen.html.HTMLGen)
     */
    @Override
    public void postfix(final S state, final TRANS trans, final Cache<HTMLGen> cache, final HTMLGen hgen) {
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.gui.Table.Data#headers()
     */
    @Override
    public String[] headers() {
        return headers;
    }

}
