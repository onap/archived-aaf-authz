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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.test.JU_AAFCli;
import org.onap.aaf.auth.cmd.user.List;
import org.onap.aaf.auth.cmd.user.ListActivity;
import org.onap.aaf.auth.cmd.user.User;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.misc.env.APIException;

@RunWith(MockitoJUnitRunner.class)
public class JU_ListActivity {

    private static ListActivity lsActivity;

    @BeforeClass
    public static void setUp () throws NoSuchFieldException, SecurityException, Exception, IllegalAccessException {
        AAFcli cli = JU_AAFCli.getAAfCli();
        User usr = new User(cli);
        List parent = new List(usr);
        lsActivity = new ListActivity(parent);

    }
//
//    @Test
//    public void exec() {
//        try {
//            assertEquals(lsActivity._exec(0, "add","del","reset","extend","clear", "rename", "create"),500);
//        } catch (CadiException e) {
//
//            e.printStackTrace();
//        } catch (APIException e) {
//
//            e.printStackTrace();
//        } catch (LocatorException e) {
//
//            e.printStackTrace();
//        }
//    }

    @Test
    public void testDetailedHelp() {
        StringBuilder sb = new StringBuilder();
        lsActivity.detailedHelp(0, sb);
    }
}
