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


package org.onap.aaf.auth.batch.helpers.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.batch.helpers.Cred;
import org.onap.aaf.auth.batch.helpers.Cred.CredCount;
import org.onap.aaf.auth.batch.helpers.Cred.Instance;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.Trans;

import com.datastax.driver.core.Session;

import junit.framework.Assert;

import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import org.junit.Test;

public class JU_Cred {

    private ByteArrayOutputStream outStream;
    private ByteArrayOutputStream errStream;
    Cred cred;
    Instance instance;
    Date date;
    Integer integer;
    PropAccess prop;
    Define define = new Define();
    Trans trans;
    Session session;
    CredCount cc;

    @Before
    public void setUp() throws CadiException {
        outStream = new ByteArrayOutputStream();
        errStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));
        System.setErr(new PrintStream(errStream));
        date = new Date();
        integer = new Integer(20);
        trans = mock(Trans.class);
        session = mock(Session.class);
        cc = new CredCount(3);
        prop = new PropAccess();
        prop.setProperty(Config.AAF_ROOT_NS, "org.onap.aaf");
        prop.setProperty(Config.AAF_ROOT_COMPANY,"test");
        Define.set(prop);
    
        instance = new Instance(12, date, integer, 125642678910L,"234");
        cred = new Cred("myid1234@aaf.att.com");
    }

    @Test
    public void testLast() {        //TODO: set instances 
        Assert.assertNull(cred.last(null));
    }

    @Test
    public void testTypes() {        //TODO: set instances 
        Assert.assertNotNull(cred.types());
    }

    @Test
    public void testCount() {        //TODO: set instances 
        Assert.assertNotNull(cred.count(3));
    }

    @Test
    public void testToString() {        //TODO: set instances 
        Assert.assertEquals("myid1234@aaf.att.com[]", cred.toString());
    }

    @Test
    public void testHashCode() {        //TODO: set instances 
        Assert.assertEquals(-1619358251, cred.hashCode());
    }

    @Test
    public void testEquals() {        //TODO: set instances 
        Assert.assertEquals(true, cred.equals("myid1234@aaf.att.com"));
    }

    @Test
    public void testInc() {    
        Date begin = new Date(date.getTime() - 10);
        Date after = new Date(date.getTime() + 10);
        cc.inc(-1, begin, after);
        cc.inc(1, begin, after);
        cc.inc(2, begin, after);
        cc.inc(200, begin, after);
    }

    @Test
    public void testAuthCount() {        //TODO: set instances 
        Assert.assertEquals(0, cc.authCount(1));
    }

    @Test
    public void testX509Count() {        //TODO: set instances 
        Assert.assertEquals(0, cc.x509Count(0));
    }

    @After
    public void cleanUp() {
        System.setErr(System.err);
        System.setOut(System.out);
    }

}
