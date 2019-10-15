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
import java.util.Stack;

import org.onap.aaf.misc.env.util.IndentPrintWriter;

public class OutJson extends Out {

    @Override
    public<IN,S> void extract(IN in, Writer writer, Parse<IN, S> prs, boolean ... options) throws IOException, ParseException {
        Parsed<S> p = prs.newParsed();
        IndentPrintWriter ipw;
        if (options.length>0 && options[0]) { // is Pretty
            ipw = writer instanceof IndentPrintWriter?(IndentPrintWriter)writer:new IndentPrintWriter(writer);
            writer = ipw;
        } else {
            ipw = null;
        }

        // If it's a fragment, print first Object Name.  If root Object, skip first name
        Stack<LevelStack> jsonLevel = new Stack<LevelStack>();
        jsonLevel.push(new LevelStack(options.length>1 && options[1]));
        boolean print = true, hadData=false;
        char afterName=0, beforeName=0, maybe = 0, prev=0;

        int count = 0;
        while ((p = prs.parse(in,p.reuse())).valid()) {
            ++count;
            switch(p.event) {
                case 1:
                    continue;
                case 2:
                    if (count==2) { // it's empty, write open/close on it's own
                        writer.append('{');
                        writer.append('}');
                    }
                    writer.flush();
                    return;
                case '{':
                    afterName = '{';
                    if (jsonLevel.peek().printObjectName) {
                        print = true;
                    } else { // don't print names on first
                        print=false;
                    }
                    maybe=jsonLevel.peek().listItem();
                    jsonLevel.push(new LevelStack(true));
                    break;
                case '}':
                    if (p.hasData()) { // if we have data, we print that, so may need to prepend a comma.
                        maybe = jsonLevel.peek().listItem();
                    } else { // No data means just print,
                        p.name = ""; // XML tags come through with names, but no data
                    }
                    print = true;
                    jsonLevel.pop();
                    afterName = p.event;
                    break;
                case '[':
                    afterName = p.event;
                    if ((prev==',' && !hadData) || prev==']')maybe=',';
                    else maybe = jsonLevel.peek().listItem();

                    jsonLevel.push(new LevelStack(false));
                    print=true;
                    break;
                case ']':
                    afterName = p.event;
                    if (p.hasData()) {
                        if (prev==',' && !hadData)maybe=',';
                        else maybe = jsonLevel.peek().listItem();
                    } else {
                        p.name = ""; // XML tags come through with names, but no data
                    }
                    jsonLevel.pop();

                    print = true;
                    break;
                case   3:
                case ',':
                    if (!p.hasData()) {
                        p.isString=false;
                        print=false;
                    } else {
                        maybe=jsonLevel.peek().listItem();
                        print = true;
                    }
                    break;
                default:
                    print = true;
            }

            if (maybe!=0) {
                if (ipw==null)writer.append(maybe);
                else ipw.println(maybe);
                maybe = 0;
            }

            // commented out unreachable code (as it is, beforeName is never
            // assigned any value except 0
            //if (beforeName!=0) {
            //    if (ipw==null)writer.append(beforeName);
            //    else ipw.println(beforeName);
            //    beforeName = 0;
            //}
            if (print) {
                if (p.hasName()) {
                    writer.append('"');
                    if (p.event==3)writer.append("__");
                    writer.append(p.name);
                    writer.append("\":");
                }
                if (p.hasData()) {
                    if (p.isString) {
                        writer.append('"');
                        escapedWrite(writer, p.sb);
                        writer.append('"');
                    } else if (p.sb.length()>0) {
                        writer.append(p.sb);
                    }
                }
            }
            if (afterName!=0) {
                if (ipw==null)writer.append(afterName);
                else {
                    switch(afterName) {
                        case '{':
                            ipw.println(afterName);
                            ipw.inc();
                            break;
                        case '}':
                            ipw.dec();
                            ipw.println();
                            ipw.print(afterName);
                            break;
                        case ']':
                            if (prev=='}' || prev==',')ipw.println();
                            ipw.dec();
                            ipw.print(afterName);
                            break;

                        case ',':
                            ipw.println(afterName);
                            break;
                        default:
                            ipw.print(afterName);
                    }
                }
                afterName = 0;
            }

            if (ipw!=null) {
                switch(p.event) {
                    case '[':
                        ipw.inc();
                        ipw.println();
                        break;
                }
            }
            prev = p.event;
            hadData = p.hasData();

        }
        writer.flush();
    }

    private void escapedWrite(Writer writer, StringBuilder sb) throws IOException {
        char c;
        for (int i=0;i<sb.length();++i) {
            switch(c=sb.charAt(i)) {
                case '\\':
                    writer.append(c);
                    if (i<sb.length()) {
                        c=sb.charAt(++i);
                        writer.append(c);
                    }
                    break;
                case '"':
                    writer.append('\\');
                    // Passthrough on purpose
                default:
                    writer.append(c);
            }
        }


    }

    @Override
    public String logName() {
        return "Rosetta JSON";
    }

    private static class LevelStack {
        public boolean printObjectName=false;
        private boolean first_n_List=true;

        public LevelStack(boolean printObjectName) {
            this.printObjectName = printObjectName;
        }

        public char listItem() {
            if (first_n_List) {
                first_n_List=false;
                return 0;
            } else {
                return ',';
            }
        }
    }
}
