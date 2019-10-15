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
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.html.HTMLGen;

public class LoginLandingAction extends Page {
    public LoginLandingAction(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env,"Login",LoginLanding.HREF, LoginLanding.fields,
            new BreadCrumbs(breadcrumbs),
            new NamedCode(true,"content") {
                final Slot sID = gui.env.slot(LoginLanding.NAME+'.'+LoginLanding.fields[0]);
//                final Slot sPassword = gui.env.slot(LoginLanding.NAME+'.'+LoginLanding.fields[1]);

                @Override
                public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                    cache.dynamic(hgen, new DynamicCode<HTMLGen,AAF_GUI, AuthzTrans>() {
                        @Override
                        public void code(final AAF_GUI gui, final AuthzTrans trans,final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                            String username = trans.get(sID,null);
//                            String password = trans.get(sPassword,null);

                            hgen.p("User: "+username);
                            hgen.p("Pass: ********");

                            // TODO: clarification from JG
                            // put in request header?
                            // then pass through authn/basicAuth call?

                        }
                    });
                }
        });
    }
}
