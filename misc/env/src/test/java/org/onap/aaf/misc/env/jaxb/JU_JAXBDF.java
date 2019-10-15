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

package org.onap.aaf.misc.env.jaxb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.EnvJAXB;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;

public class JU_JAXBDF {

    @Mock
    EnvJAXB primaryEnv;

    @Mock
    JAXBumar jumar;

    @Mock
    JAXBmar jmar;

    @Mock
    Env env;

    TimeTaken tt,ttObjectify;

    @Before
    public void setUp() {
        initMocks(this);
        tt=Mockito.mock(TimeTaken.class);
        Mockito.doReturn(tt).when(env).start("JAXB Stringify", Env.XML);
        Mockito.doNothing().when(tt).done();
        ttObjectify=Mockito.mock(TimeTaken.class);
        Mockito.doReturn(ttObjectify).when(env).start("JAXB Objectify", Env.XML);
        Mockito.doNothing().when(ttObjectify).done();
    }

    @Test
    public void testNewInstance() {
        JAXBDF<?> bdfObj = null;
        try {
            bdfObj = new JAXBDF( null, new Class[] {this.getClass()});
            bdfObj.jumar = Mockito.mock(JAXBumar.class);
            Mockito.doThrow(new IllegalAccessException("Test Exception")).when(bdfObj.jumar).newInstance();
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

    @Test
    public void testNewInstanceNoException() {
        JAXBDF<?> bdfObj = null;
        try {
            bdfObj = new JAXBDF( null, new Class[] {this.getClass()});
            Object retVal = bdfObj.newInstance();
            assertTrue(retVal instanceof JU_JAXBDF);
        } catch (APIException e) {
            e.printStackTrace();
        } 
    
    }

    @Test
    public void testPrettyNoException() {
        JAXBDF<?> bdfObj = null;
        try {
            bdfObj = new JAXBDF( null, Mockito.mock(Schema.class), new Class[] {this.getClass()});
            Object retVal = bdfObj.pretty(true);
            assertTrue(retVal instanceof JAXBDF);
        } catch (APIException e) {
            e.printStackTrace();
        } 
    }

    @Test
    public void testFragment() {
        JAXBDF<?> bdfObj = null;
        try {
            bdfObj = new JAXBDF( null, Mockito.mock(QName.class), new Class[] {this.getClass()});
            Object retVal = bdfObj.asFragment(true);
            assertTrue(retVal instanceof JAXBDF);
            bdfObj.servicePrestart(null);
            bdfObj.threadPrestart(null);
            bdfObj.refresh(null);
            bdfObj.threadDestroy(null);
            bdfObj.serviceDestroy(null);
        } catch (APIException e) {
            e.printStackTrace();
        } 
    
    }

    @Test
    public void testNewData() {
        JAXBDF<?> bdfObj = null;
        try {
            bdfObj = new JAXBDF( null, Mockito.mock(Schema.class),Mockito.mock(QName.class), new Class[] {this.getClass()});
            Data<?> retVal = bdfObj.newData();
            assertTrue(retVal instanceof JAXBData);
        } catch (APIException e) {
            e.printStackTrace();
        } 
    }

    @Test
    public void testNewDataENV() {
        JAXBDF<?> bdfObj = null;
        try {
            bdfObj = new JAXBDF( null, Mockito.mock(Schema.class),Mockito.mock(QName.class), new Class[] {this.getClass()});
            Data<?> retVal = bdfObj.newData(Mockito.mock(Env.class));
            assertTrue(retVal instanceof JAXBData);
        } catch (APIException e) {
            e.printStackTrace();
        } 
    }

    @Test
    public void testNewDataType() {
        JAXBDF<JAXBumar> bdfObj = null;
        try {
            bdfObj = new JAXBDF( null, Mockito.mock(Schema.class),Mockito.mock(QName.class), new Class[] {this.getClass()});
            Data<?> retVal = bdfObj.newData(new JAXBumar(new Class[] {this.getClass()}));
            assertTrue(retVal instanceof JAXBData);
        } catch (APIException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

    @Test
    public void testNewDataStream() {
        JAXBDF<?> bdfObj = null;
        try {
            bdfObj = new JAXBDF( null, Mockito.mock(Schema.class),Mockito.mock(QName.class), new Class[] {this.getClass()});
            bdfObj.jumar =  Mockito.mock(JAXBumar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            InputStream is = Mockito.mock(InputStream.class);
            Mockito.doReturn(this.getClass()).when(bdfObj.jumar).unmarshal(logT, is);
            Data<?> retVal = bdfObj.newDataFromStream(env, is);
            assertTrue(retVal instanceof JAXBData);
        } catch (APIException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

    @Test
    public void testNewDataStreamException() {
        JAXBDF<?> bdfObj = null;
        try {
            bdfObj = new JAXBDF( null, Mockito.mock(Schema.class),Mockito.mock(QName.class), new Class[] {this.getClass()});
            bdfObj.jumar =  Mockito.mock(JAXBumar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            InputStream is = Mockito.mock(InputStream.class);
            Mockito.doThrow(new JAXBException("test")).when(bdfObj.jumar).unmarshal(logT, is);
            Data<?> retVal = bdfObj.newDataFromStream(env, is);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            assertTrue(e.getMessage().contains("test"));
        } 
    }

    @Test
    public void testNewDataFromString() {
        JAXBDF<?> bdfObj = null;
        try {
            bdfObj = new JAXBDF( null, Mockito.mock(Schema.class),Mockito.mock(QName.class), new Class[] {this.getClass()});
            Data<?> retVal = bdfObj.newDataFromString("test");
            assertTrue(retVal instanceof JAXBData);
        } catch (APIException e) {
            e.printStackTrace();
        } 
    }

    @Test
    public void testStringify() {
        JAXBDF<JAXBmar> bdfObj = null;
        try {
            JAXBmar typeObj = new JAXBmar(new Class[] {this.getClass()});
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF( envJaxb, new Class[] {this.getClass()});
            bdfObj.jmar =  Mockito.mock(JAXBmar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(envJaxb).debug();
            Mockito.doReturn(this.getClass()).when(bdfObj.jmar).marshal(logT, typeObj, Mockito.mock(StringWriter.class));
            String retVal = bdfObj.stringify(typeObj);
            assertEquals("", retVal);
        } catch (APIException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

    @Test
    public void testStringifyException() {
        JAXBDF<JAXBmar> bdfObj = null;
        try {
            JAXBmar typeObj = new JAXBmar(new Class[] {this.getClass()});
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(envJaxb).debug();
            bdfObj = new JAXBDF<JAXBmar>( envJaxb, new Class[] {this.getClass()});
            bdfObj.jmar =  Mockito.mock(JAXBmar.class);
            Mockito.doThrow(new JAXBException("test") ).when(bdfObj.jmar).marshal(logT, typeObj, Mockito.mock(StringWriter.class));
            String retVal = bdfObj.stringify(typeObj);
            System.out.println(retVal);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    }

    @Test
    public void testStringifyWriter() {
        JAXBDF<JAXBmar> bdfObj = null;
        try {
            JAXBmar typeObj = new JAXBmar(new Class[] {this.getClass()});
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF( envJaxb, new Class[] {this.getClass()});
            bdfObj.jmar =  Mockito.mock(JAXBmar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(envJaxb).debug();
            Mockito.doReturn(this.getClass()).when(bdfObj.jmar).marshal(logT, typeObj, Mockito.mock(StringWriter.class));
            bdfObj.stringify(typeObj, Mockito.mock(StringWriter.class));
        } catch (APIException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

    @Test
    public void testStringifyWriterException() {
        JAXBDF<JAXBmar> bdfObj = null;
        try {
            JAXBmar typeObj = new JAXBmar(new Class[] {this.getClass()});
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF( envJaxb, new Class[] {this.getClass()});
            bdfObj.jmar =  Mockito.mock(JAXBmar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(envJaxb).debug();
            StringWriter sw = Mockito.mock(StringWriter.class);
            Mockito.doThrow(new JAXBException("test")).when(bdfObj.jmar).marshal(logT, typeObj, sw);
            bdfObj.stringify(typeObj, sw);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    }

    @Test
    public void testStringifyOS() {
        JAXBDF<JAXBmar> bdfObj = null;
        try {
            JAXBmar typeObj = new JAXBmar(new Class[] {this.getClass()});
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF( envJaxb, new Class[] {this.getClass()});
            bdfObj.jmar =  Mockito.mock(JAXBmar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(envJaxb).debug();
            Mockito.doReturn(this.getClass()).when(bdfObj.jmar).marshal(logT, typeObj, Mockito.mock(OutputStream.class));
            bdfObj.stringify(typeObj, Mockito.mock(OutputStream.class));
        } catch (APIException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

    @Test
    public void testStringifyOsException() {
        JAXBDF<JAXBmar> bdfObj = null;
        try {
            JAXBmar typeObj = new JAXBmar(new Class[] {this.getClass()});
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF( envJaxb, new Class[] {this.getClass()});
            bdfObj.jmar =  Mockito.mock(JAXBmar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(envJaxb).debug();
            OutputStream sw = Mockito.mock(OutputStream.class);
            Mockito.doThrow(new JAXBException("test")).when(bdfObj.jmar).marshal(logT, typeObj, sw);
            bdfObj.stringify(typeObj, sw);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    }

    @Test
    public void testStringifyOptions() {
        JAXBDF<JAXBmar> bdfObj = null;
        try {
            JAXBmar typeObj = new JAXBmar(new Class[] {this.getClass()});
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF( envJaxb, new Class[] {this.getClass()});
            bdfObj.jmar =  Mockito.mock(JAXBmar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(envJaxb).debug();
            Mockito.doReturn(this.getClass()).when(bdfObj.jmar).marshal(logT, typeObj, Mockito.mock(OutputStream.class));
            bdfObj.stringify(env, typeObj, true);
        } catch (APIException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

    @Test
    public void testStringifyOSOptions() {
        JAXBDF<JAXBmar> bdfObj = null;
        try {
            JAXBmar typeObj = new JAXBmar(new Class[] {this.getClass()});
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF( envJaxb, new Class[] {this.getClass()});
            bdfObj.jmar =  Mockito.mock(JAXBmar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            Mockito.doReturn(this.getClass()).when(bdfObj.jmar).marshal(logT, typeObj, Mockito.mock(OutputStream.class),true);
            bdfObj.stringify(env, typeObj, Mockito.mock(OutputStream.class),true);
        } catch (APIException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

    @Test
    public void testStringifyOsOptionsException() {
        JAXBDF<JAXBmar> bdfObj = null;
        try {
            JAXBmar typeObj = new JAXBmar(new Class[] {this.getClass()});
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF( envJaxb, new Class[] {this.getClass()});
            bdfObj.jmar =  Mockito.mock(JAXBmar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            OutputStream sw = Mockito.mock(OutputStream.class);
            Mockito.doThrow(new JAXBException("test")).when(bdfObj.jmar).marshal(logT, typeObj, sw,true);
            bdfObj.stringify(env, typeObj, sw,true);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    }
    @Test
    public void testStringifySWOptions() {
        JAXBDF<JAXBmar> bdfObj = null;
        try {
            JAXBmar typeObj = new JAXBmar(new Class[] {this.getClass()});
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF( envJaxb, new Class[] {this.getClass()});
            bdfObj.jmar =  Mockito.mock(JAXBmar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            Mockito.doReturn(this.getClass()).when(bdfObj.jmar).marshal(logT, typeObj, Mockito.mock(StringWriter.class),true);
            bdfObj.stringify(env, typeObj, Mockito.mock(StringWriter.class),true);
        } catch (APIException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

    @Test
    public void testStringifySWOptionsException() {
        JAXBDF<JAXBmar> bdfObj = null;
        try {
            JAXBmar typeObj = new JAXBmar(new Class[] {this.getClass()});
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF( envJaxb, new Class[] {this.getClass()});
            bdfObj.jmar =  Mockito.mock(JAXBmar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            StringWriter sw = Mockito.mock(StringWriter.class);
            Mockito.doThrow(new JAXBException("test")).when(bdfObj.jmar).marshal(logT, typeObj, sw,true);
            bdfObj.stringify(env, typeObj, sw,true);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    }
 
    @Test
    public void testObjectifyEnv() {
        JAXBDF<JAXBumar> bdfObj = null;
        try {
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF<JAXBumar>( envJaxb, new Class[] {this.getClass()});
            JAXBumar typeObj = new JAXBumar(new Class[] {this.getClass()});
            bdfObj.jumar =  Mockito.mock(JAXBumar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            Mockito.doReturn(this.getClass()).when(bdfObj.jumar).unmarshal(logT, Mockito.mock(StringReader.class));
        
            bdfObj.objectify(env, Mockito.mock(StringReader.class));
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    
    }
    @Test
    public void testObjectifyEnvException() {
        JAXBDF<JAXBumar> bdfObj = null;
        try {
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF<JAXBumar>( envJaxb, new Class[] {this.getClass()});
            JAXBumar typeObj = new JAXBumar(new Class[] {this.getClass()});
            bdfObj.jumar =  Mockito.mock(JAXBumar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            StringReader sr = Mockito.mock(StringReader.class);
            Mockito.doThrow(new JAXBException("test")).when(bdfObj.jumar).unmarshal(logT, sr);
        
            bdfObj.objectify(env, sr);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    
    }

    @Test
    public void testObjectifyRdr() {
        JAXBDF<JAXBumar> bdfObj = null;
        try {
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF<JAXBumar>( envJaxb, new Class[] {this.getClass()});
            JAXBumar typeObj = new JAXBumar(new Class[] {this.getClass()});
            bdfObj.jumar =  Mockito.mock(JAXBumar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            Mockito.doReturn(this.getClass()).when(bdfObj.jumar).unmarshal(logT, Mockito.mock(StringReader.class));
        
            bdfObj.objectify( Mockito.mock(StringReader.class));
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    
    }
    @Test
    public void testObjectifyRdrException() {
        JAXBDF<JAXBumar> bdfObj = null;
        try {
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF<JAXBumar>( envJaxb, new Class[] {this.getClass()});
            JAXBumar typeObj = new JAXBumar(new Class[] {this.getClass()});
            bdfObj.jumar =  Mockito.mock(JAXBumar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(envJaxb).debug();
            StringReader sr = Mockito.mock(StringReader.class);
            Mockito.doThrow(new JAXBException("test")).when(bdfObj.jumar).unmarshal(logT, sr);
        
            bdfObj.objectify(sr);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    
    }

    @Test
    public void testObjectifyEnvIS() {
        JAXBDF<JAXBumar> bdfObj = null;
        try {
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF<JAXBumar>( envJaxb, new Class[] {this.getClass()});
            JAXBumar typeObj = new JAXBumar(new Class[] {this.getClass()});
            bdfObj.jumar =  Mockito.mock(JAXBumar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            Mockito.doReturn(this.getClass()).when(bdfObj.jumar).unmarshal(logT, Mockito.mock(InputStream.class));
        
            bdfObj.objectify(env, Mockito.mock(InputStream.class));
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    
    }
    @Test
    public void testObjectifyEnvISException() {
        JAXBDF<JAXBumar> bdfObj = null;
        try {
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF<JAXBumar>( envJaxb, new Class[] {this.getClass()});
            JAXBumar typeObj = new JAXBumar(new Class[] {this.getClass()});
            bdfObj.jumar =  Mockito.mock(JAXBumar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            InputStream sr = Mockito.mock(InputStream.class);
            Mockito.doThrow(new JAXBException("test")).when(bdfObj.jumar).unmarshal(logT, sr);
        
            bdfObj.objectify(env, sr);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    
    }

    @Test
    public void testObjectifyIs() {
        JAXBDF<JAXBumar> bdfObj = null;
        try {
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF<JAXBumar>( envJaxb, new Class[] {this.getClass()});
            JAXBumar typeObj = new JAXBumar(new Class[] {this.getClass()});
            bdfObj.jumar =  Mockito.mock(JAXBumar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            Mockito.doReturn(this.getClass()).when(bdfObj.jumar).unmarshal(logT, Mockito.mock(InputStream.class));
        
            bdfObj.objectify( Mockito.mock(InputStream.class));
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    
    }
    @Test
    public void testObjectifyIsException() {
        JAXBDF<JAXBumar> bdfObj = null;
        try {
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF<JAXBumar>( envJaxb, new Class[] {this.getClass()});
            JAXBumar typeObj = new JAXBumar(new Class[] {this.getClass()});
            bdfObj.jumar =  Mockito.mock(JAXBumar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(envJaxb).debug();
            InputStream sr = Mockito.mock(InputStream.class);
            Mockito.doThrow(new JAXBException("test")).when(bdfObj.jumar).unmarshal(logT, sr);
        
            bdfObj.objectify(sr);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    
    }

    @Test
    public void testObjectifyEnvStr() {
        JAXBDF<JAXBumar> bdfObj = null;
        try {
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF<JAXBumar>( envJaxb, new Class[] {this.getClass()});
            JAXBumar typeObj = new JAXBumar(new Class[] {this.getClass()});
            bdfObj.jumar =  Mockito.mock(JAXBumar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            Mockito.doReturn(this.getClass()).when(bdfObj.jumar).unmarshal(logT, "test");
        
            bdfObj.objectify(env, "test");
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    
    }
    @Test
    public void testObjectifyEnvStrException() {
        JAXBDF<JAXBumar> bdfObj = null;
        try {
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF<JAXBumar>( envJaxb, new Class[] {this.getClass()});
            JAXBumar typeObj = new JAXBumar(new Class[] {this.getClass()});
            bdfObj.jumar =  Mockito.mock(JAXBumar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            InputStream sr = Mockito.mock(InputStream.class);
            Mockito.doThrow(new JAXBException("test")).when(bdfObj.jumar).unmarshal(logT, "test");
        
            bdfObj.objectify(env, "test");
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    
    }

    @Test
    public void testObjectifyStr() {
        JAXBDF<JAXBumar> bdfObj = null;
        try {
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF<JAXBumar>( envJaxb, new Class[] {this.getClass()});
            JAXBumar typeObj = new JAXBumar(new Class[] {this.getClass()});
            bdfObj.jumar =  Mockito.mock(JAXBumar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(env).debug();
            Mockito.doReturn(this.getClass()).when(bdfObj.jumar).unmarshal(logT, "test");
        
            bdfObj.objectify( "test");
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    
    }
    @Test
    public void testObjectifyStrException() {
        JAXBDF<JAXBumar> bdfObj = null;
        try {
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF<JAXBumar>( envJaxb, new Class[] {this.getClass()});
            JAXBumar typeObj = new JAXBumar(new Class[] {this.getClass()});
            bdfObj.jumar =  Mockito.mock(JAXBumar.class);
            LogTarget logT = Mockito.mock(LogTarget.class);
            Mockito.doReturn(logT).when(envJaxb).debug();
            InputStream sr = Mockito.mock(InputStream.class);
            Mockito.doThrow(new JAXBException("test")).when(bdfObj.jumar).unmarshal(logT, "test");
        
            bdfObj.objectify("test");
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } catch (JAXBException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    
    }

    @Test
    public void testTypeClass() {
        JAXBDF<JAXBumar> bdfObj = null;
        try {
            EnvJAXB envJaxb = Mockito.mock(EnvJAXB.class);
            bdfObj = new JAXBDF<JAXBumar>( envJaxb, new Class[] {this.getClass()});
        
            Object obj = bdfObj.getTypeClass();
            assertFalse(obj instanceof JU_JAXBDF);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("test"));
        } 
    
    }
}
