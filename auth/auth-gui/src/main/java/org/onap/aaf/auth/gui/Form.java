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

import java.io.IOException;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.html.HTMLGen;

public class Form extends NamedCode {
    private String preamble;
    private NamedCode content;

    public Form(boolean no_cache, NamedCode content) {
        super(no_cache,content);
        this.content = content;
        preamble=null;
    }

    public Form preamble(String preamble) {
        this.preamble = preamble;
        return this;
    }


    @Override
    public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
        if (preamble!=null) {
            hgen.incr("p","class=preamble").text(preamble).end();
        }
        hgen.incr("form","method=post");

        content.code(cache, hgen);

        hgen.tagOnly("input", "type=submit", "value=Submit")
            .tagOnly("input", "type=reset", "value=Reset")
        .end();
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.gui.NamedCode#idattrs()
     */
    @Override
    public String[] idattrs() {
        return content.idattrs();
    }

}
