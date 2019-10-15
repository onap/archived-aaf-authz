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
import java.net.ConnectException;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.BreadCrumbs;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.gui.SlotCode;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.html.HTMLGen;

import aaf.v2_0.CredRequest;

public class PassDeleteAction extends Page {
    public static final String NAME = "PassDeleteAction";
    public static final String HREF = "/gui/passdelete";
    private static enum Params{id,date,ns,type};

    public PassDeleteAction(final AAF_GUI gui, final Page ... breadcrumbs) throws APIException, IOException {
        super(gui.env,NAME,HREF,Params.values(),
            new BreadCrumbs(breadcrumbs),
            new SlotCode<AuthzTrans>(true,gui.env,NAME,Params.values()) {
                @Override
                public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                    cache.dynamic(hgen, new DynamicCode<HTMLGen,AAF_GUI, AuthzTrans>() {
                        @Override
                        public void code(final AAF_GUI gui, final AuthzTrans trans,final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                            final CredRequest cr = new CredRequest();
                            cr.setId(get(trans,Params.id, ""));
                            cr.setType(Integer.parseInt(get(trans,Params.type, "0")));
                            cr.setEntry(get(trans,Params.date,"1960-01-01"));
                            try {
                                String err = gui.clientAsUser(trans.getUserPrincipal(), new Retryable<String>() {
                                    @Override
                                    public String code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                                        Future<CredRequest> fcr = client.delete("/authn/cred", gui.getDF(CredRequest.class),cr);
                                        if (!fcr.get(AAFcli.timeout())) {
                                            return gui.aafCon.readableErrMsg(fcr);
                                        }
                                        return null;
                                    }
                                });
                                if (err==null) {
                                    hgen.p("Password " + cr.getId() + ", " + cr.getEntry() + " is Deleted");
                                } else {
                                    hgen.p(err);
                                }
                            } catch (LocatorException | CadiException e) {
                                throw new APIException(e);
                            }
                        }
                    });
                }
            }
        );
    }
}
