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

package org.onap.aaf.misc.rosetta;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.rosetta.InXML.State;

public class InXML implements Parse<Reader, State> {
    // package on purpose
    JaxInfo jaxInfo;

    public InXML(JaxInfo jaxInfo) {
        this.jaxInfo = jaxInfo;
    }

    public InXML(Class<?> cls, String ... rootNs) throws SecurityException, NoSuchFieldException, ClassNotFoundException, ParseException {
        jaxInfo = JaxInfo.build(cls,rootNs);
    }


    // @Override
    public Parsed<State> parse(Reader r, Parsed<State> parsed) throws ParseException {
        State state = parsed.state;
    
        // OK, before anything else, see if there is leftover processing, if so, do it!
        if (state.unevaluated!=null) {
            DerTag dt = state.unevaluated;
            state.unevaluated = null;
            if (!state.greatExp.eval(parsed, dt))return parsed;
        }

        if (state.hasAttributes()) {
            Prop prop = state.pop();
            parsed.event = Parse.ATTRIB;
            parsed.name = prop.tag;
            parsed.sb.append(prop.value);
            parsed.isString=true;
            return parsed;
        }
        int ch;
        char c;
        boolean inQuotes = false, escaped = false;

        StringBuilder sb = parsed.sb, tempSB = new StringBuilder();
        boolean go = true;
    
        try {
            while (go && (ch=r.read())>=0) {
                c = (char)ch;
                if (c == '"') {
                    if (state.greatExp instanceof LeafExpectations) { // within a set of Tags, make a Quote
                        sb.append(c);
                    } else {
                        if (inQuotes) {
                            if (escaped) {
                                sb.append('\\');
                                sb.append(c);
                                escaped = false;
                            } else {
                                inQuotes = false;
                            }
                        } else {
                            parsed.isString=true;
                            inQuotes = true;
                        }
                    }
                } else if (inQuotes) {
                    sb.append(c);
                } else if (c=='&') {
                    XmlEscape.xmlEscape(sb,r);
                } else if (c=='\\') {
                    escaped=true;
                } else {
                    switch(c) {
                        case '<':
                            DerTag tag=new DerTag().parse(r, tempSB);
                            go = state.greatExp.eval(parsed, tag);
                            break;
                        default:
                            // don't add Whitespace to start of SB... saves removing later
                            if (sb.length()>0) {
                                sb.append(c);
                            } else if (!Character.isWhitespace(c)) { 
                                sb.append(c);
                            }
                        }
                }
            }
            return parsed;
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    public static final class DerTag {
        public String name;
        public boolean isEndTag;
        public List<Prop> props;
        private boolean isXmlInfo;
        //private String ns; 
    
        public DerTag() {
            name=null;
            isEndTag = false;
            props = null;
            isXmlInfo = false;
        }
    
        public DerTag parse(Reader r, StringBuilder sb) throws ParseException {
            int ch;
            char c;
            boolean inQuotes = false, escaped = false;
            boolean go = true;
            String tag = null;
        
            try {
                if ((ch = r.read())<0) throw new ParseException("Reader content ended before complete");
                if (ch=='?') {
                    isXmlInfo = true;
                }
                // TODO Check for !-- comments
                do {
                    c=(char)ch;
                     if (c=='"') {
                            if (inQuotes) {
                                if (escaped) {
                                    sb.append(c);
                                    escaped = false;
                                } else {
                                    inQuotes = false;
                                }
                            } else {
                                inQuotes = true;
                            }
                     } else if (inQuotes) {
                         sb.append(c);
                     } else {
                         switch(c) {
                            case '/':
                                isEndTag = true;
                                break;
                            case ' ':
                                endField(tag,sb);
                                tag = null;
                                break;
                            case '>':
                                endField(tag,sb);
                                go = false;
                                break;
                            case '=':
                                tag = sb.toString();
                                sb.setLength(0);
                                break;
//                            case ':':
//                                ns = sb.toString();
//                                sb.setLength(0);
//                                break;
                            case '?':
                                if (!isXmlInfo)sb.append(c);
                                break;
                            default:
                                sb.append(c);
                         }
                     }
                } while (go && (ch=r.read())>=0);
            } catch (IOException e) {
                throw new ParseException(e);
            }
            return this;
        }

        private void endField(String tag, StringBuilder sb) {
            if (name==null) {
                name = sb.toString();
                sb.setLength(0);
            } else {
                String value = sb.toString();
                sb.setLength(0);
                if (tag !=null && value != null) {
                    if (props==null)props = new ArrayList<>();
                    props.add(new Prop(tag,value));
                }
            }
        }
    
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(isEndTag?"End":"Start");
            sb.append(" Tag\n");
            sb.append("  Name: ");
            sb.append(name);
            if (props!=null) for (Prop p : props) {
                sb.append("\n     ");
                sb.append(p.tag);
                sb.append("=\"");
                sb.append(p.value);
                sb.append('"');
            }
            return sb.toString();
        }
    }

    private static class ArrayState {
        public boolean firstObj = true;
        public boolean didNext = false;
    }

    public static class State {
        public GreatExpectations greatExp;
        public DerTag unevaluated;
        public Stack<ArrayState> arrayInfo;
        private List<Prop> attribs;
        private int idx;
        public State(JaxInfo ji, DerTag dt) throws ParseException {
            greatExp = new RootExpectations(this, ji, null);
            unevaluated = null;
            attribs = null;;
        }
    
        public boolean hasAttributes() {
            return attribs!=null && idx<attribs.size();
        }

        public void push(Prop prop) {
            if (attribs==null) {
                attribs = new ArrayList<>();
                idx = 0;
            }
            attribs.add(prop);
        }
    
        public Prop pop() {
            Prop rv = null;
            if (attribs!=null) {
                rv = attribs.get(idx++);
                if (idx>=attribs.size())attribs = null;
            }
            return rv;
        }
    }

    private static abstract class GreatExpectations {
        protected JaxInfo ji;
        protected GreatExpectations prev;
        private Map<String,String> ns;
    
        public GreatExpectations(State state, JaxInfo curr, GreatExpectations prev, DerTag derTag) throws ParseException {
            this.prev = prev;
            ns = null;
            ji = getDerived(state, curr,derTag);
        }
    
        public abstract boolean eval(Parsed<State> parsed, DerTag derTag) throws ParseException;

        // Recursively look back for any namespaces
        protected Map<String,String> getNS() {
            if (ns!=null)return ns;
            if (prev!=null) {
                return prev.getNS();
            }
            return null;
        }

        private void addNS(Prop prop) {
            Map<String,String> existingNS = getNS();
            if (ns==null)ns = new HashMap<>();
            // First make a copy of previous NSs so that we have everything we need, but can overwrite, if necessary
            if (existingNS!=null && ns!=existingNS) {
                ns.putAll(ns);
            }
            ns.put(prop.tag, prop.value);
        }

        private JaxInfo getDerived(State state, JaxInfo ji, DerTag derTag) throws ParseException {
            if (derTag==null)return ji;
        
            List<Prop> props = derTag.props;
        
            Prop derived = null;
            if (props!=null) {
                // Load Namespaces (if any)
                for (Prop prop : props) {
                    if (prop.tag.startsWith("xmlns:")) {
                        addNS(prop);
                    }
                }
                for (Prop prop : props) {
                    if (prop.tag.endsWith(":type")) {
                        int idx = prop.tag.indexOf(':');
                        String potentialNS = "xmlns:"+prop.tag.substring(0,idx);
                        Map<String,String> ns = getNS();
                        boolean noNamespace = false;
                        if (ns==null) {
                            noNamespace = true;
                        } else {
                            String nsVal = ns.get(potentialNS);
                            if (nsVal==null) noNamespace = true;
                            else {
                                derived = new Prop(Parsed.EXTENSION_TAG,prop.value);
                                state.push(derived);
                            }
                        }
                        if (noNamespace) {
                            throw new ParseException(prop.tag + " utilizes an invalid Namespace prefix");
                        }
                    } else if (!prop.tag.startsWith("xmlns")) {
                        state.push(prop);
                    }
                }
            }
            return derived==null?ji:ji.getDerived(derived.value);
        }
    }

    private static class RootExpectations extends GreatExpectations {
    
        public RootExpectations(State state, JaxInfo curr, GreatExpectations prev) throws ParseException {
            super(state,curr,prev, null);
        }
    
        // @Override
        public boolean eval(Parsed<State> parsed, DerTag derTag) throws ParseException {
            if (derTag.isXmlInfo) {
                parsed.event = START_DOC;
            } else if (ji.name.equals(derTag.name)) {
                if (derTag.isEndTag) {
                    parsed.event = END_DOC;
                    parsed.state.greatExp = prev;
                } else {
                    //parsed.name = derTag.name;
                    parsed.event = START_OBJ;
                    parsed.state.greatExp = new ObjectExpectations(parsed.state,ji, this, false, derTag);
                }
            }
            return false;
        }
    }

    private static class ObjectExpectations extends GreatExpectations {
        private boolean printName;

        public ObjectExpectations(State state, JaxInfo curr, GreatExpectations prev, boolean printName, DerTag derTag) throws ParseException {
            super(state, curr, prev, derTag);
            this.printName=printName;
        }

        // @Override
        public boolean eval(Parsed<State> parsed, DerTag derTag) throws ParseException {
            if (derTag.isEndTag && ji.name.equals(derTag.name)) {
                parsed.state.greatExp = prev;
                parsed.event = END_OBJ;
                if (printName)parsed.name = ji.name;
            } else {
                //Standard Members
                for (JaxInfo memb : ji.members) {
                    if (memb.name.equals(derTag.name)) {
                        parsed.name = memb.name;
                        if (memb.isArray) {
                            parsed.state.unevaluated = derTag; // evaluate within Array Context
                            parsed.event = START_ARRAY;
                            parsed.state.greatExp = new ArrayExpectations(parsed.state,memb,this);
                            return false;
                        } else if (memb.isObject()) {
                            if (derTag.isEndTag) {
                                throw new ParseException("Unexpected End Tag </" + derTag.name + '>');
                            } else {
                                parsed.event = START_OBJ;

                                parsed.state.greatExp = new ObjectExpectations(parsed.state, memb,this,true,derTag);
                                return false;
                            }
                        } else { // a leaf
                            if (derTag.isEndTag) {
                                 throw new ParseException("Misplaced End Tag </" + parsed.name + '>');
                            } else {
                                parsed.state.greatExp = new LeafExpectations(parsed.state,memb, this);
                                return true; // finish out Leaf without returning
                            }
                        }
                    }
                }

                throw new ParseException("Unexpected Tag <" + derTag.name + '>');
            }
            return false;
        }
    }

    private static class LeafExpectations extends GreatExpectations {
        public LeafExpectations(State state, JaxInfo curr, GreatExpectations prev) throws ParseException {
            super(state, curr, prev, null);
        }

        // @Override
        public boolean eval(Parsed<State> parsed, DerTag derTag) throws ParseException {
            if (ji.name.equals(derTag.name) && derTag.isEndTag) {
                parsed.event = NEXT;
                parsed.isString = ji.isString;
                parsed.state.greatExp = prev;
            } else {
                throw new ParseException("Expected </" + ji.name + '>');
            }
            return false;
        }    
    }

    private static class ArrayExpectations extends GreatExpectations {
        public ArrayExpectations(State state, JaxInfo ji, GreatExpectations prev) throws ParseException {
            super(state, ji, prev,null);
            if (state.arrayInfo==null)state.arrayInfo=new Stack<ArrayState>();
            state.arrayInfo.push(new ArrayState());
        }
        // @Override
        public boolean eval(Parsed<State> parsed, DerTag derTag) throws ParseException {
            if (ji.name.equals(derTag.name) && !derTag.isEndTag) {
                if (ji.isObject()) {
                    if (derTag.isEndTag) {
                        throw new ParseException("Unexpected End Tag </" + derTag.name + '>');
                    } else {
                        ArrayState ai = parsed.state.arrayInfo.peek();  
                        if (ai.firstObj || ai.didNext) {
                            ai.firstObj = false;
                            ai.didNext = false;
                            parsed.event = START_OBJ;
                            parsed.name=derTag.name;
                            parsed.state.greatExp = new ObjectExpectations(parsed.state,ji,this,true, derTag);
                        } else {
                            ai.didNext = true;
                            parsed.event = NEXT;
                            parsed.state.unevaluated = derTag;
                        }
                    }
                } else { // a leave
                    if (derTag.isEndTag) {
                         throw new ParseException("Misplaced End Tag </" + parsed.name + '>');
                    } else {
                        parsed.state.greatExp = new LeafExpectations(parsed.state, ji, this);
                        return true; // finish out Leaf without returning
                    }
                }
            } else { // Tag now different... Array is done
                parsed.state.unevaluated = derTag;
                parsed.event=END_ARRAY;
                parsed.state.greatExp = prev;
                parsed.state.arrayInfo.pop();
            }
            return false;
        }    
    }
    // @Override
    public Parsed<State> newParsed() throws ParseException {
        return new Parsed<State>(new State(jaxInfo, null));
    }

    // @Override
    public TimeTaken start(Env env) {
        return env.start("Rosetta XML In", Env.XML);
    }

}
