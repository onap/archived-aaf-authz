/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * *
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/

package org.onap.aaf.auth.cmd.test.user;

import org.junit.Assert;
import org.junit.Before;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.user.Cred;
import org.onap.aaf.auth.cmd.user.Delg;
import org.onap.aaf.auth.cmd.user.User;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.cadi.http.HRcli;
import org.onap.aaf.misc.env.APIException;

@RunWith(MockitoJUnitRunner.class)
public class JU_Delg {

    private static User testUser;
    private static Delg delg;
    User user;
    PropAccess prop;
    AuthzEnv aEnv;
    Writer wtr;
    Locator<URI> loc;
    HMangr hman;
    AAFcli aafcli;

    @Before
    public void setUp() throws FileNotFoundException, APIException, LocatorException, CadiException {
        prop = new PropAccess();
        aEnv = new AuthzEnv();
        wtr = mock(Writer.class);
        loc = mock(Locator.class);
        SecuritySetter<HttpURLConnection> secSet = mock(SecuritySetter.class);
        hman = mock(HMangr.class); //new HMangr(aEnv, loc);
        aafcli = mock(AAFcli.class);//new AAFcli(prop, aEnv, wtr, hman, null, secSet);
//        user = mock(User.class); //new User(aafcli);
//        delg = new Delg(user);
    }

    @Test
    public void testExec() throws APIException, LocatorException, CadiException, URISyntaxException {
        Item value = mock(Item.class);
        Locator.Item item = new Locator.Item() {
        };
        when(loc.best()).thenReturn(value);
        URI uri = new URI("http://www.oracle.com/technetwork/java/index.html");
        when(loc.get(value)).thenReturn(uri);
        SecuritySetter<HttpURLConnection> secSet = mock(SecuritySetter.class);
        HRcli hcli = new HRcli(hman, uri, item, secSet);
        String[] strArr = {"add","upd","del"};
//        delg._exec(0, strArr);
    
        String[] strArr1 = {"upd","del","add"};
//        delg._exec(0, strArr1);
    
        String[] strArr2 = {"del","add"};
//        delg._exec(0, strArr2);

    }

    @Test
    public void testDetailedHelp() {
        StringBuilder sb = new StringBuilder();
//        delg.detailedHelp(0, sb);
    }

}
