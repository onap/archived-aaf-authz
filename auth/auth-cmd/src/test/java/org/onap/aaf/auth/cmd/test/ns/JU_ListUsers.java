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
import org.onap.aaf.auth.cmd.ns.NS;
import org.onap.aaf.auth.cmd.test.JU_AAFCli;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.misc.env.APIException;

import aaf.v2_0.Users;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;

public class JU_ListUsers {

    AAFcli cli;
    NS ns;
    List list;
    ListUsers lUsers;

    @Before
    public void setUp() throws APIException, LocatorException, GeneralSecurityException, IOException, CadiException {
        cli = JU_AAFCli.getAAfCli();
        ns = new NS(cli);
        list = new List(ns);
        lUsers = new ListUsers(list);
    }

    @Test
    public void testReports() throws DatatypeConfigurationException {
        Users.User user = new Users.User();
        GregorianCalendar gcal = new GregorianCalendar();
        XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        user.setExpires(xgcal);

        lUsers.report("header", "ns");
        lUsers.report("subHead");
        lUsers.report("prefix", user);
    }

}
