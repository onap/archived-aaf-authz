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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.DeprecatedCMD;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.auth.cmd.test.JU_Cmd;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.misc.env.APIException;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.Test;

public class JU_DeprecatedCMD {

    CmdStub cmd;
    AAFcli cli;

    private class CmdStub extends Cmd {

        public CmdStub(AAFcli aafcli, String name, Param[] params) {
            super(aafcli, name, params);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected int _exec(int idx, String... args) throws CadiException, APIException, LocatorException {
            // TODO Auto-generated method stub
            return 0;
        }

    }

    @Test
    public void testExec() throws CadiException, APIException, LocatorException, GeneralSecurityException, IOException {
        cli = JU_AAFCli.getAAfCli();
        Param[] param = new Param[] {new Param("name",true)};

        cmd = new CmdStub(cli,"test", param);
        DeprecatedCMD deprecatedcmd = new DeprecatedCMD(cmd,"test", "test");
        deprecatedcmd._exec(0, "test");
    }

}
