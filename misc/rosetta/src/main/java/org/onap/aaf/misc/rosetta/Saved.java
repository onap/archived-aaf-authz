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

package org.onap.aaf.misc.rosetta;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.rosetta.Saved.State;

/**
 * An Out Object that will save off produced Parsed Stream and 
 * a Parse (In) Object that will reproduce Parsed Stream on demand
 *  
 * @author Jonathan
 *
 */
public class Saved extends Out implements Parse<Reader, State>{
    private static final String ROSETTA_SAVED = "Rosetta Saved";
    private final static int INIT_SIZE=128;
    private Content content[];
    private int idx;
    private boolean append = false;

    /**
     * Read from Parsed Stream and save
     */
    // @Override
    public<IN,S> void extract(IN in, Writer ignore, Parse<IN,S> parser, boolean ... options) throws IOException, ParseException {
        Parsed<S> p = parser.newParsed();
        if (!append) {
            // reuse array  if not too big
            if (content==null||content.length>INIT_SIZE*3) {
                content = new Content[INIT_SIZE];
                idx = -1;
            } else do {
                content[idx]=null;
            } while (--idx>=0);
        }
    
        // Note: idx needs to be -1 on initialization and no appendages
        while ((p = parser.parse(in,p.reuse())).valid()) {
            if (!(append && (p.event==START_DOC || p.event==END_DOC))) { // skip any start/end of document in appendages
                if (++idx>=content.length) {
                    Content temp[] = new Content[content.length*2];
                    System.arraycopy(content, 0, temp, 0, idx);
                    content = temp;
                }
                content[idx]= new Content(p);
            }
        }
    }

    // @Override
    public Parsed<State> parse(Reader ignore, Parsed<State> parsed) throws ParseException {
        int i;
        if ((i=parsed.state.count++)<=idx) 
            content[i].load(parsed);
        else 
            parsed.event = Parse.NONE; 
        return parsed;
    }

    public Content[] cut(char event, int count) {
        append = true;
        for (int i=idx;i>=0;--i) {
            if (content[i].event==event) count--;
            if (count==0) {
                Content[] appended = new Content[idx-i+1];
                System.arraycopy(content, i, appended, 0, appended.length);
                idx = i-1;
                return appended;
            }
        }
        return new Content[0];
    }

    public void paste(Content[] appended) {
        if (appended!=null) {
            if (idx+appended.length>content.length) {
                Content temp[] = new Content[content.length*2];
                System.arraycopy(content, 0, temp, 0, idx);
                content = temp;
            }
            System.arraycopy(appended,0,content,idx+1,appended.length);
            idx+=appended.length;
        }
        this.append = false;
    }

    public static class State {
        public int count = 0;
    }

    public static class Content {
        private boolean isString;
        private char event;
        private String name;
        private List<Prop> props;
        private String str;
    
        public Content(Parsed<?> p) {
            isString = p.isString;
            event = p.event;
            name = p.name;
            // avoid copying, because most elements don't have content
            // Cannot set to "equals", because sb ends up being cleared (and reused)
            str = p.sb.length()==0?null:p.sb.toString();
        }

        public void load(Parsed<State> p) {
            p.isString = isString;
            p.event = event;
            p.name = name;
            if (str!=null)
                p.sb.append(str);
        }
    
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(event);
            sb.append(" - ");
            sb.append(name);
            sb.append(": ");
            if (isString)sb.append('"');
            sb.append(str);
            if (isString)sb.append('"');
            sb.append(' ');
            if (props!=null) {
                boolean comma = false;
                for (Prop prop : props) {
                    if (comma)sb.append(',');
                    else comma = true;
                    sb.append(prop.tag);
                    sb.append('=');
                    sb.append(prop.value);
                }
            }
            return sb.toString();
        }
    }

    //// @Override
    public Parsed<State> newParsed() {
        Parsed<State> ps = new Parsed<State>(new State());
        return ps;
    }

    /**
     * Convenience function
     * @param rdr
     * @param in
     * @throws IOException
     * @throws ParseException
     */
    public<IN,S> void load(IN in, Parse<IN, S> parser) throws IOException, ParseException {
        extract(in,(Writer)null, parser);
    }


    // @Override
    public TimeTaken start(Env env) {
        return env.start(ROSETTA_SAVED, 0);
    }

    @Override
    public String logName() {
        return ROSETTA_SAVED;
    }


}
