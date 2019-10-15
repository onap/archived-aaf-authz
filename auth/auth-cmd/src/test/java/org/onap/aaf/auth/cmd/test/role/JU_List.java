/*
 * ============LICENSE_START==========================================
 * ===================================================================
 * Copyright © 2018 AT&T Intellectual Property. All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 */

package org.onap.aaf.auth.cmd.test.role;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.role.List;
import org.onap.aaf.auth.cmd.role.Role;
import org.onap.aaf.auth.cmd.test.JU_AAFCli;
import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.misc.env.APIException;

import aaf.v2_0.Perms;
import aaf.v2_0.Pkey;
import aaf.v2_0.Roles;
import aaf.v2_0.UserRoles;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.ArrayList;

import org.junit.Test;

public class JU_List {

    AAFcli cli;
    Role role;
    List list;
    PropAccess prop;
    AuthzEnv aEnv;
    Writer wtr;
    Locator<URI> loc;
    HMangr hman;
    AAFcli aafcli;

    private class ListRolesStub extends List {

        public ListRolesStub(Role parent) {
            super(parent);
            // TODO Auto-generated constructor stub
        }
    }

    private class RolesStub extends Roles {
        public void addRole(aaf.v2_0.Role role) {
            if (this.role == null) {
                this.role = new ArrayList<>();
            }
            this.role.add(role);
        }
    }

    private class RoleStub extends aaf.v2_0.Role {

        public void addPerms(Pkey perms) {
            if (this.perms == null) {
                this.perms = new ArrayList<>();
            }
            this.perms.add(perms);
        }
    }

    @Before
    public void setUp() throws APIException, LocatorException, GeneralSecurityException, IOException, CadiException{
        prop = new PropAccess();
        aEnv = new AuthzEnv();
        wtr = mock(Writer.class);
        loc = mock(Locator.class);
        SecuritySetter<HttpURLConnection> secSet = mock(SecuritySetter.class);
        hman = new HMangr(aEnv, loc);
        aafcli = new AAFcli(prop, aEnv, wtr, hman, null, secSet);
        role = new Role(aafcli);
        list = new List(role);
    }

    @Test
    public void testRoles() throws APIException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Role role = new Role(aafcli);
        ListRolesStub listStub = new ListRolesStub(role);
        Future future = mock(Future.class);
        Rcli rcli = mock(Rcli.class);

        Class c = listStub.getClass();
        Class[] cArg = new Class[3];
        cArg[0] = Future.class;
        cArg[1] = Rcli.class;
        cArg[2] = String.class;//Steps to test a protected method
        //Method listMethod = c.getDeclaredMethod("list", cArg);
        //listMethod.setAccessible(true);
        //listMethod.invoke(listStub, future, rcli, "test");

    }

    @Test
    public void testReport() throws Exception {
        UserRoles urs = new UserRoles();
        Perms perms = new Perms();
        RolesStub roles = new RolesStub();
        list.report(roles, perms , urs , "test");
        AAFcli cli = JU_AAFCli.getAAfCli();
        RoleStub role = new RoleStub();
        roles.addRole(role);
        Pkey pkey = new Pkey();
        pkey.setInstance("test");
        pkey.setAction("test");
        pkey.setInstance("test");
        pkey.setType("test");
        role.addPerms(pkey);
        list.report(roles, perms , null , "test");
        list.report(roles, perms , urs , "test");

        aafcli.eval("DETAILS @[ 123");
        role.setName("test");

        list.report(roles, perms , urs , "test");
    }

}
