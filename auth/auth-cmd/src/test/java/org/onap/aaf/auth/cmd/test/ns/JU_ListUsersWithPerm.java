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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.ns.List;
import org.onap.aaf.auth.cmd.ns.ListUsers;
import org.onap.aaf.auth.cmd.ns.ListUsersWithPerm;
import org.onap.aaf.auth.cmd.ns.NS;
import org.onap.aaf.auth.cmd.test.JU_AAFCli;

@RunWith(MockitoJUnitRunner.class)
public class JU_ListUsersWithPerm {

    private static ListUsersWithPerm lsUserWithPerm;

    @BeforeClass
    public static void setUp() throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
        AAFcli cli = JU_AAFCli.getAAfCli();
        NS ns = new NS(cli);
        List ls = new List(ns);//possible wrong import, remove import org.onap.aaf.auth.cmd.ns to see other option
        ListUsers lsU = new ListUsers(ls);
        lsUserWithPerm = new ListUsersWithPerm(lsU);
    }

//    @Test
//    public void exec() {
//        try {
//            assertEquals(lsUserWithPerm._exec(0, "add", "del", "reset", "extend"), 500);
//        } catch (Exception e) {
//            assertEquals(e.getMessage(), "No Services Found for https://DME2RESOLVE [ ]");
//
//        }
//    }

    @Test
    public void detailedHelp() {
        boolean hasNoError = true;
        try {
            lsUserWithPerm.detailedHelp(1, new StringBuilder("test"));
        } catch (Exception e) {
            hasNoError = false;
        }
        assertEquals(hasNoError, true);
    }

    @Test                        //TODO: Temporary fix AAF-111
    public void netYetTested() {
        Assert.assertTrue(true);
    }
}
