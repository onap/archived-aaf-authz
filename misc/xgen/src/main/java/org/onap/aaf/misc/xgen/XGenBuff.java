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

package org.onap.aaf.misc.xgen;

import java.io.IOException;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.StringBuilderWriter;
import org.onap.aaf.misc.xgen.html.State;

public class XGenBuff<G extends XGen<G>> {
    private G xgen;
    private StringBuilder sb;
    // private String forward, backward;
    
    public XGenBuff(int flags, CacheGen<G> cg) {
        sb = new StringBuilder();
        xgen = cg.create(flags, new StringBuilderWriter(sb));
    }

    /**
     * Normal case of building up Cached HTML without transaction info
     * 
     * @param cache
     * @param code
     * @throws APIException
     * @throws IOException
     */
    public void run(Cache<G> cache, Code<G> code) throws APIException, IOException {
        code.code(cache, xgen);
    }

    /**
     * Special Case where code is dynamic, so give access to State and Trans info
     *  
     * @param state
     * @param trans
     * @param cache
     * @param code
     * @throws APIException
     * @throws IOException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void run(State<Env> state, Trans trans, Cache cache, DynamicCode code) throws APIException, IOException {
            code.code(state, trans, cache, xgen);
    }
    
    public int getIndent() {
        return xgen.getIndent();
    }

    public void setIndent(int indent) {
        xgen.setIndent(indent);
    }

    public Section<G> newSection() {
        Section<G> s = new Section<G>();
        s.indent = xgen.getIndent();
        s.forward = sb.toString();
        sb.setLength(0);
        s.backward = sb.toString();
        sb.setLength(0);
        return s;
    }
}
