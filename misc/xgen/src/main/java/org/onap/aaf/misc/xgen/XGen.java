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

import java.io.PrintWriter;
import java.io.Writer;
import java.util.Stack;

import org.onap.aaf.misc.env.util.IndentPrintWriter;
import org.onap.aaf.misc.env.util.StringBuilderWriter;

public class XGen<RT extends XGen<RT>> {

    private static final int COMMENT_COLUMN = 40;
    private StringBuilder backSB = new StringBuilder();
    private Stack<Back> backStack = new Stack<Back>();
    
    protected XGen(Writer w) {
        forward = new IndentPrintWriter(w);
    }

    public int pushBack(Back b) {
        int rv = backStack.size();
        backStack.push(b);
        return rv;
    }

    public boolean pretty = false;
    protected IndentPrintWriter forward;

    public IndentPrintWriter getWriter() {
        return forward;
    }

    protected PrintWriter back = new PrintWriter(
                new StringBuilderWriter(backSB));

    @SuppressWarnings("unchecked")
    public RT pretty() {
        pretty = true;
        return (RT) this;
    }

    protected void prettyln(PrintWriter pw) {
        if(pretty)pw.println();
    }

    public RT leaf(Mark mark, String tag, String ... args) {
        mark.spot = backStack.size();
        return leaf(tag, args);
    }

    @SuppressWarnings("unchecked")
    public RT leaf(String tag, String ... attrs) {
        forward.append('<');
        forward.append(tag);
        addAttrs(attrs);
        forward.append('>');
        back.append("</");
        back.append(tag);
        back.append('>');
        backStack.push(new Back(backSB.toString(), false, true));
        backSB.setLength(0);
        return (RT)this;
    }

    public RT incr(String tag, String ... args) {
        return incr(null, tag, false, args);
    }

    public RT incr(String tag, boolean oneLine, String ... args) {
        return incr(null, tag, oneLine, args);
    }

    public RT incr(Mark mark) {
        return incr(mark,mark.comment, false, new String[0]);
    }

    public RT incr(Mark mark, String tag, String ... attrs) {
        return incr(mark, tag, false, attrs);
    }

    @SuppressWarnings("unchecked")
    public RT incr(Mark mark, String tag, boolean oneLine, String ... attrs) {
        forward.append('<');
        forward.append(tag);
        addAttrs(attrs);
        forward.append('>');
        
        back.append("</");
        back.append(tag);
        back.append('>');
    
        if(pretty) {
            if(mark!=null && mark.comment!=null) {
                int fi = forward.getIndent()*IndentPrintWriter.INDENT_MULTIPLIER;
                for(int i = fi+backSB.length();i<=COMMENT_COLUMN;++i) {
                    back.append(' ');
                }
                back.append("<!-- end ");
                back.append(mark.comment);
                back.append(" -->");
                
                forward.toCol(COMMENT_COLUMN);
                forward.append("<!-- begin ");
                forward.append(mark.comment);
                forward.append(" -->");
            }
            forward.inc();
            if(!oneLine) {
                forward.println();
            }
            back.println();
        }
        if(mark!=null)mark.spot = backStack.size();
        backStack.push(new Back(backSB.toString(),true, false));
        backSB.setLength(0);
        return (RT)this;
    }

    @SuppressWarnings("unchecked")
    public RT tagOnly(String tag, String ... attrs) {
        forward.append('<');
        forward.append(tag);
        addAttrs(attrs);
        forward.append(" />");
        if(pretty) {
            forward.println();
        }
        return (RT)this;
    }

    @SuppressWarnings("unchecked")
    public RT text(String txt) {
        forward.append(txt);
        return (RT)this;
    }
    
    @SuppressWarnings("unchecked")
    public RT xml(String txt) {
        for(int i=0; i<txt.length();++i) {
            char c = txt.charAt(i);
            switch(c) {
                case '<':
                    forward.append("&lt;");
                    break;
                case '>':
                    forward.append("&gt;");
                    break;
                case '&':
                    forward.append("&amp;");
                    break;
                default:
                    forward.append(c);
            }
        }
        return (RT)this;
    }


    @SuppressWarnings("unchecked")
    public RT textCR(int tabs, String txt) {
        for(int i=0;i<tabs;++i) {
            forward.append("  ");
        }
        forward.append(txt);
        if(pretty)forward.println();
        return (RT)this;
    }

    @SuppressWarnings("unchecked")
    public RT value() {
        Mark mark = new Mark();
        mark.spot = backStack.size()-1;
        end(mark);
        return (RT)this;
    }

    @SuppressWarnings("unchecked")
    public RT value(String txt) {
        forward.append(txt);
        Mark mark = new Mark();
        mark.spot = backStack.size()-1;
        end(mark);
        return (RT)this;
    }

    @SuppressWarnings("unchecked")
    public RT value(String txt, int levels) {
        forward.append(txt);
        Mark mark = new Mark();
        mark.spot = backStack.size()-levels;
        end(mark);
        return (RT)this;
    }

    @SuppressWarnings("unchecked")
    public RT end(Mark mark) {
        int size = backStack.size();
        Back c;
        boolean println = false;
        for(int i=mark==null?0:mark.spot;i<size;++i) {
            c = backStack.pop();
            if(c.dec)forward.dec();
            forward.append(c.str);
            println = c.cr;
        }
        if(pretty && println) {
            forward.println();
        }
        return (RT)this;
    }

    public RT end() {
        Mark mark = new Mark();
        mark.spot=backStack.size()-1;
        if(mark.spot<0)mark.spot=0;
        return end(mark);
    }

    public RT end(int i) {
        Mark mark = new Mark();
        mark.spot=backStack.size()-i;
        if(mark.spot<0)mark.spot=0;
        return end(mark);
    }

    public void endAll() {
        end(new Mark());
        forward.flush();
    }

    protected void addAttrs(String[] attrs) {
        if(attrs!=null) {
            for(String attr : attrs) {
                if(attr!=null && attr.length()>0) {
                    forward.append(' ');
                    String[] split = attr.split("=",2);
                    switch(split.length) {
                        case 0:
                            break;
                        case 1:
                            forward.append(split[0]);
//                            forward.append("=\"\"");
                            break;
                        default:
                            forward.append(split[0]);
                            forward.append("=\"");
                            forward.append(split[1]);
                            forward.append('"');
                            break;
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public RT comment(String string) {
        if(pretty) {
            forward.print("<!--  ");
            forward.print(string);
            forward.println("  -->");
        }
        return (RT)this;
    }

    public void setIndent(int indent) {
        forward.setIndent(indent);
        forward.toIndent();
    }

    public int getIndent() {
        return forward.getIndent();
    }

}
