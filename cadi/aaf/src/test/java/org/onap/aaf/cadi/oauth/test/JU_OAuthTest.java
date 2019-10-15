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

package org.onap.aaf.cadi.oauth.test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Result;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.oauth.TimedToken;
import org.onap.aaf.cadi.oauth.TokenClient;
import org.onap.aaf.cadi.oauth.TokenClientFactory;
import org.onap.aaf.cadi.oauth.TzClient;
import org.onap.aaf.cadi.principal.Kind;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Chrono;

import aafoauth.v2_0.Introspect;
import aafoauth.v2_0.Token;
import junit.framework.Assert;

public class JU_OAuthTest {

    private ByteArrayOutputStream outStream;

    private static PropAccess access;
    private static TokenClientFactory tcf;

    @BeforeClass
    public static void setUpBeforeClass()  {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));

        access = new PropAccess();
        access.setProperty(Config.CADI_LATITUDE, "38");
        access.setProperty(Config.CADI_LONGITUDE, "-72");
        try {
            tcf = TokenClientFactory.instance(access);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        Field field = SecurityInfoC.class.getDeclaredField("sicMap");
        field.setAccessible(true);
        field.set(null, new HashMap<>());
    }

    @Before
    public void setUp() throws Exception {
        outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));
    }

    @After
    public void tearDown() throws Exception {
        System.setOut(System.out);
    }

    @Test
    public void testROPCFlowHappy() {
        try {
            // AAF OAuth
            String client_id = access.getProperty(Config.AAF_APPID);
            String client_secret = access.getProperty(Config.AAF_APPPASS);
            String tokenServiceURL = access.getProperty(Config.AAF_OAUTH2_TOKEN_URL);
//            Assert.assertNotNull(tokenServiceURL);
            String tokenIntrospectURL = access.getProperty(Config.AAF_OAUTH2_INTROSPECT_URL);
            String tokenAltIntrospectURL = access.getProperty(Config.AAF_ALT_OAUTH2_INTROSPECT_URL);
//            Assert.assertNotNull(tokenIntrospectURL);
            final String endServicesURL = access.getProperty(Config.AAF_OAUTH2_HELLO_URL);
            String username = access.getProperty("cadi_username");

            TokenClient tc;
            Result<TimedToken> rtt;
            if (true) {
                tc = tcf.newClient(tokenServiceURL, 3000);
                tc.client_creds(client_id,client_secret);
                tc.password(access.getProperty("cadi_username"),access.getProperty("cadi_password"));
                rtt = tc.getToken(Kind.BASIC_AUTH,"org.osaaf.aaf","org.osaaf.test");
                if (rtt.isOK()) {
                    print(rtt.value);
                    rtt = tc.refreshToken(rtt.value);
                    if (rtt.isOK()) {
                        print(rtt.value);
                        TokenClient ic = tcf.newClient(tokenIntrospectURL,3000);
                        ic.client_creds(client_id,client_secret);

                        Result<Introspect> ri = ic.introspect(rtt.value.getAccessToken());
                        if (ri.isOK()) {
                            print(ri.value);
                        } else {
                            System.out.println(ri.code + ' ' + ri.error);
                            Assert.fail(ri.code + ' ' + ri.error);
                        }
                        TzClient helloClient = tcf.newTzClient(endServicesURL);
                        helloClient.setToken(client_id, rtt.value);
//                        String rv = serviceCall(helloClient);
//                        System.out.println(rv);
        //                Assert.assertEquals("Hello AAF OAuth2\n",rv);
                    } else {
                        System.out.println(rtt.code + ' ' + rtt.error);
                        Assert.fail(rtt.code + ' ' + rtt.error);
                    }
                } else {
                    System.out.println(rtt.code + ' ' + rtt.error);
                    Assert.fail(rtt.code + ' ' + rtt.error);
                }
            }
    
            // ISAM Test
            if (true) {
                System.out.println("**** ISAM TEST ****");
                tokenServiceURL=access.getProperty(Config.AAF_ALT_OAUTH2_TOKEN_URL);
                client_id=access.getProperty(Config.AAF_ALT_CLIENT_ID);
                client_secret=access.getProperty(Config.AAF_ALT_CLIENT_SECRET);
                if (tokenServiceURL!=null) {
                    tc = tcf.newClient(tokenServiceURL, 3000);
                    tc.client_creds(client_id, client_secret);
                    int at = username.indexOf('@');
                
                    tc.password(at>=0?username.substring(0, at):username,access.getProperty("cadi_password"));
                    rtt = tc.getToken("org.osaaf.aaf","org.osaaf.test");
                    if (rtt.isOK()) {
                        print(rtt.value);
                        rtt = tc.refreshToken(rtt.value);
                        if (rtt.isOK()) {
                            print(rtt.value);
                        
                            tc = tcf.newClient(tokenAltIntrospectURL, 3000);
                            tc.client_creds(client_id, client_secret);
                            Result<Introspect> rti = tc.introspect(rtt.value.getAccessToken());
                            if (rti.isOK()) {
                                System.out.print("Normal ISAM ");
                                print(rti.value);
                            } else {
                                System.out.println(rti.code + ' ' + rti.error);
                                Assert.fail(rtt.code + ' ' + rtt.error);
                            }

                            tc = tcf.newClient(tokenIntrospectURL, 3000);
                            tc.client_creds(client_id, client_secret);
                            rti = tc.introspect(rtt.value.getAccessToken());
                            if (rti.isOK()) {
                                System.out.print("AAF with ISAM Token ");
                                print(rti.value);
                            } else {
                                System.out.println(rti.code + ' ' + rti.error);
                                if (rti.code!=404) {
                                    Assert.fail(rti.code + ' ' + rti.error);
                                }
                            }

                            TzClient tzClient = tcf.newTzClient(endServicesURL);
                            tzClient.setToken(client_id, rtt.value);
                            // Note: this is AAF's "Hello" server
                            String rv = serviceCall(tzClient);
                            System.out.println(rv);
            //                Assert.assertEquals("Hello AAF OAuth2\n",rv);
                        } else {
                            System.out.println(rtt.code + ' ' + rtt.error);
                            Assert.fail(rtt.code + ' ' + rtt.error);
                        }
                    } else {
                        System.out.println(rtt.code + ' ' + rtt.error);
                        Assert.fail(rtt.code + ' ' + rtt.error);
                    }
                } else {
                    Assert.fail(Config.AAF_ALT_OAUTH2_TOKEN_URL + " is required");
                }
            }
        } catch (Exception e) {
//            Assert.fail();
        }
    }


//    private TokenClient testROPCFlow(final String url, final String client_id, final String client_secret, String user, String password, final String ... scope) throws Exception {
//        TokenClient tclient = tcf.newClient(url,3000);
//        tclient.client_creds(client_id, client_secret);
//        if (user!=null && password!=null) {
//            tclient.password(user,password);
//        }
//        Result<TimedToken> rt = tclient.getToken(scope);
//        if (rt.isOK()) {
//            print(rt.value);
//            Result<Introspect> rti = tclient.introspect(rt.value.getAccessToken());
//            if (rti.isOK()) {
//                print(rti.value);
//            } else {
//                printAndFail(rti);
//            }
//        } else {
//            printAndFail(rt);
//        }
//        return tclient;
//    }

    private String serviceCall(TzClient tzClient) throws Exception {
        return tzClient.best(new Retryable<String>() {
            @Override
            public String code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                Future<String> future = client.read(null,"text/plain");
                if (future.get(3000)) {
                    return future.value;
                } else {
                    throw new APIException(future.code()  + future.body());
                }
            }
        });
    }
//    private void printAndFail(Result<?> rt) {
//        System.out.printf("HTTP Code %d: %s\n", rt.code, rt.error);
//        Assert.fail(rt.toString());
//    }

    private void print(Token t) {
        GregorianCalendar exp_date = new GregorianCalendar();
        exp_date.add(GregorianCalendar.SECOND, t.getExpiresIn());
        System.out.printf("Access Token\n\tToken:\t\t%s\n\tToken Type:\t%s\n\tExpires In:\t%d (%s)\n\tScope:\t\t%s\n\tRefresh Token:\t%s\n",
        t.getAccessToken(),
        t.getTokenType(),
        t.getExpiresIn(),
        Chrono.timeStamp(new Date(System.currentTimeMillis()+(t.getExpiresIn()*1000))),
        t.getScope(),
        t.getRefreshToken());
    }

    private void print(Introspect ti) {
        if (ti==null || ti.getClientId()==null) {
            System.out.println("Empty Introspect");
            return;
        }
        Date exp = new Date(ti.getExp()*1000); // seconds
        System.out.printf("Introspect\n"
                + "\tAccessToken:\t%s\n"
                + "\tClient-id:\t%s\n"
                + "\tClient Type:\t%s\n"
                + "\tActive:  \t%s\n"
                + "\tUserName:\t%s\n"
                + "\tExpires: \t%d (%s)\n"
                + "\tScope:\t\t%s\n"
                + "\tContent:\t\t%s\n",
        ti.getAccessToken(),
        ti.getClientId(),
        ti.getClientType(),
        ti.isActive()?Boolean.TRUE.toString():Boolean.FALSE.toString(),
        ti.getUsername(),
        ti.getExp(),
        Chrono.timeStamp(exp),
        ti.getScope(),
        ti.getContent()==null?"":ti.getContent());
    
        System.out.println();
    }
}
