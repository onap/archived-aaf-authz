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

package org.onap.aaf.auth.hello.test;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.onap.aaf.auth.common.Define;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.v2_0.AAFConHttp;
import org.onap.aaf.cadi.aaf.v2_0.AAFLocator;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.misc.env.APIException;

public class HelloTester {

    public static void main(String[] args) {
        // Do Once and ONLY once
        PropAccess access =  new PropAccess(args);
        try {
            Define.set(access);
            String uriPrefix = access.getProperty("locatorURI",null);
            if (uriPrefix==null) {
                System.out.println("You must add \"locatorURI=<uri>\" to the command line or VM_Args");
            } else {
                SecurityInfoC<HttpURLConnection> si = SecurityInfoC.instance(access, HttpURLConnection.class);
                AAFLocator loc = new AAFLocator(si,new URI(uriPrefix+"/locate/"+Define.ROOT_NS()+".hello:1.0"));
                AAFConHttp aafcon = new AAFConHttp(access,loc,si);
            
                //
                String pathinfo = "/hello";
                final int iterations = Integer.parseInt(access.getProperty("iterations","5"));
                System.out.println("Calling " + loc + " with Path " + pathinfo + ' ' + iterations + " time" + (iterations==1?"":"s"));
                for (int i=0;i<iterations;++i) {
                    aafcon.best(new Retryable<Void> () {
                        @Override
                        public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                            Future<String> fs = client.read("/hello","text/plain");
                            if (fs.get(5000)) {
                                System.out.print(fs.body());
                            } else {
                                System.err.println("Ooops, missed one: " + fs.code() + ": " + fs.body());
                            }
                            return null;

                        }
                    });
                    Thread.sleep(500L);
                }
            }
        } catch (CadiException | LocatorException | URISyntaxException | APIException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
