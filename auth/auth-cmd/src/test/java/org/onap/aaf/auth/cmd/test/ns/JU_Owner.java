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

package org.onap.aaf.auth.cmd.test.ns;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.ns.Create;
import org.onap.aaf.auth.cmd.ns.NS;
import org.onap.aaf.auth.cmd.ns.Owner;
import org.onap.aaf.auth.cmd.test.JU_AAFCli;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.http.HRcli;
import org.onap.aaf.misc.env.APIException;

import static org.mockito.Mockito.*;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class JU_Owner {

    private static Owner owner;

    @BeforeClass
    public static void setUp() throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
        AAFcli cli = JU_AAFCli.getAAfCli();
        NS ns = new NS(cli);
        owner = new Owner(ns);
    }

    @Test
    public void testExec() throws APIException, LocatorException, CadiException, URISyntaxException {
        String[] strArr = {"add","del","add","del"};
        //owner._exec(0, strArr);

    }

    @Test
    public void detailedHelp() {
        boolean hasNoError = true;
        try {
            owner.detailedHelp(1, new StringBuilder("test"));
        } catch (Exception e) {
            hasNoError = false;
        }
        assertEquals(hasNoError, true);
    }

}
