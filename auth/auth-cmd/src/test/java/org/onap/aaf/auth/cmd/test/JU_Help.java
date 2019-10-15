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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.Help;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.misc.env.APIException;

import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class JU_Help {

    private static AAFcli cli;
    private static Help help;
    String[] strArr = {"null","null","b","c"};
    private class CmdStub extends Cmd {


        public CmdStub(AAFcli aafcli, String name, Param[] params) {
            super(aafcli, name, params);
            // TODO Auto-generated constructor stub
        }

        public CmdStub(Cmd parent, String name, Param[] params) {
            super(parent, name, params);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected int _exec(int idx, String... args) throws CadiException, APIException, LocatorException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public void error(Future<?> future) {
            super.error(future);
        }

    }

    @Mock
    private static List<Cmd> cmds;

    @Before
    public void setUp() throws APIException, LocatorException, GeneralSecurityException, IOException, CadiException {
        cli = JU_AAFCli.getAAfCli();
        cmds = new ArrayList<>();
        Param[] param = new Param[] {new Param("name",true)};
        CmdStub cmd = new CmdStub(cli, "null", param);
        cmds.add(cmd);
        help = new Help(cli, cmds);
    }

    @Test
    public void exec_HTTP_200() {
        try {
            assertEquals(help._exec(1, "helps"), HttpStatus.OK_200);
            assertEquals(help._exec(1, strArr), HttpStatus.OK_200);
        } catch (CadiException | APIException | LocatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void exec_HTTP_200_1() {
        try {
            assertEquals(help._exec(1, "helps","help"), HttpStatus.OK_200);
        } catch (CadiException | APIException | LocatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void detailhelp() {
        boolean hasError=false;
        try {
            help.detailedHelp(2, new StringBuilder("detail help test"));
        } catch (Exception e) {
            hasError=true;
        }
        assertEquals(hasError,false);
    }

}
