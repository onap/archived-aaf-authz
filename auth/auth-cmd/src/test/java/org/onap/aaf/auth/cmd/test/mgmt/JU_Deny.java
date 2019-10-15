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

package org.onap.aaf.auth.cmd.test.mgmt;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.mgmt.Deny;
import org.onap.aaf.auth.cmd.mgmt.Deny.DenySomething;
import org.onap.aaf.auth.cmd.mgmt.Mgmt;
import org.onap.aaf.auth.cmd.ns.Create;
import org.onap.aaf.auth.cmd.ns.NS;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransOnlyFilter;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.cadi.http.HRcli;
import org.onap.aaf.misc.env.APIException;

import static org.mockito.Mockito.*;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;

public class JU_Deny {

    Deny deny;
    DenySomething denyS;
    PropAccess prop;
    AuthzEnv aEnv;
    Writer wtr;
    Locator<URI> loc;
    HMangr hman;
    AAFcli aafcli;

    @Before
    public void setUp() throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
        prop = new PropAccess();
        aEnv = new AuthzEnv();
        wtr = mock(Writer.class);
        loc = mock(Locator.class);
        SecuritySetter<HttpURLConnection> secSet = mock(SecuritySetter.class);
//        hman = new HMangr(aEnv, loc);
//        aafcli = new AAFcli(prop, aEnv, wtr, hman, null, secSet);
//        Mgmt mgmt = new Mgmt(aafcli);
//        deny = new Deny(mgmt);
        //denyS = deny.new DenySomething(deny,"ip","ipv4or6[,ipv4or6]*");

    }



    @Test
    public void testExec() throws APIException, LocatorException, CadiException, URISyntaxException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Item value = mock(Item.class);
        Locator.Item item = new Locator.Item() {
        };
        when(loc.best()).thenReturn(value);
        URI uri = new URI("http://www.oracle.com/technetwork/java/index.html");
        when(loc.get(value)).thenReturn(uri);
        SecuritySetter<HttpURLConnection> secSet = mock(SecuritySetter.class);
//        HRcli hcli = new HRcli(hman, uri, item, secSet);

//        String[] strArr = {"add","del", "add","del"};
//        deny._exec(0, strArr);
//    
//        String[] strArr1 = {"del", "add","del"};
//        deny._exec(0, strArr1);
    
    }

}
