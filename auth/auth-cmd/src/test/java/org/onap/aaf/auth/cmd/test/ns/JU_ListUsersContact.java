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

package org.onap.aaf.auth.cmd.test.ns;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.ns.List;
import org.onap.aaf.auth.cmd.ns.ListUsers;
import org.onap.aaf.auth.cmd.ns.ListUsersContact;
import org.onap.aaf.auth.cmd.ns.NS;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.cadi.http.HRcli;
import org.onap.aaf.misc.env.APIException;

import static org.mockito.Mockito.*;

import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class JU_ListUsersContact {

    PropAccess prop;
    AuthzEnv aEnv;
    Writer wtr;
    Locator<URI> loc;
    HMangr hman;
    AAFcli aafcli;
    NS ns;
    List list;
    ListUsers lUsers;
    ListUsersContact lUContact;

    @Before
    public void setUp() throws LocatorException, APIException, CadiException {
        prop = new PropAccess();
        aEnv = new AuthzEnv();
        wtr = mock(Writer.class);
        loc = mock(Locator.class);
        SecuritySetter<HttpURLConnection> secSet = mock(SecuritySetter.class);
        hman = new HMangr(aEnv, loc);
        aafcli = new AAFcli(prop, aEnv, wtr, hman, null, secSet);
        ns = new NS(aafcli);
        list = new List(ns);
        lUsers = new ListUsers(list);
    }

    @Test
    public void testExec() throws APIException, LocatorException, CadiException, URISyntaxException {
        lUContact = new ListUsersContact(lUsers);
        Item value = mock(Item.class);
        Locator.Item item = new Locator.Item() {
        };
        when(loc.best()).thenReturn(value);
        URI uri = new URI("http://www.oracle.com/technetwork/java/index.html");
        when(loc.get(value)).thenReturn(uri);
        SecuritySetter<HttpURLConnection> secSet = mock(SecuritySetter.class);
        HRcli hcli = new HRcli(hman, uri, item, secSet);
        //lUContact._exec(0, "test");

    }

    @Test
    public void testDetailedHelp() {
        lUContact = new ListUsersContact(lUsers);
        StringBuilder sb = new StringBuilder();
        lUContact.detailedHelp(0, sb);
    }

}
