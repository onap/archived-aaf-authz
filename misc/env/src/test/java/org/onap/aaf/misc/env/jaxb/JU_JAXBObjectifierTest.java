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
import java.io.StringReader;

import javax.xml.bind.JAXBException;
import javax.xml.validation.Schema;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

public class JU_JAXBObjectifierTest {

    @Mock
    JAXBumar jumar;

    @Mock
    Schema schema;

    @Mock
    Env env;

    TimeTaken tt,ttObjectify;

    LogTarget logT;

    @Before
    public void setUp() {
        initMocks(this);
        tt=Mockito.mock(TimeTaken.class);
        Mockito.doReturn(tt).when(env).start("JAXB Unmarshal", Env.XML);
        Mockito.doNothing().when(tt).done();
        logT = Mockito.mock(LogTarget.class);
        Mockito.doReturn(logT).when(env).debug();
    }

    @Test
    public void testObjectify() {
        JAXBObjectifier<?> bdfObj = null;
        try {
            bdfObj = new JAXBObjectifier( schema, new Class[] {this.getClass()});
            bdfObj = new JAXBObjectifier(jumar);
            Mockito.doReturn(this.getClass()).when(jumar).unmarshal(logT, "test");
            bdfObj.objectify(env, "test");
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("Test Exception"));
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testObjectifyException() {
        JAXBObjectifier<?> bdfObj = null;
        try {
            bdfObj = new JAXBObjectifier(jumar);
            Mockito.doThrow(new JAXBException("Test Exception")).when(jumar).unmarshal(logT, "test");
            bdfObj.objectify(env, "test");
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("Test Exception"));
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testObjectifyRdr() {
        JAXBObjectifier<?> bdfObj = null;
        try {
            bdfObj = new JAXBObjectifier(new Class[] {this.getClass()});
            bdfObj = new JAXBObjectifier(jumar);
            Mockito.doReturn(this.getClass()).when(jumar).unmarshal(logT, Mockito.mock(StringReader.class));
            bdfObj.objectify(env, Mockito.mock(StringReader.class));
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("Test Exception"));
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testObjectifyRdrException() {
        JAXBObjectifier<?> bdfObj = null;
        try {
            bdfObj = new JAXBObjectifier(jumar);
            StringReader sr = Mockito.mock(StringReader.class);
            Mockito.doThrow(new JAXBException("Test Exception")).when(jumar).unmarshal(logT, sr);
            bdfObj.objectify(env, sr);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("Test Exception"));
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testObjectifyIs() {
        JAXBObjectifier<?> bdfObj = null;
        try {
            bdfObj = new JAXBObjectifier(jumar);
            Mockito.doReturn(this.getClass()).when(jumar).unmarshal(logT, Mockito.mock(InputStream.class));
            bdfObj.objectify(env, Mockito.mock(InputStream.class));
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("Test Exception"));
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testObjectifyIsException() {
        JAXBObjectifier<?> bdfObj = null;
        try {
            bdfObj = new JAXBObjectifier(jumar);
            InputStream sr = Mockito.mock(InputStream.class);
            Mockito.doThrow(new JAXBException("Test Exception")).when(jumar).unmarshal(logT, sr);
            bdfObj.objectify(env, sr);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("Test Exception"));
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Test
    public void testEmptyMethods() {
        JAXBObjectifier<?> bdfObj = null;
        try {
            bdfObj = new JAXBObjectifier(jumar);
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
    public void testNewInstance() {
        JAXBObjectifier<?> bdfObj = null;
        try {
            bdfObj = new JAXBObjectifier(jumar);
            Object retVal = bdfObj.newInstance();
            Mockito.doThrow(new IllegalAccessException("Test Exception")).when(jumar).newInstance();

        } catch (IllegalAccessException e) {
            assertEquals("Test Exception", e.getLocalizedMessage());
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("Test Exception"));
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testNewInstanceException() {
        JAXBObjectifier<?> bdfObj = null;
        try {
            bdfObj = new JAXBObjectifier(jumar);
            Mockito.doThrow(new IllegalAccessException("Test Exception")).when(jumar).newInstance();
            Object retVal = bdfObj.newInstance();
        } catch (IllegalAccessException e) {
            assertEquals("Test Exception", e.getLocalizedMessage());
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("Test Exception"));
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
