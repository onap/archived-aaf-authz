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

package org.onap.aaf.auth.gui;

import static org.onap.aaf.misc.xgen.html.HTMLGen.A;
import static org.onap.aaf.misc.xgen.html.HTMLGen.LI;
import static org.onap.aaf.misc.xgen.html.HTMLGen.UL;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.TransStore;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.html.HTMLGen;

public class BreadCrumbs extends NamedCode {
    Page[] breadcrumbs;

    public BreadCrumbs(Page ... pages) {
        super(false,"breadcrumbs");
        breadcrumbs = pages;
    }

    @Override
    public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
        // BreadCrumbs
        Mark mark = new Mark();
        hgen.incr(mark, UL);
            cache.dynamic(hgen, new DynamicCode<HTMLGen, AAF_GUI, TransStore>() {
                @Override
                public void code(AAF_GUI gui, TransStore trans, final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                    HttpServletRequest req = trans.get(gui.slot_httpServletRequest, null);
                    StringBuilder key = new StringBuilder();
                    String value, hidden;
                    for (Page p : breadcrumbs) {
                        hidden="";
                        // Add keys for page from commandline, where possible.
                        if (p.fields().length>0) {
                            boolean first = true;
                            key.setLength(0);
                            for (String field : p.fields()) {
                                if ((value=req.getParameter(field))==null) {
                                    hidden="style=display:none;";
                                    break;
                                }
                                if (first) {
                                    first = false;
                                    key.append('?');
                                } else {
                                    key.append("&amp;");
                                }
                                key.append(field);
                                key.append('=');
                                key.append(value);
                            }
                            hgen.incr(LI,true,hidden);
                            hgen.leaf(A,"href="+p.url()+key.toString(),hidden).text(p.name()).end(2);
                        } else {
                            hgen.incr(LI,true);
                            hgen.leaf(A,"href="+p.url(),hidden).text(p.name()).end(2);
                        }
                    }
                }
            });
        hgen.end(mark);
    }
}
