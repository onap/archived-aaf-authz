/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * <p>
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/

package org.onap.aaf.auth.rserv.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.onap.aaf.auth.rserv.Match;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.impl.EnvFactory;


public class JU_BetterMatch {

    @Test
    public void test() {
        Trans trans = EnvFactory.newTrans();
        // Bad Match
        Match bm = new Match("/req/1.0.0/:var");

        assertTrue(bm.match("/req/1.0.0/fred"));
        assertTrue(bm.match("/req/1.0.0/wilma"));
        assertTrue(bm.match("/req/1.0.0/wilma/"));
        assertFalse(bm.match("/req/1.0.0/wilma/bambam"));
        assertFalse(bm.match("/not/valid/234"));
        assertFalse(bm.match(""));
    
        TimeTaken tt = trans.start("A", Env.SUB);
        TimeTaken tt2;
        int i = 0;
        try {
            bm = new Match(null);
            tt2 = trans.start(Integer.toString(++i), Env.SUB);
            assertTrue(bm.match(""));
            tt2.done();
            tt2 = trans.start(Integer.toString(++i), Env.SUB);
            assertTrue(bm.match(null));
            tt2.done();
        } finally {
            tt.done();
        }
    

        tt = trans.start("B", Env.SUB);
        i = 0;
        try {
            bm = new Match("/req/1.0.0/:urn/:ref");
            tt2 = trans.start(Integer.toString(++i), Env.SUB);
            assertTrue(bm.match("/req/1.0.0/urn:fsdb,1.0,req,newreq/0x12345"));
            tt2.done();
            tt2 = trans.start(Integer.toString(++i), Env.SUB);
            assertFalse(bm.match("/req/1.0.0/urn"));
            tt2.done();
            tt2 = trans.start(Integer.toString(++i), Env.SUB);
            assertTrue(bm.match("/req/1.0.0/urn:fsdb,1.0,req,newreq/0x12345/"));
            tt2.done();
            tt2 = trans.start(Integer.toString(++i), Env.SUB);
            assertFalse(bm.match("/req/1.0.0/urn:fsdb,1.0,req,newreq/0x12345/x"));
            tt2.done();
            tt2 = trans.start(Integer.toString(++i), Env.SUB);
            assertFalse(bm.match("/req/1.0.0/urn:fsdb,1.0,req,newreq/0x12345/xyx"));
        } finally {
            tt2.done();
            tt.done();
        }
    
        tt = trans.start("C", Env.SUB);
        i = 0;
        try {
            String url = "/req/1.0.0/";
            bm = new Match(url+":urn*");
            tt2 = trans.start(Integer.toString(++i), Env.SUB);
            String value = "urn:fsdb,1.0,req,newreq/0x12345";
        
            assertTrue(bm.match(url+value));
            assertEquals("urn:fsdb,1.0,req,newreq/0x12345",bm.param(url+value, ":urn"));
        } finally {
            tt2.done();
            tt.done();
        }

        tt = trans.start("D", Env.SUB);
        i = 0;
        try {
            bm = new Match("/req/1.0.0/:urn/:ref*");
            tt2 = trans.start(Integer.toString(++i), Env.SUB);
            assertTrue(bm.match("/req/1.0.0/urn:fsdb,1.0,req,newreq/0x12345"));
            tt2.done();
            tt2 = trans.start(Integer.toString(++i), Env.SUB);
            assertFalse(bm.match("/req/1.0.0/urn:fsdb,1.0,req,newreq/"));
        } finally {
            tt2.done();
            tt.done();
        }

        tt = trans.start("E", Env.SUB);
        i = 0;
        try {
            bm = new Match("this*");
            tt2 = trans.start(Integer.toString(++i), Env.SUB);
            assertTrue(bm.match("this"));
            tt2.done();
            tt2 = trans.start(Integer.toString(++i), Env.SUB);
            assertTrue(bm.match("thisandthat"));
            tt2.done();
            tt2 = trans.start(Integer.toString(++i), Env.SUB);
            assertTrue(bm.match("this/1.0.0/urn:fsdb,1.0,req,newreq/0x12345/"));
        } finally {
            tt2.done();
            tt.done();
        }

        tt = trans.start("F", Env.SUB);
        i = 0;
        try {
            bm = new Match("*");
            tt2 = trans.start(Integer.toString(++i), Env.SUB);
            assertTrue(bm.match("<pass>/this"));
        } finally {
            tt2.done();
            tt.done();
        }
    
        StringBuilder sb = new StringBuilder();
        trans.auditTrail(0, sb);
        //System.out.println(sb);
    
    }

    @Test
    public void specialTest() {
        Match match = new Match("/sample");
        assertTrue(match.match("/sample"));
    
        match = new Match("/lpeer//lpeer/:key/:item*");
        assertTrue(match.match("/lpeer//lpeer/x/y"));
        assertFalse(match.match("/lpeer/x/lpeer/x/y"));

    }

    @Test
    public void testGetParamNames() {
        Match bm = new Match("/req/1.0.0/:var");
        Set s = bm.getParamNames();
        Assert.assertNotNull(s);
    }
}
