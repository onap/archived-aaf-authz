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

package org.onap.aaf.misc.xgen.html;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.onap.aaf.misc.env.util.IndentPrintWriter;
import org.onap.aaf.misc.xgen.Back;
import org.onap.aaf.misc.xgen.Mark;


public class JSGen {
    private HTMLGen htmlGen;
    private IndentPrintWriter ipw;
    private Mark mark;

    public JSGen(Mark mark, HTMLGen hg) {
        this.mark = mark==null?new Mark():mark;
        hg.incr(this.mark, "script", "language=javascript", "type=text/javascript");
        htmlGen = hg;
        ipw = hg.getWriter();
    }

    public JSGen inline(String filename, int tabstop) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        int indent = htmlGen.getIndent();
        try {
            boolean pretty = htmlGen.pretty;
            String line, el;
            int l, end;
            while((line=br.readLine())!=null) {
                if(pretty) {
                    String[] elements = line.split("\t");
                    
                    for(int i=0; i<elements.length;++i) {
                        el = elements[i];
                        l = el.length();
                        if(l==0) {// was a Tab
                            ipw.print("  ");
                        } else {
                            el = el.trim();
                            l = l-el.length();
                            end = l/tabstop;
                            for(int j=0;j<end;++j) {
                                ipw.print("  ");
                            }
                            end = l%tabstop;
                            for(int j=0;j<end;++j) {
                                ipw.print(' ');
                            }
                            if(i>0) ipw.print(' ');
                                ipw.print(el);
                            }
                    }
                    ipw.println();
                } else {
                    ipw.print(line.trim());
                }
            }
        } finally {
            htmlGen.setIndent(indent);
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this;
    }
    
    public JSGen pst(String ... lines) {
        return pst(null, lines);
    }
    
    public JSGen pst(Mark jm, String ... lines) {
        if(lines.length>0) ipw.append(lines[0]);
        ipw.append('(');
        for(int i=1;i<lines.length;++i) {
            ipw.print(lines[i]);
            ipw.print(", ");
        }
        Back back;

        if(htmlGen.pretty) {
            back = new Back(");\n",false,false);
        } else {
            back = new Back(");",false,false);
        }
        int spot = htmlGen.pushBack(back);
        if(jm!=null)jm.spot(spot);
        return this;
    }
    
    public JSGen li(String ... lines) {
        int current = ipw.getIndent();
        for(int i=0;i<lines.length;++i) {
            if(i==1)ipw.inc();
            if(i>0)ipw.println();
            ipw.print(lines[i]);
        }
        ipw.setIndent(current);
        ipw.append(';');
        if(htmlGen.pretty)ipw.println();
        return this;
    }
    
    public JSGen text(String text) {
        ipw.append(text);
        if(htmlGen.pretty)ipw.println();
        return this;
    }

    public JSGen function(String name, String ... params) {
        return function(null, name, params);
    }
    
    public JSGen jqfunc(Mark mark, String name, String ... params) {
        pst(mark,"$").function(name, params);
        return this;
    }
    
    public JSGen function(Mark jm, String name, String ... params) {
        ipw.print("function ");
        ipw.print(name);
        ipw.print('(');
        for(int i=0;i<params.length;++i) {
            if(i!=0)ipw.print(", ");
            ipw.print(params[i]);
        }
        ipw.print(") {");
        if(htmlGen.pretty) {
            ipw.println();
            ipw.inc();
        }
        int spot = htmlGen.pushBack(new Back("}",true,true));
        if(jm!=null)jm.spot(spot); 
        return this;
    }
    
    public JSGen cb(String ... lines) {
        return cb(null,lines);
    }

    public JSGen cb(Mark jm, String ... lines) {
        int current = ipw.getIndent();
        for(int i=0;i<lines.length;++i) {
            if(i==1)ipw.inc();
            if(i>0)ipw.println();
            ipw.print(lines[i]);
        }
        ipw.setIndent(current);
        ipw.print('{');
        if(htmlGen.pretty) {
            ipw.println();
            ipw.inc();
        }
        int spot = htmlGen.pushBack(new Back("}",true,true));
        if(jm!=null)jm.spot(spot); 
        return this;

    }

    
    public JSGen comment(String ... lines) {
        if(htmlGen.pretty) {
            for(int i=0;i<lines.length;++i) {
                ipw.print("// ");
                ipw.println(lines[i]);
            }
        }
        return this;
    }
    
    public JSGen end(Mark mark) {
        htmlGen.end(mark);
        return this;
    }
    
    public HTMLGen done() {
        return htmlGen.end(mark);
    }
    
}
