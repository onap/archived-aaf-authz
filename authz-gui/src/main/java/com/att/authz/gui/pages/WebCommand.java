/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import java.io.IOException;

import com.att.authz.env.AuthzTrans;
import com.att.authz.gui.AuthGUI;
import com.att.authz.gui.BreadCrumbs;
import com.att.authz.gui.NamedCode;
import com.att.authz.gui.Page;
import com.att.inno.env.APIException;
import com.att.xgen.Cache;
import com.att.xgen.DynamicCode;
import com.att.xgen.Mark;
import com.att.xgen.html.HTMLGen;

public class WebCommand extends Page {
	public static final String HREF = "/gui/cui";
	
	public WebCommand(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env, "Web Command Client",HREF, NO_FIELDS,
				new BreadCrumbs(breadcrumbs),
				new NamedCode(true, "content") {
			@Override
			public void code(Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
				hgen.leaf("p","id=help_msg")
					.text("Questions about this page? ")
					.leaf("a", "href=http://wiki.web.att.com/display/aaf/Web+CUI+Usage", "target=_blank")
					.text("Click here")
					.end()
					.text(". Type 'help' below for a list of AAF commands")
					.end()
					
					.divID("console_and_options");
				hgen.divID("console_area");				
				hgen.end(); //console_area
				
				hgen.divID("options_link", "class=closed");
				hgen.img("src=../../theme/options_down.png", "onclick=handleDivHiding('options',this);", 
						"id=options_img", "alt=Options", "title=Options")					
					.end(); //options_link
				
				hgen.divID("options");
				cache.dynamic(hgen, new DynamicCode<HTMLGen,AuthGUI,AuthzTrans>() {
					@Override
					public void code(AuthGUI state, AuthzTrans trans, Cache<HTMLGen> cache, HTMLGen xgen)
							throws APIException, IOException {
						switch(browser(trans,trans.env().slot(getBrowserType()))) {
							case ie:
							case ieOld:
								// IE doesn't support file save
								break;
							default:
								xgen.img("src=../../theme/AAFdownload.png", "onclick=saveToFile();",
										"alt=Save log to file", "title=Save log to file");
						}
//						xgen.img("src=../../theme/AAFemail.png", "onclick=emailLog();",
//								"alt=Email log to me", "title=Email log to me");
						xgen.img("src=../../theme/AAF_font_size.png", "onclick=handleDivHiding('text_slider',this);", 
								"id=fontsize_img", "alt=Change text size", "title=Change text size");
						xgen.img("src=../../theme/AAF_details.png", "onclick=selectOption(this,0);", 
								"id=details_img", "alt=Turn on/off details mode", "title=Turn on/off details mode");
						xgen.img("src=../../theme/AAF_maximize.png", "onclick=maximizeConsole(this);",
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
