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
import static org.mockito.Mockito.*;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.auth.cmd.mgmt.Cache;
import org.onap.aaf.auth.cmd.mgmt.Clear;
import org.onap.aaf.auth.cmd.mgmt.Mgmt;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.server.AbsService;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.register.Registrant;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.impl.BasicEnv;

import aaf.v2_0.History;
import aaf.v2_0.History.Item;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.util.List;

import javax.servlet.Filter;

import org.junit.Test;

public class JU_Cmd {

    CmdStub cmd;
    CmdStub cmd1;
    CmdStub cmd2;
    AAFcli cli;

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

    @Before
    public void setUp() throws APIException, LocatorException, GeneralSecurityException, IOException, CadiException {
        cli = JU_AAFCli.getAAfCli();
        Param[] param = new Param[] {new Param("name",true)};

        cmd = new CmdStub(cli,"test", param);
        cmd1 = new CmdStub(cmd,"test", param);
        cmd2 = new CmdStub(cmd,"test", param);
    }

    @Test
    public void testReportColHead() {
        String[] args = new String[] {new String("test")};
        cmd.reportColHead("format", args);
    }

    @Test
    public void testBuilder() {
        StringBuilder detail = new StringBuilder();
        StringBuilder sb = new StringBuilder("test 123");

        cmd.build(sb, detail);
        detail.append("test");
        cmd.build(sb, detail);
    }

    @Test
    public void testApi() throws APIException, CadiException {
        StringBuilder sb = new StringBuilder("test 123");
        Define def = new Define();
        PropAccess prop = new PropAccess();
        def.set(prop);
        Mgmt mgmt = new Mgmt(cli);
        Cache cache = new Cache(mgmt);
        Clear clr = new Clear(cache);
        clr.detailedHelp(0, sb);
    }

    @Test
    public void testToString() {
        cmd.toString();
    }

    @Test
    public void testFullID() {
        cmd.fullID("test");
        cmd.fullID("t@st");
        cmd.fullID(null);
    }

    @Test
    public void testError() {
        Future<?> future = mock(Future.class);
        cmd.error(future);
        when(future.code()).thenReturn(401);
        cmd.error(future);
        when(future.code()).thenReturn(403);
        cmd.error(future);
        when(future.code()).thenReturn(404);
        cmd.error(future);
        when(future.body()).thenReturn("NotNull");
        cmd.error(future);
        when(future.body()).thenReturn("{NotNull");
        cmd.error(future);
        when(future.body()).thenReturn("<html>NotNull");
        cmd.error(future);
    }

    @Test
    public void testActivity() {
        History hist = new History();
        cmd.activity(hist, "test");
        cmd.activity(hist, "te[st");
    }

    @Test
    public void testWhichOption() throws CadiException {
        String[] strArr = {"a", "b", "c"};
        cmd.whichOption(strArr, "b");
    }

    @Test
    public void testOneOf() throws APIException, CadiException, LocatorException {
        Retryable retryable = mock(Retryable.class);
        //cmd.oneOf(retryable, "host");            //TODO: AAF-111 need input for hMan
    }

    @Test
    public void testExec() throws CadiException, APIException, LocatorException {
        String[] strArr = {"a", "b", "c"};
        cmd.exec(1, strArr);
    }



}
