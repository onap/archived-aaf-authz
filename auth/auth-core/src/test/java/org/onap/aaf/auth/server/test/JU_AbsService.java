/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.server.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransFilter;
import org.onap.aaf.auth.local.AbsData;
import org.onap.aaf.auth.local.DataFile;
import org.onap.aaf.auth.local.TextIndex;
import org.onap.aaf.auth.server.AbsService;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.register.Registrant;
import org.onap.aaf.misc.env.impl.BasicEnv;

import junit.framework.Assert;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;

import javax.servlet.Filter;

public class JU_AbsService {

    ByteArrayOutputStream outStream;

    private class AbsServiceStub extends AbsService {

        public AbsServiceStub(Access access, BasicEnv env) throws CadiException {
            super(access, env);
            // TODO Auto-generated constructor stub
        }

        @Override
        public Filter[] _filters(Object ... additionalTafLurs) throws CadiException, LocatorException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Registrant[] registrants(int port) throws CadiException, LocatorException {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Before
    public void setUp() {
        outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outStream));
    }

    @After
    public void tearDown() {
        System.setOut(System.out);
    }

    @Test
    public void testStub() throws CadiException {
        BasicEnv bEnv = new BasicEnv();
        PropAccess prop = new PropAccess();
    
        prop.setProperty(Config.AAF_LOCATOR_ENTRIES, "te.st");
        prop.setProperty(Config.AAF_LOCATOR_VERSION, "te.st");
        prop.setLogLevel(Level.DEBUG);
        AbsServiceStub absServiceStub = new AbsServiceStub(prop, bEnv);    //Testing other branches requires "fails" due to exception handling, will leave that off for now.
    }

}




