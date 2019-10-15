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

package org.onap.aaf.misc.rosetta.env;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.jaxb.JAXBmar;
import org.onap.aaf.misc.env.jaxb.JAXBumar;
import org.onap.aaf.misc.rosetta.InXML;
import org.onap.aaf.misc.rosetta.Marshal;
import org.onap.aaf.misc.rosetta.Out;
import org.onap.aaf.misc.rosetta.OutXML;
import org.onap.aaf.misc.rosetta.ParseException;
import org.onap.aaf.misc.rosetta.Saved;

public class JU_RosettaData {

    @Mock
    Env env;

    @Mock
    RosettaDF df;

    @Mock
    JAXBmar bmar;

    @Mock
    Saved saved;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void testLoad() {
        RosettaData rosettaObj = new RosettaData(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        rosettaObj.setSaved(saved);
        StringReader sr= Mockito.mock(StringReader.class);
        try {
            InXML inxml = Mockito.mock(InXML.class);
            Mockito.doReturn(inxml).when(df).getIn(Data.TYPE.XML);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(inxml).start(env);
            Mockito.doNothing().when(saved).extract(sr,(Writer)null,inxml);

            rosettaObj = rosettaObj.load(sr);
        } catch (APIException | IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadException() {
        RosettaData rosettaObj = new RosettaData(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        try {
            InXML inxml = Mockito.mock(InXML.class);
            Mockito.doReturn(inxml).when(df).getIn(Data.TYPE.XML);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(inxml).start(env);
            rosettaObj = rosettaObj.load(Mockito.mock(StringReader.class));
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("NullPointerException"));
        }
    }

    @Test
    public void testLoadIs() {
        RosettaData rosettaObj = new RosettaData(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        rosettaObj.setSaved(saved);
        InputStream sr= Mockito.mock(InputStream.class);
        try {
            InXML inxml = Mockito.mock(InXML.class);
            Mockito.doReturn(inxml).when(df).getIn(Data.TYPE.XML);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(inxml).start(env);
            Mockito.doNothing().when(saved).extract(new InputStreamReader(sr),(Writer)null,inxml);

            rosettaObj = rosettaObj.load(sr);
        } catch (APIException | IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadIsException() {
        RosettaData rosettaObj = new RosettaData(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        try {
            InXML inxml = Mockito.mock(InXML.class);
            Mockito.doReturn(inxml).when(df).getIn(Data.TYPE.XML);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(inxml).start(env);
            rosettaObj = rosettaObj.load(Mockito.mock(InputStream.class));
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("NullPointerException"));
        }
    }

    @Test
    public void testLoadStr() {
        RosettaData rosettaObj = new RosettaData(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        rosettaObj.setSaved(saved);
        try {
            InXML inxml = Mockito.mock(InXML.class);
            Mockito.doReturn(inxml).when(df).getIn(Data.TYPE.XML);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(inxml).start(env);
            Mockito.doNothing().when(saved).extract(new StringReader("test"),(Writer)null,inxml);

            rosettaObj = rosettaObj.load("test");
        } catch (APIException | IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void testLoadStrJson() {
        RosettaData rosettaObj = new RosettaData(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.JSON);
        rosettaObj.setSaved(saved);
        try {
            InXML inxml = Mockito.mock(InXML.class);
            Mockito.doReturn(inxml).when(df).getIn(Data.TYPE.JSON);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(inxml).start(env);
            Mockito.doNothing().when(saved).extract(new StringReader("test"),(Writer)null,inxml);

            rosettaObj = rosettaObj.load("test");
        } catch (APIException | IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void testLoadStrException() {
        RosettaData rosettaObj = new RosettaData(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        try {
            InXML inxml = Mockito.mock(InXML.class);
            Mockito.doReturn(inxml).when(df).getIn(Data.TYPE.XML);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(inxml).start(env);
            rosettaObj = rosettaObj.load("test");
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("NullPointerException"));
        }
    }

    @Test
    public void testLoadT() {
        RosettaData rosettaObj = new RosettaData(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        rosettaObj.setSaved(saved);
        try {
            InXML inxml = Mockito.mock(InXML.class);
            Mockito.doReturn(inxml).when(df).getIn(Data.TYPE.XML);
            df.jaxMar = Mockito.mock(JAXBmar.class);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(inxml).start(env);
            Mockito.doNothing().when(saved).extract(new StringReader("test"),(Writer)null,inxml);

            rosettaObj = rosettaObj.load(bmar);
            df.marshal = null;
            rosettaObj = rosettaObj.load(bmar);
        } catch (APIException | IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadTMarshalNull() {
        RosettaData rosettaObj = new RosettaData(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        rosettaObj.setSaved(saved);
        try {
            InXML inxml = Mockito.mock(InXML.class);
            Mockito.doReturn(inxml).when(df).getIn(Data.TYPE.XML);
            df.jaxMar = Mockito.mock(JAXBmar.class);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(inxml).start(env);
            Mockito.doNothing().when(saved).extract(new StringReader("test"),(Writer)null,inxml);
            df.marshal = Mockito.mock(Marshal.class);;
            rosettaObj = rosettaObj.load(bmar);
        } catch (APIException | IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testLoadTException() {
        RosettaData rosettaObj = new RosettaData(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        try {
            InXML inxml = Mockito.mock(InXML.class);
            Mockito.doReturn(inxml).when(df).getIn(Data.TYPE.XML);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(inxml).start(env);
            rosettaObj = rosettaObj.load(bmar);
        } catch (APIException e) {
            assertTrue(e.getMessage().contains("NullPointerException"));
        }
    }

    @Test
    public void testGetEvents() {
        RosettaData rosettaObj = new RosettaData(env, df);
        Saved saved = rosettaObj.getEvents();
        assertEquals("Rosetta Saved", saved.logName());
    }

    @Test
    public void testAsObject() {
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        rosettaObj.setSaved(saved);
        try {
            Out outxml = Mockito.mock(OutXML.class);
            Mockito.doReturn(outxml).when(df).getOut(Data.TYPE.XML);
            Mockito.doReturn(Env.XML).when(df).logType(Data.TYPE.XML);
            Mockito.doReturn("test").when(outxml).logName();
            df.jaxMar = Mockito.mock(JAXBmar.class);
            df.jaxUmar = Mockito.mock(JAXBumar.class);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",0);
            LogTarget lt = Mockito.mock(LogTarget.class);
            Mockito.doReturn( lt).when(env).debug();
            Mockito.doNothing().when(saved).extract(null,new StringWriter(),saved);
            Mockito.doReturn(bmar).when(df.jaxUmar).unmarshal(lt,"");

            bmar = rosettaObj.asObject();

        } catch (APIException | IOException | ParseException | JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testAsObjectException() {
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        rosettaObj.setSaved(saved);
        try {
            Out outxml = Mockito.mock(OutXML.class);
            Mockito.doReturn(outxml).when(df).getOut(Data.TYPE.XML);
            Mockito.doReturn(Env.XML).when(df).logType(Data.TYPE.XML);
            Mockito.doReturn("test").when(outxml).logName();
            df.jaxMar = Mockito.mock(JAXBmar.class);
            df.jaxUmar = Mockito.mock(JAXBumar.class);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",0);
            LogTarget lt = Mockito.mock(LogTarget.class);
            Mockito.doReturn( lt).when(env).debug();
            Mockito.doThrow(new IOException("test Exception")).when(saved).extract(null,new StringWriter(),saved);
            Mockito.doThrow(new JAXBException("test Exception")).when(df.jaxUmar).unmarshal(lt,"");

            bmar = rosettaObj.asObject();

        } catch (APIException | IOException | ParseException | JAXBException e) {
            assertTrue(e.getMessage().contains("test Exception"));
        }
    }

    @Test
    public void testAsString() {
        TYPE type = TYPE.XML;
        Mockito.doReturn(type).when(df).getOutType();
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        rosettaObj.setSaved(saved);
        try {
            Out outxml = Mockito.mock(OutXML.class);

            Mockito.doReturn(outxml).when(df).getOut(type);
            Mockito.doReturn(Env.XML).when(df).logType(Data.TYPE.XML);
            Mockito.doReturn("test").when(outxml).logName();
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",2);

            String retVal = rosettaObj.asString();
            assertTrue("".equals(retVal));
        } catch (APIException  e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testAsStringJson() {
        TYPE type = TYPE.JSON;
        Mockito.doReturn(type).when(df).getOutType();
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.JSON);
        rosettaObj.setSaved(saved);
        try {
            Out outxml = Mockito.mock(OutXML.class);

            Mockito.doReturn(outxml).when(df).getOut(type);
            Mockito.doReturn(Env.JSON).when(df).logType(Data.TYPE.JSON);
            Mockito.doReturn("test").when(outxml).logName();
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",4);

            String retVal = rosettaObj.asString();
            assertTrue("".equals(retVal));
        } catch (APIException  e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void testToXml() {
        TYPE type = TYPE.XML;
        Mockito.doReturn(type).when(df).getOutType();
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        rosettaObj.setSaved(saved);

        try {
            InXML inxml = Mockito.mock(InXML.class);
            Mockito.doReturn(inxml).when(df).getIn(Data.TYPE.XML);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(inxml).start(env);
            Mockito.doNothing().when(saved).extract(new StringReader("test"),(Writer)null,inxml);

            rosettaObj = rosettaObj.load("test");
        } catch (APIException | IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            Out outxml = Mockito.mock(OutXML.class);

            Mockito.doReturn(outxml).when(df).getOut(type);
            Mockito.doReturn(Env.XML).when(df).logType(Data.TYPE.XML);
            Mockito.doReturn("test").when(outxml).logName();
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",2);
            RosettaData<JAXBmar> retVal = rosettaObj.to(Mockito.mock(OutputStream.class));

        } catch (APIException | IOException  e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testToJson() {
        TYPE type = TYPE.JSON;
        Mockito.doReturn(type).when(df).getOutType();
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.JSON);
        rosettaObj.setSaved(saved);

        try {
            InXML inxml = Mockito.mock(InXML.class);
            Mockito.doReturn(inxml).when(df).getIn(Data.TYPE.JSON);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(inxml).start(env);
            Mockito.doNothing().when(saved).extract(new StringReader("test"),(Writer)null,inxml);

            rosettaObj = rosettaObj.load("test");
        } catch (APIException | IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            Out outxml = Mockito.mock(OutXML.class);

            Mockito.doReturn(outxml).when(df).getOut(type);
            Mockito.doReturn(Env.JSON).when(df).logType(Data.TYPE.JSON);
            Mockito.doReturn("test").when(outxml).logName();
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",4);
            RosettaData<JAXBmar> retVal = rosettaObj.to(Mockito.mock(OutputStream.class));

        } catch (APIException | IOException  e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testTo() {
        TYPE type = TYPE.XML;
        Mockito.doReturn(type).when(df).getOutType();
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        rosettaObj.setSaved(saved);

        try {
            Out outxml = Mockito.mock(OutXML.class);

            Mockito.doReturn(outxml).when(df).getOut(type);
            Mockito.doReturn(Env.XML).when(df).logType(Data.TYPE.XML);
            Mockito.doReturn("test").when(outxml).logName();
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",2);
            RosettaData<JAXBmar> retVal = rosettaObj.to(Mockito.mock(OutputStream.class));

        } catch (APIException | IOException  e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testToWriterXml() {
        TYPE type = TYPE.XML;
        Mockito.doReturn(type).when(df).getOutType();
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        rosettaObj.setSaved(saved);

        try {
            InXML inxml = Mockito.mock(InXML.class);
            Mockito.doReturn(inxml).when(df).getIn(Data.TYPE.XML);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(inxml).start(env);
            Mockito.doNothing().when(saved).extract(new StringReader("test"),(Writer)null,inxml);

            rosettaObj = rosettaObj.load("test");
        } catch (APIException | IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            Out outxml = Mockito.mock(OutXML.class);

            Mockito.doReturn(outxml).when(df).getOut(type);
            Mockito.doReturn(Env.XML).when(df).logType(Data.TYPE.XML);
            Mockito.doReturn("test").when(outxml).logName();
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",2);
            RosettaData<JAXBmar> retVal = rosettaObj.to(Mockito.mock(StringWriter.class));

        } catch (APIException | IOException  e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testToWriterJson() {
        TYPE type = TYPE.JSON;
        Mockito.doReturn(type).when(df).getOutType();
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.JSON);
        rosettaObj.setSaved(saved);

        try {
            InXML inxml = Mockito.mock(InXML.class);
            Mockito.doReturn(inxml).when(df).getIn(Data.TYPE.JSON);
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(inxml).start(env);
            Mockito.doNothing().when(saved).extract(new StringReader("test"),(Writer)null,inxml);

            rosettaObj = rosettaObj.load("test");
        } catch (APIException | IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            Out outxml = Mockito.mock(OutXML.class);

            Mockito.doReturn(outxml).when(df).getOut(type);
            Mockito.doReturn(Env.JSON).when(df).logType(Data.TYPE.JSON);
            Mockito.doReturn("test").when(outxml).logName();
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",4);
            RosettaData<JAXBmar> retVal = rosettaObj.to(Mockito.mock(StringWriter.class));

        } catch (APIException | IOException  e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testToWriter() {
        TYPE type = TYPE.XML;
        Mockito.doReturn(type).when(df).getOutType();
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        rosettaObj = rosettaObj.in(Data.TYPE.XML);
        rosettaObj.setSaved(saved);

        try {
            Out outxml = Mockito.mock(OutXML.class);

            Mockito.doReturn(outxml).when(df).getOut(type);
            Mockito.doReturn(Env.XML).when(df).logType(Data.TYPE.XML);
            Mockito.doReturn("test").when(outxml).logName();
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",2);
            RosettaData<JAXBmar> retVal = rosettaObj.to(Mockito.mock(StringWriter.class));

        } catch (APIException | IOException  e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testGetTypeClass() {
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        rosettaObj.getTypeClass();
    }

    @Test
    public void testDirect() {
        TYPE type = TYPE.XML;
        Mockito.doReturn(type).when(df).getOutType();
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        try {
            Out outxml = Mockito.mock(OutXML.class);
            Mockito.doReturn(outxml).when(df).getOut(type);
            Mockito.doReturn(Env.XML).when(df).logType(Data.TYPE.XML);
            Mockito.doReturn("test").when(outxml).logName();
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",2);
            rosettaObj.direct(Mockito.mock(InputStream.class), Mockito.mock(OutputStream.class));
        } catch (APIException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testDirectException() {
        TYPE type = TYPE.XML;
        Mockito.doReturn(type).when(df).getOutType();
        Mockito.doReturn(type).when(df).getInType();
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        InXML inxml = null;

        inxml = Mockito.mock(InXML.class);
        Mockito.doReturn(inxml).when(df).getIn(type);


        StringReader is = Mockito.mock(StringReader.class);
        StringWriter os= Mockito.mock(StringWriter.class);
        try {
            Out outxml = Mockito.mock(OutXML.class);
            Mockito.doReturn(outxml).when(df).getOut(type);
            Mockito.doReturn(Env.XML).when(df).logType(Data.TYPE.XML);
            Mockito.doReturn("test").when(outxml).logName();
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",2);
            Mockito.doThrow( new IOException("testException")).when(outxml).extract(is, os, inxml, true);

            rosettaObj.direct(is,os, true);
        } catch (APIException | IOException | ParseException e) {
            // TODO Auto-generated catch block
            assertTrue(e.getMessage().contains("testException"));
        }
    }

    @Test
    public void testDirectT() {
        TYPE type = TYPE.XML;
        Mockito.doReturn(type).when(df).getOutType();
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        try {
            Out outxml = Mockito.mock(OutXML.class);
            Mockito.doReturn(outxml).when(df).getOut(type);
            Mockito.doReturn(Env.XML).when(df).logType(Data.TYPE.XML);
            Mockito.doReturn("test").when(outxml).logName();
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",2);

            df.jaxMar = Mockito.mock(JAXBmar.class);
            LogTarget lt = Mockito.mock(LogTarget.class);
            Mockito.doReturn( lt).when(env).debug();
            Mockito.doReturn(bmar).when(df.jaxMar).marshal(lt,bmar, new StringWriter(),true);

            rosettaObj.direct(bmar, Mockito.mock(StringWriter.class), true);
            df.marshal =Mockito.mock(Marshal.class);;
            rosettaObj.direct(bmar, Mockito.mock(StringWriter.class), true);
        } catch (APIException | IOException | JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testDirectTOS() {
        TYPE type = TYPE.XML;
        Mockito.doReturn(type).when(df).getOutType();
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        try {
            Out outxml = Mockito.mock(OutXML.class);
            Mockito.doReturn(outxml).when(df).getOut(type);
            Mockito.doReturn(Env.XML).when(df).logType(Data.TYPE.XML);
            Mockito.doReturn("test").when(outxml).logName();
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",2);

            df.jaxMar = Mockito.mock(JAXBmar.class);
            LogTarget lt = Mockito.mock(LogTarget.class);
            Mockito.doReturn( lt).when(env).debug();
            Mockito.doReturn(bmar).when(df.jaxMar).marshal(lt,bmar, new StringWriter(),true);

            rosettaObj.direct(bmar, Mockito.mock(OutputStream.class), true);
            df.marshal =Mockito.mock(Marshal.class);;
            rosettaObj.direct(bmar, Mockito.mock(OutputStream.class), true);
        } catch (APIException | IOException | JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testDirectTOSJson() {
        TYPE type = TYPE.JSON;
        Mockito.doReturn(type).when(df).getOutType();
        RosettaData<JAXBmar> rosettaObj = new RosettaData<JAXBmar>(env, df);
        try {
            Out outxml = Mockito.mock(OutXML.class);
            Mockito.doReturn(outxml).when(df).getOut(type);
            Mockito.doReturn(Env.JSON).when(df).logType(Data.TYPE.JSON);
            Mockito.doReturn("test").when(outxml).logName();
            Mockito.doReturn( Mockito.mock(TimeTaken.class)).when(env).start("test",4);

            df.jaxMar = Mockito.mock(JAXBmar.class);
            LogTarget lt = Mockito.mock(LogTarget.class);
            Mockito.doReturn( lt).when(env).debug();
            Mockito.doReturn(bmar).when(df.jaxMar).marshal(lt,bmar, new StringWriter(),true);

            rosettaObj.direct(bmar, Mockito.mock(OutputStream.class), true);
        } catch (APIException | IOException | JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
