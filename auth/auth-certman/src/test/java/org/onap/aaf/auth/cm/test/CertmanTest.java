/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.cm.test;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.http.HBasicAuthSS;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.misc.rosetta.env.RosettaDF;

import certman.v1_0.CertInfo;
import certman.v1_0.CertificateRequest;

public class CertmanTest {

    private static HMangr hman;
    private static AuthzEnv env;
    private static HBasicAuthSS ss;
    private static RosettaDF<CertificateRequest> reqDF;
    private static RosettaDF<CertInfo> certDF;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        env = new AuthzEnv();
//        InputStream ris = env.classLoader().getResource("certman.props").openStream();
//        try {
//            env.load(ris);
//        } finally {
//            ris.close();
//        }
//
//        Locator<URI> loc = new DNSLocator(env, "https", "aaf.it.att.com", "8150");
//        for (Item item = loc.first(); item!=null; item=loc.next(item)) {
//            System.out.println(loc.get(item));
//        }
//    
//    
//        SecurityInfoC<HttpURLConnection> si = SecurityInfoC.instance(env, HttpURLConnection.class);
//        ss = new HBasicAuthSS(si,"m12345@aaf.att.com", 
//                env.decrypt("enc:gvptdJyo0iKdVZw2rzMb0woxa7YKMdqLuhfQ4OQfZ8k",false));
//                env.decrypt("enc:jFfAnO3mOKb9Gzm2OFysslmXpbnyuAxuoNJK",false), si);
//                    SecuritySetter<HttpURLConnection> ss = new X509SS(si, "aaf");
    
//        hman = new HMangr(env,loc);
//
//        reqDF = env.newDataFactory(CertificateRequest.class);
//        reqDF.out(TYPE.JSON);
//        certDF = env.newDataFactory(CertInfo.class);
    }

//    @AfterClass
//    public static void tearDownAfterClass() throws Exception {
//        hman.close();
//    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {
    }

//    @Test
//    public void testX500Name() throws Exception {
//    
//        for ( InetAddress ia : InetAddress.getAllByName("aaf.dev.att.com")) {
//            System.out.printf("%s - %s\n", ia.getHostName(), ia.getHostAddress());
//            InetAddress ia1 = InetAddress.getByName(ia.getHostAddress());
//            System.out.printf("%s - %s\n", ia1.getHostName(), ia1.getHostAddress());
//        }
//    
//        hman.best(ss, new Retryable<Void>() {
//            @Override
//            public Void code(Rcli<?> client) throws APIException, CadiException {
//                CertificateRequest cr = new CertificateRequest();
//                cr.setMechid("a12345@org.osaaf.org");
//                cr.setSponsor("something");
//                cr.getFqdns().add("mithrilcsp.sbc.com");
//                cr.getFqdns().add("zld01907.vci.att.com");
//                cr.getFqdns().add("aaftest.test.att.com");
//            
//                String path = "/cert/local"; // Local Test
////                String path = "/cert/aaf"; // Official CA
//                long end=0,start = System.nanoTime();
//                try {
//                    System.out.println(reqDF.newData().option(Data.PRETTY).load(cr).asString());
//                    Future<String> f = client.updateRespondString(path, reqDF, cr);
//                    if (f.get(10000)) {
//                        end = System.nanoTime();
//                        System.out.println(f.body());
//                        CertInfo capi = certDF.newData().in(Data.TYPE.JSON).load(f.body()).asObject();
//                        for (String c :capi.getCerts()) {
//                            for ( java.security.cert.Certificate x509 : Factory.toX509Certificate(c)) {
//                                System.out.println(x509.toString());
//                            }
//                        }
//                    } else {
//                        end = System.nanoTime();
//                        String msg = "Client returned " + f.code() + ": " + f.body();
//                        System.out.println(msg);
//                        Assert.fail(msg);
//                    }
//                } catch (CertificateException e) {
//                    throw new CadiException(e);
//                } finally {
//                    System.out.println(Chrono.millisFromNanos(start,end) + " ms");
//                }
//                return null;
//            }
//        });
//    
//    
//    }
//
//    public X500Principal ephemeral() {
//        return null;
//    }

}
