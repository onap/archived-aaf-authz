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

package org.onap.aaf.cadi.enduser.test;

import java.io.IOException;
import java.net.ConnectException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Result;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.configure.Agent;
import org.onap.aaf.cadi.oauth.TimedToken;
import org.onap.aaf.cadi.oauth.TokenClient;
import org.onap.aaf.cadi.oauth.TokenClientFactory;
import org.onap.aaf.cadi.oauth.TzClient;
import org.onap.aaf.cadi.util.FQI;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Chrono;

import aafoauth.v2_0.Introspect;
import aafoauth.v2_0.Token;


public class OnapClientExample {
    private static TokenClientFactory tcf;
    private static PropAccess access;

    public final static void main(final String args[]) {
        // These Objects are expected to be Long-Lived... Construct once
        
        // Property Access
        // This method will allow you to set "cadi_prop_files" (or any other property) on Command line 
        access = new PropAccess(args);
        // Note: This style will load "cadi_prop_files" from VM Args
        // access = PropAccess();
        try {
            Map<String, String> aaf_urls = Agent.loadURLs(access);
            Agent.fillMissing(access, aaf_urls);
        
            // Token aware Client Factory
            tcf = TokenClientFactory.instance(access);
        } catch (APIException | GeneralSecurityException | IOException | CadiException e1) {
            access.log(e1, "Unable to setup OAuth Client Factory, Fail Fast");
            System.exit(1);
        }
        
        final int CALL_TIMEOUT = Integer.parseInt(access.getProperty(Config.AAF_CALL_TIMEOUT,Config.AAF_CALL_TIMEOUT_DEF));
        
        try {
            //////////////////////////////////////////////////////////////////////
            // Scenario 1:
            // Get and use an OAuth Client, which understands Token Management
            //////////////////////////////////////////////////////////////////////
            // Create a Token Client, that gets its tokens from expected OAuth Server
            //   In this example, it is AAF, but it can be the Alternate OAuth

            TokenClient tc = tcf.newClient(Config.AAF_OAUTH2_TOKEN_URL); // can set your own timeout here (url, timeoutMilliseconds)
            
            // Here's a trick to get the namespace out of a Fully Qualified AAF Identity (your MechID)
            String ns = FQI.reverseDomain(tc.client_id());
            System.out.printf("\nNote: The AAF Namespace of FQI (Fully Qualified Identity) %s is %s\n\n",tc.client_id(), ns);

            // Now, we can get a Token.  Note: for "scope", use AAF Namespaces to get AAF Permissions embedded in
            // Note: getToken checks if Token is expired, if so, then refreshes before handing back.
            Result<TimedToken> rtt = tc.getToken(ns,"org.onap.test"); // get multiple scopes
            
            // Note: you can clear a Token's Disk/Memory presence by
            //  1) removing the Token from the "token/outgoing" directory on the O/S
            //  2) programmatically by calling "clearToken" with exact params as "getToken", when it has the same credentials set
            //       tc.clearToken("org.onap.aaf","org.onap.test");
            
            // Result Object can be queried for success
            if (rtt.isOK()) {
                TimedToken token = rtt.value;
                print(token); // Take a look at what's in a Token
                
                // Use this Token in your client calls with "Tokenized Client" (TzClient)
                // These should NOT be used cross thread.
                // Get Hello Service URL... roll your own in your own world.
                final String endServicesURL = access.getProperty(Config.AAF_OAUTH2_HELLO_URL);


                TzClient helloClient = tcf.newTzClient(endServicesURL);
                helloClient.setToken(tc.client_id(), token);
                
                // This client call style, "best" call with "Retryable" inner class covers finding an available Service 
                // (when Multi-services exist) for the best service, based (currently) on distance.
                //
                // the "Generic" in Type gives a Return Value for the Code, which you can set on the "best" method
                // Note that variables used in the inner class from this part of the code must be "final", see "CALL_TIMEOUT"
                String rv = helloClient.best(new Retryable<String>() {
                    @Override
                    public String code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                        Future<String> future = client.read("hello","text/plain");
                        // The "future" calling method allows you to do other processing, such as call more than one backend
                        // client before picking up the result
                        // If "get" matches the HTTP Code for the method (i.e. read HTTP Return value is 200), then 
                        if (future.get(CALL_TIMEOUT)) {
                            // Client Returned expected value
                            return future.value;
                        } else {
                            throw new APIException(future.code()  + future.body());
                        }                    
                    }
                });
                
                // You want to do something with returned value.  Here, we say "hello"
                System.out.printf("\nPositive Response from Hello: %s\n",rv);
                
                
                //////////////////////////////////////////////////////////////////////
                // Scenario 2:
                // As a Service, read Introspection information as proof of Authenticated Authorization
                //////////////////////////////////////////////////////////////////////
                // CADI Framework (i.e. CadiFilter) works with the Introspection to drive the J2EE interfaces (
                // i.e. if (isUserInRole("ns.perm|instance|action")) {...
                //
                // Here, however, is a way to introspect via Java
                //
                // now, call Introspect (making sure right URLs are set in properties)
                // We need a Different Introspect TokenClient, because different Endpoint (and usually different Services)
                TokenClient tci = tcf.newClient(Config.AAF_OAUTH2_INTROSPECT_URL);
                Result<Introspect> is = tci.introspect(token.getAccessToken());
                if (is.isOK()) {
                    // Note that AAF will add JSON set of Permissions as part of "Content:", legitimate extension of OAuth Structure
                    print(is.value); // do something with Introspect Object
                } else {
                    access.printf(Level.ERROR, "Unable to introspect OAuth Token %s: %d %s\n",
                            token.getAccessToken(),rtt.code,rtt.error);
                }
            } else {
                access.printf(Level.ERROR, "Unable to obtain OAuth Token: %d %s\n",rtt.code,rtt.error);
            }
            
        } catch (CadiException | LocatorException | APIException | IOException e) {
            e.printStackTrace();
        }
    }
    
    /////////////////////////////////////////////////////////////
    // Examples of Object Access
    /////////////////////////////////////////////////////////////
    private static void print(Token t) {
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
    
    private static void print(Introspect ti) {
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
                + "\tContent:\t%s\n",
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
