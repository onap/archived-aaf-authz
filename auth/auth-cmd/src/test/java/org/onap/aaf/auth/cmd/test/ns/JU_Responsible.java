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

import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.test.JU_AAFCli;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.misc.env.APIException;

public class JU_Responsible {

//    private static Responsible responsible;//TODO: Gabe[JUnit] check with Jonathan
//
//    @BeforeClass
//    public static void setUp() throws APIException, LocatorException, GeneralSecurityException, IOException {
//        AAFcli cli = JU_AAFCli.getAAfCli();
//        NS ns = new NS(cli);
//        responsible = new Responsible(ns);
//
//    }
//
//    @Test
//    public void exec1() {
//        try {
//            responsible._exec(0, "del", "del", "del");
//        } catch (Exception e) {
//            assertEquals(e.getMessage(), "java.net.UnknownHostException: DME2RESOLVE");
//        }
//    }
//
//    @Test
//    public void detailedHelp() {
//        boolean hasNoError = true;
//        try {
//            responsible.detailedHelp(1, new StringBuilder("test"));
//        } catch (Exception e) {
//            hasNoError = false;
//        }
//        assertEquals(hasNoError, true);
//    }

    @Test
    public void netYetTested() {
        Assert.assertTrue(true);
    }

}
