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

package org.onap.aaf.cadi.aaf.test;

import java.net.HttpURLConnection;
import java.net.URI;

import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.v2_0.AAFLocator;
import org.onap.aaf.cadi.aaf.v2_0.AbsAAFLocator;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.impl.BasicTrans;

public class TestHClient {
    public static void main(String[] args) {
        try {
            PropAccess access = new PropAccess(args);
            String aaf_url = access.getProperty(Config.AAF_URL);
            if (aaf_url == null) {
                access.log(Level.ERROR, Config.AAF_URL," is required");
            } else {
                HMangr hman = null;
                try {
                    SecurityInfoC<HttpURLConnection> si = SecurityInfoC.instance(access, HttpURLConnection.class);
                    AbsAAFLocator<BasicTrans> loc = new AAFLocator(si,new URI(aaf_url));
                    for (Item item = loc.first(); item!=null; item=loc.next(item)) {
                        System.out.println(loc.get(item));
                    }

                    hman = new HMangr(access,loc);
                    final String path = String.format("/authz/perms/user/%s",
                            access.getProperty(Config.AAF_APPID,"xx9999@people.osaaf.org"));
                    hman.best(si.defSS, new Retryable<Void>() {
                        @Override
                        public Void code(Rcli<?> cli) throws APIException, CadiException {
                            Future<String> ft = cli.read(path,"application/json");
                            if (ft.get(10000)) {
                                System.out.println("Hurray,\n"+ft.body());
                            } else {
                                System.out.println("not quite: " + ft.code());
                            }
                            return null;
                        }});
                } finally {
                    if (hman!=null) {
                        hman.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
