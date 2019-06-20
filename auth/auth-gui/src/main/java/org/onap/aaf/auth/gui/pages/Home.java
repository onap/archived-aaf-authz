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

import static org.onap.aaf.misc.xgen.html.HTMLGen.A;
import static org.onap.aaf.misc.xgen.html.HTMLGen.H3;

import java.io.IOException;

import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.NamedCode;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.html.HTMLGen;


public class Home extends Page {
    public static final String HREF = "/gui/home";
    /*
     *      Relative path, Menu Name, Full Path
     */
    public static final String[][] MENU_ITEMS = new String[][] {
    		{"myperms","My Permissions","/gui/myperms"},
    		{"myroles","My Roles","/gui/myroles"},
    		{"ns","My Namespaces","/gui/ns"},
    		{"approve","My Approvals","/gui/approve"},
    		{"myrequests","My Pending Requests","/gui/myrequests"},
    	            // Enable later
   		//  {"onboard","Onboarding"},
    		{"passwd","Password Management","/gui/passwd"},
    		{"cui","Command Prompt","/gui/cui"},
    		{"api","AAF API","/gui/api"},
    		{"clear","Clear Preferences","/gui/clear"}
    };
    
	public Home(final AAF_GUI gui) throws APIException, IOException {
        super(gui.env,"Home",HREF, NO_FIELDS, new NamedCode(false,"content") {
            @Override
            public void code(final Cache<HTMLGen> cache, final HTMLGen htmlGen) throws APIException, IOException {
//                // TEMP
//                JSGen jsg = xgen.js();
//                jsg.function("httpPost","sURL","sParam")
//                    .text("var oURL = new java.net.URL(sURL)")
//                    .text("var oConn = oURL.openConnection();")
//                    .text("oConn.setDoInput(true);")
//                    .text("oConn.setDoOutpu(true);")
//                    .text("oConn.setUseCaches(false);")
//                    .text("oConn.setRequestProperty(\"Content-Type\",\"application/x-www-form-urlencoded\");")
//                    .text(text)
//                jsg.done();
                // TEMP
                final Mark pages = htmlGen.divID("Pages");
                htmlGen.leaf(H3).text("Choose from the following:").end();
                for(String[] mi : MENU_ITEMS) {
                	htmlGen.leaf(A,"href="+mi[0]).text(mi[1]).end();
                }
                htmlGen.end(pages);
            }
        });
    }
    
}
