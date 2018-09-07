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

public class HTML5Gen extends HTMLGen {
    public HTML5Gen(Writer w) {
        super(w);
    }

    @Override
    public HTMLGen html(String ... attrib) {
        //forward.println("<!DOCTYPE html>");
        incr("html",attrib);
        return this;
    }
    
    @Override
    public Mark head() {
        Mark head = new Mark("head");
        incr(head).directive("meta","charset=utf-8");
        return head;
    }

    @Override
    public Mark body(String ... attrs) {
        Mark body = new Mark("body");
        incr(body,"body",attrs);
        //chromeFrame();
        return body;
    }
    
    @Override
    public HTML5Gen charset(String charset) {
        forward.append("<meta charset=\"");
        forward.append(charset);
        forward.append("\">");
        prettyln(forward);
        return this;
    }

    @Override
    public Mark header(String ... attribs) {
        Mark mark = new Mark("header");
        incr(mark, mark.comment, attribs);
        return mark;
    }

    @Override
    public Mark footer(String ... attribs) {
        Mark mark = new Mark("footer");
        incr(mark, mark.comment, attribs);
        return mark;
    }

    @Override
    public Mark section(String ... attribs) {
        Mark mark = new Mark("section");
        incr(mark, mark.comment,attribs);
        return mark;
    }

    @Override
    public Mark article(String ... attribs) {
        Mark mark = new Mark("article");
        incr(mark, mark.comment,attribs);
        return mark;
    }

    @Override
    public Mark aside(String ... attribs) {
        Mark mark = new Mark("aside");
        incr(mark, mark.comment,attribs);
        return mark;
    }

    @Override
    public Mark nav(String ... attribs) {
        Mark mark = new Mark("nav");
        incr(mark, mark.comment,attribs);
        return mark;
    }
    

//    @Override
//    protected void importCSS(Imports imports) {
//        if (imports.css.size() == 1) {
//            cssInline(imports.css.get(0));
//        } else {
//            for (String str : imports.css) {
//                forward.print("<link rel=\"stylesheet\" href=\"");
//                forward.print(imports.themePath(null));
//                forward.print(str);
//                forward.println("\">");
//            }
//        }
//    }
//

    /*
    public void chromeFrame() {
        this.textCR(0,"<!--[if IE]>");
        Mark mark = new Mark();
        this.leaf(mark, "script","type=text/javascript","src=http://ajax.googleapis.com/ajax/libs/chrome-frame/1/CFInstall.min.js")
            .end(mark);
        this.incr(mark, "style")
            .textCR(0,".chromeFrameInstallDefaultStyle {")
            .textCR(1,"width: 100%; /* default is 800px * /")
            .textCR(1,"border: 5px solid blue;")
            .textCR(0,"}")
            .end(mark);

        this.incr(mark,"div","id=prompt"); // auto comment would break IE specific Script
        // "if IE without GCF, prompt goes here"
        this.text("Please load this plugin to run ClientSide Websockets")
            .end(mark);

        this.incr(mark, "script")
            .textCR(0, "// The conditional ensures that this code will only execute in IE,")
                .textCR(0, "// Therefore we can use the IE-specific attachEvent without worry")
                .textCR(0, "window.attachEvent('onload', function() {")
                .textCR(1,"CFInstall.check({")
                    .textCR(2,"mode: 'inline', // the default")
                    .textCR(2,"node: 'prompt'")
                .textCR(1, "});")
            .textCR(0, "});")
            .end(mark);
            
        this.textCR(0,"<![endif]-->");
    }
    */

}
