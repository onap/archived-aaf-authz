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

package org.onap.aaf.auth.gui.pages;

import java.io.IOException;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.NamedCode;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.rserv.CachingFileAccess;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.StaticSlot;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.html.HTMLGen;

public class WebCommand extends Page {
    public static final String HREF = "/gui/cui";
    
    public WebCommand(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env, "Web Command Client",HREF, NO_FIELDS,
                new BreadCrumbs(breadcrumbs),
                new NamedCode(true, "content") {
            StaticSlot sThemeWebPath = gui.env.staticSlot(CachingFileAccess.CFA_WEB_PATH);
            StaticSlot sTheme = gui.env.staticSlot(AAF_GUI.AAF_GUI_THEME);
            @Override
            public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                hgen.leaf("p","id=help_msg")
                    .text("Questions about this page? ")
                    .leaf("a", "href="+gui.env.getProperty(AAF_URL_CUIGUI,""), "target=_blank")
                    .text("Click here")
                    .end()
                    .text(". Type 'help' below for a list of AAF commands")
                    .end()
                    
                    .divID("console_and_options");
                hgen.divID("console_area");                
                hgen.end(); //console_area
                
                hgen.divID("options_link", "class=closed");
                cache.dynamic(hgen, new DynamicCode<HTMLGen,AAF_GUI,AuthzTrans>() {
                    @Override
                    public void code(AAF_GUI state, AuthzTrans trans, Cache<HTMLGen> cache, HTMLGen xgen)
                            throws APIException, IOException {
                        String image_root = "src=../../"+state.env.get(sThemeWebPath).toString() + '/' + state.env.get(sTheme) + "/images/icons";
                        hgen.img(image_root + "/options_down.png", "onclick=handleDivHiding('options',this);", 
                                "id=options_img", "alt=Options", "title=Options")                    
                            .end(); //options_link
                        
                        hgen.divID("options");

                        switch(browser(trans,trans.env().slot(getBrowserType()))) {
                            case ie:
                            case ieOld:
                                // IE doesn't support file save
                                break;
                            default:
                                xgen.img(image_root+"/AAF_download.png", "onclick=saveToFile();",
                                        "alt=Save log to file", "title=Save log to file");
                        }
//                        xgen.img("src=../../"+gui.theme+"/AAF_email.png", "onclick=emailLog();",
//                                "alt=Email log to me", "title=Email log to me");
                        xgen.img(image_root+"/AAF_font_size.png", "onclick=handleDivHiding('text_slider',this);", 
                                "id=fontsize_img", "alt=Change text size", "title=Change text size");
                        xgen.img(image_root+"/AAF_details.png", "onclick=selectOption(this,0);", 
                                "id=details_img", "alt=Turn on/off details mode", "title=Turn on/off details mode");
                        xgen.img(image_root+"/AAF_maximize.png", "onclick=maximizeConsole(this);",
                                "id=maximize_img", "alt=Maximize Console Window", "title=Maximize Console Window");
                    }    
                });
                hgen.divID("text_slider");
                hgen.tagOnly("input", "type=button", "class=change_font", "onclick=buttonChangeFontSize('dec')", "value=-")
                    .tagOnly("input", "id=text_size_slider", "type=range", "min=75", "max=200", "value=100", 
                        "oninput=changeFontSize(this.value)", "onchange=changeFontSize(this.value)", "title=Change Text Size")
                    .tagOnly("input", "type=button", "class=change_font", "onclick=buttonChangeFontSize('inc')", "value=+")                
                    .end(); //text_slider

                hgen.end(); //options
                hgen.end(); //console_and_options
                
                hgen.divID("input_area");
                hgen.tagOnly("input", "type=text", "id=command_field", 
                        "autocomplete=off", "autocorrect=off", "autocapitalize=off", "spellcheck=false",
                        "onkeypress=keyPressed()", "placeholder=Type your AAFCLI commands here", "autofocus")
                    .tagOnly("input", "id=submit", "type=button", "value=Submit", 
                            "onclick=http('put','../../gui/cui',getCommand(),callCUI);")
                    .end();

                Mark callCUI = new Mark();
                hgen.js(callCUI);
                hgen.text("function callCUI(resp) {")
                    .text("moveCommandToDiv();")
                    .text("printResponse(resp);") 
                    .text("}");
                hgen.end(callCUI);    
            
            }
        });

    }

}
