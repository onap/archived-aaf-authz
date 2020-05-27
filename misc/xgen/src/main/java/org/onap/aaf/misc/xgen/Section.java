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
import java.io.Writer;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.xgen.html.State;
import org.owasp.encoder.Encode;

public class Section<G extends XGen<G>> {
    protected int indent;
    protected String forward;
    protected String backward;
    
    // Default is to use the set Strings (static) 
    public Section<G> use(State<Env> state, Trans trans, XGenBuff<G> buff) throws APIException, IOException {
        return this;
    }
    
    public int getIndent() {
        return indent;
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public void forward(Writer w) throws IOException {
    	w.write(Encode.forJava(forward));
    }
    
    public void back(Writer w) throws IOException {
    	w.write(Encode.forJava(backward));
    }
    
    public String toString() {
        return forward;
    }
}