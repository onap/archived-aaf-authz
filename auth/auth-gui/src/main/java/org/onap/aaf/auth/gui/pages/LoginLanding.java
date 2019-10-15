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
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.NamedCode;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.html.HTMLGen;

public class LoginLanding extends Page {
    public static final String HREF = "/login";
    static final String NAME = "Login";
    static final String fields[] = {"id","password","environment"};
    static final String envs[] = {"DEV","TEST","PROD"};

    public LoginLanding(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env, NAME,HREF, fields, new NamedCode(true, "content") {
            @Override
            public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                hgen.leaf("p").text("No login credentials are found in your current session. " +
                         "Choose your preferred login option to continue.").end();
            
                Mark loginPaths = hgen.divID("Pages");
            
                cache.dynamic(hgen, new DynamicCode<HTMLGen, AAF_GUI, AuthzTrans>() {
                    @Override
                    public void code(AAF_GUI authGUI, AuthzTrans trans, Cache<HTMLGen> cache, HTMLGen xgen) throws APIException, IOException {
                        HttpServletRequest req = trans.get(gui.slot_httpServletRequest, null);
                        if (req!=null) {
                            String query = req.getQueryString();
                            if (query!=null) {
                                for (String qs : query.split("&")) {
                                    int equals = qs.indexOf('=');
                                    xgen.leaf(HTMLGen.A, "href="+URLDecoder.decode(qs.substring(equals+1),Config.UTF_8)).text(qs.substring(0,equals).replace('_', ' ')).end();
                                }
                            }
                        }
                        xgen.leaf(HTMLGen.A, "href=gui/home?Authentication=BasicAuth").text("AAF Basic Auth").end();
                    }
                });
//                hgen.leaf("a", "href=#","onclick=divVisibility('cso');").text("Global Login").end()
//                    .incr("p", "id=cso","style=display:none").text("this will redirect to global login").end()
//                    .leaf("a", "href=#","onclick=divVisibility('tguard');").text("tGuard").end()
//                    .incr("p", "id=tguard","style=display:none").text("this will redirect to tGuard login").end()
//                hgen.leaf("a", "href=#","onclick=divVisibility('basicauth');").text("AAF Basic Auth").end();
                hgen.end(loginPaths);
            
//                    hgen.incr("form","method=post","style=display:none","id=basicauth","gui/home?Authentication=BasicAuth");
//                    Mark table = new Mark(TABLE);
//                    hgen.incr(table);
//                    cache.dynamic(hgen, new DynamicCode<HTMLGen, AuthGUI, AuthzTrans>() {
//                        @Override
//                        public void code(final AuthGUI gui, final AuthzTrans trans,    final Cache<HTMLGen> cache, final HTMLGen hgen)
//                                throws APIException, IOException {
//                            hgen
//                            .input(fields[0],"Username",true)
//                            .input(fields[1],"Password",true, "type=password");
//                        Mark selectRow = new Mark();
//                        hgen
//                        .incr(selectRow, "tr")
//                        .incr("td")
//                        .incr("label", "for=envs", "required").text("Environment").end()
//                        .end()
//                        .incr("td")
//                        .incr("select", "name=envs", "id=envs", "required")
//                        .incr("option", "value=").text("Select Environment").end();
//                        for (String env : envs) {
//                            hgen.incr("option", "value="+env).text(env).end();
//                        }
//                        hgen        
//                        .end(selectRow) 
                        
//                        hgen.end();
//                        }
//                    });
//                    hgen.end();
//                    hgen.tagOnly("input", "type=submit", "value=Submit")
//                        .tagOnly("input", "type=reset", "value=Reset")
//                    .end();
        

            }
        });
    }
}
