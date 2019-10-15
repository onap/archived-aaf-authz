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
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.html.HTMLGen;

/**
 * Detail Page for Permissions
 *
 * @author Jonathan
 *
 */
public class ApiExample extends Page {
    public static final String HREF = "/gui/example/:tc";
    public static final String NAME = "APIExample";

    public ApiExample(final AAF_GUI gui, Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env, NAME, HREF, 2/*backdots*/, new String[] {"API Code Example"},
                new BreadCrumbs(breadcrumbs),
                new Model(NAME)
                );
    }

    private static class Model extends NamedCode {
        private static final String WITH_OPTIONAL_PARAMETERS = "\n\n////////////\n  Data with Optional Parameters \n////////////\n\n";

        public Model(String name) {
            super(false,name);
        }

        @Override
        public void code(Cache<HTMLGen> cache, HTMLGen xgen) throws APIException, IOException {
            Mark inner = xgen.divID("inner");
            xgen.divID("example","class=std");
            cache.dynamic(xgen, new DynamicCode<HTMLGen,AAF_GUI,AuthzTrans>() {
                @Override
                public void code(final AAF_GUI gui, final AuthzTrans trans, Cache<HTMLGen> cache, HTMLGen xgen) throws APIException, IOException {
                    TimeTaken tt = trans.start("Code Example",Env.REMOTE);
                    try {
                        final String typecode;
                        int prefix = trans.path().lastIndexOf('/')+1;
                        String encoded = trans.path().substring(prefix);
                        typecode = Symm.base64noSplit.decode(encoded);
                        Future<String> fp = gui.client().read("/api/example/" + encoded,
                                "application/Void+json"
                                );
                        Future<String> fs2;
                        if (typecode.contains("Request+")) {
                            fs2 = gui.client().read("/api/example/" + encoded+"?optional=true",
                                    "application/Void+json"
                                    );
                        } else {
                            fs2=null;
                        }


                        if (fp.get(5000)) {
                                xgen.incr(HTMLGen.H1).text("Sample Code").end()
                                .incr(HTMLGen.H5).text(typecode).end();
                                xgen.incr("pre");
                                if (typecode.contains("+xml")) {
                                    xgen.xml(fp.body());
                                    if (fs2!=null && fs2.get(5000)) {
                                        xgen.text(WITH_OPTIONAL_PARAMETERS);
                                        xgen.xml(fs2.body());
                                    }
                                } else {
                                    xgen.text(fp.body());
                                    if (fs2!=null && fs2.get(5000)) {
                                        xgen.text(WITH_OPTIONAL_PARAMETERS);
                                        xgen.text(fs2.body());
                                    }
                                }
                                xgen.end();
                        } else {
                            xgen.incr(HTMLGen.H3)
                                .textCR(2,"Error from AAF Service")
                                .end();
                            gui.writeError(trans, fp, xgen, 0);
                        }

                    } catch (APIException e) {
                        throw e;
                    } catch (IOException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new APIException(e);
                    }finally {
                        tt.done();
                    }
                }

            });
            xgen.end(inner);
        }
    }

}
        