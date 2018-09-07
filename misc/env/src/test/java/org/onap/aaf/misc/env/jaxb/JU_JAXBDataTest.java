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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.EnvJAXB;
import org.onap.aaf.misc.env.IOStringifier;
import org.onap.aaf.misc.env.old.Objectifier;
import org.onap.aaf.misc.env.old.Stringifier;

public class JU_JAXBDataTest {

    @Mock
    private Objectifier<String> objfr;

    private String object = "Text";

    @Mock
    private Stringifier<String> strfr;

    @Mock
    private IOStringifier<String> ioStrfr;

    @Mock
    private JAXBDF<String> df;

    @Mock
    private Env env;

    @Mock
    private Class<String> typeClass;

    @Mock
    private OutputStream os;

    @Mock
    private Writer writer;

    @Mock
    private EnvJAXB env1;

    @Before
    public void setUp() throws Exception {
        writer = mock(Writer.class);
        os = mock(OutputStream.class);
        strfr = mock(Stringifier.class);
        ioStrfr = mock(IOStringifier.class);
        objfr = mock(Objectifier.class);
        env1 = mock(EnvJAXB.class);
    }

    @Test
    public void testJAXBDataEnv() throws APIException, IOException {
        JAXBData<String> jaxb = new JAXBData<String>(env, df, strfr, objfr, object, typeClass);

        when(objfr.objectify(env, object)).thenReturn("String1");

        jaxb.to(os);
        jaxb.to(writer);

        verify(writer).write(object);
        verify(os).write(object.getBytes());

        assertEquals(jaxb.asString(), object);
        assertEquals(jaxb.asString(null), object);
        assertEquals(jaxb.toString(), object);
        assertEquals(jaxb.getTypeClass(), typeClass);
        assertEquals(jaxb.out(null), jaxb);
        assertEquals(jaxb.in(null), jaxb);
        assertTrue(jaxb.getInputStream() instanceof ByteArrayInputStream);
        assertEquals(jaxb.asObject(), "String1");
        assertEquals(jaxb.asObject(env1), "String1");
        assertEquals(jaxb.toString(), object);
    }

    @Test
    public void testJAXBDataEnvForObjectifier() throws APIException, IOException {
        JAXBData<String> jaxb = new JAXBData<String>(env, df, strfr, objfr, object, typeClass);

        when(objfr.objectify(env1, object)).thenReturn("String1");

        assertEquals(jaxb.asObject(env1), "String1");
    }

    @Test
    public void testJAXBDataEnvWithObject() throws APIException, IOException {
        JAXBData<String> jaxb = new JAXBData<String>(env, df, strfr, objfr, object);

        when(strfr.stringify(env, object, new boolean[] { false, false })).thenReturn(object);

        jaxb.to(os);

        verify(os).write(object.getBytes());

        assertEquals(jaxb.asString(), object);
        assertEquals(jaxb.asString(null), object);
        assertEquals(jaxb.toString(), object);
    }

    @Test
    public void testJAXBDataEnvForWriter() throws APIException, IOException {
        JAXBData<String> jaxb = new JAXBData<String>(env, df, strfr, objfr, object);

        when(strfr.stringify(env, object, new boolean[] { false, false })).thenReturn(object);

        jaxb.to(writer);

        verify(writer).write(object);

        assertEquals(jaxb.asString(), object);
        assertEquals(jaxb.asString(null), object);
        assertEquals(jaxb.toString(), object);
        assertEquals(jaxb.asObject(), object);
        assertEquals(jaxb.asObject(null), object);
    }

    @Test
    public void testAsStringWithNullString() throws APIException, IOException {
        JAXBData<String> jaxb = new JAXBData<String>(env, df, strfr, objfr, object);

        when(strfr.stringify(env, object, new boolean[] { false, false })).thenReturn(object);

        assertEquals(jaxb.asString(), object);
    }

    @Test
    public void testAsStringWithNullStringWithEnv() throws APIException, IOException {
        JAXBData<String> jaxb = new JAXBData<String>(env, df, strfr, objfr, object);

        when(strfr.stringify(env1, object)).thenReturn(object);

        assertEquals(jaxb.asString(env1), object);
    }

    @Test
    public void testToWithIOStrifier() throws APIException, IOException {
        JAXBData<String> jaxb = new JAXBData<String>(env, df, strfr, objfr, object);

        jaxb.option(0);

        when(strfr.stringify(env1, object)).thenReturn(object);
        when(strfr.stringify(env, object, new boolean[] { false, false })).thenReturn(object);

        assertTrue(jaxb.getInputStream() instanceof ByteArrayInputStream);
        assertEquals(jaxb.asString(env1), object);
    }
}
