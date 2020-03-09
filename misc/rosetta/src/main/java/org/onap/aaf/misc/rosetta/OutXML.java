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
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.onap.aaf.misc.env.util.IndentPrintWriter;
import org.onap.aaf.misc.env.util.StringBuilderWriter;
import org.owasp.encoder.Encode;

public class OutXML extends Out{
    private static final String XMLNS_XSI = "xmlns:xsi";
    public static final String XML_INFO = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    public static final String XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";

    private String root;
    private List<Prop> props;

    public OutXML(String root, String ... params) {
        this.root = root;
        props = new ArrayList<>();
        for (String p : params) {
            String[] tv=p.split("=");
            if (tv.length==2)
                props.add(new Prop(tv[0],tv[1]));
        }
    }

    public OutXML(JaxInfo jaxInfo) {
        this(jaxInfo.name,genNS(jaxInfo));
    }

    public OutXML(InXML inXML) {
        this(inXML.jaxInfo.name,genNS(inXML.jaxInfo));
    }

    private static String[] genNS(JaxInfo jaxInfo) {
        return new String[] {"xmlns=" + jaxInfo.ns};
    }


    @Override
    public<IN,S> void extract(IN in, Writer writer, Parse<IN,S> prs, boolean ... options) throws IOException, ParseException {
        Parsed<S> p = prs.newParsed();
        Stack<Level> stack = new Stack<Level>();
        // If it's an IndentPrintWriter, it is pretty printing.
        boolean pretty = (options.length>0&&options[0]);

        IndentPrintWriter ipw;
        if (pretty) {
            if (writer instanceof IndentPrintWriter) {
                ipw = (IndentPrintWriter)writer;
            } else {
                writer = ipw = new IndentPrintWriter(writer);
            }
        } else {
            ipw=null;
        }
        boolean closeTag = false;
        Level level = new Level(null);
        while ((p = prs.parse(in,p.reuse())).valid()) {
            if (!p.hasName() && level.multi!=null) {
                p.name=level.multi;
            }
            if (closeTag && p.event!=Parse.ATTRIB) {
                writer.append('>');
                if (pretty)writer.append('\n');
                closeTag = false;
            }
            switch(p.event) {
                case Parse.START_DOC:
                    if (!(options.length>1&&options[1])) // if not a fragment, print XML Info data
                        if (pretty)ipw.println(XML_INFO);
                        else writer.append(XML_INFO);
                    break;
                case Parse.END_DOC:
                    break;
                case Parse.START_OBJ:
                    stack.push(level);
                    level = new Level(level);
                    if (p.hasName()) {
                        closeTag = tag(writer,level.sbw,pretty,pretty,p.name,null);
                    } else if (root!=null && stack.size()==1) { // first Object
                        closeTag = tag(writer,level.sbw,pretty,pretty,root,null);
                        // Write Root Props
                        for (Prop prop : props) {
                            attrib(writer,pretty,prop.tag, prop.value,level);
                        }
                    }
                    if (pretty)ipw.inc();
                    break;
                case Parse.END_OBJ:
                    if (p.hasData())
                        closeTag = tag(writer,writer,pretty,false,p.name, XmlEscape.convert(p.sb));
                    if (pretty)ipw.dec();
                    writer.append(Encode.forJava(level.sbw.getBuffer().toString()));
                    level = stack.pop();
                    break;
                case Parse.START_ARRAY:
                    level.multi = p.name;
                    break;
                case Parse.END_ARRAY:
                    if (p.hasData())
                        closeTag = tag(writer,writer,pretty,false, p.name, XmlEscape.convert(p.sb));
                    level.multi=null;
                    break;
                case Parse.ATTRIB:
                    if (p.hasData())
                        attrib(writer,pretty,p.name, XmlEscape.convert(p.sb), level);
                    break;
                case Parse.NEXT:
                    if (p.hasData())
                        closeTag = tag(writer,writer,pretty, false,p.name, XmlEscape.convert(p.sb));
                    break;
            }
        }
        writer.append(Encode.forJava(level.sbw.getBuffer().toString()));
        writer.flush();
    }

    private class Level {
        public final StringBuilderWriter sbw;
        public String multi;
        private Level prev;
        private Map<String,String> nses;

        public Level(Level level) {
            sbw = new StringBuilderWriter();
            multi = null;
            prev = level;
        }

        public boolean hasPrinted(String ns, String value, boolean create) {
            boolean rv = false;
            if (nses==null) {
                if (prev!=null)rv = prev.hasPrinted(ns, value, false);
            } else {
                String v = nses.get(ns);
                return value.equals(v); // note: accomodates not finding NS as well
            }

            if (create && !rv) {
                if (nses == null) nses = new HashMap<>();
                nses.put(ns, value);
            }
            return rv;
        }



    }

    private boolean tag(Writer fore, Writer aft, boolean pretty, boolean returns, String tag, String data) throws IOException {
        fore.append(Encode.forJava("<"));
        fore.append(tag);
        if (data!=null) {
            fore.append(Encode.forJava(">")); // if no data, it may need some attributes...
            fore.append(data);
            if (returns)fore.append('\n');
        }
        aft.append(Encode.forJava("</"));
        aft.append(tag);
        aft.append(Encode.forJava(">"));
        if (pretty)aft.append('\n');
        return data==null;
    }

    private void attrib(Writer fore, boolean pretty, String tag, String value, Level level) throws IOException {
        String realTag = tag.startsWith("__")?tag.substring(2):tag; // remove __
        if (realTag.equals(Parsed.EXTENSION_TAG)) { // Convert Derived name into XML defined Inheritance
            fore.append(Encode.forJava(" xsi:type=\" "));
            fore.append(value);
            fore.append('"');
            if (!level.hasPrinted(XMLNS_XSI, XML_SCHEMA_INSTANCE,true)) {
                fore.append(' ');
                fore.append(XMLNS_XSI);
                fore.append("=\"");
                fore.append(XML_SCHEMA_INSTANCE);
                fore.append("\"");
            }
        } else {
            if (realTag.startsWith("xmlns:") ) {
                if (level.hasPrinted(realTag, value, true)) {
                    return;
                }
            }
            fore.append(' ');
            fore.append(realTag);
            fore.append(" =\" ");
            fore.append(value);
            fore.append('"');
        }
    }

    @Override
    public String logName() {
        return "Rosetta XML";
    }


}
