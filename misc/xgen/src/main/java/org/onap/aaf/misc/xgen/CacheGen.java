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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.xgen.html.State;
import org.onap.aaf.misc.xgen.html.Thematic;


public abstract class CacheGen<G extends XGen<G>> {
    public static final int NO_FLAGS = 0x0;
    public final static int PRETTY    = 0x1;
    public final static int XML        = 0x2;
    public final static int HTML4     = 0x4;
    public final static int HTML5     = 0x8;

    
    private ArrayList<Section<G>> sections = new ArrayList<>();
    private int flags;
    private final Thematic thematic;

    public CacheGen(int flags, Code<G> code) throws APIException, IOException {
        this.flags = flags;
        final XGenBuff<G> buff = new XGenBuff<>(flags,this);
        // Run to gather Strings and Code Class Segments
        buff.run(new Cache<G>() {
                @Override
                public void dynamic(G hgen, Code<G> code) {
                    sections.add(buff.newSection());
                    sections.add(new Dynamic(hgen.getIndent(),code));
                }
            },code);
        sections.add(buff.newSection());
    
        // If Code implements thematic, set for later
        thematic = code instanceof Thematic?(Thematic)code:null;

    }
    
    public abstract G create(int htmlStyle, Writer w);

    public void replay(State<Env> state, Trans trans, OutputStream os, String theme) throws IOException, APIException {
        replay(state, trans, new OutputStreamWriter(os), theme);
    }
    
    public void replay(State<Env> state, Trans trans,Writer w, String theme) throws IOException, APIException {
        if (thematic!=null) {
            theme = thematic.themeResolve(theme);
        }
        /* Theme
        trans.setTheme(theme);
        int htmlStyle = state.htmlVer(theme);
        */
        
        XGenBuff<G> buff = new XGenBuff<G>(flags,this);
        
        // forward
        int indent = 0;
        Section<G> s;
        int i=0;
        @SuppressWarnings("unchecked")
        Section<G>[] reverse = new Section[sections.size()];
        for (Section<G> section : sections) {
            s = section.use(state, trans, buff); // note, doesn't change cached, only dynamic, which is created for thread
            int tempIndent = s.getIndent();
            s.setIndent(indent);
            s.forward(w);
            s.setIndent(tempIndent);
            indent = tempIndent;
            reverse[i++]=s;
        }

        for (--i;i>=0;--i) {
            reverse[i].back(w);
        }
        w.flush();
    }
    
    private class Dynamic extends Section<G> {
        private Code<G> code;
        
        public Dynamic(int indent, Code<G> code) {
            this.code = code;
            this.indent = indent;
        }

        @SuppressWarnings("unchecked")
        public Section<G> use(State<Env> state, Trans trans, XGenBuff<G> buff) throws APIException, IOException {
            // Clone Dynamic to make Thread Safe
            Dynamic d = new Dynamic(indent,code);
            buff.setIndent(indent);
            if (code instanceof DynamicCode) {
                buff.run(state,trans,Cache.Null.singleton(), (DynamicCode<G,?,? extends Trans>)code);
            } else {
                buff.run((Cache<G>)Cache.Null.singleton(), code);
            }
            Section<G> s = buff.newSection();
            d.indent = s.indent;
            d.forward = s.forward;
            d.backward = s.backward;
            return d;
        }
    }
}
