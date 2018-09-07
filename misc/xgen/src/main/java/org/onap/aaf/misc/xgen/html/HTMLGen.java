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

import java.io.Writer;

import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.XGen;

public abstract class HTMLGen extends XGen<HTMLGen> {
    public static final String A = "a";
    public static final String P = "p";
    public static final String LI = "li";
    public static final String OL = "ol";
    public static final String UL = "ul";
    
    
    public static final String TABLE = "table";
    public static final String THEAD = "thead";
    public static final String TBODY = "tbody";
    public static final String TR = "tr";
    public static final String TH = "th";
    public static final String TD = "td";
    
    public static final String TITLE = "title";
    public static final String H1 = "h1";
    public static final String H2 = "h2";
    public static final String H3 = "h3";
    public static final String H4 = "h4";
    public static final String H5 = "h5";
    
    
    
    // --------------------------- HTML Version Specific -----------------------
    public abstract HTMLGen html(String ... attributes);
    public abstract HTMLGen charset(String charset);
    public abstract Mark head();
    public abstract Mark body(String ... attribs);

    
    // HTML 5 has simplified sectioning
    public abstract Mark header(String ... attribs);
    public abstract Mark footer(String ... attribs);
    public abstract Mark section(String ... attribs);
    public abstract Mark article(String ... attribs);
    public abstract Mark aside(String ... attribs);
    public abstract Mark nav(String ... attribs);

    // --------------------------- HTML Version Specific -----------------------

    public HTMLGen imports(Imports imports) {
        //this.imports=imports;
        for (String str : imports.css) {
            forward.print("<link rel=\"stylesheet\" href=\"");
            forward.print(imports.themePath(null));
            forward.print(str);
            forward.println("\">");
        }

        for (String str : imports.js) {
            forward.print("<script type=\"text/javascript\" src=\"");
            forward.print(imports.themePath(null));
            forward.print(str);
            forward.println("\"></script>");
        }
        return this;
    }
    
    public HTMLGen jsVars(String ... attrs) {
        forward.println("<script type=text/javascript>");
        if (attrs!=null) {
            for (int i=0; i<attrs.length;++i) {
                forward.append(' ');
                String[] split = attrs[i].split("=",2);
                switch(split.length) {
                    case 2:
                        forward.print("  var ");
                        forward.append(split[0]);
                        forward.append("='");
                        forward.append(split[1]);
                        forward.println("';");
                        break;
                }
            }
        }
        forward.println("</script>");
        return this;
    }

    public HTMLGen(Writer w) {
        super(w);
    }

    /**
     * Use "directive" to handle non-ended HTML tags like <meta ... >  and <link ...>
     * @param tag
     * @param attrs
     * @return
     */
    public HTMLGen directive(String tag, String ... attrs) {
        forward.append('<');
        forward.append(tag);
        addAttrs(attrs);
        forward.append('>');
        if (pretty) {
            forward.println();
        }
        return this;
    }

    public Mark divID(String ... attrs) {
        Mark div;
        if (attrs.length>0) {
            div = new Mark(attrs[0]);
            attrs[0]="id="+attrs[0];
        } else {
            div = new Mark();
        }
        incr(div, "div", attrs);
        return div;
    }

    public HTMLGen img(String ... attrs) {
        return tagOnly("img", attrs);
    }
    
    /**
     * Input Cheesecake... creates a Label and Field in the form of Table Rows.
     * Make sure you create a table first, ie.  incr(HTMLGen.TABLE);
     * 
     * Setting Required to "true" will add required Attribute to both Label and Field.  In HTML5, "required" in the input will
     * validate there is data in the fields before submitting.  "required" does nothing for label, but allows for
     * easy CSS coding... "label[required] { ... }", so that colors can be changed
     * 
     * @param id
     * @param label
     * @param required
     * @param attrs
     * @return
     */
    public HTMLGen input(String id, String label, boolean required, String ... attrs) {
        Mark mtr = new Mark(TR);
        Mark mtd = new Mark(TD);
        incr(mtr);
        incr(mtd);
        incr("label",true, "for="+id,required?"required":null).text(label).end();
        end(mtd);
        String nattrs[] = new String[attrs.length+(required?3:2)];
        nattrs[0]="id="+id;
        nattrs[1]="name="+id;
        System.arraycopy(attrs, 0, nattrs, 2, attrs.length);
        if (required) {
            nattrs[nattrs.length-1]="required";
        }
        incr(mtd);
        tagOnly("input",nattrs);
        end(mtr);
        return this;
    }
    
    //  Common tags that do not have standard endings.  These are here to help people who don't know to pick directive  
    public HTMLGen br() {
        forward.append("<br>");
        if (pretty) {
            forward.println();
        }
        return this;
    }

    public HTMLGen p(String ... text) {
        forward.append("<p>");
        for (String s : text) {
            forward.append(s);
        }
        if (pretty) {
            forward.println();
        }
        return this;
    }

    public HTMLGen hr() {
        forward.append("<hr>");
        if (pretty) {
            forward.println();
        }
        return this;
    }

    public JSGen js(Mark mark) {
        return new JSGen(mark, this);
    }

    public JSGen js() {
        return js(null);
    }
//
//    protected void cssInline(String filename) {
//        File file = new File(imports.webDir,filename);
//        try {
//            String line;
//            BufferedReader br = new BufferedReader(new FileReader(file));
//            try {
//                forward.print("<style>");
//                prettyln(forward);
//                while ((line=br.readLine())!=null) {
//                    forward.print((pretty?line:line.trim()));
//                    prettyln(forward);
//                }            
//            }finally {
//                forward.print("</style>");
//                prettyln(forward);
//                br.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            // Can't read, suffice to import normally?
//            // for now, just skip
//        }
//    }
    
}
