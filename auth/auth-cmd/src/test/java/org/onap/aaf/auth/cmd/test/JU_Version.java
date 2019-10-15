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

package org.onap.aaf.auth.cmd.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.Version;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.misc.env.APIException;

import junit.framework.Assert;

//import com.att.aft.dme2.internal.jetty.http.HttpStatus;
//TODO: Gabe [JUnit] Import missing
@RunWith(MockitoJUnitRunner.class)
public class JU_Version {

    private static AAFcli cli;
    private static Version version;

    @BeforeClass
    public static void setUp() throws APIException, LocatorException, GeneralSecurityException, IOException, CadiException {
        cli = JU_AAFCli.getAAfCli();
        version = new Version(cli);
    }

//    @Test
//    public void exec_HTTP_200() throws CadiException, APIException, LocatorException {
//        assertEquals(version._exec(0, "Version"), HttpStatus.OK_200);
//
//    }

    @Test                        //TODO: Temporary fix AAF-111
    public void netYetTested() {
        Assert.assertTrue(true);
    }
}
