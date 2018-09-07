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
    public Home(final AAF_GUI gui) throws APIException, IOException {
        super(gui.env,"Home",HREF, NO_FIELDS, new NamedCode(false,"content") {
            @Override
            public void code(final Cache<HTMLGen> cache, final HTMLGen xgen) throws APIException, IOException {
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
                final Mark pages = xgen.divID("Pages");
                xgen.leaf(H3).text("Choose from the following:").end()
                    .leaf(A,"href=myperms").text("My Permissions").end()
                    .leaf(A,"href=myroles").text("My Roles").end()
                //    TODO: uncomment when on cassandra 2.1.2 for MyNamespace GUI page
                    .leaf(A,"href=ns").text("My Namespaces").end()
                    .leaf(A,"href=approve").text("My Approvals").end()
                    .leaf(A, "href=myrequests").text("My Pending Requests").end()
                    // Enable later
//                    .leaf(A, "href=onboard").text("Onboarding").end()
                // Password Change.  If logged in as CSP/GSO, go to their page
                    .leaf(A,"href=passwd").text("Password Management").end()
                    .leaf(A,"href=cui").text("Command Prompt").end()
                    .leaf(A,"href=api").text("AAF API").end()
                    ;
                
                xgen.end(pages);
            }
        });
    }

}
