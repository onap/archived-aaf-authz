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

package org.onap.aaf.misc.env.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

public class JU_JAXBStringifierTest {

    @Mock
    JAXBmar jumar;

    @Mock
    QName qname;

    @Mock
    Env env;

    TimeTaken tt,ttstringify;

    LogTarget logT;

    @Before
    public void setUp() {
        initMocks(this);
        tt=Mockito.mock(TimeTaken.class);
        Mockito.doReturn(tt).when(env).start("JAXB Marshal", Env.XML);
        Mockito.doNothing().when(tt).done();
        logT = Mockito.mock(LogTarget.class);
        Mockito.doReturn(logT).when(env).debug();
    }

    @Test
    public void teststringify() {
        JAXBStringifier<JAXBmar> bdfObj = null;
        try {
            bdfObj = new JAXBStringifier<JAXBmar>( qname, new Class[] {this.getClass()});
            bdfObj = new JAXBStringifier<JAXBmar>(jumar);
            Mockito.doReturn(this.getClass()).when(jumar).marshal(logT, jumar, Mockito.mock(StringWriter.class), true);
            bdfObj.stringify(env, jumar, true);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("Test Exception"));
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void teststringifyWriter() {
        JAXBStringifier<JAXBmar> bdfObj = null;
        try {
            bdfObj = new JAXBStringifier<JAXBmar>(new Class[] {this.getClass()});
            bdfObj = new JAXBStringifier<JAXBmar>(jumar);
            Mockito.doReturn(this.getClass()).when(jumar).marshal(logT, jumar, Mockito.mock(StringWriter.class), true);
            bdfObj.stringify(env, jumar, Mockito.mock(StringWriter.class), true);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("Test Exception"));
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void teststringifyWriterException() {
        JAXBStringifier<JAXBmar> bdfObj = null;
        try {
            bdfObj = new JAXBStringifier<JAXBmar>(jumar);
            StringWriter sr = Mockito.mock(StringWriter.class);
            Mockito.doThrow(new JAXBException("Test Exception")).when(jumar).marshal(logT, jumar, sr, true);
            bdfObj.stringify(env, jumar, sr, true);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("Test Exception"));
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void teststringifyOs() {
        JAXBStringifier<JAXBmar> bdfObj = null;
        try {
            bdfObj = new JAXBStringifier<JAXBmar>(jumar);
            Mockito.doReturn(this.getClass()).when(jumar).marshal(logT, jumar, Mockito.mock(OutputStream.class), true);
            bdfObj.stringify(env, jumar, Mockito.mock(OutputStream.class), true);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("Test Exception"));
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void teststringifyOsException() {
        JAXBStringifier<JAXBmar> bdfObj = null;
        try {
            bdfObj = new JAXBStringifier<JAXBmar>(jumar);
            OutputStream os = Mockito.mock(OutputStream.class);
            Mockito.doThrow(new JAXBException("Test Exception")).when(jumar).marshal(logT, jumar, os, true);
            bdfObj.stringify(env, jumar, os, true);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("Test Exception"));
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testEmptyMethods() {
        JAXBStringifier<JAXBmar> bdfObj = null;
        try {
            bdfObj = new JAXBStringifier<JAXBmar>(jumar);
            bdfObj.servicePrestart(env);
            bdfObj.threadPrestart(env);
            bdfObj.threadDestroy(env);
            bdfObj.serviceDestroy(env);
            bdfObj.refresh(env);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("Test Exception"));
        }

    }

    @Test
    public void testPretty() {
        JAXBStringifier<JAXBmar> bdfObj = null;
        bdfObj = new JAXBStringifier<JAXBmar>(jumar);
        Mockito.doReturn(jumar).when(jumar).pretty(true);
        Object retVal = bdfObj.pretty(true);
        assertTrue(retVal instanceof JAXBStringifier);
    }

    @Test
    public void testNewInstanceException() {
        JAXBStringifier<JAXBmar> bdfObj = null;
        bdfObj = new JAXBStringifier<JAXBmar>(jumar);
        Mockito.doReturn(jumar).when(jumar).asFragment(true);
        Object retVal = bdfObj.asFragment(true);
        assertTrue(retVal instanceof JAXBStringifier);

    }
}
